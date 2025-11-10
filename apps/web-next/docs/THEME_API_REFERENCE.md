# Theme System API Reference

Complete API documentation for the Manual Theme Selection system.

## Table of Contents

- [REST API Endpoints](#rest-api-endpoints)
- [TypeScript APIs](#typescript-apis)
- [Database Schema](#database-schema)
- [Error Codes](#error-codes)

## REST API Endpoints

### GET /api/themes

Retrieves all published themes available for tenant selection.

#### Request

```http
GET /api/themes HTTP/1.1
Host: example.com
Accept: application/json
```

#### Response

**Success (200 OK):**

```json
[
  {
    "id": "clx1234567890abcdef",
    "key": "clinic",
    "name": "Clinic Theme"
  },
  {
    "id": "clx0987654321fedcba",
    "key": "barber",
    "name": "Barber Theme"
  },
  {
    "id": "clx1122334455667788",
    "key": "spa",
    "name": "Spa Theme"
  }
]
```

**Error (500 Internal Server Error):**

```json
{
  "error": "Failed to fetch themes"
}
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| id | string | Unique theme identifier (CUID) |
| key | string | Theme key used for file system lookup |
| name | string | Human-readable theme name |

#### Notes

- Only returns themes with `status = "published"`
- Themes are ordered by creation date (newest first)
- No authentication required (public endpoint)

#### Example Usage

**JavaScript/TypeScript:**

```typescript
const themes = await fetch('/api/themes')
  .then(res => res.json())

console.log(themes)
// [{ id: '...', key: 'clinic', name: 'Clinic Theme' }, ...]
```

**cURL:**

```bash
curl http://localhost:3001/api/themes
```

---

### GET /api/internal/tenant

Retrieves current tenant information based on the `x-tenant` header set by middleware.

#### Request

```http
GET /api/internal/tenant HTTP/1.1
Host: example.com
x-tenant: clinic-a
Accept: application/json
```

#### Response

**Success (200 OK):**

```json
{
  "slug": "clinic-a",
  "themeId": "clx1234567890abcdef"
}
```

**Error (400 Bad Request):**

```json
{
  "error": "No tenant"
}
```

**Error (404 Not Found):**

```json
{
  "error": "Tenant not found"
}
```

**Error (500 Internal Server Error):**

```json
{
  "error": "Failed to fetch tenant"
}
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| slug | string | Tenant slug identifier |
| themeId | string \| null | ID of selected theme (null if no theme assigned) |

#### Notes

- Requires `x-tenant` header (automatically set by middleware)
- Internal endpoint used by theme settings page
- Returns minimal tenant information for security

#### Example Usage

**JavaScript/TypeScript:**

```typescript
const tenant = await fetch('/api/internal/tenant')
  .then(res => res.json())

console.log(tenant)
// { slug: 'clinic-a', themeId: 'clx1234567890abcdef' }
```

**cURL:**

```bash
curl http://localhost:3001/api/internal/tenant \
  -H "x-tenant: clinic-a"
```

---

### POST /api/tenants/[slug]/theme

Updates a tenant's theme selection.

#### Request

```http
POST /api/tenants/clinic-a/theme HTTP/1.1
Host: example.com
Content-Type: application/json

{
  "themeId": "clx1234567890abcdef"
}
```

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| slug | string | Tenant slug identifier |

#### Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| themeId | string | Yes | ID of theme to assign to tenant |

#### Response

**Success (200 OK):**

```json
{
  "id": "tenant-id-123",
  "slug": "clinic-a",
  "domain": null,
  "status": "active",
  "themeId": "clx1234567890abcdef",
  "createdAt": "2024-01-01T00:00:00.000Z",
  "updatedAt": "2024-01-02T12:30:00.000Z"
}
```

**Error (400 Bad Request):**

```json
{
  "error": "Invalid request body",
  "details": [
    {
      "field": "themeId",
      "message": "Required"
    }
  ]
}
```

**Error (404 Not Found - Theme):**

```json
{
  "error": "Theme not found"
}
```

**Error (404 Not Found - Tenant):**

```json
{
  "error": "Tenant not found"
}
```

**Error (500 Internal Server Error):**

```json
{
  "error": "Failed to update theme"
}
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| id | string | Tenant unique identifier |
| slug | string | Tenant slug |
| domain | string \| null | Custom domain (if configured) |
| status | string | Tenant status |
| themeId | string | ID of assigned theme |
| createdAt | string | ISO 8601 timestamp |
| updatedAt | string | ISO 8601 timestamp |

#### Notes

- **TODO:** Authentication/authorization not yet implemented
- Theme must exist in database before assignment
- Tenant must exist in database
- Theme change takes effect immediately (no restart required)

#### Example Usage

**JavaScript/TypeScript:**

```typescript
const response = await fetch('/api/tenants/clinic-a/theme', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    themeId: 'clx1234567890abcdef'
  })
})

const tenant = await response.json()
console.log(tenant)
// { id: '...', slug: 'clinic-a', themeId: '...', ... }
```

**cURL:**

```bash
curl -X POST http://localhost:3001/api/tenants/clinic-a/theme \
  -H "Content-Type: application/json" \
  -d '{"themeId": "clx1234567890abcdef"}'
```

---

## TypeScript APIs

### getTenantTheme()

Retrieves a tenant's theme information from the database.

#### Signature

```typescript
async function getTenantTheme(slug: string): Promise<TenantTheme | null>
```

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| slug | string | Tenant slug identifier |

#### Returns

Returns `TenantTheme` object if found, `null` otherwise.

```typescript
interface TenantTheme {
  themeKey: string    // Theme key for file system lookup
  themeId: string     // Theme database ID
  themeName: string   // Human-readable theme name
}
```

#### Example

```typescript
import { getTenantTheme } from '@/lib/theme'

const theme = await getTenantTheme('clinic-a')

if (theme) {
  console.log(theme.themeKey)   // 'clinic'
  console.log(theme.themeName)  // 'Clinic Theme'
}
```

#### Notes

- Returns `null` if tenant not found
- Returns `null` if tenant has no theme assigned
- Includes theme relation in database query

---

### getDefaultTheme()

Returns the default fallback theme.

#### Signature

```typescript
async function getDefaultTheme(): Promise<TenantTheme>
```

#### Returns

Always returns a `TenantTheme` object for the default theme.

```typescript
{
  themeKey: 'default',
  themeId: 'default',
  themeName: 'Default Theme'
}
```

#### Example

```typescript
import { getDefaultTheme } from '@/lib/theme'

const theme = await getDefaultTheme()
console.log(theme.themeKey) // 'default'
```

#### Notes

- Never returns `null`
- Used as fallback when tenant theme not found
- Default theme must exist in `themes/default/` directory

---

### ThemeLayoutProps

Interface for theme layout component props.

#### Definition

```typescript
interface ThemeLayoutProps {
  children: ReactNode
  locale: string
  tenantSlug: string
}
```

#### Fields

| Field | Type | Description |
|-------|------|-------------|
| children | ReactNode | Page content to render inside theme |
| locale | string | Current locale (e.g., 'en', 'ar') |
| tenantSlug | string | Tenant identifier |

#### Example

```typescript
import { ThemeLayoutProps } from '@/themes/types'

export default function MyThemeLayout({ 
  children, 
  locale, 
  tenantSlug 
}: ThemeLayoutProps) {
  return (
    <div data-locale={locale} data-tenant={tenantSlug}>
      {children}
    </div>
  )
}
```

---

### ThemeLayout

Type for theme layout components.

#### Definition

```typescript
type ThemeLayout = React.ComponentType<ThemeLayoutProps>
```

#### Example

```typescript
import { ThemeLayout } from '@/themes/types'

const MyTheme: ThemeLayout = ({ children, locale, tenantSlug }) => {
  return <div>{children}</div>
}

export default MyTheme
```

---

## Database Schema

### Theme Table

Stores available theme definitions.

```sql
CREATE TABLE "Theme" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "key" TEXT NOT NULL UNIQUE,
  "name" TEXT NOT NULL,
  "status" TEXT NOT NULL DEFAULT 'published',
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL
);

CREATE INDEX "Theme_status_idx" ON "Theme"("status");
```

#### Columns

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | TEXT | PRIMARY KEY | Unique identifier (CUID) |
| key | TEXT | UNIQUE, NOT NULL | Theme key for file lookup |
| name | TEXT | NOT NULL | Display name |
| status | TEXT | NOT NULL, DEFAULT 'published' | 'published' or 'draft' |
| createdAt | TIMESTAMP | NOT NULL | Creation timestamp |
| updatedAt | TIMESTAMP | NOT NULL | Last update timestamp |

#### Indexes

- Primary key on `id`
- Unique index on `key`
- Index on `status` for filtering published themes

---

### Tenant Table

Stores tenant information and theme assignments.

```sql
CREATE TABLE "Tenant" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "slug" TEXT NOT NULL UNIQUE,
  "domain" TEXT UNIQUE,
  "status" TEXT NOT NULL DEFAULT 'active',
  "themeId" TEXT,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "Tenant_themeId_fkey" 
    FOREIGN KEY ("themeId") 
    REFERENCES "Theme"("id") 
    ON DELETE SET NULL 
    ON UPDATE CASCADE
);

CREATE INDEX "Tenant_themeId_idx" ON "Tenant"("themeId");
```

#### Columns

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | TEXT | PRIMARY KEY | Unique identifier (CUID) |
| slug | TEXT | UNIQUE, NOT NULL | Tenant slug for subdomain |
| domain | TEXT | UNIQUE, NULLABLE | Custom domain |
| status | TEXT | NOT NULL, DEFAULT 'active' | Tenant status |
| themeId | TEXT | NULLABLE, FOREIGN KEY | Reference to Theme.id |
| createdAt | TIMESTAMP | NOT NULL | Creation timestamp |
| updatedAt | TIMESTAMP | NOT NULL | Last update timestamp |

#### Indexes

- Primary key on `id`
- Unique index on `slug`
- Unique index on `domain`
- Index on `themeId` for joins

#### Foreign Keys

- `themeId` references `Theme.id`
- ON DELETE SET NULL (theme deletion sets themeId to null)
- ON UPDATE CASCADE (theme ID updates cascade)

---

## Error Codes

### HTTP Status Codes

| Code | Description | When Used |
|------|-------------|-----------|
| 200 | OK | Successful request |
| 400 | Bad Request | Invalid request body or missing required fields |
| 404 | Not Found | Resource (tenant or theme) not found |
| 500 | Internal Server Error | Database error or unexpected server error |

### Error Response Format

All error responses follow this format:

```json
{
  "error": "Error message",
  "details": [] // Optional, for validation errors
}
```

### Common Errors

#### Validation Errors

```json
{
  "error": "Invalid request body",
  "details": [
    {
      "field": "themeId",
      "message": "Required"
    }
  ]
}
```

#### Resource Not Found

```json
{
  "error": "Theme not found"
}
```

```json
{
  "error": "Tenant not found"
}
```

#### Missing Headers

```json
{
  "error": "No tenant"
}
```

#### Database Errors

```json
{
  "error": "Failed to fetch themes"
}
```

```json
{
  "error": "Failed to update theme"
}
```

---

## Rate Limiting

**Note:** Rate limiting is not currently implemented but should be added for production use.

Recommended limits:
- GET endpoints: 100 requests per minute per IP
- POST endpoints: 10 requests per minute per IP

---

## Authentication

**Note:** Authentication is not currently implemented. The POST endpoint includes a TODO comment for adding authentication/authorization.

Recommended implementation:
- Require JWT token in Authorization header
- Verify user has permission to modify tenant
- Log all theme changes for audit trail

---

## Versioning

Current API version: **v1** (implicit, no version in URL)

Future versions may use URL versioning:
- `/api/v1/themes`
- `/api/v2/themes`

---

## Support

For issues or questions:
- Review the main documentation: `docs/THEME_SYSTEM.md`
- Check troubleshooting guide: `docs/THEME_SYSTEM.md#troubleshooting`
- Review design document: `.kiro/specs/manual-theme-selection/design.md`
