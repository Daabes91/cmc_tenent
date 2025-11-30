# Google Search Console Submission Guide

This guide walks you through the complete process of setting up and submitting your site to Google Search Console.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Site Verification](#site-verification)
3. [Sitemap Submission](#sitemap-submission)
4. [Monitoring and Optimization](#monitoring-and-optimization)
5. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before starting, ensure:

- [ ] Your site is deployed and accessible
- [ ] You have a Google account
- [ ] Your `NEXT_PUBLIC_GSC_VERIFICATION` environment variable is set
- [ ] Your site is live at the production URL

---

## Site Verification

### Step 1: Access Google Search Console

1. Go to [Google Search Console](https://search.google.com/search-console)
2. Sign in with your Google account
3. Click **"Add Property"**

### Step 2: Choose Property Type

You have two options:

#### Option A: Domain Property (Recommended)
- Covers all subdomains and protocols (http/https)
- Requires DNS verification
- Example: `yourdomain.com`

#### Option B: URL Prefix Property
- Covers only the specific URL
- Multiple verification methods available
- Example: `https://yourdomain.com`

**For this guide, we'll use URL Prefix Property with HTML tag verification.**

### Step 3: Enter Your Site URL

1. Select **"URL prefix"**
2. Enter your full site URL: `https://yourdomain.com`
3. Click **"Continue"**

### Step 4: Verify Ownership - HTML Tag Method

1. In the verification methods, select **"HTML tag"**
2. You'll see a meta tag like:
   ```html
   <meta name="google-site-verification" content="ABC123XYZ..." />
   ```
3. Copy the `content` value (e.g., `ABC123XYZ...`)

### Step 5: Add Verification Code to Your Site

#### If Not Yet Deployed:

1. Open your `.env.production` file
2. Add or update:
   ```bash
   NEXT_PUBLIC_GSC_VERIFICATION=ABC123XYZ...
   ```
3. Deploy your site with this environment variable

#### If Already Deployed:

**For Vercel:**
1. Go to Vercel Dashboard → Your Project → Settings → Environment Variables
2. Add `NEXT_PUBLIC_GSC_VERIFICATION` with your verification code
3. Redeploy your application

**For Docker:**
1. Add the environment variable to your docker run command:
   ```bash
   -e NEXT_PUBLIC_GSC_VERIFICATION=ABC123XYZ...
   ```
2. Restart your container

**For Other Platforms:**
1. Add the environment variable to your hosting platform
2. Redeploy your application

### Step 6: Verify the Tag is Present

1. Visit your site: `https://yourdomain.com`
2. Right-click → "View Page Source"
3. Search for `google-site-verification`
4. Confirm the meta tag is present in the `<head>` section

### Step 7: Complete Verification

1. Return to Google Search Console
2. Click **"Verify"**
3. If successful, you'll see: "Ownership verified"

**Note:** If verification fails:
- Wait a few minutes for DNS/CDN cache to clear
- Ensure the meta tag is in the `<head>` section
- Check that the verification code matches exactly
- Try the verification again

---

## Sitemap Submission

### Step 1: Verify Sitemap is Accessible

1. Visit: `https://yourdomain.com/sitemap.xml`
2. Confirm you see XML content with your pages listed
3. Check that blog posts are included

Example sitemap structure:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <url>
    <loc>https://yourdomain.com</loc>
    <lastmod>2024-01-15</lastmod>
    <changefreq>weekly</changefreq>
    <priority>1.0</priority>
  </url>
  <url>
    <loc>https://yourdomain.com/blog</loc>
    <lastmod>2024-01-15</lastmod>
    <changefreq>daily</changefreq>
    <priority>0.8</priority>
  </url>
  <!-- More URLs... -->
</urlset>
```

### Step 2: Submit Sitemap to Google Search Console

1. In Google Search Console, select your property
2. Click **"Sitemaps"** in the left sidebar
3. Under "Add a new sitemap", enter: `sitemap.xml`
4. Click **"Submit"**

### Step 3: Verify Submission

1. You should see your sitemap listed with status "Success"
2. Initial status might be "Couldn't fetch" - this is normal
3. Google will crawl your sitemap within 24-48 hours

### Step 4: Monitor Sitemap Status

Check back after 24-48 hours:
- **Status: Success** - Sitemap is being processed
- **Discovered URLs** - Number of URLs found in sitemap
- **Indexed URLs** - Number of URLs indexed by Google

---

## Monitoring and Optimization

### Coverage Report

Monitor which pages are indexed:

1. Go to **"Coverage"** in the left sidebar
2. Review:
   - **Valid**: Pages successfully indexed
   - **Valid with warnings**: Indexed but with issues
   - **Error**: Pages not indexed due to errors
   - **Excluded**: Pages intentionally not indexed

### Common Issues and Fixes

#### Issue: "Submitted URL not found (404)"
**Fix:**
- Verify the URL exists and is accessible
- Check for typos in the sitemap
- Ensure the page is not behind authentication

#### Issue: "Submitted URL marked 'noindex'"
**Fix:**
- Check your page's meta tags
- Ensure no `<meta name="robots" content="noindex">` is present
- Review your robots.txt file

#### Issue: "Crawled - currently not indexed"
**Fix:**
- Improve page content quality
- Add more internal links to the page
- Ensure page loads quickly
- Wait - Google may index it later

### Performance Report

Monitor search performance:

1. Go to **"Performance"** in the left sidebar
2. View metrics:
   - **Total clicks**: Users clicking your site in search results
   - **Total impressions**: How often your site appears in search
   - **Average CTR**: Click-through rate
   - **Average position**: Your average ranking position

### Enhancement Reports

Check for structured data issues:

1. Go to **"Enhancements"** in the left sidebar
2. Review reports for:
   - **Breadcrumbs**
   - **Logo**
   - **Sitelinks searchbox**
   - **Article** (for blog posts)

### URL Inspection Tool

Test individual URLs:

1. Click the search bar at the top
2. Enter a URL from your site
3. Click **"Test Live URL"**
4. Review:
   - Indexing status
   - Coverage
   - Mobile usability
   - Structured data

---

## First 48 Hours Monitoring Checklist

### Hour 0-2: Immediate Verification
- [ ] Sitemap submitted successfully
- [ ] No immediate errors in Search Console
- [ ] Verification tag is present on site
- [ ] Robots.txt is accessible

### Hour 2-12: Initial Crawling
- [ ] Check Coverage report for crawl activity
- [ ] Monitor for any crawl errors
- [ ] Verify sitemap status changes from "Pending" to "Success"

### Hour 12-24: First Indexing
- [ ] Check if homepage is indexed (search: `site:yourdomain.com`)
- [ ] Review Coverage report for indexed pages
- [ ] Check for any enhancement issues

### Hour 24-48: Stabilization
- [ ] Monitor indexed page count
- [ ] Review any warnings or errors
- [ ] Check Performance report for initial impressions
- [ ] Verify blog posts are being indexed

---

## Weekly Monitoring Routine

### Week 1-4: Active Monitoring

**Daily:**
- [ ] Check Coverage report for new issues
- [ ] Monitor indexed page count
- [ ] Review any critical errors

**Weekly:**
- [ ] Analyze Performance report
- [ ] Review top-performing pages
- [ ] Check for mobile usability issues
- [ ] Monitor Core Web Vitals
- [ ] Review enhancement reports

### Month 2+: Maintenance Monitoring

**Weekly:**
- [ ] Quick check of Coverage report
- [ ] Review Performance trends

**Monthly:**
- [ ] Comprehensive Performance analysis
- [ ] Review and fix any persistent issues
- [ ] Analyze search query data
- [ ] Optimize underperforming pages

---

## Advanced Features

### URL Parameters

If your site uses URL parameters:

1. Go to **"URL Parameters"** (under Legacy tools)
2. Configure how Google should handle parameters
3. Example: `?utm_source=` should be ignored

### International Targeting

If you have multiple language versions:

1. Go to **"International Targeting"**
2. Set your target country (if applicable)
3. Configure hreflang tags in your pages

### Manual Actions

Check for any manual penalties:

1. Go to **"Manual Actions"**
2. Should show: "No issues detected"
3. If issues exist, follow Google's guidance to fix

### Security Issues

Monitor for security problems:

1. Go to **"Security Issues"**
2. Should show: "No issues detected"
3. Address any issues immediately if found

---

## Optimization Tips

### Improve Indexing

1. **Create quality content**
   - Write comprehensive, valuable content
   - Use proper heading structure (H1, H2, H3)
   - Include relevant keywords naturally

2. **Improve internal linking**
   - Link related blog posts
   - Create a clear site structure
   - Use descriptive anchor text

3. **Optimize page speed**
   - Compress images
   - Minimize JavaScript
   - Use caching effectively

4. **Mobile optimization**
   - Ensure responsive design
   - Test on real devices
   - Check mobile usability report

### Improve Rankings

1. **Target relevant keywords**
   - Research healthcare-related keywords
   - Use keywords in titles and headings
   - Create content around user intent

2. **Build quality backlinks**
   - Guest post on healthcare blogs
   - Get listed in healthcare directories
   - Create shareable content

3. **Optimize meta descriptions**
   - Write compelling descriptions
   - Include target keywords
   - Keep under 160 characters

4. **Improve user experience**
   - Fast page load times
   - Clear navigation
   - Mobile-friendly design
   - Engaging content

---

## Troubleshooting

### Verification Failed

**Problem:** "Verification failed. Try again later."

**Solutions:**
1. Wait 5-10 minutes and try again
2. Clear your browser cache
3. Verify the meta tag is in the `<head>` section
4. Check that the verification code matches exactly
5. Try an alternative verification method (DNS, HTML file)

### Sitemap Not Found

**Problem:** "Sitemap could not be read"

**Solutions:**
1. Verify sitemap URL is correct: `https://yourdomain.com/sitemap.xml`
2. Check sitemap is accessible (not behind authentication)
3. Validate sitemap XML syntax
4. Ensure sitemap is not blocked by robots.txt
5. Check server logs for errors

### Pages Not Indexed

**Problem:** Pages submitted but not appearing in Google

**Solutions:**
1. Wait - indexing can take days or weeks
2. Check Coverage report for specific errors
3. Improve page content quality
4. Add more internal links to the page
5. Request indexing via URL Inspection tool
6. Ensure page is not blocked by robots.txt or noindex tag

### Mobile Usability Issues

**Problem:** "Mobile usability issues detected"

**Solutions:**
1. Review specific issues in Mobile Usability report
2. Test pages on real mobile devices
3. Fix common issues:
   - Text too small to read
   - Clickable elements too close together
   - Content wider than screen
   - Viewport not set
4. Retest with URL Inspection tool

---

## Resources

### Official Documentation
- [Google Search Console Help](https://support.google.com/webmasters)
- [SEO Starter Guide](https://developers.google.com/search/docs/beginner/seo-starter-guide)
- [Structured Data Guidelines](https://developers.google.com/search/docs/advanced/structured-data/intro-structured-data)

### Testing Tools
- [Rich Results Test](https://search.google.com/test/rich-results)
- [Mobile-Friendly Test](https://search.google.com/test/mobile-friendly)
- [PageSpeed Insights](https://pagespeed.web.dev/)

### Internal Documentation
- [SEO Quick Reference](./SEO_QUICK_START.md)
- [Production Deployment Guide](./PRODUCTION_DEPLOYMENT_GUIDE.md)
- [Analytics Quick Reference](./ANALYTICS_QUICK_REFERENCE.md)

---

## Support

For issues with Google Search Console:
- [Google Search Central Community](https://support.google.com/webmasters/community)
- [Google Search Console Twitter](https://twitter.com/googlewmc)

For site-specific issues:
- Contact your technical team
- Review deployment logs
- Check server error logs

---

**Last Updated:** [Current Date]
**Version:** 1.0.0
