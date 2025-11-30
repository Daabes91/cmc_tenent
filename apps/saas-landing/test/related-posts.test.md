# Related Posts Feature - Manual Test Guide

## Test Overview
This document provides manual testing steps to verify the related posts feature implementation.

## Prerequisites
- Development server running (`npm run dev` in apps/saas-landing)
- At least 4 blog posts in content/blog directory

## Test Cases

### Test 1: Related Posts Display
**Objective**: Verify that related posts are displayed at the end of a blog post

**Steps**:
1. Navigate to http://localhost:3000/blog
2. Click on "5 Ways to Reduce Patient No-Shows in Your Clinic"
3. Scroll to the bottom of the article

**Expected Results**:
- ✅ A "Related Articles" section appears after the main content
- ✅ The section has a heading "Related Articles"
- ✅ A subtitle "Continue reading with these related posts" is displayed
- ✅ Up to 3 related posts are shown in a grid layout
- ✅ Each related post card displays:
  - Featured image
  - Category badge
  - Reading time
  - Title
  - Excerpt
  - Author name
  - Publication date

### Test 2: Related Posts Relevance (Same Category)
**Objective**: Verify that posts from the same category are prioritized

**Steps**:
1. Navigate to the "5 Ways to Reduce Patient No-Shows" post (category: practice-management)
2. Check the related posts section

**Expected Results**:
- ✅ "10 Appointment Scheduling Tips" should appear (same category: practice-management)
- ✅ Posts are relevant to the current post's topic

### Test 3: Related Posts Relevance (Matching Tags)
**Objective**: Verify that posts with matching tags are included

**Steps**:
1. Navigate to the "5 Ways to Reduce Patient No-Shows" post
   - Tags: appointments, patient-engagement, efficiency, scheduling
2. Check the related posts section

**Expected Results**:
- ✅ Posts with matching tags (appointments, scheduling, patient-engagement) appear
- ✅ "10 Appointment Scheduling Tips" should appear (matching tags: scheduling, appointments)
- ✅ "Patient Portal Benefits" may appear (matching tag: patient-engagement)

### Test 4: No Related Posts Available
**Objective**: Verify behavior when no related posts exist

**Steps**:
1. Create a new blog post with a unique category and tags
2. Navigate to that post
3. Check the bottom of the page

**Expected Results**:
- ✅ No "Related Articles" section is displayed
- ✅ No error messages appear
- ✅ The page renders normally

### Test 5: Related Posts Exclude Current Post
**Objective**: Verify that the current post is not included in related posts

**Steps**:
1. Navigate to any blog post
2. Check the related posts section

**Expected Results**:
- ✅ The current post does not appear in the related posts list
- ✅ Only different posts are shown

### Test 6: Related Posts Limit
**Objective**: Verify that a maximum of 3 related posts are shown

**Steps**:
1. Ensure there are at least 5 blog posts in the system
2. Navigate to any blog post
3. Count the related posts displayed

**Expected Results**:
- ✅ Maximum of 3 related posts are displayed
- ✅ The most relevant posts are shown (based on category and tag matching)

### Test 7: Related Posts Responsive Design
**Objective**: Verify that related posts display correctly on different screen sizes

**Steps**:
1. Navigate to any blog post with related posts
2. Test on different viewport sizes:
   - Desktop (1920px)
   - Tablet (768px)
   - Mobile (375px)

**Expected Results**:
- ✅ Desktop: 3 columns grid layout
- ✅ Tablet: 2 columns grid layout
- ✅ Mobile: 1 column stacked layout
- ✅ All content remains readable at all sizes
- ✅ Images scale appropriately

### Test 8: Related Posts Click Navigation
**Objective**: Verify that clicking a related post navigates correctly

**Steps**:
1. Navigate to any blog post
2. Click on a related post card

**Expected Results**:
- ✅ Navigation to the clicked post occurs
- ✅ The new post loads correctly
- ✅ The new post also shows its own related posts
- ✅ URL updates to /blog/[new-slug]

### Test 9: Related Posts Hover Effects
**Objective**: Verify visual feedback on hover

**Steps**:
1. Navigate to any blog post with related posts
2. Hover over each related post card

**Expected Results**:
- ✅ Card border changes color on hover
- ✅ Card shadow increases on hover
- ✅ Featured image scales slightly on hover
- ✅ Title color changes on hover
- ✅ Cursor changes to pointer

### Test 10: Related Posts with Missing Images
**Objective**: Verify graceful handling of missing featured images

**Steps**:
1. Create a blog post without a featured image
2. Navigate to a post that would show it as related

**Expected Results**:
- ✅ Related post card still displays
- ✅ No broken image icon appears
- ✅ Card layout remains intact
- ✅ Other information (title, excerpt, etc.) displays correctly

## Algorithm Verification

### Relevance Score Calculation
The related posts algorithm should prioritize posts based on:
1. Same category: +10 points
2. Matching tags: +5 points per tag
3. Same author: +3 points
4. Recent posts (within 30 days): +2 points

**Manual Verification**:
1. Check that posts with the same category appear first
2. Verify that posts with multiple matching tags rank higher
3. Confirm that the most relevant posts are selected

## Performance Testing

### Test 11: Related Posts Load Time
**Objective**: Verify that related posts don't significantly impact page load time

**Steps**:
1. Open browser DevTools Network tab
2. Navigate to a blog post
3. Check the page load time

**Expected Results**:
- ✅ Page loads in under 2 seconds
- ✅ Related posts section doesn't cause layout shift
- ✅ No console errors related to related posts

## Accessibility Testing

### Test 12: Related Posts Accessibility
**Objective**: Verify that related posts are accessible

**Steps**:
1. Navigate to a blog post with related posts
2. Use keyboard navigation (Tab key)
3. Use a screen reader

**Expected Results**:
- ✅ All related post cards are keyboard accessible
- ✅ Focus indicators are visible
- ✅ Screen reader announces section heading
- ✅ Screen reader announces each post title and metadata
- ✅ Images have appropriate alt text

## Test Results Summary

| Test Case | Status | Notes |
|-----------|--------|-------|
| Test 1: Display | ⬜ | |
| Test 2: Same Category | ⬜ | |
| Test 3: Matching Tags | ⬜ | |
| Test 4: No Related Posts | ⬜ | |
| Test 5: Exclude Current | ⬜ | |
| Test 6: Limit to 3 | ⬜ | |
| Test 7: Responsive | ⬜ | |
| Test 8: Navigation | ⬜ | |
| Test 9: Hover Effects | ⬜ | |
| Test 10: Missing Images | ⬜ | |
| Test 11: Performance | ⬜ | |
| Test 12: Accessibility | ⬜ | |

## Notes
- All tests should be performed in both light and dark mode
- Test with different blog post combinations to verify algorithm
- Check browser console for any errors during testing
