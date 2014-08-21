package org.freeshr.application.fhir;

import org.freeshr.utils.FileUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;

import static org.mockito.MockitoAnnotations.initMocks;

public class FhirValidatorTest {

    private FhirValidator validator;

    @Mock
    private TRConceptLocator trConceptLocator;

    @Before
    public void setup() {
        initMocks(this);
        validator = new FhirValidator(trConceptLocator);
    }

    @Test
    public void shouldValidateEncounter() throws Exception {
        validator.validate(FileUtil.asString("xmls/encounter.xml"), new File(this.getClass().getClassLoader().getResource("validation.zip").toURI()).getAbsolutePath());
    }
}
