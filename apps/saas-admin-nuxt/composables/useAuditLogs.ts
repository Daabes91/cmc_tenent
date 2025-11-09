// Composable for managing audit logs
import type { AuditLog, AuditLogFilters } from '~/types'

export const useAuditLogs = () => {
  const { getAuditLogs: fetchAuditLogs, exportAuditLogs: exportAuditLogsApi } = useSaasApi()
  const toast = useToast()
  const { t } = useI18n()

  // State
  const logs = ref<AuditLog[]>([])
  const loading = ref(false)
  const exporting = ref(false)
  const currentPage = ref(1)
  const totalPages = ref(0)
  const totalElements = ref(0)
  const pageSize = ref(20)
  const filters = ref<AuditLogFilters>({
    startDate: undefined,
    endDate: undefined,
    actionType: undefined,
    managerId: undefined,
    tenantId: undefined
  })

  /**
   * Fetch audit logs with current filters and pagination
   */
  const fetchLogs = async (useCaching = true) => {
    loading.value = true
    try {
      const cache = useApiCache()
      
      const params: AuditLogFilters = {
        ...filters.value,
        page: currentPage.value - 1, // Backend uses 0-based pagination
        size: pageSize.value
      }

      // Use cached call with appropriate TTL
      const response = useCaching
        ? await cache.cachedCall(
            () => fetchAuditLogs(params),
            cache.generateKey('/audit-logs', params),
            { ttl: cache.DEFAULT_TTL.auditLogs }
          )
        : await fetchAuditLogs(params)
      
      logs.value = response.content
      totalPages.value = response.totalPages
      totalElements.value = response.totalElements

      return response
    } catch (error: any) {
      console.error('Failed to fetch audit logs:', error)
      toast.add({
        title: t('auditLogs.errors.fetchFailed'),
        description: error.message || t('common.errorOccurred'),
        color: 'red',
        icon: 'heroicons:x-circle'
      })
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * Update filters and fetch logs (with debouncing)
   */
  const debounceUtil = useDebounce()
  const debouncedFetch = debounceUtil.debounce(() => {
    fetchLogs(false) // Don't use cache for filtered results
  }, 300)

  const updateFilters = (newFilters: Partial<AuditLogFilters>) => {
    filters.value = { ...filters.value, ...newFilters }
    currentPage.value = 1 // Reset to first page when filters change
    debouncedFetch()
  }

  /**
   * Clear all filters and fetch logs
   */
  const clearFilters = () => {
    filters.value = {
      startDate: undefined,
      endDate: undefined,
      actionType: undefined,
      managerId: undefined,
      tenantId: undefined
    }
    currentPage.value = 1
    return fetchLogs()
  }

  /**
   * Change page and fetch logs
   */
  const changePage = (page: number) => {
    currentPage.value = page
    return fetchLogs()
  }

  /**
   * Change page size and fetch logs
   */
  const changePageSize = (size: number) => {
    pageSize.value = size
    currentPage.value = 1 // Reset to first page when page size changes
    return fetchLogs()
  }

  /**
   * Export audit logs to CSV
   */
  const exportLogs = async () => {
    exporting.value = true
    try {
      const params: AuditLogFilters = {
        ...filters.value
      }

      const blob = await exportAuditLogsApi(params)
      
      // Create download link
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `audit-logs-${new Date().toISOString().split('T')[0]}.csv`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      toast.add({
        title: t('auditLogs.exportSuccess'),
        description: t('auditLogs.exportSuccessDescription'),
        color: 'green',
        icon: 'heroicons:check-circle'
      })

      return true
    } catch (error: any) {
      console.error('Failed to export audit logs:', error)
      toast.add({
        title: t('auditLogs.errors.exportFailed'),
        description: error.message || t('common.errorOccurred'),
        color: 'red',
        icon: 'heroicons:x-circle'
      })
      throw error
    } finally {
      exporting.value = false
    }
  }

  /**
   * Filter logs by date range
   */
  const filterByDateRange = (startDate: string, endDate: string) => {
    return updateFilters({ startDate, endDate })
  }

  /**
   * Filter logs by action type
   */
  const filterByActionType = (actionType: string) => {
    return updateFilters({ actionType })
  }

  /**
   * Filter logs by tenant
   */
  const filterByTenant = (tenantId: number) => {
    return updateFilters({ tenantId })
  }

  /**
   * Filter logs by manager
   */
  const filterByManager = (managerId: number) => {
    return updateFilters({ managerId })
  }

  return {
    // State
    logs: readonly(logs),
    loading: readonly(loading),
    exporting: readonly(exporting),
    currentPage: readonly(currentPage),
    totalPages: readonly(totalPages),
    totalElements: readonly(totalElements),
    pageSize: readonly(pageSize),
    filters: readonly(filters),

    // Methods
    fetchLogs,
    updateFilters,
    clearFilters,
    changePage,
    changePageSize,
    exportLogs,
    filterByDateRange,
    filterByActionType,
    filterByTenant,
    filterByManager
  }
}
