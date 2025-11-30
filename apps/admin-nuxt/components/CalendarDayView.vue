<template>
  <div class="space-y-5">
    <div class="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">{{ headerLabel }}</p>
        <p class="text-2xl font-semibold text-slate-900 dark:text-white">{{ dayLabel }}</p>
        <p class="text-sm text-slate-500 dark:text-slate-300">{{ t('calendar.dayView.selectionHint') || defaultHint }}</p>
      </div>
      <div class="text-sm text-slate-500 dark:text-slate-300">
        {{ appointmentCountText }}
      </div>
    </div>

    <div class="rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-800 overflow-hidden">
      <div class="flex">
        <div class="w-20 border-r border-slate-100 bg-slate-50/80 px-4 py-2 dark:border-slate-700 dark:bg-slate-900/40">
          <div
            v-for="label in hourLabels"
            :key="label.value"
            class="flex h-[88px] items-start text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
          >
            {{ label.label }}
          </div>
        </div>

        <div class="flex-1 relative">
          <div
            v-for="slot in timeSlots"
            :key="slot.minutes"
            class="h-11 border-b border-slate-100 dark:border-slate-700 relative cursor-crosshair"
            :class="slot.isHour ? 'bg-white/80 dark:bg-slate-800/80' : 'bg-white/60 dark:bg-slate-800/60'"
            @pointerdown.prevent="(event) => handlePointerDown(event, slot.minutes)"
            @pointerenter="() => handlePointerEnter(slot.minutes)"
          >
            <div
              v-if="slot.isHalf"
              class="pointer-events-none absolute inset-x-4 top-1/2 h-px bg-slate-100 dark:bg-slate-700"
            />
          </div>

          <div
            v-if="selectionStyle"
            class="pointer-events-none absolute left-2 right-2 rounded-xl border border-violet-400/70 bg-violet-500/15 shadow-inner"
            :style="selectionStyle"
          />

          <div
            v-for="event in dayEvents"
            :key="event.id"
            class="absolute left-3 right-3 rounded-xl border border-violet-500/30 bg-gradient-to-br from-violet-500/15 to-blue-500/10 px-3 py-2 text-xs text-slate-900 shadow-sm backdrop-blur dark:border-violet-400/40 dark:from-violet-400/20 dark:to-blue-400/10 dark:text-white"
            :style="event.style"
            @click="handleAppointmentClick(event.raw)"
          >
            <p class="text-sm font-semibold leading-tight truncate">
              {{ event.title }}
            </p>
            <p class="text-[11px] text-slate-500 dark:text-slate-200">
              {{ event.timeLabel }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import type { AppointmentAdmin } from '@/types/appointments';
import type { CalendarAppointmentRequest } from '@/types/calendar';

const SLOT_HEIGHT = 44;
const SLOT_STEP = 30; // minutes
const START_HOUR = 8;
const END_HOUR = 20;
const MINUTES_START = START_HOUR * 60;
const MINUTES_END = END_HOUR * 60;

interface CalendarDayViewProps {
  currentDate: Date;
  appointments: AppointmentAdmin[];
}

const props = defineProps<CalendarDayViewProps>();

const emit = defineEmits<{
  'create-appointment': [payload: CalendarAppointmentRequest];
  'appointment-click': [appointment: AppointmentAdmin];
}>();

const { t, locale } = useI18n();

const dragging = ref(false);
const dragAnchor = ref<number | null>(null);
const selection = ref<{ start: number; end: number } | null>(null);

const defaultHint = 'Click and drag to select a time range.';

const headerLabel = computed(() =>
  new Intl.DateTimeFormat(locale.value, { weekday: 'long' }).format(props.currentDate)
);

const dayLabel = computed(() =>
  new Intl.DateTimeFormat(locale.value, {
    month: 'long',
    day: 'numeric',
    year: 'numeric'
  }).format(props.currentDate)
);

const appointmentCountText = computed(() => {
  const localized = t('calendar.dayView.appointmentsCount', { count: dayEvents.value.length });
  if (localized && localized !== 'calendar.dayView.appointmentsCount') {
    return localized;
  }
  const count = dayEvents.value.length;
  return `${count} ${count === 1 ? 'appointment' : 'appointments'}`;
});

const timeSlots = computed(() => {
  const slots = [] as Array<{ minutes: number; label: string; isHour: boolean; isHalf: boolean }>;
  for (let minutes = MINUTES_START; minutes < MINUTES_END; minutes += SLOT_STEP) {
    const hour = Math.floor(minutes / 60);
    const mins = minutes % 60;
    const label = `${String(hour).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
    slots.push({ minutes, label, isHour: mins === 0, isHalf: mins === 30 });
  }
  return slots;
});

const hourLabels = computed(() => {
  const labels = [] as Array<{ value: number; label: string }>;
  for (let hour = START_HOUR; hour < END_HOUR; hour++) {
    const label = new Intl.DateTimeFormat(locale.value, {
      hour: 'numeric',
      minute: '2-digit'
    }).format(new Date(2020, 0, 1, hour, 0));
    labels.push({ value: hour, label });
  }
  return labels;
});

const selectionStyle = computed(() => {
  if (!selection.value) return null;
  const start = Math.min(selection.value.start, selection.value.end);
  const end = Math.max(selection.value.start, selection.value.end);
  const offset = Math.max(start - MINUTES_START, 0);
  const clampedEnd = Math.min(end, MINUTES_END);
  const heightMinutes = Math.max(clampedEnd - start, SLOT_STEP);
  return {
    top: `${(offset / SLOT_STEP) * SLOT_HEIGHT}px`,
    height: `${(heightMinutes / SLOT_STEP) * SLOT_HEIGHT}px`
  };
});

const dayEvents = computed(() => {
  const list: Array<{
    id: number;
    title: string;
    timeLabel: string;
    style: Record<string, string>;
    raw: AppointmentAdmin;
  }> = [];

  const targetDay = new Date(props.currentDate);
  targetDay.setHours(0, 0, 0, 0);

  props.appointments.forEach((appointment) => {
    const start = parseAppointmentDate(appointment.scheduledAt);
    const sameDay = start.toDateString() === targetDay.toDateString();
    if (!sameDay) return;

    const duration = appointment.slotDurationMinutes ?? 60;
    const end = new Date(start.getTime() + duration * 60 * 1000);
    const startMinutes = getMinutesFromDate(start);
    const endMinutes = getMinutesFromDate(end);

    if (endMinutes <= MINUTES_START || startMinutes >= MINUTES_END) return;

    const normalizedStart = Math.max(startMinutes, MINUTES_START);
    const normalizedEnd = Math.min(endMinutes, MINUTES_END);
    const relativeStart = normalizedStart - MINUTES_START;
    const relativeMinutes = Math.max(normalizedEnd - normalizedStart, SLOT_STEP);

    list.push({
      id: appointment.id,
      raw: appointment,
      title: `${appointment.patientName || 'Patient'} · ${appointment.serviceName}`,
      timeLabel: `${formatTime(start)} – ${formatTime(end)}`,
      style: {
        top: `${(relativeStart / SLOT_STEP) * SLOT_HEIGHT}px`,
        height: `${(relativeMinutes / SLOT_STEP) * SLOT_HEIGHT}px`
      }
    });
  });

  return list;
});

watch(
  () => props.currentDate,
  () => {
    selection.value = null;
    dragging.value = false;
    dragAnchor.value = null;
  }
);

const handlePointerDown = (_event: PointerEvent, minutes: number) => {
  dragging.value = true;
  dragAnchor.value = minutes;
  selection.value = {
    start: clampMinutes(minutes),
    end: clampMinutes(minutes + SLOT_STEP)
  };
};

const handlePointerEnter = (minutes: number) => {
  if (!dragging.value || dragAnchor.value === null || !selection.value) return;
  const anchor = dragAnchor.value;
  const next = clampMinutes(minutes);
  if (next >= anchor) {
    selection.value.start = clampMinutes(anchor);
    selection.value.end = clampMinutes(next + SLOT_STEP);
  } else {
    selection.value.start = clampMinutes(next);
    selection.value.end = clampMinutes(anchor + SLOT_STEP);
  }
};

const handlePointerUp = () => {
  if (!dragging.value || !selection.value) return;
  const startMinutes = Math.min(selection.value.start, selection.value.end - SLOT_STEP);
  const endMinutes = Math.max(selection.value.start + SLOT_STEP, selection.value.end);
  const startDate = minutesToDate(startMinutes);
  const endDate = minutesToDate(Math.min(endMinutes, MINUTES_END));
  emit('create-appointment', { start: startDate, end: endDate });
  selection.value = null;
  dragging.value = false;
  dragAnchor.value = null;
};

const minutesToDate = (minutes: number) => {
  const date = new Date(props.currentDate);
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  date.setHours(hours, mins, 0, 0);
  return date;
};

const clampMinutes = (value: number) => Math.min(Math.max(value, MINUTES_START), MINUTES_END - SLOT_STEP);

const parseAppointmentDate = (scheduledAt: string | number | Date) => {
  if (scheduledAt instanceof Date) return scheduledAt;
  if (typeof scheduledAt === 'number') {
    return new Date(scheduledAt * 1000);
  }
  const timestamp = Number(scheduledAt);
  if (!Number.isNaN(timestamp)) {
    return new Date(timestamp * 1000);
  }
  return new Date(scheduledAt);
};

const formatTime = (date: Date) =>
  new Intl.DateTimeFormat(locale.value, {
    hour: 'numeric',
    minute: '2-digit'
  }).format(date);

const getMinutesFromDate = (date: Date) => date.getHours() * 60 + date.getMinutes();

const handleAppointmentClick = (appointment: AppointmentAdmin) => {
  emit('appointment-click', appointment);
};

onMounted(() => {
  window.addEventListener('pointerup', handlePointerUp);
});

onBeforeUnmount(() => {
  window.removeEventListener('pointerup', handlePointerUp);
});
</script>
