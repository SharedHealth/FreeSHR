package org.freeshr.infrastructure.persistence;

import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class FacilityRepositoryIntegrationTest {

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    @Qualifier("SHRCassandraTemplate")
    private CqlOperations cqlTemplate;

    @After
    public void teardown() {
        cqlTemplate.execute("truncate facilities");
    }

    @Test
    public void returnFacilityByFacilityId() throws ExecutionException, InterruptedException {
        Facility facility = new Facility();
        facility.setFacilityId("10101");
        facility.setFacilityName("Foo");
        facility.setFacilityType("Village Hospital");
        facility.setCatchments("10,1020,102030");
        facility.setFacilityLocation(new Address("10", "11", "32", "45", "67"));
        Observable<Facility> save = facilityRepository.save(facility);
        TestSubscriber<Facility> subscriber = new TestSubscriber<>();
        save.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        Observable<Facility> savedFacilityObservable = facilityRepository.find("10101");
        TestSubscriber<Facility> facilityTestSubscriber = new TestSubscriber<>();
        savedFacilityObservable.subscribe(facilityTestSubscriber);
        facilityTestSubscriber.awaitTerminalEvent();

        facilityTestSubscriber.assertNoErrors();
        assertThat(facilityTestSubscriber.getOnNextEvents().get(0), is(facility));
    }

}