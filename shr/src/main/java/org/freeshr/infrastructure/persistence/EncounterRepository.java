package org.freeshr.infrastructure.persistence;

import org.freeshr.application.fhir.EncounterBundle;
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

    public void save(EncounterBundle encounterBundle) {
        cqlOperations.executeAsynchronously("INSERT INTO encounter (encounter_id, content) VALUES ('" + encounterBundle.getId() + "','" + encounterBundle.getContent() + "');");
    }

}
