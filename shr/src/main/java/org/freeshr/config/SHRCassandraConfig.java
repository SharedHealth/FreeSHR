package org.freeshr.config;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.SocketOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.cassandra.core.CqlTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "org.freeshr.infrastructure.persistence")
public class SHRCassandraConfig extends AbstractCassandraConfiguration {

    @Autowired
    private SHRProperties shrProperties;

    @Override
    protected String getKeyspaceName() {
        return shrProperties.getCassandraKeySpace();
    }

    @Override
    protected String getContactPoints() {
        return shrProperties.getContactPoints();
    }

    @Override
    protected int getPort() {
        return shrProperties.getCassandraPort();
    }

    @Override
    protected AuthProvider getAuthProvider() {
        return new PlainTextAuthProvider(shrProperties.getCassandraUser(), shrProperties.getCassandraPassword());
    }

    @Override
    protected SocketOptions getSocketOptions() {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.setConnectTimeoutMillis(shrProperties.getCassandraTimeout());
        socketOptions.setReadTimeoutMillis(shrProperties.getCassandraTimeout());
        return socketOptions;
    }

    @Bean(name = "SHRCassandraTemplate")
    public CqlOperations CassandraTemplate() throws Exception {
        CqlTemplate cqlTemplate = new CqlTemplate(session().getObject());
        return cqlTemplate;
    }

}
