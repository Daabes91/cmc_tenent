import { dbQuery } from './db'
import type { TenantTheme } from './theme-types'

/**
 * Theme Lookup Service
 * 
 * This module provides functions for retrieving tenant theme information from the database.
 * It handles theme resolution, fallback logic, and error handling for the theme system.
 * 
 * @module lib/theme
 */

/**
 * Retrieves the theme information for a given tenant slug
 * 
 * This function queries the database to find the tenant and their selected theme.
 * It returns the theme key, ID, and name needed to dynamically load the theme layout.
 * 
 * @param {string} slug - The tenant's unique slug identifier (e.g., "clinic-a")
 * @returns {Promise<TenantTheme | null>} TenantTheme object if found, null if tenant or theme not found
 * 
 * @example
 * ```typescript
 * const theme = await getTenantTheme('clinic-a')
 * if (theme) {
 *   console.log(`Loading theme: ${theme.themeKey}`)
 *   // theme.themeKey can be used for dynamic import
 * }
 * ```
 * 
 * @throws Does not throw - errors are logged and null is returned
 */
export async function getTenantTheme(slug: string): Promise<TenantTheme | null> {
  try {
    const result = await dbQuery<{
      themeKey: string | null
      themeId: string | null
      themeName: string | null
    }>(
      `
        SELECT th.key AS "themeKey",
               th.id AS "themeId",
               th.name AS "themeName"
        FROM tenants t
        LEFT JOIN "Theme" th ON th.id = t."themeId"
        WHERE t.slug = $1
        LIMIT 1
      `,
      [slug]
    )

    const row = result.rows[0]

    if (!row || !row.themeId) {
      return null
    }
    
    return {
      themeKey: row.themeKey ?? 'default',
      themeId: row.themeId,
      themeName: row.themeName ?? 'Default Theme'
    }
  } catch (error) {
    console.error(`Error fetching theme for tenant ${slug}:`, error)
    return null
  }
}

/**
 * Returns the default theme configuration as a fallback
 * 
 * This function provides a default theme when:
 * - A tenant has not selected a theme
 * - The tenant's selected theme cannot be found
 * - An error occurs during theme lookup
 * 
 * The default theme should always exist in the themes directory.
 * 
 * @returns {Promise<TenantTheme>} Default TenantTheme object with key "default"
 * 
 * @example
 * ```typescript
 * const theme = await getTenantTheme(slug) || await getDefaultTheme()
 * // Ensures a theme is always available
 * ```
 */
export async function getDefaultTheme(): Promise<TenantTheme> {
  return {
    themeKey: 'default',
    themeId: 'default',
    themeName: 'Default Theme'
  }
}

/**
 * Validates that a theme key corresponds to an existing theme directory
 * 
 * This function checks if a theme layout file exists for the given theme key.
 * It's useful for validating theme keys before attempting dynamic imports.
 * 
 * @param {string} themeKey - The theme key to validate
 * @returns {Promise<boolean>} True if theme exists, false otherwise
 * 
 * @example
 * ```typescript
 * if (await validateThemeExists('clinic')) {
 *   // Safe to import theme
 * }
 * ```
 */
export async function validateThemeExists(themeKey: string): Promise<boolean> {
  try {
    // Attempt to dynamically import the theme layout
    await import(`@/themes/${themeKey}/layout`)
    return true
  } catch {
    return false
  }
}
