package org.freeshr.application.fhir;


import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://www.w3.org/2005/Atom", name = "feed")
public class EncounterBundle {

    private String encounterId;
    private String healthId;
    private String date;

    private EncounterContent encounterContent;

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public EncounterContent getEncounterContent() {
        return encounterContent;
    }

    public void setEncounterContent(String content) {
        this.encounterContent = new EncounterContent(content);
    }
}
