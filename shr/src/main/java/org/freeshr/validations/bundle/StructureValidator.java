package org.freeshr.validations.bundle;

import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.validations.HIEFacilityValidator;
import org.freeshr.validations.ValidationMessages;
import org.freeshr.validations.ValidationSubject;
import org.freeshr.validations.Validator;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.validations.ValidationMessages.FEED_MUST_HAVE_COMPOSITION;

@Component
public class StructureValidator implements Validator<Bundle> {
    private FhirFeedUtil fhirFeedUtil;
    private HIEFacilityValidator hieFacilityValidator;

    @Autowired
    public StructureValidator(FhirFeedUtil fhirFeedUtil, HIEFacilityValidator hieFacilityValidator) {
        this.fhirFeedUtil = fhirFeedUtil;
        this.hieFacilityValidator = hieFacilityValidator;
    }

    @Override
    public List<ValidationMessage> validate(ValidationSubject<Bundle> subject) {
        Bundle feed = subject.extract();
        List<ValidationMessage> validationMessages = new ArrayList<>();

        Bundle.BundleEntryComponent compositionEntry = hasCompositionWithEncounter(feed.getEntry());

        if (compositionEntry == null) {
            validationMessages.add(new ValidationMessage(null, OperationOutcome.IssueType.INVALID, "Feed",
                    FEED_MUST_HAVE_COMPOSITION, OperationOutcome.IssueSeverity.ERROR));
            return validationMessages;
        }

        List<Reference> author = ((Composition) compositionEntry.getResource()).getAuthor();
        if(author.isEmpty() || !hieFacilityValidator.validate(author.get(0).getReference())){
            validationMessages.add(new ValidationMessage(null, OperationOutcome.IssueType.INVALID, "Feed",
                    ValidationMessages.INVALID_AUTHOR, OperationOutcome.IssueSeverity.ERROR));
            return validationMessages;
        }

        validateStructure(feed, validationMessages, compositionEntry);

        return validationMessages;
    }

    private void validateStructure(Bundle feed, List<ValidationMessage> validationMessages, Bundle.BundleEntryComponent compositionEntry) {
        List<String> compositionSectionIds = identifySectionIdsFromComposition(compositionEntry);
        List<String> entryReferenceIds = verifyEntryReferenceIds(feed.getEntry(), compositionSectionIds, validationMessages);
        compositionSectionIds.removeAll(entryReferenceIds);

        //Add error for each section with no entry.
        for (String entryReferenceId : compositionSectionIds) {

            validationMessages.add(new ValidationMessage(null, OperationOutcome.IssueType.INVALID, entryReferenceId, String
                    .format
                            ("No entry present" +
                                    " for the section with id %s", entryReferenceId), OperationOutcome.IssueSeverity.ERROR));
        }
    }

    private List<String> verifyEntryReferenceIds(List<Bundle.BundleEntryComponent> entryList,
                                                 List<String> compositionSectionIds,
                                                 List<ValidationMessage> validationMessages) {
        List<String> resourceDetailsList = new ArrayList<>();

        for (Bundle.BundleEntryComponent atomEntry : entryList) {
            if (!atomEntry.getResource().getResourceType().equals(ResourceType.Composition)) {
                String identifier = ((Identifier) atomEntry.getResource().getChildByName("identifier").getValues()
                        .get(0)).getValue();
                resourceDetailsList.add(identifier);

                if (compositionSectionIds.contains(identifier)) continue;

                validationMessages.add(new ValidationMessage(null, OperationOutcome.IssueType.INVALID, identifier, String.format
                        ("Entry with id %s " +
                                        "is not present in the composition section list.",
                                identifier), OperationOutcome.IssueSeverity.ERROR));
            }
        }
        return resourceDetailsList;
    }

    private List<String> identifySectionIdsFromComposition(Bundle.BundleEntryComponent compositionEntry) {
        List<Base> sections = compositionEntry.getResource().getChildByName("section").getValues();
        List<String> compositionSectionList = new ArrayList<>();
        for (Base section : sections) {
            List<Reference> sectionContent = ((Composition.SectionComponent) section).getEntry();
            String referenceId = sectionContent.get(0).getReference();
            compositionSectionList.add(referenceId);
        }
        return compositionSectionList;
    }

    private Bundle.BundleEntryComponent hasCompositionWithEncounter(List<Bundle.BundleEntryComponent> entryList) {
        Bundle.BundleEntryComponent compositionEntry = null;
        compositionEntry = fhirFeedUtil.getAtomEntryOfResourceType(entryList, ResourceType.Composition);
        if(compositionEntry == null) return null;
        return compositionEntry.getResource().getChildByName("encounter").hasValues() ? compositionEntry : null;
    }
}

