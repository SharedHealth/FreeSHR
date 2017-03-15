package org.freeshr.validations.resource;

import ca.uhn.fhir.context.FhirContext;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.ShrValidationMessage;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.assertEquals;

public class ConditionValidatorTest {

    @Test
    @Ignore("Need to implement it")
    public void shouldValidateClinicalStatus() throws Exception {
        FhirContext fhirContext = FhirContext.forDstu3();
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_localRefs_invalidCondition.xml"), fhirContext);
        List<Condition> conditions = FhirResourceHelper.findBundleResourcesOfType(bundle, Condition.class);
        List<ShrValidationMessage> validationMessages = new ConditionValidator().validate(conditions.get(0));
        assertEquals(1, validationMessages.size());

    }
}
