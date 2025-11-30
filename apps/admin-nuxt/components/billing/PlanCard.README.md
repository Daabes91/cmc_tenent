# PlanCard Component

## Overview

The `PlanCard` component displays subscription plan details for tenant administrators in the clinic settings page. It provides a comprehensive view of the current plan, including pricing, renewal date, payment method, and status, along with action buttons for plan management.

## Features

- **Plan Information Display**: Shows tier name, price, billing cycle, renewal date, and payment method
- **Status Badge**: Visual indicator of subscription status (active, past_due, canceled, pending)
- **Pending Change Notice**: Alerts users to scheduled downgrades or cancellations
- **Action Buttons**: Upgrade plan, cancel subscription, and update payment method
- **Loading State**: Skeleton loading animation during data fetch
- **Responsive Design**: Adapts to mobile and desktop layouts
- **Internationalization**: Full support for English and Arabic translations
- **Dark Mode**: Supports light and dark themes

## Props

### `plan`
- **Type**: `PlanDetails | null`
- **Required**: No
- **Default**: `null`
- **Description**: The subscription plan details object

#### PlanDetails Interface
```typescript
interface PlanDetails {
  tenantId: number;
  planTier: string;                    // e.g., 'BASIC', 'PROFESSIONAL', 'ENTERPRISE'
  tierName: string;                    // Display name, e.g., 'Professional'
  price: number;                       // Monthly or annual price
  currency: string;                    // ISO currency code, e.g., 'USD'
  billingCycle: 'MONTHLY' | 'ANNUAL'; // Billing frequency
  renewalDate: string;                 // ISO date string
  paymentMethodMask?: string;          // e.g., 'Visa ****1234'
  paymentMethodType?: string;          // e.g., 'CREDIT_CARD', 'PAYPAL'
  status: 'active' | 'past_due' | 'canceled' | 'pending';
  cancellationDate?: string;           // ISO date string
  cancellationEffectiveDate?: string;  // ISO date string
  pendingPlanTier?: string;            // For scheduled downgrades
  pendingPlanEffectiveDate?: string;   // ISO date string
  features?: string[];                 // List of plan features
}
```

### `loading`
- **Type**: `boolean`
- **Required**: No
- **Default**: `false`
- **Description**: Shows loading skeleton when true

## Events

### `@upgrade`
- **Payload**: None
- **Description**: Emitted when the user clicks the "Upgrade Plan" button
- **Usage**: Open upgrade modal or navigate to upgrade flow

### `@cancel`
- **Payload**: None
- **Description**: Emitted when the user clicks the "Cancel Subscription" button
- **Usage**: Open cancellation confirmation modal

### `@update-payment`
- **Payload**: None
- **Description**: Emitted when the user clicks the "Update" button next to payment method
- **Usage**: Redirect to PayPal billing portal or open payment update flow

## Usage Examples

### Basic Usage
```vue
<template>
  <PlanCard
    :plan="currentPlan"
    :loading="loading"
    @upgrade="handleUpgrade"
    @cancel="handleCancel"
    @update-payment="handleUpdatePayment"
  />
</template>

<script setup lang="ts">
const currentPlan = ref(null);
const loading = ref(true);

onMounted(async () => {
  try {
    const response = await $fetch('/api/billing/current-plan');
    currentPlan.value = response.data;
  } finally {
    loading.value = false;
  }
});

function handleUpgrade() {
  // Open upgrade modal
}

function handleCancel() {
  // Open cancellation confirmation
}

function handleUpdatePayment() {
  // Redirect to PayPal
}
</script>
```

### With Composable
```vue
<template>
  <PlanCard
    :plan="currentPlan"
    :loading="loading"
    @upgrade="upgradePlan"
    @cancel="cancelPlan"
    @update-payment="updatePaymentMethod"
  />
</template>

<script setup lang="ts">
const { 
  currentPlan, 
  loading, 
  upgradePlan, 
  cancelPlan, 
  updatePaymentMethod 
} = useBillingPlan();
</script>
```

## Visual States

### Active Plan
- Green status badge
- All action buttons enabled
- Shows next renewal date
- Displays payment method

### Past Due Plan
- Orange status badge
- Upgrade disabled
- Cancel disabled
- Update payment enabled

### Canceled Plan
- Red status badge
- All action buttons disabled
- Shows cancellation date

### Pending Change
- Blue info banner displayed
- Shows effective date of change
- Relevant action buttons disabled

### Loading State
- Skeleton animation
- No interactive elements
- Maintains layout structure

## Button States

### Upgrade Button
- **Enabled**: When status is 'active' and no pending changes
- **Disabled**: When highest tier, status is not active, or has pending changes
- **Hidden**: Never (always visible but may be disabled)

### Cancel Button
- **Enabled**: When status is 'active' and no cancellation scheduled
- **Disabled**: When status is not active or cancellation already scheduled
- **Hidden**: Never (always visible but may be disabled)

### Update Payment Button
- **Enabled**: When status is not 'canceled'
- **Disabled**: When status is 'canceled'
- **Hidden**: Never (always visible but may be disabled)

## Internationalization

The component uses the following translation keys:

### English (`en.json`)
```json
{
  "billing": {
    "plan": {
      "currentPlan": "Current Plan",
      "subscriptionPlan": "Subscription Plan",
      "nextRenewal": "Next Renewal",
      "paymentMethod": "Payment Method",
      "noPaymentMethod": "No payment method",
      "update": "Update",
      "upgradePlan": "Upgrade Plan",
      "cancelSubscription": "Cancel Subscription",
      "helpText": "Need help with your subscription? Contact our support team.",
      "status": {
        "active": "Active",
        "past_due": "Past Due",
        "canceled": "Canceled",
        "pending": "Pending"
      },
      "pendingChange": "Pending Change",
      "pendingCancellation": "Your subscription will be canceled on {date}",
      "pendingDowngrade": "Your plan will change to {tier} on {date}",
      "priceUnavailable": "Price unavailable",
      "month": "month",
      "year": "year",
      "noRenewalDate": "No renewal date"
    }
  }
}
```

## Styling

The component uses Nuxt UI components and Tailwind CSS classes:
- `UCard`: Container with padding
- `UBadge`: Status indicator
- `UIcon`: Icons from Heroicons
- `UButton`: Action buttons

### Color Scheme
- **Active**: Green (`green`)
- **Past Due**: Orange (`orange`)
- **Canceled**: Red (`red`)
- **Pending**: Yellow (`yellow`)
- **Info**: Blue (`blue`)

## Accessibility

- Semantic HTML structure
- ARIA labels on interactive elements
- Keyboard navigation support
- Screen reader friendly
- Color contrast compliant
- Focus indicators

## Testing

A test page is available at `/test-plan-card` that demonstrates:
- Active plan state
- Loading state
- Pending cancellation
- Pending downgrade
- Past due status
- Canceled status
- Event handling

## Integration with Clinic Settings

To integrate into the clinic settings page:

```vue
<template>
  <div class="space-y-8">
    <!-- Other settings sections -->
    
    <section>
      <h2 class="text-2xl font-bold mb-4">
        {{ $t('billing.plan.subscriptionPlan') }}
      </h2>
      
      <PlanCard
        :plan="currentPlan"
        :loading="loadingPlan"
        @upgrade="showUpgradeModal = true"
        @cancel="showCancelModal = true"
        @update-payment="redirectToPayPal"
      />
    </section>
    
    <!-- Modals -->
    <PlanUpgradeModal v-model="showUpgradeModal" />
    <CancelConfirmationModal v-model="showCancelModal" />
  </div>
</template>
```

## Requirements Satisfied

This component satisfies the following requirements from the specification:

- **1.1**: Displays current plan tier name
- **1.2**: Displays monthly or annual price in tenant's currency
- **1.3**: Displays next renewal date
- **1.4**: Displays masked payment method summary
- **1.5**: Displays subscription status (active, past_due, canceled, pending)
- **10.1**: Shows loading indicator during async operations
- **10.2**: Displays current status during operations

## Future Enhancements

Potential improvements for future iterations:
- Feature list display
- Plan comparison tooltip
- Usage metrics
- Billing history link
- Invoice download
- Proration preview for upgrades
