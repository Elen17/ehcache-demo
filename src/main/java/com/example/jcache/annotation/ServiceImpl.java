package com.example.jcache.annotation;

import javax.cache.annotation.CacheResult;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceImpl implements Service {
    private static final Logger LOGGER = Logger.getLogger(ServiceImpl.class.toString());
    @CacheResult(cacheName = "expensiveOps")
    public String expensiveOperation(String input) {
        LOGGER.log(Level.INFO, "Processing {0} in expensiveOperation to store in the cache", input);
        LOGGER.info("Value is stored in the cache");
        return "Processed-" + input;
    }
}
