<template>
  <UModal :model-value="modelValue" :ui="{ width: 'sm:max-w-2xl' }" @update:model-value="updateModelValue">
    <UCard>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-violet-100 text-violet-600">
            <UIcon name="i-lucide-calendar-plus" class="h-5 w-5" />
          </div>
          <div>
            <p class="text-base font-semibold text-slate-900 dark:text-white">{{ modalTitle }}</p>
            <p class="text-sm text-slate-500 dark:text-slate-300">{{ selectionSummary }}</p>
          </div>
        </div>
      </template>

      <div class="space-y-6">
        <div v-if="selection" class="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700 dark:border-slate-700 dark:bg-slate-900/40 dark:text-slate-200">
          <p class="font-semibold">{{ formattedDate }}</p>
          <p class="text-sm">{{ formattedTimeRange }}</p>
        </div>
        <div v-else class="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800 dark:border-amber-500/40 dark:bg-amber-500/10">
          {{ emptySelectionCopy }}
        </div>

        <UForm :state="form" class="space-y-4" @submit.prevent>
          <div class="grid gap-4 md:grid-cols-2">
            <UFormGroup label="Service" required>
              <USelect
                v-model="form.serviceId"
                :options="serviceOptions"
                option-attribute="label"
                value-attribute="value"
                placeholder="Select service"
                size="lg"
              />
            </UFormGroup>
            <UFormGroup label="Doctor" required>
              <USelect
                v-model="form.doctorId"
                :options="doctorOptions"
                option-attribute="label"
                value-attribute="value"
                placeholder="Assign doctor"
                size="lg"
              />
            </UFormGroup>
          </div>

          <UFormGroup label="Patient" required>
            <USelectMenu
              v-model="form.patientId"
              :options="patientOptions"
              option-attribute="label"
              value-attribute="value"
              placeholder="Select patient"
              searchable
              searchable-placeholder="Search patients"
              nullable
            />
          </UFormGroup>

          <div class="grid gap-4 md:grid-cols-2">
            <UFormGroup label="Start time" required>
              <UInput
                v-model="startTime"
                type="time"
                step="300"
                size="lg"
              />
            </UFormGroup>
            <UFormGroup label="End time" required>
              <UInput
                v-model="endTime"
                type="time"
                step="300"
                size="lg"
              />
            </UFormGroup>
          </div>

          <UFormGroup label="Notes">
            <UTextarea
              v-model="form.notes"
              rows="3"
              placeholder="Add context for the care team"
            />
          </UFormGroup>
        </UForm>
      </div>

      <template #footer>
        <div class="flex items-center justify-between gap-3">
          <div v-if="props.mode === 'edit' && props.defaults?.appointmentId">
            <UButton color="red" variant="soft" icon="i-lucide-trash" @click="handleDelete">
              Delete
            </UButton>
          </div>
          <div class="flex items-center gap-3 ms-auto">
            <UButton color="gray" variant="ghost" @click="closeModal">
              Cancel
            </UButton>
            <UButton color="violet" :disabled="!canSubmit || !selection" :loading="submitting" @click="handleSubmit">
              {{ submitLabel }}
            </UButton>
          </div>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import type { AdminServiceSummary } from '@/types/services';
import type { DoctorAdmin } from '@/types/doctors';
import type { PatientAdmin } from '@/types/patients';
import type { CalendarAppointmentPayload, CalendarAppointmentRequest } from '@/types/calendar';

type AppointmentModalMode = 'create' | 'edit';

type AppointmentModalDefaults = {
  appointmentId: number | null;
  serviceId: number | null;
  doctorId: number | null;
  patientId: number | null;
  notes: string;
};

type AppointmentModalSubmitPayload = CalendarAppointmentPayload & {
  start: Date;
  end: Date;
  appointmentId: number | null;
  mode: AppointmentModalMode;
};

interface CalendarAppointmentModalProps {
  modelValue: boolean;
  selection: CalendarAppointmentRequest | null;
  services: AdminServiceSummary[];
  doctors: DoctorAdmin[];
  patients: PatientAdmin[];
  submitting?: boolean;
  mode?: AppointmentModalMode;
  defaults?: AppointmentModalDefaults | null;
}

const props = withDefaults(defineProps<CalendarAppointmentModalProps>(), {
  services: () => [],
  doctors: () => [],
  patients: () => [],
  submitting: false,
  mode: 'create',
  defaults: null
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  submit: [payload: AppointmentModalSubmitPayload];
  delete: [appointmentId: number];
}>();

const { locale } = useI18n();

const form = ref({
  serviceId: null as number | null,
  doctorId: null as number | null,
  patientId: null as number | null,
  notes: ''
});

const startTime = ref('');
const endTime = ref('');

const initializeForm = () => {
  form.value = {
    serviceId: props.defaults?.serviceId ?? null,
    doctorId: props.defaults?.doctorId ?? null,
    patientId: props.defaults?.patientId ?? null,
    notes: props.defaults?.notes ?? ''
  };
};

const formatTimeInput = (date: Date | null) => {
  if (!date) return '';
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${hours}:${minutes}`;
};

const initializeTiming = () => {
  if (!props.selection) {
    startTime.value = '';
    endTime.value = '';
    return;
  }
  startTime.value = formatTimeInput(props.selection.start);
  endTime.value = formatTimeInput(props.selection.end);
};

watch(
  () => props.selection,
  () => {
    if (props.mode === 'create') {
      initializeForm();
    }
    initializeTiming();
  }
);

watch(
  () => [props.defaults, props.mode],
  () => {
    if (props.modelValue) {
      initializeForm();
      initializeTiming();
    }
  }
);

watch(
  () => props.modelValue,
  (isOpen) => {
    if (isOpen) {
      initializeForm();
      initializeTiming();
    }
  }
);

initializeForm();
initializeTiming();

const selectionSummary = computed(() => {
  if (!props.selection) return 'No time range selected';
  return `${formattedDate.value} · ${formattedTimeRange.value}`;
});

const modalTitle = computed(() =>
  props.mode === 'edit' ? 'Edit appointment' : 'Create appointment'
);

const submitLabel = computed(() =>
  props.mode === 'edit' ? 'Save changes' : 'Create appointment'
);

const emptySelectionCopy = computed(() =>
  props.mode === 'edit'
    ? 'Select a range in the calendar to edit this appointment.'
    : 'Select a range in the calendar to create an appointment.'
);

const formattedDate = computed(() => {
  if (!props.selection) return '';
  return new Intl.DateTimeFormat(locale.value, {
    weekday: 'long',
    month: 'long',
    day: 'numeric'
  }).format(props.selection.start);
});

const formattedTimeRange = computed(() => {
  if (!props.selection) return '';
  const startLabel = formatDisplayTime(startTime.value, props.selection.start);
  const endLabel = formatDisplayTime(endTime.value, props.selection.end);
  return `${startLabel} – ${endLabel}`;
});

const serviceOptions = computed(() => props.services.map((service) => ({
  value: service.id,
  label: service.nameEn || service.nameAr || `Service #${service.id}`
})));

const doctorOptions = computed(() => props.doctors.map((doctor) => ({
  value: doctor.id,
  label: doctor.fullNameEn || doctor.fullNameAr || `Doctor #${doctor.id}`
})));

const patientOptions = computed(() => props.patients.map((patient) => ({
  value: patient.id,
  label: `${patient.firstName} ${patient.lastName}`.trim() || `Patient #${patient.id}`
})));

const canSubmit = computed(() => {
  if (!props.selection) return false;
  return Boolean(form.value.serviceId && form.value.doctorId && form.value.patientId && startTime.value && endTime.value);
});

const formatTime = (date: Date) =>
  new Intl.DateTimeFormat(locale.value, { hour: 'numeric', minute: '2-digit' }).format(date);

const formatDisplayTime = (input: string, fallback: Date) => {
  if (!input) return formatTime(fallback);
  const [hours, minutes] = input.split(':').map((value) => Number(value));
  if (Number.isNaN(hours) || Number.isNaN(minutes)) {
    return formatTime(fallback);
  }
  const next = new Date(fallback);
  next.setHours(hours, minutes, 0, 0);
  return formatTime(next);
};

const closeModal = () => emit('update:modelValue', false);
const updateModelValue = (value: boolean) => emit('update:modelValue', value);

const parseTimeToDate = (base: Date, timeString: string): Date => {
  const [hours, minutes] = timeString.split(':').map((value) => Number(value));
  if (Number.isNaN(hours) || Number.isNaN(minutes)) {
    return new Date(base);
  }
  const next = new Date(base);
  next.setHours(hours, minutes, 0, 0);
  return next;
};

const ensureValidRange = (startDate: Date, endDate: Date): Date => {
  if (endDate.getTime() <= startDate.getTime()) {
    return new Date(startDate.getTime() + 30 * 60 * 1000);
  }
  return endDate;
};

const handleSubmit = () => {
  if (!props.selection || !canSubmit.value) return;
  const baseDate = props.selection.start ?? new Date();
  const startDate = parseTimeToDate(baseDate, startTime.value);
  const endDate = ensureValidRange(startDate, parseTimeToDate(baseDate, endTime.value));
  emit('submit', {
    start: startDate,
    end: endDate,
    date: startDate.toISOString().split('T')[0],
    startTime: formatTime(startDate),
    endTime: formatTime(endDate),
    serviceId: form.value.serviceId,
    doctorId: form.value.doctorId,
    patientId: form.value.patientId,
    notes: form.value.notes,
    appointmentId: props.defaults?.appointmentId ?? null,
    mode: props.mode ?? 'create'
  });
};

const handleDelete = () => {
  if (!props.defaults?.appointmentId) return;
  emit('delete', props.defaults.appointmentId);
};
</script>
