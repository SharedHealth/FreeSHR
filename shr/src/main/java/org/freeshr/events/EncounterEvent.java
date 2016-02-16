package org.freeshr.events;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.utils.Confidentiality;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.TimeUuidUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class EncounterEvent {

    public static final String ENCOUNTER_UPDATED_CATEGORY_PREFIX = "encounter_updated_at:";
    public static final String LATEST_UPDATE_EVENT_CATEGORY_PREFIX = "latest_update_event_id:";
    public static final String ENCOUNTER_MERGED_CATEGORY_PREFIX = "encounter_merged_at:";
    private static final String TITLE = "Encounter";
    private UUID eventId;
    private Date mergedAt;
    private EncounterBundle encounterBundle;
    private ArrayList<String> categories = new ArrayList<String>(){{ add("encounter"); }};

    public EncounterEvent(EncounterBundle encounterBundle, UUID eventId, Date mergedAt) {
        this.eventId = eventId;
        this.encounterBundle = encounterBundle;
        this.mergedAt = mergedAt;
    }

    @JsonProperty("publishedDate")
    public String getUpdatedDateISOString() {
        return DateUtil.toISOString(TimeUuidUtil.getDateFromUUID(eventId));
    }

    public String getId() {
        return eventId.toString();
    }

    public String getContent() {
        return this.encounterBundle.getContent();
    }

    public String getLink() {
        return String.format("/patients/%s/encounters/%s", getHealthId(), getEncounterId());
    }

    public String getTitle() {
        return TITLE + ":" + getEncounterId();
    }

    public ArrayList<String> getCategories() {
        categories.add(ENCOUNTER_UPDATED_CATEGORY_PREFIX +  DateUtil.toISOString(getEncounterLastUpdatedAt()));
        if (isEncounterFurtherEdited()) {
            final UUID updatedEventReference = this.encounterBundle.getUpdatedEventReference();
            if (updatedEventReference != null) {
                categories.add(LATEST_UPDATE_EVENT_CATEGORY_PREFIX + updatedEventReference.toString());
            } else {
                categories.add(LATEST_UPDATE_EVENT_CATEGORY_PREFIX + "unknown");
            }
        }
        if (getMergedAt() != null) {
            categories.add(ENCOUNTER_MERGED_CATEGORY_PREFIX + DateUtil.toISOString(getMergedAt()));
        }
        return categories;
    }

    @JsonIgnore
    public String getEncounterId() {
        return this.encounterBundle.getEncounterId();
    }

    @JsonIgnore
    public boolean isEncounterFurtherEdited() {
        return getEncounterLastUpdatedAt().after(getCreatedAt());
    }

    @JsonIgnore
    public String getHealthId() {
        return this.encounterBundle.getHealthId();
    }

    @JsonIgnore
    public Date getEncounterReceivedAt() {
        return this.encounterBundle.getReceivedAt();
    }

    @JsonIgnore
    public Date getEncounterLastUpdatedAt() {
        return this.encounterBundle.getUpdatedAt();
    }

    @JsonIgnore
    public Date getCreatedAt() {
        return TimeUuidUtil.getDateFromUUID(eventId);
    }

    @JsonIgnore
    public Date getMergedAt(){
        return mergedAt;
    }

    @JsonIgnore
    public boolean isConfidential() {
        return this.encounterBundle.isConfidential();
    }

    @JsonIgnore
    public Confidentiality getConfidentialityLevel() {
        return this.encounterBundle.getConfidentialityLevel();
    }

    @JsonIgnore
    public EncounterBundle getEncounterBundle() {
        return encounterBundle;
    }

}
