<template>
  <UCard :ui="{ body: { padding: 'p-6 sm:p-8' } }">
    <!-- Loading State -->
    <div v-if="loading" class="space-y-4">
      <div class="h-6 bg-gray-200 dark:bg-gray-700 rounded animate-pulse w-1/3"></div>
      <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded animate-pulse w-1/2"></div>
      <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded animate-pulse w-2/3"></div>
    </div>

    <!-- Plan Content -->
    <div v-else class="space-y-6">
      <!-- Header -->
      <div class="flex items-start justify-between">
        <div>
          <h3 class="text-2xl font-bold text-gray-900 dark:text-white">
            {{ plan?.tierName || $t('billing.plan.currentPlan') }}
          </h3>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
            {{ $t('billing.plan.subscriptionPlan') }}
          </p>
        </div>
        <UBadge 
          :color="statusColor" 
          variant="subtle" 
          size="lg"
          class="font-semibold"
        >
          {{ statusLabel }}
        </UBadge>
      </div>

      <!-- Pending Change Notice -->
      <div 
        v-if="hasPendingChange" 
        class="flex items-start gap-3 p-4 rounded-lg bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800"
      >
        <UIcon 
          name="i-heroicons-information-circle" 
          class="w-5 h-5 text-blue-600 dark:text-blue-400 flex-shrink-0 mt-0.5"
        />
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-blue-900 dark:text-blue-100">
            {{ $t('billing.plan.pendingChange') }}
          </p>
          <p class="text-sm text-blue-700 dark:text-blue-300 mt-1">
            {{ pendingChangeMessage }}
          </p>
        </div>
      </div>

      <!-- Price -->
      <div class="flex items-baseline gap-2">
        <span class="text-4xl font-bold text-gray-900 dark:text-white">
          {{ formattedPrice }}
        </span>
        <span class="text-lg text-gray-500 dark:text-gray-400">
          / {{ billingCycleLabel }}
        </span>
      </div>

      <!-- Plan Details -->
      <div class="space-y-3">
        <!-- Renewal Date -->
        <div class="flex items-center gap-3 text-sm">
          <UIcon 
            name="i-heroicons-calendar" 
            class="w-5 h-5 text-gray-400 dark:text-gray-500 flex-shrink-0"
          />
          <span class="text-gray-600 dark:text-gray-300">
            {{ $t('billing.plan.nextRenewal') }}:
          </span>
          <span class="font-medium text-gray-900 dark:text-white">
            {{ formattedRenewalDate }}
          </span>
        </div>

        <!-- Payment Method -->
        <div class="flex items-center justify-between text-sm">
          <div class="flex items-center gap-3">
            <UIcon 
              name="i-heroicons-credit-card" 
              class="w-5 h-5 text-gray-400 dark:text-gray-500 flex-shrink-0"
            />
            <span class="text-gray-600 dark:text-gray-300">
              {{ $t('billing.plan.paymentMethod') }}:
            </span>
            <span class="font-medium text-gray-900 dark:text-white">
              {{ plan?.paymentMethodMask || $t('billing.plan.noPaymentMethod') }}
            </span>
          </div>
          <UButton
            size="xs"
            variant="ghost"
            color="gray"
            :label="$t('billing.plan.update')"
            :disabled="!canUpdatePayment"
            @click="handleUpdatePayment"
          />
        </div>
      </div>

      <!-- Divider -->
      <div class="border-t border-gray-200 dark:border-gray-700"></div>

      <!-- Action Buttons -->
      <div class="flex flex-col sm:flex-row gap-3">
        <UButton
          color="primary"
          size="lg"
          :label="$t('billing.plan.upgradePlan')"
          :disabled="isHighestTier || !canUpgrade"
          :loading="loading"
          class="flex-1"
          @click="handleUpgrade"
        >
          <template #leading>
            <UIcon name="i-heroicons-arrow-trending-up" class="w-5 h-5" />
          </template>
        </UButton>

        <UButton
          v-if="canResume"
          color="green"
          variant="outline"
          size="lg"
          :label="$t('billing.plan.resumeSubscription')"
          :disabled="loading"
          :loading="loading"
          class="flex-1"
          @click="handleResume"
        >
          <template #leading>
            <UIcon name="i-heroicons-play-circle" class="w-5 h-5" />
          </template>
        </UButton>

        <UButton
          v-else
          color="gray"
          variant="outline"
          size="lg"
          :label="$t('billing.plan.cancelSubscription')"
          :disabled="!canCancel"
          :loading="loading"
          class="flex-1"
          @click="handleCancel"
        >
          <template #leading>
            <UIcon name="i-heroicons-x-circle" class="w-5 h-5" />
          </template>
        </UButton>
      </div>

      <!-- Help Text -->
      <p class="text-xs text-gray-500 dark:text-gray-400 text-center">
        {{ $t('billing.plan.helpText') }}
      </p>
    </div>
  </UCard>
</template>

<script setup lang="ts">
type PlanStatus = 'active' | 'past_due' | 'canceled' | 'pending';

interface PlanDetails {
  tenantId: number;
  planTier: string;
  tierName: string;
  price: number;
  currency: string;
  billingCycle: 'MONTHLY' | 'ANNUAL';
  renewalDate: string;
  paymentMethodMask?: string;
  paymentMethodType?: string;
  status: 'active' | 'past_due' | 'canceled' | 'pending';
  cancellationDate?: string;
  cancellationEffectiveDate?: string;
  pendingPlanTier?: string;
  pendingPlanEffectiveDate?: string;
  features?: string[];
}

interface Props {
  plan?: PlanDetails | null;
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  plan: null,
  loading: false,
});

const emit = defineEmits<{
  upgrade: [];
  cancel: [];
  resume: [];
  'update-payment': [];
}>();

const { t, locale } = useI18n();

const resolveStatus = (status?: string | null): PlanStatus | null => {
  if (!status) return null;
  const normalized = status.toLowerCase().replace(/[\s-]+/g, '_');

  switch (normalized) {
    case 'active':
      return 'active';
    case 'past_due':
      return 'past_due';
    case 'canceled':
    case 'cancelled':
      return 'canceled';
    case 'pending':
    case 'pending_change':
    case 'pending_cancellation':
    case 'pending_downgrade':
      return 'pending';
    default:
      return null;
  }
};

const normalizedStatus = computed<PlanStatus | null>(() => resolveStatus(props.plan?.status));

// Computed Properties
const statusColor = computed(() => {
  switch (normalizedStatus.value) {
    case 'active':
      return 'green';
    case 'past_due':
      return 'orange';
    case 'canceled':
      return 'red';
    case 'pending':
      return 'yellow';
    default:
      return 'gray';
  }
});

const statusLabel = computed(() => {
  if (!normalizedStatus.value) return t('billing.plan.unknown');
  
  const statusKey = `billing.plan.status.${normalizedStatus.value}`;
  return t(statusKey);
});

const hasPendingChange = computed(() => {
  return !!(props.plan?.pendingPlanTier || props.plan?.cancellationEffectiveDate);
});

const pendingChangeMessage = computed(() => {
  if (props.plan?.cancellationEffectiveDate) {
    const effectiveDate = formatDate(props.plan.cancellationEffectiveDate);
    return t('billing.plan.pendingCancellation', { date: effectiveDate });
  }
  
  if (props.plan?.pendingPlanTier && props.plan?.pendingPlanEffectiveDate) {
    const effectiveDate = formatDate(props.plan.pendingPlanEffectiveDate);
    return t('billing.plan.pendingDowngrade', { 
      tier: props.plan.pendingPlanTier, 
      date: effectiveDate 
    });
  }
  
  return '';
});

const formattedPrice = computed(() => {
  if (props.plan?.price == null || !props.plan?.currency) {
    return t('billing.plan.priceUnavailable');
  }
  
  return new Intl.NumberFormat(locale.value, {
    style: 'currency',
    currency: props.plan.currency,
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(props.plan.price);
});

const billingCycleLabel = computed(() => {
  if (!props.plan?.billingCycle) return '';

  const normalizedCycle = props.plan.billingCycle.toUpperCase();

  if (normalizedCycle === 'MONTHLY') {
    return t('billing.plan.month');
  }

  if (normalizedCycle === 'ANNUAL' || normalizedCycle === 'YEARLY') {
    return t('billing.plan.year');
  }

  return props.plan.billingCycle;
});

const formattedRenewalDate = computed(() => {
  if (!props.plan?.renewalDate) {
    return t('billing.plan.noRenewalDate');
  }
  
  return formatDate(props.plan.renewalDate);
});

const isHighestTier = computed(() => {
  // This would need to be determined based on available tiers
  // For now, assume ENTERPRISE is the highest
  return props.plan?.planTier === 'ENTERPRISE' || props.plan?.planTier === 'CUSTOM';
});

const canUpgrade = computed(() => {
  return normalizedStatus.value === 'active' && !hasPendingChange.value;
});

const canCancel = computed(() => {
  return normalizedStatus.value === 'active' && !props.plan?.cancellationEffectiveDate;
});

const canUpdatePayment = computed(() => {
  return normalizedStatus.value !== 'canceled';
});

const canResume = computed(() => normalizedStatus.value === 'canceled');

// Methods
function formatDate(dateInput: string | null | undefined): string {
  if (!dateInput) return t('billing.plan.noRenewalDate');

  const date = new Date(dateInput as any);
  if (Number.isNaN(date.getTime())) {
    return t('billing.plan.noRenewalDate');
  }

  try {
    return new Intl.DateTimeFormat(locale.value, {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    }).format(date);
  } catch (error) {
    return t('billing.plan.noRenewalDate');
  }
}

function handleUpgrade() {
  emit('upgrade');
}

function handleCancel() {
  emit('cancel');
}

function handleUpdatePayment() {
  emit('update-payment');
}

function handleResume() {
  emit('resume');
}
</script>
