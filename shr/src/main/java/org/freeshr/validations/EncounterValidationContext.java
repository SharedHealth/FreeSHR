package org.freeshr.validations;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.hl7.fhir.instance.model.AtomFeed;

public class EncounterValidationContext {
    private EncounterBundle encounterBundle;
    private ResourceOrFeedDeserializer resourceOrFeedDeserializer;
    private AtomFeed feed;

    public EncounterValidationContext(EncounterBundle encounterBundle, ResourceOrFeedDeserializer resourceOrFeedDeserializer) {
        this.encounterBundle = encounterBundle;
        this.resourceOrFeedDeserializer = resourceOrFeedDeserializer;
    }

    public String getSourceXml() {
        return encounterBundle.getContent();
    }

    public AtomFeed getFeed() {
        if(feed != null) return feed;
        feed = resourceOrFeedDeserializer.deserialize(getSourceXml());
        return feed;
    }

    public String getHealthId() {
        return this.encounterBundle.getHealthId();
    }
}
