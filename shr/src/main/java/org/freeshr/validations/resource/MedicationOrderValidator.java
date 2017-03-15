package org.freeshr.validations.resource;


import ca.uhn.fhir.model.api.IDatatype;
import org.freeshr.application.fhir.TRConceptValidator;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.validations.Severity;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.SubResourceValidator;
import org.hl7.fhir.dstu3.hapi.validation.IValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.freeshr.validations.ValidationMessages.UNSPECIFIED_MEDICATION;

@Component
public class MedicationOrderValidator implements SubResourceValidator {

    private static final Logger logger = LoggerFactory.getLogger(MedicationOrderValidator.class);
    public static final String MEDICATION_ORDER_MEDICATION_LOCATION = "f:MedicationOrder/f:medication";
    public static final String MEDICATION_DOSE_INSTRUCTION_LOCATION = "f:MedicationOrder/f:dosageInstruction/f:dose";
    private static final String MEDICATION_ORDER_DISPENSE_MEDICATION_LOCATION = "f:MedicationOrder/f:dispenseRequest/f:medication";
    private static final String MEDICATION_ORDER_DISPENSE_QUANTITY_LOCATION = "f:MedicationOrder/f:dispenseRequest/f:quantity";

    private TRConceptValidator trConceptValidator;
    private DoseQuantityValidator doseQuantityValidator;
    private FhirFeedUtil fhirFeedUtil;

    @Autowired
    public MedicationOrderValidator(TRConceptValidator trConceptValidator,
                                    DoseQuantityValidator doseQuantityValidator, FhirFeedUtil fhirFeedUtil) {
        this.trConceptValidator = trConceptValidator;
        this.doseQuantityValidator = doseQuantityValidator;
        this.fhirFeedUtil = fhirFeedUtil;
    }

    @Override
    public boolean validates(Object resource) {
        return resource instanceof MedicationRequest;
    }

    @Override
    public List<ShrValidationMessage> validate(Object resource) {
        MedicationRequest medicationOrder = (MedicationRequest) resource;
        List<ShrValidationMessage> validationMessages = new ArrayList<>();

        validationMessages.addAll(validateMedication(medicationOrder));

        validationMessages.addAll(validateDosageInstructionDosageQuantity(medicationOrder));

        validationMessages.addAll(validateDispenseMedication(medicationOrder));

        validationMessages.addAll(validateDispenseQuantity(medicationOrder));
        return validationMessages;
    }

    private Collection<? extends ShrValidationMessage> validateDispenseMedication(MedicationRequest medicationOrder) {
        if (medicationOrder.getDispenseRequest() != null) {
            IDatatype medicine = medicationOrder.getMedication();
            if (medicine == null || !(medicine instanceof CodeableConcept)) {
                return new ArrayList<>();
            }

            CodeableConcept medicationCoding = ((CodeableConcept) medicine);

            return validateCodeableConcept(medicationCoding, MEDICATION_ORDER_DISPENSE_MEDICATION_LOCATION);
        }
        return new ArrayList<>();
    }

    private Collection<? extends ShrValidationMessage> validateDispenseQuantity(MedicationRequest medicationOrder) {
        SimpleQuantity dispenseQuantity = medicationOrder.getDispenseRequest().getQuantity();
        if (dispenseQuantity != null) {
            return validateQuantity(dispenseQuantity, MEDICATION_ORDER_DISPENSE_QUANTITY_LOCATION);
        }
        return null;
    }

    private Collection<? extends ShrValidationMessage> validateDosageInstructionDosageQuantity(MedicationRequest medicationOrder) {
        List<DosageInstruction> instructions = medicationOrder.getDosageInstruction();


        for (DosageInstruction instruction : instructions) {
            IDatatype dose = instruction.getDose();

            if (dose instanceof QuantityDt) {
                return validateQuantity((Quantity) dose, MEDICATION_DOSE_INSTRUCTION_LOCATION);
            }
        }
        return new ArrayList<>();
    }

    private List<ShrValidationMessage> validateQuantity(QuantityDt quantity, String location) {
        if (doseQuantityValidator.hasReferenceUrlAndCode(quantity)) {

            IValidationSupport.CodeValidationResult codeValidationResult = doseQuantityValidator.validate(quantity);
            if (!codeValidationResult.isOk()) {
                logger.error(String.format("Medication-Prescription:Encounter failed for %s", codeValidationResult.getMessage()));
                return Arrays.asList(
                        new ShrValidationMessage(Severity.ERROR, location, "invalid", codeValidationResult.getMessage()));
            }
        }
        return new ArrayList<>();
    }

    private Collection<? extends ShrValidationMessage> validateMedication(ca.uhn.fhir.model.dstu2.resource.MedicationOrder medicationOrder) {
        IDatatype medicine = medicationOrder.getMedication();
        if (!(medicine instanceof CodeableConceptDt)) {
            return new ArrayList<>();
        }

        CodeableConceptDt medicationCoding = ((CodeableConceptDt) medicine);
        if (medicationCoding.isEmpty()) {
            return Arrays.asList(new ShrValidationMessage(Severity.ERROR, MEDICATION_ORDER_MEDICATION_LOCATION, "invalid",
                    UNSPECIFIED_MEDICATION));
        }

        return validateCodeableConcept(medicationCoding, MEDICATION_ORDER_MEDICATION_LOCATION);
    }

    private Collection<? extends ShrValidationMessage> validateCodeableConcept(CodeableConceptDt medicationCoding, String location) {
        ArrayList<ShrValidationMessage> shrValidationMessages = new ArrayList<>();
        for (CodingDt codingDt : medicationCoding.getCoding()) {
            if (codingDt.getSystem() != null && codingDt.getCode() != null) {
                if (trConceptValidator.isCodeSystemSupported(fhirFeedUtil.getFhirContext(), codingDt.getSystem())) {
                    IValidationSupport.CodeValidationResult validationResult = trConceptValidator.validateCode(fhirFeedUtil.getFhirContext(), codingDt.getSystem(), codingDt.getCode(), codingDt.getDisplay());
                    if (validationResult != null && !validationResult.isOk()) {
                        logger.error(String.format("Medication-Order:Encounter failed for %s", validationResult.getMessage()));
                        shrValidationMessages.add(new ShrValidationMessage(Severity.ERROR, location, "invalid", validationResult.getMessage()));
                    }
                }
            }
        }
        return shrValidationMessages;
    }
}
