package org.freeshr.validations;

import org.freeshr.application.fhir.TRConceptLocator;
import org.freeshr.config.SHRProperties;
import org.hl7.fhir.instance.validation.InstanceValidator;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Component
public class FhirSchemaValidator implements Validator<String> {

    private final static Logger logger = LoggerFactory.getLogger(FhirSchemaValidator.class);
    private InstanceValidator instanceValidator;

    @Autowired
    public FhirSchemaValidator(TRConceptLocator trConceptLocator, SHRProperties shrProperties) throws Exception {
        //this.instanceValidator = new InstanceValidator(shrProperties.getValidationFilePath(), null, trConceptLocator);
    }

    private Document document(String sourceXml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(sourceXml.getBytes()));
    }


    @Override
    public List<ValidationMessage> validate(ValidationSubject<String> subject) {
        String sourceXml = subject.extract();
        try {
            //return instanceValidator.validateInstance(document(sourceXml).getDocumentElement());
        } catch (Exception e) {
            logger.debug(String.format("Error in validating schema.Cause: %s", e.getMessage()));
            throw new RuntimeException(e);
        }
        return null;
    }
}
