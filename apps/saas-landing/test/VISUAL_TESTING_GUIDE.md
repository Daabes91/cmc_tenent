# Visual Testing Guide

**Quick visual reference for manual testing**

---

## 🏠 Home Page Testing

### Hero Section
```
┌─────────────────────────────────────────────────────────┐
│  Badge: "Launch your clinic online — in 1 day..."      │
│                                                         │
│  Headline: "Your All-in-One Platform to Run Your       │
│             Clinic"                                     │
│                                                         │
│  Description: "Stop relying on scattered tools..."     │
│                                                         │
│  [Get Your Clinic Portal]  [Book a Demo]               │
│                                                         │
│  ✅ Check: Healthcare terms (clinic, patients)         │
│  ✅ Check: No generic SaaS terms                       │
│  ✅ Check: CTAs are healthcare-specific                │
└─────────────────────────────────────────────────────────┘
```

### Features Section (6 features)
```
┌──────────────┬──────────────┬──────────────┐
│ 🌐 Your own  │ 📅 Smart     │ 👥 Complete  │
│ clinic       │ Appointment  │ Patient      │
│ website      │ Scheduling   │ Management   │
└──────────────┴──────────────┴──────────────┘
┌──────────────┬──────────────┬──────────────┐
│ 💰 Automated │ 📱 Virtual   │ 📊 Practice  │
│ Billing &    │ Care Ready   │ Analytics &  │
│ Invoicing    │              │ Reporting    │
└──────────────┴──────────────┴──────────────┘

✅ Check: All use healthcare terminology
✅ Check: Benefits mention clinic operations
```

### Testimonials Section (5 testimonials)
```
┌─────────────────────────────────────────────┐
│ "This platform transformed how we manage    │
│  our clinic..."                             │
│                                             │
│  Dr. Sarah Johnson                          │
│  Clinic Director, Family Medicine Practice  │
│  80% reduction in scheduling conflicts      │
└─────────────────────────────────────────────┘

✅ Check: Name, role, clinic type present
✅ Check: Quantifiable metric included
✅ Check: 5 testimonials total
```

### Pricing Section (3 tiers)
```
┌──────────────┬──────────────┬──────────────┐
│ Solo         │ Small Clinic │ Multi-       │
│ Practice     │ ⭐ POPULAR   │ Location     │
│              │              │              │
│ $49/month    │ $149/month   │ $399/month   │
│ $470/year    │ $1430/year   │ $3830/year   │
│              │              │              │
│ 1 provider   │ 5 providers  │ Unlimited    │
│ 100 patients │ 500 patients │ Unlimited    │
│ 500 appts    │ 2000 appts   │ Unlimited    │
└──────────────┴──────────────┴──────────────┘

✅ Check: Monthly and annual pricing
✅ Check: Healthcare-specific limits
✅ Check: Healthcare-specific features
```

---

## 📝 Blog System Testing

### Blog Listing Page (`/blog`)
```
┌─────────────────────────────────────────────┐
│  Search: [________________] 🔍              │
│                                             │
│  Categories: [All] [Practice] [Patient]... │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  [Featured Image]                           │
│                                             │
│  5 Ways to Reduce Patient No-Shows          │
│  Learn proven strategies to minimize...     │
│                                             │
│  Dr. Emily Rodriguez • Jan 15, 2024         │
│  Practice Management                        │
└─────────────────────────────────────────────┘

✅ Check: 11 posts visible
✅ Check: Each has title, excerpt, image, author, date
✅ Check: Search bar present
✅ Check: Category filter works
```

### Blog Post Detail (`/blog/[slug]`)
```
┌─────────────────────────────────────────────┐
│  5 Ways to Reduce Patient No-Shows          │
│                                             │
│  Dr. Emily Rodriguez • Jan 15, 2024         │
│  Practice Management                        │
│                                             │
│  [Share: Facebook] [Twitter] [LinkedIn]    │
└─────────────────────────────────────────────┘

[Full article content with MDX formatting]

┌─────────────────────────────────────────────┐
│  Related Posts                              │
│  ┌──────────┬──────────┬──────────┐        │
│  │ Post 1   │ Post 2   │ Post 3   │        │
│  └──────────┴──────────┴──────────┘        │
└─────────────────────────────────────────────┘

✅ Check: Full article renders
✅ Check: Author info displays
✅ Check: Social share buttons present
✅ Check: Related posts at bottom
```

---

## 📱 Mobile Testing

### iPhone SE (375px)
```
┌─────────────────┐
│   ☰ Menu        │
├─────────────────┤
│                 │
│  Hero Section   │
│  (Stacked)      │
│                 │
│  [CTA Button]   │
│  (Full width)   │
│                 │
├─────────────────┤
│  Feature 1      │
│  (Full width)   │
├─────────────────┤
│  Feature 2      │
│  (Full width)   │
├─────────────────┤
│  ...            │
└─────────────────┘

✅ Check: Text readable (16px min)
✅ Check: CTA buttons 44px height
✅ Check: No horizontal scroll
✅ Check: Images load properly
```

### iPad (768px)
```
┌───────────────────────────────┐
│  Header with Navigation       │
├───────────────────────────────┤
│                               │
│  Hero Section (2 columns)     │
│                               │
├───────────────────────────────┤
│  Feature 1  │  Feature 2      │
├─────────────┼─────────────────┤
│  Feature 3  │  Feature 4      │
└───────────────────────────────┘

✅ Check: Two-column layouts
✅ Check: Touch targets adequate
✅ Check: Images scale properly
```

---

## 🔍 SEO Testing

### View Page Source (Ctrl+U)
```html
<head>
  <!-- Title Tag -->
  <title>Clinic Management Software | Healthcare...</title>
  
  <!-- Meta Description -->
  <meta name="description" content="All-in-one platform...">
  
  <!-- Open Graph -->
  <meta property="og:title" content="...">
  <meta property="og:description" content="...">
  <meta property="og:image" content="...">
  
  <!-- Twitter Card -->
  <meta name="twitter:card" content="summary_large_image">
  <meta name="twitter:title" content="...">
  
  <!-- Structured Data -->
  <script type="application/ld+json">
  {
    "@context": "https://schema.org",
    "@type": "Organization",
    ...
  }
  </script>
</head>
```

✅ Check: Title tag present
✅ Check: Meta description present
✅ Check: Open Graph tags present
✅ Check: Structured data present

---

## 📈 Analytics Testing

### DevTools Network Tab
```
Filter: "google-analytics" or "gtag"

┌─────────────────────────────────────────────┐
│ Name                    Status    Type      │
├─────────────────────────────────────────────┤
│ collect?v=2&tid=G-...   200       xhr       │
│ collect?v=2&tid=G-...   200       xhr       │
│ collect?v=2&tid=G-...   200       xhr       │
└─────────────────────────────────────────────┘

✅ Check: Requests sent on page navigation
✅ Check: Requests sent on CTA clicks
✅ Check: No errors (200 status)
```

### Event Tracking
```
Action                          Event Name
─────────────────────────────────────────────
Click "Get Your Clinic Portal"  signup_started
Click "Book a Demo"             demo_requested
Scroll to pricing section       pricing_viewed
Read blog post                  blog_read
Submit form                     form_submission

✅ Check: Events appear in Network tab
✅ Check: Event names are correct
```

---

## 🔒 Security Section

```
┌─────────────────────────────────────────────┐
│  Healthcare-grade security and compliance   │
│                                             │
│  [HIPAA] [GDPR] [SOC 2] [ISO 27001]       │
│                                             │
│  ✓ Bank-level 256-bit encryption           │
│  ✓ Regular security audits                 │
│  ✓ Automated daily backups                 │
│  ✓ 99.9% uptime SLA                        │
│  ✓ Role-based access control               │
│  ✓ Audit trail logging                     │
└─────────────────────────────────────────────┘

✅ Check: HIPAA mentioned
✅ Check: GDPR mentioned
✅ Check: Security badges displayed
✅ Check: Trust indicators listed
```

---

## 🌐 Cross-Browser Testing

### Chrome
```
✅ All sections render
✅ Images load
✅ CTAs clickable
✅ Blog works
✅ Analytics tracks
```

### Firefox
```
✅ Same checks as Chrome
✅ No Firefox-specific issues
```

### Safari
```
✅ Same checks as Chrome
✅ No Safari-specific issues
```

---

## 🎯 Quick Test Checklist

### 5-Minute Quick Test
```
□ Open http://localhost:3000
□ Verify hero uses "clinic", "patients"
□ Scroll through all sections
□ Navigate to /blog
□ Click on a blog post
□ Test mobile view (F12 → Toggle device)
```

### 15-Minute Critical Test
```
□ Run automated verification script
□ Test home page content
□ Test blog system (listing, detail, search)
□ Test mobile responsiveness
□ Check analytics in Network tab
```

### 30-Minute Complete Test
```
□ All 5-minute tests
□ All 15-minute tests
□ Test on 2+ browsers
□ Verify SEO metadata
□ Check sitemap and robots.txt
□ Test all CTA buttons
```

---

## 📊 Expected Results Summary

### Content
- ✅ Healthcare terminology throughout
- ✅ 6 features, all healthcare-focused
- ✅ 5 testimonials with metrics
- ✅ 3 pricing tiers with healthcare features

### Blog
- ✅ 11 blog posts visible
- ✅ No draft posts
- ✅ No scheduled posts
- ✅ Search works
- ✅ Related posts display

### Mobile
- ✅ Responsive on 375px (iPhone SE)
- ✅ Responsive on 768px (iPad)
- ✅ Text readable (16px min)
- ✅ CTA buttons 44px height

### Analytics
- ✅ GA4 script loads
- ✅ Page views tracked
- ✅ CTA clicks tracked
- ✅ Custom events sent

### SEO
- ✅ Meta tags present
- ✅ Open Graph tags present
- ✅ Structured data present
- ✅ Sitemap includes all posts

---

## 🚀 Quick Commands

```bash
# Run automated verification
npx tsx scripts/manual-test-verification.ts

# Start dev server
yarn dev

# Build for production
yarn build

# Run tests
yarn test
```

---

**Last Updated:** December 2024  
**Status:** ✅ Ready for Testing
