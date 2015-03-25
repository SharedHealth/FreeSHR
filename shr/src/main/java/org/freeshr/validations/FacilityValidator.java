package org.freeshr.validations;

import org.apache.log4j.Logger;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.service.FacilityService;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity;

@Component
public class FacilityValidator implements Validator<AtomFeed> {

    public static final String INVALID_SERVICE_PROVIDER = "Invalid Service Provider";
    public static final String INVALID_SERVICE_PROVIDER_URL = "Invalid Service Provider URL";
    private final Logger logger = Logger.getLogger(FacilityValidator.class);
    private SHRProperties shrProperties;
    private FacilityService facilityService;

    @Autowired
    public FacilityValidator(SHRProperties shrProperties, FacilityService facilityService) {
        this.shrProperties = shrProperties;
        this.facilityService = facilityService;
    }

    @Override
    public List<ValidationMessage> validate(ValidationSubject<AtomFeed> subject) {
        AtomFeed feed = subject.extract();
        List<ValidationMessage> validationMessages = new ArrayList<>();
        AtomEntry encounterEntry = identifyEncounterEntry(feed);
        ResourceReference serviceProvider = getServiceProviderRef(encounterEntry);
        if (serviceProvider == null) {
            logger.info("Validating encounter as facility is not provided");
            return validationMessages;
        }
        String facilityUrl = serviceProvider.getReferenceSimple();
        if (facilityUrl.isEmpty() || !isValidFacilityUrl(facilityUrl)) {
            validationMessages.add(buildValidationMessage(ResourceValidator.INVALID, encounterEntry.getId(),
                    INVALID_SERVICE_PROVIDER_URL, IssueSeverity.error));
            logger.info("Encounter failed for invalid facility URL");
            return validationMessages;
        }

        Facility facility = checkForFacility(facilityUrl).toBlocking().first();
        if (facility == null) {
            validationMessages.add(buildValidationMessage(ResourceValidator.INVALID, encounterEntry.getId(), INVALID_SERVICE_PROVIDER,
                    IssueSeverity.error));
            return validationMessages;
        }

        logger.info("Encounter validated for valid facility");
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

    private Observable<Facility> checkForFacility(String facilityUrl) {
        Observable<Facility> facilityObservable = facilityService.ensurePresent(extractFacilityId(facilityUrl));
        return facilityObservable.flatMap(new Func1<Facility, Observable<Facility>>() {
                                              @Override
                                              public Observable<Facility> call(Facility facility) {
                                                  return Observable.just(facility);
                                              }
                                          },
                new Func1<Throwable, Observable<Facility>>() {
                    @Override
                    public Observable<Facility> call(Throwable throwable) {
                        return Observable.just(null);
                    }
                },
                new Func0<Observable<Facility>>() {
                    @Override
                    public Observable<Facility> call() {
                        return null;
                    }
                });
    }

    private String extractFacilityId(String referenceSimple) {
        return referenceSimple.substring(referenceSimple.lastIndexOf('/') + 1, referenceSimple.lastIndexOf('.')).trim();
    }

    private boolean isValidFacilityUrl(String referenceSimple) {
        String facilityRegistryUrl = shrProperties.getFacilityReferencePath();
        return referenceSimple.contains(facilityRegistryUrl);
    }
}