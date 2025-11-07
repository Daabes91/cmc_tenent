<template>
  <div class="calendar-day-view">
    <!-- Day Header -->
    <div class="bg-white dark:bg-slate-800 border-b border-slate-200 dark:border-slate-700 p-4 mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-lg font-semibold text-slate-900 dark:text-white">
            {{ dayTitle }}
          </h3>
          <p class="text-sm text-slate-600 dark:text-slate-300">
            {{ daySubtitle }}
          </p>

        </div>
        <div class="flex items-center gap-2">
          <span class="text-sm text-slate-500 dark:text-slate-400">
            {{ appointmentCount }}
          </span>
          <UButton
            color="violet"
            size="sm"
            icon="i-lucide-plus"
            @click="handleNewAppointment"
          >
            {{ t('calendar.dayModal.newAppointment') }}
          </UButton>
        </div>
      </div>
    </div>

    <!-- Time Grid -->
    <div class="max-h-[600px] overflow-y-auto rounded-lg border border-slate-200 dark:border-slate-700">
      <div class="grid grid-cols-1 gap-px bg-slate-200 dark:bg-slate-700">
      <div
        v-for="hour in timeSlots"
        :key="hour"
        class="bg-white dark:bg-slate-800 relative"
      >
        <!-- Hour Header -->
        <div class="flex">
          <div class="w-20 flex-shrink-0 p-3 border-r border-slate-200 dark:border-slate-700">
            <span class="text-sm font-medium text-slate-500 dark:text-slate-400">
              {{ formatHour(hour) }}
            </span>
          </div>
          
          <!-- Hour Content -->
          <div class="flex-1 min-h-[80px] relative">
            <!-- Time slot click area -->
            <div
              class="absolute inset-0 cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors"
              @click="handleTimeSlotClick(hour)"
            >
              <!-- Appointments for this hour -->
              <div
                v-for="(appointmentLayout, layoutIndex) in getAppointmentLayout(hour)"
                :key="appointmentLayout.appointment.id"
                class="absolute rounded-md p-2 cursor-pointer transition-all duration-200 shadow-sm border"
                :class="[
                  layoutIndex % 2 === 0 
                    ? 'bg-violet-100 dark:bg-violet-900/30 border-violet-300 dark:border-violet-700 hover:bg-violet-200 dark:hover:bg-violet-900/50' 
                    : 'bg-blue-100 dark:bg-blue-900/30 border-blue-300 dark:border-blue-700 hover:bg-blue-200 dark:hover:bg-blue-900/50',
                  'hover:shadow-md hover:scale-[1.02]'
                ]"
                :style="appointmentLayout.style"
                :title="`${getPatientName(appointmentLayout.appointment)} - ${getServiceName(appointmentLayout.appointment)} at ${formatAppointmentTime(appointmentLayout.appointment)} (${appointmentLayout.appointment.slotDurationMinutes || 60} min)`"
                @click.stop="handleAppointmentClick(appointmentLayout.appointment)"
              >
                <div class="flex items-center justify-between h-full min-h-[20px]">
                  <div class="flex-1 min-w-0">
                    <div 
                      class="font-medium truncate text-xs"
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
                    </div>
                  </div>
                  <div class="ml-2 flex-shrink-0">
                    <div 
                      class="text-xs font-medium"
                      :class="layoutIndex % 2 === 0 
                        ? 'text-violet-600 dark:text-violet-400' 
                        : 'text-blue-600 dark:text-blue-400'"
                    >
                      {{ formatAppointmentTime(appointmentLayout.appointment) }}
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- Multiple appointments indicator -->
              <div
                v-if="getAppointmentsForHour(hour).length > 2"
                class="absolute top-1 right-1 bg-slate-600 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold z-20"
              >
                {{ getAppointmentsForHour(hour).length }}
              </div>
              
              <!-- Empty slot hint -->
              <div
                v-if="!getAppointmentsForHour(hour).length"
                class="absolute inset-0 flex items-center justify-center opacity-0 hover:opacity-100 transition-opacity"
              >
                <span class="text-sm text-slate-400 dark:text-slate-500">
                  {{ t('calendar.dayView.clickToSchedule') }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!dayAppointments.length" class="text-center py-12">
      <UIcon name="i-lucide-calendar-x" class="h-12 w-12 text-slate-400 mx-auto mb-4" />
      <h3 class="text-lg font-medium text-slate-900 dark:text-white mb-2">
        {{ t('calendar.dayView.noAppointments') }}
      </h3>
      <p class="text-slate-600 dark:text-slate-300 mb-4">
        {{ t('calendar.dayView.clickToSchedule') }}
      </p>
      <UButton
        color="violet"
        icon="i-lucide-plus"
        @click="handleNewAppointment"
      >
        {{ t('calendar.dayModal.newAppointment') }}
      </UButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import type { AppointmentAdmin } from '@/types/appointments';

// Props
interface CalendarDayViewProps {
  currentDate: Date;
  appointments: AppointmentAdmin[];
}

const props = defineProps<CalendarDayViewProps>();

// Emits
const emit = defineEmits<{
  timeSlotClick: [date: Date, hour: number];
  appointmentClick: [appointment: AppointmentAdmin];
}>();

// Composables
const { t, locale } = useI18n();

// Time slots (All 24 hours: 12 AM to 11 PM)
const timeSlots = Array.from({ length: 24 }, (_, i) => i);

// Computed properties
const dayTitle = computed(() => {
  return new Intl.DateTimeFormat(locale.value, {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  }).format(props.currentDate);
});

const daySubtitle = computed(() => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const currentDay = new Date(props.currentDate);
  currentDay.setHours(0, 0, 0, 0);
  
  if (currentDay.getTime() === today.getTime()) {
    return t('calendar.navigation.today');
  }
  
  const tomorrow = new Date(today);
  tomorrow.setDate(today.getDate() + 1);
  
  if (currentDay.getTime() === tomorrow.getTime()) {
    return 'Tomorrow';
  }
  
  const yesterday = new Date(today);
  yesterday.setDate(today.getDate() - 1);
  
  if (currentDay.getTime() === yesterday.getTime()) {
    return 'Yesterday';
  }
  
  return '';
});

// Filter appointments for the current day
const dayAppointments = computed(() => {
  const targetDay = new Date(props.currentDate);
  targetDay.setHours(0, 0, 0, 0);
  const nextDay = new Date(targetDay);
  nextDay.setDate(targetDay.getDate() + 1);
  
  return props.appointments.filter(appointment => {
    const appointmentDate = parseAppointmentDate(appointment.scheduledAt);
    return appointmentDate >= targetDay && appointmentDate < nextDay;
  });
});

const appointmentCount = computed(() => {
  const count = dayAppointments.value.length;
  return count === 1 ? '1 appointment' : `${count} appointments`;
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
    const targetDay = new Date(props.currentDate);
    targetDay.setHours(0, 0, 0, 0);
    const nextDay = new Date(targetDay);
    nextDay.setDate(targetDay.getDate() + 1);
    
    console.log('Day View Debug:', {
      currentDate: props.currentDate,
      currentDateString: props.currentDate.toDateString(),
      totalAppointments: props.appointments.length,
      dayAppointments: dayAppointments.value.length,
      targetDay: targetDay,
      nextDay: nextDay,
      targetDayString: targetDay.toDateString(),
      nextDayString: nextDay.toDateString(),
      appointments: props.appointments.map(apt => {
        const parsed = parseAppointmentDate(apt.scheduledAt);
        const matchesDay = parsed >= targetDay && parsed < nextDay;
        return {
          id: apt.id,
          scheduledAt: apt.scheduledAt,
          scheduledAtParsed: parsed,
          scheduledAtString: parsed.toDateString(),
          scheduledAtTime: parsed.toTimeString(),
          patientName: apt.patientName,
          matchesDay: matchesDay,
          debugInfo: {
            parsedTime: parsed.getTime(),
            targetTime: targetDay.getTime(),
            nextTime: nextDay.getTime(),
            isAfterTarget: parsed >= targetDay,
            isBeforeNext: parsed < nextDay
          }
        };
      })
    });
    
    // Special debug for the specific timestamp
    if (props.appointments.some(apt => apt.scheduledAt === 1761868800)) {
      const testDate = new Date(1761868800 * 1000);
      console.log('Special Debug for 1761868800:', {
        timestamp: 1761868800,
        converted: testDate,
        dateString: testDate.toDateString(),
        timeString: testDate.toTimeString(),
        currentDateString: props.currentDate.toDateString(),
        matches: testDate.toDateString() === props.currentDate.toDateString()
      });
    }
  }
};

// Call debug function when appointments change
watch(() => props.appointments, debugAppointments, { immediate: true });

// Use the new appointment time composable for consistent time handling
const { formatForList, timezone } = useAppointmentTime();

// Helper functions
const formatHour = (hour: number) => {
  // Create a date with the specific hour in clinic timezone
  const date = new Date(2000, 0, 1, hour);
  return date.toLocaleTimeString(locale.value, {
    hour: 'numeric',
    hour12: true,
    timeZone: timezone.value
  });
};

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
      return timePart;
    }
    return formatted;
  } catch (error) {
    console.warn('Error extracting time part for calendar day view:', error);
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

const getAppointmentsForHour = (hour: number) => {
  const hourStart = new Date(props.currentDate);
  hourStart.setHours(hour, 0, 0, 0);
  const hourEnd = new Date(props.currentDate);
  hourEnd.setHours(hour + 1, 0, 0, 0);
  
  return props.appointments.filter(appointment => {
    const appointmentDate = parseAppointmentDate(appointment.scheduledAt);
    const durationMinutes = appointment.slotDurationMinutes || 60;
    const appointmentEnd = new Date(appointmentDate.getTime() + durationMinutes * 60000);
    
    // Check if appointment is on the same day
    const appointmentDay = new Date(appointmentDate);
    appointmentDay.setHours(0, 0, 0, 0);
    const targetDay = new Date(props.currentDate);
    targetDay.setHours(0, 0, 0, 0);
    
    if (appointmentDay.getTime() !== targetDay.getTime()) {
      return false;
    }
    
    // Check if appointment overlaps with this hour slot
    return appointmentDate < hourEnd && appointmentEnd > hourStart;
  });
};

// Get all appointments that span across multiple hours for proper layout calculation
const getAllDayAppointments = () => {
  return dayAppointments.value.map(appointment => {
    const appointmentDate = parseAppointmentDate(appointment.scheduledAt);
    const durationMinutes = appointment.slotDurationMinutes || 60; // Default to 60 minutes
    const endTime = new Date(appointmentDate.getTime() + durationMinutes * 60000);
    
    return {
      ...appointment,
      startTime: appointmentDate,
      endTime: endTime,
      durationMinutes: durationMinutes
    };
  });
};

// Calculate appointment positioning with collision detection
const getAppointmentLayout = (hour: number) => {
  const hourStart = new Date(props.currentDate);
  hourStart.setHours(hour, 0, 0, 0);
  const hourEnd = new Date(props.currentDate);
  hourEnd.setHours(hour + 1, 0, 0, 0);
  
  // Get all appointments that intersect with this hour
  const allDayAppointments = getAllDayAppointments();
  const hourAppointments = allDayAppointments.filter(appointment => {
    // Check if appointment overlaps with this hour
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
    
    // Calculate position within the hour slot
    let minuteOffset;
    if (hourOfAppointment === hour) {
      // Appointment starts in this hour
      minuteOffset = (minutes / 60) * 80; // 80px is the height of each hour slot
    } else {
      // Appointment started in a previous hour
      minuteOffset = 0;
    }
    
    // Calculate height based on actual appointment duration
    const durationMinutes = appointment.durationMinutes;
    let height;
    
    if (hourOfAppointment === hour) {
      // Calculate how much of the appointment fits in this hour
      const appointmentEndInThisHour = Math.min(appointment.endTime.getTime(), hourEnd.getTime());
      const appointmentStartInThisHour = Math.max(appointment.startTime.getTime(), hourStart.getTime());
      const minutesInThisHour = (appointmentEndInThisHour - appointmentStartInThisHour) / (1000 * 60);
      height = Math.max(24, (minutesInThisHour / 60) * 80); // Minimum 24px height for single line
    } else {
      // This is a continuation from a previous hour
      const appointmentEndInThisHour = Math.min(appointment.endTime.getTime(), hourEnd.getTime());
      const minutesInThisHour = (appointmentEndInThisHour - hourStart.getTime()) / (1000 * 60);
      height = Math.max(24, (minutesInThisHour / 60) * 80); // Minimum 24px height for single line
    }
    
    // Calculate position based on row stacking
    const rowIndex = (appointment as any).rowIndex;
    const appointmentHeight = 24; // Fixed height for each appointment
    const rowGap = 2; // Gap between stacked appointments
    
    // Adjust top position to stack appointments vertically by row
    const stackedTopOffset = minuteOffset + (rowIndex * (appointmentHeight + rowGap));
    
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

const getAppointmentStyle = (appointment: AppointmentAdmin, hour: number) => {
  const appointmentDate = parseAppointmentDate(appointment.scheduledAt);
  const minutes = appointmentDate.getMinutes();
  
  // Calculate position within the hour slot
  const minuteOffset = (minutes / 60) * 60; // 60px is the height of each hour slot
  
  // Calculate height based on appointment duration (default to 1 hour if not specified)
  const durationMinutes = 60; // Default duration
  const height = Math.max(35, (durationMinutes / 60) * 60); // Minimum 35px height
  
  return {
    top: `${minuteOffset}px`,
    height: `${height}px`,
    zIndex: 10
  };
};

const getStatusClass = (status: string) => {
  switch (status?.toLowerCase()) {
    case 'confirmed':
      return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300';
    case 'scheduled':
      return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300';
    case 'completed':
      return 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300';
    case 'cancelled':
      return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300';
    default:
      return 'bg-slate-100 text-slate-800 dark:bg-slate-900/30 dark:text-slate-300';
  }
};

const getStatusLabel = (status: string) => {
  return t(`appointments.status.${status?.toLowerCase()}`) || status;
};

// Event handlers
const handleTimeSlotClick = (hour: number) => {
  emit('timeSlotClick', props.currentDate, hour);
};

const handleAppointmentClick = (appointment: AppointmentAdmin) => {
  emit('appointmentClick', appointment);
};

const handleNewAppointment = () => {
  const dateStr = props.currentDate.toISOString().split('T')[0];
  // This will be handled by the parent component
  emit('timeSlotClick', props.currentDate, 9); // Default to 9 AM
};
</script>

<style scoped>
.calendar-day-view {
  /* Ensure proper layout */
  min-height: 600px;
}

/* Mobile responsive adjustments */
@media (max-width: 768px) {
  .calendar-day-view .w-20 {
    width: 4rem;
  }
  
  .calendar-day-view .min-h-[60px] {
    min-height: 50px;
  }
}

/* RTL support */
[dir="rtl"] .calendar-day-view .border-r {
  border-right: none;
  border-left: 1px solid;
}

[dir="rtl"] .calendar-day-view .border-l-4 {
  border-left: none;
  border-right: 4px solid;
}

[dir="rtl"] .calendar-day-view .rounded-r-md {
  border-radius: 0 0.375rem 0.375rem 0;
  border-radius: 0.375rem 0 0 0.375rem;
}
</style>