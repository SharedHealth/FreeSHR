package org.freeshr.shr.config;

import com.datastax.driver.core.SocketOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.cassandra.core.CqlTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "org.freeshr.shr")
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
    protected SocketOptions getSocketOptions() {
        SocketOptions socketOptions = super.getSocketOptions();
        socketOptions.setConnectTimeoutMillis(shrProperties.getCassandraTimeout());
        return socketOptions;
    }

    @Bean(name = "SHRCassandraTemplate")
    public CqlOperations CassandraTemplate() throws Exception {
        return new CqlTemplate(session().getObject());
    }

}
