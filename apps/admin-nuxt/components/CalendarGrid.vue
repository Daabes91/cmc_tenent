<template>
  <div class="calendar-grid" :class="{ 'rtl': isRTL }">
    <!-- Day Headers -->
    <div class="grid grid-cols-7 gap-2 mb-4">
      <div 
        v-for="day in dayHeaders" 
        :key="day"
        class="text-center font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400 py-2"
        :class="mobile ? 'text-xs' : 'text-xs'"
      >
        {{ mobile ? day.slice(0, 2) : day }}
      </div>
    </div>

    <!-- Calendar Days Grid -->
    <div class="grid grid-cols-7 gap-2">
      <div
        v-for="day in calendarDays"
        :key="`${day.date.getFullYear()}-${day.date.getMonth()}-${day.date.getDate()}`"
        class="calendar-day aspect-square border border-slate-200/60 dark:border-slate-700/60 rounded-lg p-2 transition-all duration-200 cursor-pointer focus:outline-none focus:ring-2 focus:ring-violet-500/50 touch-manipulation"
        :class="[
          mobile ? 'min-h-[80px]' : 'min-h-[120px]',
          day.isCurrentMonth 
            ? 'bg-white dark:bg-slate-800 hover:bg-slate-50 dark:hover:bg-slate-700/50' 
            : 'bg-slate-50 dark:bg-slate-900/50 text-slate-400',
          day.isToday && 'ring-2 ring-violet-500/50 bg-violet-50 dark:bg-violet-900/20',
          day.isSelected && 'ring-2 ring-blue-500/50 bg-blue-50 dark:bg-blue-900/20'
        ]"
        :tabindex="day.isCurrentMonth ? 0 : -1"
        :aria-label="getDateAriaLabel(day)"
        @click="handleDateClick(day.date)"
        @keydown.enter="handleDateClick(day.date)"
        @keydown.space.prevent="handleDateClick(day.date)"
      >
        <!-- Date Number -->
        <div class="flex items-center justify-between mb-2">
          <span 
            class="font-medium"
            :class="[
              mobile ? 'text-xs' : 'text-sm',
              day.isCurrentMonth ? 'text-slate-900 dark:text-white' : 'text-slate-400',
              day.isToday && 'text-violet-600 dark:text-violet-400 font-bold'
            ]"
          >
            {{ day.date.getDate() }}
          </span>
          <div v-if="day.events.length > 0" class="flex items-center gap-1">
            <!-- Multiple status dots for different appointment types -->
            <div class="flex items-center gap-0.5">
              <div 
                v-for="(event, idx) in day.events.slice(0, 3)" 
                :key="idx"
                class="w-1.5 h-1.5 rounded-full"
                :class="getStatusDotClass(event.appointment.status)"
              ></div>
              <div 
                v-if="day.events.length > 3"
                class="w-1.5 h-1.5 rounded-full bg-slate-400"
              ></div>
            </div>
            <span class="font-medium" :class="mobile ? 'text-xs' : 'text-xs'" style="color: rgb(100 116 139);">{{ day.events.length }}</span>
          </div>
        </div>

        <!-- Appointment Events -->
        <div class="space-y-1 overflow-hidden">
          <CalendarEvent
            v-for="(event, index) in day.events.slice(0, mobile ? 2 : maxEventsPerDay)"
            :key="event.id"
            :event="event"
            :compact="true"
            :mobile="mobile"
            @click="handleEventClick"
          />
          
          <!-- More events indicator -->
          <div 
            v-if="day.events.length > (mobile ? 2 : maxEventsPerDay)"
            class="text-slate-500 dark:text-slate-400 text-center py-1 cursor-pointer hover:text-slate-700 dark:hover:text-slate-300 touch-manipulation"
            :class="mobile ? 'text-xs' : 'text-xs'"
            @click.stop="showMoreEvents(day)"
          >
            +{{ day.events.length - (mobile ? 2 : maxEventsPerDay) }} {{ mobile ? '+' : t('calendar.moreEvents') }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AppointmentAdmin } from "@/types/appointments";
import { computed } from "vue";

// Props
interface CalendarGridProps {
  currentDate: Date;
  appointments: AppointmentAdmin[];
  selectedDate?: Date | null;
  maxEventsPerDay?: number;
  mobile?: boolean;
}

const props = withDefaults(defineProps<CalendarGridProps>(), {
  selectedDate: null,
  maxEventsPerDay: 3,
  mobile: false
});

// Emits
const emit = defineEmits<{
  dateClick: [date: Date];
  appointmentClick: [appointment: AppointmentAdmin];
  showMore: [day: CalendarDay];
}>();

// Composables
const { t, locale } = useI18n();

// Calendar data structures
interface CalendarEvent {
  id: string;
  date: Date;
  appointment: AppointmentAdmin;
  displayText: string;
  statusColor: string;
  timeSlot: string;
}

interface CalendarDay {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
  isSelected: boolean;
  events: CalendarEvent[];
  hasEvents: boolean;
}

// Computed properties
const isRTL = computed(() => locale.value === 'ar');

const dayHeaders = computed(() => {
  const formatter = new Intl.DateTimeFormat(locale.value, { weekday: 'short' });
  const days = [];
  
  // Start from Sunday (0) to Saturday (6)
  for (let i = 0; i < 7; i++) {
    // Create a date for each day of the week
    const date = new Date(2024, 0, 7 + i); // January 7, 2024 is a Sunday
    days.push(formatter.format(date));
  }
  
  return isRTL.value ? days.reverse() : days;
});

const calendarDays = computed((): CalendarDay[] => {
  const year = props.currentDate.getFullYear();
  const month = props.currentDate.getMonth();
  
  // Get first day of the month and calculate starting date
  const firstDay = new Date(year, month, 1);
  const startDate = new Date(firstDay);
  
  // Adjust for RTL (Arabic) - start from Saturday instead of Sunday
  const dayOffset = isRTL.value ? (firstDay.getDay() + 1) % 7 : firstDay.getDay();
  startDate.setDate(startDate.getDate() - dayOffset);
  
  const days: CalendarDay[] = [];
  const today = new Date();
  
  // Generate 42 days (6 weeks)
  for (let i = 0; i < 42; i++) {
    const date = new Date(startDate);
    date.setDate(startDate.getDate() + i);
    
    const isCurrentMonth = date.getMonth() === month;
    const isToday = date.toDateString() === today.toDateString();
    const isSelected = props.selectedDate?.toDateString() === date.toDateString();
    
    // Get events for this date
    const dayEvents = getEventsForDate(date);
    
    days.push({
      date,
      isCurrentMonth,
      isToday,
      isSelected,
      events: dayEvents,
      hasEvents: dayEvents.length > 0
    });
  }
  
  return days;
});

// Helper functions
const getEventsForDate = (date: Date): CalendarEvent[] => {
  const events = props.appointments
    .filter(appointment => {
      // Convert Unix timestamp (seconds) to milliseconds for JavaScript Date
      const appointmentDate = new Date(appointment.scheduledAt * 1000);
      return appointmentDate.toDateString() === date.toDateString();
    })
    .map(appointment => ({
      id: appointment.id.toString(),
      date,
      appointment,
      displayText: appointment.patientName || 'Unknown Patient',
      statusColor: getStatusColor(appointment.status),
      timeSlot: formatTime(appointment.scheduledAt)
    }))
    .sort((a, b) => (a.appointment.scheduledAt - b.appointment.scheduledAt));
    
  return events;
};

const getStatusColor = (status: string): string => {
  const colors = {
    'SCHEDULED': 'blue',
    'CONFIRMED': 'green',
    'COMPLETED': 'purple',
    'CANCELLED': 'red'
  };
  return colors[status as keyof typeof colors] || 'gray';
};

// Use the new appointment time composable for consistent time handling
const { formatForList } = useAppointmentTime();

const formatTime = (timestamp: number): string => {
  // Use the appointment time composable for consistent formatting
  // Convert Unix timestamp (seconds) to milliseconds for the composable
  const formatted = formatForList(timestamp * 1000);
  if (formatted === "Not scheduled" || formatted === "Invalid date") return "—";
  
  // Extract just the time part for calendar display
  try {
    const parts = formatted.split(',');
    if (parts.length >= 2) {
      // Extract time and timezone: "2:30 PM EET"
      const timePart = parts[parts.length - 1].trim();
      return timePart;
    }
    return formatted;
  } catch (error) {
    console.warn('Error extracting time part for calendar:', error);
    return formatted;
  }
};

const getStatusDotClass = (status: string): string => {
  const statusLower = status.toLowerCase();
  
  switch (statusLower) {
    case 'scheduled':
      return 'bg-blue-500';
    case 'confirmed':
      return 'bg-green-500';
    case 'completed':
      return 'bg-purple-500';
    case 'cancelled':
      return 'bg-red-500';
    default:
      return 'bg-slate-500';
  }
};

const getDateAriaLabel = (day: CalendarDay): string => {
  const formatter = new Intl.DateTimeFormat(locale.value, {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric'
  });
  
  const dateStr = formatter.format(day.date);
  const eventCount = day.events.length;
  
  if (eventCount === 0) {
    return `${dateStr}, no appointments`;
  } else if (eventCount === 1) {
    return `${dateStr}, 1 appointment`;
  } else {
    return `${dateStr}, ${eventCount} appointments`;
  }
};

// Event handlers
const handleDateClick = (date: Date) => {
  emit('dateClick', date);
};

const handleEventClick = (event: CalendarEvent) => {
  emit('appointmentClick', event.appointment);
};

const showMoreEvents = (day: CalendarDay) => {
  emit('showMore', day);
};
</script>

<style scoped>
.calendar-grid.rtl {
  direction: rtl;
}

.calendar-grid.rtl .grid {
  direction: ltr; /* Keep grid layout LTR for proper alignment */
}

.calendar-grid.rtl .calendar-day {
  direction: rtl;
}

.calendar-grid.rtl .flex {
  direction: rtl;
}

.calendar-day {
  min-height: 120px;
}

/* RTL-specific adjustments */
.calendar-grid.rtl .space-y-1 > * + * {
  margin-top: 0.25rem;
  margin-right: 0;
}

.calendar-grid.rtl .gap-1 {
  gap: 0.25rem;
}

.calendar-grid.rtl .gap-0\.5 {
  gap: 0.125rem;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .calendar-day {
    min-height: 80px;
  }
}

@media (max-width: 640px) {
  .calendar-day {
    min-height: 60px;
  }
}
</style>