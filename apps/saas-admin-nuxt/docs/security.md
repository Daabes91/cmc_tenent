# Security Features Documentation

This document describes the security features implemented in the SAAS Manager Admin Panel.

## Overview

The SAAS Manager Admin Panel implements multiple layers of security to protect sensitive data and ensure secure operations:

1. **Automatic Token Refresh** - Prevents session interruption
2. **Session Timeout Management** - Protects against unauthorized access
3. **Rate Limiting** - Prevents API abuse
4. **XSS Prevention** - Sanitizes user inputs
5. **Secure Logging** - Never logs sensitive data
6. **CSRF Token Support** - Protects against cross-site request forgery

## Features

### 1. Automatic Token Refresh

The system automatically refreshes JWT tokens before they expire to maintain uninterrupted sessions.

**Implementation:**
- Checks token expiration every minute
- Refreshes token when less than 5 minutes remain
- Automatically logs out if refresh fails

**Usage:**
```typescript
const { shouldRefreshToken, refreshToken } = useSecurity()

// Check if token needs refresh
if (shouldRefreshToken()) {
  await refreshToken()
}
```

### 2. Session Timeout Management

Sessions automatically expire after 30 minutes of inactivity to protect against unauthorized access.

**Features:**
- 30-minute inactivity timeout
- 2-minute warning before timeout
- Activity tracking (mouse, keyboard, scroll, touch)
- Session extension option

**User Experience:**
- Warning dialog appears 2 minutes before timeout
- User can extend session or logout
- Automatic logout on timeout

**Implementation:**
```typescript
const { initializeSessionMonitoring, extendSession } = useSecurity()

// Initialize in layout or app
onMounted(() => {
  initializeSessionMonitoring()
})

// Extend session when user clicks button
const handleExtend = () => {
  extendSession()
}
```

### 3. Rate Limiting

Client-side rate limiting prevents excessive API calls and protects against abuse.

**Configuration:**
- Default: 100 requests per minute
- Configurable per endpoint
- Automatic request tracking and cleanup

**Implementation:**
```typescript
const { checkRateLimit } = useSecurity()

// Check before making API call
if (!checkRateLimit('/api/endpoint')) {
  // Rate limit exceeded
  throw new Error('Too many requests')
}
```

**Integration:**
Rate limiting is automatically applied to all API calls through `useSaasApi`.

### 4. XSS Prevention

All user inputs are sanitized to prevent cross-site scripting attacks.

**Features:**
- Removes HTML tags
- Removes dangerous protocols (javascript:)
- Removes event handlers (onclick, etc.)
- Recursive object sanitization

**Implementation:**
```typescript
const { sanitizeInput, sanitizeObject } = useSecurity()

// Sanitize single string
const clean = sanitizeInput(userInput)

// Sanitize entire object
const cleanData = sanitizeObject({
  name: '<script>alert("xss")</script>',
  email: 'user@example.com'
})
// Result: { name: 'scriptalert("xss")/script', email: 'user@example.com' }
```

**Automatic Sanitization:**
All request bodies are automatically sanitized in `useSaasApi` before sending to the server.

### 5. Secure Logging

Sensitive data is never logged to prevent exposure in console or log files.

**Protected Fields:**
- password
- token
- jwt
- secret
- apiKey
- api_key
- authorization
- credentials

**Implementation:**
```typescript
const { secureLog } = useSecurity()

// Safe logging - sensitive data is redacted
secureLog('User login attempt', {
  email: 'user@example.com',
  password: 'secret123',  // Will be [REDACTED]
  token: 'jwt-token'      // Will be [REDACTED]
})
```

**Output:**
```
User login attempt {
  email: 'user@example.com',
  password: '[REDACTED]',
  token: '[REDACTED]'
}
```

### 6. CSRF Token Support

The system supports CSRF token handling for backends that require it.

**Token Sources:**
1. Meta tag: `<meta name="csrf-token" content="...">`
2. Cookie: `XSRF-TOKEN`

**Implementation:**
```typescript
const { getCsrfToken } = useSecurity()

// Get CSRF token
const token = getCsrfToken()

// Automatically included in API requests
// via useSaasApi as X-CSRF-TOKEN header
```

## Security Best Practices

### For Developers

1. **Never Log Sensitive Data**
   ```typescript
   // ❌ Bad
   console.log('User data:', { password: user.password })
   
   // ✅ Good
   secureLog('User data:', { email: user.email })
   ```

2. **Always Sanitize User Input**
   ```typescript
   // ❌ Bad
   const data = { name: userInput }
   
   // ✅ Good
   const data = { name: sanitizeInput(userInput) }
   ```

3. **Use Secure API Calls**
   ```typescript
   // ❌ Bad
   await $fetch('/api/endpoint', { body: rawData })
   
   // ✅ Good
   const { apiCall } = useSaasApi()
   await apiCall('/endpoint', { body: rawData })
   ```

4. **Handle Session Timeout**
   ```typescript
   // Initialize session monitoring in main layout
   onMounted(() => {
     initializeSessionMonitoring()
   })
   ```

### For Users

1. **Session Management**
   - Log out when finished
   - Don't leave sessions unattended
   - Extend session when prompted if still working

2. **Password Security**
   - Use strong passwords
   - Don't share credentials
   - Change passwords regularly

3. **Browser Security**
   - Keep browser updated
   - Use secure connections (HTTPS)
   - Clear browser cache on shared computers

## Configuration

### Session Timeout

Modify timeout duration in `composables/useSecurity.ts`:

```typescript
// Session timeout configuration (30 minutes)
const SESSION_TIMEOUT_MS = 30 * 60 * 1000
const WARNING_BEFORE_TIMEOUT_MS = 2 * 60 * 1000 // 2 minutes warning
```

### Rate Limiting

Modify rate limit configuration:

```typescript
// Rate limiting configuration
const DEFAULT_RATE_LIMIT: RateLimitConfig = {
  maxRequests: 100,
  windowMs: 60 * 1000 // 1 minute
}
```

### Token Refresh

Modify refresh threshold:

```typescript
// Refresh if less than 5 minutes until expiry
const REFRESH_THRESHOLD_MS = 5 * 60 * 1000
```

## Testing

### Manual Testing

1. **Session Timeout**
   - Login and wait 28 minutes
   - Verify warning dialog appears
   - Test extend session button
   - Wait 2 more minutes and verify logout

2. **Rate Limiting**
   - Make rapid API calls
   - Verify rate limit notification appears
   - Verify requests are blocked

3. **XSS Prevention**
   - Enter `<script>alert('xss')</script>` in form fields
   - Verify script tags are removed
   - Verify data is sanitized before submission

4. **Token Refresh**
   - Login and monitor network tab
   - Verify token refresh call before expiration
   - Verify seamless session continuation

### Automated Testing

```typescript
// Test XSS sanitization
describe('XSS Prevention', () => {
  it('should remove script tags', () => {
    const { sanitizeInput } = useSecurity()
    const result = sanitizeInput('<script>alert("xss")</script>')
    expect(result).not.toContain('<script>')
  })
})

// Test rate limiting
describe('Rate Limiting', () => {
  it('should block excessive requests', () => {
    const { checkRateLimit } = useSecurity()
    
    // Make 100 requests
    for (let i = 0; i < 100; i++) {
      checkRateLimit('/test')
    }
    
    // 101st request should be blocked
    expect(checkRateLimit('/test')).toBe(false)
  })
})
```

## Troubleshooting

### Session Keeps Timing Out

**Cause:** Activity not being detected
**Solution:** Check that activity listeners are properly initialized

### Token Refresh Fails

**Cause:** Backend refresh endpoint not implemented or token invalid
**Solution:** Verify backend supports `/auth/refresh` endpoint

### Rate Limit Too Restrictive

**Cause:** Default limits too low for your use case
**Solution:** Adjust `DEFAULT_RATE_LIMIT` configuration

### CSRF Token Not Found

**Cause:** Backend not providing CSRF token
**Solution:** Ensure backend sets CSRF token in meta tag or cookie

## Security Checklist

- [x] JWT authentication implemented
- [x] Automatic token refresh
- [x] Session timeout (30 minutes)
- [x] Session timeout warning (2 minutes before)
- [x] Rate limiting (100 req/min)
- [x] XSS prevention (input sanitization)
- [x] Secure logging (sensitive data redaction)
- [x] CSRF token support
- [x] Activity tracking
- [x] Automatic logout on timeout
- [x] Session extension capability

## Future Enhancements

1. **Two-Factor Authentication (2FA)**
   - Add TOTP support
   - SMS verification option

2. **IP Whitelisting**
   - Restrict access by IP address
   - Geo-location restrictions

3. **Audit Trail**
   - Log all security events
   - Failed login attempts tracking

4. **Password Policies**
   - Enforce complexity requirements
   - Password expiration
   - Password history

5. **Biometric Authentication**
   - Fingerprint support
   - Face recognition

## References

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [CSRF Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html)
- [XSS Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)
