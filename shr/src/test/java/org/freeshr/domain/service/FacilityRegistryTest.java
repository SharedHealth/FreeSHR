package org.freeshr.domain.service;


import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.infrastructure.FacilityRegistryWrapper;
import org.freeshr.infrastructure.persistence.FacilityRepository;
import org.freeshr.utils.concurrent.PreResolvedListenableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.MockitoAnnotations.initMocks;

public class FacilityRegistryTest{

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private FacilityRegistryWrapper facilityRegistryWrapper;

    private FacilityRegistry facilityRegistry;

    @Before
    public void setUp(){
        initMocks(this);
        facilityRegistry = new FacilityRegistry(facilityRepository,facilityRegistryWrapper);
    }

    @Test
    public void shouldNotQueryFacilityRegistryWrapperIfFacilityFoundInLocalDatabase(){
        Facility facility = new Facility("1", "foo", "bar","123",new Address());
        Mockito.when(facilityRepository.find(facility.getFacilityId())) .thenReturn(new PreResolvedListenableFuture<Facility>(facility));
        facilityRegistry.ensurePresent(facility.getFacilityId());
        Mockito.verify(facilityRegistryWrapper, never()).getFacility(facility.getFacilityId());
    }

    @Test
    public void shouldQueryFacilityRegistryWrapperIfFacilityNotFoundInLocalDatabase(){
        Facility facility = new Facility("1", "foo", "bar","123",new Address());

        Mockito.when(facilityRepository.find(facility.getFacilityId())).thenReturn(new PreResolvedListenableFuture<Facility>(null));
        Mockito.when(facilityRegistryWrapper.getFacility(facility.getFacilityId())).thenReturn(new PreResolvedListenableFuture<Facility>(facility));

        assertNotNull(facilityRegistry.ensurePresent(facility.getFacilityId()));

    }

}
