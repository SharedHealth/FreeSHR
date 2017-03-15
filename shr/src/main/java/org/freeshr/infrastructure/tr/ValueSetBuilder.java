package org.freeshr.infrastructure.tr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class ValueSetBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ValueSetBuilder.class);

    public ValueSet deSerializeValueSet(String content, String theSystem) throws IOException {
        JsonNode root = new ObjectMapper().readTree(content);
        TextNode identifier = (TextNode) root.get("identifier");
        TextNode name = (TextNode) root.get("name");
        TextNode description = (TextNode) root.get("description");
        TextNode status = (TextNode) root.get("status");
        ValueSet valueSet = new ValueSet();
        String defaultName = getNameFromSystem(theSystem);
        if (identifier != null) {
            valueSet.setId(identifier.asText());
        }
        if (name != null) {
            valueSet.setName(name.asText(defaultName));
        }
        if (description != null) {
            valueSet.setDescription(description.asText(defaultName));
        }
        if (status != null) {
            valueSet.setStatus(getConformanceResourceStatus(status.asText("draft")));
        }
        JsonNode codeSystemRoot = root.get("codeSystem");
        TextNode system = (TextNode) codeSystemRoot.get("system");
        BooleanNode caseSensitive = (BooleanNode) codeSystemRoot.get("caseSensitive");
        ArrayNode conceptList = (ArrayNode) codeSystemRoot.get("concept");
        org.hl7.fhir.instance.model.ValueSet.ValueSetCodeSystemComponent codeSystemComponent = new org.hl7.fhir.instance.model.ValueSet.ValueSetCodeSystemComponent();
        valueSet.setCodeSystem(codeSystemComponent);
        if (system != null) {
            codeSystemComponent.setSystem(system.asText(theSystem));
        }
        if (caseSensitive != null) {
            codeSystemComponent.setCaseSensitive(caseSensitive.asBoolean(true));
        }
        for (JsonNode conceptDef : conceptList) {
            TextNode code = (TextNode) conceptDef.get("code");
            TextNode display = (TextNode) conceptDef.get("display");
            TextNode definition = (TextNode) conceptDef.get("definition");
            if (code != null) {
                org.hl7.fhir.instance.model.ValueSet.ConceptDefinitionComponent definitionComponent = codeSystemComponent.addConcept();
                definitionComponent.setCode(code.asText());
                if (display != null) {
                    definitionComponent.setDisplay(display.asText());
                }
                if (definition != null) {
                    definitionComponent.setDefinition(definition.asText());
                }
            }
        }
        return valueSet;
    }

    private String getNameFromSystem(String theSystem) {
        try {
            URI uri = new URI(theSystem);
            String path = uri.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (URISyntaxException e) {
            logger.error("Invalid URI specified for ValueSet:" + theSystem);
        }
        return "";

    }

    private Enumerations.ConformanceResourceStatus getConformanceResourceStatus(String code) {
        try {
            return Enumerations.ConformanceResourceStatus.fromCode(code.toLowerCase());
        } catch (Exception e) {
            return Enumerations.ConformanceResourceStatus.DRAFT;
        }
    }

}
