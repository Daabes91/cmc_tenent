# Theme System Deployment Guide

Complete guide for deploying the Manual Theme Selection system to production.

## Table of Contents

- [Pre-Deployment Checklist](#pre-deployment-checklist)
- [Environment Setup](#environment-setup)
- [Database Setup](#database-setup)
- [Deployment Platforms](#deployment-platforms)
- [DNS Configuration](#dns-configuration)
- [Post-Deployment Verification](#post-deployment-verification)
- [Rollback Procedures](#rollback-procedures)

## Pre-Deployment Checklist

Before deploying, ensure:

- [ ] All theme files are committed to repository
- [ ] Database migrations are tested
- [ ] Environment variables are documented
- [ ] Theme layouts compile without errors
- [ ] Integration tests pass
- [ ] DNS configuration is planned
- [ ] Backup strategy is in place

## Environment Setup

### Required Environment Variables

Set these environment variables in your production environment:

```bash
# Database
DATABASE_URL="postgresql://user:password@host:5432/dbname?sslmode=require"

# Application Domain
APP_BASE_DOMAIN="yourdomain.com"
NEXT_PUBLIC_BASE_DOMAIN="yourdomain.com"
NEXT_PUBLIC_DEFAULT_TENANT="default"

# API Configuration
NEXT_PUBLIC_API_URL="https://api.yourdomain.com/api/public"

# Node Environment
NODE_ENV="production"
```

### Security Considerations

1. **Database URL**: Use SSL mode for production databases
2. **Secrets**: Never commit `.env` files to repository
3. **Access Control**: Restrict database access to application servers only
4. **Backups**: Enable automated database backups

## Database Setup

### Step 1: Backup Existing Database

Before running migrations, backup your database:

```bash
# PostgreSQL backup
pg_dump -h hostname -U username -d dbname > backup_$(date +%Y%m%d_%H%M%S).sql

# Or use your cloud provider's backup tools
```

### Step 2: Run Migrations

Apply database migrations to create Theme and Tenant tables:

```bash
cd apps/web-next

# Install dependencies
npm install

# Generate Prisma client
npx prisma generate

# Apply migrations (production mode)
npx prisma migrate deploy
```

**Important:** Use `migrate deploy` in production, not `migrate dev`.

### Step 3: Verify Schema

Check that tables were created correctly:

```sql
-- Connect to your database and run:
\dt "Theme"
\dt "Tenant"

-- Verify columns
\d "Theme"
\d "Tenant"
```

Expected output:
- Theme table with columns: id, key, name, status, createdAt, updatedAt
- Tenant table with columns: id, slug, domain, status, themeId, createdAt, updatedAt

### Step 4: Seed Initial Data

Add initial themes to the database:

```bash
# Option 1: Run seed script
npx prisma db seed

# Option 2: Manual SQL
psql $DATABASE_URL <<EOF
INSERT INTO "Theme" (id, key, name, status, "createdAt", "updatedAt")
VALUES 
  (gen_random_uuid()::text, 'clinic', 'Clinic Theme', 'published', NOW(), NOW()),
  (gen_random_uuid()::text, 'barber', 'Barber Theme', 'published', NOW(), NOW()),
  (gen_random_uuid()::text, 'default', 'Default Theme', 'published', NOW(), NOW());
EOF
```

### Step 5: Migrate Existing Tenants

If you have existing tenants, assign them themes:

```sql
-- Get theme IDs
SELECT id, key, name FROM "Theme";

-- Update tenants with default theme
UPDATE "Tenant" 
SET "themeId" = (SELECT id FROM "Theme" WHERE key = 'default')
WHERE "themeId" IS NULL;
```

## Deployment Platforms

### Vercel Deployment

#### 1. Configure Project

```bash
# Install Vercel CLI
npm i -g vercel

# Login
vercel login

# Link project
cd apps/web-next
vercel link
```

#### 2. Set Environment Variables

```bash
# Set production environment variables
vercel env add DATABASE_URL production
vercel env add APP_BASE_DOMAIN production
vercel env add NEXT_PUBLIC_BASE_DOMAIN production
vercel env add NEXT_PUBLIC_DEFAULT_TENANT production
vercel env add NEXT_PUBLIC_API_URL production
```

Or use the Vercel dashboard:
1. Go to Project Settings → Environment Variables
2. Add each variable for Production environment

#### 3. Configure Build Settings

In `vercel.json`:

```json
{
  "buildCommand": "npx prisma generate && npm run build",
  "installCommand": "npm install",
  "framework": "nextjs",
  "regions": ["iad1"]
}
```

#### 4. Deploy

```bash
# Deploy to production
vercel --prod

# Or push to main branch (if auto-deploy enabled)
git push origin main
```

#### 5. Run Post-Deploy Migrations

```bash
# SSH into Vercel or use Vercel CLI
vercel env pull .env.production
DATABASE_URL=$(cat .env.production | grep DATABASE_URL | cut -d '=' -f2-) \
  npx prisma migrate deploy
```

### Docker Deployment

#### 1. Create Dockerfile

```dockerfile
FROM node:18-alpine AS base

# Install dependencies only when needed
FROM base AS deps
RUN apk add --no-cache libc6-compat
WORKDIR /app

COPY package*.json ./
RUN npm ci

# Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Generate Prisma client
RUN npx prisma generate

# Build application
ENV NEXT_TELEMETRY_DISABLED 1
RUN npm run build

# Production image
FROM base AS runner
WORKDIR /app

ENV NODE_ENV production
ENV NEXT_TELEMETRY_DISABLED 1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static
COPY --from=builder /app/node_modules/.prisma ./node_modules/.prisma
COPY --from=builder /app/prisma ./prisma

USER nextjs

EXPOSE 3000

ENV PORT 3000
ENV HOSTNAME "0.0.0.0"

CMD ["node", "server.js"]
```

#### 2. Build Image

```bash
cd apps/web-next
docker build -t web-next:latest .
```

#### 3. Run Container

```bash
docker run -d \
  --name web-next \
  -p 3000:3000 \
  -e DATABASE_URL="postgresql://..." \
  -e APP_BASE_DOMAIN="yourdomain.com" \
  -e NEXT_PUBLIC_BASE_DOMAIN="yourdomain.com" \
  -e NEXT_PUBLIC_DEFAULT_TENANT="default" \
  -e NEXT_PUBLIC_API_URL="https://api.yourdomain.com/api/public" \
  -e NODE_ENV="production" \
  web-next:latest
```

#### 4. Run Migrations

```bash
# Execute migrations in container
docker exec web-next npx prisma migrate deploy
```

### AWS/DigitalOcean/Custom VPS

#### 1. Install Node.js

```bash
# Ubuntu/Debian
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Verify installation
node --version
npm --version
```

#### 2. Clone Repository

```bash
cd /var/www
git clone https://github.com/your-org/your-repo.git
cd your-repo/apps/web-next
```

#### 3. Install Dependencies

```bash
npm ci --production
```

#### 4. Configure Environment

```bash
# Create .env file
cat > .env <<EOF
DATABASE_URL="postgresql://..."
APP_BASE_DOMAIN="yourdomain.com"
NEXT_PUBLIC_BASE_DOMAIN="yourdomain.com"
NEXT_PUBLIC_DEFAULT_TENANT="default"
NEXT_PUBLIC_API_URL="https://api.yourdomain.com/api/public"
NODE_ENV="production"
EOF

# Secure the file
chmod 600 .env
```

#### 5. Build Application

```bash
npx prisma generate
npm run build
```

#### 6. Setup Process Manager

Using PM2:

```bash
# Install PM2
npm install -g pm2

# Start application
pm2 start npm --name "web-next" -- start

# Save PM2 configuration
pm2 save

# Setup auto-restart on reboot
pm2 startup
```

#### 7. Configure Nginx

```nginx
server {
    listen 80;
    server_name yourdomain.com *.yourdomain.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Enable and restart Nginx:

```bash
sudo ln -s /etc/nginx/sites-available/web-next /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

#### 8. Setup SSL

Using Certbot:

```bash
sudo apt-get install certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com -d *.yourdomain.com
```

## DNS Configuration

### Wildcard Subdomain Setup

For subdomain-based tenant resolution, configure wildcard DNS:

#### Cloudflare

1. Go to DNS settings
2. Add A record:
   - Type: `A`
   - Name: `*`
   - Content: `your-server-ip`
   - Proxy status: DNS only (gray cloud)
   - TTL: Auto

#### AWS Route 53

```bash
aws route53 change-resource-record-sets \
  --hosted-zone-id YOUR_ZONE_ID \
  --change-batch '{
    "Changes": [{
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": "*.yourdomain.com",
        "Type": "A",
        "TTL": 300,
        "ResourceRecords": [{"Value": "your-server-ip"}]
      }
    }]
  }'
```

#### DigitalOcean

1. Go to Networking → Domains
2. Add record:
   - Type: `A`
   - Hostname: `*`
   - Will direct to: `your-droplet`
   - TTL: 3600

### Custom Domain Setup

For tenants using custom domains:

1. **Tenant configures DNS:**
   ```
   www.tenant-domain.com  CNAME  yourdomain.com
   ```

2. **Update tenant record:**
   ```sql
   UPDATE "Tenant" 
   SET domain = 'www.tenant-domain.com' 
   WHERE slug = 'tenant-slug';
   ```

3. **Verify DNS propagation:**
   ```bash
   dig www.tenant-domain.com
   nslookup www.tenant-domain.com
   ```

## Post-Deployment Verification

### 1. Health Check

Verify application is running:

```bash
curl https://yourdomain.com
```

Expected: 200 OK response

### 2. Database Connection

Check database connectivity:

```bash
# In application directory
npx prisma db execute --stdin <<< "SELECT COUNT(*) FROM \"Theme\";"
```

Expected: Returns count of themes

### 3. Theme System

Test theme loading:

```bash
# Test subdomain resolution
curl -I https://clinic-a.yourdomain.com

# Test API endpoints
curl https://yourdomain.com/api/themes

# Test theme switching
curl -X POST https://yourdomain.com/api/tenants/clinic-a/theme \
  -H "Content-Type: application/json" \
  -d '{"themeId": "theme-id"}'
```

### 4. Tenant Resolution

Test both subdomain and custom domain:

```bash
# Subdomain
curl -I https://tenant-slug.yourdomain.com

# Custom domain (if configured)
curl -I https://custom-domain.com
```

### 5. Monitor Logs

Check application logs for errors:

```bash
# PM2
pm2 logs web-next

# Docker
docker logs web-next

# Vercel
vercel logs
```

## Rollback Procedures

### Application Rollback

#### Vercel

```bash
# List deployments
vercel ls

# Rollback to previous deployment
vercel rollback [deployment-url]
```

#### Docker

```bash
# Stop current container
docker stop web-next

# Start previous version
docker run -d --name web-next web-next:previous-tag
```

#### PM2

```bash
# Pull previous version
git checkout previous-commit

# Rebuild
npm run build

# Restart
pm2 restart web-next
```

### Database Rollback

If migrations cause issues:

```bash
# Restore from backup
psql $DATABASE_URL < backup_20240101_120000.sql

# Or use cloud provider's restore feature
```

### Theme Rollback

If a theme causes issues:

1. **Revert theme assignment:**
   ```sql
   UPDATE "Tenant" 
   SET "themeId" = (SELECT id FROM "Theme" WHERE key = 'default')
   WHERE slug = 'affected-tenant';
   ```

2. **Remove problematic theme:**
   ```sql
   UPDATE "Theme" 
   SET status = 'draft' 
   WHERE key = 'problematic-theme';
   ```

## Monitoring and Maintenance

### Metrics to Monitor

- Application uptime
- Response times
- Database query performance
- Theme load times
- Error rates
- Failed tenant resolutions

### Regular Maintenance

- **Weekly**: Review error logs
- **Monthly**: Database performance analysis
- **Quarterly**: Security updates
- **As needed**: Theme updates and additions

### Backup Strategy

- **Database**: Daily automated backups with 30-day retention
- **Application**: Version control (Git)
- **Environment**: Document all environment variables

## Support

For deployment issues:
- Review logs for specific error messages
- Check [Troubleshooting Guide](THEME_SYSTEM.md#troubleshooting)
- Verify all environment variables are set correctly
- Ensure database migrations completed successfully
