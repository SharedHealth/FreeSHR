package org.freeshr.domain.service;

import org.apache.log4j.Logger;
import org.freeshr.domain.model.Facility;
import org.freeshr.infrastructure.FacilityRegistryClient;
import org.freeshr.infrastructure.persistence.FacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

@Service
public class FacilityService {

    private Logger logger = Logger.getLogger(FacilityService.class);
    private FacilityRepository facilityRepository;
    private FacilityRegistryClient facilityRegistryClient;

    @Autowired
    public FacilityService(FacilityRepository facilityRepository, FacilityRegistryClient facilityRegistryClient) {
        this.facilityRepository = facilityRepository;
        this.facilityRegistryClient = facilityRegistryClient;
    }

    public Observable<Facility> ensurePresent(final String facilityId) {
        Observable<Facility> facility = facilityRepository.find(facilityId);
        return facility.flatMap(findRemoteNotFound(facilityId));
    }

    private Func1<Facility, Observable<Facility>> findRemoteNotFound(final String facilityId) {
        return new Func1<Facility, Observable<Facility>>() {
            @Override
            public Observable<Facility> call(Facility facility) {
                if (facility != null) return Observable.just(facility);
                return findRemote(facilityId);
            }
        };
    }

    private Observable<Facility> findRemote(String facilityId) {
            Observable<Facility> facility = facilityRegistryClient.getFacility(facilityId);
            return facility.flatMap(new Func1<Facility, Observable<Facility>>() {
                @Override
                public Observable<Facility> call(Facility facility) {
                    return facilityRepository.save(facility);
                }
            }, new Func1<Throwable, Observable<? extends Facility>>() {
                @Override
                public Observable<? extends Facility> call(Throwable throwable) {
                    logger.info(throwable.getMessage());
                    return Observable.just(null);
                }
            }, new Func0<Observable<? extends Facility>>() {
                @Override
                public Observable<? extends Facility> call() {
                    return null;
                }
            });

    }

}
