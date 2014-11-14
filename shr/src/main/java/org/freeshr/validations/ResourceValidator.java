package org.freeshr.validations;

import org.freeshr.application.fhir.*;
import org.freeshr.application.fhir.Error;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.hl7.fhir.instance.formats.ParserBase;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity;

@Component
public class ResourceValidator {

    public static final String INVALID = "invalid";
    public static final String CHIEF_COMPLAINT = "Complaint";
    public static final String CODE = "code";
    public static final String CODING = "coding";
    public static final String CATEGORY = "category";

    private ResourceOrFeedDeserializer resourceOrFeedDeserializer;

    public ResourceValidator() {
        this.resourceOrFeedDeserializer = new ResourceOrFeedDeserializer();
    }

    public List<ValidationMessage> validateCategories(String sourceXml) {
        List<ValidationMessage> validationMessages = new ArrayList<>();
        AtomFeed feed = resourceOrFeedDeserializer.deserialize(sourceXml);
        List<AtomEntry<? extends Resource>> entryList = feed.getEntryList();
        for (AtomEntry<? extends Resource> atomEntry : entryList) {
            if (bothSystemAndCodePresentInEntry(atomEntry) || entryIsChiefComplaint(atomEntry)) continue;

            ValidationMessage validationMessage = new ValidationMessage(null, INVALID, "", "Noncoded entry", IssueSeverity.error);
            validationMessages.add(validationMessage);
        }
        return validationMessages;
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
            bothSystemAndCodePresent |= (system != null && code != null);
        }
        return bothSystemAndCodePresent;
    }

    private boolean entryIsChiefComplaint(AtomEntry<? extends Resource> atomEntry) {
        Property category = atomEntry.getResource().getChildByName(CATEGORY);
        Coding coding = (Coding) getChildElement(category, CODING).getValues().get(0);
        Code code = coding.getCode();
        return code.getValue().equalsIgnoreCase(CHIEF_COMPLAINT);
    }

    private Property getChildElement(Property codeElement, String name) {
        return codeElement.getValues().get(0).getChildByName(name);
    }
}
