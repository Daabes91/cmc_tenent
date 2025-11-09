# Hydration Error Troubleshooting Guide

## ✅ SOLUTION APPLIED

**SSR has been disabled** in `nuxt.config.ts` because:
1. This is an admin panel (not public-facing)
2. Admin panels don't need SEO
3. SSR adds complexity without benefits for authenticated interfaces

The app now runs in **client-side only mode** (SPA mode), which is the recommended approach for admin panels.

## Quick Fix Steps (If Issues Persist)

### 1. Clear Nuxt Cache
```bash
cd apps/saas-admin-nuxt
./clear-cache.sh
# or manually:
rm -rf .nuxt .output node_modules/.cache
```

### 2. Restart Dev Server
```bash
npm run dev
```

### 3. Hard Refresh Browser
- Chrome/Firefox: `Ctrl+Shift+R` (Windows/Linux) or `Cmd+Shift+R` (Mac)
- Clear browser cache if needed

## What We Fixed

### Root Causes
1. **Responsive state using `window` during SSR**
2. **Date/time formatting differences between server and client**
3. **Random ID generation**
4. **Teleport components (require DOM)**
5. **Dropdown/Modal components (use Teleport internally)**
6. **Browser API usage during SSR**

### Components Wrapped in `<ClientOnly>`
- Sidebar (uses window.innerWidth)
- ToastContainer (uses Teleport)
- SessionTimeoutWarning (uses UModal → Teleport)
- NotificationBell (uses UDropdown → Teleport)
- LanguageSwitcher (uses UDropdown → Teleport)
- UserMenu (uses UDropdown → Teleport)

### Functions with Client-Side Checks
- `formatTimestamp()` in RecentActivityFeed
- `formatTimestamp()` in NotificationBell
- `formatTime()` in NotificationHistory

## If Error Persists

### Check Browser Console
Look for specific component causing the mismatch:
```
Hydration completed but contains mismatches.
```

### Enable Nuxt DevTools
Add to `nuxt.config.ts`:
```typescript
devtools: { 
  enabled: true,
  vscode: {}
}
```

### Check for Additional Issues

1. **Custom Components**: Any component using:
   - `window` or `document`
   - `localStorage` or `sessionStorage`
   - `navigator`
   - `Math.random()` or `Date.now()` in computed properties
   - Browser-specific APIs

2. **Third-party Libraries**: Some libraries don't support SSR
   - Wrap imports in `if (import.meta.client)`
   - Use dynamic imports: `const lib = await import('library')`

3. **CSS/Styling**: Ensure no dynamic classes based on client-only state

### Debug Mode

Add to `nuxt.config.ts`:
```typescript
export default defineNuxtConfig({
  // ... other config
  
  // Disable SSR temporarily to test
  ssr: false,
  
  // Or enable debug mode
  debug: true,
  
  // Verbose logging
  logLevel: 'verbose'
})
```

### Check Specific Components

Run diagnostics on components:
```bash
npm run typecheck
```

### Inspect Build Output

Check the generated files:
```bash
ls -la .nuxt/dist/client
```

## Common Patterns to Avoid

### ❌ Bad
```vue
<script setup>
const id = ref(Math.random()) // Different on server vs client
const now = ref(new Date()) // Different timestamps
const width = ref(window.innerWidth) // window doesn't exist on server
</script>
```

### ✅ Good
```vue
<script setup>
const id = ref('stable-id') // Same on both sides
const now = ref(null) // Set on client only
const width = ref(0) // Default value

onMounted(() => {
  now.value = new Date()
  width.value = window.innerWidth
})
</script>
```

### ✅ Better with ClientOnly
```vue
<template>
  <ClientOnly>
    <ComponentUsingBrowserAPIs />
  </ClientOnly>
</template>
```

## Still Having Issues?

1. Check if the error is actually breaking functionality
2. Some hydration warnings are cosmetic and don't affect the app
3. Consider disabling SSR for specific routes:
   ```typescript
   // In page component
   definePageMeta({
     ssr: false
   })
   ```

## Resources

- [Nuxt SSR Documentation](https://nuxt.com/docs/guide/concepts/rendering)
- [Vue Hydration Guide](https://vuejs.org/guide/scaling-up/ssr.html#hydration-mismatch)
- [ClientOnly Component](https://nuxt.com/docs/api/components/client-only)
