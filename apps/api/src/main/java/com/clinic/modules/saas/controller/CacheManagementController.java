package com.clinic.modules.saas.controller;

import com.clinic.modules.saas.service.CacheWarmingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing application caches.
 * Provides endpoints for cache warming, clearing, and statistics.
 * Restricted to SaaS managers only.
 */
@RestController
@RequestMapping("/saas/cache")
public class CacheManagementController {

    private static final Logger logger = LoggerFactory.getLogger(CacheManagementController.class);

    private final CacheWarmingService cacheWarmingService;
    private final CacheManager cacheManager;

    public CacheManagementController(
            CacheWarmingService cacheWarmingService,
            CacheManager cacheManager) {
        this.cacheWarmingService = cacheWarmingService;
        this.cacheManager = cacheManager;
    }

    /**
     * Manually trigger cache warming.
     * Pre-loads frequently accessed data into caches.
     *
     * @return success message
     */
    @PostMapping("/warm")
    public ResponseEntity<Map<String, String>> warmCaches() {
        logger.info("Manual cache warming triggered via API");
        
        try {
            cacheWarmingService.manualCacheWarmup();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache warming initiated successfully");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during manual cache warming", e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache warming failed: " + e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Clear all caches.
     * Forces fresh data to be loaded on next request.
     *
     * @return success message
     */
    @PostMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCaches() {
        logger.info("Cache clearing triggered via API");
        
        try {
            int clearedCaches = 0;
            
            // Clear all caches
            for (String cacheName : cacheManager.getCacheNames()) {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    clearedCaches++;
                    logger.info("Cleared cache: {}", cacheName);
                }
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully cleared " + clearedCaches + " caches");
            response.put("status", "success");
            response.put("clearedCaches", String.valueOf(clearedCaches));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error clearing caches", e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache clearing failed: " + e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Clear a specific cache by name.
     *
     * @param cacheName the name of the cache to clear
     * @return success message
     */
    @PostMapping("/clear/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        logger.info("Clearing cache: {}", cacheName);
        
        try {
            var cache = cacheManager.getCache(cacheName);
            
            if (cache == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Cache not found: " + cacheName);
                response.put("status", "error");
                
                return ResponseEntity.notFound().build();
            }
            
            cache.clear();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully cleared cache: " + cacheName);
            response.put("status", "success");
            response.put("cacheName", cacheName);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error clearing cache: {}", cacheName, e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache clearing failed: " + e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get cache statistics.
     * Returns information about all configured caches.
     *
     * @return cache statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        logger.debug("Fetching cache statistics");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            Map<String, Object> cacheDetails = new HashMap<>();
            
            for (String cacheName : cacheManager.getCacheNames()) {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    Map<String, Object> cacheInfo = new HashMap<>();
                    cacheInfo.put("name", cacheName);
                    cacheInfo.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
                    
                    // Try to get Caffeine cache statistics if available
                    try {
                        var nativeCache = cache.getNativeCache();
                        if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                            @SuppressWarnings("unchecked")
                            com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache = 
                                (com.github.benmanes.caffeine.cache.Cache<Object, Object>) nativeCache;
                            
                            var cacheStats = caffeineCache.stats();
                            cacheInfo.put("hitCount", cacheStats.hitCount());
                            cacheInfo.put("missCount", cacheStats.missCount());
                            cacheInfo.put("hitRate", String.format("%.2f%%", cacheStats.hitRate() * 100));
                            cacheInfo.put("evictionCount", cacheStats.evictionCount());
                            cacheInfo.put("estimatedSize", caffeineCache.estimatedSize());
                        }
                    } catch (Exception e) {
                        logger.debug("Could not retrieve detailed stats for cache: {}", cacheName, e);
                    }
                    
                    cacheDetails.put(cacheName, cacheInfo);
                }
            }
            
            stats.put("caches", cacheDetails);
            stats.put("totalCaches", cacheManager.getCacheNames().size());
            stats.put("status", "success");
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error fetching cache statistics", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Failed to fetch cache statistics: " + e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get list of all cache names.
     *
     * @return list of cache names
     */
    @GetMapping("/names")
    public ResponseEntity<Map<String, Object>> getCacheNames() {
        logger.debug("Fetching cache names");
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("cacheNames", cacheManager.getCacheNames());
            response.put("count", cacheManager.getCacheNames().size());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching cache names", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Failed to fetch cache names: " + e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
