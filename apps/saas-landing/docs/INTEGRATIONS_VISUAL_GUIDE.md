# Integrations Section - Visual Guide

## Section Overview

The integrations section showcases healthcare-relevant tools that connect with clinic operations. It appears on the landing page after the pricing section.

## Visual Layout

```
┌─────────────────────────────────────────────────────────────────┐
│                    INTEGRATIONS SECTION                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│                      Seamless Connections                        │
│                                                                  │
│           Integrates with your healthcare workflow               │
│                                                                  │
│    Connect with the tools you already use to streamline         │
│              your clinic operations                              │
│                                                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐  │
│  │ [💳] │  │ [💰] │  │ [📅] │  │ [✉️]  │  │ [📊] │  │ [📱] │  │
│  │Stripe│  │PayPal│  │Google│  │Mail- │  │Quick-│  │Twilio│  │
│  │      │  │      │  │Calen-│  │chimp │  │Books │  │      │  │
│  │Accept│  │Process│ │dar   │  │      │  │      │  │      │  │
│  │patient│ │clinic │  │      │  │Send  │  │Sync  │  │Send  │  │
│  │pay-  │  │pay-  │  │Sync  │  │appt  │  │clinic│  │SMS   │  │
│  │ments │  │ments │  │appt  │  │remind│  │revenue│ │appt  │  │
│  │secure│  │and   │  │sched-│  │-ers  │  │and   │  │remind│  │
│  │-ly...│  │manage│  │ules..│  │and...│  │expen-│  │-ers..│  │
│  │      │  │sub...│  │      │  │      │  │ses...│  │      │  │
│  └──────┘  └──────┘  └──────┘  └──────┘  └──────┘  └──────┘  │
│                                                                  │
│                  ┌──────────────────────────┐                   │
│                  │ View All Integrations → │                   │
│                  └──────────────────────────┘                   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Desktop Layout (1024px+)

**Grid:** 5 columns
**Spacing:** 24px gap between cards
**Card Size:** ~200px width

```
┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
│ Stripe │ │ PayPal │ │ Google │ │Mailchimp│ │QuickBks│
└────────┘ └────────┘ └────────┘ └────────┘ └────────┘
┌────────┐
│ Twilio │
└────────┘
```

## Tablet Layout (768px)

**Grid:** 3 columns
**Spacing:** 24px gap between cards

```
┌────────┐ ┌────────┐ ┌────────┐
│ Stripe │ │ PayPal │ │ Google │
└────────┘ └────────┘ └────────┘
┌────────┐ ┌────────┐ ┌────────┐
│Mailchimp│ │QuickBks│ │ Twilio │
└────────┘ └────────┘ └────────┘
```

## Mobile Layout (375px)

**Grid:** 2 columns
**Spacing:** 24px gap between cards

```
┌────────┐ ┌────────┐
│ Stripe │ │ PayPal │
└────────┘ └────────┘
┌────────┐ ┌────────┐
│ Google │ │Mailchimp│
└────────┘ └────────┘
┌────────┐ ┌────────┐
│QuickBks│ │ Twilio │
└────────┘ └────────┘
```

## Card Anatomy

```
┌─────────────────────────────────┐
│                                  │
│         ┌─────────┐              │ ← Logo container
│         │  [💳]   │              │   (with gradient glow on hover)
│         └─────────┘              │
│                                  │
│         Stripe                   │ ← Integration name (bold)
│                                  │
│    Accept patient payments       │ ← Description
│    securely with automated       │   (mentions clinic operations)
│    billing for clinic services   │
│                                  │
└─────────────────────────────────┘
```

## Color Scheme

### Light Mode
- **Section Background:** `bg-gradient-to-b from-white to-slate-50`
- **Card Background:** `bg-white`
- **Card Border:** `border-slate-200`
- **Card Hover Border:** `border-primary/40`
- **Card Hover Background:** `bg-slate-50`
- **Text Primary:** `text-slate-900`
- **Text Secondary:** `text-slate-600`
- **Description:** `text-slate-500`

### Dark Mode
- **Section Background:** `dark:from-gray-900 dark:to-gray-950`
- **Card Background:** `dark:bg-gray-900`
- **Card Border:** `dark:border-gray-800`
- **Card Hover Background:** `dark:hover:bg-gray-800`
- **Text Primary:** `dark:text-white`
- **Text Secondary:** `dark:text-gray-400`
- **Description:** `dark:text-gray-500`

## Interactive States

### Default State
```
┌─────────────────────────────────┐
│         ┌─────────┐              │
│         │  [💳]   │              │
│         └─────────┘              │
│         Stripe                   │
│    Accept patient payments...    │
└─────────────────────────────────┘
Border: slate-200
Background: white
```

### Hover State
```
┌═════════════════════════════════┐ ← Border changes to primary/40
│         ┌─────────┐              │
│         │ ✨[💳]✨ │              │ ← Gradient glow appears
│         └─────────┘              │
│         Stripe                   │
│    Accept patient payments...    │
└═════════════════════════════════┘
Border: primary/40
Background: slate-50
Glow: gradient-to-r from-primary/20 to-mintlify-blue/20
```

### Focus State (CTA Button)
```
┌──────────────────────────────────┐
│ View All Integrations →          │ ← Focus ring appears
└──────────────────────────────────┘
Ring: 2px primary color
Ring Offset: 2px
```

## Animation Sequence

### On Scroll Into View
```
Time: 0ms
┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐
│    │ │    │ │    │ │    │ │    │ │    │
└────┘ └────┘ └────┘ └────┘ └────┘ └────┘
Opacity: 0, Scale: 0.8

Time: 50ms
┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐
│ ✓  │ │    │ │    │ │    │ │    │ │    │
└────┘ └────┘ └────┘ └────┘ └────┘ └────┘
Opacity: 1, Scale: 1.0

Time: 100ms
┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐
│ ✓  │ │ ✓  │ │    │ │    │ │    │ │    │
└────┘ └────┘ └────┘ └────┘ └────┘ └────┘

Time: 150ms
┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐
│ ✓  │ │ ✓  │ │ ✓  │ │    │ │    │ │    │
└────┘ └────┘ └────┘ └────┘ └────┘ └────┘

... (continues with 50ms stagger)
```

## Typography

### Section Title
- **Font Size:** `text-3xl md:text-4xl` (30px → 36px)
- **Font Weight:** `font-bold` (700)
- **Color:** `text-slate-900 dark:text-white`
- **Margin Bottom:** `mb-6` (24px)

### Section Subtitle
- **Font Size:** `text-lg` (18px)
- **Color:** `text-slate-600 dark:text-gray-400`

### Badge Text
- **Font Size:** Default (16px)
- **Font Weight:** `font-medium` (500)
- **Color:** `text-primary dark:text-primary/80`

### Integration Name
- **Font Size:** Default (16px)
- **Font Weight:** `font-medium` (500)
- **Color:** `text-slate-900 dark:text-white`
- **Margin Bottom:** `mb-1` (4px)

### Integration Description
- **Font Size:** `text-xs` (12px)
- **Color:** `text-slate-500 dark:text-gray-500`

### CTA Button
- **Font Size:** `text-base` (16px)
- **Font Weight:** `font-semibold` (600)
- **Height:** `h-12` (48px)
- **Padding:** `px-8` (32px horizontal)

## Spacing

### Section Padding
- **Vertical:** `py-20 md:py-32` (80px → 128px)
- **Horizontal:** Container with `px-4 md:px-8`

### Content Max Width
- **Max Width:** `max-w-3xl` (768px) for title/subtitle
- **Margin:** `mx-auto` (centered)
- **Margin Bottom:** `mb-16` (64px)

### Grid Gap
- **Gap:** `gap-6` (24px)

### Card Padding
- **Padding:** `p-6` (24px all sides)

### Logo Container
- **Width:** `w-16` (64px)
- **Height:** `h-16` (64px)
- **Margin Bottom:** `mb-4` (16px)

### Logo Size
- **Width:** `w-12` (48px)
- **Height:** `h-12` (48px)
- **Inner Icon:** `w-8 h-8` (32px)

### CTA Margin
- **Margin Top:** `mt-12` (48px)

## Accessibility Features

### Semantic HTML
```html
<section>
  <div class="container">
    <div class="text-center">
      <p>Badge text</p>
      <h2>Section title</h2>
      <p>Section subtitle</p>
    </div>
    <div class="grid">
      <div>Integration card</div>
      ...
    </div>
    <div class="text-center">
      <a>CTA button</a>
    </div>
  </div>
</section>
```

### ARIA Labels
- Arrow icon: `aria-hidden="true"`
- CTA link: Descriptive text (no additional label needed)

### Keyboard Navigation
- Tab order: Natural flow (CTA button is focusable)
- Focus indicators: Visible ring on CTA button
- Enter key: Activates CTA link

### Screen Reader
- Section has proper heading hierarchy (h2)
- All text content is accessible
- Images have alt text (integration names)

## Bilingual Display

### English
```
Title: "Integrates with your healthcare workflow"
Subtitle: "Connect with the tools you already use..."
CTA: "View All Integrations"
```

### Arabic (RTL)
```
Title: "يتكامل مع سير عمل الرعاية الصحية الخاص بك"
Subtitle: "اتصل بالأدوات التي تستخدمها بالفعل..."
CTA: "عرض جميع التكاملات"
```

## Performance Optimizations

1. **Lazy Loading:** Section loads only when scrolled into view
2. **SVG Logos:** Lightweight vector graphics (< 1KB each)
3. **Animation Optimization:** Uses GPU-accelerated transforms
4. **Viewport Detection:** Animations trigger once with IntersectionObserver
5. **No External Dependencies:** All logos are local SVG files

## Browser Support

- ✅ Chrome 90+
- ✅ Firefox 88+
- ✅ Safari 14+
- ✅ Edge 90+
- ✅ Mobile browsers (iOS Safari, Chrome Mobile)

## Common Visual Issues & Solutions

### Issue: Logos not displaying
**Solution:** Check SVG files exist in `public/images/integrations/`

### Issue: Grid layout broken
**Solution:** Verify Tailwind classes: `grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5`

### Issue: Hover effects not working
**Solution:** Check `group` class on parent div and `group-hover:` classes

### Issue: Dark mode colors wrong
**Solution:** Verify `dark:` prefixed classes are present

### Issue: Animations not triggering
**Solution:** Check Framer Motion viewport detection settings

### Issue: CTA not navigating
**Solution:** Verify `API_DOCS_URL` environment variable is set

## Design Tokens

```css
/* Primary Colors */
--primary: /* Your primary color */
--mintlify-blue: /* Accent color */

/* Spacing Scale */
--spacing-4: 16px
--spacing-6: 24px
--spacing-12: 48px
--spacing-16: 64px
--spacing-20: 80px
--spacing-32: 128px

/* Border Radius */
--radius-xl: 12px
--radius-full: 9999px

/* Transitions */
--transition-all: all 300ms ease
--transition-opacity: opacity 300ms ease
```
