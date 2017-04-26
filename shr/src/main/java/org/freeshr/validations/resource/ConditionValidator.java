package org.freeshr.validations.resource;

import org.apache.commons.lang3.StringUtils;
import org.freeshr.config.SHRProperties;
import org.freeshr.utils.CollectionUtils;
import org.freeshr.validations.Severity;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.SubResourceValidator;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.freeshr.utils.StringUtils.removeSuffix;

@Component
public class ConditionValidator implements SubResourceValidator {

    private static final String CONDITION_CATEGORY_VALUESET_NAME = "condition-category";
    private static final String DIAGNOSIS = "Diagnosis";
    private static final String CONDITION_CODE_CODING_LOCATION_FORMAT = "Bundle.entry[%s].resource.code.coding";
    private static final String CONDITION_CODE_NOT_PRESENT_MSG = "There must be a code in condition";
    private static final String CONDITION_DIAGNOSIS_NON_CODED_MSG = "There must be a Code from TR for Diagnosis";

    private SHRProperties shrProperties;

    @Autowired
    public ConditionValidator(SHRProperties shrProperties) {
        this.shrProperties = shrProperties;
    }

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
    public List<ShrValidationMessage> validate(Object resource, int entryIndex) {
        Condition condition = (Condition) resource;
        List<Coding> coding = condition.getCode().getCoding();
        if (CollectionUtils.isEmpty(coding)) {
            String location = String.format(CONDITION_CODE_CODING_LOCATION_FORMAT, entryIndex);
            return Arrays.asList(new ShrValidationMessage(Severity.ERROR, location,
                    "invalid", CONDITION_CODE_NOT_PRESENT_MSG));
        }
        Coding categoryCoding = condition.getCategoryFirstRep().getCodingFirstRep();

        String conditionCategoryValuesetUrl = getTRValueSetUrl(CONDITION_CATEGORY_VALUESET_NAME);
        if (!isDiagnosis(categoryCoding, conditionCategoryValuesetUrl)) {
            return Collections.emptyList();
        }
        if (!hasTRCoding(coding)) {
            String location = String.format(CONDITION_CODE_CODING_LOCATION_FORMAT, entryIndex);
            return Arrays.asList(new ShrValidationMessage(Severity.ERROR, location,
                    "invalid", CONDITION_DIAGNOSIS_NON_CODED_MSG));
        }
        return new ArrayList<>();
    }

    public static boolean hasTRCoding(List<Coding> codings) {
        for (Coding coding : codings) {
            if (StringUtils.isNotBlank(coding.getSystem()) && coding.getSystem().contains("/tr/concepts/")) {
                if (StringUtils.isNotBlank(coding.getCode())) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean isDiagnosis(Coding categoryCoding, String conditionCategoryValuesetUrl) {
        return categoryCoding.getSystem().equals(conditionCategoryValuesetUrl) ||
                categoryCoding.getCode().equalsIgnoreCase(DIAGNOSIS);
    }

    private boolean isValidClinicalStatus(Condition condition, Condition.ConditionClinicalStatus[] values) {
        for (Condition.ConditionClinicalStatus value : values) {
            if (value.toCode().equals(condition.getClinicalStatus().toCode())) {
                return true;
            }
        }
        return false;
    }

    String getTRValueSetUrl(String code) {
        return removeSuffix(shrProperties.getTRLocationPath(), "/") + shrProperties.getTerminologiesContextPathForValueSet() + code;
    }

}
