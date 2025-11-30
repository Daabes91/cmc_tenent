/**
 * Healthcare Content Configuration System
 * 
 * This module provides centralized content management for the SaaS landing page
 * with healthcare-specific terminology and bilingual support (English and Arabic).
 * 
 * Usage:
 * ```typescript
 * import { healthcareCopy, useHealthcareCopy } from '@/lib/content';
 * 
 * // Direct access
 * const heroContent = healthcareCopy.en.hero;
 * 
 * // With language context
 * const copy = useHealthcareCopy(language);
 * ```
 */

export * from './types';
export * from './healthcare-copy';

import { healthcareCopy } from './healthcare-copy';
import type { Language, HealthcareCopy } from './types';

/**
 * Get healthcare copy for a specific language
 * @param language - The language code ('en' or 'ar')
 * @returns Healthcare copy for the specified language
 */
export function getHealthcareCopy(language: Language): HealthcareCopy {
  return healthcareCopy[language];
}

/**
 * Hook-style function to get healthcare copy based on current language
 * Can be used in components that have access to language context
 * @param language - The current language
 * @returns Healthcare copy for the current language
 */
export function useHealthcareCopy(language: Language): HealthcareCopy {
  return getHealthcareCopy(language);
}

/**
 * Get a specific section of healthcare copy
 * @param language - The language code
 * @param section - The section name (e.g., 'hero', 'features', 'testimonials')
 * @returns The requested section content
 */
export function getHealthcareSection<K extends keyof HealthcareCopy>(
  language: Language,
  section: K
): HealthcareCopy[K] {
  return healthcareCopy[language][section];
}
