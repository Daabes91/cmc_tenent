# Google Analytics 4 Integration

This directory contains the complete Google Analytics 4 (GA4) integration for the SaaS landing page.

## Overview

The analytics system provides:
- **Automatic page view tracking** - Tracks all page navigations
- **Conversion event tracking** - Tracks key user actions (signups, demo requests, etc.)
- **Enhanced measurement** - Scroll depth, outbound clicks, video engagement
- **Custom event tracking** - Track any custom user interactions
- **Type-safe API** - Full TypeScript support

## Setup

### 1. Environment Configuration

Add your GA4 Measurement ID to your environment variables:

```bash
# .env.local
NEXT_PUBLIC_GA_MEASUREMENT_ID=G-XXXXXXXXXX
NEXT_PUBLIC_ENABLE_ANALYTICS=true  # Optional: force enable in development
```

### 2. Automatic Initialization

The `GoogleAnalytics` component is already included in the root layout (`app/layout.tsx`). It will:
- Load the GA4 script
- Initialize tracking
- Set up enhanced measurement
- Track the initial page view

## Usage

### Tracking Page Views

Page views are tracked automatically on route changes. For manual tracking:

```typescript
import { trackPageView } from '@/lib/analytics';

trackPageView({
  url: window.location.href,
  title: 'Custom Page Title',
  referrer: document.referrer,
});
```

### Tracking Conversion Events

```typescript
import { trackConversion } from '@/lib/analytics';

// Track signup started
trackConversion({
  event: 'signup_started',
  properties: {
    plan: 'pro',
    source: 'hero_cta',
  },
});

// Track demo requested
trackConversion({
  event: 'demo_requested',
  properties: {
    company_size: 'small',
  },
});

// Track pricing viewed
trackConversion({
  event: 'pricing_viewed',
  properties: {
    tier: 'enterprise',
  },
});
```

### Tracking CTA Clicks

```typescript
import { trackCTAClick } from '@/lib/analytics';

trackCTAClick(
  'Start Free Trial',      // CTA text
  'hero_section',          // Location on page
  '/signup'                // Destination
);
```

### Tracking Form Submissions

```typescript
import { trackFormSubmission } from '@/lib/analytics';

trackFormSubmission('contact_form', 'contact');
```

### Tracking Blog Reads

```typescript
import { trackBlogRead } from '@/lib/analytics';

trackBlogRead(
  'How to Reduce Patient No-Shows',  // Post title
  'reduce-patient-no-shows',         // Post slug
  120                                 // Time on page (seconds)
);
```

### Tracking Feature Interactions

```typescript
import { trackFeatureInteraction } from '@/lib/analytics';

trackFeatureInteraction('appointment_scheduler', 'click');
```

### Tracking Custom Events

```typescript
import { trackEvent } from '@/lib/analytics';

trackEvent({
  action: 'video_play',
  category: 'engagement',
  label: 'product_demo_video',
  value: 1,
});
```

### Tracking Search

```typescript
import { trackSearch } from '@/lib/analytics';

trackSearch('patient management', 5); // query, results count
```

## Component Integration Examples

### CTA Button with Tracking

```tsx
'use client';

import { trackCTAClick } from '@/lib/analytics';

export function CTAButton() {
  const handleClick = () => {
    trackCTAClick(
      'Get Started',
      'hero_section',
      '/signup'
    );
  };

  return (
    <button onClick={handleClick}>
      Get Started
    </button>
  );
}
```

### Form with Tracking

```tsx
'use client';

import { trackFormSubmission } from '@/lib/analytics';

export function ContactForm() {
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Submit form...
    
    // Track submission
    trackFormSubmission('contact_form', 'contact');
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* form fields */}
    </form>
  );
}
```

### Blog Post with Time Tracking

```tsx
'use client';

import { useEffect } from 'react';
import { trackBlogRead } from '@/lib/analytics';

export function BlogPost({ title, slug }: { title: string; slug: string }) {
  useEffect(() => {
    const startTime = Date.now();

    return () => {
      const timeOnPage = Math.floor((Date.now() - startTime) / 1000);
      trackBlogRead(title, slug, timeOnPage);
    };
  }, [title, slug]);

  return (
    <article>
      {/* blog content */}
    </article>
  );
}
```

## Enhanced Measurement

The following are tracked automatically:

- **Scroll Depth**: Tracks when users scroll to 25%, 50%, 75%, 90%, and 100% of the page
- **Outbound Clicks**: Tracks clicks on external links
- **Site Search**: Tracks search queries (if search is implemented)
- **Video Engagement**: Tracks video plays, pauses, and completions
- **File Downloads**: Tracks file download clicks

## Configuration

Edit `lib/analytics/config.ts` to customize:

```typescript
export const analyticsConfig = {
  measurementId: process.env.NEXT_PUBLIC_GA_MEASUREMENT_ID || '',
  enabled: process.env.NODE_ENV === 'production',
  debug: process.env.NODE_ENV === 'development',
  
  enhancedMeasurement: {
    scrolls: true,
    outboundClicks: true,
    siteSearch: true,
    videoEngagement: true,
    fileDownloads: true,
  },
  
  scrollDepthThresholds: [25, 50, 75, 90, 100],
  timeOnPageInterval: 30000, // 30 seconds
};
```

## Debugging

In development mode, analytics events are logged to the console:

```bash
# Enable debug mode
NODE_ENV=development npm run dev
```

You can also force enable analytics in development:

```bash
NEXT_PUBLIC_ENABLE_ANALYTICS=true npm run dev
```

## Testing

To verify analytics is working:

1. **Check Console**: In development, events are logged to console
2. **GA4 DebugView**: Use GA4's DebugView in the Google Analytics dashboard
3. **Browser DevTools**: Check Network tab for requests to `google-analytics.com`
4. **Real-time Reports**: View real-time reports in GA4 dashboard

## Type Safety

All analytics functions are fully typed:

```typescript
import type { ConversionEvent, AnalyticsEvent } from '@/lib/analytics';

const event: ConversionEvent = {
  event: 'signup_started', // Type-checked
  properties: {
    plan: 'pro',
  },
};
```

## Best Practices

1. **Track User Intent**: Focus on tracking actions that indicate user intent (CTA clicks, form submissions)
2. **Meaningful Labels**: Use descriptive labels for events
3. **Consistent Naming**: Use consistent naming conventions for events
4. **Privacy First**: Don't track PII (personally identifiable information)
5. **Test Thoroughly**: Test analytics in staging before production

## Conversion Events

The following conversion events are predefined:

- `signup_started` - User begins signup process
- `demo_requested` - User requests a demo
- `pricing_viewed` - User views pricing page
- `blog_read` - User reads a blog post
- `form_submitted` - User submits a form
- `feature_interaction` - User interacts with a feature

## Requirements Validation

This implementation satisfies:

- **Requirement 6.1**: GA4 integration with measurement ID ✓
- **Requirement 6.3**: Page view, session duration, and bounce rate tracking ✓
- **Requirement 6.4**: Enhanced measurement for scroll depth, outbound clicks, video engagement ✓

## Support

For issues or questions:
1. Check GA4 documentation: https://developers.google.com/analytics/devguides/collection/ga4
2. Verify measurement ID is correct
3. Check browser console for errors
4. Use GA4 DebugView for real-time debugging
