// Core type definitions for SAAS Admin Panel

export interface SaasManager {
  id: number
  email: string
  fullName: string
  status: 'ACTIVE' | 'SUSPENDED'
}

export interface Tenant {
  id: number
  slug: string
  name: string
  customDomain: string | null
  status: 'ACTIVE' | 'INACTIVE'
  createdAt: string | number  // Can be ISO string or Unix timestamp
  updatedAt: string | number  // Can be ISO string or Unix timestamp
  deletedAt: string | number | null  // Can be ISO string or Unix timestamp
}

export interface TenantMetrics {
  tenantId: number
  userCount: number
  staffCount: number
  patientCount: number
  appointmentCount: number
  storageUsedMB: number
  lastActivityAt: string
}

export interface SystemMetrics {
  totalTenants: number
  activeTenants: number
  totalUsers: number
  activeUsers: number
  apiResponseTime: number
  databaseStatus: 'healthy' | 'degraded' | 'down'
  recentActivity: Activity[]
}

export interface Activity {
  id: string
  timestamp: string
  type: 'tenant_created' | 'tenant_updated' | 'tenant_deleted'
  description: string
  managerName: string
}

export interface AuditLog {
  id: string
  timestamp: string
  managerName: string
  managerEmail: string
  action: 'TENANT_CREATED' | 'TENANT_UPDATED' | 'TENANT_DELETED' | 'TENANT_STATUS_CHANGED'
  tenantId: number
  tenantSlug: string
  details: Record<string, any>
}

export interface BrandingConfig {
  primaryColor: string
  secondaryColor: string
  logoUrl: string | null
}

export interface TenantFormData {
  slug: string
  name: string
  customDomain?: string
  status?: 'ACTIVE' | 'INACTIVE'
}

export interface TenantCreateResponse {
  id: number
  slug: string
  name: string
  customDomain: string | null
  status: string
  createdAt: string
  adminCredentials: {
    email: string
    password: string
  }
}

export interface TimeRange {
  label: string
  value: string
}

export interface AnalyticsData {
  tenantGrowth: {
    labels: string[]
    totalTenants: number[]
    newTenants: number[]
  }
  usageMetrics: {
    labels: string[]
    users: number[]
    appointments: number[]
    storage: number[]
  }
  summary: {
    totalTenants: number
    totalUsers: number
    totalAppointments: number
    storageUsedMB: number
  }
}

export interface AnalyticsParams {
  timeRange?: '7d' | '30d' | '90d' | 'custom'
  startDate?: string
  endDate?: string
}

export interface AuditLogFilters {
  startDate?: string
  endDate?: string
  actionType?: string
  managerId?: number
  tenantId?: number
  page?: number
  size?: number
}
