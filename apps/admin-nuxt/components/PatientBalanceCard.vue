<template>
  <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
    <!-- Header -->
    <div :class="headerGradientClass">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <UIcon name="i-lucide-wallet" class="h-5 w-5 text-white" />
          <div>
            <h3 class="text-lg font-semibold text-white">{{ t('patients.details.balance.title') }}</h3>
            <p class="text-sm text-white/80">{{ t('patients.details.balance.subtitle') }}</p>
          </div>
        </div>
        <UBadge 
          v-if="hasOutstandingBalance" 
          :color="balanceBadgeColor" 
          variant="solid" 
          size="sm"
        >
          {{ t('patients.details.balance.outstanding') }}
        </UBadge>
      </div>
    </div>

    <!-- Content -->
    <div class="p-6">
      <div v-if="loading" class="space-y-4">
        <USkeleton class="h-8 w-32 rounded" />
        <USkeleton class="h-4 w-24 rounded" />
      </div>
      
      <div v-else-if="error" class="text-center py-6">
        <div class="h-12 w-12 rounded-full bg-red-100 dark:bg-red-900/20 flex items-center justify-center mx-auto mb-3">
          <UIcon name="i-lucide-alert-triangle" class="h-6 w-6 text-red-600 dark:text-red-400" />
        </div>
        <h4 class="text-sm font-medium text-slate-900 dark:text-white mb-1">
          {{ t('patients.details.balance.error.title') }}
        </h4>
        <p class="text-xs text-slate-500 dark:text-slate-400 mb-3">
          {{ t('patients.details.balance.error.description') }}
        </p>
        <UButton
          size="xs"
          color="red"
          variant="soft"
          icon="i-lucide-refresh-cw"
          @click="$emit('retry')"
        >
          {{ t('patients.details.balance.error.retry') }}
        </UButton>
      </div>
      
      <div v-else class="space-y-4">
        <!-- Total Balance Display -->
        <div class="text-center">
          <div class="text-3xl font-bold" :class="balanceTextColor">
            <CurrencyValue
              :amount="balanceSummary.totalBalance"
              :currency="balanceSummary.currency"
              variant="primary"
              size="lg"
            />
          </div>
          <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">
            {{ balanceDescription }}
          </p>
        </div>

        <!-- Balance Breakdown Summary -->
        <div v-if="hasActiveBalances" class="space-y-3">
          <div class="border-t border-slate-200 dark:border-slate-700 pt-4">
            <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide mb-3">
              {{ t('patients.details.balance.breakdown.title') }}
            </p>
            <div class="space-y-2">
              <div 
                v-for="balance in activeBalances.slice(0, 3)" 
                :key="balance.id"
                class="flex items-center justify-between text-sm"
              >
                <span class="text-slate-600 dark:text-slate-300 truncate flex-1 mr-2">
                  {{ balance.treatmentTypeName }}
                </span>
                <span class="font-medium text-slate-900 dark:text-white">
                  <CurrencyValue
                    :amount="balance.convertedRemainingBalance || balance.remainingBalance"
                    :currency="balance.convertedCurrency || balance.currency"
                    variant="primary"
                    size="sm"
                  />
                </span>
              </div>
              <div v-if="activeBalances.length > 3" class="text-xs text-slate-400 dark:text-slate-500 text-center pt-1">
                {{ t('patients.details.balance.breakdown.more', { count: activeBalances.length - 3 }) }}
              </div>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="flex gap-2 pt-2">
          <UButton
            v-if="hasActiveBalances"
            size="sm"
            color="blue"
            variant="soft"
            icon="i-lucide-list"
            @click="showBreakdownModal = true"
            class="flex-1"
          >
            {{ t('patients.details.balance.actions.viewBreakdown') }}
          </UButton>
          <UButton
            v-if="hasOutstandingBalance"
            size="sm"
            color="emerald"
            variant="outline"
            icon="i-lucide-credit-card"
            @click="showPaymentModal = true"
            class="flex-1"
          >
            {{ t('patients.details.balance.actions.recordPayment') }}
          </UButton>
        </div>
      </div>
    </div>

    <!-- Balance Breakdown Modal -->
    <UModal 
      v-model="showBreakdownModal"
      :ui="{
        width: 'w-full sm:max-w-2xl',
        height: 'h-auto max-h-[90vh]'
      }"
    >
      <UCard>
        <template #header>
          <div class="flex items-center justify-between">
            <div>
              <h3 
                id="balance-modal-title"
                class="text-lg font-semibold text-slate-900 dark:text-white"
              >
                {{ t('patients.details.balance.modal.title') }}
              </h3>
              <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">
                {{ t('patients.details.balance.modal.subtitle', { 
                  total: formattedTotalBalance, 
                  count: balanceCount 
                }) }}
              </p>
            </div>
            <UButton
              color="gray"
              variant="ghost"
              icon="i-lucide-x"
              :aria-label="t('common.actions.close')"
              @click="closeModal"
              @keydown.escape="closeModal"
            />
          </div>
        </template>

        <div 
          class="space-y-4"
          role="main"
          :aria-labelledby="'balance-modal-title'"
        >
          <!-- No Balance State -->
          <div v-if="!hasActiveBalances" class="text-center py-8">
            <div class="h-16 w-16 rounded-full bg-green-100 dark:bg-green-900/20 flex items-center justify-center mx-auto mb-4">
              <UIcon name="i-lucide-check-circle" class="h-8 w-8 text-green-600 dark:text-green-400" />
            </div>
            <h4 class="text-lg font-semibold text-slate-900 dark:text-white">
              {{ t('patients.details.balance.modal.noBalance.title') }}
            </h4>
            <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
              {{ t('patients.details.balance.modal.noBalance.description') }}
            </p>
          </div>

          <!-- Balance Breakdown List -->
          <div v-else class="space-y-3">
            <!-- Summary Card -->
            <div class="rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 p-4">
              <div class="flex items-center justify-between">
                <div>
                  <h4 class="font-medium text-slate-900 dark:text-white">
                    {{ t('patients.details.balance.modal.summary.title') }}
                  </h4>
                  <p class="text-sm text-slate-500 dark:text-slate-400">
                    {{ t('patients.details.balance.modal.summary.description', { count: balanceCount }) }}
                  </p>
                </div>
                <div class="text-right">
                  <div class="text-xl font-bold" :class="balanceTextColor">
                    <CurrencyValue
                      :amount="balanceSummary.totalBalance"
                      :currency="balanceSummary.currency"
                      variant="primary"
                      size="lg"
                    />
                  </div>
                  <UBadge :color="balanceBadgeColor" variant="soft" size="sm">
                    {{ t(`patients.details.balance.categories.${balanceCategory}`) }}
                  </UBadge>
                </div>
              </div>
            </div>

            <!-- Individual Balance Items -->
            <div
              v-for="(balance, index) in activeBalances"
              :key="balance.id"
              class="rounded-xl border border-slate-200 dark:border-slate-600 p-4 hover:shadow-sm hover:border-slate-300 dark:hover:border-slate-500 transition-all duration-200 focus-within:ring-2 focus-within:ring-blue-500 focus-within:ring-opacity-50"
              :tabindex="0"
              role="listitem"
              :aria-label="t('patients.details.balance.modal.itemLabel', { 
                index: index + 1, 
                treatment: balance.treatmentTypeName,
                amount: formatCurrency(balance.remainingBalance, balance.currency)
              })"
            >
              <div class="flex items-start justify-between">
                <div class="flex-1 min-w-0">
                  <h4 class="font-medium text-slate-900 dark:text-white truncate">
                    {{ balance.treatmentTypeName }}
                  </h4>
                  <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">
                    {{ t('patients.details.balance.modal.planWithDoctor', { doctor: balance.doctorName }) }}
                  </p>
                  <div class="flex items-center gap-2 mt-2">
                    <UBadge :color="getPlanStatusColor(balance.status)" variant="soft" size="xs">
                      {{ getPlanStatusLabel(balance.status) }}
                    </UBadge>
                    <span class="text-xs text-slate-400 dark:text-slate-500">
                      {{ t('patients.details.balance.modal.planId', { id: balance.id }) }}
                    </span>
                  </div>
                </div>
                <div class="text-right ml-4 flex-shrink-0">
                  <div class="text-lg font-semibold text-slate-900 dark:text-white">
                    <CurrencyValue
                      :amount="balance.convertedRemainingBalance || balance.remainingBalance"
                      :currency="balance.convertedCurrency || balance.currency"
                      variant="primary"
                      size="md"
                    />
                  </div>
                  <div class="flex gap-2">
                    <UButton
                      size="xs"
                      color="emerald"
                      variant="soft"
                      icon="i-lucide-credit-card"
                      @click="handleRecordPlanPayment(balance)"
                      :aria-label="t('patients.details.balance.modal.actions.recordPlanPaymentLabel', { 
                        treatment: balance.treatmentTypeName 
                      })"
                    >
                      {{ t('patients.details.balance.modal.actions.recordPayment') }}
                    </UButton>
                    <UButton
                      size="xs"
                      color="blue"
                      variant="soft"
                      icon="i-lucide-external-link"
                      :to="`/treatment-plans/${balance.id}`"
                      :aria-label="t('patients.details.balance.modal.actions.viewPlanLabel', { 
                        treatment: balance.treatmentTypeName 
                      })"
                    >
                      {{ t('patients.details.balance.modal.actions.viewPlan') }}
                    </UButton>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="flex justify-between items-center">
            <div class="text-sm text-slate-500 dark:text-slate-400">
              {{ t('patients.details.balance.modal.footer.lastUpdated') }}
            </div>
            <div class="flex gap-2">
              <UButton
                v-if="hasActiveBalances"
                color="emerald"
                variant="soft"
                icon="i-lucide-credit-card"
                size="sm"
                @click="showPaymentModal = true; showBreakdownModal = false"
              >
                {{ t('patients.details.balance.modal.actions.recordPayment') }}
              </UButton>
              <UButton
                color="gray"
                variant="ghost"
                @click="closeModal"
              >
                {{ t('common.actions.close') }}
              </UButton>
            </div>
          </div>
        </template>
      </UCard>
    </UModal>

    <!-- Payment Recording Modal -->
    <UModal 
      v-model="showPaymentModal"
      :ui="{
        width: 'w-full sm:max-w-lg',
        height: 'h-auto max-h-[90vh]'
      }"
    >
      <UCard>
        <template #header>
          <div class="flex items-center justify-between">
            <div>
              <h3 
                id="payment-modal-title"
                class="text-lg font-semibold text-slate-900 dark:text-white"
              >
                {{ t('patients.details.balance.payment.title') }}
              </h3>
              <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">
                {{ selectedPlan ? 
                  t('patients.details.balance.payment.subtitlePlan', { plan: selectedPlan.treatmentTypeName }) :
                  t('patients.details.balance.payment.subtitle')
                }}
              </p>
            </div>
            <UButton
              color="gray"
              variant="ghost"
              icon="i-lucide-x"
              :aria-label="t('common.actions.close')"
              @click="closePaymentModal"
            />
          </div>
        </template>

        <div class="space-y-4">
          <!-- Plan Selection (if no specific plan selected) -->
          <div v-if="!selectedPlan && hasActiveBalances" class="space-y-3">
            <h4 class="text-sm font-medium text-slate-900 dark:text-white">
              {{ t('patients.details.balance.payment.selectPlan') }}
            </h4>
            <div class="space-y-2">
              <button
                v-for="balance in activeBalances"
                :key="balance.id"
                @click="selectPlanForPayment(balance)"
                class="w-full text-left p-3 rounded-lg border border-slate-200 dark:border-slate-600 hover:border-emerald-300 dark:hover:border-emerald-500 hover:bg-emerald-50 dark:hover:bg-emerald-900/20 transition-colors duration-200"
              >
                <div class="flex items-center justify-between">
                  <div>
                    <p class="font-medium text-slate-900 dark:text-white">
                      {{ balance.treatmentTypeName }}
                    </p>
                    <p class="text-sm text-slate-500 dark:text-slate-400">
                      {{ t('patients.details.balance.payment.planWithDoctor', { doctor: balance.doctorName }) }}
                    </p>
                  </div>
                  <div class="text-right">
                    <p class="font-semibold text-slate-900 dark:text-white">
                      <CurrencyValue
                        :amount="balance.remainingBalance"
                        :currency="balance.currency"
                        variant="primary"
                        size="sm"
                      />
                    </p>
                  </div>
                </div>
              </button>
            </div>
          </div>

          <!-- Payment Form (when plan is selected) -->
          <div v-if="selectedPlan" class="space-y-4">
            <!-- Selected Plan Info -->
            <div class="p-3 bg-emerald-50 dark:bg-emerald-900/20 rounded-lg border border-emerald-200 dark:border-emerald-700">
              <div class="flex items-center justify-between">
                <div>
                  <p class="font-medium text-emerald-900 dark:text-emerald-100">
                    {{ selectedPlan.treatmentTypeName }}
                  </p>
                  <p class="text-sm text-emerald-700 dark:text-emerald-300">
                    {{ t('patients.details.balance.payment.planWithDoctor', { doctor: selectedPlan.doctorName }) }}
                  </p>
                </div>
                <div class="text-right">
                  <p class="text-sm text-emerald-700 dark:text-emerald-300">
                    {{ t('patients.details.balance.payment.outstandingBalance') }}
                  </p>
                  <p class="font-semibold text-emerald-900 dark:text-emerald-100">
                    <CurrencyValue
                      :amount="selectedPlan.remainingBalance"
                      :currency="selectedPlan.currency"
                      variant="primary"
                      size="sm"
                    />
                  </p>
                </div>
              </div>
            </div>

            <!-- Payment Amount -->
            <div>
              <div class="flex items-center justify-between mb-2">
                <label class="block text-sm font-medium text-slate-900 dark:text-white">
                  {{ t('patients.details.balance.payment.amount') }}
                  <span class="text-xs text-slate-500 dark:text-slate-400 ml-1">({{ selectedPlan.currency }})</span>
                </label>
                <UButton
                  size="xs"
                  color="blue"
                  variant="ghost"
                  @click="payFullBalance"
                  :disabled="isSubmittingPayment"
                >
                  {{ t('patients.details.balance.payment.payFullBalance') }}
                </UButton>
              </div>
              <UInput
                v-model="paymentAmount"
                type="number"
                :placeholder="t('patients.details.balance.payment.amountPlaceholder')"
                :max="selectedPlan.remainingBalance"
                step="0.01"
                min="0.01"
                size="lg"
                :error="!!paymentAmountError"
                @blur="validatePaymentAmount"
                @input="paymentAmountError = ''"
              />
              <p v-if="paymentAmountError" class="mt-1 text-sm text-red-600 dark:text-red-400">
                {{ paymentAmountError }}
              </p>
            </div>

            <!-- Payment Method -->
            <div>
              <label class="block text-sm font-medium text-slate-900 dark:text-white mb-2">
                {{ t('patients.details.balance.payment.method') }}
              </label>
              <USelect
                v-model="paymentMethod"
                :options="paymentMethods"
                :placeholder="t('patients.details.balance.payment.methodPlaceholder')"
                size="lg"
                :disabled="isSubmittingPayment"
              />
            </div>

            <!-- Payment Date -->
            <div>
              <label class="block text-sm font-medium text-slate-900 dark:text-white mb-2">
                {{ t('patients.details.balance.payment.date') }}
              </label>
              <UInput
                v-model="paymentDate"
                type="date"
                :max="new Date().toISOString().split('T')[0]"
                size="lg"
                :disabled="isSubmittingPayment"
                icon="i-lucide-calendar"
              />
              <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">
                {{ t('patients.details.balance.payment.dateHint') }}
              </p>
            </div>

            <!-- Payment Notes -->
            <div>
              <label class="block text-sm font-medium text-slate-900 dark:text-white mb-2">
                {{ t('patients.details.balance.payment.notes') }}
                <span class="text-xs text-slate-400 font-normal ml-1">{{ t('common.optional') }}</span>
              </label>
              <UTextarea
                v-model="paymentNotes"
                :placeholder="t('patients.details.balance.payment.notesPlaceholder')"
                rows="3"
                :disabled="isSubmittingPayment"
              />
            </div>
          </div>
        </div>

        <template #footer>
          <div class="flex justify-between items-center">
            <UButton
              v-if="selectedPlan"
              color="gray"
              variant="ghost"
              icon="i-lucide-arrow-left"
              @click="selectedPlan = null"
            >
              {{ t('patients.details.balance.payment.backToPlanSelection') }}
            </UButton>
            <div v-else></div>
            
            <div class="flex gap-2">
              <UButton
                color="gray"
                variant="ghost"
                @click="closePaymentModal"
              >
                {{ t('common.actions.cancel') }}
              </UButton>
              <UButton
                v-if="selectedPlan"
                color="emerald"
                icon="i-lucide-credit-card"
                :disabled="!paymentAmount || !paymentMethod || !paymentDate || isSubmittingPayment"
                :loading="isSubmittingPayment"
                @click="submitPayment"
              >
                {{ t('patients.details.balance.payment.recordPayment') }}
              </UButton>
            </div>
          </div>
        </template>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { TreatmentPlan, TreatmentPlanStatus } from '~/composables/useTreatmentPlans'
import { usePatientBalance, type PatientBalanceItem } from '~/composables/usePatientBalance'
import { usePatientBalanceWithClinicSettings } from '../composables/usePatientBalance'
import { useClinicSettings } from '../composables/useClinicSettings'
import { useAdminApi } from '../composables/useAdminApi'

interface Props {
  treatmentPlans: TreatmentPlan[]
  loading?: boolean
  error?: Error | null
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  error: null
})

const emit = defineEmits<{
  retry: []
}>()

const { t, locale } = useI18n()
const showBreakdownModal = ref(false)
const showPaymentModal = ref(false)
const selectedPlan = ref<PatientBalanceItem | null>(null)
const paymentAmount = ref('')
const paymentMethod = ref('')
const paymentNotes = ref('')
const paymentDate = ref('')
const isSubmittingPayment = ref(false)
const paymentAmountError = ref('')

// Use the patient balance composable with clinic settings
const treatmentPlansRef = computed(() => props.treatmentPlans)
const { settings } = useClinicSettings()
const {
  balanceSummary,
  activeBalances,
  totalBalance,
  hasOutstandingBalance,
  hasActiveBalances,
  balanceCount,
  balanceCategory,
  formatTotalBalance,
  formatBalance,
  getBalanceColor,
  getBalanceGradient
} = usePatientBalanceWithClinicSettings(treatmentPlansRef, settings.value)

// Currency formatting (fallback for individual balances)
const formatCurrency = (amount: number | null | undefined, currency: string | undefined) => {
  return formatBalance(amount, currency)
}

const formattedTotalBalance = computed(() => {
  return formatTotalBalance()
})

// Visual styling based on balance category
const balanceTextColor = computed(() => {
  const colorMap = {
    none: 'text-green-600 dark:text-green-400',
    low: 'text-blue-600 dark:text-blue-400',
    medium: 'text-amber-600 dark:text-amber-400',
    high: 'text-red-600 dark:text-red-400'
  }
  return colorMap[balanceCategory.value] || 'text-gray-600 dark:text-gray-400'
})

const headerGradientClass = computed(() => {
  const baseClass = 'px-6 py-4'
  const gradient = getBalanceGradient()
  return `${baseClass} bg-gradient-to-r ${gradient}`
})

const balanceBadgeColor = computed(() => {
  return getBalanceColor()
})

const balanceDescription = computed(() => {
  if (!hasOutstandingBalance.value) {
    return t('patients.details.balance.descriptions.noBalance')
  }
  if (balanceCount.value === 1) {
    return t('patients.details.balance.descriptions.singlePlan')
  }
  return t('patients.details.balance.descriptions.multiplePlans', { count: balanceCount.value })
})

// Payment methods
const paymentMethods = computed(() => [
  { label: t('patients.details.balance.payment.methods.cash'), value: 'CASH' },
  { label: t('patients.details.balance.payment.methods.card'), value: 'CARD' },
  { label: t('patients.details.balance.payment.methods.bankTransfer'), value: 'BANK_TRANSFER' },
  { label: t('patients.details.balance.payment.methods.check'), value: 'CHECK' },
  { label: t('patients.details.balance.payment.methods.other'), value: 'OTHER' }
])

// Treatment plan status helpers
const getPlanStatusColor = (status?: TreatmentPlanStatus | string) => {
  switch (status) {
    case 'PLANNED': return 'blue'
    case 'IN_PROGRESS': return 'amber'
    case 'COMPLETED': return 'green'
    case 'CANCELLED': return 'red'
    default: return 'gray'
  }
}

const planStatusLabels = computed<Record<TreatmentPlanStatus, string>>(() => ({
  PLANNED: t('treatmentPlans.common.status.planned'),
  IN_PROGRESS: t('treatmentPlans.common.status.inProgress'),
  COMPLETED: t('treatmentPlans.common.status.completed'),
  CANCELLED: t('treatmentPlans.common.status.cancelled')
}))

const getPlanStatusLabel = (status?: TreatmentPlanStatus | string) => {
  if (!status) return ''
  return planStatusLabels.value[status as TreatmentPlanStatus] ?? status
}

// Modal interaction methods
const closeModal = () => {
  showBreakdownModal.value = false
}

// Payment handling methods
const selectPlanForPayment = (plan: PatientBalanceItem) => {
  selectedPlan.value = plan
  paymentAmount.value = ''
  paymentMethod.value = ''
  paymentNotes.value = ''
  paymentDate.value = new Date().toISOString().split('T')[0]
  paymentAmountError.value = ''
}

const handleRecordPlanPayment = (plan: PatientBalanceItem) => {
  selectedPlan.value = plan
  paymentAmount.value = ''
  paymentMethod.value = ''
  paymentNotes.value = ''
  paymentDate.value = new Date().toISOString().split('T')[0]
  paymentAmountError.value = ''
  showPaymentModal.value = true
  showBreakdownModal.value = false
}

const closePaymentModal = () => {
  showPaymentModal.value = false
  selectedPlan.value = null
  paymentAmount.value = ''
  paymentMethod.value = ''
  paymentNotes.value = ''
  paymentDate.value = ''
  paymentAmountError.value = ''
  isSubmittingPayment.value = false
}

// Auto-select plan if only one has outstanding balance
const autoSelectPlanIfSingle = () => {
  if (activeBalances.value.length === 1 && !selectedPlan.value) {
    selectPlanForPayment(activeBalances.value[0])
  }
}

// Watch for modal opening to auto-select
watch(showPaymentModal, (isOpen) => {
  if (isOpen) {
    autoSelectPlanIfSingle()
  }
})

// Validate payment amount
const validatePaymentAmount = () => {
  paymentAmountError.value = ''

  if (!paymentAmount.value) {
    paymentAmountError.value = t('patients.details.balance.payment.validation.amountRequired')
    return false
  }

  const amount = parseFloat(paymentAmount.value)

  if (isNaN(amount) || amount <= 0) {
    paymentAmountError.value = t('patients.details.balance.payment.validation.amountPositive')
    return false
  }

  if (selectedPlan.value && amount > selectedPlan.value.remainingBalance) {
    paymentAmountError.value = t('patients.details.balance.payment.validation.amountExceedsBalance', {
      max: formatBalance(selectedPlan.value.remainingBalance, selectedPlan.value.currency)
    })
    return false
  }

  return true
}

// Quick action: Pay full remaining balance
const payFullBalance = () => {
  if (selectedPlan.value) {
    paymentAmount.value = selectedPlan.value.remainingBalance.toString()
    paymentAmountError.value = ''
  }
}

const submitPayment = async () => {
  // Validate all required fields
  if (!selectedPlan.value || !paymentAmount.value || !paymentMethod.value || !paymentDate.value) {
    return
  }

  // Validate payment amount
  if (!validatePaymentAmount()) {
    return
  }

  // Prevent double submission
  if (isSubmittingPayment.value) {
    return
  }

  try {
    isSubmittingPayment.value = true
    const { request } = useAdminApi()

    // Convert date to ISO string with time
    const paymentDateTime = new Date(paymentDate.value).toISOString()

    const paymentData = {
      amount: parseFloat(paymentAmount.value),
      paymentMethod: paymentMethod.value,
      paymentDate: paymentDateTime,
      transactionReference: null,
      notes: paymentNotes.value.trim() || null
    }

    // Make the API call to record the payment
    await request(`/treatment-plans/${selectedPlan.value.id}/payments`, {
      method: 'POST',
      body: paymentData
    })

    // Show success toast
    const toast = useToast()
    toast.add({
      title: t('patients.details.balance.payment.success.title'),
      description: t('patients.details.balance.payment.success.description', {
        amount: formatCurrency(parseFloat(paymentAmount.value), selectedPlan.value.currency),
        plan: selectedPlan.value.treatmentTypeName
      }),
      color: 'green',
      icon: 'i-lucide-check-circle'
    })

    // Close modal first
    closePaymentModal()

    // Clear Nuxt data cache to force a fresh fetch
    const route = useRoute()
    const patientId = Number(route.params.id)
    await clearNuxtData(`admin-patient-${patientId}-treatment-plans`)

    // Refresh the treatment plans data
    emit('retry')
  } catch (error: any) {
    // Show error toast
    const toast = useToast()
    toast.add({
      title: t('patients.details.balance.payment.error.title'),
      description: error?.data?.message ?? error?.message ?? t('patients.details.balance.payment.error.description'),
      color: 'red',
      icon: 'i-lucide-alert-circle'
    })
  } finally {
    isSubmittingPayment.value = false
  }
}

// Keyboard navigation
const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Escape') {
    closeModal()
  }
}
</script>