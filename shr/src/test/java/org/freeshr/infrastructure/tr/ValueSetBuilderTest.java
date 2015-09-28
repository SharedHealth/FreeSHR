package org.freeshr.infrastructure.tr;


import org.hl7.fhir.instance.model.ValueSet;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.freeshr.utils.FileUtil.asString;

public class ValueSetBuilderTest {
    @Test
    public void shouldParseValueSet() throws IOException, URISyntaxException {
        String content = asString("jsons/encounter-type-case-insensitive.json");
        ValueSet valueSet = new ValueSetBuilder().deSerializeValueSet(content, "http://tr.hie.org/rest/v1/tr/vs/encounter-type");
        Assert.assertNotNull(valueSet);
        Assert.assertEquals(3, valueSet.getCodeSystem().getConcept().size());
    }



}
