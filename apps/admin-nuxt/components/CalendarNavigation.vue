<template>
  <div class="calendar-navigation px-6 py-4" style="background: #366e85;">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-3">
        <UIcon name="i-lucide-calendar" class="h-5 w-5 text-white" />
        <div>
          <h2 class="text-lg font-semibold text-white">{{ currentPeriodDisplay }}</h2>
          <p class="text-sm text-slate-200">{{ subtitle }}</p>
        </div>
      </div>
      <div class="flex items-center gap-3">
        <!-- View Mode Toggle -->
        <div class="hidden sm:block">
          <CalendarViewToggle
            :current-view-mode="viewMode"
            :mobile="false"
            @view-mode-change="handleViewModeChange"
          />
        </div>
        
        <!-- Navigation Controls -->
        <div class="flex items-center gap-2">
          <UButton
            icon="i-lucide-chevron-left"
            variant="ghost"
            :size="mobile ? 'xs' : 'sm'"
            class="text-white hover:bg-white/10 touch-manipulation"
            @click="handlePrevious"
            :aria-label="previousAriaLabel"
          />
          <UButton
            variant="soft"
            :size="mobile ? 'xs' : 'sm'"
            class="bg-white/20 text-white hover:bg-white/30 touch-manipulation"
            :class="mobile ? 'min-w-[60px] text-xs' : 'min-w-[80px]'"
            @click="handleToday"
          >
            {{ mobile ? t("calendar.navigation.today").slice(0, 3) : t("calendar.navigation.today") }}
          </UButton>
          <UButton
            icon="i-lucide-chevron-right"
            variant="ghost"
            :size="mobile ? 'xs' : 'sm'"
            class="text-white hover:bg-white/10 touch-manipulation"
            @click="handleNext"
            :aria-label="nextAriaLabel"
          />
        </div>
      </div>
    </div>
    
    <!-- Mobile View Mode Toggle -->
    <div class="sm:hidden mt-3 pt-3 border-t border-white/20">
      <CalendarViewToggle
        :current-view-mode="viewMode"
        :mobile="true"
        @view-mode-change="handleViewModeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

// Props
interface CalendarNavigationProps {
  currentDate: Date;
  viewMode: 'month' | 'week' | 'day';
  subtitle?: string;
  mobile?: boolean;
}

const props = withDefaults(defineProps<CalendarNavigationProps>(), {
  subtitle: '',
  mobile: false
});

// Emits
const emit = defineEmits<{
  dateChange: [date: Date];
  todayClick: [];
  viewModeChange: [mode: 'month' | 'week' | 'day'];
}>();

// Composables
const { t, locale } = useI18n();

// Computed properties
const currentPeriodDisplay = computed(() => {
  const date = props.currentDate;
  
  switch (props.viewMode) {
    case 'month':
      return new Intl.DateTimeFormat(locale.value, {
        month: 'long',
        year: 'numeric'
      }).format(date);
      
    case 'week': {
      const startOfWeek = new Date(date);
      startOfWeek.setDate(date.getDate() - date.getDay());
      const endOfWeek = new Date(startOfWeek);
      endOfWeek.setDate(startOfWeek.getDate() + 6);
      
      const startMonth = new Intl.DateTimeFormat(locale.value, { month: 'short' }).format(startOfWeek);
      const endMonth = new Intl.DateTimeFormat(locale.value, { month: 'short' }).format(endOfWeek);
      const year = date.getFullYear();
      
      if (startOfWeek.getMonth() === endOfWeek.getMonth()) {
        return `${startOfWeek.getDate()}-${endOfWeek.getDate()} ${startMonth} ${year}`;
      } else {
        return `${startOfWeek.getDate()} ${startMonth} - ${endOfWeek.getDate()} ${endMonth} ${year}`;
      }
    }
      
    case 'day':
      return new Intl.DateTimeFormat(locale.value, {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      }).format(date);
      
    default:
      return '';
  }
});

const previousAriaLabel = computed(() => {
  switch (props.viewMode) {
    case 'month':
      return t('calendar.navigation.previousMonth');
    case 'week':
      return t('calendar.navigation.previousWeek');
    case 'day':
      return t('calendar.navigation.previousDay');
    default:
      return '';
  }
});

const nextAriaLabel = computed(() => {
  switch (props.viewMode) {
    case 'month':
      return t('calendar.navigation.nextMonth');
    case 'week':
      return t('calendar.navigation.nextWeek');
    case 'day':
      return t('calendar.navigation.nextDay');
    default:
      return '';
  }
});

// Event handlers
const handlePrevious = () => {
  const newDate = new Date(props.currentDate);
  
  switch (props.viewMode) {
    case 'month':
      newDate.setMonth(newDate.getMonth() - 1);
      break;
    case 'week':
      newDate.setDate(newDate.getDate() - 7);
      break;
    case 'day':
      newDate.setDate(newDate.getDate() - 1);
      break;
  }
  
  emit('dateChange', newDate);
};

const handleNext = () => {
  const newDate = new Date(props.currentDate);
  
  switch (props.viewMode) {
    case 'month':
      newDate.setMonth(newDate.getMonth() + 1);
      break;
    case 'week':
      newDate.setDate(newDate.getDate() + 7);
      break;
    case 'day':
      newDate.setDate(newDate.getDate() + 1);
      break;
  }
  
  emit('dateChange', newDate);
};

const handleToday = () => {
  emit('todayClick');
};

const handleViewModeChange = (mode: 'month' | 'week' | 'day') => {
  emit('viewModeChange', mode);
};
</script>

<style scoped>
.calendar-navigation {
  background: #366e85;
}
</style>