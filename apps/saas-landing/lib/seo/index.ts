/**
 * SEO Utilities - Main Export
 * Central export point for all SEO-related utilities
 */

// Configuration
export { seoConfig } from '@/config/seo.config';
export { keywordStrategy, getKeywordsForPage, getKeywordsByIntent } from '@/config/keywords.config';

// Types
export type {
  PageMetadata,
  OpenGraphMetadata,
  TwitterMetadata,
  BlogPostMetadata,
  OrganizationSchema,
  SoftwareApplicationSchema,
  ArticleSchema,
  FAQSchema,
  BreadcrumbSchema,
  LocalBusinessSchema,
  ProductSchema,
  StructuredDataType,
  MetadataOptions,
  NextMetadata,
} from './types';

// Metadata utilities
export {
  getBaseUrl,
  getCanonicalUrl,
  generateOpenGraphMetadata,
  generateTwitterMetadata,
  getDefaultMetadata,
  generatePageMetadata,
  generateBlogMetadata,
  generateProductMetadata,
  mergeMetadata,
  generateJsonLd,
  validateMetadata,
} from './metadata';
