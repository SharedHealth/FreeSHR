package org.freeshr.validations.bundle;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.config.SHRProperties;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.FhirMessageFilter;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.ValidationSubject;
import org.freeshr.validations.providerIdentifiers.ClinicalResourceProviderIdentifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.freeshr.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.freeshr.utils.HttpUtil.CLIENT_ID_KEY;
import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
@TestPropertySource(properties = {"PROVIDER_REGISTRY_URL=http://localhost:9997/providers"})
public class ProviderValidatorTest {

    ProviderValidator providerValidator;

    @Autowired
    FhirFeedUtil fhirFeedUtil;

    FhirMessageFilter fhirMessageFilter;

    @Autowired
    List<ClinicalResourceProviderIdentifier> clinicalResourceProviderIdentifiers;
    @Autowired
    private SHRProperties shrProperties;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Before
    public void setup() {
        initMocks(this);
        fhirMessageFilter = new FhirMessageFilter();
        providerValidator = new ProviderValidator(clinicalResourceProviderIdentifiers, shrProperties);

        givenThat(get(urlPathMatching("/providers/19.json"))
                .willReturn(aResponse()
                        .withHeader(AUTH_TOKEN_KEY, shrProperties.getIdPAuthToken())
                        .withHeader(CLIENT_ID_KEY, shrProperties.getIdPClientId())
                        .withStatus(200)));

    }

    @Test
    public void shouldValidateEncounterWithValidProvider() throws Exception {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml"), fhirFeedUtil.getFhirContext());
        List<ShrValidationMessage> validationMessages = providerValidator.validate(getBundleContext(bundle));
        assertTrue(validationMessages.isEmpty());
    }

    @Test
    public void shouldFailEncounterWithInvalidProvider() throws Exception {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_invalid_participants_and_provider.xml"), fhirFeedUtil.getFhirContext());
        List<ShrValidationMessage> validationMessages = providerValidator.validate(getBundleContext(bundle));
        assertFailureFromResponseErrors("urn:c41cabed-3c47-4260-bd70-ac4893b97ee8", "Invalid Provider URL in Encounter:urn:uuid:dd4d51ac-d4b6-42e4-8b50-fa88af41a3e3",
                validationMessages);
        assertFailureFromResponseErrors("urn:a86018fa-e15d-4004-85bc-a1ee713dc923", "Invalid Provider URL in Condition:urn:uuid:04e9f317-680c-4ff1-9942-bcb5e2b5243b",
                validationMessages);
    }

    @Test
    public void shouldValidateEncounterWithoutAnyProvider() throws Exception {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_without_participant.xml"), fhirFeedUtil.getFhirContext());
        List<ShrValidationMessage> validationMessages = providerValidator.validate(getBundleContext(bundle));
        assertTrue(validationMessages.isEmpty());
    }

    private ValidationSubject<Bundle> getBundleContext(final Bundle bundle) {
        return new ValidationSubject<Bundle>() {
            @Override
            public Bundle extract() {
                return bundle;
            }
        };
    }

    private void assertFailureFromResponseErrors(String fieldName, String reason, List<ShrValidationMessage> validationMessages) {
        for (ShrValidationMessage msg : validationMessages) {
            if (msg.getMessage().equals(reason)) {
                assertEquals(reason, msg.getMessage());
                return;
            }
        }
        fail(String.format("Couldn't find expected error with fieldName [%s] reason [%s]", fieldName, reason));
    }
}