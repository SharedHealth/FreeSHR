package org.freeshr.application.fhir;

import org.freeshr.utils.FileUtil;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.freeshr.validations.*;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

public class ResourceValidatorTest {

    private ResourceValidator resourceValidator;
    ResourceOrFeedDeserializer resourceOrFeedDeserializer;

    @Mock
    MedicationPrescriptionValidator medicationPrescriptionValidator;
    @Mock
    ImmunizationValidator immunizationValidator;
    @Mock
    ProcedureValidator procedureValidator;

    @Before
    public void setup() {
        initMocks(this);
        resourceValidator = new ResourceValidator(new ConditionValidator(), medicationPrescriptionValidator, immunizationValidator, procedureValidator);
        resourceOrFeedDeserializer = new ResourceOrFeedDeserializer();
    }

    @Test
    public void shouldVerifyThatEveryCodeableConceptInAConditionIsCoded() {
        //This example has 4 entries:
        // 1. Without code and system for diagnosis, 2. With valid code and system for diagnosis,
        // 3. With only code for daiagnosis
        // 4. With non-coded severity, location, evidence etc.


        final String xml = FileUtil.asString("xmls/encounters/coded_and_noncoded_diagnosis.xml");
        List<ValidationMessage> messages = resourceValidator.validate(new ValidationSubject<AtomFeed>() {
            @Override
            public AtomFeed extract() {
                return resourceOrFeedDeserializer.deserialize(xml);
            }
        });
        assertThat(messages.size(), is(3));
        assertThat(messages.get(0).getLevel(), is(OperationOutcome.IssueSeverity.error));
        assertThat(messages.get(0).getMessage(), is("Viral pneumonia 785857"));
        assertThat(messages.get(0).getType(), is(ResourceValidator.CODE_UNKNOWN));
        assertThat(messages.get(1).getLevel(), is(OperationOutcome.IssueSeverity.error));
        assertThat(messages.get(1).getMessage(), is("Viral pneumonia 785857"));
        assertThat(messages.get(1).getType(), is(ResourceValidator.CODE_UNKNOWN));
        assertThat(messages.get(2).getLevel(), is(OperationOutcome.IssueSeverity.error));
        assertThat(messages.get(2).getMessage(), is("Moderate"));
        assertThat(messages.get(2).getType(), is(ResourceValidator.CODE_UNKNOWN));
    }

    @Test
    public void shouldValidateConditionDiagnosisWithAllValidComponents() {
        final String xml = FileUtil.asString("xmls/encounters/valid_diagnosis.xml");
        List<ValidationMessage> messages = resourceValidator.validate(new ValidationSubject<AtomFeed>() {
            @Override
            public AtomFeed extract() {
                return resourceOrFeedDeserializer.deserialize(xml);
            }
        });

        assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void shouldAllowResourceTypeConditionWithCodedAsWellAsNonCodedForAnythingOtherThanDiagnosis() {
        final String xml = FileUtil.asString("xmls/encounters/other_conditions.xml");
        List<ValidationMessage> messages = resourceValidator.validate(new ValidationSubject<AtomFeed>() {
            @Override
            public AtomFeed extract() {
                return resourceOrFeedDeserializer.deserialize(xml);
            }
        });
        assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void shouldAcceptDiagnosisIfAtLeaseOneReferenceTermIsRight() {
        final String xml = FileUtil.asString("xmls/encounters/multiple_coded_diagnosis.xml");
        List<ValidationMessage> messages = resourceValidator.validate(new ValidationSubject<AtomFeed>() {
            @Override
            public AtomFeed extract() {
                return resourceOrFeedDeserializer.deserialize(xml);
            }
        });
        assertThat(messages.isEmpty(), is(true));
    }

}