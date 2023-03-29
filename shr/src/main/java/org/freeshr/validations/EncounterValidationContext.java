package org.freeshr.validations;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.utils.FhirFeedUtil;
import org.hl7.fhir.dstu3.model.Bundle;
import java.util.Optional;

public class EncounterValidationContext {
    private EncounterBundle encounterBundle;
    private FhirFeedUtil fhirFeedUtil;
    private Bundle bundle;

    public EncounterValidationContext(EncounterBundle encounterBundle,
                                      FhirFeedUtil fhirFeedUtil) {
        this.encounterBundle = encounterBundle;
        this.fhirFeedUtil = fhirFeedUtil;
    }

    public String getHealthId() {
        return this.encounterBundle.getHealthId();
    }

    public ValidationSubject<Bundle> bundleFragment() {
        return new ValidationSubject<Bundle>() {
            @Override
            public Bundle extract() {
                return getBundle();
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

    public Bundle getBundle() {
        if (bundle != null) return bundle;

        String type = Optional.ofNullable(encounterBundle.getContentType()).orElse("xml");
        bundle = fhirFeedUtil.parseBundle(encounterBundle.getContent(), type);

        return bundle;
    }
}
