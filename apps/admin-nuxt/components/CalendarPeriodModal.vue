<template>
  <UModal :model-value="modelValue" :ui="{ width: 'sm:max-w-lg' }" @update:model-value="updateModelValue">
    <UCard>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-100 text-blue-600">
            <UIcon name="i-lucide-calendar-range" class="h-5 w-5" />
          </div>
          <div>
            <p class="text-base font-semibold text-slate-900 dark:text-white">Create calendar period</p>
            <p class="text-sm text-slate-500 dark:text-slate-300">{{ rangeSummary }}</p>
          </div>
        </div>
      </template>

      <div class="space-y-5">
        <div v-if="selection" class="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700 dark:border-slate-700 dark:bg-slate-900/40 dark:text-slate-200">
          <p class="font-semibold">{{ rangeSummary }}</p>
          <p class="text-sm">{{ totalDaysLabel }}</p>
        </div>
        <div v-else class="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800 dark:border-amber-500/40 dark:bg-amber-500/10">
          Select one or multiple days to create a period.
        </div>

        <UForm :state="form" class="space-y-4" @submit.prevent>
          <UFormGroup label="Type" required>
            <USelect
              v-model="form.type"
              :options="periodTypeOptions"
              option-attribute="label"
              value-attribute="value"
              placeholder="Choose a category"
            />
          </UFormGroup>
          <UFormGroup label="Notes">
            <UTextarea v-model="form.notes" rows="3" placeholder="Optional description" />
          </UFormGroup>
        </UForm>
      </div>

      <template #footer>
        <div class="flex items-center justify-end gap-3">
          <UButton color="gray" variant="ghost" @click="closeModal">
            Cancel
          </UButton>
          <UButton color="blue" :disabled="!canSubmit" :loading="submitting" @click="handleSubmit">
            Save period
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import type { CalendarPeriodPayload } from '@/types/calendar';

type PeriodSelection = { start: Date; end: Date };
type PeriodModalPayload = CalendarPeriodPayload & PeriodSelection;

interface CalendarPeriodModalProps {
  modelValue: boolean;
  selection: PeriodSelection | null;
  submitting?: boolean;
}

const props = withDefaults(defineProps<CalendarPeriodModalProps>(), {
  submitting: false
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  submit: [payload: PeriodModalPayload];
}>();

const { locale } = useI18n();

const form = ref({
  type: null as string | null,
  notes: ''
});

watch(
  () => props.selection,
  () => {
    form.value = { type: null, notes: '' };
  }
);

const periodTypeOptions = [
  { value: 'Clinic closed', label: 'Clinic closed' },
  { value: 'Doctor vacation', label: 'Doctor vacation' },
  { value: 'Campaign', label: 'Campaign' }
];

const rangeSummary = computed(() => {
  if (!props.selection) return 'No range selected';
  const start = formatDate(props.selection.start);
  const end = formatDate(props.selection.end);
  return start === end ? start : `${start} → ${end}`;
});

const totalDaysLabel = computed(() => {
  if (!props.selection) return '';
  const diff = Math.abs(
    Math.round((startOfDay(props.selection.end).getTime() - startOfDay(props.selection.start).getTime()) / (1000 * 60 * 60 * 24))
  ) + 1;
  return `${diff} day${diff > 1 ? 's' : ''}`;
});

const formatDate = (date: Date) =>
  new Intl.DateTimeFormat(locale.value, { month: 'long', day: 'numeric', year: 'numeric' }).format(date);

const startOfDay = (date: Date) => {
  const copy = new Date(date);
  copy.setHours(0, 0, 0, 0);
  return copy;
};

const canSubmit = computed(() => Boolean(props.selection && form.value.type));

const closeModal = () => emit('update:modelValue', false);
const updateModelValue = (value: boolean) => emit('update:modelValue', value);

const handleSubmit = () => {
  if (!props.selection || !canSubmit.value || !form.value.type) return;
  const startDate = props.selection.start.toISOString().split('T')[0];
  const endDate = props.selection.end.toISOString().split('T')[0];
  emit('submit', {
    start: props.selection.start,
    end: props.selection.end,
    startDate,
    endDate,
    type: form.value.type,
    notes: form.value.notes
  });
};
</script>
