// Security Monitoring Composable
import { ref } from 'vue'
import { useNotifications } from './useNotifications'
import { useSaasApi } from '#imports'
import { useI18n } from '#imports'

interface AuthenticationFailure {
  tenantId: number
  tenantSlug: string
  tenantName: string
  failureCount: number
  lastFailureAt: string
}

interface SecurityAlert {
  id: string
  type: 'auth_failures' | 'suspicious_activity' | 'data_breach_attempt'
  severity: 'low' | 'medium' | 'high' | 'critical'
  message: string
  timestamp: Date
  tenantId?: number
  tenantName?: string
}

// Track notified alerts to avoid duplicates
const notifiedAlerts = ref<Set<string>>(new Set())

// Threshold for authentication failures before alerting
const AUTH_FAILURE_THRESHOLD = 5
const AUTH_FAILURE_TIME_WINDOW = 15 * 60 * 1000 // 15 minutes in milliseconds

export const useSecurityMonitoring = () => {
  const { warning, error: notifyError } = useNotifications()
  const { t } = useI18n()
  const api = useSaasApi()

  const alerts = ref<SecurityAlert[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Check for authentication failures
  const checkAuthenticationFailures = async () => {
    try {
      // This would call a backend endpoint that tracks auth failures
      // For now, we'll simulate the structure
      const failures: AuthenticationFailure[] = []
      
      // In a real implementation, this would fetch from the API
      // const failures = await api.getAuthenticationFailures({
      //   threshold: AUTH_FAILURE_THRESHOLD,
      //   timeWindow: AUTH_FAILURE_TIME_WINDOW
      // })

      for (const failure of failures) {
        const alertKey = `auth_failure_${failure.tenantId}_${failure.failureCount}`
        
        // Only alert if we haven't already notified about this
        if (!notifiedAlerts.value.has(alertKey)) {
          warning(
            t('notifications.securityAlert'),
            t('notifications.authenticationFailures', { name: failure.tenantName })
          )

          // Add to alerts list
          alerts.value.unshift({
            id: alertKey,
            type: 'auth_failures',
            severity: failure.failureCount > AUTH_FAILURE_THRESHOLD * 2 ? 'high' : 'medium',
            message: `${failure.failureCount} failed login attempts detected for ${failure.tenantName}`,
            timestamp: new Date(failure.lastFailureAt),
            tenantId: failure.tenantId,
            tenantName: failure.tenantName
          })

          // Mark as notified
          notifiedAlerts.value.add(alertKey)
        }
      }
    } catch (err: any) {
      console.error('Error checking authentication failures:', err)
      error.value = err.message || 'Failed to check authentication failures'
    }
  }

  // Monitor security events
  const monitorSecurityEvents = async () => {
    // TODO: Enable when backend endpoint is implemented
    // loading.value = true
    // error.value = null
    // try {
    //   await checkAuthenticationFailures()
    //   // Add more security checks here as needed
    // } catch (err: any) {
    //   console.error('Error monitoring security events:', err)
    //   error.value = err.message || 'Failed to monitor security events'
    // } finally {
    //   loading.value = false
    // }
  }

  // Clear old alerts (keep last 50)
  const pruneAlerts = () => {
    if (alerts.value.length > 50) {
      alerts.value = alerts.value.slice(0, 50)
    }
  }

  // Get alerts for a specific tenant
  const getTenantAlerts = (tenantId: number) => {
    return alerts.value.filter(alert => alert.tenantId === tenantId)
  }

  return {
    alerts: computed(() => alerts.value),
    loading: computed(() => loading.value),
    error: computed(() => error.value),
    monitorSecurityEvents,
    checkAuthenticationFailures,
    getTenantAlerts,
    pruneAlerts
  }
}
