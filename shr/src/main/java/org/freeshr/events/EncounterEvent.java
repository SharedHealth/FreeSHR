package org.freeshr.events;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.TimeUuidUtil;

import java.util.ArrayList;
import java.util.Date;

public class EncounterEvent {

    private Date updatedAt;
    private EncounterBundle encounterBundle;
    private ArrayList<String> categories = new ArrayList<String>(){{ add("encounter"); }};
    private String title = "Encounter";

    @JsonProperty("publishedDate")
    public String getUpdatedDateISOString() {
        return DateUtil.toISOString(updatedAt);
    }

    public String getId() {
        return TimeUuidUtil.uuidForDate(updatedAt).toString();
    }

    public String getContent() {
        return this.encounterBundle.getContent();
    }

    public String getLink() {
        return String.format("/patients/%s/encounters/%s", getHealthId(), getEncounterId());
    }

    public String getTitle() {
        return title + ":" + getEncounterId();
    }

    public ArrayList<String> getCategories() {
        if (isEncounterFurtherEdited()) {
            categories.add(String.format("latest_update_event_id : %s", TimeUuidUtil.uuidForDate(getEncounterLastUpdatedAt())));
        }
        return categories;
    }

    @JsonIgnore
    public String getEncounterId() {
        return this.encounterBundle.getEncounterId();
    }

    @JsonIgnore
    public boolean isEncounterFurtherEdited() {
        return getEncounterLastUpdatedAt().after(getUpdatedAt());
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
    public Date getUpdatedAt() {
        return updatedAt;
    }

    @JsonIgnore
    public boolean isConfidentialEncounter() {
        return this.encounterBundle.isConfidentialEncounter();
    }

    @JsonIgnore
    public EncounterBundle getEncounterBundle() {
        return encounterBundle;
    }

    public EncounterEvent(Date updatedAt, EncounterBundle encounterBundle) {
        this.updatedAt = updatedAt;
        this.encounterBundle = encounterBundle;
    }

}
