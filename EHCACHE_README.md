### Ehcache 3 — README & Quick Start

## Overview

Ehcache is a robust, standards-based Java caching library. Version 3 introduces a modern, type-safe API, JSR-107 (
javax.cache) compatibility, off-heap/disk persistence, and more.

This document shows how to:

* Add Ehcache as a dependency
* Configure caches (programmatically / via XML)
* Use basic cache operations
* Integrate with JSR-107 (javax.cache)
* (Optional) sample for persisting to disk

## Prerequisites

* Java 8 or above
* A build tool (Maven / Gradle)
* (If using javax.cache) include the `javax.cache:cache-api` dependency

---

## Dependency Setup

### Maven

```xml

<dependencies>
    <dependency>
        <groupId>org.ehcache</groupId>
        <artifactId>ehcache</artifactId>
        <version>3.11.1</version>
        <!-- If in a Jakarta (e.g. Spring Boot 3) setup, use the `jakarta` classifier -->
        <!-- <classifier>jakarta</classifier> -->
    </dependency>
    <!-- If using JSR-107 (javax.cache) API: -->
    <dependency>
        <groupId>javax.cache</groupId>
        <artifactId>cache-api</artifactId>
        <version>1.1.0</version>
    </dependency>
</dependencies>
```

### Gradle (Groovy)

```groovy
implementation "org.ehcache:ehcache:3.11.1"
// For Jakarta compatibility (if needed):
// implementation "org.ehcache:ehcache:3.11.1:jakarta"

implementation "javax.cache:cache-api:1.1.0"
```

---

## Programmatic (Java) Configuration & Usage

Here’s a minimal example:

```java
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.ExpiryPolicy;
import org.ehcache.expiry.ExpiryPolicyBuilder;

import java.time.Duration;

public class EhcacheExample {
    public static void main(String[] args) {
        // Build & initialize cache manager
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

        // Create a cache: key Long, value String, heap size 100 entries
        Cache<Long, String> myCache = cacheManager.createCache(
                "myCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                Long.class,
                                String.class,
                                ResourcePoolsBuilder.heap(100)
                        )
                        // optional: set expiry (time-to-live)
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(60)))
                        .build()
        );

        // Use cache: put / get
        myCache.put(1L, "hello");
        String val = myCache.get(1L);
        System.out.println("cached: " + val);

        // Close (shut down) the manager
        cacheManager.close();
    }
}
```

Key points:

* You must call `build(true)` or call `.init()` before using the caches.
* Always close (`.close()`) the manager when done to release resources.
* You can configure expiry, off-heap, disk, etc., via the builder.

### Disk-persistent cache (optional)

If you want to persist overflow to disk or have a three-tier cache (heap + offheap + disk):

```java
    import org.ehcache.PersistentCacheManager;
    import org.ehcache.config.builders.ResourcePoolsBuilder;
    import org.ehcache.config.units.MemoryUnit;
    
    import java.nio.file.Paths;
    
    PersistentCacheManager pManager = CacheManagerBuilder
            .newCacheManagerBuilder()
            .with(CacheManagerBuilder.persistence(Paths.get("cache-data")))
            .withCache(
                    "persistentCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(
                            Long.class, String.class,
                            ResourcePoolsBuilder.newResourcePoolsBuilder()
                                    .heap(50, org.ehcache.config.units.EntryUnit.ENTRIES)
                                    .offheap(10, MemoryUnit.MB)
                                    .disk(20, MemoryUnit.MB, true)
                    )
            )
            .build(true);
    
    // Use the cache similarly
    Cache<Long, String> pc = pManager.getCache("persistentCache", Long.class, String.class);
    pc.put(10L,"persisted");
    
    // Close
    pManager.close();
```

---

## XML Configuration Example

You can also define caches via an `ehcache.xml` (v3 schema). Then load via `XmlConfiguration`. ([ehcache.org][2])

A sample `ehcache.xml`:

```xml

<config xmlns="http://www.ehcache.org/v3">
    <cache alias="myCache">
        <key-type>java.lang.Long</key-type>
        <value-type>java.lang.String</value-type>
        <expiry>
            <ttl unit="seconds">60</ttl>
        </expiry>
        <heap unit="entries">100</heap>
    </cache>
</config>
```

Loading from XML:

```java
import org.ehcache.config.xml.XmlConfiguration;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;

URL xmlUrl = getClass().getResource("/ehcache.xml");
XmlConfiguration xmlConfig = new XmlConfiguration(xmlUrl);
CacheManager cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
cacheManager.init();

Cache<Long, String> myCache = cacheManager.getCache("myCache", Long.class, String.class);
```

You can also use `<cache-template>` definitions in XML to reuse settings. ([ehcache.org][2])

---

## Using JSR-107 (javax.cache) API

If you want your code to be portable via the standardized JCache API, you can do:

```java
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.Cache;

import org.ehcache.jsr107.EhcacheCachingProvider;

URI uri = getClass().getResource("/ehcache.xml").toURI();
ClassLoader cl = getClass().getClassLoader();
CacheManager jcacheManager = Caching.getCachingProvider(
        EhcacheCachingProvider.class.getName()
).getCacheManager(uri, cl);

// Use javax.cache.Cache API
Cache<Long, String> jcache = jcacheManager.getCache("myCache", Long.class, String.class);
jcache.put(2L,"world");

String v = jcache.get(2L);

// Close
jcacheManager.close();
```

Ehcache serves as the **implementation** behind the JCache (javax.cache) interface.

---

## Project README Template

Here’s a skeleton you can include in your project:

````markdown
# MyProject — Ehcache Integration

## Overview

This project uses **Ehcache 3.x** for in-process caching of application data.  
We support both programmatic and XML-based configuration, and optional JCache compatibility.

## Requirements

- Java 8+
- (If using JCache) `javax.cache:cache-api` dependency

## Setup

### Dependency

(Add in your `pom.xml` or `build.gradle`)

```xml
<dependency>
  <groupId>org.ehcache</groupId>
  <artifactId>ehcache</artifactId>
  <version>3.11.1</version>
  <!-- Add <classifier>jakarta</classifier> if needed -->
</dependency>

<dependency>
  <groupId>javax.cache</groupId>
  <artifactId>cache-api</artifactId>
  <version>1.1.0</version>
</dependency>
````

### Configuration Options

1. **Programmatic** — build `CacheManager`, then define `CacheConfigurationBuilder`
2. **XML** — use an `ehcache.xml` file and load with `XmlConfiguration`
3. **JSR-107** — use `javax.cache.Caching` APIs with Ehcache as provider

### Usage Example (Java)

```java
// … (see Programmatic example above) …
```

### XML Example

```xml
<!-- /resources/ehcache.xml -->
<config xmlns="http://www.ehcache.org/v3">
    <cache alias="myCache">
        <key-type>java.lang.Long</key-type>
        <value-type>java.lang.String</value-type>
        <expiry>
            <ttl unit="seconds">60</ttl>
        </expiry>
        <heap unit="entries">100</heap>
    </cache>
</config>
```

### JCache Example

```java
// … (see JSR-107 example above) …
```

## Notes & Tips

* Always close (`cacheManager.close()`) to avoid resource leaks
* Use expiration policies to avoid stale entries
* Use off-heap/disk if your cache may exceed heap size
* For Jakarta EE / Spring Boot 3 compatibility, you may need the `jakarta` classifier on the
  dependency ([Stack Overflow][3])

---

[1]: https://www.ehcache.org/?utm_source=chatgpt.com "Ehcache"

[2]: https://www.ehcache.org/documentation/3.7/xml.html?utm_source=chatgpt.com "XML Configuration"

[3]: https://stackoverflow.com/questions/75719824/how-to-implement-second-level-caching-in-spring-boot-3-using-ehcache-3?utm_source=chatgpt.com "How to implement second-level caching in Spring Boot 3 ..."

## Advantages of Ehcache

✅ Mature & stable (production since 2003)
✅ Lightweight & embeddable (runs inside JVM)
✅ Flexible storage tiers: heap, off-heap, disk
✅ JSR-107 (JCache) support
✅ Integration with Hibernate, Spring, etc.
✅ Persistence (survives restarts)

---

## Disadvantages of Ehcache

⚠️ No distributed caching in OSS (requires commercial Terracotta)
⚠️ Heap caching may cause GC overhead if misconfigured
⚠️ Manual tuning needed for expiry/memory tiers
⚠️ Not cloud-native by default

---

## When to Use Ehcache

* Best for **in-JVM caching** (local to your application)
* Good for **Hibernate 2nd-level cache**
* When you want **disk persistence**
* When you don’t need **distributed caching**

---

## Alternatives & Use Cases

| Tool           | Type                  | Pros                                           | Cons                             | Best Use Case                                       |
|----------------|-----------------------|------------------------------------------------|----------------------------------|-----------------------------------------------------|
| **Ehcache**    | In-JVM w/ persistence | Mature, disk/off-heap, Hibernate-friendly      | No built-in clustering           | Enterprise apps needing persistence + local speed   |
| **Caffeine**   | In-JVM (heap-only)    | Extremely fast, async eviction (W-TinyLFU)     | No disk/off-heap, no clustering  | Microservices needing ultra-fast method-level cache |
| **Redis**      | Distributed cache     | Cloud-native, clustering, persistence, pub/sub | Network latency, extra infra     | Shared cache across multiple nodes/services         |
| **Memcached**  | Distributed cache     | Lightweight, very fast                         | No persistence, limited features | Stateless ephemeral caching (e.g., sessions)        |
| **Hazelcast**  | Distributed data grid | Clustering, computation, rich features         | More complex, heavier            | Data grid + distributed caching needs               |
| **Infinispan** | Distributed data grid | Advanced features, clustering                  | Learning curve, footprint        | Complex distributed caching & data grid workloads   |

---

## Decision Guide (Diagram)

```text
                       ┌───────────────────────────┐
                       │   Do you need a cache?    │
                       └─────────────┬─────────────┘
                                     │
                ┌────────────────────┴────────────────────┐
                │                                         │
     Need cache only in JVM?                 Need cache shared across nodes?
                │                                         │
        ┌───────┴────────┐                       ┌────────┴─────────┐
        │                │                       │                  │
Need persistence?    Need fastest heap?     Cloud-native service?   In-JVM distributed grid?
        │                │                       │                  │
   ┌────┴─────┐     ┌────┴─────┐            ┌────┴──────┐      ┌────┴──────┐
   │  Ehcache │     │ Caffeine │            │   Redis    │      │ Hazelcast │
   └──────────┘     └──────────┘            └────────────┘      └───────────┘
```

---

## Quick Setup Examples

### Maven

```xml

<dependency>
    <groupId>org.ehcache</groupId>
    <artifactId>ehcache</artifactId>
    <version>3.11.1</version>
</dependency>
```

### Java Programmatic Config

```java
CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

Cache<Long, String> cache = cacheManager.createCache("myCache",
        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                Long.class, String.class, ResourcePoolsBuilder.heap(100))
);

cache.

put(1L,"hello");
System.out.

println(cache.get(1L));

        cacheManager.

close();
```

### XML Config (`resources/ehcache.xml`)

```xml

<config xmlns="http://www.ehcache.org/v3">
    <cache alias="myCache">
        <key-type>java.lang.Long</key-type>
        <value-type>java.lang.String</value-type>
        <expiry>
            <ttl unit="seconds">60</ttl>
        </expiry>
        <heap unit="entries">100</heap>
    </cache>
</config>
```

---
