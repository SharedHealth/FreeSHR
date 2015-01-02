package org.freeshr.validations;


import org.freeshr.infrastructure.tr.MedicationCodeValidator;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;

import java.lang.Boolean;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity;

@Component
public class MedicationValidator implements Validator<AtomEntry<? extends Resource>> {

    private static final String MEDICATION = "medication";
    private static final String PRESCRIBER = "prescriber";
    private static final String DISPENSE = "dispense";

    private static final Logger logger = LoggerFactory.getLogger(MedicationValidator.class);
    public static final String INVALID_MEDICATION_REFERENCE_URL = "Invalid Medication reference URL";
    public static final String UNSPECIFIED_MEDICATION = "Unspecified Medication";

    public static final String INVALID_PRESCRIBER_REFERENCE_URL = "Invalid Prescriber reference URL";

    public static final String INVALID_DISPENSE_MEDICATION_REFERENCE_URL = "Invalid Dispense-Medication reference URL";

    private MedicationCodeValidator codeValidator;
    private AtomFeed atomFeed;

    @Autowired
    public MedicationValidator(MedicationCodeValidator codeValidator) {
        this.codeValidator = codeValidator;
    }

    @Override
    public List<ValidationMessage> validate(ValidationSubject<AtomEntry<? extends Resource>> subject) {

        AtomEntry<? extends Resource> atomEntry = subject.extract();
        ArrayList<ValidationMessage> validationMessages = new ArrayList<>();

        /* Validate Medication*/
        boolean result = validateMedication(atomEntry, validationMessages);
        if (!result) {
            return validationMessages;
        }

        /* Validate Medication-Prescriber */
        result = validatePrescriber(atomEntry, validationMessages);
        if (!result) {
            return validationMessages;
        }

        /* Validate Dispense Medication */
        result = validateDispenseMedication(atomEntry, validationMessages);
        if (!result) {
            return validationMessages;
        }



        return validationMessages;
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
        if ((dispenseMedicationRefUrl == null) || (!isValidReferenceUrl(dispenseMedicationRefUrl))) {
            logger.error("Dispense-Medication URL is invalid:" + dispenseMedicationRefUrl);
            validationMessages.add(new ValidationMessage(null, ResourceValidator.INVALID, id, INVALID_DISPENSE_MEDICATION_REFERENCE_URL, IssueSeverity.error));
            return false;
        }


        return true;
    }

    private boolean validatePrescriber(AtomEntry<? extends Resource> atomEntry, ArrayList<ValidationMessage> validationMessages) {
        String id = atomEntry.getId();
        Property prescriber = atomEntry.getResource().getChildByName(PRESCRIBER);
        /* Not a Mandatory Field.Skip it if not present */
        if (prescriber == null || !prescriber.hasValues()) {
            return true;
        }

        String prescriberRefUrl = getReferenceUrl(prescriber);
        if ((prescriberRefUrl == null) || (!isValidReferenceUrl(prescriberRefUrl))) {
            logger.error("Prescriber URL is invalid:" + prescriberRefUrl);
            validationMessages.add(new ValidationMessage(null, ResourceValidator.INVALID, id, INVALID_PRESCRIBER_REFERENCE_URL, IssueSeverity.error));
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
        if ((medicationRefUrl == null) || (!isValidReferenceUrl(medicationRefUrl))) {
            logger.error("Medication URL is invalid:" + medicationRefUrl);
            validationMessages.add(new ValidationMessage(null, ResourceValidator.INVALID, id, INVALID_MEDICATION_REFERENCE_URL, IssueSeverity.error));
            return false;
        }

        Observable<Boolean> obs = codeValidator.isValid(medicationRefUrl, "");
        Boolean result = obs.toBlocking().first();
        if (!result) {
            validationMessages.add(new ValidationMessage(null, ResourceValidator.INVALID, id, INVALID_MEDICATION_REFERENCE_URL, IssueSeverity.error));
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

    private boolean isValidReferenceUrl(final String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            //do nothing
        }
        return false;
    }

    public MedicationCodeValidator getMedicationCodeValidator() {
        return codeValidator;
    }

}
