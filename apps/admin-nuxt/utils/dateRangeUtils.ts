import { parseDateValue } from './dateUtils'

export interface DateRange {
  startDate: string | null
  endDate: string | null
}

export interface DateRangeValidationResult {
  isValid: boolean
  error: string | null
}

export const MAX_RANGE_DAYS = 730 // 2 years

/**
 * Validates a date range for common business rules
 * @param dateRange - The date range to validate
 * @returns Validation result with error message if invalid
 */
export function validateDateRange(dateRange: DateRange): DateRangeValidationResult {
  const { startDate, endDate } = dateRange
  
  if (!startDate || !endDate) {
    return {
      isValid: false,
      error: 'Both start and end dates are required'
    }
  }
  
  const start = parseDateValue(startDate)
  const end = parseDateValue(endDate)
  const now = new Date()
  
  if (!start || !end) {
    return {
      isValid: false,
      error: 'Invalid date format'
    }
  }
  
  if (end < start) {
    return {
      isValid: false,
      error: 'End date must be after start date'
    }
  }
  
  if (end > now) {
    return {
      isValid: false,
      error: 'End date cannot be in the future'
    }
  }
  
  const daysDiff = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
  if (daysDiff > MAX_RANGE_DAYS) {
    return {
      isValid: false,
      error: 'Date range cannot exceed 2 years'
    }
  }
  
  return {
    isValid: true,
    error: null
  }
}

/**
 * Calculates the number of days between two dates
 * @param startDate - Start date string
 * @param endDate - End date string
 * @returns Number of days between the dates
 */
export function getDateRangeDays(startDate: string, endDate: string): number {
  const start = parseDateValue(startDate)
  const end = parseDateValue(endDate)
  
  if (!start || !end) {
    return 0
  }
  
  return Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
}

/**
 * Formats a date for API consumption (YYYY-MM-DD)
 * @param date - Date object to format
 * @returns ISO date string (YYYY-MM-DD)
 */
export function formatDateForApi(date: Date): string {
  return date.toISOString().split('T')[0]
}

/**
 * Checks if a date range is within the last N days
 * @param dateRange - The date range to check
 * @param days - Number of days to check against
 * @returns True if the range is within the specified days
 */
export function isWithinLastDays(dateRange: DateRange, days: number): boolean {
  if (!dateRange.startDate) return false
  
  const start = parseDateValue(dateRange.startDate)
  const now = new Date()
  const daysAgo = new Date(now.getTime() - (days * 24 * 60 * 60 * 1000))
  
  return start ? start >= daysAgo : false
}