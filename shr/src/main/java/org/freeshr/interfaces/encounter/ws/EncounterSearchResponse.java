package org.freeshr.interfaces.encounter.ws;

import org.freeshr.application.fhir.EncounterBundle;

import java.util.List;

public class EncounterSearchResponse {
    private String nextUrl;
    private String prevUrl;
    private List<EncounterBundle> results;

    public EncounterSearchResponse(String prevUrl, String nextUrl, List<EncounterBundle> results) {
        this.prevUrl = prevUrl;
        this.nextUrl = nextUrl;
        this.results = results;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public String getPrevUrl() {
        return prevUrl;
    }

    public List<EncounterBundle> getResults() {
        return results;
    }
}
