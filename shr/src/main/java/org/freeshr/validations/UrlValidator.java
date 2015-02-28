package org.freeshr.validations;

import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

@Component
public class UrlValidator {

    public boolean isValid(final String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException ignored) {
        }
        return false;
    }
}
