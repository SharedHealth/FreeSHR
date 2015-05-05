package org.freeshr.events;

import org.freeshr.application.fhir.EncounterBundle;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EncounterEventTest {

    @Test
    public void encounterBundleIsEditedIfUpdatedAfterReceived() throws Exception {
        EncounterEvent encounterEvent = new EncounterEvent();
        EncounterBundle encounterBundle = new EncounterBundle();
        DateTime now= DateTime.now();
        Date fiveSecondsLater = now.plusSeconds(5).toDate();
        encounterBundle.setReceivedAt(now.toDate());

        encounterEvent.setUpdatedAt(fiveSecondsLater);
        encounterEvent.setEncounterBundle(encounterBundle);

        assertTrue(encounterEvent.isUpdateEvent());

    }

    @Test
    public void encounterBundleIsNotIfJustReceived() throws Exception {
        EncounterEvent encounterEvent = new EncounterEvent();
        EncounterBundle encounterBundle = new EncounterBundle();
        Date now= DateTime.now().toDate();
        encounterBundle.setReceivedAt(now);

        encounterEvent.setUpdatedAt(now);
        encounterEvent.setEncounterBundle(encounterBundle);

        assertFalse(encounterEvent.isUpdateEvent());

    }
}