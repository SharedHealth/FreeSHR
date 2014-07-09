package org.freeshr.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Entry {

    private Composition content;

    public Composition getContent() {
        return content;
    }
}
