package org.freeshr.domain.service;

import org.apache.log4j.Logger;
import org.freeshr.domain.model.Facility;
import org.freeshr.infrastructure.FacilityRegistryWrapper;
import org.freeshr.infrastructure.persistence.FacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import rx.Observable;
import rx.functions.Func1;

import static org.freeshr.utils.concurrent.FutureConverter.toListenableFuture;
import static org.freeshr.utils.concurrent.FutureConverter.toObservable;

@Service
public class FacilityRegistry {

    private Logger logger = Logger.getLogger(FacilityRegistry.class);
    private FacilityRepository facilityRepository;
    private FacilityRegistryWrapper facilityRegistryWrapper;

    @Autowired
    public FacilityRegistry(FacilityRepository facilityRepository,FacilityRegistryWrapper facilityRegistryWrapper) {
        this.facilityRepository = facilityRepository;
        this.facilityRegistryWrapper = facilityRegistryWrapper;
    }

    public ListenableFuture<Facility> ensurePresent(final String facilityId) {
        Observable<Facility> localSearch = toObservable(facilityRepository.find(facilityId));

        Observable<Facility> search = localSearch.flatMap(new Func1<Facility, Observable<Facility>>() {
            @Override
            public Observable<Facility> call(Facility facility) {
                if (facility != null) return Observable.from(facility);
                return doRemoteSearch(facilityId);
            }
        });

        return toListenableFuture(search);
    }

    private Observable<Facility> doRemoteSearch(String facilityId) {
        Observable<Facility> facilityObservable = toObservable(facilityRegistryWrapper.getFacility(facilityId));
        return facilityObservable.flatMap(new Func1<Facility, Observable<Facility>>() {
            @Override
            public Observable<Facility> call(Facility facility) {
                facilityRepository.save(facility);
                return Observable.from(facility);
            }
        });
    }

}
