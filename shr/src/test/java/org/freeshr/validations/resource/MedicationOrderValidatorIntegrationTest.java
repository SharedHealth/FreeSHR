package org.freeshr.validations.resource;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.sf.ehcache.CacheManager;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.util.ValidationFailureTestHelper;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.ShrValidationMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.BundleHelper.parseResource;
import static org.freeshr.validations.resource.MedicationOrderValidator.MEDICATION_ORDER_MEDICATION_LOCATION;
import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
@TestPropertySource(properties = {"MCI_SERVER_URL=http://localhost:9997", "FACILITY_REGISTRY_URL=http://localhost:9997/facilities/", "PROVIDER_REGISTRY_URL=http://localhost:9997/providers/"})
public class MedicationOrderValidatorIntegrationTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private MedicationOrderValidator medicationOrderValidator;

    @Before
    public void setup() throws Exception {
        initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        CacheManager.getInstance().clearAll();
    }

    @Test
    public void shouldFailForInvalidMedication() throws Exception {
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/drugs/23d7e743-75bd-4a25-8f34-bd849bd50394"))
                .willReturn(aResponse()
                        .withStatus(404)));

        final FhirContext fhirContext = FhirContext.forDstu2();
        Bundle medicationOrderBundle = (Bundle) parseResource(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_scheduled_date.xml"), fhirContext);
        MedicationOrder medicationOrder = medicationOrderBundle.getAllPopulatedChildElementsOfType(MedicationOrder.class).get(0);
        List<ShrValidationMessage> shrValidationMessages = medicationOrderValidator.validate(medicationOrder);
        assertEquals(1, shrValidationMessages.size());
        ValidationFailureTestHelper.assertFailureFromShrValidationMessages(MEDICATION_ORDER_MEDICATION_LOCATION,
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/drugs/23d7e743-75bd-4a25-8f34-bd849bd50394], code[23d7e743-75bd-4a25-8f34-bd849bd50394]",
                shrValidationMessages);
    }

    @Test
    @Ignore
    public void shouldValidateMedicationQuantity() {
        //TODO
    }

    @Test
    @Ignore
    public void shouldValidateMedicationDispense() {
        //TODO
    }
}