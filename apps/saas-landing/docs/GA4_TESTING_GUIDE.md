# Google Analytics 4 - Testing Guide

## Prerequisites

1. **Set up GA4 Measurement ID**:
   ```bash
   # In apps/saas-landing/.env.local
   NEXT_PUBLIC_GA_MEASUREMENT_ID=G-XXXXXXXXXX
   NEXT_PUBLIC_ENABLE_ANALYTICS=true
   ```

2. **Start the development server**:
   ```bash
   cd apps/saas-landing
   npm run dev
   ```

## Testing Checklist

### ✅ 1. Verify Script Loading

**Steps**:
1. Open the landing page in your browser
2. Open DevTools (F12) → Network tab
3. Filter by "gtag"
4. Refresh the page

**Expected Result**:
- ✓ Request to `googletagmanager.com/gtag/js?id=G-XXXXXXXXXX`
- ✓ Status: 200 OK
- ✓ Script loads successfully

**Console Output**:
```
Google Analytics 4 initialized: G-XXXXXXXXXX
Analytics tracking initialized
```

### ✅ 2. Test Page View Tracking

**Steps**:
1. Open browser console
2. Navigate to the landing page
3. Check console for page view event

**Expected Console Output**:
```javascript
// In development mode, you should see:
{
  event: 'page_view',
  page_title: 'SaasPro - Modern SaaS Platform',
  page_location: 'http://localhost:3000/',
  page_referrer: ''
}
```

**Verify in GA4**:
1. Go to GA4 Dashboard → Reports → Realtime
2. You should see 1 active user
3. Page views should increment

### ✅ 3. Test Scroll Depth Tracking

**Steps**:
1. Open the landing page
2. Slowly scroll down the page
3. Watch the console for scroll events

**Expected Console Output**:
```javascript
// As you scroll, you should see:
{ event: 'scroll', event_category: 'engagement', event_label: '25% - /', value: 25 }
{ event: 'scroll', event_category: 'engagement', event_label: '50% - /', value: 50 }
{ event: 'scroll', event_category: 'engagement', event_label: '75% - /', value: 75 }
{ event: 'scroll', event_category: 'engagement', event_label: '100% - /', value: 100 }
```

### ✅ 4. Test CTA Click Tracking

**Steps**:
1. Add tracking to a CTA button (example below)
2. Click the button
3. Check console for conversion event

**Example Implementation**:
```tsx
// In any component
'use client';
import { trackCTAClick } from '@/lib/analytics';

export function HeroCTA() {
  return (
    <button 
      onClick={() => trackCTAClick('Get Started', 'hero_section', '/signup')}
      className="btn-primary"
    >
      Get Started
    </button>
  );
}
```

**Expected Console Output**:
```javascript
{
  event: 'signup_started',
  cta_text: 'Get Started',
  cta_location: 'hero_section',
  cta_destination: '/signup',
  timestamp: '2024-01-15T10:30:00.000Z'
}
```

### ✅ 5. Test Form Submission Tracking

**Steps**:
1. Add tracking to a form (example below)
2. Submit the form
3. Check console for form submission event

**Example Implementation**:
```tsx
'use client';
import { trackFormSubmission } from '@/lib/analytics';

export function ContactForm() {
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // ... form logic
    trackFormSubmission('contact_form', 'contact');
  };

  return <form onSubmit={handleSubmit}>...</form>;
}
```

**Expected Console Output**:
```javascript
{
  event: 'form_submitted',
  form_name: 'contact_form',
  form_type: 'contact',
  timestamp: '2024-01-15T10:30:00.000Z'
}
```

### ✅ 6. Test Time on Page Tracking

**Steps**:
1. Stay on the page for at least 30 seconds
2. Watch console for time tracking events

**Expected Console Output** (every 30 seconds):
```javascript
{
  event: 'time_on_page',
  event_category: 'engagement',
  event_label: '/',
  value: 30
}
// Then at 60 seconds:
{
  event: 'time_on_page',
  event_category: 'engagement',
  event_label: '/',
  value: 60
}
```

### ✅ 7. Test Route Change Tracking

**Steps**:
1. Navigate to different pages (e.g., /blog, /pricing)
2. Check console for page view events on each navigation

**Expected Console Output**:
```javascript
// On navigation to /blog
{
  event: 'page_view',
  page_title: 'Blog',
  page_location: 'http://localhost:3000/blog',
  page_referrer: 'http://localhost:3000/'
}
```

### ✅ 8. Test Custom Event Tracking

**Steps**:
1. Add custom event tracking (example below)
2. Trigger the event
3. Check console

**Example Implementation**:
```tsx
'use client';
import { trackEvent } from '@/lib/analytics';

export function VideoPlayer() {
  const handlePlay = () => {
    trackEvent({
      action: 'video_play',
      category: 'engagement',
      label: 'product_demo',
      value: 1,
    });
  };

  return <button onClick={handlePlay}>Play Video</button>;
}
```

**Expected Console Output**:
```javascript
{
  event: 'video_play',
  event_category: 'engagement',
  event_label: 'product_demo',
  value: 1
}
```

## GA4 Dashboard Verification

### Real-time Reports

1. **Navigate to GA4 Dashboard**:
   - Go to https://analytics.google.com
   - Select your property
   - Go to Reports → Realtime

2. **Verify Active Users**:
   - You should see yourself as an active user
   - Location should show your city/country

3. **Verify Events**:
   - Click on "Event count by Event name"
   - You should see:
     - `page_view`
     - `scroll`
     - `signup_started` (if you clicked a CTA)
     - `form_submitted` (if you submitted a form)
     - Custom events you triggered

### DebugView (Recommended)

1. **Enable Debug Mode**:
   ```bash
   # Already enabled in development with NEXT_PUBLIC_ENABLE_ANALYTICS=true
   ```

2. **Navigate to DebugView**:
   - GA4 Dashboard → Configure → DebugView
   - You should see events in real-time with full details

3. **Inspect Events**:
   - Click on any event to see all parameters
   - Verify event names and parameters are correct

### Event Reports (After 24-48 hours)

1. **Navigate to Events Report**:
   - GA4 Dashboard → Reports → Engagement → Events

2. **Verify Event Counts**:
   - Check total event counts
   - Verify conversion events are tracked
   - Check event parameters

## Common Issues & Solutions

### Issue: Script Not Loading

**Symptoms**: No GA4 script in Network tab

**Solutions**:
1. Verify `NEXT_PUBLIC_GA_MEASUREMENT_ID` is set
2. Check measurement ID format (should be `G-XXXXXXXXXX`)
3. Restart development server after adding env var
4. Check browser console for errors

### Issue: Events Not Showing in Console

**Symptoms**: No console logs for events

**Solutions**:
1. Verify `NEXT_PUBLIC_ENABLE_ANALYTICS=true` is set
2. Check that you're in development mode
3. Open browser console (F12)
4. Check for JavaScript errors

### Issue: Events Not in GA4 Dashboard

**Symptoms**: Events in console but not in GA4

**Solutions**:
1. Wait 1-2 minutes for real-time data to appear
2. Verify measurement ID matches your GA4 property
3. Check DebugView instead of real-time reports
4. Verify GA4 property is not filtered

### Issue: Duplicate Events

**Symptoms**: Same event tracked multiple times

**Solutions**:
1. Check for multiple `GoogleAnalytics` components
2. Verify event handlers aren't called multiple times
3. Check for React strict mode double-rendering

## Browser DevTools Inspection

### Check dataLayer

Open console and run:
```javascript
window.dataLayer
```

**Expected Output**:
```javascript
[
  ['js', Date],
  ['config', 'G-XXXXXXXXXX', {...}],
  ['event', 'page_view', {...}],
  ['event', 'scroll', {...}],
  // ... more events
]
```

### Check gtag Function

Open console and run:
```javascript
typeof window.gtag
```

**Expected Output**:
```javascript
'function'
```

### Manual Event Test

Open console and run:
```javascript
window.gtag('event', 'test_event', {
  event_category: 'test',
  event_label: 'manual_test',
  value: 1
});
```

Check GA4 DebugView for the `test_event`.

## Production Testing

### Before Deployment

1. ✓ Verify all tracking code is in place
2. ✓ Test in staging environment
3. ✓ Verify measurement ID is correct for production
4. ✓ Test all conversion events
5. ✓ Verify no console errors

### After Deployment

1. ✓ Check real-time reports in GA4
2. ✓ Verify page views are tracked
3. ✓ Test conversion events in production
4. ✓ Monitor for 24-48 hours
5. ✓ Set up custom reports and dashboards

## Performance Testing

### Check Script Load Time

1. Open DevTools → Network tab
2. Reload page
3. Find `gtag/js` request
4. Check load time (should be < 500ms)

### Check Impact on Page Load

1. Open DevTools → Lighthouse
2. Run performance audit
3. Verify analytics doesn't significantly impact score
4. Target: < 100ms impact on First Contentful Paint

## Automated Testing

### Unit Tests (Future)

```typescript
// Example test structure
describe('Analytics Tracking', () => {
  it('should track page views', () => {
    // Mock window.gtag
    // Call trackPageView
    // Verify gtag was called with correct params
  });

  it('should track CTA clicks', () => {
    // Mock window.gtag
    // Call trackCTAClick
    // Verify conversion event was sent
  });
});
```

## Checklist Summary

- [ ] GA4 script loads successfully
- [ ] Page views tracked on navigation
- [ ] Scroll depth tracked at thresholds
- [ ] CTA clicks tracked as conversions
- [ ] Form submissions tracked
- [ ] Time on page tracked every 30s
- [ ] Custom events tracked correctly
- [ ] Events appear in GA4 real-time reports
- [ ] Events appear in GA4 DebugView
- [ ] No console errors
- [ ] No performance impact

## Next Steps

After verifying the implementation:

1. **Configure GA4 Conversions**:
   - Mark important events as conversions
   - Set up conversion goals

2. **Create Custom Reports**:
   - User journey reports
   - Conversion funnel reports
   - Content engagement reports

3. **Set Up Alerts**:
   - Traffic drops
   - Conversion rate changes
   - Error rate increases

4. **Implement Task 14**:
   - Add tracking to all CTAs
   - Add tracking to all forms
   - Add tracking to blog posts

## Resources

- [GA4 Documentation](https://developers.google.com/analytics/devguides/collection/ga4)
- [GA4 DebugView Guide](https://support.google.com/analytics/answer/7201382)
- [GA4 Real-time Reports](https://support.google.com/analytics/answer/9271392)
- [Analytics Implementation Guide](./ANALYTICS_QUICK_REFERENCE.md)
