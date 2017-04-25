package org.freeshr.validations.resource;

import ca.uhn.fhir.context.FhirContext;
import org.freeshr.config.SHRProperties;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.ShrValidationMessage;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConditionValidatorTest {
    @Mock
    private SHRProperties shrProperties;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    @Ignore("Need to implement it")
    public void shouldValidateClinicalStatus() throws Exception {
        FhirContext fhirContext = FhirContext.forDstu3();
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_localRefs_invalidCondition.xml"), fhirContext);
        List<Condition> conditions = FhirResourceHelper.findBundleResourcesOfType(bundle, Condition.class);
        List<ShrValidationMessage> validationMessages = new ConditionValidator(shrProperties).validate(conditions.get(0), 2);
        assertEquals(1, validationMessages.size());

    }
}
