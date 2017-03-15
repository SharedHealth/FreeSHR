package org.freeshr.validations.providerIdentifiers;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiagnosticReportPerformerIdentifier extends ClinicalResourceProviderIdentifier {
    @Override
    protected boolean canValidate(Resource resource) {
        return (resource instanceof DiagnosticReport);
    }

    @Override
    protected List<Reference> getProviderReferences(Resource resource) {
        DiagnosticReport diagnosticReport = (DiagnosticReport) resource;
        ArrayList<Reference> practitioners = new ArrayList<>();
        for (DiagnosticReport.DiagnosticReportPerformerComponent performerComponent : diagnosticReport.getPerformer()) {
            practitioners.add(performerComponent.getActor());
        }
        return practitioners;
    }
}


