# Blog Post Creation Guide

This guide explains how to create and publish blog posts for the SaaS landing page.

## Overview

Blog posts are written in MDX (Markdown with JSX) format and stored in the `content/blog/` directory. Each post includes frontmatter metadata and content written in Markdown.

## MDX Frontmatter Format

Every blog post must include frontmatter at the top of the file, enclosed in triple dashes (`---`). The frontmatter contains metadata about the post.

### Required Fields

All blog posts **must** include the following fields:

#### `title` (string, required)
The main title of the blog post. This appears as the H1 heading and in search results.

**Example:**
```yaml
title: "5 Ways to Reduce Patient No-Shows in Your Clinic"
```

**Guidelines:**
- Keep between 40-60 characters for optimal SEO
- Use title case
- Be descriptive and include keywords
- Avoid clickbait or misleading titles

#### `excerpt` (string, required)
A brief summary of the post (150-200 characters). This appears in blog listings and search results.

**Example:**
```yaml
excerpt: "Learn proven strategies to minimize appointment cancellations and improve your clinic's efficiency. Discover how automated reminders can reduce no-shows by up to 38%."
```

**Guidelines:**
- Keep between 150-200 characters
- Summarize the main value proposition
- Include a call-to-action or benefit
- Use complete sentences

#### `author` (object, required)
Information about the post author.

**Structure:**
```yaml
author:
  name: "Dr. Emily Rodriguez"
  role: "Healthcare Consultant"
  avatar: "/images/authors/dr-emily-rodriguez.jpg"
```

**Fields:**
- `name` (string, required): Full name of the author
- `role` (string, required): Professional title or role
- `avatar` (string, optional): Path to author's profile image

#### `publishedAt` (string, required)
The publication date in ISO 8601 format (YYYY-MM-DD).

**Example:**
```yaml
publishedAt: "2024-01-15"
```

**Guidelines:**
- Use ISO 8601 format: YYYY-MM-DD
- For scheduled posts, use a future date
- Posts with future dates won't appear until that date

#### `category` (string, required)
The primary category for the post. Must be one of the predefined categories.

**Valid Categories:**
- `practice-management` - Practice management tips and strategies
- `patient-care` - Patient care best practices
- `technology` - Healthcare technology and tools
- `compliance` - Regulatory compliance and standards
- `industry-news` - Healthcare industry news and updates

**Example:**
```yaml
category: "practice-management"
```

#### `tags` (array, required)
An array of relevant tags for the post. Used for search and related posts.

**Example:**
```yaml
tags: ["appointments", "patient-engagement", "efficiency", "scheduling"]
```

**Guidelines:**
- Include 3-6 tags per post
- Use lowercase
- Use hyphens for multi-word tags
- Be specific and relevant

#### `featuredImage` (string, required)
Path to the featured image for the post.

**Example:**
```yaml
featuredImage: "/images/blog/reduce-no-shows.jpg"
```

**Guidelines:**
- Use high-quality images (1200x630px recommended)
- Store images in `/public/images/blog/`
- Use descriptive filenames
- Optimize images for web (WebP format preferred)

#### `seo` (object, required)
SEO metadata for search engines and social sharing.

**Structure:**
```yaml
seo:
  title: "Reduce Patient No-Shows: 5 Proven Strategies for Clinics"
  description: "Discover effective methods to minimize appointment cancellations and improve your clinic's scheduling efficiency with these proven strategies."
  keywords: ["patient no-shows", "appointment reminders", "clinic efficiency", "patient engagement"]
```

**Fields:**
- `title` (string, required): SEO-optimized title (50-60 characters)
- `description` (string, required): Meta description (150-160 characters)
- `keywords` (array, required): Array of SEO keywords (5-10 keywords)

### Optional Fields

#### `updatedAt` (string, optional)
The date when the post was last updated (YYYY-MM-DD format).

**Example:**
```yaml
updatedAt: "2024-02-20"
```

#### `draft` (boolean, optional)
Set to `true` to mark the post as a draft. Draft posts are not displayed publicly.

**Example:**
```yaml
draft: true
```

**Default:** `false`

## Complete Frontmatter Example

```yaml
---
title: "5 Ways to Reduce Patient No-Shows in Your Clinic"
excerpt: "Learn proven strategies to minimize appointment cancellations and improve your clinic's efficiency. Discover how automated reminders can reduce no-shows by up to 38%."
author:
  name: "Dr. Emily Rodriguez"
  role: "Healthcare Consultant"
  avatar: "/images/authors/dr-emily-rodriguez.jpg"
publishedAt: "2024-01-15"
updatedAt: "2024-02-20"
category: "practice-management"
tags: ["appointments", "patient-engagement", "efficiency", "scheduling"]
featuredImage: "/images/blog/reduce-no-shows.jpg"
seo:
  title: "Reduce Patient No-Shows: 5 Proven Strategies for Clinics"
  description: "Discover effective methods to minimize appointment cancellations and improve your clinic's scheduling efficiency with these proven strategies."
  keywords: ["patient no-shows", "appointment reminders", "clinic efficiency", "patient engagement"]
draft: false
---
```

## Content Guidelines

### Markdown Formatting

After the frontmatter, write your content using standard Markdown:

```markdown
# Main Heading (H1)

Your introduction paragraph...

## Section Heading (H2)

Content for this section...

### Subsection (H3)

More detailed content...

- Bullet point 1
- Bullet point 2

1. Numbered list item 1
2. Numbered list item 2

**Bold text** and *italic text*

[Link text](https://example.com)

![Alt text](/images/blog/image.jpg)
```

### Healthcare-Specific Content

- Use healthcare terminology consistently
- Focus on clinic management, patient care, and practice efficiency
- Include practical, actionable advice
- Cite sources and statistics when possible
- Use real-world examples from healthcare settings

### SEO Best Practices

- Use headings (H2, H3) to structure content
- Include keywords naturally in the first paragraph
- Use descriptive alt text for images
- Link to other relevant blog posts
- Keep paragraphs short (2-4 sentences)
- Use bullet points and numbered lists for readability

## File Naming Convention

Blog post files should be named using the slug format:

**Format:** `lowercase-with-hyphens.mdx`

**Examples:**
- `reduce-patient-no-shows.mdx`
- `hipaa-compliance-guide.mdx`
- `appointment-scheduling-tips.mdx`

**Guidelines:**
- Use lowercase letters only
- Separate words with hyphens
- Keep it short and descriptive
- Match the post title (simplified)
- Use `.mdx` extension

## Creating a New Blog Post

### Step 1: Generate a Slug

Use the slug generation utility to create a URL-friendly slug from your title:

```bash
npm run blog:generate-slug "Your Blog Post Title"
```

Or use the slug generation function in code:

```typescript
import { generateSlug } from '@/lib/blog/slug';

const slug = generateSlug("5 Ways to Reduce Patient No-Shows");
// Returns: "5-ways-to-reduce-patient-no-shows"
```

### Step 2: Create the File

Create a new file in `content/blog/` with the slug as the filename:

```bash
touch content/blog/your-slug-here.mdx
```

### Step 3: Use the Template

Copy the blog post template (see below) and fill in your content.

### Step 4: Validate the Frontmatter

Before publishing, validate your frontmatter:

```bash
npm run blog:validate content/blog/your-slug-here.mdx
```

This will check for:
- All required fields are present
- Category is valid
- Dates are in correct format
- SEO fields meet length requirements

### Step 5: Preview the Post

Start the development server to preview your post:

```bash
npm run dev
```

Navigate to: `http://localhost:3000/blog/your-slug-here`

### Step 6: Publish

To publish the post:
1. Set `draft: false` (or remove the draft field)
2. Set `publishedAt` to today's date or a future date
3. Commit and push to your repository
4. Deploy to production

## Draft and Scheduled Posts

### Draft Posts

Posts marked as drafts are not displayed publicly:

```yaml
draft: true
```

Use drafts for:
- Work-in-progress posts
- Posts pending review
- Posts awaiting images or final edits

### Scheduled Posts

Posts with a future `publishedAt` date are automatically hidden until that date:

```yaml
publishedAt: "2024-12-25"  # Will not appear until December 25, 2024
```

Use scheduled posts for:
- Planning content calendar in advance
- Coordinating with marketing campaigns
- Publishing during optimal times

## Validation Rules

The blog system validates the following:

### Required Field Validation
- All required fields must be present
- Fields cannot be empty strings
- Arrays must contain at least one item

### Category Validation
- Must be one of: `practice-management`, `patient-care`, `technology`, `compliance`, `industry-news`

### Date Validation
- Must be in YYYY-MM-DD format
- Must be a valid date

### SEO Validation
- Title: 40-60 characters recommended
- Description: 150-160 characters recommended
- Keywords: 5-10 keywords recommended

### Tag Validation
- Must be an array with at least one tag
- Tags should be lowercase strings

### Image Validation
- Featured image path must be provided
- Path should start with `/images/`

## Common Errors and Solutions

### Error: "Missing required field: title"
**Solution:** Add the `title` field to your frontmatter.

### Error: "Invalid category"
**Solution:** Use one of the valid categories listed above.

### Error: "Invalid date format"
**Solution:** Use YYYY-MM-DD format (e.g., "2024-01-15").

### Error: "SEO title too long"
**Solution:** Keep SEO title under 60 characters.

### Error: "Tags must be an array"
**Solution:** Format tags as an array: `tags: ["tag1", "tag2"]`

## Best Practices

1. **Write for your audience**: Focus on clinic owners, practice managers, and healthcare administrators
2. **Be actionable**: Provide specific, implementable advice
3. **Use data**: Include statistics and research to support your points
4. **Add visuals**: Include relevant images, charts, or diagrams
5. **Link internally**: Reference other blog posts and landing page sections
6. **Optimize for SEO**: Use keywords naturally and follow SEO guidelines
7. **Proofread**: Check for spelling, grammar, and formatting errors
8. **Test on mobile**: Ensure your post looks good on mobile devices

## Resources

- [Markdown Guide](https://www.markdownguide.org/)
- [MDX Documentation](https://mdxjs.com/)
- [SEO Best Practices](https://developers.google.com/search/docs/fundamentals/seo-starter-guide)
- [Healthcare Content Guidelines](https://www.hhs.gov/web/section-508/index.html)

## Support

For questions or issues with blog post creation:
1. Check this guide first
2. Review existing blog posts for examples
3. Run the validation tool to identify issues
4. Contact the development team for technical support
