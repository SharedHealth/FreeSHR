package org.freeshr.validations.providerIdentifiers;

import org.freeshr.config.SHRProperties;
import org.freeshr.utils.CollectionUtils;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ClinicalResourceProviderIdentifier {
    private static final String START_MARKER = "^";
    private static final String END_MARKER = "$";
    private static final String PROVIDER_ID_MATCHER = "([0-9]+)";
    private static final String JSON = ".json";

    protected abstract boolean validates(Resource resource);
    protected abstract List<String> extractUrls(Resource resource);

    public final boolean isValid(Resource resource, SHRProperties shrProperties) {
        if (!validates(resource))  return true;
        List<String> urls = extractUrls(resource);
        return validateUrlPattern(urls, shrProperties);
    }

    private boolean validateUrlPattern(List<String> urls, SHRProperties shrProperties) {
        if (CollectionUtils.isEmpty(urls)) return true;

        for (String url : urls) {
            if (!isUrlPatternMatched(url, shrProperties)) return false;
        }
        return true;
    }

    private boolean isUrlPatternMatched(String url, SHRProperties shrProperties) {
        String providerReferencePath = shrProperties.getProviderReferencePath();
        String regex = START_MARKER + providerReferencePath + PROVIDER_ID_MATCHER + JSON + END_MARKER;
        Matcher matcher = Pattern.compile(regex).matcher(url);
        return matcher.matches();
    }
}
