package org.freeshr.validations;

import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity;
import static org.freeshr.domain.ErrorMessageBuilder.*;

@Component
public class ConditionValidator implements Validator<AtomEntry<? extends Resource>> {
    private static final String CODEABLE_CONCEPT = "CodeableConcept";

    public static final String DIAGNOSIS = "Diagnosis";
    public static final String CATEGORY = "category";



    @Override
    public List<ValidationMessage> validate(ValidationSubject<AtomEntry<? extends Resource>> subject) {
        AtomEntry<? extends Resource> atomEntry = subject.extract();
        ArrayList<ValidationMessage> validationMessages = new ArrayList<>();
        for (Property property : atomEntry.getResource().children()) {

            if (verifyIfPropertyIsARelatedItem(validationMessages, property, atomEntry.getId())) continue;
            checkCodeableConcept(validationMessages, property, atomEntry);
        }
        return validationMessages;

    }

    private boolean verifyIfPropertyIsARelatedItem(List<ValidationMessage> validationMessages, Property property,
                                                   String id) {
        if (!property.getName().equals("relatedItem") || !(property.hasValues())) return false;

        Condition.ConditionRelationshipType relatedItem = ((Condition.ConditionRelatedItemComponent) property
                .getValues().get(0)).getTypeSimple();
        Condition.ConditionRelationshipTypeEnumFactory conditionRelationshipTypeEnumFactory = new Condition
                .ConditionRelationshipTypeEnumFactory();
        try {
            if (conditionRelationshipTypeEnumFactory.toCode(relatedItem).equals("?")) {
                validationMessages.add(buildValidationMessage(id, ResourceValidator.INVALID, UNKNOWN_CONDITION_RELATION_CODE, IssueSeverity.error));
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

    protected void checkCodeableConcept(List<ValidationMessage> validationMessages, Property property,
                                        AtomEntry<? extends Resource> atomEntry) {
        if (!property.getTypeCode().equals(CODEABLE_CONCEPT) || !property.hasValues() || skipCheckForThisTypeOfEntry
                (atomEntry))
            return;

        boolean bothSystemAndCodePresent = bothSystemAndCodePresent(property);
        if (bothSystemAndCodePresent) return;

        String errorMessage = (((CodeableConcept) property.getValues().get(0)).getCoding()).get(0).getDisplaySimple();
        ValidationMessage validationMessage = new ValidationMessage(null, ResourceValidator.CODE_UNKNOWN,
                atomEntry.getId(), errorMessage, IssueSeverity.error);
        validationMessages.add(validationMessage);
    }

    private boolean bothSystemAndCodePresent(Property property) {
        boolean bothSystemAndCodePresent = false;
        List<Coding> codings = ((CodeableConcept) property.getValues().get(0)).getCoding();
        for (Coding coding : codings) {
            bothSystemAndCodePresent |= (coding.getSystem() != null && coding.getCode() != null);
        }
        return bothSystemAndCodePresent;
    }

    private boolean checkMedicationPrescription(){

        return true;
    }

}
