package org.freeshr.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Bundle {

    @JsonProperty("entry")
    private List<Entry> entries;

    public List<Entry> getEntries() {
        return entries;
    }
}
