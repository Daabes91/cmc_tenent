# Blog Listing Page - Quick Reference

## Overview
The blog listing page at `/blog` displays all published healthcare articles with filtering, search, and pagination.

## Key Features

### 📝 Blog Post Display
- Featured image with hover effects
- Title, excerpt, and reading time
- Author information with avatar
- Publication date
- Category badge
- Tags (up to 3 shown)

### 🔍 Search
- Search across titles, excerpts, and tags
- Real-time filtering
- Clear button to reset
- Results count display

### 🏷️ Category Filtering
- Practice Management
- Patient Care
- Technology
- Compliance
- Industry News
- All Posts (default)

### 📄 Pagination
- 9 posts per page (configurable)
- Smart page number display
- Previous/Next navigation
- Auto-scroll to top on page change

## Component Architecture

```
BlogPage (Server Component)
  └── BlogList (Client Component)
      ├── SearchBar
      ├── CategoryFilter
      ├── BlogCard (multiple)
      └── Pagination
```

## Usage

### Adding New Blog Posts

1. Create MDX file in `content/blog/`:
```mdx
---
title: "Your Article Title"
excerpt: "Brief description..."
author:
  name: "Dr. Jane Smith"
  role: "Healthcare Consultant"
  avatar: "/images/authors/jane-smith.jpg"
publishedAt: "2024-01-15"
category: "practice-management"
tags: ["scheduling", "efficiency"]
featuredImage: "/images/blog/your-image.jpg"
seo:
  title: "SEO Title"
  description: "SEO description"
  keywords: ["keyword1", "keyword2"]
draft: false
---

# Your Article Content
```

2. The post will automatically appear on the blog listing page

### Customizing Posts Per Page

```typescript
<BlogList posts={posts} postsPerPage={12} />
```

## File Locations

- **Page**: `app/blog/page.tsx`
- **Components**: `components/blog/`
- **Content**: `content/blog/`
- **Utilities**: `lib/blog/`

## Styling

All components use:
- Tailwind CSS for styling
- Dark mode support
- Responsive design (mobile-first)
- Hover effects and transitions

## State Management

Client-side state in `BlogList`:
- `selectedCategory`: Current category filter
- `searchKeyword`: Current search term
- `currentPage`: Current pagination page

## Performance

- Server-side data fetching
- Client-side filtering (instant)
- Image lazy loading
- Optimized with `useMemo` hooks

## Accessibility

- Semantic HTML
- ARIA labels
- Keyboard navigation
- Focus management
- Screen reader friendly

## Browser Support

- Modern browsers (Chrome, Firefox, Safari, Edge)
- Mobile browsers (iOS Safari, Chrome Mobile)
- Dark mode support

## Common Tasks

### Filter by Category
Click any category button to filter posts

### Search Articles
Type in search bar and press Enter or click Search

### Navigate Pages
Use Previous/Next buttons or click page numbers

### Clear Filters
Click "All Posts" or clear search to reset

## Troubleshooting

**No posts showing?**
- Check if posts exist in `content/blog/`
- Verify `draft: false` in frontmatter
- Ensure `publishedAt` date is not in future

**Search not working?**
- Verify search term matches title, excerpt, or tags
- Check for typos
- Try different keywords

**Pagination issues?**
- Check total post count
- Verify `postsPerPage` setting
- Clear filters and try again

## Related Documentation

- [Blog System Guide](./BLOG_SYSTEM_GUIDE.md)
- [Task 6 Implementation](./TASK_6_BLOG_INFRASTRUCTURE_SUMMARY.md)
- [Task 7 Implementation](./TASK_7_BLOG_LISTING_IMPLEMENTATION.md)
