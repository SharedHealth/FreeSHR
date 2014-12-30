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
import rx.Observable;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.FileUtil.asString;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class MedicationCodeValidatorIntegrationTest {

    public static final String REF_DRUG_URL = "/openmrs/ws/rest/v1/tr/drugs/3be99d23-e50d-41a6-ad8c-f6434e49f513";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    MedicationCodeValidator validator;

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo(REF_DRUG_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/medication_paracetamol.json"))));
    }

    @Test
    public void shouldValidateMedicationCode() {
        Observable<Boolean> valid = validator.isValid("http://localhost:9997" + REF_DRUG_URL, "");
        Boolean result = valid.toBlocking().first();
        assertTrue(result);
    }

}