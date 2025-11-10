# Theme System Type Definitions

This document provides an overview of the TypeScript type definitions for the manual theme selection system.

## Overview

The theme system uses TypeScript to provide type safety across all theme-related operations. Types are organized into several categories:

- **Prisma Model Types**: Direct representations of database models
- **API Types**: Request and response types for API endpoints
- **Service Types**: Types used by theme lookup services
- **Component Types**: Props interfaces for theme components
- **Utility Types**: Type guards and helper types

## File Organization

```
lib/
├── theme-types.ts      # All theme-related type definitions
├── theme.ts            # Theme lookup service with JSDoc
├── index.ts            # Centralized exports
└── TYPES_README.md     # This file

themes/
└── types.ts            # Re-exports from lib/theme-types (for backward compatibility)
```

## Core Types

### Prisma Model Types

#### Theme

Represents a predefined theme in the database.

```typescript
interface Theme {
  id: string              // Unique identifier (cuid)
  key: string             // Theme key for file system lookup (e.g., "clinic")
  name: string            // Display name
  status: ThemeStatus     // "published" | "draft"
  createdAt: Date
  updatedAt: Date
}
```

#### Tenant

Represents a clinic or organization.

```typescript
interface Tenant {
  id: number
  slug: string            // Unique slug for subdomain
  name: string
  status: TenantStatus    // "ACTIVE" | "SUSPENDED" | "INACTIVE"
  customDomain: string | null
  domain: string | null   // For custom domain resolution
  themeId: string | null  // Foreign key to Theme
  createdAt: Date
  updatedAt: Date
  deletedAt: Date | null
}
```

#### TenantWithTheme

Tenant with theme relation included.

```typescript
interface TenantWithTheme extends Tenant {
  theme: Theme | null
}
```

### API Types

#### ThemeListItem

Response type for `GET /api/themes`.

```typescript
interface ThemeListItem {
  id: string
  key: string
  name: string
}
```

#### TenantInfo

Response type for `GET /api/internal/tenant`.

```typescript
interface TenantInfo {
  slug: string
  themeId: string | null
}
```

#### UpdateThemeRequest

Request body for `POST /api/tenants/[slug]/theme`.

```typescript
interface UpdateThemeRequest {
  themeId: string
}
```

#### UpdateThemeResponse

Response type for `POST /api/tenants/[slug]/theme`.

```typescript
interface UpdateThemeResponse {
  id: number
  slug: string
  themeId: string | null
  updatedAt: Date
}
```

#### ApiErrorResponse

Standard error response format.

```typescript
interface ApiErrorResponse {
  error: string
  message?: string
}
```

### Service Types

#### TenantTheme

Returned by theme lookup service functions.

```typescript
interface TenantTheme {
  themeKey: string    // Used for dynamic import
  themeId: string     // Database ID
  themeName: string   // Display name
}
```

### Component Types

#### ThemeLayoutProps

Props interface that all theme layouts must implement.

```typescript
interface ThemeLayoutProps {
  children: ReactNode
  locale: string
  tenantSlug: string
}
```

#### ThemeLayout

Type definition for theme layout components.

```typescript
type ThemeLayout = React.ComponentType<ThemeLayoutProps>
```

#### ThemeHeaderProps

Props for theme header components.

```typescript
interface ThemeHeaderProps {
  locale: string
  tenantSlug: string
}
```

#### ThemeFooterProps

Props for theme footer components.

```typescript
interface ThemeFooterProps {
  locale: string
}
```

#### ThemeNavigationProps

Props for theme navigation components.

```typescript
interface ThemeNavigationProps {
  locale: string
  tenantSlug: string
}
```

## Utility Types and Functions

### Type Guards

#### isThemeStatus

Checks if a value is a valid ThemeStatus.

```typescript
function isThemeStatus(value: unknown): value is ThemeStatus
```

#### isTenantStatus

Checks if a value is a valid TenantStatus.

```typescript
function isTenantStatus(value: unknown): value is TenantStatus
```

#### isAvailableThemeKey

Checks if a theme key is available in the file system.

```typescript
function isAvailableThemeKey(key: string): key is AvailableThemeKey
```

### Available Theme Keys

Type representing theme keys that exist in the file system.

```typescript
type AvailableThemeKey = 'default' | 'clinic' | 'barber'
```

**Note**: Update this type when adding new themes.

## Usage Examples

### Importing Types

```typescript
// Import from centralized location
import type { TenantTheme, ThemeLayoutProps } from '@/lib/theme-types'

// Or use the index file
import type { TenantTheme, ThemeLayoutProps } from '@/lib'

// Backward compatible import (deprecated)
import type { ThemeLayoutProps } from '@/themes/types'
```

### Using Types in Components

```typescript
import type { ThemeLayoutProps } from '@/lib/theme-types'

export default function ClinicLayout({ 
  children, 
  locale, 
  tenantSlug 
}: ThemeLayoutProps) {
  return (
    <div className="clinic-theme">
      <Header locale={locale} tenantSlug={tenantSlug} />
      <main>{children}</main>
      <Footer locale={locale} />
    </div>
  )
}
```

### Using Types in API Routes

```typescript
import type { ThemeListItem, ApiErrorResponse } from '@/lib/theme-types'

export async function GET(): Promise<NextResponse<ThemeListItem[] | ApiErrorResponse>> {
  // Implementation
}
```

### Using Type Guards

```typescript
import { isAvailableThemeKey } from '@/lib/theme-types'

const themeKey = 'clinic'
if (isAvailableThemeKey(themeKey)) {
  // TypeScript knows themeKey is AvailableThemeKey
  const ThemeLayout = await import(`@/themes/${themeKey}/layout`)
}
```

### Using Service Functions

```typescript
import { getTenantTheme, getDefaultTheme } from '@/lib/theme'

// Get tenant theme with fallback
const theme = await getTenantTheme(slug) || await getDefaultTheme()

// Validate theme exists
import { validateThemeExists } from '@/lib/theme'
if (await validateThemeExists('clinic')) {
  // Safe to import
}
```

## Best Practices

1. **Always use types for API responses**: This ensures type safety across the client-server boundary.

2. **Use type guards for runtime validation**: TypeScript types are erased at runtime, so use type guards when validating external data.

3. **Import from centralized locations**: Use `@/lib/theme-types` or `@/lib` for better organization.

4. **Document complex types**: Add JSDoc comments to explain non-obvious type constraints.

5. **Update AvailableThemeKey**: When adding new themes, update the `AvailableThemeKey` type.

6. **Use satisfies for validation schemas**: Ensure Zod schemas match TypeScript types:
   ```typescript
   const schema = z.object({
     themeId: z.string()
   }) satisfies z.ZodType<UpdateThemeRequest>
   ```

## Adding New Types

When adding new theme-related types:

1. Add the type definition to `lib/theme-types.ts`
2. Add JSDoc comments explaining the type's purpose
3. Export the type from `lib/index.ts`
4. Update this README with usage examples
5. If it's a theme key, update `AvailableThemeKey`

## Type Safety Checklist

- [ ] All API routes have typed request/response
- [ ] All theme components use ThemeLayoutProps
- [ ] All service functions have return types
- [ ] Type guards are used for runtime validation
- [ ] JSDoc comments explain complex types
- [ ] New themes update AvailableThemeKey

## Related Documentation

- [Design Document](../../.kiro/specs/manual-theme-selection/design.md)
- [Requirements Document](../../.kiro/specs/manual-theme-selection/requirements.md)
- [Theme Development Guide](../../themes/README.md)
