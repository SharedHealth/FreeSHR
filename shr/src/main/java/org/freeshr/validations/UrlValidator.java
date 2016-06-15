package org.freeshr.validations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

@Component
public class UrlValidator {

    private static final Logger logger = LoggerFactory.getLogger(UrlValidator.class);

    public boolean isValid(final String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException ignored) {
            logger.error(String.format("%s is a Malformedurl", url));
        }
        return false;
    }
}
