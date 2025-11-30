<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-3xl font-bold text-slate-900 dark:text-white">{{ $t('billing.paypal.title') }}</h1>
        <p class="text-slate-600 dark:text-slate-400 mt-1">{{ $t('billing.paypal.subtitle') }}</p>
      </div>
    </div>

    <!-- Loading -->
    <LoadingSkeleton v-if="loading" type="form" :rows="5" />

    <!-- Error -->
    <UAlert
      v-else-if="error"
      color="red"
      variant="subtle"
      icon="i-heroicons-exclamation-triangle"
      title="Error"
      :description="error"
    >
      <template #actions>
        <UButton color="red" variant="ghost" @click="loadConfig">{{ $t('common.retry') }}</UButton>
      </template>
    </UAlert>

    <!-- Configuration Form -->
    <div v-if="!loading && !error" class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
      <form @submit.prevent="handleSubmit" class="space-y-6">
        <!-- Sandbox Mode Toggle -->
        <div class="flex items-center justify-between p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
          <div class="flex items-center gap-3">
            <UIcon 
              :name="form.sandboxMode ? 'i-heroicons-beaker' : 'i-heroicons-globe-alt'" 
              class="w-5 h-5 text-blue-600 dark:text-blue-400"
            />
            <div>
              <p class="font-medium text-slate-900 dark:text-white">
                {{ form.sandboxMode ? $t('billing.paypal.sandboxMode') : $t('billing.paypal.productionMode') }}
              </p>
              <p class="text-sm text-slate-600 dark:text-slate-400">
                {{ form.sandboxMode ? $t('billing.paypal.sandboxDescription') : $t('billing.paypal.productionDescription') }}
              </p>
            </div>
          </div>
          <UToggle 
            v-model="form.sandboxMode" 
            size="lg"
            :aria-label="$t('billing.paypal.toggleSandboxMode')"
          />
        </div>

        <!-- Client ID -->
        <div>
          <label for="clientId" class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
            {{ $t('billing.paypal.clientId') }}
            <span class="text-red-500">*</span>
          </label>
          <UInput
            id="clientId"
            v-model="form.clientId"
            type="text"
            size="lg"
            :placeholder="$t('billing.paypal.clientIdPlaceholder')"
            :disabled="saving"
            required
          />
          <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
            {{ $t('billing.paypal.clientIdHelp') }}
          </p>
        </div>

        <!-- Client Secret -->
        <div>
          <label for="clientSecret" class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
            {{ $t('billing.paypal.clientSecret') }}
            <span class="text-red-500">*</span>
          </label>
          <div class="relative">
            <UInput
              id="clientSecret"
              v-model="form.clientSecret"
              :type="showSecret ? 'text' : 'password'"
              size="lg"
              :placeholder="config?.maskedClientSecret || '****'"
              :disabled="saving"
            />
            <button
              type="button"
              @click="showSecret = !showSecret"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-300"
              :aria-label="showSecret ? $t('login.hidePassword') : $t('login.showPassword')"
            >
              <UIcon :name="showSecret ? 'i-heroicons-eye-slash' : 'i-heroicons-eye'" class="w-5 h-5" />
            </button>
          </div>
          <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
            {{ $t('billing.paypal.clientSecretHelp') }}
          </p>
        </div>

        <!-- Plan Mappings -->
        <div class="space-y-4">
          <div>
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">
              {{ $t('billing.paypal.planMappingsTitle') }}
            </p>
            <p class="text-sm text-slate-500 dark:text-slate-400">
              {{ $t('billing.paypal.planMappingsDescription') }}
            </p>
          </div>

          <div class="space-y-4">
            <div
              v-for="(plan, index) in form.planConfigs"
              :key="`plan-${index}`"
              class="rounded-xl border border-slate-200 dark:border-slate-700 p-4 space-y-4 bg-slate-50/60 dark:bg-slate-900/20"
            >
              <div class="flex flex-col sm:flex-row sm:items-center gap-4">
                <div class="flex-1">
                  <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {{ $t('billing.paypal.planTierLabel') }}
                  </label>
                  <USelect
                    v-model="plan.tier"
                    :options="tierOptions"
                    option-attribute="label"
                    value-attribute="value"
                    size="lg"
                    :disabled="saving"
                    :placeholder="$t('billing.paypal.planTierPlaceholder')"
                  />
                </div>
                <UButton
                  v-if="form.planConfigs.length > 1"
                  type="button"
                  color="red"
                  variant="ghost"
                  icon="i-heroicons-trash"
                  class="self-start"
                  @click="removePlanConfig(index)"
                  :disabled="saving"
                >
                  {{ $t('billing.paypal.removePlanConfig') }}
                </UButton>
              </div>

              <div class="grid gap-4 sm:grid-cols-2">
                <div>
                  <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {{ $t('billing.paypal.planName') }}
                  </label>
                  <UInput
                    v-model="plan.displayName"
                    type="text"
                    size="lg"
                    :placeholder="$t('billing.paypal.planNamePlaceholder')"
                    :disabled="saving"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {{ $t('billing.paypal.currency') }}
                  </label>
                  <UInput
                    v-model="plan.currency"
                    type="text"
                    size="lg"
                    placeholder="USD"
                    :disabled="saving"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  {{ $t('billing.paypal.planDescription') }}
                </label>
                <UTextarea
                  v-model="plan.description"
                  :placeholder="$t('billing.paypal.planDescriptionPlaceholder')"
                  :disabled="saving"
                  :rows="3"
                />
              </div>

              <div class="grid gap-4 sm:grid-cols-2">
                <div>
                  <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {{ $t('billing.paypal.monthlyPrice') }}
                  </label>
                  <UInput
                    v-model="plan.monthlyPrice"
                    type="number"
                    size="lg"
                    :placeholder="$t('billing.paypal.monthlyPricePlaceholder')"
                    :disabled="saving"
                    step="0.01"
                    min="0"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {{ $t('billing.paypal.annualPrice') }}
                  </label>
                  <UInput
                    v-model="plan.annualPrice"
                    type="number"
                    size="lg"
                    :placeholder="$t('billing.paypal.annualPricePlaceholder')"
                    :disabled="saving"
                    step="0.01"
                    min="0"
                  />
                </div>
              </div>

              <div class="grid gap-4 sm:grid-cols-2">
                <div>
                  <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {{ $t('billing.paypal.monthlyPlanId') }}
                  </label>
                  <UInput
                    v-model="plan.monthlyPlanId"
                    type="text"
                    size="lg"
                    :placeholder="$t('billing.paypal.monthlyPlanIdPlaceholder')"
                    :disabled="saving"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {{ $t('billing.paypal.annualPlanId') }}
                  </label>
                  <UInput
                    v-model="plan.annualPlanId"
                    type="text"
                    size="lg"
                    :placeholder="$t('billing.paypal.annualPlanIdPlaceholder')"
                    :disabled="saving"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  {{ $t('billing.paypal.features') }}
                </label>
                <UTextarea
                  v-model="featuresInput[index]"
                  :placeholder="$t('billing.paypal.featuresPlaceholder')"
                  :disabled="saving"
                  :rows="3"
                  @blur="applyFeatures(index)"
                />
                <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">
                  {{ $t('billing.paypal.featuresHint') }}
                </p>
              </div>
            </div>
          </div>

          <UButton
            type="button"
            color="gray"
            variant="ghost"
            icon="i-heroicons-plus"
            @click="addPlanConfig"
            :disabled="saving"
          >
            {{ $t('billing.paypal.addPlanConfig') }}
          </UButton>
        </div>

        <!-- Webhook ID (Optional) -->
        <div>
          <label for="webhookId" class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
            {{ $t('billing.paypal.webhookId') }}
          </label>
          <UInput
            id="webhookId"
            v-model="form.webhookId"
            type="text"
            size="lg"
            :placeholder="$t('billing.paypal.webhookIdPlaceholder')"
            :disabled="saving"
          />
          <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
            {{ $t('billing.paypal.webhookIdHelp') }}
          </p>
        </div>

        <!-- Action Buttons -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t border-slate-200 dark:border-slate-700">
          <UButton
            type="button"
            color="gray"
            variant="ghost"
            size="lg"
            @click="resetForm"
            :disabled="saving"
          >
            {{ $t('common.cancel') }}
          </UButton>
          <UButton
            type="submit"
            color="primary"
            size="lg"
            :loading="saving"
            :disabled="!isFormValid"
          >
            {{ saving ? $t('billing.paypal.saving') : $t('billing.paypal.saveConfiguration') }}
          </UButton>
        </div>
      </form>
    </div>

    <!-- Info Card -->
    <div class="bg-blue-50 dark:bg-blue-900/20 rounded-xl border border-blue-200 dark:border-blue-800 p-6">
      <div class="flex gap-4">
        <div class="flex-shrink-0">
          <UIcon name="i-heroicons-information-circle" class="w-6 h-6 text-blue-600 dark:text-blue-400" />
        </div>
        <div class="flex-1">
          <h3 class="font-semibold text-slate-900 dark:text-white mb-2">
            {{ $t('billing.paypal.infoTitle') }}
          </h3>
          <ul class="space-y-2 text-sm text-slate-600 dark:text-slate-400">
            <li class="flex items-start gap-2">
              <UIcon name="i-heroicons-check-circle" class="w-4 h-4 text-blue-600 dark:text-blue-400 mt-0.5 flex-shrink-0" />
              <span>{{ $t('billing.paypal.info1') }}</span>
            </li>
            <li class="flex items-start gap-2">
              <UIcon name="i-heroicons-check-circle" class="w-4 h-4 text-blue-600 dark:text-blue-400 mt-0.5 flex-shrink-0" />
              <span>{{ $t('billing.paypal.info2') }}</span>
            </li>
            <li class="flex items-start gap-2">
              <UIcon name="i-heroicons-check-circle" class="w-4 h-4 text-blue-600 dark:text-blue-400 mt-0.5 flex-shrink-0" />
              <span>{{ $t('billing.paypal.info3') }}</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'default'
})

import type { PayPalPlanConfig } from '~/types'

const { t } = useI18n()
const toast = useToast()
const api = useSaasApi()

// State
const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const showSecret = ref(false)
const config = ref<any>(null)

// Form data
const form = reactive({
  clientId: '',
  clientSecret: '',
  webhookId: '',
  sandboxMode: true,
  planConfigs: [] as PayPalPlanConfig[]
})
const featuresInput = ref<string[]>([])

const planTierKeys = ['BASIC', 'PROFESSIONAL', 'ENTERPRISE', 'CUSTOM']
const tierOptions = computed(() =>
  planTierKeys.map((tier) => ({
    label: t(`billing.${tier.toLowerCase()}`),
    value: tier
  }))
)

const createPlanConfig = (tier = 'BASIC'): PayPalPlanConfig => ({
  tier,
  displayName: '',
  description: '',
  currency: 'USD',
  monthlyPrice: undefined,
  annualPrice: undefined,
  features: [],
  monthlyPlanId: '',
  annualPlanId: ''
})

const sanitizedPlanConfigs = computed(() =>
  form.planConfigs
    .map((cfg) => ({
      tier: (cfg.tier || '').toUpperCase(),
      displayName: cfg.displayName?.trim() || undefined,
      description: cfg.description?.trim() || undefined,
      currency: cfg.currency?.trim() || undefined,
      monthlyPrice: cfg.monthlyPrice ?? undefined,
      annualPrice: cfg.annualPrice ?? undefined,
      features: cfg.features?.map((f) => f.trim()).filter(Boolean) || undefined,
      monthlyPlanId: cfg.monthlyPlanId?.trim() || undefined,
      annualPlanId: cfg.annualPlanId?.trim() || undefined
    }))
    .filter((cfg) => cfg.tier && (cfg.monthlyPlanId || cfg.annualPlanId))
)

// Computed
const isFormValid = computed(() => {
  return form.clientId.trim() !== '' && 
         sanitizedPlanConfigs.value.length > 0 &&
         (form.clientSecret.trim() !== '' || config.value !== null)
})

const ensurePlanConfigs = (configs?: PayPalPlanConfig[]) => {
  if (configs && configs.length) {
    form.planConfigs = configs.map((cfg) => ({
      tier: cfg.tier || 'BASIC',
      displayName: cfg.displayName || '',
      description: cfg.description || '',
      currency: cfg.currency || 'USD',
      monthlyPrice: cfg.monthlyPrice,
      annualPrice: cfg.annualPrice,
      features: cfg.features || [],
      monthlyPlanId: cfg.monthlyPlanId || '',
      annualPlanId: cfg.annualPlanId || ''
    }))
    featuresInput.value = form.planConfigs.map((cfg) => (cfg.features || []).join('\n'))
  } else {
    form.planConfigs = [createPlanConfig('BASIC')]
    featuresInput.value = ['']
  }
}

const addPlanConfig = () => {
  form.planConfigs.push(createPlanConfig())
  featuresInput.value.push('')
}

const removePlanConfig = (index: number) => {
  if (form.planConfigs.length === 1) return
  form.planConfigs.splice(index, 1)
  featuresInput.value.splice(index, 1)
}

const applyFeatures = (index: number) => {
  const raw = featuresInput.value[index] || ''
  const parts = raw
    .split(/\r?\n|,/)
    .map((f) => f.trim())
    .filter(Boolean)
  form.planConfigs[index].features = parts
}

// Load configuration
const loadConfig = async () => {
  loading.value = true
  error.value = null

  try {
    const response = await api.getPayPalConfig()
    config.value = response
    
    // Populate form with existing config
    form.clientId = response.clientId || ''
    form.webhookId = response.webhookId || ''
    form.sandboxMode = response.sandboxMode ?? true
    ensurePlanConfigs(response.planConfigs)
    // Don't populate clientSecret - it's masked
    form.clientSecret = ''
  } catch (err: any) {
    console.error('Error loading PayPal config:', err)
    error.value = err.message || t('billing.paypal.loadError')
  } finally {
    loading.value = false
  }
}

// Save configuration
const handleSubmit = async () => {
  if (!isFormValid.value) return

  saving.value = true

  try {
    const plans = sanitizedPlanConfigs.value
    if (plans.length === 0) {
      throw new Error(t('billing.paypal.planConfigValidationError'))
    }

    const payload: any = {
      clientId: form.clientId.trim(),
      sandboxMode: form.sandboxMode,
      planConfigs: plans
    }

    // Only include clientSecret if it was changed
    if (form.clientSecret.trim() !== '') {
      payload.clientSecret = form.clientSecret.trim()
    }

    // Include webhookId if provided
    if (form.webhookId.trim() !== '') {
      payload.webhookId = form.webhookId.trim()
    }

    const response = await api.updatePayPalConfig(payload)
    config.value = response

    toast.add({
      title: t('billing.paypal.saveSuccess'),
      description: t('billing.paypal.saveSuccessDescription'),
      color: 'green',
      icon: 'i-heroicons-check-circle'
    })

    // Clear the client secret field after successful save
    form.clientSecret = ''
  } catch (err: any) {
    console.error('Error saving PayPal config:', err)
    
    let errorMessage = t('billing.paypal.saveError')
    if (err.message) {
      errorMessage = err.message
    }

    toast.add({
      title: t('common.error'),
      description: errorMessage,
      color: 'red',
      icon: 'i-heroicons-exclamation-triangle'
    })
  } finally {
    saving.value = false
  }
}

// Reset form
const resetForm = () => {
  if (config.value) {
    form.clientId = config.value.clientId || ''
    form.webhookId = config.value.webhookId || ''
    form.sandboxMode = config.value.sandboxMode ?? true
    ensurePlanConfigs(config.value.planConfigs)
    form.clientSecret = ''
  } else {
    form.clientId = ''
    form.clientSecret = ''
    form.webhookId = ''
    form.sandboxMode = true
    ensurePlanConfigs()
  }
}

// Load config on mount
onMounted(() => {
  loadConfig()
})
</script>
