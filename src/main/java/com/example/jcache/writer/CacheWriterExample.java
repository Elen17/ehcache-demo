package com.example.jcache.writer;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🔹 What is a CacheWriter?
 * A CacheWriter is part of the write-through caching mechanism in JCache (JSR-107).
 * It lets you synchronize changes from the cache to an underlying data store (e.g., database, file system, external service).
 * When you enable setWriteThrough(true) and configure a CacheWriterFactory, every cache mutation operation (like put, remove)
 * will also trigger calls to your CacheWriter.
 * setWriteThrough(true) → tells JCache that all writes must also go to the CacheWriter.
 * setCacheWriterFactory(...) → provides a factory that creates an instance of your custom BookCacheWriter.
 * ⚠️ Important: Cache expiry does NOT trigger CacheWriter.delete() automatically.
 * A CacheWriter is only called when you explicitly remove() from the cache, not when TTL expires.
 * Summary:
 * CacheWriter ensures cache mutations are synchronized to an external store.
 * Expiration removes items from cache memory only (doesn’t call writer).
 * For DB cleanup on expiry → combine with a CacheEntryExpiredListener.
 */

public class CacheWriterExample {
    private static final Logger LOGGER = Logger.getLogger(CacheWriterExample.class.getName());

    public static void main(String[] args) {

        try (CachingProvider provider = Caching.getCachingProvider()) {
            try (CacheManager cacheManager = provider.getCacheManager()) {
                MutableConfiguration<Integer, Book> cacheConfiguration = new MutableConfiguration<Integer, Book>()
                        .setTypes(Integer.class, Book.class)
                        .setWriteThrough(true)
                        .setCacheWriterFactory(FactoryBuilder.factoryOf(BookCacheWriter.class))
                        .setExpiryPolicyFactory(FactoryBuilder.factoryOf(new CreatedExpiryPolicy(Duration.ONE_MINUTE)));// Set the expiry policy

                try (Cache<Integer, Book> cacheWriterCache = cacheManager.createCache("bookCache", cacheConfiguration)) {
                    cacheWriterCache.put(1, new Book(1, "Book 1", "Author 1"));
                    cacheWriterCache.put(2, new Book(2, "Book 2", "Author 1"));
                    Thread.sleep(61_000);
                    Book book = cacheWriterCache.get(1);
                    if (book != null) {
                        LOGGER.log(Level.INFO, "{0}", book);
                    } else {
                        LOGGER.info("Cache entry with key 1 has expired or does not exist");
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.severe(e.getMessage());
        }
    }
}
