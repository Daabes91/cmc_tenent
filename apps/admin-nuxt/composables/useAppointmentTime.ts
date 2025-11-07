import { computed, type Ref } from 'vue'
import { 
  normalizeAppointmentTime, 
  convertClinicTimeToUTC, 
  convertUTCToClinicTime,
  formatDateTimeInClinicTimezone 
} from '../utils/dateUtils'
import { useClinicTimezone } from './useClinicTimezone'

/**
 * Composable for consistent appointment time handling across the admin dashboard
 * Centralizes appointment time formatting, validation, and conversion logic
 */
export function useAppointmentTime() {
  const { timezone, abbreviation } = useClinicTimezone()

  /**
   * Format appointment time for display with consistent formatting
   * @param input - Appointment time input (Unix timestamp, ISO string, Date, or null)
   * @returns Formatted appointment time string
   */
  const formatAppointmentTime = (input: string | number | Date | null | undefined): string => {
    if (input === null || input === undefined) {
      return 'Not scheduled'
    }

    try {
      const date = normalizeAppointmentTime(input)
      if (!date) {
        console.warn('Failed to normalize appointment time for display:', {
          input,
          inputType: typeof input,
          timezone: timezone.value,
          abbreviation: abbreviation.value
        })
        return 'Invalid date'
      }

      // Use consistent format: "Jan 15, 2024, 2:30 PM EET"
      const formatted = formatDateTimeInClinicTimezone(
        date,
        timezone.value,
        abbreviation.value
      )

      // Validate the formatted result
      if (!formatted || formatted === 'Invalid date' || formatted === 'Not set') {
        console.warn('Invalid formatted result for appointment time:', {
          input,
          date: date.toISOString(),
          formatted,
          timezone: timezone.value
        })
        return 'Invalid date'
      }

      console.debug('Formatted appointment time for display:', input, '→', formatted)
      return formatted
    } catch (error) {
      console.error('Error formatting appointment time for display:', {
        error: error instanceof Error ? error.message : error,
        input,
        inputType: typeof input,
        timezone: timezone.value,
        stack: error instanceof Error ? error.stack : undefined
      })
      return 'Invalid date'
    }
  }

  /**
   * Format appointment time for datetime-local input field
   * Converts UTC/timestamp to clinic timezone and formats for HTML input
   * @param input - Appointment time input (Unix timestamp, ISO string, Date, or null)
   * @returns Formatted string for datetime-local input (YYYY-MM-DDTHH:mm)
   */
  const formatForInput = (input: string | number | Date | null | undefined): string => {
    if (input === null || input === undefined) {
      return ''
    }

    try {
      const date = normalizeAppointmentTime(input)
      if (!date) {
        console.warn('Failed to normalize appointment time for input:', input)
        return ''
      }

      // For datetime-local input, we need to show the time as it would appear in clinic timezone
      // The date object already represents the correct UTC time, so we can use it directly
      // but we need to format it as if it were in the clinic timezone
      
      // Create a new date that represents the clinic time
      const utcTime = date.getTime()
      const clinicDate = new Date(utcTime)
      
      // Adjust for timezone offset to get the local representation
      try {
        const tempFormatter = new Intl.DateTimeFormat('en-CA', {
          timeZone: timezone.value,
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          hour12: false
        })
        
        const parts = tempFormatter.formatToParts(clinicDate)
        const year = parts.find(p => p.type === 'year')?.value || '2024'
        const month = parts.find(p => p.type === 'month')?.value || '01'
        const day = parts.find(p => p.type === 'day')?.value || '01'
        const hour = parts.find(p => p.type === 'hour')?.value || '00'
        const minute = parts.find(p => p.type === 'minute')?.value || '00'
        
        const formatted = `${year}-${month}-${day}T${hour}:${minute}`
        console.debug('Formatted appointment time for input:', input, '→', formatted)
        return formatted
      } catch (timezoneError) {
        console.warn('Failed to format with timezone, using UTC:', timezoneError)
        // Fallback to UTC formatting
        const year = clinicDate.getFullYear()
        const month = String(clinicDate.getMonth() + 1).padStart(2, '0')
        const day = String(clinicDate.getDate()).padStart(2, '0')
        const hours = String(clinicDate.getHours()).padStart(2, '0')
        const minutes = String(clinicDate.getMinutes()).padStart(2, '0')
        
        const formatted = `${year}-${month}-${day}T${hours}:${minutes}`
        console.debug('Formatted appointment time for input (UTC fallback):', input, '→', formatted)
        return formatted
      }


    } catch (error) {
      console.error('Error formatting appointment time for input:', input, error)
      return ''
    }
  }

  /**
   * Parse datetime-local input for API submission
   * Converts clinic timezone input to UTC ISO string
   * @param input - Datetime-local input string (YYYY-MM-DDTHH:mm)
   * @returns UTC ISO string for API submission
   */
  const parseInputForAPI = (input: string): string => {
    if (!input || !input.trim()) {
      throw new Error('Empty datetime input cannot be parsed for API')
    }

    try {
      // Convert clinic timezone input to UTC
      const utcString = convertClinicTimeToUTC(input, timezone.value)
      console.debug('Parsed appointment time for API:', input, '→', utcString)
      return utcString
    } catch (error) {
      console.error('Error parsing appointment time for API:', input, error)
      throw new Error(`Invalid appointment time format: ${input}`)
    }
  }

  /**
   * Validate appointment time input
   * @param input - Datetime-local input string
   * @returns True if valid, false otherwise
   */
  const validateAppointmentTime = (input: string): boolean => {
    if (!input || !input.trim()) {
      console.debug('Appointment time validation failed: empty input')
      return false
    }

    try {
      // Check if it matches datetime-local format
      const datetimeLocalPattern = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/
      if (!datetimeLocalPattern.test(input)) {
        console.warn('Invalid datetime-local format:', {
          input,
          expectedFormat: 'YYYY-MM-DDTHH:mm',
          actualLength: input.length
        })
        return false
      }

      // Try to parse it
      const date = new Date(input)
      if (Number.isNaN(date.getTime())) {
        console.warn('Invalid date value:', {
          input,
          parsedTime: date.getTime(),
          dateString: date.toString()
        })
        return false
      }

      // Check if it's a reasonable appointment time (not too far in past/future)
      const now = new Date()
      const oneYearAgo = new Date(now.getFullYear() - 1, now.getMonth(), now.getDate())
      const twoYearsFromNow = new Date(now.getFullYear() + 2, now.getMonth(), now.getDate())

      if (date < oneYearAgo || date > twoYearsFromNow) {
        console.warn('Appointment time outside reasonable range:', {
          input,
          parsedDate: date.toISOString(),
          rangeStart: oneYearAgo.toISOString(),
          rangeEnd: twoYearsFromNow.toISOString(),
          isTooEarly: date < oneYearAgo,
          isTooLate: date > twoYearsFromNow
        })
        return false
      }

      // Additional validation: check for obviously invalid dates
      const year = date.getFullYear()
      const month = date.getMonth() + 1
      const day = date.getDate()
      const hours = date.getHours()
      const minutes = date.getMinutes()

      if (year < 1900 || year > 2200) {
        console.warn('Invalid year in appointment time:', { input, year })
        return false
      }

      if (month < 1 || month > 12) {
        console.warn('Invalid month in appointment time:', { input, month })
        return false
      }

      if (day < 1 || day > 31) {
        console.warn('Invalid day in appointment time:', { input, day })
        return false
      }

      if (hours < 0 || hours > 23) {
        console.warn('Invalid hours in appointment time:', { input, hours })
        return false
      }

      if (minutes < 0 || minutes > 59) {
        console.warn('Invalid minutes in appointment time:', { input, minutes })
        return false
      }

      console.debug('Validated appointment time:', input, '→', 'valid', {
        parsedDate: date.toISOString(),
        timezone: timezone.value
      })
      return true
    } catch (error) {
      console.error('Error validating appointment time:', {
        error: error instanceof Error ? error.message : error,
        input,
        inputLength: input.length,
        stack: error instanceof Error ? error.stack : undefined
      })
      return false
    }
  }

  /**
   * Format appointment time for list display (shorter format)
   * @param input - Appointment time input
   * @returns Formatted time string for list views
   */
  const formatForList = (input: string | number | Date | null | undefined): string => {
    if (input === null || input === undefined) {
      return 'Not scheduled'
    }

    try {
      const date = normalizeAppointmentTime(input)
      if (!date) {
        return 'Invalid date'
      }

      // Shorter format for list views: "Jan 15, 2:30 PM"
      const formatted = date.toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        timeZone: timezone.value
      })

      return `${formatted} ${abbreviation.value}`
    } catch (error) {
      console.error('Error formatting appointment time for list:', input, error)
      return 'Invalid date'
    }
  }

  /**
   * Get appointment duration display
   * @param startTime - Appointment start time
   * @param durationMinutes - Duration in minutes
   * @returns Formatted duration string
   */
  const formatDuration = (
    startTime: string | number | Date | null | undefined,
    durationMinutes: number | null | undefined
  ): string => {
    const formattedStart = formatAppointmentTime(startTime)
    
    if (!durationMinutes || durationMinutes <= 0) {
      return formattedStart
    }

    const hours = Math.floor(durationMinutes / 60)
    const minutes = durationMinutes % 60
    
    let durationText = ''
    if (hours > 0) {
      durationText += `${hours}h`
      if (minutes > 0) {
        durationText += ` ${minutes}m`
      }
    } else {
      durationText = `${minutes}m`
    }

    return `${formattedStart} (${durationText})`
  }

  /**
   * Check if appointment time is in the past
   * @param input - Appointment time input
   * @returns True if appointment is in the past
   */
  const isInPast = (input: string | number | Date | null | undefined): boolean => {
    if (input === null || input === undefined) {
      return false
    }

    try {
      const date = normalizeAppointmentTime(input)
      if (!date) {
        return false
      }

      return date.getTime() < Date.now()
    } catch (error) {
      console.error('Error checking if appointment is in past:', input, error)
      return false
    }
  }

  /**
   * Get relative time description (e.g., "in 2 hours", "3 days ago")
   * @param input - Appointment time input
   * @returns Relative time string
   */
  const getRelativeTime = (input: string | number | Date | null | undefined): string => {
    if (input === null || input === undefined) {
      return 'Not scheduled'
    }

    try {
      const date = normalizeAppointmentTime(input)
      if (!date) {
        console.warn('Failed to normalize appointment time for relative time:', {
          input,
          inputType: typeof input
        })
        return 'Invalid date'
      }

      const now = new Date()
      const diffMs = date.getTime() - now.getTime()
      
      // Validate the time difference
      if (!Number.isFinite(diffMs)) {
        console.warn('Invalid time difference calculated for relative time:', {
          input,
          dateTime: date.getTime(),
          nowTime: now.getTime(),
          diffMs
        })
        return 'Unknown'
      }

      const diffMinutes = Math.round(diffMs / (1000 * 60))
      const diffHours = Math.round(diffMs / (1000 * 60 * 60))
      const diffDays = Math.round(diffMs / (1000 * 60 * 60 * 24))

      // Handle extreme time differences
      if (Math.abs(diffDays) > 365) {
        console.warn('Extreme time difference detected:', {
          input,
          diffDays,
          date: date.toISOString(),
          now: now.toISOString()
        })
        return diffDays > 0 ? 'Far in the future' : 'Long ago'
      }

      if (Math.abs(diffMinutes) < 60) {
        if (diffMinutes > 0) {
          return `in ${diffMinutes} minute${diffMinutes !== 1 ? 's' : ''}`
        } else {
          return `${Math.abs(diffMinutes)} minute${Math.abs(diffMinutes) !== 1 ? 's' : ''} ago`
        }
      } else if (Math.abs(diffHours) < 24) {
        if (diffHours > 0) {
          return `in ${diffHours} hour${diffHours !== 1 ? 's' : ''}`
        } else {
          return `${Math.abs(diffHours)} hour${Math.abs(diffHours) !== 1 ? 's' : ''} ago`
        }
      } else {
        if (diffDays > 0) {
          return `in ${diffDays} day${diffDays !== 1 ? 's' : ''}`
        } else {
          return `${Math.abs(diffDays)} day${Math.abs(diffDays) !== 1 ? 's' : ''} ago`
        }
      }
    } catch (error) {
      console.error('Error getting relative time:', {
        error: error instanceof Error ? error.message : error,
        input,
        inputType: typeof input,
        stack: error instanceof Error ? error.stack : undefined
      })
      return 'Unknown'
    }
  }

  return {
    // Core functions
    formatAppointmentTime,
    formatForInput,
    parseInputForAPI,
    validateAppointmentTime,
    
    // Additional utilities
    formatForList,
    formatDuration,
    isInPast,
    getRelativeTime,
    
    // Timezone info (reactive)
    timezone: computed(() => timezone.value),
    abbreviation: computed(() => abbreviation.value)
  }
}

/**
 * Type definitions for the appointment time composable
 */
export interface AppointmentTimeComposable {
  // Core functions
  formatAppointmentTime(input: string | number | Date | null | undefined): string
  formatForInput(input: string | number | Date | null | undefined): string
  parseInputForAPI(input: string): string
  validateAppointmentTime(input: string): boolean
  
  // Additional utilities
  formatForList(input: string | number | Date | null | undefined): string
  formatDuration(startTime: string | number | Date | null | undefined, durationMinutes: number | null | undefined): string
  isInPast(input: string | number | Date | null | undefined): boolean
  getRelativeTime(input: string | number | Date | null | undefined): string
  
  // Reactive timezone info
  timezone: Readonly<Ref<string>>
  abbreviation: Readonly<Ref<string>>
}