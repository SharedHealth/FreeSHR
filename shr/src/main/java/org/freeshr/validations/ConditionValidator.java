package org.freeshr.validations;

import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity;

@Component
public class ConditionValidator extends Validator {

    public static final String DIAGNOSIS = "Diagnosis";
    public static final String CATEGORY = "category";

    @Override
    public void validate(List<ValidationMessage> validationMessages, AtomEntry<? extends Resource> atomEntry) {

        for (Property property : atomEntry.getResource().children()) {
            if (verifyIfPropertyIsARelatedItem(validationMessages, property, atomEntry.getId())) continue;
            checkCodeableConcept(validationMessages, property, atomEntry);
        }

    }

    private boolean verifyIfPropertyIsARelatedItem(List<ValidationMessage> validationMessages, Property property, String id) {
        if (!property.getName().equals("relatedItem") || !(property.hasValues())) return false;

        Condition.ConditionRelationshipType relatedItem = ((Condition.ConditionRelatedItemComponent) property.getValues().get(0)).getTypeSimple();
        Condition.ConditionRelationshipTypeEnumFactory conditionRelationshipTypeEnumFactory = new Condition.ConditionRelationshipTypeEnumFactory();
        try {
            if (conditionRelationshipTypeEnumFactory.toCode(relatedItem).equals("?")) {
                validationMessages.add(new ValidationMessage(null, ResourceValidator.INVALID, id, "Unknown ConditionRelationshipType code", IssueSeverity.error));
            }
            return true;
        } catch (Exception e) {
            //Logically can never be thrown hence swallowing exception
            e.printStackTrace();
        }
        return true;
    }

    boolean skipCheckForThisTypeOfEntry(AtomEntry<? extends Resource> atomEntry) {
        Property category = atomEntry.getResource().getChildByName(CATEGORY);
        Coding coding = ((CodeableConcept) category.getValues().get(0)).getCoding().get(0);
        return !coding.getDisplaySimple().equalsIgnoreCase(DIAGNOSIS);
    }
}
