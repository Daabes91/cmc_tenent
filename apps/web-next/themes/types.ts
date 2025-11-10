/**
 * Theme Types Module
 * 
 * This module re-exports theme-related types from lib/theme-types.
 * It maintains backward compatibility while centralizing type definitions.
 * 
 * @deprecated Import from '@/lib/theme-types' instead for better organization
 * @module themes/types
 */

// Re-export all theme-related types from the centralized location
export type {
  ThemeLayoutProps,
  ThemeLayout,
  ThemeHeaderProps,
  ThemeFooterProps,
  ThemeNavigationProps,
  TenantTheme,
  Theme,
  Tenant,
  TenantWithTheme,
  ThemeStatus,
  TenantStatus,
  ThemeListItem,
  TenantInfo,
  UpdateThemeRequest,
  UpdateThemeResponse,
  ApiErrorResponse,
  AvailableThemeKey
} from '@/lib/theme-types'

export {
  isThemeStatus,
  isTenantStatus,
  isAvailableThemeKey
} from '@/lib/theme-types'
