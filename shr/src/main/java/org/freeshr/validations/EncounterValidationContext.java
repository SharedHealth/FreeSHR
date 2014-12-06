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

    public AtomFeed getFeed() {
        //deserialize only once
        if(feed != null) return feed;
        feed = resourceOrFeedDeserializer.deserialize(encounterBundle.getContent());
        return feed;
    }

    public String getHealthId() {
        return this.encounterBundle.getHealthId();
    }


    public EncounterValidationFragment<AtomFeed> feedFragment() {
        return new EncounterValidationFragment<AtomFeed>() {
            @Override
            public AtomFeed extract() {
                return getFeed();
            }
        };
    }

    public EncounterValidationFragment<EncounterValidationContext> context() {
        return new EncounterValidationFragment<EncounterValidationContext>() {
            @Override
            public EncounterValidationContext extract() {
                return EncounterValidationContext.this;
            }
        };
    }

    public EncounterValidationFragment<String> sourceFragment() {
        return new EncounterValidationFragment<String>() {
            @Override
            public String extract() {
                return encounterBundle.getContent();
            }
        };
    }
}
