# SSR Hydration Fix - Testing Guide

## Current Status

SSR has been **temporarily disabled** in `nuxt.config.ts` to isolate the hydration issue.

## Testing Steps

### 1. Test with SSR Disabled (Current State)

```bash
# Clear cache
rm -rf .nuxt .output node_modules/.cache

# Start dev server
npm run dev
```

**Expected Result**: App should load without any errors.

### 2. If App Works Without SSR

This confirms the issue is SSR-related. The app will work but:
- ❌ No server-side rendering (slower initial load)
- ❌ Worse SEO
- ✅ No hydration errors
- ✅ All functionality works

### 3. Re-enable SSR Gradually

Once confirmed working without SSR, we can re-enable it:

**Option A: Enable SSR Globally**
```typescript
// nuxt.config.ts
export default defineNuxtConfig({
  ssr: true, // or remove the line (true is default)
  // ...
})
```

**Option B: Enable SSR Per Route**
```typescript
// In specific page components
definePageMeta({
  ssr: true
})
```

**Option C: Keep SSR Disabled for Admin Panel**
Since this is an admin panel (not public-facing), SSR might not be necessary:
- Admin panels don't need SEO
- Users are authenticated (no public crawling)
- Client-side rendering is acceptable

## Recommended Approach

### For Admin Panel (Current App)
**Keep SSR disabled** - it's not needed for admin interfaces.

```typescript
// nuxt.config.ts
export default defineNuxtConfig({
  ssr: false, // Admin panel doesn't need SSR
  // ...
})
```

### For Public-Facing Apps
If you need SSR for SEO/performance:

1. **Remove all `<ClientOnly>` wrappers** we added
2. **Fix components properly**:
   - Move browser API calls to `onMounted()`
   - Use `process.client` checks
   - Ensure stable IDs and values

## Components That Need SSR Fixes

If re-enabling SSR, these components need attention:

### High Priority
- ✅ `layouts/default.vue` - Sidebar responsive logic
- ✅ `components/notifications/ToastContainer.vue` - Teleport
- ✅ `components/dashboard/RecentActivityFeed.vue` - Date formatting
- ✅ `components/NotificationBell.vue` - Date formatting
- ✅ `components/notifications/NotificationHistory.vue` - Date formatting
- ✅ `components/dashboard/MetricCard.vue` - ID generation

### Medium Priority
- `components/UserMenu.vue` - UDropdown (Teleport)
- `components/LanguageSwitcher.vue` - UDropdown (Teleport)
- `components/SessionTimeoutWarning.vue` - UModal (Teleport)

## Alternative: Hybrid Rendering

You can also use hybrid rendering:

```typescript
// nuxt.config.ts
export default defineNuxtConfig({
  routeRules: {
    // SSR for public pages
    '/': { ssr: true },
    
    // Client-side for admin pages
    '/tenants/**': { ssr: false },
    '/analytics': { ssr: false },
    '/audit-logs': { ssr: false },
  }
})
```

## Verification Checklist

- [ ] App loads without errors
- [ ] No console warnings
- [ ] All pages accessible
- [ ] Forms work correctly
- [ ] Navigation works
- [ ] Dropdowns/modals work
- [ ] Notifications appear
- [ ] Data loads correctly

## Decision Matrix

| Scenario | SSR Needed? | Recommendation |
|----------|-------------|----------------|
| Admin Panel | ❌ No | Keep SSR disabled |
| Public Marketing Site | ✅ Yes | Enable SSR, fix components |
| Hybrid (Public + Admin) | ⚠️ Mixed | Use route-based SSR |
| Internal Tool | ❌ No | Keep SSR disabled |

## Current Recommendation

**Keep SSR disabled** for this admin panel. It's simpler, faster to develop, and SSR provides no benefit for authenticated admin interfaces.

If you need SSR in the future, all the fixes are documented in `HYDRATION_FIX_SUMMARY.md`.
