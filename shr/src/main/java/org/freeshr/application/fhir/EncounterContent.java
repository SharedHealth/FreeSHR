package org.freeshr.application.fhir;

public class EncounterContent {

    private final String content;

    public EncounterContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content;
    }
}
