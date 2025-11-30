<template>
  <UModal v-model="isOpen" :ui="{ width: 'sm:max-w-4xl' }">
    <UCard>
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-slate-100 dark:bg-slate-900 flex items-center justify-center">
              <UIcon name="i-heroicons-clock" class="w-6 h-6 text-slate-600 dark:text-slate-400" />
            </div>
            <h3 class="text-lg font-semibold">{{ $t('billing.auditLog') }}</h3>
          </div>
          <UButton
            color="gray"
            variant="ghost"
            icon="i-heroicons-x-mark"
            size="sm"
            @click="isOpen = false"
          />
        </div>
      </template>

      <div class="space-y-4">
        <!-- Filters -->
        <div class="flex gap-3">
          <USelect
            v-model="filters.actionType"
            :options="actionTypeOptions"
            :placeholder="$t('billing.allActions')"
            size="sm"
            class="flex-1"
            @change="loadAuditLogs"
          />
          <UButton
            color="gray"
            variant="outline"
            icon="i-heroicons-arrow-path"
            size="sm"
            :loading="loading"
            @click="loadAuditLogs"
          >
            {{ $t('common.refresh') }}
          </UButton>
        </div>

        <!-- Loading -->
        <div v-if="loading && !auditLogs.length" class="space-y-3">
          <USkeleton class="h-20" v-for="i in 3" :key="i" />
        </div>

        <!-- Error -->
        <UAlert
          v-else-if="error"
          color="red"
          variant="subtle"
          icon="i-heroicons-exclamation-triangle"
          :title="$t('common.error')"
          :description="error"
        >
          <template #actions>
            <UButton color="red" variant="ghost" size="xs" @click="loadAuditLogs">
              {{ $t('common.retry') }}
            </UButton>
          </template>
        </UAlert>

        <!-- Audit Logs List -->
        <div v-else-if="auditLogs.length > 0" class="space-y-3 max-h-96 overflow-y-auto">
          <div
            v-for="log in auditLogs"
            :key="log.id"
            class="p-4 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors"
          >
            <div class="flex items-start justify-between gap-4">
              <div class="flex-1">
                <div class="flex items-center gap-2 mb-2">
                  <UBadge :color="getActionColor(log.action)" size="sm">
                    {{ formatAction(log.action) }}
                  </UBadge>
                  <span class="text-xs text-slate-500">
                    {{ formatTimestamp(log.timestamp) }}
                  </span>
                </div>
                
                <p class="text-sm font-medium mb-1">{{ log.description }}</p>
                
                <div class="flex items-center gap-2 text-xs text-slate-600 dark:text-slate-400">
                  <UIcon name="i-heroicons-user" class="w-3 h-3" />
                  <span>{{ log.managerName || log.managerEmail }}</span>
                </div>

                <!-- Details -->
                <div v-if="log.details && Object.keys(log.details).length > 0" class="mt-3 p-3 rounded bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700">
                  <p class="text-xs font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {{ $t('billing.details') }}
                  </p>
                  <div class="space-y-1">
                    <div
                      v-for="(value, key) in log.details"
                      :key="key"
                      class="flex items-start gap-2 text-xs"
                    >
                      <span class="text-slate-500 font-medium">{{ formatDetailKey(key) }}:</span>
                      <span class="text-slate-700 dark:text-slate-300">{{ formatDetailValue(value) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else class="text-center py-12">
          <UIcon name="i-heroicons-document-text" class="w-12 h-12 text-slate-400 mx-auto mb-3" />
          <p class="text-slate-600 dark:text-slate-400">{{ $t('billing.noAuditLogs') }}</p>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="flex justify-center pt-4 border-t border-slate-200 dark:border-slate-700">
          <UPagination
            v-model="currentPage"
            :total="totalElements"
            :page-count="pageSize"
            @update:model-value="loadAuditLogs"
          />
        </div>
      </div>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
const { t } = useI18n()
const api = useSaasApi()

interface Props {
  modelValue: boolean
  tenantId: number
}

const props = defineProps<Props>()
const emit = defineEmits(['update:modelValue'])

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const auditLogs = ref<any[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const currentPage = ref(1)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = 10

const filters = reactive({
  actionType: ''
})

const actionTypeOptions = [
  { label: t('billing.allActions'), value: '' },
  { label: t('billing.planUpgrade'), value: 'PLAN_UPGRADE' },
  { label: t('billing.planDowngrade'), value: 'PLAN_DOWNGRADE' },
  { label: t('billing.planCancellation'), value: 'PLAN_CANCELLATION' },
  { label: t('billing.paymentMethodUpdate'), value: 'PAYMENT_METHOD_UPDATE' },
  { label: t('billing.manualOverride'), value: 'MANUAL_OVERRIDE' }
]

const loadAuditLogs = async () => {
  loading.value = true
  error.value = null

  try {
    const response = await api.getBillingAuditLogs({
      tenantId: props.tenantId,
      actionType: filters.actionType || undefined,
      page: currentPage.value - 1,
      size: pageSize
    })

    auditLogs.value = response.content
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
  } catch (err: any) {
    error.value = err.message || t('billing.auditLogLoadError')
    console.error('Failed to load audit logs:', err)
  } finally {
    loading.value = false
  }
}

const getActionColor = (action: string) => {
  switch (action) {
    case 'PLAN_UPGRADE':
      return 'green'
    case 'PLAN_DOWNGRADE':
      return 'yellow'
    case 'PLAN_CANCELLATION':
      return 'red'
    case 'PAYMENT_METHOD_UPDATE':
      return 'blue'
    case 'MANUAL_OVERRIDE':
      return 'purple'
    default:
      return 'gray'
  }
}

const formatAction = (action: string) => {
  if (!action) return ''
  return action.split('_').map(word => 
    word.charAt(0) + word.slice(1).toLowerCase()
  ).join(' ')
}

const formatTimestamp = (timestamp: string | number) => {
  const date = typeof timestamp === 'number'
    ? new Date(timestamp * 1000)
    : new Date(timestamp)

  if (Number.isNaN(date.getTime())) {
    return t('common.notAvailable')
  }

  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const formatDetailKey = (key: string) => {
  return key.split(/(?=[A-Z])/).join(' ').toLowerCase()
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}

const formatDetailValue = (value: any) => {
  if (value === null || value === undefined) {
    return t('common.notAvailable')
  }
  if (typeof value === 'object') {
    return JSON.stringify(value, null, 2)
  }
  return String(value)
}

// Load audit logs when modal opens
watch(isOpen, (newValue) => {
  if (newValue) {
    currentPage.value = 1
    filters.actionType = ''
    loadAuditLogs()
  }
})
</script>
