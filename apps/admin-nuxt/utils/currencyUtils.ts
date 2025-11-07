/**
 * Currency formatting utilities for multi-currency display
 */

/**
 * Normalize currency code to uppercase 3-letter format
 */
export function normalizeCurrencyCode(currency: string | null | undefined): string {
  if (!currency) return ''
  const trimmed = currency.trim().toUpperCase()
  return trimmed.length >= 3 ? trimmed.slice(0, 3) : trimmed
}

/**
 * Check if two currency codes are the same
 */
export function areCurrenciesEqual(
  currency1: string | null | undefined,
  currency2: string | null | undefined
): boolean {
  const normalized1 = normalizeCurrencyCode(currency1)
  const normalized2 = normalizeCurrencyCode(currency2)
  return normalized1 === normalized2 && normalized1 !== ''
}

/**
 * Determine if dual currency display should be shown
 */
export function shouldShowDualCurrency(
  originalAmount: number | null | undefined,
  originalCurrency: string | null | undefined,
  convertedAmount: number | null | undefined,
  convertedCurrency: string | null | undefined,
  alwaysShow: boolean = false
): boolean {
  // Always show if explicitly requested
  if (alwaysShow) return true
  
  // Don't show if no original amount
  if (originalAmount === null || originalAmount === undefined) return false
  
  // Don't show if no converted amount
  if (convertedAmount === null || convertedAmount === undefined) return false
  
  // Don't show if currencies are the same
  if (areCurrenciesEqual(originalCurrency, convertedCurrency)) return false
  
  return true
}

/**
 * Format currency value with fallback handling
 */
export function formatCurrencyValue(
  amount: number | null | undefined,
  currency: string | null | undefined,
  locale?: string,
  options?: Intl.NumberFormatOptions
): string {
  if (amount === null || amount === undefined) {
    return '—'
  }
  
  const numericAmount = Number(amount)
  if (!Number.isFinite(numericAmount)) {
    return '—'
  }
  
  const currencyCode = normalizeCurrencyCode(currency) || 'USD'
  const localeCode = locale || 'en-US'
  
  try {
    const formatOptions: Intl.NumberFormatOptions = {
      style: 'currency',
      currency: currencyCode,
      ...options
    }
    
    return new Intl.NumberFormat(localeCode, formatOptions).format(numericAmount)
  } catch (error) {
    // Fallback formatting if Intl.NumberFormat fails
    const decimals = options?.minimumFractionDigits ?? 2
    return `${numericAmount.toFixed(decimals)} ${currencyCode}`
  }
}

/**
 * Resolve currency symbol for a given currency code
 */
export function resolveCurrencySymbol(currencyCode: string): string {
  try {
    const formatter = new Intl.NumberFormat(undefined, {
      style: 'currency',
      currency: currencyCode,
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    })
    const parts = formatter.formatToParts(1)
    const symbol = parts.find(part => part.type === 'currency')?.value
    return symbol || currencyCode
  } catch {
    return currencyCode
  }
}

/**
 * Get currency display configuration based on clinic settings
 */
export interface CurrencyDisplayConfig {
  clinicCurrency: string
  locale: string
  showOriginalWhenSame: boolean
  primaryCurrencyEmphasis: boolean
}

export function getCurrencyDisplayConfig(
  clinicSettings?: any
): CurrencyDisplayConfig {
  return {
    clinicCurrency: clinicSettings?.currency || 'USD',
    locale: clinicSettings?.locale || 'en-US',
    showOriginalWhenSame: false,
    primaryCurrencyEmphasis: true
  }
}

/**
 * Format dual currency display data
 */
export interface DualCurrencyData {
  primary: {
    amount: number | null | undefined
    currency: string | null | undefined
    formatted: string
  }
  secondary?: {
    amount: number | null | undefined
    currency: string | null | undefined
    formatted: string
  }
  showSecondary: boolean
}

export function prepareDualCurrencyData(
  convertedAmount: number | null | undefined,
  convertedCurrency: string | null | undefined,
  originalAmount: number | null | undefined,
  originalCurrency: string | null | undefined,
  locale?: string,
  alwaysShowOriginal: boolean = false
): DualCurrencyData {
  const showSecondary = shouldShowDualCurrency(
    originalAmount,
    originalCurrency,
    convertedAmount,
    convertedCurrency,
    alwaysShowOriginal
  )
  
  const primary = {
    amount: convertedAmount,
    currency: convertedCurrency,
    formatted: formatCurrencyValue(convertedAmount, convertedCurrency, locale)
  }
  
  const secondary = showSecondary ? {
    amount: originalAmount,
    currency: originalCurrency,
    formatted: formatCurrencyValue(originalAmount, originalCurrency, locale)
  } : undefined
  
  return {
    primary,
    secondary,
    showSecondary
  }
}