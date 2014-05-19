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
    @Value("${CASSANDRA_HOST}")
    private String cassandraHost;
    @Value("${CASSANDRA_PORT}")
    private int cassandraPort;
    @Value("${CASSANDRA_TIMEOUT}")
    private int cassandraTimeout;
    @Value("${REST_POOL_SIZE}")
    private int restPoolSize;

    public String getMCIUrl() {
        return "http://" + mciHost + ":" + mciPort;
    }

    public String getCassandraKeySpace() {
        return cassandraKeySpace;
    }

    public String getContactPoints() {
        return cassandraHost;
    }

    public int getCassandraPort() {
        return cassandraPort;
    }

    public int getCassandraTimeout() {
        return cassandraTimeout;
    }

    public int getRestPoolSize() {
        return restPoolSize;
    }
}
