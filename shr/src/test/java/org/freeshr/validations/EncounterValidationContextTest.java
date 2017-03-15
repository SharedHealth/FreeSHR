package org.freeshr.validations;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.data.EncounterBundleData;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.utils.FileUtil;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterValidationContextTest {
    @Mock
    private FhirFeedUtil fhirFeedUtil;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldDeserialiseOnlyOnce() throws Exception {
        String content = FileUtil.asString("xmls/encounters/stu3/p99001046345_encounter_with_diagnoses_with_local_refs.xml");
        EncounterBundle encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID, content);

        when(fhirFeedUtil.parseBundle(content, "xml")).thenReturn(new Bundle());

        EncounterValidationContext validationContext = new EncounterValidationContext(encounterBundle,
                fhirFeedUtil);
        validationContext.getBundle();
        validationContext.getBundle();
        validationContext.getBundle();

        verify(fhirFeedUtil, atMost(1)).parseBundle(content, "xml");

    }
}
