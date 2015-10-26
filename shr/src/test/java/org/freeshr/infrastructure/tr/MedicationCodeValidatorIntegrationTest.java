package org.freeshr.infrastructure.tr;

import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.utils.FhirFeedUtil;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observable;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.FileUtil.asString;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class MedicationCodeValidatorIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    MedicationCodeValidator validator;

    @Autowired
    FhirFeedUtil feedUtil;

    @Test
    public void shouldPassValidMedicationUrlIfCodeIsEmpty() {
        String validUrl = "/openmrs/ws/rest/v1/tr/drugs/3be99d23-e50d-41a6-ad8c-f6434e49f513";

        givenThat(get(urlEqualTo(validUrl))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/medication_paracetamol.json"))));

        Observable<Boolean> valid = validator.isValid("http://localhost:9997" + validUrl, "");
        Boolean result = valid.toBlocking().first();

        assertTrue(result);
    }

    @Test
    public void shouldPassValidMedicationUrlIfAMatchingCodeIsPassed() throws Exception {
        String validUrl = "/openmrs/ws/rest/v1/tr/drugs/paracetamol";

        givenThat(get(urlEqualTo(validUrl))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/medication_paracetamol.json"))));


        Observable<Boolean> valid = validator.isValid("http://localhost:9997/" + validUrl, "paracetamol");
        Boolean result = valid.toBlocking().first();

        assertTrue(result);
    }

    @Test
    public void shouldRejectValidMedicationUrlIfAMatchingCodeIsNotPassed() throws Exception {
        String validUrl = "/openmrs/ws/rest/v1/tr/drugs/code";

        givenThat(get(urlEqualTo(validUrl))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/medication_paracetamol.json"))));


        Observable<Boolean> valid = validator.isValid("http://localhost:9997/" + validUrl, "some-other-code");
        Boolean result = valid.toBlocking().first();

        assertFalse(result);
    }

    @Test
    public void shouldRejectInvalidMedicationCode() throws Exception {
        String invalidUrl = "/openmrs/ws/rest/v1/tr/drugs/invalid-code";

        givenThat(get(urlEqualTo(invalidUrl))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Invalid medication code")));

        Observable<Boolean> valid = validator.isValid("http://localhost:9997" + invalidUrl, "");
        Boolean result = valid.toBlocking().first();

        assertFalse(result);
    }

    @Test
    public void shouldParseMedicationResource() {
        IBaseResource resource = feedUtil.getFhirContext().newJsonParser().parseResource(asString("jsons/medication_paracetamol.json"));
        assertTrue(resource instanceof Medication);
        List<CodingDt> codings = ((Medication) resource).getCode().getCoding();
        assertEquals(2, codings.size());

    }
}