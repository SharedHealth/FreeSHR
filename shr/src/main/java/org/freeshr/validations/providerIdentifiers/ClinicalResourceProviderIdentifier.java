package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.config.SHRProperties;
import org.freeshr.infrastructure.ProviderRegistryClient;
import org.freeshr.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.freeshr.utils.StringUtils.ensureSuffix;

public abstract class ClinicalResourceProviderIdentifier {
    private static final String START_MARKER = "^";
    private static final String END_MARKER = "$";
    private static final String PROVIDER_ID_MATCHER = "([0-9]+)";
    private static final String JSON = ".json";

    @Autowired
    private ProviderRegistryClient providerRegistryClient;

    protected abstract boolean validates(IResource resource);

    protected abstract List<ResourceReferenceDt> getProviderReferences(IResource resource);

    public final boolean isValid(IResource resource, SHRProperties shrProperties) {
        if (!validates(resource)) return true;
        List<ResourceReferenceDt> refs = getProviderReferences(resource);
        if (!validateUrl(refs, shrProperties)) return false;
        return true;
    }

    private boolean validateUrl(List<ResourceReferenceDt> urls, SHRProperties shrProperties) {
        if (CollectionUtils.isEmpty(urls)) return true;

        for (ResourceReferenceDt ref : urls) {
            String refUrl = ref.getReference().getValue();
            if (StringUtils.isBlank(refUrl)) continue;
            if (!isUrlPatternMatched(refUrl, shrProperties)) return false;
            try {
                return providerRegistryClient.checkProvider(refUrl);
            } catch (Exception e) {
//            what to do?
            }
        }
        return false;
    }

    private boolean isUrlPatternMatched(String refUrl, SHRProperties shrProperties) {
        String providerReferencePath = shrProperties.getProviderReferencePath();
        String regex = START_MARKER + ensureSuffix(providerReferencePath, "/") + PROVIDER_ID_MATCHER + JSON + END_MARKER;
        Matcher matcher = Pattern.compile(regex).matcher(refUrl);
        return matcher.matches();
    }

}
