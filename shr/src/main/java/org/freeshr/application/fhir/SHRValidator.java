package org.freeshr.application.fhir;

import org.freeshr.utils.CollectionUtils;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.hl7.fhir.instance.formats.ParserBase;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;

import java.lang.Boolean;
import java.util.ArrayList;
import java.util.List;

import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity;

public class SHRValidator {

    private ResourceOrFeedDeserializer resourceOrFeedDeserializer;

    public static final String CHIEF_COMPLAINT = "Complaint";
    public static final String CODE = "code";
    public static final String CODING = "coding";
    public static final String CATEGORY = "category";
    public static final String SYSTEM = "system";

    public SHRValidator() {
        this.resourceOrFeedDeserializer = new ResourceOrFeedDeserializer();
    }

    public List<ValidationMessage> validateCategories(String sourceXml) {
        List<ValidationMessage> validationMessages = new ArrayList<>();
        ParserBase.ResourceOrFeed resourceOrFeed = resourceOrFeedDeserializer.deserialize(sourceXml);
        AtomFeed feed = resourceOrFeed.getFeed();
        List<AtomEntry<? extends Resource>> entryList = feed.getEntryList();
        for (AtomEntry<? extends Resource> atomEntry : entryList) {
            if (!bothSystemAndCodePresentInEntry(atomEntry)) {
                if (!entryIsChiefComplaint(atomEntry)) {
                    ValidationMessage validationMessage = new ValidationMessage();
                    validationMessage.setLevel(IssueSeverity.error);
                    validationMessage.setMessage("Noncoded entry");
                    validationMessage.setType("invalid");
                    validationMessages.add(validationMessage);
                }
            }
        }
        return validationMessages;
    }

    private boolean bothSystemAndCodePresentInEntry(AtomEntry<? extends Resource> atomEntry) {
        Property codeElement = atomEntry.getResource().getChildByName(CODE);
        boolean bothSystemAndCodePresent = false;
        if (codeElement != null) {
            if (!areAllValuesNull(codeElement.getValues())) {
                Property coding = getChildElement(codeElement, CODING);
                for (Element element : coding.getValues()) {
                    Property system = element.getChildByName(SYSTEM);
                    Property code = element.getChildByName(CODE);
                    bothSystemAndCodePresent |= ((system.getValues().get(0) != null) && (code.getValues().get(0) != null));
                }
                return bothSystemAndCodePresent;
            }
        }
        return true;
    }

    private boolean entryIsChiefComplaint(AtomEntry<? extends Resource> atomEntry) {
        Property category = atomEntry.getResource().getChildByName(CATEGORY);
        List<Element> coding = getChildElement(category, CODING).getValues();
        String code = ((Code) coding.get(0).getChildByName(CODE).getValues().get(0)).getValue();
        return code.equalsIgnoreCase(CHIEF_COMPLAINT);
    }

    private boolean areAllValuesNull(List<Element> values) {
        return CollectionUtils.isEvery(values, new CollectionUtils.Fn<Element, Boolean>() {
            @Override
            public Boolean call(Element input) {
                return input == null;
            }
        });
    }

    private Property getChildElement(Property codeElement, String name) {
        return codeElement.getValues().get(0).getChildByName(name);
    }
}
