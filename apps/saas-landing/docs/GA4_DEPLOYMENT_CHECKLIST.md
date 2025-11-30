# Google Analytics 4 - Deployment Checklist

## Pre-Deployment

### 1. GA4 Property Setup

- [ ] Create GA4 property in Google Analytics
- [ ] Note the Measurement ID (format: `G-XXXXXXXXXX`)
- [ ] Configure data retention settings (14 months recommended)
- [ ] Set up data streams for web
- [ ] Configure enhanced measurement settings
- [ ] Add team members with appropriate permissions

### 2. Environment Configuration

- [ ] Add `NEXT_PUBLIC_GA_MEASUREMENT_ID` to production environment variables
- [ ] Verify measurement ID is correct (no typos)
- [ ] Test in staging environment first
- [ ] Ensure `NEXT_PUBLIC_ENABLE_ANALYTICS` is NOT set in production (defaults to enabled)

**Production Environment Variables**:
```bash
NEXT_PUBLIC_GA_MEASUREMENT_ID=G-XXXXXXXXXX
```

### 3. Code Review

- [ ] Verify `GoogleAnalytics` component is in root layout
- [ ] Check all tracking calls are implemented correctly
- [ ] Ensure no PII (personally identifiable information) is tracked
- [ ] Verify error handling is in place
- [ ] Check TypeScript types are correct
- [ ] Review console.log statements (should only appear in dev)

### 4. Testing in Staging

- [ ] Deploy to staging environment
- [ ] Verify GA4 script loads
- [ ] Test page view tracking
- [ ] Test CTA click tracking
- [ ] Test form submission tracking
- [ ] Test scroll depth tracking
- [ ] Verify events appear in GA4 DebugView
- [ ] Check for console errors
- [ ] Test on multiple browsers (Chrome, Firefox, Safari)
- [ ] Test on mobile devices

### 5. Performance Check

- [ ] Run Lighthouse audit
- [ ] Verify analytics doesn't impact Core Web Vitals
- [ ] Check script load time (< 500ms)
- [ ] Verify no blocking resources
- [ ] Test page load speed with analytics enabled

## Deployment

### 1. Deploy to Production

- [ ] Deploy application with analytics code
- [ ] Verify deployment successful
- [ ] Check production build logs for errors

### 2. Immediate Verification (0-5 minutes)

- [ ] Visit production site
- [ ] Open browser DevTools → Network tab
- [ ] Verify GA4 script loads (gtag/js request)
- [ ] Check for JavaScript errors in console
- [ ] Navigate between pages
- [ ] Click a CTA button
- [ ] Submit a form (if available)

### 3. GA4 Dashboard Verification (5-15 minutes)

- [ ] Open GA4 Dashboard → Reports → Realtime
- [ ] Verify active users appear
- [ ] Check page views are tracked
- [ ] Verify events are appearing:
  - `page_view`
  - `scroll`
  - `signup_started` (or other conversion events)
  - `form_submitted`
- [ ] Check event parameters are correct

### 4. DebugView Verification (Optional but Recommended)

- [ ] Navigate to GA4 → Configure → DebugView
- [ ] Trigger various events on production site
- [ ] Verify events appear in DebugView with correct parameters
- [ ] Check for any error events

## Post-Deployment (24-48 hours)

### 1. Data Collection Verification

- [ ] Check GA4 Reports → Engagement → Events
- [ ] Verify event counts are reasonable
- [ ] Check conversion events are tracked
- [ ] Review user demographics (if enabled)
- [ ] Check traffic sources
- [ ] Verify page views match expected traffic

### 2. Conversion Setup

- [ ] Mark important events as conversions in GA4:
  - `signup_started`
  - `demo_requested`
  - `pricing_viewed`
  - `form_submitted`
- [ ] Set up conversion goals
- [ ] Configure conversion values (if applicable)

### 3. Custom Reports

- [ ] Create custom reports for:
  - User journey analysis
  - Conversion funnel
  - Content engagement
  - CTA performance
  - Form completion rates
- [ ] Set up custom dashboards
- [ ] Share reports with team

### 4. Alerts Configuration

- [ ] Set up alerts for:
  - Significant traffic drops (> 20%)
  - Conversion rate changes (> 15%)
  - Error rate increases
  - Unusual traffic patterns
- [ ] Configure email notifications
- [ ] Test alert triggers

### 5. Integration with Other Tools

- [ ] Link GA4 with Google Search Console
- [ ] Connect to Google Ads (if applicable)
- [ ] Set up BigQuery export (for advanced analysis)
- [ ] Configure data import (if needed)

## Monitoring (Ongoing)

### Daily Checks (First Week)

- [ ] Check real-time reports for active users
- [ ] Verify events are being tracked
- [ ] Monitor for errors or anomalies
- [ ] Review conversion rates

### Weekly Checks

- [ ] Review weekly traffic trends
- [ ] Check conversion funnel performance
- [ ] Analyze top pages and content
- [ ] Review user behavior flow
- [ ] Check for tracking issues

### Monthly Checks

- [ ] Review monthly performance reports
- [ ] Analyze conversion trends
- [ ] Check goal completion rates
- [ ] Review user acquisition channels
- [ ] Optimize based on insights

## Troubleshooting

### Issue: No Data in GA4

**Possible Causes**:
- Measurement ID incorrect
- Script not loading
- Ad blockers preventing tracking
- Data processing delay (wait 24-48 hours)

**Solutions**:
1. Verify measurement ID in environment variables
2. Check Network tab for gtag/js request
3. Test in incognito mode (bypasses some blockers)
4. Wait 24-48 hours for data to appear in reports

### Issue: Events Not Tracked

**Possible Causes**:
- Tracking code not implemented
- JavaScript errors
- Event name typos
- Ad blockers

**Solutions**:
1. Check browser console for errors
2. Verify tracking code is called
3. Test in DebugView
4. Check event names match GA4 configuration

### Issue: Duplicate Events

**Possible Causes**:
- Multiple GA4 components
- Event handlers called multiple times
- React strict mode (development only)

**Solutions**:
1. Verify only one `GoogleAnalytics` component
2. Check event handler implementation
3. Test in production build

### Issue: Performance Impact

**Possible Causes**:
- Script loading synchronously
- Too many events tracked
- Large event payloads

**Solutions**:
1. Verify script uses `afterInteractive` strategy
2. Throttle event tracking
3. Reduce event payload size
4. Use batch event sending

## Rollback Plan

If analytics causes issues:

1. **Immediate Rollback**:
   ```bash
   # Remove measurement ID from environment
   unset NEXT_PUBLIC_GA_MEASUREMENT_ID
   # Redeploy
   ```

2. **Partial Rollback**:
   - Comment out `<GoogleAnalytics />` in layout
   - Keep tracking code for future use
   - Redeploy

3. **Debug and Redeploy**:
   - Fix issues in staging
   - Test thoroughly
   - Redeploy to production

## Success Criteria

✅ **Analytics is successfully deployed when**:

1. GA4 script loads on all pages
2. Page views tracked correctly
3. Conversion events tracked
4. No JavaScript errors
5. No performance impact (< 100ms)
6. Data appears in GA4 dashboard
7. Real-time reports show activity
8. Events appear in DebugView
9. No increase in error rates
10. Team can access reports

## Documentation

- [ ] Update team documentation with:
  - GA4 property details
  - Measurement ID location
  - How to access reports
  - Custom report locations
  - Alert configuration
  - Troubleshooting guide

## Training

- [ ] Train team on:
  - Accessing GA4 dashboard
  - Reading reports
  - Understanding metrics
  - Using DebugView
  - Responding to alerts

## Compliance

- [ ] Verify GDPR compliance (if applicable)
- [ ] Update privacy policy with analytics disclosure
- [ ] Implement cookie consent (if required)
- [ ] Configure data retention settings
- [ ] Set up data deletion requests process

## Next Steps

After successful deployment:

1. **Week 1**: Monitor closely, fix any issues
2. **Week 2-4**: Analyze initial data, optimize tracking
3. **Month 2**: Set up advanced reports and dashboards
4. **Month 3**: Implement A/B testing based on insights
5. **Ongoing**: Regular review and optimization

## Support Contacts

- **GA4 Documentation**: https://developers.google.com/analytics/devguides/collection/ga4
- **GA4 Support**: https://support.google.com/analytics
- **Team Lead**: [Name/Email]
- **Developer**: [Name/Email]

## Sign-off

- [ ] Developer: Verified implementation _______________
- [ ] QA: Tested in staging _______________
- [ ] Product Manager: Approved for production _______________
- [ ] Marketing: Verified tracking requirements _______________
- [ ] Deployed to Production: Date _______________
- [ ] Post-deployment verification complete: Date _______________

---

**Deployment Date**: _______________
**Deployed By**: _______________
**Measurement ID**: G-_______________
**Status**: ⬜ Not Started | ⬜ In Progress | ⬜ Complete
