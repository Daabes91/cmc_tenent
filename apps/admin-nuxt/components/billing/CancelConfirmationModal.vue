<template>
  <UModal v-model="isOpen" prevent-close :ui="{ width: 'max-w-xl' }">
    <UCard>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-full bg-red-100 dark:bg-red-900/30">
            <UIcon name="i-heroicons-shield-exclamation" class="h-5 w-5 text-red-600 dark:text-red-400" />
          </div>
          <div>
            <p class="text-lg font-semibold text-gray-900 dark:text-white">
              {{ $t('billing.plan.modal.cancelTitle') }}
            </p>
            <p class="text-sm text-gray-500 dark:text-gray-400">
              {{ $t('billing.plan.modal.cancelSubtitle') }}
            </p>
          </div>
        </div>
      </template>

      <div class="space-y-4">
        <!-- Warning Alert -->
        <div class="rounded-lg border border-amber-200 dark:border-amber-800 bg-amber-50 dark:bg-amber-900/20 p-4">
          <div class="flex items-start gap-3">
            <UIcon name="i-heroicons-exclamation-triangle" class="h-5 w-5 text-amber-600 dark:text-amber-400 flex-shrink-0 mt-0.5" />
            <div class="flex-1">
              <p class="text-sm font-medium text-amber-900 dark:text-amber-100 mb-1">
                {{ $t('billing.plan.modal.warningTitle') }}
              </p>
              <p class="text-sm text-amber-700 dark:text-amber-300">
                {{ $t('billing.plan.modal.warningMessage', { date: effectiveDate }) }}
              </p>
            </div>
          </div>
        </div>

        <!-- Cancellation Type Selection -->
        <div class="space-y-3">
          <p class="text-sm font-medium text-gray-700 dark:text-gray-300">
            {{ $t('billing.plan.modal.whenToCancel') }}
          </p>
          
          <div class="space-y-2">
            <button
              type="button"
              class="w-full flex items-start gap-3 rounded-lg border-2 p-4 text-left transition-all"
              :class="!immediate
                ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                : 'border-gray-200 dark:border-gray-700 hover:border-blue-300'"
              @click="immediate = false"
            >
              <input
                type="radio"
                :checked="!immediate"
                class="mt-0.5 h-4 w-4 text-blue-600 focus:ring-blue-500"
              />
              <div class="flex-1">
                <p class="font-semibold text-gray-900 dark:text-white">
                  {{ $t('billing.plan.modal.endOfPeriod') }}
                </p>
                <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                  {{ $t('billing.plan.modal.endOfPeriodDescription', { date: effectiveDate }) }}
                </p>
                <UBadge color="green" size="xs" class="mt-2">
                  {{ $t('billing.plan.modal.recommended') }}
                </UBadge>
              </div>
            </button>

            <button
              type="button"
              class="w-full flex items-start gap-3 rounded-lg border-2 p-4 text-left transition-all"
              :class="immediate
                ? 'border-red-500 bg-red-50 dark:bg-red-900/20'
                : 'border-gray-200 dark:border-gray-700 hover:border-red-300'"
              @click="immediate = true"
            >
              <input
                type="radio"
                :checked="immediate"
                class="mt-0.5 h-4 w-4 text-red-600 focus:ring-red-500"
              />
              <div class="flex-1">
                <p class="font-semibold text-gray-900 dark:text-white">
                  {{ $t('billing.plan.modal.immediately') }}
                </p>
                <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                  {{ $t('billing.plan.modal.immediatelyDescription') }}
                </p>
              </div>
            </button>
          </div>
        </div>

        <!-- Cancellation Reason (Optional) -->
        <div class="space-y-2">
          <label class="text-sm font-medium text-gray-700 dark:text-gray-300">
            {{ $t('billing.plan.modal.reasonLabel') }}
            <span class="text-gray-500 dark:text-gray-400 font-normal">
              ({{ $t('billing.plan.modal.optional') }})
            </span>
          </label>
          <UTextarea
            v-model="reason"
            :placeholder="$t('billing.plan.modal.reasonPlaceholder')"
            :rows="3"
          />
        </div>

        <!-- What Happens Next -->
        <div class="rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800/50 p-4">
          <p class="text-sm font-medium text-gray-900 dark:text-white mb-2">
            {{ $t('billing.plan.modal.whatHappensNext') }}
          </p>
          <ul class="space-y-2 text-sm text-gray-600 dark:text-gray-400">
            <li class="flex items-start gap-2">
              <UIcon name="i-heroicons-check-circle" class="h-4 w-4 text-gray-400 mt-0.5 flex-shrink-0" />
              <span>{{ $t('billing.plan.modal.point1') }}</span>
            </li>
            <li class="flex items-start gap-2">
              <UIcon name="i-heroicons-check-circle" class="h-4 w-4 text-gray-400 mt-0.5 flex-shrink-0" />
              <span>{{ $t('billing.plan.modal.point2') }}</span>
            </li>
            <li class="flex items-start gap-2">
              <UIcon name="i-heroicons-check-circle" class="h-4 w-4 text-gray-400 mt-0.5 flex-shrink-0" />
              <span>{{ $t('billing.plan.modal.point3') }}</span>
            </li>
          </ul>
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
            {{ $t('billing.plan.modal.keepSubscription') }}
          </UButton>
          <UButton
            color="red"
            icon="i-heroicons-x-circle"
            :loading="loading"
            @click="handleConfirm"
          >
            {{ $t('billing.plan.modal.confirmCancel') }}
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
interface Props {
  modelValue: boolean;
  effectiveDate: string;
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  confirm: [immediate: boolean, reason?: string];
}>();

const { t } = useI18n();

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
});

const immediate = ref(false);
const reason = ref('');

// Reset form when modal opens
watch(isOpen, (newValue) => {
  if (newValue) {
    immediate.value = false;
    reason.value = '';
  }
});

function handleCancel() {
  isOpen.value = false;
}

function handleConfirm() {
  emit('confirm', immediate.value, reason.value || undefined);
}
</script>
