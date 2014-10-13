package org.freeshr.domain.service;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.infrastructure.FacilityRegistryClient;
import org.freeshr.infrastructure.persistence.FacilityRepository;
import org.freeshr.utils.concurrent.PreResolvedListenableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.MockitoAnnotations.initMocks;

public class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private FacilityRegistryClient facilityRegistryClient;

    private FacilityService facilityService;

    @Before
    public void setUp(){
        initMocks(this);
        facilityService = new FacilityService(facilityRepository, facilityRegistryClient);
    }

    @Test
    public void shouldNotQueryFacilityRegistryWrapperIfFacilityFoundInLocalDatabase() throws ExecutionException, InterruptedException {
        Facility facility = new Facility("1", "foo", "bar","123",new Address());
        Mockito.when(facilityRepository.find(facility.getFacilityId())) .thenReturn(facility);
        facilityService.ensurePresent(facility.getFacilityId());
        Mockito.verify(facilityRegistryClient, never()).getFacility(facility.getFacilityId());
    }

    @Test
    public void shouldQueryFacilityRegistryWrapperIfFacilityNotFoundInLocalDatabase() throws ExecutionException, InterruptedException {
        Facility facility = new Facility("1", "foo", "bar","123",new Address());

        Mockito.when(facilityRepository.find(facility.getFacilityId())).thenReturn(null);
        Mockito.when(facilityRegistryClient.getFacility(facility.getFacilityId())).thenReturn(new PreResolvedListenableFuture<Facility>(facility));
        assertNotNull(facilityService.ensurePresent(facility.getFacilityId()));

    }

}
