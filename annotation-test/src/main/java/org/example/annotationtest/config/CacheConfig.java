package org.example.annotationtest.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public JCacheCacheManager customCacheManager() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        // Explicitly create the cache if it doesn't exist
        if (cacheManager.getCache("expensiveOps") == null) {
            cacheManager.createCache("expensiveOps",
                    new javax.cache.configuration.MutableConfiguration<>()
                            .setStoreByValue(false)
                            .setStatisticsEnabled(true));
        }

        return new JCacheCacheManager(cacheManager);
    }
}
