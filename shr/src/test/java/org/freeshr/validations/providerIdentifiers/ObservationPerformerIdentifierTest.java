package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import ca.uhn.fhir.model.dstu2.resource.Observation;
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
        assertTrue(observationPerformerIdentifier.validates(obs));
    }

    @Test
    public void shouldExtractProperObservationPerformerReferences() {
        Observation obs = new Observation();
        obs.setPerformer(Arrays.asList(new ResourceReferenceDt("http://127.0.0.1:9997/providers/18.json")));
        List<ResourceReferenceDt> providerReferences = observationPerformerIdentifier.getProviderReferences(obs);
        assertEquals(1, providerReferences.size());
        assertEquals("http://127.0.0.1:9997/providers/18.json", providerReferences.get(0).getReference().getValue());

    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        Immunization immunization = new Immunization();
        assertFalse(observationPerformerIdentifier.validates(immunization));
    }


}