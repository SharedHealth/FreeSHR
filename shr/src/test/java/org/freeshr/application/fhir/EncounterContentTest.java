package org.freeshr.application.fhir;

import org.freeshr.utils.FileUtil;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Resource;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class EncounterContentTest {

    private static final String encounterJson = FileUtil.asString("jsons/encounter.json");

    @Test
    public void shouldIdentifyDiagnosis() throws Exception {
        EncounterContent content = new EncounterContent(encounterJson);

        List<Condition> diagnosisList = content.allDiagnosis();

        assertThat(diagnosisList, is(notNullValue()));
        assertThat(diagnosisList.size(), is(1));

    }
}