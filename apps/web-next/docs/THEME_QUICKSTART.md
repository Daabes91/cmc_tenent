# Theme System Quick Start Guide

Get started with the Manual Theme Selection system in 5 minutes.

## Prerequisites

- Node.js 18+ installed
- PostgreSQL database running
- Basic knowledge of React and Next.js

## Quick Setup

### 1. Install Dependencies

```bash
cd apps/web-next
npm install
```

### 2. Configure Environment

Create `.env` file:

```bash
DATABASE_URL="postgresql://user:password@localhost:5432/clinic_db"
APP_BASE_DOMAIN="localhost:3001"
NEXT_PUBLIC_BASE_DOMAIN="localhost:3001"
NEXT_PUBLIC_DEFAULT_TENANT="default"
```

### 3. Setup Database

```bash
# Run migrations
npx prisma migrate deploy

# Seed initial data
npx prisma db seed

# Generate Prisma client
npx prisma generate
```

This creates:
- Theme table with sample themes (clinic, barber)
- Tenant table with test tenants (clinic-a, clinic-b)

### 4. Start Development Server

```bash
npm run dev
```

### 5. Test Theme System

Visit these URLs to see different themes:

- `http://clinic-a.localhost:3001` - Clinic theme
- `http://clinic-b.localhost:3001` - Barber theme
- `http://default.localhost:3001` - Default theme

## Create Your First Theme

### Step 1: Create Theme Directory

```bash
mkdir -p themes/my-theme/components/styles
```

### Step 2: Create Layout Component

Create `themes/my-theme/layout.tsx`:

```typescript
import { ThemeLayoutProps } from '../types'
import './styles/theme.css'

export default function MyThemeLayout({ 
  children, 
  locale, 
  tenantSlug 
}: ThemeLayoutProps) {
  return (
    <div className="my-theme">
      <header>
        <h1>My Theme</h1>
        <nav>
          <a href={`/${locale}`}>Home</a>
          <a href={`/${locale}/about`}>About</a>
        </nav>
      </header>
      <main>{children}</main>
      <footer>
        <p>&copy; 2024 My Theme</p>
      </footer>
    </div>
  )
}
```

### Step 3: Add Styles

Create `themes/my-theme/styles/theme.css`:

```css
.my-theme {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: system-ui, -apple-system, sans-serif;
}

.my-theme header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2rem;
}

.my-theme main {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.my-theme footer {
  background: #f7fafc;
  padding: 2rem;
  text-align: center;
  color: #4a5568;
}
```

### Step 4: Add Theme to Database

Open Prisma Studio:

```bash
npx prisma studio
```

1. Go to "Theme" table
2. Click "Add record"
3. Fill in:
   - key: `my-theme`
   - name: `My Theme`
   - status: `published`
4. Save

### Step 5: Assign Theme to Tenant

In Prisma Studio:

1. Go to "Tenant" table
2. Find or create a tenant
3. Set `themeId` to your new theme's ID
4. Save

### Step 6: Test Your Theme

Visit your tenant's subdomain:

```
http://your-tenant-slug.localhost:3001
```

You should see your new theme!

## Common Tasks

### Switch Theme via API

```bash
curl -X POST http://localhost:3001/api/tenants/clinic-a/theme \
  -H "Content-Type: application/json" \
  -d '{"themeId": "your-theme-id"}'
```

### List Available Themes

```bash
curl http://localhost:3001/api/themes
```

### View Current Tenant Info

```bash
curl http://localhost:3001/api/internal/tenant \
  -H "x-tenant: clinic-a"
```

### Inspect Database

```bash
npx prisma studio
```

## Next Steps

- Read the full documentation: `docs/THEME_SYSTEM.md`
- Explore existing themes: `themes/clinic/` and `themes/barber/`
- Review the design document: `.kiro/specs/manual-theme-selection/design.md`
- Run integration tests: `npm test -- theme-integration.test.js`

## Troubleshooting

**Theme not loading?**
- Check theme key matches directory name exactly
- Verify theme is marked as "published" in database
- Ensure layout.tsx has default export

**Tenant not found?**
- Check tenant exists in database
- Verify subdomain format: `slug.localhost:3001`
- Ensure APP_BASE_DOMAIN is set correctly

**Styles not applied?**
- Import CSS in layout.tsx: `import './styles/theme.css'`
- Clear Next.js cache: `rm -rf .next`
- Rebuild: `npm run dev`

For more help, see the troubleshooting section in `docs/THEME_SYSTEM.md`.
