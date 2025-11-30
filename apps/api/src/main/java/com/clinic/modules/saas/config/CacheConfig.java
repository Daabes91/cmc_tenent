package com.clinic.modules.saas.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for caching billing status information.
 * Uses Caffeine cache with a 5-minute TTL to reduce database queries.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure cache manager with Caffeine.
     * Billing status cache expires after 5 minutes.
     *
     * @return configured CacheManager
     */
    @Bean("billingSaasCacheManager")
    public CacheManager billingSaasCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        CaffeineCache billingStatusCache = new CaffeineCache(
                "billingStatus",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(1000)
                        .recordStats()
                        .build()
        );

        CaffeineCache paypalConfigCache = new CaffeineCache(
                "paypalConfig",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(10)
                        .recordStats()
                        .build()
        );

        CaffeineCache paypalAccessTokenCache = new CaffeineCache(
                "paypalAccessToken",
                Caffeine.newBuilder()
                        .expireAfterWrite(9, TimeUnit.HOURS)
                        .maximumSize(1)
                        .recordStats()
                        .build()
        );

        CaffeineCache tenantPlanCache = new CaffeineCache(
                "tenantPlan",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(1000)
                        .recordStats()
                        .build()
        );

        CaffeineCache planTierConfigCache = new CaffeineCache(
                "planTierConfig",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .maximumSize(100)
                        .recordStats()
                        .build()
        );

        cacheManager.setCaches(List.of(
                billingStatusCache, 
                paypalConfigCache, 
                paypalAccessTokenCache, 
                tenantPlanCache,
                planTierConfigCache
        ));
        return cacheManager;
    }
}
