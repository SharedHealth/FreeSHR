package org.freeshr.validations.resource;


import ca.uhn.fhir.context.FhirContext;
import org.freeshr.application.fhir.TRConceptLocator;
import org.freeshr.config.SHRProperties;
import org.freeshr.infrastructure.tr.MedicationCodeValidator;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.DoseQuantityValidator;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.UrlValidator;
import org.freeshr.validations.resource.MedicationPrescriptionValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import rx.Observable;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseResource;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MedicationPrescriptionValidatorTest {

    private DoseQuantityValidator quantityValidator;


    private MedicationPrescriptionValidator medicationOrderValidator;

    @Mock
    TRConceptLocator trConceptLocator;

    @Mock
    SHRProperties properties;

    @Mock
    private MedicationCodeValidator codeValidator;

    @Mock
    UrlValidator urlValidator;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        quantityValidator = new DoseQuantityValidator(trConceptLocator);
        medicationOrderValidator = new MedicationPrescriptionValidator(codeValidator, quantityValidator, urlValidator);

    }

    @Test
    public void shouldFailForInvalidMedication() throws Exception {
        final FhirContext fhirContext = FhirContext.forDstu2();
        IBaseResource medicationOrder = parseResource(FileUtil.asString("xmls/encounters/dstu2/example_medication_order.xml"), fhirContext);
        //IDatatype medication = ((MedicationOrder) medicationOrder).getMedication();
        when(codeValidator.isValid(anyString(), anyString())).thenReturn(Observable.just(Boolean.FALSE));
        when(urlValidator.isValid(anyString())).thenReturn(true);
        List<ShrValidationMessage> validate = medicationOrderValidator.validate(medicationOrder);
        assertFalse(validate.isEmpty());
        for (ShrValidationMessage shrValidationMessage : validate) {
            System.out.println(shrValidationMessage.getMessage());
        }
        verify(codeValidator, times(1)).isValid(eq("Medication/f001"), eq(""));
    }

    @Test
    @Ignore
    public void shouldValidateMedicationQuantity() {
        //TODO
    }

    @Test
    @Ignore
    public void shouldValidateMedicationDispense() {
        //TODO
    }
}