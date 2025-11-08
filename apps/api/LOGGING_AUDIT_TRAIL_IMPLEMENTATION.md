# Logging and Audit Trail Implementation

## Overview

This document describes the comprehensive logging and audit trail implementation for the Tenant CRUD API. All logging follows SLF4J best practices with structured logging patterns and appropriate log levels.

## Implementation Summary

### 1. Tenant Creation Logging

**Location:** `TenantManagementService.createTenant()`

**Log Events:**
- **INFO**: Tenant creation initiated with slug, name, and custom domain
- **WARN**: Tenant creation failed due to duplicate slug
- **INFO**: Tenant created successfully with tenant ID, slug, and status
- **INFO**: Admin staff auto-provisioned with staff ID, tenant ID, email, and role
- **INFO**: Full permissions granted with staff ID and tenant ID
- **INFO**: Tenant creation completed with **masked password** (shows `[MASKED]` instead of actual password)

**Example Log Output:**
```
INFO  - Tenant creation initiated - slug: downtown-dental, name: Downtown Dental Clinic, customDomain: dental.example.com
INFO  - Tenant created successfully - tenantId: 5, slug: downtown-dental, status: ACTIVE
INFO  - Admin staff auto-provisioned - staffId: 12, tenantId: 5, email: admin@downtown-dental.clinic.local, role: ADMIN
INFO  - Full permissions granted - staffId: 12, tenantId: 5, modules: ALL, actions: ALL
INFO  - Tenant creation completed successfully - tenantId: 5, slug: downtown-dental, adminEmail: admin@downtown-dental.clinic.local, passwordGenerated: [MASKED]
```

**Security Note:** The generated password is **never logged** in plain text. All log entries show `[MASKED]` for password fields.

### 2. Soft Delete Logging

**Location:** `TenantManagementService.softDeleteTenant()`

**Log Events:**
- **INFO**: Soft delete initiated with tenant ID
- **WARN**: Soft delete failed - tenant not found or already deleted
- **INFO**: Soft delete completed with tenant ID, slug, name, deletedAt timestamp, and new status

**Example Log Output:**
```
INFO  - Tenant soft delete initiated - tenantId: 5
INFO  - Tenant soft deleted successfully - tenantId: 5, slug: downtown-dental, name: Downtown Dental Clinic, deletedAt: 2024-12-08T15:30:00Z, newStatus: INACTIVE
```

### 3. SAAS Manager Authentication Logging

**Location:** `SaasManagerService.authenticate()`

**Log Events:**
- **INFO**: Authentication attempt with email
- **WARN**: Authentication failed - account not active (includes status)
- **WARN**: Authentication failed - invalid credentials
- **WARN**: Authentication failed - account not found
- **INFO**: Authentication successful with manager ID, email, name, and token expiration

**Example Log Output:**
```
INFO  - SAAS Manager authentication attempt - email: manager@saas.com
INFO  - SAAS Manager authentication successful - managerId: 1, email: manager@saas.com, name: John Doe, tokenExpiresAt: 2024-12-08T16:30:00Z
```

**Failed Authentication Examples:**
```
WARN  - SAAS Manager authentication failed - account status: SUSPENDED - email: manager@saas.com
WARN  - SAAS Manager authentication failed - invalid credentials - email: manager@saas.com
WARN  - SAAS Manager authentication failed - account not found - email: unknown@saas.com
```

### 4. JWT Authentication Filter Logging

**Location:** `SaasManagerJwtAuthenticationFilter`

**Log Events:**
- **WARN**: Authentication failed - missing token with method and URI
- **WARN**: Authentication failed - invalid token with method and URI
- **WARN**: Authentication failed - wrong audience with method and URI
- **WARN**: JWT parsing failed with method, URI, and error message
- **ERROR**: JWT validation error with method, URI, and full exception
- **DEBUG**: Authentication successful with subject, roles, method, and URI

**Example Log Output:**
```
WARN  - SAAS Manager authentication failed - missing token - method: POST, uri: /saas/tenants
WARN  - SAAS Manager authentication failed - wrong audience: STAFF - method: POST, uri: /saas/tenants
DEBUG - SAAS Manager authenticated successfully - subject: manager@saas.com, roles: [ROLE_SAAS_MANAGER], method: POST, uri: /saas/tenants
```

### 5. Controller-Level Audit Logging

**Locations:** 
- `TenantManagementController`
- `SaasAuthController`

**Log Events:**
- **INFO**: API request received with endpoint and key parameters
- **INFO**: API response sent with endpoint and status code

**Example Log Output:**
```
INFO  - API request received - POST /saas/tenants - slug: downtown-dental
INFO  - API response sent - POST /saas/tenants - status: 201, tenantId: 5

INFO  - API request received - PUT /saas/tenants/5 - name: Updated Name, customDomain: new.example.com
INFO  - API response sent - PUT /saas/tenants/5 - status: 200

INFO  - API request received - DELETE /saas/tenants/5
INFO  - API response sent - DELETE /saas/tenants/5 - status: 204

INFO  - API request received - POST /saas/auth/login - email: manager@saas.com
INFO  - API response sent - POST /saas/auth/login - status: 200, managerId: 1
```

### 6. Additional Service-Level Logging

**Tenant Retrieval:**
- **DEBUG**: Tenant retrieval requested with ID and includeDeleted flag
- **WARN**: Tenant retrieval failed - not found
- **DEBUG**: Tenant retrieved successfully with details

**Tenant List:**
- **DEBUG**: Tenant list requested with pagination parameters
- **DEBUG**: Tenant list retrieved with total elements and pages

**Tenant Update:**
- **INFO**: Tenant update initiated with ID and new values
- **WARN**: Tenant update failed - not found or deleted
- **INFO**: Tenant updated successfully with change indicators

## Log Levels

### INFO Level
Used for successful operations and important business events:
- Tenant creation completed
- Admin staff provisioned
- Permissions granted
- Authentication successful
- Soft delete completed
- API requests and responses

### WARN Level
Used for expected failures and security-relevant events:
- Duplicate slug on creation
- Invalid credentials
- Account not active
- Tenant not found
- Missing or invalid JWT tokens
- Wrong token audience

### DEBUG Level
Used for detailed operational information:
- Tenant retrieval requests
- Tenant list requests
- Successful JWT authentication with details

### ERROR Level
Used for unexpected system errors:
- JWT validation exceptions
- Unexpected errors during token parsing

## Structured Logging Format

All log entries follow a consistent structured format:

```
<LEVEL> - <Operation> - <Key-Value Pairs>
```

**Key-Value Pairs Include:**
- `tenantId`: Tenant identifier
- `slug`: Tenant slug
- `name`: Tenant name
- `email`: Email address
- `managerId`: SAAS Manager identifier
- `staffId`: Staff user identifier
- `status`: Entity status
- `method`: HTTP method
- `uri`: Request URI
- `passwordGenerated`: Always shown as `[MASKED]`

## Security Considerations

1. **Password Masking**: All generated passwords are masked in logs with `[MASKED]` placeholder
2. **Sensitive Data**: Email addresses are logged for audit purposes but passwords are never logged
3. **Authentication Failures**: All authentication failures are logged with WARN level for security monitoring
4. **Token Validation**: JWT parsing and validation errors are logged with appropriate context

## Audit Trail Coverage

The logging implementation provides complete audit trail for:

✅ **Requirement 1.1**: Tenant creation events with masked passwords  
✅ **Requirement 1.2**: Admin staff auto-provisioning events  
✅ **Requirement 4.1**: Soft delete operations  
✅ **Requirement 6.1**: SAAS Manager authentication attempts  

## Usage Examples

### Monitoring Tenant Creation
```bash
# View all tenant creation events
grep "Tenant creation" application.log

# View successful tenant creations
grep "Tenant creation completed successfully" application.log

# View failed tenant creations
grep "Tenant creation failed" application.log
```

### Monitoring Authentication
```bash
# View all authentication attempts
grep "SAAS Manager authentication attempt" application.log

# View successful authentications
grep "SAAS Manager authentication successful" application.log

# View failed authentications
grep "SAAS Manager authentication failed" application.log
```

### Monitoring Soft Deletes
```bash
# View all soft delete operations
grep "Tenant soft delete" application.log

# View successful soft deletes
grep "Tenant soft deleted successfully" application.log
```

## Testing the Logging

To verify the logging implementation:

1. **Create a tenant** and check logs for:
   - Tenant creation initiated
   - Tenant created successfully
   - Admin staff auto-provisioned
   - Full permissions granted
   - Tenant creation completed (with masked password)

2. **Authenticate as SAAS Manager** and check logs for:
   - Authentication attempt
   - Authentication successful (or failed with reason)

3. **Soft delete a tenant** and check logs for:
   - Soft delete initiated
   - Soft delete completed with timestamp

4. **Attempt invalid operations** and check logs for:
   - Appropriate WARN level messages
   - Clear error descriptions

## Conclusion

The logging implementation provides comprehensive audit trail coverage for all tenant management operations and SAAS Manager authentication events. All logs follow structured patterns with appropriate log levels, and sensitive information (passwords) is properly masked.
