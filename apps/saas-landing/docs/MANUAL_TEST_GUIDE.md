# Manual Testing Guide for Blog Post Creation System

This guide provides step-by-step instructions to manually test the blog post creation system.

## Prerequisites

1. Navigate to the saas-landing directory:
   ```bash
   cd apps/saas-landing
   ```

2. Install dependencies (if not already done):
   ```bash
   npm install
   ```

## Test 1: Generate Slug from Title

### Test Case 1.1: Basic Slug Generation
```bash
npm run blog:generate-slug "5 Ways to Reduce Patient No-Shows"
```

**Expected Output:**
```
Generated slug: 5-ways-to-reduce-patient-no-shows
Valid: ✓
Unique: ✓

File path: content/blog/5-ways-to-reduce-patient-no-shows.mdx
```

### Test Case 1.2: Special Characters
```bash
npm run blog:generate-slug "HIPAA Compliance: What You Need to Know!"
```

**Expected Output:**
```
Generated slug: hipaa-compliance-what-you-need-to-know
Valid: ✓
Unique: ✓

File path: content/blog/hipaa-compliance-what-you-need-to-know.mdx
```

### Test Case 1.3: Multiple Spaces
```bash
npm run blog:generate-slug "Patient   Management    Tips"
```

**Expected Output:**
```
Generated slug: patient-management-tips
Valid: ✓
Unique: ✓

File path: content/blog/patient-management-tips.mdx
```

## Test 2: Create New Blog Post

### Test Case 2.1: Create Post from Template
```bash
npm run blog:create "Getting Started with Clinic Management"
```

**Expected Output:**
```
✓ Created new blog post:
  File: content/blog/getting-started-with-clinic-management.mdx
  Title: Getting Started with Clinic Management
  Slug: getting-started-with-clinic-management

Next steps:
  1. Edit the file and fill in the frontmatter
  2. Write your content
  3. Validate: npm run blog:validate content/blog/getting-started-with-clinic-management.mdx
  4. Preview: npm run dev (then visit /blog/getting-started-with-clinic-management)
```

**Verification:**
1. Check that the file was created:
   ```bash
   ls -la content/blog/getting-started-with-clinic-management.mdx
   ```

2. Verify the title was pre-filled:
   ```bash
   head -n 5 content/blog/getting-started-with-clinic-management.mdx
   ```
   Should show: `title: "Getting Started with Clinic Management"`

## Test 3: List Blog Posts

### Test Case 3.1: List All Posts
```bash
npm run blog:list
```

**Expected Output:**
```
Found X blog post(s):

1. [PUBLISHED] 5 Ways to Reduce Patient No-Shows in Your Clinic
   Slug: reduce-patient-no-shows
   Category: practice-management
   Published: 2024-01-15

2. [DRAFT] Getting Started with Clinic Management
   Slug: getting-started-with-clinic-management
   Category: practice-management
   Published: 2024-01-15
```

## Test 4: Validate Blog Post

### Test Case 4.1: Validate Existing Post
```bash
npm run blog:validate content/blog/example-post.mdx
```

**Expected Output:**
```
Validating: content/blog/example-post.mdx
✓ Validation passed!
```

### Test Case 4.2: Validate Post with Missing Fields

1. Create a test post with missing fields:
   ```bash
   cat > content/blog/test-invalid.mdx << 'EOF'
---
title: "Test Post"
excerpt: "Short"
---

# Test Content
EOF
   ```

2. Validate it:
   ```bash
   npm run blog:validate content/blog/test-invalid.mdx
   ```

**Expected Output:**
```
Validating: content/blog/test-invalid.mdx
✗ Validation failed!

Errors:
  ✗ author: Missing required field: author
  ✗ publishedAt: Missing required field: publishedAt
  ✗ category: Missing required field: category
  ✗ tags: Missing required field: tags
  ✗ featuredImage: Missing required field: featuredImage
  ✗ seo: Missing required field: seo

Warnings:
  ⚠ excerpt: Excerpt is shorter than recommended (5/150 characters)
```

3. Clean up:
   ```bash
   rm content/blog/test-invalid.mdx
   ```

### Test Case 4.3: Validate Post with Invalid Category

1. Create a test post with invalid category:
   ```bash
   cat > content/blog/test-invalid-category.mdx << 'EOF'
---
title: "Test Post with Invalid Category"
excerpt: "This is a test excerpt that is long enough to meet the minimum length requirement of 150 characters. It provides a good summary of the blog post content."
author:
  name: "Test Author"
  role: "Test Role"
publishedAt: "2024-01-15"
category: "invalid-category"
tags: ["test", "blog"]
featuredImage: "/images/blog/test.jpg"
seo:
  title: "Test Post: A Comprehensive Guide for Testing"
  description: "This is a test SEO description that is long enough to meet the minimum length requirement of 150 characters for optimal search engine optimization."
  keywords: ["test", "blog", "validation"]
---

# Test Content
EOF
   ```

2. Validate it:
   ```bash
   npm run blog:validate content/blog/test-invalid-category.mdx
   ```

**Expected Output:**
```
Validating: content/blog/test-invalid-category.mdx
✗ Validation failed!

Errors:
  ✗ category: Invalid category. Must be one of: practice-management, patient-care, technology, compliance, industry-news
```

3. Clean up:
   ```bash
   rm content/blog/test-invalid-category.mdx
   ```

## Test 5: Help Command

### Test Case 5.1: Display Help
```bash
npm run blog:help
```

**Expected Output:**
```
Blog Tools CLI

Usage:
  npm run blog:generate-slug <title>     Generate a slug from a title
  npm run blog:validate <file>           Validate a blog post file
  npm run blog:create <title>            Create a new blog post from template
  npm run blog:list                      List all blog posts
  npm run blog:help                      Show this help message

Examples:
  npm run blog:generate-slug "5 Ways to Reduce Patient No-Shows"
  npm run blog:validate content/blog/my-post.mdx
  npm run blog:create "Getting Started with Clinic Management"
  npm run blog:list
```

## Test 6: Programmatic Usage

### Test Case 6.1: Test Slug Generation in Code

1. Create a test file:
   ```bash
   cat > test-slug.ts << 'EOF'
import { generateSlug, isValidSlug } from './lib/blog/slug';

const title = "5 Ways to Reduce Patient No-Shows";
const slug = generateSlug(title);

console.log('Title:', title);
console.log('Slug:', slug);
console.log('Valid:', isValidSlug(slug));
EOF
   ```

2. Run it:
   ```bash
   npx tsx test-slug.ts
   ```

**Expected Output:**
```
Title: 5 Ways to Reduce Patient No-Shows
Slug: 5-ways-to-reduce-patient-no-shows
Valid: true
```

3. Clean up:
   ```bash
   rm test-slug.ts
   ```

### Test Case 6.2: Test Validation in Code

1. Create a test file:
   ```bash
   cat > test-validation.ts << 'EOF'
import { validateFrontmatter } from './lib/blog/validate';

const frontmatter = {
  title: "Test Post",
  excerpt: "Short excerpt",
  author: {
    name: "Test Author",
    role: "Test Role"
  },
  publishedAt: "2024-01-15",
  category: "practice-management",
  tags: ["test"],
  featuredImage: "/images/blog/test.jpg",
  seo: {
    title: "Test",
    description: "Test description",
    keywords: ["test"]
  }
};

const result = validateFrontmatter(frontmatter);
console.log('Valid:', result.valid);
console.log('Errors:', result.errors.length);
console.log('Warnings:', result.warnings.length);
EOF
   ```

2. Run it:
   ```bash
   npx tsx test-validation.ts
   ```

**Expected Output:**
```
Valid: true
Errors: 0
Warnings: X (warnings about length recommendations)
```

3. Clean up:
   ```bash
   rm test-validation.ts
   ```

## Test 7: End-to-End Workflow

### Complete Blog Post Creation Workflow

1. **Create a new post:**
   ```bash
   npm run blog:create "How to Improve Patient Satisfaction Scores"
   ```

2. **Edit the post:**
   ```bash
   # Open the file in your editor
   # Fill in all required fields
   # Write your content
   ```

3. **Validate the post:**
   ```bash
   npm run blog:validate content/blog/how-to-improve-patient-satisfaction-scores.mdx
   ```

4. **List all posts to verify:**
   ```bash
   npm run blog:list
   ```

5. **Preview the post:**
   ```bash
   npm run dev
   ```
   Visit: http://localhost:3000/blog/how-to-improve-patient-satisfaction-scores

6. **Clean up (optional):**
   ```bash
   rm content/blog/how-to-improve-patient-satisfaction-scores.mdx
   ```

## Test Results Checklist

Mark each test as passed (✓) or failed (✗):

- [ ] Test 1.1: Basic slug generation
- [ ] Test 1.2: Special characters handling
- [ ] Test 1.3: Multiple spaces handling
- [ ] Test 2.1: Create post from template
- [ ] Test 3.1: List all posts
- [ ] Test 4.1: Validate existing post
- [ ] Test 4.2: Validate post with missing fields
- [ ] Test 4.3: Validate post with invalid category
- [ ] Test 5.1: Display help
- [ ] Test 6.1: Programmatic slug generation
- [ ] Test 6.2: Programmatic validation
- [ ] Test 7: End-to-end workflow

## Troubleshooting

### Error: "tsx: command not found"
**Solution:** Install tsx:
```bash
npm install
```

### Error: "Cannot find module"
**Solution:** Ensure you're in the correct directory:
```bash
cd apps/saas-landing
```

### Error: "File not found"
**Solution:** Check that the file path is correct and relative to the saas-landing directory.

## Notes

- All commands should be run from the `apps/saas-landing` directory
- The blog post creation system is fully functional and ready for use
- Refer to the comprehensive guides for more detailed information:
  - `docs/BLOG_POST_CREATION_GUIDE.md`
  - `docs/BLOG_POST_CREATION_QUICK_REFERENCE.md`
