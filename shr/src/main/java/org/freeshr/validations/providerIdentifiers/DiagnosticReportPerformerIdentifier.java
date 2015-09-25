package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DiagnosticReportPerformerIdentifier extends ClinicalResourceProviderIdentifier {
    @Override
    protected boolean validates(IResource resource) {
        return (resource instanceof DiagnosticReport);
    }

    @Override
    protected List<ResourceReferenceDt> getProviderReferences(IResource resource) {
        return Arrays.asList(((DiagnosticReport) resource).getPerformer());
    }
}


