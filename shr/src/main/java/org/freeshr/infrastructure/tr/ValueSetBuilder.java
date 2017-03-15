package org.freeshr.infrastructure.tr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ValueSetBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ValueSetBuilder.class);

    public CodeSystem deserializeValueSetAndGetCodeSystem(String content, String theSystem) throws IOException {
        JsonNode root = new ObjectMapper().readTree(content);
        JsonNode codeSystemRoot = root.get("codeSystem");
        TextNode system = (TextNode) codeSystemRoot.get("system");
        BooleanNode caseSensitive = (BooleanNode) codeSystemRoot.get("caseSensitive");
        ArrayNode conceptList = (ArrayNode) codeSystemRoot.get("concept");
        CodeSystem codeSystem = new CodeSystem();
        if (system != null) {
            codeSystem.setUrl(system.asText(theSystem));
        }
        if (caseSensitive != null) {
            codeSystem.setCaseSensitive(caseSensitive.asBoolean(true));
        }
        for (JsonNode conceptDef : conceptList) {
            TextNode code = (TextNode) conceptDef.get("code");
            TextNode display = (TextNode) conceptDef.get("display");
            TextNode definition = (TextNode) conceptDef.get("definition");
            if (code != null) {
                CodeSystem.ConceptDefinitionComponent definitionComponent = codeSystem.addConcept();
                definitionComponent.setCode(code.asText());
                if (display != null) {
                    definitionComponent.setDisplay(display.asText());
                }
                if (definition != null) {
                    definitionComponent.setDefinition(definition.asText());
                }
            }
        }
        return codeSystem;
    }
}
