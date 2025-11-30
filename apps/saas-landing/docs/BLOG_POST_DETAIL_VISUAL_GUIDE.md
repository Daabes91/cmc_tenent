# Blog Post Detail Page - Visual Guide

## Page Layout

```
┌─────────────────────────────────────────────────────────────┐
│                         HEADER                               │
│                    (Site Navigation)                         │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                      BLOG POST HEADER                        │
│                                                              │
│  [Practice Management]  8 min read                          │
│                                                              │
│  5 Ways to Reduce Patient No-Shows in Your Clinic          │
│  ═══════════════════════════════════════════════            │
│                                                              │
│  Learn proven strategies to minimize appointment            │
│  cancellations and improve your clinic's efficiency...      │
│                                                              │
│  ┌────┐                                                     │
│  │ 👤 │  Dr. Emily Rodriguez                               │
│  └────┘  Healthcare Consultant                             │
│          • January 15, 2024                                 │
│                                                              │
│  Share: [🐦] [📘] [💼] [✉️] [🔗]                           │
│                                                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     FEATURED IMAGE                           │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │         [Clinic Appointment Scheduling Image]        │  │
│  │                                                       │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                      ARTICLE CONTENT                         │
│                                                              │
│  # 5 Ways to Reduce Patient No-Shows in Your Clinic        │
│                                                              │
│  Patient no-shows are one of the biggest challenges         │
│  facing healthcare practices today. When patients miss      │
│  appointments without notice, it creates scheduling gaps... │
│                                                              │
│  ## 1. Implement Automated Appointment Reminders            │
│                                                              │
│  Studies show that automated reminders can reduce           │
│  no-shows by up to 38%. The key is using multiple...       │
│                                                              │
│  ### Best Practices:                                        │
│  • Send the first reminder 7 days before                    │
│  • Send a second reminder 24 hours before                   │
│  • Use multiple channels: SMS, email, and phone calls       │
│  • Include appointment details and easy rescheduling        │
│                                                              │
│  [Image: Appointment Reminder Example]                      │
│                                                              │
│  ## 2. Offer Online Rescheduling                            │
│                                                              │
│  Make it easy for patients to reschedule appointments...    │
│                                                              │
│  [Content continues...]                                     │
│                                                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                          TAGS                                │
│                                                              │
│  Tags                                                        │
│  [#appointments] [#patient-engagement] [#efficiency]        │
│  [#scheduling]                                              │
│                                                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                       AUTHOR BIO                             │
│                                                              │
│  ┌────┐                                                     │
│  │ 👤 │  About Dr. Emily Rodriguez                         │
│  └────┘  Healthcare Consultant                             │
│                                                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    SHARE AGAIN SECTION                       │
│                                                              │
│  Found this helpful?                                         │
│  Share it with your colleagues                              │
│                                                              │
│  Share: [🐦] [📘] [💼] [✉️] [🔗]                           │
│                                                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                       NAVIGATION                             │
│                                                              │
│  ← Back to all posts                                        │
│                                                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                         FOOTER                               │
│                    (Site Footer)                             │
└─────────────────────────────────────────────────────────────┘
```

## Component Breakdown

### 1. Blog Post Header
**Location**: Top of page  
**Contains**:
- Category badge (clickable, filters blog)
- Reading time indicator
- Post title (H1)
- Post excerpt/description
- Author information (avatar, name, role)
- Publication date
- Social sharing buttons

**Styling**:
- Max width: 768px (3xl)
- Centered on page
- Large, bold title (4xl on mobile, 5xl on desktop)
- Muted text for metadata

### 2. Featured Image
**Location**: Below header  
**Contains**:
- Optimized image using Next.js Image component
- 2:1 aspect ratio
- Rounded corners

**Styling**:
- Full width within container
- Responsive sizing
- Lazy loading enabled

### 3. Article Content
**Location**: Main content area  
**Contains**:
- MDX-rendered content
- Headings (H1-H4)
- Paragraphs
- Lists (ordered and unordered)
- Links
- Code blocks
- Blockquotes
- Images
- Tables

**Styling**:
- Prose typography (Tailwind Typography)
- Large text (prose-lg)
- Dark mode support
- Proper spacing between elements

### 4. Tags Section
**Location**: After content  
**Contains**:
- "Tags" heading
- Tag badges (clickable, filter blog)

**Styling**:
- Border top separator
- Flex wrap layout
- Muted background for tags
- Hover effects

### 5. Author Bio
**Location**: After tags  
**Contains**:
- Author avatar (larger)
- "About [Author Name]" heading
- Author role

**Styling**:
- Border top separator
- Flex layout with avatar and text
- Larger avatar (64px)

### 6. Share Again Section
**Location**: After author bio  
**Contains**:
- "Found this helpful?" heading
- Encouragement text
- Social sharing buttons (repeated)

**Styling**:
- Border top separator
- Flex layout (space between)
- Responsive wrapping

### 7. Navigation
**Location**: Bottom of page  
**Contains**:
- "Back to all posts" link

**Styling**:
- Primary color
- Hover underline
- Left arrow icon

## Responsive Behavior

### Mobile (< 768px)
```
┌─────────────────────┐
│   Category  8 min   │
│                     │
│   Post Title        │
│   ════════          │
│                     │
│   Excerpt text...   │
│                     │
│   👤 Author         │
│   Role              │
│   • Date            │
│                     │
│   Share: 🐦📘💼✉️🔗 │
│                     │
│   [Featured Image]  │
│                     │
│   Content...        │
│                     │
│   Tags              │
│   #tag1 #tag2       │
│                     │
│   👤 About Author   │
│                     │
│   Share Again       │
│   🐦📘💼✉️🔗        │
│                     │
│   ← Back to posts   │
└─────────────────────┘
```

### Tablet (768px - 1024px)
- Similar to mobile but with more breathing room
- Larger text sizes
- More spacing between elements

### Desktop (> 1024px)
- Max width container (768px)
- Centered on page
- Optimal reading width
- Larger images and text

## Color Scheme

### Light Mode
- Background: White
- Text: Dark gray/black
- Muted text: Gray
- Primary: Brand color (blue/teal)
- Borders: Light gray
- Code blocks: Light gray background

### Dark Mode
- Background: Dark gray/black
- Text: White/light gray
- Muted text: Medium gray
- Primary: Lighter brand color
- Borders: Dark gray
- Code blocks: Darker gray background

## Interactive Elements

### Social Share Buttons
**Appearance**:
```
Share: [🐦] [📘] [💼] [✉️] [🔗]
       ↓    ↓    ↓    ↓    ↓
     Twitter FB  LI  Email Copy
```

**Behavior**:
- Hover: Background changes to muted
- Click: Opens share dialog or copies link
- Copy: Shows "Copied!" tooltip for 2 seconds

### Category Badge
**Appearance**:
```
[Practice Management]
```

**Behavior**:
- Hover: Background darkens slightly
- Click: Navigates to filtered blog listing

### Tag Badges
**Appearance**:
```
[#appointments] [#patient-engagement]
```

**Behavior**:
- Hover: Background darkens slightly
- Click: Navigates to filtered blog listing

### Links in Content
**Appearance**:
- Primary color text
- Underline on hover

**Behavior**:
- Internal links: Navigate within site
- External links: Open in new tab with noopener

## Typography

### Headings
- H1 (Title): 2.5rem (mobile) / 3rem (desktop), bold
- H2: 1.875rem, bold, margin top/bottom
- H3: 1.5rem, bold, margin top/bottom
- H4: 1.25rem, bold, margin top/bottom

### Body Text
- Paragraph: 1.125rem (18px), line-height 1.75
- Lists: Same as paragraph with proper indentation
- Code: Monospace font, smaller size, muted background

### Metadata
- Category/Tags: 0.875rem (14px)
- Reading time: 0.875rem (14px)
- Author role: 0.875rem (14px)
- Date: Regular size, muted color

## Spacing

### Sections
- Between header and image: 2rem
- Between image and content: 2rem
- Between content and tags: 2rem
- Between sections: 2rem

### Content
- Paragraph margin: 1rem
- Heading margin top: 2rem
- Heading margin bottom: 1rem
- List item spacing: 0.5rem

## Accessibility

### ARIA Labels
- Social buttons: "Share on Twitter", "Share on Facebook", etc.
- Images: Descriptive alt text
- Links: Descriptive text or aria-label

### Keyboard Navigation
- All interactive elements are keyboard accessible
- Proper focus indicators
- Logical tab order

### Screen Readers
- Semantic HTML structure
- Proper heading hierarchy
- Time elements with datetime attribute
- Article element for main content

## SEO Elements (Not Visible)

### Meta Tags
```html
<title>Reduce Patient No-Shows: 5 Proven Strategies</title>
<meta name="description" content="...">
<meta name="keywords" content="...">
<meta name="author" content="Dr. Emily Rodriguez">
<link rel="canonical" href="...">
```

### Open Graph
```html
<meta property="og:title" content="...">
<meta property="og:description" content="...">
<meta property="og:type" content="article">
<meta property="og:image" content="...">
<meta property="og:url" content="...">
<meta property="article:published_time" content="...">
<meta property="article:author" content="...">
```

### Twitter Card
```html
<meta name="twitter:card" content="summary_large_image">
<meta name="twitter:title" content="...">
<meta name="twitter:description" content="...">
<meta name="twitter:image" content="...">
```

### Structured Data
```json
{
  "@context": "https://schema.org",
  "@type": "BlogPosting",
  "headline": "...",
  "description": "...",
  "image": "...",
  "datePublished": "...",
  "author": {...},
  "publisher": {...}
}
```

## Animation and Transitions

### Hover Effects
- Social buttons: Background color transition (200ms)
- Category badge: Background color transition (200ms)
- Tag badges: Background color transition (200ms)
- Links: Underline appears (200ms)

### Copy Link Feedback
- Tooltip fades in (100ms)
- Tooltip stays visible (2000ms)
- Tooltip fades out (100ms)

## Performance Optimizations

### Images
- Next.js Image component
- Automatic format selection (WebP, AVIF)
- Responsive sizes
- Lazy loading (except featured image)
- Proper aspect ratios

### Code Splitting
- Client components only where needed
- Server components for static content
- Minimal JavaScript bundle

### Caching
- Static generation at build time
- ISR (Incremental Static Regeneration) possible
- Long cache headers for static assets
