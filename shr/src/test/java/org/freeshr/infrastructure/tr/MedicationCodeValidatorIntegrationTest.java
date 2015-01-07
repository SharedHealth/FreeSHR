package org.freeshr.infrastructure.tr;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observable;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.FileUtil.asString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class MedicationCodeValidatorIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    MedicationCodeValidator validator;

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
        String validUrl = "/openmrs/ws/rest/v1/tr/drugs/matching-code";

        givenThat(get(urlEqualTo(validUrl))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/medication_paracetamol.json"))));


        Observable<Boolean> valid = validator.isValid("http://localhost:9997/" + validUrl, "matching-code");
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
}