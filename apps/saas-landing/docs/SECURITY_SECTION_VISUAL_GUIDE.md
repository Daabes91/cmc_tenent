# Security Section - Visual Guide

## Quick Visual Reference

This guide provides a visual description of the Security and Compliance section to help with testing and verification.

## Section Location

```
Landing Page Structure:
├── Header
├── Hero
├── Social Proof
├── Features
├── How It Works
├── Testimonials
├── Pricing
├── Comparison Table
├── Integrations
├── 🔒 SECURITY (NEW) ← You are here
├── FAQ
├── Blog Preview
├── CTA
└── Footer
```

## Desktop Layout (1440px)

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                   │
│                         [Shield Icon]                             │
│                                                                   │
│           Healthcare-grade security and compliance                │
│                                                                   │
│     Your patient data is protected with enterprise-level          │
│     security measures and healthcare compliance standards         │
│                                                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │ [Shield] │  │ [Shield] │  │ [Shield] │  │ [Shield] │        │
│  │  HIPAA   │  │   GDPR   │  │ SOC 2    │  │ ISO      │        │
│  │Compliant │  │  Ready   │  │ Type II  │  │ 27001    │        │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘        │
│                                                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌────────────────────┐  ┌────────────────────┐  ┌──────────┐  │
│  │ [Lock] Bank-level  │  │ [Shield] Regular   │  │[Database]│  │
│  │ 256-bit encryption │  │ security audits    │  │Automated │  │
│  └────────────────────┘  └────────────────────┘  │daily     │  │
│                                                   │backups   │  │
│  ┌────────────────────┐  ┌────────────────────┐  └──────────┘  │
│  │ [Clock] 99.9%      │  │ [UserCheck] Role-  │  ┌──────────┐  │
│  │ uptime SLA         │  │ based access       │  │[FileCheck│  │
│  └────────────────────┘  └────────────────────┘  │Audit     │  │
│                                                   │trail log │  │
│                                                   └──────────┘  │
│                                                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│         [Privacy Policy]    [Terms of Service]                    │
│         (Primary Button)    (Outline Button)                      │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## Mobile Layout (375px)

```
┌─────────────────────────┐
│                         │
│     [Shield Icon]       │
│                         │
│  Healthcare-grade       │
│  security and           │
│  compliance             │
│                         │
│  Your patient data is   │
│  protected with...      │
│                         │
├─────────────────────────┤
│                         │
│  ┌─────────┐ ┌────────┐│
│  │[Shield] │ │[Shield]││
│  │ HIPAA   │ │ GDPR   ││
│  │Compliant│ │ Ready  ││
│  └─────────┘ └────────┘│
│                         │
│  ┌─────────┐ ┌────────┐│
│  │[Shield] │ │[Shield]││
│  │ SOC 2   │ │  ISO   ││
│  │Type II  │ │ 27001  ││
│  └─────────┘ └────────┘│
│                         │
├─────────────────────────┤
│                         │
│  ┌───────────────────┐ │
│  │ [Lock] Bank-level │ │
│  │ 256-bit encryption│ │
│  └───────────────────┘ │
│                         │
│  ┌───────────────────┐ │
│  │ [Shield] Regular  │ │
│  │ security audits   │ │
│  └───────────────────┘ │
│                         │
│  ┌───────────────────┐ │
│  │ [Database]        │ │
│  │ Automated daily   │ │
│  │ backups           │ │
│  └───────────────────┘ │
│                         │
│  (... 3 more cards)     │
│                         │
├─────────────────────────┤
│                         │
│  ┌───────────────────┐ │
│  │  Privacy Policy   │ │
│  └───────────────────┘ │
│                         │
│  ┌───────────────────┐ │
│  │ Terms of Service  │ │
│  └───────────────────┘ │
│                         │
└─────────────────────────┘
```

## Color Scheme

### Light Mode
```
Background:     Gradient from #F8FAFC (slate-50) to #FFFFFF (white)
Decorative:     Primary/10 and Mintlify Blue/10 blur circles
Cards:          #FFFFFF (white) with #E2E8F0 (slate-200) borders
Text:           #0F172A (slate-900) headings, #475569 (slate-600) body
Icons:          Primary brand color
Hover:          Primary/40 borders, shadow-lg
```

### Dark Mode
```
Background:     Gradient from #020617 (gray-950) to #111827 (gray-900)
Decorative:     Primary/10 and Mintlify Blue/10 blur circles
Cards:          #111827 (gray-900) with #1F2937 (gray-800) borders
Text:           #FFFFFF (white) headings, #D1D5DB (gray-300) body
Icons:          Primary brand color
Hover:          Primary/40 borders, shadow-md
```

## Component Breakdown

### 1. Header Section
```
┌─────────────────────────────────────┐
│         [Shield Icon Badge]         │
│    (Primary color, rounded circle)  │
│                                     │
│  Healthcare-grade security and      │
│  compliance                         │
│  (3xl/4xl font, bold, centered)     │
│                                     │
│  Your patient data is protected...  │
│  (lg font, slate-600, centered)     │
└─────────────────────────────────────┘
```

### 2. Security Badges
```
Grid: 2 columns (mobile) → 4 columns (desktop)
Gap: 1rem (16px)

Each Badge:
┌──────────────────┐
│   [Shield Icon]  │ ← Primary/10 background, rounded
│                  │
│  HIPAA Compliant │ ← Bold, slate-900
└──────────────────┘
Border: 2px primary/20
Hover: Border → primary/40, shadow-lg
```

### 3. Trust Indicators
```
Grid: 1 column (mobile) → 2 columns (tablet) → 3 columns (desktop)
Gap: 1.5rem (24px)

Each Indicator:
┌────────────────────────────────┐
│ [Icon]  Bank-level 256-bit     │
│         encryption             │
└────────────────────────────────┘
Icon: 40x40px, primary/10 background
Text: Medium weight, slate-700
Border: 1px slate-200
Hover: Border → primary/40, shadow-md
```

### 4. CTA Buttons
```
Desktop (side-by-side):
┌──────────────────┐  ┌──────────────────┐
│  Privacy Policy  │  │ Terms of Service │
│  (Primary solid) │  │ (Outline)        │
└──────────────────┘  └──────────────────┘

Mobile (stacked):
┌──────────────────┐
│  Privacy Policy  │
└──────────────────┘
┌──────────────────┐
│ Terms of Service │
└──────────────────┘

Size: h-12 (48px), px-8 (32px horizontal)
Font: Base (16px), semibold
```

## Animation Sequence

```
Timeline (on scroll into view):

0ms:    Section enters viewport
        ↓
100ms:  Header fades in + slides up
        ↓
200ms:  Badge 1 scales + fades in
        ↓
250ms:  Badge 2 scales + fades in
        ↓
300ms:  Badge 3 scales + fades in
        ↓
350ms:  Badge 4 scales + fades in
        ↓
400ms:  Indicator 1 slides up + fades in
        ↓
500ms:  Indicator 2 slides up + fades in
        ↓
600ms:  Indicator 3 slides up + fades in
        ↓
700ms:  Indicator 4 slides up + fades in
        ↓
800ms:  Indicator 5 slides up + fades in
        ↓
900ms:  Indicator 6 slides up + fades in
        ↓
1200ms: CTA buttons fade in

Total animation duration: ~1.2 seconds
```

## Hover States

### Security Badge Hover
```
Before:
┌──────────────────┐
│   [Shield Icon]  │
│  HIPAA Compliant │
└──────────────────┘
Border: primary/20

After (hover):
┌══════════════════┐  ← Thicker border (primary/40)
│   [Shield Icon]  │  ← Icon scales to 110%
│  HIPAA Compliant │
└══════════════════┘
Shadow: Large shadow with primary tint
Transition: 300ms smooth
```

### Trust Indicator Hover
```
Before:
┌────────────────────────────────┐
│ [Icon]  Bank-level 256-bit     │
│         encryption             │
└────────────────────────────────┘
Icon bg: primary/10

After (hover):
┌════════════════════════════════┐  ← Border: primary/40
│ [Icon]  Bank-level 256-bit     │  ← Icon bg: primary/20
│         encryption             │
└════════════════════════════════┘
Shadow: Medium shadow
Transition: 300ms smooth
```

### Button Hover
```
Primary Button:
Before: bg-primary
After:  bg-primary/90 (slightly lighter)

Outline Button:
Before: border-primary/30, bg-transparent
After:  border-primary/30, bg-primary/10
```

## Spacing & Sizing

### Section Padding
- Mobile: py-20 (5rem / 80px top & bottom)
- Desktop: py-32 (8rem / 128px top & bottom)

### Container
- Max width: Full width with container class
- Horizontal padding: px-4 (mobile), px-8 (desktop)

### Header
- Max width: 3xl (48rem / 768px)
- Margin bottom: mb-16 (4rem / 64px)

### Badges Grid
- Max width: 4xl (56rem / 896px)
- Margin bottom: mb-16 (4rem / 64px)
- Gap: gap-4 (1rem / 16px)

### Indicators Grid
- Max width: 6xl (72rem / 1152px)
- Gap: gap-6 (1.5rem / 24px)

### CTA Section
- Margin top: mt-16 (4rem / 64px)
- Button gap: gap-4 (1rem / 16px)

## Icon Mapping

```
Trust Indicator Icons:
1. Bank-level encryption     → Lock (lucide-react)
2. Regular security audits   → Shield (lucide-react)
3. Automated daily backups   → Database (lucide-react)
4. 99.9% uptime SLA          → Clock (lucide-react)
5. Role-based access control → UserCheck (lucide-react)
6. Audit trail logging       → FileCheck (lucide-react)

Badge Icons:
All badges → Shield (lucide-react)

Header Icon:
Section header → Shield (lucide-react)
```

## Accessibility Features

### Keyboard Navigation
```
Tab Order:
1. Privacy Policy button
2. Terms of Service button

Focus Indicator:
┌══════════════════┐
║  Privacy Policy  ║  ← Ring: 2px primary color
║  (Focused)       ║     Offset: 2px
└══════════════════┘
```

### Screen Reader
```
Announced Elements:
- Section heading: "Healthcare-grade security and compliance"
- Badge text: "HIPAA Compliant", "GDPR Ready", etc.
- Indicator text: Full text of each indicator
- Button labels: "Privacy Policy", "Terms of Service"

Hidden from Screen Reader:
- Decorative icons (aria-hidden="true")
- Background blur circles
- Decorative gradients
```

## Responsive Breakpoints

```
Mobile (< 768px):
- 2-column badge grid
- 1-column indicator grid
- Stacked CTA buttons
- Smaller text sizes
- Reduced padding

Tablet (768px - 1023px):
- 4-column badge grid
- 2-column indicator grid
- Side-by-side CTA buttons
- Medium text sizes
- Standard padding

Desktop (≥ 1024px):
- 4-column badge grid
- 3-column indicator grid
- Side-by-side CTA buttons
- Large text sizes
- Increased padding
```

## Testing Checklist

Use this visual guide to verify:
- [ ] Section appears after Integrations, before FAQ
- [ ] Header has shield icon, title, and description
- [ ] 4 security badges display in correct grid
- [ ] 6 trust indicators display with correct icons
- [ ] 2 CTA buttons at bottom
- [ ] Hover effects work on all interactive elements
- [ ] Animations play on scroll (once)
- [ ] Responsive layout works on all breakpoints
- [ ] Dark mode colors are correct
- [ ] Arabic text displays correctly (RTL)

## Common Issues & Solutions

### Issue: Badges not in grid
**Solution**: Check Tailwind classes `grid grid-cols-2 md:grid-cols-4`

### Issue: Icons not showing
**Solution**: Verify lucide-react is installed and icons are imported

### Issue: Animations not playing
**Solution**: Check Framer Motion is installed and viewport prop is set

### Issue: Dark mode not working
**Solution**: Verify dark: classes are applied and theme provider is set up

### Issue: Arabic text not RTL
**Solution**: Check LanguageContext is providing correct language value

## Quick Verification Commands

```bash
# Check component exists
ls apps/saas-landing/components/security.tsx

# Check it's imported in page
grep "Security" apps/saas-landing/app/page.tsx

# Run build to check for errors
cd apps/saas-landing && npm run build

# Start dev server
cd apps/saas-landing && npm run dev
```

## Visual Comparison

### Before (No Security Section)
```
... Integrations Section ...
... FAQ Section ...
```

### After (With Security Section)
```
... Integrations Section ...
... Security Section (NEW) ...
... FAQ Section ...
```

The security section adds approximately 600-800px of height to the page, depending on viewport width.
