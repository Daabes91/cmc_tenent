// Security Features Composable
import { ref, onMounted, onUnmounted } from 'vue'

interface RateLimitConfig {
  maxRequests: number
  windowMs: number
}

interface RequestRecord {
  timestamp: number
  endpoint: string
}

// Session timeout configuration (30 minutes)
const SESSION_TIMEOUT_MS = 30 * 60 * 1000
const WARNING_BEFORE_TIMEOUT_MS = 2 * 60 * 1000 // 2 minutes warning

// Rate limiting configuration
const DEFAULT_RATE_LIMIT: RateLimitConfig = {
  maxRequests: 100,
  windowMs: 60 * 1000 // 1 minute
}

// Global state for rate limiting
const requestHistory = ref<RequestRecord[]>([])
const lastActivityTime = ref<number>(Date.now())
const sessionTimeoutTimer = ref<NodeJS.Timeout | null>(null)
const warningTimer = ref<NodeJS.Timeout | null>(null)
const showTimeoutWarning = ref(false)

export const useSecurity = () => {
  const { logout } = useSaasAuth()
  const { warning, error, success } = useNotifications()

  // XSS Prevention: Sanitize input
  const sanitizeInput = (input: string): string => {
    if (!input) return ''
    
    // Remove HTML tags and dangerous characters
    return input
      .replace(/[<>]/g, '') // Remove < and >
      .replace(/javascript:/gi, '') // Remove javascript: protocol
      .replace(/on\w+\s*=/gi, '') // Remove event handlers
      .trim()
  }

  // Sanitize object recursively
  const sanitizeObject = <T extends Record<string, any>>(obj: T): T => {
    const sanitized = { ...obj }
    
    for (const key in sanitized) {
      if (typeof sanitized[key] === 'string') {
        sanitized[key] = sanitizeInput(sanitized[key]) as any
      } else if (typeof sanitized[key] === 'object' && sanitized[key] !== null) {
        sanitized[key] = sanitizeObject(sanitized[key])
      }
    }
    
    return sanitized
  }

  // Rate Limiting: Check if request is allowed
  const checkRateLimit = (endpoint: string, config: RateLimitConfig = DEFAULT_RATE_LIMIT): boolean => {
    const now = Date.now()
    const windowStart = now - config.windowMs

    // Clean up old requests outside the window
    requestHistory.value = requestHistory.value.filter(
      record => record.timestamp > windowStart
    )

    // Count requests in current window
    const requestsInWindow = requestHistory.value.length

    if (requestsInWindow >= config.maxRequests) {
      // Rate limit exceeded
      console.warn(`Rate limit exceeded for endpoint: ${endpoint}`)
      warning('Rate Limit Exceeded', 'Too many requests. Please slow down.', 5000)
      return false
    }

    // Add current request to history
    requestHistory.value.push({
      timestamp: now,
      endpoint
    })

    return true
  }

  // Token Refresh: Check if token needs refresh
  const shouldRefreshToken = (): boolean => {
    if (!import.meta.client) return false

    const expiry = localStorage.getItem('saas_token_expiry')
    if (!expiry) return false

    const expiryDate = new Date(expiry)
    const now = new Date()
    const timeUntilExpiry = expiryDate.getTime() - now.getTime()

    // Refresh if less than 5 minutes until expiry
    const REFRESH_THRESHOLD_MS = 5 * 60 * 1000
    return timeUntilExpiry > 0 && timeUntilExpiry < REFRESH_THRESHOLD_MS
  }

  // Token Refresh: Refresh the token
  const refreshToken = async (): Promise<boolean> => {
    try {
      const { getToken } = useSaasAuth()
      const token = getToken()

      if (!token) return false

      const config = useRuntimeConfig()
      const response = await $fetch<{ token: string; expiresAt: string }>(
        `${config.public.saasApiBase}/auth/refresh`,
        {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      )

      // Update token in localStorage
      if (import.meta.client) {
        localStorage.setItem('saas_auth_token', response.token)
        localStorage.setItem('saas_token_expiry', response.expiresAt)
      }

      return true
    } catch (error) {
      console.error('Token refresh failed:', error)
      return false
    }
  }

  // Session Timeout: Update last activity time
  const updateActivity = () => {
    lastActivityTime.value = Date.now()
    resetSessionTimeout()
  }

  // Session Timeout: Show warning dialog
  const showSessionWarning = () => {
    showTimeoutWarning.value = true
    warning('Session Timeout Warning', 'Your session will expire in 2 minutes due to inactivity.', 10000)
  }

  // Session Timeout: Handle session expiration
  const handleSessionTimeout = () => {
    error('Session Expired', 'Your session has expired due to inactivity.')
    logout()
    navigateTo('/login')
  }

  // Session Timeout: Reset timers
  const resetSessionTimeout = () => {
    // Clear existing timers
    if (warningTimer.value) {
      clearTimeout(warningTimer.value)
    }
    if (sessionTimeoutTimer.value) {
      clearTimeout(sessionTimeoutTimer.value)
    }

    showTimeoutWarning.value = false

    // Set warning timer (28 minutes)
    warningTimer.value = setTimeout(() => {
      showSessionWarning()
    }, SESSION_TIMEOUT_MS - WARNING_BEFORE_TIMEOUT_MS)

    // Set timeout timer (30 minutes)
    sessionTimeoutTimer.value = setTimeout(() => {
      handleSessionTimeout()
    }, SESSION_TIMEOUT_MS)
  }

  // Session Timeout: Extend session
  const extendSession = () => {
    showTimeoutWarning.value = false
    updateActivity()
    success('Session Extended', 'Session extended successfully.', 3000)
  }

  // Initialize session monitoring
  const initializeSessionMonitoring = () => {
    if (!import.meta.client) return

    // Set up activity listeners
    const activityEvents = ['mousedown', 'keydown', 'scroll', 'touchstart']
    
    activityEvents.forEach(event => {
      window.addEventListener(event, updateActivity, { passive: true })
    })

    // Start session timeout
    resetSessionTimeout()

    // Check for token refresh every minute
    const refreshInterval = setInterval(async () => {
      if (shouldRefreshToken()) {
        const refreshed = await refreshToken()
        if (!refreshed) {
          // If refresh fails, log out
          handleSessionTimeout()
        }
      }
    }, 60 * 1000)

    // Cleanup on unmount
    onUnmounted(() => {
      activityEvents.forEach(event => {
        window.removeEventListener(event, updateActivity)
      })
      
      if (warningTimer.value) clearTimeout(warningTimer.value)
      if (sessionTimeoutTimer.value) clearTimeout(sessionTimeoutTimer.value)
      clearInterval(refreshInterval)
    })
  }

  // Secure logging: Never log sensitive data
  const secureLog = (message: string, data?: any) => {
    if (!data) {
      console.log(message)
      return
    }

    // List of sensitive keys to redact
    const sensitiveKeys = [
      'password',
      'token',
      'jwt',
      'secret',
      'apiKey',
      'api_key',
      'authorization',
      'credentials'
    ]

    const redactSensitiveData = (obj: any): any => {
      if (typeof obj !== 'object' || obj === null) {
        return obj
      }

      if (Array.isArray(obj)) {
        return obj.map(item => redactSensitiveData(item))
      }

      const redacted: any = {}
      for (const key in obj) {
        const lowerKey = key.toLowerCase()
        if (sensitiveKeys.some(sensitive => lowerKey.includes(sensitive))) {
          redacted[key] = '[REDACTED]'
        } else if (typeof obj[key] === 'object') {
          redacted[key] = redactSensitiveData(obj[key])
        } else {
          redacted[key] = obj[key]
        }
      }
      return redacted
    }

    console.log(message, redactSensitiveData(data))
  }

  // CSRF Token handling (if backend requires it)
  const getCsrfToken = (): string | null => {
    if (!import.meta.client) return null
    
    // Try to get from meta tag
    const metaTag = document.querySelector('meta[name="csrf-token"]')
    if (metaTag) {
      return metaTag.getAttribute('content')
    }

    // Try to get from cookie
    const cookies = document.cookie.split(';')
    for (const cookie of cookies) {
      const [name, value] = cookie.trim().split('=')
      if (name === 'XSRF-TOKEN') {
        return decodeURIComponent(value)
      }
    }

    return null
  }

  return {
    // XSS Prevention
    sanitizeInput,
    sanitizeObject,

    // Rate Limiting
    checkRateLimit,

    // Token Refresh
    shouldRefreshToken,
    refreshToken,

    // Session Timeout
    updateActivity,
    extendSession,
    initializeSessionMonitoring,
    showTimeoutWarning,

    // Secure Logging
    secureLog,

    // CSRF Token
    getCsrfToken
  }
}
