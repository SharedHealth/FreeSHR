package org.freeshr.application.fhir;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.freeshr.infrastructure.tr.TerminologyServer;
import org.freeshr.utils.CollectionUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Condition;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.freeshr.application.fhir.EncounterFunctions.hasSystem;
import static org.freeshr.application.fhir.InvalidEncounter.invalidDiagnosis;
import static org.freeshr.application.fhir.InvalidEncounter.missingSystem;
import static org.freeshr.utils.CollectionUtils.filter;
import static org.freeshr.utils.CollectionUtils.find;
import static org.freeshr.utils.CollectionUtils.isEmpty;
import static org.freeshr.utils.CollectionUtils.not;
import static org.freeshr.utils.Lambda.throwIf;

public class EncounterBundle {

    private String encounterId;
    private String healthId;
    private String date;
    @JsonIgnore
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

    @JsonProperty("content")
    public JsonNode getContent() {
        try {
            return new ObjectMapper().readTree(this.encounterContent.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EncounterBundle{");
        sb.append("encounterId='").append(encounterId).append('\'');
        sb.append(", healthId='").append(healthId).append('\'');
        sb.append(", date='").append(date).append('\'');
        sb.append(", encounterContent='").append(encounterContent.toString()).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public void validate(TerminologyServer terminologyServer) {
        for (Condition condition : encounterContent.allDiagnosis()) {
            validateDiagnosis(condition, terminologyServer);
        }
    }

    private void validateDiagnosis(Condition diagnosis, final TerminologyServer terminologyServer) {
        validateCode(diagnosis.getCode(), terminologyServer);
        validateCode(diagnosis.getCategory(), terminologyServer);
    }

    private void validateCode(CodeableConcept concept, TerminologyServer terminologyServer) {
        throwIf(isEmpty(filter(concept.getCoding(), hasSystem)), missingSystem(concept.getTextSimple()));

        Coding invalidCategoryCode = find(concept.getCoding(), not(isValid(terminologyServer)));
        if (invalidCategoryCode != null) {
            throw invalidDiagnosis(invalidCategoryCode.getCodeSimple());
        }
    }

    private CollectionUtils.Fn<Coding, Boolean> isValid(final TerminologyServer terminologyServer) {
        return new CollectionUtils.Fn<Coding, Boolean>() {
            public Boolean call(Coding coding) {
                try {
                    return terminologyServer.isValid(coding.getSystem().getValue(), coding.getCodeSimple()).get();
                } catch (InterruptedException e) {
                    throw InvalidEncounter.systemError();
                } catch (ExecutionException e) {
                    throw InvalidEncounter.systemError();
                }
            }
        };
    }
}
