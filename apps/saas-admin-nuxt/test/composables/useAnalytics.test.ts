import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useAnalytics } from '~/composables/useAnalytics'

// Mock dependencies
const mockGetAnalytics = vi.fn()
const mockExportAnalytics = vi.fn()
const mockToastAdd = vi.fn()

vi.mock('~/composables/useSaasApi', () => ({
  useSaasApi: () => ({
    getAnalytics: mockGetAnalytics,
    exportAnalytics: mockExportAnalytics
  })
}))

vi.mock('#app', () => ({
  useToast: () => ({
    add: mockToastAdd
  }),
  ref: (val: any) => ({ value: val })
}))

vi.mock('~/composables/useApiCache', () => ({
  useApiCache: () => ({
    cachedCall: vi.fn((fn) => fn()),
    generateKey: vi.fn(),
    DEFAULT_TTL: { analytics: 300000 }
  })
}))

describe('useAnalytics', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('fetchAnalytics', () => {
    it('should fetch analytics data successfully', async () => {
      const mockData = {
        tenantGrowth: [
          { date: '2024-01-01', totalTenants: 10, newTenants: 2 },
          { date: '2024-01-02', totalTenants: 12, newTenants: 2 }
        ],
        userActivity: {
          totalLogins: 500,
          activeUsers: 120
        },
        resourceUsage: {
          databaseSizeMB: 1024,
          apiCallVolume: 50000
        }
      }

      mockGetAnalytics.mockResolvedValueOnce(mockData)

      const analytics = useAnalytics()
      const params = { timeRange: '7d', startDate: '2024-01-01', endDate: '2024-01-07' }
      const result = await analytics.fetchAnalytics(params)

      expect(mockGetAnalytics).toHaveBeenCalledWith(params)
      expect(result).toEqual(mockData)
      expect(analytics.analyticsData.value).toEqual(mockData)
      expect(analytics.loading.value).toBe(false)
    })

    it('should handle fetch errors', async () => {
      mockGetAnalytics.mockRejectedValueOnce(new Error('Failed to fetch'))

      const analytics = useAnalytics()
      const params = { timeRange: '7d', startDate: '2024-01-01', endDate: '2024-01-07' }

      await expect(analytics.fetchAnalytics(params)).rejects.toThrow('Failed to fetch')
      expect(mockToastAdd).toHaveBeenCalledWith(
        expect.objectContaining({
          title: 'Error',
          color: 'red'
        })
      )
      expect(analytics.loading.value).toBe(false)
    })
  })

  describe('exportAnalyticsPDF', () => {
    it('should export analytics to PDF successfully', async () => {
      const mockBlob = new Blob(['pdf content'], { type: 'application/pdf' })
      mockExportAnalytics.mockResolvedValueOnce(mockBlob)

      // Mock DOM methods
      const mockCreateElement = vi.spyOn(document, 'createElement')
      const mockAppendChild = vi.spyOn(document.body, 'appendChild')
      const mockRemoveChild = vi.spyOn(document.body, 'removeChild')
      const mockClick = vi.fn()
      
      mockCreateElement.mockReturnValue({
        click: mockClick,
        href: '',
        download: ''
      } as any)

      const analytics = useAnalytics()
      const params = { timeRange: '30d', startDate: '2024-01-01', endDate: '2024-01-30' }
      
      await analytics.exportAnalyticsPDF(params)

      expect(mockExportAnalytics).toHaveBeenCalledWith(params)
      expect(mockToastAdd).toHaveBeenCalledWith(
        expect.objectContaining({
          title: 'Success',
          color: 'green'
        })
      )
      expect(mockClick).toHaveBeenCalled()
    })

    it('should handle export errors', async () => {
      mockExportAnalytics.mockRejectedValueOnce(new Error('Export failed'))

      const analytics = useAnalytics()
      const params = { timeRange: '30d', startDate: '2024-01-01', endDate: '2024-01-30' }

      await expect(analytics.exportAnalyticsPDF(params)).rejects.toThrow('Export failed')
      expect(mockToastAdd).toHaveBeenCalledWith(
        expect.objectContaining({
          title: 'Error',
          color: 'red'
        })
      )
    })
  })

  describe('formatTimeRange', () => {
    it('should format time range correctly', () => {
      const analytics = useAnalytics()

      expect(analytics.formatTimeRange('7d')).toBe('Last 7 Days')
      expect(analytics.formatTimeRange('30d')).toBe('Last 30 Days')
      expect(analytics.formatTimeRange('90d')).toBe('Last 90 Days')
      expect(analytics.formatTimeRange('custom')).toBe('Custom Range')
    })

    it('should return original value for unknown range', () => {
      const analytics = useAnalytics()

      expect(analytics.formatTimeRange('unknown')).toBe('unknown')
    })
  })

  describe('calculateDateRange', () => {
    it('should calculate 7 day range', () => {
      const analytics = useAnalytics()
      const result = analytics.calculateDateRange('7d')

      expect(result).toHaveProperty('startDate')
      expect(result).toHaveProperty('endDate')
      
      const start = new Date(result.startDate)
      const end = new Date(result.endDate)
      const diffDays = Math.round((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
      
      expect(diffDays).toBe(7)
    })

    it('should calculate 30 day range', () => {
      const analytics = useAnalytics()
      const result = analytics.calculateDateRange('30d')

      const start = new Date(result.startDate)
      const end = new Date(result.endDate)
      const diffDays = Math.round((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
      
      expect(diffDays).toBe(30)
    })

    it('should calculate 90 day range', () => {
      const analytics = useAnalytics()
      const result = analytics.calculateDateRange('90d')

      const start = new Date(result.startDate)
      const end = new Date(result.endDate)
      const diffDays = Math.round((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
      
      expect(diffDays).toBe(90)
    })

    it('should default to 30 days for unknown range', () => {
      const analytics = useAnalytics()
      const result = analytics.calculateDateRange('unknown')

      const start = new Date(result.startDate)
      const end = new Date(result.endDate)
      const diffDays = Math.round((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
      
      expect(diffDays).toBe(30)
    })
  })
})
