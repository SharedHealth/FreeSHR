package org.freeshr.validations.ProviderSubResourceValidators;

import org.freeshr.config.SHRProperties;
import org.freeshr.utils.CollectionUtils;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SubResourceProvider {

    public static final String START_MARKER = "^";
    public static final String END_MARKER = "$";
    public static final String PROVIDER_ID_MATCHER = "([0-9]+)";
    public static final String JSON = ".json";


    abstract boolean canHandle(Resource resource);

    abstract List<String> extractUrls(Resource resource);

    public final boolean validateProvider(Resource resource, SHRProperties shrProperties) {
        if (!canHandle(resource)) {
            return true;
        }
        List<String> urls = extractUrls(resource);
        return validateUrlPattern(urls, shrProperties);
    }

    private boolean validateUrlPattern(List<String> urls, SHRProperties shrProperties) {
        if (CollectionUtils.isEmpty(urls)) {
            return true;
        }

        for (String url : urls) {
            if (!isUrlPatternMatched(url, shrProperties)) {
                return false;
            }
        }
        return true;
    }

    private boolean isUrlPatternMatched(String url, SHRProperties shrProperties) {
        String providerRegistryUrl = shrProperties.getProviderRegistryUrl();
        String regex = START_MARKER + providerRegistryUrl + PROVIDER_ID_MATCHER + JSON + END_MARKER;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }
}
