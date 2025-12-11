# Plan Limit Validation Implementation

## Overview

Implemented comprehensive subscription plan limit validation to enforce patient, staff, and doctor limits based on the tenant's subscription plan tier.

## What Was Implemented

### 1. Exception Handling
**File**: `PlanLimitExceededException.java`
- Custom exception thrown when plan limits are exceeded
- Returns HTTP 402 Payment Required
- Includes limit type, current count, and maximum allowed

### 2. Validation Service
**File**: `PlanLimitValidationService.java`
- Core service for validating subscription limits
- Methods:
  - `validatePatientLimit(tenantId)` - Validates before creating patients
  - `validateStaffLimit(tenantId)` - Validates before creating staff (TODO)
  - `validateDoctorLimit(tenantId)` - Validates before creating doctors (TODO)
  - `getUsageStats(tenantId)` - Returns current usage statistics
- Caches tenant plan tier for 5 minutes to reduce database queries
- Handles unlimited plans (-1 value)
- Backwards compatible (allows operations if no subscription exists)

### 3. Integration with PatientAdminService
**File**: `PatientAdminService.java`
- Added `PlanLimitValidationService` dependency
- Calls `validatePatientLimit()` before creating new patients
- Throws `PlanLimitExceededException` if limit is reached

### 4. API Endpoint for Usage Stats
**File**: `PlanUsageController.java`
- Endpoint: `GET /api/admin/plan-usage`
- Returns current usage statistics for the authenticated tenant
- Response includes:
  - Current and max patients, staff, doctors
  - Plan tier name
  - Boolean flags for limit reached status

### 5. Global Exception Handler
**File**: `GlobalExceptionHandler.java`
- Added handler for `PlanLimitExceededException`
- Returns HTTP 402 with detailed error information
- Includes limit type, current count, and max allowed in response

### 6. Comprehensive Tests
**File**: `PlanLimitValidationServiceTest.java`
- Tests for under limit scenarios
- Tests for limit exceeded scenarios
- Tests for unlimited plans (Enterprise)
- Tests for backwards compatibility (no subscription)
- Tests for usage statistics

## Plan Limits Configuration

Current limits from `PlanTierConfig.java`:

| Plan | Max Patients | Max Staff | Max Doctors | Price/Month |
|------|--------------|-----------|-------------|-------------|
| BASIC | 100 | 2 | 2 | $29.99 |
| PROFESSIONAL | 500 | 10 | 10 | $79.99 |
| ENTERPRISE | Unlimited | Unlimited | Unlimited | $199.99 |
| CUSTOM | Unlimited | Unlimited | Unlimited | Negotiated |

## API Usage

### Check Usage Stats
```bash
GET /api/admin/plan-usage
Authorization: Bearer {token}

Response:
{
  "currentPatients": 75,
  "maxPatients": 100,
  "currentStaff": 1,
  "maxStaff": 2,
  "currentDoctors": 1,
  "maxDoctors": 2,
  "planTier": "BASIC",
  "patientLimitReached": false,
  "staffLimitReached": false,
  "doctorLimitReached": false
}
```

### Create Patient (with validation)
```bash
POST /api/admin/patients
Authorization: Bearer {token}

# If limit exceeded, returns:
HTTP 402 Payment Required
{
  "success": false,
  "errorCode": "PLAN_LIMIT_EXCEEDED",
  "message": "Plan limit exceeded: patients. Current: 100, Maximum allowed: 100. Please upgrade your plan.",
  "errors": [
    { "field": "limitType", "message": "patients" },
    { "field": "currentCount", "message": "100" },
    { "field": "maxAllowed", "message": "100" }
  ]
}
```

## Frontend Integration

### Display Usage in UI
```typescript
// Fetch usage stats
const response = await fetch('/api/admin/plan-usage', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const usage = await response.json();

// Show progress bar
const percentage = (usage.currentPatients / usage.maxPatients) * 100;

// Show warning if near limit
if (percentage >= 90) {
  showWarning('You are approaching your patient limit. Consider upgrading your plan.');
}

// Disable "Add Patient" button if limit reached
if (usage.patientLimitReached) {
  disableAddPatientButton();
  showUpgradePrompt();
}
```

### Handle Limit Exceeded Error
```typescript
try {
  await createPatient(patientData);
} catch (error) {
  if (error.status === 402 && error.errorCode === 'PLAN_LIMIT_EXCEEDED') {
    showUpgradeModal({
      message: error.message,
      currentPlan: usage.planTier,
      suggestedPlan: getNextTier(usage.planTier)
    });
  }
}
```

## TODO: Future Enhancements

1. **Staff Limit Validation**
   - Implement `StaffRepository.countByTenantId()`
   - Add validation in staff creation service
   - Update usage stats endpoint

2. **Doctor Limit Validation**
   - Implement `DoctorRepository.countByTenantId()`
   - Add validation in doctor creation service
   - Update usage stats endpoint

3. **Soft Limits with Grace Period**
   - Allow 10% overage with warning
   - Send email notifications when approaching limit
   - Implement grace period before hard enforcement

4. **Usage Analytics**
   - Track limit exceeded attempts
   - Monitor upgrade conversion rates
   - Alert when tenants consistently hit limits

5. **Bulk Operations**
   - Validate limits before bulk patient imports
   - Show preview of how many can be imported
   - Suggest plan upgrade if needed

## Testing

Run the tests:
```bash
cd apps/api
./gradlew test --tests PlanLimitValidationServiceTest
```

## Deployment Checklist

- [x] Exception class created
- [x] Validation service implemented
- [x] Integrated with PatientAdminService
- [x] API endpoint for usage stats
- [x] Global exception handler updated
- [x] Unit tests written
- [ ] Integration tests with real database
- [ ] Frontend UI for displaying usage
- [ ] Frontend error handling for limit exceeded
- [ ] Documentation for support team
- [ ] Monitoring/alerting for limit exceeded events

## Monitoring

Add these metrics to your monitoring system:

- `plan_limit_exceeded_total{limit_type="patients"}` - Counter of limit exceeded events
- `plan_usage_percentage{limit_type="patients"}` - Gauge of current usage percentage
- `plan_upgrade_conversions_total` - Counter of upgrades triggered by limits

## Support Team Guide

When a customer reports they can't add patients:

1. Check their current plan and usage:
   ```sql
   SELECT t.name, s.plan_tier, COUNT(p.id) as patient_count
   FROM tenants t
   JOIN subscriptions s ON s.tenant_id = t.id
   LEFT JOIN patients p ON p.tenant_id = t.id
   WHERE t.id = ?
   GROUP BY t.id;
   ```

2. Compare with plan limits (see table above)

3. Options:
   - Upgrade their plan
   - Clean up inactive/duplicate patients
   - Apply temporary override (if authorized)

## Security Considerations

- Validation happens server-side (cannot be bypassed)
- Cached plan tier is invalidated on subscription changes
- Backwards compatible (no subscription = no limits)
- Audit log for limit exceeded attempts (future enhancement)

