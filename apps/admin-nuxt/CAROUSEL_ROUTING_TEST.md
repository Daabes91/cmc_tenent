# Carousel Routing Test Guide

## Routes Verification

### 1. Main Carousel Routes
- ✅ `/ecommerce/carousels` - List all carousels
- ✅ `/ecommerce/carousels/new` - Create new carousel
- ✅ `/ecommerce/carousels/[id]` - Edit existing carousel

### 2. Navigation Integration
- ✅ Navigation menu includes carousel link when e-commerce is enabled
- ✅ Proper icons and labels configured
- ✅ Translation keys added for both English and Arabic

### 3. Middleware Protection
- ✅ `ecommerce-enabled.ts` middleware protects routes
- ✅ Redirects to home with toast notification if e-commerce disabled
- ✅ Client-side only execution to avoid SSR issues

### 4. Component Structure
```
pages/ecommerce/
├── carousels.vue          # Main listing page
└── carousels/
    ├── [id].vue          # Edit page with item management
    └── new.vue           # Creation page
```

### 5. Navigation Configuration
Located in `layouts/default.vue`:
```typescript
...(tenant.value?.ecommerceEnabled
  ? [
      {
        label: t("navigation.ecommerceProducts") || "E-commerce",
        icon: "i-lucide-shopping-bag",
        to: "/ecommerce/products"
      },
      {
        label: t("navigation.ecommerceCarousels") || "Carousels",
        icon: "i-lucide-panels-top-left",
        to: "/ecommerce/carousels"
      }
    ]
  : [])
```

### 6. Translation Keys
**English (`locales/en.json`):**
```json
{
  "navigation": {
    "ecommerceProducts": "E-commerce",
    "ecommerceCarousels": "Carousels"
  }
}
```

**Arabic (`locales/ar.json`):**
```json
{
  "navigation": {
    "ecommerceProducts": "التجارة الإلكترونية",
    "ecommerceCarousels": "العروض المتحركة"
  }
}
```

## Testing Checklist

### Manual Testing
1. **Enable E-commerce**
   - Go to clinic settings
   - Enable e-commerce module
   - Verify carousel link appears in navigation

2. **Navigation Flow**
   - Click "Carousels" in sidebar
   - Should navigate to `/ecommerce/carousels`
   - Verify page loads with redesigned interface

3. **Create Flow**
   - Click "Create Carousel" button
   - Should navigate to `/ecommerce/carousels/new`
   - Fill form and submit
   - Should redirect appropriately based on carousel type

4. **Edit Flow**
   - Click "Edit" on any carousel card
   - Should navigate to `/ecommerce/carousels/[id]`
   - Verify comprehensive editing interface loads

5. **Middleware Protection**
   - Disable e-commerce in settings
   - Try to access carousel routes directly
   - Should redirect to home with warning toast

### URL Testing
Test these URLs directly:
- `http://localhost:3000/ecommerce/carousels`
- `http://localhost:3000/ecommerce/carousels/new`
- `http://localhost:3000/ecommerce/carousels/1` (with valid ID)

### Browser Back/Forward
- Navigate between carousel pages
- Use browser back/forward buttons
- Verify state is maintained correctly

### Mobile Navigation
- Test on mobile viewport
- Verify carousel links appear in mobile menu
- Test touch navigation

## Common Issues & Solutions

### Issue: Navigation link not appearing
**Solution:** Ensure e-commerce is enabled in tenant settings

### Issue: 404 on carousel routes
**Solution:** Verify file structure matches Nuxt routing conventions

### Issue: Middleware not working
**Solution:** Check middleware is properly registered and tenant context is available

### Issue: Translation keys missing
**Solution:** Verify keys exist in both `en.json` and `ar.json`

## Performance Considerations

1. **Lazy Loading**: Components are loaded on-demand
2. **Route Caching**: Nuxt handles route caching automatically
3. **API Calls**: Proper loading states and error handling implemented
4. **Mobile Optimization**: Responsive design for all screen sizes

## Security Notes

1. **Authentication**: All routes require authentication via `auth.global.ts`
2. **Authorization**: E-commerce middleware checks feature access
3. **Tenant Isolation**: All API calls include tenant context
4. **Input Validation**: Forms include proper validation

## Next Steps

1. Test with real data in development environment
2. Verify API endpoints are working correctly
3. Test with different user permission levels
4. Validate mobile experience thoroughly
5. Check accessibility compliance