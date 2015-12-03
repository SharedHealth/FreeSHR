package org.freeshr.events;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.TimeUuidUtil;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.freeshr.events.EncounterEvent.ENCOUNTER_MERGED_CATEGORY_PREFIX;
import static org.freeshr.events.EncounterEvent.ENCOUNTER_UPDATED_CATEGORY_PREFIX;
import static org.freeshr.events.EncounterEvent.LATEST_UPDATE_EVENT_CATEGORY_PREFIX;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.*;


public class EncounterEventTest {

    @Test
    public void encounterIsFurtherEditedIfTheEncounterUpdateTimeIsAfterTheEventGeneratedTime() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        DateTime now = DateTime.now();
        Date fiveSecondsLater = now.plusSeconds(5).toDate();
        encounterBundle.setUpdatedAt(fiveSecondsLater);


        EncounterEvent encounterEvent = new EncounterEvent(encounterBundle, now.toDate(), null);

        assertTrue(encounterEvent.isEncounterFurtherEdited());

    }

    @Test
    public void encounterIsNotFurtherEditedIfTheEncounterUpdateTimeMatchesTheEventGeneratedTime() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        Date now = DateTime.now().toDate();
        encounterBundle.setUpdatedAt(now);

        EncounterEvent encounterEvent = new EncounterEvent(encounterBundle, now, null);

        assertFalse(encounterEvent.isEncounterFurtherEdited());

    }

    @Test
    public void shouldAddEncounterUpdateTimeToCategories() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        DateTime encounterUpdateEventDate = new DateTime(2015, 05, 06, 9, 30);
        encounterBundle.setUpdatedAt(encounterUpdateEventDate.toDate());
        encounterBundle.setReceivedAt(encounterUpdateEventDate.minusHours(1).toDate());

        EncounterEvent encounterUpdateEvent = new EncounterEvent(encounterBundle, new Date(), null);
        ArrayList<String> categories = encounterUpdateEvent.getCategories();

        assertEquals(2, categories.size());
        assertEquals("encounter", categories.get(0));
        assertThat(ENCOUNTER_UPDATED_CATEGORY_PREFIX + DateUtil.toISOString(encounterUpdateEventDate.toDate()), isIn(categories));

    }

    @Test
    public void shouldAddEncounterFurtherEditedCategoryIfTheEventGeneratedTimeIsBeforeUpdateTime() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        Date may6 = new DateTime(2015, 05, 06, 9, 30).toDate();
        Date may5 = new DateTime(2015, 05, 05, 9, 30).toDate();
        encounterBundle.setUpdatedAt(may6);
        encounterBundle.setReceivedAt(may5);

        EncounterEvent encounterUpdateEvent = new EncounterEvent(encounterBundle, may5, null);
        ArrayList<String> categories = encounterUpdateEvent.getCategories();

        assertEquals(3, categories.size());
        assertEquals("encounter", categories.get(0));
        assertThat(ENCOUNTER_UPDATED_CATEGORY_PREFIX + DateUtil.toISOString(may6), isIn(categories));
        assertThat(LATEST_UPDATE_EVENT_CATEGORY_PREFIX + TimeUuidUtil.uuidForDate(may6), isIn(categories));

    }

    @Test
    public void shouldAddCategoryIfTheEncounterIsMerged() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        Date encounterCreateEventDate = new DateTime(2015, 05, 06, 9, 30).toDate();
        encounterBundle.setUpdatedAt(encounterCreateEventDate);
        encounterBundle.setReceivedAt(encounterCreateEventDate);

        Date encounterMergeEventDate = new DateTime(2015, 06, 06, 9, 30).toDate();

        EncounterEvent encounterMergeEvent = new EncounterEvent(encounterBundle, encounterMergeEventDate, encounterMergeEventDate);
        ArrayList<String> categories = encounterMergeEvent.getCategories();

        assertEquals(3, categories.size());
        assertEquals("encounter", categories.get(0));
        assertThat(ENCOUNTER_MERGED_CATEGORY_PREFIX + DateUtil.toISOString(encounterMergeEventDate),isIn(categories));
        assertThat(ENCOUNTER_UPDATED_CATEGORY_PREFIX + DateUtil.toISOString(encounterCreateEventDate),isIn(categories));
    }
}