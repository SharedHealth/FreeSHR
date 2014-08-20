package org.freeshr.application.fhir;


import org.hl7.fhir.instance.validation.InstanceValidator;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.freeshr.utils.Lambda.throwIfNot;

@Component
public class FhirValidator {

    private TRConceptLocator trConceptLocator;

    @Autowired
    public FhirValidator(TRConceptLocator trConceptLocator) {
        this.trConceptLocator = trConceptLocator;
    }

    public void validate(String sourceXml, String definitionsZipPath) throws Exception {
        List<ValidationMessage> outputs = new ArrayList<ValidationMessage>();
        outputs.addAll(validateDocument(definitionsZipPath, sourceXml));
        throwIfNot(outputs.isEmpty(), new InvalidEncounter(new Error("123", "")));
    }

    private List<ValidationMessage> validateDocument(String definitionsZipPath, String sourceXml) throws Exception {
        return new InstanceValidator(definitionsZipPath, null, trConceptLocator).validateInstance(document(sourceXml).getDocumentElement());
    }

    private Document document(String sourceXml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(sourceXml.getBytes()));
    }
}
