# Web-Next Documentation

Complete documentation for the web-next application and Manual Theme Selection system.

## Quick Links

- **[Theme System Overview](THEME_SYSTEM.md)** - Complete guide to the theme system
- **[Quick Start Guide](THEME_QUICKSTART.md)** - Get started in 5 minutes
- **[API Reference](THEME_API_REFERENCE.md)** - Complete API documentation
- **[Deployment Guide](THEME_DEPLOYMENT.md)** - Production deployment instructions
- **[Troubleshooting](THEME_TROUBLESHOOTING.md)** - Common issues and solutions

## Documentation Structure

### For Developers

If you're building themes or working on the application:

1. Start with **[Quick Start Guide](THEME_QUICKSTART.md)** to set up your environment
2. Read **[Theme System Overview](THEME_SYSTEM.md)** for architecture and concepts
3. Reference **[API Documentation](THEME_API_REFERENCE.md)** while developing
4. Use **[Troubleshooting Guide](THEME_TROUBLESHOOTING.md)** when issues arise

### For DevOps/Deployment

If you're deploying to production:

1. Review **[Deployment Guide](THEME_DEPLOYMENT.md)** for step-by-step instructions
2. Check **[Theme System Overview](THEME_SYSTEM.md#deployment)** for requirements
3. Keep **[Troubleshooting Guide](THEME_TROUBLESHOOTING.md#production-issues)** handy

### For System Administrators

If you're managing tenants and themes:

1. Read **[Theme System Overview](THEME_SYSTEM.md#api-endpoints)** for API usage
2. Reference **[API Documentation](THEME_API_REFERENCE.md)** for endpoint details
3. Use **[Troubleshooting Guide](THEME_TROUBLESHOOTING.md)** for common issues

## What is the Manual Theme Selection System?

The Manual Theme Selection system enables:

- **Developers** to hand-build complete theme implementations with full control
- **Tenants** to select from predefined themes through an admin interface
- **Immediate theme changes** without requiring application redeployment

### Key Features

- ✅ Hand-built themes with complete layout control
- ✅ Database-driven theme selection
- ✅ Subdomain and custom domain support
- ✅ Dynamic theme loading at runtime
- ✅ Zero-downtime theme switching
- ✅ TypeScript support with type safety
- ✅ Internationalization ready

## Getting Started

### Prerequisites

- Node.js 18 or higher
- PostgreSQL database
- Basic knowledge of React and Next.js

### Installation

```bash
# Clone repository
git clone https://github.com/your-org/your-repo.git
cd your-repo/apps/web-next

# Install dependencies
npm install

# Configure environment
cp .env.example .env
# Edit .env with your settings

# Setup database
npm run prisma:generate
npm run prisma:migrate
npm run prisma:seed

# Start development server
npm run dev
```

Visit `http://localhost:3001` to see the application.

### Create Your First Theme

```bash
# 1. Create theme directory
mkdir -p themes/my-theme/components/styles

# 2. Create layout component
cat > themes/my-theme/layout.tsx <<'EOF'
import { ThemeLayoutProps } from '../types'

export default function MyThemeLayout({ children }: ThemeLayoutProps) {
  return <div className="my-theme">{children}</div>
}
EOF

# 3. Add to database
npx prisma studio
# Create new Theme record with key='my-theme'

# 4. Test it
# Visit http://tenant-slug.localhost:3001
```

For detailed instructions, see the [Quick Start Guide](THEME_QUICKSTART.md).

## Documentation Overview

### [Theme System Overview](THEME_SYSTEM.md)

Complete documentation covering:
- System architecture and data flow
- Theme development process
- Creating new themes
- Best practices
- API endpoints
- Deployment instructions
- Troubleshooting

**Read this for:** Understanding how the system works and developing themes.

### [Quick Start Guide](THEME_QUICKSTART.md)

Get up and running quickly:
- 5-minute setup
- Create your first theme
- Common tasks
- Quick troubleshooting

**Read this for:** Getting started fast without reading all documentation.

### [API Reference](THEME_API_REFERENCE.md)

Complete API documentation:
- REST API endpoints
- TypeScript APIs
- Database schema
- Error codes
- Request/response examples

**Read this for:** Integrating with the theme system programmatically.

### [Deployment Guide](THEME_DEPLOYMENT.md)

Production deployment instructions:
- Pre-deployment checklist
- Environment setup
- Database migrations
- Platform-specific guides (Vercel, Docker, AWS, etc.)
- DNS configuration
- Post-deployment verification
- Rollback procedures

**Read this for:** Deploying to production environments.

### [Troubleshooting Guide](THEME_TROUBLESHOOTING.md)

Common issues and solutions:
- Quick diagnostics
- Common problems
- Error messages
- Performance issues
- Development issues
- Production issues
- Database issues
- Debugging tools

**Read this for:** Solving problems and debugging issues.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      Client Request                          │
│              (subdomain or custom domain)                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Edge Middleware                             │
│  - Resolve tenant from hostname                              │
│  - Set x-tenant header                                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Root Layout (SSR)                           │
│  - Query database for tenant theme                           │
│  - Dynamic import theme layout                               │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Theme Layout Component                      │
│  - Theme-specific UI and styling                             │
│  - Wraps page content                                        │
└─────────────────────────────────────────────────────────────┘
```

## Key Concepts

### Themes

Themes are complete, hand-built implementations including:
- Layout components
- Theme-specific components (Header, Footer, Navigation)
- Styles (CSS)
- Assets

Themes are stored in the `themes/` directory with this structure:

```
themes/
├── clinic/
│   ├── layout.tsx
│   ├── components/
│   └── styles/
├── barber/
│   ├── layout.tsx
│   ├── components/
│   └── styles/
└── default/
    ├── layout.tsx
    └── components/
```

### Tenants

Tenants are individual organizations using the platform. Each tenant:
- Has a unique slug (e.g., "clinic-a")
- Can use a subdomain (e.g., clinic-a.yourdomain.com)
- Can optionally use a custom domain
- Selects one theme from available options

### Theme Selection

Tenants select themes through:
1. Admin UI at `/dashboard/settings/theme`
2. API endpoint: `POST /api/tenants/[slug]/theme`

Theme changes take effect immediately without redeployment.

## Common Tasks

### List Available Themes

```bash
curl http://localhost:3001/api/themes
```

### Get Current Tenant Info

```bash
curl http://localhost:3001/api/internal/tenant \
  -H "x-tenant: clinic-a"
```

### Switch Tenant Theme

```bash
curl -X POST http://localhost:3001/api/tenants/clinic-a/theme \
  -H "Content-Type: application/json" \
  -d '{"themeId": "theme-id"}'
```

### Inspect Database

```bash
npx prisma studio
```

### Run Tests

```bash
npm test -- theme-integration.test.js
```

## Environment Variables

Required environment variables:

```bash
# Database
DATABASE_URL="postgresql://user:password@localhost:5432/dbname"

# Application
APP_BASE_DOMAIN="localhost:3001"
NEXT_PUBLIC_BASE_DOMAIN="localhost:3001"
NEXT_PUBLIC_DEFAULT_TENANT="default"

# API
NEXT_PUBLIC_API_URL="http://localhost:8080/api/public"

# Node
NODE_ENV="development"
```

See [Deployment Guide](THEME_DEPLOYMENT.md#environment-setup) for production configuration.

## Database Schema

### Theme Table

Stores available theme definitions:

| Column | Type | Description |
|--------|------|-------------|
| id | TEXT | Unique identifier |
| key | TEXT | Theme key (matches directory name) |
| name | TEXT | Display name |
| status | TEXT | "published" or "draft" |
| createdAt | TIMESTAMP | Creation timestamp |
| updatedAt | TIMESTAMP | Last update timestamp |

### Tenant Table

Stores tenant information:

| Column | Type | Description |
|--------|------|-------------|
| id | TEXT | Unique identifier |
| slug | TEXT | Tenant slug for subdomain |
| domain | TEXT | Optional custom domain |
| themeId | TEXT | Selected theme (FK to Theme) |
| status | TEXT | Tenant status |
| createdAt | TIMESTAMP | Creation timestamp |
| updatedAt | TIMESTAMP | Last update timestamp |

## API Endpoints

### GET /api/themes

Returns all published themes.

**Response:**
```json
[
  {
    "id": "theme-id",
    "key": "clinic",
    "name": "Clinic Theme"
  }
]
```

### GET /api/internal/tenant

Returns current tenant information.

**Response:**
```json
{
  "slug": "clinic-a",
  "themeId": "theme-id"
}
```

### POST /api/tenants/[slug]/theme

Updates tenant theme selection.

**Request:**
```json
{
  "themeId": "theme-id"
}
```

**Response:**
```json
{
  "id": "tenant-id",
  "slug": "clinic-a",
  "themeId": "theme-id",
  "status": "active"
}
```

See [API Reference](THEME_API_REFERENCE.md) for complete documentation.

## Testing

### Run Integration Tests

```bash
npm test -- theme-integration.test.js
```

### Manual Testing

1. Start development server: `npm run dev`
2. Visit tenant subdomain: `http://clinic-a.localhost:3001`
3. Verify theme loads correctly
4. Switch theme via admin UI
5. Verify new theme loads

See [Theme System Overview](THEME_SYSTEM.md#testing-your-theme) for testing checklist.

## Support and Resources

### Documentation

- [Theme System Overview](THEME_SYSTEM.md)
- [Quick Start Guide](THEME_QUICKSTART.md)
- [API Reference](THEME_API_REFERENCE.md)
- [Deployment Guide](THEME_DEPLOYMENT.md)
- [Troubleshooting Guide](THEME_TROUBLESHOOTING.md)

### Specification Documents

- [Requirements](.kiro/specs/manual-theme-selection/requirements.md)
- [Design](.kiro/specs/manual-theme-selection/design.md)
- [Tasks](.kiro/specs/manual-theme-selection/tasks.md)

### Test Files

- Integration tests: `test/theme-integration.test.js`
- API tests: `test/api-themes-endpoint.test.js`
- Test guide: `test/THEME_INTEGRATION_TEST_GUIDE.md`

## Contributing

When contributing to the theme system:

1. Read the [Theme System Overview](THEME_SYSTEM.md)
2. Follow TypeScript best practices
3. Add tests for new features
4. Update documentation
5. Test with multiple themes

## License

[Your License Here]

## Changelog

### Version 1.0.0 (Current)

- ✅ Manual theme selection system
- ✅ Subdomain-based tenant resolution
- ✅ Custom domain support
- ✅ Dynamic theme loading
- ✅ Database-driven theme management
- ✅ REST API for theme operations
- ✅ Complete documentation

## Next Steps

1. **New to the system?** Start with the [Quick Start Guide](THEME_QUICKSTART.md)
2. **Building themes?** Read the [Theme System Overview](THEME_SYSTEM.md)
3. **Deploying?** Follow the [Deployment Guide](THEME_DEPLOYMENT.md)
4. **Having issues?** Check the [Troubleshooting Guide](THEME_TROUBLESHOOTING.md)
