# Hero Media Performance Testing Guide

## Quick Start

This guide helps you verify that all performance optimizations are working correctly.

## Prerequisites

- API running on `http://localhost:8080`
- Web-Next running on `http://localhost:3000`
- Browser DevTools available (Chrome recommended)

## Test 1: Verify Cache Headers (2 minutes)

### Steps:

1. Open Chrome DevTools (F12)
2. Go to **Network** tab
3. Check "Disable cache" is **OFF**
4. Navigate to `http://localhost:3000`
5. Find the request to `/public/settings`
6. Click on it and go to **Headers** tab

### Expected Results:

```
Response Headers:
  Cache-Control: public, max-age=300, stale-while-revalidate=600
  Content-Type: application/json
```

### ✅ Pass Criteria:
- Cache-Control header is present
- max-age=300 (5 minutes)
- stale-while-revalidate=600 (10 minutes)
- public directive is set

### ❌ Fail Indicators:
- Cache-Control: no-cache, no-store
- Cache-Control header missing
- max-age=0

---

## Test 2: Verify Client-Side Caching (3 minutes)

### Steps:

1. Open Chrome DevTools (F12)
2. Go to **Application** tab
3. Expand **Session Storage** in left sidebar
4. Click on `http://localhost:3000`
5. Navigate to homepage
6. Look for `hero-media-settings` key

### Expected Results:

```
Key: hero-media-settings
Value: {"type":"image","imageUrl":"..."}

Key: hero-media-settings-time
Value: 1234567890123 (timestamp)
```

### ✅ Pass Criteria:
- Both keys are present
- Value is valid JSON
- Timestamp is recent

### Test Cache Expiry:

1. Note the timestamp value
2. Wait 5+ minutes
3. Reload the page
4. Check if timestamp updated (should be new)

---

## Test 3: Verify Lazy Loading (2 minutes)

### Steps:

1. Open Chrome DevTools (F12)
2. Go to **Network** tab
3. Filter by "youtube.com"
4. Navigate to homepage
5. **Don't scroll** - stay at top of page
6. Wait 3 seconds

### Expected Results:

- **No YouTube requests** should appear initially

### Now Scroll:

1. Scroll down to hero section
2. Watch Network tab

### Expected Results:

- YouTube iframe request appears **only when scrolling**
- Request URL contains `youtube.com/embed/`

### ✅ Pass Criteria:
- No YouTube requests on initial load
- YouTube loads when hero section is visible
- Placeholder shows before video loads

---

## Test 4: Verify Image Optimization (2 minutes)

### Steps:

1. Open Chrome DevTools (F12)
2. Go to **Network** tab
3. Filter by "Img"
4. Navigate to homepage
5. Find the hero image request

### Expected Results:

```
Request URL: .../_next/image?url=...&w=1200&q=75
Content-Type: image/webp (or image/avif)
Size: < 200KB (optimized)
```

### ✅ Pass Criteria:
- Image served through Next.js Image API (`/_next/image`)
- Format is WebP or AVIF (not JPEG/PNG)
- File size is significantly smaller than original
- Responsive sizes are used

---

## Test 5: Performance Metrics (5 minutes)

### Automated Test:

```bash
node apps/web-next/test/hero-media-performance.test.js
```

### Expected Output:

```
✅ API Response Status: 200
📦 Cache-Control Header: public, max-age=300, stale-while-revalidate=600
⏱️  Cache Duration: 300 seconds (5 minutes)
🔄 Stale-While-Revalidate: 600 seconds (10 minutes)

📊 Response Time Statistics:
  Average: <50ms
  Min: <20ms
  Max: <100ms
✅ Excellent response time!
```

### ✅ Pass Criteria:
- All tests pass
- Average response time < 50ms
- Cache headers properly configured

---

## Test 6: Lighthouse Audit (5 minutes)

### Steps:

1. Open Chrome DevTools (F12)
2. Go to **Lighthouse** tab
3. Select:
   - ✅ Performance
   - ✅ Desktop
4. Click "Analyze page load"

### Expected Results:

```
Performance Score: 90-100
Metrics:
  First Contentful Paint: < 1.8s
  Largest Contentful Paint: < 2.5s
  Total Blocking Time: < 300ms
  Cumulative Layout Shift: < 0.1
  Speed Index: < 3.4s
```

### ✅ Pass Criteria:
- Performance score > 90
- LCP < 2.5s
- No major performance issues flagged

---

## Test 7: Cache Performance Comparison (3 minutes)

### Test Uncached Performance:

1. Open Chrome DevTools (F12)
2. Go to **Network** tab
3. Check "Disable cache"
4. Reload page
5. Note the load time for `/public/settings`

### Test Cached Performance:

1. Uncheck "Disable cache"
2. Reload page
3. Note the load time for `/public/settings`

### Expected Results:

```
Uncached: 20-50ms
Cached: 0-5ms (from disk cache)
Improvement: 80-100%
```

### ✅ Pass Criteria:
- Cached request is significantly faster
- Second request shows "(from disk cache)" or "(from memory cache)"
- Performance improvement > 50%

---

## Test 8: Admin Panel Preview (2 minutes)

### Steps:

1. Navigate to admin panel clinic settings
2. Open Chrome DevTools (F12)
3. Go to **Network** tab
4. Filter by "youtube.com"
5. Scroll to hero media configuration section

### Expected Results:

- YouTube iframe **doesn't load** until preview is visible
- Placeholder icon shows before load
- Iframe loads smoothly when scrolling to preview

### ✅ Pass Criteria:
- Lazy loading works in admin panel
- No performance impact on settings page load
- Preview loads on-demand

---

## Troubleshooting

### Cache Headers Not Working

**Symptoms:** Cache-Control shows "no-cache, no-store"

**Solutions:**
1. Restart API server
2. Check SecurityConfig.java changes are deployed
3. Verify no proxy/CDN is overriding headers
4. Clear browser cache and try again

### Client-Side Cache Not Working

**Symptoms:** sessionStorage is empty

**Solutions:**
1. Check browser console for errors
2. Verify sessionStorage is not blocked by privacy settings
3. Check if code is running in incognito mode
4. Verify JavaScript is enabled

### Lazy Loading Not Working

**Symptoms:** YouTube loads immediately

**Solutions:**
1. Check browser supports Intersection Observer
2. Verify no JavaScript errors in console
3. Check if video is above the fold (should still lazy load)
4. Try in different browser

### Images Not Optimizing

**Symptoms:** Large JPEG/PNG files, no WebP

**Solutions:**
1. Verify Next.js Image component is used
2. Check next.config.ts for image optimization settings
3. Verify Sharp is installed: `npm list sharp`
4. Check if image domain is allowed in config

---

## Quick Checklist

Use this checklist for rapid verification:

- [ ] Cache-Control header present with max-age=300
- [ ] sessionStorage contains hero-media-settings
- [ ] YouTube iframe lazy loads (not on initial page load)
- [ ] Images served as WebP/AVIF through Next.js
- [ ] Performance test script passes all checks
- [ ] Lighthouse score > 90
- [ ] Cached requests are faster than uncached
- [ ] Admin panel preview lazy loads

---

## Success Criteria

All optimizations are working if:

1. ✅ Cache headers are properly set
2. ✅ Client-side caching reduces API calls by 80%
3. ✅ YouTube iframes lazy load on scroll
4. ✅ Images are optimized and responsive
5. ✅ Performance score > 90 on Lighthouse
6. ✅ Page load time improved by 40%

---

## Performance Benchmarks

### Target Metrics:

| Metric | Target | Acceptable | Poor |
|--------|--------|------------|------|
| LCP | < 1.5s | < 2.5s | > 2.5s |
| FID | < 50ms | < 100ms | > 100ms |
| CLS | < 0.05 | < 0.1 | > 0.1 |
| Performance Score | > 95 | > 90 | < 90 |
| API Response | < 30ms | < 50ms | > 50ms |
| Cache Hit Rate | > 80% | > 60% | < 60% |

---

## Reporting Issues

If any test fails:

1. Note which test failed
2. Capture screenshots/network logs
3. Check browser console for errors
4. Verify all code changes are deployed
5. Try in different browser
6. Check server logs for errors

---

## Additional Resources

- [Web.dev Performance Guide](https://web.dev/performance/)
- [Chrome DevTools Performance](https://developer.chrome.com/docs/devtools/performance/)
- [Next.js Image Optimization](https://nextjs.org/docs/basic-features/image-optimization)
- [HTTP Caching](https://developer.mozilla.org/en-US/docs/Web/HTTP/Caching)

---

**Last Updated:** 2024
**Version:** 1.0
