// API Error Handler Composable
import { useNotifications } from './useNotifications'

export interface ApiError {
  status?: number
  statusCode?: number
  message: string
  data?: any
}

export interface RetryOptions {
  maxRetries?: number
  retryDelay?: number
  retryOn?: number[]
}

export const useApiErrorHandler = () => {
  const { error: showError } = useNotifications()
  const { t } = useI18n()

  // Check if error is a network error
  const isNetworkError = (error: any): boolean => {
    return (
      !error.status &&
      !error.statusCode &&
      (error.message?.includes('fetch') ||
        error.message?.includes('network') ||
        error.message?.includes('Failed to fetch'))
    )
  }

  // Check if error should trigger retry
  const shouldRetry = (error: any, retryOptions?: RetryOptions): boolean => {
    const retryOn = retryOptions?.retryOn || [408, 429, 500, 502, 503, 504]
    
    if (isNetworkError(error)) {
      return true
    }

    const status = error.status || error.statusCode
    return status && retryOn.includes(status)
  }

  // Get user-friendly error message
  const getErrorMessage = (error: any): string => {
    // Network errors
    if (isNetworkError(error)) {
      return t('errors.networkError')
    }

    // HTTP status errors
    const status = error.status || error.statusCode

    switch (status) {
      case 400:
        return error.data?.message || t('errors.badRequest')
      case 401:
        return t('errors.unauthorized')
      case 403:
        return t('errors.forbidden')
      case 404:
        return t('errors.notFound')
      case 409:
        return error.data?.message || t('errors.conflict')
      case 422:
        return error.data?.message || t('errors.validationError')
      case 429:
        return t('errors.tooManyRequests')
      case 500:
        return t('errors.serverError')
      case 502:
        return t('errors.badGateway')
      case 503:
        return t('errors.serviceUnavailable')
      case 504:
        return t('errors.gatewayTimeout')
      default:
        return error.message || t('errors.unknownError')
    }
  }

  // Handle API error with notification
  const handleError = (error: any, customMessage?: string) => {
    const message = customMessage || getErrorMessage(error)
    
    console.error('API Error:', error)
    
    showError(
      t('errors.errorOccurred'),
      message
    )
  }

  // Retry function with exponential backoff
  const retry = async <T>(
    fn: () => Promise<T>,
    options?: RetryOptions
  ): Promise<T> => {
    const maxRetries = options?.maxRetries || 3
    const retryDelay = options?.retryDelay || 1000

    let lastError: any

    for (let attempt = 0; attempt <= maxRetries; attempt++) {
      try {
        return await fn()
      } catch (error: any) {
        lastError = error

        // Don't retry if it's not a retryable error
        if (!shouldRetry(error, options)) {
          throw error
        }

        // Don't retry if we've exhausted attempts
        if (attempt === maxRetries) {
          throw error
        }

        // Wait before retrying with exponential backoff
        const delay = retryDelay * Math.pow(2, attempt)
        console.log(`Retrying in ${delay}ms (attempt ${attempt + 1}/${maxRetries})...`)
        await new Promise(resolve => setTimeout(resolve, delay))
      }
    }

    throw lastError
  }

  // Extract validation errors from API response
  const extractValidationErrors = (error: any): Record<string, string> => {
    const errors: Record<string, string> = {}

    if (error.data?.errors) {
      // Handle array of errors
      if (Array.isArray(error.data.errors)) {
        error.data.errors.forEach((err: any) => {
          if (err.field && err.message) {
            errors[err.field] = err.message
          }
        })
      }
      // Handle object of errors
      else if (typeof error.data.errors === 'object') {
        Object.entries(error.data.errors).forEach(([field, message]) => {
          errors[field] = String(message)
        })
      }
    }

    return errors
  }

  return {
    isNetworkError,
    shouldRetry,
    getErrorMessage,
    handleError,
    retry,
    extractValidationErrors
  }
}
