# Troubleshooting Guide

## Table of Contents

1. [Installation Issues](#installation-issues)
2. [Development Server Issues](#development-server-issues)
3. [Authentication Issues](#authentication-issues)
4. [API Connection Issues](#api-connection-issues)
5. [UI/Display Issues](#ui-display-issues)
6. [Performance Issues](#performance-issues)
7. [Build and Deployment Issues](#build-and-deployment-issues)
8. [Testing Issues](#testing-issues)
9. [Browser-Specific Issues](#browser-specific-issues)
10. [Common Error Messages](#common-error-messages)

## Installation Issues

### Problem: `pnpm install` fails with dependency errors

**Symptoms**:
```
ERR_PNPM_PEER_DEP_ISSUES  Unmet peer dependencies
```

**Solutions**:

1. **Update pnpm to latest version**:
   ```bash
   npm install -g pnpm@latest
   pnpm --version  # Should be 8.x or higher
   ```

2. **Clear pnpm cache**:
   ```bash
   pnpm store prune
   rm -rf node_modules
   pnpm install
   ```

3. **Use legacy peer deps** (if necessary):
   ```bash
   pnpm install --legacy-peer-deps
   ```

4. **Check Node.js version**:
   ```bash
   node --version  # Should be 18.x or higher
   ```

### Problem: `postinstall` script fails

**Symptoms**:
```
Error: Cannot find module '@nuxt/kit'
```

**Solutions**:

1. **Clear Nuxt cache**:
   ```bash
   rm -rf .nuxt
   pnpm install
   ```

2. **Reinstall dependencies**:
   ```bash
   rm -rf node_modules pnpm-lock.yaml
   pnpm install
   ```

3. **Check for conflicting global packages**:
   ```bash
   npm list -g --depth=0
   # Uninstall conflicting Nuxt versions
   npm uninstall -g nuxt
   ```

### Problem: Permission errors during installation

**Symptoms**:
```
EACCES: permission denied
```

**Solutions**:

1. **Fix npm permissions** (macOS/Linux):
   ```bash
   sudo chown -R $(whoami) ~/.npm
   sudo chown -R $(whoami) ~/.pnpm-store
   ```

2. **Don't use sudo** with pnpm:
   ```bash
   # Wrong
   sudo pnpm install
   
   # Correct
   pnpm install
   ```

3. **Check directory permissions**:
   ```bash
   ls -la
   # Ensure you own the directory
   sudo chown -R $(whoami) .
   ```

## Development Server Issues

### Problem: Server won't start on port 3002

**Symptoms**:
```
Error: listen EADDRINUSE: address already in use :::3002
```

**Solutions**:

1. **Find and kill process using port 3002**:
   ```bash
   # macOS/Linux
   lsof -i :3002
   kill -9 <PID>
   
   # Or use a different port
   pnpm dev -- --port 3003
   ```

2. **Check for zombie processes**:
   ```bash
   ps aux | grep nuxt
   kill -9 <PID>
   ```

3. **Restart your terminal/IDE**

### Problem: Hot reload not working

**Symptoms**:
- Changes to code don't reflect in browser
- Need to manually refresh

**Solutions**:

1. **Clear Nuxt cache**:
   ```bash
   rm -rf .nuxt
   pnpm dev
   ```

2. **Check file watcher limits** (Linux):
   ```bash
   # Increase file watcher limit
   echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf
   sudo sysctl -p
   ```

3. **Disable browser cache**:
   - Open DevTools (F12)
   - Go to Network tab
   - Check "Disable cache"

4. **Try hard reload**:
   - Cmd+Shift+R (Mac)
   - Ctrl+Shift+R (Windows/Linux)

### Problem: Server crashes with memory errors

**Symptoms**:
```
FATAL ERROR: Reached heap limit Allocation failed - JavaScript heap out of memory
```

**Solutions**:

1. **Increase Node.js memory limit**:
   ```bash
   # In package.json, update dev script:
   "dev": "NODE_OPTIONS='--max-old-space-size=4096' nuxt dev --port 3002"
   ```

2. **Close other applications** to free up memory

3. **Check for memory leaks**:
   - Open DevTools → Memory tab
   - Take heap snapshots
   - Look for growing memory usage

## Authentication Issues

### Problem: Cannot log in with valid credentials

**Symptoms**:
- Login form submits but returns error
- "Invalid credentials" message appears

**Solutions**:

1. **Verify backend is running**:
   ```bash
   curl http://localhost:8080/saas/auth/login \
     -X POST \
     -H "Content-Type: application/json" \
     -d '{"email":"your@email.com","password":"yourpassword"}'
   ```

2. **Check API base URL**:
   ```bash
   # In .env file
   cat .env | grep API_BASE
   # Should match your backend URL
   ```

3. **Clear localStorage**:
   ```javascript
   // In browser console
   localStorage.clear()
   location.reload()
   ```

4. **Check CORS configuration** in backend:
   - Ensure backend allows requests from `http://localhost:3002`
   - Check browser console for CORS errors

5. **Verify credentials** with backend team

### Problem: Logged out unexpectedly

**Symptoms**:
- Redirected to login page randomly
- "Session expired" message

**Solutions**:

1. **Check token expiration**:
   ```javascript
   // In browser console
   const token = localStorage.getItem('saas_auth_token')
   console.log(token)
   // Decode JWT to check expiration
   ```

2. **Verify backend token expiration settings**:
   - Check if backend tokens expire too quickly
   - Request longer token expiration from backend team

3. **Check for 401 responses**:
   - Open DevTools → Network tab
   - Look for failed API calls with 401 status
   - Check backend logs for authentication errors

4. **Disable browser extensions** that might interfere:
   - Privacy extensions
   - Ad blockers
   - Cookie managers

### Problem: "Token expired" error immediately after login

**Symptoms**:
- Login succeeds but immediately logs out
- Token expiration error in console

**Solutions**:

1. **Check system clock**:
   ```bash
   date
   # Ensure system time is correct
   ```

2. **Verify token expiration in response**:
   ```javascript
   // Check login response in Network tab
   // expiresAt should be in the future
   ```

3. **Check timezone issues**:
   - Backend and frontend should use UTC
   - Check for timezone conversion errors

## API Connection Issues

### Problem: API calls fail with network errors

**Symptoms**:
```
Failed to fetch
Network request failed
```

**Solutions**:

1. **Verify backend is running**:
   ```bash
   curl http://localhost:8080/health
   ```

2. **Check environment variables**:
   ```bash
   cat .env
   # Verify NUXT_PUBLIC_API_BASE is correct
   ```

3. **Test API endpoint directly**:
   ```bash
   curl http://localhost:8080/saas/tenants \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

4. **Check firewall/antivirus**:
   - Temporarily disable to test
   - Add exception for localhost:8080

5. **Check proxy settings**:
   - Disable VPN
   - Check system proxy configuration

### Problem: CORS errors in browser console

**Symptoms**:
```
Access to fetch at 'http://localhost:8080/saas/tenants' from origin 'http://localhost:3002' has been blocked by CORS policy
```

**Solutions**:

1. **Configure backend CORS** (Spring Boot example):
   ```java
   @Configuration
   public class CorsConfig {
       @Bean
       public WebMvcConfigurer corsConfigurer() {
           return new WebMvcConfigurer() {
               @Override
               public void addCorsMappings(CorsRegistry registry) {
                   registry.addMapping("/**")
                       .allowedOrigins("http://localhost:3002")
                       .allowedMethods("GET", "POST", "PUT", "DELETE")
                       .allowedHeaders("*")
                       .allowCredentials(true);
               }
           };
       }
   }
   ```

2. **Use proxy in development** (nuxt.config.ts):
   ```typescript
   export default defineNuxtConfig({
     nitro: {
       devProxy: {
         '/api': {
           target: 'http://localhost:8080',
           changeOrigin: true
         }
       }
     }
   })
   ```

3. **Contact backend team** to enable CORS

### Problem: API returns 401 for all requests

**Symptoms**:
- All API calls fail with 401
- Even after successful login

**Solutions**:

1. **Check Authorization header**:
   ```javascript
   // In browser DevTools → Network tab
   // Check request headers for:
   // Authorization: Bearer <token>
   ```

2. **Verify token is stored**:
   ```javascript
   // In browser console
   localStorage.getItem('saas_auth_token')
   ```

3. **Check token format**:
   - Should be a valid JWT
   - Should not have extra quotes or spaces

4. **Verify backend JWT validation**:
   - Check backend logs for JWT errors
   - Ensure backend uses same secret key

## UI/Display Issues

### Problem: Blank white screen

**Symptoms**:
- Application loads but shows nothing
- No errors in console

**Solutions**:

1. **Check browser console** for JavaScript errors:
   - Press F12 to open DevTools
   - Look for red error messages

2. **Clear browser cache**:
   - Hard reload: Cmd+Shift+R (Mac) or Ctrl+Shift+R (Windows)
   - Or clear cache in browser settings

3. **Check .nuxt directory**:
   ```bash
   ls -la .nuxt
   # Should contain generated files
   # If missing or empty:
   rm -rf .nuxt
   pnpm dev
   ```

4. **Verify environment variables**:
   ```bash
   cat .env
   # Ensure all required variables are set
   ```

### Problem: Translations showing as keys

**Symptoms**:
- UI shows `auth.login.title` instead of "Login"
- Translation keys visible instead of text

**Solutions**:

1. **Verify locale files exist**:
   ```bash
   ls -la locales/
   # Should show en.json and ar.json
   ```

2. **Check i18n configuration**:
   ```bash
   cat nuxt.config.ts | grep i18n
   ```

3. **Clear Nuxt cache**:
   ```bash
   rm -rf .nuxt
   pnpm dev
   ```

4. **Check for JSON syntax errors** in locale files:
   ```bash
   # Validate JSON
   cat locales/en.json | jq .
   ```

### Problem: Styles not loading or broken layout

**Symptoms**:
- No colors or styling
- Layout is broken
- Elements overlapping

**Solutions**:

1. **Clear Tailwind cache**:
   ```bash
   rm -rf .nuxt/tailwind
   pnpm dev
   ```

2. **Check for CSS errors** in browser console

3. **Verify Nuxt UI is installed**:
   ```bash
   pnpm list @nuxt/ui
   ```

4. **Check for conflicting CSS**:
   - Disable browser extensions
   - Check for custom CSS overrides

5. **Rebuild application**:
   ```bash
   rm -rf .nuxt node_modules
   pnpm install
   pnpm dev
   ```

### Problem: RTL layout not working for Arabic

**Symptoms**:
- Arabic text appears but layout is still LTR
- Elements not mirrored

**Solutions**:

1. **Check RTL plugin**:
   ```bash
   cat plugins/rtl-direction.client.ts
   ```

2. **Verify HTML dir attribute**:
   ```javascript
   // In browser console
   document.documentElement.dir
   // Should be 'rtl' for Arabic
   ```

3. **Clear cache and reload**:
   ```bash
   rm -rf .nuxt
   pnpm dev
   ```

4. **Check Tailwind RTL configuration**:
   ```bash
   cat nuxt.config.ts | grep rtl
   ```

## Performance Issues

### Problem: Application is slow or laggy

**Symptoms**:
- Slow page loads
- Delayed interactions
- Choppy animations

**Solutions**:

1. **Check Network tab** in DevTools:
   - Look for slow API calls
   - Check for large file downloads
   - Verify API response times

2. **Check for memory leaks**:
   - DevTools → Memory tab
   - Take heap snapshots over time
   - Look for growing memory usage

3. **Disable browser extensions**:
   - Test in incognito/private mode
   - Disable extensions one by one

4. **Use production build**:
   ```bash
   pnpm build
   pnpm preview
   # Production build is much faster
   ```

5. **Check backend performance**:
   - Verify database queries are optimized
   - Check for N+1 query problems
   - Monitor backend response times

### Problem: Large bundle size

**Symptoms**:
- Slow initial load
- Large JavaScript files

**Solutions**:

1. **Analyze bundle**:
   ```bash
   pnpm build --analyze
   ```

2. **Check for duplicate dependencies**:
   ```bash
   pnpm list --depth=0
   ```

3. **Lazy load components**:
   ```vue
   <script setup>
   const MyComponent = defineAsyncComponent(() => 
     import('~/components/MyComponent.vue')
   )
   </script>
   ```

4. **Optimize images**:
   - Use WebP format
   - Compress images
   - Use appropriate sizes

## Build and Deployment Issues

### Problem: Build fails with TypeScript errors

**Symptoms**:
```
Type error: Cannot find module '~/composables/useSaasAuth'
```

**Solutions**:

1. **Run type check**:
   ```bash
   pnpm nuxt typecheck
   ```

2. **Clear Nuxt cache**:
   ```bash
   rm -rf .nuxt
   pnpm build
   ```

3. **Check tsconfig.json**:
   ```bash
   cat .nuxt/tsconfig.json
   ```

4. **Verify auto-imports**:
   ```bash
   cat .nuxt/imports.d.ts
   ```

### Problem: Docker build fails

**Symptoms**:
```
ERROR [builder 5/6] RUN npm run build
```

**Solutions**:

1. **Check Dockerfile syntax**:
   ```bash
   docker build --no-cache -t saas-admin .
   ```

2. **Verify .dockerignore**:
   ```bash
   cat .dockerignore
   # Should exclude node_modules, .nuxt, etc.
   ```

3. **Check environment variables**:
   ```bash
   # Ensure .env file exists
   cp .env.example .env
   ```

4. **Increase Docker memory**:
   - Docker Desktop → Settings → Resources
   - Increase memory to 4GB+

5. **Build locally first**:
   ```bash
   pnpm build
   # If this works, Docker should too
   ```

### Problem: Production build works but shows errors

**Symptoms**:
- Build succeeds
- Application runs but has errors
- Features don't work in production

**Solutions**:

1. **Check environment variables**:
   ```bash
   # Ensure production .env is configured
   cat .env
   ```

2. **Test production build locally**:
   ```bash
   pnpm build
   pnpm preview
   ```

3. **Check browser console** for errors

4. **Verify API URLs** are correct for production

5. **Check for hardcoded localhost URLs**:
   ```bash
   grep -r "localhost" pages/ components/ composables/
   ```

## Testing Issues

### Problem: Tests fail to run

**Symptoms**:
```
Error: Cannot find module 'vitest'
```

**Solutions**:

1. **Install test dependencies**:
   ```bash
   pnpm install -D vitest @vue/test-utils happy-dom
   ```

2. **Check vitest.config.ts**:
   ```bash
   cat vitest.config.ts
   ```

3. **Clear cache**:
   ```bash
   rm -rf node_modules/.vite
   pnpm test
   ```

### Problem: Tests pass locally but fail in CI

**Symptoms**:
- Tests work on your machine
- Fail in GitHub Actions/CI

**Solutions**:

1. **Check Node.js version** in CI matches local

2. **Ensure all dependencies are installed**:
   ```yaml
   # In CI config
   - run: pnpm install --frozen-lockfile
   ```

3. **Check for environment-specific issues**:
   - Timezone differences
   - File path differences (Windows vs Linux)
   - Missing environment variables

4. **Run tests in CI mode locally**:
   ```bash
   CI=true pnpm test
   ```

## Browser-Specific Issues

### Chrome/Edge

**Problem**: Service worker conflicts

**Solution**:
1. Open DevTools → Application → Service Workers
2. Unregister all service workers
3. Clear site data
4. Reload

### Firefox

**Problem**: localStorage not persisting

**Solution**:
1. Check Privacy settings
2. Ensure cookies are enabled
3. Check for "Delete cookies and site data when Firefox is closed"

### Safari

**Problem**: Fetch API issues

**Solution**:
1. Update Safari to latest version
2. Check for "Prevent cross-site tracking" setting
3. Test in private window

## Common Error Messages

### `Cannot read property 'value' of undefined`

**Cause**: Accessing reactive ref without `.value`

**Solution**:
```typescript
// Wrong
const count = ref(0)
console.log(count)

// Correct
const count = ref(0)
console.log(count.value)
```

### `Hydration mismatch`

**Cause**: Server-rendered HTML doesn't match client

**Solution**:
1. Use `<ClientOnly>` for client-only components
2. Avoid using `Date.now()` or random values in templates
3. Check for browser extensions modifying HTML

### `Module not found: Can't resolve '~/composables/...'`

**Cause**: Auto-import not working

**Solution**:
1. Restart dev server
2. Clear `.nuxt` directory
3. Check file exists and is exported correctly

### `[nuxt] A composable that requires access to the Nuxt instance was called outside of a plugin, Nuxt hook, Nuxt middleware, or Vue setup()`

**Cause**: Using Nuxt composable outside of Vue context

**Solution**:
```typescript
// Wrong
const api = useSaasApi()
export default api

// Correct
export default defineNuxtPlugin(() => {
  const api = useSaasApi()
  return { provide: { api } }
})
```

## Getting Additional Help

If your issue is not covered here:

1. **Check browser console** for error messages
2. **Check backend logs** for API errors
3. **Search GitHub issues** for similar problems
4. **Ask in team chat** with:
   - Error message
   - Steps to reproduce
   - Screenshots
   - Browser/OS information
5. **Create detailed bug report** with:
   - Environment details
   - Reproduction steps
   - Expected vs actual behavior
   - Relevant logs/screenshots

## Debug Mode

Enable verbose logging:

```javascript
// In browser console
localStorage.setItem('debug', 'true')
location.reload()
```

This enables:
- API request/response logging
- State change logging
- Performance timing logs

Disable with:
```javascript
localStorage.removeItem('debug')
location.reload()
```

---

**Document Version**: 1.0  
**Last Updated**: 2024  
**Maintained By**: Development Team
