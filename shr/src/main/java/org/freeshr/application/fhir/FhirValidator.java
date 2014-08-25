package org.freeshr.application.fhir;


import org.apache.log4j.Logger;
import org.freeshr.config.SHRProperties;
import org.freeshr.utils.CollectionUtils;
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
import java.util.ArrayList;
import java.util.List;

import static org.freeshr.utils.CollectionUtils.filter;
import static org.freeshr.utils.Lambda.throwIfNot;

@Component
public class FhirValidator {

    private Logger logger = Logger.getLogger(FhirValidator.class);

    private TRConceptLocator trConceptLocator;
    private SHRProperties shrProperties;

    @Autowired
    public FhirValidator(TRConceptLocator trConceptLocator, SHRProperties shrProperties) {
        this.trConceptLocator = trConceptLocator;
        this.shrProperties = shrProperties;
    }

    public boolean validate(String sourceXML) {
        try {
            validate(sourceXML, shrProperties.getValidationFilePath());
            return true;
        } catch (Exception e) {
            logger.warn(e);
            return false;
        }
    }

    private void validate(String sourceXml, String definitionsZipPath) throws Exception {
        List<ValidationMessage> outputs = new ArrayList<>();
        outputs.addAll(validateDocument(definitionsZipPath, sourceXml));
        outputs = filterMessagesHavingPriorityGreaterThan(outputs, OperationOutcome.IssueSeverity.warning);
        throwIfNot(outputs.isEmpty(), new InvalidEncounter(new Error(null, null)));
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

    private List<ValidationMessage> filterMessagesHavingPriorityGreaterThan(List<ValidationMessage> outputs, final OperationOutcome.IssueSeverity expectedPriority) {
        return filter(outputs, new CollectionUtils.Fn<ValidationMessage, Boolean>() {
            @Override
            public Boolean call(ValidationMessage input) {
                return expectedPriority.compareTo(input.getLevel()) >= 0;
            }
        });
    }
}
