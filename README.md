# Clinic Platform Monorepo

This repository hosts the full-stack clinic platform composed of:

- `apps/api` — Spring Boot backend exposing public (`/public/**`) and staff (`/admin/**`) APIs.
- `apps/web-next` — Patient-facing Next.js 15 app (App Router) for marketing, bookings, and self-service flows.
- `apps/admin-nuxt` — Staff dashboard built with Nuxt 3 for operations, scheduling, and reporting.

## Development

```bash
pnpm dev:web-next      # Start patient web (Next.js)
pnpm dev:admin-nuxt    # Start staff dashboard (Nuxt 3)
pnpm dev:api           # Run Spring Boot API (Gradle)
pnpm dev:frontends     # Run both web-next and admin-nuxt together
```

Each app can also be developed independently by running `pnpm install` inside its directory and using the framework-specific CLI.

### Multi-tenant routing

The backend already scopes requests by tenant (header `X-Tenant-Slug`, query param `tenant`, or the default slug). The frontends now resolve that slug automatically:

- **Patient web (`apps/web-next`)** – Configure `NEXT_PUBLIC_BASE_DOMAIN` (e.g. `clinic.dev`) and `NEXT_PUBLIC_DEFAULT_TENANT` in `.env`. Visiting `https://<slug>.clinic.dev` sets the slug cookie and all API calls include `X-Tenant-Slug`. For local testing, append `?tenant=<slug>` once and the middleware will persist it.
- **Admin dashboard (`apps/admin-nuxt`)** – Set `NUXT_PUBLIC_DEFAULT_TENANT` and use the new tenant badge (top-right) to switch clinics at runtime. You can also use the same `?tenant=<slug>` helper.

Under the hood both apps store the active slug in a shared `tenantSlug` cookie and mirror it into the `X-Tenant-Slug` header so every API request hits the correct tenant. Clear the cookie or navigate with a different `?tenant=` parameter to switch contexts quickly when demoing multiple clinics.

## Docs & Deployment

- `docs/project-phases.md` — condensed delivery roadmap across the API, patient web, and admin dashboard.
- `docs/digitalocean-deployment-guide.md` — step-by-step deployment guide (Fly.io + Vercel).
- GitHub Actions workflows (`.github/workflows/*.yml`) cover CI, staging deploys, and tagged releases for the three active apps only.

Historical apps and the shared UI kit have been removed to keep the repository focused on the current production stack.
