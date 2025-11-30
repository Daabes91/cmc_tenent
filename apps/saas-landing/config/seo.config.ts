/**
 * SEO Configuration
 * Central configuration for all SEO-related metadata, keywords, and organization details
 */

export const seoConfig = {
  // Site Information
  siteName: 'Vireo Health - Clinic Management Software',
  siteUrl: process.env.NEXT_PUBLIC_SITE_URL || 'https://vireohealth.com',
  defaultTitle: 'Vireo Health | Complete Clinic Management Solution',
  titleTemplate: '%s | Vireo Health',
  defaultDescription:
    'Transform your clinic with Vireo Health - the all-in-one management platform for appointments, patients, billing, and more. Start your free trial today.',
  
  // Primary Keywords
  defaultKeywords: [
    'clinic management software',
    'medical practice management',
    'appointment scheduling software',
    'patient management system',
    'healthcare software',
    'clinic booking system',
    'medical billing software',
    'EMR software',
    'practice management solution',
    'healthcare practice software',
  ],

  // Author & Brand
  author: 'Vireo Health',
  creator: 'Vireo Health Team',

  // Social Media
  social: {
    twitter: '@vireohealth',
    twitterCreator: '@vireohealth',
    facebook: 'https://facebook.com/vireohealth',
    linkedin: 'https://linkedin.com/company/vireohealth',
    instagram: 'https://instagram.com/vireohealth',
    youtube: 'https://youtube.com/@vireohealth',
  },

  // Organization Schema Data
  organization: {
    name: 'Vireo Health',
    legalName: 'Vireo Health Inc.',
    url: process.env.NEXT_PUBLIC_SITE_URL || 'https://vireohealth.com',
    logo: `${process.env.NEXT_PUBLIC_SITE_URL || 'https://vireohealth.com'}/logo.png`,
    description:
      'Leading provider of comprehensive clinic management software solutions for healthcare practices worldwide.',
    foundingDate: '2024',
    founders: [],
    address: {
      streetAddress: '',
      addressLocality: '',
      addressRegion: '',
      postalCode: '',
      addressCountry: 'US',
    },
    contactPoint: {
      telephone: '',
      contactType: 'customer support',
      email: process.env.NEXT_PUBLIC_SALES_EMAIL || 'support@vireohealth.com',
      availableLanguage: ['English', 'Arabic'],
    },
  },

  // Product Information
  product: {
    name: 'Vireo Health Platform',
    description:
      'Complete clinic management solution with appointment scheduling, patient records, billing, and analytics',
    category: 'BusinessApplication',
    applicationCategory: 'Business Application',
    operatingSystem: 'Web-based, iOS, Android',
    features: [
      'Appointment Scheduling',
      'Patient Management',
      'Billing & Invoicing',
      'Multi-location Support',
      'Analytics & Reporting',
      'Mobile Access',
      'Secure Data Storage',
      'Customizable Workflows',
      'Multi-language Support',
      'HIPAA Compliant',
    ],
    offers: {
      price: '0',
      priceCurrency: 'USD',
      availability: 'https://schema.org/InStock',
      priceValidUntil: '2025-12-31',
    },
  },

  // Locale & Language
  locale: 'en_US',
  alternateLocales: ['ar_SA'],

  // Open Graph Defaults
  openGraph: {
    type: 'website',
    locale: 'en_US',
    siteName: 'Vireo Health',
    images: {
      width: 1200,
      height: 630,
      type: 'image/png',
    },
  },

  // Twitter Card Defaults
  twitter: {
    card: 'summary_large_image' as const,
    site: '@vireohealth',
    creator: '@vireohealth',
  },

  // Robots
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      'max-video-preview': -1,
      'max-image-preview': 'large' as const,
      'max-snippet': -1,
    },
  },

  // Verification
  verification: {
    google: process.env.NEXT_PUBLIC_GOOGLE_SITE_VERIFICATION || '',
    yandex: process.env.NEXT_PUBLIC_YANDEX_VERIFICATION || '',
    bing: process.env.NEXT_PUBLIC_BING_VERIFICATION || '',
  },
};

export type SeoConfig = typeof seoConfig;
