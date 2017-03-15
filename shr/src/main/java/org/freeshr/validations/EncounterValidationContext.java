package org.freeshr.validations;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.utils.FhirFeedUtil;
import org.hl7.fhir.dstu3.model.Bundle;

public class EncounterValidationContext {
    private EncounterBundle encounterBundle;
    private FhirFeedUtil fhirFeedUtil;
    private Bundle bundle;

    public EncounterValidationContext(EncounterBundle encounterBundle,
                                      FhirFeedUtil fhirFeedUtil) {
        this.encounterBundle = encounterBundle;
        this.fhirFeedUtil = fhirFeedUtil;
    }

//    @Deprecated
//    public org.hl7.fhir.instance.model.Bundle getFeed() {
//        return null;
//    }

    public String getHealthId() {
        return this.encounterBundle.getHealthId();
    }


//    @Deprecated
//    public ValidationSubject<org.hl7.fhir.instance.model.Bundle> feedFragment() {
//        return new ValidationSubject<org.hl7.fhir.instance.model.Bundle>() {
//            @Override
//            public org.hl7.fhir.instance.model.Bundle extract() {
//                return getFeed();
//            }
//        };
//    }

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
        bundle = fhirFeedUtil.parseBundle(encounterBundle.getContent(), "xml");
        return bundle;
    }
}
