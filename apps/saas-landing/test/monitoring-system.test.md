# Monitoring System - Manual Test Guide

## Overview

This guide helps you verify that the monitoring and analytics system is working correctly.

## Prerequisites

- Development server running (`npm run dev`)
- Browser with developer tools
- Google Analytics configured (optional for full testing)

## Test 1: Error Monitoring

### Setup
1. Open browser console
2. Navigate to any page

### Test Steps

**1.1 Test Exception Capture**
```javascript
// In browser console
import { ErrorMonitoring } from '@/lib/monitoring';
ErrorMonitoring.captureException(new Error('Test error'), {
  level: 'error',
  tags: { test: 'manual' }
});
```

**Expected Result:**
- Console shows error log with timestamp
- If GA4 configured, event sent to analytics

**1.2 Test Global Error Handler**
```javascript
// In browser console
throw new Error('Unhandled test error');
```

**Expected Result:**
- Error is caught by global handler
- Console shows error monitoring log

**1.3 Test Promise Rejection**
```javascript
// In browser console
Promise.reject('Test rejection');
```

**Expected Result:**
- Rejection is caught
- Console shows error monitoring log

### Verification
- [ ] Exceptions are captured
- [ ] Global errors are caught
- [ ] Promise rejections are handled
- [ ] Console shows proper logs

---

## Test 2: Performance Monitoring

### Setup
1. Open browser console
2. Navigate to homepage
3. Wait for page to fully load

### Test Steps

**2.1 Check Core Web Vitals**
```javascript
// In browser console
import { getCurrentPerformanceMetrics } from '@/lib/monitoring';
const metrics = getCurrentPerformanceMetrics();
console.log('Performance Metrics:', metrics);
```

**Expected Result:**
```javascript
{
  lcp: 2345,  // Largest Contentful Paint
  fid: 45,    // First Input Delay
  cls: 0.05,  // Cumulative Layout Shift
  fcp: 1234,  // First Contentful Paint
  ttfb: 567,  // Time to First Byte
  url: '/',
  timestamp: 1234567890
}
```

**2.2 Check Performance Summary**
```javascript
// In browser console
import { getPerformanceSummary } from '@/lib/monitoring';
const summary = getPerformanceSummary();
console.log('Performance Summary:', summary);
```

**Expected Result:**
```javascript
{
  totalPages: 3,
  averageLCP: 2500,
  averageFID: 50,
  averageCLS: 0.08,
  averagePageLoadTime: 3000
}
```

**2.3 Export Metrics**
```javascript
// In browser console
import { exportPerformanceMetrics } from '@/lib/monitoring';
const json = exportPerformanceMetrics();
console.log(json);
```

**Expected Result:**
- JSON string with all metrics
- Includes all visited pages

### Verification
- [ ] LCP is tracked
- [ ] FID is tracked
- [ ] CLS is tracked
- [ ] FCP is tracked
- [ ] TTFB is tracked
- [ ] Metrics are stored per page
- [ ] Summary calculates averages
- [ ] Export works

---

## Test 3: Analytics Dashboard

### Setup
1. Open browser console
2. Navigate through several pages
3. Click some CTAs

### Test Steps

**3.1 Track Custom Event**
```javascript
// In browser console
import { trackDashboardEvent } from '@/lib/monitoring';
trackDashboardEvent('test_event', {
  action: 'button_click',
  label: 'test_button',
  value: 1
});
```

**Expected Result:**
- Event is tracked
- Console shows confirmation (in dev mode)

**3.2 Get Dashboard Metrics**
```javascript
// In browser console
import { getDashboardMetrics } from '@/lib/monitoring';
const metrics = getDashboardMetrics();
console.log('Dashboard Metrics:', metrics);
```

**Expected Result:**
```javascript
{
  pageViews: 5,
  uniqueVisitors: 1,
  signupStarted: 0,
  demoRequested: 0,
  pricingViewed: 1,
  conversionRate: 20,
  blogPostReads: 2,
  ctaClicks: 3,
  ctaClickRate: 60,
  trafficSources: {
    direct: 5,
    organic: 0,
    social: 0,
    referral: 0,
    paid: 0
  },
  topPages: [
    { url: '/', views: 3, avgTime: 0 },
    { url: '/blog', views: 2, avgTime: 0 }
  ]
}
```

**3.3 Get Real-Time Metrics**
```javascript
// In browser console
import { getRealTimeMetrics } from '@/lib/monitoring';
const realTime = getRealTimeMetrics();
console.log('Real-Time Metrics:', realTime);
```

**Expected Result:**
```javascript
{
  activeUsers: 1,
  recentEvents: [...], // Last 20 events
  topPages: ['/', '/blog']
}
```

**3.4 Export as CSV**
```javascript
// In browser console
import { getDashboardMetrics, exportMetricsAsCSV } from '@/lib/monitoring';
const metrics = getDashboardMetrics();
const csv = exportMetricsAsCSV(metrics);
console.log(csv);
```

**Expected Result:**
- CSV formatted string
- Contains all metrics

### Verification
- [ ] Events are tracked
- [ ] Page views are counted
- [ ] Conversions are tracked
- [ ] Traffic sources are analyzed
- [ ] Top pages are calculated
- [ ] Real-time metrics work
- [ ] CSV export works

---

## Test 4: Rate Limiting

### Setup
1. Open browser console
2. Navigate to blog page

### Test Steps

**4.1 Test Rate Limit Check**
```javascript
// In browser console
import { checkBlogSearchRateLimit, getRateLimitIdentifier } from '@/lib/blog/rate-limit';

const identifier = getRateLimitIdentifier();
console.log('Identifier:', identifier);

// Make 15 requests
for (let i = 0; i < 15; i++) {
  const result = checkBlogSearchRateLimit(identifier);
  console.log(`Request ${i + 1}:`, {
    success: result.success,
    remaining: result.remaining,
    message: result.message
  });
}
```

**Expected Result:**
- First 10 requests succeed
- Requests 11-15 fail with message
- Remaining count decreases
- Reset time is provided

**4.2 Test Rate Limit Reset**
```javascript
// In browser console
import { resetBlogSearchRateLimit, getRateLimitIdentifier } from '@/lib/blog/rate-limit';

const identifier = getRateLimitIdentifier();
resetBlogSearchRateLimit(identifier);

// Try again
const result = checkBlogSearchRateLimit(identifier);
console.log('After reset:', result);
```

**Expected Result:**
- Limit is reset
- New request succeeds

**4.3 Test Rate Limit Stats**
```javascript
// In browser console
import { getBlogSearchRateLimitStats } from '@/lib/blog/rate-limit';

const stats = getBlogSearchRateLimitStats();
console.log('Rate Limit Stats:', stats);
```

**Expected Result:**
```javascript
{
  totalIdentifiers: 1,
  totalRequests: 10
}
```

**4.4 Test in Blog Search Component**
1. Navigate to blog page
2. Use search bar
3. Search 15 times quickly

**Expected Result:**
- First 10 searches work
- After 10, error message appears
- Message says "Too many search requests..."

### Verification
- [ ] Rate limit enforces 10 requests/minute
- [ ] Identifier is generated correctly
- [ ] Reset works
- [ ] Stats are accurate
- [ ] UI shows error message
- [ ] Limit resets after 1 minute

---

## Test 5: Integration

### Setup
1. Open browser console
2. Navigate to homepage

### Test Steps

**5.1 Check Monitoring Status**
```javascript
// In browser console
import { getMonitoringStatus } from '@/lib/monitoring';

const status = getMonitoringStatus();
console.log('Monitoring Status:', status);
```

**Expected Result:**
```javascript
{
  errorMonitoring: true,
  performanceMonitoring: true,
  analytics: true
}
```

**5.2 Export All Data**
```javascript
// In browser console
import { exportMonitoringData } from '@/lib/monitoring';

const data = exportMonitoringData();
console.log('Performance Data:', data.performance);
console.log('Analytics Data:', data.analytics);
```

**Expected Result:**
- Performance data as JSON
- Analytics data as JSON
- Both contain valid data

**5.3 Test Full Flow**
1. Navigate to homepage
2. Click "Get Started" CTA
3. Navigate to blog
4. Search for "patient"
5. Click on a blog post
6. Navigate to pricing

```javascript
// In browser console
import { getDashboardMetrics } from '@/lib/monitoring';
const metrics = getDashboardMetrics();
console.log('Full Flow Metrics:', metrics);
```

**Expected Result:**
- Page views: 4+
- CTA clicks: 1+
- Blog reads: 1+
- Pricing viewed: 1+

### Verification
- [ ] All services are initialized
- [ ] Status check works
- [ ] Data export works
- [ ] Full user flow is tracked
- [ ] Metrics are accurate

---

## Test 6: Google Analytics Integration

### Setup
1. Ensure `NEXT_PUBLIC_GA_MEASUREMENT_ID` is set
2. Open browser console
3. Open Network tab

### Test Steps

**6.1 Check GA4 Script**
1. View page source
2. Look for Google Analytics script

**Expected Result:**
- Script tag with `googletagmanager.com/gtag/js`
- Measurement ID in script

**6.2 Check GA4 Events**
1. Open Network tab
2. Filter by "google-analytics.com"
3. Navigate through pages
4. Click CTAs

**Expected Result:**
- Network requests to GA4
- Events include: page_view, click, etc.

**6.3 Check GA4 DebugView**
1. Open GA4 dashboard
2. Go to DebugView
3. Perform actions on site

**Expected Result:**
- Events appear in real-time
- Page views tracked
- Conversions tracked

### Verification
- [ ] GA4 script loads
- [ ] Events are sent
- [ ] Page views tracked
- [ ] Conversions tracked
- [ ] Performance metrics sent

---

## Test 7: Error Scenarios

### Test Steps

**7.1 Test with Sentry DSN Missing**
1. Remove `NEXT_PUBLIC_SENTRY_DSN`
2. Reload page
3. Check console

**Expected Result:**
- Warning: "Error monitoring disabled"
- No errors thrown

**7.2 Test with GA4 Missing**
1. Remove `NEXT_PUBLIC_GA_MEASUREMENT_ID`
2. Reload page
3. Check console

**Expected Result:**
- Warning about analytics
- Monitoring still works

**7.3 Test Offline**
1. Disconnect internet
2. Perform actions
3. Check console

**Expected Result:**
- Errors are logged locally
- No crashes
- Graceful degradation

### Verification
- [ ] Works without Sentry
- [ ] Works without GA4
- [ ] Works offline
- [ ] No crashes

---

## Performance Benchmarks

### Expected Values

| Metric | Good | Acceptable | Poor |
|--------|------|------------|------|
| LCP | < 2.5s | 2.5s - 4.0s | > 4.0s |
| FID | < 100ms | 100ms - 300ms | > 300ms |
| CLS | < 0.1 | 0.1 - 0.25 | > 0.25 |
| FCP | < 1.8s | 1.8s - 3.0s | > 3.0s |
| TTFB | < 800ms | 800ms - 1800ms | > 1800ms |

### Verification
- [ ] LCP is in "Good" range
- [ ] FID is in "Good" range
- [ ] CLS is in "Good" range
- [ ] FCP is in "Good" range
- [ ] TTFB is in "Good" range

---

## Final Checklist

### Error Monitoring
- [ ] Exceptions are captured
- [ ] Global errors are caught
- [ ] Promise rejections handled
- [ ] User context works
- [ ] Tags work
- [ ] GA4 integration works

### Performance Monitoring
- [ ] Core Web Vitals tracked
- [ ] Custom metrics tracked
- [ ] Thresholds checked
- [ ] Slow resources detected
- [ ] Summary calculated
- [ ] Export works

### Analytics Dashboard
- [ ] Events tracked
- [ ] Page views counted
- [ ] Conversions tracked
- [ ] Traffic sources analyzed
- [ ] Top pages calculated
- [ ] Real-time metrics work
- [ ] CSV export works

### Rate Limiting
- [ ] Limits enforced
- [ ] Reset works
- [ ] Stats accurate
- [ ] UI shows errors
- [ ] API routes protected

### Integration
- [ ] All services initialized
- [ ] Status check works
- [ ] Data export works
- [ ] GA4 integration works
- [ ] Graceful degradation

---

## Troubleshooting

### Issue: Metrics not showing
**Solution:** Wait for page to fully load, then check console

### Issue: Rate limit not working
**Solution:** Check localStorage for sessionId, clear if needed

### Issue: GA4 events not sending
**Solution:** Check measurement ID, disable ad blockers

### Issue: Performance metrics missing
**Solution:** Install web-vitals: `npm install web-vitals`

---

## Success Criteria

All tests pass and:
- ✅ Error monitoring captures exceptions
- ✅ Performance metrics are tracked
- ✅ Analytics dashboard shows data
- ✅ Rate limiting works
- ✅ GA4 integration works
- ✅ No console errors
- ✅ Graceful degradation

## Notes

- Some metrics may take time to populate
- Real-time metrics update every 5 minutes
- Rate limits reset after 1 minute
- Performance metrics require page navigation

## Support

For issues:
1. Check browser console
2. Verify environment variables
3. Review documentation
4. Check GA4 DebugView
