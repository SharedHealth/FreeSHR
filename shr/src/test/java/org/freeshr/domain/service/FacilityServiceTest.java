package org.freeshr.domain.service;

import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.infrastructure.FacilityRegistryClient;
import org.freeshr.infrastructure.persistence.FacilityRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import rx.Observable;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.MockitoAnnotations.initMocks;

public class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private FacilityRegistryClient facilityRegistryClient;

    private FacilityService facilityService;

    @Before
    public void setUp() {
        initMocks(this);
        facilityService = new FacilityService(facilityRepository, facilityRegistryClient);
    }

    @Test
    public void shouldNotQueryFacilityRegistryWrapperIfFacilityFoundInLocalDatabase() throws ExecutionException,
            InterruptedException {
        Facility facility = new Facility("1", "foo", "bar", "123", new Address());
        Mockito.when(facilityRepository.find(facility.getFacilityId())).thenReturn(Observable.just(facility));
        facilityService.ensurePresent(facility.getFacilityId());
        Mockito.verify(facilityRegistryClient, never()).getFacility(facility.getFacilityId());
    }

    @Test
    public void shouldQueryFacilityRegistryWrapperIfFacilityNotFoundInLocalDatabase() throws ExecutionException,
            InterruptedException {
        Facility facility = new Facility("1", "foo", "bar", "123", new Address());

        Mockito.when(facilityRepository.find(facility.getFacilityId())).thenReturn(Observable.<Facility>empty());
        Mockito.when(facilityRegistryClient.getFacility(facility.getFacilityId())).thenReturn(Observable.just
                (facility));
        assertNotNull(facilityService.ensurePresent(facility.getFacilityId()));
    }

    @Test
    public void shouldReturnNullIfFacilityNotFoundOnFacilityRegistry() throws Exception {
        Facility facility = new Facility("1", "foo", "bar", "123", new Address());

        Mockito.when(facilityRepository.find(facility.getFacilityId())).thenReturn(Observable.<Facility>just(null));
        Mockito.when(facilityRegistryClient.getFacility(facility.getFacilityId())).thenReturn(Observable.<Facility>error(new RuntimeException()));
        assertNull(facilityService.checkForFacility(facility.getFacilityId()).toBlocking().first());
    }
}
