package org.freeshr.application.fhir;


import org.freeshr.infrastructure.tr.TerminologyServer;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Condition;

import java.util.concurrent.ExecutionException;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.freeshr.application.fhir.InvalidEncounter.INVALID_DIAGNOSIS;

public class EncounterBundle {

    private String encounterId;
    private String healthId;
    private String date;
    private EncounterContent content;

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

    public EncounterContent getContent() {
        return content;
    }

    public void setContent(EncounterContent content) {
        this.content = content;
    }

    public void setContent(String content) {
        this.content = new EncounterContent(content);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EncounterBundle{");
        sb.append("encounterId='").append(encounterId).append('\'');
        sb.append(", healthId='").append(healthId).append('\'');
        sb.append(", date='").append(date).append('\'');
        //sb.append(", content='").append(content).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public void validate(TerminologyServer terminologyServer) throws ExecutionException, InterruptedException {
        for (Condition condition : content.allDiagnosis()) {
            validateDiagnosis(condition, terminologyServer);
        }
    }

    public void validateDiagnosis(Condition condition, TerminologyServer terminologyServer) throws ExecutionException, InterruptedException {
        for (Coding coding : condition.getCode().getCoding()) {
            if (coding.getSystem() != null
                    && isNotEmpty(coding.getSystem().getValue())
                    && !terminologyServer.isValid(coding.getSystem().getValue(), coding.getCodeSimple()).get()) {
                throw INVALID_DIAGNOSIS;
            }
        }
    }
}
