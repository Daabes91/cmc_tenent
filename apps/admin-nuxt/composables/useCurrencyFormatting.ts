import { computed } from 'vue'
import { 
  formatCurrencyValue, 
  prepareDualCurrencyData, 
  getCurrencyDisplayConfig,
  type DualCurrencyData 
} from '~/utils/currencyUtils'

/**
 * Composable for currency formatting with clinic settings integration
 */
export function useCurrencyFormatting() {
  const { settings } = useClinicSettings()
  
  const config = computed(() => getCurrencyDisplayConfig(settings.value))
  
  /**
   * Format a single currency value using clinic settings
   */
  const formatCurrency = (
    amount: number | null | undefined,
    currency?: string | null | undefined,
    options?: Intl.NumberFormatOptions
  ): string => {
    return formatCurrencyValue(
      amount,
      currency || config.value.clinicCurrency,
      config.value.locale,
      options
    )
  }
  
  /**
   * Format clinic currency specifically
   */
  const formatClinicCurrency = (
    amount: number | null | undefined,
    options?: Intl.NumberFormatOptions
  ): string => {
    return formatCurrency(amount, config.value.clinicCurrency, options)
  }
  
  /**
   * Prepare dual currency display data
   */
  const prepareDualCurrency = (
    convertedAmount: number | null | undefined,
    convertedCurrency: string | null | undefined,
    originalAmount: number | null | undefined,
    originalCurrency: string | null | undefined,
    alwaysShowOriginal: boolean = false
  ): DualCurrencyData => {
    return prepareDualCurrencyData(
      convertedAmount,
      convertedCurrency,
      originalAmount,
      originalCurrency,
      config.value.locale,
      alwaysShowOriginal
    )
  }
  
  /**
   * Check if we should show dual currency display
   */
  const shouldShowDual = (
    originalAmount: number | null | undefined,
    originalCurrency: string | null | undefined,
    convertedAmount: number | null | undefined,
    convertedCurrency: string | null | undefined
  ): boolean => {
    const data = prepareDualCurrency(
      convertedAmount,
      convertedCurrency,
      originalAmount,
      originalCurrency
    )
    return data.showSecondary
  }
  
  return {
    config,
    formatCurrency,
    formatClinicCurrency,
    prepareDualCurrency,
    shouldShowDual
  }
}