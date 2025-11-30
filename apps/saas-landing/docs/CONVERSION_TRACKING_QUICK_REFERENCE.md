# Conversion Tracking Quick Reference

## Overview

Quick reference for conversion event tracking implementation on the SaaS landing page.

## Tracked Events

### 1. CTA Button Clicks

**Event Names**: `signup_started`, `demo_requested`, `pricing_viewed`

**Locations**:
- Hero section primary CTA → `signup_started`
- Hero section secondary CTA → `demo_requested`
- Hero section video CTA → custom event
- Pricing plan CTAs → `pricing_viewed`

**Properties**:
- `cta_text`: Button text
- `cta_location`: Section location (e.g., 'hero_section', 'pricing_BASIC')
- `cta_destination`: Target URL or action

### 2. Form Submissions

**Event Name**: `form_submitted`

**Trigger**: Successful signup form submission

**Properties**:
- `form_name`: 'signup_form'
- `form_type`: 'signup'

### 3. Blog Post Reads

**Event Name**: `blog_read`

**Trigger**: 
- Initial page view
- User leaves page (includes time_on_page)

**Properties**:
- `post_title`: Blog post title
- `post_slug`: URL slug
- `time_on_page`: Seconds spent (optional, sent on exit)

### 4. Scroll Depth

**Event Name**: `scroll`

**Trigger**: User scrolls to 25%, 50%, 75%, or 100% of page

**Properties**:
- `event_category`: 'engagement'
- `event_label`: '{depth}% - {page_path}'
- `value`: Percentage (25, 50, 75, 100)
- `page_path`: Current page path

### 5. Feature Interactions

**Event Name**: `feature_interaction`

**Trigger**: User clicks on feature card

**Properties**:
- `feature_name`: Name of the feature
- `interaction_type`: 'click'

## Usage Examples

### Track CTA Click
```typescript
import { useAnalytics } from '@/hooks/use-analytics';

const { trackCTA } = useAnalytics();

trackCTA('Get Started', 'hero_section', '/signup');
```

### Track Form Submission
```typescript
import { trackFormSubmission } from '@/lib/analytics';

trackFormSubmission('signup_form', 'signup');
```

### Track Blog Read
```typescript
import { trackBlogRead } from '@/lib/analytics';

trackBlogRead('Post Title', 'post-slug', 120); // 120 seconds
```

### Track Feature Interaction
```typescript
import { useAnalytics } from '@/hooks/use-analytics';

const { trackFeature } = useAnalytics();

trackFeature('Smart Appointment Scheduling', 'click');
```

## Testing in GA4

### Enable Debug Mode
Add `?debug_mode=true` to any URL to see events in GA4 DebugView.

### Verify Events
1. Open Google Analytics
2. Navigate to Configure → DebugView
3. Perform actions on the site
4. Verify events appear in real-time

### Check Event Properties
Click on any event in DebugView to see all properties and values.

## Common Issues

### Events Not Appearing
- Check if GA4 measurement ID is configured
- Verify `NEXT_PUBLIC_GA_MEASUREMENT_ID` environment variable
- Check browser console for errors
- Ensure ad blockers are disabled during testing

### Missing Properties
- Verify property names match expected format
- Check that values are being passed correctly
- Review event structure in DebugView

### Duplicate Events
- Check for multiple tracking calls
- Verify component mounting/unmounting behavior
- Review useEffect dependencies

## Performance Notes

- Scroll events are throttled using `requestAnimationFrame`
- Event listeners use `{ passive: true }` for better performance
- Tracking calls fail silently to not disrupt user experience
- All tracking is client-side only (no SSR impact)

## Related Files

- `lib/analytics/tracking.ts` - Core tracking service
- `hooks/use-analytics.tsx` - React hook for tracking
- `components/blog/BlogPostTracking.tsx` - Blog tracking component
- `components/GoogleAnalytics.tsx` - GA4 initialization

## Conversion Goals Setup

To set up conversion goals in GA4:

1. Go to Admin → Events
2. Mark events as conversions:
   - `signup_started`
   - `demo_requested`
   - `form_submitted`
   - `blog_read`
3. Create custom audiences based on these events
4. Set up conversion funnels in Explorations

## Monitoring

### Key Metrics to Track
- CTA click-through rates by location
- Form submission success rate
- Blog engagement (time on page, scroll depth)
- Feature interaction rates
- Conversion funnel drop-off points

### Recommended Reports
- Conversion events report (Events → Conversions)
- User engagement report (Engagement → Overview)
- Custom funnel analysis (Explore → Funnel exploration)
- Event parameter report (Events → Event name → View details)

## Support

For issues or questions:
- Review [Task 14 Implementation](./TASK_14_CONVERSION_TRACKING_IMPLEMENTATION.md)
- Check [Analytics Quick Reference](./ANALYTICS_QUICK_REFERENCE.md)
- See [GA4 Testing Guide](./GA4_TESTING_GUIDE.md)
