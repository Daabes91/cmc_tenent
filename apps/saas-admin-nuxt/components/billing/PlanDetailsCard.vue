<template>
  <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
    <div class="border-b border-slate-200 dark:border-slate-700 px-6 py-4">
      <div class="flex items-center justify-between">
        <h2 class="text-lg font-semibold">{{ $t('billing.currentPlan') }}</h2>
        <UButton
          color="blue"
          variant="ghost"
          icon="i-heroicons-pencil"
          size="sm"
          @click="$emit('override')"
        >
          {{ $t('billing.overridePlan') }}
        </UButton>
      </div>
    </div>

<div class="p-6">
  <div class="flex items-start gap-6">
        <!-- Plan Icon -->
        <div :class="[
          'w-20 h-20 rounded-xl flex items-center justify-center flex-shrink-0',
          getPlanBgClass(plan.planTier)
        ]">
          <UIcon :name="getPlanIcon(plan.planTier)" :class="[
            'w-10 h-10',
            getPlanIconClass(plan.planTier)
          ]" />
        </div>

        <!-- Plan Details -->
        <div class="flex-1 space-y-4">
          <!-- Plan Name and Status -->
          <div>
            <div class="flex items-center gap-3 mb-2">
              <h3 class="text-2xl font-bold">{{ plan.planTierName || plan.planTier }}</h3>
              <UBadge :color="getStatusColor(plan.status)" size="lg">
                {{ formatStatus(plan.status) }}
              </UBadge>
            </div>
            <p class="text-slate-600 dark:text-slate-400">
              {{ getPlanDescription(plan.planTier) }}
            </p>
          </div>

          <!-- Pricing -->
          <div class="flex items-baseline gap-2">
            <span class="text-3xl font-bold">{{ formatCurrency(plan.price, plan.currency) }}</span>
            <span class="text-slate-600 dark:text-slate-400">/ {{ plan.billingCycle || 'month' }}</span>
          </div>

          <!-- Plan Info Grid -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-slate-200 dark:border-slate-700">
            <!-- Renewal Date -->
            <div v-if="plan.renewalDate">
              <div class="flex items-center gap-2 mb-1">
                <UIcon name="i-heroicons-calendar-days" class="w-4 h-4 text-slate-500" />
                <p class="text-xs text-slate-500 uppercase font-medium">{{ $t('billing.nextRenewal') }}</p>
              </div>
              <p class="font-semibold">{{ formatDate(plan.renewalDate) }}</p>
            </div>

            <!-- Payment Method -->
            <div v-if="plan.paymentMethodMask">
              <div class="flex items-center gap-2 mb-1">
                <UIcon name="i-heroicons-credit-card" class="w-4 h-4 text-slate-500" />
                <p class="text-xs text-slate-500 uppercase font-medium">{{ $t('billing.paymentMethod') }}</p>
              </div>
              <div class="flex items-center gap-2">
                <p class="font-semibold">{{ plan.paymentMethodMask }}</p>
                <UBadge color="gray" size="xs">{{ plan.paymentMethodType }}</UBadge>
              </div>
            </div>

            <!-- Tenant Info -->
            <div v-if="tenant">
              <div class="flex items-center gap-2 mb-1">
                <UIcon name="i-heroicons-building-office" class="w-4 h-4 text-slate-500" />
                <p class="text-xs text-slate-500 uppercase font-medium">{{ $t('billing.tenant') }}</p>
              </div>
              <p class="font-semibold">{{ tenant.name }}</p>
            </div>

            <!-- Billing Status -->
            <div v-if="tenant">
              <div class="flex items-center gap-2 mb-1">
                <UIcon name="i-heroicons-banknotes" class="w-4 h-4 text-slate-500" />
                <p class="text-xs text-slate-500 uppercase font-medium">{{ $t('billing.billingStatus') }}</p>
              </div>
              <UBadge :color="getBillingStatusColor(tenant.billingStatus)">
                {{ formatBillingStatus(tenant.billingStatus) }}
              </UBadge>
            </div>
          </div>

          <!-- Usage -->
          <div v-if="usageItems.length" class="pt-4 border-t border-slate-200 dark:border-slate-700">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300 mb-3">
              {{ $t('billing.usage') }}
            </p>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
              <div
                v-for="item in usageItems"
                :key="item.label"
                class="flex items-center justify-between rounded-lg border px-3 py-2"
                :class="item.color === 'red' ? 'border-red-200 bg-red-50 dark:border-red-800 dark:bg-red-900/20' : item.color === 'amber' ? 'border-amber-200 bg-amber-50 dark:border-amber-800 dark:bg-amber-900/20' : 'border-slate-200 bg-slate-50 dark:border-slate-700 dark:bg-slate-800/50'"
              >
                <div class="flex items-center gap-2">
                  <UIcon :name="item.icon" class="w-4 h-4" />
                  <span class="text-sm font-medium">{{ item.label }}</span>
                </div>
                <span class="text-sm font-semibold">{{ item.value }}</span>
              </div>
            </div>
          </div>

          <!-- Pending Changes Notice -->
          <div v-if="plan.pendingPlanTier || plan.cancellationDate" class="mt-4 p-4 rounded-lg bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800">
            <div class="flex items-start gap-3">
              <UIcon name="i-heroicons-information-circle" class="w-5 h-5 text-yellow-600 dark:text-yellow-400 flex-shrink-0 mt-0.5" />
              <div class="flex-1">
                <p class="font-medium text-yellow-900 dark:text-yellow-100 mb-1">
                  {{ $t('billing.pendingChanges') }}
                </p>
                <div class="text-sm text-yellow-800 dark:text-yellow-200 space-y-1">
                  <p v-if="plan.pendingPlanTier">
                    {{ $t('billing.planWillChange', { 
                      tier: plan.pendingPlanTier, 
                      date: formatDate(plan.pendingPlanEffectiveDate) 
                    }) }}
                  </p>
                  <p v-if="plan.cancellationDate">
                    {{ $t('billing.subscriptionWillCancel', { 
                      date: formatDate(plan.cancellationEffectiveDate) 
                    }) }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Features List -->
          <div v-if="plan.features && plan.features.length > 0" class="pt-4 border-t border-slate-200 dark:border-slate-700">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300 mb-3">
              {{ $t('billing.includedFeatures') }}
            </p>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
              <div
                v-for="(feature, index) in plan.features"
                :key="index"
                class="flex items-center gap-2 text-sm"
              >
                <UIcon name="i-heroicons-check-circle" class="w-4 h-4 text-green-600 dark:text-green-400 flex-shrink-0" />
                <span>{{ feature }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n()
const props = defineProps<Props>()
defineEmits(['override'])

const usageItems = computed(() => {
  const plan = props.plan || {}
  const items: { label: string; value: string; color: 'red' | 'amber' | 'blue'; icon: string }[] = []

  const addItem = (used?: number, max?: number, label?: string) => {
    if (max == null || max < 0) return
    const safeUsed = used ?? 0
    const ratio = max === 0 ? 1 : safeUsed / max
    const color = safeUsed >= max ? 'red' : ratio >= 0.8 ? 'amber' : 'blue'
    items.push({
      label: label || 'Usage',
      value: `${safeUsed}/${max}`,
      color,
      icon: color === 'red' ? 'i-heroicons-exclamation-triangle' : 'i-heroicons-chart-bar'
    })
  }

  addItem(plan.staffUsed, plan.maxStaff, t('billing.staff') || 'Staff')
  addItem(plan.doctorsUsed, plan.maxDoctors, t('billing.doctors') || 'Doctors')

  return items
})

const getPlanBgClass = (tier: string) => {
  switch (tier?.toUpperCase()) {
    case 'BASIC':
      return 'bg-blue-100 dark:bg-blue-900'
    case 'PROFESSIONAL':
      return 'bg-purple-100 dark:bg-purple-900'
    case 'ENTERPRISE':
      return 'bg-emerald-100 dark:bg-emerald-900'
    case 'CUSTOM':
      return 'bg-amber-100 dark:bg-amber-900'
    default:
      return 'bg-gray-100 dark:bg-gray-900'
  }
}

const getPlanIcon = (tier: string) => {
  switch (tier?.toUpperCase()) {
    case 'BASIC':
      return 'i-heroicons-star'
    case 'PROFESSIONAL':
      return 'i-heroicons-rocket-launch'
    case 'ENTERPRISE':
      return 'i-heroicons-building-office-2'
    case 'CUSTOM':
      return 'i-heroicons-sparkles'
    default:
      return 'i-heroicons-cube'
  }
}

const getPlanIconClass = (tier: string) => {
  switch (tier?.toUpperCase()) {
    case 'BASIC':
      return 'text-blue-600 dark:text-blue-400'
    case 'PROFESSIONAL':
      return 'text-purple-600 dark:text-purple-400'
    case 'ENTERPRISE':
      return 'text-emerald-600 dark:text-emerald-400'
    case 'CUSTOM':
      return 'text-amber-600 dark:text-amber-400'
    default:
      return 'text-gray-600 dark:text-gray-400'
  }
}

const getPlanDescription = (tier: string) => {
  switch (tier?.toUpperCase()) {
    case 'BASIC':
      return t('billing.basicDescription')
    case 'PROFESSIONAL':
      return t('billing.professionalDescription')
    case 'ENTERPRISE':
      return t('billing.enterpriseDescription')
    case 'CUSTOM':
      return t('billing.customDescription')
    default:
      return ''
  }
}

const getStatusColor = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
      return 'green'
    case 'PAST_DUE':
      return 'orange'
    case 'CANCELED':
      return 'red'
    case 'PENDING':
      return 'yellow'
    default:
      return 'gray'
  }
}

const getBillingStatusColor = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
      return 'green'
    case 'PENDING_PAYMENT':
      return 'yellow'
    case 'PAST_DUE':
      return 'orange'
    case 'SUSPENDED':
      return 'blue'
    case 'CANCELED':
      return 'red'
    default:
      return 'gray'
  }
}

const formatStatus = (status: string) => {
  if (!status) return ''
  return status.split('_').map(word => 
    word.charAt(0) + word.slice(1).toLowerCase()
  ).join(' ')
}

const formatBillingStatus = (status: string) => {
  if (!status) return ''
  return status.split('_').map(word => 
    word.charAt(0) + word.slice(1).toLowerCase()
  ).join(' ')
}

const formatCurrency = (amount: number, currency: string = 'USD') => {
  if (!amount) return '$0.00'
  
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency
  }).format(amount)
}

const formatDate = (timestamp: number | string | null | undefined) => {
  if (!timestamp) return t('common.notAvailable')

  const date = typeof timestamp === 'number'
    ? new Date(timestamp * 1000)
    : new Date(timestamp)

  if (Number.isNaN(date.getTime())) {
    return t('common.notAvailable')
  }

  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  }).format(date)
}
</script>
