import { ref, computed, watch } from 'vue'
import type { Ref, ComputedRef } from 'vue'
import { parseDateValue } from '../utils/dateUtils'
import { validateDateRange, formatDateForApi, type DateRangeValidationResult, type DateRange } from '../utils/dateRangeUtils'

export interface DateRangePreset {
  label: string
  value: DateRange
  key: string
}

export interface DateRangeFilterComposable {
  dateRange: Ref<DateRange>
  setDateRange: (range: DateRange) => void
  resetToDefault: () => void
  getApiParams: () => URLSearchParams
  presets: ComputedRef<DateRangePreset[]>
  isValidRange: ComputedRef<boolean>
  rangeError: ComputedRef<string | null>
}

const SESSION_STORAGE_KEY = 'reports-date-range'

// Utility functions for date operations
const parseApiDate = (dateString: string): Date => {
  const parsed = parseDateValue(dateString + 'T00:00:00.000Z')
  return parsed || new Date(dateString + 'T00:00:00.000Z')
}

const getDefaultDateRange = (): DateRange => {
  const now = new Date()
  const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
  const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0)
  
  return {
    startDate: formatDateForApi(startOfMonth),
    endDate: formatDateForApi(endOfMonth)
  }
}

const getPresetRanges = (): DateRangePreset[] => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  
  // Helper function to get start of week (Monday)
  const getStartOfWeek = (date: Date): Date => {
    const day = date.getDay()
    const diff = date.getDate() - day + (day === 0 ? -6 : 1) // Adjust when day is Sunday
    return new Date(date.setDate(diff))
  }
  
  // Helper function to get end of week (Sunday)
  const getEndOfWeek = (date: Date): Date => {
    const startOfWeek = getStartOfWeek(new Date(date))
    return new Date(startOfWeek.getTime() + 6 * 24 * 60 * 60 * 1000)
  }

  return [
    {
      label: 'Today',
      key: 'today',
      value: {
        startDate: formatDateForApi(today),
        endDate: formatDateForApi(today)
      }
    },
    {
      label: 'This Week',
      key: 'thisWeek',
      value: {
        startDate: formatDateForApi(getStartOfWeek(new Date(now))),
        endDate: formatDateForApi(getEndOfWeek(new Date(now)))
      }
    },
    {
      label: 'This Month',
      key: 'thisMonth',
      value: {
        startDate: formatDateForApi(new Date(now.getFullYear(), now.getMonth(), 1)),
        endDate: formatDateForApi(new Date(now.getFullYear(), now.getMonth() + 1, 0))
      }
    },
    {
      label: 'Last Month',
      key: 'lastMonth',
      value: {
        startDate: formatDateForApi(new Date(now.getFullYear(), now.getMonth() - 1, 1)),
        endDate: formatDateForApi(new Date(now.getFullYear(), now.getMonth(), 0))
      }
    },
    {
      label: 'Last 3 Months',
      key: 'last3Months',
      value: {
        startDate: formatDateForApi(new Date(now.getFullYear(), now.getMonth() - 3, 1)),
        endDate: formatDateForApi(new Date(now.getFullYear(), now.getMonth() + 1, 0))
      }
    },
    {
      label: 'Last 6 Months',
      key: 'last6Months',
      value: {
        startDate: formatDateForApi(new Date(now.getFullYear(), now.getMonth() - 6, 1)),
        endDate: formatDateForApi(new Date(now.getFullYear(), now.getMonth() + 1, 0))
      }
    },
    {
      label: 'This Year',
      key: 'thisYear',
      value: {
        startDate: formatDateForApi(new Date(now.getFullYear(), 0, 1)),
        endDate: formatDateForApi(new Date(now.getFullYear(), 11, 31))
      }
    }
  ]
}

// Session persistence functions
const persistDateRange = (range: DateRange): void => {
  if (typeof window !== 'undefined') {
    try {
      sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(range))
    } catch (error) {
      console.warn('Failed to persist date range to session storage:', error)
    }
  }
}

const restoreDateRange = (): DateRange => {
  if (typeof window !== 'undefined') {
    try {
      const stored = sessionStorage.getItem(SESSION_STORAGE_KEY)
      if (stored) {
        const parsed = JSON.parse(stored) as DateRange
        // Validate the restored data
        if (parsed.startDate && parsed.endDate) {
          return parsed
        }
      }
    } catch (error) {
      console.warn('Failed to restore date range from session storage:', error)
    }
  }
  return getDefaultDateRange()
}

export const useDateRangeFilter = (): DateRangeFilterComposable => {
  // Initialize with restored or default range
  const dateRange = ref<DateRange>(restoreDateRange())

  // Computed properties for validation
  const validationResult = computed((): DateRangeValidationResult => {
    return validateDateRange(dateRange.value)
  })

  const isValidRange = computed(() => validationResult.value.isValid)
  const rangeError = computed(() => validationResult.value.error)

  const presets = computed(() => getPresetRanges())

  // Watch for changes and persist to session storage
  watch(dateRange, (newRange) => {
    persistDateRange(newRange)
  }, { deep: true })

  const setDateRange = (range: DateRange): void => {
    dateRange.value = { ...range }
  }

  const resetToDefault = (): void => {
    dateRange.value = getDefaultDateRange()
  }

  const getApiParams = (): URLSearchParams => {
    const params = new URLSearchParams()
    
    if (dateRange.value.startDate) {
      params.append('startDate', dateRange.value.startDate)
    }
    
    if (dateRange.value.endDate) {
      params.append('endDate', dateRange.value.endDate)
    }
    
    return params
  }

  return {
    dateRange,
    setDateRange,
    resetToDefault,
    getApiParams,
    presets,
    isValidRange,
    rangeError
  }
}

// Export utility functions for use in other components
export {
  parseApiDate,
  getDefaultDateRange,
  getPresetRanges
}