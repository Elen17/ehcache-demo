package com.example.jcache.loader;

import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyCacheLoader implements CacheLoader<String, String> {
    private static final Logger LOGGER = Logger.getLogger(MyCacheLoader.class.getName());
    @Override
    public String load(String key) throws CacheLoaderException {
        // In a real application, this would retrieve data from a database or other backend
        LOGGER.log(Level.INFO, "Loading key: {0}", key);
        return "Value for " + key;
    }

    @Override
    public Map<String, String> loadAll(Iterable<? extends String> keys) throws CacheLoaderException {
        Map<String, String> map = new HashMap<>();
        for (String key : keys) {
            map.put(key, load(key)); // Reuse the load method for individual keys
        }
        return map;
    }
}
