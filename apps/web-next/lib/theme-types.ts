/**
 * Theme System Type Definitions
 * 
 * This module contains TypeScript type definitions for the manual theme selection system.
 * These types are derived from the Prisma schema and provide type safety for theme-related operations.
 */

import { ReactNode } from 'react'

// ============================================================================
// Prisma Model Types
// ============================================================================

/**
 * Theme model representing a predefined theme in the database
 * 
 * @property {string} id - Unique identifier (cuid)
 * @property {string} key - Unique theme key used for file system lookup (e.g., "clinic", "barber")
 * @property {string} name - Display name of the theme
 * @property {ThemeStatus} status - Publication status of the theme
 * @property {Date} createdAt - Timestamp when theme was created
 * @property {Date} updatedAt - Timestamp when theme was last updated
 */
export interface Theme {
  id: string
  key: string
  name: string
  status: ThemeStatus
  createdAt: Date
  updatedAt: Date
}

/**
 * Tenant model representing a clinic or organization
 * 
 * @property {number} id - Unique identifier
 * @property {string} slug - Unique slug for subdomain resolution
 * @property {string} name - Display name of the tenant
 * @property {TenantStatus} status - Current status of the tenant
 * @property {string | null} customDomain - Optional custom domain
 * @property {string | null} domain - Optional domain for custom domain resolution
 * @property {string | null} themeId - Foreign key to selected theme
 * @property {Date} createdAt - Timestamp when tenant was created
 * @property {Date} updatedAt - Timestamp when tenant was last updated
 * @property {Date | null} deletedAt - Soft delete timestamp
 */
export interface Tenant {
  id: number
  slug: string
  name: string
  status: TenantStatus
  customDomain: string | null
  domain: string | null
  themeId: string | null
  createdAt: Date
  updatedAt: Date
  deletedAt: Date | null
}

/**
 * Tenant with theme relation included
 */
export interface TenantWithTheme extends Tenant {
  theme: Theme | null
}

// ============================================================================
// Enum Types
// ============================================================================

/**
 * Theme publication status
 * 
 * - `published`: Theme is available for selection by tenants
 * - `draft`: Theme is in development and not available for selection
 */
export type ThemeStatus = 'published' | 'draft'

/**
 * Tenant status
 * 
 * - `ACTIVE`: Tenant is active and operational
 * - `SUSPENDED`: Tenant is temporarily suspended
 * - `INACTIVE`: Tenant is inactive
 */
export type TenantStatus = 'ACTIVE' | 'SUSPENDED' | 'INACTIVE'

// ============================================================================
// API Response Types
// ============================================================================

/**
 * Response type for GET /api/themes endpoint
 * Returns minimal theme information for selection UI
 */
export interface ThemeListItem {
  id: string
  key: string
  name: string
}

/**
 * Response type for GET /api/internal/tenant endpoint
 * Returns current tenant information including theme selection
 */
export interface TenantInfo {
  slug: string
  themeId: string | null
}

/**
 * Request body type for POST /api/tenants/[slug]/theme endpoint
 * Used to update a tenant's theme selection
 */
export interface UpdateThemeRequest {
  themeId: string
}

/**
 * Response type for POST /api/tenants/[slug]/theme endpoint
 * Returns updated tenant information after theme change
 */
export interface UpdateThemeResponse {
  id: number
  slug: string
  themeId: string | null
  updatedAt: Date
}

/**
 * Error response type for API endpoints
 */
export interface ApiErrorResponse {
  error: string
  message?: string
}

// ============================================================================
// Theme Service Types
// ============================================================================

/**
 * Tenant theme information returned by theme lookup service
 * Contains the essential information needed to load a theme
 * 
 * @property {string} themeKey - The theme key used for dynamic import
 * @property {string} themeId - The theme's database ID
 * @property {string} themeName - The theme's display name
 */
export interface TenantTheme {
  themeKey: string
  themeId: string
  themeName: string
}

// ============================================================================
// Theme Layout Types
// ============================================================================

/**
 * Props interface for theme layout components
 * All theme layouts must implement this interface
 * 
 * @property {ReactNode} children - The page content to be wrapped by the theme layout
 * @property {string} locale - The current locale (e.g., 'en', 'ar')
 * @property {string} tenantSlug - The tenant slug identifier
 * 
 * @example
 * ```tsx
 * export default function ClinicLayout({ children, locale, tenantSlug }: ThemeLayoutProps) {
 *   return (
 *     <div className="clinic-theme">
 *       <Header locale={locale} tenantSlug={tenantSlug} />
 *       <main>{children}</main>
 *       <Footer locale={locale} />
 *     </div>
 *   )
 * }
 * ```
 */
export interface ThemeLayoutProps {
  children: ReactNode
  locale: string
  tenantSlug: string
}

/**
 * Type definition for theme layout components
 * Theme layouts are React components that accept ThemeLayoutProps
 */
export type ThemeLayout = React.ComponentType<ThemeLayoutProps>

// ============================================================================
// Theme Component Types
// ============================================================================

/**
 * Props for theme header components
 */
export interface ThemeHeaderProps {
  locale: string
  tenantSlug: string
}

/**
 * Props for theme footer components
 */
export interface ThemeFooterProps {
  locale: string
}

/**
 * Props for theme navigation components
 */
export interface ThemeNavigationProps {
  locale: string
  tenantSlug: string
}

// ============================================================================
// Utility Types
// ============================================================================

/**
 * Type guard to check if a value is a valid theme status
 * 
 * @param value - Value to check
 * @returns True if value is a valid ThemeStatus
 */
export function isThemeStatus(value: unknown): value is ThemeStatus {
  return value === 'published' || value === 'draft'
}

/**
 * Type guard to check if a value is a valid tenant status
 * 
 * @param value - Value to check
 * @returns True if value is a valid TenantStatus
 */
export function isTenantStatus(value: unknown): value is TenantStatus {
  return value === 'ACTIVE' || value === 'SUSPENDED' || value === 'INACTIVE'
}

/**
 * Type for theme keys that exist in the file system
 * This should be updated when new themes are added
 */
export type AvailableThemeKey = 'default' | 'clinic' | 'barber'

/**
 * Type guard to check if a theme key is available
 * 
 * @param key - Theme key to check
 * @returns True if theme key is available
 */
export function isAvailableThemeKey(key: string): key is AvailableThemeKey {
  return key === 'default' || key === 'clinic' || key === 'barber'
}
