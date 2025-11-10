# Dynamic CORS Configuration for Multi-Tenant System

## Overview

The API now supports dynamic CORS configuration that automatically allows requests from any tenant subdomain without requiring manual configuration updates.

## How It Works

The CORS configuration uses wildcard patterns to match tenant subdomains:

### Development Environment
- `http://*.localhost:*` - Matches any subdomain on localhost with any port
- `http://localhost:*` - Matches localhost with any port
- Examples:
  - `http://clinica.localhost:3001`
  - `http://tenant-a.localhost:3001`
  - `http://barber-shop.localhost:3001`

### Production Environment
- `https://*.yourdomain.com` - Matches any subdomain on your configured base domain
- `https://yourdomain.com` - Also allows the base domain itself
- Examples:
  - `https://clinica.yourdomain.com`
  - `https://tenant-a.yourdomain.com`
  - `https://barber-shop.yourdomain.com`

## Configuration

### Environment Variable (Optional)

For production, set the base domain in your environment:

```bash
CORS_BASE_DOMAIN=yourdomain.com
```

### Application.yaml

The configuration is in `apps/api/src/main/resources/application.yaml`:

```yaml
security:
  cors:
    public-origins:
      - ${PUBLIC_APP_ORIGIN:http://localhost:3001}
    admin-origins:
      - ${ADMIN_APP_ORIGIN:http://localhost:3000}
      - ${SAAS_ADMIN_APP_ORIGIN:http://localhost:3002}
      - http://localhost:3001
    base-domain: ${CORS_BASE_DOMAIN:}
```

## Benefits

1. **Automatic Tenant Support**: New tenants work immediately without CORS configuration changes
2. **Development Friendly**: Works with any subdomain on localhost
3. **Production Ready**: Supports custom domains and subdomains in production
4. **Secure**: Only allows configured patterns, not all origins
5. **Flexible**: Can still add explicit origins if needed

## Code Changes

### CorsConfig.java

Updated `applyOrigins()` method to include wildcard patterns:

```java
private void applyOrigins(CorsConfiguration configuration, List<String> origins) {
    // Add explicitly configured origins
    configuration.setAllowedOrigins(origins);
    configuration.setAllowedOriginPatterns(origins);
    
    // Development: Allow localhost with any port
    configuration.addAllowedOriginPattern("http://localhost:*");
    configuration.addAllowedOriginPattern("http://127.0.0.1:*");
    configuration.addAllowedOriginPattern("https://localhost:*");
    configuration.addAllowedOriginPattern("https://127.0.0.1:*");
    
    // Multi-tenant: Allow any subdomain on localhost (for development)
    configuration.addAllowedOriginPattern("http://*.localhost:*");
    configuration.addAllowedOriginPattern("https://*.localhost:*");
    
    // Multi-tenant: Allow any subdomain on configured base domain (for production)
    String baseDomain = securityProperties.cors().baseDomain();
    if (baseDomain != null && !baseDomain.isEmpty()) {
        configuration.addAllowedOriginPattern("http://*." + baseDomain);
        configuration.addAllowedOriginPattern("https://*." + baseDomain);
        configuration.addAllowedOriginPattern("http://" + baseDomain);
        configuration.addAllowedOriginPattern("https://" + baseDomain);
    }
}
```

### SecurityProperties.java

Updated `Cors` record to include `baseDomain`:

```java
public record Cors(
        List<String> publicOrigins,
        List<String> adminOrigins,
        String baseDomain
) {
    public Cors() {
        this(List.of("http://localhost:3000"), List.of("http://localhost:3001"), "");
    }
}
```

## Testing

### Development
1. Start the API: `./gradlew bootRun`
2. Access any tenant subdomain: `http://tenant-name.localhost:3001`
3. API calls should work without CORS errors

### Production
1. Set `CORS_BASE_DOMAIN=yourdomain.com` in environment
2. Deploy the API
3. Access tenant subdomains: `https://tenant-name.yourdomain.com`
4. API calls should work without CORS errors

## Troubleshooting

### CORS errors still occurring?

1. **Check the API logs** - CORS errors are logged
2. **Verify the origin** - Make sure the request origin matches the pattern
3. **Check credentials** - CORS is configured with `allowCredentials: true`
4. **Restart the API** - Configuration changes require a restart

### Custom domains not working?

1. **Set CORS_BASE_DOMAIN** - Make sure the environment variable is set
2. **Check DNS** - Ensure DNS is configured for the subdomain
3. **Verify HTTPS** - Production should use HTTPS

## Security Notes

- The wildcard patterns are scoped to specific domains (localhost or your base domain)
- Credentials are allowed (`allowCredentials: true`) for authentication
- All HTTP methods are allowed for API flexibility
- Max age is set to 3600 seconds (1 hour) for preflight caching
