package com.example.jcache.listener;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.Random;
import java.util.logging.Logger;

public class ListenerExample {

    private static final Logger LOGGER = Logger.getLogger(ListenerExample.class.getName());

    public static void main(String[] args) {
        try (CachingProvider provider = Caching.getCachingProvider()) {
            try (CacheManager cacheManager = provider.getCacheManager()) {

                CacheEntryListenerConfiguration<String, String> listenerConfig =
                        new MutableCacheEntryListenerConfiguration<>(
                                FactoryBuilder.factoryOf(MyListener.class), null, false, true);

                MutableConfiguration<String, String> cacheConfiguration = new MutableConfiguration<String, String>()
                        .setTypes(String.class, String.class)
                        .addCacheEntryListenerConfiguration(listenerConfig);

                try (Cache<String, String> listenerCache = cacheManager.createCache("listenerCache", cacheConfiguration)) {
                    for (int i = 0; i < 15; i++) {
                        listenerCache.put("key" + i, "value" + i);
                    }


                    for (int i = 0; i < 5; i++) {
                        int index = new Random().nextInt();
                        listenerCache.put("key" + index, listenerCache.get("key" + index) + "-updated");
                    }

                    listenerCache.remove("key1");
                }
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }
}
