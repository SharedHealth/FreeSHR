package org.freeshr.infrastructure;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import junit.framework.TestCase;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.mci.MasterClientIndexClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.FileUtil.asString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class FacilityRegistryClientIntegrationTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private FacilityRegistryClient hrm;

    @Test
    public void shouldFetchFacility() throws ExecutionException, InterruptedException {
        givenThat(get(urlEqualTo("/facilities/10000001.json"))
                .withHeader("client_id", matching("18550"))
                .withHeader("X-Auth-Token", matching("c6e6fd3a26313eb250e1019519af33e743808f5bb50428ae5423b8ee278e6fa5"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/Facility.json"))));

        Facility facility = hrm.getFacility("10000001").toBlocking().first();

        assertThat(facility, is(notNullValue()));
        assertThat(facility.getFacilityId(), is("10000001"));
        assertEquals(1, facility.getCatchments().size());
        assertEquals("302601", facility.getCatchments().get(0));

    }

}