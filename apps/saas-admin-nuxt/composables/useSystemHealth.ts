// System Health Monitoring Composable
import { ref, computed } from 'vue'
import { useNotifications } from './useNotifications'
import { useSaasApi } from '#imports'
import { useI18n } from '#imports'

interface SystemHealthMetrics {
  apiResponseTime: number
  databaseStatus: 'healthy' | 'degraded' | 'down'
  activeConnections: number
  memoryUsage: number
  cpuUsage: number
}

interface HealthThresholds {
  apiResponseTimeWarning: number // ms
  apiResponseTimeCritical: number // ms
  memoryUsageWarning: number // percentage
  memoryUsageCritical: number // percentage
  cpuUsageWarning: number // percentage
  cpuUsageCritical: number // percentage
}

const DEFAULT_THRESHOLDS: HealthThresholds = {
  apiResponseTimeWarning: 1000,
  apiResponseTimeCritical: 3000,
  memoryUsageWarning: 75,
  memoryUsageCritical: 90,
  cpuUsageWarning: 75,
  cpuUsageCritical: 90
}

// Track previous health status to avoid duplicate notifications
const previousHealthStatus = ref<Record<string, boolean>>({})

export const useSystemHealth = () => {
  const { warning, error: notifyError } = useNotifications()
  const { t } = useI18n()
  const api = useSaasApi()

  const healthMetrics = ref<SystemHealthMetrics | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Check if a metric has degraded
  const checkMetricHealth = (
    metricName: string,
    currentValue: number,
    warningThreshold: number,
    criticalThreshold: number,
    isHigherWorse: boolean = true
  ) => {
    const key = `${metricName}_degraded`
    const isDegraded = isHigherWorse
      ? currentValue >= warningThreshold
      : currentValue <= warningThreshold
    
    const isCritical = isHigherWorse
      ? currentValue >= criticalThreshold
      : currentValue <= criticalThreshold

    // Only notify if status changed from healthy to degraded
    if (isDegraded && !previousHealthStatus.value[key]) {
      const message = t('notifications.systemHealthDegraded', { 
        metric: `${metricName}: ${currentValue}${metricName.includes('Usage') ? '%' : 'ms'}` 
      })

      if (isCritical) {
        notifyError(t('notifications.systemHealthWarning'), message)
      } else {
        warning(t('notifications.systemHealthWarning'), message)
      }

      previousHealthStatus.value[key] = true
    } else if (!isDegraded && previousHealthStatus.value[key]) {
      // Reset status when metric returns to healthy
      previousHealthStatus.value[key] = false
    }
  }

  // Check database health
  const checkDatabaseHealth = (status: string) => {
    const key = 'database_degraded'
    const isDegraded = status !== 'healthy'

    if (isDegraded && !previousHealthStatus.value[key]) {
      const message = t('notifications.systemHealthDegraded', { 
        metric: `Database Status: ${status}` 
      })

      if (status === 'down') {
        notifyError(t('notifications.systemHealthWarning'), message)
      } else {
        warning(t('notifications.systemHealthWarning'), message)
      }

      previousHealthStatus.value[key] = true
    } else if (!isDegraded && previousHealthStatus.value[key]) {
      previousHealthStatus.value[key] = false
    }
  }

  // Fetch and analyze system health
  const fetchSystemHealth = async (thresholds: HealthThresholds = DEFAULT_THRESHOLDS) => {
    loading.value = true
    error.value = null

    try {
      // TODO: Enable when backend endpoint is implemented
      // const metrics = await api.getSystemMetrics()
      
      // Temporary: Use mock data
      const metrics = {
        apiResponseTime: 0,
        databaseStatus: 'healthy' as const,
        activeConnections: 0,
        memoryUsage: 0,
        cpuUsage: 0
      }
      
      healthMetrics.value = {
        apiResponseTime: metrics.apiResponseTime || 0,
        databaseStatus: metrics.databaseStatus || 'healthy',
        activeConnections: (metrics as any).activeConnections || 0,
        memoryUsage: (metrics as any).memoryUsage || 0,
        cpuUsage: (metrics as any).cpuUsage || 0
      }

      // Check each metric against thresholds
      checkMetricHealth(
        'API Response Time',
        healthMetrics.value.apiResponseTime,
        thresholds.apiResponseTimeWarning,
        thresholds.apiResponseTimeCritical,
        true
      )

      checkMetricHealth(
        'Memory Usage',
        healthMetrics.value.memoryUsage,
        thresholds.memoryUsageWarning,
        thresholds.memoryUsageCritical,
        true
      )

      checkMetricHealth(
        'CPU Usage',
        healthMetrics.value.cpuUsage,
        thresholds.cpuUsageWarning,
        thresholds.cpuUsageCritical,
        true
      )

      checkDatabaseHealth(healthMetrics.value.databaseStatus)

    } catch (err: any) {
      console.error('Error fetching system health:', err)
      error.value = err.message || 'Failed to fetch system health'
    } finally {
      loading.value = false
    }
  }

  // Computed health status
  const overallHealth = computed(() => {
    if (!healthMetrics.value) return 'unknown'
    
    const { databaseStatus, apiResponseTime, memoryUsage, cpuUsage } = healthMetrics.value
    
    // Critical conditions
    if (
      databaseStatus === 'down' ||
      apiResponseTime >= DEFAULT_THRESHOLDS.apiResponseTimeCritical ||
      memoryUsage >= DEFAULT_THRESHOLDS.memoryUsageCritical ||
      cpuUsage >= DEFAULT_THRESHOLDS.cpuUsageCritical
    ) {
      return 'critical'
    }

    // Degraded conditions
    if (
      databaseStatus === 'degraded' ||
      apiResponseTime >= DEFAULT_THRESHOLDS.apiResponseTimeWarning ||
      memoryUsage >= DEFAULT_THRESHOLDS.memoryUsageWarning ||
      cpuUsage >= DEFAULT_THRESHOLDS.cpuUsageWarning
    ) {
      return 'degraded'
    }

    return 'healthy'
  })

  return {
    healthMetrics: computed(() => healthMetrics.value),
    overallHealth,
    loading: computed(() => loading.value),
    error: computed(() => error.value),
    fetchSystemHealth
  }
}
