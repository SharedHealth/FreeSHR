package org.freeshr.application.fhir;

import org.freeshr.utils.Confidentiality;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EncounterBundleTest {

    @Test
    public void shouldReturnEncounterConfidentiality() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        encounterBundle.setPatientConfidentiality(Confidentiality.Normal);

        encounterBundle.setEncounterConfidentiality(Confidentiality.Unrestricted);
        assertFalse(encounterBundle.isConfidentialEncounter());

        encounterBundle.setEncounterConfidentiality(Confidentiality.Low);
        assertFalse(encounterBundle.isConfidentialEncounter());

        encounterBundle.setEncounterConfidentiality(Confidentiality.Moderate);
        assertFalse(encounterBundle.isConfidentialEncounter());

        encounterBundle.setEncounterConfidentiality(Confidentiality.Normal);
        assertFalse(encounterBundle.isConfidentialEncounter());

        encounterBundle.setEncounterConfidentiality(Confidentiality.Restricted);
        assertTrue(encounterBundle.isConfidentialEncounter());

        encounterBundle.setEncounterConfidentiality(Confidentiality.VeryRestricted);
        assertTrue(encounterBundle.isConfidentialEncounter());
    }

    @Test
    public void shouldReturnEncounterConfidentialityConsideringPatientConfidentiality() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        encounterBundle.setEncounterConfidentiality(Confidentiality.Normal);

        encounterBundle.setPatientConfidentiality(Confidentiality.Unrestricted);
        assertFalse(encounterBundle.isConfidentialEncounter());

        encounterBundle.setPatientConfidentiality(Confidentiality.Low);
        assertFalse(encounterBundle.isConfidentialEncounter());

        encounterBundle.setPatientConfidentiality(Confidentiality.Moderate);
        assertFalse(encounterBundle.isConfidentialEncounter());

        encounterBundle.setPatientConfidentiality(Confidentiality.Normal);
        assertFalse(encounterBundle.isConfidentialEncounter());

        encounterBundle.setPatientConfidentiality(Confidentiality.Restricted);
        assertTrue(encounterBundle.isConfidentialEncounter());

        encounterBundle.setPatientConfidentiality(Confidentiality.VeryRestricted);
        assertTrue(encounterBundle.isConfidentialEncounter());
    }
}