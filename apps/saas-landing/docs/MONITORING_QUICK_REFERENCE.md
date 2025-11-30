# Monitoring & Analytics Quick Reference

Quick reference for monitoring and analytics features.

## Setup

### Initialize Monitoring

```typescript
import { initMonitoring } from '@/lib/monitoring';

initMonitoring({
  sentryDsn: process.env.NEXT_PUBLIC_SENTRY_DSN,
  gaEnabled: true,
  performanceEnabled: true,
});
```

### Environment Variables

```bash
NEXT_PUBLIC_SENTRY_DSN=https://your-dsn@sentry.io/project-id
NEXT_PUBLIC_GA_MEASUREMENT_ID=G-XXXXXXXXXX
```

## Error Monitoring

### Capture Exception

```typescript
import { ErrorMonitoring } from '@/lib/monitoring';

ErrorMonitoring.captureException(error, {
  level: 'error',
  tags: { component: 'BlogPost' },
  extra: { postId: '123' },
});
```

### Capture Message

```typescript
ErrorMonitoring.captureMessage('User action completed', {
  level: 'info',
  tags: { action: 'signup' },
});
```

### Set User Context

```typescript
ErrorMonitoring.setUser({
  id: 'user-123',
  email: 'user@example.com',
});
```

## Performance Monitoring

### Get Current Metrics

```typescript
import { getCurrentPerformanceMetrics } from '@/lib/monitoring';

const metrics = getCurrentPerformanceMetrics();
console.log('LCP:', metrics?.lcp);
console.log('FID:', metrics?.fid);
console.log('CLS:', metrics?.cls);
```

### Get Performance Summary

```typescript
import { getPerformanceSummary } from '@/lib/monitoring';

const summary = getPerformanceSummary();
console.log('Average LCP:', summary.averageLCP);
console.log('Average FID:', summary.averageFID);
```

### Export Metrics

```typescript
import { exportPerformanceMetrics } from '@/lib/monitoring';

const json = exportPerformanceMetrics();
console.log(json);
```

## Analytics Dashboard

### Get Dashboard Metrics

```typescript
import { getDashboardMetrics } from '@/lib/monitoring';

const metrics = getDashboardMetrics(
  new Date('2024-01-01'),
  new Date('2024-01-31')
);

console.log('Page Views:', metrics.pageViews);
console.log('Conversion Rate:', metrics.conversionRate);
console.log('CTA Clicks:', metrics.ctaClicks);
```

### Get Real-Time Metrics

```typescript
import { getRealTimeMetrics } from '@/lib/monitoring';

const realTime = getRealTimeMetrics();
console.log('Active Users:', realTime.activeUsers);
console.log('Top Pages:', realTime.topPages);
```

### Export as CSV

```typescript
import { getDashboardMetrics, exportMetricsAsCSV } from '@/lib/monitoring';

const metrics = getDashboardMetrics();
const csv = exportMetricsAsCSV(metrics);
// Download or save CSV
```

## Rate Limiting

### Check Rate Limit

```typescript
import { checkBlogSearchRateLimit, getRateLimitIdentifier } from '@/lib/blog/rate-limit';

const identifier = getRateLimitIdentifier();
const result = checkBlogSearchRateLimit(identifier);

if (!result.success) {
  console.error(result.message);
  console.log('Reset at:', new Date(result.resetAt));
}
```

### Use Rate Limit Hook

```typescript
import { useRateLimit } from '@/lib/blog/rate-limit';

function BlogSearch() {
  const { checkLimit } = useRateLimit();

  const handleSearch = (query: string) => {
    const result = checkLimit();
    if (!result.success) {
      toast.error(result.message);
      return;
    }
    // Perform search
  };
}
```

### Protect API Route

```typescript
import { withRateLimit } from '@/lib/blog/rate-limit';

async function handler(request: Request) {
  // Your API logic
  return Response.json({ success: true });
}

export const GET = withRateLimit(handler);
```

## Key Metrics

### Traffic Metrics
- `pageViews` - Total page views
- `uniqueVisitors` - Unique visitors
- `bounceRate` - Bounce rate percentage
- `avgSessionDuration` - Average session duration

### Conversion Metrics
- `signupStarted` - Signup conversions
- `demoRequested` - Demo requests
- `pricingViewed` - Pricing page views
- `conversionRate` - Overall conversion rate

### Engagement Metrics
- `blogPostReads` - Blog post reads
- `avgTimeOnBlog` - Average time on blog
- `ctaClicks` - CTA button clicks
- `ctaClickRate` - CTA click rate

### Performance Metrics
- `avgPageLoadTime` - Average page load time
- `avgLCP` - Average Largest Contentful Paint
- `avgFID` - Average First Input Delay
- `avgCLS` - Average Cumulative Layout Shift

## Performance Thresholds

| Metric | Good | Poor |
|--------|------|------|
| LCP | ≤ 2.5s | > 4.0s |
| FID | ≤ 100ms | > 300ms |
| CLS | ≤ 0.1 | > 0.25 |
| FCP | ≤ 1.8s | > 3.0s |
| TTFB | ≤ 800ms | > 1800ms |

## Common Tasks

### Track Custom Event

```typescript
import { trackDashboardEvent } from '@/lib/monitoring';

trackDashboardEvent('custom_action', {
  action: 'button_click',
  label: 'hero_cta',
  value: 1,
});
```

### Monitor Component Performance

```typescript
import { usePerformanceMonitor } from '@/lib/web-vitals';

function MyComponent() {
  const monitor = usePerformanceMonitor('MyComponent');

  useEffect(() => {
    monitor?.markMountEnd();
  }, []);

  return <div>Content</div>;
}
```

### Check Monitoring Status

```typescript
import { getMonitoringStatus } from '@/lib/monitoring';

const status = getMonitoringStatus();
console.log('Error Monitoring:', status.errorMonitoring);
console.log('Performance Monitoring:', status.performanceMonitoring);
console.log('Analytics:', status.analytics);
```

## Troubleshooting

### Error Monitoring Not Working
1. Check `NEXT_PUBLIC_SENTRY_DSN` is set
2. Verify `initMonitoring()` is called
3. Check browser console for errors

### Performance Metrics Missing
1. Install: `npm install web-vitals`
2. Verify browser supports Performance API
3. Check `performanceEnabled: true`

### Rate Limiting Too Strict
Adjust in `lib/blog/rate-limit.ts`:
```typescript
maxRequests: 20, // Increase from 10
windowMs: 60000, // 1 minute
```

### Analytics Not Tracking
1. Verify `NEXT_PUBLIC_GA_MEASUREMENT_ID` is set
2. Check GA4 script is loaded
3. Disable ad blockers for testing

## Resources

- [Full Documentation](../lib/monitoring/README.md)
- [Google Analytics 4 Docs](https://developers.google.com/analytics/devguides/collection/ga4)
- [Web Vitals Guide](https://web.dev/vitals/)
- [Sentry Documentation](https://docs.sentry.io/)
