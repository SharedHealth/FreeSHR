package org.freeshr.web.launch;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import com.toddfast.mutagen.Plan;
import com.toddfast.mutagen.cassandra.CassandraMutagen;
import com.toddfast.mutagen.cassandra.impl.CassandraMutagenImpl;

import java.io.IOException;

import static java.lang.Integer.valueOf;
import static java.lang.System.getenv;

public class Migrations {

    private static final String MUTAGEN_CONNECTION_POOL_NAME = "shrMigrationConnectionPool";

    public void migrate() throws IOException {
        CassandraMutagen mutagen = new CassandraMutagenImpl();
        mutagen.initialize("org/freeshr/cassandra/migrations");

        Keyspace keyspace = getKeySpace();
        Plan.Result<Integer> result = mutagen.mutate(keyspace);
        if (result.getException() != null) {
            throw new RuntimeException(result.getException());
        } else {
            throw new RuntimeException("Failed to apply cassandra migrations");
        }
    }

    private Keyspace getKeySpace() {
        AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
                .forKeyspace(getenv("CASSANDRA_KEYSPACE"))
                .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                                .setCqlVersion(getenv("CQL_VERSION"))
                                .setTargetCassandraVersion(getenv("CASSANDRA_VERSION"))
                )
                .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl(MUTAGEN_CONNECTION_POOL_NAME)
                                .setPort(valueOf(getenv("MUTAGEN_CONNECTION_POOL_PORT")))
                                .setMaxConnsPerHost(1)
                                .setSeeds(getenv("MUTAGEN_SEEDS"))
                )
                .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
                .buildKeyspace(ThriftFamilyFactory.getInstance());
        context.start();
        return context.getClient();
    }
}
