# SEO Infrastructure

This directory contains the SEO infrastructure and utilities for the SaaS landing page.

## Overview

The SEO system provides a comprehensive solution for managing metadata, structured data, and search engine optimization across the entire application.

## Structure

```
lib/seo/
├── index.ts           # Main export file
├── types.ts           # TypeScript type definitions
├── metadata.ts        # Metadata generation utilities
└── README.md          # This file

config/
├── seo.config.ts      # SEO configuration
└── keywords.config.ts # Keyword strategy
```

## Configuration

### SEO Config (`config/seo.config.ts`)

Central configuration for all SEO-related settings:

- **Site Information**: Name, URL, title, description
- **Keywords**: Default keywords for the site
- **Social Media**: Twitter, Facebook, LinkedIn, Instagram handles
- **Organization**: Company details for Schema.org markup
- **Product**: Product information for structured data
- **Open Graph**: Default OG settings
- **Twitter Card**: Default Twitter Card settings
- **Robots**: Crawler directives

### Keywords Config (`config/keywords.config.ts`)

Keyword strategy organized by:

- **Primary Keywords**: High-volume, competitive terms
- **Long-tail Keywords**: Lower volume, higher conversion terms
- **Local Keywords**: Geographic-specific terms

## Usage

### Basic Page Metadata

```typescript
import { generatePageMetadata } from '@/lib/seo';

export const metadata = generatePageMetadata('/', {
  title: 'Home',
  description: 'Welcome to our clinic management platform',
});
```

### Blog Post Metadata

```typescript
import { generateBlogMetadata } from '@/lib/seo';

export async function generateMetadata({ params }) {
  const post = await getPost(params.slug);
  
  return generateBlogMetadata(params.slug, {
    title: post.title,
    description: post.excerpt,
    publishedAt: post.publishedAt,
    updatedAt: post.updatedAt,
    author: post.author,
    category: post.category,
    tags: post.tags,
    image: post.image,
  });
}
```

### Custom Metadata

```typescript
import { generatePageMetadata } from '@/lib/seo';

export const metadata = generatePageMetadata('/features', {
  title: 'Features',
  description: 'Explore our powerful features',
  keywords: ['clinic features', 'appointment scheduling', 'patient management'],
  image: '/images/features-og.png',
});
```

### Get Keywords for a Page

```typescript
import { getKeywordsForPage } from '@/lib/seo';

const keywords = getKeywordsForPage('/pricing');
// Returns: ['appointment scheduling software', 'medical billing software', ...]
```

### Canonical URLs

```typescript
import { getCanonicalUrl } from '@/lib/seo';

const canonical = getCanonicalUrl('/about');
// Returns: 'https://vireohealth.com/about'
```

### Validate Metadata

```typescript
import { validateMetadata } from '@/lib/seo';

const validation = validateMetadata(metadata);
if (!validation.valid) {
  console.error('Metadata errors:', validation.errors);
}
```

## Types

All TypeScript types are available from the main export:

```typescript
import type {
  PageMetadata,
  OpenGraphMetadata,
  TwitterMetadata,
  MetadataOptions,
  OrganizationSchema,
  SoftwareApplicationSchema,
  ArticleSchema,
  FAQSchema,
} from '@/lib/seo';
```

## Best Practices

### Title Tags

- Keep titles between 50-60 characters
- Include primary keyword
- Make each title unique
- Add brand name at the end

### Meta Descriptions

- Keep descriptions between 150-160 characters
- Include a call-to-action
- Make each description unique
- Include primary keyword naturally

### Keywords

- Use 5-10 keywords per page
- Include primary and long-tail keywords
- Don't keyword stuff
- Use keywords naturally in content

### Images

- Always provide alt text
- Use descriptive file names
- Optimize image size
- Use WebP format when possible

### URLs

- Use hyphens, not underscores
- Keep URLs short and descriptive
- Use lowercase
- Include keywords when relevant

## Environment Variables

Add these to your `.env.local` file:

```env
NEXT_PUBLIC_SITE_URL=https://vireohealth.com
NEXT_PUBLIC_SALES_EMAIL=sales@vireohealth.com
NEXT_PUBLIC_GOOGLE_SITE_VERIFICATION=your-verification-code
NEXT_PUBLIC_BING_VERIFICATION=your-verification-code
NEXT_PUBLIC_YANDEX_VERIFICATION=your-verification-code
```

## Next Steps

After setting up the infrastructure:

1. Implement structured data components (Task 3)
2. Create sitemap.xml and robots.txt (Task 4)
3. Add Open Graph images (Task 5)
4. Optimize content for keywords (Task 6)
5. Implement performance optimizations (Task 7)

## Resources

- [Next.js Metadata API](https://nextjs.org/docs/app/building-your-application/optimizing/metadata)
- [Schema.org Documentation](https://schema.org/)
- [Google Search Central](https://developers.google.com/search)
- [Open Graph Protocol](https://ogp.me/)
- [Twitter Cards](https://developer.twitter.com/en/docs/twitter-for-websites/cards/overview/abouts-cards)
