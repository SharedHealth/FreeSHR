package org.freeshr.validations.ProviderSubResourceValidators;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DiagnosticReportPerformer extends SubResourceProvider {
    @Override
    boolean canHandle(Resource resource) {
        return (resource instanceof DiagnosticReport);
    }

    @Override
    List<String> extractUrls(Resource resource) {
        ResourceReference performer = ((DiagnosticReport) resource).getPerformer();
        String url = null;
        if (performer != null) {
            url = performer.getReferenceSimple() == null ? StringUtils.EMPTY : performer.getReferenceSimple();
        }
        return url == null ? null : Arrays.asList(url);
    }
}


