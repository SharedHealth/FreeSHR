package org.freeshr.validations.providerIdentifiers;

import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ObservationPerformerIdentifierTest {

    private ObservationPerformerIdentifier observationPerformerIdentifier;

    @Before
    public void setUp() {
        observationPerformerIdentifier = new ObservationPerformerIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeObservation() {
        Observation obs = new Observation();
        assertTrue(observationPerformerIdentifier.canValidate(obs));
    }

    @Test
    public void shouldExtractProperObservationPerformerReferences() {
        Observation obs = new Observation();
        obs.setPerformer(Arrays.asList(new Reference("http://127.0.0.1:9997/providers/18.json")));
        List<Reference> providerReferences = observationPerformerIdentifier.getProviderReferences(obs);
        assertEquals(1, providerReferences.size());
        assertEquals("http://127.0.0.1:9997/providers/18.json", providerReferences.get(0).getReference());

    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        Immunization immunization = new Immunization();
        assertFalse(observationPerformerIdentifier.canValidate(immunization));
    }


}
