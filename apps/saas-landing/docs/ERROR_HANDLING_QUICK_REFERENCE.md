# Error Handling Quick Reference

## Quick Start

### Wrap Components with Error Boundary

```tsx
import { ErrorBoundary } from '@/components/ErrorBoundary';

<ErrorBoundary section="section-name">
  <YourComponent />
</ErrorBoundary>
```

### Load Content Safely

```typescript
import { loadHealthcareCopy } from '@/lib/content/loader';

const content = await loadHealthcareCopy('en'); // Always returns valid content
```

### Handle Blog Errors

```typescript
import { getBlogPost } from '@/lib/blog/get-posts';
import { BlogPostError } from '@/components/blog/BlogPostError';

const post = await getBlogPost(slug);
if (!post) {
  return <BlogPostError slug={slug} />;
}
```

### Log Errors

```typescript
import { logError, ErrorSeverity } from '@/lib/error-logger';

logError(error, { component: 'MyComponent' }, ErrorSeverity.HIGH);
```

## Error Types

| Type | Description | Action |
|------|-------------|--------|
| Content Load Error | Failed to load configuration | Use fallback content |
| Blog Not Found | Post doesn't exist | Show 404 page |
| Blog Parsing Error | MDX parsing failed | Show error page |
| Component Error | React render error | Show error boundary |
| Analytics Error | Tracking failed | Log and continue |

## Error Severity Levels

- **LOW** - Informational, no action needed
- **MEDIUM** - Standard error, monitor
- **HIGH** - Serious issue, investigate
- **CRITICAL** - System failure, urgent

## Common Patterns

### Pattern 1: Safe Data Fetching

```typescript
try {
  const data = await fetchData();
  return <Component data={data} />;
} catch (error) {
  logError(error, { component: 'DataComponent' });
  return <ErrorFallback />;
}
```

### Pattern 2: Fallback Content

```typescript
const content = await loadContentSection('hero', 'en');
// content is guaranteed to be valid (primary or fallback)
```

### Pattern 3: Silent Failure

```typescript
try {
  trackAnalyticsEvent('page_view');
} catch (error) {
  console.error('Analytics error:', error);
  // Don't disrupt user experience
}
```

### Pattern 4: Image Error Handling

```tsx
<Image
  src={imageSrc}
  alt={alt}
  onError={(e) => {
    e.currentTarget.src = '/images/placeholder.jpg';
  }}
/>
```

## File Locations

| File | Purpose |
|------|---------|
| `components/ErrorBoundary.tsx` | React error boundary |
| `lib/content/loader.ts` | Content loading with fallback |
| `lib/content/fallback.ts` | Fallback content definitions |
| `lib/blog/error-handler.ts` | Blog-specific error handling |
| `lib/error-logger.ts` | Centralized error logging |
| `components/blog/BlogPostError.tsx` | Blog error pages |

## Testing Checklist

- [ ] Test content loading failure
- [ ] Test blog post 404
- [ ] Test blog post parsing error
- [ ] Test component error boundary
- [ ] Test image loading failure
- [ ] Verify error logging to GA4
- [ ] Check fallback content displays correctly
- [ ] Test error recovery (retry functionality)

## Monitoring

### Check Error Logs

```bash
# Development
Check browser console for error logs

# Production
1. Open Google Analytics 4
2. Navigate to Events
3. Filter by 'exception' event
4. Review error descriptions and context
```

### Error Metrics to Monitor

- Exception event count
- Error types distribution
- Component error frequency
- Blog post error rate
- Content loading failures

## Quick Fixes

### Content Not Loading
```typescript
// Check if content file exists and is valid
import { validateContent } from '@/lib/content/loader';
const isValid = validateContent(content);
```

### Blog Post Errors
```bash
# Validate frontmatter
npm run validate-blog-posts

# Check file encoding
file -I content/blog/post-name.mdx
```

### Error Boundary Not Working
```tsx
// Ensure ErrorBoundary is parent component
<ErrorBoundary>
  <ComponentThatMightError />
</ErrorBoundary>
```

## Support

For detailed information, see [ERROR_HANDLING_GUIDE.md](./ERROR_HANDLING_GUIDE.md)
