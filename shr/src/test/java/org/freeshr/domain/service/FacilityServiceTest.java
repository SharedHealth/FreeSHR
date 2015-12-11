package org.freeshr.domain.service;

import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.infrastructure.FacilityRegistryClient;
import org.freeshr.infrastructure.persistence.FacilityRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import rx.Observable;

import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
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
        when(facilityRepository.find(facility.getFacilityId())).thenReturn(Observable.just(facility));
        facilityService.ensurePresent(facility.getFacilityId());
        verify(facilityRegistryClient, never()).getFacility(facility.getFacilityId());
    }

    @Test
    public void shouldQueryFacilityRegistryWrapperIfFacilityNotFoundInLocalDatabase() throws ExecutionException,
            InterruptedException {
        Facility facility = new Facility("1", "foo", "bar", "123", new Address());

        when(facilityRepository.find(facility.getFacilityId())).thenReturn(Observable.<Facility>empty());
        when(facilityRegistryClient.getFacility(facility.getFacilityId())).thenReturn(Observable.just
                (facility));
        assertNotNull(facilityService.ensurePresent(facility.getFacilityId()));
    }

    @Test
    public void checkForFacilityShouldFailIfFacilityNotFoundLocallyAndCallToFRFails() {
        Facility facility = new Facility("1", "foo", "bar", "123", new Address());

        when(facilityRepository.find(facility.getFacilityId())).thenReturn(Observable.<Facility>just(null));
        when(facilityRegistryClient.getFacility(facility.getFacilityId())).thenReturn(Observable.<Facility>error(new RuntimeException()));
        assertFalse(facilityService.checkForFacility(facility.getFacilityId()).toBlocking().first());
    }

    @Test
    public void checkForFacilityShouldPassIfFacilityFoundLocally() throws Exception {
        Facility facility = new Facility("1", "foo", "bar", "123", new Address());

        when(facilityRepository.find(facility.getFacilityId())).thenReturn(Observable.<Facility>just(facility));
        assertTrue(facilityService.checkForFacility(facility.getFacilityId()).toBlocking().first());

    }

    @Test
    public void checkForFacilityShouldPassIfFacilityNotFoundLocallyAndDownloadFromFRSucceeds() throws Exception {
        Facility facility = new Facility("1", "foo", "bar", "123", new Address());

        when(facilityRepository.find(facility.getFacilityId())).thenReturn(Observable.<Facility>just(null));
        when(facilityRegistryClient.getFacility(facility.getFacilityId())).thenReturn(Observable.just(facility));
        when(facilityRepository.save(facility)).thenReturn(Observable.just(facility));

        assertTrue(facilityService.checkForFacility(facility.getFacilityId()).toBlocking().first());

    }

    @Test
    public void checkForFacilityShouldFailIfFacilityNotFoundLocallyAndOnFR() throws Exception {
        Facility facility = new Facility("1", "foo", "bar", "123", new Address());

        when(facilityRepository.find(facility.getFacilityId())).thenReturn(Observable.<Facility>just(null));
        when(facilityRegistryClient.getFacility(facility.getFacilityId())).thenReturn(Observable.<Facility>just(null));

        assertFalse(facilityService.checkForFacility(facility.getFacilityId()).toBlocking().first());

        verify(facilityRepository, never()).save(any(Facility.class));


    }
}
