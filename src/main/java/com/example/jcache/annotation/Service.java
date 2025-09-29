package com.example.jcache.annotation;

import javax.cache.annotation.CacheResult;

public interface Service {
    @CacheResult(cacheName = "expensiveOps")
    String expensiveOperation(String input);
}
