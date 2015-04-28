package org.freeshr.validations;


import org.freeshr.infrastructure.tr.MedicationCodeValidator;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.utils.ConceptLocator;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.validations.ValidationMessages.*;
import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity.error;

@Component
public class MedicationPrescriptionValidator implements Validator<AtomEntry<? extends Resource>> {

    public static final String DOSAGE_INSTRUCTION = "dosageInstruction";
    private static final String MEDICATION = "medication";
    private static final String DISPENSE = "dispense";
    private static final Logger logger = LoggerFactory.getLogger(MedicationPrescriptionValidator.class);
    private MedicationCodeValidator codeValidator;
    private DoseQuantityValidator doseQuantityValidator;
    private UrlValidator urlValidator;

    @Autowired
    public MedicationPrescriptionValidator(MedicationCodeValidator codeValidator,
                                           DoseQuantityValidator doseQuantityValidator, UrlValidator urlValidator) {
        this.codeValidator = codeValidator;
        this.doseQuantityValidator = doseQuantityValidator;
        this.urlValidator = urlValidator;
    }

    @Override
    public List<ValidationMessage> validate(ValidationSubject<AtomEntry<? extends Resource>> subject) {

        AtomEntry<? extends Resource> atomEntry = subject.extract();
        List<ValidationMessage> validationMessages = new ArrayList<>();

        validationMessages.addAll(validateMedication(atomEntry));
        if (validationMessages.size() > 0) return validationMessages;

        validationMessages.addAll(validateDosageQuantity(atomEntry));
        if (validationMessages.size() > 0) return validationMessages;

        validationMessages.addAll(validateDispenseMedication(atomEntry));
        return validationMessages;
    }

    private List<ValidationMessage> validateDosageQuantity(AtomEntry<? extends Resource> atomEntry) {
        Property dosageInstruction = atomEntry.getResource().getChildByName(DOSAGE_INSTRUCTION);
        List<Element> dosageInstructionValues = dosageInstruction.getValues();
        for (Element dosageInstructionValue : dosageInstructionValues) {
            if (!(dosageInstructionValue instanceof MedicationPrescription
                    .MedicationPrescriptionDosageInstructionComponent)) continue;

            Quantity doseQuantity = ((MedicationPrescription.MedicationPrescriptionDosageInstructionComponent)
                    dosageInstructionValue).getDoseQuantity();
            if (doseQuantityValidator.isReferenceUrlNotFound(doseQuantity)) return new ArrayList<>();
            if (!urlValidator.isValid(doseQuantity.getSystemSimple())) {
                logger.debug(String.format("Medication-Prescription:Encounter failed for %s", INVALID_DOSAGE_QUANTITY_REFERENCE));
                return validationMessages(new ValidationMessage(null, ResourceValidator.INVALID, atomEntry.getId(),
                        INVALID_DOSAGE_QUANTITY_REFERENCE, error));
            }
            ConceptLocator.ValidationResult validationResult = doseQuantityValidator.validate(doseQuantity);
            if (validationResult != null) {
                logger.debug(String.format("Medication-Prescription:Encounter failed for %s", INVALID_DOSAGE_QUANTITY));
                return validationMessages(new ValidationMessage(null, ResourceValidator.INVALID, atomEntry.getId(),
                        INVALID_DOSAGE_QUANTITY, error));
            }

        }
        return new ArrayList<>();
    }


    private List<ValidationMessage> validateDispenseMedication(AtomEntry<? extends Resource> atomEntry) {
        Property dispense = atomEntry.getResource().getChildByName(DISPENSE);
        if (dispense == null || (!dispense.hasValues())) return new ArrayList<>();
        Property dispenseMedication = dispense.getValues().get(0).getChildByName(MEDICATION);
        if (dispenseMedication == null || !dispenseMedication.hasValues()) return new ArrayList<>();
        String dispenseMedicationRefUrl = getReferenceUrl(dispenseMedication);
        if ((dispenseMedicationRefUrl == null)) return new ArrayList<>();
        if (!urlValidator.isValid(dispenseMedicationRefUrl))
            return validationMessages(new ValidationMessage(null, ResourceValidator.INVALID, atomEntry.getId(),
                    INVALID_DISPENSE_MEDICATION_REFERENCE_URL, error));
        if (!isValidCodeableConceptUrl(dispenseMedicationRefUrl, ""))
            return validationMessages(new ValidationMessage(null, ResourceValidator.INVALID, atomEntry.getId(),
                    INVALID_DISPENSE_MEDICATION_REFERENCE_URL, error));
        return new ArrayList<>();
    }


    private List<ValidationMessage> validateMedication(AtomEntry<? extends Resource> atomEntry) {
        Property medication = atomEntry.getResource().getChildByName(MEDICATION);
        if ((medication == null) || (!medication.hasValues())) {
            logger.debug(String.format("Medication-Prescription:Encounter failed for %s", UNSPECIFIED_MEDICATION));
            return validationMessages(new ValidationMessage(null, ResourceValidator.INVALID, atomEntry.getId(),
                    UNSPECIFIED_MEDICATION,
                    error));
        }
        String medicationRefUrl = getReferenceUrl(medication);
        if ((medicationRefUrl == null)) return new ArrayList<>();
        if (!urlValidator.isValid(medicationRefUrl)) {
            logger.debug(String.format("Medication-Prescription:Encounter failed for %s", INVALID_MEDICATION_REFERENCE_URL));
            return validationMessages(new ValidationMessage(null, ResourceValidator.INVALID, atomEntry.getId(),
                    INVALID_MEDICATION_REFERENCE_URL, error));
        }
        if (!isValidCodeableConceptUrl(medicationRefUrl, ""))
            return validationMessages(new ValidationMessage(null, ResourceValidator.INVALID, atomEntry.getId(),
                    INVALID_MEDICATION_REFERENCE_URL, error));
        return new ArrayList<>();
    }

    private List<ValidationMessage> validationMessages(ValidationMessage message) {
        List<ValidationMessage> validationMessages = new ArrayList<>();
        validationMessages.add(message);
        return validationMessages;
    }

    private boolean isValidCodeableConceptUrl(String url, String code) {
        return codeValidator.isValid(url, code).toBlocking().first();
    }

    private String getReferenceUrl(Property medication) {
        Element element = medication.getValues().get(0);
        return element instanceof ResourceReference ? ((ResourceReference) element).getReferenceSimple() : null;
    }


}
