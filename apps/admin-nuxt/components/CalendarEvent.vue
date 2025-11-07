<template>
  <UTooltip :text="tooltipText" :ui="tooltipUi" :disabled="mobile">
    <div
      class="calendar-event rounded-lg transition-all duration-200 cursor-pointer border border-transparent touch-manipulation"
      :class="[
        eventClasses,
        statusClasses,
        mobile ? 'text-xs p-1' : 'text-xs',
        compact ? 'p-1.5' : 'p-2'
      ]"
      @click="handleClick"
    >
      <!-- Status indicator dot -->
      <div class="flex items-start gap-1.5">
        <div 
          class="w-2 h-2 rounded-full flex-shrink-0 mt-1"
          :class="statusDotClass"
        ></div>
        <div class="flex-1 min-w-0">
          <div class="font-medium truncate" :class="mobile ? 'text-xs' : ''">
            {{ mobile ? event.timeSlot.slice(0, 5) : event.timeSlot }}
          </div>
          <div class="truncate opacity-75" :class="mobile ? 'text-xs' : ''">
            {{ mobile ? event.displayText.slice(0, 10) + (event.displayText.length > 10 ? '...' : '') : event.displayText }}
          </div>
          <div v-if="!compact && !mobile && event.appointment.serviceName" class="truncate text-xs opacity-60 mt-1">
            {{ event.appointment.serviceName }}
          </div>
        </div>
      </div>
    </div>
  </UTooltip>
</template>

<script setup lang="ts">
import type { AppointmentAdmin } from "@/types/appointments";
import { computed } from "vue";

// Calendar event interface
interface CalendarEvent {
  id: string;
  date: Date;
  appointment: AppointmentAdmin;
  displayText: string;
  statusColor: string;
  timeSlot: string;
}

// Props
interface CalendarEventProps {
  event: CalendarEvent;
  compact?: boolean;
  mobile?: boolean;
}

const props = withDefaults(defineProps<CalendarEventProps>(), {
  compact: false,
  mobile: false
});

// Emits
const emit = defineEmits<{
  click: [event: CalendarEvent];
}>();

// Computed properties
const statusColor = computed(() => props.event.statusColor);

const eventClasses = computed(() => {
  const baseClasses = ['calendar-event'];
  
  if (props.compact) {
    baseClasses.push('compact');
  }
  
  return baseClasses;
});

const statusClasses = computed(() => {
  const status = props.event.appointment.status.toLowerCase();
  const baseClasses = [];
  
  switch (status) {
    case 'scheduled':
      baseClasses.push('bg-blue-50 dark:bg-blue-900/20 text-blue-800 dark:text-blue-200');
      baseClasses.push('hover:bg-blue-100 dark:hover:bg-blue-900/30 border-blue-200 dark:border-blue-800');
      break;
    case 'confirmed':
      baseClasses.push('bg-green-50 dark:bg-green-900/20 text-green-800 dark:text-green-200');
      baseClasses.push('hover:bg-green-100 dark:hover:bg-green-900/30 border-green-200 dark:border-green-800');
      break;
    case 'completed':
      baseClasses.push('bg-purple-50 dark:bg-purple-900/20 text-purple-800 dark:text-purple-200');
      baseClasses.push('hover:bg-purple-100 dark:hover:bg-purple-900/30 border-purple-200 dark:border-purple-800');
      break;
    case 'cancelled':
      baseClasses.push('bg-red-50 dark:bg-red-900/20 text-red-800 dark:text-red-200');
      baseClasses.push('hover:bg-red-100 dark:hover:bg-red-900/30 border-red-200 dark:border-red-800');
      break;
    default:
      baseClasses.push('bg-slate-50 dark:bg-slate-900/20 text-slate-800 dark:text-slate-200');
      baseClasses.push('hover:bg-slate-100 dark:hover:bg-slate-900/30 border-slate-200 dark:border-slate-800');
  }
  
  return baseClasses;
});

const statusDotClass = computed(() => {
  const status = props.event.appointment.status.toLowerCase();
  
  switch (status) {
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
});

const tooltipText = computed(() => {
  const lines = [
    `${props.event.timeSlot} - ${props.event.displayText}`,
    `Status: ${formatStatus(props.event.appointment.status)}`
  ];
  
  if (props.event.appointment.serviceName) {
    lines.push(`Service: ${props.event.appointment.serviceName}`);
  }
  
  if (props.event.appointment.doctorName) {
    lines.push(`Doctor: ${props.event.appointment.doctorName}`);
  }
  
  return lines.join('\n');
});

const tooltipUi = {
  background: 'bg-slate-900 dark:bg-slate-800',
  color: 'text-white',
  rounded: 'rounded-lg',
  shadow: 'shadow-lg',
  ring: 'ring-1 ring-slate-200 dark:ring-slate-700'
};

// Helper functions
const formatStatus = (status: string): string => {
  return status.charAt(0).toUpperCase() + status.slice(1).toLowerCase();
};

// Event handlers
const handleClick = (e: Event) => {
  e.stopPropagation();
  emit('click', props.event);
};
</script>

<style scoped>
.calendar-event {
  word-break: break-word;
  overflow-wrap: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.calendar-event.compact {
  font-size: 0.65rem;
  line-height: 1.2;
}

.calendar-event:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.12);
}

.calendar-event:active {
  transform: translateY(0);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

/* Responsive adjustments */
@media (max-width: 640px) {
  .calendar-event.compact {
    font-size: 0.6rem;
    line-height: 1.1;
  }
}
</style>