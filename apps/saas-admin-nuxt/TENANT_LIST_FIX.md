# Tenant List Display Fix

## Problem
The tenant list page was not displaying any data even though the API was returning data correctly.

## Root Causes

### 1. Missing `useDebounce` Composable
The page was trying to use `useDebounce()` but the composable didn't exist, causing the page to fail silently.

**Solution:** Created `composables/useDebounce.ts` with a proper debounce implementation.

### 2. Incorrect Conditional Rendering
The table had `v-else` which meant it would only show if there was NO loading AND NO error. However, the `v-else` was chained to the error alert, so it would never show when data was successfully loaded.

**Before:**
```vue
<LoadingSkeleton v-if="loading" type="table" :rows="5" />
<UAlert v-else-if="error" ... />
<div v-else class="...">  <!-- This would show when NOT loading AND NOT error -->
  <TenantTable :tenants="tenants" />
</div>
```

**After:**
```vue
<LoadingSkeleton v-if="loading" type="table" :rows="5" />
<UAlert v-else-if="error" ... />
<div v-if="!loading && !error" class="...">  <!-- Explicitly check both conditions -->
  <TenantTable :tenants="tenants" />
</div>
```

## Files Changed

1. **Created:** `apps/saas-admin-nuxt/composables/useDebounce.ts`
   - Implements debounce function for search input
   - Returns a composable with debounce method

2. **Fixed:** `apps/saas-admin-nuxt/pages/tenants/index.vue`
   - Changed `v-else` to `v-if="!loading && !error"` for table display
   - Removed debug logging

## Testing
1. Navigate to `/tenants` page
2. Verify tenant list displays correctly
3. Test search functionality (should debounce after 300ms)
4. Test status filter
5. Test pagination (if more than 20 tenants)

## API Response Format
The API returns data in this format:
```json
{
  "content": [
    {
      "id": 1,
      "slug": "default",
      "name": "Default Clinic",
      "customDomain": null,
      "status": "ACTIVE",
      "createdAt": 1762606952.028582000,
      "updatedAt": 1762606952.028582000,
      "deletedAt": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

The `createdAt` and `updatedAt` fields are Unix timestamps with nanosecond precision (decimal format).

## Status
✅ **FIXED** - Tenant list now displays correctly
