<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950">
    <UContainer class="py-10">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="text-sm font-semibold uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">
            {{ t('navigation.clinicSettings') }}
          </p>
          <h1 class="text-3xl font-bold text-slate-900 dark:text-white">
            {{ t('billing.plan.title') }}
          </h1>
          <p class="text-slate-600 dark:text-slate-300">
            {{ t('billing.plan.subtitle') }}
          </p>
        </div>
        <div class="flex gap-3">
          <UButton
            variant="ghost"
            color="gray"
            icon="i-lucide-refresh-cw"
            :loading="billingPlanLoading"
            @click="refreshPlan"
          >
            {{ t('clinicSettings.actions.refresh') }}
          </UButton>
          <UButton
            color="violet"
            icon="i-lucide-credit-card"
            :loading="billingPlanLoading"
            @click="handleUpdatePaymentMethod"
          >
            {{ t('billing.plan.update') }}
          </UButton>
        </div>
      </div>

      <div class="mt-8 grid gap-6 lg:grid-cols-3">
        <div class="lg:col-span-2">
          <UAlert
            v-if="usageAlert"
            :color="usageAlert.color"
            :icon="usageAlert.isBlocking ? 'i-lucide-alert-triangle' : 'i-lucide-info'"
            variant="subtle"
            class="mb-4"
          >
            <template #title>
              {{ usageAlert.isBlocking ? (t('billing.limitReached') || 'Plan limit reached') : (t('billing.usageUpdate') || 'Usage update') }}
            </template>
            {{ usageAlert.message }}
          </UAlert>

          <PlanCard
            :plan="billingPlanData"
            :loading="billingPlanLoading"
            @upgrade="showUpgradeModal = true"
            @cancel="showCancelModal = true"
            @resume="handleResumePlan"
            @update-payment="handleUpdatePaymentMethod"
          />

          <UAlert
            v-if="billingPlanError"
            color="red"
            icon="i-lucide-alert-circle"
            class="mt-6"
            title="Unable to load subscription"
          >
            {{ billingPlanError.message || t('clinicSettings.errors.loadTitle') }}
          </UAlert>
        </div>

        <div class="space-y-4">
          <UCard>
            <template #header>
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-shield-check" class="h-5 w-5 text-emerald-600" />
                <div>
                  <p class="text-sm font-semibold text-slate-900 dark:text-white">
                    {{ t('billing.plan.title') }}
                  </p>
                  <p class="text-xs text-slate-500 dark:text-slate-400">
                    {{ t('billing.plan.subtitle') }}
                  </p>
                </div>
              </div>
            </template>
            <ul class="space-y-2">
              <li
                v-for="(feature, idx) in billingPlanData?.features || fallbackFeatures"
                :key="idx"
                class="flex items-start gap-2 text-sm text-slate-700 dark:text-slate-200"
              >
                <UIcon name="i-lucide-check-circle-2" class="mt-0.5 h-4 w-4 text-emerald-600" />
                <span>{{ feature }}</span>
              </li>
            </ul>
          </UCard>

          <UCard>
            <template #header>
              <div class="flex items-center gap-2 text-sm font-semibold text-slate-900 dark:text-white">
                <UIcon name="i-lucide-activity" class="h-4 w-4" />
                {{ t('billing.plan.helpText') }}
              </div>
            </template>
            <p class="text-sm text-slate-600 dark:text-slate-300">
              {{ t('billing.plan.modal.upgradeInfo') }}
            </p>
          </UCard>
        </div>
      </div>
    </UContainer>

    <PlanUpgradeModal
      v-model="showUpgradeModal"
      :current-tier="currentPlanTier"
      :available-tiers="availablePlanTiers"
      :loading="upgradeLoading"
      @confirm="handleUpgradeConfirm"
    />

    <CancelConfirmationModal
      v-model="showCancelModal"
      :effective-date="billingPlanData?.renewalDate || ''"
      :loading="cancelLoading"
      @confirm="handleCancelConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import PlanCard from '@/components/billing/PlanCard.vue';
import PlanUpgradeModal from '@/components/billing/PlanUpgradeModal.vue';
import CancelConfirmationModal from '@/components/billing/CancelConfirmationModal.vue';
import { useBillingPlan } from '@/composables/useBillingPlan';
import { usePlanCatalog } from '@/composables/usePlanCatalog';

const { t } = useI18n();

const {
  currentPlan: billingPlan,
  isLoading: billingPlanLoading,
  error: billingPlanError,
  fetchPlanDetails,
  upgradePlan,
  cancelPlan: cancelBillingPlan,
  updatePaymentMethod,
  resumePlan
} = useBillingPlan();
const { normalizedPlans: planCatalog, fallbackNormalized: fallbackPlanCatalog, fetchPlans: fetchPlanCatalog } = usePlanCatalog();

const showUpgradeModal = ref(false);
const showCancelModal = ref(false);
const upgradeLoading = ref(false);
const cancelLoading = ref(false);

const fallbackFeatures = [
  "Unlimited doctors and staff accounts",
  "Priority chat & email support",
  "Custom domains and branding",
  "Audit-ready activity logs"
];

const normalizePlanStatus = (status?: string | null) => {
  if (!status) return 'pending';
  const normalized = status.toLowerCase().replace(/[\s-]+/g, '_');
  if (normalized === 'cancelled') return 'canceled';
  if (normalized === 'trial' || normalized === 'trialing') return 'trial';
  if (['active', 'past_due', 'canceled', 'pending', 'trial'].includes(normalized)) {
    return normalized;
  }
  return 'pending';
};

const resolvedPlanPrice = computed(() => {
  const price = billingPlan.value?.price;
  if (price && price > 0) return price;

  const catalog = planCatalog.value.length ? planCatalog.value : fallbackPlanCatalog;
  const match = catalog.find((plan) => plan.tier === billingPlan.value?.planTier);
  return match?.price ?? 0;
});

const resolvedPlanCurrency = computed(() => {
  if (billingPlan.value?.currency) return billingPlan.value.currency;
  const catalog = planCatalog.value.length ? planCatalog.value : fallbackPlanCatalog;
  const match = catalog.find((plan) => plan.tier === billingPlan.value?.planTier);
  return match?.currency || 'USD';
});

const billingPlanData = computed(() => {
  if (!billingPlan.value) return null;
  return {
    tenantId: billingPlan.value.tenantId,
    planTier: billingPlan.value.planTier,
    tierName: billingPlan.value.planTierName,
    price: resolvedPlanPrice.value,
    currency: resolvedPlanCurrency.value,
    billingCycle: (billingPlan.value.billingCycle || 'MONTHLY').toUpperCase() as 'MONTHLY' | 'ANNUAL' | 'TRIAL',
    renewalDate: billingPlan.value.renewalDate || '',
    paymentMethodMask: billingPlan.value.paymentMethodMask,
    paymentMethodType: billingPlan.value.paymentMethodType,
    status: normalizePlanStatus(billingPlan.value.status),
    cancellationDate: billingPlan.value.cancellationDate,
    cancellationEffectiveDate: billingPlan.value.cancellationEffectiveDate,
    pendingPlanTier: billingPlan.value.pendingPlanTier,
    pendingPlanEffectiveDate: billingPlan.value.pendingPlanEffectiveDate,
    features: billingPlan.value.features,
    maxStaff: billingPlan.value.maxStaff,
    maxDoctors: billingPlan.value.maxDoctors,
    staffUsed: billingPlan.value.staffUsed,
    doctorsUsed: billingPlan.value.doctorsUsed
  };
});

const currentPlanTier = computed(() => billingPlan.value?.planTierName || 'Basic');

const availablePlanTiers = computed(() => {
  const currentTier = billingPlan.value?.planTier || 'BASIC';
  const tiers = planCatalog.value.length ? planCatalog.value : fallbackPlanCatalog;

  const tierOrder = ['BASIC', 'PROFESSIONAL', 'ENTERPRISE', 'CUSTOM'];
  const currentIndex = tierOrder.indexOf(currentTier);

  return tiers.filter(tier => tierOrder.indexOf(tier.value) > currentIndex);
});

const usageAlert = computed(() => {
  const plan = billingPlan.value;
  if (!plan) return null;

  const entries: string[] = [];
  let severity: 'red' | 'amber' | 'blue' | null = null;

  const register = (used: number, max: number, label: string) => {
    if (max == null || max < 0) return;
    const ratio = max === 0 ? 1 : used / max;
    entries.push(`${used}/${max} ${label} used`);
    if (used >= max) {
      severity = 'red';
    } else if (ratio >= 0.8 && severity !== 'red') {
      severity = 'amber';
    } else if (!severity) {
      severity = 'blue';
    }
  };

  register(plan.staffUsed ?? 0, plan.maxStaff ?? -1, t('billing.staffSeats') || 'Staff seats');
  register(plan.doctorsUsed ?? 0, plan.maxDoctors ?? -1, t('billing.doctors') || 'Doctors');

  if (!entries.length) return null;

  return {
    color: severity || 'blue',
    message: entries.join(' · '),
    isBlocking: severity === 'red'
  };
});

onMounted(() => {
  fetchPlanDetails();
  fetchPlanCatalog();
});

async function handleUpgradeConfirm(targetTier: string, billingCycle: string) {
  upgradeLoading.value = true;
  try {
    await upgradePlan(targetTier, billingCycle);
    showUpgradeModal.value = false;
  } finally {
    upgradeLoading.value = false;
  }
}

async function handleCancelConfirm(immediate: boolean, reason?: string) {
  cancelLoading.value = true;
  try {
    await cancelBillingPlan(immediate, reason);
    showCancelModal.value = false;
  } finally {
    cancelLoading.value = false;
  }
}

async function handleUpdatePaymentMethod() {
  try {
    await updatePaymentMethod();
  } catch (error) {
    console.error('Payment method update failed:', error);
  }
}

async function handleResumePlan() {
  try {
    await resumePlan();
  } catch (error) {
    console.error('Resume plan failed:', error);
  }
}

function refreshPlan() {
  fetchPlanDetails(true);
}
</script>
