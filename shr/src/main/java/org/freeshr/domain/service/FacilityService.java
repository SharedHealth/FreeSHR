package org.freeshr.domain.service;

import org.freeshr.domain.model.Facility;
import org.freeshr.infrastructure.FacilityRegistryClient;
import org.freeshr.infrastructure.persistence.FacilityRepository;
import org.freeshr.infrastructure.persistence.RxMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.functions.Func1;

@Service
public class FacilityService {
    private final static Logger LOG = LoggerFactory.getLogger(FacilityService.class);
    private FacilityRepository facilityRepository;
    private FacilityRegistryClient facilityRegistryClient;

    @Autowired
    public FacilityService(FacilityRepository facilityRepository, FacilityRegistryClient facilityRegistryClient) {
        this.facilityRepository = facilityRepository;
        this.facilityRegistryClient = facilityRegistryClient;
    }

    public Observable<Boolean> checkForFacility(String facilityId) {
        Observable<Facility> facilityObservable = ensurePresent(facilityId);
        return facilityObservable.flatMap(new Func1<Facility, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call(Facility facility) {
                return Observable.just(facility != null);
            }
        },new Func1<Throwable, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call(Throwable throwable) {
                return Observable.just(false);
            }
        },RxMaps.<Boolean>completeResponds());
    }

    public Observable<Facility> ensurePresent(final String facilityId) {
        Observable<Facility> facilityLocalCheckObservable = facilityRepository.find(facilityId);
        return facilityLocalCheckObservable.flatMap(findRemoteIfNotFound(facilityId),
                RxMaps.<Facility>logAndForwardError(LOG),
                RxMaps.<Facility>completeResponds());
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
        LOG.debug(String.format("Facility (%s) not present in db.Finding it from remote FR", facilityId));
        Observable<Facility> facility = facilityRegistryClient.getFacility(facilityId);
        return facility.flatMap(new Func1<Facility, Observable<Facility>>() {
            @Override
            public Observable<Facility> call(Facility facility) {
                if(facility!=null)
                    return facilityRepository.save(facility);
                return Observable.just(null);
            }
        },RxMaps.<Facility>logAndForwardError(LOG,"Unable to find facility in Facility Registry"), RxMaps.<Facility>completeResponds());
    }
}
