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

import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.freeshr.utils.FileUtil.asString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class MasterClientIndexClientIntegrationTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private MasterClientIndexClient mci;

    @Test
    public void shouldFetchAPatientByHealthId() throws ExecutionException, InterruptedException {
        String heathId = "5dd24827-fd5d-4024-9f65-5a3c88a28af5";
        givenThat(get(urlEqualTo("/api/v1/patients/" + heathId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient.json"))));

        Patient patient = mci.getPatient(heathId).get();

        assertThat(patient, is(notNullValue()));
        assertThat(patient.getHealthId(), is(heathId));
        assertThat(patient.getGender(), is("1"));
        Address adddress = patient.getAddress();
        assertThat(adddress.getLine(), is("house30"));
        assertThat(adddress.getDistrict(), is("56"));
        assertThat(adddress.getDivision(), is("30"));
        assertThat(adddress.getUpazila(), is("10"));
        assertThat(adddress.getWard(), is("17"));
        assertThat(adddress.getCityCorporation(), is("99"));
    }
}