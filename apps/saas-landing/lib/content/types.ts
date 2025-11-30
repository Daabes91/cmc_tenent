/**
 * Type definitions for healthcare content configuration
 * Supports bilingual content (English and Arabic)
 */

export interface HeroContent {
  badge: string;
  headline: {
    prefix: string;
    highlight: string;
  };
  description: string;
  ctaPrimary: string;
  ctaSecondary: string;
  trustLabel: string;
  companyLogos: string[];
}

export interface FeatureContent {
  icon: string;
  title: string;
  description: string;
  benefits: string[];
}

export interface TestimonialContent {
  name: string;
  role: string;
  clinicType: string;
  quote: string;
  metric?: string;
  avatar?: string;
}

export interface PricingTier {
  name: string;
  description: string;
  price: {
    monthly: number | null;
    annual: number | null;
  };
  features: string[];
  limits: {
    providers?: number;
    patients?: number;
    appointments?: number;
  };
  cta: string;
  popular?: boolean;
}

export interface IntegrationContent {
  name: string;
  description: string;
  logo: string;
  category: 'payment' | 'calendar' | 'email' | 'accounting' | 'other';
}

export interface SecurityContent {
  title: string;
  description: string;
  badges: string[];
  trustIndicators: string[];
}

export interface HealthcareCopy {
  hero: HeroContent;
  features: {
    title: string;
    subtitle: string;
    items: FeatureContent[];
  };
  testimonials: {
    title: string;
    subtitle: string;
    items: TestimonialContent[];
  };
  pricing: {
    title: string;
    subtitle: string;
    tiers: PricingTier[];
  };
  integrations: {
    title: string;
    subtitle: string;
    items: IntegrationContent[];
    ctaText: string;
  };
  security: SecurityContent;
}

export type Language = 'en' | 'ar';

export type HealthcareContent = {
  [K in Language]: HealthcareCopy;
};
