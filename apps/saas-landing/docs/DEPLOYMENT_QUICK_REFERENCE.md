# Deployment Quick Reference

Quick commands and checklists for deploying the SaaS Landing Page to production.

## 🚀 Quick Deploy

### 1. Prepare Environment
```bash
cd apps/saas-landing
cp .env.production.template .env.production
# Edit .env.production with your values
```

### 2. Deploy
```bash
pnpm run deploy:production
```

### 3. Verify
```bash
pnpm run verify:production https://yourdomain.com
```

---

## 📋 Pre-Deployment Checklist

- [ ] All tests pass (`pnpm test:run`)
- [ ] Build succeeds (`pnpm build`)
- [ ] `.env.production` configured
- [ ] Content reviewed and approved
- [ ] Team notified

---

## 🔧 Required Environment Variables

```bash
NEXT_PUBLIC_API_BASE_URL=https://api.yourdomain.com
NEXT_PUBLIC_APP_URL=https://yourdomain.com
NEXT_PUBLIC_PAYPAL_CLIENT_ID=your-production-client-id
NEXT_PUBLIC_GA_MEASUREMENT_ID=G-XXXXXXXXXX
NEXT_PUBLIC_GSC_VERIFICATION=your-verification-code
NEXT_PUBLIC_SITE_URL=https://yourdomain.com
NEXT_PUBLIC_SITE_NAME=ClinicPro
```

---

## 🎯 Deployment Methods

### Vercel (Recommended)
```bash
vercel --prod
```

### Docker
```bash
docker build -t saas-landing:latest .
docker run -d -p 3003:3003 saas-landing:latest
```

### Manual
```bash
pnpm build
pnpm start -p 3003
```

---

## ✅ Post-Deployment Verification

### Automated Check
```bash
pnpm run verify:production https://yourdomain.com
```

### Manual Checks
1. Visit homepage
2. Test blog pages
3. Check analytics (GA4 Real-Time)
4. Verify sitemap: `/sitemap.xml`
5. Check robots.txt: `/robots.txt`

---

## 🔍 Google Search Console

### 1. Verify Site
1. Go to [Google Search Console](https://search.google.com/search-console)
2. Add property: `https://yourdomain.com`
3. Select "HTML tag" method
4. Copy verification code
5. Add to `.env.production`:
   ```bash
   NEXT_PUBLIC_GSC_VERIFICATION=ABC123...
   ```
6. Redeploy
7. Click "Verify"

### 2. Submit Sitemap
1. In GSC, go to "Sitemaps"
2. Enter: `sitemap.xml`
3. Click "Submit"

---

## 📊 First 48 Hours Monitoring

### Hour 0-2
- [ ] Site loads correctly
- [ ] Analytics tracking
- [ ] No console errors
- [ ] SEO elements present

### Hour 2-12
- [ ] Check GA4 dashboard
- [ ] Monitor error logs
- [ ] Verify performance
- [ ] Check traffic sources

### Hour 12-24
- [ ] Review analytics
- [ ] Check conversions
- [ ] Monitor indexing
- [ ] Test mobile

### Hour 24-48
- [ ] Analyze trends
- [ ] Review feedback
- [ ] Check SEO progress
- [ ] Plan optimizations

---

## 🔄 Rollback

### Vercel
```
Dashboard → Deployments → Previous → Promote to Production
```

### Docker
```bash
docker stop saas-landing
docker rm saas-landing
docker run -d saas-landing:previous-tag
```

### Git
```bash
git revert HEAD
git push origin main
```

---

## 📈 Key Metrics to Monitor

### Performance
- PageSpeed Score: > 90 (desktop), > 85 (mobile)
- LCP: < 2.5s
- FID: < 100ms
- CLS: < 0.1

### Analytics
- Page views
- Conversion events
- Bounce rate
- Session duration

### SEO
- Pages indexed
- Coverage issues
- Mobile usability
- Core Web Vitals

---

## 🆘 Troubleshooting

### Site Not Loading
- Check DNS settings
- Verify SSL certificate
- Check server logs
- Verify environment variables

### Analytics Not Tracking
- Verify GA4 measurement ID
- Check browser console
- Test in incognito mode
- Verify script loads

### Sitemap Not Found
- Verify build completed
- Check `app/sitemap.ts` exists
- Rebuild and redeploy
- Clear CDN cache

---

## 📚 Documentation

- [Full Deployment Guide](./PRODUCTION_DEPLOYMENT_GUIDE.md)
- [GSC Submission Guide](./GOOGLE_SEARCH_CONSOLE_SUBMISSION.md)
- [48-Hour Monitoring](./FIRST_48_HOURS_MONITORING.md)
- [Task 27 Summary](./TASK_27_DEPLOYMENT_SUMMARY.md)

---

## 🔗 Useful Links

- [PageSpeed Insights](https://pagespeed.web.dev/)
- [Google Analytics](https://analytics.google.com/)
- [Google Search Console](https://search.google.com/search-console)
- [Rich Results Test](https://search.google.com/test/rich-results)
- [Mobile-Friendly Test](https://search.google.com/test/mobile-friendly)

---

## 📞 Support

**Critical Issues:**
- DevOps Team: devops@yourdomain.com
- Technical Lead: tech@yourdomain.com

**Questions:**
- See full documentation in `docs/` folder
- Check troubleshooting sections
- Review error logs

---

**Last Updated:** [Current Date]
**Version:** 1.0.0
