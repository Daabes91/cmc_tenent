# Manual Testing Quick Reference

**Task 26: Manual Testing and Content Review**

## 🚀 Quick Start (5 minutes)

```bash
# 1. Run automated verification
cd apps/saas-landing
npx tsx scripts/manual-test-verification.ts

# 2. Start dev server
yarn dev

# 3. Open browser
http://localhost:3000
```

**Expected:** 93.8% pass rate (45/48 tests)

---

## ✅ Critical Tests (15 minutes)

### 1. Home Page Content (3 min)
- [ ] Hero uses "clinic", "patients", "providers"
- [ ] 6 features are healthcare-focused
- [ ] 5 testimonials with metrics
- [ ] 3 pricing tiers (Solo, Small, Multi-Location)
- [ ] Security mentions HIPAA/GDPR

### 2. Blog System (5 min)
- [ ] Navigate to `/blog` → 11 posts visible
- [ ] Click "reduce-patient-no-shows" → full article renders
- [ ] Scroll to bottom → related posts appear
- [ ] Search "appointment" → results display

### 3. Mobile Test (4 min)
- [ ] F12 → Toggle device toolbar (Ctrl+Shift+M)
- [ ] Select iPhone SE (375px)
- [ ] All content readable
- [ ] CTA buttons touch-friendly

### 4. Analytics (3 min)
- [ ] F12 → Network tab → Filter "google-analytics"
- [ ] Navigate pages → tracking requests sent
- [ ] Click CTA → event tracked

---

## 📊 Automated Verification Results

### ✅ Passed (45/48)
- Healthcare terminology ✓
- 5 testimonials with complete structure ✓
- 3 pricing tiers with healthcare features ✓
- 11 blog posts with metadata ✓
- 6 integrations with healthcare descriptions ✓
- HIPAA/GDPR compliance mentioned ✓

### ⚠️ Warnings (3/48)
- "dashboard" in "admin dashboard" (acceptable)
- "users" in technical context (acceptable)
- "saas" in meta context (acceptable)

---

## 🔍 SEO Quick Check (5 minutes)

### Home Page
```
1. Right-click → View Page Source (Ctrl+U)
2. Search for: <title>, <meta name="description">
3. Search for: og:title, og:description
4. Search for: application/ld+json
```

### Blog Post
```
1. Open: /blog/reduce-patient-no-shows
2. View source
3. Verify: Unique title and description
4. Verify: BlogPosting schema
```

### Sitemap
```
Navigate to: /sitemap.xml
Verify: All 11 posts included
```

---

## 📱 Mobile Responsive Check (5 minutes)

### iPhone SE (375px)
```
DevTools → Toggle device toolbar
Select: iPhone SE

Check:
- Text readable (16px minimum)
- CTA buttons 44px height
- No horizontal scroll
- Images load properly
```

### iPad (768px)
```
Select: iPad

Check:
- Two-column layouts
- Touch targets adequate
- Images scale properly
```

---

## 🎯 Blog System Tests

### Listing Page (`/blog`)
- [ ] 11 posts display
- [ ] Each has: title, excerpt, image, author, date
- [ ] Category filter works
- [ ] Search bar visible

### Detail Page (`/blog/[slug]`)
- [ ] Full article renders
- [ ] MDX formatting works
- [ ] Author info displays
- [ ] Social share buttons present
- [ ] Related posts at bottom

### Search
- [ ] Search "appointment" → results
- [ ] Search "patient" → results
- [ ] Search "xyz123" → no results message

---

## 📈 Analytics Verification

### Network Tab Method
```
1. F12 → Network tab
2. Filter: "google-analytics" or "gtag"
3. Navigate pages → verify requests
4. Click CTAs → verify events
```

### Events to Test
- [ ] "Get Your Clinic Portal" → `signup_started`
- [ ] "Book a Demo" → `demo_requested`
- [ ] Scroll to pricing → `pricing_viewed`
- [ ] Read blog post → `blog_read`

---

## 🌐 Cross-Browser Tests

### Chrome
- [ ] All sections render
- [ ] Images load
- [ ] CTAs clickable
- [ ] Blog works

### Firefox or Safari
- [ ] Same checks as Chrome
- [ ] No browser-specific issues

---

## 🔒 Security Section Check

- [ ] Security section visible on home page
- [ ] Mentions HIPAA compliance
- [ ] Mentions GDPR compliance
- [ ] Shows security badges
- [ ] Lists trust indicators (encryption, backups, etc.)

---

## 📝 Content Accuracy Checklist

### Healthcare Terminology
✅ **Use:** patient, appointment, clinic, provider, treatment, healthcare
❌ **Avoid:** workspace, projects, users (in marketing copy)

### Hero Section
- [ ] Headline: "Your All-in-One Platform to Run Your Clinic"
- [ ] CTA: "Get Your Clinic Portal"
- [ ] Description mentions clinic operations

### Features (6 total)
1. Your own clinic website
2. Smart Appointment Scheduling
3. Complete Patient Management
4. Automated Billing & Invoicing
5. Virtual Care Ready
6. Practice Analytics & Reporting

### Testimonials (5 total)
1. Dr. Sarah Johnson - Family Medicine
2. Michael Chen - Dental Clinic
3. Dr. Aisha Al-Rashid - Physical Therapy
4. Dr. Layla Cliniqax - Dental Group
5. Sameer Haddad - Health Network

### Pricing Tiers (3 total)
1. Solo Practice - $49/month
2. Small Clinic - $149/month (Popular)
3. Multi-Location - $399/month

---

## 🐛 Common Issues

### Blog Posts Not Showing
- Check `draft: false` in frontmatter
- Check `publishedAt` date is in past
- Check console for errors

### Analytics Not Tracking
- Verify GA4 measurement ID configured
- Disable ad blockers
- Check browser console

### Mobile Layout Issues
- Check CSS media queries
- Test with actual device
- Verify responsive classes

---

## ✅ Sign-off Checklist

- [ ] Automated verification: 93.8% pass rate
- [ ] Home page content reviewed
- [ ] Blog system tested end-to-end
- [ ] Analytics tracking verified
- [ ] Mobile responsiveness checked
- [ ] SEO metadata verified
- [ ] Cross-browser tested
- [ ] Security section verified

**Status:** ☐ Passed ☐ Failed ☐ Passed with Issues

---

## 📚 Full Documentation

- **Detailed Guide:** `docs/TASK_26_MANUAL_TESTING_GUIDE.md`
- **Complete Checklist:** `docs/MANUAL_TESTING_CHECKLIST.md`
- **Verification Script:** `scripts/manual-test-verification.ts`

---

**Last Updated:** December 2024  
**Estimated Time:** 30-45 minutes for complete testing
