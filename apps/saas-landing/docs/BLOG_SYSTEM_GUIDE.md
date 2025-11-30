# Blog System Implementation Guide

## Overview

The blog system provides a complete content management solution for the SaaS landing page using MDX (Markdown with JSX). This guide covers the infrastructure, usage, and best practices.

## Architecture

### Directory Structure

```
apps/saas-landing/
├── app/
│   └── blog/
│       ├── page.tsx              # Blog listing page
│       └── [slug]/
│           └── page.tsx          # Individual blog post page
├── content/
│   └── blog/
│       ├── .gitkeep
│       └── example-post.mdx      # Example blog post
├── lib/
│   └── blog/
│       ├── types.ts              # TypeScript interfaces
│       ├── get-posts.ts          # Post fetching utilities
│       ├── search.ts             # Search functionality
│       ├── related.ts            # Related posts logic
│       ├── index.ts              # Main export
│       └── README.md             # Documentation
└── mdx-components.tsx            # MDX component customization
```

## Features

### ✅ Implemented

1. **MDX Configuration**
   - Next.js configured with @next/mdx
   - Support for .md and .mdx files
   - Remark and Rehype plugins (GFM, syntax highlighting)

2. **Type-Safe Interfaces**
   - BlogPost, BlogCategory, BlogAuthor
   - BlogFrontmatter, BlogMetadata
   - BlogListItem, BlogSearchResult

3. **Post Fetching**
   - `getAllBlogPosts()` - Get all published posts
   - `getBlogPost(slug)` - Get single post by slug
   - `getBlogPostsByCategory()` - Filter by category
   - `getBlogPostsByTag()` - Filter by tag
   - `getPaginatedBlogPosts()` - Paginated results

4. **Search Functionality**
   - `searchBlogPosts(keyword)` - Search by keyword
   - Searches title, excerpt, content, and tags
   - Relevance scoring and sorting

5. **Related Posts**
   - `getRelatedPosts()` - Find similar posts
   - Based on category, tags, and author
   - Configurable result limit

6. **Draft & Scheduling**
   - Draft posts excluded from listings
   - Scheduled posts (future dates) hidden until published
   - Automatic reading time calculation

7. **SEO Optimization**
   - Meta tags for each post
   - Open Graph and Twitter Card support
   - Structured data ready (BlogPosting schema)

## Usage

### Creating a Blog Post

1. Create a new `.mdx` file in `content/blog/`:

```bash
touch content/blog/my-new-post.mdx
```

2. Add frontmatter and content:

```mdx
---
title: "Your Post Title"
excerpt: "Brief summary of your post"
author:
  name: "Dr. Jane Smith"
  role: "Healthcare Consultant"
  avatar: "/images/authors/jane-smith.jpg"
publishedAt: "2024-01-15"
category: "practice-management"
tags: ["tag1", "tag2", "tag3"]
featuredImage: "/images/blog/featured.jpg"
seo:
  title: "SEO Title"
  description: "SEO Description"
  keywords: ["keyword1", "keyword2"]
draft: false
---

# Your Content Here

Write your blog post content using Markdown...
```

### Fetching Posts in Components

```typescript
import { getAllBlogPosts, getBlogPost } from '@/lib/blog';

// In a Server Component
export default async function BlogPage() {
  const posts = await getAllBlogPosts();
  
  return (
    <div>
      {posts.map(post => (
        <article key={post.slug}>
          <h2>{post.title}</h2>
          <p>{post.excerpt}</p>
        </article>
      ))}
    </div>
  );
}
```

### Implementing Search

```typescript
import { searchBlogPosts } from '@/lib/blog';

const results = await searchBlogPosts('patient management');
```

### Getting Related Posts

```typescript
import { getRelatedPosts } from '@/lib/blog';

const currentPost = await getBlogPost('my-post');
const related = await getRelatedPosts(currentPost, 3);
```

## Categories

The system supports five healthcare-focused categories:

- `practice-management` - Practice management tips
- `patient-care` - Patient care best practices
- `technology` - Healthcare technology
- `compliance` - Regulatory compliance
- `industry-news` - Industry news and updates

## Configuration

### MDX Plugins

The system uses the following plugins:

- **remark-gfm**: GitHub Flavored Markdown support
- **rehype-highlight**: Syntax highlighting for code blocks

### Reading Time

Reading time is automatically calculated using the `reading-time` package and displayed in minutes.

### Frontmatter Parsing

Frontmatter is parsed using `gray-matter` for reliable metadata extraction.

## Best Practices

### Content Guidelines

1. **Title**: Keep under 60 characters for SEO
2. **Excerpt**: 150-160 characters, compelling summary
3. **Featured Image**: 1200x630px for social sharing
4. **Tags**: 3-5 relevant tags per post
5. **Reading Time**: Aim for 5-10 minute reads

### SEO Optimization

1. Use unique meta titles and descriptions
2. Include target keywords naturally
3. Add alt text to all images
4. Use descriptive headings (H2, H3)
5. Internal linking to related posts

### Performance

1. Featured images should be optimized (WebP format)
2. Use Next.js Image component when possible
3. Lazy load images below the fold
4. Keep MDX files under 50KB

## Testing

### Manual Testing Checklist

- [ ] Blog listing page displays all posts
- [ ] Individual post pages render correctly
- [ ] Draft posts are hidden
- [ ] Scheduled posts don't appear early
- [ ] Search returns relevant results
- [ ] Related posts are displayed
- [ ] Reading time is accurate
- [ ] SEO meta tags are present
- [ ] Images load properly
- [ ] Mobile responsive

### Example Test

```typescript
import { getAllBlogPosts, getBlogPost } from '@/lib/blog';

// Test fetching posts
const posts = await getAllBlogPosts();
console.assert(posts.length > 0, 'Should have posts');

// Test single post
const post = await getBlogPost('example-post');
console.assert(post !== null, 'Should find example post');
console.assert(!post.draft, 'Example post should not be draft');
```

## Next Steps

### Immediate Tasks (Task 7-10)

1. **Task 7**: Create blog listing page with filters
2. **Task 8**: Create blog post detail page with MDX rendering
3. **Task 9**: Implement search functionality with UI
4. **Task 10**: Add related posts feature

### Future Enhancements

1. Add blog categories page
2. Implement tag pages
3. Add author pages
4. RSS feed generation
5. Newsletter signup integration
6. Social sharing buttons
7. Comments system
8. Blog analytics

## Troubleshooting

### Posts Not Appearing

1. Check `draft: false` in frontmatter
2. Verify `publishedAt` is not in future
3. Ensure file is in `content/blog/`
4. Check file extension (.mdx or .md)

### MDX Rendering Issues

1. Verify MDX syntax is valid
2. Check for unclosed JSX tags
3. Ensure frontmatter is properly formatted
4. Review mdx-components.tsx for conflicts

### Search Not Working

1. Verify posts have content
2. Check search term is not empty
3. Ensure posts are published (not draft)
4. Review search.ts logic

## Support

For issues or questions:
1. Check this documentation
2. Review lib/blog/README.md
3. Examine example-post.mdx
4. Test with provided utilities

## Summary

The blog system infrastructure is now complete with:
- ✅ MDX configuration
- ✅ Type-safe interfaces
- ✅ Post fetching utilities
- ✅ Search functionality
- ✅ Related posts logic
- ✅ Draft and scheduling support
- ✅ SEO optimization
- ✅ Example blog post
- ✅ Documentation

Ready for tasks 7-10 to build the UI components and pages.
