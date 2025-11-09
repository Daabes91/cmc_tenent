# How to Run the Multi-Tenant Clinic Management System

This guide provides multiple ways to run the complete system.

## Prerequisites

- **Docker Desktop** - [Download here](https://www.docker.com/products/docker-desktop)
- **Java 17+** - For running API locally
- **Node.js 18+** - For running frontend apps locally

## Quick Start Options

### Option 1: Docker Compose (Recommended for Testing)

Run everything in Docker containers:

```bash
# Make sure Docker Desktop is running first!

# Start all services
./start-all.sh

# Or manually:
docker compose up --build
```

**Access the applications:**
- SAAS Admin Panel: http://localhost:3002
- Tenant Admin Panel: http://localhost:3000
- Public Web App: http://localhost:3001
- API: http://localhost:8080
- API Docs: http://localhost:8080/swagger-ui.html

**Useful commands:**
```bash
# View logs
docker compose logs -f

# View logs for specific service
docker compose logs -f saas-admin

# Stop all services
docker compose down

# Stop and remove volumes
docker compose down -v

# Rebuild specific service
docker compose up --build saas-admin
```

### Option 2: Local Development (Recommended for Development)

Run apps locally for faster development with hot-reload:

```bash
# 1. Start database
./start-local.sh

# 2. Start API (Terminal 1)
cd apps/api
./mvnw spring-boot:run

# 3. Start SAAS Admin Panel (Terminal 2)
cd apps/saas-admin-nuxt
npm install
npm run dev

# 4. Start Tenant Admin (Terminal 3 - Optional)
cd apps/admin-nuxt
npm install
npm run dev

# 5. Start Public Web (Terminal 4 - Optional)
cd apps/web-next
npm install
npm run dev
```

**Access the applications:**
- SAAS Admin Panel: http://localhost:3002
- Tenant Admin Panel: http://localhost:3001
- Public Web App: http://localhost:3000
- API: http://localhost:8080

### Option 3: Hybrid (Database in Docker, Apps Local)

Best of both worlds - database in Docker, apps running locally:

```bash
# Start PostgreSQL
docker run -d --name clinic-postgres \
  -e POSTGRES_DB=clinic_multi_tenant \
  -e POSTGRES_USER=clinic \
  -e POSTGRES_PASSWORD=clinic_password \
  -p 5442:5432 \
  postgres:15

# Then follow steps 2-5 from Option 2
```

## Testing the SAAS Admin Panel

The SAAS Admin Panel includes comprehensive accessibility features:

```bash
# Start the SAAS Admin Panel
cd apps/saas-admin-nuxt
npm run dev

# Open in browser
open http://localhost:3002

# Test accessibility features
open http://localhost:3002/accessibility-test
```

### Accessibility Testing

1. **Keyboard Navigation**
   - Press `Tab` to see skip link
   - Navigate through all interactive elements
   - Test forms, tables, and modals

2. **Screen Reader (macOS)**
   - Enable VoiceOver: `Cmd + F5`
   - Navigate with `VO + Right Arrow`
   - Test all pages and components

3. **Lighthouse Audit**
   - Open Chrome DevTools: `Cmd + Option + I`
   - Go to Lighthouse tab
   - Run accessibility audit
   - Should score 95+

## Environment Variables

### API
Create `apps/api/.env` or use `application.properties`:
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5442/clinic_multi_tenant
SPRING_DATASOURCE_USERNAME=clinic
SPRING_DATASOURCE_PASSWORD=clinic_password
JWT_SECRET=your-secret-key-here
```

### SAAS Admin Panel
Create `apps/saas-admin-nuxt/.env`:
```bash
NUXT_PUBLIC_API_BASE=http://localhost:8080
NUXT_PUBLIC_SAAS_API_BASE=http://localhost:8080/saas
```

### Tenant Admin Panel
Create `apps/admin-nuxt/.env`:
```bash
NUXT_PUBLIC_API_BASE=http://localhost:8080/admin
```

### Public Web App
Create `apps/web-next/.env`:
```bash
NEXT_PUBLIC_API_URL=http://localhost:8080/public
```

## Troubleshooting

### Docker Not Running
```bash
# Error: Cannot connect to Docker daemon
# Solution: Start Docker Desktop application
```

### Port Already in Use
```bash
# Find and kill process using port
lsof -ti:3002 | xargs kill -9

# Or change port in nuxt.config.ts
```

### Database Connection Failed
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs clinic-postgres

# Restart database
docker restart clinic-postgres
```

### API Won't Start
```bash
# Check Java version
java -version  # Should be 17+

# Clean and rebuild
cd apps/api
./mvnw clean install
```

### Frontend Build Errors
```bash
# Clear cache and reinstall
cd apps/saas-admin-nuxt
rm -rf node_modules .nuxt .output package-lock.json
npm install
npm run dev
```

### Docker Build Fails
```bash
# Clean Docker cache
docker builder prune -af

# Remove old containers
docker compose down -v

# Rebuild from scratch
docker compose build --no-cache

# Start services
docker compose up
```

## Development Workflow

### Recommended Setup

1. **Database**: Run in Docker (always on)
2. **API**: Run locally (hot reload with Spring Boot DevTools)
3. **SAAS Admin**: Run locally (hot reload with Nuxt)
4. **Other Apps**: Run as needed

### Hot Reload

All apps support hot reload:
- **API**: Spring Boot DevTools auto-restart
- **Nuxt Apps**: Vite HMR (instant updates)
- **Next.js**: Fast Refresh

### Running Tests

```bash
# SAAS Admin Panel tests
cd apps/saas-admin-nuxt
npm run test

# API tests
cd apps/api
./mvnw test
```

## Production Deployment

See individual deployment guides:
- `apps/saas-admin-nuxt/DEPLOYMENT.md`
- `apps/api/README.md`
- `apps/admin-nuxt/README.md`
- `apps/web-next/README.md`

## Getting Help

- Check `apps/saas-admin-nuxt/docs/TROUBLESHOOTING.md`
- Check `apps/saas-admin-nuxt/docs/accessibility.md`
- Review application logs
- Check Docker logs: `docker compose logs -f`

## Quick Reference

### Start Everything (Docker)
```bash
./start-all.sh
```

### Start Local Development
```bash
./start-local.sh
# Then start API and apps in separate terminals
```

### Stop Everything
```bash
# Docker
docker compose down

# Local (Ctrl+C in each terminal)
```

### View Logs
```bash
# Docker
docker compose logs -f

# Local (check each terminal)
```

### Access Applications
- SAAS Admin: http://localhost:3002
- Tenant Admin: http://localhost:3001 or 3000
- Public Web: http://localhost:3000 or 3001
- API: http://localhost:8080
- API Docs: http://localhost:8080/swagger-ui.html

## Next Steps

1. Create SAAS manager account via API
2. Login to SAAS Admin Panel
3. Create your first tenant
4. Test accessibility features
5. Configure tenant branding
6. Monitor system health

Enjoy building with the Multi-Tenant Clinic Management System! 🚀
