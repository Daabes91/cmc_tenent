<template>
  <div class="calendar-week-view">


    <!-- Week Header -->
    <div class="grid grid-cols-8 gap-px bg-slate-200 dark:bg-slate-700 rounded-t-lg overflow-hidden">
      <!-- Time column header -->
      <div class="bg-slate-50 dark:bg-slate-800 p-3">
        <div class="text-xs font-medium text-slate-500 dark:text-slate-400">
          {{ t('calendar.weekView.timeSlot', { time: '' }) }}
        </div>
      </div>
      
      <!-- Day headers -->
      <div
        v-for="day in weekDays"
        :key="day.date.toISOString()"
        class="bg-white dark:bg-slate-800 p-3 text-center"
        :class="{
          'bg-violet-50 dark:bg-violet-900/20': day.isToday
        }"
      >
        <div class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">
          {{ day.dayName }}
        </div>
        <div 
          class="text-lg font-semibold mt-1"
          :class="{
            'text-violet-600 dark:text-violet-400': day.isToday,
            'text-slate-900 dark:text-white': !day.isToday
          }"
        >
          {{ day.dayNumber }}
        </div>
      </div>
    </div>

    <!-- Week Grid -->
    <div class="max-h-[600px] overflow-y-auto">
      <div class="grid grid-cols-8 gap-px bg-slate-200 dark:bg-slate-700 rounded-b-lg overflow-hidden">
      <!-- Time slots -->
      <div class="bg-slate-50 dark:bg-slate-800">
        <div
          v-for="hour in timeSlots"
          :key="hour"
          class="h-16 border-b border-slate-200 dark:border-slate-700 flex items-center justify-center"
        >
          <span class="text-xs text-slate-500 dark:text-slate-400 font-medium">
            {{ formatHour(hour) }}
          </span>
        </div>
      </div>

      <!-- Day columns -->
      <div
        v-for="day in weekDays"
        :key="day.date.toISOString()"
        class="bg-white dark:bg-slate-800 relative"
        :class="{
          'bg-violet-50/30 dark:bg-violet-900/10': day.isToday
        }"
      >
        <!-- Time slot grid -->
        <div
          v-for="hour in timeSlots"
          :key="`${day.date.toISOString()}-${hour}`"
          class="h-16 border-b border-slate-200 dark:border-slate-700 relative cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors"
          @click="handleTimeSlotClick(day.date, hour)"
        >
          <!-- Appointments for this time slot -->
          <div
            v-for="(appointmentLayout, layoutIndex) in getAppointmentLayoutForTimeSlot(day.date, hour)"
            :key="appointmentLayout.appointment.id"
            class="absolute rounded-sm p-1 cursor-pointer transition-colors border-l-2"
            :class="[
              layoutIndex % 2 === 0 
                ? 'bg-violet-100 dark:bg-violet-900/30 border-violet-500 hover:bg-violet-200 dark:hover:bg-violet-900/50' 
                : 'bg-blue-100 dark:bg-blue-900/30 border-blue-500 hover:bg-blue-200 dark:hover:bg-blue-900/50'
            ]"
            :style="appointmentLayout.style"
            :title="`${getPatientName(appointmentLayout.appointment)} - ${getServiceName(appointmentLayout.appointment)} at ${formatAppointmentTime(appointmentLayout.appointment)} (${appointmentLayout.appointment.slotDurationMinutes || 60} min)`"
            @click.stop="handleAppointmentClick(appointmentLayout.appointment)"
          >
            <div class="flex items-center justify-between h-full min-h-[16px]">
              <div class="flex-1 min-w-0">
                <span 
                  class="text-xs font-medium truncate"
                  :class="layoutIndex % 2 === 0 
                    ? 'text-violet-900 dark:text-violet-100' 
                    : 'text-blue-900 dark:text-blue-100'"
                >
                  {{ getPatientName(appointmentLayout.appointment) }}
                  <span 
                    class="text-xs opacity-75 ml-1"
                    :class="layoutIndex % 2 === 0 
                      ? 'text-violet-700 dark:text-violet-300' 
                      : 'text-blue-700 dark:text-blue-300'"
                  >
                    ({{ getDoctorName(appointmentLayout.appointment) }})
                  </span>
                </span>
              </div>
              <div class="ml-1 flex-shrink-0">
                <span 
                  class="text-xs font-medium"
                  :class="layoutIndex % 2 === 0 
                    ? 'text-violet-600 dark:text-violet-400' 
                    : 'text-blue-600 dark:text-blue-400'"
                >
                  {{ formatAppointmentTime(appointmentLayout.appointment) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!weekAppointments.length" class="text-center py-12">
      <UIcon name="i-lucide-calendar-x" class="h-12 w-12 text-slate-400 mx-auto mb-4" />
      <h3 class="text-lg font-medium text-slate-900 dark:text-white mb-2">
        {{ t('calendar.weekView.noAppointments') }}
      </h3>
      <p class="text-slate-600 dark:text-slate-300">
        {{ t('calendar.dayView.clickToSchedule') }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import type { AppointmentAdmin } from '@/types/appointments';

// Props
interface CalendarWeekViewProps {
  currentDate: Date;
  appointments: AppointmentAdmin[];
  selectedDate?: Date | null;
}

const props = withDefaults(defineProps<CalendarWeekViewProps>(), {
  selectedDate: null
});

// Emits
const emit = defineEmits<{
  dateClick: [date: Date];
  appointmentClick: [appointment: AppointmentAdmin];
}>();

// Composables
const { t, locale } = useI18n();

// Time slots (All 24 hours: 12 AM to 11 PM)
const timeSlots = Array.from({ length: 24 }, (_, i) => i);

// Week days calculation
const weekDays = computed(() => {
  const startOfWeek = new Date(props.currentDate);
  startOfWeek.setDate(props.currentDate.getDate() - props.currentDate.getDay());
  
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  return Array.from({ length: 7 }, (_, i) => {
    const date = new Date(startOfWeek);
    date.setDate(startOfWeek.getDate() + i);
    
    return {
      date,
      dayName: new Intl.DateTimeFormat(locale.value, { weekday: 'short' }).format(date),
      dayNumber: date.getDate(),
      isToday: date.getTime() === today.getTime()
    };
  });
});

// Filter appointments for the current week
const weekAppointments = computed(() => {
  const startOfWeek = new Date(props.currentDate);
  startOfWeek.setDate(props.currentDate.getDate() - props.currentDate.getDay());
  startOfWeek.setHours(0, 0, 0, 0);
  
  const endOfWeek = new Date(startOfWeek);
  endOfWeek.setDate(startOfWeek.getDate() + 7);
  
  return props.appointments.filter(appointment => {
    const appointmentDate = parseAppointmentDate(appointment.scheduledAt);
    return appointmentDate >= startOfWeek && appointmentDate < endOfWeek;
  });
});

// Helper function to safely parse appointment date
const parseAppointmentDate = (scheduledAt: string | Date | number) => {
  if (scheduledAt instanceof Date) {
    return scheduledAt;
  }
  
  // Handle Unix timestamp (number in seconds)
  if (typeof scheduledAt === 'number') {
    // Convert seconds to milliseconds for JavaScript Date
    return new Date(scheduledAt * 1000);
  }
  
  // Handle different string date formats
  if (typeof scheduledAt === 'string') {
    // If it's already an ISO string, parse it directly
    if (scheduledAt.includes('T') || scheduledAt.includes('Z')) {
      return new Date(scheduledAt);
    }
    
    // If it's a timestamp string, convert to number first
    const timestamp = parseInt(scheduledAt);
    if (!isNaN(timestamp)) {
      // Assume it's in seconds if it's a reasonable timestamp
      return new Date(timestamp * 1000);
    }
    
    // Try parsing as regular date string
    return new Date(scheduledAt);
  }
  
  // Fallback to current date if parsing fails
  return new Date();
};

// Debug function to log appointment data
const debugAppointments = () => {
  if (process.env.NODE_ENV === 'development') {
    const startOfWeek = new Date(props.currentDate);
    startOfWeek.setDate(props.currentDate.getDate() - props.currentDate.getDay());
    startOfWeek.setHours(0, 0, 0, 0);
    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 7);
    
    console.log('Week View Debug:', {
      currentDate: props.currentDate,
      totalAppointments: props.appointments.length,
      weekAppointments: weekAppointments.value.length,
      weekRange: {
        start: startOfWeek.toDateString(),
        end: endOfWeek.toDateString()
      },
      weekDays: weekDays.value.map(day => day.date.toDateString()),
      appointments: props.appointments.map(apt => {
        const parsed = parseAppointmentDate(apt.scheduledAt);
        return {
          id: apt.id,
          scheduledAt: apt.scheduledAt,
          scheduledAtParsed: parsed,
          scheduledAtString: parsed.toDateString(),
          patientName: apt.patientName,
          matchesWeek: parsed >= startOfWeek && parsed < endOfWeek
        };
      })
    });
  }
};

// Call debug function when appointments change
watch(() => props.appointments, debugAppointments, { immediate: true });

// Helper functions
const formatHour = (hour: number) => {
  return new Intl.DateTimeFormat(locale.value, {
    hour: 'numeric',
    hour12: true
  }).format(new Date(2000, 0, 1, hour));
};

// Use the new appointment time composable for consistent time handling
const { formatForList } = useAppointmentTime();

const formatAppointmentTime = (appointment: AppointmentAdmin) => {
  // Use the appointment time composable for consistent formatting
  // Convert Unix timestamp (seconds) to milliseconds for the composable
  const timestamp = typeof appointment.scheduledAt === 'number' 
    ? appointment.scheduledAt * 1000 
    : appointment.scheduledAt;
    
  const formatted = formatForList(timestamp);
  if (formatted === "Not scheduled" || formatted === "Invalid date") return "—";
  
  // Extract just the time part for calendar display
  try {
    const parts = formatted.split(',');
    if (parts.length >= 2) {
      // Extract time and timezone: "2:30 PM EET"
      const timePart = parts[parts.length - 1].trim();
      // For week view, show just the time without timezone for compactness
      return timePart.replace(/\s+[A-Z]{2,4}$/, ''); // Remove timezone abbreviation
    }
    return formatted;
  } catch (error) {
    console.warn('Error extracting time part for calendar week view:', error);
    return formatted;
  }
};

const getPatientName = (appointment: AppointmentAdmin) => {
  return appointment.patientName || 'Unknown Patient';
};

const getServiceName = (appointment: AppointmentAdmin) => {
  return appointment.serviceName || 'Unknown Service';
};

const getDoctorName = (appointment: AppointmentAdmin) => {
  return appointment.doctorName || 'Unknown Doctor';
};

const getAppointmentsForTimeSlot = (date: Date, hour: number) => {
  const hourStart = new Date(date);
  hourStart.setHours(hour, 0, 0, 0);
  const hourEnd = new Date(date);
  hourEnd.setHours(hour + 1, 0, 0, 0);
  
  return props.appointments.filter(appointment => {
    const appointmentDate = parseAppointmentDate(appointment.scheduledAt);
    const durationMinutes = appointment.slotDurationMinutes || 60;
    const appointmentEnd = new Date(appointmentDate.getTime() + durationMinutes * 60000);
    
    // Check if appointment is on the same day
    const appointmentDay = new Date(appointmentDate);
    appointmentDay.setHours(0, 0, 0, 0);
    const targetDay = new Date(date);
    targetDay.setHours(0, 0, 0, 0);
    
    if (appointmentDay.getTime() !== targetDay.getTime()) {
      return false;
    }
    
    // Check if appointment overlaps with this hour slot
    return appointmentDate < hourEnd && appointmentEnd > hourStart;
  });
};

// Calculate appointment layout for week view time slots
const getAppointmentLayoutForTimeSlot = (date: Date, hour: number) => {
  const hourStart = new Date(date);
  hourStart.setHours(hour, 0, 0, 0);
  const hourEnd = new Date(date);
  hourEnd.setHours(hour + 1, 0, 0, 0);
  
  // Get all appointments for this day that might overlap with this hour
  const dayAppointments = props.appointments.filter(appointment => {
    const appointmentDate = parseAppointmentDate(appointment.scheduledAt);
    const appointmentDay = new Date(appointmentDate);
    appointmentDay.setHours(0, 0, 0, 0);
    const targetDay = new Date(date);
    targetDay.setHours(0, 0, 0, 0);
    
    return appointmentDay.getTime() === targetDay.getTime();
  }).map(appointment => {
    const appointmentDate = parseAppointmentDate(appointment.scheduledAt);
    const durationMinutes = appointment.slotDurationMinutes || 60;
    const appointmentEnd = new Date(appointmentDate.getTime() + durationMinutes * 60000);
    
    return {
      ...appointment,
      startTime: appointmentDate,
      endTime: appointmentEnd,
      durationMinutes: durationMinutes
    };
  });
  
  // Filter appointments that overlap with this hour
  const hourAppointments = dayAppointments.filter(appointment => {
    return appointment.startTime < hourEnd && appointment.endTime > hourStart;
  });
  
  if (hourAppointments.length === 0) {
    return [];
  }
  
  // Sort appointments by start time
  const sortedAppointments = hourAppointments.sort((a, b) => {
    return a.startTime.getTime() - b.startTime.getTime();
  });
  
  // Proper overlap detection - create rows for non-overlapping appointments
  const appointmentRows: any[][] = [];
  
  sortedAppointments.forEach(appointment => {
    let placedInRow = false;
    
    // Try to find a row where this appointment doesn't overlap with any existing appointment
    for (let rowIndex = 0; rowIndex < appointmentRows.length; rowIndex++) {
      const row = appointmentRows[rowIndex];
      let hasOverlap = false;
      
      // Check if this appointment overlaps with any appointment in this row
      for (const existingAppointment of row) {
        if (appointment.startTime < existingAppointment.endTime && 
            appointment.endTime > existingAppointment.startTime) {
          hasOverlap = true;
          break;
        }
      }
      
      // If no overlap, place the appointment in this row
      if (!hasOverlap) {
        row.push(appointment);
        (appointment as any).rowIndex = rowIndex;
        (appointment as any).positionInRow = row.length - 1;
        placedInRow = true;
        break;
      }
    }
    
    // If couldn't place in any existing row, create a new row
    if (!placedInRow) {
      appointmentRows.push([appointment]);
      (appointment as any).rowIndex = appointmentRows.length - 1;
      (appointment as any).positionInRow = 0;
    }
  });
  
  return sortedAppointments.map((appointment, index) => {
    const appointmentDate = appointment.startTime;
    const minutes = appointmentDate.getMinutes();
    const hourOfAppointment = appointmentDate.getHours();
    
    // Calculate position within the hour slot (48px height)
    let topOffset;
    if (hourOfAppointment === hour) {
      // Appointment starts in this hour
      topOffset = (minutes / 60) * 64; // 64px is the height of each hour slot
    } else {
      // Appointment started in a previous hour
      topOffset = 0;
    }
    
    // Calculate height based on actual appointment duration
    let height;
    if (hourOfAppointment === hour) {
      // Calculate how much of the appointment fits in this hour
      const appointmentEndInThisHour = Math.min(appointment.endTime.getTime(), hourEnd.getTime());
      const appointmentStartInThisHour = Math.max(appointment.startTime.getTime(), hourStart.getTime());
      const minutesInThisHour = (appointmentEndInThisHour - appointmentStartInThisHour) / (1000 * 60);
      height = Math.max(20, (minutesInThisHour / 60) * 64); // Minimum 20px height for single line
    } else {
      // This is a continuation from a previous hour
      const appointmentEndInThisHour = Math.min(appointment.endTime.getTime(), hourEnd.getTime());
      const minutesInThisHour = (appointmentEndInThisHour - hourStart.getTime()) / (1000 * 60);
      height = Math.max(20, (minutesInThisHour / 60) * 64); // Minimum 20px height for single line
    }
    
    // Calculate position based on row stacking
    const rowIndex = (appointment as any).rowIndex;
    const appointmentHeight = 18; // Fixed height for each appointment in week view
    const rowGap = 1; // Gap between stacked appointments
    
    // Adjust top position to stack appointments vertically by row
    const stackedTopOffset = topOffset + (rowIndex * (appointmentHeight + rowGap));
    
    // Full width for each appointment
    const appointmentWidth = 96; // 96% width with 2% margins
    const leftOffset = '2%';
    
    return {
      appointment,
      style: {
        top: `${stackedTopOffset}px`,
        height: `${appointmentHeight}px`,
        left: leftOffset,
        width: `${appointmentWidth}%`,
        zIndex: 10 + index,
        right: 'auto'
      }
    };
  });
};

// Event handlers
const handleTimeSlotClick = (date: Date, hour: number) => {
  emit('dateClick', date);
};

const handleAppointmentClick = (appointment: AppointmentAdmin) => {
  emit('appointmentClick', appointment);
};
</script>

<style scoped>
.calendar-week-view {
  /* Ensure proper scrolling on mobile */
  overflow-x: auto;
  min-width: 100%;
}

/* Mobile responsive adjustments */
@media (max-width: 768px) {
  .calendar-week-view {
    font-size: 0.875rem;
  }
  
  .calendar-week-view .grid-cols-8 {
    min-width: 640px;
  }
}

/* RTL support */
[dir="rtl"] .calendar-week-view .grid {
  direction: ltr; /* Keep time slots on the left */
}
</style>