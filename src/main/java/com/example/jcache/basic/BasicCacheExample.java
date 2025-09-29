package com.example.jcache.basic;

import com.google.common.base.Preconditions;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicCacheExample {
    private static final Logger LOGGER = Logger.getLogger(BasicCacheExample.class.getName());

    public static void main(String[] args) {
        try (CachingProvider provider = Caching.getCachingProvider()) {
            try (CacheManager cacheManager = provider.getCacheManager()) {
                MutableConfiguration<String, String> cacheConfiguration = new MutableConfiguration<String, String>()
                        .setTypes(String.class, String.class);
                try (Cache<String, String> basicCache = cacheManager.createCache("basicCache", cacheConfiguration)) {
                    basicCache.put("key1", "value1");

                    for (Cache.Entry<String, String> entry : basicCache) {
                        LOGGER.log(Level.INFO, "key: {0}, value: {1}%n", new Object[]{entry.getKey(), entry.getValue()});
                    }

                    String value = basicCache.get("key1");
                    Preconditions.checkNotNull(value);
                    LOGGER.log(Level.FINE, value);

                    basicCache.remove("key1");

                    for (Cache.Entry<String, String> entry : basicCache) {
                        Preconditions.checkNotNull(entry);
                        Preconditions.checkNotNull(entry.getKey());
                        Preconditions.checkNotNull(entry.getValue());
                        LOGGER.log(Level.INFO, "key: {0}%n, value: {1}%n", new Object[]{entry.getKey(), entry.getValue()});
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

    }
}
