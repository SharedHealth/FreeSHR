package org.freeshr.validations;

import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;

import static org.freeshr.domain.ErrorMessageBuilder.buildValidationMessage;
import static org.freeshr.domain.ErrorMessageBuilder.FEED_MUST_HAVE_COMPOSITION;

@Component
public class StructureValidator implements Validator<AtomFeed> {
    @Override
    public List<ValidationMessage> validate(ValidationSubject<AtomFeed> subject) {
        AtomFeed feed = subject.extract();
        List<ValidationMessage> validationMessages = new ArrayList<>();

        AtomEntry<? extends Resource> compositionEntry = hasCompositionWithEncounter(feed.getEntryList());

        if (compositionEntry == null) {
            validationMessages.add(buildValidationMessage("Feed", ResourceValidator.INVALID, FEED_MUST_HAVE_COMPOSITION, OperationOutcome.IssueSeverity.error));
            return validationMessages;
        }

        List<String> compositionSectionIds = identifySectionIdsFromComposition(compositionEntry);
        List<String> entryReferenceIds = verifyEntryReferenceIds(feed.getEntryList(), compositionSectionIds, validationMessages);
        compositionSectionIds.removeAll(entryReferenceIds);

        //Add error for each section with no entry.
        for (String entryReferenceId : compositionSectionIds) {
            validationMessages.add(buildValidationMessage(entryReferenceId, ResourceValidator.INVALID, String.format("No entry present for the section with id %s", entryReferenceId), OperationOutcome.IssueSeverity.error));
        }

        return validationMessages;
    }

    private List<String> verifyEntryReferenceIds(List<AtomEntry<? extends Resource>> entryList,
                                                 List<String> compositionSectionIds,
                                                 List<ValidationMessage> validationMessages) {
        List<String> resourceDetailsList = new ArrayList<>();

        for (AtomEntry<? extends Resource> atomEntry : entryList) {
            if (!atomEntry.getResource().getResourceType().equals(ResourceType.Composition)) {
                String identifier = ((Identifier) atomEntry.getResource().getChildByName("identifier").getValues()
                        .get(0)).getValueSimple();
                resourceDetailsList.add(identifier);

                if (compositionSectionIds.contains(identifier)) continue;

                validationMessages.add(buildValidationMessage(identifier, ResourceValidator.INVALID, String.format("Entry with id %s is not present in the composition section list.",
                        identifier), OperationOutcome.IssueSeverity.error));
            }
        }
        return resourceDetailsList;
    }

    private List<String> identifySectionIdsFromComposition(AtomEntry<? extends Resource> compositionEntry) {
        List<Element> sections = compositionEntry.getResource().getChildByName("section").getValues();
        List<String> compositionSectionList = new ArrayList<>();
        for (Element section : sections) {
            ResourceReference sectionContent = ((Composition.SectionComponent) section).getContent();
            String referenceId = sectionContent.getReferenceSimple();
            compositionSectionList.add(referenceId);
        }
        return compositionSectionList;
    }

    private AtomEntry<? extends Resource> hasCompositionWithEncounter(List<AtomEntry<? extends Resource>> entryList) {
        AtomEntry<? extends Resource> compositionEntry = null;
        for (AtomEntry<? extends Resource> atomEntry : entryList) {
            Resource resource = atomEntry.getResource();
            if (resource.getResourceType().equals(ResourceType.Composition)) {
                compositionEntry = resource.getChildByName("encounter").hasValues() ? atomEntry : null;
                break;
            }
        }
        return compositionEntry;
    }

}

