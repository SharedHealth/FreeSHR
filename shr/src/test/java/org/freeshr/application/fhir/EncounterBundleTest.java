package org.freeshr.application.fhir;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EncounterBundleTest {

    @Test
    public void encounterBundleIsEditedIfUpdatedAfterReceived() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        DateTime now= DateTime.now();
        Date fiveSecondsLater = now.plusSeconds(5).toDate();
        encounterBundle.setReceivedAt(now.toDate());
        encounterBundle.setUpdatedAt(fiveSecondsLater);

        assertTrue(encounterBundle.isEdited());

    }

    @Test
    public void encounterBundleIsNotIfJustReceived() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        Date now= DateTime.now().toDate();
        encounterBundle.setReceivedAt(now);
        encounterBundle.setUpdatedAt(now);

        assertFalse(encounterBundle.isEdited());

    }
}