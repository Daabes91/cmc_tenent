# Blog Post Detail Page - Quick Reference

## Overview
The blog post detail page displays individual blog posts with full content, author information, social sharing, and SEO optimization.

## URL Structure
```
/blog/[slug]
```

Example: `/blog/reduce-patient-no-shows`

## Components

### BlogPost
Main component that renders the complete blog post.

**Location**: `components/blog/BlogPost.tsx`

**Props**:
```typescript
{
  post: BlogPost;      // Blog post data
  siteUrl: string;     // Site URL for sharing
}
```

### SocialShare
Social media sharing buttons.

**Location**: `components/blog/SocialShare.tsx`

**Props**:
```typescript
{
  url: string;         // URL to share
  title: string;       // Post title
  description?: string; // Post description
}
```

### BlogPostSchema
Structured data for SEO.

**Location**: `components/blog/BlogPostSchema.tsx`

**Props**:
```typescript
{
  post: BlogPost;      // Blog post data
  siteUrl: string;     // Site URL
}
```

## Features

### Content Display
- ✅ MDX rendering with custom components
- ✅ Featured image with optimization
- ✅ Category badge with link
- ✅ Reading time indicator
- ✅ Proper typography and formatting

### Author Information
- ✅ Author avatar
- ✅ Author name and role
- ✅ Publication date
- ✅ Author bio section

### Social Sharing
- ✅ Twitter/X
- ✅ Facebook
- ✅ LinkedIn
- ✅ Email
- ✅ Copy link

### SEO
- ✅ Meta tags (title, description, keywords)
- ✅ Open Graph tags
- ✅ Twitter Card tags
- ✅ Canonical URL
- ✅ BlogPosting schema

## Testing Checklist

### Visual
- [ ] Post title displays correctly
- [ ] Featured image loads and scales properly
- [ ] Author avatar displays
- [ ] Content formatting is correct
- [ ] Tags are clickable
- [ ] Social buttons are visible

### Functionality
- [ ] Category link filters blog listing
- [ ] Tag links filter blog listing
- [ ] Social share buttons open correctly
- [ ] Copy link shows "Copied!" feedback
- [ ] Back to blog link works
- [ ] External links open in new tab

### SEO
- [ ] Meta tags are present in page source
- [ ] Open Graph tags are correct
- [ ] Twitter Card tags are present
- [ ] Structured data validates
- [ ] Canonical URL is correct

### Responsive
- [ ] Mobile (375px): Content is readable
- [ ] Tablet (768px): Layout adapts properly
- [ ] Desktop (1200px+): Optimal reading width

## Common Issues

### Images Not Loading
- Check `featuredImage` path in frontmatter
- Verify image exists in `public/images/blog/`
- Check Next.js Image configuration

### Social Sharing Not Working
- Verify `NEXT_PUBLIC_SITE_URL` is set
- Check popup blocker settings
- Ensure URLs are properly encoded

### Content Not Rendering
- Verify MDX file has valid frontmatter
- Check for syntax errors in MDX content
- Ensure `draft: false` in frontmatter

### 404 Error
- Check slug matches filename
- Verify post is not draft
- Ensure publication date is not in future

## Quick Commands

### View Blog Post
```bash
# Development
open http://localhost:3000/blog/example-post

# Production
open https://yourdomain.com/blog/example-post
```

### Validate Structured Data
```bash
# Google Rich Results Test
open https://search.google.com/test/rich-results?url=https://yourdomain.com/blog/example-post
```

### Check Meta Tags
```bash
# View page source
curl https://yourdomain.com/blog/example-post | grep -E '<meta|<title'
```

## Environment Variables

```bash
# Required for social sharing and SEO
NEXT_PUBLIC_SITE_URL=https://yourdomain.com
```

## Related Files

- `app/blog/[slug]/page.tsx` - Page component
- `components/blog/BlogPost.tsx` - Main component
- `components/blog/SocialShare.tsx` - Sharing buttons
- `components/blog/BlogPostSchema.tsx` - Structured data
- `lib/blog/get-posts.ts` - Data fetching
- `lib/blog/types.ts` - Type definitions
- `mdx-components.tsx` - MDX component styling

## Next Steps

After implementing the blog post detail page:

1. ✅ Task 8: Create blog post detail page
2. ⏭️ Task 9: Implement blog search functionality
3. ⏭️ Task 10: Implement related posts feature
4. ⏭️ Task 11: Create blog post creation system
