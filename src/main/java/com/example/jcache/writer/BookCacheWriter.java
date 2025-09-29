package com.example.jcache.writer;

import com.google.common.base.Preconditions;

import javax.cache.Cache;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookCacheWriter implements CacheWriter<Integer, Book> {
    private static final Logger LOGGER = Logger.getLogger(BookCacheWriter.class.getName());

    @Override
    public void write(Cache.Entry<? extends Integer, ? extends Book> entry) throws CacheWriterException {
        // Called when a single entry is added or updated in the cache
        LOGGER.info("Writing " + entry.getKey() + " to cache");
        // Here you would persist to database; for synchronous writes, this could be a blocking call
    }

    @Override
    public void writeAll(Collection<Cache.Entry<? extends Integer, ? extends Book>> collection) throws CacheWriterException {
        // Called when multiple entries are written (e.g., putAll)
        LOGGER.log(Level.INFO, "Writing {0} entries to cache", collection.size());
    }

    @Override
    public void delete(Object o) throws CacheWriterException {
        LOGGER.log(Level.INFO, "Deleting {0} from cache", o);
        // Here you would remove from database
    }

    @Override
    public void deleteAll(Collection<?> collection) throws CacheWriterException {
        int size  = collection.size();
        Preconditions.checkArgument(size > 0, "collection is empty");
        LOGGER.log(Level.INFO, "Deleting {0} entries from cache", size);
    }
}
