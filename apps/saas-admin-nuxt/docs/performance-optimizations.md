# Performance Optimizations

This document describes the performance optimizations implemented in the SAAS Admin Nuxt application.

## Overview

The application includes several performance optimizations to ensure fast load times, smooth interactions, and efficient resource usage:

1. **Lazy Loading** - Components and pages load on demand
2. **Code Splitting** - Vendor and app bundles are split for better caching
3. **Image Optimization** - WebP format with lazy loading
4. **API Response Caching** - Intelligent caching with appropriate TTL
5. **Debouncing** - Search inputs debounced to reduce API calls
6. **Optimistic UI Updates** - Immediate feedback for better UX

## 1. Lazy Loading

### Component Lazy Loading

Components are lazy-loaded using Vue's `defineAsyncComponent`:

```typescript
// Lazy load a component
const LazyComponent = lazyLoadComponent(
  () => import('~/components/MyComponent.vue'),
  {
    loadingComponent: LoadingSkeleton,
    delay: 200,
    timeout: 10000
  }
)
```

### Page Lazy Loading

Nuxt automatically lazy-loads pages. Heavy components within pages are also lazy-loaded:

```vue
<script setup>
// Lazy load chart components
const TenantGrowthChart = defineAsyncComponent(
  () => import('~/components/analytics/TenantGrowthChart.vue')
)
</script>
```

### Utilities

Use the `lazyLoad.ts` utilities for advanced lazy loading:

```typescript
import { lazyLoadComponent, preloadComponent } from '~/utils/lazyLoad'

// Preload a component for faster subsequent loads
await preloadComponent(() => import('~/components/HeavyComponent.vue'))
```

## 2. Code Splitting

### Vendor Chunks

The build configuration splits vendor code into separate chunks:

```typescript
// nuxt.config.ts
vite: {
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router'],
          'chart-vendor': ['chart.js'],
          'ui-vendor': ['@nuxt/ui']
        }
      }
    }
  }
}
```

### Benefits

- Better browser caching (vendor code changes less frequently)
- Faster initial load (parallel downloads)
- Smaller bundle sizes per chunk

## 3. Image Optimization

### OptimizedImage Component

Use the `OptimizedImage` component for automatic WebP conversion and lazy loading:

```vue
<OptimizedImage
  src="/images/logo.png"
  webp-src="/images/logo.webp"
  alt="Logo"
  width="200"
  height="100"
  loading="lazy"
  class="rounded-lg"
/>
```

### Features

- Automatic WebP format with fallback
- Lazy loading by default
- Fade-in transition on load
- Error handling with fallback UI
- Responsive image sizing

### Image Configuration

The Nuxt config includes image optimization settings:

```typescript
image: {
  formats: ['webp', 'avif', 'png', 'jpg'],
  quality: 80,
  screens: {
    xs: 320,
    sm: 640,
    md: 768,
    lg: 1024,
    xl: 1280,
    xxl: 1536
  }
}
```

## 4. API Response Caching

### useApiCache Composable

The `useApiCache` composable provides intelligent caching:

```typescript
const { cachedCall, generateKey, DEFAULT_TTL } = useApiCache()

// Cached API call
const data = await cachedCall(
  () => api.getTenants(params),
  generateKey('/tenants', params),
  { ttl: DEFAULT_TTL.tenantList }
)
```

### Default TTL Values

- **System Metrics**: 30 seconds (frequently changing data)
- **Tenant List**: 1 minute (moderate change frequency)
- **Tenant Detail**: 2 minutes (less frequent changes)
- **Analytics**: 5 minutes (historical data)
- **Audit Logs**: 1 minute (append-only data)
- **Branding**: 5 minutes (rarely changes)

### Cache Invalidation

```typescript
const { invalidate, invalidatePattern, clear } = useApiCache()

// Invalidate specific cache entry
invalidate('/tenants?page=1')

// Invalidate all tenant-related cache
invalidatePattern('/tenants')

// Clear all cache
clear()
```

### Usage in Composables

All data-fetching composables use caching:

```typescript
// useTenantManagement.ts
const fetchTenants = async (useCache = true) => {
  const { cachedCall, generateKey, DEFAULT_TTL } = useApiCache()
  
  const response = useCache
    ? await cachedCall(
        () => api.getTenants(params),
        generateKey('/tenants', params),
        { ttl: DEFAULT_TTL.tenantList }
      )
    : await api.getTenants(params)
  
  // ... update state
}
```

## 5. Debouncing

### useDebounce Composable

The `useDebounce` composable provides debouncing utilities:

```typescript
const { debounce, debouncedRef, watchDebounced } = useDebounce()

// Debounce a function
const debouncedSearch = debounce((query: string) => {
  performSearch(query)
}, 300)

// Debounced ref
const { value, debouncedValue } = debouncedRef('', 300)

// Watch with debouncing
watchDebounced(searchQuery, (newValue) => {
  performSearch(newValue)
}, 300)
```

### Search Input Debouncing

All search inputs are debounced to 300ms:

```typescript
// useTenantManagement.ts
const { debounce } = useDebounce()
const debouncedFetch = debounce(() => {
  syncUrlParams()
  fetchTenants(false)
}, 300)

const setSearch = (value: string) => {
  state.search = value
  state.page = 1
  debouncedFetch()
}
```

### Benefits

- Reduces API calls (fewer requests)
- Improves server performance
- Better user experience (no lag)
- Lower bandwidth usage

## 6. Optimistic UI Updates

### useOptimisticUpdate Composable

The `useOptimisticUpdate` composable provides optimistic updates:

```typescript
const { optimisticUpdate, optimisticAdd, optimisticUpdateItem, optimisticDelete } = useOptimisticUpdate()

// Optimistic update
await optimisticUpdate(
  tenantStatus,
  'ACTIVE',
  () => api.updateTenant(id, { status: 'ACTIVE' }),
  {
    showSuccessToast: true,
    successMessage: 'Tenant activated',
    showErrorToast: true
  }
)

// Optimistic add
await optimisticAdd(
  tenantList,
  newTenant,
  () => api.createTenant(newTenant),
  { showSuccessToast: true }
)

// Optimistic update item
await optimisticUpdateItem(
  tenantList,
  tenantId,
  { name: 'New Name' },
  () => api.updateTenant(tenantId, { name: 'New Name' }),
  { showSuccessToast: true }
)

// Optimistic delete
await optimisticDelete(
  tenantList,
  tenantId,
  () => api.deleteTenant(tenantId),
  { showSuccessToast: true }
)
```

### Features

- Immediate UI feedback
- Automatic rollback on error
- Toast notifications
- Error handling
- Success callbacks

### Benefits

- Perceived performance improvement
- Better user experience
- Reduced waiting time
- Smooth interactions

## Route Rules and Caching

### Static Route Caching

The Nuxt config includes route rules for caching:

```typescript
routeRules: {
  '/login': { prerender: false },
  '/api/**': { cache: false },
  '/': { swr: 30 },
  '/analytics': { swr: 60 },
  '/audit-logs': { swr: 30 }
}
```

### SWR (Stale-While-Revalidate)

- Dashboard: 30 seconds
- Analytics: 60 seconds
- Audit Logs: 30 seconds

## Build Optimizations

### Nitro Configuration

```typescript
nitro: {
  compressPublicAssets: true,
  minify: true
}
```

### Vite Optimizations

```typescript
vite: {
  optimizeDeps: {
    include: ['chart.js']
  }
}
```

## Performance Monitoring

### Metrics to Monitor

1. **First Contentful Paint (FCP)**: < 1.8s
2. **Largest Contentful Paint (LCP)**: < 2.5s
3. **Time to Interactive (TTI)**: < 3.8s
4. **Total Blocking Time (TBT)**: < 200ms
5. **Cumulative Layout Shift (CLS)**: < 0.1

### Tools

- Chrome DevTools Performance tab
- Lighthouse
- WebPageTest
- Nuxt DevTools

## Best Practices

### Do's

✅ Use lazy loading for heavy components
✅ Implement caching for API responses
✅ Debounce search inputs
✅ Use optimistic updates for better UX
✅ Optimize images with WebP
✅ Split vendor code into chunks
✅ Monitor performance metrics

### Don'ts

❌ Don't load all data at once
❌ Don't make API calls on every keystroke
❌ Don't use large unoptimized images
❌ Don't bundle everything in one chunk
❌ Don't ignore cache invalidation
❌ Don't skip loading states

## Testing Performance

### Local Testing

```bash
# Build for production
npm run build

# Preview production build
npm run preview

# Run Lighthouse
npx lighthouse http://localhost:3002 --view
```

### Performance Budget

- Initial bundle size: < 200KB (gzipped)
- Vendor bundle size: < 150KB (gzipped)
- Image sizes: < 100KB each
- API response time: < 500ms
- Page load time: < 3s

## Future Improvements

1. **Service Worker**: Offline support and background sync
2. **HTTP/2 Server Push**: Push critical resources
3. **Resource Hints**: Preload, prefetch, preconnect
4. **Virtual Scrolling**: For large lists
5. **Progressive Web App**: Install as native app
6. **Edge Caching**: CDN integration
7. **Database Query Optimization**: Backend improvements
8. **GraphQL**: Reduce over-fetching

## Conclusion

These performance optimizations ensure the SAAS Admin Panel provides a fast, responsive, and efficient user experience. Regular monitoring and continuous improvement are essential to maintain optimal performance.
