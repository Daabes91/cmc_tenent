# CTA Buttons Quick Reference

## Overview
Quick reference guide for all CTA (Call-to-Action) buttons on the landing page.

## CTA Locations & Destinations

### 1. Hero Section
**Location:** Top of page  
**Primary CTA:** "Get Your Clinic Portal" → `/signup`  
**Secondary CTA:** "Book a Demo" → Email  
**Video CTA:** "Watch video tour" → Video modal (if configured)

### 2. Features Section
**Location:** After features grid  
**Primary CTA:** "Start Free Trial" → `/signup`  
**Secondary CTA:** "Schedule a Demo" → Email

### 3. Testimonials Section
**Location:** After testimonial cards  
**Primary CTA:** "Get Started Now" → `/signup`  
**Secondary CTA:** "Talk to Sales" → Email

### 4. Pricing Section
**Location:** After pricing table  
**Primary CTA:** "Contact Sales" → Email  
**Secondary CTA:** "View Documentation" → `/docs`

### 5. Integrations Section
**Location:** Bottom of integrations  
**CTA:** "View All Integrations" → API Docs

### 6. Final CTA Section
**Location:** Before footer  
**CTA:** "Book a strategy call" → Email

## Email Destinations

All email CTAs use: `mailto:sales@example.com`

**Subject Lines:**
- Hero Demo: "Schedule a Demo"
- Features Demo: "Schedule a Demo"
- Testimonials: "Clinic Management Platform Inquiry"
- Pricing: "Clinic Management Platform Inquiry"
- Final CTA: "Clinic Management Platform - Strategy Call"

## Component Files

- **Hero CTAs:** `components/hero.tsx`
- **Section CTAs:** `components/SectionCTA.tsx`
- **Integrations CTA:** `components/integrations.tsx`
- **Final CTA:** `components/cta.tsx`

## Analytics Tracking

All CTAs track with:
- Event: `cta_click`
- Category: Section name (e.g., `hero_section`, `features_section_cta`)
- Label: Button text
- Value: Destination URL

## Customization

### To Change CTA Text:
Edit `lib/content/healthcare-copy.ts` for hero CTAs  
Edit `components/SectionCTA.tsx` for section CTAs  
Edit `components/cta.tsx` for final CTA

### To Change Destinations:
Edit the `getPrimaryCTAHref()` and `getSecondaryCTAHref()` functions in `components/SectionCTA.tsx`

### To Add New CTA Section:
1. Import `SectionCTA` component
2. Add `<SectionCTA variant="your-variant" />` to your section
3. Add variant copy to `components/SectionCTA.tsx`

## Testing

Run manual tests: `test/cta-buttons.test.md`

**Quick Test:**
1. Click each CTA button
2. Verify correct navigation
3. Check analytics events fire
4. Test in Arabic language
5. Test on mobile device

## Troubleshooting

**CTA not appearing:**
- Check component import
- Verify variant prop is correct
- Check language context is available

**Wrong destination:**
- Check href logic in component
- Verify environment variables
- Check email address constant

**Analytics not tracking:**
- Verify `useAnalytics` hook is imported
- Check GA4 is configured
- Verify event handler is called

**Arabic not working:**
- Check language context
- Verify translations in copy object
- Check RTL styles are applied

## Best Practices

1. Always use healthcare-specific terminology
2. Include descriptive email subjects
3. Track all CTA clicks with analytics
4. Ensure mobile-friendly button sizes (min 44px)
5. Provide keyboard accessibility
6. Use ARIA labels for screen readers
7. Test in both languages
8. Verify email links work in production

## Support

For issues or questions:
- Review implementation summary: `docs/TASK_17_CTA_IMPLEMENTATION_SUMMARY.md`
- Check test guide: `test/cta-buttons.test.md`
- Review healthcare copy: `lib/content/healthcare-copy.ts`
