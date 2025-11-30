# Blog System Quick Reference

## 🚀 Quick Start

### View the Blog
```
http://localhost:3000/blog
```

### Create a New Post
```bash
# 1. Create file
touch content/blog/my-post.mdx

# 2. Add frontmatter and content (see template below)

# 3. Post automatically appears on blog page
```

## 📝 Blog Post Template

```mdx
---
title: "Your Post Title"
excerpt: "Brief 150-character summary"
author:
  name: "Dr. Jane Smith"
  role: "Healthcare Consultant"
  avatar: "/images/authors/jane-smith.jpg"
publishedAt: "2024-01-20"
category: "practice-management"
tags: ["tag1", "tag2", "tag3"]
featuredImage: "/images/blog/featured.jpg"
seo:
  title: "SEO-Optimized Title"
  description: "SEO description"
  keywords: ["keyword1", "keyword2"]
draft: false
---

# Your Content Here

Write your blog post using Markdown...
```

## 📂 File Locations

```
apps/saas-landing/
├── app/blog/                    # Blog pages
├── content/blog/                # Your blog posts go here
├── lib/blog/                    # Utilities (don't modify)
└── docs/BLOG_SYSTEM_GUIDE.md   # Full documentation
```

## 🏷️ Categories

- `practice-management` - Practice management
- `patient-care` - Patient care
- `technology` - Healthcare technology
- `compliance` - Compliance
- `industry-news` - Industry news

## 🔧 Common Tasks

### Fetch All Posts
```typescript
import { getAllBlogPosts } from '@/lib/blog';
const posts = await getAllBlogPosts();
```

### Get Single Post
```typescript
import { getBlogPost } from '@/lib/blog';
const post = await getBlogPost('my-post-slug');
```

### Search Posts
```typescript
import { searchBlogPosts } from '@/lib/blog';
const results = await searchBlogPosts('patient care');
```

### Get Related Posts
```typescript
import { getRelatedPosts } from '@/lib/blog';
const related = await getRelatedPosts(currentPost, 3);
```

## ✅ Checklist for New Posts

- [ ] Title under 60 characters
- [ ] Excerpt 150-160 characters
- [ ] Featured image 1200x630px
- [ ] 3-5 relevant tags
- [ ] Category selected
- [ ] SEO fields filled
- [ ] `draft: false` when ready
- [ ] `publishedAt` date set

## 🐛 Troubleshooting

**Post not showing?**
- Check `draft: false`
- Verify `publishedAt` is not future date
- Ensure file is in `content/blog/`

**MDX error?**
- Check frontmatter YAML syntax
- Verify all JSX tags are closed
- Review mdx-components.tsx

## 📚 Full Documentation

See `docs/BLOG_SYSTEM_GUIDE.md` for complete documentation.
