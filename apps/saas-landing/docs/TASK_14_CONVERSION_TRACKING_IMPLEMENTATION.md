# Task 14: Conversion Event Tracking Implementation

## Overview

This document describes the implementation of conversion event tracking for the SaaS landing page. The tracking system monitors user interactions and key conversion events to measure marketing effectiveness.

## Implementation Summary

### 1. CTA Button Tracking

**Location**: Hero Section (`components/hero.tsx`)

Implemented tracking for all CTA buttons in the hero section:

- **Primary CTA** ("Get Your Clinic Portal"): Tracks `signup_started` event
- **Secondary CTA** ("Book a Demo"): Tracks `demo_requested` event  
- **Video CTA** ("Watch video tour"): Tracks custom event for video engagement

**Implementation**:
```typescript
import { useAnalytics } from '@/hooks/use-analytics';

const { trackCTA } = useAnalytics();

// Primary CTA
onClick={() => trackCTA(copy.ctaPrimary, 'hero_section', '/signup')}

// Secondary CTA
onClick={() => trackCTA(copy.ctaSecondary, 'hero_section', `mailto:${SALES_EMAIL}`)}

// Video CTA
onClick={() => {
  setShowVideo(true);
  trackCTA(videoLabel, 'hero_section', 'video_tour');
}}
```

**Location**: Pricing Section (`components/pricing.tsx`)

Implemented tracking for pricing plan CTAs:

- Tracks `pricing_viewed` event when users click on plan CTAs
- Includes plan tier information in tracking data

**Implementation**:
```typescript
onClick={() => trackCTA(plan.cta, `pricing_${plan.tier}`, plan.ctaHref)}
```

### 2. Form Submission Tracking

**Location**: Signup Form (`components/SignupForm.tsx`)

Implemented tracking for successful form submissions:

- Tracks `form_submitted` event with form name and type
- Only tracks successful submissions (after API response confirms success)
- Includes form metadata (form_name: 'signup_form', form_type: 'signup')

**Implementation**:
```typescript
import { trackFormSubmission } from '@/lib/analytics';

if (response.ok && data.success) {
  // Track successful form submission
  trackFormSubmission('signup_form', 'signup');
  
  // Redirect to payment or admin
  if (data.approvalUrl) {
    window.location.href = data.approvalUrl;
  }
}
```

### 3. Blog Post Read Tracking

**Location**: Blog Post Tracking Component (`components/blog/BlogPostTracking.tsx`)

Created a dedicated tracking component for blog engagement:

**Features**:
- Tracks initial blog post view with `blog_read` event
- Monitors scroll depth at 25%, 50%, 75%, and 100% thresholds
- Tracks time spent on page (sent when user leaves)
- Throttles scroll events for performance

**Implementation**:
```typescript
export function BlogPostTracking({ postTitle, postSlug }: BlogPostTrackingProps) {
  const startTime = useRef(Date.now());
  const tracked = useRef(new Set<number>());

  useEffect(() => {
    // Track initial blog read
    trackBlogRead(postTitle, postSlug);

    // Set up scroll depth tracking
    const handleScroll = () => {
      const scrollPercentage = Math.round((scrolled / scrollHeight) * 100);
      thresholds.forEach((threshold) => {
        if (scrollPercentage >= threshold && !tracked.current.has(threshold)) {
          tracked.current.add(threshold);
          trackScrollDepth({ depth: threshold, page: `/blog/${postSlug}` });
        }
      });
    };

    // Track time on page when leaving
    const handleBeforeUnload = () => {
      const timeOnPage = Math.floor((Date.now() - startTime.current) / 1000);
      trackBlogRead(postTitle, postSlug, timeOnPage);
    };

    window.addEventListener('scroll', throttledScroll, { passive: true });
    window.addEventListener('beforeunload', handleBeforeUnload);

    return () => {
      window.removeEventListener('scroll', throttledScroll);
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [postTitle, postSlug]);

  return null;
}
```

**Integration**: Added to blog post page (`app/blog/[slug]/page.tsx`):
```typescript
import { BlogPostTracking } from '@/components/blog/BlogPostTracking';

return (
  <>
    <BlogPostTracking postTitle={post.title} postSlug={slug} />
    <BlogPost post={post} siteUrl={SITE_URL} />
    {/* ... */}
  </>
);
```

### 4. Feature Interaction Tracking

**Location**: Features Section (`components/features.tsx`)

Implemented tracking for feature card interactions:

- Tracks `feature_interaction` event when users click on feature cards
- Includes feature name and interaction type
- Made feature cards interactive with proper accessibility (keyboard support)

**Implementation**:
```typescript
const { trackFeature } = useAnalytics();

const handleFeatureClick = (featureName: string) => {
  trackFeature(featureName, 'click');
};

<div
  onClick={() => handleFeatureClick(feature.title)}
  role="button"
  tabIndex={0}
  onKeyDown={(e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      handleFeatureClick(feature.title);
    }
  }}
>
  {/* Feature content */}
</div>
```

## Events Tracked

### Conversion Events

1. **signup_started**
   - Triggered when: User clicks primary CTA in hero section
   - Properties: cta_text, cta_location, cta_destination

2. **demo_requested**
   - Triggered when: User clicks demo/contact CTA
   - Properties: cta_text, cta_location, cta_destination

3. **pricing_viewed**
   - Triggered when: User clicks pricing plan CTA
   - Properties: cta_text, cta_location (includes plan tier), cta_destination

4. **form_submitted**
   - Triggered when: User successfully submits signup form
   - Properties: form_name, form_type

5. **blog_read**
   - Triggered when: User views a blog post
   - Properties: post_title, post_slug, time_on_page (optional)

### Engagement Events

1. **scroll_depth**
   - Triggered when: User scrolls to 25%, 50%, 75%, or 100% of page
   - Properties: depth (percentage), page (path)

2. **feature_interaction**
   - Triggered when: User clicks on a feature card
   - Properties: feature_name, interaction_type

## Analytics Service Methods Used

The implementation leverages the following methods from `AnalyticsService`:

- `trackCTAClick(ctaText, ctaLocation, ctaDestination)` - Tracks CTA button clicks
- `trackFormSubmission(formName, formType)` - Tracks form submissions
- `trackBlogRead(postTitle, postSlug, timeOnPage?)` - Tracks blog post reads
- `trackScrollDepth({ depth, page })` - Tracks scroll depth
- `trackFeatureInteraction(featureName, interactionType)` - Tracks feature interactions

## Testing

### Manual Testing Checklist

1. **CTA Tracking**
   - [ ] Click "Get Your Clinic Portal" button in hero
   - [ ] Click "Book a Demo" button in hero
   - [ ] Click "Watch video tour" button (if video URL configured)
   - [ ] Click pricing plan CTAs
   - [ ] Verify events appear in GA4 DebugView

2. **Form Submission Tracking**
   - [ ] Fill out signup form completely
   - [ ] Submit form successfully
   - [ ] Verify `form_submitted` event in GA4

3. **Blog Post Tracking**
   - [ ] Navigate to a blog post
   - [ ] Verify initial `blog_read` event
   - [ ] Scroll to 25%, 50%, 75%, 100%
   - [ ] Verify scroll depth events
   - [ ] Leave page and verify time_on_page is tracked

4. **Feature Interaction Tracking**
   - [ ] Click on feature cards
   - [ ] Verify `feature_interaction` events
   - [ ] Test keyboard navigation (Tab + Enter)

### GA4 DebugView Testing

To test in GA4 DebugView:

1. Enable debug mode by adding `?debug_mode=true` to URL
2. Open GA4 DebugView in Google Analytics
3. Perform actions listed in manual testing checklist
4. Verify events appear in real-time with correct properties

### Event Verification

Expected event structure in GA4:

```javascript
// CTA Click Event
{
  event: 'signup_started', // or 'demo_requested', 'pricing_viewed'
  cta_text: 'Get Your Clinic Portal',
  cta_location: 'hero_section',
  cta_destination: '/signup',
  timestamp: '2024-01-15T10:30:00Z'
}

// Form Submission Event
{
  event: 'form_submitted',
  form_name: 'signup_form',
  form_type: 'signup'
}

// Blog Read Event
{
  event: 'blog_read',
  post_title: '5 Ways to Reduce Patient No-Shows',
  post_slug: '5-ways-to-reduce-patient-no-shows',
  time_on_page: 120 // seconds
}

// Scroll Depth Event
{
  event: 'scroll',
  event_category: 'engagement',
  event_label: '50% - /blog/post-slug',
  value: 50,
  page_path: '/blog/post-slug'
}

// Feature Interaction Event
{
  event: 'feature_interaction',
  feature_name: 'Smart Appointment Scheduling',
  interaction_type: 'click'
}
```

## Performance Considerations

1. **Scroll Event Throttling**: Scroll events are throttled using `requestAnimationFrame` to prevent performance issues
2. **Passive Event Listeners**: Scroll listeners use `{ passive: true }` for better scrolling performance
3. **Debouncing**: Form validation and subdomain checks are debounced to reduce API calls
4. **Lazy Loading**: Analytics tracking components are client-side only and don't block initial page load

## Requirements Validation

This implementation satisfies the following requirements:

- **Requirement 6.2**: ✅ Tracks CTA button clicks (signup_started, demo_requested, pricing_viewed)
- **Requirement 6.5**: ✅ Tracks custom events for key user actions (form submissions, blog reads, feature interactions)

### Specific Task Requirements

- ✅ Track CTA button clicks (signup_started, demo_requested, pricing_viewed)
- ✅ Track form submissions
- ✅ Track blog post reads (time on page, scroll depth)
- ✅ Track feature interactions

## Files Modified

1. `components/hero.tsx` - Added CTA tracking to hero buttons
2. `components/pricing.tsx` - Added CTA tracking to pricing plan buttons
3. `components/features.tsx` - Added feature interaction tracking
4. `components/SignupForm.tsx` - Added form submission tracking
5. `components/blog/BlogPostTracking.tsx` - Created new tracking component
6. `app/blog/[slug]/page.tsx` - Integrated blog post tracking
7. `lib/content/examples.ts` - Fixed React Hook naming convention

## Next Steps

1. Monitor conversion events in GA4 dashboard
2. Set up conversion goals in GA4 for key events
3. Create custom reports for conversion funnel analysis
4. Set up alerts for significant drops in conversion rates
5. A/B test different CTA copy based on tracking data

## Related Documentation

- [Analytics Quick Reference](./ANALYTICS_QUICK_REFERENCE.md)
- [GA4 Testing Guide](./GA4_TESTING_GUIDE.md)
- [Task 13: GA4 Implementation](./TASK_13_GA4_IMPLEMENTATION_SUMMARY.md)
