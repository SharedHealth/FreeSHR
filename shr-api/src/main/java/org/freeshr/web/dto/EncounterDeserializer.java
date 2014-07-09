package org.freeshr.web.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.hl7.fhir.instance.model.Encounter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EncounterDeserializer extends JsonDeserializer<List<Encounter>> {
    private org.hl7.fhir.instance.formats.JsonParser encounterParser = new org.hl7.fhir.instance.formats.JsonParser();

    @Override
    public List<Encounter> deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        List<Encounter> encounters = new ArrayList<Encounter>();
        final TreeNode treeNode = jp.readValueAsTree();
        int size = treeNode.size();
        for (int i =0; i<size; i++) {
            String json = treeNode.get(i).toString();
            Encounter encounter = parse(json);
            encounters.add(encounter);
        }
        return encounters;
    }

    private Encounter parse(String json) {
        try {
            return (Encounter) encounterParser.parse(new ByteArrayInputStream(json.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
