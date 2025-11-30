# Backend API

Spring Boot service exposing:
- `/public/**` for patient-facing endpoints.
- `/admin/**` for staff-only endpoints (RBAC enforced).
- `/saas/**` for SAAS manager endpoints.

## Environment Variables

The API requires several environment variables to be configured. Copy `.env.example` to `.env.local` and update the values as needed.

### Database Configuration

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5442/clinic_multi_tenant
SPRING_DATASOURCE_USERNAME=clinic
SPRING_DATASOURCE_PASSWORD=clinic_password
```

### JWT Configuration

```bash
JWT_PATIENT_ISSUER=http://localhost:8080
JWT_PATIENT_AUDIENCE=patient
JWT_PATIENT_PUBLIC_KEY=classpath:keys/patient_public.pem
JWT_PATIENT_PRIVATE_KEY=classpath:keys/patient_private.pem

JWT_STAFF_ISSUER=http://localhost:8080
JWT_STAFF_AUDIENCE=staff
JWT_STAFF_PUBLIC_KEY=classpath:keys/staff_public.pem
JWT_STAFF_PRIVATE_KEY=classpath:keys/staff_private.pem
```

### Google OAuth Configuration

```bash
# Google OAuth 2.0 credentials from Google Cloud Console
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret

# OAuth callback URL (must match Google Console configuration)
GOOGLE_REDIRECT_URI=http://localhost:3001/auth/google/callback

# OAuth state parameter settings
OAUTH_STATE_EXPIRATION_MINUTES=5
OAUTH_STATE_LENGTH=32

# Feature flag to enable/disable Google OAuth
ENABLE_GOOGLE_OAUTH=true
```

For complete Google OAuth setup instructions, see [Google OAuth Configuration Guide](../../.kiro/specs/patient-google-oauth/GOOGLE_OAUTH_CONFIGURATION.md).

### PayPal Configuration

```bash
PAYPAL_CLIENT_ID=your-paypal-client-id
PAYPAL_CLIENT_SECRET=your-paypal-client-secret
PAYPAL_PLAN_ID=your-paypal-plan-id
PAYPAL_WEBHOOK_ID=your-paypal-webhook-id
PAYPAL_ENVIRONMENT=sandbox
PAYPAL_ENCRYPTION_KEY=your-32-byte-encryption-key
PAYPAL_WEBHOOK_URL=http://localhost:8080/api/webhooks/paypal
```

### Cloudflare Images Configuration

```bash
CLOUDFLARE_ACCOUNT_ID=your-cloudflare-account-id
CLOUDFLARE_API_TOKEN=your-cloudflare-api-token
CLOUDFLARE_IMAGES_BASE_URL=https://api.cloudflare.com/client/v4
CLOUDFLARE_IMAGES_DELIVERY_URL=https://imagedelivery.net
CLOUDFLARE_MAX_FILE_SIZE_MB=10
```

### CORS Configuration

```bash
PUBLIC_APP_ORIGIN=http://localhost:3001
ADMIN_APP_ORIGIN=http://localhost:3000
SAAS_ADMIN_APP_ORIGIN=http://localhost:3002
```

## Running the API

### Local Development

```bash
# Start the database
./start-local.sh

# Run the API
./gradlew bootRun
```

The API will be available at `http://localhost:8080`.

### API Documentation

Once running, access the Swagger UI at:
- http://localhost:8080/swagger-ui.html

## Phase Coverage
- Phase 1: directory placeholder and Gradle/Maven decision.
- Phase 2: introduce namespace split, security config, and Flyway baseline.
