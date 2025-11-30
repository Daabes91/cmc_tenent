# Pricing Section Healthcare Update - Manual Test Guide

## Overview
This guide helps verify that the pricing section has been successfully updated with healthcare-specific content.

## Test Checklist

### ✅ Visual Verification

1. **Navigate to Landing Page**
   - Open the landing page in a browser
   - Scroll to the pricing section (or navigate to `/#pricing`)

2. **Verify Pricing Tiers**
   - [ ] Three pricing tiers are displayed: "Solo Practice", "Small Clinic", "Multi-Location Practice"
   - [ ] Each tier shows healthcare-specific descriptions
   - [ ] "Small Clinic" tier has a "Most Popular" badge

3. **Verify Pricing Display**
   - [ ] Monthly prices are displayed prominently
   - [ ] Annual prices are shown below monthly prices with savings indicator
   - [ ] Prices match the healthcare content configuration:
     - Solo Practice: $49/month, $470/year
     - Small Clinic: $149/month, $1430/year
     - Multi-Location: $399/month, $3830/year

4. **Verify Healthcare-Specific Limits**
   - [ ] Each tier displays provider, patient, and appointment limits
   - [ ] Solo Practice shows: 1 provider, 100 patients, 500 appointments
   - [ ] Small Clinic shows: 5 providers, 500 patients, 2000 appointments
   - [ ] Multi-Location shows: "Unlimited" for all limits

5. **Verify Healthcare Features**
   - [ ] Features list includes healthcare-specific terminology:
     - "provider account" (not "user account")
     - "patients" (not "users")
     - "appointments" (not "bookings")
   - [ ] Features mention healthcare-specific capabilities:
     - Patient portal
     - Video consultations
     - Insurance billing
     - HIPAA compliance documentation

6. **Verify Comparison Table**
   - [ ] Comparison table is displayed below pricing cards
   - [ ] Table shows all three tiers side-by-side
   - [ ] Rows include:
     - Providers limit
     - Patients limit
     - Appointments limit
     - Key features with checkmarks
   - [ ] Checkmarks appear for features included in each tier

7. **Verify CTA Buttons**
   - [ ] Solo Practice and Small Clinic have "Start Free Trial" buttons
   - [ ] Multi-Location has "Contact Sales" button
   - [ ] Buttons link to appropriate destinations

### ✅ Bilingual Support (Arabic)

8. **Switch to Arabic Language**
   - [ ] Change language to Arabic
   - [ ] Verify pricing section title and subtitle are in Arabic
   - [ ] Verify tier names are translated
   - [ ] Verify features are in Arabic
   - [ ] Verify comparison table labels are in Arabic
   - [ ] Verify limit labels (providers, patients, appointments) are in Arabic

### ✅ Responsive Design

9. **Test Mobile View**
   - [ ] Resize browser to mobile width (375px)
   - [ ] Pricing cards stack vertically
   - [ ] All content remains readable
   - [ ] Comparison table scrolls horizontally if needed
   - [ ] CTA buttons are touch-friendly (minimum 44px height)

### ✅ Content Validation

10. **Verify Healthcare Terminology**
    - [ ] No generic SaaS terms like "workspace", "projects", "users"
    - [ ] Consistent use of healthcare terms throughout
    - [ ] Professional medical/clinic language

## Expected Results

### Pricing Cards
- Three cards displayed in a grid
- Healthcare-focused tier names and descriptions
- Both monthly and annual pricing visible
- Healthcare-specific limits prominently displayed
- Features list with healthcare terminology
- Appropriate CTAs for each tier

### Comparison Table
- Clean, readable table layout
- All three tiers compared side-by-side
- Limits and features clearly indicated
- Checkmarks for included features
- Responsive on mobile devices

## Test Data Reference

### Solo Practice Tier
- **Name**: Solo Practice
- **Monthly Price**: $49
- **Annual Price**: $470
- **Limits**: 1 provider, 100 patients, 500 appointments/month
- **Key Features**: 
  - 1 provider account
  - Up to 100 patients
  - 500 appointments/month
  - Basic reporting
  - Email support
  - Custom domain
  - Patient portal

### Small Clinic Tier (Popular)
- **Name**: Small Clinic
- **Monthly Price**: $149
- **Annual Price**: $1430
- **Limits**: 5 providers, 500 patients, 2000 appointments/month
- **Key Features**:
  - Up to 5 providers
  - Up to 500 patients
  - 2000 appointments/month
  - Advanced reporting
  - Priority support
  - Custom branding
  - Video consultations
  - Insurance billing

### Multi-Location Practice Tier
- **Name**: Multi-Location Practice
- **Monthly Price**: $399
- **Annual Price**: $3830
- **Limits**: Unlimited providers, patients, appointments
- **Key Features**:
  - Unlimited providers
  - Unlimited patients
  - Unlimited appointments
  - Custom integrations
  - Dedicated account manager
  - White-label options
  - API access
  - Advanced analytics
  - HIPAA compliance documentation

## Notes
- The pricing section now uses the healthcare content configuration from `lib/content/healthcare-copy.ts`
- All content is bilingual (English and Arabic)
- The comparison table provides an easy way to compare features across tiers
- Healthcare-specific limits are prominently displayed to help clinics choose the right tier
