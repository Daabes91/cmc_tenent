import { ref } from 'vue'
import type { AnalyticsData, AnalyticsParams } from '~/types'

export const useAnalytics = () => {
  const { getAnalytics, exportAnalytics } = useSaasApi()
  const toast = useToast()

  const loading = ref(false)
  const analyticsData = ref<AnalyticsData | null>(null)

  /**
   * Fetch analytics data based on time range parameters
   */
  const fetchAnalytics = async (params: AnalyticsParams, useCaching = true): Promise<AnalyticsData> => {
    loading.value = true
    try {
      const cache = useApiCache()
      
      // Use cached call with longer TTL for analytics
      const data = useCaching
        ? await cache.cachedCall(
            () => getAnalytics(params),
            cache.generateKey('/analytics', params),
            { ttl: cache.DEFAULT_TTL.analytics }
          )
        : await getAnalytics(params)
      
      analyticsData.value = data
      return data
    } catch (error: any) {
      console.error('Failed to fetch analytics:', error)
      toast.add({
        title: 'Error',
        description: error.message || 'Failed to fetch analytics data',
        color: 'red',
        timeout: 5000
      })
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * Export analytics data to PDF
   */
  const exportAnalyticsPDF = async (params: AnalyticsParams): Promise<void> => {
    try {
      toast.add({
        title: 'Exporting',
        description: 'Preparing your analytics report...',
        color: 'blue',
        timeout: 3000
      })

      const blob = await exportAnalytics(params)
      
      // Create download link
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      
      // Generate filename with timestamp
      const timestamp = new Date().toISOString().split('T')[0]
      link.download = `analytics-report-${timestamp}.pdf`
      
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      toast.add({
        title: 'Success',
        description: 'Analytics report exported successfully',
        color: 'green',
        timeout: 5000
      })
    } catch (error: any) {
      console.error('Failed to export analytics:', error)
      toast.add({
        title: 'Error',
        description: error.message || 'Failed to export analytics report',
        color: 'red',
        timeout: 5000
      })
      throw error
    }
  }

  /**
   * Format time range for display
   */
  const formatTimeRange = (timeRange: string): string => {
    const ranges: Record<string, string> = {
      '7d': 'Last 7 Days',
      '30d': 'Last 30 Days',
      '90d': 'Last 90 Days',
      'custom': 'Custom Range'
    }
    return ranges[timeRange] || timeRange
  }

  /**
   * Calculate date range from time range value
   */
  const calculateDateRange = (timeRange: string): { startDate: string; endDate: string } => {
    const endDate = new Date()
    const startDate = new Date()

    switch (timeRange) {
      case '7d':
        startDate.setDate(endDate.getDate() - 7)
        break
      case '30d':
        startDate.setDate(endDate.getDate() - 30)
        break
      case '90d':
        startDate.setDate(endDate.getDate() - 90)
        break
      default:
        startDate.setDate(endDate.getDate() - 30)
    }

    return {
      startDate: startDate.toISOString().split('T')[0],
      endDate: endDate.toISOString().split('T')[0]
    }
  }

  return {
    loading,
    analyticsData,
    fetchAnalytics,
    exportAnalyticsPDF,
    formatTimeRange,
    calculateDateRange
  }
}
