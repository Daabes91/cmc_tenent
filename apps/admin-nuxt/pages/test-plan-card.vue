<template>
  <div class="container mx-auto p-8 space-y-8">
    <h1 class="text-3xl font-bold">PlanCard Component Test</h1>

    <!-- Test Case 1: Active Plan -->
    <div>
      <h2 class="text-xl font-semibold mb-4">Active Plan</h2>
      <PlanCard
        :plan="activePlan"
        :loading="false"
        @upgrade="handleUpgrade"
        @cancel="handleCancel"
        @update-payment="handleUpdatePayment"
      />
    </div>

    <!-- Test Case 2: Loading State -->
    <div>
      <h2 class="text-xl font-semibold mb-4">Loading State</h2>
      <PlanCard :loading="true" />
    </div>

    <!-- Test Case 3: Plan with Pending Cancellation -->
    <div>
      <h2 class="text-xl font-semibold mb-4">Plan with Pending Cancellation</h2>
      <PlanCard
        :plan="planWithCancellation"
        :loading="false"
        @upgrade="handleUpgrade"
        @cancel="handleCancel"
        @update-payment="handleUpdatePayment"
      />
    </div>

    <!-- Test Case 4: Plan with Pending Downgrade -->
    <div>
      <h2 class="text-xl font-semibold mb-4">Plan with Pending Downgrade</h2>
      <PlanCard
        :plan="planWithDowngrade"
        :loading="false"
        @upgrade="handleUpgrade"
        @cancel="handleCancel"
        @update-payment="handleUpdatePayment"
      />
    </div>

    <!-- Test Case 5: Past Due Plan -->
    <div>
      <h2 class="text-xl font-semibold mb-4">Past Due Plan</h2>
      <PlanCard
        :plan="pastDuePlan"
        :loading="false"
        @upgrade="handleUpgrade"
        @cancel="handleCancel"
        @update-payment="handleUpdatePayment"
      />
    </div>

    <!-- Test Case 6: Canceled Plan -->
    <div>
      <h2 class="text-xl font-semibold mb-4">Canceled Plan</h2>
      <PlanCard
        :plan="canceledPlan"
        :loading="false"
        @upgrade="handleUpgrade"
        @cancel="handleCancel"
        @update-payment="handleUpdatePayment"
      />
    </div>

    <!-- Event Log -->
    <div class="mt-8 p-4 bg-gray-100 dark:bg-gray-800 rounded-lg">
      <h3 class="text-lg font-semibold mb-2">Event Log</h3>
      <div v-if="events.length === 0" class="text-gray-500">No events yet</div>
      <ul v-else class="space-y-1">
        <li v-for="(event, index) in events" :key="index" class="text-sm">
          {{ event }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
const toast = useToast();
const events = ref<string[]>([]);

const activePlan = {
  tenantId: 1,
  planTier: 'PROFESSIONAL',
  tierName: 'Professional',
  price: 99.99,
  currency: 'USD',
  billingCycle: 'MONTHLY' as const,
  renewalDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
  paymentMethodMask: 'Visa ****1234',
  paymentMethodType: 'CREDIT_CARD',
  status: 'active' as const,
  features: ['Feature 1', 'Feature 2', 'Feature 3'],
};

const planWithCancellation = {
  ...activePlan,
  cancellationDate: new Date().toISOString(),
  cancellationEffectiveDate: new Date(Date.now() + 15 * 24 * 60 * 60 * 1000).toISOString(),
};

const planWithDowngrade = {
  ...activePlan,
  pendingPlanTier: 'BASIC',
  pendingPlanEffectiveDate: new Date(Date.now() + 20 * 24 * 60 * 60 * 1000).toISOString(),
};

const pastDuePlan = {
  ...activePlan,
  status: 'past_due' as const,
};

const canceledPlan = {
  ...activePlan,
  status: 'canceled' as const,
  cancellationDate: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString(),
};

function handleUpgrade() {
  const message = `Upgrade event triggered at ${new Date().toLocaleTimeString()}`;
  events.value.unshift(message);
  toast.add({
    title: 'Upgrade Clicked',
    description: 'Upgrade plan action triggered',
    color: 'green',
  });
}

function handleCancel() {
  const message = `Cancel event triggered at ${new Date().toLocaleTimeString()}`;
  events.value.unshift(message);
  toast.add({
    title: 'Cancel Clicked',
    description: 'Cancel subscription action triggered',
    color: 'orange',
  });
}

function handleUpdatePayment() {
  const message = `Update payment event triggered at ${new Date().toLocaleTimeString()}`;
  events.value.unshift(message);
  toast.add({
    title: 'Update Payment Clicked',
    description: 'Update payment method action triggered',
    color: 'blue',
  });
}
</script>
