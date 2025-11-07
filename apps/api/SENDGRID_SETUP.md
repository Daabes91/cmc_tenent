# SendGrid Email Integration Setup

This guide explains how to set up SendGrid for sending appointment confirmation and cancellation emails.

## Step 1: Create a SendGrid Account

1. Go to [https://signup.sendgrid.com/](https://signup.sendgrid.com/)
2. Sign up for a free account (100 emails/day free forever)
3. Verify your email address

## Step 2: Create an API Key

1. Log in to SendGrid dashboard
2. Go to **Settings** → **API Keys**
3. Click **Create API Key**
4. Name it: `Clinic API - Production` (or whatever you prefer)
5. Select **Full Access** permissions
6. Click **Create & View**
7. **IMPORTANT**: Copy the API key immediately (you won't be able to see it again!)

## Step 3: Verify Sender Identity

Before you can send emails, you need to verify your sender identity:

### Option A: Single Sender Verification (Easiest for development)
1. Go to **Settings** → **Sender Authentication**
2. Click **Verify a Single Sender**
3. Fill in your details:
   - From Name: `Qadri's Clinic` (or your clinic name)
   - From Email Address: `noreply@yourdomain.com`
   - Reply To: `info@yourdomain.com`
4. Check your email and click the verification link

### Option B: Domain Authentication (Recommended for production)
1. Go to **Settings** → **Sender Authentication**
2. Click **Authenticate Your Domain**
3. Follow the DNS setup instructions for your domain

## Step 4: Configure Environment Variables

Add these variables to your `.env.local` file in `apps/api/`:

```bash
# SendGrid Configuration
SENDGRID_API_KEY=SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
EMAIL_FROM=noreply@yourdomain.com
EMAIL_FROM_NAME=Qadri's Clinic
EMAIL_ENABLED=true
```

## Step 5: Test the Integration

1. Restart your API server
2. Create a test appointment booking
3. Check if the confirmation email was sent
4. Check SendGrid dashboard for email activity

## Email Templates

The integration includes two email templates:

### 1. Appointment Confirmation Email
Sent when:
- Patient books an appointment (authenticated)
- Guest books an appointment (with email provided)

Includes:
- Doctor name
- Service name
- Date and time
- Consultation type (clinic visit or virtual)
- Important reminders

### 2. Appointment Cancellation Email
Sent when:
- Appointment is cancelled by admin/staff

Includes:
- Doctor name
- Service name
- Original date and time

## Configuration Options

All email settings can be configured in `application.yaml`:

```yaml
security:
  email:
    sendgrid-api-key: ${SENDGRID_API_KEY:}
    from-email: ${EMAIL_FROM:noreply@clinic.com}
    from-name: ${EMAIL_FROM_NAME:Qadri's Clinic}
    enabled: ${EMAIL_ENABLED:false}
```

## Disabling Emails

To disable email sending (useful for development):

```bash
EMAIL_ENABLED=false
```

Emails will be logged to console but not actually sent.

## Troubleshooting

### Emails not being sent?

1. **Check API key**: Make sure `SENDGRID_API_KEY` is set correctly
2. **Check enabled flag**: Ensure `EMAIL_ENABLED=true`
3. **Check sender verification**: Verify your sender email in SendGrid dashboard
4. **Check logs**: Look for error messages in API logs
5. **Check SendGrid activity**: Go to SendGrid dashboard → Activity to see email status

### Common errors:

- **403 Forbidden**: Sender email not verified
- **401 Unauthorized**: Invalid API key
- **Empty response**: `EMAIL_ENABLED=false` or missing API key

## Production Checklist

Before deploying to production:

- [ ] Create production SendGrid API key
- [ ] Verify domain authentication (not just single sender)
- [ ] Set up proper from-email with your domain
- [ ] Test email delivery
- [ ] Set up email suppression lists
- [ ] Configure webhook for bounce/spam handling
- [ ] Enable email analytics in SendGrid

## Monitoring

Monitor email delivery in SendGrid dashboard:
- **Activity Feed**: See all sent emails
- **Stats**: Track delivery rates, opens, clicks
- **Suppressions**: Manage bounces and unsubscribes

## Rate Limits

Free tier limits:
- 100 emails/day
- 40,000 emails first 30 days

If you need more, upgrade to a paid plan.

## Support

- SendGrid Documentation: https://docs.sendgrid.com/
- SendGrid Support: https://support.sendgrid.com/
