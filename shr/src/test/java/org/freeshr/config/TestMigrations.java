package org.freeshr.config;

import com.google.common.collect.ImmutableMap;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.exceptions.BadRequestException;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import org.freeshr.utils.cassandra.Migrations;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.lang.Integer.valueOf;

public class TestMigrations extends Migrations {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TestMigrations.class);

    private static final String MUTAGEN_CONNECTION_POOL_NAME = "shrMigrationConnectionPool";

    public TestMigrations(Map<String, String> env) {
        super(env);
    }

    @Override
    protected Keyspace createKeyspaceIfNoneExist(AstyanaxContext<Keyspace> context) throws ConnectionException {
        Keyspace keyspace = context.getClient();
        try {
            keyspace.describeKeyspace();
        } catch (BadRequestException e) {
            keyspace.createKeyspace(ImmutableMap.<String, Object>builder()
                    .put("strategy_options", ImmutableMap.<String, Object>builder()
                            .put("replication_factor", "1")
                            .build())
                    .put("strategy_class", "SimpleStrategy")
                    .put("durable_writes", false)
                    .put("key_cache_save_period", 0)
                    .put("row_cache_save_period", 0)
                    .build());

        }
        logger.debug("Creating KEYSPACE");
        return keyspace;
    }

    @Override
    protected AstyanaxContext<Keyspace> buildContext() {
        return new AstyanaxContext.Builder()
                .forKeyspace(env.get("CASSANDRA_KEYSPACE"))
                .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                                .setCqlVersion(env.get("CQL_VERSION"))
                                .setTargetCassandraVersion(env.get("CASSANDRA_VERSION"))
                )
                .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl(MUTAGEN_CONNECTION_POOL_NAME)
                                .setPort(valueOf(env.get("MUTAGEN_CONNECTION_POOL_PORT")))
                                .setMaxConnsPerHost(100)
                                .setSeeds(env.get("MUTAGEN_SEEDS"))
                )
                .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
                .buildKeyspace(ThriftFamilyFactory.getInstance());
    }
}
