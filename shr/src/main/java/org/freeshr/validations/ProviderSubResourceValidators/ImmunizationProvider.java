package org.freeshr.validations.ProviderSubResourceValidators;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ImmunizationProvider extends SubResourceProvider {

    @Override
    boolean canHandle(Resource resource) {
        return (resource instanceof Immunization);
    }

    @Override
    List<String> extractUrls(Resource resource) {
        List<String> urls = new ArrayList<>();

        ResourceReference requester = ((Immunization) resource).getRequester();
        String requesterUrl = null;
        if (requester != null) {
            requesterUrl = requester.getReferenceSimple() == null ? StringUtils.EMPTY : requester.getReferenceSimple();
        }

        ResourceReference performer = ((Immunization) resource).getPerformer();
        String performerUrl = null;
        if (performer != null) {
            performerUrl = performer.getReferenceSimple() == null ? StringUtils.EMPTY : performer.getReferenceSimple();
        }

        if (requesterUrl != null) {
            urls.add(requesterUrl);
        }
        if (performerUrl != null) {
            urls.add(performerUrl);
        }
        return urls;
    }
}
