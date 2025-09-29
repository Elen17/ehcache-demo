package com.example.jcache.statistics;

import com.example.jcache.loader.MyCacheLoader;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.management.CacheStatisticsMXBean;
import javax.cache.spi.CachingProvider;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 📊 Cache Statistics in JCache
 * 🔹 What are cache statistics?
 * Statistics in JCache are metrics collected at runtime about how your cache is performing. They help you understand:
 * How effective the cache is (are you serving mostly from cache or reloading a lot from DB/external source?).
 * When you need tuning (size, expiry policy, eviction strategy, etc.).
 * Troubleshooting slow performance by measuring cache hits/misses.
 * 🔹 Main metrics you get
 * When you enable statistics:
 * getCacheHits() → Number of times requested data was found in the cache.
 * getCacheMisses() → Number of times requested data was NOT found in the cache.
 * getCacheGets() → Total number of gets (hits + misses).
 * getCachePuts() → Number of times entries were added.
 * getCacheRemovals() → Number of times entries were removed.
 * getAverageGetTime() → Average time to retrieve entries.
 * getAveragePutTime() → Average time to put entries.
 * getAverageRemoveTime() → Average time to remove entries.
 * Use cases
 * Performance tuning: If misses are too high, your expiry/eviction settings may be too aggressive.
 * Monitoring in production: These stats are exposed via JMX → can be hooked into Grafana, Prometheus, or tools like VisualVM.
 * Debugging: Helps you see whether cache is actually being used or bypassed.
 */
public class StatisticsExample {

    private static final Logger LOGGER = Logger.getLogger(StatisticsExample.class.getName());

    public static void main(String[] args) {
        try (CachingProvider provider = Caching.getCachingProvider()) {
            try (CacheManager cacheManager = provider.getCacheManager()) {
                MutableConfiguration<String, String> cacheConfiguration = new MutableConfiguration<String, String>()
                        .setTypes(String.class, String.class)
                        .setStatisticsEnabled(true)
                        .setReadThrough(true)
                        .setCacheLoaderFactory(FactoryBuilder.factoryOf(MyCacheLoader.class)); // Provide the loader factory

                try (Cache<String, String> statisticsCache = cacheManager.createCache("statisticsCache", cacheConfiguration)) {
                    statisticsCache.put("key1", "value1");
                    statisticsCache.put("key2", "value2");

                    for (Cache.Entry<String, String> entry : statisticsCache) {
                        LOGGER.log(Level.INFO, "key: {0}%n, value: {1}%n", new Object[]{entry.getKey(), entry.getValue()});
                    }

                    // later, pull statistics via JMX
                    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                    ObjectName statsName = new ObjectName("javax.cache:type=CacheStatistics,"
                            + "CacheManager=" + cacheManager.getURI().toString().replace(":", ".")
                            + ",Cache=statisticsCache");

                    CacheStatisticsMXBean stats = MBeanServerInvocationHandler
                            .newProxyInstance(mbs, statsName, CacheStatisticsMXBean.class, false);

                    LOGGER.info("Cache Hits   : " + stats.getCacheHits());
                    LOGGER.info("Cache Misses : " + stats.getCacheMisses());
                    LOGGER.info("Cache Puts   : " + stats.getCachePuts());
                    LOGGER.info("Cache Gets   : " + stats.getCacheGets());

                } catch (MalformedObjectNameException e) {
                    LOGGER.severe(e.getMessage());
                }
            }
        }
    }
}
