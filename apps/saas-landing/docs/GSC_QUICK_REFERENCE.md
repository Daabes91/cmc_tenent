# Google Search Console - Quick Reference

## Quick Setup (5 Minutes)

1. **Get verification code** from [Google Search Console](https://search.google.com/search-console)
2. **Add to environment**: `NEXT_PUBLIC_GSC_VERIFICATION=your-code`
3. **Deploy** your site
4. **Verify** in Google Search Console
5. **Submit sitemap**: `https://yourdomain.com/sitemap.xml`

## Environment Variables

```bash
# Required
NEXT_PUBLIC_GSC_VERIFICATION=abc123xyz456
NEXT_PUBLIC_SITE_URL=https://yourdomain.com
NEXT_PUBLIC_SITE_NAME=ClinicPro

# Optional
SALES_EMAIL=sales@yourdomain.com
```

## URLs to Test

| URL | Purpose |
|-----|---------|
| `/robots.txt` | Crawling rules |
| `/sitemap.xml` | All pages and posts |
| `/` | Organization + Software schemas |
| `/blog/[slug]` | BlogPosting schema |

## Validation Tools

- **Verification**: View page source, search for `google-site-verification`
- **Robots.txt**: Visit `/robots.txt` directly
- **Sitemap**: Visit `/sitemap.xml` directly
- **Structured Data**: [Rich Results Test](https://search.google.com/test/rich-results)

## What's Included

### ✅ Verification Meta Tag
Automatically added to `<head>` on all pages via root layout.

### ✅ Robots.txt
- Allows all crawlers
- Blocks: `/api/`, `/payment-confirmation/`, `/_next/`, `/admin/`
- Points to sitemap

### ✅ XML Sitemap
Auto-generated with:
- Homepage (priority 1.0)
- Signup page (priority 0.9)
- Blog listing (priority 0.8)
- All published blog posts (priority 0.7)

### ✅ Structured Data

**Homepage:**
- Organization schema (company info)
- SoftwareApplication schema (product info)

**Blog Posts:**
- BlogPosting schema (article info)

## Common Commands

```bash
# Test locally
npm run dev
# Visit http://localhost:3000/robots.txt
# Visit http://localhost:3000/sitemap.xml

# Build for production
npm run build

# Deploy
npm run start
```

## Monitoring Checklist

Weekly checks in Google Search Console:

- [ ] Check coverage for indexing errors
- [ ] Review performance metrics (clicks, impressions)
- [ ] Monitor Core Web Vitals
- [ ] Check for mobile usability issues
- [ ] Review structured data enhancements
- [ ] Verify sitemap is being processed

## Quick Fixes

| Issue | Solution |
|-------|----------|
| Verification failed | Check code, redeploy, wait 5 min |
| Sitemap not found | Verify URL, check deployment |
| Pages not indexed | Check robots.txt, wait 24-48 hours |
| Schema errors | Use Rich Results Test, fix JSON-LD |

## Key Files

```
apps/saas-landing/
├── app/
│   ├── layout.tsx              # GSC verification meta tag
│   ├── robots.ts               # Robots.txt generation
│   ├── sitemap.ts              # Sitemap generation
│   ├── page.tsx                # Organization + Software schemas
│   └── blog/[slug]/page.tsx    # BlogPosting schema
├── lib/
│   └── seo/
│       └── structured-data.ts  # Schema generation utilities
└── docs/
    ├── GOOGLE_SEARCH_CONSOLE_SETUP.md  # Full guide
    └── GSC_QUICK_REFERENCE.md          # This file
```

## Support Links

- [GSC Dashboard](https://search.google.com/search-console)
- [Rich Results Test](https://search.google.com/test/rich-results)
- [Schema.org](https://schema.org/)
- [Full Setup Guide](./GOOGLE_SEARCH_CONSOLE_SETUP.md)
