# Blog Post Detail Page - Manual Test Guide

## Test Environment
- **URL**: http://localhost:3000/blog/example-post
- **Browser**: Chrome, Safari, Firefox, Edge
- **Devices**: Desktop, Tablet, Mobile

## Test Cases

### 1. Page Load and Content Display

#### Test 1.1: Basic Content Rendering
**Steps**:
1. Navigate to `/blog/example-post`
2. Verify page loads without errors

**Expected Results**:
- ✅ Page loads successfully
- ✅ Post title is visible and properly formatted
- ✅ Featured image displays
- ✅ Content is readable and properly formatted
- ✅ No console errors

**Status**: [ ] Pass [ ] Fail

---

#### Test 1.2: Header Information
**Steps**:
1. Check the post header section

**Expected Results**:
- ✅ Category badge displays (e.g., "Practice Management")
- ✅ Reading time shows (e.g., "8 min read")
- ✅ Post title is large and prominent
- ✅ Excerpt/description is visible
- ✅ Author avatar displays
- ✅ Author name and role are visible
- ✅ Publication date is formatted correctly

**Status**: [ ] Pass [ ] Fail

---

#### Test 1.3: MDX Content Formatting
**Steps**:
1. Scroll through the blog post content
2. Check various content elements

**Expected Results**:
- ✅ Headings (H1, H2, H3) are properly styled
- ✅ Paragraphs have proper spacing
- ✅ Lists (ordered and unordered) display correctly
- ✅ Links are styled and clickable
- ✅ Code blocks have proper formatting
- ✅ Blockquotes are styled distinctly
- ✅ Images in content display properly

**Status**: [ ] Pass [ ] Fail

---

### 2. Social Sharing Functionality

#### Test 2.1: Twitter Share
**Steps**:
1. Click the Twitter share button
2. Check the popup window

**Expected Results**:
- ✅ Twitter share dialog opens in popup
- ✅ Post title is pre-filled
- ✅ Post URL is included
- ✅ Popup dimensions are appropriate (600x400)

**Status**: [ ] Pass [ ] Fail

---

#### Test 2.2: Facebook Share
**Steps**:
1. Click the Facebook share button
2. Check the popup window

**Expected Results**:
- ✅ Facebook share dialog opens in popup
- ✅ Post URL is included
- ✅ Popup opens correctly

**Status**: [ ] Pass [ ] Fail

---

#### Test 2.3: LinkedIn Share
**Steps**:
1. Click the LinkedIn share button
2. Check the popup window

**Expected Results**:
- ✅ LinkedIn share dialog opens in popup
- ✅ Post URL is included
- ✅ Popup opens correctly

**Status**: [ ] Pass [ ] Fail

---

#### Test 2.4: Email Share
**Steps**:
1. Click the Email share button
2. Check email client opens

**Expected Results**:
- ✅ Default email client opens
- ✅ Subject contains post title
- ✅ Body contains post description and URL

**Status**: [ ] Pass [ ] Fail

---

#### Test 2.5: Copy Link
**Steps**:
1. Click the Copy Link button
2. Check for visual feedback
3. Paste into a text editor

**Expected Results**:
- ✅ "Copied!" tooltip appears
- ✅ Tooltip disappears after 2 seconds
- ✅ Correct URL is copied to clipboard
- ✅ Pasted URL matches blog post URL

**Status**: [ ] Pass [ ] Fail

---

### 3. Navigation and Links

#### Test 3.1: Category Link
**Steps**:
1. Click the category badge in the header
2. Verify navigation

**Expected Results**:
- ✅ Navigates to `/blog?category=practice-management`
- ✅ Blog listing shows filtered posts
- ✅ Category filter is applied

**Status**: [ ] Pass [ ] Fail

---

#### Test 3.2: Tag Links
**Steps**:
1. Scroll to the tags section
2. Click on a tag (e.g., "#appointments")
3. Verify navigation

**Expected Results**:
- ✅ Navigates to `/blog?tag=appointments`
- ✅ Blog listing shows filtered posts
- ✅ Tag filter is applied

**Status**: [ ] Pass [ ] Fail

---

#### Test 3.3: Back to Blog Link
**Steps**:
1. Scroll to the bottom
2. Click "← Back to all posts"

**Expected Results**:
- ✅ Navigates to `/blog`
- ✅ Blog listing page displays

**Status**: [ ] Pass [ ] Fail

---

### 4. SEO and Meta Tags

#### Test 4.1: Page Title and Description
**Steps**:
1. View page source (Ctrl+U or Cmd+Option+U)
2. Search for `<title>` and `<meta name="description"`

**Expected Results**:
- ✅ Title tag contains post SEO title
- ✅ Description meta tag contains post SEO description
- ✅ Keywords meta tag is present

**Status**: [ ] Pass [ ] Fail

---

#### Test 4.2: Open Graph Tags
**Steps**:
1. View page source
2. Search for `og:` tags

**Expected Results**:
- ✅ `og:title` is present
- ✅ `og:description` is present
- ✅ `og:type` is "article"
- ✅ `og:image` contains featured image URL
- ✅ `og:url` contains post URL
- ✅ `article:published_time` is present
- ✅ `article:author` is present

**Status**: [ ] Pass [ ] Fail

---

#### Test 4.3: Twitter Card Tags
**Steps**:
1. View page source
2. Search for `twitter:` tags

**Expected Results**:
- ✅ `twitter:card` is "summary_large_image"
- ✅ `twitter:title` is present
- ✅ `twitter:description` is present
- ✅ `twitter:image` is present

**Status**: [ ] Pass [ ] Fail

---

#### Test 4.4: Structured Data
**Steps**:
1. View page source
2. Search for `application/ld+json`
3. Copy JSON and validate at https://validator.schema.org/

**Expected Results**:
- ✅ BlogPosting schema is present
- ✅ Schema includes headline, description, image
- ✅ Schema includes author information
- ✅ Schema includes datePublished
- ✅ Schema validates without errors

**Status**: [ ] Pass [ ] Fail

---

### 5. Responsive Design

#### Test 5.1: Mobile View (375px)
**Steps**:
1. Open DevTools (F12)
2. Toggle device toolbar
3. Select iPhone SE or similar (375px width)

**Expected Results**:
- ✅ Content is readable without horizontal scroll
- ✅ Featured image scales properly
- ✅ Text is at least 16px
- ✅ Social buttons are accessible
- ✅ Author avatar displays correctly
- ✅ Tags wrap properly

**Status**: [ ] Pass [ ] Fail

---

#### Test 5.2: Tablet View (768px)
**Steps**:
1. Set viewport to 768px width

**Expected Results**:
- ✅ Layout adapts properly
- ✅ Content remains centered
- ✅ Images scale appropriately
- ✅ Social buttons are accessible

**Status**: [ ] Pass [ ] Fail

---

#### Test 5.3: Desktop View (1200px+)
**Steps**:
1. Set viewport to 1200px or wider

**Expected Results**:
- ✅ Content is centered with max-width
- ✅ Reading width is optimal (not too wide)
- ✅ Images display at full quality
- ✅ All elements are properly spaced

**Status**: [ ] Pass [ ] Fail

---

### 6. Edge Cases

#### Test 6.1: Draft Post
**Steps**:
1. Create a test post with `draft: true`
2. Try to access the post URL

**Expected Results**:
- ✅ Returns 404 Not Found
- ✅ Does not display post content

**Status**: [ ] Pass [ ] Fail

---

#### Test 6.2: Scheduled Post
**Steps**:
1. Create a test post with future `publishedAt` date
2. Try to access the post URL

**Expected Results**:
- ✅ Returns 404 Not Found
- ✅ Does not display post content

**Status**: [ ] Pass [ ] Fail

---

#### Test 6.3: Missing Featured Image
**Steps**:
1. Create a test post without `featuredImage`
2. Access the post

**Expected Results**:
- ✅ Post displays without errors
- ✅ Content is still readable
- ✅ No broken image placeholders

**Status**: [ ] Pass [ ] Fail

---

#### Test 6.4: Long Content
**Steps**:
1. Create a test post with very long content (5000+ words)
2. Access the post

**Expected Results**:
- ✅ Page loads without performance issues
- ✅ Scrolling is smooth
- ✅ All content displays correctly

**Status**: [ ] Pass [ ] Fail

---

## Browser Compatibility

### Chrome
- [ ] All tests pass
- [ ] No console errors
- [ ] Social sharing works

### Safari
- [ ] All tests pass
- [ ] No console errors
- [ ] Social sharing works

### Firefox
- [ ] All tests pass
- [ ] No console errors
- [ ] Social sharing works

### Edge
- [ ] All tests pass
- [ ] No console errors
- [ ] Social sharing works

## Performance

### Lighthouse Scores
Run Lighthouse audit and record scores:

- Performance: ___/100
- Accessibility: ___/100
- Best Practices: ___/100
- SEO: ___/100

### Core Web Vitals
- LCP (Largest Contentful Paint): ___ seconds
- FID (First Input Delay): ___ ms
- CLS (Cumulative Layout Shift): ___

## Issues Found

| Issue # | Description | Severity | Status |
|---------|-------------|----------|--------|
| 1       |             |          |        |
| 2       |             |          |        |
| 3       |             |          |        |

## Test Summary

- **Total Tests**: 24
- **Passed**: ___
- **Failed**: ___
- **Blocked**: ___

**Overall Status**: [ ] Pass [ ] Fail

**Tested By**: _______________
**Date**: _______________
**Notes**: 
