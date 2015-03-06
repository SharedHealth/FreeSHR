package org.freeshr.validations.providerIdentifiers;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.MedicationPrescription;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MedicationPrescriberIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean validates(Resource resource) {
        return (resource instanceof MedicationPrescription);
    }

    @Override
    protected List<String> extractUrls(Resource resource) {
        ResourceReference prescriber = ((MedicationPrescription) resource).getPrescriber();
        String url = null;
        if (prescriber != null) {
            url = prescriber.getReferenceSimple() == null ? StringUtils.EMPTY : prescriber.getReferenceSimple();
        }
        return url == null ? null : Arrays.asList(url);
    }
}



