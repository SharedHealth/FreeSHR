package org.freeshr.domain.service;

import org.apache.log4j.Logger;
import org.freeshr.domain.model.Facility;
import org.freeshr.infrastructure.persistence.FacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;

import java.util.concurrent.ExecutionException;
@Service
public class FacilityRegistry {

    private Logger logger = Logger.getLogger(FacilityRegistry.class);
    private FacilityRepository facilityRepository;

    @Autowired
    public FacilityRegistry(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    public ListenableFuture<Facility> ensurePresent(final String facilityId) {
        return new ListenableFutureAdapter<Facility, Facility>(facilityRepository.find(facilityId)) {

            @Override
            protected Facility adapt(Facility facility) throws ExecutionException {
                return facility;
            }
        };
    }
}
