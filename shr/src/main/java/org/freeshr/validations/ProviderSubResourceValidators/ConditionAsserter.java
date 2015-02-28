package org.freeshr.validations.ProviderSubResourceValidators;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ConditionAsserter extends SubResourceProvider {

    @Override
    boolean canHandle(Resource resource) {
        return (resource instanceof Condition);
    }

    @Override
    List<String> extractUrls(Resource resource) {
        ResourceReference asserter = ((Condition) resource).getAsserter();
        String url = null;
        if (asserter != null) {
            url = asserter.getReferenceSimple() == null ? StringUtils.EMPTY : asserter.getReferenceSimple();
        }
        return url == null ? null : Arrays.asList(url);
    }
}
