// API Response Caching Composable
interface CacheEntry<T> {
  data: T
  timestamp: number
  ttl: number
}

interface CacheOptions {
  ttl?: number // Time to live in milliseconds
  key?: string // Custom cache key
}

export const useApiCache = () => {
  // In-memory cache store
  const cache = new Map<string, CacheEntry<any>>()

  // Default TTL values for different data types
  const DEFAULT_TTL = {
    systemMetrics: 30000, // 30 seconds
    tenantList: 60000, // 1 minute
    tenantDetail: 120000, // 2 minutes
    analytics: 300000, // 5 minutes
    auditLogs: 60000, // 1 minute
    branding: 300000 // 5 minutes
  }

  /**
   * Generate cache key from endpoint and params
   */
  const generateKey = (endpoint: string, params?: Record<string, any>): string => {
    if (!params) return endpoint
    
    const sortedParams = Object.keys(params)
      .sort()
      .reduce((acc, key) => {
        acc[key] = params[key]
        return acc
      }, {} as Record<string, any>)
    
    return `${endpoint}:${JSON.stringify(sortedParams)}`
  }

  /**
   * Check if cache entry is still valid
   */
  const isValid = (entry: CacheEntry<any>): boolean => {
    return Date.now() - entry.timestamp < entry.ttl
  }

  /**
   * Get data from cache
   */
  const get = <T>(key: string): T | null => {
    const entry = cache.get(key)
    
    if (!entry) return null
    
    if (!isValid(entry)) {
      cache.delete(key)
      return null
    }
    
    return entry.data as T
  }

  /**
   * Set data in cache
   */
  const set = <T>(key: string, data: T, ttl: number): void => {
    cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl
    })
  }

  /**
   * Invalidate cache entry
   */
  const invalidate = (key: string): void => {
    cache.delete(key)
  }

  /**
   * Invalidate all cache entries matching a pattern
   */
  const invalidatePattern = (pattern: string): void => {
    const regex = new RegExp(pattern)
    
    for (const key of cache.keys()) {
      if (regex.test(key)) {
        cache.delete(key)
      }
    }
  }

  /**
   * Clear all cache
   */
  const clear = (): void => {
    cache.clear()
  }

  /**
   * Cached API call wrapper
   */
  const cachedCall = async <T>(
    fetcher: () => Promise<T>,
    endpoint: string,
    options: CacheOptions = {}
  ): Promise<T> => {
    const cacheKey = options.key || endpoint
    const ttl = options.ttl || DEFAULT_TTL.tenantList

    // Try to get from cache first
    const cached = get<T>(cacheKey)
    if (cached !== null) {
      return cached
    }

    // Fetch fresh data
    const data = await fetcher()
    
    // Store in cache
    set(cacheKey, data, ttl)
    
    return data
  }

  /**
   * Prefetch and cache data
   */
  const prefetch = async <T>(
    fetcher: () => Promise<T>,
    endpoint: string,
    options: CacheOptions = {}
  ): Promise<void> => {
    const cacheKey = options.key || endpoint
    const ttl = options.ttl || DEFAULT_TTL.tenantList

    try {
      const data = await fetcher()
      set(cacheKey, data, ttl)
    } catch (error) {
      console.error('Prefetch failed:', error)
    }
  }

  return {
    generateKey,
    get,
    set,
    invalidate,
    invalidatePattern,
    clear,
    cachedCall,
    prefetch,
    DEFAULT_TTL
  }
}
