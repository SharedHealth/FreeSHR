package org.freeshr.events;


import org.eclipse.persistence.oxm.annotations.XmlCDATA;
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

    public String getEncounterId() {
        return this.encounterBundle.getEncounterId();
    }

    public String getUpdatedDateISOString() {
        return DateUtil.toISOString(updatedAt);
    }

    public String getEventId(){
        return TimeUuidUtil.uuidForDate(updatedAt).toString();
    }

    @XmlCDATA
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
            if(isUpdateEvent()){
                categories.add(String.format("Updated since : %s", DateUtil.toISOString(getReceivedAt()) ));
            };
            return categories;
        }

    public boolean isUpdateEvent(){
        return updatedAt.after(getReceivedAt());
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setEncounterBundle(EncounterBundle encounterBundle) {
        this.encounterBundle = encounterBundle;
    }

    public String getHealthId() {
        return this.encounterBundle.getHealthId();
    }

    public Date getReceivedAt() {
        return this.encounterBundle.getReceivedAt();
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public boolean isConfidentialEncounter() {
        return this.encounterBundle.isConfidentialEncounter();
    }

    public EncounterBundle getEncounterBundle() {
        return encounterBundle;
    }
}
