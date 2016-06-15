package org.freeshr.validations.bundle;


import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Composition;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.FamilyMemberHistory;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Procedure;
import ca.uhn.fhir.model.dstu2.resource.Specimen;
import org.freeshr.config.SHRProperties;
import org.freeshr.utils.StringUtils;
import org.freeshr.validations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.freeshr.validations.ValidationMessages.HEALTH_ID_NOT_MATCH;
import static org.freeshr.validations.ValidationMessages.HEALTH_ID_NOT_PRESENT_IN_COMPOSITION;

@Component
public class HealthIdValidator implements ShrValidator<EncounterValidationContext> {

    private static final Logger logger = LoggerFactory.getLogger(HealthIdValidator.class);
    private SHRProperties shrProperties;
    //match all urls that have /api/*/patients, 2nd groups contains the variable
    private Pattern healthIdReferencePattern = Pattern.compile("(^(http|https)://(?<host>.+)\\/api\\/)(\\w+)(\\/patients\\/(?<hid>.+))");

    @Autowired
    public HealthIdValidator(SHRProperties shrProperties) {
        this.shrProperties = shrProperties;
    }

    @Override
    public List<ShrValidationMessage> validate(ValidationSubject<EncounterValidationContext> validationSubject) {
        EncounterValidationContext validationContext = validationSubject.extract();
        Bundle bundle = validationContext.getBundle();
        String expectedHealthId = validationContext.getHealthId();
        List<ShrValidationMessage> validationMessages = new ArrayList<>();
        for (Bundle.Entry entry : bundle.getEntry()) {
            IResource resource = entry.getResource();
            if (!PatientReferenceIdentifier.canIdentify(resource)) {
                continue;
            }
            ResourceReferenceDt patientRef = PatientReferenceIdentifier.identifyPatientReference(resource);
            String patientRefUrl = getPatientRefUrl(patientRef);

            if ((resource instanceof  Composition) && (patientRefUrl == null)) {
                logger.error(String.format("Encounter failed for %s", HEALTH_ID_NOT_PRESENT_IN_COMPOSITION));
                ShrValidationMessage message = new ShrValidationMessage(Severity.ERROR, "f:Composition/f:subject",
                        "invalid", "Composition:" + HEALTH_ID_NOT_PRESENT_IN_COMPOSITION);
                validationMessages.add(message);
                return validationMessages;
            }

            String healthIdFromUrl = validateAndIdentifyPatientId(patientRefUrl, expectedHealthId);
            if (healthIdFromUrl == null) {
                logger.debug(String.format("Encounter failed for %s", HEALTH_ID_NOT_MATCH));
                ShrValidationMessage message = new ShrValidationMessage(Severity.ERROR,
                        String.format("f:%s/f:patient", resource.getResourceName()),"invalid",
                        patientRef.getReference().getValue()  + ":" + HEALTH_ID_NOT_MATCH);
                validationMessages.add(message);
            }
        }
        return validationMessages;
    }

    private String getPatientRefUrl(ResourceReferenceDt patientRef) {
        if ((patientRef == null) || (patientRef.getReference() == null)) return null;
        return patientRef.getReference().getValue();
    }

    public String validateAndIdentifyPatientId(String patientRefUrl, String healthId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(patientRefUrl)) return null;
        String expectedUrl = StringUtils.ensureSuffix(shrProperties.getPatientReferencePath(), "/") + healthId;
        Matcher actual = healthIdReferencePattern.matcher(patientRefUrl);
        Matcher expected = healthIdReferencePattern.matcher(expectedUrl);

        if (!actual.find() || !expected.find() || actual.groupCount() != 6) return null;
        if (expected.group("host").equalsIgnoreCase(actual.group("host")) && expected.group("hid").equalsIgnoreCase(actual.group("hid")))
            return actual.group("hid");
        return null;
    }

    public static class PatientReferenceIdentifier {
        public static List<Class<? extends BaseResource>> supportedTypes = Arrays.asList(Composition.class, Encounter.class, Condition.class, FamilyMemberHistory.class,
                Observation.class, DiagnosticOrder.class, DiagnosticReport.class, Specimen.class,
                Immunization.class, Procedure.class, MedicationOrder.class);

        public static boolean canIdentify(IResource resource) {
            return supportedTypes.contains(resource.getClass());
        }

        public static ResourceReferenceDt identifyPatientReference(IResource resource) {
            ResourceReferenceDt patientRef = null;
            if (resource instanceof Composition) {
                patientRef = ((Composition) resource).getSubject();
            } else if (resource instanceof Encounter) {
                patientRef = ((Encounter) resource).getPatient();
            } else if (resource instanceof Condition) {
                patientRef = ((Condition) resource).getPatient();
            } else if (resource instanceof FamilyMemberHistory) {
                patientRef = ((FamilyMemberHistory) resource).getPatient();
            } else if (resource instanceof Observation) {
                patientRef = ((Observation) resource).getSubject();
            } else if (resource instanceof DiagnosticOrder) {
                patientRef = ((DiagnosticOrder) resource).getSubject();
            } else if (resource instanceof DiagnosticReport) {
                patientRef = ((DiagnosticReport) resource).getSubject();
            } else if (resource instanceof Specimen) {
                patientRef = ((Specimen) resource).getSubject();
            } else if (resource instanceof Immunization) {
                patientRef = ((Immunization) resource).getPatient();
            } else if (resource instanceof Procedure) {
                patientRef = ((Procedure) resource).getSubject();
            } else if (resource instanceof MedicationOrder) {
                patientRef = ((MedicationOrder) resource).getPatient();
            }
            return patientRef;
        }

    }

}
