package org.freeshr.validations.resource;


import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.infrastructure.tr.MedicationCodeValidator;
import org.freeshr.validations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.freeshr.validations.ValidationMessages.*;

@Component
public class MedicationPrescriptionValidator implements SubResourceValidator {

    public static final String DOSAGE_INSTRUCTION = "dosageInstruction";
    private static final String MEDICATION = "medication";
    private static final String DISPENSE = "dispense";
    private static final Logger logger = LoggerFactory.getLogger(MedicationPrescriptionValidator.class);
    public static final String ORDER_MEDICATION_LOCATION = "f:MedicationOrder/f:medication";
    public static final String MEDICATION_DOSE_INSTRUCTION_LOCATION = "f:MedicationOrder/f:dosageInstruction/f:dose";
    private MedicationCodeValidator medicationValidator;
    private DoseQuantityValidator doseQuantityValidator;
    private UrlValidator urlValidator;

    @Autowired
    public MedicationPrescriptionValidator(MedicationCodeValidator medicationValidator,
                                           DoseQuantityValidator doseQuantityValidator, UrlValidator urlValidator) {
        this.medicationValidator = medicationValidator;
        this.doseQuantityValidator = doseQuantityValidator;
        this.urlValidator = urlValidator;
    }

//    @Override
//    public List<ValidationMessage> validate(ValidationSubject<Bundle.BundleEntryComponent> subject) {
//
//        Bundle.BundleEntryComponent atomEntry = subject.extract();
//        List<ValidationMessage> validationMessages = new ArrayList<>();
//
//        validationMessages.addAll(validateMedication(atomEntry));
//        if (validationMessages.size() > 0) return validationMessages;
//
//        validationMessages.addAll(validateDosageQuantity(atomEntry));
//        if (validationMessages.size() > 0) return validationMessages;
//
//        validationMessages.addAll(validateDispenseMedication(atomEntry));
//        return validationMessages;
//    }

//    private List<ValidationMessage> validateDosageQuantity(Bundle.BundleEntryComponent atomEntry) {
//        Property dosageInstruction = atomEntry.getResource().getChildByName(DOSAGE_INSTRUCTION);
//        List<Base> dosageInstructionValues = dosageInstruction.getValues();
//        for (Base dosageInstructionValue : dosageInstructionValues) {
//            if (!(dosageInstructionValue instanceof MedicationOrder
//                    .MedicationOrderDosageInstructionComponent)) continue;
//
//            SimpleQuantity doseQuantity = null;
//            try {
//                doseQuantity = ((MedicationOrder.MedicationOrderDosageInstructionComponent)
//                        dosageInstructionValue).getDoseSimpleQuantity();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            if (doseQuantityValidator.isReferenceUrlNotFound(doseQuantity)) return new ArrayList<>();
//            if (!urlValidator.isValid(doseQuantity.getSystem())) {
//                logger.debug(String.format("Medication-Prescription:Encounter failed for %s", INVALID_DOSAGE_QUANTITY_REFERENCE));
//                return validationMessages(new ValidationMessage(null, OperationOutcome.IssueType.INVALID, atomEntry.getId(),
//                        INVALID_DOSAGE_QUANTITY_REFERENCE, ERROR));
//            }
//            if (doseQuantityValidator.validate(doseQuantity) != null) {
//                logger.debug(String.format("Medication-Prescription:Encounter failed for %s", INVALID_DOSAGE_QUANTITY));
//                return validationMessages(new ValidationMessage(null, OperationOutcome.IssueType.INVALID, atomEntry.getId(),
//                        INVALID_DOSAGE_QUANTITY, ERROR));
//            }
//
//        }
//        return new ArrayList<>();
//    }
//
//
//    private List<ValidationMessage> validateDispenseMedication(Bundle.BundleEntryComponent atomEntry) {
//        Property dispense = atomEntry.getResource().getChildByName(DISPENSE);
//        if (dispense == null || (!dispense.hasValues())) return new ArrayList<>();
//        Property dispenseMedication = dispense.getValues().get(0).getChildByName(MEDICATION);
//        if (dispenseMedication == null || !dispenseMedication.hasValues()) return new ArrayList<>();
//        String dispenseMedicationRefUrl = getReferenceUrl(dispenseMedication);
//        if ((dispenseMedicationRefUrl == null)) return new ArrayList<>();
//        if (!urlValidator.isValid(dispenseMedicationRefUrl))
//            return validationMessages(new ValidationMessage(null, OperationOutcome.IssueType.INVALID, atomEntry.getId(),
//                    INVALID_DISPENSE_MEDICATION_REFERENCE_URL, ERROR));
//        if (!isValidCodeableConceptUrl(dispenseMedicationRefUrl, ""))
//            return validationMessages(new ValidationMessage(null, OperationOutcome.IssueType.INVALID, atomEntry.getId(),
//                    INVALID_DISPENSE_MEDICATION_REFERENCE_URL, ERROR));
//        return new ArrayList<>();
//    }
//
//
//    private List<ValidationMessage> validateMedication(Bundle.BundleEntryComponent atomEntry) {
//        Property medication = atomEntry.getResource().getChildByName(MEDICATION);
//        if ((medication == null) || (!medication.hasValues())) {
//            logger.debug(String.format("Medication-Prescription:Encounter failed for %s", UNSPECIFIED_MEDICATION));
//            return validationMessages(new ValidationMessage(null, OperationOutcome.IssueType.INVALID, atomEntry.getId(),
//                    UNSPECIFIED_MEDICATION,
//                    ERROR));
//        }
//        String medicationRefUrl = getReferenceUrl(medication);
//        if ((medicationRefUrl == null)) return new ArrayList<>();
//        if (!urlValidator.isValid(medicationRefUrl)) {
//            logger.debug(String.format("Medication-Prescription:Encounter failed for %s", INVALID_MEDICATION_REFERENCE_URL));
//            return validationMessages(new ValidationMessage(null, OperationOutcome.IssueType.INVALID, atomEntry.getId(),
//                    INVALID_MEDICATION_REFERENCE_URL, ERROR));
//        }
//        if (!isValidCodeableConceptUrl(medicationRefUrl, ""))
//            return validationMessages(new ValidationMessage(null, OperationOutcome.IssueType.INVALID, atomEntry.getId(),
//                    INVALID_MEDICATION_REFERENCE_URL, ERROR));
//        return new ArrayList<>();
//    }

//    private List<ValidationMessage> validationMessages(ValidationMessage message) {
//        List<ValidationMessage> validationMessages = new ArrayList<>();
//        validationMessages.add(message);
//        return validationMessages;
//    }

    private boolean isValidCodeableConceptUrl(String url, String code) {
        return medicationValidator.validate(url, code);
    }

//    private String getReferenceUrl(Property medication) {
//        Base element = medication.getValues().get(0);
//        return element instanceof Reference ? ((Reference) element).getReference() : null;
//    }


    @Override
    public boolean validates(Object resource) {
        return resource instanceof ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
    }

    @Override
    public List<ShrValidationMessage> validate(Object resource) {
        ca.uhn.fhir.model.dstu2.resource.MedicationOrder medicationOrder = (ca.uhn.fhir.model.dstu2.resource.MedicationOrder) resource;
        List<ShrValidationMessage> validationMessages = new ArrayList<>();

        validationMessages.addAll(validateMedication(medicationOrder));
        if (validationMessages.size() > 0) return validationMessages;

        validationMessages.addAll(validateDosageQuantity(medicationOrder));
        if (validationMessages.size() > 0) return validationMessages;

        validationMessages.addAll(validateDispenseMedication(medicationOrder));
        return validationMessages;
    }

    private Collection<? extends ShrValidationMessage> validateDispenseMedication(ca.uhn.fhir.model.dstu2.resource.MedicationOrder medicationOrder) {
        ca.uhn.fhir.model.dstu2.resource.MedicationOrder.DispenseRequest dispenseRequest = medicationOrder.getDispenseRequest();
        if (dispenseRequest.isEmpty()) return new ArrayList<>();
        IDatatype dispenseMedication = dispenseRequest.getMedication();
        //TODO check for null
        if (!(dispenseMedication instanceof ResourceReferenceDt)) return new ArrayList<>();
        ResourceReferenceDt dispenseMedicationRef = (ResourceReferenceDt) dispenseMedication;
        String dispenseMedicationRefUrl = dispenseMedicationRef.getReference().getValue();
        if (StringUtils.isBlank(dispenseMedicationRefUrl)) return new ArrayList<>();
        if (!urlValidator.isValid(dispenseMedicationRefUrl)) {
            logger.debug(String.format("MedicationOrder:Encounter failed for %s", INVALID_DISPENSE_MEDICATION_REFERENCE_URL));
            return Arrays.asList(
                    new ShrValidationMessage(Severity.ERROR, "f:MedicationOrder/f:dispenseRequest/f:medication", "invalid",
                            INVALID_DISPENSE_MEDICATION_REFERENCE_URL));
        }
        if (!isValidCodeableConceptUrl(dispenseMedicationRefUrl, "")) {
            logger.debug(String.format("MedicationOrder:Encounter failed for %s", INVALID_DISPENSE_MEDICATION_REFERENCE_URL));
            return Arrays.asList(
                    new ShrValidationMessage(Severity.ERROR, "f:MedicationOrder/f:dispenseRequest/f:medication", "invalid",
                            INVALID_DISPENSE_MEDICATION_REFERENCE_URL));
        }
        return new ArrayList<>();
    }

    private Collection<? extends ShrValidationMessage> validateDosageQuantity(ca.uhn.fhir.model.dstu2.resource.MedicationOrder medicationOrder) {
        List<ca.uhn.fhir.model.dstu2.resource.MedicationOrder.DosageInstruction> instructions = medicationOrder.getDosageInstruction();


        for (ca.uhn.fhir.model.dstu2.resource.MedicationOrder.DosageInstruction instruction : instructions) {
            IDatatype dose = instruction.getDose();

            if (dose instanceof QuantityDt) {
                QuantityDt doseQuantity = (QuantityDt) dose;
                if (doseQuantityValidator.isReferenceUrlNotFound(doseQuantity)) return new ArrayList<>();

                if (!urlValidator.isValid(doseQuantity.getSystem())) {
                    logger.debug(String.format("Medication-Prescription:Encounter failed for %s", INVALID_DOSAGE_QUANTITY_REFERENCE));
                    return Arrays.asList(
                       new ShrValidationMessage(Severity.ERROR, MEDICATION_DOSE_INSTRUCTION_LOCATION, "invalid", INVALID_DOSAGE_QUANTITY_REFERENCE));
                }
                if (doseQuantityValidator.validate(doseQuantity) != null) {
                    logger.debug(String.format("Medication-Prescription:Encounter failed for %s", INVALID_DOSAGE_QUANTITY));
                    return Arrays.asList(
                            new ShrValidationMessage(Severity.ERROR, MEDICATION_DOSE_INSTRUCTION_LOCATION, "invalid", INVALID_DOSAGE_QUANTITY));
                }


            }

        }

        return new ArrayList<>();
    }

    private Collection<? extends ShrValidationMessage> validateMedication(ca.uhn.fhir.model.dstu2.resource.MedicationOrder medicationOrder) {
        IDatatype medicine = medicationOrder.getMedication();
        if (!(medicine instanceof ResourceReferenceDt)) {
            return new ArrayList<>();
        }

        String medicationRef = ((ResourceReferenceDt) medicine).getReference().getValue();
        String medicationDisplay = ((ResourceReferenceDt) medicine).getDisplay().getValue();
        if (StringUtils.isBlank(medicationRef) && StringUtils.isBlank(medicationDisplay)) {
            return Arrays.asList(new ShrValidationMessage(Severity.ERROR, ORDER_MEDICATION_LOCATION, "invalid",
                    UNSPECIFIED_MEDICATION));
        }

        if ((medicationRef == null)) return new ArrayList<>();

        if ((!urlValidator.isValid(medicationRef)) || (!isValidCodeableConceptUrl(medicationRef, ""))) {
            logger.debug(String.format("Medication-Order:Encounter failed for %s", INVALID_MEDICATION_REFERENCE_URL));
            return Arrays.asList(new ShrValidationMessage(Severity.ERROR, ORDER_MEDICATION_LOCATION, "invalid", INVALID_MEDICATION_REFERENCE_URL));
        }
        return new ArrayList<>();
    }
}
