package org.freeshr.application.fhir;

import org.freeshr.utils.FileUtil;
import org.junit.Test;

public class FhirValidatorTest {

    @Test
    public void shouldValidateEncounter() throws Exception {
        FhirValidator validator = new FhirValidator();
        validator.validate(FileUtil.asString("xmls/encounter.xml"), "validation.zip");
    }
}
