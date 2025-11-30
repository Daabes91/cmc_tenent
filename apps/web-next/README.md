This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Environment Variables

This application requires several environment variables to be configured. Copy `.env.example` to `.env` and update the values as needed:

```bash
cp .env.example .env
```

### Required Environment Variables

#### Database Configuration

- **`DATABASE_URL`** (Required)
  - PostgreSQL connection string for Prisma ORM
  - Format: `postgresql://USER:PASSWORD@HOST:PORT/DATABASE?schema=SCHEMA`
  - Example: `postgresql://clinic:clinic_password@localhost:5442/clinic_multi_tenant?schema=public`
  - Used for storing tenant and theme data

#### Multi-Tenant Configuration

- **`APP_BASE_DOMAIN`** (Required)
  - Base domain for server-side tenant resolution in middleware
  - Format: `domain:port` (include port for local development)
  - Examples:
    - Development: `localhost:3001`
    - Production: `example.com`
  - Used to extract tenant slug from subdomains (e.g., `clinic-a.localhost:3001`)
  - Also used for custom domain lookup in the database

- **`NEXT_PUBLIC_BASE_DOMAIN`** (Required)
  - Base domain for client-side tenant identification
  - Format: `domain` (without protocol or port)
  - Examples:
    - Development: `localhost`
    - Production: `example.com`
  - Exposed to the browser via `NEXT_PUBLIC_` prefix

- **`NEXT_PUBLIC_DEFAULT_TENANT`** (Optional)
  - Default tenant slug to use when tenant cannot be resolved
  - Default: `default`
  - Used as fallback when subdomain/custom domain resolution fails

#### API Configuration

- **`NEXT_PUBLIC_API_URL`** (Required)
  - Backend API URL for public endpoints
  - Example: `http://localhost:8080/api/public`
  - Used for booking and public-facing API calls

#### Node Environment

- **`NODE_ENV`** (Optional)
  - Node.js environment mode
  - Values: `development`, `production`, `test`
  - Affects Prisma client caching and other optimizations

#### Google OAuth Configuration

- **`NEXT_PUBLIC_GOOGLE_CLIENT_ID`** (Optional)
  - Google OAuth 2.0 Client ID for patient authentication
  - Get from [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
  - Example: `123456789-abc.apps.googleusercontent.com`
  - Safe to expose in frontend (public credential)

- **`NEXT_PUBLIC_GOOGLE_REDIRECT_URI`** (Optional)
  - OAuth callback URL where Google redirects after authentication
  - Must match the redirect URI configured in Google Cloud Console
  - Format: `https://yourdomain.com/auth/google/callback`
  - Example: `http://localhost:3001/auth/google/callback` (development)

- **`NEXT_PUBLIC_ENABLE_GOOGLE_OAUTH`** (Optional)
  - Feature flag to enable/disable Google OAuth authentication
  - Values: `true`, `false`
  - Default: `true`
  - Set to `false` to hide Google OAuth buttons

For complete Google OAuth setup instructions, see [Google OAuth Configuration Guide](../../.kiro/specs/patient-google-oauth/GOOGLE_OAUTH_CONFIGURATION.md).

### Tenant Resolution

The application supports two methods of tenant identification:

1. **Subdomain-based**: `{tenant-slug}.{APP_BASE_DOMAIN}`
   - Example: `clinic-a.localhost:3001`
   - Tenant slug is extracted from the subdomain

2. **Custom domain**: Any domain configured in the `Tenant.domain` field
   - Example: `myclinic.com`
   - Requires database lookup to map domain to tenant

### Theme Selection

Tenants can select from predefined themes stored in the database. The theme selection is stored in the `Tenant.themeId` field and loaded dynamically at runtime without requiring redeployment.

## Getting Started

### 1. Set Up Environment Variables

Copy `.env.example` to `.env` and configure the required variables:

```bash
cp .env.example .env
```

Edit `.env` and update the values, especially:
- `DATABASE_URL` - Your PostgreSQL connection string
- `APP_BASE_DOMAIN` - Your base domain (e.g., `localhost:3001` for development)

### 2. Set Up Database

Install dependencies and set up the database:

```bash
# Install dependencies
npm install
# or
pnpm install

# Generate Prisma client
npm run prisma:generate

# Run database migrations
npm run prisma:migrate

# Seed the database with sample themes and tenants
npm run prisma:seed
```

### 3. Run Development Server

Start the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3001](http://localhost:3001) with your browser to see the result.

### 4. Test Multi-Tenant Setup

To test subdomain-based tenant resolution locally, you can:

1. Use a tool like [localhost.run](https://localhost.run) or configure your `/etc/hosts` file
2. Add entries like:
   ```
   127.0.0.1 clinic-a.localhost
   127.0.0.1 clinic-b.localhost
   ```
3. Access `http://clinic-a.localhost:3001` to see the clinic-a tenant with its selected theme

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Database Management

### Prisma Commands

The following npm scripts are available for database management:

```bash
# Generate Prisma client (run after schema changes)
npm run prisma:generate

# Create and apply database migrations
npm run prisma:migrate

# Seed database with sample data (themes and tenants)
npm run prisma:seed

# Open Prisma Studio to inspect database
npm run prisma:studio
```

### Database Schema

The application uses two main models:

- **Theme**: Stores predefined theme definitions
  - `id`: Unique identifier
  - `key`: Theme key (e.g., "clinic", "barber")
  - `name`: Display name
  - `status`: "published" or "draft"

- **Tenant**: Stores tenant information
  - `id`: Unique identifier
  - `slug`: Tenant slug for subdomain resolution
  - `domain`: Optional custom domain
  - `themeId`: Selected theme (foreign key to Theme)
  - `status`: Tenant status

## Theme System

The Manual Theme Selection system enables developers to hand-build complete theme implementations and allows tenants to select from predefined themes. Theme changes take effect immediately without requiring redeployment.

### Documentation

- **[Complete Theme System Guide](docs/THEME_SYSTEM.md)** - Full documentation including architecture, development, API, deployment, and troubleshooting
- **[Quick Start Guide](docs/THEME_QUICKSTART.md)** - Get started in 5 minutes
- **[API Reference](docs/THEME_API_REFERENCE.md)** - Complete API documentation

### Available Themes

The application comes with predefined themes in the `/themes` directory:

- **default**: Fallback theme
- **clinic**: Medical clinic theme
- **barber**: Barber shop theme

### Quick Start

Create a new theme in 3 steps:

1. **Create theme directory:**
   ```bash
   mkdir -p themes/my-theme/components/styles
   ```

2. **Create layout component** (`themes/my-theme/layout.tsx`):
   ```typescript
   import { ThemeLayoutProps } from '../types'
   
   export default function MyThemeLayout({ children }: ThemeLayoutProps) {
     return <div>{children}</div>
   }
   ```

3. **Add to database:**
   ```bash
   npx prisma studio
   # Add new Theme record with key='my-theme'
   ```

For detailed instructions, see the [Quick Start Guide](docs/THEME_QUICKSTART.md).

### Theme Selection

Tenants can select their theme through:

1. **Admin UI**: Navigate to `/dashboard/settings/theme`
2. **API**: POST to `/api/tenants/{slug}/theme` with `{ themeId: "theme-id" }`

Theme changes take effect immediately without requiring redeployment.

## API Endpoints

### Theme Management

- **GET /api/themes**: List all published themes
- **GET /api/internal/tenant**: Get current tenant information
- **POST /api/tenants/[slug]/theme**: Update tenant theme selection

For complete API documentation, see the [API Reference](docs/THEME_API_REFERENCE.md).

### Example: Update Tenant Theme

```bash
curl -X POST http://clinic-a.localhost:3001/api/tenants/clinic-a/theme \
  -H "Content-Type: application/json" \
  -d '{"themeId": "clinic-theme-id"}'
```

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
