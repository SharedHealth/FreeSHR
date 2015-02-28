package org.freeshr.validations.ProviderSubResourceValidators;

import org.apache.commons.lang3.StringUtils;
import org.freeshr.utils.CollectionUtils;
import org.hl7.fhir.instance.model.Procedure;
import org.hl7.fhir.instance.model.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ProcedurePerformerValidator extends ProviderSubresourceValidator {

    @Override
    protected boolean validates(Resource resource) {
        return (resource instanceof Procedure);
    }

    @Override
    protected List<String> extractUrls(Resource resource) {
        List<Procedure.ProcedurePerformerComponent> performers = ((Procedure) resource).getPerformer();
        String url = null;
        if (!CollectionUtils.isEmpty(performers)) {
            url = performers.get(0).getPerson().getReferenceSimple();
            url = url == null ? StringUtils.EMPTY : url;
        }
        return url == null ? null : Arrays.asList(url);
    }
}



