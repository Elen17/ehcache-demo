package com.example.jcache.loader;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.util.logging.Logger;

/**
 * If you call cache.get("A") and the key does not exist in the cache:
 * Since readThrough(true) is enabled, the cache will automatically call MyCacheLoader.load("A").
 * The returned value ("ValueFor-A") is stored in the cache.
 * Future cache.get("A") calls will return the cached value, not call load again, unless the entry expires or is evicted.
 * If you call cache.get("A") again (and the entry is still valid in the cache):
 * The cache returns the cached value.
 * load is not called again.
 * If the entry expires, is evicted, or you explicitly call cache.remove("A"),
 * then the next cache.get("A") will trigger load again.
 * So:
 * 👉 It does not call load every time.
 * 👉 It only calls it when the key is missing or invalid, and then caches the result.
 */

public class CacheLoaderExample {
    private static final Logger LOGGER = Logger.getLogger(CacheLoaderExample.class.getName());

    public static void main(String[] args) {
        try (CachingProvider provider = Caching.getCachingProvider()) {
            try (CacheManager cacheManager = provider.getCacheManager()) {

                MutableConfiguration<String, String> cacheConfiguration = new MutableConfiguration<String, String>()
                        .setReadThrough(true)
                        // Enable read-through mode
                        // the cache will automatically call MyCacheLoader.load(key) when a key is not found in the cache
                        // after that, the value will be cached
                        .setCacheLoaderFactory(FactoryBuilder.factoryOf(MyCacheLoader.class)) // Provide the loader factory
                        .setExpiryPolicyFactory(FactoryBuilder.factoryOf(new CreatedExpiryPolicy(Duration.ONE_MINUTE))); // Set the expiry policy

                try (Cache<String, String> cacheLoaderCache = cacheManager.createCache("cacheLoaderCache", cacheConfiguration)) {

                    // as cache is empty should call the loader
                    String value1 = cacheLoaderCache.get("key1");
                    LOGGER.info(value1);
                    // already in cache; no need to call the loader
                    String value2 = cacheLoaderCache.get("key1");
                    LOGGER.info(value2);

                    Thread.sleep(61_000);
                    // cache expired, should call the loader
                    String value = cacheLoaderCache.get("key1");
                    LOGGER.info(value);

                }

            }
        } catch (InterruptedException  e) {
            Thread.currentThread().interrupt();
            LOGGER.severe("Thread was interrupted while sleeping: " + e.getMessage());
        }
    }

}
