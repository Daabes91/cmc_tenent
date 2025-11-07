# Project Phases Roadmap

## Phase 1 — Monorepo Foundations
- Establish pnpm workspace around `apps/api`, `apps/web-next`, and `apps/admin-nuxt`.
- Share base tooling (TypeScript configs, ESLint, Prettier, Tailwind tokens) across the two frontend stacks.
- Define environment variable templates for every app plus JWT key management for the API.

## Phase 2 — Backend Namespace Split
- Restructure the Spring Boot service into `public` vs `admin` modules with dedicated controllers, services, and DTOs.
- Add Flyway baseline migrations, seed data, and per-namespace security filter chains.
- Implement core entities (patients, doctors, appointments, services) consumed by both API surfaces.

## Phase 3 — Patient Web (Next.js)
- Build the marketing + booking shell in `apps/web-next` with Next.js 15 App Router, Next Intl, and SWR data hooks.
- Implement booking wizard, doctor/services listing, and bilingual theming helpers.
- Harden SEO defaults (metadata, sitemap, dynamic head) and add smoke tests for booking flows.

## Phase 4 — Admin Dashboard (Nuxt 3)
- Deliver the operations dashboard in `apps/admin-nuxt` using Nuxt 3 + @nuxt/ui with Tailwind 3 theming.
- Provide authenticated layouts, role-aware navigation, CRUD pages for appointments/doctors/patients, and reporting modules.
- Integrate runtime config + composables for Admin API access, optimistic updates, and local mocking utilities.

## Phase 5 — Security & Operations
- Finalize dual JWT flows (patient + staff), refresh rotation, 2FA hooks, IP allowlisting, and rate limiting policies.
- Extend CI/CD to lint/build both frontends and run Gradle tests, wiring secrets for Vercel + Fly.io deployments.
- Document deployment runbooks, staging verification steps, and incident response procedures.
