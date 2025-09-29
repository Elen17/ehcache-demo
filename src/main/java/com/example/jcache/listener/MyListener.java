package com.example.jcache.listener;

import javax.cache.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyListener implements CacheEntryUpdatedListener<String, String>,
        CacheEntryCreatedListener<String, String>,
        CacheEntryRemovedListener<String, String>,
        CacheEntryExpiredListener<String, String> {
    private static final Logger LOGGER = Logger.getLogger(MyListener.class.getName());

    @Override
    @SuppressWarnings("unchecked")
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends String>> iterable) throws CacheEntryListenerException {
        CacheEntryEvent<String, String> event = (CacheEntryEvent<String, String>) iterable.iterator().next();
        LOGGER.info(event.getSource().getName() + " cache was updated.");
        LOGGER.log(Level.INFO, "key: \"{0}\" was updated from \"{1}\" to \"{2}\"", new Object[]{event.getKey(), event.getOldValue(), event.getValue()});
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends String>> iterable) throws CacheEntryListenerException {
        CacheEntryEvent<String, String> event = (CacheEntryEvent<String, String>) iterable.iterator().next();
        LOGGER.info("New entry created in " + event.getSource().getName() + " cache");
        LOGGER.info("key: \"" + event.getKey() + "\", value: \"" + event.getValue() + "\"");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends String>> iterable) throws CacheEntryListenerException {
        CacheEntryEvent<String, String> event = (CacheEntryEvent<String, String>) iterable.iterator().next();

        LOGGER.log(Level.INFO, "Entry removed from {0} cache", event.getSource().getName());
        LOGGER.log(Level.INFO, "key: \"{0}\", value: \"{1}\"", new Object[]{event.getKey(), event.getValue()});
    }

    /**
     * 🔹 Key points:
     * event.getKey() → the key of the expired entry.
     * event.getValue() → the value that was expired.
     * This is where you can synchronize cleanup with your underlying datastore, because unlike CacheWriter, expiry does not call delete() on its own.
     * So your comment is correct 👌 — onExpired is the right place to handle expired entries if you want to delete them from DB (or log, notify, etc.).
     */
    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends String>> iterable) throws CacheEntryListenerException {
        for (CacheEntryEvent<? extends String, ? extends String> event : iterable) {
            LOGGER.info("Entry expired: key=" + event.getKey() + ", value=" + event.getValue());
            // ✅ Here you can remove from DB or trigger cleanup in external datastore
            // e.g., bookRepository.deleteById(event.getKey())
        }
    }
}
