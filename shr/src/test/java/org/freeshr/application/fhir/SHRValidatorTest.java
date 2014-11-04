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
    public void setup(){
        shrValidator = new SHRValidator();
    }

    @Test
    public void shouldRejectAnyCategoryWithoutCode(){
        List<ValidationMessage> messages = shrValidator.validateCategories(FileUtil.asString("xmls/encounters/coded_and_noncoded_diagnosis.xml"));
        assertThat(messages.size(), is(1));
        assertThat(messages.get(0).getLevel(), is(OperationOutcome.IssueSeverity.error));
        assertThat(messages.get(0).getMessage(), is("Noncoded entry"));
    }

    @Test
    public void shouldAllowDiagnosisWithValidCode(){
            List<ValidationMessage> messages = shrValidator.validateCategories(FileUtil.asString("xmls/encounters/encounter.xml"));
            assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void shouldAllowCodedAsWellAsNonCodedChiefComplaint(){
        List<ValidationMessage> messages = shrValidator.validateCategories(FileUtil.asString("xmls/encounters/two_chief_complaints.xml"));
        assertThat(messages.isEmpty(), is(true));
    }
}