package org.freeshr.validations.ProviderSubResourceValidators;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.DiagnosticOrder;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DiagnosticOrdererValidator extends ProviderSubresourceValidator {

    @Override
    protected boolean validates(Resource resource) {
        return (resource instanceof DiagnosticOrder);
    }

    @Override
    protected List<String> extractUrls(Resource resource) {
        ResourceReference orderer = ((DiagnosticOrder) resource).getOrderer();
        String url = null;
        if (orderer != null) {
            url = orderer.getReferenceSimple() == null ? StringUtils.EMPTY : orderer.getReferenceSimple();
        }
        return url == null ? null : Arrays.asList(url);
    }
}


