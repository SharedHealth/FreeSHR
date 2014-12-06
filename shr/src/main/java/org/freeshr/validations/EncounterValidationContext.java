package org.freeshr.validations;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.hl7.fhir.instance.model.AtomFeed;

public class EncounterValidationContext {
    private EncounterBundle encounterBundle;
    private ResourceOrFeedDeserializer resourceOrFeedDeserializer;
    private AtomFeed feed;

    public EncounterValidationContext(EncounterBundle encounterBundle,
                                      ResourceOrFeedDeserializer resourceOrFeedDeserializer) {
        this.encounterBundle = encounterBundle;
        this.resourceOrFeedDeserializer = resourceOrFeedDeserializer;
    }

    public AtomFeed getFeed() {
        //deserialize only once
        if (feed != null) return feed;
        feed = resourceOrFeedDeserializer.deserialize(encounterBundle.getContent());
        return feed;
    }

    public String getHealthId() {
        return this.encounterBundle.getHealthId();
    }


    public ValidationSubject<AtomFeed> feedFragment() {
        return new ValidationSubject<AtomFeed>() {
            @Override
            public AtomFeed extract() {
                return getFeed();
            }
        };
    }

    public ValidationSubject<EncounterValidationContext> context() {
        return new ValidationSubject<EncounterValidationContext>() {
            @Override
            public EncounterValidationContext extract() {
                return EncounterValidationContext.this;
            }
        };
    }

    public ValidationSubject<String> sourceFragment() {
        return new ValidationSubject<String>() {
            @Override
            public String extract() {
                return encounterBundle.getContent();
            }
        };
    }
}
