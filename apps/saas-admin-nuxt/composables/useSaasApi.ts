// SAAS Manager API Integration Composable
import type { 
  Tenant, 
  TenantFormData, 
  TenantCreateResponse,
  SystemMetrics,
  TenantMetrics,
  AuditLog,
  BrandingConfig
} from '~/types'
import { useApiErrorHandler } from './useApiErrorHandler'
import { useConnectionStatus } from './useConnectionStatus'

interface TenantListParams {
  page?: number
  size?: number
  search?: string
  status?: string
  sortBy?: string
  sortDirection?: 'asc' | 'desc'
}

interface AuditLogFilters {
  startDate?: string
  endDate?: string
  actionType?: string
  managerId?: number
  tenantId?: number
  page?: number
  size?: number
}

interface AnalyticsParams {
  timeRange?: '7d' | '30d' | '90d' | 'custom'
  startDate?: string
  endDate?: string
}

export const useSaasApi = () => {
  const config = useRuntimeConfig()
  const { getToken, logout } = useSaasAuth()
  const router = useRouter()
  const { handleError, retry, isNetworkError } = useApiErrorHandler()
  const { updateOnlineStatus } = useConnectionStatus()
  const { checkRateLimit, sanitizeObject, getCsrfToken, secureLog } = useSecurity()

  // Generic API call wrapper with authentication and security features
  const apiCall = async <T>(
    endpoint: string,
    options: any = {}
  ): Promise<T> => {
    // Check rate limit before making request
    if (!checkRateLimit(endpoint)) {
      throw new Error('Rate limit exceeded. Please try again later.')
    }

    const token = getToken()

    // Sanitize request body if present
    let sanitizedBody = options.body
    if (options.body && typeof options.body === 'string') {
      try {
        const parsed = JSON.parse(options.body)
        const sanitized = sanitizeObject(parsed)
        sanitizedBody = JSON.stringify(sanitized)
      } catch (e) {
        // If not JSON, leave as is
      }
    }

    // Get CSRF token if available
    const csrfToken = getCsrfToken()

    try {
      const response = await $fetch<T>(`${config.public.saasApiBase}${endpoint}`, {
        ...options,
        body: sanitizedBody,
        headers: {
          ...options.headers,
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json',
          ...(csrfToken ? { 'X-CSRF-TOKEN': csrfToken } : {})
        }
      })

      // Secure logging (never log sensitive data)
      secureLog(`API call successful: ${endpoint}`)

      return response
    } catch (error: any) {
      // Handle 401 Unauthorized - token expired or invalid
      if (error.status === 401 || error.statusCode === 401) {
        secureLog('Authentication failed, logging out...')
        logout()
        
        // Redirect to login page
        if (import.meta.client) {
          router.push('/login')
        }
        
        throw new Error('Session expired. Please login again.')
      }

      // Check if it's a network error and update connection status
      if (isNetworkError(error)) {
        await updateOnlineStatus()
      }

      // Secure error logging (redact sensitive data)
      secureLog('API call failed:', { endpoint, error: error.message })
      throw error
    }
  }

  // API call with retry logic
  const apiCallWithRetry = async <T>(
    endpoint: string,
    options: any = {},
    retryOptions?: { maxRetries?: number; retryDelay?: number }
  ): Promise<T> => {
    return retry(
      () => apiCall<T>(endpoint, options),
      retryOptions
    )
  }

  // Convert params object to URLSearchParams
  const toQueryString = (params: Record<string, any>): string => {
    const searchParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        searchParams.append(key, String(value))
      }
    })
    return searchParams.toString()
  }

  return {
    // Authentication
    login: (email: string, password: string) =>
      $fetch<{ token: string; managerName: string; expiresAt: string }>(
        `${config.public.saasApiBase}/auth/login`,
        {
          method: 'POST',
          body: JSON.stringify({ email, password }),
          headers: {
            'Content-Type': 'application/json'
          }
        }
      ),

    // Tenant Management
    getTenants: (params: TenantListParams = {}) => {
      const queryString = toQueryString(params)
      return apiCall<{ content: Tenant[]; totalPages: number; totalElements: number }>(
        `/tenants${queryString ? `?${queryString}` : ''}`
      )
    },

    getTenant: (id: number, includeDeleted = false) =>
      apiCall<Tenant>(`/tenants/${id}?includeDeleted=${includeDeleted}`),

    createTenant: (data: TenantFormData) =>
      apiCall<TenantCreateResponse>('/tenants', {
        method: 'POST',
        body: JSON.stringify(data)
      }),

    updateTenant: (id: number, data: Partial<TenantFormData>) =>
      apiCall<Tenant>(`/tenants/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
      }),

    deleteTenant: (id: number) =>
      apiCall<void>(`/tenants/${id}`, {
        method: 'DELETE'
      }),

    // Metrics (to be implemented in backend)
    getSystemMetrics: () =>
      apiCall<SystemMetrics>('/metrics/system'),

    getTenantMetrics: (id: number) =>
      apiCall<TenantMetrics>(`/metrics/tenants/${id}`),

    getAnalytics: (params: AnalyticsParams = {}) => {
      const queryString = toQueryString(params)
      return apiCall<any>(`/analytics${queryString ? `?${queryString}` : ''}`)
    },

    exportAnalytics: async (params: AnalyticsParams = {}): Promise<Blob> => {
      const token = getToken()
      const queryString = toQueryString(params)
      
      const response = await fetch(
        `${config.public.saasApiBase}/analytics/export${queryString ? `?${queryString}` : ''}`,
        {
          method: 'GET',
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          }
        }
      )

      if (!response.ok) {
        throw new Error('Failed to export analytics')
      }

      return await response.blob()
    },

    // Audit Logs (to be implemented in backend)
    getAuditLogs: (params: AuditLogFilters = {}) => {
      const queryString = toQueryString(params)
      return apiCall<{ content: AuditLog[]; totalPages: number; totalElements: number }>(
        `/audit-logs${queryString ? `?${queryString}` : ''}`
      )
    },

    exportAuditLogs: async (params: AuditLogFilters = {}): Promise<Blob> => {
      const token = getToken()
      const queryString = toQueryString(params)
      
      const response = await fetch(
        `${config.public.saasApiBase}/audit-logs/export${queryString ? `?${queryString}` : ''}`,
        {
          method: 'GET',
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          }
        }
      )

      if (!response.ok) {
        throw new Error('Failed to export audit logs')
      }

      return await response.blob()
    },

    // Branding Configuration
    getTenantBranding: (id: number) =>
      apiCall<BrandingConfig>(`/tenants/${id}/branding`),

    updateTenantBranding: (id: number, config: BrandingConfig) =>
      apiCall<BrandingConfig>(`/tenants/${id}/branding`, {
        method: 'PUT',
        body: JSON.stringify(config)
      })
  }
}
