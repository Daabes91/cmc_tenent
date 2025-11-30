# Blog Post Draft and Scheduling Guide

This guide explains how to use the draft and scheduling features for blog posts in the SaaS landing page.

## Overview

The blog system supports two key features for content management:

1. **Draft Posts**: Posts that are not yet ready for publication
2. **Scheduled Posts**: Posts that are published at a future date

## Draft Posts

### What are Draft Posts?

Draft posts are blog posts that exist in the content directory but are not visible to the public. They are useful for:

- Work-in-progress articles
- Posts awaiting review
- Content that needs additional research or editing
- Placeholder posts for future topics

### How to Create a Draft Post

Add `draft: true` to the frontmatter of your MDX file:

```mdx
---
title: "My Draft Article"
excerpt: "This article is still being written"
author:
  name: "John Doe"
  role: "Content Writer"
publishedAt: "2024-01-15"
category: "practice-management"
tags: ["draft", "work-in-progress"]
featuredImage: "/images/blog/draft.jpg"
seo:
  title: "Draft Article"
  description: "This is a draft"
  keywords: ["draft"]
draft: true
---

# My Draft Article

Content goes here...
```

### Draft Post Behavior

- **Not listed**: Draft posts do NOT appear in the blog listing page (`/blog`)
- **Not accessible**: Attempting to access a draft post directly (e.g., `/blog/my-draft-article`) returns a 404 error
- **Can be read internally**: The post file can still be read by the system for internal purposes
- **Excluded from search**: Draft posts are not included in blog search results
- **Not in sitemap**: Draft posts are excluded from the XML sitemap

### Publishing a Draft Post

To publish a draft post, simply change `draft: true` to `draft: false` (or remove the draft field entirely):

```mdx
---
title: "My Published Article"
# ... other fields ...
draft: false  # or remove this line entirely
---
```

## Scheduled Posts

### What are Scheduled Posts?

Scheduled posts are blog posts with a future publication date. They allow you to:

- Write content in advance
- Schedule posts for optimal publishing times
- Maintain a consistent publishing schedule
- Prepare content for product launches or events

### How to Create a Scheduled Post

Set the `publishedAt` field to a future date:

```mdx
---
title: "Future Article About Healthcare Trends"
excerpt: "This article will be published in the future"
author:
  name: "Jane Smith"
  role: "Healthcare Analyst"
publishedAt: "2025-12-31"  # Future date
category: "technology"
tags: ["future", "trends"]
featuredImage: "/images/blog/future.jpg"
seo:
  title: "Future Healthcare Trends"
  description: "Upcoming trends in healthcare"
  keywords: ["healthcare", "trends", "future"]
draft: false  # Not a draft, just scheduled
---

# Future Article

Content goes here...
```

### Scheduled Post Behavior

- **Not listed until date**: Scheduled posts do NOT appear in the blog listing until the publication date is reached
- **Not accessible until date**: Attempting to access a scheduled post before its publication date returns a 404 error
- **Automatically published**: Once the publication date/time is reached, the post automatically becomes visible
- **Timezone**: Publication times are based on the server's timezone
- **Excluded from search**: Scheduled posts are not searchable until published
- **Not in sitemap**: Scheduled posts are excluded from the XML sitemap until published

### Publication Date Format

The `publishedAt` field accepts dates in ISO 8601 format:

```yaml
publishedAt: "2024-01-15"           # Date only (midnight UTC)
publishedAt: "2024-01-15T10:00:00"  # Date and time
publishedAt: "2024-01-15T10:00:00Z" # Date and time with timezone
```

## Combining Draft and Scheduled

You can combine both features:

```mdx
---
title: "Future Draft Article"
publishedAt: "2025-12-31"  # Future date
draft: true                 # Still a draft
---
```

In this case:
- The post will NOT be visible even after the publication date is reached
- You must set `draft: false` for the post to become visible on the scheduled date

## Best Practices

### For Draft Posts

1. **Use descriptive titles**: Include "Draft" or "WIP" in the title for easy identification
2. **Set realistic dates**: Use a past date for `publishedAt` so it appears immediately when published
3. **Regular reviews**: Review draft posts regularly to avoid forgotten content
4. **Version control**: Use git branches for major drafts to track changes

### For Scheduled Posts

1. **Plan ahead**: Schedule posts at least a few days in advance
2. **Optimal timing**: Schedule posts for times when your audience is most active
3. **Buffer time**: Add a few hours buffer before important events
4. **Test before scheduling**: Review the post thoroughly before setting a future date
5. **Monitor after publication**: Check that scheduled posts appear correctly after publication

## Testing Draft and Scheduling

### Manual Testing

1. **Test draft posts**:
   ```bash
   # Create a draft post
   # Visit /blog - should not see the draft
   # Visit /blog/draft-slug - should get 404
   ```

2. **Test scheduled posts**:
   ```bash
   # Create a post with future date
   # Visit /blog - should not see the scheduled post
   # Visit /blog/scheduled-slug - should get 404
   # Change date to past - should now be visible
   ```

### Automated Testing

Run the test suite to verify draft and scheduling functionality:

```bash
pnpm test:run lib/blog/__tests__/draft-scheduling.test.ts
```

## Implementation Details

### Filtering Logic

The blog system filters posts in `lib/blog/get-posts.ts`:

```typescript
export async function getAllBlogPosts(includeContent = false): Promise<BlogPost[]> {
  const slugs = getAllBlogSlugs();
  const posts: BlogPost[] = [];
  
  for (const slug of slugs) {
    const post = await getBlogPost(slug);
    // Filter out drafts and scheduled posts
    if (post && !post.draft && isPublished(post.publishedAt)) {
      posts.push(post);
    }
  }
  
  return posts.sort((a, b) => 
    new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime()
  );
}

function isPublished(publishedAt: string): boolean {
  const publishDate = new Date(publishedAt);
  const now = new Date();
  return publishDate <= now;
}
```

### Detail Page Protection

The blog post detail page (`app/blog/[slug]/page.tsx`) includes protection:

```typescript
export default async function BlogPostPage({ params }: BlogPostPageProps) {
  const { slug } = await params;
  const post = await getBlogPost(slug);

  // Return 404 for drafts
  if (!post || post.draft) {
    notFound();
  }

  // Return 404 for scheduled posts
  const publishDate = new Date(post.publishedAt);
  const now = new Date();
  if (publishDate > now) {
    notFound();
  }

  // ... render post
}
```

## Troubleshooting

### Draft post is visible

**Problem**: A draft post appears in the blog listing

**Solution**: 
- Check that `draft: true` is set in the frontmatter
- Verify there are no syntax errors in the frontmatter
- Clear Next.js cache: `rm -rf .next && pnpm dev`

### Scheduled post published too early

**Problem**: A scheduled post appears before its publication date

**Solution**:
- Verify the `publishedAt` date is in the future
- Check server timezone settings
- Ensure date format is correct (ISO 8601)

### Post not appearing after scheduled date

**Problem**: A scheduled post doesn't appear after its publication date

**Solution**:
- Check that `draft` is not set to `true`
- Verify the date has actually passed (consider timezone)
- Rebuild the site: `pnpm build`
- Check for errors in the post frontmatter

## Related Documentation

- [Blog Post Creation Guide](./BLOG_POST_CREATION_GUIDE.md)
- [Blog System Guide](./BLOG_SYSTEM_GUIDE.md)
- [Blog Quick Reference](./BLOG_QUICK_REFERENCE.md)

## Requirements Validation

This implementation validates:

- **Requirement 4.3**: Draft posts are filtered from public listings and not accessible via direct URL
- **Requirement 4.4**: Scheduled posts don't appear until their publication date is reached

## Property Tests

The following property-based tests validate the implementation:

- **Property 7**: Draft Post Visibility - Draft posts should not appear in public listings
- **Property 8**: Scheduled Post Visibility - Scheduled posts should not appear until publication date

Run tests with:
```bash
pnpm test:run lib/blog/__tests__/draft-scheduling.test.ts
```
