# SAAS Manager Admin Panel

A comprehensive admin panel for SAAS managers to manage tenants, monitor system health, and oversee the multi-tenant clinic management platform.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Configuration](#configuration)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Architecture](#architecture)
- [API Integration](#api-integration)
- [Troubleshooting](#troubleshooting)
- [Additional Documentation](#additional-documentation)

## Overview

The SAAS Manager Admin Panel is a Nuxt 3 application that provides centralized management capabilities for the multi-tenant clinic management platform. It enables SAAS managers to:

- Create and manage tenant organizations
- Monitor system-wide metrics and health
- Configure tenant branding and settings
- View analytics and usage reports
- Track administrative actions through audit logs
- Receive real-time notifications for critical events

## Features

### Accessibility (Task 20) ✅

The SAAS Manager Admin Panel is built with accessibility as a core principle, meeting WCAG 2.1 AA standards:

- **Keyboard Navigation**: Full keyboard support with skip links, logical tab order, and no keyboard traps
- **Screen Reader Support**: Comprehensive ARIA labels, landmarks, and live regions for dynamic content
- **Focus Indicators**: Clear, visible focus indicators on all interactive elements
- **Color Contrast**: WCAG AA compliant contrast ratios (4.5:1 for normal text, 3:1 for large text)
- **Form Accessibility**: Proper labels, required field indicators, and error messages
- **Responsive Design**: Touch targets minimum 44x44px, mobile-friendly navigation
- **Motion Support**: Respects prefers-reduced-motion for users with vestibular disorders
- **Internationalization**: Accessibility labels in English and Arabic with RTL support

See [Accessibility Guide](./docs/accessibility.md) for detailed information.

### Authentication System (Task 2)

#### ✅ 2.1 Authentication Composable (`composables/useSaasAuth.ts`)
- JWT-based authentication with localStorage persistence
- Token expiration handling with automatic logout
- Methods: `login()`, `logout()`, `checkAuth()`, `getToken()`
- Reactive state management for authentication status

#### ✅ 2.2 Login Page (`pages/login.vue`)
- Email and password form with validation
- Real-time field validation
- Loading states during authentication
- Error handling with user-friendly messages
- Responsive design with Nuxt UI components
- Bilingual support (English/Arabic)

#### ✅ 2.3 API Integration Composable (`composables/useSaasApi.ts`)
- Authenticated API call wrapper with automatic JWT header injection
- Automatic logout on 401 responses
- Error handling for all API calls
- Methods for tenant management, metrics, analytics, and audit logs
- Query parameter serialization

#### ✅ 2.4 Authentication Middleware (`middleware/saas-auth.global.ts`)
- Global route protection
- Automatic redirect to login for unauthenticated users
- Automatic redirect to dashboard for authenticated users on login page
- Token validation on every route change

## Quick Start

```bash
# Clone the repository and navigate to the project
cd apps/saas-admin-nuxt

# Install dependencies
pnpm install

# Copy environment configuration
cp .env.example .env

# Start development server
pnpm dev
```

The application will be available at `http://localhost:3002`

## Installation

### Prerequisites

- Node.js 18.x or higher
- pnpm 8.x or higher (or npm/yarn)
- Access to the backend API (running on port 8080 by default)

### Step-by-Step Installation

1. **Install Node.js dependencies:**

```bash
pnpm install
```

2. **Configure environment variables:**

Copy the example environment file and update with your configuration:

```bash
cp .env.example .env
```

Edit `.env` with your settings (see [Configuration](#configuration) section).

3. **Verify backend API is running:**

Ensure the backend API is accessible at the configured `NUXT_PUBLIC_API_BASE` URL.

4. **Start the development server:**

```bash
pnpm dev
```

The application will start on `http://localhost:3002`

## Configuration

### Environment Variables

Create a `.env` file in the root directory with the following variables:

```env
# API Configuration
NUXT_PUBLIC_API_BASE=http://localhost:8080
NUXT_PUBLIC_SAAS_API_BASE=http://localhost:8080/saas

# Application Configuration
NUXT_PUBLIC_APP_NAME=SAAS Manager Panel
NUXT_PUBLIC_APP_URL=http://localhost:3002

# Feature Flags
NUXT_PUBLIC_ENABLE_ANALYTICS=true
NUXT_PUBLIC_ENABLE_AUDIT_LOGS=true
```

#### Variable Descriptions

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `NUXT_PUBLIC_API_BASE` | Base URL for the backend API | `http://localhost:8080` | Yes |
| `NUXT_PUBLIC_SAAS_API_BASE` | Base URL for SAAS-specific endpoints | `http://localhost:8080/saas` | Yes |
| `NUXT_PUBLIC_APP_NAME` | Application display name | `SAAS Manager Panel` | No |
| `NUXT_PUBLIC_APP_URL` | Public URL of the application | `http://localhost:3002` | No |
| `NUXT_PUBLIC_ENABLE_ANALYTICS` | Enable analytics features | `true` | No |
| `NUXT_PUBLIC_ENABLE_AUDIT_LOGS` | Enable audit log features | `true` | No |

### Production Configuration

For production deployments, update the environment variables:

```env
NUXT_PUBLIC_API_BASE=https://api.yourdomain.com
NUXT_PUBLIC_SAAS_API_BASE=https://api.yourdomain.com/saas
NUXT_PUBLIC_APP_NAME=SAAS Manager Panel
NUXT_PUBLIC_APP_URL=https://saas-admin.yourdomain.com
NUXT_PUBLIC_ENABLE_ANALYTICS=true
NUXT_PUBLIC_ENABLE_AUDIT_LOGS=true
```

## Development

### Available Scripts

```bash
# Start development server with hot reload
pnpm dev

# Build for production
pnpm build

# Preview production build locally
pnpm preview

# Run tests
pnpm test

# Run tests in watch mode
pnpm test:watch

# Run tests with coverage
pnpm test:coverage

# Prepare Nuxt (run automatically after install)
pnpm postinstall
```

### Development Workflow

1. **Start the development server:**
   ```bash
   pnpm dev
   ```

2. **Make changes to the code** - Hot reload will automatically update the browser

3. **Run tests** to ensure your changes work:
   ```bash
   pnpm test
   ```

4. **Build for production** to verify the build succeeds:
   ```bash
   pnpm build
   ```

### Project Structure

```
apps/saas-admin-nuxt/
├── assets/              # Static assets (CSS, images)
├── components/          # Vue components
│   ├── analytics/       # Analytics-related components
│   ├── audit/          # Audit log components
│   ├── dashboard/      # Dashboard components
│   ├── notifications/  # Notification components
│   └── tenants/        # Tenant management components
├── composables/        # Vue composables (reusable logic)
├── docs/               # Additional documentation
├── layouts/            # Layout components
├── locales/            # i18n translation files
├── middleware/         # Route middleware
├── pages/              # Application pages (auto-routed)
├── plugins/            # Nuxt plugins
├── public/             # Public static files
├── test/               # Test files
├── types/              # TypeScript type definitions
└── utils/              # Utility functions
```

## Testing

The application includes comprehensive test coverage:

### Running Tests

```bash
# Run all tests once
pnpm test

# Run tests in watch mode (for development)
pnpm test:watch

# Run tests with coverage report
pnpm test:coverage
```

### Test Structure

- **Unit Tests** (`test/composables/`): Test individual composables
- **Component Tests** (`test/components/`): Test Vue components
- **Integration Tests** (`test/integration/`): Test complete user flows

See [test/README.md](./test/README.md) for detailed testing documentation.

## Architecture

### Technology Stack

- **Framework**: Nuxt 3 (Vue 3 with Composition API)
- **UI Library**: Nuxt UI (Tailwind CSS-based components)
- **State Management**: Vue Composables + Pinia (for complex state)
- **HTTP Client**: Nuxt's built-in $fetch
- **Charts**: Chart.js with vue-chartjs
- **Authentication**: JWT-based with localStorage
- **Internationalization**: @nuxtjs/i18n (English and Arabic)
- **Testing**: Vitest with @vue/test-utils

### Authentication Flow

1. User navigates to any protected route
2. Global middleware (`middleware/saas-auth.global.ts`) checks authentication status
3. If not authenticated, redirects to `/login`
4. User enters credentials and submits form
5. `useSaasAuth.login()` calls the API endpoint
6. On success, JWT token is stored in localStorage
7. User is redirected to dashboard
8. All subsequent API calls include the JWT token in Authorization header
9. On 401 response, user is automatically logged out and redirected to login

### Security Features

- JWT tokens stored in localStorage with expiration tracking
- Automatic token validation on route changes
- Automatic logout on token expiration
- Automatic logout on 401 API responses
- Password field with show/hide toggle
- Client-side form validation
- Input sanitization for XSS prevention
- Rate limiting on client side for API calls
- Session timeout with warning dialog (30 minutes)
- HTTPS enforcement in production

### Internationalization

The application supports both English and Arabic:
- **English**: Default language (LTR)
- **Arabic**: Full RTL layout with complete translations

Translations are stored in:
- `locales/en.json` - English translations
- `locales/ar.json` - Arabic translations

Language can be switched via the user menu in the header.

### Responsive Design

The application is fully responsive and supports:
- **Mobile**: 320px - 768px (card layouts, bottom navigation)
- **Tablet**: 768px - 1024px (adaptive layouts)
- **Desktop**: 1024px+ (full table views, sidebar navigation)

See [docs/responsive-design.md](./docs/responsive-design.md) for details.

## API Integration

### Base Configuration

The application communicates with the backend API using the following base URLs:

- **API Base**: `NUXT_PUBLIC_API_BASE` (e.g., `http://localhost:8080`)
- **SAAS API Base**: `NUXT_PUBLIC_SAAS_API_BASE` (e.g., `http://localhost:8080/saas`)

### API Endpoints

#### Authentication

- `POST /saas/auth/login` - Authenticate SAAS manager
  - Request: `{ email: string, password: string }`
  - Response: `{ token: string, managerName: string, expiresAt: string }`

#### Tenant Management

- `GET /saas/tenants` - List all tenants (with pagination, search, filters)
  - Query params: `page`, `size`, `search`, `status`, `sortBy`, `sortDirection`
  - Response: `{ content: Tenant[], totalElements: number, totalPages: number }`

- `GET /saas/tenants/{id}` - Get tenant details
  - Query params: `includeDeleted` (boolean)
  - Response: `Tenant`

- `POST /saas/tenants` - Create new tenant
  - Request: `{ slug: string, name: string, customDomain?: string }`
  - Response: `{ tenant: Tenant, adminCredentials: { email: string, password: string } }`

- `PUT /saas/tenants/{id}` - Update tenant
  - Request: `{ name?: string, customDomain?: string, status?: string }`
  - Response: `Tenant`

- `DELETE /saas/tenants/{id}` - Delete tenant
  - Response: `{ message: string }`

#### System Metrics (Backend Implementation Required)

- `GET /saas/metrics/system` - Get system-wide metrics
  - Response: `SystemMetrics`

- `GET /saas/metrics/tenants/{id}` - Get tenant-specific metrics
  - Response: `TenantMetrics`

#### Analytics (Backend Implementation Required)

- `GET /saas/analytics` - Get analytics data
  - Query params: `startDate`, `endDate`, `metric`
  - Response: `AnalyticsData`

#### Audit Logs (Backend Implementation Required)

- `GET /saas/audit-logs` - Get audit logs
  - Query params: `startDate`, `endDate`, `actionType`, `managerId`, `tenantId`, `page`, `size`
  - Response: `{ content: AuditLog[], totalElements: number, totalPages: number }`

- `GET /saas/audit-logs/export` - Export audit logs to CSV
  - Query params: Same as GET
  - Response: CSV file

### API Composable

The `useSaasApi` composable provides a centralized interface for all API calls:

```typescript
const api = useSaasApi()

// Example: Fetch tenants
const tenants = await api.getTenants({ page: 0, size: 20 })

// Example: Create tenant
const result = await api.createTenant({ 
  slug: 'clinic-abc', 
  name: 'ABC Clinic' 
})
```

All API calls automatically include:
- JWT token in Authorization header
- Error handling with automatic logout on 401
- Request/response logging (in development)

See [API_INTEGRATION.md](./docs/API_INTEGRATION.md) for complete API documentation.

### Main Layout and Navigation (Task 3)

#### ✅ 3.1 Default Layout (`layouts/default.vue`)
- Responsive sidebar navigation with mobile hamburger menu
- Header with user menu and logout functionality
- Navigation links for Dashboard, Tenants, Analytics, and Audit Logs
- Mobile-friendly bottom navigation
- Bilingual support with language switcher

### Dashboard (Task 4)

#### ✅ 4.1 Dashboard Page (`pages/index.vue`)
- Grid layout with system metric cards
- Real-time metrics with auto-refresh
- Loading states and skeleton screens
- Responsive design

#### ✅ 4.2 System Metrics Composable (`composables/useSystemMetrics.ts`)
- Metrics data management with auto-refresh
- Error handling and fallback states
- API integration for system metrics

#### ✅ 4.3 Dashboard Components
- `MetricCard.vue` - Reusable metric display with trend indicators
- `SystemHealthWidget.vue` - Health status indicators
- `RecentActivityFeed.vue` - Activity timeline

### Tenant Management (Tasks 5-7)

#### ✅ 5.1 Tenant List Page (`pages/tenants/index.vue`)
- Responsive table/card layout
- Search with debouncing (300ms)
- Status filter (All, Active, Inactive, Deleted)
- Column sorting (name, creation date, status)
- Pagination with URL query sync
- "Create Tenant" button

#### ✅ 5.2 Tenant Components
- `TenantTable.vue` - Desktop table view with sorting and pagination
- `TenantCard.vue` - Mobile-friendly card view

#### ✅ 5.3 Tenant Management Composable (`composables/useTenantManagement.ts`)
- Tenant CRUD operations
- Search, filter, and sort functionality
- Pagination state management
- URL query parameter synchronization

#### ✅ 6.1 Create Tenant Page (`pages/tenants/new.vue`)
- Tenant creation form with validation
- Success modal with admin credentials
- Copy-to-clipboard functionality
- Navigation to tenant detail after creation

#### ✅ 6.2 Tenant Form Component (`components/tenants/TenantForm.vue`)
- Reusable form for create and edit modes
- Real-time slug validation with uniqueness check
- Admin email preview
- Form validation with error messages
- Auto-formatting for slug field

#### ✅ 7.1 Tenant Detail Page (`pages/tenants/[id].vue`)
- Comprehensive tenant information display
- Tenant-specific metrics (users, staff, patients, appointments, storage)
- Recent activity timeline
- Action buttons (Edit, Configure Branding, Delete)
- Status badges and indicators
- Loading and error states

#### ✅ 7.2 Tenant Edit Page (`pages/tenants/[id]/edit.vue`)
- Edit form reusing TenantForm component
- Status toggle (Active/Inactive) with confirmation
- Success notifications
- Navigation back to detail page

#### ✅ 7.3 Tenant Deletion
- Delete button with confirmation dialog
- Warning about data archival and access prevention
- Audit log entry (handled by backend)
- Navigation to tenant list after deletion

## Next Steps

The following tasks are ready to be implemented:
- Task 8: Implement branding configuration editor
- Task 9: Implement analytics dashboard
- Task 10: Implement audit log viewer
- Task 11: Implement notification system

## Deployment

### Quick Start

```bash
# Local development
pnpm install
pnpm dev

# Docker deployment (automated)
./deploy.sh

# Docker Compose (development)
docker-compose -f docker-compose.dev.yml up -d

# Docker Compose (production)
docker-compose -f docker-compose.prod.yml up -d
```

### Documentation

- **[DEPLOYMENT.md](./DEPLOYMENT.md)** - Comprehensive deployment guide (500+ lines)
- **[DEPLOYMENT-CHECKLIST.md](./DEPLOYMENT-CHECKLIST.md)** - Step-by-step deployment checklist
- **[QUICK-DEPLOY.md](./QUICK-DEPLOY.md)** - Quick reference for common commands
- **[DOCKER-DEPLOYMENT-SUMMARY.md](./DOCKER-DEPLOYMENT-SUMMARY.md)** - Implementation summary

### Environment Variables

Required environment variables:

- `NUXT_PUBLIC_API_BASE` - Base URL for the API
- `NUXT_PUBLIC_SAAS_API_BASE` - Base URL for SAAS endpoints
- `NUXT_PUBLIC_APP_NAME` - Application name
- `NUXT_PUBLIC_APP_URL` - Public URL of the application
- `NUXT_PUBLIC_ENABLE_ANALYTICS` - Enable analytics (true/false)
- `NUXT_PUBLIC_ENABLE_AUDIT_LOGS` - Enable audit logs (true/false)

### Docker Configuration

The application includes:

- `Dockerfile` - Multi-stage production build
- `docker-compose.dev.yml` - Development environment
- `docker-compose.prod.yml` - Production environment with Nginx
- `nginx.conf` - Nginx reverse proxy configuration
- `.dockerignore` - Docker build exclusions
- `deploy.sh` - Automated deployment script (executable)

### Deployment Options

1. **Automated Script** (Recommended)
   ```bash
   ./deploy.sh
   ```

2. **Docker Compose - Development**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

3. **Docker Compose - Production**
   ```bash
   cp .env.production.template .env
   docker-compose -f docker-compose.prod.yml up -d
   ```

4. **Full Stack** (from project root)
   ```bash
   docker-compose up -d
   ```

See [DEPLOYMENT.md](./DEPLOYMENT.md) for detailed instructions.

## Troubleshooting

### Common Issues

#### 1. Application won't start

**Problem**: `pnpm dev` fails with errors

**Solutions**:
- Ensure Node.js 18+ is installed: `node --version`
- Clear node_modules and reinstall: `rm -rf node_modules && pnpm install`
- Clear Nuxt cache: `rm -rf .nuxt && pnpm dev`
- Check for port conflicts (port 3002): `lsof -i :3002`

#### 2. Cannot connect to API

**Problem**: API calls fail with network errors

**Solutions**:
- Verify backend API is running: `curl http://localhost:8080/health`
- Check `NUXT_PUBLIC_API_BASE` in `.env` matches your API URL
- Check for CORS issues in browser console
- Verify firewall/network settings allow connections

#### 3. Authentication not working

**Problem**: Login fails or redirects to login repeatedly

**Solutions**:
- Clear browser localStorage: `localStorage.clear()` in console
- Check backend `/saas/auth/login` endpoint is accessible
- Verify JWT token format in response matches expected structure
- Check browser console for 401/403 errors
- Ensure backend is returning valid JWT tokens

#### 4. Blank page or white screen

**Problem**: Application loads but shows blank page

**Solutions**:
- Check browser console for JavaScript errors
- Verify all environment variables are set correctly
- Clear browser cache and hard reload (Cmd+Shift+R / Ctrl+Shift+R)
- Check `.nuxt` directory exists: `ls -la .nuxt`
- Rebuild the application: `pnpm build && pnpm preview`

#### 5. Translations not working

**Problem**: UI shows translation keys instead of text

**Solutions**:
- Verify `locales/en.json` and `locales/ar.json` exist
- Check i18n configuration in `nuxt.config.ts`
- Clear Nuxt cache: `rm -rf .nuxt && pnpm dev`
- Check browser console for i18n errors

#### 6. Tests failing

**Problem**: `pnpm test` shows failures

**Solutions**:
- Ensure all dependencies are installed: `pnpm install`
- Check test environment setup in `test/setup.ts`
- Run tests with verbose output: `pnpm test -- --reporter=verbose`
- Check for missing test utilities: `pnpm add -D @vue/test-utils vitest`

#### 7. Docker build fails

**Problem**: Docker build or container won't start

**Solutions**:
- Check Docker is running: `docker ps`
- Verify `.env` file exists and is configured
- Check Dockerfile syntax and build logs
- Ensure sufficient disk space for Docker images
- Try rebuilding without cache: `docker-compose build --no-cache`

#### 8. Slow performance

**Problem**: Application is slow or unresponsive

**Solutions**:
- Check browser DevTools Network tab for slow API calls
- Verify backend API response times
- Clear browser cache and localStorage
- Check for memory leaks in browser DevTools Memory tab
- Disable browser extensions that might interfere
- Use production build for better performance: `pnpm build && pnpm preview`

### Getting Help

If you encounter issues not covered here:

1. Check the [Additional Documentation](#additional-documentation) section
2. Review browser console for error messages
3. Check backend API logs for errors
4. Review the [GitHub Issues](https://github.com/your-repo/issues) (if applicable)
5. Contact the development team

### Debug Mode

Enable debug logging by setting in browser console:

```javascript
localStorage.setItem('debug', 'true')
```

This will enable verbose logging for API calls and state changes.

## Additional Documentation

### User Documentation
- **[USER_GUIDE.md](./docs/USER_GUIDE.md)** - Complete user guide for SAAS managers
- **[TROUBLESHOOTING.md](./docs/TROUBLESHOOTING.md)** - Comprehensive troubleshooting guide

### Technical Documentation
- **[API_INTEGRATION.md](./docs/API_INTEGRATION.md)** - Detailed API integration documentation
- **[docs/accessibility.md](./docs/accessibility.md)** - Accessibility guide and WCAG compliance
- **[ACCESSIBILITY_QUICK_REFERENCE.md](./ACCESSIBILITY_QUICK_REFERENCE.md)** - Quick reference for developers
- **[docs/error-handling.md](./docs/error-handling.md)** - Error handling patterns
- **[docs/internationalization.md](./docs/internationalization.md)** - i18n implementation details
- **[docs/performance-optimizations.md](./docs/performance-optimizations.md)** - Performance optimization guide
- **[docs/responsive-design.md](./docs/responsive-design.md)** - Responsive design implementation
- **[docs/security.md](./docs/security.md)** - Security features and best practices
- **[test/README.md](./test/README.md)** - Testing documentation

### Deployment Documentation
- **[DEPLOYMENT.md](./DEPLOYMENT.md)** - Comprehensive deployment guide
- **[DEPLOYMENT-CHECKLIST.md](./DEPLOYMENT-CHECKLIST.md)** - Step-by-step deployment checklist
- **[QUICK-DEPLOY.md](./QUICK-DEPLOY.md)** - Quick reference for deployment

## Contributing

### Development Guidelines

1. Follow Vue 3 Composition API patterns
2. Use TypeScript for type safety
3. Write tests for new features
4. Follow existing code style and conventions
5. Update documentation for significant changes

### Code Style

- Use 2 spaces for indentation
- Use single quotes for strings
- Add trailing commas in objects and arrays
- Use meaningful variable and function names
- Add comments for complex logic

## License

[Your License Here]

## Support

For support and questions:
- Email: support@yourdomain.com
- Documentation: https://docs.yourdomain.com
- Issues: https://github.com/your-repo/issues
