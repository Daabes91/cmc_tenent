// System Metrics Composable
import type { SystemMetrics } from '~/types'

export const useSystemMetrics = () => {
  const { getSystemMetrics } = useSaasApi()
  const toast = useToast()
  
  const metrics = ref<SystemMetrics | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  
  // Auto-refresh interval (5 minutes for development)
  const REFRESH_INTERVAL = 300000
  let refreshTimer: NodeJS.Timeout | null = null

  // Fetch system metrics
  const fetchMetrics = async (useCache = true) => {
    loading.value = true
    error.value = null

    try {
      const { cachedCall, DEFAULT_TTL } = useApiCache()
      const data = useCache
        ? await cachedCall(
            () => getSystemMetrics(),
            '/metrics/system',
            { ttl: DEFAULT_TTL.systemMetrics }
          )
        : await getSystemMetrics()
      metrics.value = data
    } catch (err: any) {
      console.error('Failed to fetch system metrics:', err)
      error.value = err.message || 'Failed to load system metrics'

      // Provide fallback data on error
      metrics.value = {
        totalTenants: 0,
        activeTenants: 0,
        totalUsers: 0,
        activeUsers: 0,
        apiResponseTime: 0,
        databaseStatus: 'healthy',
        recentActivity: []
      }
    } finally {
      loading.value = false
    }
  }

  // Start auto-refresh
  const startAutoRefresh = () => {
    stopAutoRefresh() // Clear any existing timer
    
    refreshTimer = setInterval(() => {
      fetchMetrics()
    }, REFRESH_INTERVAL)
  }

  // Stop auto-refresh
  const stopAutoRefresh = () => {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
  }

  // Manual refresh (bypass cache)
  const refresh = async () => {
    await fetchMetrics(false)
    startAutoRefresh()
  }

  // Cleanup on unmount
  onUnmounted(() => {
    stopAutoRefresh()
  })

  return {
    metrics: readonly(metrics),
    loading: readonly(loading),
    error: readonly(error),
    refresh,
    startAutoRefresh,
    stopAutoRefresh
  }
}
