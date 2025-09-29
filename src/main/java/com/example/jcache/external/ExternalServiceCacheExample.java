package com.example.jcache.external;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.spi.CachingProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExternalServiceCacheExample {
    private static final Logger LOGGER = Logger.getLogger(ExternalServiceCacheExample.class.getName());
    private static final String PARIS = "Paris";
    public static final String RESULT_0 = "Result: {0}";

    public static void main(String[] args) throws Exception {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        // Configure cache with read-through and expiry
        MutableConfiguration<String, String> config = new MutableConfiguration<String, String>()
                .setTypes(String.class, String.class)
                .setReadThrough(true) // ✅ load from external service on miss
                .setCacheLoaderFactory(FactoryBuilder.factoryOf(WeatherServiceCacheLoader.class))
                .setStatisticsEnabled(true)
                .setExpiryPolicyFactory(FactoryBuilder.factoryOf(new CreatedExpiryPolicy(Duration.ONE_MINUTE)));

        try (Cache<String, String> weatherCache = cacheManager.createCache("weatherCache", config)) {

            // First call - goes to external service
            LOGGER.info("Fetching weather for Paris...");
            String paris1 = weatherCache.get(PARIS);
            LOGGER.log(Level.INFO, RESULT_0, paris1);

            // Second call - served from cache
            LOGGER.info("Fetching weather for Paris again...");
            String paris2 = weatherCache.get(PARIS);
            LOGGER.log(Level.INFO, RESULT_0, paris2);

            // Sleep so you can see expiry effect (simulate cache timeout)
            LOGGER.info("Sleeping for 65s...");
            TimeUnit.SECONDS.sleep(65);

            // Third call - expired, so external service called again
            String paris3 = weatherCache.get(PARIS);
            LOGGER.log(Level.INFO, RESULT_0, paris3);
        }
    }

    // ✅ Custom CacheLoader that calls external service
    public static class WeatherServiceCacheLoader implements CacheLoader<String, String> {
        @Override
        public String load(String city) throws CacheLoaderException {
            // Simulate external REST API call (slow)
            LOGGER.log(Level.INFO, "[ExternalService] Fetching weather for {0} ...", city);
            try {
                Thread.sleep(2000); // simulate network latency
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Weather in " + city + " is SUNNY";
        }

        @Override
        public Map<String, String> loadAll(Iterable<? extends String> keys) {
            Map<String, String> results = new HashMap<>();
            for (String key : keys) {
                results.put(key, load(key));
            }
            return results;
        }
    }
}
