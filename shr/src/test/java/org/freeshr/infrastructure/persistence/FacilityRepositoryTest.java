package org.freeshr.infrastructure.persistence;

import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.domain.model.Facility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class FacilityRepositoryTest {

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    @Qualifier("SHRCassandraTemplate")
    private CqlOperations cqlTemplate;

    @Test
    public void returnFacilityByFacilityId() throws ExecutionException, InterruptedException {
        Facility facility = new Facility();
        facility.setFacilityId("10101");
        facility.setFacilityName("Foo");
        facility.setCatchments("10,1020,102030");
        facilityRepository.save(facility);
        Facility actualFacility = facilityRepository.find("10101").get();
        assertThat(actualFacility, is(facility));
    }

}