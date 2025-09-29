---

# 📘 JCache (JSR 107) and Ehcache — Full Guide

This document provides a conceptual and practical overview of **JCache (JSR 107)** and **Ehcache**, covering caching fundamentals, configuration, annotations, statistics, listeners, integration with JPA, and advanced concepts.

---

## 🚀 What is Caching?

**Caching** is the process of storing frequently accessed data in-memory (or near the application) so that subsequent requests can be served faster.

Benefits:

* ⚡ Improves performance by reducing expensive computations and I/O.
* 📉 Reduces load on databases and external services.
* 🕒 Improves response times and scalability.
* ✅ Manages data freshness using expiry/eviction policies.

---

## 🏷️ JCache (JSR 107)

**JCache** is the **Java caching standard (JSR 107)** that defines a common API for caching in Java.

It is **vendor-neutral**, meaning you can switch between implementations (e.g., Ehcache, Hazelcast, Infinispan) without changing your business logic.

### 🔑 Key Concepts

1. **CachingProvider**

    * Entry point to JCache.
    * Creates and manages `CacheManager`.

   ```java
   CachingProvider provider = Caching.getCachingProvider();
   CacheManager manager = provider.getCacheManager();
   ```

2. **CacheManager**

    * Creates, retrieves, and manages caches.

   ```java
   MutableConfiguration<String, String> config = new MutableConfiguration<String, String>()
           .setTypes(String.class, String.class);
   Cache<String, String> cache = manager.createCache("myCache", config);
   ```

3. **Cache**

    * A map-like structure (`put`, `get`, `remove`, etc.) but with caching semantics.

   ```java
   cache.put("key1", "value1");
   String value = cache.get("key1");
   ```

4. **CacheLoader / Read-Through**

    * Automatically loads values from a data source when cache misses occur.

   ```java
   public class MyLoader implements CacheLoader<String, String> {
       @Override
       public String load(String key) {
           return "ValueFor-" + key;
       }
   }
   ```

   Usage:

   ```java
   new MutableConfiguration<String, String>()
       .setReadThrough(true)
       .setCacheLoaderFactory(FactoryBuilder.factoryOf(MyLoader.class));
   ```

5. **CacheWriter / Write-Through**

    * Persists values into a data source when entries are added/updated/removed.

   ```java
   public class MyWriter implements CacheWriter<String, String> {
       @Override
       public void write(Cache.Entry<? extends String, ? extends String> entry) {
           System.out.println("Persisting " + entry);
       }
   }
   ```

6. **Expiry Policies**

    * Define how long entries stay valid in cache.
    * Examples: `CreatedExpiryPolicy`, `AccessedExpiryPolicy`, `ModifiedExpiryPolicy`.

   ```java
   config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_MINUTE));
   ```

7. **Events and Listeners**

    * React to cache events (e.g., created, updated, expired, removed).

   ```java
   @Override
   public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends String>> events) {
       for (CacheEntryEvent<?, ?> e : events) {
           System.out.println("Expired: " + e.getKey());
       }
   }
   ```

8. **Statistics & Management**

    * Monitor cache performance.

   ```java
   config.setStatisticsEnabled(true);
   CacheStatisticsMXBean stats = manager.unwrap(CacheManager.class)
       .getCache("myCache", String.class, String.class)
       .getManagementBeanFactory()
       .getCacheStatistics("myCache");
   System.out.println("Hits: " + stats.getCacheHits());
   ```

---

## 🟦 Ehcache

**Ehcache** is the most widely used **JCache implementation**.
It provides both:

* **Stand-alone API** (Ehcache native)
* **JCache-compliant API**

### 🔑 Features

* **Persistent caching** (store data on disk).
* **Heap + off-heap storage**.
* **Clustered caching**.
* **Integration with Hibernate / JPA**.
* **Advanced eviction/expiry policies**.
* **Management and monitoring** via JMX.

---

## ⚙️ Configurations

### Programmatic (Ehcache + JCache)

```java
CacheManager manager = Caching.getCachingProvider().getCacheManager();
MutableConfiguration<String, String> config = new MutableConfiguration<>();
config.setTypes(String.class, String.class)
      .setStatisticsEnabled(true);
Cache<String, String> cache = manager.createCache("demoCache", config);
```

### XML-based (Ehcache native)

```xml
<config xmlns="http://www.ehcache.org/v3">
  <cache alias="myCache">
    <heap unit="entries">100</heap>
    <expiry>
      <ttl unit="seconds">60</ttl>
    </expiry>
  </cache>
</config>
```

```java
Configuration xmlConfig = new XmlConfiguration(getClass().getResource("/ehcache.xml"));
CacheManager ehCacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
```

---

## 📊 Cache Statistics

Metrics include:

* **Cache Hits / Misses**
* **Evictions**
* **Expirations**
* **Average Get Time**
* **Put/Remove Count**

These help measure:

* Effectiveness of caching.
* Whether to adjust expiry/eviction policies.

---

## 🎯 JPA Second-Level Cache (Hibernate + Ehcache)

* Hibernate supports **first-level cache** (per session) by default.
* **Second-level cache** stores entities across sessions.

### Setup

```xml
<property name="hibernate.cache.use_second_level_cache" value="true"/>
<property name="hibernate.cache.region.factory_class"
          value="org.hibernate.cache.jcache.JCacheRegionFactory"/>
<property name="hibernate.javax.cache.provider" value="org.ehcache.jsr107.EhcacheCachingProvider"/>
```

Entity example:

```java
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Product {
    @Id
    private Long id;
    private String name;
}
```

---

## 📌 Use Cases

* **External Service Calls** → cache results to reduce latency.
* **Database Queries** → reduce load on DB with second-level caching.
* **Expensive Computations** → store intermediate results.
* **Session Storage** → maintain state in distributed systems.

---

## ✅ Best Practices

1. Enable **statistics** and monitor cache efficiency.
2. Use **read-through / write-through** for consistency with data source.
3. Choose **expiry policies** wisely (avoid stale data).
4. For **critical data**, use cache as a complement, not as a source of truth.
5. When using with **JPA/Hibernate**, carefully configure cache regions.

---

## 📚 References

* [JCache (JSR 107) Spec](https://jcp.org/en/jsr/detail?id=107)
* [Ehcache Documentation](https://www.ehcache.org/documentation/3.0/)
* [Hibernate Second-Level Cache](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#caching)

---
