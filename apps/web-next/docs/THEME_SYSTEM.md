# Manual Theme Selection System

## Overview

The Manual Theme Selection system enables developers to hand-build complete theme implementations (layouts, components, styles) and allows tenants to select from these predefined themes. Theme changes take effect immediately without requiring application redeployment.

## Table of Contents

- [Architecture](#architecture)
- [Theme Development](#theme-development)
- [API Endpoints](#api-endpoints)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)

## Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                      Client Request                          │
│              (subdomain or custom domain)                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Edge Middleware                             │
│  - Extract hostname from request                             │
│  - Resolve tenant slug (subdomain/custom domain)             │
│  - Set x-tenant header                                       │
│  - Rewrite to error page if tenant not found                 │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Root Layout (SSR)                           │
│  - Read x-tenant header                                      │
│  - Query database for tenant + theme                         │
│  - Dynamic import theme layout                               │
│  - Render with themed layout                                 │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Theme Layout Component                      │
│  - Theme-specific header, footer, navigation                 │
│  - Theme-specific styling and components                     │
│  - Wraps page content                                        │
└─────────────────────────────────────────────────────────────┘
```

### Data Flow

1. **Request arrives** → Edge Middleware resolves tenant from hostname
2. **Tenant identified** → `x-tenant` header set with tenant slug
3. **Root layout renders** → Reads `x-tenant`, queries database for theme
4. **Theme loaded** → Dynamic import of theme layout component
5. **Page rendered** → Content wrapped in themed layout

### Database Schema

The system uses two main tables:

**Theme Table:**
- `id` (String, Primary Key): Unique theme identifier
- `key` (String, Unique): Theme key used for file system lookup (e.g., "clinic", "barber")
- `name` (String): Display name for the theme
- `status` (String): "published" or "draft"
- `createdAt`, `updatedAt` (DateTime): Timestamps

**Tenant Table:**
- `id` (String, Primary Key): Unique tenant identifier
- `slug` (String, Unique): Tenant slug for subdomain resolution
- `domain` (String, Unique, Optional): Custom domain
- `themeId` (String, Foreign Key): Reference to selected theme
- `status` (String): Tenant status
- `createdAt`, `updatedAt` (DateTime): Timestamps

## Theme Development

### Creating a New Theme

Follow these steps to create a new theme:

#### 1. Create Theme Directory Structure

```bash
cd apps/web-next/themes
mkdir my-theme
cd my-theme
mkdir components styles
```

Your theme directory should follow this structure:

```
themes/
└── my-theme/
    ├── layout.tsx          # Main layout component (required)
    ├── components/         # Theme-specific components
    │   ├── Header.tsx
    │   ├── Footer.tsx
    │   └── Navigation.tsx
    └── styles/
        └── theme.css       # Theme-specific styles
```

#### 2. Create Layout Component

Create `themes/my-theme/layout.tsx`:

```typescript
import { ThemeLayoutProps } from '../types'
import Header from './components/Header'
import Footer from './components/Footer'
import './styles/theme.css'

export default function MyThemeLayout({ 
  children, 
  locale, 
  tenantSlug 
}: ThemeLayoutProps) {
  return (
    <div className="my-theme">
      <Header locale={locale} tenantSlug={tenantSlug} />
      <main className="my-theme-main">
        {children}
      </main>
      <Footer locale={locale} />
    </div>
  )
}
```

**Important:** The layout component must:
- Accept `ThemeLayoutProps` interface
- Export as default
- Render the `children` prop
- Be a valid React Server Component

#### 3. Create Theme Components

Create `themes/my-theme/components/Header.tsx`:

```typescript
interface HeaderProps {
  locale: string
  tenantSlug: string
}

export default function Header({ locale, tenantSlug }: HeaderProps) {
  return (
    <header className="my-theme-header">
      <nav>
        <a href={`/${locale}`}>Home</a>
        <a href={`/${locale}/about`}>About</a>
        <a href={`/${locale}/contact`}>Contact</a>
      </nav>
    </header>
  )
}
```

Create `themes/my-theme/components/Footer.tsx`:

```typescript
interface FooterProps {
  locale: string
}

export default function Footer({ locale }: FooterProps) {
  return (
    <footer className="my-theme-footer">
      <p>&copy; 2024 My Theme. All rights reserved.</p>
    </footer>
  )
}
```

#### 4. Add Theme Styles

Create `themes/my-theme/styles/theme.css`:

```css
.my-theme {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.my-theme-header {
  background-color: #1a1a1a;
  color: white;
  padding: 1rem 2rem;
}

.my-theme-main {
  flex: 1;
  padding: 2rem;
}

.my-theme-footer {
  background-color: #f5f5f5;
  padding: 2rem;
  text-align: center;
}
```

#### 5. Add Theme to Database

Use Prisma Studio or create a migration:

```bash
cd apps/web-next
npx prisma studio
```

Or add to your seed file (`prisma/seed.ts`):

```typescript
await prisma.theme.create({
  data: {
    key: 'my-theme',
    name: 'My Theme',
    status: 'published'
  }
})
```

#### 6. Test Your Theme

1. Assign the theme to a test tenant:

```typescript
await prisma.tenant.update({
  where: { slug: 'test-tenant' },
  data: { themeId: 'your-theme-id' }
})
```

2. Visit the tenant's subdomain: `http://test-tenant.localhost:3001`

3. Verify the theme loads correctly

### Theme Development Best Practices

#### Use Shared Components

Themes can import shared components from the main application:

```typescript
import { Button } from '@/components/ui/button'
import { useTranslations } from 'next-intl'

export default function Header({ locale }: { locale: string }) {
  const t = useTranslations('common')
  
  return (
    <header>
      <Button>{t('getStarted')}</Button>
    </header>
  )
}
```

#### Responsive Design

Ensure your theme works on all screen sizes:

```css
.my-theme-header {
  padding: 1rem 2rem;
}

@media (max-width: 768px) {
  .my-theme-header {
    padding: 0.5rem 1rem;
  }
}
```

#### Accessibility

Follow accessibility best practices:

```typescript
export default function Header() {
  return (
    <header role="banner">
      <nav role="navigation" aria-label="Main navigation">
        <a href="/" aria-label="Home page">Home</a>
      </nav>
    </header>
  )
}
```

#### Performance

- Keep theme bundles small
- Use Next.js Image component for images
- Lazy load heavy components
- Minimize CSS file size

#### Internationalization

Support multiple languages:

```typescript
import { useTranslations } from 'next-intl'

export default function Footer({ locale }: { locale: string }) {
  const t = useTranslations('footer')
  
  return (
    <footer>
      <p>{t('copyright', { year: new Date().getFullYear() })}</p>
    </footer>
  )
}
```

### Theme TypeScript Types

The system provides TypeScript types for theme development:

```typescript
// themes/types.ts
import { ReactNode } from 'react'

export interface ThemeLayoutProps {
  children: ReactNode
  locale: string
  tenantSlug: string
}

export type ThemeLayout = React.ComponentType<ThemeLayoutProps>
```

### Testing Your Theme

#### Manual Testing Checklist

- [ ] Theme loads on tenant subdomain
- [ ] All pages render correctly
- [ ] Navigation works properly
- [ ] Responsive design works on mobile/tablet/desktop
- [ ] Theme styles don't conflict with page styles
- [ ] Internationalization works for all supported languages
- [ ] Images and assets load correctly
- [ ] Theme performs well (no layout shifts, fast load times)

#### Automated Testing

See `test/theme-integration.test.js` for integration tests.

Run tests:

```bash
npm test -- theme-integration.test.js
```

## API Endpoints

### GET /api/themes

Returns all published themes available for selection.

**Request:**
```http
GET /api/themes HTTP/1.1
Host: example.com
```

**Response:**
```json
[
  {
    "id": "clx1234567890",
    "key": "clinic",
    "name": "Clinic Theme"
  },
  {
    "id": "clx0987654321",
    "key": "barber",
    "name": "Barber Theme"
  }
]
```

**Status Codes:**
- `200 OK`: Success
- `500 Internal Server Error`: Database error

### GET /api/internal/tenant

Returns current tenant information based on the `x-tenant` header.

**Request:**
```http
GET /api/internal/tenant HTTP/1.1
Host: example.com
x-tenant: clinic-a
```

**Response:**
```json
{
  "slug": "clinic-a",
  "themeId": "clx1234567890"
}
```

**Status Codes:**
- `200 OK`: Success
- `400 Bad Request`: No tenant header present
- `404 Not Found`: Tenant not found
- `500 Internal Server Error`: Database error

### POST /api/tenants/[slug]/theme

Updates a tenant's theme selection.

**Request:**
```http
POST /api/tenants/clinic-a/theme HTTP/1.1
Host: example.com
Content-Type: application/json

{
  "themeId": "clx1234567890"
}
```

**Response:**
```json
{
  "id": "tenant-id",
  "slug": "clinic-a",
  "themeId": "clx1234567890",
  "status": "active",
  "createdAt": "2024-01-01T00:00:00.000Z",
  "updatedAt": "2024-01-02T00:00:00.000Z"
}
```

**Status Codes:**
- `200 OK`: Theme updated successfully
- `400 Bad Request`: Invalid request body
- `404 Not Found`: Theme or tenant not found
- `500 Internal Server Error`: Database error

**Validation:**
- `themeId` must be a valid string
- Theme must exist in database
- Tenant must exist in database

## Deployment

### Prerequisites

Before deploying the theme system, ensure:

1. PostgreSQL database is set up and accessible
2. Environment variables are configured
3. All theme files are committed to repository
4. Database migrations are ready

### Environment Variables

Required environment variables:

```bash
# Database
DATABASE_URL="postgresql://user:password@host:5432/dbname"

# Application
APP_BASE_DOMAIN="yourdomain.com"
NEXT_PUBLIC_BASE_DOMAIN="yourdomain.com"
NEXT_PUBLIC_DEFAULT_TENANT="default"

# Node Environment
NODE_ENV="production"
```

### Deployment Steps

#### 1. Run Database Migrations

```bash
cd apps/web-next
npx prisma migrate deploy
```

This creates the `Theme` and `Tenant` tables if they don't exist.

#### 2. Seed Initial Data

```bash
npx prisma db seed
```

This creates:
- Default themes (clinic, barber)
- Sample tenants for testing

#### 3. Generate Prisma Client

```bash
npx prisma generate
```

#### 4. Build Application

```bash
npm run build
```

Verify that:
- All theme layouts compile successfully
- No TypeScript errors
- Build completes without warnings

#### 5. Deploy to Production

Deploy using your preferred method:

**Vercel:**
```bash
vercel --prod
```

**Docker:**
```bash
docker build -t web-next .
docker run -p 3000:3000 web-next
```

**Custom Server:**
```bash
npm run start
```

#### 6. Verify Deployment

1. Check that themes load correctly:
   - Visit tenant subdomains
   - Verify theme rendering
   - Test theme switching

2. Test API endpoints:
   - GET /api/themes
   - GET /api/internal/tenant
   - POST /api/tenants/[slug]/theme

3. Monitor logs for errors

### DNS Configuration

#### Subdomain Setup

Configure wildcard DNS for subdomains:

```
*.yourdomain.com  A  your-server-ip
```

Or for specific subdomains:

```
clinic-a.yourdomain.com  A  your-server-ip
clinic-b.yourdomain.com  A  your-server-ip
```

#### Custom Domain Setup

For custom domains, tenants need to:

1. Add CNAME record pointing to your domain:
   ```
   www.tenant-domain.com  CNAME  yourdomain.com
   ```

2. Update tenant record in database:
   ```sql
   UPDATE "Tenant" 
   SET domain = 'www.tenant-domain.com' 
   WHERE slug = 'tenant-slug';
   ```

### Zero-Downtime Updates

Theme changes don't require application restart:

1. Tenant selects new theme via admin panel
2. Database updated immediately
3. Next request loads new theme
4. No deployment or restart needed

For theme file updates:

1. Deploy new theme files
2. Existing themes continue working
3. New theme available immediately after deployment

### Monitoring

Monitor these metrics:

- Theme load times
- Database query performance
- Failed theme lookups
- Tenant resolution errors

Use logging to track:

```typescript
console.log('[Theme System]', {
  tenant: tenantSlug,
  theme: themeKey,
  timestamp: new Date().toISOString()
})
```

## Troubleshooting

### Theme Not Loading

**Symptom:** Tenant sees default theme instead of selected theme

**Possible Causes:**

1. **Theme file doesn't exist**
   - Check that `themes/[theme-key]/layout.tsx` exists
   - Verify theme key matches database record
   - Ensure file is committed to repository

2. **Database connection issue**
   - Verify `DATABASE_URL` is correct
   - Check database is accessible
   - Review Prisma connection logs

3. **Theme not assigned to tenant**
   - Query database: `SELECT * FROM "Tenant" WHERE slug = 'tenant-slug'`
   - Verify `themeId` is set
   - Check theme exists: `SELECT * FROM "Theme" WHERE id = 'theme-id'`

**Solution:**

```bash
# Check theme files
ls -la apps/web-next/themes/

# Verify database connection
cd apps/web-next
npx prisma studio

# Check tenant theme assignment
npx prisma db execute --stdin <<< "SELECT t.slug, t.themeId, th.key FROM \"Tenant\" t LEFT JOIN \"Theme\" th ON t.themeId = th.id;"
```

### Tenant Not Resolved

**Symptom:** User sees "Tenant not found" error page

**Possible Causes:**

1. **Subdomain doesn't match tenant slug**
   - Verify subdomain format: `slug.APP_BASE_DOMAIN`
   - Check tenant exists in database
   - Ensure DNS is configured correctly

2. **Custom domain not configured**
   - Verify domain field in tenant record
   - Check DNS CNAME record
   - Ensure domain is unique

3. **Middleware not running**
   - Check middleware.ts is deployed
   - Verify matcher configuration
   - Review middleware logs

**Solution:**

```bash
# Check tenant exists
npx prisma db execute --stdin <<< "SELECT * FROM \"Tenant\" WHERE slug = 'tenant-slug';"

# Test DNS resolution
nslookup tenant-slug.yourdomain.com

# Check middleware logs
# Review application logs for middleware errors
```

### Theme Styles Not Applied

**Symptom:** Theme loads but styles are missing or incorrect

**Possible Causes:**

1. **CSS file not imported**
   - Verify `import './styles/theme.css'` in layout.tsx
   - Check CSS file exists
   - Ensure file is in build output

2. **CSS conflicts**
   - Global styles overriding theme styles
   - Specificity issues
   - CSS modules not configured

3. **Build cache issue**
   - Old styles cached
   - Need to rebuild application

**Solution:**

```bash
# Clear Next.js cache
rm -rf apps/web-next/.next

# Rebuild application
npm run build

# Check CSS is included in build
ls -la apps/web-next/.next/static/css/
```

### Dynamic Import Errors

**Symptom:** Error: "Cannot find module" or "Failed to load theme"

**Possible Causes:**

1. **Theme key mismatch**
   - Database theme key doesn't match directory name
   - Case sensitivity issue
   - Typo in theme key

2. **Layout file missing or incorrect export**
   - No default export in layout.tsx
   - File named incorrectly
   - TypeScript compilation error

**Solution:**

```typescript
// Verify theme key matches directory
const themeKey = 'clinic' // Must match themes/clinic/

// Check default export exists
// themes/clinic/layout.tsx must have:
export default function ClinicLayout({ children }: ThemeLayoutProps) {
  return <div>{children}</div>
}
```

### Database Migration Issues

**Symptom:** Prisma errors about missing tables or columns

**Possible Causes:**

1. **Migrations not run**
   - Database schema out of sync
   - Migration files not applied

2. **Migration conflicts**
   - Multiple developers creating migrations
   - Schema drift between environments

**Solution:**

```bash
# Check migration status
npx prisma migrate status

# Apply pending migrations
npx prisma migrate deploy

# Reset database (development only!)
npx prisma migrate reset

# Generate new migration
npx prisma migrate dev --name add_theme_system
```

### Performance Issues

**Symptom:** Slow page loads or theme switching

**Possible Causes:**

1. **Database queries not optimized**
   - Missing indexes
   - N+1 query problems
   - Slow database connection

2. **Large theme bundles**
   - Too much CSS/JS in theme
   - Unoptimized images
   - No code splitting

3. **No caching**
   - Theme lookup on every request
   - Database queries not cached

**Solution:**

```typescript
// Add database indexes
// In Prisma schema:
model Tenant {
  slug    String @unique
  themeId String
  
  @@index([themeId])
}

// Implement caching
const themeCache = new Map<string, TenantTheme>()

export async function getTenantTheme(slug: string): Promise<TenantTheme | null> {
  if (themeCache.has(slug)) {
    return themeCache.get(slug)!
  }
  
  const theme = await prisma.tenant.findUnique({
    where: { slug },
    include: { theme: true }
  })
  
  if (theme) {
    themeCache.set(slug, theme)
  }
  
  return theme
}
```

### API Endpoint Errors

**Symptom:** API returns 500 errors or unexpected responses

**Possible Causes:**

1. **Authentication not implemented**
   - TODO comments in code
   - No auth checks on endpoints

2. **Validation errors**
   - Invalid request body
   - Missing required fields
   - Type mismatches

3. **Database errors**
   - Connection issues
   - Query failures
   - Constraint violations

**Solution:**

```typescript
// Add proper error handling
export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    const { themeId } = updateThemeSchema.parse(body)
    
    // Verify theme exists
    const theme = await prisma.theme.findUnique({
      where: { id: themeId }
    })
    
    if (!theme) {
      return NextResponse.json(
        { error: 'Theme not found' }, 
        { status: 404 }
      )
    }
    
    // Update tenant
    const tenant = await prisma.tenant.update({
      where: { slug: params.slug },
      data: { themeId }
    })
    
    return NextResponse.json(tenant)
  } catch (error) {
    console.error('Theme update error:', error)
    return NextResponse.json(
      { error: 'Internal server error' }, 
      { status: 500 }
    )
  }
}
```

### Getting Help

If you encounter issues not covered here:

1. Check application logs for detailed error messages
2. Review Prisma logs: `DEBUG=prisma:* npm run dev`
3. Test with the default theme to isolate theme-specific issues
4. Verify environment variables are set correctly
5. Check that all dependencies are installed: `npm install`

For additional support:
- Review the design document: `.kiro/specs/manual-theme-selection/design.md`
- Check requirements: `.kiro/specs/manual-theme-selection/requirements.md`
- Run integration tests: `npm test`
