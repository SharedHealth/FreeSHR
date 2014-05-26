package org.freeshr.infrastructure.persistence;

import org.freeshr.domain.model.encounter.Encounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;

@Component
public class EncounterRepository {

    private CqlOperations cqlOperations;

    @Autowired
    public EncounterRepository(@Qualifier("SHRCassandraTemplate") CqlOperations cassandraTemplate) {
        this.cqlOperations = cassandraTemplate;
    }

    public void save(Encounter encounter) {
        cqlOperations.executeAsynchronously("INSERT INTO patient (health_id) VALUES ('" + encounter.getHealthId() + "');");
    }

}
