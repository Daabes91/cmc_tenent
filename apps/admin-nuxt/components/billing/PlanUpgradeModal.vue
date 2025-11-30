<template>
  <UModal v-model="isOpen" prevent-close :ui="{ width: 'max-w-2xl' }">
    <UCard>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900/30">
            <UIcon name="i-heroicons-arrow-trending-up" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
          </div>
          <div>
            <p class="text-lg font-semibold text-gray-900 dark:text-white">
              {{ $t('billing.plan.modal.upgradeTitle') }}
            </p>
            <p class="text-sm text-gray-500 dark:text-gray-400">
              {{ $t('billing.plan.modal.upgradeDescription') }}
            </p>
          </div>
        </div>
      </template>

      <div class="space-y-6">
        <!-- Current Plan Info -->
        <div class="rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800/50 p-4">
          <p class="text-xs font-medium uppercase text-gray-500 dark:text-gray-400 mb-1">
            {{ $t('billing.plan.modal.currentPlan') }}
          </p>
          <p class="text-lg font-semibold text-gray-900 dark:text-white">
            {{ currentTier }}
          </p>
        </div>

        <!-- Available Tiers -->
        <div class="space-y-3">
          <p class="text-sm font-medium text-gray-700 dark:text-gray-300">
            {{ $t('billing.plan.modal.selectNewPlan') }}
          </p>
          
          <div class="grid gap-3">
            <div
              v-for="tier in availableTiers"
              :key="tier.value"
              class="relative flex items-center gap-4 rounded-lg border-2 p-4 cursor-pointer transition-all"
              :class="selectedTier === tier.value
                ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                : 'border-gray-200 dark:border-gray-700 hover:border-blue-300 dark:hover:border-blue-700'"
              @click="selectedTier = tier.value"
            >
              <input
                type="radio"
                :value="tier.value"
                v-model="selectedTier"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500"
              />
              <div class="flex-1">
                <div class="flex items-center justify-between">
                  <p class="font-semibold text-gray-900 dark:text-white">
                    {{ tier.label }}
                  </p>
                  <p class="text-lg font-bold text-gray-900 dark:text-white">
                    {{ formatPrice(tier.price, tier.currency) }}
                    <span class="text-sm font-normal text-gray-500">
                      /{{ tier.billingCycle === 'ANNUAL' ? $t('billing.plan.year') : $t('billing.plan.month') }}
                    </span>
                  </p>
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                  {{ tier.description }}
                </p>
                <ul v-if="tier.features?.length" class="mt-2 space-y-1">
                  <li
                    v-for="(feature, idx) in tier.features.slice(0, 3)"
                    :key="idx"
                    class="flex items-center gap-2 text-xs text-gray-600 dark:text-gray-400"
                  >
                    <UIcon name="i-heroicons-check-circle" class="h-4 w-4 text-green-500 flex-shrink-0" />
                    <span>{{ feature }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- Billing Cycle Selection -->
        <div class="space-y-2">
          <p class="text-sm font-medium text-gray-700 dark:text-gray-300">
            {{ $t('billing.plan.modal.billingCycle') }}
          </p>
          <div class="flex gap-3">
            <button
              type="button"
              class="flex-1 rounded-lg border-2 p-3 text-center transition-all"
              :class="billingCycle === 'MONTHLY'
                ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300'
                : 'border-gray-200 dark:border-gray-700 hover:border-blue-300'"
              @click="billingCycle = 'MONTHLY'"
            >
              <p class="font-semibold">{{ $t('billing.plan.monthly') }}</p>
              <p class="text-xs text-gray-500 dark:text-gray-400">{{ $t('billing.plan.modal.billedMonthly') }}</p>
            </button>
            <button
              type="button"
              class="flex-1 rounded-lg border-2 p-3 text-center transition-all"
              :class="billingCycle === 'ANNUAL'
                ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300'
                : 'border-gray-200 dark:border-gray-700 hover:border-blue-300'"
              @click="billingCycle = 'ANNUAL'"
            >
              <p class="font-semibold">{{ $t('billing.plan.annual') }}</p>
              <p class="text-xs text-gray-500 dark:text-gray-400">{{ $t('billing.plan.modal.billedAnnually') }}</p>
              <UBadge color="green" size="xs" class="mt-1">{{ $t('billing.plan.modal.save20') }}</UBadge>
            </button>
          </div>
        </div>

        <!-- Info Alert -->
        <div class="rounded-lg border border-blue-200 dark:border-blue-800 bg-blue-50 dark:bg-blue-900/20 p-4">
          <div class="flex items-start gap-3">
            <UIcon name="i-heroicons-information-circle" class="h-5 w-5 text-blue-600 dark:text-blue-400 flex-shrink-0 mt-0.5" />
            <p class="text-sm text-blue-700 dark:text-blue-300">
              {{ $t('billing.plan.modal.upgradeInfo') }}
            </p>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex flex-col sm:flex-row sm:justify-end gap-3">
          <UButton
            variant="ghost"
            color="gray"
            :disabled="loading"
            @click="handleCancel"
          >
            {{ $t('billing.plan.modal.cancelButton') }}
          </UButton>
          <UButton
            color="primary"
            icon="i-heroicons-arrow-trending-up"
            :loading="loading"
            :disabled="!selectedTier"
            @click="handleConfirm"
          >
            {{ $t('billing.plan.modal.confirmUpgrade') }}
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
interface PlanTier {
  value: string;
  label: string;
  price: number;
  currency: string;
  billingCycle: 'MONTHLY' | 'ANNUAL';
  description: string;
  features?: string[];
}

interface Props {
  modelValue: boolean;
  currentTier: string;
  availableTiers: PlanTier[];
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  confirm: [tier: string, billingCycle: string];
}>();

const { t, locale } = useI18n();

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
});

const selectedTier = ref<string>('');
const billingCycle = ref<'MONTHLY' | 'ANNUAL'>('MONTHLY');

// Reset selection when modal opens
watch(isOpen, (newValue) => {
  if (newValue) {
    selectedTier.value = '';
    billingCycle.value = 'MONTHLY';
  }
});

function formatPrice(price: number, currency: string): string {
  try {
    return new Intl.NumberFormat(locale.value, {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(price);
  } catch (error) {
    return `${price} ${currency}`;
  }
}

function handleCancel() {
  isOpen.value = false;
}

function handleConfirm() {
  if (selectedTier.value) {
    emit('confirm', selectedTier.value, billingCycle.value);
  }
}
</script>
