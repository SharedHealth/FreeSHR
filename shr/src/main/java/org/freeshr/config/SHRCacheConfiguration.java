package org.freeshr.config;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static net.sf.ehcache.CacheManager.newInstance;

@Configuration
@EnableCaching(proxyTargetClass = true)
public class SHRCacheConfiguration implements CachingConfigurer {

    @Autowired
    private SHRProperties shrProperties;

    @Bean(destroyMethod = "shutdown", name = "ehCacheManager")
    public net.sf.ehcache.CacheManager ehCacheManager() {
        CacheConfiguration trCacheConfig = getTrCacheConfiguration();
        trCacheConfig.persistence(getPersistenceConfiguration());

        CacheConfiguration identityCacheConfig = getIdentityCacheConfiguration();
        identityCacheConfig.persistence(getPersistenceConfiguration());
        
        net.sf.ehcache.config.Configuration ehCacheConfig = new net.sf.ehcache.config.Configuration();
        ehCacheConfig.addCache(trCacheConfig);
        ehCacheConfig.addCache(identityCacheConfig);

        return newInstance(ehCacheConfig);
    }

    private CacheConfiguration getTrCacheConfiguration() {
        CacheConfiguration cacheConfig = new CacheConfiguration();
        cacheConfig.setName("trCache");
        cacheConfig.setMemoryStoreEvictionPolicy("LRU");
        cacheConfig.setMaxEntriesLocalHeap(10000);
        cacheConfig.setTimeToLiveSeconds(shrProperties.getLocalCacheTTL());
        return cacheConfig;
    }

    private PersistenceConfiguration getPersistenceConfiguration() {
        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration();
        persistenceConfiguration.setStrategy("NONE");
        return persistenceConfiguration;
    }

    private CacheConfiguration getIdentityCacheConfiguration() {
        CacheConfiguration cacheConfig = new CacheConfiguration();
        cacheConfig.setName("identityCache");
        cacheConfig.setMemoryStoreEvictionPolicy("LRU");
        cacheConfig.setMaxEntriesLocalHeap(1000);
        cacheConfig.setTimeToLiveSeconds(shrProperties.getIdentityCacheTTL());
        return cacheConfig;
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManager());
    }

    @Override
    public CacheResolver cacheResolver() {
        return new SimpleCacheResolver(cacheManager());
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }
}
