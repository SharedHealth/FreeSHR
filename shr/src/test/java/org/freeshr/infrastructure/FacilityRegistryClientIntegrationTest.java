package org.freeshr.infrastructure;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Facility;
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

    @Autowired
    private SHRProperties shrProperties;

    @Test
    public void shouldFetchFacility() throws ExecutionException, InterruptedException {
        givenThat(get(urlEqualTo("/facilities/10000069.json"))
                .withHeader("client_id", matching(shrProperties.getIdPClientId()))
                .withHeader("X-Auth-Token", matching(shrProperties.getIdPAuthToken()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/facility10000069.json"))));


        Facility facility = hrm.getFacility("10000069").toBlocking().first();

        assertThat(facility, is(notNullValue()));
        assertThat(facility.getFacilityId(), is("10000069"));
        assertEquals(1, facility.getCatchments().size());
        assertEquals("302618", facility.getCatchments().get(0));

    }

}