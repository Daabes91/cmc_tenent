# Composable Setup Function Fix

## Issue

After restructuring the app for performance, encountered error:
```
Error: Must be called at the top of a `setup` function
```

This occurred in `useTenantManagement.ts` when calling `useApiErrorHandler()` inside the `fetchTenants` function.

## Root Cause

Vue 3 composables (like `useI18n()`, `useRouter()`, etc.) must be called at the top level of a setup function or another composable, not inside nested functions or callbacks.

The problem was:
```typescript
const fetchTenants = async () => {
  try {
    const { handleError } = useApiErrorHandler() // ❌ Called inside async function
    const { cachedCall } = useApiCache()         // ❌ Called inside async function
    // ...
  }
}
```

## Solution

Move all composable calls to the top level of the parent composable:

```typescript
export const useTenantManagement = () => {
  const api = useSaasApi()
  const router = useRouter()
  const route = useRoute()
  const { handleError } = useApiErrorHandler()     // ✅ Called at top level
  const { cachedCall, generateKey, DEFAULT_TTL } = useApiCache() // ✅ Called at top level

  const fetchTenants = async () => {
    try {
      // Now we can use handleError and cachedCall directly
      // ...
    } catch (err) {
      handleError(err, 'Failed to load tenants')
    }
  }
}
```

## Files Modified

- `apps/saas-admin-nuxt/composables/useTenantManagement.ts`

## Key Takeaway

Always call Vue composables at the top level of your setup function or composable. Never call them:
- Inside callbacks
- Inside async functions
- Inside conditional statements
- Inside loops

This is a fundamental rule of Vue 3 Composition API.
