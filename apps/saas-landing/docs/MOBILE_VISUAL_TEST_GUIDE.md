# Mobile Visual Testing Guide

## Overview
This guide provides visual testing procedures to verify mobile responsiveness across different viewports and devices.

## Visual Testing Checklist

### 1. Hero Section

#### 320px (iPhone SE)
**What to Check:**
- [ ] Badge is visible and properly sized
- [ ] Headline text wraps correctly (no overflow)
- [ ] Description text is readable (16px minimum)
- [ ] CTA buttons are full width
- [ ] CTA buttons are 44px height minimum
- [ ] Buttons stack vertically with proper spacing
- [ ] Hero image scales appropriately
- [ ] No horizontal scrolling

**Screenshot Locations:**
- Top of hero section
- CTA buttons area
- Hero image

**Expected Layout:**
```
┌─────────────────────────┐
│       [Badge]           │
│                         │
│   Headline Text Here    │
│   Wraps to Multiple     │
│   Lines                 │
│                         │
│   Description text      │
│   is readable and       │
│   wraps properly        │
│                         │
│ ┌─────────────────────┐ │
│ │  Primary CTA (44px) │ │
│ └─────────────────────┘ │
│ ┌─────────────────────┐ │
│ │ Secondary CTA (44px)│ │
│ └─────────────────────┘ │
│                         │
│   [Hero Image]          │
│                         │
└─────────────────────────┘
```

#### 375px (iPhone 12 Mini)
**What to Check:**
- [ ] Layout improves with extra width
- [ ] Text remains readable
- [ ] Buttons maintain proper height
- [ ] Image quality is good

#### 768px (iPad)
**What to Check:**
- [ ] Buttons may be inline
- [ ] Text size increases
- [ ] Image uses larger variant
- [ ] Layout transitions smoothly

---

### 2. Features Section

#### 320px (iPhone SE)
**What to Check:**
- [ ] Feature cards stack vertically
- [ ] Each card has proper padding
- [ ] Icons are visible (24px minimum)
- [ ] Title text is readable
- [ ] Description text is 16px minimum
- [ ] Cards are touch-friendly
- [ ] Proper spacing between cards

**Expected Layout:**
```
┌─────────────────────────┐
│  [Icon]                 │
│  Feature Title          │
│  Description text here  │
│  that wraps properly    │
└─────────────────────────┘
        ↓ (spacing)
┌─────────────────────────┐
│  [Icon]                 │
│  Feature Title          │
│  Description text here  │
└─────────────────────────┘
```

#### 768px (iPad)
**What to Check:**
- [ ] Features display in 2 columns
- [ ] Grid spacing is appropriate
- [ ] Cards maintain aspect ratio

---

### 3. Testimonials Section

#### 320px (iPhone SE)
**What to Check:**
- [ ] Testimonial cards stack vertically
- [ ] Quote text is readable (16px)
- [ ] Avatar images are 48px (touch-friendly)
- [ ] Name and role are visible
- [ ] Clinic type is displayed
- [ ] Metric badge (if present) is visible
- [ ] Proper card spacing

**Expected Layout:**
```
┌─────────────────────────┐
│  "Quote text here that  │
│   wraps to multiple     │
│   lines properly"       │
│                         │
│  [Metric Badge]         │
│                         │
│  [Avatar] Name          │
│           Role          │
│           Clinic Type   │
└─────────────────────────┘
```

#### 768px (iPad)
**What to Check:**
- [ ] Testimonials in 2-3 columns
- [ ] Cards maintain equal height
- [ ] Spacing is consistent

---

### 4. Pricing Section

#### 320px (iPhone SE)
**What to Check:**
- [ ] Pricing cards stack vertically
- [ ] Plan name is prominent
- [ ] Price is large and readable
- [ ] Features list is readable
- [ ] Healthcare limits are visible
- [ ] CTA button is full width
- [ ] CTA button is 44px height
- [ ] Popular badge (if present) is visible

**Expected Layout:**
```
┌─────────────────────────┐
│   [Popular Badge]       │
│                         │
│   Plan Name             │
│   Description           │
│                         │
│   $XX                   │
│   per month             │
│                         │
│   Limits:               │
│   X providers           │
│   X patients            │
│   X appointments        │
│                         │
│   ✓ Feature 1           │
│   ✓ Feature 2           │
│   ✓ Feature 3           │
│                         │
│ ┌─────────────────────┐ │
│ │   CTA Button        │ │
│ └─────────────────────┘ │
└─────────────────────────┘
```

#### 768px (iPad)
**What to Check:**
- [ ] Pricing cards in 2-3 columns
- [ ] Comparison table is visible
- [ ] Table scrolls horizontally if needed

---

### 5. Blog Section

#### 320px (iPhone SE)
**What to Check:**
- [ ] Blog cards stack vertically
- [ ] Featured image aspect ratio maintained
- [ ] Category badge is visible
- [ ] Title is readable
- [ ] Excerpt text is 16px minimum
- [ ] Author info is visible
- [ ] Date and reading time visible
- [ ] Tags wrap properly

**Expected Layout:**
```
┌─────────────────────────┐
│   [Featured Image]      │
│   [Category Badge]      │
└─────────────────────────┘
│   Title Here            │
│                         │
│   Excerpt text that     │
│   wraps properly        │
│                         │
│   [Avatar] Author       │
│            Role         │
│                         │
│   Date • X min read     │
│                         │
│   [Tag] [Tag] [Tag]     │
└─────────────────────────┘
```

#### 768px (iPad)
**What to Check:**
- [ ] Blog cards in 2 columns
- [ ] Grid spacing is appropriate
- [ ] Images load efficiently

---

### 6. Security Section

#### 320px (iPhone SE)
**What to Check:**
- [ ] Security badges in 2 columns
- [ ] Badge icons are visible
- [ ] Badge text is readable
- [ ] Trust indicators stack vertically
- [ ] Icons are 20px minimum
- [ ] Text is 16px minimum
- [ ] CTA buttons stack vertically
- [ ] Buttons are full width

**Expected Layout:**
```
┌───────────┬───────────┐
│  [Icon]   │  [Icon]   │
│  Badge 1  │  Badge 2  │
└───────────┴───────────┘
┌───────────┬───────────┐
│  [Icon]   │  [Icon]   │
│  Badge 3  │  Badge 4  │
└───────────┴───────────┘

┌─────────────────────────┐
│  [Icon] Trust Item 1    │
└─────────────────────────┘
┌─────────────────────────┐
│  [Icon] Trust Item 2    │
└─────────────────────────┘
```

---

### 7. CTA Sections

#### 320px (iPhone SE)
**What to Check:**
- [ ] Title is readable
- [ ] Description wraps properly
- [ ] CTA button is full width
- [ ] Button is 44px height
- [ ] Bullet points are visible
- [ ] Proper spacing throughout

**Expected Layout:**
```
┌─────────────────────────┐
│   Title Here            │
│                         │
│   Description text      │
│   that wraps properly   │
│                         │
│ ┌─────────────────────┐ │
│ │   CTA Button        │ │
│ └─────────────────────┘ │
│                         │
│   ✓ Bullet point 1      │
│   ✓ Bullet point 2      │
│   ✓ Bullet point 3      │
└─────────────────────────┘
```

---

## Image Quality Checklist

### Hero Image
- [ ] Loads with priority
- [ ] Appropriate size for viewport
- [ ] No pixelation
- [ ] Blur placeholder visible during load
- [ ] Aspect ratio maintained

### Feature Images
- [ ] Lazy loads
- [ ] Appropriate size
- [ ] Icons are crisp
- [ ] No layout shift

### Blog Images
- [ ] Lazy loads
- [ ] Responsive srcset
- [ ] Proper aspect ratio (16:9)
- [ ] Category badge visible
- [ ] No layout shift

### Avatar Images
- [ ] Circular shape maintained
- [ ] 48px size (touch-friendly)
- [ ] Loads efficiently
- [ ] Fallback if missing

---

## Touch Target Verification

### Visual Inspection
For each interactive element, verify:
1. Minimum 44px height
2. Minimum 44px width (for icons/buttons)
3. Adequate spacing (8px minimum)
4. Visual feedback on tap
5. No overlapping targets

### Elements to Check
- [ ] All CTA buttons
- [ ] Navigation menu items
- [ ] Category filter buttons
- [ ] Pagination controls
- [ ] Social share buttons
- [ ] Form inputs
- [ ] Links in text
- [ ] Card click areas

---

## Typography Verification

### Font Size Measurement
Use browser DevTools to measure:

1. **Body Text**
   - Minimum: 16px
   - Line height: 1.5-1.6
   - Color contrast: 4.5:1 minimum

2. **Headings**
   - H1: 28px+ on mobile
   - H2: 24px+ on mobile
   - H3: 20px+ on mobile

3. **Small Text**
   - Metadata: 14px minimum
   - Captions: 14px minimum
   - Never below 12px

### Readability Check
- [ ] Text is crisp and clear
- [ ] No text overflow
- [ ] Proper line length (45-75 characters)
- [ ] Adequate line spacing
- [ ] Good color contrast

---

## Spacing Verification

### Vertical Spacing
- [ ] Section padding: 48px+ on mobile
- [ ] Card spacing: 24px+ between cards
- [ ] Element spacing: 16px+ between elements
- [ ] Button spacing: 12px+ between buttons

### Horizontal Spacing
- [ ] Container padding: 16px on mobile
- [ ] No content touching edges
- [ ] Proper grid gaps
- [ ] Adequate margins

---

## Animation & Interaction

### Smooth Scrolling
- [ ] Scroll is smooth
- [ ] No janky animations
- [ ] Lazy loading doesn't cause jumps
- [ ] Proper scroll restoration

### Touch Feedback
- [ ] Buttons show active state
- [ ] Cards show hover/active state
- [ ] Links show active state
- [ ] Form inputs show focus state

### Performance
- [ ] Animations are smooth (60fps)
- [ ] No lag on interactions
- [ ] Quick response to taps
- [ ] No memory issues

---

## Cross-Browser Testing

### Safari (iOS)
- [ ] Layout correct
- [ ] Fonts render properly
- [ ] Images load correctly
- [ ] Interactions work
- [ ] No iOS-specific bugs

### Chrome (Android)
- [ ] Layout correct
- [ ] Fonts render properly
- [ ] Images load correctly
- [ ] Interactions work
- [ ] No Android-specific bugs

### Firefox (Android)
- [ ] Layout correct
- [ ] All features work
- [ ] No Firefox-specific bugs

---

## Orientation Testing

### Portrait Mode
- [ ] All sections visible
- [ ] Proper layout
- [ ] Images scale correctly
- [ ] Navigation accessible

### Landscape Mode
- [ ] Layout adapts
- [ ] Content remains accessible
- [ ] No horizontal scrolling
- [ ] Navigation still accessible

---

## Screenshot Documentation

### Required Screenshots
For each viewport (320px, 375px, 768px):

1. **Hero Section**
   - Full hero view
   - CTA buttons close-up
   - Hero image

2. **Features Section**
   - Feature cards grid
   - Individual card close-up

3. **Testimonials**
   - Testimonial cards
   - Avatar and metadata

4. **Pricing**
   - Pricing cards
   - Comparison table

5. **Blog**
   - Blog card grid
   - Individual blog card

6. **Security**
   - Security badges
   - Trust indicators

7. **CTA**
   - Final CTA section

### Screenshot Naming Convention
```
{section}_{viewport}_{orientation}_{browser}.png

Examples:
hero_320px_portrait_chrome.png
features_768px_landscape_safari.png
pricing_375px_portrait_firefox.png
```

---

## Issue Reporting Template

### Issue Format
```markdown
## Issue: [Brief Description]

**Severity:** Critical / High / Medium / Low

**Device:** iPhone SE / iPad / etc.
**Viewport:** 320px / 375px / etc.
**Browser:** Safari / Chrome / etc.
**Orientation:** Portrait / Landscape

**Description:**
[Detailed description of the issue]

**Expected Behavior:**
[What should happen]

**Actual Behavior:**
[What actually happens]

**Screenshot:**
[Attach screenshot]

**Steps to Reproduce:**
1. Step 1
2. Step 2
3. Step 3

**Suggested Fix:**
[If known]
```

---

## Sign-off Checklist

Before marking task as complete:

- [ ] All viewports tested (320px, 375px, 390px, 768px, 1024px)
- [ ] All sections verified
- [ ] Font sizes measured and verified
- [ ] Touch targets measured and verified
- [ ] Images optimized and verified
- [ ] Screenshots documented
- [ ] Issues logged and resolved
- [ ] Cross-browser testing complete
- [ ] Orientation testing complete
- [ ] Performance verified
- [ ] Accessibility verified
- [ ] Real device testing complete

**Tester:** _______________________
**Date:** _______________________
**Status:** ☐ Pass ☐ Fail ☐ Needs Review
