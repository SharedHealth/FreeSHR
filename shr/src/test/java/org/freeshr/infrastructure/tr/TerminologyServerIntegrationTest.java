package org.freeshr.infrastructure.tr;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.FileUtil.asString;
import static org.freeshr.utils.StringUtils.concat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class TerminologyServerIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private TerminologyServer trServer;

    private static String REFERENCE_TERM_PATH =
            "/openmrs/ws/rest/v1/tr/referenceterms/fa460ea6-04c7-45af-a6fa-5072e7caed40";
    private static String CONCEPT_URL = "/openmrs/ws/rest/v1/tr/concepts/eddb01eb-61fc-4f9e-aca5-e44193509f35";

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo(REFERENCE_TERM_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/refterm.json"))));

        givenThat(get(urlEqualTo(CONCEPT_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/concept.json"))));
    }


    @Test
    public void shouldIdentifyValidReferenceTerms() throws Exception {
        assertTrue(trServer.isValid(concat("http://localhost:9997", REFERENCE_TERM_PATH), "S40").toBlocking().first());
        assertFalse(trServer.isValid(concat("http://localhost:9997", REFERENCE_TERM_PATH),
                "invalid_ref_code").toBlocking().first());
    }

    @Test
    public void shouldIdentifyValidConcepts() throws Exception {
        assertTrue(trServer.isValid(concat("http://localhost:9997", CONCEPT_URL),
                "eddb01eb-61fc-4f9e-aca5-e44193509f35").toBlocking().first());
        assertFalse(trServer.isValid(concat("http://localhost:9997", CONCEPT_URL), "invalid_uuid").toBlocking().first
                ());

    }

    @Test
    public void shouldRejectInvalidSystemPath() throws Exception {
        assertFalse(trServer.isValid("http://localhost:9997/invalid/path/code", "code").toBlocking().first());
    }

    @Test
    public void shouldFetchValueSetWithCaseSensitivity() throws Exception {
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/encounter-type"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/encounter-type-case-sensitive.json"))));

        assertTrue(trServer.isValid("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/encounter-type",
                "REG").toBlocking().first());
        assertFalse(trServer.isValid("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/encounter-type",
                "reg").toBlocking().first());
        assertFalse(trServer.isValid("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/encounter-type",
                "friend").toBlocking().first());
    }

    @Test
    public void shouldFetchValueSetWithCaseInSensitivity() throws Exception {
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/encounter-type"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/encounter-type-case-insensitive.json"))));

        assertTrue(trServer.isValid("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/encounter-type",
                "REG").toBlocking().first());
        assertTrue(trServer.isValid("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/encounter-type",
                "reg").toBlocking().first());
        assertFalse(trServer.isValid("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/encounter-type",
                "friend").toBlocking().first());
    }
}