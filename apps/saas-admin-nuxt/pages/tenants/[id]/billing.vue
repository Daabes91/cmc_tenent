<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <div class="flex items-center gap-2 text-sm text-slate-500 dark:text-slate-400 mb-4">
        <NuxtLink to="/tenants" class="hover:text-primary-600 transition-colors">
          {{ $t('tenants.title') }}
        </NuxtLink>
        <UIcon name="i-heroicons-chevron-right" class="w-4 h-4" />
        <NuxtLink :to="`/tenants/${tenantId}`" class="hover:text-primary-600 transition-colors">
          {{ tenant?.name || 'Loading...' }}
        </NuxtLink>
        <UIcon name="i-heroicons-chevron-right" class="w-4 h-4" />
        <span>{{ $t('billing.title') }}</span>
      </div>

      <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
        <div>
          <h1 class="text-3xl font-bold text-slate-900 dark:text-white mb-2">
            {{ $t('billing.planManagement') }}
          </h1>
          <p class="text-slate-600 dark:text-slate-400">
            {{ $t('billing.manageSubscription') }}
          </p>
        </div>

        <div class="flex gap-2">
          <UButton
            color="gray"
            variant="outline"
            icon="i-heroicons-arrow-path"
            :loading="loading"
            @click="loadPlanDetails"
          >
            {{ $t('common.refresh') }}
          </UButton>
          <UButton
            color="blue"
            icon="i-heroicons-clock"
            @click="showAuditLogModal = true"
          >
            {{ $t('billing.viewAuditLog') }}
          </UButton>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-6">
      <USkeleton class="h-64" />
      <USkeleton class="h-48" />
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
        <UButton color="red" variant="ghost" @click="loadPlanDetails">
          {{ $t('common.retry') }}
        </UButton>
      </template>
    </UAlert>

    <!-- Content -->
    <div v-else-if="planDetails" class="space-y-6">
      <!-- Plan Details Card -->
      <PlanDetailsCard
        :plan="planDetails"
        :tenant="tenant"
        @override="showOverrideModal = true"
      />

      <!-- Subscription Actions -->
      <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700">
        <div class="border-b border-slate-200 dark:border-slate-700 px-6 py-4">
          <div class="flex items-center justify-between gap-3">
            <div>
              <h2 class="text-lg font-semibold">{{ $t('billing.subscriptionActions') }}</h2>
              <p class="text-sm text-slate-600 dark:text-slate-400">
                {{ $t('billing.cancelSubscriptionDescription') }}
              </p>
            </div>
            <UBadge :color="planStatusColor">
              {{ formatStatus(planStatus) }}
            </UBadge>
          </div>
        </div>

        <div class="p-6 space-y-4">
          <div class="flex flex-wrap items-center gap-3">
            <UButton
              color="red"
              icon="i-heroicons-no-symbol"
              :disabled="!canCancel"
              :loading="cancelLoading"
              @click="showCancelModal = true"
            >
              {{ $t('billing.cancelSubscription') }}
            </UButton>
            <UButton
              v-if="canResume"
              color="emerald"
              variant="outline"
              icon="i-heroicons-arrow-path"
              :loading="resumeLoading"
              @click="handleResumeSubscription"
            >
              {{ resumeActionLabel }}
            </UButton>
          </div>

          <div class="flex items-start gap-2 text-sm text-slate-600 dark:text-slate-400">
            <UIcon name="i-heroicons-calendar-days" class="w-4 h-4 mt-0.5 text-slate-500" />
            <span v-if="planDetails.renewalDate">
              {{ $t('billing.renewsOn') }} {{ formatDate(planDetails.renewalDate) }}
            </span>
            <span v-else>
              {{ $t('billing.noHistory') }}
            </span>
          </div>

          <UAlert
            v-if="pendingCancellationText"
            color="amber"
            variant="subtle"
            icon="i-heroicons-exclamation-triangle"
            :description="pendingCancellationText"
          />
        </div>
      </div>

      <!-- Subscription History -->
      <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700">
        <div class="border-b border-slate-200 dark:border-slate-700 px-6 py-4">
          <h2 class="text-lg font-semibold">{{ $t('billing.subscriptionHistory') }}</h2>
        </div>

        <div class="p-6">
          <div v-if="planDetails.renewalDate" class="space-y-3">
            <div class="flex items-center justify-between py-3 border-b border-slate-200 dark:border-slate-700">
              <div>
                <p class="font-medium">{{ $t('billing.currentPeriod') }}</p>
                <p class="text-sm text-slate-600 dark:text-slate-400">
                  {{ $t('billing.renewsOn') }} {{ formatDate(planDetails.renewalDate) }}
                </p>
              </div>
              <UBadge color="green">{{ $t('billing.active') }}</UBadge>
            </div>

            <div v-if="planDetails.cancellationDate" class="flex items-center justify-between py-3 border-b border-slate-200 dark:border-slate-700">
              <div>
                <p class="font-medium">{{ $t('billing.cancellationScheduled') }}</p>
                <p class="text-sm text-slate-600 dark:text-slate-400">
                  {{ $t('billing.effectiveDate') }}: {{ formatDate(planDetails.cancellationEffectiveDate) }}
                </p>
              </div>
              <UBadge color="red">{{ $t('billing.pending') }}</UBadge>
            </div>

            <div v-if="planDetails.pendingPlanTier" class="flex items-center justify-between py-3">
              <div>
                <p class="font-medium">{{ $t('billing.planChangeScheduled') }}</p>
                <p class="text-sm text-slate-600 dark:text-slate-400">
                  {{ $t('billing.changingTo') }} {{ planDetails.pendingPlanTier }} {{ $t('billing.on') }} {{ formatDate(planDetails.pendingPlanEffectiveDate) }}
                </p>
              </div>
              <UBadge color="yellow">{{ $t('billing.pending') }}</UBadge>
            </div>
          </div>

          <div v-else class="text-center py-8 text-slate-500">
            {{ $t('billing.noHistory') }}
          </div>
        </div>
      </div>
    </div>

    <!-- Plan Override Modal -->
    <PlanOverrideModal
      v-model="showOverrideModal"
      :tenant-id="tenantId"
      :current-plan="planDetails"
      @success="handleOverrideSuccess"
    />

    <!-- Cancel Subscription Modal -->
    <UModal v-model="showCancelModal">
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-red-100 dark:bg-red-900 flex items-center justify-center">
              <UIcon name="i-heroicons-no-symbol" class="w-6 h-6 text-red-600 dark:text-red-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold">{{ $t('billing.cancelSubscription') }}</h3>
              <p class="text-sm text-slate-600 dark:text-slate-400">
                {{ $t('billing.cancelSubscriptionDescription') }}
              </p>
            </div>
          </div>
        </template>

        <div class="space-y-4">
          <UFormGroup :label="$t('billing.cancellationTiming')">
            <URadioGroup
              v-model="cancelForm.immediate"
              :options="cancelTimingOptions"
              :ui="{ wrapper: 'space-y-2' }"
            >
              <template #label="{ option }">
                <div class="flex flex-col">
                  <span class="font-medium">{{ option.label }}</span>
                  <span v-if="option.description" class="text-xs text-slate-500 dark:text-slate-400">
                    {{ option.description }}
                  </span>
                </div>
              </template>
            </URadioGroup>
          </UFormGroup>

          <UFormGroup
            :label="$t('billing.cancellationReason')"
            :help="$t('billing.cancellationReasonHelp')"
          >
            <UTextarea
              v-model="cancelForm.reason"
              :rows="3"
            />
          </UFormGroup>

          <UAlert
            v-if="cancelError"
            color="red"
            variant="subtle"
            icon="i-heroicons-exclamation-circle"
            :description="cancelError"
          />
        </div>

        <template #footer>
          <div class="flex justify-end gap-3">
            <UButton
              color="gray"
              variant="ghost"
              :disabled="cancelLoading"
              @click="handleCancelModalClose"
            >
              {{ $t('common.cancel') }}
            </UButton>
            <UButton
              color="red"
              :loading="cancelLoading"
              :disabled="cancelLoading"
              @click="handleCancelSubscription"
            >
              {{ $t('billing.confirmCancellation') }}
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>

    <!-- Audit Log Modal -->
    <BillingAuditLogModal
      v-model="showAuditLogModal"
      :tenant-id="tenantId"
    />
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const { t } = useI18n()
const api = useSaasApi()
const toast = useToast()

const tenantId = computed(() => Number(route.params.id))

const tenant = ref<any>(null)
const planDetails = ref<any>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const showOverrideModal = ref(false)
const showAuditLogModal = ref(false)
const showCancelModal = ref(false)
const cancelLoading = ref(false)
const resumeLoading = ref(false)
const cancelError = ref<string | null>(null)

const cancelForm = reactive({
  immediate: false,
  reason: ''
})

const planStatus = computed(() => {
  const status = planDetails.value?.status
  return typeof status === 'string' ? status.toUpperCase() : ''
})

const planStatusColor = computed(() => getPlanStatusColor(planStatus.value))

const pendingCancellationText = computed(() => {
  if (!planDetails.value?.cancellationEffectiveDate) return ''

  return t('billing.pendingCancellationNotice', {
    date: formatDate(planDetails.value.cancellationEffectiveDate)
  })
})

const canCancel = computed(() => {
  if (!planDetails.value) return false
  if (planDetails.value.cancellationEffectiveDate) return false

  return ['ACTIVE', 'PAST_DUE'].includes(planStatus.value)
})

const canResume = computed(() => {
  if (!planDetails.value) return false
  return ['CANCELLED', 'CANCELED'].includes(planStatus.value) || !!planDetails.value.cancellationDate
})

const resumeActionLabel = computed(() => {
  if (['CANCELLED', 'CANCELED'].includes(planStatus.value)) {
    return t('billing.activateSubscription')
  }

  if (planDetails.value?.cancellationDate) {
    return t('billing.renewSubscription')
  }

  return t('billing.resumeSubscription')
})

const cancelTimingOptions = computed(() => {
  const atPeriodEndDescription = planDetails.value?.renewalDate
    ? t('billing.subscriptionWillCancel', { date: formatDate(planDetails.value.renewalDate) })
    : ''

  return [
    { label: t('billing.cancelAtPeriodEnd'), value: false, description: atPeriodEndDescription },
    { label: t('billing.cancelImmediate'), value: true, description: t('billing.cancelSubscriptionDescription') }
  ]
})

const loadPlanDetails = async () => {
  loading.value = true
  error.value = null

  try {
    // Load tenant details
    tenant.value = await api.getTenant(tenantId.value)
    
    // Load plan details
    planDetails.value = await api.getTenantPlan(tenantId.value)
  } catch (err: any) {
    error.value = err.message || t('billing.loadError')
    console.error('Failed to load plan details:', err)
  } finally {
    loading.value = false
  }
}

const handleOverrideSuccess = async () => {
  toast.add({
    title: t('billing.overrideSuccess'),
    description: t('billing.planUpdated'),
    color: 'green'
  })
  
  await loadPlanDetails()
}

const handleCancelSubscription = async () => {
  cancelLoading.value = true
  cancelError.value = null

  try {
    const response = await api.cancelTenantSubscription(
      tenantId.value,
      cancelForm.immediate,
      cancelForm.reason || undefined
    )

    const effectiveDateText = response?.effectiveDate
      ? formatDate(response.effectiveDate)
      : t('common.notAvailable')

    toast.add({
      title: cancelForm.immediate
        ? t('billing.cancelSuccessImmediate')
        : t('billing.cancelSuccessScheduled', { date: effectiveDateText }),
      description: response?.message,
      color: 'green'
    })

    showCancelModal.value = false
    await loadPlanDetails()
  } catch (err: any) {
    const message = err?.data?.message || err?.message || t('billing.cancelError')
    cancelError.value = message
    toast.add({
      title: t('billing.cancelError'),
      description: message,
      color: 'red'
    })
  } finally {
    cancelLoading.value = false
  }
}

const handleResumeSubscription = async () => {
  resumeLoading.value = true

  try {
    await api.resumeTenantSubscription(tenantId.value)

    toast.add({
      title: t('billing.resumeSuccess'),
      description: t('billing.resumeDescription'),
      color: 'green'
    })

    await loadPlanDetails()
  } catch (err: any) {
    const message = err?.data?.message || err?.message || t('billing.resumeError')
    toast.add({
      title: t('billing.resumeError'),
      description: message,
      color: 'red'
    })
  } finally {
    resumeLoading.value = false
  }
}

const resetCancelForm = () => {
  cancelForm.immediate = false
  cancelForm.reason = ''
  cancelError.value = null
}

const handleCancelModalClose = () => {
  showCancelModal.value = false
}

const getPlanStatusColor = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'green'
    case 'PAST_DUE':
      return 'orange'
    case 'CANCELLED':
    case 'CANCELED':
      return 'red'
    case 'PENDING':
      return 'yellow'
    default:
      return 'gray'
  }
}

const formatStatus = (status: string) => {
  if (!status) return t('common.notAvailable')

  return status.split('_').map(word => 
    word.charAt(0) + word.slice(1).toLowerCase()
  ).join(' ')
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

onMounted(() => {
  loadPlanDetails()
})

watch(showCancelModal, (isOpen) => {
  if (!isOpen) {
    resetCancelForm()
  }
})
</script>
