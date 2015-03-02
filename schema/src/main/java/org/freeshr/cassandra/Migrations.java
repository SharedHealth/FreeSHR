package org.freeshr.cassandra;


import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.toddfast.mutagen.cassandra.CassandraMutagen;
import com.toddfast.mutagen.cassandra.CassandraSubject;
import com.toddfast.mutagen.cassandra.impl.CassandraMutagenImpl;

import java.io.IOException;
import java.util.Map;

import static java.lang.System.getenv;

public class Migrations {

    private static final String MUTAGEN_CONNECTION_POOL_NAME = "shrMigrationConnectionPool";
    public static final int ONE_MINUTE = 6000;

    protected final Map<String, String> env;

    public Migrations() {
        this(getenv());
    }

    public Migrations(Map<String, String> env) {
        this.env = env;
    }

    public void migrate() throws IOException {
        String freeSHRKeyspace = env.get("CASSANDRA_KEYSPACE");
        Session session = connectKeyspace();

        CassandraMutagen mutagen = new CassandraMutagenImpl(freeSHRKeyspace);
        mutagen.initialize(env.get("CASSANDRA_MIGRATIONS_PATH"));
        com.toddfast.mutagen.Plan.Result<Integer> result = mutagen.mutate(new CassandraSubject(session,
                freeSHRKeyspace));

        if (result.getException() != null) {
            throw new RuntimeException(result.getException());
        } else if (!result.isMutationComplete()) {
            throw new RuntimeException("Failed to apply cassandra migrations");
        }
    }

    private Session connectKeyspace() {
        Cluster cluster = connectCluster();
        return createSession(cluster);
    }

    protected Cluster connectCluster() {
        Cluster.Builder clusterBuilder = new Cluster.Builder();

        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setConsistencyLevel(ConsistencyLevel.QUORUM);

        clusterBuilder
                .withPort(Integer.parseInt(env.get("CASSANDRA_PORT")))
                .withClusterName(env.get("CASSANDRA_KEYSPACE"))
                .withLoadBalancingPolicy(new RoundRobinPolicy())
                .withPoolingOptions(new PoolingOptions())
                .withProtocolVersion(Integer.parseInt(env.get("CASSANDRA_VERSION")))
                .withQueryOptions(queryOptions)
                .withReconnectionPolicy(new ConstantReconnectionPolicy(ONE_MINUTE))
                .addContactPoint(env.get("CASSANDRA_HOST"));
        return clusterBuilder.build();

    }

    protected Session createSession(Cluster cluster) {
        String keyspace = env.get("CASSANDRA_KEYSPACE");
        String replicationStrategy = env.get("CASSANDRA_REPLICATION_STRATEGY");
        String replicationFactor = env.get("CASSANDRA_REPLICATION_FACTOR");

        Session session = cluster.connect();
        session.execute(
                String.format(
                        "CREATE KEYSPACE IF NOT EXISTS %s " +
                                "WITH replication = {'class':'%s', 'replication_factor':%s}; ",
                        keyspace, replicationStrategy, replicationFactor)
        );
        session.close();
        return cluster.connect(keyspace);
    }

    public static void main(String[] args) throws IOException {
        new Migrations().migrate();
    }
}
