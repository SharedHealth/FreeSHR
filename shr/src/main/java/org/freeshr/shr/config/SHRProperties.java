package org.freeshr.shr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SHRProperties {

    @Value("${MCI_HOST}")
    private String mciHost;
    @Value("${MCI_PORT}")
    private String mciPort;
    @Value("${CASSANDRA_KEYSPACE}")
    private String cassandraKeySpace;

    public String getMCIUrl() {
        return "http://" + mciHost + ":" + mciPort;
    }

    public String getCassandraKeySpace() {
        return cassandraKeySpace;
    }
}
