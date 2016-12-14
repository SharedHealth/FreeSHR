package org.freeshr.infrastructure.mci;

import ca.uhn.fhir.model.dstu2.composite.AddressDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.LinkTypeEnum;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.StringDt;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.utils.CollectionUtils;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.AsyncRestTemplate;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

import static org.freeshr.utils.HttpUtil.*;

@Component
public class MCIClient {

    private AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;
    private FhirFeedUtil fhirFeedUtil;
    private static String CONFIDENTIALITY_EXTENSION_URL = "https://sharedhealth.atlassian.net/wiki/display/docs/fhir-extensions#Confidentiality";
    private static String ADDRESS_CODE_EXTENSION_URL = "https://sharedhealth.atlassian.net/wiki/display/docs/fhir-extensions#AddressCode";
    private static final int ADDRESS_CODE_EACH_LEVEL_LENGTH = 2;

    @Autowired
    public MCIClient(@Qualifier("SHRRestTemplate") AsyncRestTemplate shrRestTemplate,
                     SHRProperties shrProperties, FhirFeedUtil fhirFeedUtil) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
        this.fhirFeedUtil = fhirFeedUtil;
    }

    public Observable<Patient> getPatient(final String healthId, UserInfo userInfo) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(AUTH_TOKEN_KEY, userInfo.getProperties().getAccessToken());
        headers.add(CLIENT_ID_KEY, userInfo.getProperties().getId());
        headers.add(FROM_KEY, userInfo.getProperties().getEmail());

        Observable<ResponseEntity<String>> responseEntityObservable = Observable.from(shrRestTemplate.exchange(
                StringUtils.ensureSuffix(shrProperties.getMCIPatientLocationPath(), "/") + healthId,
                HttpMethod.GET,
                new HttpEntity(headers),
                String.class));

        return responseEntityObservable.map(new Func1<ResponseEntity<String>, Patient>() {
            @Override
            public Patient call(ResponseEntity<String> response) {
                if (response.getStatusCode().is2xxSuccessful()) {
                    Patient patient = mapFhirBundleToPatient(response);
                    patient.setHealthId(healthId);
                    return patient;
                } else {
                    return null;
                }
            }
        });
    }

    private Patient mapFhirBundleToPatient(ResponseEntity<String> patientResponse) {
        Patient patient = new Patient();
        Bundle bundle = fhirFeedUtil.parseBundle(patientResponse.getBody(), "xml");

        ca.uhn.fhir.model.dstu2.resource.Patient fhirPatient = getPatientFromBundle(bundle);

        if (fhirPatient == null) return null;
        patient.setGender(findGender(fhirPatient));
        patient.setActive(fhirPatient.getActive());

        BooleanDt confidentiality = (BooleanDt) fhirPatient.getUndeclaredExtensionsByUrl(CONFIDENTIALITY_EXTENSION_URL).get(0).getValue();
        patient.setConfidentiality(confidentiality.getValue());

        List<ca.uhn.fhir.model.dstu2.resource.Patient.Link> replaceLinks = findReplaceLinks(fhirPatient);
        if (CollectionUtils.isNotEmpty(replaceLinks)) {
            String patientUrl = replaceLinks.get(0).getOther().getReference().getValue();
            patient.setMergedWith(org.apache.commons.lang3.StringUtils.substringAfterLast(patientUrl, "/"));
        }
        patient.setAddress(createAddressFromFhirPatient(fhirPatient));
        return patient;
    }

    private String findGender(ca.uhn.fhir.model.dstu2.resource.Patient fhirPatient) {
        if (fhirPatient.getGender().equals(AdministrativeGenderEnum.MALE.getCode())) return "M";
        if (fhirPatient.getGender().equals(AdministrativeGenderEnum.FEMALE.getCode())) return "F";
        if (fhirPatient.getGender().equals(AdministrativeGenderEnum.OTHER.getCode())) return "O";
        return null;
    }

    private Address createAddressFromFhirPatient(ca.uhn.fhir.model.dstu2.resource.Patient fhirPatient) {
        AddressDt addressDt = fhirPatient.getAddress().get(0);
        StringDt addressCode = (StringDt) addressDt.getUndeclaredExtensionsByUrl(ADDRESS_CODE_EXTENSION_URL).get(0).getValue();
        Iterable<String> codes = Splitter.fixedLength(ADDRESS_CODE_EACH_LEVEL_LENGTH).split(addressCode.getValue());
        List<String> addressLevels = Lists.newArrayList(codes);
        Address address = new Address();
        setDivision(address, addressLevels);
        setDistrict(address, addressLevels);
        setUpazila(address, addressLevels);
        setCityCorporation(address, addressLevels);
        setUnionWard(address, addressLevels);
        address.setLine(addressDt.getLineFirstRep().getValue());
        return address;
    }

    private List<ca.uhn.fhir.model.dstu2.resource.Patient.Link> findReplaceLinks(ca.uhn.fhir.model.dstu2.resource.Patient fhirPatient) {
        return CollectionUtils.filter(fhirPatient.getLink(), new CollectionUtils.Fn<ca.uhn.fhir.model.dstu2.resource.Patient.Link, Boolean>() {
            @Override
            public Boolean call(ca.uhn.fhir.model.dstu2.resource.Patient.Link link) {
                return link.getType().equals(LinkTypeEnum.REPLACE.getCode());
            }
        });
    }

    private ca.uhn.fhir.model.dstu2.resource.Patient getPatientFromBundle(Bundle bundle) {
        for (Bundle.Entry entry : bundle.getEntry()) {
            if (entry.getResource().getResourceName().equals(new ca.uhn.fhir.model.dstu2.resource.Patient().getResourceName()))
                return (ca.uhn.fhir.model.dstu2.resource.Patient) entry.getResource();
        }
        return null;
    }

    private void setUnionWard(Address address, List<String> addressLevels) {
        if (addressLevels.size() > 4) {
            address.setUnionOrUrbanWardId(addressLevels.get(4));
        }
    }

    private void setCityCorporation(Address address, List<String> addressLevels) {
        if (addressLevels.size() > 3) {
            address.setCityCorporation(addressLevels.get(3));
        }
    }

    private void setUpazila(Address address, List<String> addressLevels) {
        if (addressLevels.size() > 2) {
            address.setUpazila(addressLevels.get(2));
        }
    }

    private void setDistrict(Address address, List<String> addressLevels) {
        if (addressLevels.size() > 1) {
            address.setDistrict(addressLevels.get(1));
        }
    }

    private void setDivision(Address address, List<String> addressLevels) {
        if (addressLevels.size() > 0) {
            address.setDivision(addressLevels.get(0));
        }
    }


}
