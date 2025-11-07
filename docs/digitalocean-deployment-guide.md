# DigitalOcean Deployment Guide

Complete step-by-step guide to deploy the Clinic Management Platform to DigitalOcean App Platform.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [GitHub Repository Setup](#github-repository-setup)
3. [Generate Security Keys](#generate-security-keys)
4. [Third-Party Service Setup](#third-party-service-setup)
5. [DigitalOcean Setup](#digitalocean-setup)
6. [Environment Variables Configuration](#environment-variables-configuration)
7. [Deploy Applications](#deploy-applications)
8. [Post-Deployment Verification](#post-deployment-verification)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before starting, ensure you have:

- [ ] DigitalOcean account (sign up at https://digitalocean.com)
- [ ] GitHub account with repository access
- [ ] OpenSSL installed (for generating keys)
- [ ] Git installed locally
- [ ] Access to register/configure:
  - SendGrid account (email service)
  - Cloudflare account (image hosting)
  - PayPal account (payment processing)

---

## GitHub Repository Setup

### Step 1: Create GitHub Repository

1. Go to https://github.com/new
2. Create a **private** repository (recommended for production code)
3. Name it (e.g., `clinic-management-system`)
4. Do NOT initialize with README (we'll push existing code)

### Step 2: Remove Sensitive Data from Code

**CRITICAL SECURITY STEP**: Remove hardcoded secrets from `apps/api/src/main/resources/application.yaml`

```bash
# Navigate to your project
cd /Users/mohammaddaabes/Documents/CMC

# Edit application.yaml and remove these hardcoded values:
# - security.email.sendgrid-api-key (line 63)
# - security.cloudflare.images.account-id (line 71)
# - security.cloudflare.images.api-token (line 72)
# - paypal.client-id (line 105)
# - paypal.client-secret (line 106)
```

Edit `apps/api/src/main/resources/application.yaml`:

```yaml
# Replace lines 63-78 with environment variable references:
  email:
    sendgrid-api-key: ${SENDGRID_API_KEY}
    from-email: ${EMAIL_FROM:clinic.notifier@gmail.com}
    from-name: ${EMAIL_FROM_NAME:Clinic}
    enabled: true
    google-meet-link: ${GOOGLE_MEET_LINK}
    clinic-address: ${CLINIC_ADDRESS}
  cloudflare:
    images:
      account-id: ${CLOUDFLARE_ACCOUNT_ID}
      api-token: ${CLOUDFLARE_API_TOKEN}
      base-url: https://api.cloudflare.com/client/v4
      delivery-url: ${CLOUDFLARE_DELIVERY_URL}
      max-file-size-mb: 10
      image-quality: 85
      auto-optimize: true
      allowed-extensions: jpg,jpeg,png,webp,gif

# Replace lines 104-109 with:
paypal:
  client-id: ${PAYPAL_CLIENT_ID}
  client-secret: ${PAYPAL_CLIENT_SECRET}
  webhook-id: ${PAYPAL_WEBHOOK_ID:}
  environment: ${PAYPAL_ENVIRONMENT:sandbox}
  base-url: ${PAYPAL_BASE_URL:https://api-m.sandbox.paypal.com}
```

### Step 3: Add .gitignore Entries

Ensure your `.gitignore` includes:

```bash
# Add to .gitignore if not already present
echo "
# Environment files
.env.local
.env.production
.env.cloudflare
*.pem
!apps/api/src/main/resources/*.example

# DigitalOcean
.do/deploy.log
" >> .gitignore
```

### Step 4: Push to GitHub

```bash
# Initialize git (if not already)
git init

# Add remote
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git

# Stage all files
git add .

# Commit
git commit -m "Initial commit - Clinic Management Platform"

# Push to main branch
git push -u origin main
```

---

## Generate Security Keys

### Step 1: Generate Patient JWT Keys

```bash
# Navigate to project root
cd /Users/mohammaddaabes/Documents/CMC

# Generate private key
openssl genrsa -out patient_private.pem 2048

# Generate public key
openssl rsa -in patient_private.pem -pubout -out patient_public.pem

# Display public key (copy this for later)
cat patient_public.pem
```

**Save the output** - you'll need it for environment variables. Format:
```
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...
...multiple lines...
-----END PUBLIC KEY-----
```

### Step 2: Generate Staff JWT Keys

```bash
# Generate private key
openssl genrsa -out staff_private.pem 2048

# Generate public key
openssl rsa -in staff_private.pem -pubout -out staff_public.pem

# Display public key (copy this for later)
cat staff_public.pem
```

### Step 3: Store Keys Securely

**DO NOT COMMIT .pem FILES TO GITHUB!**

```bash
# Verify .pem files are in .gitignore
git status

# You should NOT see any .pem files listed
```

**Store private keys securely**:
- Use a password manager (1Password, LastPass, Bitwarden)
- Keep offline backups in encrypted storage
- Share with team via secure channels only

---

## Third-Party Service Setup

### SendGrid (Email Service)

1. **Sign up**: https://signup.sendgrid.com/
2. **Verify email** and complete setup
3. **Create API Key**:
   - Go to Settings > API Keys
   - Click "Create API Key"
   - Name: `clinic-production`
   - Permissions: **Full Access** or **Mail Send** only
   - Copy the key (shown only once!)
4. **Configure sender**:
   - Go to Settings > Sender Authentication
   - Verify your domain or email address
   - Use verified email as `EMAIL_FROM`

**Save**: `SENDGRID_API_KEY=SG.xxxxxxxxxxxxxxxxxxxx`

### Cloudflare Images

1. **Sign up**: https://dash.cloudflare.com/sign-up
2. **Add Images**:
   - Go to Images in dashboard
   - Click "Get Started" (may require payment method)
3. **Get credentials**:
   - Account ID: In dashboard URL `dash.cloudflare.com/<ACCOUNT_ID>/images`
   - Delivery URL: Click "Preview" on any image, note the base URL format
4. **Create API Token**:
   - Go to My Profile > API Tokens
   - Create Token > "Create Custom Token"
   - Permissions: `Account > Cloudflare Images > Edit`
   - Click "Continue to summary" > "Create Token"
   - Copy token (shown only once!)

**Save**:
- `CLOUDFLARE_ACCOUNT_ID=c91a793d345e6332...`
- `CLOUDFLARE_API_TOKEN=xxxxxxxxxxxx`
- `CLOUDFLARE_DELIVERY_URL=https://imagedelivery.net/<YOUR_HASH>`

### PayPal (Payment Gateway)

#### For Testing (Sandbox):

1. **Sign up**: https://developer.paypal.com/
2. **Create App**:
   - Dashboard > My Apps & Credentials
   - Click "Create App"
   - App Name: `clinic-sandbox`
   - App Type: Merchant
3. **Get credentials** (Sandbox section):
   - Client ID: Copy from app details
   - Secret: Click "Show" and copy

**Save**:
- `PAYPAL_CLIENT_ID=AbJKTdpggZ_JdoLuIDgJ...`
- `PAYPAL_CLIENT_SECRET=EICjGMRjHqLcKKE5VT4iTkx...`
- `PAYPAL_ENVIRONMENT=sandbox`
- `PAYPAL_BASE_URL=https://api-m.sandbox.paypal.com`

#### For Production:

1. Switch to "Live" section in dashboard
2. Submit for review/approval (required by PayPal)
3. Once approved, get Live credentials
4. Update environment variables:
   - `PAYPAL_ENVIRONMENT=live`
   - `PAYPAL_BASE_URL=https://api-m.paypal.com`

---

## DigitalOcean Setup

### Step 1: Create DigitalOcean Account

1. Sign up at https://digitalocean.com
2. Add payment method
3. Complete verification

### Step 2: Connect GitHub

1. In DigitalOcean dashboard, go to **Apps**
2. Click **"Create App"**
3. Choose **GitHub**
4. Click **"Authorize DigitalOcean"**
5. Select your repository
6. Choose branch: `main`
7. **STOP** - don't click "Next" yet

### Step 3: Configure App from Spec

Instead of manual configuration, we'll use the App Spec file:

1. Click **"Edit Your App Spec"** (bottom of page)
2. Delete the auto-generated YAML
3. Open `.do/app.yaml` from your local project
4. **Update the following placeholders** in `.do/app.yaml`:

```yaml
# Line 8: Update repo path
github:
  repo: YOUR_GITHUB_USERNAME/YOUR_REPO_NAME  # e.g., johndoe/clinic-system

# Lines 47-48: Update API domain (you'll get this after first deploy)
- key: JWT_PATIENT_ISSUER
  value: "https://YOUR_API_DOMAIN"  # e.g., https://api-abc123.ondigitalocean.app

# Line 61: Update web domain
- key: PUBLIC_APP_ORIGIN
  value: "https://YOUR_WEB_DOMAIN"  # e.g., https://web-abc123.ondigitalocean.app

# Repeat for other YOUR_* placeholders
```

5. Copy the **entire updated** `.do/app.yaml` content
6. Paste into DigitalOcean App Spec editor
7. Click **"Save"**
8. Click **"Next"** > **"Review"**

### Step 4: Review Pricing

DigitalOcean will show estimated costs:

- **Database (Professional-xs)**: ~$12/month
- **API Service (Professional-xs)**: ~$12/month
- **Web Service (Basic-xs)**: ~$5/month
- **Admin Service (Basic-xs)**: ~$5/month

**Total**: ~$34/month (may vary by region)

### Step 5: Initial Deployment

1. Click **"Create Resources"**
2. DigitalOcean will start provisioning:
   - Database (takes ~5 minutes)
   - API Service (will **FAIL** initially - expected!)
   - Web Service (will **FAIL** - expected!)
   - Admin Service (will **FAIL** - expected!)

**Don't worry about failures** - we haven't set environment variables yet!

---

## Environment Variables Configuration

### Step 1: Get Auto-Generated URLs

After resources are created, note these URLs in DigitalOcean dashboard:

1. Go to your App > **Settings**
2. Note the URLs:
   - **API**: `https://api-<random>.ondigitalocean.app`
   - **Web**: `https://web-<random>.ondigitalocean.app`
   - **Admin**: `https://admin-<random>.ondigitalocean.app`

### Step 2: Format JWT Public Keys

Convert your public keys to single-line format:

```bash
# For patient key
cat patient_public.pem | sed 's/$/\\n/g' | tr -d '\n'

# For staff key
cat staff_public.pem | sed 's/$/\\n/g' | tr -d '\n'
```

Copy the output (it will look like):
```
-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG...\n-----END PUBLIC KEY-----\n
```

### Step 3: Configure API Environment Variables

1. In DigitalOcean, go to **Apps** > Your App
2. Click on **"api"** service
3. Click **"Settings"** tab
4. Scroll to **"Environment Variables"**
5. Click **"Edit"**

Add/update these variables (one at a time):

| Variable | Value | Encrypt? |
|----------|-------|----------|
| `JWT_PATIENT_ISSUER` | `https://api-<your-random>.ondigitalocean.app` | No |
| `JWT_PATIENT_AUDIENCE` | `patient` | No |
| `JWT_PATIENT_PUBLIC_KEY` | Paste formatted key from Step 2 | **YES** ✓ |
| `JWT_STAFF_ISSUER` | `https://api-<your-random>.ondigitalocean.app` | No |
| `JWT_STAFF_AUDIENCE` | `staff` | No |
| `JWT_STAFF_PUBLIC_KEY` | Paste formatted key from Step 2 | **YES** ✓ |
| `PUBLIC_APP_ORIGIN` | `https://web-<your-random>.ondigitalocean.app` | No |
| `ADMIN_APP_ORIGIN` | `https://admin-<your-random>.ondigitalocean.app` | No |
| `SENDGRID_API_KEY` | From SendGrid setup | **YES** ✓ |
| `EMAIL_FROM` | `clinic.notifier@gmail.com` | No |
| `EMAIL_FROM_NAME` | `Your Clinic Name` | No |
| `CLOUDFLARE_ACCOUNT_ID` | From Cloudflare setup | **YES** ✓ |
| `CLOUDFLARE_API_TOKEN` | From Cloudflare setup | **YES** ✓ |
| `CLOUDFLARE_DELIVERY_URL` | From Cloudflare setup | No |
| `PAYPAL_CLIENT_ID` | From PayPal setup | **YES** ✓ |
| `PAYPAL_CLIENT_SECRET` | From PayPal setup | **YES** ✓ |
| `PAYPAL_ENVIRONMENT` | `sandbox` | No |
| `PAYPAL_BASE_URL` | `https://api-m.sandbox.paypal.com` | No |

6. Click **"Save"**

### Step 4: Configure Web App Variables

1. Click on **"web"** service
2. Click **"Settings"** > **"Environment Variables"** > **"Edit"**

| Variable | Value |
|----------|-------|
| `NEXT_PUBLIC_API_URL` | `https://api-<your-random>.ondigitalocean.app/api/public` |
| `NODE_ENV` | `production` |

3. Click **"Save"**

### Step 5: Configure Admin App Variables

1. Click on **"admin"** service
2. Click **"Settings"** > **"Environment Variables"** > **"Edit"**

| Variable | Value |
|----------|-------|
| `NUXT_PUBLIC_API_BASE` | `https://api-<your-random>.ondigitalocean.app/api/admin` |
| `NUXT_PUBLIC_PUBLIC_API_BASE` | `https://api-<your-random>.ondigitalocean.app/api` |
| `NODE_ENV` | `production` |

3. Click **"Save"**

---

## Deploy Applications

### Step 1: Trigger Deployment

After saving all environment variables:

1. Go to your App overview
2. Click **"Actions"** > **"Force Rebuild and Deploy"**
3. Wait for deployment (~10-15 minutes)

Watch the logs:
- **Database**: Should show "Running"
- **API**: Build > Deploy > Running (check logs for errors)
- **Web**: Build > Deploy > Running
- **Admin**: Build > Deploy > Running

### Step 2: Monitor Build Logs

If any service fails:

1. Click on the failed service
2. Click **"Runtime Logs"** or **"Build Logs"**
3. Check for errors (common issues below)

### Step 3: Verify Database Migrations

API logs should show Flyway migrations:

```
INFO  FlywayExecutor : Flyway Community Edition 9.x.x
INFO  FlywayExecutor : Database: jdbc:postgresql://...
INFO  FlywayExecutor : Successfully validated 5 migrations
INFO  FlywayExecutor : Current version of schema "public": 5
INFO  FlywayExecutor : Schema "public" is up to date. No migration necessary.
```

---

## Post-Deployment Verification

### Step 1: Check API Health

```bash
# Replace with your API URL
curl https://api-<your-random>.ondigitalocean.app/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### Step 2: Test Patient Web App

1. Open `https://web-<your-random>.ondigitalocean.app` in browser
2. Verify homepage loads
3. Try browsing services/doctors
4. Test booking flow (if available without login)

### Step 3: Test Admin Dashboard

1. Open `https://admin-<your-random>.ondigitalocean.app` in browser
2. You should see login page
3. Try logging in (you'll need to seed staff users first)

### Step 4: Seed Initial Staff User

You need to create the first admin user directly in the database:

1. In DigitalOcean, go to **Databases** > **clinic-db**
2. Click **"Console"** or use connection details with a PostgreSQL client
3. Run this SQL (replace password hash):

```sql
-- First, generate a BCrypt hash of your desired password
-- Use: https://bcrypt-generator.com/ with rounds=10

INSERT INTO staff (
  id, email, password_hash, first_name, last_name,
  role, is_active, created_at, updated_at
) VALUES (
  gen_random_uuid(),
  'admin@clinic.com',
  '$2a$10$YOUR_BCRYPT_HASH_HERE',  -- Replace with actual hash
  'System',
  'Administrator',
  'ADMIN',
  true,
  NOW(),
  NOW()
);
```

4. Now you can log into admin dashboard with:
   - Email: `admin@clinic.com`
   - Password: `<whatever you hashed>`

### Step 5: Test End-to-End Flow

1. **Admin side**:
   - Log into admin dashboard
   - Create a doctor
   - Create a service
   - Set doctor availability

2. **Patient side**:
   - Open patient web app
   - Sign up as patient
   - Browse doctors/services
   - Book an appointment
   - Check email for confirmation

---

## Custom Domain Setup (Optional)

### Step 1: Add Domain to DigitalOcean

1. In DigitalOcean, go to **Networking** > **Domains**
2. Add your domain (e.g., `clinic.com`)
3. Update DNS at your registrar:
   - Point nameservers to DigitalOcean:
     - `ns1.digitalocean.com`
     - `ns2.digitalocean.com`
     - `ns3.digitalocean.com`

### Step 2: Configure App Domains

1. Go to **Apps** > Your App > **Settings**
2. For each service (api, web, admin):
   - Click **"Domains"**
   - Click **"Add Domain"**
   - Enter subdomain:
     - API: `api.clinic.com`
     - Web: `www.clinic.com` or `clinic.com`
     - Admin: `admin.clinic.com`
   - Click **"Add Domain"**
3. DigitalOcean will auto-configure SSL (Let's Encrypt)

### Step 3: Update Environment Variables

After domains are active:

1. Update `JWT_PATIENT_ISSUER` → `https://api.clinic.com`
2. Update `JWT_STAFF_ISSUER` → `https://api.clinic.com`
3. Update `PUBLIC_APP_ORIGIN` → `https://clinic.com`
4. Update `ADMIN_APP_ORIGIN` → `https://admin.clinic.com`
5. Update frontend API URLs accordingly
6. Save and redeploy

---

## Troubleshooting

### API Service Won't Start

**Symptom**: API shows "Deployment failed" or crashes on startup

**Check**:
1. **Database connection**:
   ```
   # In API logs, look for:
   ERROR: Connection to ... refused
   ```
   - Verify database is "Running"
   - Check `SPRING_DATASOURCE_*` variables are set

2. **JWT key format**:
   ```
   ERROR: Invalid key format
   ```
   - Ensure public keys have `\n` line breaks
   - Check BEGIN/END PUBLIC KEY lines are included

3. **Missing environment variables**:
   ```
   ERROR: Required variable ... not found
   ```
   - Review all API environment variables
   - Ensure no `YOUR_*` placeholders remain

### Frontend Apps Show 404 or Build Errors

**Symptom**: `pnpm: command not found` or module errors

**Solution**:
1. Add build environment in DigitalOcean:
   - Service Settings > "Environment Variables"
   - Add `PNPM_VERSION` = `8.15.0`
2. Update build command to install pnpm:
   ```bash
   npm install -g pnpm@8.15.0 && cd apps/web-next && pnpm install && pnpm build
   ```

### CORS Errors in Browser

**Symptom**: Console shows "CORS policy" errors

**Solution**:
1. Verify `PUBLIC_APP_ORIGIN` / `ADMIN_APP_ORIGIN` match exact URLs (including https://)
2. No trailing slashes
3. Restart API service after changes

### PayPal Payments Failing

**Symptom**: Payment button doesn't work or shows errors

**Check**:
1. Sandbox vs Live environment matches credentials
2. `PAYPAL_BASE_URL` correct for environment
3. In PayPal developer dashboard, verify app status is "Active"

### Email Not Sending

**Symptom**: No confirmation emails received

**Check**:
1. `SENDGRID_API_KEY` is correct and not expired
2. In SendGrid, check Activity > API Key usage
3. Verify sender email is verified in SendGrid
4. Check API logs for email-related errors

### Images Not Uploading

**Symptom**: Profile pictures fail to upload

**Check**:
1. `CLOUDFLARE_API_TOKEN` has Images edit permission
2. `CLOUDFLARE_DELIVERY_URL` format is correct
3. Image file size under 10MB
4. File extension allowed (jpg, png, webp, gif)

---

## Maintenance & Updates

### Deploying Updates

1. Push changes to GitHub:
   ```bash
   git add .
   git commit -m "Description of changes"
   git push origin main
   ```
2. DigitalOcean auto-deploys on push to `main`
3. Monitor deployment in Apps dashboard

### Manual Deployment

If auto-deploy disabled:
1. Go to Apps > Your App
2. Click **"Create Deployment"**
3. Select branch and click **"Deploy"**

### Rolling Back

1. Go to Apps > Your App > **"Deployments"**
2. Find previous successful deployment
3. Click **"⋮"** > **"Rollback to this deployment"**
4. Confirm

### Viewing Logs

- **Runtime logs**: Apps > Service > "Runtime Logs"
- **Build logs**: Apps > Service > "Build Logs"
- **Database logs**: Databases > Your DB > "Logs"

### Scaling

To handle more traffic:

1. Go to Apps > Your App > Service
2. Click **"Settings"** > **"Resources"**
3. Adjust:
   - **Instance size**: Basic → Professional → Premium
   - **Instance count**: 1 → 2+ (for redundancy)
4. Click **"Save"**

---

## Security Checklist

- [ ] All `.pem` files excluded from git
- [ ] No hardcoded secrets in `application.yaml`
- [ ] All sensitive env vars marked as "Encrypted" in DigitalOcean
- [ ] Database connection uses SSL (default in DigitalOcean)
- [ ] JWT issuer URLs match actual API domain
- [ ] CORS origins match actual frontend domains
- [ ] SendGrid API key has minimum required permissions
- [ ] Cloudflare API token has minimum required permissions
- [ ] PayPal using sandbox initially (switch to live when ready)
- [ ] Admin IP allowlist configured (if needed)
- [ ] Rate limiting enabled
- [ ] SSL/TLS enabled for all services (auto by DigitalOcean)

---

## Cost Optimization Tips

1. **Start small**: Use Basic instances initially, scale up if needed
2. **Database backups**: DigitalOcean includes daily backups, no extra config needed
3. **Monitor usage**: Set up billing alerts in DigitalOcean
4. **CDN**: Cloudflare Images includes CDN - no separate CDN needed
5. **Staging environment**: Create separate app for staging with smaller resources

---

## Support & Resources

- **DigitalOcean Docs**: https://docs.digitalocean.com/products/app-platform/
- **DigitalOcean Community**: https://www.digitalocean.com/community
- **SendGrid Support**: https://support.sendgrid.com/
- **Cloudflare Support**: https://support.cloudflare.com/
- **PayPal Developer**: https://developer.paypal.com/support/

---

## Next Steps

After successful deployment:

1. **Monitor** application health and logs for 24-48 hours
2. **Test** all critical user flows
3. **Set up** monitoring/alerts (DigitalOcean Monitoring is free)
4. **Document** your specific configuration for team members
5. **Schedule** regular backups (database auto-backed up daily)
6. **Plan** for production launch:
   - Switch PayPal to live
   - Update CORS for final domain
   - Set up custom domains
   - Configure additional staff users
