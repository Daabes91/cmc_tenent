<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ $t('analytics.title') }}</h1>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
          {{ $t('analytics.description') }}
        </p>
      </div>

      <!-- Time Range Selector -->
      <USelectMenu
        v-model="selectedTimeRange"
        :options="timeRangeOptions"
        value-attribute="value"
        option-attribute="label"
        class="w-full sm:w-48"
      >
        <template #label>
          <UIcon name="i-heroicons-calendar" class="w-4 h-4 mr-2" />
          {{ selectedTimeRange.label }}
        </template>
      </USelectMenu>
    </div>

    <!-- Custom Date Range (if selected) -->
    <UCard v-if="selectedTimeRange.value === 'custom'" class="mb-6">
      <div class="flex flex-col sm:flex-row gap-4">
        <div class="flex-1">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            {{ $t('analytics.startDate') }}
          </label>
          <input
            v-model="customStartDate"
            type="date"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md dark:bg-gray-800 dark:text-white"
            @change="fetchAnalyticsData"
          />
        </div>
        <div class="flex-1">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            {{ $t('analytics.endDate') }}
          </label>
          <input
            v-model="customEndDate"
            type="date"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md dark:bg-gray-800 dark:text-white"
            @change="fetchAnalyticsData"
          />
        </div>
      </div>
    </UCard>

    <!-- Loading State -->
    <div v-if="loading" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <USkeleton class="h-96" />
      <USkeleton class="h-96" />
    </div>

    <!-- Charts -->
    <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Tenant Growth Chart -->
      <UCard>
        <template #header>
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
              {{ $t('analytics.tenantGrowth') }}
            </h3>
            <UIcon name="i-heroicons-chart-bar" class="w-5 h-5 text-gray-400" />
          </div>
        </template>
        <TenantGrowthChart :data="tenantGrowthData" />
      </UCard>

      <!-- Usage Metrics Chart -->
      <UCard>
        <template #header>
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
              {{ $t('analytics.usageMetrics') }}
            </h3>
            <UIcon name="i-heroicons-chart-pie" class="w-5 h-5 text-gray-400" />
          </div>
        </template>
        <UsageMetricsChart :data="usageMetricsData" />
      </UCard>
    </div>

    <!-- Summary Statistics -->
    <div v-if="!loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <UCard>
        <div class="text-center">
          <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('analytics.totalTenants') }}</p>
          <p class="text-3xl font-bold text-gray-900 dark:text-white mt-2">
            {{ analyticsData?.summary?.totalTenants || 0 }}
          </p>
        </div>
      </UCard>
      <UCard>
        <div class="text-center">
          <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('analytics.totalUsers') }}</p>
          <p class="text-3xl font-bold text-gray-900 dark:text-white mt-2">
            {{ analyticsData?.summary?.totalUsers || 0 }}
          </p>
        </div>
      </UCard>
      <UCard>
        <div class="text-center">
          <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('analytics.totalAppointments') }}</p>
          <p class="text-3xl font-bold text-gray-900 dark:text-white mt-2">
            {{ analyticsData?.summary?.totalAppointments || 0 }}
          </p>
        </div>
      </UCard>
      <UCard>
        <div class="text-center">
          <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('analytics.storageUsed') }}</p>
          <p class="text-3xl font-bold text-gray-900 dark:text-white mt-2">
            {{ formatStorage(analyticsData?.summary?.storageUsedMB || 0) }}
          </p>
        </div>
      </UCard>
    </div>

    <!-- Export Button -->
    <div class="flex justify-end">
      <UButton
        color="primary"
        icon="i-heroicons-arrow-down-tray"
        :loading="exporting"
        @click="exportToPDF"
      >
        {{ exporting ? $t('analytics.exporting') : $t('analytics.exportPDF') }}
      </UButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import type { TimeRange, AnalyticsData } from '~/types'

definePageMeta({
  layout: 'default'
})

const { t } = useI18n()
const { fetchAnalytics, exportAnalyticsPDF } = useAnalytics()

// Time range options
const timeRangeOptions = computed(() => [
  { label: t('analytics.last7Days'), value: '7d' },
  { label: t('analytics.last30Days'), value: '30d' },
  { label: t('analytics.last90Days'), value: '90d' },
  { label: t('analytics.customRange'), value: 'custom' }
])

const selectedTimeRange = ref(timeRangeOptions.value[1]) // Default to 30 days
const customStartDate = ref('')
const customEndDate = ref('')
const loading = ref(false)
const exporting = ref(false)
const analyticsData = ref<AnalyticsData | null>(null)

// Computed data for charts
const tenantGrowthData = computed(() => {
  if (!analyticsData.value?.tenantGrowth) return null
  return analyticsData.value.tenantGrowth
})

const usageMetricsData = computed(() => {
  if (!analyticsData.value?.usageMetrics) return null
  return analyticsData.value.usageMetrics
})

// Fetch analytics data
const fetchAnalyticsData = async () => {
  loading.value = true
  try {
    const params: any = {}
    
    if (selectedTimeRange.value.value === 'custom') {
      if (customStartDate.value && customEndDate.value) {
        params.startDate = customStartDate.value
        params.endDate = customEndDate.value
      }
    } else {
      params.timeRange = selectedTimeRange.value.value
    }

    analyticsData.value = await fetchAnalytics(params)
  } catch (error) {
    console.error('Failed to fetch analytics:', error)
    // Error handling will be done by the composable
  } finally {
    loading.value = false
  }
}

// Export to PDF
const exportToPDF = async () => {
  exporting.value = true
  try {
    const params: any = {}
    
    if (selectedTimeRange.value.value === 'custom') {
      if (customStartDate.value && customEndDate.value) {
        params.startDate = customStartDate.value
        params.endDate = customEndDate.value
      }
    } else {
      params.timeRange = selectedTimeRange.value.value
    }

    await exportAnalyticsPDF(params)
  } catch (error) {
    console.error('Failed to export analytics:', error)
  } finally {
    exporting.value = false
  }
}

// Format storage size
const formatStorage = (mb: number): string => {
  if (mb < 1024) {
    return `${mb.toFixed(1)} MB`
  }
  return `${(mb / 1024).toFixed(1)} GB`
}

// Watch for time range changes
watch(selectedTimeRange, () => {
  if (selectedTimeRange.value.value !== 'custom') {
    fetchAnalyticsData()
  }
})

// Initial load
onMounted(() => {
  fetchAnalyticsData()
})
</script>
