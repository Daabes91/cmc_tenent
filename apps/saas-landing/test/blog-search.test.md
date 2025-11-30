# Blog Search Functionality Test Guide

## Overview
This document provides a comprehensive testing guide for the blog search functionality implementation.

## Test Environment Setup

### Prerequisites
1. Start the development server:
   ```bash
   cd apps/saas-landing
   yarn dev
   ```

2. Navigate to the blog page:
   ```
   http://localhost:3000/blog
   ```

## Test Cases

### 1. Basic Search Functionality

#### Test 1.1: Search by Title
**Steps:**
1. Navigate to `/blog`
2. Enter "clinic" in the search bar
3. Press Enter or click Search button

**Expected Results:**
- Posts with "clinic" in the title should appear
- The search term "clinic" should be highlighted in yellow in the title
- Results count should be displayed
- No posts without "clinic" in title/excerpt/content/tags should appear

#### Test 1.2: Search by Excerpt
**Steps:**
1. Enter "patient" in the search bar
2. Press Enter

**Expected Results:**
- Posts with "patient" in excerpt should appear
- The term "patient" should be highlighted in the excerpt
- Results should be sorted by relevance (title matches first)

#### Test 1.3: Search by Tags
**Steps:**
1. Enter a tag keyword (e.g., "appointments")
2. Press Enter

**Expected Results:**
- Posts with matching tags should appear
- The tag should be highlighted in the tags section
- Tag matches should appear in results

#### Test 1.4: Search by Content
**Steps:**
1. Enter a term that appears in post content but not in title/excerpt
2. Press Enter

**Expected Results:**
- Posts with the term in content should appear
- Results should show the post with content matches

### 2. Search Highlighting

#### Test 2.1: Title Highlighting
**Steps:**
1. Search for "management"
2. Observe the blog cards

**Expected Results:**
- The word "management" should be wrapped in a yellow highlight (`<mark>` tag)
- Highlighting should be case-insensitive
- Multiple occurrences should all be highlighted

#### Test 2.2: Excerpt Highlighting
**Steps:**
1. Search for "scheduling"
2. Check the excerpt text in blog cards

**Expected Results:**
- "scheduling" should be highlighted in yellow in excerpts
- Highlighting should preserve the original text case

#### Test 2.3: Tag Highlighting
**Steps:**
1. Search for a tag name
2. Check the tag pills at the bottom of blog cards

**Expected Results:**
- Matching tags should be highlighted
- Non-matching tags should not be highlighted

### 3. Empty Search Results

#### Test 3.1: No Results Found
**Steps:**
1. Search for "xyzabc123" (non-existent term)
2. Observe the results area

**Expected Results:**
- Empty state message should appear
- Message should say "No articles found"
- Should show the search term in quotes
- "Clear all filters" button should be visible
- Helpful message suggesting to try different keywords

#### Test 3.2: Clear Filters Button
**Steps:**
1. Search for a term with no results
2. Click "Clear all filters" button

**Expected Results:**
- Search bar should be cleared
- All posts should be displayed again
- Category filter should reset to "all"
- Page should reset to page 1

### 4. Search Bar Interactions

#### Test 4.1: Enter Key Search
**Steps:**
1. Type a search term
2. Press Enter key

**Expected Results:**
- Search should execute
- Results should update
- Loading indicator should briefly appear

#### Test 4.2: Escape Key Clear
**Steps:**
1. Type a search term
2. Press Escape key

**Expected Results:**
- Search bar should clear
- All posts should be displayed
- Search should reset

#### Test 4.3: Clear Button
**Steps:**
1. Type a search term
2. Click the X button in the search bar

**Expected Results:**
- Search bar should clear immediately
- All posts should be displayed
- X button should disappear

#### Test 4.4: Auto-search with Debounce
**Steps:**
1. Type a search term slowly
2. Wait 500ms without typing

**Expected Results:**
- Search should execute automatically after 500ms
- No need to press Enter
- Loading indicator should appear briefly

#### Test 4.5: Loading State
**Steps:**
1. Type a search term
2. Press Enter
3. Observe the search icon

**Expected Results:**
- Search icon should change to a spinning loader
- Search button should show loading state
- Loading should complete within 300ms

### 5. Search with Filters

#### Test 5.1: Search + Category Filter
**Steps:**
1. Select "Practice Management" category
2. Search for "patient"

**Expected Results:**
- Only posts in "Practice Management" category with "patient" should appear
- Both filters should be active
- Results count should reflect both filters

#### Test 5.2: Clear Search, Keep Category
**Steps:**
1. Apply both search and category filter
2. Clear only the search term

**Expected Results:**
- Category filter should remain active
- All posts in selected category should appear
- Search highlighting should disappear

### 6. Pagination with Search

#### Test 6.1: Search Results Pagination
**Steps:**
1. Search for a common term that returns many results
2. Check if pagination appears

**Expected Results:**
- Pagination should work with search results
- Page numbers should reflect filtered results count
- Navigating pages should maintain search term
- Highlighting should persist across pages

#### Test 6.2: Page Reset on New Search
**Steps:**
1. Navigate to page 2 of results
2. Perform a new search

**Expected Results:**
- Page should reset to page 1
- New search results should appear
- Pagination should update for new results

### 7. Edge Cases

#### Test 7.1: Empty Search Term
**Steps:**
1. Click search button without entering text
2. Or enter only spaces

**Expected Results:**
- All posts should be displayed
- No filtering should occur
- No error should appear

#### Test 7.2: Special Characters in Search
**Steps:**
1. Search for terms with special characters: "patient's", "clinic-management"

**Expected Results:**
- Search should handle special characters correctly
- Highlighting should work with special characters
- No JavaScript errors should occur

#### Test 7.3: Very Long Search Term
**Steps:**
1. Enter a very long search term (100+ characters)

**Expected Results:**
- Search should execute without errors
- UI should handle long terms gracefully
- No layout breaking should occur

#### Test 7.4: Case Sensitivity
**Steps:**
1. Search for "PATIENT"
2. Then search for "patient"
3. Then search for "Patient"

**Expected Results:**
- All three searches should return the same results
- Search should be case-insensitive
- Highlighting should preserve original case

### 8. Accessibility

#### Test 8.1: Keyboard Navigation
**Steps:**
1. Tab to the search input
2. Type a search term
3. Press Enter
4. Tab to clear button
5. Press Space or Enter

**Expected Results:**
- All interactive elements should be keyboard accessible
- Focus indicators should be visible
- Enter and Space keys should work on buttons

#### Test 8.2: Screen Reader Support
**Steps:**
1. Use a screen reader (NVDA, JAWS, or VoiceOver)
2. Navigate to search bar
3. Perform a search

**Expected Results:**
- Search input should have proper aria-label
- Buttons should have descriptive labels
- Results count should be announced
- Empty state should be announced

### 9. Performance

#### Test 9.1: Search Speed
**Steps:**
1. Search for various terms
2. Measure response time

**Expected Results:**
- Search should complete in < 300ms
- No noticeable lag
- Smooth transitions

#### Test 9.2: Large Result Sets
**Steps:**
1. Search for a very common term
2. Check rendering performance

**Expected Results:**
- Page should remain responsive
- No frame drops
- Smooth scrolling

### 10. Mobile Responsiveness

#### Test 10.1: Mobile Search Bar
**Steps:**
1. Open blog page on mobile device (or use DevTools mobile view)
2. Test search functionality

**Expected Results:**
- Search bar should be full width on mobile
- Touch targets should be at least 44px
- Keyboard should appear when tapping input
- Search should work the same as desktop

#### Test 10.2: Mobile Results Display
**Steps:**
1. Perform a search on mobile
2. Check results layout

**Expected Results:**
- Results should stack vertically
- Highlighting should be visible
- Empty state should be readable
- Clear button should be easily tappable

## Automated Testing

### Unit Tests
Run unit tests for search functionality:
```bash
cd apps/saas-landing
yarn test lib/blog/search.test.ts
```

### Integration Tests
Run integration tests:
```bash
yarn test:e2e blog-search
```

## Known Issues
- None at this time

## Browser Compatibility
Test in the following browsers:
- [ ] Chrome (latest)
- [ ] Firefox (latest)
- [ ] Safari (latest)
- [ ] Edge (latest)
- [ ] Mobile Safari (iOS)
- [ ] Chrome Mobile (Android)

## Success Criteria
- ✅ All test cases pass
- ✅ No console errors
- ✅ Search is fast and responsive
- ✅ Highlighting works correctly
- ✅ Empty states are helpful
- ✅ Keyboard navigation works
- ✅ Mobile experience is smooth

## Reporting Issues
If you find any issues during testing:
1. Note the test case number
2. Describe the expected vs actual behavior
3. Include browser/device information
4. Provide steps to reproduce
5. Include screenshots if applicable
