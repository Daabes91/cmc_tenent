# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Multi-application clinic platform monorepo with patient-facing web app, staff admin app, and Spring Boot backend API. Uses pnpm workspaces with separate namespaced API endpoints for public and admin functionality.

## Architecture

### API Module Structure (`apps/api/src/main/java/com/clinic`)
- **`modules/publicapi/`** — Patient-facing endpoints under `/public/**`
  - Services: BookingService, PatientAuthService
  - Anonymous access allowed for services/doctors listing, booking, patient auth
- **`modules/admin/`** — Staff-only endpoints under `/admin/**`
  - Services: AppointmentService, DoctorService, etc.
  - Protected by staff JWT + role checks (ADMIN/RECEPTIONIST/DOCTOR)
- **`modules/core/`** — Shared JPA entities and repositories
  - Entities: AppointmentEntity, DoctorEntity, PatientEntity, ClinicServiceEntity
  - Used by both public and admin services
- **`security/`** — Authentication and authorization infrastructure
  - Dual JWT system: PatientJwtAuthenticationFilter + StaffJwtAuthenticationFilter
  - JwtIssuer for token generation (uses RSA keys in root: `staff_private.pem`/`staff_public.pem`)
  - RateLimitingFilter, AdminIpFilter
  - SecurityConfig defines two separate SecurityFilterChain beans (admin vs public)
- **`config/`** — CORS, rate-limiting, database configs

### Frontend Apps
- **`apps/web-next`** — Next.js 15 (App Router) patient-facing marketing, booking, and self-service flows
- **`apps/admin-nuxt`** — Nuxt 3 admin dashboard (current/active implementation)
  - Uses @nuxt/ui + Tailwind v3
  - API base configured via `runtimeConfig.public.apiBase`
  - Pages for appointments, doctors, patients, reports, services, settings

### Database
- PostgreSQL with Flyway migrations in `apps/api/src/main/resources/db/migration/`
- Migration naming: `V1__baseline_schema.sql`, `V2__staff_auth_seed.sql`, etc.
- Baseline applied automatically on first run (see `application.yaml`)

## Development Commands

### Root Level
```bash
pnpm dev:web-next          # Start patient web app (Next.js)
pnpm dev:admin-nuxt        # Start Nuxt admin dashboard
pnpm dev:api               # Run Spring Boot backend via Gradle
pnpm dev:frontends         # Run both web-next and admin-nuxt concurrently
pnpm lint                  # Lint all workspace packages
pnpm build                 # Build all workspace packages
```

### API Backend (`apps/api/`)
```bash
./gradlew bootRun          # Run Spring Boot API locally (port 8080)
./gradlew build            # Compile and run tests
./gradlew test             # Run JUnit tests only
./gradlew clean build      # Clean build
```

**Important:** API requires PostgreSQL running. Connection details in `apps/api/.env.local` (see `.env.example`). Default: `jdbc:postgresql://localhost:5432/clinic`

### Frontend Apps
```bash
cd apps/admin-nuxt && pnpm dev          # Start Nuxt admin on port 3000
cd apps/web-next && pnpm dev            # Start Next.js web app on port 3001
```

## Security Implementation Details

### JWT Authentication
- **Patient tokens:** Issued by `/public/auth/login` or `/public/auth/signup`
- **Staff tokens:** Issued by `/admin/auth/login`
- Both use RSA-256 signatures
- Public keys configured in `application.yaml` (JWT_PATIENT_PUBLIC_KEY, JWT_STAFF_PUBLIC_KEY)
- Private keys in repo root (`staff_private.pem`, `staff_public.pem`) — **DO NOT COMMIT to production repos**
- Token verification: RsaJwtVerifier handles signature validation and claims checks

### Role-Based Access Control (RBAC)
- Staff roles: ADMIN, RECEPTIONIST, DOCTOR
- Roles checked in SecurityConfig and can be verified at method level using `@PreAuthorize`
- Admin API requires at least one role from staff set

### Rate Limiting
- RateLimitingFilter configured per-endpoint pattern (public auth, booking, admin auth)
- Capacity and refill periods in `application.yaml` under `security.rate-limiting`
- Uses token bucket algorithm per IP

### IP Allowlisting
- AdminIpFilter restricts `/admin/**` access by IP (disabled in dev, enable in prod)
- Configured via `security.admin.ip-allowlist` in `application.yaml`

## Testing

### API Tests
```bash
cd apps/api
./gradlew test                              # Run all tests
./gradlew test --tests '*BookingService*'   # Run specific test class
```

Tests use JUnit 5 and Spring Boot Test. Located in `apps/api/src/test/java/`

## Deployment

### CI/CD Workflows (`.github/workflows/`)
- **`ci.yml`** — Lint and build checks on all PRs
- **`staging.yml`** — Deploy to staging on merge to main
- **`deploy.yml`** — Deploy to production on version tags (`vX.Y.Z`)

### Deployment Targets
- **Frontend apps:** Vercel (web and admin-nuxt)
  - Requires `VERCEL_TOKEN`, `VERCEL_ORG_ID`, `VERCEL_WEB_PROJECT_ID`, `VERCEL_ADMIN_PROJECT_ID` secrets
- **API backend:** Fly.io
  - Configured via `fly.toml` (production) and `fly.staging.toml`
  - Requires `FLY_API_TOKEN`, `FLY_APP_NAME`, `FLY_STAGING_APP_NAME`
  - Docker build via Dockerfile in `apps/api/`

### Database Migrations
Migrations run automatically on API deployment via Flyway. To add a new migration:
1. Create `apps/api/src/main/resources/db/migration/V{N}__{description}.sql`
2. Ensure version number increments sequentially
3. Test locally before deploying

See `docs/deployment-runbook.md` for detailed release process, rollback, monitoring, and secrets rotation.

## Environment Variables

### API (`apps/api/.env.local`)
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `JWT_PATIENT_PUBLIC_KEY`, `JWT_STAFF_PUBLIC_KEY` (PEM format, newlines as `\n`)
- `PUBLIC_APP_ORIGIN`, `ADMIN_APP_ORIGIN` (CORS configuration)
- `CLINIC_TIMEZONE_ZONE_ID` — Clinic timezone (IANA identifier, e.g., `Asia/Dubai`, `Asia/Amman`). Default: `Asia/Amman`
- Rate limit capacities and IP allowlist overrides

### Admin Nuxt (`apps/admin-nuxt`)
- `NUXT_PUBLIC_API_BASE` — API base URL (default: `http://localhost:8080/admin`)

### Web App (`apps/web-next`)
- `NEXT_PUBLIC_API_URL` — Public API base URL

## Key Files and Locations

- **API main class:** `apps/api/src/main/java/com/clinic/ClinicApplication.java`
- **Security config:** `apps/api/src/main/java/com/clinic/config/SecurityConfig.java`
- **Spring config:** `apps/api/src/main/resources/application.yaml`
- **Admin Nuxt config:** `apps/admin-nuxt/nuxt.config.ts`
- **Shared TypeScript config:** `tsconfig.base.json`
- **Workspace definition:** `pnpm-workspace.yaml`

## Project Documentation

Active docs live in `docs/`:
- `project-phases.md` — Roadmap overview focused on the API, web-next, and admin-nuxt
- `digitalocean-deployment-guide.md` — Production deployment procedures and troubleshooting

## Common Patterns

### Adding a New Admin API Endpoint
1. Create DTO in `apps/api/src/main/java/com/clinic/modules/admin/dto/`
2. Add service method in appropriate service (e.g., `AppointmentService`)
3. Create/update controller in `modules/admin/controller/`
4. Update SecurityConfig if new path pattern needs custom auth rules
5. Test with staff JWT token

### Adding a New Public API Endpoint
1. Create DTO in `apps/api/src/main/java/com/clinic/modules/publicapi/dto/`
2. Add service method (e.g., `BookingService`)
3. Create/update controller in `modules/publicapi/controller/`
4. Ensure endpoint added to permitAll() in SecurityConfig if public, or requires patient auth

### Creating a New Entity
1. Add entity in `modules/core/{domain}/` (e.g., `core/appointment/AppointmentEntity.java`)
2. Create repository interface extending JpaRepository
3. Write Flyway migration in `db/migration/V{N}__*.sql`
4. Restart API to apply migration

## Notes

- **RSA keys in repo:** The `staff_private.pem` and `staff_public.pem` in root are for local development only. Production keys must be managed via secrets.
- **pnpm version:** Project uses pnpm 8.15.0 (see `packageManager` in root `package.json`)
- **Java version:** API requires Java 21 (configured in `build.gradle.kts`)
- **Timezone configuration:** The system uses a configurable timezone for booking slots and availability. Set via `CLINIC_TIMEZONE_ZONE_ID` environment variable. Common values:
  - `Asia/Amman` (Jordan, UTC+3) — default
  - `Asia/Dubai` (UAE, UTC+4)
  - `Asia/Riyadh` (Saudi Arabia, UTC+3)
  - See [IANA timezone list](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones) for all valid identifiers
