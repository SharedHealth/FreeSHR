package org.freeshr.config;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
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

    public static final int A_DAY_IN_SECONDS = 86400;

    @Bean(destroyMethod = "shutdown", name = "ehCacheManager")
    public net.sf.ehcache.CacheManager ehCacheManager() {
        CacheConfiguration cacheConfig = getCacheConfiguration();
        cacheConfig.persistence(getPersistenceConfiguration());
        net.sf.ehcache.config.Configuration ehCacheConfig = new net.sf.ehcache.config.Configuration();
        ehCacheConfig.addCache(cacheConfig);
        return newInstance(ehCacheConfig);
    }

    private CacheConfiguration getCacheConfiguration() {
        CacheConfiguration cacheConfig = new CacheConfiguration();
        cacheConfig.setName("shrCache");
        cacheConfig.setMemoryStoreEvictionPolicy("LRU");
        cacheConfig.setMaxEntriesLocalHeap(1000);
        cacheConfig.setTimeToLiveSeconds(A_DAY_IN_SECONDS);
        return cacheConfig;
    }

    private PersistenceConfiguration getPersistenceConfiguration() {
        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration();
        persistenceConfiguration.setStrategy("NONE");
        return persistenceConfiguration;
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
