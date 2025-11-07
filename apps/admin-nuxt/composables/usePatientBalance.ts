import { computed, readonly, ref, type ComputedRef } from 'vue'
import { useI18n } from 'vue-i18n'
import type { TreatmentPlan, TreatmentPlanStatus } from '~/composables/useTreatmentPlans'
import { formatCurrencyValue, areCurrenciesEqual } from '~/utils/currencyUtils'
import { useClinicSettings } from './useClinicSettings'

export interface PatientBalanceItem {
  id: number
  treatmentTypeName: string
  doctorName: string
  remainingBalance: number
  currency: string
  status: TreatmentPlanStatus
  convertedRemainingBalance?: number
  convertedCurrency?: string
}

export interface PatientBalanceSummary {
  totalBalance: number
  currency: string
  convertedTotalBalance?: number
  convertedCurrency?: string
  activeBalances: PatientBalanceItem[]
  hasOutstandingBalance: boolean
  hasActiveBalances: boolean
  balanceCount: number
}

export interface UsePatientBalanceOptions {
  /**
   * Primary currency to use for calculations (defaults to clinic currency)
   */
  primaryCurrency?: string
  /**
   * Whether to include only active treatment plans
   */
  activeOnly?: boolean
  /**
   * Minimum balance threshold to consider (defaults to 0.01)
   */
  minimumThreshold?: number
}

/**
 * Composable for calculating and managing patient balance information
 */
export function usePatientBalance(
  treatmentPlans: ComputedRef<TreatmentPlan[]> | TreatmentPlan[],
  options: UsePatientBalanceOptions = {}
) {
  const { locale } = useI18n()
  const { settings } = useClinicSettings()
  
  const {
    primaryCurrency = settings.value?.currency || 'SAR',
    activeOnly = true,
    minimumThreshold = 0.01
  } = options

  // Convert treatment plans to computed ref if it's not already
  const plans = computed(() => {
    return Array.isArray(treatmentPlans) ? treatmentPlans : treatmentPlans.value
  })

  // Filter treatment plans based on criteria
  const eligiblePlans = computed(() => {
    return plans.value.filter(plan => {
      // Filter by status if activeOnly is true
      if (activeOnly && !isActivePlan(plan.status)) {
        return false
      }

      // Filter by minimum balance threshold
      // IMPORTANT: Only check ORIGINAL balance, not converted balance
      // Backend validation uses original currency, so we can't accept payments
      // for plans with 0 original balance (even if converted balance exists)
      if (plan.remainingBalance < minimumThreshold) {
        return false
      }

      return true
    })
  })

  // Convert treatment plans to balance items (prioritize clinic currency)
  const activeBalances = computed<PatientBalanceItem[]>(() => {
    return eligiblePlans.value.map(plan => {
      // Use converted amount and currency if available, otherwise use original
      const effectiveAmount = plan.convertedRemainingBalance ?? plan.remainingBalance
      const effectiveCurrency = plan.convertedCurrency ?? plan.currency
      
      return {
        id: plan.id,
        treatmentTypeName: plan.treatmentTypeName,
        doctorName: plan.doctorName,
        remainingBalance: effectiveAmount,
        currency: effectiveCurrency,
        status: plan.status,
        convertedRemainingBalance: plan.convertedRemainingBalance,
        convertedCurrency: plan.convertedCurrency
      }
    })
  })

  // Calculate total balance in original currencies
  const totalBalance = computed(() => {
    return eligiblePlans.value.reduce((sum, plan) => sum + plan.remainingBalance, 0)
  })

  // Calculate total balance in converted currency
  const convertedTotalBalance = computed(() => {
    return eligiblePlans.value.reduce((sum, plan) => {
      // Use converted balance if available and different currency
      if (plan.convertedRemainingBalance !== undefined && 
          !areCurrenciesEqual(plan.currency, plan.convertedCurrency)) {
        return sum + plan.convertedRemainingBalance
      }
      // Otherwise use original balance
      return sum + plan.remainingBalance
    }, 0)
  })

  // Determine the primary currency to use (always prefer clinic currency)
  const effectiveCurrency = computed(() => {
    // Always use the primary currency (clinic currency) when available
    return primaryCurrency
  })

  // Determine which total balance to use (always prefer converted to clinic currency)
  const effectiveTotalBalance = computed(() => {
    // Always use converted total balance (in clinic currency)
    return convertedTotalBalance.value
  })

  // Balance summary
  const balanceSummary = computed<PatientBalanceSummary>(() => ({
    totalBalance: effectiveTotalBalance.value,
    currency: effectiveCurrency.value,
    convertedTotalBalance: convertedTotalBalance.value !== totalBalance.value 
      ? convertedTotalBalance.value 
      : undefined,
    convertedCurrency: effectiveCurrency.value !== primaryCurrency 
      ? effectiveCurrency.value 
      : undefined,
    activeBalances: activeBalances.value,
    hasOutstandingBalance: effectiveTotalBalance.value > minimumThreshold,
    hasActiveBalances: activeBalances.value.length > 0,
    balanceCount: activeBalances.value.length
  }))

  // Formatting utilities
  const formatBalance = (
    amount: number | null | undefined, 
    currency?: string,
    options?: Intl.NumberFormatOptions
  ) => {
    return formatCurrencyValue(
      amount, 
      currency || effectiveCurrency.value, 
      locale.value,
      options
    )
  }

  const formatTotalBalance = (options?: Intl.NumberFormatOptions) => {
    return formatBalance(effectiveTotalBalance.value, effectiveCurrency.value, options)
  }

  // Balance categorization
  const getBalanceCategory = (amount: number) => {
    if (amount <= minimumThreshold) return 'none'
    if (amount <= 100) return 'low'
    if (amount <= 500) return 'medium'
    return 'high'
  }

  const balanceCategory = computed(() => {
    return getBalanceCategory(effectiveTotalBalance.value)
  })

  // Visual styling helpers
  const getBalanceColor = (category?: string) => {
    const cat = category || balanceCategory.value
    switch (cat) {
      case 'none': return 'green'
      case 'low': return 'blue'
      case 'medium': return 'amber'
      case 'high': return 'red'
      default: return 'gray'
    }
  }

  const getBalanceGradient = (category?: string) => {
    const cat = category || balanceCategory.value
    switch (cat) {
      case 'none': return 'from-green-500 to-emerald-600'
      case 'low': return 'from-blue-500 to-indigo-600'
      case 'medium': return 'from-amber-500 to-orange-600'
      case 'high': return 'from-red-500 to-rose-600'
      default: return 'from-gray-500 to-slate-600'
    }
  }

  // Utility functions
  const isActivePlan = (status: TreatmentPlanStatus): boolean => {
    return status === 'PLANNED' || status === 'IN_PROGRESS'
  }

  const getBalancesByStatus = () => {
    const grouped = activeBalances.value.reduce((acc, balance) => {
      if (!acc[balance.status]) {
        acc[balance.status] = []
      }
      acc[balance.status].push(balance)
      return acc
    }, {} as Record<TreatmentPlanStatus, PatientBalanceItem[]>)
    
    return grouped
  }

  const getBalancesByCurrency = () => {
    const grouped = activeBalances.value.reduce((acc, balance) => {
      if (!acc[balance.currency]) {
        acc[balance.currency] = []
      }
      acc[balance.currency].push(balance)
      return acc
    }, {} as Record<string, PatientBalanceItem[]>)
    
    return grouped
  }

  // Error handling
  const validateBalanceData = () => {
    const errors: string[] = []
    
    plans.value.forEach((plan, index) => {
      if (typeof plan.remainingBalance !== 'number' || plan.remainingBalance < 0) {
        errors.push(`Invalid remaining balance for plan ${index + 1}: ${plan.remainingBalance}`)
      }
      
      if (!plan.currency || typeof plan.currency !== 'string') {
        errors.push(`Invalid currency for plan ${index + 1}: ${plan.currency}`)
      }
    })
    
    return errors
  }

  return {
    // Core data
    balanceSummary: readonly(balanceSummary),
    activeBalances: readonly(activeBalances),
    
    // Calculated values
    totalBalance: readonly(computed(() => balanceSummary.value.totalBalance)),
    effectiveCurrency: readonly(effectiveCurrency),
    hasOutstandingBalance: readonly(computed(() => balanceSummary.value.hasOutstandingBalance)),
    hasActiveBalances: readonly(computed(() => balanceSummary.value.hasActiveBalances)),
    balanceCount: readonly(computed(() => balanceSummary.value.balanceCount)),
    balanceCategory: readonly(balanceCategory),
    
    // Formatting functions
    formatBalance,
    formatTotalBalance,
    
    // Visual helpers
    getBalanceColor,
    getBalanceGradient,
    
    // Utility functions
    getBalanceCategory,
    getBalancesByStatus,
    getBalancesByCurrency,
    validateBalanceData,
    
    // Configuration
    options: readonly(ref(options))
  }
}

/**
 * Helper function to create a patient balance composable with default clinic settings
 */
export function usePatientBalanceWithClinicSettings(
  treatmentPlans: ComputedRef<TreatmentPlan[]> | TreatmentPlan[],
  clinicSettings?: any
) {
  const options: UsePatientBalanceOptions = {
    primaryCurrency: clinicSettings?.currency || 'SAR',
    activeOnly: true,
    minimumThreshold: 0.01
  }
  
  return usePatientBalance(treatmentPlans, options)
}