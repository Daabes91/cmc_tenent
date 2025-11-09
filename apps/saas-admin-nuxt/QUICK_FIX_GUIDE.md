# Quick Fix Guide - SAAS Admin Nuxt

## Current Status

✅ **Performance Optimized** - Dev server starts in 3-4 seconds  
✅ **Composable Errors Fixed** - No more setup function errors  
✅ **Hydration Warnings Fixed** - ToastContainer properly wrapped  
⚠️ **CORS Error** - Requires API restart  

## To Fix CORS Error

The CORS configuration has been updated in the API, but you need to restart the API server:

### Step 1: Stop Current API Server

If running in terminal:
- Press `Ctrl+C` to stop

If running as background process:
```bash
# Find the process
lsof -i :8080

# Kill it
kill -9 <PID>
```

### Step 2: Restart API Server

```bash
cd apps/api
./gradlew bootRun
```

### Step 3: Verify

1. Wait for API to fully start (look for "Started ClinicApiApplication")
2. Refresh the saas-admin-nuxt page in your browser
3. Try logging in - CORS error should be gone

## What Was Changed

### API Configuration:
- `apps/api/.env.local` - Added CORS origins for ports 3001 and 3002
- `apps/api/src/main/resources/application.yaml` - Added port 3001 to admin-origins

### Frontend Configuration:
- `apps/saas-admin-nuxt/app.vue` - Wrapped ToastContainer in ClientOnly
- `apps/saas-admin-nuxt/nuxt.config.ts` - Optimized for fast dev
- `apps/saas-admin-nuxt/package.json` - Removed unnecessary dependencies
- `apps/saas-admin-nuxt/composables/useTenantManagement.ts` - Fixed composable calls

## Expected Behavior After Fix

✅ No CORS errors  
✅ Login works  
✅ API requests succeed  
✅ No hydration warnings  
✅ Fast dev server startup  

## Docker Build

### Package Lock Sync Error:

If you get "package.json and package-lock.json are not in sync":

```bash
cd apps/saas-admin-nuxt
npm install --package-lock-only
```

### Node Modules Conflict Error:

If you get "cannot replace to directory... node_modules":

```bash
# Clean Docker build cache
docker-compose build --no-cache saas-admin
```

The `.dockerignore` file now excludes node_modules from being copied, preventing conflicts.

## Troubleshooting

### If CORS error persists:

1. **Check API is running on port 8080:**
   ```bash
   lsof -i :8080
   ```

2. **Check which port saas-admin-nuxt is on:**
   - Look at terminal output when you run `pnpm dev`
   - Should show: `http://localhost:3002/saas-admin` or `3001`

3. **Verify API logs show CORS config loaded:**
   - Look for startup logs mentioning CORS configuration

### If hydration warnings persist:

1. **Clear browser cache and hard reload:**
   - Chrome/Edge: `Ctrl+Shift+R` (Windows) or `Cmd+Shift+R` (Mac)
   - Firefox: `Ctrl+F5` (Windows) or `Cmd+Shift+R` (Mac)

2. **Clear Nuxt cache:**
   ```bash
   cd apps/saas-admin-nuxt
   rm -rf .nuxt .output
   pnpm dev
   ```

## Quick Commands

```bash
# Restart everything fresh
cd apps/api && ./gradlew bootRun &
cd apps/saas-admin-nuxt && pnpm dev
```

## Documentation

- Full details: `RESTRUCTURE_SUMMARY.md`
- Performance: `DEV_PERFORMANCE_OPTIMIZATION.md`
- CORS fix: `CORS_FIX.md`
- Composable fix: `COMPOSABLE_SETUP_FIX.md`
