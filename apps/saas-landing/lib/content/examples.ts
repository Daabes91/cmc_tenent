/**
 * Usage examples for the Healthcare Content Configuration System
 * 
 * These examples demonstrate how to use the content configuration
 * in various scenarios throughout the application.
 */

import { healthcareCopy, getHealthcareCopy, getHealthcareSection, useHealthcareCopy } from './index';
import type { Language } from './types';

// ============================================================================
// Example 1: Direct Access to Content
// ============================================================================

export function example1_DirectAccess() {
  // Access English hero content directly
  const heroEn = healthcareCopy.en.hero;
  console.log('Hero Badge (EN):', heroEn.badge);
  console.log('Hero Headline (EN):', heroEn.headline.prefix);
  
  // Access Arabic hero content directly
  const heroAr = healthcareCopy.ar.hero;
  console.log('Hero Badge (AR):', heroAr.badge);
  console.log('Hero Headline (AR):', heroAr.headline.prefix);
}

// ============================================================================
// Example 2: Using Language Parameter
// ============================================================================

export function example2_WithLanguageParameter(language: Language) {
  const copy = getHealthcareCopy(language);
  
  return {
    heroHeadline: copy.hero.headline.prefix,
    heroDescription: copy.hero.description,
    ctaPrimary: copy.hero.ctaPrimary,
    ctaSecondary: copy.hero.ctaSecondary
  };
}

// ============================================================================
// Example 3: Getting Specific Sections
// ============================================================================

export function example3_SpecificSections(language: Language) {
  // Get only the features section
  const features = getHealthcareSection(language, 'features');
  
  // Get only the testimonials section
  const testimonials = getHealthcareSection(language, 'testimonials');
  
  // Get only the pricing section
  const pricing = getHealthcareSection(language, 'pricing');
  
  return { features, testimonials, pricing };
}

// ============================================================================
// Example 4: React Component Usage
// ============================================================================

/**
 * Example React component using the content configuration
 * 
 * Note: This is a TypeScript example, not actual JSX
 */
export function Example4ReactComponent() {
  // In a real component, you would use:
  // const { language } = useLanguage();
  const language: Language = 'en'; // Example
  
  const copy = useHealthcareCopy(language);
  
  // Component would render:
  return {
    badge: copy.hero.badge,
    headline: copy.hero.headline.prefix,
    highlight: copy.hero.headline.highlight,
    description: copy.hero.description,
    buttons: {
      primary: copy.hero.ctaPrimary,
      secondary: copy.hero.ctaSecondary
    }
  };
}

// ============================================================================
// Example 5: Iterating Over Features
// ============================================================================

export function example5_IterateFeatures(language: Language) {
  const features = getHealthcareSection(language, 'features');
  
  // Map over features to create a list
  return features.items.map((feature, index) => ({
    id: index,
    icon: feature.icon,
    title: feature.title,
    description: feature.description,
    benefitCount: feature.benefits.length,
    firstBenefit: feature.benefits[0]
  }));
}

// ============================================================================
// Example 6: Filtering Testimonials
// ============================================================================

export function example6_FilterTestimonials(language: Language, clinicType?: string) {
  const testimonials = getHealthcareSection(language, 'testimonials');
  
  // Filter testimonials by clinic type if provided
  if (clinicType) {
    return testimonials.items.filter(
      t => t.clinicType.toLowerCase().includes(clinicType.toLowerCase())
    );
  }
  
  // Return all testimonials
  return testimonials.items;
}

// ============================================================================
// Example 7: Getting Pricing Tiers
// ============================================================================

export function example7_PricingTiers(language: Language) {
  const pricing = getHealthcareSection(language, 'pricing');
  
  // Get the popular tier
  const popularTier = pricing.tiers.find(tier => tier.popular);
  
  // Get tiers sorted by monthly price
  const sortedTiers = [...pricing.tiers].sort((a, b) => {
    const priceA = a.price.monthly ?? Infinity;
    const priceB = b.price.monthly ?? Infinity;
    return priceA - priceB;
  });
  
  return {
    popularTier,
    sortedTiers,
    tierCount: pricing.tiers.length
  };
}

// ============================================================================
// Example 8: Integration Categories
// ============================================================================

export function example8_IntegrationsByCategory(language: Language) {
  const integrations = getHealthcareSection(language, 'integrations');
  
  // Group integrations by category
  const byCategory = integrations.items.reduce((acc, integration) => {
    const category = integration.category;
    if (!acc[category]) {
      acc[category] = [];
    }
    acc[category].push(integration);
    return acc;
  }, {} as Record<string, typeof integrations.items>);
  
  return byCategory;
}

// ============================================================================
// Example 9: Security Badges
// ============================================================================

export function example9_SecurityInfo(language: Language) {
  const security = getHealthcareSection(language, 'security');
  
  return {
    title: security.title,
    description: security.description,
    badgeCount: security.badges.length,
    badges: security.badges,
    trustIndicatorCount: security.trustIndicators.length,
    trustIndicators: security.trustIndicators
  };
}

// ============================================================================
// Example 10: Bilingual Comparison
// ============================================================================

export function example10_BilingualComparison() {
  const enCopy = getHealthcareCopy('en');
  const arCopy = getHealthcareCopy('ar');
  
  return {
    english: {
      heroHeadline: enCopy.hero.headline.prefix,
      featureCount: enCopy.features.items.length,
      testimonialCount: enCopy.testimonials.items.length,
      pricingTierCount: enCopy.pricing.tiers.length
    },
    arabic: {
      heroHeadline: arCopy.hero.headline.prefix,
      featureCount: arCopy.features.items.length,
      testimonialCount: arCopy.testimonials.items.length,
      pricingTierCount: arCopy.pricing.tiers.length
    }
  };
}

// ============================================================================
// Example 11: Content Validation
// ============================================================================

export function example11_ValidateContent(language: Language): {
  valid: boolean;
  errors: string[];
} {
  const errors: string[] = [];
  const copy = getHealthcareCopy(language);
  
  // Validate hero section
  if (!copy.hero.headline.prefix) {
    errors.push('Hero headline prefix is missing');
  }
  if (!copy.hero.description) {
    errors.push('Hero description is missing');
  }
  
  // Validate features
  if (copy.features.items.length === 0) {
    errors.push('No features defined');
  }
  copy.features.items.forEach((feature, index) => {
    if (!feature.title) {
      errors.push(`Feature ${index} is missing a title`);
    }
    if (feature.benefits.length === 0) {
      errors.push(`Feature ${index} has no benefits`);
    }
  });
  
  // Validate testimonials
  if (copy.testimonials.items.length === 0) {
    errors.push('No testimonials defined');
  }
  copy.testimonials.items.forEach((testimonial, index) => {
    if (!testimonial.name || !testimonial.role || !testimonial.clinicType) {
      errors.push(`Testimonial ${index} is missing required fields`);
    }
  });
  
  // Validate pricing
  if (copy.pricing.tiers.length === 0) {
    errors.push('No pricing tiers defined');
  }
  
  return {
    valid: errors.length === 0,
    errors
  };
}

// ============================================================================
// Example 12: Healthcare Terminology Check
// ============================================================================

export function example12_HealthcareTerminologyCheck(language: Language): {
  hasHealthcareTerms: boolean;
  hasGenericTerms: boolean;
  healthcareTermsFound: string[];
  genericTermsFound: string[];
} {
  const copy = getHealthcareCopy(language);
  const content = JSON.stringify(copy).toLowerCase();
  
  // Healthcare-specific terms we want to see
  const healthcareTerms = ['patient', 'appointment', 'clinic', 'provider', 'treatment', 'doctor', 'medical'];
  const healthcareTermsFound = healthcareTerms.filter(term => content.includes(term));
  
  // Generic SaaS terms we want to avoid
  const genericTerms = ['user', 'workspace', 'project', 'dashboard', 'customer'];
  const genericTermsFound = genericTerms.filter(term => content.includes(term));
  
  return {
    hasHealthcareTerms: healthcareTermsFound.length > 0,
    hasGenericTerms: genericTermsFound.length > 0,
    healthcareTermsFound,
    genericTermsFound
  };
}
