/**
 * Title handling utilities for admin-nuxt application
 * Provides robust fallback strategies for browser tab titles
 */

/**
 * Default titles for admin application
 */
const DEFAULT_ADMIN_TITLES = {
  base: 'Admin',
  fallbackPage: 'Dashboard',
  separator: ' | '
};

/**
 * Format admin title with fallback handling
 * 
 * @param pageName - The current page name
 * @param clinicName - Optional clinic name (not used in admin format)
 * @returns Formatted title in "Admin | Page Name" format
 */
export function formatAdminTitle(
  pageName?: string | null,
  clinicName?: string | null // Not used but kept for consistency
): string {
  const safePage = pageName?.trim() || DEFAULT_ADMIN_TITLES.fallbackPage;
  return `${DEFAULT_ADMIN_TITLES.base}${DEFAULT_ADMIN_TITLES.separator}${safePage}`;
}

/**
 * Get safe page name with fallback
 * 
 * @param pageName - The page name to validate
 * @returns Safe page name or fallback
 */
export function getSafePageName(pageName?: string | null): string {
  if (!pageName?.trim()) {
    return DEFAULT_ADMIN_TITLES.fallbackPage;
  }
  
  // Sanitize page name (remove excessive whitespace, limit length)
  const sanitized = pageName.trim().replace(/\s+/g, ' ');
  
  // Limit title length to prevent browser tab overflow
  const maxLength = 50;
  if (sanitized.length > maxLength) {
    return sanitized.substring(0, maxLength - 3) + '...';
  }
  
  return sanitized;
}

/**
 * Validate and format title for admin application
 * Handles all edge cases and provides appropriate fallbacks
 * 
 * @param pageName - Current page name
 * @param options - Additional options for title formatting
 * @returns Safe, formatted title
 */
export function createSafeAdminTitle(
  pageName?: string | null,
  options: {
    maxLength?: number;
    fallbackPage?: string;
  } = {}
): string {
  const {
    maxLength = 100,
    fallbackPage = DEFAULT_ADMIN_TITLES.fallbackPage
  } = options;
  
  const safePage = pageName?.trim() || fallbackPage;
  const title = formatAdminTitle(safePage);
  
  // Ensure total title length doesn't exceed browser limits
  if (title.length > maxLength) {
    const availablePageLength = maxLength - DEFAULT_ADMIN_TITLES.base.length - DEFAULT_ADMIN_TITLES.separator.length - 3;
    const truncatedPage = safePage.substring(0, availablePageLength) + '...';
    return formatAdminTitle(truncatedPage);
  }
  
  return title;
}

/**
 * Handle title updates with error recovery
 * 
 * @param pageName - Current page name
 * @param onError - Optional error handler
 * @returns Safe title or fallback
 */
export function handleTitleUpdate(
  pageName?: string | null,
  onError?: (error: Error) => void
): string {
  try {
    return createSafeAdminTitle(pageName);
  } catch (error) {
    if (onError) {
      onError(error instanceof Error ? error : new Error('Unknown title error'));
    }
    
    // Return absolute fallback
    return formatAdminTitle(DEFAULT_ADMIN_TITLES.fallbackPage);
  }
}