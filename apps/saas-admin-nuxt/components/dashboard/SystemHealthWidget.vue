<template>
  <UCard>
    <template #header>
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
          {{ $t('dashboard.systemHealth') }}
        </h3>
        <UIcon 
          name="i-heroicons-heart" 
          class="w-5 h-5 text-gray-400"
        />
      </div>
    </template>

    <div v-if="loading" class="space-y-4">
      <div class="animate-pulse">
        <div class="h-4 bg-gray-200 rounded w-32 mb-2"></div>
        <div class="h-8 bg-gray-200 rounded w-full"></div>
      </div>
      <div class="animate-pulse">
        <div class="h-4 bg-gray-200 rounded w-32 mb-2"></div>
        <div class="h-8 bg-gray-200 rounded w-full"></div>
      </div>
    </div>

    <div v-else class="space-y-6">
      <!-- Database Status -->
      <div>
        <div class="flex items-center justify-between mb-2">
          <span class="text-sm font-medium text-gray-700 dark:text-gray-300">
            {{ $t('dashboard.databaseStatus') }}
          </span>
          <UBadge 
            :color="databaseStatusColor" 
            variant="subtle"
            size="sm"
          >
            {{ databaseStatusText }}
          </UBadge>
        </div>
        <div class="flex items-center gap-2">
          <div class="flex-1 h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
            <div 
              class="h-full transition-all duration-300"
              :class="databaseStatusBarClass"
              :style="{ width: databaseStatusPercentage }"
            ></div>
          </div>
        </div>
      </div>

      <!-- API Response Time -->
      <div>
        <div class="flex items-center justify-between mb-2">
          <span class="text-sm font-medium text-gray-700 dark:text-gray-300">
            {{ $t('dashboard.apiResponseTime') }}
          </span>
          <span class="text-sm font-semibold" :class="apiResponseTimeColorClass">
            {{ apiResponseTime }}ms
          </span>
        </div>
        <div class="flex items-center gap-2">
          <div class="flex-1 h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
            <div 
              class="h-full transition-all duration-300"
              :class="apiResponseTimeBarClass"
              :style="{ width: apiResponseTimePercentage }"
            ></div>
          </div>
        </div>
        <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">
          {{ apiResponseTimeDescription }}
        </p>
      </div>

      <!-- Quick Actions -->
      <div class="pt-4 border-t border-gray-200 dark:border-gray-700">
        <p class="text-xs font-medium text-gray-700 dark:text-gray-300 mb-3">
          {{ $t('dashboard.quickActions') }}
        </p>
        <div class="flex flex-wrap gap-2">
          <UButton
            to="/tenants/new"
            color="primary"
            variant="soft"
            size="xs"
            icon="i-heroicons-plus"
          >
            {{ $t('dashboard.createTenant') }}
          </UButton>
          <UButton
            to="/tenants"
            color="gray"
            variant="soft"
            size="xs"
            icon="i-heroicons-building-office"
          >
            {{ $t('dashboard.viewTenants') }}
          </UButton>
          <UButton
            to="/audit-logs"
            color="gray"
            variant="soft"
            size="xs"
            icon="i-heroicons-document-text"
          >
            {{ $t('dashboard.viewAuditLogs') }}
          </UButton>
        </div>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
interface SystemHealthWidgetProps {
  apiResponseTime: number
  databaseStatus: 'healthy' | 'degraded' | 'down'
  loading?: boolean
}

const props = withDefaults(defineProps<SystemHealthWidgetProps>(), {
  loading: false
})

// Database Status
const databaseStatusColor = computed(() => {
  switch (props.databaseStatus) {
    case 'healthy':
      return 'green'
    case 'degraded':
      return 'yellow'
    case 'down':
      return 'red'
    default:
      return 'gray'
  }
})

const { t } = useI18n()

const databaseStatusText = computed(() => {
  switch (props.databaseStatus) {
    case 'healthy':
      return t('dashboard.status.healthy')
    case 'degraded':
      return t('dashboard.status.degraded')
    case 'down':
      return t('dashboard.status.down')
    default:
      return t('dashboard.status.unknown')
  }
})

const databaseStatusBarClass = computed(() => {
  switch (props.databaseStatus) {
    case 'healthy':
      return 'bg-green-500'
    case 'degraded':
      return 'bg-yellow-500'
    case 'down':
      return 'bg-red-500'
    default:
      return 'bg-gray-500'
  }
})

const databaseStatusPercentage = computed(() => {
  switch (props.databaseStatus) {
    case 'healthy':
      return '100%'
    case 'degraded':
      return '60%'
    case 'down':
      return '20%'
    default:
      return '0%'
  }
})

// API Response Time
const apiResponseTimeColorClass = computed(() => {
  if (props.apiResponseTime < 100) return 'text-green-600 dark:text-green-400'
  if (props.apiResponseTime < 300) return 'text-yellow-600 dark:text-yellow-400'
  return 'text-red-600 dark:text-red-400'
})

const apiResponseTimeBarClass = computed(() => {
  if (props.apiResponseTime < 100) return 'bg-green-500'
  if (props.apiResponseTime < 300) return 'bg-yellow-500'
  return 'bg-red-500'
})

const apiResponseTimePercentage = computed(() => {
  // Scale: 0-500ms mapped to 0-100%
  const percentage = Math.min((props.apiResponseTime / 500) * 100, 100)
  return `${percentage}%`
})

const apiResponseTimeDescription = computed(() => {
  if (props.apiResponseTime < 100) return t('dashboard.performance.excellent')
  if (props.apiResponseTime < 300) return t('dashboard.performance.good')
  return t('dashboard.performance.needsAttention')
})
</script>
