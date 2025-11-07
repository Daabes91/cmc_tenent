/**
 * Title handling utilities for web-next application
 * Provides robust fallback strategies for browser tab titles
 */

/**
 * Default titles for web application
 */
const DEFAULT_WEB_TITLES = {
  clinicName: 'Clinic',
  fallbackPage: 'Home',
  separator: ' | ',
  maxLength: 100
};

/**
 * Format web title with fallback handling
 * 
 * @param pageTitle - The current page title
 * @param clinicName - The clinic name from settings
 * @returns Formatted title in "Clinic Name | Page Title" format
 */
export function formatWebTitle(
  pageTitle?: string | null,
  clinicName?: string | null
): string {
  const safePage = pageTitle?.trim() || DEFAULT_WEB_TITLES.fallbackPage;
  const safeClinic = clinicName?.trim() || DEFAULT_WEB_TITLES.clinicName;
  
  return `${safeClinic}${DEFAULT_WEB_TITLES.separator}${safePage}`;
}

/**
 * Get safe clinic name with fallback
 * 
 * @param clinicName - The clinic name to validate
 * @returns Safe clinic name or fallback
 */
export function getSafeClinicName(clinicName?: string | null): string {
  if (!clinicName?.trim()) {
    return DEFAULT_WEB_TITLES.clinicName;
  }
  
  // Sanitize clinic name (remove excessive whitespace, limit length)
  const sanitized = clinicName.trim().replace(/\s+/g, ' ');
  
  // Limit clinic name length to prevent browser tab overflow
  const maxLength = 40;
  if (sanitized.length > maxLength) {
    return sanitized.substring(0, maxLength - 3) + '...';
  }
  
  return sanitized;
}

/**
 * Get safe page title with fallback
 * 
 * @param pageTitle - The page title to validate
 * @returns Safe page title or fallback
 */
export function getSafePageTitle(pageTitle?: string | null): string {
  if (!pageTitle?.trim()) {
    return DEFAULT_WEB_TITLES.fallbackPage;
  }
  
  // Sanitize page title (remove excessive whitespace, limit length)
  const sanitized = pageTitle.trim().replace(/\s+/g, ' ');
  
  // Limit page title length
  const maxLength = 50;
  if (sanitized.length > maxLength) {
    return sanitized.substring(0, maxLength - 3) + '...';
  }
  
  return sanitized;
}

/**
 * Validate and format title for web application
 * Handles all edge cases and provides appropriate fallbacks
 * 
 * @param pageTitle - Current page title
 * @param clinicName - Clinic name from settings
 * @param options - Additional options for title formatting
 * @returns Safe, formatted title
 */
export function createSafeWebTitle(
  pageTitle?: string | null,
  clinicName?: string | null,
  options: {
    maxLength?: number;
    fallbackClinic?: string;
    fallbackPage?: string;
  } = {}
): string {
  const {
    maxLength = DEFAULT_WEB_TITLES.maxLength,
    fallbackClinic = DEFAULT_WEB_TITLES.clinicName,
    fallbackPage = DEFAULT_WEB_TITLES.fallbackPage
  } = options;
  
  const safeClinic = getSafeClinicName(clinicName) || fallbackClinic;
  const safePage = getSafePageTitle(pageTitle) || fallbackPage;
  
  const title = formatWebTitle(safePage, safeClinic);
  
  // Ensure total title length doesn't exceed browser limits
  if (title.length > maxLength) {
    // Try to truncate page title first
    const baseLength = safeClinic.length + DEFAULT_WEB_TITLES.separator.length;
    const availablePageLength = maxLength - baseLength - 3;
    
    if (availablePageLength > 10) {
      const truncatedPage = safePage.substring(0, availablePageLength) + '...';
      return formatWebTitle(truncatedPage, safeClinic);
    }
    
    // If still too long, truncate clinic name too
    const maxClinicLength = Math.floor((maxLength - DEFAULT_WEB_TITLES.separator.length - 10) / 2);
    const truncatedClinic = safeClinic.substring(0, maxClinicLength) + '...';
    const truncatedPage = safePage.substring(0, maxClinicLength) + '...';
    
    return formatWebTitle(truncatedPage, truncatedClinic);
  }
  
  return title;
}

/**
 * Handle title updates with error recovery
 * 
 * @param pageTitle - Current page title
 * @param clinicName - Clinic name from settings
 * @param onError - Optional error handler
 * @returns Safe title or fallback
 */
export function handleTitleUpdate(
  pageTitle?: string | null,
  clinicName?: string | null,
  onError?: (error: Error) => void
): string {
  try {
    return createSafeWebTitle(pageTitle, clinicName);
  } catch (error) {
    if (onError) {
      onError(error instanceof Error ? error : new Error('Unknown title error'));
    }
    
    // Return absolute fallback
    return formatWebTitle(DEFAULT_WEB_TITLES.fallbackPage, DEFAULT_WEB_TITLES.clinicName);
  }
}

/**
 * Enhanced title formatting that includes locale-aware fallbacks
 * 
 * @param pageTitle - Current page title
 * @param clinicName - Clinic name from settings
 * @param locale - Current locale (en/ar)
 * @returns Localized, safe title
 */
export function createLocalizedWebTitle(
  pageTitle?: string | null,
  clinicName?: string | null,
  locale: string = 'en'
): string {
  const localizedFallbacks = {
    en: {
      clinicName: 'Clinic',
      fallbackPage: 'Home'
    },
    ar: {
      clinicName: 'العيادة',
      fallbackPage: 'الرئيسية'
    }
  };
  
  const fallbacks = localizedFallbacks[locale as keyof typeof localizedFallbacks] || localizedFallbacks.en;
  
  return createSafeWebTitle(pageTitle, clinicName, {
    fallbackClinic: fallbacks.clinicName,
    fallbackPage: fallbacks.fallbackPage
  });
}