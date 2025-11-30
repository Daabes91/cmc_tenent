<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-mint-500 to-mint-300 shadow-lg">
              <UIcon name="i-lucide-calendar-days" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t("calendar.title") }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t("calendar.subtitle") }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="pending"
              @click="refresh"
            >
              {{ t("common.actions.refresh") }}
            </UButton>
            <UButton 
              color="violet" 
              icon="i-lucide-plus" 
              @click="navigateTo('/appointments/new')"
            >
              {{ t("appointments.list.actions.new") }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">
      <!-- Calendar Navigation -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden mb-6">
        <CalendarNavigation
          :current-date="currentDate"
          :view-mode="viewMode"
          :subtitle="t('calendar.subtitle')"
          :mobile="isMobile"
          @date-change="handleDateChange"
          @today-click="goToToday"
          @view-mode-change="handleViewModeChange"
        />
      </div>

      <!-- Filters -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden mb-6 p-4">
        <CalendarDoctorFilter
          v-model="selectedDoctorId"
          :doctors="doctors"
          :loading="loadingDoctors"
          :mobile="isMobile"
          @filter-change="handleDoctorFilterChange"
        />
      </div>

      <!-- Calendar Grid -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <!-- Loading State -->
        <div v-if="pending" class="p-8">
          <div class="grid grid-cols-7 gap-4">
            <div v-for="i in 42" :key="i" class="aspect-square">
              <USkeleton class="h-full w-full rounded-lg" />
            </div>
          </div>
        </div>

        <!-- Calendar Content -->
        <div v-else class="p-6">
          <!-- Month View -->
          <CalendarGrid
            v-if="viewMode === 'month'"
            :current-date="currentDate"
            :appointments="appointments"
            @date-click="selectDate"
          @appointment-click="handleExistingAppointmentClick"
            @create-period="handlePeriodSelection"
            :periods="periods"
          />
          
          <!-- Week View -->
          <CalendarWeekView
            v-else-if="viewMode === 'week'"
            :current-date="currentDate"
            :appointments="appointments"
            @date-click="selectDate"
          @appointment-click="handleExistingAppointmentClick"
            @create-appointment="handleAppointmentSelection"
          />
          
          <!-- Day View -->
          <CalendarDayView
            v-else-if="viewMode === 'day'"
            :current-date="currentDate"
            :appointments="appointments"
            @create-appointment="handleAppointmentSelection"
          @appointment-click="handleExistingAppointmentClick"
          />
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!pending && !appointments.length" class="mt-8 rounded-2xl bg-white dark:bg-slate-800 p-12 text-center shadow-sm border border-slate-200/60 dark:border-slate-700/60">
        <div class="mx-auto max-w-md">
          <div class="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-slate-100 dark:bg-slate-700">
            <UIcon name="i-lucide-calendar-x" class="h-10 w-10 text-slate-400" />
          </div>
          <h3 class="mt-6 text-xl font-semibold text-slate-900 dark:text-white">
            No appointments this month
          </h3>
          <p class="mt-2 text-sm text-slate-600 dark:text-slate-300">
            Get started by creating your first appointment for this period.
          </p>
          <div class="mt-6">
            <UButton color="violet" @click="navigateTo('/appointments/new')">
              <UIcon name="i-lucide-plus" class="mr-2 h-4 w-4" />
              New Appointment
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Day Modal for Multiple Appointments -->
    <CalendarAppointmentModal
      v-model="showAppointmentModal"
      :selection="appointmentSelection"
      :services="services"
      :doctors="doctors"
      :patients="patients"
      :mode="appointmentModalMode"
      :defaults="appointmentFormDefaults"
      :submitting="creatingAppointment"
      @submit="handleAppointmentSubmit"
      @delete="handleAppointmentDelete"
    />

    <CalendarPeriodModal
      v-model="showPeriodModal"
      :selection="periodSelection"
      :submitting="creatingPeriod"
      @submit="handlePeriodSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import type { AppointmentAdmin, AppointmentAdminDetail } from "@/types/appointments";
import type { DoctorAdmin } from "@/types/doctors";
import type { AdminServiceSummary } from "@/types/services";
import type { PatientAdmin } from "@/types/patients";
import type { CalendarAppointmentRequest, CalendarAppointmentPayload, CalendarPeriod, CalendarPeriodPayload } from "@/types/calendar";
import { computed, ref, watch, onMounted, onUnmounted } from "vue";
// Remove the explicit import since useAdminApi should be auto-imported

// Meta and SEO
definePageMeta({
  title: "Calendar"
});

useHead({
  title: "Calendar – Clinic Admin"
});

// Composables
const { t, locale } = useI18n();
const { request: adminRequest, fetcher } = useAdminApi();
const toast = useToast();
const router = useRouter();
const navigateTo = (path: string) => router.push(path);

// Reactive state
const currentDate = ref(new Date());
const selectedDate = ref<Date | null>(null);
const viewMode = ref<'month' | 'week' | 'day'>('month');
const appointments = ref<AppointmentAdmin[]>([]);
const allAppointments = ref<AppointmentAdmin[]>([]); // Store all appointments before filtering
const pending = ref(false);
const selectedDoctorId = ref<number | null>(null);

type MaybePaginated<T> = T[] | { content: T[] } | { data: T[] };

// Fetch doctors for filtering
const { data: doctorsData, pending: loadingDoctors } = await useAsyncData<MaybePaginated<DoctorAdmin>>(
  'calendar-doctors',
  () => fetcher<MaybePaginated<DoctorAdmin>>('/doctors?size=250', [])
);

const { data: servicesData } = await useAsyncData<MaybePaginated<AdminServiceSummary>>(
  'calendar-services',
  () => fetcher<MaybePaginated<AdminServiceSummary>>('/services?size=250', [])
);

const { data: patientsData } = await useAsyncData<MaybePaginated<PatientAdmin>>(
  'calendar-patients',
  () => fetcher<MaybePaginated<PatientAdmin>>('/patients?size=250', [])
);

const { data: periodData } = await useAsyncData(
  'calendar-periods',
  () => fetcher<CalendarPeriod[]>('/calendar/periods', [])
);

const extractItems = <T extends Record<string, any>>(value: any): T[] => {
  if (!value) return [];
  if (Array.isArray(value)) return value as T[];
  if (Array.isArray(value?.content)) return value.content as T[];
  if (Array.isArray(value?.data)) return value.data as T[];
  return [];
};

const doctors = computed(() => extractItems<DoctorAdmin>(doctorsData.value));
const services = computed(() => extractItems<AdminServiceSummary>(servicesData.value));
const patients = computed(() => extractItems<PatientAdmin>(patientsData.value));

const periods = ref<CalendarPeriod[]>(periodData.value ?? []);

watch(
  () => periodData.value,
  (value) => {
    if (value) {
      periods.value = value;
    }
  }
);

// Calendar page specific logic

// Mobile detection
const isMobile = ref(false);

// Filter appointments by selected doctor
const filteredAppointments = computed(() => {
  if (!selectedDoctorId.value) {
    return allAppointments.value;
  }
  
  return allAppointments.value.filter(appointment => {
    // Match by doctor name since we don't have doctorId in the appointment object
    const selectedDoctor = doctors.value.find(d => d.id === selectedDoctorId.value);
    if (!selectedDoctor) return false;
    
    const selectedDoctorName = selectedDoctor.fullNameEn || selectedDoctor.fullNameAr || selectedDoctor.fullName;
    return appointment.doctorName === selectedDoctorName;
  });
});

// Update appointments to use filtered appointments
watch(filteredAppointments, (newAppointments) => {
  appointments.value = newAppointments;
}, { immediate: true });

type AppointmentFormDefaults = {
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
  mode: "create" | "edit";
};
type PeriodModalSubmitPayload = CalendarPeriodPayload & { start: Date; end: Date };

const appointmentSelection = ref<CalendarAppointmentRequest | null>(null);
const showAppointmentModal = ref(false);
const creatingAppointment = ref(false);
const appointmentModalMode = ref<"create" | "edit">("create");
const defaultAppointmentForm = (): AppointmentFormDefaults => ({
  appointmentId: null,
  serviceId: null,
  doctorId: null,
  patientId: null,
  notes: ""
});
const appointmentFormDefaults = ref<AppointmentFormDefaults>(defaultAppointmentForm());
const resetAppointmentModalState = () => {
  showAppointmentModal.value = false;
  appointmentSelection.value = null;
  appointmentModalMode.value = "create";
  appointmentFormDefaults.value = defaultAppointmentForm();
};

const periodSelection = ref<{ start: Date; end: Date } | null>(null);
const showPeriodModal = ref(false);
const creatingPeriod = ref(false);

// Navigation functions
const handleDateChange = (newDate: Date) => {
  currentDate.value = newDate;
};

const goToToday = () => {
  currentDate.value = new Date();
  selectedDate.value = new Date();
};

const handleViewModeChange = (mode: 'month' | 'week' | 'day') => {
  viewMode.value = mode;
  // Store view mode preference in session storage
  if (process.client) {
    sessionStorage.setItem('calendar-view-mode', mode);
  }
};

const handleAppointmentSelection = (range: CalendarAppointmentRequest) => {
  appointmentModalMode.value = "create";
  appointmentFormDefaults.value = defaultAppointmentForm();
  appointmentSelection.value = range;
  showAppointmentModal.value = true;
};

const handlePeriodSelection = (range: { start: Date; end: Date }) => {
  periodSelection.value = range;
  showPeriodModal.value = true;
};

const selectDate = (date: Date) => {
  selectedDate.value = date;
};

const handleExistingAppointmentClick = async (appointment: AppointmentAdmin) => {
  try {
    const detail = await adminRequest<AppointmentAdminDetail>(`/appointments/${appointment.id}`);
    const start = detail?.scheduledAt
      ? new Date(detail.scheduledAt)
      : new Date(appointment.scheduledAt * 1000);
    const duration = detail?.slotDurationMinutes ?? appointment.slotDurationMinutes ?? 60;
    const end = new Date(start.getTime() + duration * 60 * 1000);
    appointmentSelection.value = { start, end };
    appointmentModalMode.value = "edit";
    appointmentFormDefaults.value = {
      appointmentId: detail?.id ?? appointment.id,
      serviceId: detail?.service?.id ?? null,
      doctorId: detail?.doctor?.id ?? null,
      patientId: detail?.patient?.id ?? null,
      notes: detail?.notes ?? ""
    };
    showAppointmentModal.value = true;
  } catch (error: any) {
    toast.add({
      title: "Unable to load appointment",
      description: error?.data?.message ?? error?.message ?? "Please try again later.",
      color: "red"
    });
  }
};

const handleDoctorFilterChange = (doctorId: number | null) => {
  selectedDoctorId.value = doctorId;
  // Store filter preference in session storage
  if (process.client) {
    if (doctorId) {
      sessionStorage.setItem('calendar-doctor-filter', doctorId.toString());
    } else {
      sessionStorage.removeItem('calendar-doctor-filter');
    }
  }
};

const getServiceNameById = (serviceId: number | null) => {
  if (!serviceId) return "Service";
  const service = services.value.find((item) => item.id === serviceId);
  return service?.nameEn || service?.nameAr || `Service #${serviceId}`;
};

const getDoctorNameById = (doctorId: number | null) => {
  if (!doctorId) return "Doctor";
  const doctor = doctors.value.find((item) => item.id === doctorId);
  return doctor?.fullNameEn || doctor?.fullNameAr || `Doctor #${doctorId}`;
};

const getPatientNameById = (patientId: number | null) => {
  if (!patientId) return "Patient";
  const patient = patients.value.find((item) => item.id === patientId);
  if (!patient) return `Patient #${patientId}`;
  const fullName = `${patient.firstName ?? ""} ${patient.lastName ?? ""}`.trim();
  return fullName || `Patient #${patientId}`;
};

const createOptimisticAppointment = (
  payload: AppointmentModalSubmitPayload,
  slotDurationMinutes: number,
  tempId: number
): AppointmentAdmin => ({
  id: tempId,
  patientName: getPatientNameById(payload.patientId),
  doctorName: getDoctorNameById(payload.doctorId),
  serviceName: getServiceNameById(payload.serviceId),
  scheduledAt: Math.floor(payload.start.getTime() / 1000),
  status: "scheduled",
  bookingMode: "CLINIC_VISIT",
  slotDurationMinutes,
  paymentCollected: false,
  patientAttended: null,
  treatmentPlanId: null,
  followUpVisitNumber: null,
  paymentAmount: null,
  paymentMethod: null,
  paymentCurrency: null
});

const normalizeAppointmentDetail = (
  detail: AppointmentAdminDetail | null,
  fallback: AppointmentAdmin
): AppointmentAdmin => {
  if (!detail) return fallback;
  const scheduledAtDate = detail.scheduledAt ? new Date(detail.scheduledAt) : new Date(fallback.scheduledAt * 1000);
  return {
    id: detail.id ?? fallback.id,
    patientName: detail.patient?.name ?? fallback.patientName,
    doctorName: detail.doctor?.name ?? fallback.doctorName,
    serviceName: detail.service?.name ?? fallback.serviceName,
    scheduledAt: Math.floor(scheduledAtDate.getTime() / 1000),
    status: detail.status ?? fallback.status,
    bookingMode: detail.bookingMode ?? fallback.bookingMode,
    slotDurationMinutes: detail.slotDurationMinutes ?? fallback.slotDurationMinutes,
    treatmentPlanId: detail.treatmentPlanId ?? fallback.treatmentPlanId ?? null,
    followUpVisitNumber: detail.followUpVisitNumber ?? fallback.followUpVisitNumber ?? null,
    paymentCollected: detail.paymentCollected ?? fallback.paymentCollected ?? false,
    patientAttended: detail.patientAttended ?? fallback.patientAttended ?? null,
    paymentAmount: detail.paymentAmount ?? fallback.paymentAmount ?? null,
    paymentMethod: detail.paymentMethod ?? fallback.paymentMethod ?? null,
    paymentCurrency: detail.paymentCurrency ?? fallback.paymentCurrency ?? null
  };
};

const replaceAppointmentInState = (tempId: number, replacement: AppointmentAdmin | null) => {
  const withoutTemp = allAppointments.value.filter((appointment) => appointment.id !== tempId);
  allAppointments.value = replacement ? [...withoutTemp, replacement] : withoutTemp;
};

const appendAppointmentToState = (appointment: AppointmentAdmin) => {
  allAppointments.value = [...allAppointments.value, appointment];
};

const replacePeriodInState = (tempId: string, replacement: CalendarPeriod | null) => {
  const withoutTemp = periods.value.filter((period) => period.id !== tempId);
  periods.value = replacement ? [...withoutTemp, replacement] : withoutTemp;
};

const normalizePeriod = (period: CalendarPeriod, fallbackId: string): CalendarPeriod => ({
  ...period,
  id: period.id ?? fallbackId
});

const handleAppointmentSubmit = async (payload: AppointmentModalSubmitPayload) => {
  if (!payload.patientId || !payload.doctorId || !payload.serviceId) {
    toast.add({
      title: "Missing details",
      description: "Please select patient, doctor, and service before scheduling.",
      color: "red"
    });
    return;
  }

  creatingAppointment.value = true;
  const slotDurationMinutes = Math.max(
    30,
    Math.round((payload.end.getTime() - payload.start.getTime()) / (1000 * 60))
  );

  const body = {
    patientId: payload.patientId,
    doctorId: payload.doctorId,
    serviceId: payload.serviceId,
    scheduledAt: payload.start.toISOString(),
    slotDurationMinutes,
    bookingMode: "CLINIC_VISIT",
    notes: payload.notes?.trim() || null
  };

  if (payload.mode === "edit" && payload.appointmentId) {
    try {
      await adminRequest(`/appointments/${payload.appointmentId}`, {
        method: "PUT",
        body
      });

      const updatedDetail = await adminRequest<AppointmentAdminDetail>(`/appointments/${payload.appointmentId}`);
      const fallbackAppointment =
        allAppointments.value.find((appointment) => appointment.id === payload.appointmentId) ??
        createOptimisticAppointment(payload, slotDurationMinutes, payload.appointmentId);
      const normalizedAppointment = normalizeAppointmentDetail(updatedDetail, fallbackAppointment);
      replaceAppointmentInState(payload.appointmentId, normalizedAppointment);
      toast.add({
        title: "Appointment updated",
        description: `${normalizedAppointment.patientName} · ${payload.startTime} – ${payload.endTime}`,
        color: "green"
      });
    } catch (error: any) {
      toast.add({
        title: "Unable to update appointment",
        description: error?.message ?? "Please try again later.",
        color: "red"
      });
    } finally {
      creatingAppointment.value = false;
      resetAppointmentModalState();
    }
    return;
  }

  const tempId = Date.now();
  const optimisticAppointment = createOptimisticAppointment(payload, slotDurationMinutes, tempId);
  appendAppointmentToState(optimisticAppointment);

  try {
    const response = await adminRequest<AppointmentAdminDetail>("/appointments", {
      method: "POST",
      body
    });

    const normalizedAppointment = normalizeAppointmentDetail(response, optimisticAppointment);
    replaceAppointmentInState(tempId, normalizedAppointment);
    toast.add({
      title: "Appointment scheduled",
      description: `${normalizedAppointment.patientName} · ${payload.startTime} – ${payload.endTime}`,
      color: "green"
    });
  } catch (error: any) {
    replaceAppointmentInState(tempId, null);
    toast.add({
      title: "Unable to schedule appointment",
      description: error?.message ?? "Please try again later.",
      color: "red"
    });
  } finally {
    creatingAppointment.value = false;
    resetAppointmentModalState();
  }
};

const handleAppointmentDelete = async (appointmentId: number | null) => {
  if (!appointmentId) return;
  creatingAppointment.value = true;
  try {
    await adminRequest(`/appointments/${appointmentId}`, { method: "DELETE" });
    replaceAppointmentInState(appointmentId, null);
    toast.add({
      title: "Appointment deleted",
      description: "The appointment has been removed from the calendar.",
      color: "green"
    });
  } catch (error: any) {
    toast.add({
      title: "Unable to delete appointment",
      description: error?.data?.message ?? error?.message ?? "Please try again later.",
      color: "red"
    });
  } finally {
    creatingAppointment.value = false;
    resetAppointmentModalState();
  }
};

const handlePeriodSubmit = async (payload: PeriodModalSubmitPayload) => {
  creatingPeriod.value = true;
  const tempId = `temp-${Date.now()}`;
  const optimisticPeriod: CalendarPeriod = {
    id: tempId,
    startDate: payload.startDate,
    endDate: payload.endDate,
    type: payload.type,
    notes: payload.notes
  };
  periods.value = [...periods.value, optimisticPeriod];

  try {
    const createdPeriod = await adminRequest<CalendarPeriod>("/calendar/periods", {
      method: "POST",
      body: {
        startDate: payload.startDate,
        endDate: payload.endDate,
        type: payload.type,
        notes: payload.notes
      }
    });

    const normalizedPeriod = createdPeriod ? normalizePeriod(createdPeriod, tempId) : optimisticPeriod;
    replacePeriodInState(tempId, normalizedPeriod);
    toast.add({
      title: "Period saved",
      description: `${payload.type} · ${payload.startDate} → ${payload.endDate}`,
      color: "blue"
    });
  } catch (error: any) {
    replacePeriodInState(tempId, null);
    toast.add({
      title: "Unable to save period",
      description: error?.message ?? "Please try again later.",
      color: "red"
    });
  } finally {
    creatingPeriod.value = false;
    showPeriodModal.value = false;
    periodSelection.value = null;
  }
};

// Data fetching with caching
const appointmentCache = new Map<string, { data: AppointmentAdmin[]; timestamp: number }>();
const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

const getCacheKey = (startDate: Date, endDate: Date): string => {
  return `${startDate.toISOString().split('T')[0]}_${endDate.toISOString().split('T')[0]}`;
};

const fetchAppointments = async (force = false) => {
  try {
    pending.value = true;
    
    // Calculate date range based on view mode
    let startDate: Date;
    let endDate: Date;
    
    switch (viewMode.value) {
      case 'month': {
        const year = currentDate.value.getFullYear();
        const month = currentDate.value.getMonth();
        
        startDate = new Date(year, month, 1);
        startDate.setDate(startDate.getDate() - startDate.getDay()); // Start from Sunday
        
        endDate = new Date(startDate);
        endDate.setDate(startDate.getDate() + 41); // 42 days total
        break;
      }
      
      case 'week': {
        startDate = new Date(currentDate.value);
        startDate.setDate(currentDate.value.getDate() - currentDate.value.getDay());
        startDate.setHours(0, 0, 0, 0);
        
        endDate = new Date(startDate);
        endDate.setDate(startDate.getDate() + 7);
        break;
      }
      
      case 'day': {
        startDate = new Date(currentDate.value);
        startDate.setHours(0, 0, 0, 0);
        
        endDate = new Date(startDate);
        endDate.setDate(startDate.getDate() + 1);
        break;
      }
      
      default: {
        // Fallback to month view
        const year = currentDate.value.getFullYear();
        const month = currentDate.value.getMonth();
        
        startDate = new Date(year, month, 1);
        startDate.setDate(startDate.getDate() - startDate.getDay());
        
        endDate = new Date(startDate);
        endDate.setDate(startDate.getDate() + 41);
      }
    }
    
    // Check cache first
    const cacheKey = getCacheKey(startDate, endDate);
    const cached = appointmentCache.get(cacheKey);
    const now = Date.now();
    
    if (!force && cached && (now - cached.timestamp) < CACHE_DURATION) {
      appointments.value = cached.data;
      pending.value = false;
      performanceMetrics.value.cacheHits++;
      return;
    }
    
    performanceMetrics.value.cacheMisses++;
    
    const queryParams = {
      fromDate: startDate.toISOString().split('T')[0],
      toDate: endDate.toISOString().split('T')[0],
      size: 1000 // Get all appointments in the range
    };

    // Debug logging
    if (process.dev) {
      console.log('Fetching appointments:', {
        viewMode: viewMode.value,
        currentDate: currentDate.value,
        dateRange: {
          from: queryParams.fromDate,
          to: queryParams.toDate,
          startDate: startDate,
          endDate: endDate
        }
      });
    }

    const fetchStart = performance.now();
    const response = await adminRequest<{
      content: AppointmentAdmin[];
      totalElements: number;
    }>("/appointments", { query: queryParams });
    const fetchEnd = performance.now();
    
    performanceMetrics.value.lastFetchTime = fetchEnd - fetchStart;
    
    const appointmentData = response.content || [];
    allAppointments.value = appointmentData;
    
    // Debug the fetched appointments
    if (process.dev) {
      console.log('Fetched appointments:', {
        count: appointmentData.length,
        appointments: appointmentData.map(apt => ({
          id: apt.id,
          scheduledAt: apt.scheduledAt,
          scheduledAtDate: new Date(apt.scheduledAt * 1000),
          patientName: apt.patientName
        })),
        hasTargetAppointment: appointmentData.some(apt => apt.scheduledAt === 1761868800)
      });
    }
    
    // Cache the result
    appointmentCache.set(cacheKey, {
      data: appointmentData,
      timestamp: now
    });
    
    // Clean old cache entries (keep only last 10)
    if (appointmentCache.size > 10) {
      const entries = Array.from(appointmentCache.entries());
      entries.sort((a, b) => b[1].timestamp - a[1].timestamp);
      appointmentCache.clear();
      entries.slice(0, 10).forEach(([key, value]) => {
        appointmentCache.set(key, value);
      });
    }
    
  } catch (error: any) {
    console.error("[calendar] Failed to load appointments:", error);
    
    // Show user-friendly error message
    const errorMessage = error?.data?.message || error?.message || "Unknown error occurred";
    toast.add({
      title: "Failed to load appointments",
      description: "Please try again or contact support if the problem persists.",
      color: "red"
    });
    
    // Don't clear appointments on error - keep showing cached data if available
    if (!allAppointments.value.length) {
      allAppointments.value = [];
    }
  } finally {
    pending.value = false;
  }
};

const refresh = () => {
  fetchAppointments(true); // Force refresh, bypass cache
};

// Debounced fetch to prevent too many API calls
let fetchTimeout: ReturnType<typeof setTimeout> | null = null;

const debouncedFetch = () => {
  if (fetchTimeout) {
    clearTimeout(fetchTimeout);
  }
  fetchTimeout = setTimeout(() => {
    fetchAppointments();
  }, 300); // 300ms debounce
};

// Watch for date and view mode changes
watch([currentDate, viewMode], () => {
  debouncedFetch();
}, { immediate: true });

// Performance monitoring
const performanceMetrics = ref({
  lastFetchTime: 0,
  cacheHits: 0,
  cacheMisses: 0
});

// Mobile detection
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768;
};

// Initialize
onMounted(() => {
  // Set today as initially selected
  selectedDate.value = new Date();
  
  // Restore view mode preference from session storage
  if (process.client) {
    const savedViewMode = sessionStorage.getItem('calendar-view-mode') as 'month' | 'week' | 'day' | null;
    if (savedViewMode && ['month', 'week', 'day'].includes(savedViewMode)) {
      viewMode.value = savedViewMode;
    }
    
    // Restore doctor filter preference
    const savedDoctorFilter = sessionStorage.getItem('calendar-doctor-filter');
    if (savedDoctorFilter) {
      const doctorId = parseInt(savedDoctorFilter);
      if (!isNaN(doctorId)) {
        selectedDoctorId.value = doctorId;
      }
    }
  }
  
  // Check mobile on mount
  checkMobile();
  window.addEventListener('resize', checkMobile);
  
  // Performance monitoring in development
  if (process.dev) {
    console.log('[Calendar] Component mounted, cache size:', appointmentCache.size);
  }
});

// Cleanup on unmount
onUnmounted(() => {
  // Clear any pending timeouts
  if (fetchTimeout) {
    clearTimeout(fetchTimeout);
  }
  
  // Remove resize listener
  window.removeEventListener('resize', checkMobile);
  
  // Log performance metrics in development
  if (process.dev) {
    console.log('[Calendar] Performance metrics:', performanceMetrics.value);
  }
});
</script>
