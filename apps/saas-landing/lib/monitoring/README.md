# Monitoring and Analytics System

Comprehensive monitoring and analytics infrastructure for the SaaS landing page.

## Overview

This monitoring system provides:

1. **Error Monitoring** - Track and report errors in production
2. **Performance Monitoring** - Monitor Core Web Vitals and custom metrics
3. **Analytics Dashboard** - Track key business metrics and user behavior
4. **Rate Limiting** - Prevent abuse of blog search functionality

## Quick Start

### 1. Initialize Monitoring

Add to your app entry point (e.g., `app/layout.tsx`):

```typescript
import { initMonitoring } from '@/lib/monitoring';

// In a client component or useEffect
useEffect(() => {
  initMonitoring({
    sentryDsn: process.env.NEXT_PUBLIC_SENTRY_DSN,
    sentryEnvironment: process.env.NODE_ENV,
    gaEnabled: true,
    gaMeasurementId: process.env.NEXT_PUBLIC_GA_MEASUREMENT_ID,
    performanceEnabled: true,
    debug: process.env.NODE_ENV === 'development',
  });
}, []);
```

### 2. Environment Variables

Add to your `.env.local`:

```bash
# Error Monitoring (Sentry)
NEXT_PUBLIC_SENTRY_DSN=https://your-sentry-dsn@sentry.io/project-id

# Analytics
NEXT_PUBLIC_GA_MEASUREMENT_ID=G-XXXXXXXXXX

# Optional: Custom analytics endpoint
NEXT_PUBLIC_ANALYTICS_ENDPOINT=https://your-api.com/analytics
```

## Features

### Error Monitoring

Track errors and exceptions in production:

```typescript
import { ErrorMonitoring } from '@/lib/monitoring';

try {
  // Your code
} catch (error) {
  ErrorMonitoring.captureException(error, {
    level: 'error',
    tags: { component: 'BlogPost' },
    extra: { postId: '123' },
  });
}
```

**Features:**
- Automatic error capture
- Unhandled promise rejection tracking
- Global error handler
- User context tracking
- Breadcrumb tracking
- Integration with Google Analytics

### Performance Monitoring

Monitor Core Web Vitals and custom metrics:

```typescript
import { initPerformanceMonitoring, getCurrentPerformanceMetrics } from '@/lib/monitoring';

// Initialize
initPerformanceMonitoring();

// Get current metrics
const metrics = getCurrentPerformanceMetrics();
console.log('LCP:', metrics?.lcp);
console.log('FID:', metrics?.fid);
console.log('CLS:', metrics?.cls);
```

**Monitored Metrics:**
- **LCP** (Largest Contentful Paint) - Loading performance
- **FID** (First Input Delay) - Interactivity
- **CLS** (Cumulative Layout Shift) - Visual stability
- **FCP** (First Contentful Paint) - Initial render
- **TTFB** (Time to First Byte) - Server response time
- **INP** (Interaction to Next Paint) - Responsiveness

**Custom Metrics:**
- Page load time
- DOM content loaded time
- Resource load time
- Slow resource detection

### Analytics Dashboard

Track key business metrics:

```typescript
import { getDashboardMetrics, getRealTimeMetrics } from '@/lib/monitoring';

// Get metrics for date range
const metrics = getDashboardMetrics(
  new Date('2024-01-01'),
  new Date('2024-01-31')
);

console.log('Page Views:', metrics.pageViews);
console.log('Conversion Rate:', metrics.conversionRate);
console.log('CTA Clicks:', metrics.ctaClicks);

// Get real-time metrics
const realTime = getRealTimeMetrics();
console.log('Active Users:', realTime.activeUsers);
console.log('Top Pages:', realTime.topPages);
```

**Tracked Metrics:**

**Traffic:**
- Page views
- Unique visitors
- Bounce rate
- Session duration
- Traffic sources (direct, organic, social, referral, paid)

**Conversions:**
- Signup started
- Demo requested
- Pricing viewed
- Conversion rate

**Engagement:**
- Blog post reads
- Average time on blog
- CTA clicks
- CTA click rate

**Performance:**
- Average page load time
- Average Core Web Vitals

### Rate Limiting

Prevent abuse of blog search:

```typescript
import { checkBlogSearchRateLimit, getRateLimitIdentifier } from '@/lib/blog/rate-limit';

// Check rate limit
const identifier = getRateLimitIdentifier();
const result = checkBlogSearchRateLimit(identifier);

if (!result.success) {
  console.error(result.message);
  console.log('Reset at:', new Date(result.resetAt));
} else {
  // Perform search
  console.log('Remaining requests:', result.remaining);
}
```

**Configuration:**
- 10 requests per minute per user
- Automatic cleanup of old entries
- IP-based or session-based identification
- Customizable limits and windows

**React Hook:**

```typescript
import { useRateLimit } from '@/lib/blog/rate-limit';

function BlogSearch() {
  const { checkLimit, resetLimit } = useRateLimit();

  const handleSearch = (query: string) => {
    const result = checkLimit();

    if (!result.success) {
      toast.error(result.message);
      return;
    }

    // Perform search
    searchBlogPosts(query);
  };

  return <SearchBar onSearch={handleSearch} />;
}
```

## API Routes with Rate Limiting

Protect API routes with rate limiting middleware:

```typescript
// app/api/blog/search/route.ts
import { withRateLimit } from '@/lib/blog/rate-limit';

async function handler(request: Request) {
  const { searchParams } = new URL(request.url);
  const query = searchParams.get('q');

  // Perform search
  const results = await searchBlogPosts(query);

  return Response.json(results);
}

export const GET = withRateLimit(handler);
```

## Monitoring Dashboard

### View Metrics

```typescript
import { getDashboardMetrics, exportMetricsAsCSV } from '@/lib/monitoring';

// Get metrics
const metrics = getDashboardMetrics();

// Export as CSV
const csv = exportMetricsAsCSV(metrics);
console.log(csv);
```

### Real-Time Monitoring

```typescript
import { getRealTimeMetrics } from '@/lib/monitoring';

// Update every 5 seconds
setInterval(() => {
  const realTime = getRealTimeMetrics();
  console.log('Active Users:', realTime.activeUsers);
  console.log('Recent Events:', realTime.recentEvents.length);
}, 5000);
```

## Integration with Google Analytics

All monitoring events are automatically sent to Google Analytics when configured:

```typescript
// Automatically tracked:
// - Page views
// - Conversions (signup, demo, pricing)
// - CTA clicks
// - Blog reads
// - Feature interactions
// - Errors and exceptions
// - Performance metrics
```

## Production Setup

### 1. Configure Sentry (Optional)

1. Create a Sentry account at https://sentry.io
2. Create a new project
3. Copy the DSN
4. Add to environment variables:

```bash
NEXT_PUBLIC_SENTRY_DSN=https://your-dsn@sentry.io/project-id
```

### 2. Configure Google Analytics

Already configured in Task 13. Ensure `NEXT_PUBLIC_GA_MEASUREMENT_ID` is set.

### 3. Monitor Core Web Vitals

Install web-vitals package (if not already installed):

```bash
npm install web-vitals
```

### 4. Set Up Alerts

Configure alerts in Google Analytics:
- Page load time > 3 seconds
- Conversion rate drops > 20%
- Error rate increases
- CLS > 0.25

## Performance Thresholds

Based on Google's recommendations:

| Metric | Good | Needs Improvement | Poor |
|--------|------|-------------------|------|
| LCP | ≤ 2.5s | 2.5s - 4.0s | > 4.0s |
| FID | ≤ 100ms | 100ms - 300ms | > 300ms |
| CLS | ≤ 0.1 | 0.1 - 0.25 | > 0.25 |
| FCP | ≤ 1.8s | 1.8s - 3.0s | > 3.0s |
| TTFB | ≤ 800ms | 800ms - 1800ms | > 1800ms |
| INP | ≤ 200ms | 200ms - 500ms | > 500ms |

## Troubleshooting

### Error Monitoring Not Working

1. Check Sentry DSN is configured
2. Verify environment is production
3. Check browser console for errors
4. Ensure `initMonitoring()` is called

### Performance Metrics Not Showing

1. Install web-vitals package: `npm install web-vitals`
2. Check browser supports Performance API
3. Verify `performanceEnabled: true` in config
4. Check Google Analytics is configured

### Rate Limiting Too Strict

Adjust configuration in `lib/blog/rate-limit.ts`:

```typescript
const blogSearchRateLimiter = new RateLimiter({
  maxRequests: 20, // Increase from 10
  windowMs: 60000, // Keep at 1 minute
});
```

### Analytics Not Tracking

1. Verify GA4 measurement ID is set
2. Check Google Analytics script is loaded
3. Verify events in GA4 DebugView
4. Check browser ad blockers

## Best Practices

1. **Error Handling**
   - Always wrap critical code in try-catch
   - Provide context with errors
   - Use appropriate severity levels

2. **Performance**
   - Monitor Core Web Vitals regularly
   - Set up alerts for poor performance
   - Optimize slow resources

3. **Rate Limiting**
   - Apply to all public APIs
   - Use appropriate limits for use case
   - Provide clear error messages

4. **Analytics**
   - Track meaningful events
   - Review metrics weekly
   - Set up conversion goals

## Support

For issues or questions:
- Check documentation
- Review error logs in Sentry
- Check Google Analytics reports
- Contact development team

## License

Internal use only.
