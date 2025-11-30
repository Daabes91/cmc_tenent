# Production Deployment Guide

This guide covers the complete process for deploying the SaaS Landing Page to production.

## Table of Contents

1. [Pre-Deployment Checklist](#pre-deployment-checklist)
2. [Environment Variables Setup](#environment-variables-setup)
3. [Deployment Options](#deployment-options)
4. [Google Search Console Setup](#google-search-console-setup)
5. [Post-Deployment Verification](#post-deployment-verification)
6. [Monitoring Setup](#monitoring-setup)
7. [Rollback Procedures](#rollback-procedures)

---

## Pre-Deployment Checklist

Before deploying to production, ensure the following:

### Code Quality
- [ ] All tests pass (`pnpm test:run`)
- [ ] Build succeeds locally (`pnpm build`)
- [ ] No console errors in production build
- [ ] All images are optimized
- [ ] Blog posts are reviewed and published

### Content Review
- [ ] Healthcare-specific content is accurate
- [ ] All CTAs link to correct destinations
- [ ] Testimonials are authentic and approved
- [ ] Pricing information is current
- [ ] Legal pages (Privacy Policy, Terms) are linked

### SEO Configuration
- [ ] Meta titles and descriptions are unique
- [ ] Open Graph images are set
- [ ] Structured data is implemented
- [ ] Sitemap includes all pages
- [ ] Robots.txt allows crawling

### Analytics Setup
- [ ] Google Analytics 4 measurement ID is configured
- [ ] Conversion events are tested
- [ ] Google Search Console verification code is ready

---

## Environment Variables Setup

### Required Production Environment Variables

Create a `.env.production` file or configure these in your hosting platform:

```bash
# API Configuration
NEXT_PUBLIC_API_BASE_URL=https://api.yourdomain.com

# Application Configuration
NEXT_PUBLIC_APP_URL=https://yourdomain.com

# PayPal Configuration (Production)
NEXT_PUBLIC_PAYPAL_CLIENT_ID=your-production-paypal-client-id

# Google Analytics 4
NEXT_PUBLIC_GA_MEASUREMENT_ID=G-XXXXXXXXXX

# Google Search Console
NEXT_PUBLIC_GSC_VERIFICATION=your-google-site-verification-code

# Site Configuration
NEXT_PUBLIC_SITE_URL=https://yourdomain.com
NEXT_PUBLIC_SITE_NAME=ClinicPro

# Optional: Sentry for Error Monitoring
NEXT_PUBLIC_SENTRY_DSN=your-sentry-dsn
SENTRY_AUTH_TOKEN=your-sentry-auth-token
```

### Environment Variable Descriptions

| Variable | Description | Required |
|----------|-------------|----------|
| `NEXT_PUBLIC_API_BASE_URL` | Backend API URL | Yes |
| `NEXT_PUBLIC_APP_URL` | Frontend application URL | Yes |
| `NEXT_PUBLIC_PAYPAL_CLIENT_ID` | PayPal production client ID | Yes |
| `NEXT_PUBLIC_GA_MEASUREMENT_ID` | Google Analytics 4 measurement ID | Yes |
| `NEXT_PUBLIC_GSC_VERIFICATION` | Google Search Console verification code | Yes |
| `NEXT_PUBLIC_SITE_URL` | Canonical site URL for SEO | Yes |
| `NEXT_PUBLIC_SITE_NAME` | Site name for branding | Yes |
| `NEXT_PUBLIC_SENTRY_DSN` | Sentry error tracking DSN | No |

---

## Deployment Options

### Option 1: Vercel (Recommended)

Vercel is the recommended platform for Next.js applications.

#### Steps:

1. **Install Vercel CLI**
   ```bash
   npm i -g vercel
   ```

2. **Login to Vercel**
   ```bash
   vercel login
   ```

3. **Deploy from Project Root**
   ```bash
   cd apps/saas-landing
   vercel --prod
   ```

4. **Configure Environment Variables**
   - Go to Vercel Dashboard → Project Settings → Environment Variables
   - Add all required production environment variables
   - Redeploy to apply changes

#### Vercel Configuration

Create `vercel.json` in `apps/saas-landing/`:

```json
{
  "buildCommand": "pnpm build",
  "outputDirectory": ".next",
  "framework": "nextjs",
  "regions": ["iad1"],
  "env": {
    "NEXT_PUBLIC_API_BASE_URL": "@api-base-url",
    "NEXT_PUBLIC_APP_URL": "@app-url",
    "NEXT_PUBLIC_PAYPAL_CLIENT_ID": "@paypal-client-id",
    "NEXT_PUBLIC_GA_MEASUREMENT_ID": "@ga-measurement-id",
    "NEXT_PUBLIC_GSC_VERIFICATION": "@gsc-verification",
    "NEXT_PUBLIC_SITE_URL": "@site-url",
    "NEXT_PUBLIC_SITE_NAME": "@site-name"
  }
}
```

### Option 2: Docker Deployment

Use the provided Dockerfile for containerized deployment.

#### Steps:

1. **Build Docker Image**
   ```bash
   cd apps/saas-landing
   docker build \
     --build-arg NEXT_PUBLIC_API_BASE_URL=https://api.yourdomain.com \
     --build-arg NEXT_PUBLIC_APP_URL=https://yourdomain.com \
     --build-arg NEXT_PUBLIC_PAYPAL_CLIENT_ID=your-paypal-client-id \
     -t saas-landing:latest .
   ```

2. **Run Container**
   ```bash
   docker run -d \
     -p 3003:3003 \
     --name saas-landing \
     -e NEXT_PUBLIC_GA_MEASUREMENT_ID=G-XXXXXXXXXX \
     -e NEXT_PUBLIC_GSC_VERIFICATION=your-verification-code \
     -e NEXT_PUBLIC_SITE_URL=https://yourdomain.com \
     -e NEXT_PUBLIC_SITE_NAME=ClinicPro \
     saas-landing:latest
   ```

3. **Verify Container**
   ```bash
   docker logs saas-landing
   curl http://localhost:3003
   ```

### Option 3: DigitalOcean App Platform

#### Steps:

1. **Create App**
   - Go to DigitalOcean Dashboard → Apps → Create App
   - Connect your GitHub repository
   - Select `apps/saas-landing` as the source directory

2. **Configure Build Settings**
   - Build Command: `pnpm build`
   - Run Command: `pnpm start -p 3003`
   - HTTP Port: `3003`

3. **Add Environment Variables**
   - Add all required production environment variables in the App Settings

4. **Deploy**
   - Click "Deploy" and wait for the build to complete

### Option 4: AWS (EC2 + Docker)

#### Steps:

1. **Launch EC2 Instance**
   - AMI: Amazon Linux 2
   - Instance Type: t3.medium (minimum)
   - Security Group: Allow HTTP (80), HTTPS (443), SSH (22)

2. **Install Docker**
   ```bash
   sudo yum update -y
   sudo yum install docker -y
   sudo service docker start
   sudo usermod -a -G docker ec2-user
   ```

3. **Deploy Application**
   ```bash
   # Pull or build your Docker image
   docker pull your-registry/saas-landing:latest
   
   # Run container
   docker run -d \
     -p 80:3003 \
     --restart unless-stopped \
     --name saas-landing \
     -e NEXT_PUBLIC_API_BASE_URL=https://api.yourdomain.com \
     -e NEXT_PUBLIC_APP_URL=https://yourdomain.com \
     -e NEXT_PUBLIC_PAYPAL_CLIENT_ID=your-paypal-client-id \
     -e NEXT_PUBLIC_GA_MEASUREMENT_ID=G-XXXXXXXXXX \
     -e NEXT_PUBLIC_GSC_VERIFICATION=your-verification-code \
     -e NEXT_PUBLIC_SITE_URL=https://yourdomain.com \
     -e NEXT_PUBLIC_SITE_NAME=ClinicPro \
     your-registry/saas-landing:latest
   ```

4. **Configure Nginx (Optional)**
   ```nginx
   server {
       listen 80;
       server_name yourdomain.com;
       
       location / {
           proxy_pass http://localhost:3003;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection 'upgrade';
           proxy_set_header Host $host;
           proxy_cache_bypass $http_upgrade;
       }
   }
   ```

---

## Google Search Console Setup

### 1. Verify Site Ownership

#### Method A: HTML Tag Verification (Recommended)

1. Go to [Google Search Console](https://search.google.com/search-console)
2. Click "Add Property" → Enter your domain
3. Select "HTML tag" verification method
4. Copy the verification code (e.g., `google-site-verification=ABC123...`)
5. Add to your `.env.production`:
   ```bash
   NEXT_PUBLIC_GSC_VERIFICATION=ABC123...
   ```
6. Deploy your application
7. Return to Search Console and click "Verify"

The verification meta tag is automatically added to your site's `<head>` via the layout component.

#### Method B: DNS Verification

1. Select "DNS record" verification method
2. Add the TXT record to your domain's DNS settings
3. Wait for DNS propagation (up to 48 hours)
4. Click "Verify" in Search Console

### 2. Submit Sitemap

After verification:

1. In Google Search Console, go to "Sitemaps" in the left sidebar
2. Enter your sitemap URL: `https://yourdomain.com/sitemap.xml`
3. Click "Submit"
4. Wait for Google to crawl your sitemap (can take 24-48 hours)

### 3. Monitor Indexing

- Check "Coverage" report to see indexed pages
- Review "Enhancements" for any issues
- Monitor "Performance" for search analytics

---

## Post-Deployment Verification

### Automated Verification Script

Run the verification script after deployment:

```bash
cd apps/saas-landing
pnpm run verify:production
```

### Manual Verification Checklist

#### 1. Site Accessibility
- [ ] Homepage loads correctly
- [ ] All navigation links work
- [ ] Blog listing page displays posts
- [ ] Individual blog posts load
- [ ] Signup page is accessible
- [ ] Payment confirmation page works

#### 2. SEO Elements
- [ ] View page source and verify meta tags
- [ ] Check `https://yourdomain.com/sitemap.xml` loads
- [ ] Check `https://yourdomain.com/robots.txt` loads
- [ ] Verify Google Search Console verification tag is present
- [ ] Test structured data with [Rich Results Test](https://search.google.com/test/rich-results)

#### 3. Analytics Tracking
- [ ] Open Google Analytics Real-Time report
- [ ] Navigate through the site
- [ ] Verify page views are tracked
- [ ] Click CTA buttons and verify events are tracked
- [ ] Check conversion events appear in GA4

#### 4. Performance
- [ ] Run [PageSpeed Insights](https://pagespeed.web.dev/)
- [ ] Target: Performance score > 90
- [ ] Check Core Web Vitals:
  - LCP (Largest Contentful Paint) < 2.5s
  - FID (First Input Delay) < 100ms
  - CLS (Cumulative Layout Shift) < 0.1

#### 5. Mobile Responsiveness
- [ ] Test on mobile device or Chrome DevTools
- [ ] Verify all sections are readable
- [ ] Check CTA buttons are touch-friendly
- [ ] Test navigation menu on mobile

#### 6. Content Verification
- [ ] All healthcare terminology is correct
- [ ] Testimonials display properly
- [ ] Pricing information is accurate
- [ ] Blog posts render correctly
- [ ] Images load and are optimized

#### 7. Functionality
- [ ] Blog search works
- [ ] Related posts appear
- [ ] Social sharing buttons work
- [ ] Form submissions work (if applicable)
- [ ] PayPal integration works (test mode)

---

## Monitoring Setup

### Google Analytics 4 Dashboard

1. **Create Custom Dashboard**
   - Go to GA4 → Reports → Library → Create new report
   - Add key metrics:
     - Page views
     - User sessions
     - Conversion events
     - Bounce rate
     - Average session duration

2. **Set Up Alerts**
   - Go to Admin → Custom Alerts
   - Create alerts for:
     - Traffic drops > 50%
     - Conversion rate drops > 30%
     - Error rate increases > 5%

### Error Monitoring (Sentry)

If using Sentry:

1. **Configure Sentry**
   ```bash
   # Install Sentry
   pnpm add @sentry/nextjs
   
   # Initialize
   npx @sentry/wizard@latest -i nextjs
   ```

2. **Add to Environment Variables**
   ```bash
   NEXT_PUBLIC_SENTRY_DSN=your-sentry-dsn
   SENTRY_AUTH_TOKEN=your-sentry-auth-token
   ```

3. **Monitor Errors**
   - Check Sentry dashboard daily
   - Set up alerts for critical errors
   - Review error trends weekly

### Uptime Monitoring

Use a service like:
- **UptimeRobot** (free): https://uptimerobot.com
- **Pingdom**: https://www.pingdom.com
- **StatusCake**: https://www.statuscake.com

Configure to check your site every 5 minutes and alert on downtime.

### Performance Monitoring

1. **Set Up Lighthouse CI**
   ```bash
   npm install -g @lhci/cli
   lhci autorun --upload.target=temporary-public-storage
   ```

2. **Monitor Core Web Vitals**
   - Use Google Search Console → Core Web Vitals report
   - Set up alerts for poor URLs

---

## First 48 Hours Monitoring

### Hour 0-2: Immediate Checks
- [ ] Verify deployment succeeded
- [ ] Check all pages load
- [ ] Test critical user flows
- [ ] Monitor error logs
- [ ] Check GA4 real-time data

### Hour 2-12: Initial Monitoring
- [ ] Review GA4 for traffic patterns
- [ ] Check for any error spikes
- [ ] Monitor server resources (CPU, memory)
- [ ] Verify sitemap submission status
- [ ] Check for any broken links

### Hour 12-24: First Day Review
- [ ] Analyze user behavior in GA4
- [ ] Review conversion events
- [ ] Check blog post engagement
- [ ] Monitor page load times
- [ ] Review any user feedback

### Hour 24-48: Stabilization
- [ ] Compare metrics to baseline
- [ ] Identify any performance issues
- [ ] Review search console for crawl errors
- [ ] Check indexing status
- [ ] Plan any necessary optimizations

### Monitoring Checklist

Create a daily monitoring routine:

**Daily (First Week)**
- [ ] Check GA4 dashboard
- [ ] Review error logs
- [ ] Monitor uptime status
- [ ] Check Core Web Vitals
- [ ] Review user feedback

**Weekly (Ongoing)**
- [ ] Analyze traffic trends
- [ ] Review conversion rates
- [ ] Check SEO rankings
- [ ] Monitor page speed
- [ ] Review blog performance

**Monthly**
- [ ] Comprehensive analytics review
- [ ] SEO performance analysis
- [ ] Content performance review
- [ ] Technical health check
- [ ] Plan improvements

---

## Rollback Procedures

### Vercel Rollback

1. Go to Vercel Dashboard → Deployments
2. Find the last working deployment
3. Click "..." → "Promote to Production"
4. Confirm rollback

### Docker Rollback

```bash
# Stop current container
docker stop saas-landing
docker rm saas-landing

# Run previous version
docker run -d \
  -p 3003:3003 \
  --name saas-landing \
  [environment variables] \
  saas-landing:previous-tag
```

### Git Rollback

```bash
# Revert to previous commit
git revert HEAD
git push origin main

# Or reset to specific commit
git reset --hard <commit-hash>
git push --force origin main
```

---

## Troubleshooting

### Common Issues

#### Issue: Site Not Loading
**Solution:**
- Check DNS settings
- Verify SSL certificate
- Check server logs
- Verify environment variables

#### Issue: Analytics Not Tracking
**Solution:**
- Verify GA4 measurement ID
- Check browser console for errors
- Ensure analytics script loads
- Test in incognito mode

#### Issue: Sitemap Not Found
**Solution:**
- Verify build completed successfully
- Check `app/sitemap.ts` exists
- Rebuild and redeploy
- Clear CDN cache

#### Issue: Images Not Loading
**Solution:**
- Check image paths
- Verify image optimization settings
- Check CDN configuration
- Review Next.js image config

---

## Support and Resources

### Documentation
- [Next.js Deployment](https://nextjs.org/docs/deployment)
- [Vercel Documentation](https://vercel.com/docs)
- [Google Analytics 4](https://support.google.com/analytics/answer/10089681)
- [Google Search Console](https://support.google.com/webmasters)

### Internal Documentation
- [SEO Quick Reference](./SEO_QUICK_START.md)
- [Analytics Quick Reference](./ANALYTICS_QUICK_REFERENCE.md)
- [Monitoring System Overview](./MONITORING_SYSTEM_OVERVIEW.md)
- [Error Handling Guide](./ERROR_HANDLING_GUIDE.md)

### Contact
For deployment issues, contact:
- DevOps Team: devops@yourdomain.com
- Technical Lead: tech@yourdomain.com

---

## Deployment Checklist Summary

Use this quick checklist for each deployment:

- [ ] All tests pass
- [ ] Build succeeds locally
- [ ] Environment variables configured
- [ ] Content reviewed and approved
- [ ] SEO elements verified
- [ ] Analytics configured
- [ ] Deployment executed
- [ ] Site accessibility verified
- [ ] Analytics tracking confirmed
- [ ] Sitemap submitted to GSC
- [ ] Performance tested
- [ ] Monitoring set up
- [ ] Team notified
- [ ] Documentation updated

---

**Last Updated:** [Current Date]
**Version:** 1.0.0
