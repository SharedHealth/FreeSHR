package org.freeshr.domain.service;

import org.apache.log4j.Logger;
import org.freeshr.domain.model.Facility;
import org.freeshr.infrastructure.FacilityRegistryClient;
import org.freeshr.infrastructure.persistence.FacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

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

    public Facility ensurePresent(final String facilityId) throws ExecutionException, InterruptedException {
        Facility facility = facilityRepository.find(facilityId);
        if (facility != null) return facility;
        logger.debug("FACILITY NOT FOUND LOCALLY. INITIATING REMOTE FIND");
        return findRemote(facilityId);
    }

    private Facility findRemote(String facilityId) throws ExecutionException, InterruptedException {
        try{
            Facility facility = facilityRegistryClient.getFacility(facilityId).get();
            facilityRepository.save(facility);
            return facility;
        }
        catch(Exception e){
            logger.warn(e);
            return null;
        }
    }

}
