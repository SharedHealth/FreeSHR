package org.freeshr.events;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.utils.TimeUuidUtil;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class EncounterEventTest {

    @Test
    public void encounterIsFurtherEditedIfTheEncounterUpdateTimeIsAfterTheEventGeneratedTime() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        DateTime now= DateTime.now();
        Date fiveSecondsLater = now.plusSeconds(5).toDate();
        encounterBundle.setUpdatedAt(fiveSecondsLater);


        EncounterEvent encounterEvent = new EncounterEvent(now.toDate(), encounterBundle);

        assertTrue(encounterEvent.isEncounterFurtherEdited());

    }

    @Test
    public void encounterIsNotFurtherEditedIfTheEncounterUpdateTimeMatchesTheEventGeneratedTime() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        Date now= DateTime.now().toDate();
        encounterBundle.setUpdatedAt(now);

        EncounterEvent encounterEvent = new EncounterEvent(now, encounterBundle);

        assertFalse(encounterEvent.isEncounterFurtherEdited());

    }

    @Test
    public void shouldAddReplacedByCategoryIfThereAreFurtherEncounterEditEvents() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        Date encounterCreateEventDate= new DateTime(2015, 05, 06, 9, 20).toDate();
        Date encounterEditEventDate= new DateTime(2015, 05, 06, 9, 30).toDate();
        encounterBundle.setUpdatedAt(encounterEditEventDate);
        encounterBundle.setReceivedAt(encounterCreateEventDate);

        EncounterEvent encounterCreateEvent = new EncounterEvent(encounterCreateEventDate, encounterBundle);

        ArrayList<String> categories = encounterCreateEvent.getCategories();
        assertEquals(2, categories.size());
        Assert.assertEquals(String.format("latest_update_event_id : %s", TimeUuidUtil.uuidForDate(encounterEditEventDate)), categories.get(1));

    }

    @Test
    public void shouldNotAddCategoryIfTheEncounterEditEventIsTheLatest() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        Date encounterCreateEventDate= new DateTime(2015, 05, 06, 9, 20).toDate();
        Date encounterEditEventDate= new DateTime(2015, 05, 06, 9, 30).toDate();
        encounterBundle.setUpdatedAt(encounterEditEventDate);
        encounterBundle.setReceivedAt(encounterCreateEventDate);

        EncounterEvent encounterUpdateEvent = new EncounterEvent(encounterEditEventDate, encounterBundle);

        ArrayList<String> categories = encounterUpdateEvent.getCategories();
        assertEquals(1, categories.size());

    }

    @Test
    public void shouldNotAddCategoryIfTheEncounterIsNotEdited() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        Date encounterCreateEventDate= new DateTime(2015, 05, 06, 9, 30).toDate();
        encounterBundle.setUpdatedAt(encounterCreateEventDate);
        encounterBundle.setReceivedAt(encounterCreateEventDate);

        EncounterEvent encounterCreateEvent = new EncounterEvent(encounterCreateEventDate, encounterBundle);

        ArrayList<String> categories = encounterCreateEvent.getCategories();
        assertEquals(1, categories.size());
    }
}