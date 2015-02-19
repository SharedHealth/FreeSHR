package org.freeshr.validations;

import org.apache.log4j.Logger;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.service.FacilityService;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.domain.ErrorMessageBuilder.*;
import static org.freeshr.validations.ResourceValidator.INVALID;
import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity.error;

@Component
public class FacilityValidator implements Validator<AtomFeed> {

    private final Logger logger = Logger.getLogger(FacilityValidator.class);
    private SHRProperties shrProperties;
    private FacilityService facilityService;

    @Autowired
    public FacilityValidator(SHRProperties shrProperties, FacilityService facilityService) {
        this.shrProperties = shrProperties;
        this.facilityService = facilityService;
    }

    @Override
    public List<ValidationMessage> validate(ValidationSubject<AtomFeed> subject) {
        AtomFeed feed = subject.extract();
        List<ValidationMessage> validationMessages = new ArrayList<>();
        for (AtomEntry<? extends Resource> atomEntry : feed.getEntryList()) {
            Resource resource = atomEntry.getResource();
            if (!(resource instanceof Encounter)) {
                continue;
            }
            Encounter encounter = (Encounter) resource;
            ResourceReference serviceProvider = encounter.getServiceProvider();
            if (serviceProvider == null) {
                logger.info("Validating encounter as facility is not provided");
                return validationMessages;
            }
            String facilityUrl = serviceProvider.getReferenceSimple();
            if (facilityUrl.isEmpty() || !isValidFacilityUrl(facilityUrl)) {
                validationMessages.add(buildValidationMessage(atomEntry.getId(), INVALID, INVALID_FACILITY_URL, error));
                logger.info("Encounter failed for invalid facility URL");
                return validationMessages;
            }
            if (!checkIfValidFacility(facilityUrl)) {
                validationMessages.add(buildValidationMessage(atomEntry.getId(), INVALID, INVALID_FACILITY, error));
                return validationMessages;
            }
        }
        logger.info("Encounter validated for valid facility");
        return validationMessages;
    }

    private boolean checkIfValidFacility(String facilityUrl) {
        Observable<Facility> facilityObservable = facilityService.ensurePresent(extractFacilityId(facilityUrl));
        return facilityObservable.toBlocking().first() != null;
    }

    private String extractFacilityId(String referenceSimple) {
        return referenceSimple.substring(referenceSimple.lastIndexOf('/') + 1, referenceSimple.lastIndexOf('.')).trim();
    }

    private boolean isValidFacilityUrl(String referenceSimple) {
        String facilityRegistryUrl = shrProperties.getFacilityRegistryUrl();
        return referenceSimple.contains(facilityRegistryUrl);
    }
}
