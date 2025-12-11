<template>
  <UModal v-model="isOpen">
    <UCard>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
            <UIcon name="i-heroicons-pencil-square" class="w-6 h-6 text-blue-600 dark:text-blue-400" />
          </div>
          <h3 class="text-lg font-semibold">{{ $t('billing.overridePlan') }}</h3>
        </div>
      </template>

      <div class="space-y-4">
        <p class="text-slate-600 dark:text-slate-400">
          {{ $t('billing.overrideDescription') }}
        </p>

        <!-- Current Plan Info -->
        <div v-if="currentPlan" class="p-4 rounded-lg bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700">
          <p class="text-xs text-slate-500 uppercase font-medium mb-1">{{ $t('billing.currentPlan') }}</p>
          <div class="flex items-center gap-2">
            <p class="font-semibold text-lg">{{ currentPlan.planTierName || currentPlan.planTier }}</p>
            <UBadge :color="getStatusColor(currentPlan.status)">
              {{ formatStatus(currentPlan.status) }}
            </UBadge>
          </div>
        </div>

        <!-- New Plan Selection -->
        <div>
          <label class="block text-sm font-medium mb-2">
            {{ $t('billing.newPlanTier') }} <span class="text-red-500">*</span>
          </label>
          <USelect
            v-model="form.targetTier"
            :options="planTierOptions"
            size="lg"
            :placeholder="$t('billing.selectPlanTier')"
          />
        </div>

        <div>
          <label class="block text-sm font-medium mb-2">
            {{ $t('billing.billingCycle') }} <span class="text-red-500">*</span>
          </label>
          <USelect
            v-model="form.billingCycle"
            :options="billingCycleOptions"
            size="lg"
            :placeholder="$t('billing.selectBillingCycle')"
          />
        </div>

        <div>
          <label class="block text-sm font-medium mb-2">
            {{ $t('billing.billingCycle') }}
          </label>
          <USelect
            v-model="form.billingCycle"
            :options="billingCycleOptions"
            size="lg"
            :placeholder="$t('billing.selectBillingCycle')"
          />
        </div>

        <!-- Reason Input -->
        <div>
          <label class="block text-sm font-medium mb-2">
            {{ $t('billing.reasonForOverride') }} <span class="text-red-500">*</span>
          </label>
          <UTextarea
            v-model="form.reason"
            :placeholder="$t('billing.reasonPlaceholder')"
            :rows="4"
          />
          <p class="text-xs text-slate-500 mt-1">
            {{ $t('billing.reasonHint') }}
          </p>
        </div>

        <!-- Warning Alert -->
        <UAlert
          color="amber"
          variant="subtle"
          icon="i-heroicons-exclamation-triangle"
          :title="$t('billing.warning')"
          :description="$t('billing.overrideWarning')"
        />

        <!-- Validation Errors -->
        <UAlert
          v-if="validationError"
          color="red"
          variant="subtle"
          icon="i-heroicons-exclamation-circle"
          :title="$t('common.validationError')"
          :description="validationError"
        />
      </div>

      <template #footer>
        <div class="flex justify-end gap-3">
          <UButton
            color="gray"
            variant="ghost"
            :disabled="loading"
            @click="handleCancel"
          >
            {{ $t('common.cancel') }}
          </UButton>
          <UButton
            color="blue"
            :loading="loading"
            :disabled="!isFormValid"
            @click="handleSubmit"
          >
            {{ $t('billing.confirmOverride') }}
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
const { t } = useI18n()
const api = useSaasApi()
const toast = useToast()

interface Props {
  modelValue: boolean
  tenantId: number
  currentPlan?: any
}

const props = defineProps<Props>()
const emit = defineEmits(['update:modelValue', 'success'])

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const form = reactive({
  targetTier: '',
  billingCycle: 'MONTHLY',
  reason: ''
})

const loading = ref(false)
const validationError = ref<string | null>(null)

const planTierOptions = [
  { label: t('billing.basic'), value: 'BASIC' },
  { label: t('billing.professional'), value: 'PROFESSIONAL' },
  { label: t('billing.enterprise'), value: 'ENTERPRISE' },
  { label: t('billing.custom'), value: 'CUSTOM' }
]

const billingCycleOptions = [
  { label: t('billing.monthly'), value: 'MONTHLY' },
  { label: t('billing.annual'), value: 'ANNUAL' }
]

const isFormValid = computed(() => {
  return form.targetTier && form.reason.trim().length >= 10
})

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

const formatStatus = (status: string) => {
  if (!status) return ''
  return status.split('_').map(word => 
    word.charAt(0) + word.slice(1).toLowerCase()
  ).join(' ')
}

const validateForm = () => {
  validationError.value = null

  if (!form.targetTier) {
    validationError.value = t('billing.selectPlanTierError')
    return false
  }

  if (!form.billingCycle) {
    validationError.value = t('billing.billingCycleError')
    return false
  }

  if (form.reason.trim().length < 10) {
    validationError.value = t('billing.reasonTooShortError')
    return false
  }

  if (props.currentPlan && form.targetTier === props.currentPlan.planTier) {
    validationError.value = t('billing.samePlanError')
    return false
  }

  return true
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  loading.value = true

  try {
    await api.overrideTenantPlan(props.tenantId, {
      targetTier: form.targetTier,
      billingCycle: form.billingCycle,
      reason: form.reason.trim()
    })

    toast.add({
      title: t('billing.overrideSuccess'),
      description: t('billing.planOverridden', { tier: form.targetTier }),
      color: 'green'
    })

    emit('success')
    handleCancel()
  } catch (err: any) {
    toast.add({
      title: t('billing.overrideFailed'),
      description: err.message || t('common.unknownError'),
      color: 'red'
    })
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  form.targetTier = ''
  form.billingCycle = 'MONTHLY'
  form.reason = ''
  validationError.value = null
  isOpen.value = false
}

// Reset form when modal opens
watch(isOpen, (newValue) => {
  if (newValue) {
    form.targetTier = ''
    form.reason = ''
    validationError.value = null
  }
})
</script>
