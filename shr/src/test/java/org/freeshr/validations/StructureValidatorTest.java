package org.freeshr.validations;


import org.freeshr.utils.FileUtil;
import org.freeshr.utils.FhirFeedUtil;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class StructureValidatorTest {

    private FhirFeedUtil fhirFeedUtil;
    private StructureValidator structureValidator;

    @Before
    public void setup() {
        fhirFeedUtil = new FhirFeedUtil();
        structureValidator = new StructureValidator(fhirFeedUtil);
    }

    @Test
    public void shouldAcceptAValidXmlWithOneEntryForEachSectionPresentInComposition() {
        final String xml = FileUtil.asString("xmls/encounters/diagnostic_order_valid.xml");
        List<ValidationMessage> validationMessages = structureValidator.validate(new ValidationSubject<AtomFeed>() {
            @Override
            public AtomFeed extract() {
                return fhirFeedUtil.deserialize(xml);
            }
        });
        assertThat(validationMessages.isEmpty(), is(true));
    }

    @Test
    public void shouldRejectIfCompositionIsNotPresent() {
        final String xml = FileUtil.asString("xmls/encounters/no_composition.xml");
        List<ValidationMessage> validationMessages = structureValidator.validate(new ValidationSubject<AtomFeed>() {
            @Override
            public AtomFeed extract() {
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
        List<ValidationMessage> validationMessages = structureValidator.validate(new ValidationSubject<AtomFeed>() {
            @Override
            public AtomFeed extract() {
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
        List<ValidationMessage> validationMessages = structureValidator.validate(new ValidationSubject<AtomFeed>() {
            @Override
            public AtomFeed extract() {
                return fhirFeedUtil.deserialize(xml);
            }
        });
        assertThat(validationMessages.isEmpty(), is(false));
        assertThat(validationMessages.size(), is(4));
    }

}