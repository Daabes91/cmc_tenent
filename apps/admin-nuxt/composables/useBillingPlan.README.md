# useBillingPlan Composable

## Overview

The `useBillingPlan` composable provides a comprehensive interface for managing tenant subscription plans. It handles fetching plan details, upgrading plans, cancelling subscriptions, and updating payment methods with built-in caching, error handling, and toast notifications.

## Features

- ✅ Fetch current plan details with 5-minute caching
- ✅ Upgrade plan with PayPal redirect
- ✅ Cancel subscription (immediate or scheduled)
- ✅ Update payment method via PayPal portal
- ✅ Automatic error handling with toast notifications
- ✅ Loading state management
- ✅ Computed properties for UI state
- ✅ Shared state across component instances

## Usage

### Basic Setup

```vue
<script setup lang="ts">
const {
  currentPlan,
  isLoading,
  hasPendingChange,
  pendingChangeDescription,
  isActive,
  statusColor,
  fetchPlanDetails,
  upgradePlan,
  cancelPlan,
  updatePaymentMethod
} = useBillingPlan();

// Fetch plan details on mount
onMounted(async () => {
  await fetchPlanDetails();
});
</script>

<template>
  <div v-if="isLoading">Loading...</div>
  <div v-else-if="currentPlan">
    <h2>{{ currentPlan.planTierName }}</h2>
    <p>{{ currentPlan.price }} {{ currentPlan.currency }} / {{ currentPlan.billingCycle }}</p>
    <UBadge :color="statusColor">{{ currentPlan.status }}</UBadge>
  </div>
</template>
```

### Upgrade Plan

```vue
<script setup lang="ts">
const { upgradePlan, isLoading } = useBillingPlan();

async function handleUpgrade() {
  // This will redirect to PayPal
  await upgradePlan('PROFESSIONAL');
}
</script>

<template>
  <UButton 
    @click="handleUpgrade" 
    :loading="isLoading"
  >
    Upgrade to Professional
  </UButton>
</template>
```

### Cancel Subscription

```vue
<script setup lang="ts">
const { cancelPlan, isLoading } = useBillingPlan();

async function handleCancel() {
  const confirmed = confirm('Are you sure you want to cancel your subscription?');
  if (confirmed) {
    // Cancel at end of billing period
    await cancelPlan(false, 'User requested cancellation');
  }
}

async function handleImmediateCancel() {
  const confirmed = confirm('Cancel immediately? You will lose access right away.');
  if (confirmed) {
    // Cancel immediately
    await cancelPlan(true, 'Immediate cancellation requested');
  }
}
</script>

<template>
  <div>
    <UButton 
      @click="handleCancel" 
      :loading="isLoading"
      color="gray"
    >
      Cancel at End of Period
    </UButton>
    
    <UButton 
      @click="handleImmediateCancel" 
      :loading="isLoading"
      color="red"
    >
      Cancel Immediately
    </UButton>
  </div>
</template>
```

### Update Payment Method

```vue
<script setup lang="ts">
const { updatePaymentMethod, isLoading, currentPlan } = useBillingPlan();

async function handleUpdatePayment() {
  // This will redirect to PayPal billing portal
  await updatePaymentMethod();
}
</script>

<template>
  <div>
    <p>Current payment method: {{ currentPlan?.paymentMethodMask }}</p>
    <UButton 
      @click="handleUpdatePayment" 
      :loading="isLoading"
    >
      Update Payment Method
    </UButton>
  </div>
</template>
```

### Display Pending Changes

```vue
<script setup lang="ts">
const { 
  hasPendingChange, 
  pendingChangeDescription,
  currentPlan 
} = useBillingPlan();
</script>

<template>
  <UAlert 
    v-if="hasPendingChange"
    color="yellow"
    icon="i-heroicons-information-circle"
  >
    <template #title>Pending Change</template>
    <template #description>
      {{ pendingChangeDescription }}
    </template>
  </UAlert>
</template>
```

## API Reference

### State

#### `currentPlan`
- **Type:** `Readonly<Ref<TenantPlan | null>>`
- **Description:** The current subscription plan details
- **Properties:**
  - `tenantId`: Tenant identifier
  - `planTier`: Plan tier enum value (e.g., 'BASIC', 'PROFESSIONAL')
  - `planTierName`: Human-readable plan name
  - `price`: Plan price
  - `currency`: Currency code (e.g., 'USD')
  - `billingCycle`: Billing cycle (e.g., 'MONTHLY', 'ANNUAL')
  - `renewalDate`: Next renewal date
  - `paymentMethodMask`: Masked payment method (e.g., 'Visa ****1234')
  - `paymentMethodType`: Payment method type
  - `status`: Subscription status
  - `cancellationDate`: Date cancellation was requested
  - `cancellationEffectiveDate`: Date cancellation takes effect
  - `pendingPlanTier`: Pending plan tier for scheduled downgrades
  - `pendingPlanEffectiveDate`: Date pending plan change takes effect
  - `features`: List of included features
  - `paypalSubscriptionId`: PayPal subscription ID

#### `isLoading`
- **Type:** `Readonly<Ref<boolean>>`
- **Description:** Loading state for async operations

#### `error`
- **Type:** `Readonly<Ref<Error | null>>`
- **Description:** Last error that occurred

#### `isCacheValid`
- **Type:** `ComputedRef<boolean>`
- **Description:** Whether cached plan data is still valid (< 5 minutes old)

### Computed Properties

#### `hasPendingChange`
- **Type:** `ComputedRef<boolean>`
- **Description:** Whether there's a pending plan change or cancellation

#### `pendingChangeDescription`
- **Type:** `ComputedRef<string | null>`
- **Description:** Human-readable description of pending change

#### `isActive`
- **Type:** `ComputedRef<boolean>`
- **Description:** Whether the subscription is currently active

#### `isCancelled`
- **Type:** `ComputedRef<boolean>`
- **Description:** Whether the subscription is cancelled

#### `statusColor`
- **Type:** `ComputedRef<string>`
- **Description:** UI color for status badge ('green', 'orange', 'red', 'yellow', 'gray')

### Methods

#### `fetchPlanDetails(force?: boolean): Promise<void>`
Fetch current plan details from the API. Uses cached data if available and valid unless `force` is true.

**Parameters:**
- `force` (optional): Force refresh from API, bypassing cache

**Example:**
```typescript
await fetchPlanDetails(); // Use cache if valid
await fetchPlanDetails(true); // Force refresh
```

#### `refreshPlanDetails(): Promise<void>`
Convenience method to force refresh plan details. Equivalent to `fetchPlanDetails(true)`.

**Example:**
```typescript
await refreshPlanDetails();
```

#### `upgradePlan(targetTier: string, billingCycle?: string): Promise<void>`
Upgrade subscription to a higher tier. Redirects to PayPal for payment approval.

**Parameters:**
- `targetTier`: Target plan tier (e.g., 'PROFESSIONAL', 'ENTERPRISE')
- `billingCycle` (optional): Billing cycle override

**Example:**
```typescript
await upgradePlan('PROFESSIONAL');
await upgradePlan('ENTERPRISE', 'ANNUAL');
```

**Behavior:**
- Shows loading state
- Calls `/api/saas/tenants/{tenantId}/plan/upgrade`
- Displays success toast
- Redirects to PayPal approval URL
- Shows error toast on failure

#### `cancelPlan(immediate?: boolean, reason?: string): Promise<void>`
Cancel subscription. Can be immediate or scheduled for end of billing period.

**Parameters:**
- `immediate` (optional): Cancel immediately (default: false)
- `reason` (optional): Cancellation reason for audit log

**Example:**
```typescript
await cancelPlan(); // Cancel at end of period
await cancelPlan(true, 'No longer needed'); // Cancel immediately
```

**Behavior:**
- Shows loading state
- Calls `/api/saas/tenants/{tenantId}/plan/cancel`
- Displays success toast with effective date
- Refreshes plan details
- Shows error toast on failure

#### `updatePaymentMethod(): Promise<void>`
Update payment method. Redirects to PayPal billing portal.

**Example:**
```typescript
await updatePaymentMethod();
```

**Behavior:**
- Shows loading state
- Calls `/api/saas/tenants/{tenantId}/plan/payment-method`
- Displays success toast
- Redirects to PayPal billing portal
- Shows error toast on failure

#### `clearPlanDetails(): void`
Clear cached plan details and reset state.

**Example:**
```typescript
clearPlanDetails();
```

## Error Handling

All methods include comprehensive error handling:

1. **Network Errors:** Caught and displayed via toast notifications
2. **API Errors:** Error messages extracted from response and shown to user
3. **Validation Errors:** Checked before API calls (e.g., plan must be loaded)
4. **Loading States:** Prevent concurrent operations

Error toasts include:
- Clear error title
- Actionable description
- Suggestion to retry or contact support

## Caching Strategy

- Plan details are cached for **5 minutes**
- Cache is automatically invalidated on plan changes
- Use `refreshPlanDetails()` to force refresh
- Cache is shared across all component instances using `useState`

## Toast Notifications

The composable uses `useEnhancedToast` for all notifications:

- **Success:** Green toast with checkmark icon
- **Error:** Red toast with error icon and detailed message
- **Info:** Blue toast for informational messages

## Requirements Coverage

This composable satisfies the following requirements from the spec:

- ✅ **Requirement 2.1:** Upgrade plan with PayPal link generation
- ✅ **Requirement 2.5:** Error handling for upgrade failures
- ✅ **Requirement 3.1:** Downgrade plan (via upgrade endpoint with lower tier)
- ✅ **Requirement 3.5:** Error handling for downgrade failures
- ✅ **Requirement 4.1:** Cancel subscription with confirmation
- ✅ **Requirement 4.5:** Error handling for cancellation failures
- ✅ **Requirement 5.1:** Update payment method with PayPal portal
- ✅ **Requirement 5.4:** Success message for payment updates
- ✅ **Requirement 5.5:** Error handling for payment update failures
- ✅ **Requirement 10.3:** Real-time feedback with toast notifications
- ✅ **Requirement 10.4:** Error messages with actionable guidance

## Integration with PlanCard Component

The composable is designed to work seamlessly with the `PlanCard` component:

```vue
<script setup lang="ts">
const billingPlan = useBillingPlan();

onMounted(async () => {
  await billingPlan.fetchPlanDetails();
});
</script>

<template>
  <PlanCard
    :plan="billingPlan.currentPlan.value"
    :loading="billingPlan.isLoading.value"
    @upgrade="() => billingPlan.upgradePlan('PROFESSIONAL')"
    @cancel="() => billingPlan.cancelPlan()"
    @update-payment="billingPlan.updatePaymentMethod"
  />
</template>
```

## Testing

To test the composable:

1. **Load Plan Details:**
   ```typescript
   await fetchPlanDetails();
   expect(currentPlan.value).toBeTruthy();
   ```

2. **Test Caching:**
   ```typescript
   await fetchPlanDetails();
   const firstFetch = Date.now();
   await fetchPlanDetails(); // Should use cache
   expect(Date.now() - firstFetch).toBeLessThan(100);
   ```

3. **Test Upgrade:**
   ```typescript
   await upgradePlan('PROFESSIONAL');
   // Should redirect to PayPal
   ```

4. **Test Error Handling:**
   ```typescript
   // Mock API error
   await cancelPlan();
   // Should show error toast
   ```

## Notes

- All API calls require authentication via `useAuth()`
- Tenant context is automatically included via `useTenantSlug()`
- PayPal redirects will navigate away from the current page
- After PayPal operations, users return via webhook processing
- The composable uses shared state, so updates in one component affect all instances
