package org.freeshr.web.launch.migration;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.exceptions.BadRequestException;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import com.toddfast.mutagen.Plan;
import com.toddfast.mutagen.cassandra.CassandraMutagen;
import com.toddfast.mutagen.cassandra.impl.CassandraMutagenImpl;

import java.io.IOException;
import java.util.HashMap;

import static java.lang.Integer.valueOf;
import static java.lang.System.getenv;

public class Migrations {

    private static final String MUTAGEN_CONNECTION_POOL_NAME = "shrMigrationConnectionPool";

    public void migrate() throws IOException, ConnectionException {
        CassandraMutagen mutagen = new CassandraMutagenImpl();
        mutagen.initialize("org/freeshr/cassandra/migrations");
        Keyspace keyspace = getKeySpace();
        Plan.Result<Integer> result = mutagen.mutate(keyspace);
        if (result.getException() != null) {
            throw new RuntimeException(result.getException());
        } else if (!result.isMutationComplete()) {
            throw new RuntimeException("Failed to apply cassandra migrations");
        }
    }

    private Keyspace getKeySpace() throws ConnectionException {
        AstyanaxContext<Keyspace> context = buildContext();
        context.start();
        return createKeyspaceIfNoneExist(context);
    }

    private AstyanaxContext<Keyspace> buildContext() {
        return new AstyanaxContext.Builder()
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
    }

    private Keyspace createKeyspaceIfNoneExist(AstyanaxContext<Keyspace> context) throws ConnectionException {
        Keyspace keyspace = context.getClient();
        try {
            keyspace.describeKeyspace();
        } catch (BadRequestException e) {
            keyspace.createKeyspace(new HashMap<String, Object>() {{
                put("strategy_options", new HashMap<String, Object>() {{
                    put("replication_factor", "1");
                }});
                put("strategy_class", "SimpleStrategy");
            }});
        }
        return keyspace;
    }

    public static void main(String[] args) throws IOException, ConnectionException {
        new Migrations().migrate();
    }
}
