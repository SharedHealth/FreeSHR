package org.freeshr.interfaces.encounter.ws;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.freeshr.application.fhir.EncounterBundle;

import java.util.List;

public class EncounterSearchResponse {
    @JsonProperty("feedUrl")
    private String requestUrl;
    private String author = "FreeSHR";
    private String title  = "Encounters";
    private String nextUrl;
    private String prevUrl;
    private List<EncounterBundle> entries;

    public EncounterSearchResponse(String requestUrl, List<EncounterBundle> entries) {
        this.requestUrl = requestUrl;
        this.entries = entries;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public String getPrevUrl() {
        return prevUrl;
    }

    public List<EncounterBundle> getEntries() {
        return entries;
    }

    public void setNavLinks(String prevResultUrl, String nextResultURL) {
        this.prevUrl = prevResultUrl;
        this.nextUrl = nextResultURL;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    @Override
    public String toString() {
        return "EncounterSearchResponse{" +
                "requestUrl='" + requestUrl + '\'' +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", nextUrl='" + nextUrl + '\'' +
                ", prevUrl='" + prevUrl + '\'' +
                ", entries=" + entries +
                '}';
    }
}
