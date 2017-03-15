package org.freeshr.validations.resource;

import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.SubResourceValidator;
import org.hl7.fhir.dstu3.model.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConditionValidator implements SubResourceValidator {

    private static final Logger logger = LoggerFactory.getLogger(ConditionValidator.class);
    private static final String CODEABLE_CONCEPT = "CodeableConcept";
    public static final String DIAGNOSIS = "Diagnosis";
    public static final String CATEGORY = "category";
    private static final String CONDITION_CLINICAL_STATUS_LOCATION = "/f:Bundle/f:entry/f:resource/f:Condition/f:clinicalStatus";
    private static final String UNKNOWN_CONDITION_CLINICAL_STATUS_MSG =
            "Coded value %s is not in value set http://hl7.org/fhir/ValueSet/condition-clinical (http://hl7.org/fhir/ValueSet/condition-clinical). Condition: %s";

//    @Override
//    public List<ValidationMessage> validate(ValidationSubject<Bundle.BundleEntryComponent> subject) {
//        Bundle.BundleEntryComponent atomEntry = subject.extract();
//        ArrayList<ValidationMessage> validationMessages = new ArrayList<>();
//        for (Property property : atomEntry.getResource().children()) {
//            if (isRelatedItem(property, atomEntry.getId(), validationMessages)) continue;
//            checkCodeableConcept(property, atomEntry, validationMessages);
//        }
//        return validationMessages;
//
//    }
//
//    private boolean isRelatedItem(Property property, String id, List<ValidationMessage> validationMessages) {
//        if (!property.getName().equals("relatedItem") || !(property.hasValues())) return false;
//
//        Condition.ConditionRelationshipType relatedItem = ((Condition.ConditionRelatedItemComponent) property
//                .getValues().get(0)).getTypeSimple();
//        Condition.ConditionRelationshipTypeEnumFactory conditionRelationshipTypeEnumFactory = new Condition
//                .ConditionRelationshipTypeEnumFactory();
//        try {
//            if (!conditionRelationshipTypeEnumFactory.toCode(relatedItem).equals("?")) return true;
//
//            validationMessages.add(new ValidationMessage(null, OperationOutcome.IssueType.INVALID, id,
//                    UNKNOWN_CONDITION_RELATION_CODE, IssueSeverity.ERROR));
//            logger.debug(String.format("Condition: Encounter failed for %s", UNKNOWN_CONDITION_RELATION_CODE));
//            return true;
//        } catch (Exception e) {
//            logger.debug(e.getMessage());
//        }
//        return true;
//    }
//
//    private boolean skipCheck(Bundle.BundleEntryComponent atomEntry) {
//        Property category = atomEntry.getResource().getChildByName(CATEGORY);
//        Coding coding = ((CodeableConcept) category.getValues().get(0)).getCoding().get(0);
//        return !coding.getDisplay().equalsIgnoreCase(DIAGNOSIS);
//    }
//
//    private void checkCodeableConcept(Property property, Bundle.BundleEntryComponent atomEntry,
//                                      List<ValidationMessage> validationMessages) {
//        if (!property.getTypeCode().equals(CODEABLE_CONCEPT) || !property.hasValues() || skipCheck
//                (atomEntry))
//            return;
//
//        if (bothSystemAndCodePresent(property)) return;
//
//        String errorMessage = (((CodeableConcept) property.getValues().get(0)).getCoding()).get(0).getDisplay();
//
//        ValidationMessage validationMessage = new ValidationMessage(null, OperationOutcome.IssueType.UNKNOWN,
//                atomEntry.getId(), errorMessage, IssueSeverity.ERROR);
//        validationMessages.add(validationMessage);
//    }
//
//    private boolean bothSystemAndCodePresent(Property property) {
//        boolean bothSystemAndCodePresent = false;
//        List<Coding> codings = ((CodeableConcept) property.getValues().get(0)).getCoding();
//        for (Coding coding : codings) {
//            bothSystemAndCodePresent |= (coding.getSystem() != null && coding.getCode() != null);
//        }
//        return bothSystemAndCodePresent;
//    }

    @Override
    public boolean validates(Object resource) {
        return (resource instanceof Condition);
    }

    @Override
    public List<ShrValidationMessage> validate(Object resource) {
//        Condition condition = (Condition) resource;
//        ConditionClinicalStatusCodesEnum[] values = ConditionClinicalStatusCodesEnum.values();
//        boolean validClinicalStatus = isValidClinicalStatus(condition, values);
//        if (!validClinicalStatus) {
//           return Arrays.asList(new ShrValidationMessage(Severity.ERROR, CONDITION_CLINICAL_STATUS_LOCATION,
//                   "invalid", String.format(UNKNOWN_CONDITION_CLINICAL_STATUS_MSG, condition.getClinicalStatus(), condition.getId().getValue())));
//        }
        return new ArrayList<>();
    }

    private boolean isValidClinicalStatus(Condition condition, Condition.ConditionClinicalStatus[] values) {
        for (Condition.ConditionClinicalStatus value : values) {
            if (value.toCode().equals(condition.getClinicalStatus().toCode())){
                return true;
            }
        }
        return false;
    }
}
