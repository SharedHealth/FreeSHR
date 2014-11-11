package org.freeshr.application.fhir;

import org.freeshr.utils.FileUtil;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SHRValidatorTest {

    private SHRValidator shrValidator;

    @Before
    public void setup() {
        shrValidator = new SHRValidator();
    }

    @Test
    public void shouldRejectAnyCategoryWithoutCode() {
        //This example has 3 entries:
        // 1. Without code and system, 2. With valid code and system, 3. With only code

        List<ValidationMessage> messages = shrValidator.validateCategories(FileUtil.asString("xmls/encounters/coded_and_noncoded_diagnosis.xml"));
        assertThat(messages.size(), is(2));
        assertThat(messages.get(0).getLevel(), is(OperationOutcome.IssueSeverity.error));
        assertThat(messages.get(0).getMessage(), is("Noncoded entry"));
        assertThat(messages.get(1).getLevel(), is(OperationOutcome.IssueSeverity.error));
        assertThat(messages.get(1).getMessage(), is("Noncoded entry"));
        assertThat(messages.get(0).getType(), is("invalid"));
    }

    @Test
    public void shouldAllowDiagnosisWithValidCode() {
        List<ValidationMessage> messages = shrValidator.validateCategories(FileUtil.asString("xmls/encounters/encounter.xml"));
        assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void shouldAllowCodedAsWellAsNonCodedChiefComplaint() {
        List<ValidationMessage> messages = shrValidator.validateCategories(FileUtil.asString("xmls/encounters/two_chief_complaints.xml"));
        assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void shouldAllowIfAtLeaseOneCodeIsRight() {
        List<ValidationMessage> messages = shrValidator.validateCategories(FileUtil.asString("xmls/encounters/multiple_coded_diagnosis.xml"));
        assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void shouldValidateIfHealthIdsInTheBodyMatchGivenHealthId() {
        EncounterValidationResponse response = shrValidator.validateHealthId(FileUtil.asString("xmls/encounters/diagnostic_order_valid.xml"), "5893922485019082753");
        assertThat(response.isSuccessful(), is(true));

        response = shrValidator.validateHealthId(FileUtil.asString("xmls/encounters/encounter.xml"), "11112222233333");
        assertThat(response.isSuccessful(), is(false));
        assertThat(response.getErrors().get(0).getType(), is(SHRValidator.INVALID));
        assertThat(response.getErrors().get(0).getReason(), is("Patient's Health Id does not match."));
    }

}