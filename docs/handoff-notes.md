# Handoff Notes

## Repository Overview
- Workspace: pnpm-based monorepo with Next.js patient app (`apps/web-next`), Nuxt admin dashboard (`apps/admin-nuxt`), and Spring Boot API (`apps/api`).
- CI Pipelines: GitHub Actions workflows for CI, staging, and production deployments. Adjust secrets before first run.
- Infrastructure: Vercel for frontend hosting, Fly.io for backend, PostgreSQL via managed service, optional S3/GCS for assets.

## Configuration Checklist
1. Populate GitHub repository secrets listed in `docs/deployment-runbook.md`.
2. Configure Vercel projects with environment variables from respective `.env.production.example` files.
3. Create Fly.io apps (`clinic-api`, `clinic-api-staging`) and run `flyctl secrets set` using `apps/api/.env.example` as reference.
4. Provision PostgreSQL, update `SPRING_DATASOURCE_*` values, and allow network access from Fly.io.
5. Upload RSA public keys for JWT verification and align issuers/audiences with auth provider.

## Testing & QA
- Frontend checks via `pnpm --filter web-next lint` and `pnpm --filter admin-nuxt build`.
- API tests via `./gradlew test` in `apps/api`.
- Recommended: add Playwright smoke tests for booking flow and Cypress tests for admin navigation.

## Launch Readiness
- Run CI on main branch to confirm pipelines succeed.
- Deploy to staging (automatic on main push) and validate checklists.
- Tag release when ready; monitor Fly.io metrics and Vercel dashboards.

## Future Enhancements
- Integrate real authentication providers, connect API to persistence layer, and add E2E coverage.
- Set up observability stack (Datadog/New Relic) and incident response playbooks.
- Consider infrastructure-as-code (Terraform) for Fly.io/Vercel resources and secrets automation.
