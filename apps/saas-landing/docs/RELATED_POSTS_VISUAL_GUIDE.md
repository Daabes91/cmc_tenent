# Related Posts Feature - Visual Guide

## Overview
This guide provides a visual representation of the related posts feature and how it appears on blog post pages.

## Layout Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                     Blog Post Content                            │
│                     (Main Article)                               │
│                                                                   │
│  [Title, Author, Content, Tags, etc.]                           │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Related Articles Section                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  Related Articles                                                │
│  Continue reading with these related posts                       │
│                                                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                      │
│  │  Card 1  │  │  Card 2  │  │  Card 3  │                      │
│  │          │  │          │  │          │                      │
│  │  [Image] │  │  [Image] │  │  [Image] │                      │
│  │          │  │          │  │          │                      │
│  │  Badge   │  │  Badge   │  │  Badge   │                      │
│  │  Title   │  │  Title   │  │  Title   │                      │
│  │  Excerpt │  │  Excerpt │  │  Excerpt │                      │
│  │  Meta    │  │  Meta    │  │  Meta    │                      │
│  └──────────┘  └──────────┘  └──────────┘                      │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## Related Post Card Structure

```
┌─────────────────────────────────────────┐
│                                         │
│  ┌───────────────────────────────────┐ │
│  │                                   │ │
│  │      Featured Image (16:9)       │ │
│  │      (Hover: Scale 1.05)         │ │
│  │                                   │ │
│  └───────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │ Practice Management  • 5 min    │   │ ← Category Badge + Reading Time
│  └─────────────────────────────────┘   │
│                                         │
│  10 Appointment Scheduling Tips         │ ← Title (line-clamp-2)
│  to Maximize Your Clinic's Efficiency   │
│                                         │
│  Master the art of appointment          │ ← Excerpt (line-clamp-2)
│  scheduling with these proven...        │
│                                         │
│  Dr. Emily Rodriguez • Jan 25, 2024     │ ← Author + Date
│                                         │
└─────────────────────────────────────────┘
```

## Responsive Layouts

### Desktop (≥1024px)
```
┌──────────────────────────────────────────────────────────────┐
│  Related Articles                                             │
│  Continue reading with these related posts                    │
│                                                               │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │   Card 1    │    │   Card 2    │    │   Card 3    │     │
│  │             │    │             │    │             │     │
│  │   [Image]   │    │   [Image]   │    │   [Image]   │     │
│  │   Content   │    │   Content   │    │   Content   │     │
│  └─────────────┘    └─────────────┘    └─────────────┘     │
└──────────────────────────────────────────────────────────────┘
```

### Tablet (768px - 1023px)
```
┌──────────────────────────────────────────────────────┐
│  Related Articles                                     │
│  Continue reading with these related posts            │
│                                                       │
│  ┌─────────────────┐    ┌─────────────────┐         │
│  │     Card 1      │    │     Card 2      │         │
│  │                 │    │                 │         │
│  │    [Image]      │    │    [Image]      │         │
│  │    Content      │    │    Content      │         │
│  └─────────────────┘    └─────────────────┘         │
│                                                       │
│  ┌─────────────────┐                                 │
│  │     Card 3      │                                 │
│  │                 │                                 │
│  │    [Image]      │                                 │
│  │    Content      │                                 │
│  └─────────────────┘                                 │
└──────────────────────────────────────────────────────┘
```

### Mobile (<768px)
```
┌────────────────────────────────┐
│  Related Articles               │
│  Continue reading with these    │
│  related posts                  │
│                                 │
│  ┌──────────────────────────┐  │
│  │       Card 1             │  │
│  │                          │  │
│  │      [Image]             │  │
│  │      Content             │  │
│  └──────────────────────────┘  │
│                                 │
│  ┌──────────────────────────┐  │
│  │       Card 2             │  │
│  │                          │  │
│  │      [Image]             │  │
│  │      Content             │  │
│  └──────────────────────────┘  │
│                                 │
│  ┌──────────────────────────┐  │
│  │       Card 3             │  │
│  │                          │  │
│  │      [Image]             │  │
│  │      Content             │  │
│  └──────────────────────────┘  │
└────────────────────────────────┘
```

## Visual States

### Default State
```
┌─────────────────────────────────────┐
│                                     │
│  ┌───────────────────────────────┐ │
│  │      Featured Image           │ │
│  └───────────────────────────────┘ │
│                                     │
│  Practice Management  • 5 min      │
│                                     │
│  10 Appointment Scheduling Tips    │
│                                     │
│  Master the art of appointment...  │
│                                     │
│  Dr. Emily Rodriguez • Jan 25      │
│                                     │
└─────────────────────────────────────┘
  Border: border (default)
  Shadow: none
```

### Hover State
```
┌═════════════════════════════════════┐ ← Border: border-primary/50
║                                     ║
║  ┌───────────────────────────────┐ ║
║  │   Featured Image (scaled)     │ ║ ← Image: scale-105
║  └───────────────────────────────┘ ║
║                                     ║
║  Practice Management  • 5 min      ║
║                                     ║
║  10 Appointment Scheduling Tips    ║ ← Title: text-primary
║                                     ║
║  Master the art of appointment...  ║
║                                     ║
║  Dr. Emily Rodriguez • Jan 25      ║
║                                     ║
└═════════════════════════════════════┘
  Border: border-primary/50
  Shadow: shadow-lg
  Cursor: pointer
```

### Focus State (Keyboard Navigation)
```
┌═════════════════════════════════════┐
║ ┌─────────────────────────────────┐ ║ ← Focus ring
║ │                                 │ ║
║ │  ┌───────────────────────────┐ │ ║
║ │  │   Featured Image          │ │ ║
║ │  └───────────────────────────┘ │ ║
║ │                               │ ║
║ │  Practice Management • 5 min  │ ║
║ │                               │ ║
║ │  10 Appointment Scheduling... │ ║
║ │                               │ ║
║ │  Master the art of...         │ ║
║ │                               │ ║
║ │  Dr. Emily Rodriguez • Jan 25 │ ║
║ │                               │ ║
║ └─────────────────────────────────┘ ║
└═════════════════════════════════════┘
  Focus: ring-2 ring-primary
```

## Color Scheme

### Light Mode
```
Section Background: white / bg-background
Card Background: white / bg-card
Card Border: gray-200 / border
Hover Border: primary-500 / border-primary/50
Title: gray-900 / text-foreground
Hover Title: primary-600 / text-primary
Excerpt: gray-600 / text-muted-foreground
Badge Background: primary-50 / bg-primary/10
Badge Text: primary-600 / text-primary
```

### Dark Mode
```
Section Background: gray-950 / bg-background
Card Background: gray-900 / bg-card
Card Border: gray-800 / border
Hover Border: primary-500 / border-primary/50
Title: gray-100 / text-foreground
Hover Title: primary-400 / text-primary
Excerpt: gray-400 / text-muted-foreground
Badge Background: primary-950 / bg-primary/10
Badge Text: primary-400 / text-primary
```

## Typography Scale

```
Section Heading:    text-3xl (30px) font-bold
Section Subtitle:   text-base (16px) text-muted-foreground
Card Title:         text-lg (18px) font-semibold
Card Excerpt:       text-sm (14px) text-muted-foreground
Category Badge:     text-xs (12px) text-primary
Reading Time:       text-xs (12px) text-muted-foreground
Author/Date:        text-xs (12px) text-muted-foreground
```

## Spacing System

```
Section:
  - Margin Top: mt-16 (64px)
  - Padding Top: pt-16 (64px)
  - Border Top: border-t

Heading:
  - Margin Bottom: mb-8 (32px)

Grid:
  - Gap: gap-6 (24px)

Card:
  - Padding: p-5 (20px)
  - Border Radius: rounded-lg (8px)

Card Elements:
  - Category/Time Gap: gap-2 (8px)
  - Category Margin Bottom: mb-3 (12px)
  - Title Margin Bottom: mb-2 (8px)
  - Excerpt Margin Bottom: mb-3 (12px)
```

## Animation Timings

```
Image Hover Scale:
  - Duration: 300ms
  - Easing: ease-in-out
  - Transform: scale(1.05)

Card Hover:
  - Duration: 200ms (default transition)
  - Properties: border-color, box-shadow, color

Title Color Change:
  - Duration: 200ms (default transition)
  - Property: color
```

## Example Scenarios

### Scenario 1: Post with 3 Related Posts
```
Current Post: "5 Ways to Reduce Patient No-Shows"
Category: practice-management
Tags: appointments, patient-engagement, efficiency

Related Posts Shown:
1. "10 Appointment Scheduling Tips" (same category + matching tags)
2. "Patient Portal Benefits" (matching tag: patient-engagement)
3. "HIPAA Compliance Guide" (different category, no matching tags)
```

### Scenario 2: Post with 1 Related Post
```
Current Post: "HIPAA Compliance Guide"
Category: compliance
Tags: HIPAA, compliance, data-security

Related Posts Shown:
1. "Patient Portal Benefits" (different category, no matching tags)

Note: Only 1 card displayed, grid adjusts automatically
```

### Scenario 3: Post with No Related Posts
```
Current Post: "Unique Topic Post"
Category: unique-category
Tags: unique-tags

Related Posts Shown:
(Section is hidden - no "Related Articles" heading appears)
```

## Accessibility Features

### Keyboard Navigation Flow
```
Tab Order:
1. Related Post Card 1 (entire card is clickable)
2. Related Post Card 2
3. Related Post Card 3

Focus Indicators:
- Visible outline on focused card
- High contrast focus ring
- Maintains focus when navigating with keyboard
```

### Screen Reader Announcements
```
Section: "Related Articles section"
Heading: "Related Articles"
Subtitle: "Continue reading with these related posts"

Each Card:
"Link: [Post Title]
Category: [Category Name]
Reading time: [X] minutes
[Excerpt text]
By [Author Name]
Published [Date]"
```

## Implementation Notes

### Component Hierarchy
```
<section> (Related Articles Section)
  ├── <div> (Heading Container)
  │   ├── <h2> (Section Heading)
  │   └── <p> (Section Subtitle)
  └── <div> (Grid Container)
      ├── <Link> (Card 1)
      │   ├── <div> (Image Container)
      │   │   └── <Image> (Featured Image)
      │   └── <div> (Content Container)
      │       ├── <div> (Badge + Time)
      │       ├── <h3> (Title)
      │       ├── <p> (Excerpt)
      │       └── <div> (Author + Date)
      ├── <Link> (Card 2)
      └── <Link> (Card 3)
```

### CSS Classes Applied
```
Section: "mt-16 pt-16 border-t"
Heading: "text-3xl font-bold mb-2"
Subtitle: "text-muted-foreground"
Grid: "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
Card: "group block bg-card rounded-lg overflow-hidden border hover:border-primary/50 transition-all hover:shadow-lg"
Image: "object-cover group-hover:scale-105 transition-transform duration-300"
```

## Testing Checklist

Visual testing should verify:
- ✅ Section appears after blog content
- ✅ Heading and subtitle are visible
- ✅ Cards display in correct grid layout
- ✅ Images load and display correctly
- ✅ Hover effects work smoothly
- ✅ Responsive breakpoints work correctly
- ✅ Dark mode colors are appropriate
- ✅ Typography is readable
- ✅ Spacing is consistent
- ✅ Focus indicators are visible
- ✅ Cards are clickable
- ✅ Navigation works correctly

## Browser Rendering

The feature has been tested and renders correctly in:
- Chrome 120+ ✅
- Safari 17+ ✅
- Firefox 121+ ✅
- Edge 120+ ✅

## Performance Metrics

Expected performance:
- First Contentful Paint: < 1.5s
- Largest Contentful Paint: < 2.5s
- Cumulative Layout Shift: < 0.1
- Time to Interactive: < 3.5s

Images are optimized using Next.js Image component with:
- Automatic format selection (WebP/AVIF)
- Responsive sizing
- Lazy loading
- Blur placeholder
