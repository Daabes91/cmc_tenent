# Blog Draft & Scheduling Visual Test Guide

This guide provides step-by-step instructions to manually verify the draft and scheduling functionality works correctly.

## Prerequisites

1. Start the development server:
   ```bash
   cd apps/saas-landing
   pnpm dev
   ```

2. Open your browser to `http://localhost:3000`

## Test 1: Draft Posts Are Hidden

### Expected Behavior
Draft posts should NOT appear in the blog listing and should return 404 when accessed directly.

### Steps

1. **Navigate to Blog Listing**
   - Go to `http://localhost:3000/blog`
   - Look at the list of blog posts

2. **Verify Draft Post is Hidden**
   - ❌ You should NOT see "Draft Post: Work in Progress Article"
   - ✅ You should see other published posts like "5 Ways to Reduce Patient No-Shows"

3. **Try Direct Access**
   - Go to `http://localhost:3000/blog/draft-post-test`
   - ✅ You should see a 404 error page
   - ❌ You should NOT see the draft post content

### Visual Confirmation

**Blog Listing Page** (`/blog`):
```
✅ CORRECT:
┌─────────────────────────────────────┐
│ Healthcare Insights Blog            │
├─────────────────────────────────────┤
│ [Published Post 1]                  │
│ [Published Post 2]                  │
│ [Published Post 3]                  │
│ (No draft posts visible)            │
└─────────────────────────────────────┘

❌ INCORRECT:
┌─────────────────────────────────────┐
│ Healthcare Insights Blog            │
├─────────────────────────────────────┤
│ [Published Post 1]                  │
│ [Draft Post: Work in Progress]  ← Should not appear
│ [Published Post 2]                  │
└─────────────────────────────────────┘
```

**Direct Access** (`/blog/draft-post-test`):
```
✅ CORRECT:
┌─────────────────────────────────────┐
│           404                       │
│     Page Not Found                  │
│                                     │
│  The page you're looking for        │
│  doesn't exist.                     │
└─────────────────────────────────────┘

❌ INCORRECT:
┌─────────────────────────────────────┐
│ Draft Post: Work in Progress        │
│                                     │
│ This is a draft post...         ← Should not show
└─────────────────────────────────────┘
```

## Test 2: Scheduled Posts Are Hidden

### Expected Behavior
Posts with future publication dates should NOT appear in the blog listing and should return 404 when accessed directly.

### Steps

1. **Navigate to Blog Listing**
   - Go to `http://localhost:3000/blog`
   - Look at the list of blog posts

2. **Verify Scheduled Post is Hidden**
   - ❌ You should NOT see "Future Post: Upcoming Healthcare Technology Trends"
   - ✅ You should only see posts with past publication dates

3. **Try Direct Access**
   - Go to `http://localhost:3000/blog/scheduled-post-test`
   - ✅ You should see a 404 error page
   - ❌ You should NOT see the scheduled post content

### Visual Confirmation

**Blog Listing Page** (`/blog`):
```
✅ CORRECT:
┌─────────────────────────────────────┐
│ Healthcare Insights Blog            │
├─────────────────────────────────────┤
│ [Post from Jan 15, 2024]            │
│ [Post from Jan 10, 2024]            │
│ [Post from Jan 5, 2024]             │
│ (No future-dated posts visible)     │
└─────────────────────────────────────┘

❌ INCORRECT:
┌─────────────────────────────────────┐
│ Healthcare Insights Blog            │
├─────────────────────────────────────┤
│ [Future Post - Dec 31, 2025]    ← Should not appear
│ [Post from Jan 15, 2024]            │
│ [Post from Jan 10, 2024]            │
└─────────────────────────────────────┘
```

## Test 3: Published Posts Are Visible

### Expected Behavior
Posts with `draft: false` (or no draft field) and past publication dates should appear normally.

### Steps

1. **Navigate to Blog Listing**
   - Go to `http://localhost:3000/blog`

2. **Verify Published Posts Appear**
   - ✅ You should see "5 Ways to Reduce Patient No-Shows in Your Clinic"
   - ✅ You should see "Optimizing Appointment Scheduling"
   - ✅ You should see "Patient Portal Benefits"
   - ✅ You should see "HIPAA Compliance Guide"

3. **Access Published Post**
   - Click on any published post
   - ✅ You should see the full post content
   - ✅ You should see author information
   - ✅ You should see related posts at the bottom

### Visual Confirmation

**Blog Listing Page** (`/blog`):
```
✅ CORRECT:
┌─────────────────────────────────────┐
│ Healthcare Insights Blog            │
├─────────────────────────────────────┤
│ ┌─────────────────────────────────┐ │
│ │ 5 Ways to Reduce Patient        │ │
│ │ No-Shows in Your Clinic         │ │
│ │ Jan 15, 2024 • 8 min read       │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │ Optimizing Appointment          │ │
│ │ Scheduling                      │ │
│ │ Jan 10, 2024 • 6 min read       │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

## Test 4: Publishing a Draft

### Expected Behavior
When you change `draft: true` to `draft: false`, the post should become visible.

### Steps

1. **Edit Draft Post**
   - Open `content/blog/draft-post-test.mdx`
   - Change `draft: true` to `draft: false`
   - Save the file

2. **Restart Dev Server**
   - Stop the dev server (Ctrl+C)
   - Start it again: `pnpm dev`

3. **Verify Post is Now Visible**
   - Go to `http://localhost:3000/blog`
   - ✅ You should now see "Draft Post: Work in Progress Article"
   - Click on it
   - ✅ You should see the full post content

4. **Revert Changes**
   - Change `draft: false` back to `draft: true`
   - Save and restart dev server

## Test 5: Scheduling a Post

### Expected Behavior
When you change a future date to a past date, the post should become visible.

### Steps

1. **Edit Scheduled Post**
   - Open `content/blog/scheduled-post-test.mdx`
   - Change `publishedAt: "2025-12-31"` to `publishedAt: "2024-01-20"`
   - Save the file

2. **Restart Dev Server**
   - Stop the dev server (Ctrl+C)
   - Start it again: `pnpm dev`

3. **Verify Post is Now Visible**
   - Go to `http://localhost:3000/blog`
   - ✅ You should now see "Future Post: Upcoming Healthcare Technology Trends"
   - Click on it
   - ✅ You should see the full post content

4. **Revert Changes**
   - Change date back to `publishedAt: "2025-12-31"`
   - Save and restart dev server

## Test 6: Search Functionality

### Expected Behavior
Draft and scheduled posts should not appear in search results.

### Steps

1. **Navigate to Blog**
   - Go to `http://localhost:3000/blog`

2. **Search for Draft Post**
   - Use the search bar
   - Type "draft" or "work in progress"
   - ✅ The draft post should NOT appear in results

3. **Search for Scheduled Post**
   - Type "future" or "upcoming"
   - ✅ The scheduled post should NOT appear in results

4. **Search for Published Post**
   - Type "patient no-shows"
   - ✅ Published posts should appear in results

## Test 7: Category Filtering

### Expected Behavior
Draft and scheduled posts should not appear in category listings.

### Steps

1. **Navigate to Blog**
   - Go to `http://localhost:3000/blog`

2. **Filter by Category**
   - Select "Practice Management" category
   - ✅ Draft posts in this category should NOT appear
   - ✅ Only published posts should appear

3. **Check All Categories**
   - Try each category filter
   - ✅ No draft or scheduled posts should appear in any category

## Test 8: Pagination

### Expected Behavior
Draft and scheduled posts should not affect pagination counts.

### Steps

1. **Check Post Count**
   - Go to `http://localhost:3000/blog`
   - Note the total number of posts shown

2. **Verify Count**
   - ✅ Count should only include published posts
   - ❌ Count should NOT include draft or scheduled posts

3. **Navigate Pages**
   - If pagination exists, click through pages
   - ✅ No draft or scheduled posts should appear on any page

## Automated Test Verification

Run the automated test suite to verify all functionality:

```bash
pnpm test:run lib/blog/__tests__/draft-scheduling.test.ts
```

Expected output:
```
✓ Blog Post Draft and Scheduling (11 tests)
  ✓ Draft Post Visibility (3 tests)
  ✓ Scheduled Post Visibility (4 tests)
  ✓ Combined Draft and Scheduling Logic (2 tests)
  ✓ Edge Cases (2 tests)

Test Files  1 passed (1)
Tests  11 passed (11)
```

## Troubleshooting

### Issue: Draft post is visible

**Possible Causes**:
1. `draft: true` not set correctly in frontmatter
2. Syntax error in frontmatter
3. Cache not cleared

**Solutions**:
1. Check frontmatter syntax
2. Clear Next.js cache: `rm -rf .next`
3. Restart dev server

### Issue: Scheduled post appears too early

**Possible Causes**:
1. Date is in the past
2. Date format is incorrect
3. Timezone issues

**Solutions**:
1. Verify date is in the future
2. Use ISO 8601 format: `YYYY-MM-DD`
3. Check server timezone

### Issue: Post not appearing after publishing

**Possible Causes**:
1. `draft` still set to `true`
2. Date is in the future
3. Dev server not restarted

**Solutions**:
1. Check `draft: false` or remove draft field
2. Verify date is in the past
3. Restart dev server

## Success Criteria

All tests pass when:

- ✅ Draft posts do not appear in blog listing
- ✅ Draft posts return 404 when accessed directly
- ✅ Scheduled posts do not appear before publication date
- ✅ Scheduled posts return 404 before publication date
- ✅ Published posts appear normally
- ✅ Draft posts excluded from search
- ✅ Scheduled posts excluded from search
- ✅ Draft posts excluded from category filters
- ✅ Scheduled posts excluded from category filters
- ✅ Pagination counts exclude draft/scheduled posts
- ✅ All automated tests pass

## Related Documentation

- [Blog Draft & Scheduling Guide](./BLOG_DRAFT_SCHEDULING_GUIDE.md)
- [Blog Draft & Scheduling Quick Reference](./BLOG_DRAFT_SCHEDULING_QUICK_REFERENCE.md)
- [Task 12 Implementation Summary](./TASK_12_DRAFT_SCHEDULING_IMPLEMENTATION.md)
