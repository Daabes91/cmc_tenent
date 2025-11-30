# Google Search Console Setup Guide

This guide explains how to configure and use Google Search Console (GSC) for the SaaS landing page.

## Overview

Google Search Console is configured with:
- ✅ Verification meta tag in the root layout
- ✅ Automatic XML sitemap generation
- ✅ Robots.txt configuration
- ✅ Structured data markup (Organization, SoftwareApplication, BlogPosting)

## Configuration Steps

### 1. Get Your Verification Code

1. Go to [Google Search Console](https://search.google.com/search-console)
2. Click "Add Property"
3. Enter your domain (e.g., `https://yourdomain.com`)
4. Choose "HTML tag" verification method
5. Copy the verification code from the meta tag (the part after `content="`)

Example meta tag from Google:
```html
<meta name="google-site-verification" content="abc123xyz456..." />
```

Copy only the content value: `abc123xyz456...`

### 2. Add Verification Code to Environment

Add the verification code to your `.env.local` file:

```bash
NEXT_PUBLIC_GSC_VERIFICATION=abc123xyz456...
```

For production, add it to your deployment platform's environment variables.

### 3. Verify Ownership

1. Deploy your site with the verification code
2. Return to Google Search Console
3. Click "Verify"
4. Wait for Google to confirm verification (usually instant)

### 4. Submit Sitemap

Once verified:

1. In Google Search Console, go to "Sitemaps" in the left sidebar
2. Enter your sitemap URL: `https://yourdomain.com/sitemap.xml`
3. Click "Submit"
4. Wait for Google to process (can take a few hours to days)

## Features

### Automatic Sitemap Generation

The sitemap is automatically generated at `/sitemap.xml` and includes:

- **Homepage** (priority: 1.0, weekly updates)
- **Signup page** (priority: 0.9, monthly updates)
- **Blog listing** (priority: 0.8, daily updates)
- **All published blog posts** (priority: 0.7, monthly updates)

The sitemap automatically:
- Excludes draft posts
- Excludes scheduled posts (not yet published)
- Uses the correct last modified dates
- Updates dynamically when new posts are added

### Robots.txt Configuration

The robots.txt file is automatically served at `/robots.txt` and:

- Allows all search engines to crawl the site
- Disallows crawling of:
  - `/api/` (API endpoints)
  - `/payment-confirmation/` (private pages)
  - `/_next/` (Next.js internal files)
  - `/admin/` (admin pages)
- Points to the sitemap location

### Structured Data Markup

Three types of structured data are implemented:

#### 1. Organization Schema (Homepage)

Located on the homepage, describes the company:

```json
{
  "@context": "https://schema.org",
  "@type": "Organization",
  "name": "ClinicPro",
  "url": "https://yourdomain.com",
  "logo": "https://yourdomain.com/brand-logo.png",
  "description": "All-in-one clinic management platform",
  "contactPoint": {
    "@type": "ContactPoint",
    "contactType": "Sales",
    "email": "sales@yourdomain.com"
  }
}
```

#### 2. SoftwareApplication Schema (Homepage)

Also on the homepage, describes the software product:

```json
{
  "@context": "https://schema.org",
  "@type": "SoftwareApplication",
  "name": "ClinicPro",
  "applicationCategory": "BusinessApplication",
  "operatingSystem": "Web Browser",
  "offers": {
    "@type": "Offer",
    "price": "49",
    "priceCurrency": "USD"
  },
  "description": "Comprehensive clinic management software..."
}
```

#### 3. BlogPosting Schema (Blog Posts)

On each blog post page, describes the article:

```json
{
  "@context": "https://schema.org",
  "@type": "BlogPosting",
  "headline": "Post Title",
  "description": "Post excerpt",
  "image": "https://yourdomain.com/images/post.jpg",
  "author": {
    "@type": "Person",
    "name": "Author Name"
  },
  "publisher": {
    "@type": "Organization",
    "name": "ClinicPro",
    "logo": {
      "@type": "ImageObject",
      "url": "https://yourdomain.com/brand-logo.png"
    }
  },
  "datePublished": "2024-01-15T00:00:00.000Z",
  "dateModified": "2024-01-20T00:00:00.000Z"
}
```

## Environment Variables

Required environment variables:

```bash
# Google Search Console verification code
NEXT_PUBLIC_GSC_VERIFICATION=your-verification-code

# Site configuration (used in structured data and sitemap)
NEXT_PUBLIC_SITE_URL=https://yourdomain.com
NEXT_PUBLIC_SITE_NAME=ClinicPro

# Optional: Sales email for Organization schema
SALES_EMAIL=sales@yourdomain.com
```

## Testing

### Test Robots.txt

Visit: `https://yourdomain.com/robots.txt`

Expected output:
```
User-agent: *
Allow: /
Disallow: /api/
Disallow: /payment-confirmation/
Disallow: /_next/
Disallow: /admin/

Sitemap: https://yourdomain.com/sitemap.xml
```

### Test Sitemap

Visit: `https://yourdomain.com/sitemap.xml`

You should see an XML file with all pages and blog posts.

### Test Structured Data

Use [Google's Rich Results Test](https://search.google.com/test/rich-results):

1. Enter your homepage URL
2. Check for Organization and SoftwareApplication schemas
3. Enter a blog post URL
4. Check for BlogPosting schema

All schemas should show "Valid" with no errors.

### Test Verification Meta Tag

View page source on your homepage and search for:
```html
<meta name="google-site-verification" content="your-code" />
```

## Monitoring

### Key Metrics to Track

In Google Search Console, monitor:

1. **Performance**
   - Total clicks
   - Total impressions
   - Average CTR (Click-Through Rate)
   - Average position

2. **Coverage**
   - Valid pages indexed
   - Pages with errors
   - Pages excluded

3. **Enhancements**
   - Mobile usability issues
   - Core Web Vitals
   - Structured data errors

4. **Sitemaps**
   - Pages discovered
   - Pages indexed
   - Last read date

### Common Issues

#### Verification Failed
- Check that the verification code is correct
- Ensure the site is deployed and accessible
- Wait a few minutes and try again

#### Sitemap Not Found
- Verify the sitemap URL is correct
- Check that the site is deployed
- Ensure no firewall is blocking Google's crawlers

#### Structured Data Errors
- Use the Rich Results Test to identify specific errors
- Check that all required fields are present
- Verify URLs are absolute (not relative)

#### Pages Not Indexed
- Check robots.txt isn't blocking pages
- Verify pages are in the sitemap
- Ensure pages have unique, descriptive content
- Check for canonical URL issues

## Best Practices

### 1. Regular Monitoring
- Check GSC weekly for new issues
- Monitor search performance trends
- Review coverage reports for indexing issues

### 2. Content Updates
- Update blog posts regularly
- Add new content consistently
- Fix any broken links or 404 errors

### 3. Mobile Optimization
- Ensure all pages pass mobile usability tests
- Monitor Core Web Vitals
- Fix any mobile-specific issues

### 4. Structured Data Maintenance
- Keep structured data up to date
- Add new schema types as needed
- Test after any major changes

## Troubleshooting

### Issue: Verification Code Not Working

**Solution:**
1. Clear browser cache
2. Verify the code is in the `<head>` section
3. Check for typos in the verification code
4. Ensure the site is publicly accessible

### Issue: Sitemap Shows 0 Pages

**Solution:**
1. Check that blog posts exist and are published
2. Verify `NEXT_PUBLIC_SITE_URL` is set correctly
3. Rebuild and redeploy the site
4. Wait 24 hours for Google to re-crawl

### Issue: Structured Data Not Detected

**Solution:**
1. View page source and verify JSON-LD is present
2. Use the Rich Results Test tool
3. Check for JavaScript errors in console
4. Ensure scripts are not blocked by CSP

## Additional Resources

- [Google Search Console Help](https://support.google.com/webmasters)
- [Structured Data Testing Tool](https://search.google.com/test/rich-results)
- [Schema.org Documentation](https://schema.org/)
- [Next.js Metadata Documentation](https://nextjs.org/docs/app/building-your-application/optimizing/metadata)

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review Google Search Console help documentation
3. Test with Google's validation tools
4. Contact the development team
