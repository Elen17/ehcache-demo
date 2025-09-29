package com.example.jcache.annotation;


import com.google.common.base.Preconditions;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.annotation.CacheResult;
import javax.cache.configuration.MutableConfiguration;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;

public class AnnotationExample {
    private static final Logger LOGGER = Logger.getLogger(AnnotationExample.class.toString());

    public static void main(String[] args) {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        MutableConfiguration<String, String> config = new MutableConfiguration<>();
        Cache<String, String> cache = cacheManager.createCache("expensiveOps", config);

        Service realService = new ServiceImpl();

        // Create proxy with caching behavior
        Service cachedService = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class[]{Service.class},
                new CacheInterceptor(realService, cache));

        // First → real call
        String result1 = cachedService.expensiveOperation("test1");
        Preconditions.checkNotNull(result1);
        LOGGER.info(result1);
        // Second → from cache
        String result2 = cachedService.expensiveOperation("test1");
        Preconditions.checkNotNull(result2);
        LOGGER.info(result2);
        // Results should be the same
    }


}

class CacheInterceptor implements InvocationHandler {
    private final Object target;
    private final Cache<String, String> cache;

    public CacheInterceptor(Object target, Cache<String, String> cache) {
        this.target = target;
        this.cache = cache;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        CacheResult annotation = method.getAnnotation(CacheResult.class);
        if (annotation != null) {
            String key = (String) args[0];
            String cached = cache.get(key);
            if (cached != null) {
                return cached;
            }
            String result = (String) method.invoke(target, args);
            cache.put(key, result);
            return result;
        }
        return method.invoke(target, args);
    }
}
