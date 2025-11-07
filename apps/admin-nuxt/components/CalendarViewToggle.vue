<template>
  <div class="calendar-view-toggle">
    <div class="flex items-center gap-1 bg-slate-100 dark:bg-slate-700 rounded-lg p-1">
      <button
        v-for="mode in viewModes"
        :key="mode.value"
        :class="[
          'flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-all duration-200',
          'focus:outline-none focus:ring-2 focus:ring-violet-500 focus:ring-offset-2',
          'dark:focus:ring-offset-slate-800',
          currentViewMode === mode.value
            ? 'bg-white dark:bg-slate-600 text-violet-600 dark:text-violet-400 shadow-sm'
            : 'text-slate-600 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white hover:bg-white/50 dark:hover:bg-slate-600/50'
        ]"
        @click="handleViewModeChange(mode.value)"
        :aria-label="t('calendar.viewModes.switchTo', { mode: mode.label })"
        :aria-pressed="currentViewMode === mode.value"
      >
        <UIcon :name="mode.icon" class="h-4 w-4" />
        <span v-if="!mobile">{{ mode.label }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

// Props
interface CalendarViewToggleProps {
  currentViewMode: 'month' | 'week' | 'day';
  mobile?: boolean;
}

const props = withDefaults(defineProps<CalendarViewToggleProps>(), {
  mobile: false
});

// Emits
const emit = defineEmits<{
  viewModeChange: [mode: 'month' | 'week' | 'day'];
}>();

// Composables
const { t } = useI18n();

// View modes configuration
const viewModes = computed(() => [
  {
    value: 'month' as const,
    label: t('calendar.viewModes.month'),
    icon: 'i-lucide-calendar'
  },
  {
    value: 'week' as const,
    label: t('calendar.viewModes.week'),
    icon: 'i-lucide-calendar-days'
  },
  {
    value: 'day' as const,
    label: t('calendar.viewModes.day'),
    icon: 'i-lucide-calendar-clock'
  }
]);

// Event handlers
const handleViewModeChange = (mode: 'month' | 'week' | 'day') => {
  if (mode !== props.currentViewMode) {
    emit('viewModeChange', mode);
  }
};

// Keyboard navigation support
const handleKeydown = (event: KeyboardEvent, mode: 'month' | 'week' | 'day') => {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault();
    handleViewModeChange(mode);
  }
};
</script>

<style scoped>
.calendar-view-toggle {
  /* Ensure proper focus management */
  isolation: isolate;
}

/* Mobile responsive adjustments */
@media (max-width: 640px) {
  .calendar-view-toggle .flex {
    gap: 0.25rem;
  }
  
  .calendar-view-toggle button {
    padding: 0.5rem;
    min-width: 2.5rem;
  }
}

/* RTL support */
[dir="rtl"] .calendar-view-toggle .flex {
  direction: ltr; /* Keep button order consistent */
}
</style>