/**
 * Content Loader with Error Handling
 * 
 * Safely loads healthcare content with fallback support
 */

import { HealthcareContent } from './types';
import { healthcareCopy } from './healthcare-copy';
import { getFallbackCopy } from './fallback';

/**
 * Error types for content loading
 */
export class ContentLoadError extends Error {
  constructor(
    message: string,
    public readonly section?: string,
    public readonly language?: string
  ) {
    super(message);
    this.name = 'ContentLoadError';
  }
}

/**
 * Load healthcare content with error handling and fallback
 */
export async function loadHealthcareCopy(
  language: 'en' | 'ar' = 'en'
): Promise<HealthcareContent[typeof language]> {
  try {
    // Validate that the content exists
    if (!healthcareCopy || !healthcareCopy[language]) {
      throw new ContentLoadError(
        `Content not available for language: ${language}`,
        undefined,
        language
      );
    }

    // Validate critical sections exist
    const content = healthcareCopy[language];
    const criticalSections = ['hero', 'features', 'pricing'];
    
    for (const section of criticalSections) {
      if (!content[section as keyof typeof content]) {
        throw new ContentLoadError(
          `Critical section missing: ${section}`,
          section,
          language
        );
      }
    }

    return content;
  } catch (error) {
    console.error('Failed to load healthcare copy:', error);
    
    // Log error for monitoring
    if (typeof window !== 'undefined' && (window as any).gtag) {
      (window as any).gtag('event', 'exception', {
        description: `Content load error: ${error instanceof Error ? error.message : 'Unknown error'}`,
        fatal: false,
      });
    }

    // Return fallback content
    console.warn(`Using fallback content for language: ${language}`);
    return getFallbackCopy(language);
  }
}

/**
 * Load a specific section with fallback
 */
export async function loadContentSection<K extends keyof HealthcareContent['en']>(
  section: K,
  language: 'en' | 'ar' = 'en'
): Promise<HealthcareContent['en'][K]> {
  try {
    const content = await loadHealthcareCopy(language);
    
    if (!content[section]) {
      throw new ContentLoadError(
        `Section not found: ${section}`,
        section,
        language
      );
    }

    return content[section];
  } catch (error) {
    console.error(`Failed to load section ${section}:`, error);
    
    // Return fallback section
    const fallback = getFallbackCopy(language);
    return fallback[section];
  }
}

/**
 * Validate content structure
 */
export function validateContent(content: any): boolean {
  try {
    // Check for required top-level sections
    const requiredSections = ['hero', 'features', 'testimonials', 'pricing'];
    
    for (const section of requiredSections) {
      if (!content[section]) {
        console.warn(`Missing required section: ${section}`);
        return false;
      }
    }

    // Validate hero section
    if (!content.hero.headline || !content.hero.description) {
      console.warn('Invalid hero section structure');
      return false;
    }

    // Validate features section
    if (!Array.isArray(content.features.items) || content.features.items.length === 0) {
      console.warn('Invalid features section structure');
      return false;
    }

    // Validate pricing section
    if (!Array.isArray(content.pricing.tiers) || content.pricing.tiers.length === 0) {
      console.warn('Invalid pricing section structure');
      return false;
    }

    return true;
  } catch (error) {
    console.error('Content validation error:', error);
    return false;
  }
}
