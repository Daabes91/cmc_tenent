/**
 * Structured Data (Schema.org) Utilities
 * 
 * Functions to generate JSON-LD structured data for SEO.
 * Supports Organization, SoftwareApplication, and BlogPosting schemas.
 */

import { BlogPost } from '@/lib/blog/types';

/**
 * Organization Schema
 * Used on the homepage to describe the company
 */
export interface OrganizationSchema {
  '@context': 'https://schema.org';
  '@type': 'Organization';
  name: string;
  url: string;
  logo: string;
  description: string;
  contactPoint?: {
    '@type': 'ContactPoint';
    contactType: string;
    email?: string;
  };
  sameAs?: string[];
}

/**
 * SoftwareApplication Schema
 * Used to describe the clinic management software
 */
export interface SoftwareApplicationSchema {
  '@context': 'https://schema.org';
  '@type': 'SoftwareApplication';
  name: string;
  applicationCategory: string;
  operatingSystem: string;
  offers: {
    '@type': 'Offer';
    price: string;
    priceCurrency: string;
  };
  aggregateRating?: {
    '@type': 'AggregateRating';
    ratingValue: string;
    ratingCount: string;
  };
  description: string;
}

/**
 * BlogPosting Schema
 * Used on individual blog post pages
 */
export interface BlogPostingSchema {
  '@context': 'https://schema.org';
  '@type': 'BlogPosting';
  headline: string;
  description: string;
  image: string;
  author: {
    '@type': 'Person';
    name: string;
  };
  publisher: {
    '@type': 'Organization';
    name: string;
    logo: {
      '@type': 'ImageObject';
      url: string;
    };
  };
  datePublished: string;
  dateModified?: string;
  mainEntityOfPage: {
    '@type': 'WebPage';
    '@id': string;
  };
}

/**
 * Generate Organization structured data
 */
export function generateOrganizationSchema(): OrganizationSchema {
  const baseUrl = process.env.NEXT_PUBLIC_APP_URL || 'https://cliniqax.com/landing';
  const siteName = process.env.NEXT_PUBLIC_SITE_NAME || 'ClinicPro';

  return {
    '@context': 'https://schema.org',
    '@type': 'Organization',
    name: siteName,
    url: baseUrl,
    logo: `${baseUrl}/brand-logo.png`,
    description: 'All-in-one clinic management platform for healthcare practices',
    contactPoint: {
      '@type': 'ContactPoint',
      contactType: 'Sales',
      email: process.env.SALES_EMAIL,
    },
    sameAs: [
      // Add social media URLs here when available
      // 'https://twitter.com/yourcompany',
      // 'https://linkedin.com/company/yourcompany',
    ],
  };
}

/**
 * Generate SoftwareApplication structured data
 */
export function generateSoftwareApplicationSchema(): SoftwareApplicationSchema {
  const siteName = process.env.NEXT_PUBLIC_SITE_NAME || 'ClinicPro';

  return {
    '@context': 'https://schema.org',
    '@type': 'SoftwareApplication',
    name: siteName,
    applicationCategory: 'BusinessApplication',
    operatingSystem: 'Web Browser',
    offers: {
      '@type': 'Offer',
      price: '49',
      priceCurrency: 'USD',
    },
    description: 'Comprehensive clinic management software for appointment scheduling, patient records, billing, and practice analytics',
  };
}

/**
 * Generate BlogPosting structured data
 */
export function generateBlogPostingSchema(post: BlogPost): BlogPostingSchema {
  const baseUrl = process.env.NEXT_PUBLIC_SITE_URL || 'http://localhost:3000';
  const siteName = process.env.NEXT_PUBLIC_SITE_NAME || 'ClinicPro';

  return {
    '@context': 'https://schema.org',
    '@type': 'BlogPosting',
    headline: post.title,
    description: post.excerpt,
    image: `${baseUrl}${post.featuredImage}`,
    author: {
      '@type': 'Person',
      name: post.author.name,
    },
    publisher: {
      '@type': 'Organization',
      name: siteName,
      logo: {
        '@type': 'ImageObject',
        url: `${baseUrl}/brand-logo.png`,
      },
    },
    datePublished: new Date(post.publishedAt).toISOString(),
    dateModified: post.updatedAt 
      ? new Date(post.updatedAt).toISOString() 
      : new Date(post.publishedAt).toISOString(),
    mainEntityOfPage: {
      '@type': 'WebPage',
      '@id': `${baseUrl}/blog/${post.slug}`,
    },
  };
}

/**
 * Render structured data as JSON-LD script tag
 */
export function renderStructuredData(schema: OrganizationSchema | SoftwareApplicationSchema | BlogPostingSchema): string {
  return JSON.stringify(schema);
}
