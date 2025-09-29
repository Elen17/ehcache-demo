package com.example.jcache.expiry;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExpiryPolicyExample {
    private static final Logger LOGGER = Logger.getLogger(ExpiryPolicyExample.class.getName());

    public static void main(String[] args) {
        try (CachingProvider provider = Caching.getCachingProvider()) {
            try (CacheManager cacheManager = provider.getCacheManager()) {
                MutableConfiguration<String, String> cacheConfiguration = new MutableConfiguration<String, String>()
                        .setTypes(String.class, String.class)
                        .setExpiryPolicyFactory(FactoryBuilder.factoryOf(
                                new CreatedExpiryPolicy(Duration.ONE_MINUTE)));

                try (Cache<String, String> expiringCache = cacheManager.createCache("expiringCache", cacheConfiguration)) {
                    expiringCache.put("key1", "value1");

                    for (Cache.Entry<String, String> entry : expiringCache) {
                        LOGGER.log(Level.INFO, "key: {0}%n, value: {1}%n", new Object[]{entry.getKey(), entry.getValue()});
                    }

                    Thread.sleep(61_000);
                    String value = expiringCache.get("key1");
                    LOGGER.info(value);

                    for (Cache.Entry<String, String> entry : expiringCache) {
                        LOGGER.log(Level.INFO, "key: {0}%n, value: {1}%n", new Object[]{entry.getKey(), entry.getValue()});
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.severe(e.getMessage());
        }

    }

}
