# Blog Listing Page - Manual Test Guide

## Test Environment
- **URL**: http://localhost:3000/blog
- **Browser**: Chrome, Firefox, Safari, Edge
- **Devices**: Desktop, Tablet, Mobile

## Test Cases

### 1. Page Load and Display

#### Test 1.1: Initial Page Load
**Steps:**
1. Navigate to `/blog`
2. Observe page load

**Expected Results:**
- ✅ Page loads without errors
- ✅ Header displays "Healthcare Insights Blog"
- ✅ Subtitle shows descriptive text
- ✅ Search bar is visible
- ✅ Category filters are displayed
- ✅ Blog posts are shown in grid layout

#### Test 1.2: Blog Post Cards
**Steps:**
1. Examine individual blog post cards

**Expected Results:**
- ✅ Featured image displays correctly
- ✅ Category badge shows in top-left of image
- ✅ Title is visible and clickable
- ✅ Excerpt text is displayed (max 3 lines)
- ✅ Author name and role are shown
- ✅ Publication date is formatted correctly
- ✅ Reading time is displayed
- ✅ Tags are shown (up to 3)

#### Test 1.3: Empty State
**Steps:**
1. Remove all blog posts from `content/blog/`
2. Reload page

**Expected Results:**
- ✅ Empty state message displays
- ✅ Icon is shown
- ✅ Message: "No articles yet"
- ✅ Subtext: "We're working on creating valuable content..."

### 2. Category Filtering

#### Test 2.1: Filter by Category
**Steps:**
1. Click "Practice Management" category
2. Observe filtered results
3. Click "Patient Care" category
4. Click "All Posts"

**Expected Results:**
- ✅ Only posts from selected category are shown
- ✅ Selected category button is highlighted
- ✅ Post count updates in category badges
- ✅ Page resets to 1 when category changes
- ✅ "All Posts" shows all posts again

#### Test 2.2: Category Count Accuracy
**Steps:**
1. Count posts in each category manually
2. Compare with badge numbers

**Expected Results:**
- ✅ Badge numbers match actual post counts
- ✅ "All Posts" count equals total posts

### 3. Search Functionality

#### Test 3.1: Basic Search
**Steps:**
1. Type "patient" in search bar
2. Press Enter or click Search button

**Expected Results:**
- ✅ Only posts containing "patient" are shown
- ✅ Results count displays: "Found X articles for 'patient'"
- ✅ Page resets to 1
- ✅ Search works across title, excerpt, and tags

#### Test 3.2: Clear Search
**Steps:**
1. Enter search term
2. Click X (clear) button

**Expected Results:**
- ✅ Search input clears
- ✅ All posts are shown again
- ✅ Results count disappears

#### Test 3.3: No Results
**Steps:**
1. Search for "xyz123nonexistent"

**Expected Results:**
- ✅ "No articles found" message displays
- ✅ Suggestion to adjust search criteria
- ✅ No blog cards shown

#### Test 3.4: Search + Filter Combination
**Steps:**
1. Select "Technology" category
2. Search for "clinic"

**Expected Results:**
- ✅ Only Technology posts containing "clinic" are shown
- ✅ Both filters apply simultaneously

### 4. Pagination

#### Test 4.1: Page Navigation
**Steps:**
1. Ensure more than 9 posts exist
2. Click "Next" button
3. Click page number "3"
4. Click "Previous" button

**Expected Results:**
- ✅ Next button advances to page 2
- ✅ Clicking page 3 shows page 3 posts
- ✅ Previous button goes back one page
- ✅ Page scrolls to top on navigation
- ✅ Current page is highlighted

#### Test 4.2: Pagination Boundaries
**Steps:**
1. Go to first page
2. Try clicking "Previous"
3. Go to last page
4. Try clicking "Next"

**Expected Results:**
- ✅ Previous button is disabled on page 1
- ✅ Next button is disabled on last page
- ✅ No errors occur

#### Test 4.3: Pagination with Filters
**Steps:**
1. Go to page 2
2. Change category filter

**Expected Results:**
- ✅ Page resets to 1
- ✅ Pagination updates for filtered results

#### Test 4.4: Ellipsis Display
**Steps:**
1. Create 15+ pages of posts
2. Navigate to middle pages

**Expected Results:**
- ✅ Ellipsis (...) shows between page numbers
- ✅ First and last pages always visible
- ✅ Current page and neighbors visible

### 5. Responsive Design

#### Test 5.1: Mobile View (320px - 767px)
**Steps:**
1. Resize browser to 375px width
2. Test all features

**Expected Results:**
- ✅ Single column layout
- ✅ Search bar is full width
- ✅ Category filters wrap properly
- ✅ Blog cards stack vertically
- ✅ Images scale correctly
- ✅ Text is readable (min 16px)
- ✅ Pagination buttons are touch-friendly

#### Test 5.2: Tablet View (768px - 1023px)
**Steps:**
1. Resize browser to 768px width

**Expected Results:**
- ✅ Two column grid layout
- ✅ All features work correctly
- ✅ Spacing is appropriate

#### Test 5.3: Desktop View (1024px+)
**Steps:**
1. Resize browser to 1280px width

**Expected Results:**
- ✅ Three column grid layout
- ✅ Maximum width container
- ✅ Optimal spacing and typography

### 6. Dark Mode

#### Test 6.1: Dark Mode Toggle
**Steps:**
1. Toggle dark mode on
2. Test all features
3. Toggle dark mode off

**Expected Results:**
- ✅ All components adapt to dark mode
- ✅ Text remains readable
- ✅ Images have proper contrast
- ✅ Hover states work in both modes
- ✅ No visual glitches

### 7. Interactions and Hover States

#### Test 7.1: Card Hover Effects
**Steps:**
1. Hover over blog post cards

**Expected Results:**
- ✅ Image scales slightly (zoom effect)
- ✅ Border color changes to primary
- ✅ Shadow appears
- ✅ Title color changes to primary
- ✅ Smooth transitions

#### Test 7.2: Button Hover States
**Steps:**
1. Hover over category buttons
2. Hover over pagination buttons
3. Hover over search button

**Expected Results:**
- ✅ Visual feedback on hover
- ✅ Cursor changes to pointer
- ✅ Smooth transitions

### 8. Performance

#### Test 8.1: Image Loading
**Steps:**
1. Open DevTools Network tab
2. Load blog page
3. Scroll down

**Expected Results:**
- ✅ Images load progressively
- ✅ Lazy loading works
- ✅ Appropriate image sizes served
- ✅ No layout shift

#### Test 8.2: Filter Performance
**Steps:**
1. Apply category filter
2. Type in search
3. Change pages

**Expected Results:**
- ✅ Instant filtering (no delay)
- ✅ Smooth transitions
- ✅ No lag or stuttering

### 9. Accessibility

#### Test 9.1: Keyboard Navigation
**Steps:**
1. Use Tab key to navigate
2. Use Enter to activate buttons
3. Use arrow keys in pagination

**Expected Results:**
- ✅ All interactive elements are focusable
- ✅ Focus indicators are visible
- ✅ Logical tab order
- ✅ Enter key activates buttons

#### Test 9.2: Screen Reader
**Steps:**
1. Enable screen reader
2. Navigate through page

**Expected Results:**
- ✅ Semantic HTML is used
- ✅ Images have alt text
- ✅ Buttons have descriptive labels
- ✅ Time elements are properly formatted

### 10. Edge Cases

#### Test 10.1: Single Post
**Steps:**
1. Have only 1 blog post
2. Load page

**Expected Results:**
- ✅ Single post displays correctly
- ✅ No pagination shown
- ✅ Filters still work

#### Test 10.2: Exactly 9 Posts
**Steps:**
1. Have exactly 9 posts
2. Load page

**Expected Results:**
- ✅ All posts shown on page 1
- ✅ No pagination shown

#### Test 10.3: Long Titles and Excerpts
**Steps:**
1. Create post with very long title
2. Create post with very long excerpt

**Expected Results:**
- ✅ Title wraps properly
- ✅ Excerpt truncates with ellipsis (line-clamp-3)
- ✅ Card layout doesn't break

#### Test 10.4: Missing Images
**Steps:**
1. Create post with invalid image URL

**Expected Results:**
- ✅ Placeholder or error handling
- ✅ Card still displays
- ✅ No console errors

## Test Results Template

```
Date: ___________
Tester: ___________
Browser: ___________
Device: ___________

| Test Case | Status | Notes |
|-----------|--------|-------|
| 1.1 Initial Page Load | ☐ Pass ☐ Fail | |
| 1.2 Blog Post Cards | ☐ Pass ☐ Fail | |
| 1.3 Empty State | ☐ Pass ☐ Fail | |
| 2.1 Filter by Category | ☐ Pass ☐ Fail | |
| 2.2 Category Count | ☐ Pass ☐ Fail | |
| 3.1 Basic Search | ☐ Pass ☐ Fail | |
| 3.2 Clear Search | ☐ Pass ☐ Fail | |
| 3.3 No Results | ☐ Pass ☐ Fail | |
| 3.4 Search + Filter | ☐ Pass ☐ Fail | |
| 4.1 Page Navigation | ☐ Pass ☐ Fail | |
| 4.2 Pagination Boundaries | ☐ Pass ☐ Fail | |
| 4.3 Pagination with Filters | ☐ Pass ☐ Fail | |
| 4.4 Ellipsis Display | ☐ Pass ☐ Fail | |
| 5.1 Mobile View | ☐ Pass ☐ Fail | |
| 5.2 Tablet View | ☐ Pass ☐ Fail | |
| 5.3 Desktop View | ☐ Pass ☐ Fail | |
| 6.1 Dark Mode | ☐ Pass ☐ Fail | |
| 7.1 Card Hover | ☐ Pass ☐ Fail | |
| 7.2 Button Hover | ☐ Pass ☐ Fail | |
| 8.1 Image Loading | ☐ Pass ☐ Fail | |
| 8.2 Filter Performance | ☐ Pass ☐ Fail | |
| 9.1 Keyboard Navigation | ☐ Pass ☐ Fail | |
| 9.2 Screen Reader | ☐ Pass ☐ Fail | |
| 10.1 Single Post | ☐ Pass ☐ Fail | |
| 10.2 Exactly 9 Posts | ☐ Pass ☐ Fail | |
| 10.3 Long Content | ☐ Pass ☐ Fail | |
| 10.4 Missing Images | ☐ Pass ☐ Fail | |
```

## Known Issues
- None at this time

## Notes
- Test with real healthcare content for best results
- Verify all images are optimized before testing
- Test on actual devices, not just browser DevTools
