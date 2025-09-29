package org.example.annotationtest.service;

import org.springframework.stereotype.Component;
import javax.cache.annotation.CacheResult;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class Service {
    private static final Logger LOGGER = Logger.getLogger(Service.class.toString());
    @CacheResult(cacheName = "expensiveOps")
    public String expensiveOperation(String input) {
        LOGGER.log(Level.INFO, "Processing {0} in expensiveOperation to store in the cache", input);
        return "DataFor-" + input + "@" + System.currentTimeMillis();
    }
}
