<template>
  <div class="space-y-5">
    <div>
      <p class="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
        {{ monthLabel }}
      </p>
      <p class="text-2xl font-semibold text-slate-900 dark:text-white">{{ monthRangeLabel }}</p>
      <p class="text-sm text-slate-500 dark:text-slate-300">{{ t('calendar.monthView.selectionHint') || defaultHint }}</p>
    </div>

    <div class="rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <div class="grid grid-cols-7 border-b border-slate-100 bg-slate-50/80 px-3 py-2 text-xs font-semibold uppercase tracking-wide text-slate-500 dark:border-slate-700 dark:bg-slate-900/40 dark:text-slate-300">
        <div v-for="weekday in weekdayLabels" :key="weekday" class="text-center">{{ weekday }}</div>
      </div>

      <div class="grid grid-cols-7 gap-px bg-slate-100/80 dark:bg-slate-700/80">
        <button
          v-for="day in calendarDays"
          :key="day.key"
          type="button"
          class="relative min-h-[130px] rounded-none border-0 bg-white px-3 py-2 text-left transition dark:bg-slate-900/30"
          :class="[
            day.isCurrentMonth ? 'text-slate-900 dark:text-white' : 'text-slate-400 dark:text-slate-500',
            day.isToday ? 'ring-1 ring-violet-500/50' : '',
            isDaySelected(day.key) ? 'bg-violet-50/60 dark:bg-violet-900/20 ring-2 ring-violet-400/70' : ''
          ]"
          @pointerdown.prevent="(event) => handlePointerDown(event, day.date)"
          @pointerenter="() => handlePointerEnter(day.date)"
          @dblclick.stop="emit('date-click', day.date)"
        >
          <div class="flex items-center justify-between">
            <span class="text-sm font-semibold">
              {{ day.date.getDate() }}
            </span>
            <span v-if="day.appointments.length" class="text-xs font-semibold text-slate-400 dark:text-slate-300">
              {{ day.appointments.length }}
            </span>
          </div>

          <div class="mt-2 space-y-1">
            <button
              v-for="appointment in day.previewAppointments"
              :key="appointment.id"
              class="block w-full truncate rounded-lg bg-slate-100 px-2 py-1 text-xs font-semibold text-slate-700 transition hover:bg-violet-100 hover:text-violet-900 dark:bg-slate-800/70 dark:text-slate-200 dark:hover:bg-violet-900/30"
              @click.stop="emit('appointment-click', appointment)"
            >
              {{ formatAppointmentTitle(appointment) }}
            </button>
            <p v-if="day.appointments.length > day.previewAppointments.length" class="text-[11px] font-medium text-slate-400 dark:text-slate-500">
              +{{ day.appointments.length - day.previewAppointments.length }} more
            </p>
          </div>

          <div class="mt-3 space-y-1">
            <div
              v-for="annotation in day.periods"
              :key="annotation.id"
              class="flex items-center gap-2 rounded-lg px-2 py-1 text-[11px] font-semibold"
              :class="annotation.style.bg"
            >
              <span class="h-2 w-2 rounded-full" :class="annotation.style.dot"></span>
              <span>{{ annotation.type }}</span>
            </div>
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import type { AppointmentAdmin } from '@/types/appointments';
import type { CalendarPeriod } from '@/types/calendar';

interface CalendarGridProps {
  currentDate: Date;
  appointments: AppointmentAdmin[];
  periods?: CalendarPeriod[];
}

const props = withDefaults(defineProps<CalendarGridProps>(), {
  periods: () => []
});

const emit = defineEmits<{
  'appointment-click': [appointment: AppointmentAdmin];
  'date-click': [date: Date];
  'create-period': [payload: { start: Date; end: Date }];
}>();

const { t, locale } = useI18n();

const defaultHint = 'Drag across days to block periods, doctor vacations, or clinic closures.';
const selecting = ref(false);
const selectionRange = ref<{ start: Date; end: Date } | null>(null);
const hasDragged = ref(false);

const weekdayLabels = computed(() => {
  const baseDate = new Date(2020, 5, 1);
  return Array.from({ length: 7 }, (_, index) =>
    new Intl.DateTimeFormat(locale.value, { weekday: 'short' }).format(
      new Date(baseDate.getFullYear(), baseDate.getMonth(), baseDate.getDate() + index)
    )
  );
});

const calendarDays = computed(() => {
  const results: Array<CalendarDay> = [];
  const firstOfMonth = new Date(props.currentDate);
  firstOfMonth.setDate(1);
  firstOfMonth.setHours(0, 0, 0, 0);
  const firstWeekday = (firstOfMonth.getDay() + 6) % 7; // Monday start grid
  const gridStart = new Date(firstOfMonth);
  gridStart.setDate(firstOfMonth.getDate() - firstWeekday);
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  for (let index = 0; index < 42; index++) {
    const date = new Date(gridStart);
    date.setDate(gridStart.getDate() + index);
    const key = getDateKey(date);
    const appointments = appointmentsByDay.value.get(key) ?? [];
    const previewAppointments = appointments.slice(0, 2);
    results.push({
      key,
      date,
      isCurrentMonth: date.getMonth() === props.currentDate.getMonth(),
      isToday: date.getTime() === today.getTime(),
      appointments,
      previewAppointments,
      periods: buildPeriodAnnotations(date)
    });
  }

  return results;
});

const appointmentsByDay = computed(() => {
  const map = new Map<string, AppointmentAdmin[]>();
  props.appointments.forEach((appointment) => {
    const date = parseAppointmentDate(appointment.scheduledAt);
    const key = getDateKey(date);
    if (!map.has(key)) {
      map.set(key, []);
    }
    map.get(key)!.push(appointment);
  });
  return map;
});

const periodColors: Record<string, { bg: string; dot: string }> = {
  'Clinic closed': { bg: 'bg-rose-50 text-rose-700 dark:bg-rose-500/15 dark:text-rose-200', dot: 'bg-rose-500' },
  'Doctor vacation': { bg: 'bg-amber-50 text-amber-700 dark:bg-amber-500/15 dark:text-amber-200', dot: 'bg-amber-500' },
  Campaign: { bg: 'bg-sky-50 text-sky-700 dark:bg-sky-500/15 dark:text-sky-200', dot: 'bg-sky-500' }
};

const buildPeriodAnnotations = (date: Date) => {
  const annotations: Array<{ id: string; type: string; style: { bg: string; dot: string } }> = [];
  props.periods.forEach((period) => {
    const start = new Date(period.startDate);
    const end = new Date(period.endDate);
    start.setHours(0, 0, 0, 0);
    end.setHours(0, 0, 0, 0);
    const current = new Date(date);
    current.setHours(0, 0, 0, 0);
    if (current >= start && current <= end) {
      const styles = periodColors[period.type] ?? { bg: 'bg-slate-100 text-slate-600', dot: 'bg-slate-400' };
      annotations.push({ id: `${period.id}-${current.toISOString()}`, type: period.type, style: styles });
    }
  });
  return annotations;
};

const monthLabel = computed(() =>
  new Intl.DateTimeFormat(locale.value, { month: 'long', year: 'numeric' }).format(props.currentDate)
);

const monthRangeLabel = computed(() => {
  const formatter = new Intl.DateTimeFormat(locale.value, { month: 'long' });
  const monthName = formatter.format(props.currentDate);
  return `${monthName} ${props.currentDate.getFullYear()}`;
});

const normalizedSelection = computed(() => {
  if (!selectionRange.value) return null;
  const start = startOfDay(selectionRange.value.start);
  const end = startOfDay(selectionRange.value.end);
  return start <= end
    ? { start, end }
    : { start: end, end: start };
});

const selectedKeys = computed(() => {
  const range = normalizedSelection.value;
  if (!range) return new Set<string>();
  const keys = new Set<string>();
  const cursor = new Date(range.start);
  while (cursor <= range.end) {
    keys.add(getDateKey(cursor));
    cursor.setDate(cursor.getDate() + 1);
  }
  return keys;
});

const isDaySelected = (key: string) => selectedKeys.value.has(key);

const handlePointerDown = (event: PointerEvent, date: Date) => {
  if (event.button !== 0) return;
  selecting.value = true;
  hasDragged.value = false;
  const normalized = startOfDay(date);
  selectionRange.value = { start: normalized, end: normalized };
};

const handlePointerEnter = (date: Date) => {
  if (!selecting.value || !selectionRange.value) return;
  const normalized = startOfDay(date);
  if (selectionRange.value.end.getTime() !== normalized.getTime()) {
    hasDragged.value = true;
  }
  selectionRange.value.end = normalized;
};

const handlePointerUp = () => {
  if (!selecting.value || !selectionRange.value) return;
  const range = normalizedSelection.value;
  if (!range) {
    resetSelection();
    return;
  }
  if (!hasDragged.value) {
    emit('date-click', new Date(range.start));
  } else {
    emit('create-period', { start: new Date(range.start), end: new Date(range.end) });
  }
  resetSelection();
};

const resetSelection = () => {
  selectionRange.value = null;
  selecting.value = false;
  hasDragged.value = false;
};

const formatAppointmentTitle = (appointment: AppointmentAdmin) => `${appointment.patientName || 'Patient'} · ${appointment.serviceName}`;

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

const getDateKey = (date: Date) => date.toISOString().split('T')[0];

const startOfDay = (date: Date) => {
  const copy = new Date(date);
  copy.setHours(0, 0, 0, 0);
  return copy;
};

type CalendarDay = {
  key: string;
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
  appointments: AppointmentAdmin[];
  previewAppointments: AppointmentAdmin[];
  periods: Array<{ id: string; type: string; style: { bg: string; dot: string } }>;
};

watch(
  () => props.currentDate,
  () => {
    resetSelection();
  }
);

onMounted(() => {
  window.addEventListener('pointerup', handlePointerUp);
});

onBeforeUnmount(() => {
  window.removeEventListener('pointerup', handlePointerUp);
});
</script>
