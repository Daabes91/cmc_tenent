# Google Analytics 4 - Quick Reference

## Setup

1. **Add Measurement ID to environment**:
   ```bash
   NEXT_PUBLIC_GA_MEASUREMENT_ID=G-XXXXXXXXXX
   ```

2. **Analytics loads automatically** via `GoogleAnalytics` component in root layout

## Common Tracking Patterns

### Track CTA Click
```tsx
import { trackCTAClick } from '@/lib/analytics';

<button onClick={() => trackCTAClick('Get Started', 'hero', '/signup')}>
  Get Started
</button>
```

### Track Form Submission
```tsx
import { trackFormSubmission } from '@/lib/analytics';

const handleSubmit = () => {
  // ... submit logic
  trackFormSubmission('contact_form', 'contact');
};
```

### Track Conversion Event
```tsx
import { trackConversion } from '@/lib/analytics';

trackConversion({
  event: 'signup_started',
  properties: { plan: 'pro' }
});
```

### Using the Hook
```tsx
'use client';
import { useAnalytics } from '@/hooks/use-analytics';

export function MyComponent() {
  const { trackCTA, trackForm } = useAnalytics();
  
  return (
    <button onClick={() => trackCTA('Demo', 'pricing', '/demo')}>
      Request Demo
    </button>
  );
}
```

## Conversion Events

- `signup_started` - User begins signup
- `demo_requested` - User requests demo
- `pricing_viewed` - User views pricing
- `blog_read` - User reads blog post
- `form_submitted` - User submits form
- `feature_interaction` - User interacts with feature

## Automatic Tracking

✓ Page views on route changes
✓ Scroll depth (25%, 50%, 75%, 90%, 100%)
✓ Time on page (every 30 seconds)
✓ Outbound link clicks
✓ Video engagement
✓ File downloads

## Testing

**Development Mode**: Events logged to console
**Production**: View in GA4 dashboard

**Enable in Development**:
```bash
NEXT_PUBLIC_ENABLE_ANALYTICS=true npm run dev
```

## Files

- `lib/analytics/tracking.ts` - Main analytics service
- `lib/analytics/config.ts` - Configuration
- `lib/analytics/types.ts` - TypeScript types
- `components/GoogleAnalytics.tsx` - GA4 component
- `hooks/use-analytics.tsx` - React hooks

## Requirements Satisfied

✓ **6.1**: GA4 integration with measurement ID
✓ **6.3**: Page view, session duration, bounce rate tracking  
✓ **6.4**: Enhanced measurement (scroll, outbound clicks, video)
