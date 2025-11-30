# Blog SEO Quick Reference

## ✅ SEO Checklist for New Blog Posts

When creating a new blog post, ensure:

- [ ] Unique SEO title (50-60 characters)
- [ ] Unique SEO description (120-160 characters)
- [ ] 3-5 relevant keywords
- [ ] SEO-friendly slug (lowercase, hyphens, no special chars)
- [ ] Featured image (1200x630px, <200KB)
- [ ] Author name and role
- [ ] Publication date
- [ ] Category assigned
- [ ] 3-5 relevant tags
- [ ] Content is valuable and well-written
- [ ] Internal links to related posts

## 📝 Frontmatter Template

```yaml
---
title: "Your Compelling Blog Post Title"
excerpt: "Brief summary that entices readers to click and read more"
author:
  name: "Author Full Name"
  role: "Author Title/Role"
  avatar: "/images/authors/author-slug.jpg"
publishedAt: "2024-01-15"
category: "practice-management"  # or patient-care, technology, compliance, industry-news
tags: ["keyword1", "keyword2", "keyword3"]
featuredImage: "/images/blog/post-slug.jpg"
seo:
  title: "SEO Title with Primary Keyword | Brand"
  description: "Compelling 120-160 char description with primary keyword and call-to-action"
  keywords: ["primary keyword", "secondary keyword", "long-tail keyword"]
draft: false
---
```

## 🔍 Verification Commands

```bash
# Verify all blog posts have proper SEO
npm run verify-blog-seo

# Or manually
npx tsx scripts/verify-blog-seo.ts
```

## 🌐 Testing URLs

### Meta Tags Testing
- **Facebook:** https://developers.facebook.com/tools/debug/
- **Twitter:** https://cards-dev.twitter.com/validator
- **LinkedIn:** https://www.linkedin.com/post-inspector/

### Structured Data Testing
- **Google Rich Results:** https://search.google.com/test/rich-results

### Sitemap
- **Local:** http://localhost:3000/sitemap.xml
- **Production:** https://yourdomain.com/sitemap.xml

## 📊 What Gets Generated

### Meta Tags
```html
<title>SEO Title | Brand</title>
<meta name="description" content="SEO description" />
<meta name="keywords" content="keyword1, keyword2, keyword3" />
<link rel="canonical" href="https://yourdomain.com/blog/post-slug" />
```

### Open Graph
```html
<meta property="og:title" content="SEO Title" />
<meta property="og:description" content="SEO description" />
<meta property="og:type" content="article" />
<meta property="og:url" content="https://yourdomain.com/blog/post-slug" />
<meta property="og:image" content="https://yourdomain.com/images/blog/post.jpg" />
<meta property="article:published_time" content="2024-01-15T00:00:00.000Z" />
<meta property="article:author" content="Author Name" />
```

### Twitter Cards
```html
<meta name="twitter:card" content="summary_large_image" />
<meta name="twitter:title" content="SEO Title" />
<meta name="twitter:description" content="SEO description" />
<meta name="twitter:image" content="https://yourdomain.com/images/blog/post.jpg" />
```

### BlogPosting Schema
```json
{
  "@context": "https://schema.org",
  "@type": "BlogPosting",
  "headline": "Post Title",
  "description": "Post excerpt",
  "image": "https://yourdomain.com/images/blog/post.jpg",
  "author": {
    "@type": "Person",
    "name": "Author Name"
  },
  "publisher": {
    "@type": "Organization",
    "name": "Brand Name",
    "logo": {
      "@type": "ImageObject",
      "url": "https://yourdomain.com/brand-logo.png"
    }
  },
  "datePublished": "2024-01-15T00:00:00.000Z",
  "dateModified": "2024-01-15T00:00:00.000Z",
  "mainEntityOfPage": {
    "@type": "WebPage",
    "@id": "https://yourdomain.com/blog/post-slug"
  }
}
```

## 🎯 SEO Best Practices

### Meta Titles
- **Length:** 50-60 characters
- **Format:** Primary Keyword - Benefit | Brand
- **Example:** "Reduce Patient No-Shows: 5 Proven Strategies | ClinicPro"
- Include primary keyword early
- Make it compelling
- Unique for each post

### Meta Descriptions
- **Length:** 120-160 characters
- Include primary keyword naturally
- Write compelling copy
- Include call-to-action
- Unique for each post

### Keywords
- 3-5 keywords per post
- Focus on long-tail keywords
- Include variations
- Research with keyword tools

### Featured Images
- **Size:** 1200x630 pixels
- **Format:** JPG or PNG
- **File size:** < 200KB
- **Alt text:** Descriptive with keywords

### URLs
- Short and descriptive
- Include primary keyword
- Use hyphens (not underscores)
- Lowercase only
- Never change after publication

## 🚨 Common Issues

### Post Not in Sitemap
- Check `draft: false` in frontmatter
- Verify `publishedAt` is not in future
- Ensure file is in `content/blog/`
- File must be `.mdx` or `.md`

### Meta Tags Not Showing
- Verify `seo` section in frontmatter
- Clear browser cache
- Rebuild the site
- Test with social debuggers

### Schema Errors
- Check all required fields present
- Use ISO date format (YYYY-MM-DD)
- Test with Google Rich Results Test

## 📈 Monitoring

### Google Search Console
1. Submit sitemap
2. Monitor index coverage
3. Track search performance
4. Check mobile usability

### Analytics Metrics
- Organic search traffic
- Average time on page
- Bounce rate
- Conversion rate
- Top performing posts

## 🔗 Key Files

- `app/blog/[slug]/page.tsx` - Meta tags & schema
- `app/sitemap.ts` - Sitemap generation
- `lib/seo/structured-data.ts` - Schema generation
- `lib/blog/slug.ts` - Slug utilities
- `lib/blog/get-posts.ts` - Post fetching
- `scripts/verify-blog-seo.ts` - Verification script

## 📚 Resources

- [Google SEO Starter Guide](https://developers.google.com/search/docs/fundamentals/seo-starter-guide)
- [Open Graph Protocol](https://ogp.me/)
- [Twitter Cards](https://developer.twitter.com/en/docs/twitter-for-websites/cards/overview/abouts-cards)
- [Schema.org BlogPosting](https://schema.org/BlogPosting)
- [Next.js Metadata](https://nextjs.org/docs/app/building-your-application/optimizing/metadata)
