package org.freeshr.application.fhir;


import org.freeshr.config.SHRProperties;
import org.hl7.fhir.instance.model.OperationOutcome;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.freeshr.utils.CollectionUtils.reduce;

@Component
public class FhirValidator {

    private FhirMessageFilter fhirMessageFilter;
    private SHRValidator shrValidator;
    private TRConceptLocator trConceptLocator;
    private SHRProperties shrProperties;

    @Autowired
    public FhirValidator(TRConceptLocator trConceptLocator, SHRProperties shrProperties, FhirMessageFilter fhirMessageFilter) {
        this.trConceptLocator = trConceptLocator;
        this.shrProperties = shrProperties;
        this.fhirMessageFilter = fhirMessageFilter;
        this.shrValidator = new SHRValidator();
    }

    public EncounterValidationResponse validate(String sourceXML) {
        try {
            return validate(sourceXML, shrProperties.getValidationFilePath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private EncounterValidationResponse validate(String sourceXml, String definitionsZipPath) {
        List<ValidationMessage> outputs = new ArrayList<>();
        outputs.addAll(validateDocument(definitionsZipPath, sourceXml));
        return fhirMessageFilter.filterMessagesSevereThan(outputs, OperationOutcome.IssueSeverity.warning);
    }

    private List<ValidationMessage> validateDocument(String definitionsZipPath, String sourceXml) {
        try {
            List<ValidationMessage> validationMessages = new InstanceValidator(definitionsZipPath, null, trConceptLocator).validateInstance(document(sourceXml).getDocumentElement());
            validationMessages.addAll(shrValidator.validateCategories(sourceXml));
            return validationMessages;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Document document(String sourceXml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(sourceXml.getBytes()));
    }
}
