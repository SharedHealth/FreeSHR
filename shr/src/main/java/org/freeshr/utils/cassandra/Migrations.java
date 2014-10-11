package org.freeshr.utils.cassandra;

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
import java.util.Map;

import static java.lang.Integer.valueOf;
import static java.lang.System.getenv;

public class Migrations {

    private static final String MUTAGEN_CONNECTION_POOL_NAME = "shrMigrationConnectionPool";
    protected final Map<String, String> env;

    public Migrations() {
        this(getenv());
    }

    public Migrations(Map<String, String> env) {
        this.env = env;
    }

    public void migrate() throws IOException, ConnectionException {
        CassandraMutagen mutagen = new CassandraMutagenImpl();
        mutagen.initialize(env.get("CASSANDRA_MIGRATIONS_PATH"));
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

    protected AstyanaxContext<Keyspace> buildContext() {
        return new AstyanaxContext.Builder()
                .forKeyspace(env.get("CASSANDRA_KEYSPACE"))
                .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                                .setCqlVersion(env.get("CQL_VERSION"))
                                .setTargetCassandraVersion(env.get("CASSANDRA_VERSION"))
                )
                .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl(MUTAGEN_CONNECTION_POOL_NAME)
                                .setPort(valueOf(env.get("MUTAGEN_CONNECTION_POOL_PORT")))
                                .setMaxConnsPerHost(1)
                                .setSeeds(env.get("MUTAGEN_SEEDS"))
                )
                .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
                .buildKeyspace( ThriftFamilyFactory.getInstance());
    }

    protected Keyspace createKeyspaceIfNoneExist(AstyanaxContext<Keyspace> context) throws ConnectionException {
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
