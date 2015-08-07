package org.freeshr.validations;

import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.utils.UrlUtil.extractFacilityId;
import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity;

@Component
public class FacilityValidator implements Validator<AtomFeed> {

    public static final String INVALID_SERVICE_PROVIDER = "Invalid Service Provider";
    private final static Logger logger = LoggerFactory.getLogger(FacilityValidator.class);
    private final HIEFacilityValidator hieFacilityValidator;

    @Autowired
    public FacilityValidator(HIEFacilityValidator hieFacilityValidator) {
        this.hieFacilityValidator = hieFacilityValidator;
    }

    @Override
    public List<ValidationMessage> validate(ValidationSubject<AtomFeed> subject) {
        AtomFeed feed = subject.extract();
        List<ValidationMessage> validationMessages = new ArrayList<>();
        AtomEntry encounterEntry = identifyEncounterEntry(feed);
        ResourceReference serviceProvider = getServiceProviderRef(encounterEntry);
        if (serviceProvider == null) {
            logger.debug("Validating encounter as facility is not provided");
            return validationMessages;
        }
        String facilityUrl = serviceProvider.getReferenceSimple();
        if (!hieFacilityValidator.validate(facilityUrl)) {
            logger.debug("Encounter failed for invalid facility URL");
            validationMessages.add(buildValidationMessage(ResourceValidator.INVALID, encounterEntry.getId(), INVALID_SERVICE_PROVIDER,
                    IssueSeverity.error));
            return validationMessages;
        }

        logger.debug(String.format("Encounter validated for valid facility %s", extractFacilityId(facilityUrl)));
        return validationMessages;
    }

    private ResourceReference getServiceProviderRef(AtomEntry encounterEntry) {
        return (encounterEntry != null) ? ((Encounter) encounterEntry.getResource()).getServiceProvider() : null;
    }

    private AtomEntry<? extends Resource> identifyEncounterEntry(AtomFeed feed) {
        for (AtomEntry<? extends Resource> atomEntry : feed.getEntryList()) {
            Resource resource = atomEntry.getResource();
            if (resource instanceof Encounter) {
                return atomEntry;
            }
        }
        return null;
    }

    private ValidationMessage buildValidationMessage(String type, String path, String message, IssueSeverity error) {
        return new ValidationMessage(ValidationMessage.Source.ResourceValidator, type, path, message, error);
    }


}