# CTA Buttons Test Guide

## Overview
This document provides manual testing steps to verify that all CTA (Call-to-Action) buttons throughout the landing page are using healthcare-specific text and navigating to the correct destinations.

## Test Scenarios

### 1. Hero Section CTAs

**Location:** Top of the landing page

**Expected CTAs:**
- Primary: "Get Your Clinic Portal" → `/signup`
- Secondary: "Book a Demo" → `mailto:sales@example.com`
- Video (if configured): "Watch video tour" → Opens video modal

**Test Steps:**
1. Navigate to the landing page
2. Verify the hero section displays healthcare-specific CTA text
3. Click "Get Your Clinic Portal" and verify it navigates to `/signup`
4. Click "Book a Demo" and verify it opens email client with correct subject
5. If video is configured, click "Watch video tour" and verify video modal opens

**Expected Results:**
- ✅ All buttons use healthcare-specific terminology
- ✅ Primary CTA navigates to signup page
- ✅ Secondary CTA opens email with demo request subject
- ✅ Analytics events are tracked for each click

---

### 2. Features Section CTA

**Location:** After the features grid

**Expected CTAs:**
- Primary: "Start Free Trial" → `/signup`
- Secondary: "Schedule a Demo" → `mailto:sales@example.com`

**Test Steps:**
1. Scroll to the features section
2. Verify CTA section appears after features grid
3. Verify title: "Ready to streamline your clinic operations?"
4. Click "Start Free Trial" and verify navigation to `/signup`
5. Click "Schedule a Demo" and verify email client opens

**Expected Results:**
- ✅ CTA section is visible after features
- ✅ Healthcare-specific messaging is displayed
- ✅ Both buttons navigate to correct destinations
- ✅ Analytics tracking fires on click

---

### 3. Testimonials Section CTA

**Location:** After the testimonials cards

**Expected CTAs:**
- Primary: "Get Started Now" → `/signup`
- Secondary: "Talk to Sales" → `mailto:sales@example.com`

**Test Steps:**
1. Scroll to the testimonials section
2. Verify CTA section appears after testimonial cards
3. Verify title: "Transform your clinic today"
4. Click "Get Started Now" and verify navigation to `/signup`
5. Click "Talk to Sales" and verify email client opens

**Expected Results:**
- ✅ CTA section is visible after testimonials
- ✅ Healthcare transformation messaging is displayed
- ✅ Both buttons navigate to correct destinations
- ✅ Analytics tracking fires on click

---

### 4. Pricing Section CTA

**Location:** After the pricing comparison table

**Expected CTAs:**
- Primary: "Contact Sales" → `mailto:sales@example.com`
- Secondary: "View Documentation" → `/docs`

**Test Steps:**
1. Scroll to the pricing section
2. Verify CTA section appears after comparison table
3. Verify title: "Still have questions?"
4. Click "Contact Sales" and verify email client opens
5. Click "View Documentation" and verify navigation to `/docs`

**Expected Results:**
- ✅ CTA section is visible after pricing table
- ✅ Help-oriented messaging is displayed
- ✅ Both buttons navigate to correct destinations
- ✅ Analytics tracking fires on click

---

### 5. Integrations Section CTA

**Location:** At the bottom of integrations section

**Expected CTA:**
- "View All Integrations" → API documentation URL

**Test Steps:**
1. Scroll to the integrations section
2. Verify healthcare-specific integration descriptions
3. Click "View All Integrations" button
4. Verify it opens API documentation in new tab

**Expected Results:**
- ✅ Integration descriptions mention clinic operations
- ✅ CTA button is visible and styled correctly
- ✅ Button navigates to API docs
- ✅ Analytics tracking fires on click

---

### 6. Final CTA Section

**Location:** Near the bottom of the page (before footer)

**Expected CTA:**
- "Book a strategy call" → `mailto:sales@example.com`

**Test Steps:**
1. Scroll to the final CTA section
2. Verify title: "Take your clinic to the next level"
3. Verify healthcare-specific bullet points are displayed
4. Click "Book a strategy call" button
5. Verify email client opens with correct subject

**Expected Results:**
- ✅ Healthcare-focused messaging throughout
- ✅ Bullet points mention clinic-specific features
- ✅ Button navigates to email with strategy call subject
- ✅ Analytics tracking fires on click

---

## Arabic Language Testing

Repeat all tests above with Arabic language selected:

**Test Steps:**
1. Switch language to Arabic using language selector
2. Verify all CTA text is translated to Arabic
3. Verify RTL (right-to-left) layout is applied
4. Verify arrow icons are rotated for RTL
5. Test all CTA button functionality

**Expected Results:**
- ✅ All CTA text is in Arabic
- ✅ Layout is RTL-compliant
- ✅ Icons are properly oriented
- ✅ All navigation still works correctly

---

## Analytics Verification

**Test Steps:**
1. Open browser developer tools
2. Navigate to Network tab
3. Filter for analytics requests (GA4)
4. Click each CTA button
5. Verify analytics events are sent

**Expected Event Properties:**
- Event name: `cta_click`
- Event category: Section name (e.g., `hero_section`, `features_section_cta`)
- Event label: Button text
- Event value: Destination URL

**Expected Results:**
- ✅ Each CTA click sends an analytics event
- ✅ Event properties are correctly populated
- ✅ Events appear in GA4 real-time reports

---

## Accessibility Testing

**Test Steps:**
1. Navigate page using keyboard only (Tab key)
2. Verify all CTA buttons are focusable
3. Verify focus indicators are visible
4. Press Enter/Space on focused buttons
5. Test with screen reader (VoiceOver/NVDA)

**Expected Results:**
- ✅ All CTAs are keyboard accessible
- ✅ Focus indicators are clearly visible
- ✅ Buttons activate with Enter/Space
- ✅ Screen readers announce button labels correctly
- ✅ ARIA labels provide context

---

## Mobile Responsiveness

**Test Steps:**
1. Open page on mobile device or use responsive mode
2. Verify all CTA sections are visible
3. Verify buttons are touch-friendly (min 44px height)
4. Test button clicks on mobile
5. Verify no layout issues

**Expected Results:**
- ✅ All CTA sections display correctly on mobile
- ✅ Buttons are large enough for touch interaction
- ✅ Text is readable without zooming
- ✅ No horizontal scrolling required
- ✅ Buttons stack vertically on small screens

---

## Test Checklist

- [ ] Hero section CTAs work correctly
- [ ] Features section CTA displays and functions
- [ ] Testimonials section CTA displays and functions
- [ ] Pricing section CTA displays and functions
- [ ] Integrations section CTA works correctly
- [ ] Final CTA section works correctly
- [ ] All CTAs use healthcare-specific text
- [ ] All CTAs navigate to correct destinations
- [ ] Analytics tracking works for all CTAs
- [ ] Arabic translations are correct
- [ ] RTL layout works properly
- [ ] Keyboard navigation works
- [ ] Screen reader accessibility verified
- [ ] Mobile responsiveness confirmed

---

## Known Issues

None at this time.

---

## Notes

- All CTA buttons should use healthcare-specific terminology
- Email CTAs should include descriptive subject lines
- Analytics tracking is essential for measuring conversion rates
- Ensure consistent styling across all CTA sections
