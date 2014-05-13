package org.freeshr.shr.config;

import org.springframework.cassandra.core.CqlOperations;
import org.springframework.cassandra.core.CqlTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "org.freeshr.shr")
public class SHRCassandraConfig extends AbstractCassandraConfiguration {

    @Override
    protected String getKeyspaceName() {
        return "freeshr";
    }

    @Bean(name = "SHRCassandraTemplate")
    public CqlOperations CassandraTemplate() throws Exception {
        return new CqlTemplate(session().getObject());
    }

}
