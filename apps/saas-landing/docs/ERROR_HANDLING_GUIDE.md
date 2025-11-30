# Error Handling Implementation Guide

## Overview

This document describes the comprehensive error handling system implemented for the SaaS landing page. The system ensures graceful degradation, user-friendly error messages, and proper error logging for monitoring.

## Architecture

### Error Handling Layers

1. **Content Loading Errors** - Fallback content for configuration failures
2. **Blog Post Errors** - Graceful handling of MDX parsing and loading errors
3. **Component Errors** - React Error Boundaries for component failures
4. **Analytics Errors** - Silent failures for tracking scripts
5. **Image Loading Errors** - Fallback images and placeholders

## Components

### 1. ErrorBoundary Component

**Location**: `components/ErrorBoundary.tsx`

React Error Boundary that catches errors in child components and displays fallback UI.

**Usage**:
```tsx
<ErrorBoundary section="hero">
  <HeroSection />
</ErrorBoundary>
```

**Features**:
- Catches React rendering errors
- Logs errors to analytics
- Displays user-friendly error message
- Provides retry functionality
- Custom fallback UI support

### 2. Content Loader with Fallback

**Location**: `lib/content/loader.ts`

Safely loads healthcare content with automatic fallback to minimal viable content.

**Usage**:
```typescript
import { loadHealthcareCopy } from '@/lib/content/loader';

const content = await loadHealthcareCopy('en');
```

**Features**:
- Validates content structure
- Returns fallback content on error
- Logs errors for monitoring
- Supports both English and Arabic

### 3. Blog Error Handling

**Location**: `lib/blog/error-handler.ts`

Specialized error handling for blog operations.

**Error Types**:
- `NOT_FOUND` - Blog post doesn't exist
- `PARSING_ERROR` - MDX parsing failed
- `INVALID_FRONTMATTER` - Missing or invalid metadata
- `FILE_READ_ERROR` - File system error
- `UNKNOWN` - Unexpected error

**Usage**:
```typescript
import { handleBlogPostError, BlogError } from '@/lib/blog/error-handler';

try {
  const post = await getBlogPost(slug);
} catch (error) {
  const blogError = handleBlogPostError(error, slug);
  // Handle error appropriately
}
```

### 4. Blog Post Error Components

**Location**: `components/blog/BlogPostError.tsx`

User-friendly error pages for blog post failures.

**Components**:
- `BlogPostError` - For 404 not found errors
- `BlogPostParsingError` - For content parsing errors

**Features**:
- Clear error messaging
- Navigation options (back to blog, home)
- Helpful suggestions
- Consistent design with site theme

## Error Logging

### Error Logger Utility

**Location**: `lib/error-logger.ts`

Centralized error logging with severity levels and context.

**Severity Levels**:
- `LOW` - Minor issues, informational
- `MEDIUM` - Standard errors, needs attention
- `HIGH` - Serious errors, immediate attention
- `CRITICAL` - System-breaking errors, urgent

**Usage**:
```typescript
import { logError, ErrorSeverity, createComponentLogger } from '@/lib/error-logger';

// Direct logging
logError(
  new Error('Failed to load content'),
  { component: 'HeroSection', action: 'loadContent' },
  ErrorSeverity.HIGH
);

// Component-specific logger
const logger = createComponentLogger('BlogList');
logger.error('Failed to fetch posts', { page: 1 });
logger.warning('Slow response time', { duration: 5000 });
```

**Integration**:
- Logs to console in development
- Sends to Google Analytics in production
- Can integrate with Sentry, LogRocket, etc.

## Fallback Content

### Healthcare Content Fallback

**Location**: `lib/content/fallback.ts`

Minimal viable content for critical sections when primary content fails.

**Sections Covered**:
- Hero section
- Features section
- Testimonials section
- Pricing section
- Integrations section
- Security section

**Languages**:
- English (en)
- Arabic (ar)

## Implementation Examples

### 1. Wrapping Components with Error Boundary

```tsx
import { ErrorBoundary } from '@/components/ErrorBoundary';

export default function Page() {
  return (
    <ErrorBoundary section="pricing">
      <PricingSection />
    </ErrorBoundary>
  );
}
```

### 2. Safe Content Loading

```typescript
import { loadContentSection } from '@/lib/content/loader';

export async function HeroSection() {
  const heroContent = await loadContentSection('hero', 'en');
  // heroContent will always be valid, either primary or fallback
  
  return <Hero content={heroContent} />;
}
```

### 3. Blog Post Error Handling

```typescript
import { getBlogPost } from '@/lib/blog/get-posts';
import { BlogPostError } from '@/components/blog/BlogPostError';

export default async function BlogPostPage({ params }) {
  try {
    const post = await getBlogPost(params.slug);
    
    if (!post) {
      return <BlogPostError slug={params.slug} />;
    }
    
    return <BlogPost post={post} />;
  } catch (error) {
    return <BlogPostParsingError slug={params.slug} />;
  }
}
```

### 4. Image Error Handling

```tsx
import Image from 'next/image';

<Image
  src={post.featuredImage}
  alt={post.title}
  width={600}
  height={400}
  onError={(e) => {
    e.currentTarget.src = '/images/blog-placeholder.jpg';
  }}
/>
```

## Error Monitoring

### Google Analytics Integration

All errors are automatically logged to Google Analytics with the following structure:

```javascript
gtag('event', 'exception', {
  description: 'Error message',
  fatal: false, // true for critical errors
  component: 'ComponentName',
  action: 'actionName',
});
```

### Viewing Errors in GA4

1. Navigate to Events in GA4
2. Look for `exception` events
3. Filter by `description`, `component`, or `fatal` parameters

### Production Error Tracking

For production environments, integrate with error tracking services:

**Sentry Integration** (example):
```typescript
import * as Sentry from '@sentry/nextjs';

Sentry.init({
  dsn: process.env.NEXT_PUBLIC_SENTRY_DSN,
  environment: process.env.NODE_ENV,
});

// In error logger
if (process.env.NODE_ENV === 'production') {
  Sentry.captureException(error, {
    contexts: { custom: context },
  });
}
```

## Testing Error Handling

### 1. Test Content Loading Errors

```typescript
// Temporarily break content file
// Verify fallback content is displayed
// Check console for error logs
```

### 2. Test Blog Post Errors

```bash
# Test 404 error
curl http://localhost:3000/blog/non-existent-post

# Test parsing error
# Add invalid frontmatter to a blog post
# Verify error page is displayed
```

### 3. Test Component Errors

```tsx
// Add error-throwing component
function BrokenComponent() {
  throw new Error('Test error');
}

// Wrap with ErrorBoundary
<ErrorBoundary section="test">
  <BrokenComponent />
</ErrorBoundary>

// Verify error boundary catches and displays fallback
```

## Best Practices

### 1. Always Use Error Boundaries

Wrap major sections with ErrorBoundary to prevent entire page crashes:

```tsx
<ErrorBoundary section="features">
  <FeaturesSection />
</ErrorBoundary>
```

### 2. Provide Fallback Content

Always have fallback content for critical sections:

```typescript
const content = await loadContentSection('hero', 'en');
// Never returns null, always valid content
```

### 3. Log Errors with Context

Include relevant context when logging errors:

```typescript
logError(error, {
  component: 'BlogList',
  action: 'fetchPosts',
  metadata: { page: 1, category: 'technology' }
}, ErrorSeverity.HIGH);
```

### 4. User-Friendly Error Messages

Never expose technical errors to users:

```tsx
// Bad
<p>Error: ENOENT: no such file or directory</p>

// Good
<p>We're having trouble loading this content. Please try again later.</p>
```

### 5. Silent Failures for Non-Critical Features

Analytics and tracking should fail silently:

```typescript
try {
  trackEvent('page_view');
} catch (error) {
  console.error('Analytics error:', error);
  // Don't show error to user
}
```

## Troubleshooting

### Content Not Loading

1. Check console for error logs
2. Verify content files exist
3. Check file permissions
4. Validate content structure

### Blog Posts Not Displaying

1. Check MDX file exists
2. Validate frontmatter fields
3. Check for parsing errors
4. Verify file encoding (UTF-8)

### Error Boundary Not Catching Errors

1. Ensure error occurs during render
2. Check ErrorBoundary is parent of error component
3. Verify React version supports Error Boundaries
4. Check for async errors (not caught by boundaries)

## Maintenance

### Regular Tasks

1. **Review Error Logs** - Weekly review of GA4 exception events
2. **Update Fallback Content** - Keep fallback content current
3. **Test Error Paths** - Monthly testing of error scenarios
4. **Monitor Error Rates** - Set up alerts for error spikes

### Adding New Error Types

1. Define error type in appropriate error handler
2. Add logging with proper context
3. Create user-friendly error component
4. Add fallback content if needed
5. Document in this guide

## Related Documentation

- [Blog System Guide](./BLOG_SYSTEM_GUIDE.md)
- [Content Configuration](../lib/content/README.md)
- [Analytics Tracking](./ANALYTICS_QUICK_REFERENCE.md)
