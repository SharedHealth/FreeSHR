package org.freeshr.validations.providerIdentifiers;

import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ImmunizationProviderIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean canValidate(Resource resource) {
        return (resource instanceof Immunization);
    }

    @Override
    protected List<Reference> getProviderReferences(Resource resource) {
        Immunization immunization = (Immunization) resource;
        ArrayList<Reference> practitioners = new ArrayList<>();
        for (Immunization.ImmunizationPractitionerComponent practitionerComponent : immunization.getPractitioner()) {
            practitioners.add(practitionerComponent.getActor());
        }
        return practitioners;
    }
}
