package org.freeshr.infrastructure.tr;


import org.hl7.fhir.dstu3.model.CodeSystem;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.freeshr.utils.FileUtil.asString;

public class ValueSetBuilderTest {

    @Test
    public void shouldParseValueSet() throws IOException, URISyntaxException {
        String content = asString("jsons/encounter-type-case-insensitive.json");
        CodeSystem codeSystem = new ValueSetBuilder().deserializeValueSetAndGetCodeSystem(content, "http://tr.hie.org/rest/v1/tr/vs/encounter-type");
        Assert.assertNotNull(codeSystem);
        Assert.assertEquals(4, codeSystem.getConcept().size());
    }
}
