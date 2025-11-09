/**
 * Enhanced timestamp normalization
 * @param value - Numeric timestamp value
 * @returns Normalized timestamp in milliseconds
 */
const normalizeTimestamp = (value: number): number => {
  if (!Number.isFinite(value)) {
    console.warn('Invalid timestamp: not a finite number', value)
    return NaN
  }
  
  const abs = Math.abs(value)
  
  // Unix timestamps in seconds: 1000000000 (2001) to 4102444800 (2100)
  // Unix timestamps in milliseconds: 1000000000000 (2001) to 4102444800000 (2100)
  if (abs >= 1_000_000_000 && abs <= 4_102_444_800) {
    // Likely seconds - convert to milliseconds
    return value * 1000
  } else if (abs >= 1_000_000_000_000 && abs <= 4_102_444_800_000) {
    // Likely milliseconds - use as is
    return value
  } else {
    // Outside reasonable range
    console.warn('Timestamp outside reasonable range:', value)
    return value
  }
}

/**
 * Parse date input handling various formats
 * @param dateInput - The date input (string, number, Date, null, or undefined)
 * @returns Parsed Date object or null if invalid
 */
export const parseDateInput = (dateInput: string | number | Date | null | undefined): Date | null => {
  if (dateInput === null || dateInput === undefined) {
    return null
  }

  if (dateInput instanceof Date) {
    return !isNaN(dateInput.getTime()) ? dateInput : null
  }

  if (typeof dateInput === 'number') {
    const normalized = normalizeTimestamp(dateInput)
    if (isNaN(normalized)) {
      return null
    }
    const date = new Date(normalized)
    return !isNaN(date.getTime()) ? date : null
  }

  // Handle string dates
  try {
    const date = new Date(dateInput)
    return !isNaN(date.getTime()) ? date : null
  } catch {
    return null
  }
}

/**
 * Format date for display
 * @param dateInput - The date input
 * @param options - Intl.DateTimeFormatOptions for formatting
 * @returns Formatted date string
 */
export const formatDate = (
  dateInput: string | number | Date | null | undefined,
  options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }
): string => {
  try {
    const date = parseDateInput(dateInput)
    if (!date) {
      return 'Invalid Date'
    }
    return date.toLocaleDateString('en-US', options)
  } catch {
    return 'Invalid Date'
  }
}

/**
 * Format datetime for display
 * @param dateInput - The date input
 * @returns Formatted datetime string
 */
export const formatDateTime = (dateInput: string | number | Date | null | undefined): string => {
  return formatDate(dateInput, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
