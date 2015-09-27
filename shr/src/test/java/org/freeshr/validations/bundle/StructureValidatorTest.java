package org.freeshr.validations.bundle;


import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.HIEFacilityValidator;
import org.freeshr.validations.ValidationSubject;
import org.freeshr.validations.bundle.StructureValidator;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class StructureValidatorTest {

    private FhirFeedUtil fhirFeedUtil;
    private StructureValidator structureValidator;
    @Mock
    HIEFacilityValidator hieFacilityValidator;

    @Before
    public void setup() {
        initMocks(this);
        fhirFeedUtil = new FhirFeedUtil();
        when(hieFacilityValidator.validate(anyString())).thenReturn(true);
        structureValidator = new StructureValidator(fhirFeedUtil, hieFacilityValidator);
    }

    @Test
    public void shouldAcceptAValidXmlWithOneEntryForEachSectionPresentInComposition() {
        final String xml = FileUtil.asString("xmls/encounters/diagnostic_order_valid.xml");
        List<ValidationMessage> validationMessages = structureValidator.validate(new ValidationSubject<Bundle>() {
            @Override
            public Bundle extract() {
                return fhirFeedUtil.deserialize(xml);
            }
        });
        assertThat(validationMessages.isEmpty(), is(true));
    }

    @Test
    public void shouldAcceptIfAuthorInCompositionIsAValidFacility() {
        final String xml = FileUtil.asString("xmls/encounters/diagnostic_order_valid.xml");
        when(hieFacilityValidator.validate(anyString())).thenReturn(true);
        List<ValidationMessage> validationMessages = structureValidator.validate(new ValidationSubject<Bundle>() {
            @Override
            public Bundle extract() {
                return fhirFeedUtil.deserialize(xml);
            }
        });
        assertThat(validationMessages.isEmpty(), is(true));
    }

    @Test
    public void shouldRejectIfCompositionHasInvalidAuthor() throws Exception {
        final String xml = FileUtil.asString("xmls/encounters/diagnostic_order_valid.xml");
        when(hieFacilityValidator.validate(anyString())).thenReturn(false);
        List<ValidationMessage> validationMessages = structureValidator.validate(new ValidationSubject<Bundle>() {
            @Override
            public Bundle extract() {
                return fhirFeedUtil.deserialize(xml);
            }
        });

        assertThat(validationMessages.isEmpty(), is(false));
        assertThat(validationMessages.size(), is(1));
        assertThat(validationMessages.get(0).getMessage(), is("Author must be a valid HIE Facility"));
    }

    @Test
    public void shouldRejectIfCompositionIsNotPresent() {
        final String xml = FileUtil.asString("xmls/encounters/no_composition.xml");
        List<ValidationMessage> validationMessages = structureValidator.validate(new ValidationSubject<Bundle>() {
            @Override
            public Bundle extract() {
                return fhirFeedUtil.deserialize(xml);
            }
        });
        assertThat(validationMessages.isEmpty(), is(false));
        assertThat(validationMessages.size(), is(1));
        assertThat(validationMessages.get(0).getMessage(), is("Feed must have a Composition with an encounter."));
    }

    @Test
    public void shouldRejectIfCompositionDoesNotContainASectionCalledEncounter() {
        final String xml = FileUtil.asString("xmls/encounters/invalid_composition.xml");
        List<ValidationMessage> validationMessages = structureValidator.validate(new ValidationSubject<Bundle>() {
            @Override
            public Bundle extract() {
                return fhirFeedUtil.deserialize(xml);
            }
        });
        assertThat(validationMessages.isEmpty(), is(false));
        assertThat(validationMessages.size(), is(1));
        assertThat(validationMessages.get(0).getMessage(), is("Feed must have a Composition with an encounter."));
    }

    @Test
    public void shouldRejectIfThereIsAMismatchBetweenEntriesAndSections() {
        /*
         Scenarios Covered

        1. No entry present for the section
        2. Ids mismatching in the entry (2 errors for this)
        3. An entry with no matching section

         */
        final String xml = FileUtil.asString("xmls/encounters/invalid_composition_sections.xml");
        List<ValidationMessage> validationMessages = structureValidator.validate(new ValidationSubject<Bundle>() {
            @Override
            public Bundle extract() {
                return fhirFeedUtil.deserialize(xml);
            }
        });
        assertThat(validationMessages.isEmpty(), is(false));
        assertThat(validationMessages.size(), is(4));
    }

}