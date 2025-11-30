# Blog System

This directory contains the blog system infrastructure for the SaaS landing page.

## Overview

The blog system provides a complete content management solution using MDX (Markdown with JSX) files. It includes:

- **Type-safe interfaces** for blog posts, categories, and metadata
- **Post fetching utilities** to read and parse MDX files
- **Search functionality** to find posts by keyword
- **Related posts logic** to suggest similar content
- **Category and tag filtering** for content organization

## Directory Structure

```
lib/blog/
├── types.ts          # TypeScript interfaces and types
├── get-posts.ts      # Functions to fetch and parse blog posts
├── search.ts         # Search functionality
├── related.ts        # Related posts logic
├── index.ts          # Main export file
└── README.md         # This file
```

## Usage

### Fetching All Posts

```typescript
import { getAllBlogPosts } from '@/lib/blog';

const posts = await getAllBlogPosts();
```

### Fetching a Single Post

```typescript
import { getBlogPost } from '@/lib/blog';

const post = await getBlogPost('my-blog-post-slug');
```

### Searching Posts

```typescript
import { searchBlogPosts } from '@/lib/blog';

const results = await searchBlogPosts('patient management');
```

### Getting Related Posts

```typescript
import { getRelatedPosts } from '@/lib/blog';

const related = await getRelatedPosts(currentPost, 3);
```

### Filtering by Category

```typescript
import { getBlogPostsByCategory } from '@/lib/blog';

const posts = await getBlogPostsByCategory('practice-management');
```

## Blog Post Format

Blog posts are stored as MDX files in `content/blog/` with the following frontmatter:

```mdx
---
title: "Your Blog Post Title"
excerpt: "A brief summary of the post"
author:
  name: "Dr. Jane Smith"
  role: "Healthcare Consultant"
  avatar: "/images/authors/jane-smith.jpg"
publishedAt: "2024-01-15"
updatedAt: "2024-01-20"
category: "practice-management"
tags: ["appointments", "scheduling", "efficiency"]
featuredImage: "/images/blog/featured.jpg"
seo:
  title: "SEO-optimized title"
  description: "SEO-optimized description"
  keywords: ["keyword1", "keyword2"]
draft: false
---

# Your Blog Post Content

Write your content here using Markdown and JSX components...
```

## Categories

The blog system supports the following categories:

- `practice-management` - Practice management tips and strategies
- `patient-care` - Patient care best practices
- `technology` - Healthcare technology and tools
- `compliance` - Regulatory compliance and standards
- `industry-news` - Healthcare industry news and updates

## Features

### Draft Posts

Posts with `draft: true` in frontmatter are excluded from public listings.

### Scheduled Posts

Posts with a future `publishedAt` date are not displayed until that date.

### Reading Time

Reading time is automatically calculated based on content length.

### SEO Optimization

Each post includes SEO metadata for search engine optimization.

### Pagination

Use `getPaginatedBlogPosts()` to implement pagination on listing pages.

## Next Steps

1. Create blog listing page at `app/blog/page.tsx`
2. Create blog post detail page at `app/blog/[slug]/page.tsx`
3. Add blog components in `components/blog/`
4. Create sample blog posts in `content/blog/`
