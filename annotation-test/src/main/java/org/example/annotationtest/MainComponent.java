package org.example.annotationtest;

import jakarta.annotation.PostConstruct;
import org.example.annotationtest.service.Service;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class MainComponent {
    private static final Logger LOGGER = Logger.getLogger(MainComponent.class.getName());
    private final Service service;

    public MainComponent(Service service) {
        this.service = service;
    }

    @PostConstruct
    public void init() {
        // First call - should execute the method
        LOGGER.info("First call...");
        String result1 = service.expensiveOperation("key");
        LOGGER.log(Level.INFO, "First result: {0}", result1);

        // Second call - should come from cache
        LOGGER.info("Second call...");
        String result2 = service.expensiveOperation("key");
        LOGGER.log(Level.INFO, "Second result: {0}", result2);

        // Results should be the same
        if (result1.equals(result2)) {
            LOGGER.info("Results match - caching is working!");
        } else {
            LOGGER.warning("Results don't match - caching is not working!");
        }
    }
}
