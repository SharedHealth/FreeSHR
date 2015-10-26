package org.freeshr.validations.resource;


import ca.uhn.fhir.context.FhirContext;
import org.freeshr.application.fhir.TRConceptValidator;
import org.freeshr.config.SHRProperties;
import org.freeshr.infrastructure.tr.MedicationCodeValidator;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.UrlValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseResource;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MedicationOrderValidatorTest {

    private DoseQuantityValidator quantityValidator;


    private MedicationOrderValidator medicationOrderValidator;

    @Mock
    TRConceptValidator trConceptValidator;

    @Mock
    SHRProperties properties;

    @Mock
    private MedicationCodeValidator medicationValidator;

    @Mock
    UrlValidator urlValidator;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        quantityValidator = new DoseQuantityValidator(trConceptValidator);
        medicationOrderValidator = new MedicationOrderValidator(trConceptValidator, quantityValidator, urlValidator);

    }

    @Test
    public void shouldFailForInvalidMedication() throws Exception {
        final FhirContext fhirContext = FhirContext.forDstu2();
        IBaseResource medicationOrder = parseResource(FileUtil.asString("xmls/encounters/dstu2/example_medication_order.xml"), fhirContext);
        //IDatatype medication = ((MedicationOrder) medicationOrder).getMedication();
        when(medicationValidator.validate(anyString(), anyString())).thenReturn(false);
        when(urlValidator.isValid(anyString())).thenReturn(true);
        List<ShrValidationMessage> validate = medicationOrderValidator.validate(medicationOrder);
        assertFalse(validate.isEmpty());
        for (ShrValidationMessage shrValidationMessage : validate) {
            System.out.println(shrValidationMessage.getMessage());
        }
        verify(medicationValidator, times(1)).validate(eq("Medication/f001"), eq(""));
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