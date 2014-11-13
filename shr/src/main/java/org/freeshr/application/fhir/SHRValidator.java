package org.freeshr.application.fhir;

import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.hl7.fhir.instance.formats.ParserBase;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;

import java.util.ArrayList;
import java.util.List;

import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity;

public class SHRValidator {

    public static final String SUBJECT = "subject";
    public static final String INVALID = "invalid";
    private ResourceOrFeedDeserializer resourceOrFeedDeserializer;

    public static final String CHIEF_COMPLAINT = "Complaint";
    public static final String CODE = "code";
    public static final String CODING = "coding";
    public static final String CATEGORY = "category";

    public SHRValidator() {
        this.resourceOrFeedDeserializer = new ResourceOrFeedDeserializer();
    }

    public List<ValidationMessage> validateCategories(String sourceXml) {
        List<ValidationMessage> validationMessages = new ArrayList<>();
        AtomFeed feed = getAtomFeed(sourceXml);
        List<AtomEntry<? extends Resource>> entryList = feed.getEntryList();
        for (AtomEntry<? extends Resource> atomEntry : entryList) {
            if (bothSystemAndCodePresentInEntry(atomEntry) || entryIsChiefComplaint(atomEntry)) continue;

            ValidationMessage validationMessage = new ValidationMessage(null, INVALID, "", "Noncoded entry", IssueSeverity.error);
            validationMessages.add(validationMessage);
        }
        return validationMessages;
    }

    public EncounterValidationResponse validateHealthId(String sourceXml, String expectedHealthId) {
        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        AtomFeed feed = getAtomFeed(sourceXml);
        for (AtomEntry<? extends Resource> atomEntry : feed.getEntryList()) {
            Property subject = atomEntry.getResource().getChildByName(SUBJECT);
            if (!subject.hasValues())  continue;

            String healthId = ((ResourceReference)subject.getValues().get(0)).getReferenceSimple();
            if (healthId.equalsIgnoreCase(expectedHealthId)) continue;

            Error error = new Error("healthId", INVALID, "Patient's Health Id does not match.");
            encounterValidationResponse.addError(error);
            return encounterValidationResponse;
        }

        return encounterValidationResponse;
    }

    private boolean bothSystemAndCodePresentInEntry(AtomEntry<? extends Resource> atomEntry) {
        Property codeElement = atomEntry.getResource().getChildByName(CODE);

        if (codeElement == null || !codeElement.hasValues()) return true;

        boolean bothSystemAndCodePresent = false;
        Property codingElement = getChildElement(codeElement, CODING);
        for (Element element : codingElement.getValues()) {
            Coding coding = (Coding) element;
            Uri system = coding.getSystem();
            Code code = coding.getCode();
            bothSystemAndCodePresent |= (system != null && code!= null);
        }
        return bothSystemAndCodePresent;
    }

    private AtomFeed getAtomFeed(String sourceXml) {
        ParserBase.ResourceOrFeed resourceOrFeed = resourceOrFeedDeserializer.deserialize(sourceXml);
        return resourceOrFeed.getFeed();
    }

    private boolean entryIsChiefComplaint(AtomEntry<? extends Resource> atomEntry) {
        Property category = atomEntry.getResource().getChildByName(CATEGORY);
        Coding coding = (Coding)getChildElement(category, CODING).getValues().get(0);
        Code code = coding.getCode();
        return code.getValue().equalsIgnoreCase(CHIEF_COMPLAINT);
    }

    private Property getChildElement(Property codeElement, String name) {
        return codeElement.getValues().get(0).getChildByName(name);
    }
}
