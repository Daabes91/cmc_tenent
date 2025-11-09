# Clinic Platform Monorepo

This repository hosts the full-stack multi-tenant clinic platform composed of:

- `apps/api` — Spring Boot backend exposing public (`/public/**`), staff (`/admin/**`), and SAAS manager (`/saas/**`) APIs.
- `apps/web-next` — Patient-facing Next.js 15 app (App Router) for marketing, bookings, and self-service flows.
- `apps/admin-nuxt` — Staff dashboard built with Nuxt 3 for operations, scheduling, and reporting.
- `apps/saas-admin-nuxt` — **NEW!** SAAS Manager Admin Panel with full accessibility support (WCAG 2.1 AA compliant).

## 🚀 Quick Start

**Get started in 5 minutes:**

```bash
# Option 1: Run everything in Docker
./start-all.sh

# Option 2: Run locally (faster for development)
./start-local.sh
# Then start API and apps in separate terminals
```

**📖 See [QUICKSTART.md](./QUICKSTART.md) for detailed instructions**

**Access the applications:**
- SAAS Admin Panel: http://localhost:3002 ✨ **NEW - with accessibility features!**
- Tenant Admin Panel: http://localhost:3000
- Public Web App: http://localhost:3001
- API: http://localhost:8080

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

## 📚 Documentation

### Getting Started
- **[QUICKSTART.md](./QUICKSTART.md)** — Get running in 5 minutes
- **[RUNNING.md](./RUNNING.md)** — Comprehensive guide for all run options
- **[DOCKER_FIX_SUMMARY.md](./DOCKER_FIX_SUMMARY.md)** — Docker build troubleshooting

### SAAS Admin Panel (NEW!)
- **[apps/saas-admin-nuxt/README.md](./apps/saas-admin-nuxt/README.md)** — Full feature documentation
- **[apps/saas-admin-nuxt/docs/accessibility.md](./apps/saas-admin-nuxt/docs/accessibility.md)** — WCAG 2.1 AA compliance guide
- **[apps/saas-admin-nuxt/ACCESSIBILITY_QUICK_REFERENCE.md](./apps/saas-admin-nuxt/ACCESSIBILITY_QUICK_REFERENCE.md)** — Developer quick reference
- **[apps/saas-admin-nuxt/DEPLOYMENT.md](./apps/saas-admin-nuxt/DEPLOYMENT.md)** — Production deployment guide

### Project Documentation
- `docs/project-phases.md` — Delivery roadmap
- `docs/digitalocean-deployment-guide.md` — Deployment guide (Fly.io + Vercel)
- GitHub Actions workflows (`.github/workflows/*.yml`) — CI/CD pipelines

## ♿ Accessibility

The SAAS Admin Panel includes comprehensive accessibility features:
- ✅ Full keyboard navigation with skip links
- ✅ Screen reader support (ARIA labels, landmarks, live regions)
- ✅ WCAG AA compliant color contrast (4.5:1 for text)
- ✅ Visible focus indicators on all interactive elements
- ✅ Responsive design with 44x44px touch targets
- ✅ Reduced motion support
- ✅ Internationalization (English & Arabic with RTL)

**Test it:** http://localhost:3002/accessibility-test
