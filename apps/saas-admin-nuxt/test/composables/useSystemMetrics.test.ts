import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useSystemMetrics } from '~/composables/useSystemMetrics'

// Mock dependencies
const mockGetSystemMetrics = vi.fn()
const mockToastAdd = vi.fn()

vi.mock('~/composables/useSaasApi', () => ({
  useSaasApi: () => ({
    getSystemMetrics: mockGetSystemMetrics
  })
}))

vi.mock('#app', () => ({
  useToast: () => ({
    add: mockToastAdd
  }),
  ref: (val: any) => ({ value: val }),
  onUnmounted: vi.fn()
}))

vi.mock('~/composables/useApiCache', () => ({
  useApiCache: () => ({
    cachedCall: vi.fn((fn) => fn()),
    DEFAULT_TTL: { systemMetrics: 30000 }
  })
}))

describe('useSystemMetrics', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('fetchMetrics', () => {
    it('should fetch system metrics successfully', async () => {
      const mockMetrics = {
        totalTenants: 10,
        activeTenants: 8,
        totalUsers: 150,
        activeUsers: 120,
        apiResponseTime: 45,
        databaseStatus: 'healthy' as const,
        recentActivity: []
      }

      mockGetSystemMetrics.mockResolvedValueOnce(mockMetrics)

      const metrics = useSystemMetrics()
      await metrics.fetchMetrics()

      expect(mockGetSystemMetrics).toHaveBeenCalled()
      expect(metrics.metrics.value).toEqual(mockMetrics)
      expect(metrics.loading.value).toBe(false)
      expect(metrics.error.value).toBe(null)
    })

    it('should handle fetch errors with fallback data', async () => {
      mockGetSystemMetrics.mockRejectedValueOnce(new Error('API Error'))

      const metrics = useSystemMetrics()
      await metrics.fetchMetrics()

      expect(metrics.error.value).toBe('API Error')
      expect(metrics.metrics.value).toEqual({
        totalTenants: 0,
        activeTenants: 0,
        totalUsers: 0,
        activeUsers: 0,
        apiResponseTime: 0,
        databaseStatus: 'healthy',
        recentActivity: []
      })
      expect(mockToastAdd).toHaveBeenCalledWith(
        expect.objectContaining({
          title: 'Error',
          color: 'red'
        })
      )
    })

    it('should not show toast on subsequent errors if metrics exist', async () => {
      const mockMetrics = {
        totalTenants: 10,
        activeTenants: 8,
        totalUsers: 150,
        activeUsers: 120,
        apiResponseTime: 45,
        databaseStatus: 'healthy' as const,
        recentActivity: []
      }

      mockGetSystemMetrics.mockResolvedValueOnce(mockMetrics)
      const metrics = useSystemMetrics()
      await metrics.fetchMetrics()

      mockToastAdd.mockClear()
      mockGetSystemMetrics.mockRejectedValueOnce(new Error('API Error'))
      await metrics.fetchMetrics()

      expect(mockToastAdd).not.toHaveBeenCalled()
    })
  })

  describe('refresh', () => {
    it('should bypass cache and restart auto-refresh', async () => {
      const mockMetrics = {
        totalTenants: 10,
        activeTenants: 8,
        totalUsers: 150,
        activeUsers: 120,
        apiResponseTime: 45,
        databaseStatus: 'healthy' as const,
        recentActivity: []
      }

      mockGetSystemMetrics.mockResolvedValue(mockMetrics)

      const metrics = useSystemMetrics()
      await metrics.refresh()

      expect(mockGetSystemMetrics).toHaveBeenCalled()
    })
  })

  describe('auto-refresh', () => {
    it('should start auto-refresh timer', async () => {
      const mockMetrics = {
        totalTenants: 10,
        activeTenants: 8,
        totalUsers: 150,
        activeUsers: 120,
        apiResponseTime: 45,
        databaseStatus: 'healthy' as const,
        recentActivity: []
      }

      mockGetSystemMetrics.mockResolvedValue(mockMetrics)

      const metrics = useSystemMetrics()
      metrics.startAutoRefresh()

      // Fast-forward time by 30 seconds
      await vi.advanceTimersByTimeAsync(30000)

      expect(mockGetSystemMetrics).toHaveBeenCalled()
    })

    it('should stop auto-refresh timer', () => {
      const metrics = useSystemMetrics()
      metrics.startAutoRefresh()
      metrics.stopAutoRefresh()

      // Verify timer is cleared by checking no calls after time advance
      mockGetSystemMetrics.mockClear()
      vi.advanceTimersByTime(30000)

      expect(mockGetSystemMetrics).not.toHaveBeenCalled()
    })
  })
})
