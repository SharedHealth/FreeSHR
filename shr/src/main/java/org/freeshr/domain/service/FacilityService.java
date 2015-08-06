package org.freeshr.domain.service;

import org.freeshr.domain.model.Facility;
import org.freeshr.infrastructure.FacilityRegistryClient;
import org.freeshr.infrastructure.persistence.FacilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

@Service
public class FacilityService {
    private final static Logger logger = LoggerFactory.getLogger(FacilityService.class);
    private FacilityRepository facilityRepository;
    private FacilityRegistryClient facilityRegistryClient;

    @Autowired
    public FacilityService(FacilityRepository facilityRepository, FacilityRegistryClient facilityRegistryClient) {
        this.facilityRepository = facilityRepository;
        this.facilityRegistryClient = facilityRegistryClient;
    }

    public Observable<Facility> checkForFacility(String facilityId) {
        Observable<Facility> facilityObservable = ensurePresent(facilityId);
        return facilityObservable.flatMap(new Func1<Facility, Observable<Facility>>() {
                                              @Override
                                              public Observable<Facility> call(Facility facility) {
                                                  return Observable.just(facility);
                                              }
                                          },
                new Func1<Throwable, Observable<Facility>>() {
                    @Override
                    public Observable<Facility> call(Throwable throwable) {
                        logger.debug("Facility not found");
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

    public Observable<Facility> ensurePresent(final String facilityId) {
        Observable<Facility> facility = facilityRepository.find(facilityId);
        return facility.flatMap(findRemoteIfNotFound(facilityId), new Func1<Throwable, Observable<? extends Facility>>() {
            @Override
            public Observable<? extends Facility> call(Throwable throwable) {
                return Observable.error(throwable);
            }
        }, new Func0<Observable<? extends Facility>>() {
            @Override
            public Observable<? extends Facility> call() {
                return null;
            }
        });
    }

    private Func1<Facility, Observable<Facility>> findRemoteIfNotFound(final String facilityId) {
        return new Func1<Facility, Observable<Facility>>() {
            @Override
            public Observable<Facility> call(Facility facility) {
                if (facility != null) return Observable.just(facility);
                return findRemote(facilityId);
            }
        };
    }

    private Observable<Facility> findRemote(String facilityId) {
        logger.debug(String.format("Facility (%s) not present in db.Finding it from remote FR", facilityId));
        Observable<Facility> facility = facilityRegistryClient.getFacility(facilityId);
        return facility.flatMap(new Func1<Facility, Observable<Facility>>() {
            @Override
            public Observable<Facility> call(Facility facility) {
                return facilityRepository.save(facility);
            }
        }, new Func1<Throwable, Observable<? extends Facility>>() {
            @Override
            public Observable<? extends Facility> call(Throwable throwable) {
                logger.debug(String.format("Unable to find facility. Cause: %s", throwable.getMessage()));
                return Observable.error(throwable);
            }
        }, new Func0<Observable<? extends Facility>>() {
            @Override
            public Observable<? extends Facility> call() {
                return null;
            }
        });
    }
}
