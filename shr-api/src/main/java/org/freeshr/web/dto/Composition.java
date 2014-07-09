package org.freeshr.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.hl7.fhir.instance.model.Encounter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Composition {

    private String date;

    @JsonProperty("section")
    @JsonDeserialize(using = EncounterDeserializer.class)
    private List<Encounter> sections;

    public String getDate() {
        return date;
    }

    public List<Encounter> getSections() {
        return sections;
    }
}
