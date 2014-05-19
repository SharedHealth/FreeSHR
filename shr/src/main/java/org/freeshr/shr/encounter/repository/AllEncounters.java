package org.freeshr.shr.encounter.repository;

import org.freeshr.shr.encounter.model.Encounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;

@Component
public class AllEncounters {

    private CqlOperations cqlOperations;

    @Autowired
    public AllEncounters(@Qualifier("SHRCassandraTemplate") CqlOperations cassandraTemplate) {
        this.cqlOperations = cassandraTemplate;
    }

    public void save(Encounter encounter) {
        cqlOperations.executeAsynchronously("INSERT INTO patient (health_id) VALUES ('" + encounter.getHealthId() + "');");
    }

}
