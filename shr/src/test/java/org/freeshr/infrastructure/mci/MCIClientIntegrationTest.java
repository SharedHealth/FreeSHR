package org.freeshr.infrastructure.mci;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.FileUtil.asString;
import static org.freeshr.utils.HttpUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class MCIClientIntegrationTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private MCIClient mci;

    @Test
    public void shouldFetchAPatientByHealthId() throws ExecutionException, InterruptedException {
        String heathId = "5893922485019082753";
        String accessToken = UUID.randomUUID().toString();
        String clientId = "123";
        String userEmail = "email@gmail.com";
        
        givenThat(get(urlEqualTo("/api/v1/patients/" + heathId))
                .withHeader(AUTH_TOKEN_KEY, equalTo(accessToken))
                .withHeader(CLIENT_ID_KEY, equalTo(clientId))
                .withHeader(FROM_KEY, equalTo(userEmail))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient.json"))));

        Patient patient = mci.getPatient(heathId, clientId, userEmail, accessToken).toBlocking().first();

        assertThat(patient, is(notNullValue()));
        assertThat(patient.getHealthId(), is(heathId));
        assertThat(patient.getGender(), is("1"));
        assertTrue(patient.isConfidential());
        Address address = patient.getAddress();
        assertThat(address.getLine(), is("house30"));
        assertThat(address.getDistrict(), is("56"));
        assertThat(address.getDivision(), is("30"));
        assertThat(address.getUpazila(), is("10"));
        assertThat(address.getUnionOrUrbanWardId(), is("17"));
        assertThat(address.getCityCorporation(), is("99"));
    }
}