package org.freeshr.validations;


import org.freeshr.domain.ErrorMessage;
import org.freeshr.infrastructure.tr.MedicationCodeValidator;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.utils.ConceptLocator;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;

import java.lang.Boolean;
import java.util.ArrayList;
import java.util.List;

import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity;

@Component
public class MedicationPrescriptionValidator implements Validator<AtomEntry<? extends Resource>> {

    public static final String DOSAGE_INSTRUCTION = "dosageInstruction";
    public static final String INVALID_MEDICATION_REFERENCE_URL = "Invalid Medication Reference URL";
    public static final String UNSPECIFIED_MEDICATION = "Unspecified Medication";
    public static final String INVALID_DISPENSE_MEDICATION_REFERENCE_URL = "Invalid Dispense-Medication Reference URL";
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
        ArrayList<ValidationMessage> validationMessages = new ArrayList<>();

        if (!validateMedication(atomEntry, validationMessages)) {
            return validationMessages;
        }

        if (!validateDosageQuantity(atomEntry, validationMessages)) {
            return validationMessages;
        }

        if (!validateDispenseMedication(atomEntry, validationMessages)) {
            return validationMessages;
        }

        return validationMessages;
    }

    private boolean validateDosageQuantity(AtomEntry<? extends Resource> atomEntry, ArrayList<ValidationMessage> validationMessages) {
        String id = atomEntry.getId();
        Property dosageInstruction = atomEntry.getResource().getChildByName(DOSAGE_INSTRUCTION);
        List<Element> dosageInstructionValues = dosageInstruction.getValues();
        for (Element dosageInstructionValue : dosageInstructionValues) {

            if (dosageInstructionValue instanceof MedicationPrescription.MedicationPrescriptionDosageInstructionComponent) {
                Quantity doseQuantity = ((MedicationPrescription.MedicationPrescriptionDosageInstructionComponent) dosageInstructionValue).getDoseQuantity();

                if(doseQuantityValidator.isReferenceUrlNotFound(doseQuantity)){
                    return true;
                }

                if(!urlValidator.isValid(doseQuantity.getSystemSimple())){
                    return false;
                }

                ConceptLocator.ValidationResult validationResult = doseQuantityValidator.validate(doseQuantity);
                if (validationResult != null) {
                    logger.error("Medication-Prescription DosageQuantity Code is invalid:");
                    validationMessages.add(new ValidationMessage(null, ResourceValidator.INVALID, id, ErrorMessage.INVALID_DOSAGE_QUANTITY, IssueSeverity.error));
                    return false;
                }
            }
        }

        return true;
    }


    private boolean validateDispenseMedication(AtomEntry<? extends Resource> atomEntry, ArrayList<ValidationMessage> validationMessages) {

        String id = atomEntry.getId();
        Property dispense = atomEntry.getResource().getChildByName(DISPENSE);
        /* Not a Mandatory Field.Skip it if not present */
        if (dispense == null || (!dispense.hasValues())) {
            return true;
        }
        Property dispenseMedication = dispense.getValues().get(0).getChildByName(MEDICATION);
        if (dispenseMedication == null || !dispenseMedication.hasValues()) {
            return true;
        }
        String dispenseMedicationRefUrl = getReferenceUrl(dispenseMedication);
        if ((dispenseMedicationRefUrl == null)) {
            return true;
        }
        if ((!urlValidator.isValid(dispenseMedicationRefUrl))) {
            logger.error("Dispense-Medication URL is invalid:" + dispenseMedicationRefUrl);
            validationMessages.add(new ValidationMessage(null, ResourceValidator.INVALID, id, INVALID_DISPENSE_MEDICATION_REFERENCE_URL, IssueSeverity.error));
            return false;
        }

        if (!isValidCodeableConceptUrl(dispenseMedicationRefUrl, "")) {
            validationMessages.add(new ValidationMessage(null, ResourceValidator.INVALID, id, INVALID_DISPENSE_MEDICATION_REFERENCE_URL, IssueSeverity.error));
            return false;
        }

        return true;
    }


    private boolean validateMedication(AtomEntry<? extends Resource> atomEntry, List<ValidationMessage> validationMessages) {
        String id = atomEntry.getId();
        Property medication = atomEntry.getResource().getChildByName(MEDICATION);
        if ((medication == null) || (!medication.hasValues())) {
            validationMessages.add(new ValidationMessage(null, ResourceValidator.INVALID, id, UNSPECIFIED_MEDICATION, IssueSeverity.error));
            return false;
        }

        String medicationRefUrl = getReferenceUrl(medication);
        //now to check for valid or invalid
        if ((medicationRefUrl == null)) {
            return true;
        }
        if ((!urlValidator.isValid(medicationRefUrl))) {
            logger.error("Medication URL is invalid:" + medicationRefUrl);
            validationMessages.add(new ValidationMessage(null, ResourceValidator.INVALID, id, INVALID_MEDICATION_REFERENCE_URL, IssueSeverity.error));
            return false;
        }

        if (!isValidCodeableConceptUrl(medicationRefUrl, "")) {
            validationMessages.add(new ValidationMessage(null, ResourceValidator.INVALID, id, INVALID_MEDICATION_REFERENCE_URL, IssueSeverity.error));
            return false;
        }

        return true;
    }

    private boolean isValidCodeableConceptUrl(String url, String code) {

        Observable<Boolean> obs = codeValidator.isValid(url, code);
        Boolean result = obs.toBlocking().first();
        if (!result) {
            return false;
        }
        return true;
    }


    private String getReferenceUrl(Property medication) {
        Element element = medication.getValues().get(0);
        if (element instanceof ResourceReference) {
            return ((ResourceReference) element).getReferenceSimple();
        }
        return null;
    }



}