<template>
  <UModal v-model="isOpen" :ui="modalUi">
    <UCard :ui="cardUi">
      <template #header>
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold text-slate-900 dark:text-white">
              {{ formattedDate }}
            </h3>
            <p class="text-sm text-slate-600 dark:text-slate-300">
              {{ t('calendar.dayModal.appointmentsCount', { count: day?.events.length || 0 }) }}
            </p>
          </div>
          <UButton
            icon="i-lucide-x"
            variant="ghost"
            size="sm"
            class="text-slate-400 hover:text-slate-600"
            @click="close"
          />
        </div>
      </template>

      <div class="space-y-3 max-h-96 overflow-y-auto">
        <div
          v-for="event in sortedEvents"
          :key="event.id"
          class="p-3 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors cursor-pointer"
          @click="handleEventClick(event)"
        >
          <div class="flex items-start gap-3">
            <!-- Status indicator -->
            <div 
              class="w-3 h-3 rounded-full flex-shrink-0 mt-1"
              :class="getStatusDotClass(event.appointment.status)"
            ></div>
            
            <!-- Event details -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between mb-1">
                <span class="font-medium text-slate-900 dark:text-white">
                  {{ event.timeSlot }}
                </span>
                <UBadge 
                  :color="getStatusColor(event.appointment.status)" 
                  variant="soft" 
                  size="xs"
                >
                  {{ formatStatus(event.appointment.status) }}
                </UBadge>
              </div>
              
              <div class="text-sm text-slate-600 dark:text-slate-300 mb-1">
                {{ event.displayText }}
              </div>
              
              <div v-if="event.appointment.serviceName" class="text-xs text-slate-500 dark:text-slate-400">
                {{ event.appointment.serviceName }}
              </div>
              
              <div v-if="event.appointment.doctorName" class="text-xs text-slate-500 dark:text-slate-400 mt-1">
                {{ t('common.titles.doctorWithName', { name: event.appointment.doctorName }) }}
              </div>
            </div>
            
            <!-- Action button -->
            <UButton
              icon="i-lucide-chevron-right"
              variant="ghost"
              size="xs"
              class="text-slate-400"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex items-center justify-between">
          <UButton
            color="violet"
            icon="i-lucide-plus"
            @click="handleNewAppointment"
          >
            {{ t('calendar.dayModal.newAppointment') }}
          </UButton>
          <UButton
            variant="ghost"
            @click="close"
          >
            {{ t('common.actions.close') }}
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>
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

interface CalendarDay {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
  isSelected: boolean;
  events: CalendarEvent[];
  hasEvents: boolean;
}

// Props
interface CalendarDayModalProps {
  day: CalendarDay | null;
  modelValue: boolean;
}

const props = defineProps<CalendarDayModalProps>();

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  appointmentClick: [appointment: AppointmentAdmin];
  newAppointment: [date: Date];
}>();

// Composables
const { t, locale } = useI18n();
const router = useRouter();

// RTL support
const isRTL = computed(() => locale.value === 'ar');

// Computed properties
const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const formattedDate = computed(() => {
  if (!props.day) return '';
  
  const formatter = new Intl.DateTimeFormat(locale.value, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
  
  return formatter.format(props.day.date);
});

const sortedEvents = computed(() => {
  if (!props.day) return [];
  
  return [...props.day.events].sort((a, b) => 
    new Date(a.appointment.scheduledAt).getTime() - new Date(b.appointment.scheduledAt).getTime()
  );
});

// UI configuration
const modalUi = computed(() => ({
  width: 'sm:max-w-md',
  height: 'h-auto',
  base: isRTL.value ? 'rtl' : 'ltr'
}));

const cardUi = {
  body: { padding: 'px-4 py-5 sm:p-6' },
  header: { padding: 'px-4 py-5 sm:px-6' },
  footer: { padding: 'px-4 py-4 sm:px-6' }
};

// Helper functions
const getStatusColor = (status: string): string => {
  const colors = {
    'SCHEDULED': 'blue',
    'CONFIRMED': 'green',
    'COMPLETED': 'purple',
    'CANCELLED': 'red'
  };
  return colors[status as keyof typeof colors] || 'gray';
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

const formatStatus = (status: string): string => {
  return status.charAt(0).toUpperCase() + status.slice(1).toLowerCase();
};

// Event handlers
const close = () => {
  isOpen.value = false;
};

const handleEventClick = (event: CalendarEvent) => {
  emit('appointmentClick', event.appointment);
  close();
};

const handleNewAppointment = () => {
  if (props.day) {
    emit('newAppointment', props.day.date);
  }
  close();
};
</script>