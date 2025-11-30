# Blog Post Creation Quick Reference

Quick guide for creating and managing blog posts.

## Quick Start

### 1. Create a New Post

```bash
npm run blog:create "Your Blog Post Title"
```

This will:
- Generate a slug from your title
- Create a new file in `content/blog/`
- Pre-fill the template with your title

### 2. Edit the Post

Open the generated file and fill in:
- Author information
- Category (choose from: `practice-management`, `patient-care`, `technology`, `compliance`, `industry-news`)
- Tags (3-6 recommended)
- Featured image path
- SEO metadata
- Content

### 3. Validate the Post

```bash
npm run blog:validate content/blog/your-slug.mdx
```

### 4. Preview the Post

```bash
npm run dev
```

Visit: `http://localhost:3000/blog/your-slug`

### 5. Publish

Set `draft: false` and deploy!

## Common Commands

```bash
# Generate a slug from a title
npm run blog:generate-slug "Your Title Here"

# Validate a blog post
npm run blog:validate content/blog/my-post.mdx

# Create a new post
npm run blog:create "New Post Title"

# List all posts
npm run blog:list

# Show help
npm run blog:help
```

## Required Frontmatter Fields

```yaml
---
title: "Post Title"                    # 40-60 chars recommended
excerpt: "Brief summary"               # 150-200 chars
author:
  name: "Author Name"
  role: "Professional Title"
  avatar: "/images/authors/name.jpg"  # Optional
publishedAt: "2024-01-15"             # YYYY-MM-DD format
category: "practice-management"        # See valid categories below
tags: ["tag1", "tag2", "tag3"]        # 3-6 recommended
featuredImage: "/images/blog/img.jpg"
seo:
  title: "SEO Title"                  # 50-60 chars
  description: "SEO description"      # 150-160 chars
  keywords: ["kw1", "kw2", "kw3"]    # 5-10 recommended
draft: false                           # Optional, default: false
---
```

## Valid Categories

- `practice-management` - Practice management tips
- `patient-care` - Patient care best practices
- `technology` - Healthcare technology
- `compliance` - Regulatory compliance
- `industry-news` - Healthcare industry news

## File Naming

Files should be named using the slug format:
- Lowercase letters only
- Words separated by hyphens
- No special characters
- `.mdx` extension

Examples:
- `reduce-patient-no-shows.mdx`
- `hipaa-compliance-guide.mdx`
- `appointment-scheduling-tips.mdx`

## Draft vs Published

### Draft Post
```yaml
draft: true
```
- Not visible on the website
- Good for work-in-progress

### Published Post
```yaml
draft: false  # or omit this field
publishedAt: "2024-01-15"
```
- Visible on the website
- Appears in blog listings

### Scheduled Post
```yaml
draft: false
publishedAt: "2024-12-25"  # Future date
```
- Not visible until the date arrives
- Automatically published on that date

## Validation Errors

Common errors and how to fix them:

| Error | Solution |
|-------|----------|
| Missing required field | Add the missing field to frontmatter |
| Invalid category | Use one of the valid categories |
| Invalid date format | Use YYYY-MM-DD format |
| SEO title too long | Keep under 60 characters |
| Tags must be an array | Format: `tags: ["tag1", "tag2"]` |

## Content Guidelines

### Structure
- Use H2 (`##`) for main sections
- Use H3 (`###`) for subsections
- Keep paragraphs short (2-4 sentences)
- Use bullet points and numbered lists

### Healthcare Focus
- Use healthcare terminology
- Focus on clinic management topics
- Include practical, actionable advice
- Cite sources when possible

### SEO
- Include keywords in first paragraph
- Use descriptive headings
- Add alt text to images
- Link to other blog posts

## Image Guidelines

### Featured Image
- Size: 1200x630px recommended
- Format: WebP preferred, JPEG fallback
- Location: `/public/images/blog/`
- Naming: descriptive-filename.jpg

### Content Images
```markdown
![Descriptive alt text](/images/blog/image.jpg)
```

## Markdown Formatting

```markdown
# H1 Heading (use for title only)
## H2 Heading (main sections)
### H3 Heading (subsections)

**Bold text**
*Italic text*

- Bullet point
- Another point

1. Numbered item
2. Another item

[Link text](https://example.com)

![Alt text](/images/blog/image.jpg)
```

## Programmatic Usage

### Generate Slug
```typescript
import { generateSlug } from '@/lib/blog/slug';

const slug = generateSlug("My Blog Post Title");
// Returns: "my-blog-post-title"
```

### Validate Frontmatter
```typescript
import { validateFrontmatter } from '@/lib/blog/validate';

const result = validateFrontmatter(frontmatter);
if (!result.valid) {
  console.error(result.errors);
}
```

### Get All Posts
```typescript
import { getAllBlogPosts } from '@/lib/blog';

const posts = await getAllBlogPosts();
```

## Troubleshooting

### Post not appearing on website
1. Check `draft` is set to `false`
2. Verify `publishedAt` is not a future date
3. Run validation: `npm run blog:validate`
4. Check for errors in terminal

### Validation failing
1. Read the error messages carefully
2. Check all required fields are present
3. Verify date format (YYYY-MM-DD)
4. Ensure category is valid
5. Check tags are formatted as array

### Slug conflicts
If a slug already exists, the system will suggest a unique one:
```
Generated slug: my-post
Unique: ✗

Suggested unique slug: my-post-2
```

## Resources

- [Full Creation Guide](./BLOG_POST_CREATION_GUIDE.md)
- [Blog System README](../lib/blog/README.md)
- [Markdown Guide](https://www.markdownguide.org/)
- [MDX Documentation](https://mdxjs.com/)

## Support

For issues or questions:
1. Check this guide
2. Review existing blog posts for examples
3. Run validation tool
4. Contact development team
