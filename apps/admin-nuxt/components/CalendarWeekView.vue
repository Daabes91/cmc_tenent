<template>
  <div class="space-y-5">
    <div class="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
          {{ weekTitle }}
        </p>
        <p class="text-2xl font-semibold text-slate-900 dark:text-white">{{ weekRangeLabel }}</p>
        <p class="text-sm text-slate-500 dark:text-slate-300">
          {{ t('calendar.weekView.selectionHint') || defaultHint }}
        </p>
      </div>
    </div>

    <div class="rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-800 overflow-hidden">
      <div class="grid grid-cols-[auto,repeat(7,minmax(0,1fr))] border-b border-slate-100 bg-slate-50/80 dark:border-slate-700 dark:bg-slate-900/40">
        <div class="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
          {{ t('calendar.weekView.timeColumn') || 'Time' }}
        </div>
        <button
          v-for="day in weekDays"
          :key="day.key"
          class="px-4 py-3 text-left transition hover:bg-white/60 dark:hover:bg-slate-900/60"
          :class="day.isToday ? 'bg-white dark:bg-slate-900/40' : ''"
          type="button"
          @click="emitDateClick(day.date)"
        >
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">{{ day.weekday }}</p>
          <p class="text-lg font-semibold" :class="day.isToday ? 'text-violet-600 dark:text-violet-300' : 'text-slate-900 dark:text-white'">
            {{ day.dayNumber }}
          </p>
        </button>
      </div>

      <div class="grid grid-cols-[auto,repeat(7,minmax(0,1fr))]">
        <div class="border-r border-slate-100 bg-slate-50/80 dark:border-slate-700 dark:bg-slate-900/30">
          <div
            v-for="label in hourLabels"
            :key="label.value"
            class="flex h-[88px] items-start px-4 py-2 text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
          >
            {{ label.label }}
          </div>
        </div>

        <div
          v-for="day in weekDays"
          :key="`column-${day.key}`"
          class="relative border-r border-slate-100 last:border-r-0 dark:border-slate-700"
        >
          <div
            v-for="slot in timeSlots"
            :key="`${day.key}-${slot.minutes}`"
            class="h-11 border-b border-slate-100 dark:border-slate-700 relative cursor-crosshair"
            :class="day.isToday ? 'bg-violet-50/30 dark:bg-violet-900/10' : 'bg-white/60 dark:bg-slate-800/60'"
            @pointerdown.prevent="(event) => handlePointerDown(event, day.key, slot.minutes)"
            @pointerenter="() => handlePointerEnter(day.key, slot.minutes)"
          >
            <div v-if="slot.isHalf" class="pointer-events-none absolute inset-x-2 top-1/2 h-px bg-slate-100 dark:bg-slate-700" />
          </div>

          <div
            v-if="getSelectionStyle(day.key)"
            class="pointer-events-none absolute left-2 right-2 rounded-xl border border-violet-400/70 bg-violet-500/15 shadow-inner"
            :style="getSelectionStyle(day.key)!"
          />

          <div
            v-for="event in day.events"
            :key="event.id"
            class="absolute left-2 right-2 rounded-xl border border-violet-500/30 bg-gradient-to-br from-violet-500/15 to-blue-500/10 px-2 py-1 text-[11px] text-slate-900 shadow-sm hover:shadow-md dark:border-violet-400/40 dark:from-violet-400/20 dark:to-blue-400/10 dark:text-white"
            :style="event.style"
            @click="handleAppointmentClick(event.raw)"
          >
            <p class="font-semibold truncate">{{ event.title }}</p>
            <p class="text-[10px] text-slate-500 dark:text-slate-200">{{ event.timeLabel }}</p>
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
const SLOT_STEP = 30;
const START_HOUR = 8;
const END_HOUR = 20;
const MINUTES_START = START_HOUR * 60;
const MINUTES_END = END_HOUR * 60;

interface CalendarWeekViewProps {
  currentDate: Date;
  appointments: AppointmentAdmin[];
}

const props = defineProps<CalendarWeekViewProps>();

const emit = defineEmits<{
  'create-appointment': [payload: CalendarAppointmentRequest];
  'appointment-click': [appointment: AppointmentAdmin];
  'date-click': [date: Date];
}>();

const { t, locale } = useI18n();

const defaultHint = 'Click and drag inside a day column to select a time range.';

const weekDragging = ref(false);
const weekAnchor = ref<{ dayKey: string; minutes: number } | null>(null);
const weekSelection = ref<{ dayKey: string; start: number; end: number } | null>(null);

const weekStart = computed(() => {
  const current = new Date(props.currentDate);
  current.setHours(0, 0, 0, 0);
  const day = current.getDay();
  const diff = (day + 6) % 7; // Monday as first day
  current.setDate(current.getDate() - diff);
  return current;
});

const weekDays = computed(() => {
  const days: Array<{
    key: string;
    date: Date;
    weekday: string;
    dayNumber: number;
    isToday: boolean;
    events: WeekEvent[];
  }> = [];
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  for (let index = 0; index < 7; index++) {
    const date = new Date(weekStart.value);
    date.setDate(weekStart.value.getDate() + index);
    const dateKey = getDateKey(date);
    const events = buildEventsForDate(date);
    days.push({
      key: dateKey,
      date,
      weekday: new Intl.DateTimeFormat(locale.value, { weekday: 'short' }).format(date),
      dayNumber: date.getDate(),
      isToday: date.getTime() === today.getTime(),
      events
    });
  }
  return days;
});

const hourLabels = computed(() => {
  const labels: Array<{ value: number; label: string }> = [];
  for (let hour = START_HOUR; hour < END_HOUR; hour++) {
    labels.push({
      value: hour,
      label: new Intl.DateTimeFormat(locale.value, { hour: 'numeric', minute: '2-digit' }).format(
        new Date(2020, 0, 1, hour, 0)
      )
    });
  }
  return labels;
});

const timeSlots = computed(() => {
  const slots: Array<{ minutes: number; isHalf: boolean }> = [];
  for (let minutes = MINUTES_START; minutes < MINUTES_END; minutes += SLOT_STEP) {
    slots.push({ minutes, isHalf: minutes % 60 === 30 });
  }
  return slots;
});

const weekTitle = computed(() =>
  new Intl.DateTimeFormat(locale.value, { month: 'long', year: 'numeric' }).format(props.currentDate)
);

const weekRangeLabel = computed(() => {
  const start = weekDays.value[0]?.date;
  const end = weekDays.value[6]?.date;
  if (!start || !end) return '';
  return `${new Intl.DateTimeFormat(locale.value, { month: 'short', day: 'numeric' }).format(start)} — ${new Intl.DateTimeFormat(locale.value, { month: 'short', day: 'numeric' }).format(end)}`;
});

watch(
  () => props.currentDate,
  () => {
    weekSelection.value = null;
    weekAnchor.value = null;
    weekDragging.value = false;
  }
);

const getSelectionStyle = (dayKey: string) => {
  if (!weekSelection.value || weekSelection.value.dayKey !== dayKey) return null;
  const start = Math.min(weekSelection.value.start, weekSelection.value.end - SLOT_STEP);
  const end = Math.max(weekSelection.value.start + SLOT_STEP, weekSelection.value.end);
  const offset = Math.max(start - MINUTES_START, 0);
  const heightMinutes = Math.max(Math.min(end, MINUTES_END) - start, SLOT_STEP);
  return {
    top: `${(offset / SLOT_STEP) * SLOT_HEIGHT}px`,
    height: `${(heightMinutes / SLOT_STEP) * SLOT_HEIGHT}px`
  };
};

const handlePointerDown = (_event: PointerEvent, dayKey: string, minutes: number) => {
  weekDragging.value = true;
  weekAnchor.value = { dayKey, minutes };
  weekSelection.value = {
    dayKey,
    start: clampMinutes(minutes),
    end: clampMinutes(minutes + SLOT_STEP)
  };
};

const handlePointerEnter = (dayKey: string, minutes: number) => {
  if (!weekDragging.value || !weekAnchor.value || weekAnchor.value.dayKey !== dayKey || !weekSelection.value) {
    return;
  }
  const anchor = weekAnchor.value.minutes;
  const next = clampMinutes(minutes);
  if (next >= anchor) {
    weekSelection.value.start = clampMinutes(anchor);
    weekSelection.value.end = clampMinutes(next + SLOT_STEP);
  } else {
    weekSelection.value.start = clampMinutes(next);
    weekSelection.value.end = clampMinutes(anchor + SLOT_STEP);
  }
};

const handlePointerUp = () => {
  if (!weekDragging.value || !weekSelection.value) return;
  const selection = weekSelection.value;
  const day = weekDays.value.find((item) => item.key === selection.dayKey);
  if (!day) {
    resetSelection();
    return;
  }
  const startMinutes = Math.min(selection.start, selection.end - SLOT_STEP);
  const endMinutes = Math.max(selection.start + SLOT_STEP, selection.end);
  const startDate = mergeDateAndMinutes(day.date, startMinutes);
  const endDate = mergeDateAndMinutes(day.date, Math.min(endMinutes, MINUTES_END));
  emit('create-appointment', { start: startDate, end: endDate });
  resetSelection();
};

const handleAppointmentClick = (appointment: AppointmentAdmin) => {
  emit('appointment-click', appointment);
};

const emitDateClick = (date: Date) => emit('date-click', date);

const resetSelection = () => {
  weekSelection.value = null;
  weekDragging.value = false;
  weekAnchor.value = null;
};

const clampMinutes = (value: number) => Math.min(Math.max(value, MINUTES_START), MINUTES_END - SLOT_STEP);

const mergeDateAndMinutes = (date: Date, minutes: number) => {
  const merged = new Date(date);
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  merged.setHours(hours, mins, 0, 0);
  return merged;
};

const getDateKey = (date: Date) => date.toISOString().split('T')[0];

type WeekEvent = {
  id: number;
  style: Record<string, string>;
  title: string;
  timeLabel: string;
  raw: AppointmentAdmin;
};

const buildEventsForDate = (date: Date): WeekEvent[] => {
  const dayStart = new Date(date);
  dayStart.setHours(0, 0, 0, 0);
  const dayString = dayStart.toDateString();

  return props.appointments
    .map((appointment) => {
      const start = parseAppointmentDate(appointment.scheduledAt);
      if (start.toDateString() !== dayString) return null;
      const duration = appointment.slotDurationMinutes ?? 60;
      const end = new Date(start.getTime() + duration * 60 * 1000);
      const startMinutes = getMinutesFromDate(start);
      const endMinutes = getMinutesFromDate(end);
      if (endMinutes <= MINUTES_START || startMinutes >= MINUTES_END) return null;
      const normalizedStart = Math.max(startMinutes, MINUTES_START);
      const normalizedEnd = Math.min(endMinutes, MINUTES_END);
      const relativeStart = normalizedStart - MINUTES_START;
      const relativeMinutes = Math.max(normalizedEnd - normalizedStart, SLOT_STEP);
      return {
        id: appointment.id,
        raw: appointment,
        title: `${appointment.patientName || 'Patient'} · ${appointment.serviceName}`,
        timeLabel: `${formatTime(start)} – ${formatTime(end)}`,
        style: {
          top: `${(relativeStart / SLOT_STEP) * SLOT_HEIGHT}px`,
          height: `${(relativeMinutes / SLOT_STEP) * SLOT_HEIGHT}px`
        }
      } as WeekEvent;
    })
    .filter((event): event is WeekEvent => Boolean(event));
};

const parseAppointmentDate = (scheduledAt: number | string | Date) => {
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

const getMinutesFromDate = (date: Date) => date.getHours() * 60 + date.getMinutes();

const formatTime = (date: Date) =>
  new Intl.DateTimeFormat(locale.value, { hour: 'numeric', minute: '2-digit' }).format(date);

onMounted(() => {
  window.addEventListener('pointerup', handlePointerUp);
});

onBeforeUnmount(() => {
  window.removeEventListener('pointerup', handlePointerUp);
});
</script>
