/**
 * Library Module Exports
 * 
 * This module provides a centralized export point for commonly used library functions and types.
 * Import from this module for better organization and easier refactoring.
 * 
 * @module lib
 */

// Theme-related exports
export type {
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
  TenantTheme,
  ThemeLayoutProps,
  ThemeLayout,
  ThemeHeaderProps,
  ThemeFooterProps,
  ThemeNavigationProps,
  AvailableThemeKey
} from './theme-types'

export {
  isThemeStatus,
  isTenantStatus,
  isAvailableThemeKey
} from './theme-types'

export {
  getTenantTheme,
  getDefaultTheme,
  validateThemeExists
} from './theme'

// Re-export commonly used utilities
export { prisma } from './prisma'
export { TENANT_HEADER } from './tenant'
