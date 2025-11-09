# CORS Error Fix

## Issue

Getting CORS errors when the saas-admin-nuxt app tries to connect to the API.

## Root Cause

The API's CORS configuration in `apps/api/src/main/resources/application.yaml` allows:
- `http://localhost:3000` (admin-nuxt)
- `http://localhost:3002` (saas-admin-nuxt)

However, if port 3002 is already in use, Nuxt automatically uses port 3001, which is not in the allowed origins list.

## Solution Options

### Option 1: Update API Environment Variable (Recommended)

Add to `apps/api/.env.local`:

```env
SAAS_ADMIN_APP_ORIGIN=http://localhost:3001
```

Or if you want to allow both ports:

```env
# Note: Spring Boot doesn't support multiple values in a single env var easily
# You'll need to modify the application.yaml directly
```

### Option 2: Modify application.yaml Directly

Edit `apps/api/src/main/resources/application.yaml`:

```yaml
security:
  cors:
    admin-origins:
      - ${ADMIN_APP_ORIGIN:http://localhost:3000}
      - ${SAAS_ADMIN_APP_ORIGIN:http://localhost:3002}
      - http://localhost:3001  # Add this line
```

### Option 3: Stop Other Services on Port 3002

Make sure port 3002 is available:

```bash
# Find what's using port 3002
lsof -i :3002

# Kill the process if needed
kill -9 <PID>

# Then restart saas-admin-nuxt
cd apps/saas-admin-nuxt
pnpm dev
```

### Option 4: Use Wildcard for Development (Not Recommended for Production)

For development only, you could allow all localhost ports by modifying `CorsConfig.java`:

```java
private CorsConfiguration baseCorsConfiguration() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("http://localhost:*"));
    // ... rest of config
}
```

## Applied Fix

✅ Updated `apps/api/.env.local` with CORS origins:

```bash
PUBLIC_APP_ORIGIN=http://localhost:3001
ADMIN_APP_ORIGIN=http://localhost:3000
SAAS_ADMIN_APP_ORIGIN=http://localhost:3002
```

✅ Updated `apps/api/src/main/resources/application.yaml` to allow both ports 3001 and 3002:

```yaml
security:
  cors:
    admin-origins:
      - ${ADMIN_APP_ORIGIN:http://localhost:3000}
      - ${SAAS_ADMIN_APP_ORIGIN:http://localhost:3002}
      - http://localhost:3001  # Added for flexibility
```

This allows the saas-admin-nuxt app to work on either port 3001 or 3002.

## Next Steps

**Restart the API server** to apply the CORS configuration:

```bash
# Stop the current API server (Ctrl+C or kill the process)
# Then restart it
cd apps/api
./gradlew bootRun
```

Or if running in Docker:
```bash
docker-compose restart api
```

## Verification

After restarting the API:
1. The CORS error should be resolved
2. Login and API requests should work
3. Check browser console - no more CORS errors

## Verification

After applying the fix:

1. Open browser console
2. Navigate to `http://localhost:3001/saas-admin` (or whatever port it's on)
3. Check Network tab - API requests should succeed without CORS errors
4. Look for successful responses from `http://localhost:8080/saas/*` endpoints
