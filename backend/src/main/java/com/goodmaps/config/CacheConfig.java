package com.goodmaps.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Configuration du cache Caffeine pour respecter la politique de quotas OpenStreetMap (anti-quota & anti-rate-limit).
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${goodmaps.cache.geocoding-ttl-hours:24}")
    private int geocodingTtlHours;

    @Value("${goodmaps.cache.activities-ttl-minutes:60}")
    private int activitiesTtlMinutes;

    @Value("${goodmaps.cache.max-size:1000}")
    private int maxCacheSize;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();

        CaffeineCache geocodingCache = new CaffeineCache("geocodingCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(geocodingTtlHours, TimeUnit.HOURS)
                        .maximumSize(maxCacheSize)
                        .recordStats()
                        .build()
        );

        CaffeineCache activitiesCache = new CaffeineCache("activitiesCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(activitiesTtlMinutes, TimeUnit.MINUTES)
                        .maximumSize(maxCacheSize)
                        .recordStats()
                        .build()
        );

        manager.setCaches(List.of(geocodingCache, activitiesCache));
        return manager;
    }
}
