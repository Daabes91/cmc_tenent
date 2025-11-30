# SEO Quick Start Guide

This guide will help you quickly implement SEO best practices in the SaaS landing page.

## Setup Complete ✅

The SEO infrastructure has been set up with:

- ✅ SEO configuration file (`config/seo.config.ts`)
- ✅ Keyword strategy (`config/keywords.config.ts`)
- ✅ TypeScript interfaces (`lib/seo/types.ts`)
- ✅ Metadata utilities (`lib/seo/metadata.ts`)
- ✅ Usage examples (`lib/seo/examples.ts`)

## Quick Usage

### 1. Add Metadata to a Page

```typescript
// app/features/page.tsx
import { generatePageMetadata } from '@/lib/seo';

export const metadata = generatePageMetadata('/features', {
  title: 'Features',
  description: 'Explore our powerful clinic management features',
});

export default function FeaturesPage() {
  return <div>Features content...</div>;
}
```

### 2. Dynamic Metadata (Blog Posts, etc.)

```typescript
// app/blog/[slug]/page.tsx
import { generateBlogMetadata } from '@/lib/seo';

export async function generateMetadata({ params }: { params: { slug: string } }) {
  // Fetch your blog post data
  const post = await getBlogPost(params.slug);
  
  return generateBlogMetadata(params.slug, {
    title: post.title,
    description: post.excerpt,
    publishedAt: post.publishedAt,
    author: post.author,
    category: post.category,
    tags: post.tags,
    image: post.coverImage,
  });
}
```

### 3. Custom Keywords

```typescript
import { generatePageMetadata } from '@/lib/seo';

export const metadata = generatePageMetadata('/pricing', {
  title: 'Pricing Plans',
  description: 'Affordable pricing for clinics of all sizes',
  keywords: [
    'clinic software pricing',
    'affordable medical software',
    'healthcare pricing plans',
  ],
});
```

### 4. Custom Open Graph Image

```typescript
import { generatePageMetadata } from '@/lib/seo';

export const metadata = generatePageMetadata('/about', {
  title: 'About Us',
  description: 'Learn about our mission to transform healthcare',
  image: '/images/about-og.png', // Custom OG image
});
```

## Configuration

### Environment Variables

Add these to your `.env.local`:

```env
NEXT_PUBLIC_SITE_URL=https://vireohealth.com
NEXT_PUBLIC_SALES_EMAIL=sales@vireohealth.com
NEXT_PUBLIC_GOOGLE_SITE_VERIFICATION=your-code-here
```

### Update Site Information

Edit `config/seo.config.ts` to customize:

- Site name and URL
- Default title and description
- Social media handles
- Organization details
- Product information

### Update Keywords

Edit `config/keywords.config.ts` to add:

- Primary keywords (high volume)
- Long-tail keywords (specific, lower volume)
- Local keywords (geographic targeting)

## Best Practices

### Title Tags

```typescript
// ✅ Good
title: 'Appointment Scheduling Software | Vireo Health'

// ❌ Bad (too long)
title: 'The Best Most Amazing Appointment Scheduling Software for Medical Clinics and Healthcare Practices'
```

### Meta Descriptions

```typescript
// ✅ Good (150-160 chars, includes CTA)
description: 'Streamline your clinic with our appointment scheduling software. Book demos, manage patients, and grow your practice. Start free trial today.'

// ❌ Bad (too short, no CTA)
description: 'Appointment software'
```

### Keywords

```typescript
// ✅ Good (5-10 relevant keywords)
keywords: [
  'clinic management software',
  'appointment scheduling',
  'patient management',
  'medical billing',
  'healthcare software',
]

// ❌ Bad (keyword stuffing)
keywords: [
  'clinic', 'clinic software', 'clinic management', 'clinic system',
  'medical clinic', 'dental clinic', 'clinic app', 'clinic tool',
  // ... 50 more variations
]
```

## Common Patterns

### Homepage

```typescript
export const metadata = generatePageMetadata('/', {
  title: 'Complete Clinic Management Solution',
  description: 'Transform your clinic with our all-in-one platform for appointments, patients, billing, and more',
});
```

### Feature Pages

```typescript
export const metadata = generatePageMetadata('/features/scheduling', {
  title: 'Appointment Scheduling',
  description: 'Powerful appointment scheduling with online booking, reminders, and calendar sync',
  keywords: ['appointment scheduling', 'online booking', 'calendar management'],
});
```

### Pricing Page

```typescript
export const metadata = generatePageMetadata('/pricing', {
  title: 'Pricing Plans',
  description: 'Flexible pricing for clinics of all sizes. Start with our free plan or upgrade for advanced features',
});
```

### Contact Page

```typescript
export const metadata = generatePageMetadata('/contact', {
  title: 'Contact Us',
  description: 'Get in touch with our team. We\'re here to help you transform your clinic management',
});
```

## Next Steps

1. ✅ **Task 1 Complete**: SEO infrastructure set up
2. ⏭️ **Task 2**: Implement core metadata system (add metadata to all pages)
3. ⏭️ **Task 3**: Implement structured data (Schema.org)
4. ⏭️ **Task 4**: Create sitemap and robots.txt
5. ⏭️ **Task 5**: Add Open Graph images

## Testing

### Validate Metadata

```typescript
import { validateMetadata } from '@/lib/seo';

const validation = validateMetadata(metadata);
if (!validation.valid) {
  console.error('Metadata issues:', validation.errors);
}
```

### Check Keywords

```typescript
import { getKeywordsForPage } from '@/config/keywords.config';

const keywords = getKeywordsForPage('/features');
console.log('Keywords for /features:', keywords);
```

## Resources

- [Full Documentation](./lib/seo/README.md)
- [Usage Examples](./lib/seo/examples.ts)
- [Next.js Metadata Docs](https://nextjs.org/docs/app/building-your-application/optimizing/metadata)

## Support

For questions or issues:
1. Check the [SEO README](./lib/seo/README.md)
2. Review [examples](./lib/seo/examples.ts)
3. Consult the design document (`.kiro/specs/saas-landing-seo-optimization/design.md`)
