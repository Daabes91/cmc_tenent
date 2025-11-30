# Related Posts Feature - Quick Reference

## Overview
The related posts feature automatically displays relevant blog posts at the end of each blog post detail page, encouraging readers to continue engaging with your content.

## Implementation Summary

### Components Created
- **`components/blog/RelatedPosts.tsx`**: Main component that displays related posts
- **`RelatedPostCard`**: Individual card component for each related post

### Integration Points
- **`app/blog/[slug]/page.tsx`**: Blog post detail page that fetches and displays related posts

### Algorithm
The related posts algorithm calculates a relevance score based on:
1. **Same category**: +10 points
2. **Matching tags**: +5 points per matching tag
3. **Same author**: +3 points
4. **Recent posts** (within 30 days): +2 points

Posts are sorted by relevance score (highest first) and the top 3 are displayed.

## Usage

### Automatic Display
Related posts are automatically displayed at the end of every blog post that has at least one related post available.

### Configuration
The number of related posts can be adjusted in `app/blog/[slug]/page.tsx`:

```typescript
// Get related posts (default: 3)
const relatedPosts = await getRelatedPosts(post, 3);
```

## Features

### Visual Design
- Grid layout (3 columns on desktop, 2 on tablet, 1 on mobile)
- Featured image with hover zoom effect
- Category badge
- Reading time indicator
- Title with hover color change
- Excerpt preview
- Author and publication date
- Card hover effects (border, shadow)

### Behavior
- Excludes the current post from related posts
- Shows maximum of 3 related posts
- Hides section if no related posts are available
- Responsive design for all screen sizes
- Keyboard accessible
- Screen reader friendly

## API Reference

### `getRelatedPosts(currentPost, limit)`
Fetches related posts for a given blog post.

**Parameters**:
- `currentPost` (BlogPost): The current blog post
- `limit` (number, optional): Maximum number of related posts to return (default: 3)

**Returns**: `Promise<BlogPost[]>`

**Example**:
```typescript
const relatedPosts = await getRelatedPosts(post, 3);
```

### `RelatedPosts` Component
Displays a grid of related blog posts.

**Props**:
- `posts` (BlogPost[]): Array of related blog posts to display

**Example**:
```tsx
<RelatedPosts posts={relatedPosts} />
```

## Styling

### Tailwind Classes Used
- Layout: `grid`, `grid-cols-1`, `md:grid-cols-2`, `lg:grid-cols-3`
- Spacing: `gap-6`, `p-5`, `mb-8`
- Typography: `text-3xl`, `font-bold`, `line-clamp-2`
- Effects: `hover:scale-105`, `transition-transform`, `hover:shadow-lg`
- Colors: `bg-card`, `text-primary`, `text-muted-foreground`

### Customization
To customize the appearance, modify the Tailwind classes in `components/blog/RelatedPosts.tsx`.

## Testing

### Manual Testing
See `test/related-posts.test.md` for comprehensive manual testing guide.

### Key Test Scenarios
1. Related posts display correctly
2. Algorithm prioritizes same category
3. Posts with matching tags appear
4. Current post is excluded
5. Maximum of 3 posts shown
6. Responsive design works
7. Navigation functions correctly
8. Hover effects work
9. Accessibility features work

## Performance

### Optimization
- Related posts are fetched server-side during page generation
- No client-side data fetching required
- Images use Next.js Image component for optimization
- Lazy loading for images

### Caching
Related posts are calculated at build time for static pages, ensuring fast load times.

## Accessibility

### Features
- Semantic HTML structure
- Keyboard navigation support
- Focus indicators on interactive elements
- Screen reader friendly labels
- Alt text for images
- ARIA labels where appropriate

## Troubleshooting

### No Related Posts Showing
**Possible causes**:
1. Only one blog post exists
2. Current post has unique category and tags
3. All other posts are drafts or scheduled

**Solution**: Add more published blog posts with similar categories or tags

### Related Posts Not Relevant
**Possible causes**:
1. Posts have different categories
2. No matching tags
3. Algorithm needs tuning

**Solution**: Adjust relevance score weights in `lib/blog/related.ts`

### Layout Issues
**Possible causes**:
1. Missing featured images
2. Long titles or excerpts
3. CSS conflicts

**Solution**: Check Tailwind classes and ensure consistent content structure

## Future Enhancements

Potential improvements:
1. User preference-based recommendations
2. View count-based popularity
3. Machine learning for better relevance
4. A/B testing different algorithms
5. Related posts analytics tracking

## Related Files

- `lib/blog/related.ts` - Related posts algorithm
- `lib/blog/types.ts` - TypeScript interfaces
- `components/blog/RelatedPosts.tsx` - Display component
- `app/blog/[slug]/page.tsx` - Integration point
- `test/related-posts.test.md` - Testing guide

## Support

For issues or questions about the related posts feature:
1. Check this documentation
2. Review the test guide
3. Examine the implementation in the source files
4. Check browser console for errors
