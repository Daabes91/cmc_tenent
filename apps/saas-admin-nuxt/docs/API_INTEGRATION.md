# API Integration Documentation

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [API Endpoints](#api-endpoints)
4. [Request/Response Formats](#requestresponse-formats)
5. [Error Handling](#error-handling)
6. [Rate Limiting](#rate-limiting)
7. [API Composable Usage](#api-composable-usage)
8. [Backend Implementation Status](#backend-implementation-status)

## Overview

The SAAS Manager Admin Panel communicates with the backend API using RESTful HTTP requests. All API calls are authenticated using JWT tokens and follow consistent patterns for requests and responses.

### Base URLs

The application uses two base URLs configured via environment variables:

- **API Base**: `NUXT_PUBLIC_API_BASE` (e.g., `http://localhost:8080`)
- **SAAS API Base**: `NUXT_PUBLIC_SAAS_API_BASE` (e.g., `http://localhost:8080/saas`)

All SAAS-specific endpoints are prefixed with `/saas`.

### API Client

The `useSaasApi` composable provides a centralized interface for all API calls with:
- Automatic JWT token injection
- Error handling and logging
- Request/response transformation
- Automatic logout on 401 responses

## Authentication

### Login Endpoint

**Endpoint**: `POST /saas/auth/login`

**Description**: Authenticates a SAAS manager and returns a JWT token.

**Request**:
```json
{
  "email": "manager@example.com",
  "password": "securePassword123"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "managerName": "John Doe",
  "expiresAt": "2024-12-31T23:59:59Z"
}
```

**Error Responses**:
- `401 Unauthorized`: Invalid credentials
- `403 Forbidden`: Account suspended or not a SAAS manager
- `500 Internal Server Error`: Server error

**Usage**:
```typescript
const { login } = useSaasAuth()

try {
  await login('manager@example.com', 'password123')
  // User is now authenticated and redirected to dashboard
} catch (error) {
  // Handle error (invalid credentials, etc.)
}
```

### Token Management

**Token Storage**: JWT tokens are stored in browser localStorage with the key `saas_auth_token`.

**Token Expiration**: Tokens include an expiration timestamp. The application checks expiration on:
- Every route navigation
- Every API call
- Page load/refresh

**Automatic Logout**: The application automatically logs out users when:
- Token expires
- API returns 401 Unauthorized
- User manually logs out

**Token Format**: JWT tokens include the following claims:
```json
{
  "sub": "manager@example.com",
  "name": "John Doe",
  "role": "SAAS_MANAGER",
  "exp": 1735689599,
  "iat": 1735603199
}
```

## API Endpoints

### Tenant Management

#### List Tenants

**Endpoint**: `GET /saas/tenants`

**Description**: Retrieves a paginated list of tenants with optional search and filters.

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `page` | number | No | Page number (0-indexed), default: 0 |
| `size` | number | No | Items per page, default: 20 |
| `search` | string | No | Search by name or slug |
| `status` | string | No | Filter by status: ACTIVE, INACTIVE, DELETED |
| `sortBy` | string | No | Sort field: name, createdAt, status |
| `sortDirection` | string | No | Sort direction: ASC, DESC |

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "slug": "abc-clinic",
      "name": "ABC Clinic",
      "customDomain": "abcclinic.com",
      "status": "ACTIVE",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-20T14:45:00Z",
      "deletedAt": null
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "number": 0,
  "size": 20
}
```

**Usage**:
```typescript
const api = useSaasApi()

const result = await api.getTenants({
  page: 0,
  size: 20,
  search: 'clinic',
  status: 'ACTIVE',
  sortBy: 'createdAt',
  sortDirection: 'DESC'
})
```

#### Get Tenant Details

**Endpoint**: `GET /saas/tenants/{id}`

**Description**: Retrieves detailed information about a specific tenant.

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | number | Yes | Tenant ID |

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `includeDeleted` | boolean | No | Include deleted tenants, default: false |

**Response** (200 OK):
```json
{
  "id": 1,
  "slug": "abc-clinic",
  "name": "ABC Clinic",
  "customDomain": "abcclinic.com",
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-20T14:45:00Z",
  "deletedAt": null,
  "branding": {
    "primaryColor": "#3B82F6",
    "secondaryColor": "#10B981",
    "logoUrl": "https://example.com/logo.png"
  }
}
```

**Error Responses**:
- `404 Not Found`: Tenant not found
- `403 Forbidden`: Not authorized to view this tenant

**Usage**:
```typescript
const api = useSaasApi()

const tenant = await api.getTenant(1, false)
```

#### Create Tenant

**Endpoint**: `POST /saas/tenants`

**Description**: Creates a new tenant organization with auto-generated admin credentials.

**Request**:
```json
{
  "slug": "new-clinic",
  "name": "New Clinic",
  "customDomain": "newclinic.com"
}
```

**Validation Rules**:
- `slug`: Required, 3-50 characters, lowercase alphanumeric and hyphens only, must be unique
- `name`: Required, 3-100 characters
- `customDomain`: Optional, valid domain format

**Response** (201 Created):
```json
{
  "tenant": {
    "id": 2,
    "slug": "new-clinic",
    "name": "New Clinic",
    "customDomain": "newclinic.com",
    "status": "ACTIVE",
    "createdAt": "2024-01-25T09:00:00Z",
    "updatedAt": "2024-01-25T09:00:00Z",
    "deletedAt": null
  },
  "adminCredentials": {
    "email": "admin@new-clinic",
    "password": "Xy9$mK2pL#qR"
  }
}
```

**Error Responses**:
- `400 Bad Request`: Validation errors (slug already exists, invalid format, etc.)
- `500 Internal Server Error`: Server error during creation

**Usage**:
```typescript
const api = useSaasApi()

const result = await api.createTenant({
  slug: 'new-clinic',
  name: 'New Clinic',
  customDomain: 'newclinic.com'
})

// Display admin credentials to user (shown only once!)
console.log('Admin Email:', result.adminCredentials.email)
console.log('Admin Password:', result.adminCredentials.password)
```

#### Update Tenant

**Endpoint**: `PUT /saas/tenants/{id}`

**Description**: Updates an existing tenant's information.

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | number | Yes | Tenant ID |

**Request**:
```json
{
  "name": "Updated Clinic Name",
  "customDomain": "updatedclinic.com",
  "status": "INACTIVE"
}
```

**Note**: All fields are optional. Only include fields you want to update.

**Response** (200 OK):
```json
{
  "id": 1,
  "slug": "abc-clinic",
  "name": "Updated Clinic Name",
  "customDomain": "updatedclinic.com",
  "status": "INACTIVE",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-25T11:00:00Z",
  "deletedAt": null
}
```

**Error Responses**:
- `400 Bad Request`: Validation errors
- `404 Not Found`: Tenant not found
- `403 Forbidden`: Not authorized to update this tenant

**Usage**:
```typescript
const api = useSaasApi()

const updated = await api.updateTenant(1, {
  name: 'Updated Clinic Name',
  status: 'INACTIVE'
})
```

#### Delete Tenant

**Endpoint**: `DELETE /saas/tenants/{id}`

**Description**: Soft-deletes a tenant (marks as deleted but preserves data).

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | number | Yes | Tenant ID |

**Response** (200 OK):
```json
{
  "message": "Tenant deleted successfully",
  "tenantId": 1,
  "deletedAt": "2024-01-25T12:00:00Z"
}
```

**Error Responses**:
- `404 Not Found`: Tenant not found
- `403 Forbidden`: Not authorized to delete this tenant
- `409 Conflict`: Tenant already deleted

**Usage**:
```typescript
const api = useSaasApi()

await api.deleteTenant(1)
```

### System Metrics (Backend Implementation Required)

#### Get System Metrics

**Endpoint**: `GET /saas/metrics/system`

**Description**: Retrieves system-wide metrics and health indicators.

**Response** (200 OK):
```json
{
  "totalTenants": 50,
  "activeTenants": 45,
  "totalUsers": 1250,
  "activeUsers": 890,
  "apiResponseTime": 145,
  "databaseStatus": "healthy",
  "storageUsedMB": 15360,
  "recentActivity": [
    {
      "id": "act_123",
      "timestamp": "2024-01-25T11:30:00Z",
      "type": "tenant_created",
      "description": "New tenant 'XYZ Clinic' created",
      "managerName": "John Doe"
    }
  ]
}
```

**Usage**:
```typescript
const api = useSaasApi()

const metrics = await api.getSystemMetrics()
```

#### Get Tenant Metrics

**Endpoint**: `GET /saas/metrics/tenants/{id}`

**Description**: Retrieves metrics for a specific tenant.

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | number | Yes | Tenant ID |

**Response** (200 OK):
```json
{
  "tenantId": 1,
  "userCount": 25,
  "staffCount": 5,
  "patientCount": 20,
  "appointmentCount": 150,
  "storageUsedMB": 512,
  "lastActivityAt": "2024-01-25T10:15:00Z"
}
```

**Usage**:
```typescript
const api = useSaasApi()

const metrics = await api.getTenantMetrics(1)
```

### Analytics (Backend Implementation Required)

#### Get Analytics Data

**Endpoint**: `GET /saas/analytics`

**Description**: Retrieves analytics data for specified time range and metrics.

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `startDate` | string | Yes | Start date (ISO 8601 format) |
| `endDate` | string | Yes | End date (ISO 8601 format) |
| `metric` | string | No | Specific metric to retrieve |

**Response** (200 OK):
```json
{
  "tenantGrowth": [
    {
      "date": "2024-01-01",
      "totalTenants": 40,
      "newTenants": 2
    },
    {
      "date": "2024-01-02",
      "totalTenants": 42,
      "newTenants": 2
    }
  ],
  "usageMetrics": {
    "activeUsers": [
      { "date": "2024-01-01", "count": 850 },
      { "date": "2024-01-02", "count": 870 }
    ],
    "appointmentsCreated": [
      { "date": "2024-01-01", "count": 120 },
      { "date": "2024-01-02", "count": 135 }
    ]
  },
  "resourceUsage": {
    "databaseSizeMB": 25600,
    "fileStorageMB": 51200,
    "apiCallVolume": 1500000,
    "avgResponseTimeMs": 145
  }
}
```

**Usage**:
```typescript
const api = useSaasApi()

const analytics = await api.getAnalytics({
  startDate: '2024-01-01',
  endDate: '2024-01-31',
  metric: 'tenantGrowth'
})
```

### Audit Logs (Backend Implementation Required)

#### Get Audit Logs

**Endpoint**: `GET /saas/audit-logs`

**Description**: Retrieves audit logs with optional filters.

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `page` | number | No | Page number (0-indexed), default: 0 |
| `size` | number | No | Items per page, default: 50 |
| `startDate` | string | No | Filter by start date (ISO 8601) |
| `endDate` | string | No | Filter by end date (ISO 8601) |
| `actionType` | string | No | Filter by action type |
| `managerId` | number | No | Filter by manager ID |
| `tenantId` | number | No | Filter by tenant ID |

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "log_123",
      "timestamp": "2024-01-25T11:30:00Z",
      "managerName": "John Doe",
      "managerEmail": "john@example.com",
      "action": "TENANT_CREATED",
      "tenantId": 2,
      "tenantSlug": "new-clinic",
      "details": {
        "name": "New Clinic",
        "customDomain": "newclinic.com"
      }
    }
  ],
  "totalElements": 500,
  "totalPages": 10,
  "number": 0,
  "size": 50
}
```

**Usage**:
```typescript
const api = useSaasApi()

const logs = await api.getAuditLogs({
  page: 0,
  size: 50,
  startDate: '2024-01-01',
  endDate: '2024-01-31',
  actionType: 'TENANT_CREATED'
})
```

#### Export Audit Logs

**Endpoint**: `GET /saas/audit-logs/export`

**Description**: Exports audit logs to CSV format.

**Query Parameters**: Same as Get Audit Logs

**Response** (200 OK):
- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename="audit-logs-{timestamp}.csv"`

**CSV Format**:
```csv
Timestamp,Manager Name,Manager Email,Action,Tenant ID,Tenant Slug,Details
2024-01-25T11:30:00Z,John Doe,john@example.com,TENANT_CREATED,2,new-clinic,"{""name"":""New Clinic""}"
```

**Usage**:
```typescript
const api = useSaasApi()

const csvBlob = await api.exportAuditLogs({
  startDate: '2024-01-01',
  endDate: '2024-01-31'
})

// Trigger download in browser
const url = URL.createObjectURL(csvBlob)
const a = document.createElement('a')
a.href = url
a.download = `audit-logs-${Date.now()}.csv`
a.click()
```

## Request/Response Formats

### Request Headers

All authenticated requests include:

```
Authorization: Bearer {jwt_token}
Content-Type: application/json
Accept: application/json
```

### Response Format

All API responses follow a consistent format:

**Success Response**:
```json
{
  "data": { ... },
  "status": 200,
  "message": "Success"
}
```

**Error Response**:
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "slug",
        "message": "Slug already exists"
      }
    ]
  },
  "status": 400
}
```

### Date/Time Format

All dates and times use ISO 8601 format:
- `2024-01-25T11:30:00Z` (UTC)
- `2024-01-25T11:30:00+02:00` (with timezone offset)

### Pagination Format

Paginated responses follow Spring Data's Page format:

```json
{
  "content": [ ... ],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

## Error Handling

### HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request succeeded |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Validation error or invalid request |
| 401 | Unauthorized | Authentication required or token invalid |
| 403 | Forbidden | Authenticated but not authorized |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource conflict (e.g., duplicate slug) |
| 500 | Internal Server Error | Server error |
| 503 | Service Unavailable | Service temporarily unavailable |

### Error Response Structure

```typescript
interface ApiError {
  error: {
    code: string
    message: string
    details?: Array<{
      field?: string
      message: string
    }>
  }
  status: number
}
```

### Common Error Codes

| Code | Description |
|------|-------------|
| `VALIDATION_ERROR` | Request validation failed |
| `AUTHENTICATION_FAILED` | Invalid credentials |
| `TOKEN_EXPIRED` | JWT token expired |
| `UNAUTHORIZED` | Not authorized for this action |
| `NOT_FOUND` | Resource not found |
| `DUPLICATE_SLUG` | Tenant slug already exists |
| `INTERNAL_ERROR` | Server error |

### Error Handling in Code

The `useSaasApi` composable automatically handles errors:

```typescript
try {
  const result = await api.createTenant(data)
  // Success
} catch (error) {
  if (error.status === 401) {
    // Automatic logout triggered
  } else if (error.status === 400) {
    // Show validation errors
    console.error(error.error.details)
  } else {
    // Show generic error message
    console.error(error.error.message)
  }
}
```

## Rate Limiting

### Client-Side Rate Limiting

The application implements client-side rate limiting:

- **Search Debouncing**: 300ms delay for search inputs
- **API Call Throttling**: Maximum 10 requests per second per endpoint
- **Retry Logic**: Automatic retry with exponential backoff for network errors

### Server-Side Rate Limiting (Backend Implementation)

Backend should implement rate limiting:

- **Per User**: 100 requests per minute
- **Per IP**: 1000 requests per minute
- **Burst Allowance**: 20 requests in 1 second

Rate limit headers in response:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1706184000
```

## API Composable Usage

### Basic Usage

```typescript
// In a Vue component or composable
const api = useSaasApi()

// Make API calls
const tenants = await api.getTenants({ page: 0, size: 20 })
```

### With Error Handling

```typescript
const api = useSaasApi()
const toast = useToast()

try {
  const tenant = await api.createTenant(formData)
  toast.add({
    title: 'Success',
    description: 'Tenant created successfully',
    color: 'green'
  })
} catch (error) {
  toast.add({
    title: 'Error',
    description: error.error?.message || 'Failed to create tenant',
    color: 'red'
  })
}
```

### With Loading States

```typescript
const api = useSaasApi()
const loading = ref(false)

async function loadTenants() {
  loading.value = true
  try {
    const result = await api.getTenants({ page: 0, size: 20 })
    tenants.value = result.content
  } catch (error) {
    console.error('Failed to load tenants:', error)
  } finally {
    loading.value = false
  }
}
```

### With Reactive State

```typescript
const api = useSaasApi()
const tenants = ref([])
const loading = ref(false)
const error = ref(null)

watchEffect(async () => {
  loading.value = true
  error.value = null
  
  try {
    const result = await api.getTenants({ page: 0, size: 20 })
    tenants.value = result.content
  } catch (e) {
    error.value = e
  } finally {
    loading.value = false
  }
})
```

## Backend Implementation Status

### Implemented Endpoints ✅

- `POST /saas/auth/login` - Authentication
- `GET /saas/tenants` - List tenants
- `GET /saas/tenants/{id}` - Get tenant details
- `POST /saas/tenants` - Create tenant
- `PUT /saas/tenants/{id}` - Update tenant
- `DELETE /saas/tenants/{id}` - Delete tenant

### Pending Implementation ⏳

The following endpoints are referenced in the frontend but require backend implementation:

- `GET /saas/metrics/system` - System metrics
- `GET /saas/metrics/tenants/{id}` - Tenant metrics
- `GET /saas/analytics` - Analytics data
- `GET /saas/audit-logs` - Audit logs
- `GET /saas/audit-logs/export` - Export audit logs

**Note**: The frontend includes mock data and placeholder implementations for these endpoints. Update the `useSaasApi` composable once backend endpoints are available.

### Backend Implementation Checklist

For backend developers implementing these endpoints:

1. **System Metrics**
   - [ ] Implement metrics collection service
   - [ ] Create `/saas/metrics/system` endpoint
   - [ ] Create `/saas/metrics/tenants/{id}` endpoint
   - [ ] Add caching for performance
   - [ ] Add tests

2. **Analytics**
   - [ ] Implement analytics data aggregation
   - [ ] Create `/saas/analytics` endpoint
   - [ ] Support date range filtering
   - [ ] Add PDF export functionality
   - [ ] Add tests

3. **Audit Logs**
   - [ ] Implement audit logging service
   - [ ] Create `/saas/audit-logs` endpoint
   - [ ] Create `/saas/audit-logs/export` endpoint
   - [ ] Add filtering and pagination
   - [ ] Add tests

4. **Rate Limiting**
   - [ ] Implement rate limiting middleware
   - [ ] Add rate limit headers to responses
   - [ ] Configure limits per endpoint
   - [ ] Add tests

## Testing API Integration

### Manual Testing

Use tools like curl or Postman to test endpoints:

```bash
# Login
curl -X POST http://localhost:8080/saas/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"manager@example.com","password":"password"}'

# List tenants (with token)
curl -X GET http://localhost:8080/saas/tenants \
  -H "Authorization: Bearer {token}"

# Create tenant
curl -X POST http://localhost:8080/saas/tenants \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"slug":"test-clinic","name":"Test Clinic"}'
```

### Automated Testing

The application includes integration tests for API calls:

```bash
# Run API integration tests
pnpm test test/integration/
```

See [test/README.md](../test/README.md) for testing documentation.

---

**Document Version**: 1.0  
**Last Updated**: 2024  
**Maintained By**: Development Team
