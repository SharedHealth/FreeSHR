package org.freeshr.validations.bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.validation.*;
import org.freeshr.utils.FileUtil;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.validations.*;
import org.freeshr.validations.bundle.ResourceValidator;
import org.freeshr.validations.resource.ConditionValidator;
import org.freeshr.validations.resource.ImmunizationValidator;
import org.freeshr.validations.resource.MedicationPrescriptionValidator;
import org.freeshr.validations.resource.ProcedureValidator;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.validation.IResourceValidator;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.junit.*;
import org.mockito.Mock;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

public class ResourceValidatorTest {

    private ResourceValidator resourceValidator;
    FhirFeedUtil fhirFeedUtil;

    @Mock
    MedicationPrescriptionValidator medicationPrescriptionValidator;
    @Mock
    ImmunizationValidator immunizationValidator;
    @Mock
    ProcedureValidator procedureValidator;

    @Before
    public void setup() {
        initMocks(this);
        resourceValidator = new ResourceValidator(new ConditionValidator(), medicationPrescriptionValidator, immunizationValidator,
                procedureValidator);
        fhirFeedUtil = new FhirFeedUtil();
    }

    @Ignore
    @Test
    public void shouldVerifyThatEveryCodeableConceptInAConditionIsCoded() {
        //This example has 4 entries:
        // 1. Without code and system for diagnosis, 2. With valid code and system for diagnosis,
        // 3. With only code for daiagnosis
        // 4. With non-coded severity, location, evidence etc.


        final String xml = FileUtil.asString("xmls/encounters/coded_and_noncoded_diagnosis.xml");
        List<ValidationMessage> messages = resourceValidator.validate(new ValidationSubject<Bundle>() {
            @Override
            public Bundle extract() {
                return fhirFeedUtil.deserialize(xml);
            }
        });
        assertThat(messages.size(), is(3));
        assertThat(messages.get(0).getLevel(), is(OperationOutcome.IssueSeverity.ERROR));
        assertThat(messages.get(0).getMessage(), is("Viral pneumonia 785857"));
        assertThat(messages.get(0).getType(), is(OperationOutcome.IssueType.UNKNOWN));
        assertThat(messages.get(1).getLevel(), is(OperationOutcome.IssueSeverity.ERROR));
        assertThat(messages.get(1).getMessage(), is("Viral pneumonia 785857"));
        assertThat(messages.get(1).getType(), is(OperationOutcome.IssueType.UNKNOWN));
        assertThat(messages.get(2).getLevel(), is(OperationOutcome.IssueSeverity.ERROR));
        assertThat(messages.get(2).getMessage(), is("Moderate"));
        assertThat(messages.get(2).getType(), is(OperationOutcome.IssueType.UNKNOWN));
    }

    @Ignore
    @Test
    public void shouldValidateConditionDiagnosisWithAllValidComponents() {
        final String xml = FileUtil.asString("xmls/encounters/valid_diagnosis.xml");
        List<ValidationMessage> messages = resourceValidator.validate(new ValidationSubject<Bundle>() {
            @Override
            public Bundle extract() {
                return fhirFeedUtil.deserialize(xml);
            }
        });

        assertThat(messages.isEmpty(), is(true));
    }

    @Ignore
    @Test
    public void shouldAllowResourceTypeConditionWithCodedAsWellAsNonCodedForAnythingOtherThanDiagnosis() {
        final String xml = FileUtil.asString("xmls/encounters/other_conditions.xml");
        List<ValidationMessage> messages = resourceValidator.validate(new ValidationSubject<Bundle>() {
            @Override
            public Bundle extract() {
                return fhirFeedUtil.deserialize(xml);
            }
        });
        assertThat(messages.isEmpty(), is(true));
    }

    @Ignore
    @Test
    public void shouldAcceptDiagnosisIfAtLeaseOneReferenceTermIsRight() {
        final String xml = FileUtil.asString("xmls/encounters/multiple_coded_diagnosis.xml");
        List<ValidationMessage> messages = resourceValidator.validate(new ValidationSubject<Bundle>() {
            @Override
            public Bundle extract() {
                return fhirFeedUtil.deserialize(xml);
            }
        });
        assertThat(messages.isEmpty(), is(true));
    }

    @Ignore
    @Test
    public void shouldValidateBundle() {
        String content = FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_all_resources.xml");
        FhirContext fhirContext = FhirContext.forDstu2();
        IResource bundle = (IResource) fhirContext.newXmlParser().parseResource(content);
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        FhirValidator fhirValidator = fhirContext.newValidator();
        fhirValidator.registerValidatorModule(instanceValidator);
        ValidationSupportChain support = new ValidationSupportChain(new DefaultProfileValidationSupport(), getCustomValidationSupport());
        instanceValidator.setValidationSupport(support);
        instanceValidator.setBestPracticeWarningLevel(IResourceValidator.BestPracticeWarningLevel.Warning);
        ValidationResult result = fhirValidator.validateWithResult(bundle);
        for (SingleValidationMessage singleValidationMessage : result.getMessages()) {
            System.out.println(singleValidationMessage.getSeverity() + ":" + singleValidationMessage.getMessage());
        }

    }

    private IValidationSupport getCustomValidationSupport() {
        return new IValidationSupport() {
            @Override
            public ValueSet.ValueSetExpansionComponent expandValueSet(ValueSet.ConceptSetComponent theInclude) {
                return null;
            }

            @Override
            public ValueSet fetchCodeSystem(String theSystem) {
                return null;
            }

            @Override
            public <T extends IBaseResource> T fetchResource(FhirContext theContext, Class<T> theClass, String theUri) {
                return null;
            }

            @Override
            public boolean isCodeSystemSupported(String theSystem) {
                if (theSystem.contains("/openmrs/ws/rest/v1/tr/referenceterms/")
                    ||
                   theSystem.contains("/openmrs/ws/rest/v1/tr/concepts/")) {
                    return true;
                }
                return false;
            }

            @Override
            public CodeValidationResult validateCode(String theCodeSystem, String theCode, String theDisplay) {
                if (theCodeSystem.contains("/openmrs/ws/rest/v1/tr/referenceterms/")
                        ||
                        theCodeSystem.contains("/openmrs/ws/rest/v1/tr/concepts/")) {
                    ValueSet.ConceptDefinitionComponent conceptDefinitionComponent = new ValueSet.ConceptDefinitionComponent();
                    conceptDefinitionComponent.setCode(theCode);
                    conceptDefinitionComponent.setDisplay(theDisplay);
                    conceptDefinitionComponent.setDefinition(theCodeSystem);
                    return new CodeValidationResult(conceptDefinitionComponent);
                }
                return new CodeValidationResult(OperationOutcome.IssueSeverity.INFORMATION, "Unknown code: " + theCodeSystem + " / " + theCode);

            }
        };
    }


}