<template>
  <div v-if="shouldShowBanner" :class="bannerClasses" role="alert">
    <div class="flex items-start gap-3">
      <!-- Icon -->
      <div class="flex-shrink-0">
        <UIcon 
          :name="bannerIcon" 
          :class="iconClasses"
          class="w-5 h-5"
        />
      </div>

      <!-- Content -->
      <div class="flex-1 min-w-0">
        <h3 class="text-sm font-semibold mb-1">
          {{ bannerTitle }}
        </h3>
        <p class="text-sm">
          {{ bannerMessage }}
        </p>
        
        <!-- Countdown for suspension (if applicable) -->
        <div v-if="showCountdown && daysUntilSuspension !== null" class="mt-2 text-sm font-medium">
          {{ $t('billing.suspensionCountdown', { days: daysUntilSuspension }) }}
        </div>

        <!-- Action button -->
        <div v-if="showActionButton" class="mt-3">
          <UButton
            :label="actionButtonLabel"
            :color="actionButtonColor"
            size="sm"
            @click="handleAction"
          />
        </div>
      </div>

      <!-- Close button -->
      <button
        v-if="isDismissible"
        type="button"
        :class="closeButtonClasses"
        @click="dismissBanner"
        :aria-label="$t('common.close')"
      >
        <UIcon name="i-heroicons-x-mark-20-solid" class="w-5 h-5" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n();
const billing = useBillingStatus();

// Local state for dismissal
const isDismissed = ref(false);

// Computed properties
const shouldShowBanner = computed(() => {
  if (isDismissed.value) return false;
  if (!billing.billingStatus.value) return false;
  return billing.billingStatus.value !== 'active';
});

const bannerClasses = computed(() => {
  const baseClasses = 'p-4 rounded-lg border';
  
  switch (billing.billingStatus.value) {
    case 'pending_payment':
      return `${baseClasses} bg-yellow-50 border-yellow-200 text-yellow-900 dark:bg-yellow-900/10 dark:border-yellow-800 dark:text-yellow-200`;
    case 'past_due':
      return `${baseClasses} bg-orange-50 border-orange-200 text-orange-900 dark:bg-orange-900/10 dark:border-orange-800 dark:text-orange-200`;
    case 'canceled':
      return `${baseClasses} bg-red-50 border-red-200 text-red-900 dark:bg-red-900/10 dark:border-red-800 dark:text-red-200`;
    default:
      return `${baseClasses} bg-gray-50 border-gray-200 text-gray-900 dark:bg-gray-900/10 dark:border-gray-800 dark:text-gray-200`;
  }
});

const iconClasses = computed(() => {
  switch (billing.billingStatus.value) {
    case 'pending_payment':
      return 'text-yellow-600 dark:text-yellow-400';
    case 'past_due':
      return 'text-orange-600 dark:text-orange-400';
    case 'canceled':
      return 'text-red-600 dark:text-red-400';
    default:
      return 'text-gray-600 dark:text-gray-400';
  }
});

const closeButtonClasses = computed(() => {
  switch (billing.billingStatus.value) {
    case 'pending_payment':
      return 'text-yellow-600 hover:text-yellow-800 dark:text-yellow-400 dark:hover:text-yellow-200';
    case 'past_due':
      return 'text-orange-600 hover:text-orange-800 dark:text-orange-400 dark:hover:text-orange-200';
    case 'canceled':
      return 'text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-200';
    default:
      return 'text-gray-600 hover:text-gray-800 dark:text-gray-400 dark:hover:text-gray-200';
  }
});

const bannerIcon = computed(() => {
  switch (billing.billingStatus.value) {
    case 'pending_payment':
      return 'i-heroicons-clock-20-solid';
    case 'past_due':
      return 'i-heroicons-exclamation-triangle-20-solid';
    case 'canceled':
      return 'i-heroicons-x-circle-20-solid';
    default:
      return 'i-heroicons-information-circle-20-solid';
  }
});

const bannerTitle = computed(() => {
  switch (billing.billingStatus.value) {
    case 'pending_payment':
      return t('billing.pendingPaymentTitle');
    case 'past_due':
      return t('billing.pastDueTitle');
    case 'canceled':
      return t('billing.canceledTitle');
    default:
      return t('billing.statusTitle');
  }
});

const bannerMessage = computed(() => {
  return billing.statusMessage.value || t('billing.defaultMessage');
});

const showActionButton = computed(() => {
  return billing.billingStatus.value !== 'active';
});

const actionButtonLabel = computed(() => {
  switch (billing.billingStatus.value) {
    case 'pending_payment':
      return t('billing.completePayment');
    case 'past_due':
      return t('billing.updatePayment');
    case 'canceled':
      return t('billing.reactivate');
    default:
      return t('billing.manageSubscription');
  }
});

const actionButtonColor = computed(() => {
  switch (billing.billingStatus.value) {
    case 'canceled':
      return 'red';
    case 'past_due':
      return 'orange';
    default:
      return 'primary';
  }
});

const isDismissible = computed(() => {
  // Only pending_payment can be dismissed temporarily
  return billing.billingStatus.value === 'pending_payment';
});

const showCountdown = computed(() => {
  // Show countdown for past_due status
  return billing.billingStatus.value === 'past_due';
});

// Calculate days until suspension (placeholder - would need actual data from API)
const daysUntilSuspension = computed(() => {
  // This would typically come from the API response
  // For now, return null as we don't have this data
  return null;
});

// Methods
function dismissBanner() {
  isDismissed.value = true;
}

function handleAction() {
  // Navigate to appropriate billing page or external PayPal link
  switch (billing.billingStatus.value) {
    case 'pending_payment':
      navigateTo('/billing/pending');
      break;
    case 'past_due':
      navigateTo('/billing/past-due');
      break;
    case 'canceled':
      navigateTo('/billing/canceled');
      break;
    default:
      break;
  }
}
</script>
