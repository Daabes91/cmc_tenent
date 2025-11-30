/**
 * SEO Metadata Utilities
 * Functions for generating and managing page metadata
 */

import type { Metadata } from 'next';
import { seoConfig } from '@/config/seo.config';
import { getKeywordsForPage } from '@/config/keywords.config';
import type {
  PageMetadata,
  OpenGraphMetadata,
  TwitterMetadata,
  MetadataOptions,
} from './types';

/**
 * Get the base URL for the site
 */
export function getBaseUrl(): string {
  return seoConfig.siteUrl;
}

/**
 * Generate canonical URL for a page
 */
export function getCanonicalUrl(path: string): string {
  const baseUrl = getBaseUrl();
  const cleanPath = path.startsWith('/') ? path : `/${path}`;
  return `${baseUrl}${cleanPath}`;
}

/**
 * Generate Open Graph metadata
 */
export function generateOpenGraphMetadata(
  title: string,
  description: string,
  url: string,
  image?: string,
  type: 'website' | 'article' | 'product' = 'website'
): OpenGraphMetadata {
  const baseUrl = getBaseUrl();
  const ogImage = image || `${baseUrl}/image.png`;

  return {
    title,
    description,
    url,
    siteName: seoConfig.siteName,
    images: [
      {
        url: ogImage,
        width: seoConfig.openGraph.images.width,
        height: seoConfig.openGraph.images.height,
        alt: title,
        type: seoConfig.openGraph.images.type,
      },
    ],
    locale: seoConfig.locale,
    type,
  };
}

/**
 * Generate Twitter Card metadata
 */
export function generateTwitterMetadata(
  title: string,
  description: string,
  image?: string
): TwitterMetadata {
  const baseUrl = getBaseUrl();
  const twitterImage = image || `${baseUrl}/image.png`;

  return {
    card: seoConfig.twitter.card,
    title,
    description,
    images: [twitterImage],
    creator: seoConfig.social.twitterCreator,
    site: seoConfig.social.twitter,
  };
}

/**
 * Get default metadata for fallback
 */
export function getDefaultMetadata(): PageMetadata {
  const baseUrl = getBaseUrl();
  const canonicalUrl = getCanonicalUrl('/');

  return {
    title: seoConfig.defaultTitle,
    description: seoConfig.defaultDescription,
    keywords: seoConfig.defaultKeywords,
    canonical: canonicalUrl,
    openGraph: generateOpenGraphMetadata(
      seoConfig.defaultTitle,
      seoConfig.defaultDescription,
      canonicalUrl
    ),
    twitter: generateTwitterMetadata(
      seoConfig.defaultTitle,
      seoConfig.defaultDescription
    ),
    robots: seoConfig.robots,
    authors: [{ name: seoConfig.author }],
    creator: seoConfig.creator,
  };
}

/**
 * Generate page metadata with custom options
 */
export function generatePageMetadata(
  path: string,
  options: MetadataOptions = {}
): Metadata {
  const baseUrl = getBaseUrl();
  const canonicalUrl = getCanonicalUrl(path);

  // Get keywords for this page
  const pageKeywords = getKeywordsForPage(path);
  const keywords = options.keywords || pageKeywords.length > 0 ? pageKeywords : [...seoConfig.defaultKeywords];

  // Build title
  const title = options.title
    ? `${options.title} | ${seoConfig.siteName}`
    : seoConfig.defaultTitle;

  // Build description
  const description = options.description || seoConfig.defaultDescription;

  // Build image URL
  const image = options.image ? `${baseUrl}${options.image}` : `${baseUrl}/image.png`;

  // Build Open Graph metadata
  const openGraph: Metadata['openGraph'] = {
    title: options.title || seoConfig.defaultTitle,
    description,
    url: canonicalUrl,
    siteName: seoConfig.siteName,
    images: [
      {
        url: image,
        width: seoConfig.openGraph.images.width,
        height: seoConfig.openGraph.images.height,
        alt: options.title || seoConfig.defaultTitle,
      },
    ],
    locale: seoConfig.locale,
    type: options.type === 'product' ? 'website' : (options.type || 'website'),
  };

  // Add article-specific metadata
  if (options.type === 'article' && options.publishedTime) {
    (openGraph as any).publishedTime = options.publishedTime;
    (openGraph as any).modifiedTime = options.modifiedTime;
    (openGraph as any).authors = options.authors;
  }

  // Build Twitter metadata
  const twitter: Metadata['twitter'] = {
    card: 'summary_large_image',
    title: options.title || seoConfig.defaultTitle,
    description,
    images: [image],
    creator: seoConfig.social.twitterCreator,
    site: seoConfig.social.twitter,
  };

  // Build robots configuration
  const robots: Metadata['robots'] = options.noindex || options.nofollow
    ? {
        index: !options.noindex,
        follow: !options.nofollow,
      }
    : seoConfig.robots;

  // Build complete metadata
  const metadata: Metadata = {
    title,
    description,
    keywords: keywords.join(', '),
    authors: [{ name: seoConfig.author }],
    creator: seoConfig.creator,
    publisher: seoConfig.organization.name,
    openGraph,
    twitter,
    robots,
    alternates: {
      canonical: canonicalUrl,
    },
    verification: seoConfig.verification,
  };

  // Add category if provided
  if (options.section) {
    metadata.category = options.section;
  }

  return metadata;
}

/**
 * Generate metadata for blog posts
 */
export function generateBlogMetadata(
  slug: string,
  post: {
    title: string;
    description: string;
    publishedAt: string;
    updatedAt?: string;
    author: string;
    category?: string;
    tags?: string[];
    image?: string;
  }
): Metadata {
  const path = `/blog/${slug}`;
  
  return generatePageMetadata(path, {
    title: post.title,
    description: post.description,
    image: post.image,
    type: 'article',
    publishedTime: post.publishedAt,
    modifiedTime: post.updatedAt,
    authors: [post.author],
    section: post.category,
    tags: post.tags,
  });
}

/**
 * Generate metadata for product pages
 */
export function generateProductMetadata(
  slug: string,
  product: {
    name: string;
    description: string;
    image?: string;
  }
): Metadata {
  const path = `/products/${slug}`;
  
  return generatePageMetadata(path, {
    title: product.name,
    description: product.description,
    image: product.image,
    type: 'product',
  });
}

/**
 * Merge custom metadata with defaults
 */
export function mergeMetadata(
  baseMetadata: Metadata,
  customMetadata: Partial<Metadata>
): Metadata {
  return {
    ...baseMetadata,
    ...customMetadata,
    openGraph: {
      ...baseMetadata.openGraph,
      ...customMetadata.openGraph,
    },
    twitter: {
      ...baseMetadata.twitter,
      ...customMetadata.twitter,
    },
  };
}

/**
 * Generate JSON-LD script tag content
 */
export function generateJsonLd<T extends Record<string, any>>(data: T): string {
  return JSON.stringify(data, null, 2);
}

/**
 * Validate metadata completeness
 */
export function validateMetadata(metadata: Metadata): {
  valid: boolean;
  errors: string[];
} {
  const errors: string[] = [];

  if (!metadata.title) {
    errors.push('Title is required');
  }

  if (!metadata.description) {
    errors.push('Description is required');
  }

  if (metadata.description && metadata.description.length > 160) {
    errors.push('Description should be 160 characters or less');
  }

  if (!metadata.openGraph?.images || (Array.isArray(metadata.openGraph.images) && metadata.openGraph.images.length === 0)) {
    errors.push('Open Graph image is required');
  }

  return {
    valid: errors.length === 0,
    errors,
  };
}
