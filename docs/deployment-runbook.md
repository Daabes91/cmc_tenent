# Deployment Runbook

## Prerequisites
- GitHub repository connected with required secrets:
  - `VERCEL_TOKEN`, `VERCEL_ORG_ID`, `VERCEL_WEB_PROJECT_ID`, `VERCEL_ADMIN_PROJECT_ID`
  - `FLY_API_TOKEN`, `FLY_APP_NAME`, `FLY_STAGING_APP_NAME`
- Vercel projects configured with environment variables from `.env.production.example` (web) and `.env.production.example` (admin).
- Fly.io app created with Postgres attachment and secrets set via `flyctl secrets set` (see variables in `apps/api/.env.example`).
- Docker registry access for Fly.io (automatically provided when using `flyctl`).

## Release Flow
1. Create release branch `release/x.y.z`; ensure CHANGELOG is updated.
2. Merge release branch into `main` via PR to trigger staging deployment (workflow `staging.yml`).
3. Validate staging:
   - Web staging URL responds and booking flow works.
   - Admin staging URL authenticates with mock or test staff user.
   - API health check `https://clinic-api-staging.fly.dev/actuator/health` returns `UP`.
4. Tag commit: `git tag vX.Y.Z && git push origin vX.Y.Z`.
5. GitHub Actions `deploy.yml` runs:
   - Builds and deploys web/admin to Vercel production.
   - Builds API image, deploys to Fly.io, runs Flyway migrations.
6. Post-deploy validation:
   - Verify Fly.io release `flyctl status --app $FLY_APP_NAME`.
   - Confirm DB migrations applied (`flyctl ssh console` and inspect `flyway_schema_history`).
   - Run synthetic booking and staff login tests.
7. Close release: update documentation, notify stakeholders.

## Rollback Procedure
- Web/Admin: use Vercel dashboard to promote previous deployment or re-run `vercel rollback <deploymentId>`.
- API:
  1. `flyctl releases --app $FLY_APP_NAME` to list releases.
  2. `flyctl deploy --image <previous-image> --app $FLY_APP_NAME`.
  3. If migrations failed, manually revert using Flyway baseline or restore from managed DB snapshot.

## Monitoring & Alerts
- Enable Fly.io HTTP checks (configured in `fly.toml`).
- Add BetterStack or Pingdom monitors to public/admin URLs, verifying status codes and latency.
- Configure log shipping from Fly.io to centralized logging (Datadog/New Relic) using `flyctl logs` integration or syslog drains.
- Set up alerts for rate-limit breaches and 401/403 spikes via structured logging fields.

## Secrets Rotation
- Generate new RSA keys; update GitHub secrets and Fly.io secrets.
- Redeploy API; rotate refresh tokens by forcing logout or shortening TTL temporarily.
- Update Vercel environment variables and redeploy web/admin.

## Disaster Recovery Checklist
- Maintain daily automated DB backups; verify restore process quarterly.
- Store infrastructure as code artifacts (Fly configs, Vercel settings) within repository.
- Document emergency contacts and escalation policy.
