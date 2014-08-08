package org.freeshr.application.fhir;

import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.ParserBase;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Resource;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.apache.commons.lang.StringUtils.deleteWhitespace;
import static org.freeshr.application.fhir.EncounterFunctions.isDiagnosis;
import static org.freeshr.application.fhir.EncounterFunctions.toResource;
import static org.freeshr.utils.CollectionUtils.Fn;
import static org.freeshr.utils.CollectionUtils.filter;
import static org.freeshr.utils.CollectionUtils.map;


public class EncounterContent {
    private final String json;
    private final ParserBase.ResourceOrFeed resourceOrFeed;


    public EncounterContent(String json) {
        try {
            this.json = deleteWhitespace(json);
            this.resourceOrFeed = new JsonParser().parseGeneral(new ByteArrayInputStream(json.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("error parsing the json string");
        }
    }

    @Override
    public String toString() {
        return json;
    }

    public List<Resource> resources() {
        return map(resourceOrFeed.getFeed().getEntryList(), toResource);
    }

    public List<Condition> conditions() {
        return map(filter(resources(), EncounterFunctions.isCondition), new Fn<Resource, Condition>() {
            public Condition call(Resource input) {
                return (Condition) input;
            }
        });
    }

    public List<Condition> allDiagnosis() {
        return filter(conditions(), isDiagnosis);
    }
}
