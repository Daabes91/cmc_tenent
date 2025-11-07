import { formatDate, formatDateTime, formatRelativeTime } from '~/utils/dateUtils'

/**
 * Composable for date formatting utilities
 * Provides easy access to date formatting functions throughout the app
 */
export const useDateFormatting = () => {
  return {
    formatDate,
    formatDateTime,
    formatRelativeTime
  }
}