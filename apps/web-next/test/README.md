# Web-Next Test Suite

This directory contains integration tests for the web-next application, with a focus on the manual theme selection system.

## Test Files

### Integration Tests

#### `theme-integration.test.js`
Comprehensive integration tests for the manual theme selection system.

**Coverage:**
- Tenant resolution with subdomain (Requirements: 4.5, 6.3)
- Tenant resolution with custom domain (Requirements: 6.3, 6.4)
- Theme loading for different tenants (Requirements: 4.5, 6.3)
- Theme switching via API (Requirement: 8.3)
- Fallback to default theme (Requirement: 4.5)
- End-to-end theme selection flow

**Usage:**
```bash
# Run automated tests
node test/theme-integration.test.js run

# View test guide
node test/theme-integration.test.js
```

#### `subdomain-routing.test.js`
Tests for subdomain routing and tenant resolution.

**Coverage:**
- Subdomain cookie setting
- API request headers
- Tenant context switching
- Data isolation

#### `api-themes-endpoint.test.js`
Tests for the themes API endpoint.

**Coverage:**
- GET /api/themes endpoint
- Response field validation
- Error handling
- Published themes filtering

### Test Scripts

#### `verify-theme-system.sh`
Quick verification script for the theme system.

**Usage:**
```bash
# Make executable (first time only)
chmod +x test/verify-theme-system.sh

# Run verification
./test/verify-theme-system.sh
```

**Checks:**
- Development server status
- Theme files existence
- Hosts file configuration
- API endpoints functionality
- Database connectivity

### Documentation

#### `THEME_INTEGRATION_TEST_GUIDE.md`
Comprehensive guide for testing the manual theme selection system.

**Contents:**
- Prerequisites and setup
- Automated test execution
- Manual browser testing
- API testing with cURL
- Verification checklist
- Troubleshooting guide
- Database queries
- CI/CD integration

## Quick Start

### 1. Setup

```bash
# Install dependencies
pnpm install

# Setup database
pnpm prisma:migrate
pnpm prisma:seed

# Configure hosts file (macOS/Linux)
sudo nano /etc/hosts
# Add:
# 127.0.0.1 clinic-a.localhost
# 127.0.0.1 clinic-b.localhost
# 127.0.0.1 custom.localhost

# Start development server
pnpm dev
```

### 2. Run Tests

```bash
# Quick verification
./test/verify-theme-system.sh

# Full automated tests
node test/theme-integration.test.js run

# Manual browser testing
# Open: http://clinic-a.localhost:3001
```

### 3. Verify Results

Check that:
- ✅ All automated tests pass
- ✅ Subdomains resolve correctly
- ✅ Themes load for different tenants
- ✅ Theme switching works via UI
- ✅ API endpoints respond correctly

## Test Requirements

### System Requirements
- Node.js 20+
- PostgreSQL database
- pnpm package manager
- jq (for shell scripts)
- curl (for API testing)

### Environment Variables
```bash
DATABASE_URL="postgresql://user:password@localhost:5432/dbname"
NEXT_PUBLIC_BASE_DOMAIN="localhost"
NEXT_PUBLIC_DEFAULT_TENANT="default"
```

### Database State
- Themes table populated with clinic, barber, default themes
- Tenants table with test tenants (clinic-a, clinic-b)
- Theme assignments configured

## Test Coverage

### Unit Tests
Currently, the test suite focuses on integration testing. Unit tests for individual components can be added using:
- Jest for React components
- Vitest for utility functions

### Integration Tests
✅ Tenant resolution (subdomain and custom domain)
✅ Theme loading and rendering
✅ API endpoints (themes, tenant, theme update)
✅ Theme switching functionality
✅ Fallback behavior

### End-to-End Tests
Manual browser testing covers:
- Complete user journey
- Theme settings page
- Visual verification
- Cross-browser compatibility

## Continuous Integration

### GitHub Actions Example

```yaml
name: Theme Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '20'
      
      - name: Install pnpm
        run: npm install -g pnpm
      
      - name: Install dependencies
        run: pnpm install
      
      - name: Setup database
        run: |
          pnpm prisma:migrate
          pnpm prisma:seed
        env:
          DATABASE_URL: postgresql://postgres:postgres@localhost:5432/test
      
      - name: Run tests
        run: |
          pnpm dev &
          sleep 5
          node test/theme-integration.test.js run
```

## Troubleshooting

### Common Issues

**Tests fail with "Server not running"**
- Ensure `pnpm dev` is running
- Check port 3001 is not in use
- Verify DATABASE_URL is correct

**Subdomain tests fail**
- Check /etc/hosts configuration
- Restart browser after hosts file changes
- Clear DNS cache

**Theme not loading**
- Verify theme files exist in themes/ directory
- Check database has theme records
- Review server console for errors

**API tests fail**
- Ensure database is seeded
- Check API routes exist
- Verify middleware is working

### Debug Commands

```bash
# Check database state
pnpm prisma:studio

# View server logs
pnpm dev

# Test API directly
curl http://localhost:3001/api/themes | jq

# Check theme files
ls -la themes/*/layout.tsx

# Verify hosts file
cat /etc/hosts | grep localhost
```

## Contributing

When adding new tests:

1. **Follow naming conventions**
   - Integration tests: `*.test.js`
   - Test scripts: `*.sh`
   - Documentation: `*.md`

2. **Document requirements**
   - Reference requirement numbers
   - Explain what is being tested
   - Provide expected results

3. **Include verification steps**
   - Automated checks where possible
   - Manual verification instructions
   - Troubleshooting guidance

4. **Update this README**
   - Add new test files to the list
   - Update coverage information
   - Add any new prerequisites

## Resources

- [Design Document](../.kiro/specs/manual-theme-selection/design.md)
- [Requirements Document](../.kiro/specs/manual-theme-selection/requirements.md)
- [Tasks Document](../.kiro/specs/manual-theme-selection/tasks.md)
- [Prisma Documentation](https://www.prisma.io/docs)
- [Next.js Testing](https://nextjs.org/docs/testing)

## Support

For issues or questions:
1. Review the test guide: `THEME_INTEGRATION_TEST_GUIDE.md`
2. Check troubleshooting section above
3. Review server and browser console logs
4. Verify database state with Prisma Studio
