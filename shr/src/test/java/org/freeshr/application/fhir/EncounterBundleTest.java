package org.freeshr.application.fhir;

import org.freeshr.utils.Confidentiality;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EncounterBundleTest {

    @Test
    public void shouldCheckEncounterConfidentiality() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        encounterBundle.setPatientConfidentiality(Confidentiality.Normal);

        encounterBundle.setEncounterConfidentiality(Confidentiality.Unrestricted);
        assertFalse(encounterBundle.isConfidential());

        encounterBundle.setEncounterConfidentiality(Confidentiality.Low);
        assertFalse(encounterBundle.isConfidential());

        encounterBundle.setEncounterConfidentiality(Confidentiality.Moderate);
        assertFalse(encounterBundle.isConfidential());

        encounterBundle.setEncounterConfidentiality(Confidentiality.Normal);
        assertFalse(encounterBundle.isConfidential());

        encounterBundle.setEncounterConfidentiality(Confidentiality.Restricted);
        assertTrue(encounterBundle.isConfidential());

        encounterBundle.setEncounterConfidentiality(Confidentiality.VeryRestricted);
        assertTrue(encounterBundle.isConfidential());
    }

    @Test
    public void shouldReturnEncounterConfidentialityConsideringPatientConfidentiality() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        encounterBundle.setEncounterConfidentiality(Confidentiality.Normal);

        encounterBundle.setPatientConfidentiality(Confidentiality.Unrestricted);
        assertFalse(encounterBundle.isConfidential());

        encounterBundle.setPatientConfidentiality(Confidentiality.Low);
        assertFalse(encounterBundle.isConfidential());

        encounterBundle.setPatientConfidentiality(Confidentiality.Moderate);
        assertFalse(encounterBundle.isConfidential());

        encounterBundle.setPatientConfidentiality(Confidentiality.Normal);
        assertFalse(encounterBundle.isConfidential());

        encounterBundle.setPatientConfidentiality(Confidentiality.Restricted);
        assertTrue(encounterBundle.isConfidential());

        encounterBundle.setPatientConfidentiality(Confidentiality.VeryRestricted);
        assertTrue(encounterBundle.isConfidential());
    }
}