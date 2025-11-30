/**
 * SEO Type Definitions
 * TypeScript interfaces for metadata management
 */

import type { Metadata } from 'next';

/**
 * Open Graph Image Configuration
 */
export interface OpenGraphImage {
  url: string;
  width?: number;
  height?: number;
  alt?: string;
  type?: string;
  secureUrl?: string;
}

/**
 * Open Graph Metadata
 */
export interface OpenGraphMetadata {
  title: string;
  description: string;
  url: string;
  siteName: string;
  images: OpenGraphImage[];
  locale: string;
  type: 'website' | 'article' | 'product' | 'profile';
  alternateLocale?: string[];
  publishedTime?: string;
  modifiedTime?: string;
  authors?: string[];
  section?: string;
  tags?: string[];
}

/**
 * Twitter Card Metadata
 */
export interface TwitterMetadata {
  card: 'summary' | 'summary_large_image' | 'app' | 'player';
  title: string;
  description: string;
  images: string[];
  creator?: string;
  site?: string;
  creatorId?: string;
  siteId?: string;
}

/**
 * Page Metadata Configuration
 */
export interface PageMetadata {
  title: string;
  description: string;
  keywords: string[];
  canonical: string;
  openGraph: OpenGraphMetadata;
  twitter: TwitterMetadata;
  alternates?: {
    canonical?: string;
    languages?: Record<string, string>;
  };
  robots?: {
    index?: boolean;
    follow?: boolean;
    nocache?: boolean;
    googleBot?: {
      index?: boolean;
      follow?: boolean;
      noimageindex?: boolean;
      'max-video-preview'?: number;
      'max-image-preview'?: 'none' | 'standard' | 'large';
      'max-snippet'?: number;
    };
  };
  authors?: Array<{ name: string; url?: string }>;
  creator?: string;
  publisher?: string;
  category?: string;
  classification?: string;
}

/**
 * Blog Post Metadata
 */
export interface BlogPostMetadata extends PageMetadata {
  publishedTime: string;
  modifiedTime?: string;
  authors: Array<{ name: string; url?: string }>;
  section?: string;
  tags?: string[];
}

/**
 * Schema.org Organization
 */
export interface OrganizationSchema {
  '@context': 'https://schema.org';
  '@type': 'Organization';
  name: string;
  legalName?: string;
  url: string;
  logo: string;
  description: string;
  foundingDate?: string;
  founders?: Array<{
    '@type': 'Person';
    name: string;
  }>;
  address?: {
    '@type': 'PostalAddress';
    streetAddress?: string;
    addressLocality?: string;
    addressRegion?: string;
    postalCode?: string;
    addressCountry?: string;
  };
  contactPoint?: {
    '@type': 'ContactPoint';
    telephone?: string;
    contactType: string;
    email?: string;
    availableLanguage?: string[];
  };
  sameAs?: string[];
}

/**
 * Schema.org SoftwareApplication
 */
export interface SoftwareApplicationSchema {
  '@context': 'https://schema.org';
  '@type': 'SoftwareApplication';
  name: string;
  applicationCategory: string;
  operatingSystem: string;
  description: string;
  url: string;
  offers?: {
    '@type': 'Offer';
    price: string;
    priceCurrency: string;
    availability?: string;
    priceValidUntil?: string;
  };
  aggregateRating?: {
    '@type': 'AggregateRating';
    ratingValue: string;
    ratingCount: number;
    bestRating?: string;
    worstRating?: string;
  };
  features?: string[];
  screenshot?: string[];
  softwareVersion?: string;
  releaseNotes?: string;
  author?: {
    '@type': 'Organization';
    name: string;
  };
}

/**
 * Schema.org Article
 */
export interface ArticleSchema {
  '@context': 'https://schema.org';
  '@type': 'Article' | 'BlogPosting' | 'NewsArticle';
  headline: string;
  description: string;
  image: string | string[];
  datePublished: string;
  dateModified?: string;
  author: {
    '@type': 'Person' | 'Organization';
    name: string;
    url?: string;
  };
  publisher: {
    '@type': 'Organization';
    name: string;
    logo: {
      '@type': 'ImageObject';
      url: string;
    };
  };
  mainEntityOfPage?: {
    '@type': 'WebPage';
    '@id': string;
  };
  articleSection?: string;
  keywords?: string[];
  wordCount?: number;
}

/**
 * Schema.org FAQ
 */
export interface FAQSchema {
  '@context': 'https://schema.org';
  '@type': 'FAQPage';
  mainEntity: Array<{
    '@type': 'Question';
    name: string;
    acceptedAnswer: {
      '@type': 'Answer';
      text: string;
    };
  }>;
}

/**
 * Schema.org BreadcrumbList
 */
export interface BreadcrumbSchema {
  '@context': 'https://schema.org';
  '@type': 'BreadcrumbList';
  itemListElement: Array<{
    '@type': 'ListItem';
    position: number;
    name: string;
    item?: string;
  }>;
}

/**
 * Schema.org LocalBusiness
 */
export interface LocalBusinessSchema {
  '@context': 'https://schema.org';
  '@type': 'LocalBusiness';
  name: string;
  image: string;
  '@id': string;
  url: string;
  telephone?: string;
  address?: {
    '@type': 'PostalAddress';
    streetAddress?: string;
    addressLocality?: string;
    addressRegion?: string;
    postalCode?: string;
    addressCountry?: string;
  };
  geo?: {
    '@type': 'GeoCoordinates';
    latitude: number;
    longitude: number;
  };
  openingHoursSpecification?: Array<{
    '@type': 'OpeningHoursSpecification';
    dayOfWeek: string[];
    opens: string;
    closes: string;
  }>;
  priceRange?: string;
}

/**
 * Schema.org Product
 */
export interface ProductSchema {
  '@context': 'https://schema.org';
  '@type': 'Product';
  name: string;
  image: string | string[];
  description: string;
  brand?: {
    '@type': 'Brand';
    name: string;
  };
  offers?: {
    '@type': 'Offer';
    url: string;
    priceCurrency: string;
    price: string;
    availability: string;
    priceValidUntil?: string;
  };
  aggregateRating?: {
    '@type': 'AggregateRating';
    ratingValue: string;
    reviewCount: number;
  };
  review?: Array<{
    '@type': 'Review';
    reviewRating: {
      '@type': 'Rating';
      ratingValue: string;
      bestRating?: string;
    };
    author: {
      '@type': 'Person';
      name: string;
    };
    reviewBody?: string;
  }>;
}

/**
 * Structured Data Type Union
 */
export type StructuredDataType =
  | OrganizationSchema
  | SoftwareApplicationSchema
  | ArticleSchema
  | FAQSchema
  | BreadcrumbSchema
  | LocalBusinessSchema
  | ProductSchema;

/**
 * SEO Metadata Generation Options
 */
export interface MetadataOptions {
  title?: string;
  description?: string;
  keywords?: string[];
  image?: string;
  type?: 'website' | 'article' | 'product';
  publishedTime?: string;
  modifiedTime?: string;
  authors?: string[];
  section?: string;
  tags?: string[];
  noindex?: boolean;
  nofollow?: boolean;
}

/**
 * Next.js Metadata Export Type
 */
export type NextMetadata = Metadata;
