<template>
  <div class="space-y-4">
    <!-- Filters -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <!-- Date Range Filter -->
      <div>
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
          {{ $t('auditLogs.startDate') }}
        </label>
        <input
          type="date"
          v-model="localFilters.startDate"
          @change="emitFilterChange"
          class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:text-white"
        />
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
          {{ $t('auditLogs.endDate') }}
        </label>
        <input
          type="date"
          v-model="localFilters.endDate"
          @change="emitFilterChange"
          class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:text-white"
        />
      </div>

      <!-- Action Type Filter -->
      <div>
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
          {{ $t('auditLogs.actionType') }}
        </label>
        <select
          v-model="localFilters.actionType"
          @change="emitFilterChange"
          class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:text-white"
        >
          <option value="">{{ $t('auditLogs.allActions') }}</option>
          <option value="TENANT_CREATED">{{ $t('auditLogs.actions.created') }}</option>
          <option value="TENANT_UPDATED">{{ $t('auditLogs.actions.updated') }}</option>
          <option value="TENANT_DELETED">{{ $t('auditLogs.actions.deleted') }}</option>
          <option value="TENANT_STATUS_CHANGED">{{ $t('auditLogs.actions.statusChanged') }}</option>
        </select>
      </div>

      <!-- Clear Filters Button -->
      <div class="flex items-end">
        <UButton
          color="gray"
          variant="outline"
          @click="clearFilters"
          class="w-full"
        >
          {{ $t('auditLogs.clearFilters') }}
        </UButton>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
    </div>

    <!-- Desktop Table View -->
    <div v-else-if="logs.length > 0" class="hidden md:block overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('auditLogs.timestamp') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('auditLogs.manager') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('auditLogs.action') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('auditLogs.tenant') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('auditLogs.details') }}
            </th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
          <tr
            v-for="log in logs"
            :key="log.id"
            class="hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
          >
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
              {{ formatTimestamp(log.timestamp) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm font-medium text-gray-900 dark:text-gray-100">
                {{ log.managerName }}
              </div>
              <div class="text-sm text-gray-500 dark:text-gray-400">
                {{ log.managerEmail }}
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span :class="getActionBadgeClass(log.action)" class="px-2 py-1 text-xs font-semibold rounded-full">
                {{ formatAction(log.action) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900 dark:text-gray-100">
                {{ log.tenantSlug }}
              </div>
              <div class="text-sm text-gray-500 dark:text-gray-400">
                ID: {{ log.tenantId }}
              </div>
            </td>
            <td class="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
              <button
                @click="showDetails(log)"
                class="text-primary-600 hover:text-primary-800 dark:text-primary-400 dark:hover:text-primary-300"
              >
                {{ $t('auditLogs.viewDetails') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Mobile Card View -->
    <div v-else-if="logs.length > 0" class="md:hidden space-y-4">
      <div
        v-for="log in logs"
        :key="log.id"
        class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 space-y-3"
      >
        <div class="flex justify-between items-start">
          <div>
            <div class="text-sm font-medium text-gray-900 dark:text-gray-100">
              {{ log.managerName }}
            </div>
            <div class="text-xs text-gray-500 dark:text-gray-400">
              {{ log.managerEmail }}
            </div>
          </div>
          <span :class="getActionBadgeClass(log.action)" class="px-2 py-1 text-xs font-semibold rounded-full">
            {{ formatAction(log.action) }}
          </span>
        </div>

        <div class="text-sm text-gray-600 dark:text-gray-300">
          <span class="font-medium">{{ $t('auditLogs.tenant') }}:</span> {{ log.tenantSlug }}
        </div>

        <div class="text-xs text-gray-500 dark:text-gray-400">
          {{ formatTimestamp(log.timestamp) }}
        </div>

        <button
          @click="showDetails(log)"
          class="text-sm text-primary-600 hover:text-primary-800 dark:text-primary-400 dark:hover:text-primary-300"
        >
          {{ $t('auditLogs.viewDetails') }}
        </button>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="text-center py-12">
      <Icon name="heroicons:document-text" class="mx-auto h-12 w-12 text-gray-400" />
      <h3 class="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">
        {{ $t('auditLogs.noLogs') }}
      </h3>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
        {{ $t('auditLogs.noLogsDescription') }}
      </p>
    </div>

    <!-- Pagination -->
    <div v-if="logs.length > 0" class="flex justify-between items-center mt-6">
      <div class="text-sm text-gray-700 dark:text-gray-300">
        {{ $t('auditLogs.showing') }} {{ (page - 1) * pageSize + 1 }} - {{ Math.min(page * pageSize, totalElements) }} {{ $t('auditLogs.of') }} {{ totalElements }}
      </div>
      <div class="flex gap-2">
        <UButton
          :disabled="page === 1"
          @click="$emit('page-change', page - 1)"
          color="gray"
          variant="outline"
        >
          {{ $t('common.previous') }}
        </UButton>
        <UButton
          :disabled="page >= totalPages"
          @click="$emit('page-change', page + 1)"
          color="gray"
          variant="outline"
        >
          {{ $t('common.next') }}
        </UButton>
      </div>
    </div>

    <!-- Details Modal -->
    <UModal v-model="detailsModalOpen">
      <div class="p-6">
        <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-4">
          {{ $t('auditLogs.logDetails') }}
        </h3>
        <div v-if="selectedLog" class="space-y-3">
          <div>
            <span class="text-sm font-medium text-gray-700 dark:text-gray-300">{{ $t('auditLogs.timestamp') }}:</span>
            <span class="text-sm text-gray-900 dark:text-gray-100 ml-2">{{ formatTimestamp(selectedLog.timestamp) }}</span>
          </div>
          <div>
            <span class="text-sm font-medium text-gray-700 dark:text-gray-300">{{ $t('auditLogs.manager') }}:</span>
            <span class="text-sm text-gray-900 dark:text-gray-100 ml-2">{{ selectedLog.managerName }} ({{ selectedLog.managerEmail }})</span>
          </div>
          <div>
            <span class="text-sm font-medium text-gray-700 dark:text-gray-300">{{ $t('auditLogs.action') }}:</span>
            <span class="text-sm text-gray-900 dark:text-gray-100 ml-2">{{ formatAction(selectedLog.action) }}</span>
          </div>
          <div>
            <span class="text-sm font-medium text-gray-700 dark:text-gray-300">{{ $t('auditLogs.tenant') }}:</span>
            <span class="text-sm text-gray-900 dark:text-gray-100 ml-2">{{ selectedLog.tenantSlug }} (ID: {{ selectedLog.tenantId }})</span>
          </div>
          <div v-if="selectedLog.details && Object.keys(selectedLog.details).length > 0">
            <span class="text-sm font-medium text-gray-700 dark:text-gray-300">{{ $t('auditLogs.additionalDetails') }}:</span>
            <pre class="mt-2 text-xs bg-gray-100 dark:bg-gray-800 p-3 rounded overflow-x-auto">{{ JSON.stringify(selectedLog.details, null, 2) }}</pre>
          </div>
        </div>
        <div class="mt-6 flex justify-end">
          <UButton @click="detailsModalOpen = false" color="gray">
            {{ $t('common.close') }}
          </UButton>
        </div>
      </div>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import type { AuditLog, AuditLogFilters } from '~/types'

interface Props {
  logs: AuditLog[]
  loading: boolean
  page: number
  totalPages: number
  totalElements: number
  pageSize: number
  filters: AuditLogFilters
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'filter-change': [filters: AuditLogFilters]
  'page-change': [page: number]
}>()

const { t } = useI18n()

const localFilters = ref<AuditLogFilters>({ ...props.filters })
const detailsModalOpen = ref(false)
const selectedLog = ref<AuditLog | null>(null)

// Watch for external filter changes
watch(() => props.filters, (newFilters) => {
  localFilters.value = { ...newFilters }
}, { deep: true })

const emitFilterChange = () => {
  emit('filter-change', { ...localFilters.value })
}

const clearFilters = () => {
  localFilters.value = {
    startDate: undefined,
    endDate: undefined,
    actionType: undefined,
    managerId: undefined,
    tenantId: undefined
  }
  emitFilterChange()
}

const formatTimestamp = (timestamp: string) => {
  const date = new Date(timestamp)
  return date.toLocaleString()
}

const formatAction = (action: string) => {
  const actionMap: Record<string, string> = {
    'TENANT_CREATED': t('auditLogs.actions.created'),
    'TENANT_UPDATED': t('auditLogs.actions.updated'),
    'TENANT_DELETED': t('auditLogs.actions.deleted'),
    'TENANT_STATUS_CHANGED': t('auditLogs.actions.statusChanged')
  }
  return actionMap[action] || action
}

const getActionBadgeClass = (action: string) => {
  const classMap: Record<string, string> = {
    'TENANT_CREATED': 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    'TENANT_UPDATED': 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    'TENANT_DELETED': 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
    'TENANT_STATUS_CHANGED': 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
  }
  return classMap[action] || 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
}

const showDetails = (log: AuditLog) => {
  selectedLog.value = log
  detailsModalOpen.value = true
}
</script>
