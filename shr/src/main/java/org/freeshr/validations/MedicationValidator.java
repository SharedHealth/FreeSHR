package org.freeshr.validations;


import org.freeshr.infrastructure.tr.MedicationCodeValidator;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;

import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity;

import java.lang.Boolean;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class MedicationValidator implements Validator<AtomEntry<? extends Resource>>{

    private static final String MEDICATION= "medication";

    private static final Logger logger = LoggerFactory.getLogger(MedicationValidator.class);
    public static final String INVALID_MEDICATION_REFERENCE_URL = "Invalid Medication reference URL";
    public static final String UNSPECIFIED_MEDICATION = "Unspecified Medication";
    private MedicationCodeValidator codeValidator;

    @Autowired
    public MedicationValidator(MedicationCodeValidator codeValidator) {
        this.codeValidator = codeValidator;
    }

    @Override
    public List<ValidationMessage> validate(ValidationSubject<AtomEntry<? extends Resource>> subject) {

        AtomEntry<? extends Resource> atomEntry = subject.extract();
        ArrayList<ValidationMessage> validationMessages = new ArrayList<>();
        boolean result = validateMedication(atomEntry, validationMessages);
        if (!result) {
            return validationMessages;
        }

        result = validatePrescriber(atomEntry, validationMessages);




        return validationMessages;
    }

    private boolean validatePrescriber(AtomEntry<? extends Resource> atomEntry, ArrayList<ValidationMessage> validationMessages) {
        return true;
    }

    private boolean validateMedication(AtomEntry<? extends Resource> atomEntry, List<ValidationMessage> validationMessages) {
        String id = atomEntry.getId();
        Property medication = atomEntry.getResource().getChildByName(MEDICATION);
        if ((medication == null) || (!medication.hasValues())) {
            validationMessages.add(new ValidationMessage(null, ResourceValidator.INVALID, id, UNSPECIFIED_MEDICATION, IssueSeverity.error));
            return false;
        }

        String medicationRefUrl = getMedicationReferenceUrl(medication);
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


    private String getMedicationReferenceUrl(Property medication) {
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


}
