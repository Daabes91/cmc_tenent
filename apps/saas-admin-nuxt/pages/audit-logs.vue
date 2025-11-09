<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
          {{ $t('auditLogs.title') }}
        </h1>
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
          {{ $t('auditLogs.description') }}
        </p>
      </div>

      <!-- Export Button -->
      <UButton
        @click="exportLogs"
        :loading="exporting"
        :disabled="logs.length === 0"
        color="primary"
        icon="heroicons:arrow-down-tray"
      >
        {{ $t('auditLogs.exportCSV') }}
      </UButton>
    </div>

    <!-- Audit Log Table -->
    <AuditLogTable
      :logs="logs"
      :loading="loading"
      :page="currentPage"
      :total-pages="totalPages"
      :total-elements="totalElements"
      :page-size="pageSize"
      :filters="filters"
      @filter-change="handleFilterChange"
      @page-change="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts">
import type { AuditLog, AuditLogFilters } from '~/types'

definePageMeta({
  layout: 'default'
})

const { t } = useI18n()
const toast = useToast()

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

// Composables
const { getAuditLogs, exportAuditLogs } = useSaasApi()

// Fetch audit logs
const fetchLogs = async () => {
  loading.value = true
  try {
    const params: AuditLogFilters = {
      ...filters.value,
      page: currentPage.value - 1, // Backend uses 0-based pagination
      size: pageSize.value
    }

    const response = await getAuditLogs(params)
    logs.value = response.content
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
  } catch (error: any) {
    console.error('Failed to fetch audit logs:', error)
    toast.add({
      title: t('auditLogs.errors.fetchFailed'),
      description: error.message || t('common.errorOccurred'),
      color: 'red',
      icon: 'heroicons:x-circle'
    })
  } finally {
    loading.value = false
  }
}

// Handle filter changes
const handleFilterChange = (newFilters: AuditLogFilters) => {
  filters.value = { ...newFilters }
  currentPage.value = 1 // Reset to first page when filters change
  fetchLogs()
}

// Handle page changes
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchLogs()
}

// Export logs to CSV
const exportLogs = async () => {
  exporting.value = true
  try {
    const params: AuditLogFilters = {
      ...filters.value
    }

    const blob = await exportAuditLogs(params)
    
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
  } catch (error: any) {
    console.error('Failed to export audit logs:', error)
    toast.add({
      title: t('auditLogs.errors.exportFailed'),
      description: error.message || t('common.errorOccurred'),
      color: 'red',
      icon: 'heroicons:x-circle'
    })
  } finally {
    exporting.value = false
  }
}

// Initial fetch
onMounted(() => {
  fetchLogs()
})
</script>
