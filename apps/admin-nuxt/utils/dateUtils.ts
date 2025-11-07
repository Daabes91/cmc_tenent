/**
 * Formats a date input (string, number, or Date) into a readable format
 * @param dateInput - The date input (string, number timestamp, or Date object)
 * @param options - Intl.DateTimeFormatOptions for formatting
 * @returns Formatted date string
 */
const NUMERIC_DATE_PATTERN = /^[+-]?\d+(\.\d+)?$/;

/**
 * Enhanced timestamp normalization with better detection and logging
 * @param value - Numeric timestamp value
 * @returns Normalized timestamp in milliseconds
 */
const normalizeTimestamp = (value: number): number => {
  if (!Number.isFinite(value)) {
    console.warn('Invalid timestamp: not a finite number', value);
    return NaN;
  }
  
  const abs = Math.abs(value);
  
  // Enhanced detection logic for seconds vs milliseconds
  // Unix timestamps in seconds: 1000000000 (2001) to 4102444800 (2100)
  // Unix timestamps in milliseconds: 1000000000000 (2001) to 4102444800000 (2100)
  if (abs >= 1_000_000_000 && abs <= 4_102_444_800) {
    // Likely seconds - convert to milliseconds
    console.debug('Detected Unix timestamp in seconds, converting to milliseconds:', value);
    return value * 1000;
  } else if (abs >= 1_000_000_000_000 && abs <= 4_102_444_800_000) {
    // Likely milliseconds - use as is
    console.debug('Detected Unix timestamp in milliseconds:', value);
    return value;
  } else if (abs > 0 && abs < 1_000_000_000) {
    // Very small numbers - likely seconds from a different epoch or test data
    console.warn('Unusual timestamp detected, treating as seconds:', value);
    return value * 1000;
  } else {
    // Outside reasonable range
    console.warn('Timestamp outside reasonable range:', value);
    return value; // Return as-is and let Date constructor handle it
  }
};

/**
 * Enhanced date input parsing with better error handling and logging
 * @param dateInput - The date input (string, number, Date, null, or undefined)
 * @returns Parsed Date object or null if invalid
 */
const parseDateInput = (dateInput: string | number | Date | null | undefined): Date | null => {
  if (dateInput === null || dateInput === undefined) {
    return null;
  }

  if (dateInput instanceof Date) {
    const isValid = !Number.isNaN(dateInput.getTime());
    if (!isValid) {
      console.warn('Invalid Date object provided:', dateInput);
    }
    return isValid ? dateInput : null;
  }

  if (typeof dateInput === 'number') {
    const normalized = normalizeTimestamp(dateInput);
    if (Number.isNaN(normalized)) {
      console.warn('Failed to normalize timestamp:', dateInput);
      return null;
    }
    const date = new Date(normalized);
    const isValid = !Number.isNaN(date.getTime());
    if (!isValid) {
      console.warn('Failed to create Date from normalized timestamp:', normalized, 'original:', dateInput);
    }
    return isValid ? date : null;
  }

  const value = String(dateInput).trim();
  if (!value) {
    return null;
  }

  // Handle numeric strings (Unix timestamps as strings)
  if (NUMERIC_DATE_PATTERN.test(value)) {
    const numericValue = Number(value);
    if (!Number.isFinite(numericValue)) {
      console.warn('Non-finite numeric string:', value);
      return null;
    }
    const normalized = normalizeTimestamp(numericValue);
    if (Number.isNaN(normalized)) {
      console.warn('Failed to normalize numeric string timestamp:', value);
      return null;
    }
    const date = new Date(normalized);
    const isValid = !Number.isNaN(date.getTime());
    if (!isValid) {
      console.warn('Failed to create Date from numeric string:', value);
    }
    return isValid ? date : null;
  }

  // Handle ISO strings and other date formats
  try {
    const parsed = new Date(value);
    const isValid = !Number.isNaN(parsed.getTime());
    if (!isValid) {
      console.warn('Failed to parse date string:', value);
    }
    return isValid ? parsed : null;
  } catch (error) {
    console.warn('Exception parsing date string:', value, error);
    return null;
  }
};

export const parseDateValue = (
  dateInput: string | number | Date | null | undefined
): Date | null => parseDateInput(dateInput);

/**
 * Normalize appointment time data from different API formats to consistent Date objects
 * Handles both Unix timestamps (from list APIs) and ISO strings (from detail APIs)
 * @param input - Appointment time input (Unix timestamp, ISO string, Date object, or null)
 * @returns Normalized Date object or null
 */
export function normalizeAppointmentTime(input: string | number | Date | null | undefined): Date | null {
  if (input === null || input === undefined) {
    return null;
  }

  try {
    const parsed = parseDateInput(input);
    if (parsed) {
      console.debug('Successfully normalized appointment time:', input, '→', parsed.toISOString());
    } else {
      console.warn('Failed to normalize appointment time:', input);
    }
    return parsed;
  } catch (error) {
    console.error('Error normalizing appointment time:', input, error);
    return null;
  }
}

/**
 * Convert clinic timezone time to UTC for API submission
 * @param dateInput - Date/time string in clinic timezone (e.g., from datetime-local input)
 * @param clinicTimezone - The clinic timezone (e.g., "Asia/Amman")
 * @returns UTC ISO string for API submission
 */
export function convertClinicTimeToUTC(dateInput: string, clinicTimezone: string = 'Asia/Amman'): string {
  if (!dateInput || !dateInput.trim()) {
    throw new Error('Invalid date input for UTC conversion');
  }

  try {
    // Create a date assuming the input is in the clinic timezone
    // Note: This is tricky because datetime-local inputs don't include timezone info
    // We need to interpret the input as being in the clinic timezone
    
    // Parse the datetime-local format (YYYY-MM-DDTHH:mm)
    const localDate = new Date(dateInput);
    if (Number.isNaN(localDate.getTime())) {
      throw new Error(`Invalid date format: ${dateInput}`);
    }

    // Get the timezone offset for the clinic timezone at this date
    const tempDate = new Date(localDate.getTime());
    const utcTime = tempDate.getTime() + (tempDate.getTimezoneOffset() * 60000);
    
    // Create a date in the clinic timezone
    const clinicDate = new Date(utcTime + getTimezoneOffset(clinicTimezone, tempDate));
    
    const utcString = clinicDate.toISOString();
    console.debug('Converted clinic time to UTC:', dateInput, '→', utcString, 'timezone:', clinicTimezone);
    return utcString;
  } catch (error) {
    console.error('Error converting clinic time to UTC:', dateInput, clinicTimezone, error);
    throw error;
  }
}

/**
 * Convert UTC time to clinic timezone for display
 * @param utcInput - UTC timestamp or ISO string
 * @param clinicTimezone - The clinic timezone (e.g., "Asia/Amman")
 * @returns Date object in clinic timezone context
 */
export function convertUTCToClinicTime(utcInput: string | number, clinicTimezone: string = 'Asia/Amman'): Date | null {
  try {
    const utcDate = normalizeAppointmentTime(utcInput);
    if (!utcDate) {
      console.warn('Failed to parse UTC input for clinic conversion:', utcInput);
      return null;
    }

    // The Date object represents the correct UTC time
    // When we format it with the clinic timezone, it will show the correct local time
    console.debug('Converted UTC to clinic time context:', utcInput, '→', utcDate.toISOString(), 'timezone:', clinicTimezone);
    return utcDate;
  } catch (error) {
    console.error('Error converting UTC to clinic time:', utcInput, clinicTimezone, error);
    return null;
  }
}

/**
 * Helper function to get timezone offset in milliseconds
 * @param timezone - IANA timezone identifier
 * @param date - Reference date for offset calculation
 * @returns Offset in milliseconds
 */
function getTimezoneOffset(timezone: string, date: Date): number {
  try {
    // Use Intl.DateTimeFormat to get the timezone offset
    const utcDate = new Date(date.getTime());
    const tzDate = new Date(utcDate.toLocaleString('en-US', { timeZone: timezone }));
    const utcTime = new Date(utcDate.toLocaleString('en-US', { timeZone: 'UTC' }));
    return tzDate.getTime() - utcTime.getTime();
  } catch (error) {
    console.warn('Failed to calculate timezone offset, using 0:', timezone, error);
    return 0;
  }
}

export function formatDate(
  dateInput: string | number | Date | null | undefined,
  options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }
): string {
  if (dateInput === null || dateInput === undefined || (typeof dateInput === 'string' && !dateInput.trim())) {
    return 'Not set';
  }

  try {
    const date = parseDateInput(dateInput);
    if (!date) {
      console.warn('Invalid date input for formatting:', {
        input: dateInput,
        type: typeof dateInput,
        isString: typeof dateInput === 'string',
        stringLength: typeof dateInput === 'string' ? dateInput.length : 'N/A'
      });
      return 'Invalid date';
    }

    const formatted = date.toLocaleDateString('en-US', options);
    console.debug('Successfully formatted date:', dateInput, '→', formatted);
    return formatted;
  } catch (error) {
    console.error('Error formatting date:', {
      error: error instanceof Error ? error.message : error,
      input: dateInput,
      inputType: typeof dateInput,
      options
    });
    return 'Invalid date';
  }
}

/**
 * Formats a date input into a relative time string (e.g., "2 days ago")
 * @param dateInput - The date input
 * @returns Relative time string
 */
export function formatRelativeTime(dateInput: string | number | Date | null | undefined): string {
  if (dateInput === null || dateInput === undefined || (typeof dateInput === 'string' && !dateInput.trim())) {
    return 'Unknown';
  }

  try {
    const date = parseDateInput(dateInput);
    if (!date) {
      console.warn('Invalid date input for relative time formatting:', {
        input: dateInput,
        type: typeof dateInput
      });
      return 'Invalid date';
    }

    const now = new Date();
    const diffInMs = now.getTime() - date.getTime();
    
    // Handle edge cases
    if (!Number.isFinite(diffInMs)) {
      console.warn('Invalid time difference calculated:', { now: now.getTime(), date: date.getTime() });
      return 'Unknown';
    }

    const diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24));
    const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));
    const diffInMinutes = Math.floor(diffInMs / (1000 * 60));

    if (diffInDays > 0) {
      return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`;
    } else if (diffInHours > 0) {
      return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`;
    } else if (diffInMinutes > 0) {
      return `${diffInMinutes} minute${diffInMinutes > 1 ? 's' : ''} ago`;
    } else {
      return 'Just now';
    }
  } catch (error) {
    console.error('Error formatting relative time:', {
      error: error instanceof Error ? error.message : error,
      input: dateInput,
      inputType: typeof dateInput
    });
    return 'Unknown';
  }
}

/**
 * Formats a date input into a full datetime string
 * @param dateInput - The date input
 * @returns Full datetime string
 */
export function formatDateTime(dateInput: string | number | Date | null | undefined): string {
  return formatDate(dateInput, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

/**
 * Formats a date in the clinic's timezone (for admin dashboard).
 * CRITICAL: Admin users should always see times in clinic timezone, not their browser timezone.
 *
 * @param dateInput - The date input (UTC timestamp or ISO string)
 * @param clinicTimezone - The clinic timezone (e.g., "Asia/Amman")
 * @param options - Additional formatting options
 * @returns Formatted date string in clinic timezone
 */
export function formatDateInClinicTimezone(
  dateInput: string | number | Date | null | undefined,
  clinicTimezone: string = 'Asia/Amman',
  options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }
): string {
  if (dateInput === null || dateInput === undefined || (typeof dateInput === 'string' && !dateInput.trim())) {
    return 'Not set';
  }

  try {
    const date = normalizeAppointmentTime(dateInput);
    if (!date) {
      console.warn('Invalid date input for clinic timezone formatting:', dateInput);
      return 'Invalid date';
    }

    // IMPORTANT: Always use clinic timezone, not browser timezone
    const formatted = date.toLocaleDateString('en-US', {
      ...options,
      timeZone: clinicTimezone
    });
    
    console.debug('Formatted date in clinic timezone:', dateInput, '→', formatted, 'timezone:', clinicTimezone);
    return formatted;
  } catch (error) {
    console.error('Error formatting date in clinic timezone:', error, 'Input:', dateInput, 'Timezone:', clinicTimezone);
    return 'Invalid date';
  }
}

/**
 * Formats a datetime in the clinic's timezone with timezone abbreviation.
 * Example: "Jan 15, 2024, 2:00 PM EET"
 *
 * @param dateInput - The date input
 * @param clinicTimezone - The clinic timezone
 * @param abbreviation - Timezone abbreviation to display (e.g., "EET")
 * @returns Formatted datetime string with timezone
 */
export function formatDateTimeInClinicTimezone(
  dateInput: string | number | Date | null | undefined,
  clinicTimezone: string = 'Asia/Amman',
  abbreviation?: string
): string {
  if (dateInput === null || dateInput === undefined || (typeof dateInput === 'string' && !dateInput.trim())) {
    return 'Not set';
  }

  try {
    const date = normalizeAppointmentTime(dateInput);
    if (!date) {
      console.warn('Invalid date input for clinic datetime formatting:', dateInput);
      return 'Invalid date';
    }

    const formatted = date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      timeZone: clinicTimezone
    });

    const result = abbreviation ? `${formatted} ${abbreviation}` : formatted;
    console.debug('Formatted datetime in clinic timezone:', dateInput, '→', result, 'timezone:', clinicTimezone);
    return result;
  } catch (error) {
    console.error('Error formatting datetime in clinic timezone:', error, 'Input:', dateInput, 'Timezone:', clinicTimezone);
    return 'Invalid date';
  }
}

/**
 * Formats only the time portion in the clinic's timezone.
 * Example: "2:00 PM EET"
 *
 * @param dateInput - The date input
 * @param clinicTimezone - The clinic timezone
 * @param abbreviation - Timezone abbreviation to display
 * @returns Formatted time string
 */
export function formatTimeInClinicTimezone(
  dateInput: string | number | Date | null | undefined,
  clinicTimezone: string = 'Asia/Amman',
  abbreviation?: string
): string {
  if (dateInput === null || dateInput === undefined || (typeof dateInput === 'string' && !dateInput.trim())) {
    return 'Not set';
  }

  try {
    const date = normalizeAppointmentTime(dateInput);
    if (!date) {
      console.warn('Invalid date input for clinic time formatting:', dateInput);
      return 'Invalid time';
    }

    const formatted = date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      timeZone: clinicTimezone
    });

    const result = abbreviation ? `${formatted} ${abbreviation}` : formatted;
    console.debug('Formatted time in clinic timezone:', dateInput, '→', result, 'timezone:', clinicTimezone);
    return result;
  } catch (error) {
    console.error('Error formatting time in clinic timezone:', error, 'Input:', dateInput, 'Timezone:', clinicTimezone);
    return 'Invalid time';
  }
}
