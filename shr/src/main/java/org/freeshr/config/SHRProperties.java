package org.freeshr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SHRProperties {

    @Value("${MCI_HOST}")
    private String mciHost;
    @Value("${MCI_PORT}")
    private String mciPort;
    @Value("${MCI_USER}")
    private String mciUser;
    @Value("${MCI_PASSWORD}")
    private String mciPassword;
    @Value("${TR_USER}")
    private String trUser;
    @Value("${TR_PASSWORD}")
    private String trPassword;

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

    public String getMCIPatientUrl() {
        return String.format("http://%s:%s/api/v1/patients",mciHost,mciPort);
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

    public String getMciUser() {
        return mciUser;
    }

    public String getMciPassword() {
        return mciPassword;
    }

    public String getTrUser() {
        return trUser;
    }

    public String getTrPassword() {
        return trPassword;
    }
}
