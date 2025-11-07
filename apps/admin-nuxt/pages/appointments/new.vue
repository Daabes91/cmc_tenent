<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-violet-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-calendar-plus" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t("appointments.create.hero.title") }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t("appointments.create.hero.description") }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-arrow-left" 
              to="/appointments"
            >
              {{ t("common.actions.cancel") }}
            </UButton>
            <UButton 
              color="violet" 
              icon="i-lucide-save" 
              :loading="saving"
              @click="handleSave"
            >
              {{ t("appointments.create.actions.submit") }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-4xl mx-auto px-6 py-8">
      <UForm :state="form" @submit.prevent="handleSave">
        <div class="space-y-6">
          <!-- Booking Details Card -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-violet-500 to-indigo-600 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-calendar-check" class="h-5 w-5 text-white" />
                <div>
                  <h2 class="text-lg font-semibold text-white">{{ t("appointments.create.cards.booking.title") }}</h2>
                  <p class="text-sm text-violet-100">{{ t("appointments.create.cards.booking.subtitle") }}</p>
                </div>
              </div>
            </div>

            <div class="p-6">
              <div class="space-y-6">
                <div class="grid gap-6 md:grid-cols-2">
                  <UFormGroup :label="t('appointments.create.fields.patient')" required>
                    <USelect
                      v-model="form.patientId"
                      :options="patientOptions"
                      option-attribute="label"
                      value-attribute="value"
                      :placeholder="t('appointments.create.placeholders.selectPatient')"
                      size="lg"
                    />
                  </UFormGroup>
                  <UFormGroup :label="t('appointments.create.fields.service')" required>
                    <USelect
                      v-model="form.serviceId"
                      :options="serviceOptions"
                      option-attribute="label"
                      value-attribute="value"
                      :placeholder="t('appointments.create.placeholders.selectService')"
                      size="lg"
                    />
                  </UFormGroup>
                </div>

                <div class="grid gap-6 md:grid-cols-2">
                  <UFormGroup :label="t('appointments.create.fields.doctor')" required>
                    <USelect
                      v-model="form.doctorId"
                      :options="doctorOptions"
                      option-attribute="label"
                      value-attribute="value"
                      :placeholder="t('appointments.create.placeholders.selectDoctor')"
                      size="lg"
                    />
                    <template #help v-if="!doctorOptions.length">
                      <div class="flex items-center gap-2 text-amber-600">
                        <UIcon name="i-lucide-alert-triangle" class="h-3.5 w-3.5" />
                        <span>{{ t("appointments.create.messages.noDoctorForService") }}</span>
                      </div>
                    </template>
                  </UFormGroup>
                  <UFormGroup :label="t('appointments.create.fields.bookingMode')" required>
                    <USelect
                      v-model="form.bookingMode"
                      :options="bookingModeOptions"
                      option-attribute="label"
                      value-attribute="value"
                      size="lg"
                    />
                  </UFormGroup>
                </div>

                <UFormGroup :label="t('appointments.create.fields.date')" :hint="t('appointments.create.hints.clinicDate')" required>
                  <div class="relative">
                    <input
                      ref="dateInputRef"
                      v-model="form.date"
                      type="date"
                      :min="minDate"
                      class="block w-full rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 py-2.5 pl-11 pr-3 text-sm text-slate-900 dark:text-white transition-all duration-200 focus:border-violet-500 focus:outline-none focus:ring-2 focus:ring-violet-200"
                    />
                    <button
                      type="button"
                      class="absolute inset-y-0 start-0 flex items-center px-3 text-violet-600 dark:text-violet-400 transition hover:text-violet-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 focus-visible:ring-offset-1 focus-visible:ring-offset-white"
                      :aria-label="t('appointments.create.actions.openDatePicker')"
                      @click="openDatePicker"
                    >
                      <UIcon name="i-lucide-calendar" class="h-5 w-5" aria-hidden="true" />
                    </button>
                  </div>
                </UFormGroup>
                <UFormGroup :label="t('appointments.create.fields.slotDuration')" :hint="t('appointments.create.hints.slotDuration')">
                  <UInput
                    :model-value="form.slotDurationMinutes"
                    type="number"
                    min="5"
                    max="240"
                    step="5"
                    size="lg"
                    icon="i-lucide-timer"
                    disabled
                  />
                </UFormGroup>
              </div>
            </div>
          </div>

          <!-- Available Slots Card -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-blue-500 to-cyan-600 px-6 py-4">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <UIcon name="i-lucide-clock" class="h-5 w-5 text-white" />
                  <div>
                    <h2 class="text-lg font-semibold text-white">{{ t("appointments.create.cards.slots.title") }}</h2>
                    <p class="text-sm text-blue-100">
                      {{ canLoadSlots ? t("appointments.create.cards.slots.descriptionReady") : t("appointments.create.cards.slots.descriptionPending") }}
                    </p>
                  </div>
                </div>
                <UButton
                  variant="soft"
                  color="white"
                  size="sm"
                  icon="i-heroicons-arrow-path-20-solid"
                  :disabled="slotsLoading || !canLoadSlots"
                  :loading="slotsLoading"
                  @click="loadSlots"
                >
                  {{ t("common.actions.refresh") }}
                </UButton>
              </div>
            </div>

            <div class="p-6">

              <div class="space-y-4">
                <UAlert v-if="slotError" color="red" icon="i-lucide-alert-triangle" variant="soft">
                  <template #title>{{ t("appointments.create.cards.slots.errorTitle") }}</template>
                  <template #description>{{ slotError }}</template>
                </UAlert>

                <div v-if="slotsLoading" class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                  <USkeleton v-for="i in 8" :key="`slot-skeleton-${i}`" class="h-20 rounded-xl" />
                </div>

                <ClientOnly>
                  <div
                    v-if="canLoadSlots && !slotsLoading && !slotOptions.length"
                    class="flex flex-col items-center justify-center rounded-xl border-2 border-dashed border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-6 py-12 text-center"
                  >
                    <div class="mb-3 flex h-16 w-16 items-center justify-center rounded-full bg-slate-100 dark:bg-slate-700">
                      <UIcon name="i-lucide-calendar-x" class="h-8 w-8 text-slate-400" />
                    </div>
                    <h3 class="mb-1 text-sm font-semibold text-slate-900 dark:text-white">{{ t("appointments.create.cards.slots.emptyTitle") }}</h3>
                    <p class="text-sm text-slate-500 dark:text-slate-400">
                      {{ t("appointments.create.cards.slots.emptyDescription") }}
                    </p>
                  </div>

                  <div
                    v-else-if="slotOptions.length"
                    class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4"
                  >
                    <button
                      v-for="slot in slotOptions"
                      :key="slot.value"
                      type="button"
                      @click="selectSlot(slot.value)"
                      :class="[
                        'group relative overflow-hidden rounded-xl border-2 bg-white dark:bg-slate-700 px-4 py-4 text-left shadow-sm transition-all duration-200 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-violet-200 focus:ring-offset-2',
                        selectedSlot === slot.value
                          ? 'border-violet-500 bg-violet-50 dark:bg-violet-900/20 ring-2 ring-violet-200'
                          : 'border-slate-200 dark:border-slate-600 hover:border-violet-300 hover:bg-violet-50/50 dark:hover:bg-violet-900/10'
                      ]"
                    >
                      <div class="flex items-center justify-between">
                      <div>
                        <p class="text-base font-semibold text-slate-900 dark:text-white">{{ slot.label }}</p>
                        <p class="mt-0.5 text-xs text-slate-500 dark:text-slate-400">{{ slot.range }}</p>
                      </div>
                      <UIcon
                        v-if="selectedSlot === slot.value"
                        name="i-lucide-check"
                        class="h-5 w-5 text-violet-600 dark:text-violet-400"
                      />
                    </div>
                  </button>
                </div>
              </ClientOnly>
            </div>
          </div>
        </div>

          <!-- Internal Notes Card -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-amber-500 to-orange-600 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-file-text" class="h-5 w-5 text-white" />
                <div>
                  <h2 class="text-lg font-semibold text-white">{{ t("appointments.create.cards.notes.title") }}</h2>
                  <p class="text-sm text-amber-100">{{ t("appointments.create.cards.notes.subtitle") }}</p>
                </div>
              </div>
            </div>

            <div class="p-6">
              <UFormGroup :label="t('appointments.create.cards.notes.placeholder')">
                <UTextarea
                  v-model="form.notes"
                  :rows="4"
                  size="lg"
                  :placeholder="t('appointments.create.cards.notes.placeholder')"
                />
              </UFormGroup>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="flex justify-end gap-3 pt-6">
            <UButton
              variant="ghost"
              color="gray"
              :disabled="saving"
              @click="router.push('/appointments')"
            >
              {{ t("common.actions.cancel") }}
            </UButton>
            <UButton
              type="submit"
              color="violet"
              icon="i-lucide-calendar-plus"
              :loading="saving"
              :disabled="!form.patientId || !form.doctorId || !form.serviceId || !form.date || !selectedSlot"
            >
              {{ t("appointments.create.actions.submit") }}
            </UButton>
          </div>
        </div>
      </UForm>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AppointmentAdminDetail, AvailabilitySlot } from "@/types/appointments";
import type { PatientAdmin } from "@/types/patients";
import type { DoctorAdmin } from "@/types/doctors";
import type { AdminServiceSummary } from "@/types/services";
import { computed, watch, ref, reactive } from "vue";

const toast = useToast();
const router = useRouter();
const { t, locale } = useI18n();
const { fetcher, request } = useAdminApi();
const selectPaddingUi = {
  trailing: {
    padding: {
      sm: "pe-12",
      md: "pe-12",
      lg: "pe-12"
    }
  }
};
const dateInputRef = ref<HTMLInputElement | null>(null);
const bookingModeOptions = computed(() => [
  { value: "CLINIC_VISIT", label: t("appointments.modes.clinic") },
  { value: "VIRTUAL_CONSULTATION", label: t("appointments.modes.virtual") }
]);
const minDate = computed(() => formatDateForInput(new Date()));

useHead(() => ({
  title: t("appointments.create.meta.title")
}));

const { settings: clinicSettings } = useClinicSettings();
const defaultSlotDurationMinutes = computed(() => clinicSettings.value?.slotDurationMinutes ?? 30);

const { data: patientsData } = await useAsyncData("admin-appointment-patients", () =>
  fetcher<PatientAdmin[]>("/patients", [])
);
const { data: doctorsData } = await useAsyncData("admin-appointment-doctors", () =>
  fetcher<DoctorAdmin[]>("/doctors", [])
);
const { data: servicesData } = await useAsyncData("admin-appointment-services", () =>
  fetcher<AdminServiceSummary[]>("/services", [])
);

function extractItems<T>(value: any): T[] {
  if (Array.isArray(value)) {
    return value as T[];
  }
  if (value && Array.isArray((value as any).content)) {
    return (value as any).content as T[];
  }
  if (value && Array.isArray((value as any).data)) {
    return (value as any).data as T[];
  }
  return [];
}

const patients = computed(() => extractItems<PatientAdmin>(patientsData.value));
const doctors = computed(() => extractItems<DoctorAdmin>(doctorsData.value));
const services = computed(() => extractItems<AdminServiceSummary>(servicesData.value));

const patientOptions = computed(() =>
  patients.value.map(patient => ({
    label: `${patient.firstName} ${patient.lastName}`,
    value: patient.id.toString()
  }))
);

const form = reactive({
  patientId: null as string | null,
  doctorId: null as string | null,
  serviceId: null as string | null,
  bookingMode: "CLINIC_VISIT",
  date: "",
  slotDurationMinutes: defaultSlotDurationMinutes.value.toString(),
  notes: ""
});

const selectedSlot = ref<string>("");
const slotsLoading = ref(false);
const slotError = ref<string | null>(null);
const availabilitySlots = ref<AvailabilitySlot[]>([]);

const saving = ref(false);

const serviceOptions = computed(() =>
  services.value.map(service => ({
    label: service.nameEn ?? service.nameAr ?? service.slug,
    value: service.id.toString()
  }))
);

const refreshKey = ref(0);

const doctorOptions = computed(() => {
  const selectedService = form.serviceId ? Number(form.serviceId) : null;
  return doctors.value
    .filter(doctor =>
      selectedService ? doctor.services.some(service => Number(service.id) === selectedService) : true
    )
    .map(doctor => ({
      label: doctor.fullNameEn || doctor.fullNameAr || (doctor as any).fullName,
      value: doctor.id.toString()
    }));
});

if (import.meta.client) {
  watch(
    () => form.serviceId,
    serviceId => {
      if (!serviceId) {
        resetSlots();
        return;
      }
      const doctorMatchesService = doctorOptions.value.some(option => option.value === form.doctorId);
      if (!doctorMatchesService) {
        form.doctorId = null;
      }
    }
  );

  watch(
    [() => form.serviceId, () => form.doctorId, () => form.date, () => form.bookingMode, refreshKey],
    async ([serviceId, doctorId, date, bookingModeValue]) => {
      console.debug("[appointments:new] Watching form changes", { serviceId, doctorId, date, bookingMode: bookingModeValue }); // Debugging log
      await fetchSlots(serviceId ?? null, doctorId ?? null, date ?? "");
    },
    { immediate: true, flush: "post" }
  );
}

watch(
  () => form.date,
  (value) => {
    if (value && value < minDate.value) {
      form.date = minDate.value;
    }
  }
);

function openDatePicker() {
  const input = dateInputRef.value;
  if (!input) return;
  const picker = (input as HTMLInputElement & { showPicker?: () => void }).showPicker;
  if (typeof picker === "function") {
    picker.call(input);
    return;
  }
  input.focus();
  input.click();
}

function formatDateForInput(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

const serviceSlugById = computed(() => {
  const map = new Map<number, string>();
  services.value.forEach(service => map.set(Number(service.id), service.slug));
  return map;
});

const slotFormatter = computed(
  () =>
    new Intl.DateTimeFormat(locale.value, {
      hour: "numeric",
      minute: "2-digit"
    })
);

const slotOptions = computed(() => {
  console.debug("[appointments:new] Computing slot options from availabilitySlots", availabilitySlots.value); // Debugging log
  if (!availabilitySlots.value.length) {
    console.warn("[appointments:new] availabilitySlots is empty, no slot options to compute."); // Debugging log
  }
  return availabilitySlots.value.map(slot => {
    const start = new Date(slot.start);
    const end = new Date(slot.end);
    return {
      value: slot.start,
      label: slotFormatter.value.format(start),
      range: `${slotFormatter.value.format(start)} – ${slotFormatter.value.format(end)}`
    };
  });
});

watch(
  availabilitySlots,
  (newSlots) => {
    console.debug("[appointments:new] availabilitySlots updated", newSlots); // Debugging log
  },
  { deep: true }
);

watch(
  slotOptions,
  (newOptions) => {
    console.debug("[appointments:new] slotOptions updated", newOptions); // Debugging log
  },
  { deep: true }
);

const canLoadSlots = computed(() => Boolean(form.serviceId && form.doctorId && form.bookingMode && form.date));

function clearSlots() {
  availabilitySlots.value = [];
  selectedSlot.value = "";
  slotError.value = null;
  slotsLoading.value = false;
  form.slotDurationMinutes = defaultSlotDurationMinutes.value.toString();
}

function resetSlots(clearDate = true) {
  clearSlots();
  if (clearDate) {
    form.date = "";
  }
}

async function fetchSlots(serviceId: string | null, doctorIdValue: string | null, date: string) {
  if (import.meta.server) return;
  if (!serviceId || !doctorIdValue || !date) {
    clearSlots();
    return;
  }

  slotsLoading.value = true;
  slotError.value = null;
  console.debug("[appointments:new] Fetching slots with", { serviceId, doctorIdValue, date }); // Debugging log
  try {
    const serviceSlug = serviceSlugById.value.get(Number(serviceId));
    if (!serviceSlug) {
      throw new Error(t("common.errors.unexpected"));
    }

    const doctorId = Number(doctorIdValue);
    const response = await request<{
      data: AvailabilitySlot[];
    }>(`/doctors/${doctorId}/availability/slots`, {
      method: "POST",
      body: {
        serviceSlug,
        date
      }
    });

    console.debug("[appointments:new] Raw API response", response); // Log raw API response
    const slots = response?.data && Array.isArray(response.data) ? response.data : Array.isArray(response) ? response : [];
    console.debug("[appointments:new] Parsed slots from API before filtering", slots); // Debugging log

    if (!Array.isArray(slots)) {
      console.error("[appointments:new] API response is not an array", slots); // Debugging log
      throw new Error(t("common.errors.unexpected"));
    }

    // Validate and transform slots
    const transformedSlots = slots
      .filter(slot => {
        const isValid = typeof slot.start === "string" && typeof slot.end === "string";
        if (!isValid) {
          console.warn("[appointments:new] Slot is missing required fields or has invalid types", slot); // Debugging log
        }
        return isValid;
      })
      .map(slot => ({
        ...slot,
        start: new Date(slot.start).toISOString(),
        end: new Date(slot.end).toISOString()
      }));

    console.debug("[appointments:new] Transformed slots", transformedSlots); // Debugging log

    if (!transformedSlots.length) {
      console.warn("[appointments:new] No valid slots available after transformation."); // Debugging log
    }

    availabilitySlots.value = transformedSlots;
    console.debug("[appointments:new] Updated availabilitySlots", availabilitySlots.value); // Debugging log

    const previousSelection = selectedSlot.value;
    const existing = transformedSlots.find(slot => slot.start === previousSelection);
    selectedSlot.value = existing ? existing.start : transformedSlots.length ? transformedSlots[0].start : "";
    const initialSlot = availabilitySlots.value.find(slot => slot.start === selectedSlot.value);
    form.slotDurationMinutes = initialSlot
      ? computeSlotDurationMinutes(initialSlot.start, initialSlot.end).toString()
      : defaultSlotDurationMinutes.value.toString();
  } catch (error: any) {
    console.error("[appointments:new] Error fetching slots", error); // Debugging log
    slotError.value = error?.data?.message ?? error?.message ?? t("common.errors.unexpected");
    availabilitySlots.value = [];
  } finally {
    slotsLoading.value = false;
  }
}

function loadSlots() {
  if (import.meta.server) return;
  refreshKey.value += 1;
}

function computeSlotDurationMinutes(start: string, end: string) {
  const startMs = new Date(start).getTime();
  const endMs = new Date(end).getTime();
  if (!Number.isFinite(startMs) || !Number.isFinite(endMs) || endMs <= startMs) {
    return defaultSlotDurationMinutes.value;
  }
  const diff = Math.round((endMs - startMs) / 60000);
  return Math.min(Math.max(diff, 5), 240);
}

function selectSlot(value: string) {
  selectedSlot.value = value;
  const slot = availabilitySlots.value.find(slot => slot.start === value);
}

async function handleSave() {
  if (!form.patientId || !form.doctorId || !form.serviceId || !form.bookingMode || !form.date || !selectedSlot.value) {
    toast.add({
      title: t("appointments.create.toasts.missing.title"),
      description: t("appointments.create.toasts.missing.description"),
      color: "red"
    });
    return;
  }

  saving.value = true;

  const parsedSlotDuration = Number(form.slotDurationMinutes);
  const slotDurationMinutes = Number.isFinite(parsedSlotDuration)
    ? Math.min(Math.max(Math.round(parsedSlotDuration), 5), 240)
    : defaultSlotDurationMinutes.value;

  const payload = {
    patientId: Number(form.patientId),
    doctorId: Number(form.doctorId),
    serviceId: Number(form.serviceId),
    scheduledAt: selectedSlot.value,
    bookingMode: form.bookingMode,
    notes: form.notes.trim() || null,
    slotDurationMinutes,
    paymentCollected: false,
    patientAttended: null
  };

  try {
    const response = await request<AppointmentAdminDetail>("/appointments", {
      method: "POST",
      body: payload
    });

    toast.add({ title: t("appointments.create.toasts.createSuccess") });
    const createdId = response?.id;
    if (createdId) {
      router.push(`/appointments/${createdId}`);
    } else {
      router.push("/appointments");
    }
  } catch (error: any) {
    toast.add({
      title: t("appointments.create.toasts.createError.title"),
      description: error?.data?.message ?? error?.message ?? t("common.errors.unexpected"),
      color: "red"
    });
  } finally {
    saving.value = false;
  }
}
</script>
