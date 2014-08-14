package org.freeshr.application.fhir;

import org.freeshr.data.EncounterBundleData;
import org.freeshr.infrastructure.tr.TerminologyServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.util.concurrent.SettableListenableFuture;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterBundleTest {

    private final String REF_TERM_URL = "http://localhost:9997/openmrs/ws/rest/v1/tr/referenceterms/fa460ea6-04c7-45af-a6fa-5072e7caed40";
    private final String HL7_INALID_CATEGORY = "http://hl7.org/fhir/vs/condition-category-invalid";
    private final String HL7_VALID_CATEGORY = "http://hl7.org/fhir/vs/condition-category";

    @Mock
    private TerminologyServer terminologyServer;

    private SettableListenableFuture<Boolean> future(Boolean val){
        SettableListenableFuture<Boolean> future = new SettableListenableFuture<Boolean>();
        future.set(val);
        return future;
    }

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(terminologyServer.isValid(REF_TERM_URL, "S40")).thenReturn(future(true));
        when(terminologyServer.isValid(REF_TERM_URL, "S40-invalid")).thenReturn(future(false));
        when(terminologyServer.isValid(HL7_INALID_CATEGORY, "Diagnosis")).thenReturn(future(false));
        when(terminologyServer.isValid(HL7_VALID_CATEGORY, "Diagnosis")).thenReturn(future(true));
    }

    @Test
    public void shouldValidateDiagnosis(){
        EncounterBundleData.withValidEncounter("health-id").validate(terminologyServer);
        EncounterBundleData.withDiagnosisHavingAllValidRefSystems("health-id").validate(terminologyServer);
    }

    @Test(expected = InvalidEncounter.class)
    public void shouldRejectEncounterWithMissingSystemForDiagnosis(){
        EncounterBundleData.withDiagnosisHavingNoRefSystems("health-id").validate(terminologyServer);
    }

    @Test(expected = InvalidEncounter.class)
    public void shouldRejectEncountersWithDiagnosisHavingAllInvalidSystems(){
        EncounterBundleData.withDiagnosisHavingFewValidRefSystems("health-id").validate(terminologyServer);
    }

    @Test (expected = InvalidEncounter.class)
    public void shouldRejectInvalidDiagnosisCategory() throws Exception {
        EncounterBundleData.withInvalidDiagnosisCategory("health-Id").validate(terminologyServer);
    }
}
