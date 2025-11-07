<template>
  <div class="flex items-center gap-3" :class="{ 'flex-col items-start gap-2': mobile }">
    <div class="flex items-center gap-2" :class="{ 'w-full': mobile }">
      <UIcon name="i-lucide-user-check" class="h-4 w-4 text-slate-500 dark:text-slate-400" />
      <span class="text-sm font-medium text-slate-700 dark:text-slate-300">
        {{ t('calendar.filter.doctor.label') }}
      </span>
    </div>
    
    <div class="relative" :class="mobile ? 'w-full' : 'min-w-[200px]'">
      <!-- Loading indicator -->
      <div v-if="loading" class="absolute inset-y-0 left-3 flex items-center pointer-events-none">
        <UIcon name="i-lucide-loader-2" class="h-4 w-4 text-slate-400 animate-spin" />
      </div>
      
      <!-- Doctor icon -->
      <div v-else class="absolute inset-y-0 left-3 flex items-center pointer-events-none">
        <UIcon name="i-lucide-stethoscope" class="h-4 w-4 text-slate-400" />
      </div>
      
      <!-- Native select dropdown -->
      <select
        v-model="selectedDoctorId"
        :disabled="loading"
        class="w-full pl-10 pr-10 py-2.5 text-sm bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-600 rounded-lg shadow-sm focus:ring-2 focus:ring-violet-500 focus:border-violet-500 dark:focus:ring-violet-400 dark:focus:border-violet-400 text-slate-900 dark:text-white disabled:opacity-50 disabled:cursor-not-allowed appearance-none"
        :class="{
          'cursor-pointer': !loading,
          'cursor-not-allowed opacity-50': loading
        }"
      >
        <option :value="null">
          {{ t('calendar.filter.doctor.all') }}
        </option>
        <option
          v-for="doctor in sortedDoctors"
          :key="doctor.id"
          :value="doctor.id"
        >
          {{ getDoctorDisplayName(doctor) }}
        </option>
      </select>
      
      <!-- Dropdown arrow -->
      <div class="absolute inset-y-0 right-3 flex items-center pointer-events-none">
        <UIcon name="i-lucide-chevron-down" class="h-4 w-4 text-slate-400" />
      </div>
    </div>
    
    <!-- Clear filter button -->
    <UButton
      v-if="selectedDoctorId"
      variant="ghost"
      color="gray"
      size="sm"
      icon="i-lucide-x"
      @click="clearFilter"
      :title="t('calendar.filter.doctor.clear')"
    />
    
    <!-- Filter indicator -->
    <div v-if="selectedDoctorId" class="flex items-center gap-1">
      <div class="w-2 h-2 bg-violet-500 rounded-full"></div>
      <span class="text-xs text-slate-500 dark:text-slate-400">
        {{ t('calendar.filter.active') }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { DoctorAdmin } from '@/types/doctors';
import { computed, watch } from 'vue';

// Props
interface CalendarDoctorFilterProps {
  doctors: DoctorAdmin[];
  loading?: boolean;
  modelValue?: number | null;
  mobile?: boolean;
}

const props = withDefaults(defineProps<CalendarDoctorFilterProps>(), {
  loading: false,
  modelValue: null,
  mobile: false
});

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: number | null];
  'filter-change': [doctorId: number | null];
}>();

// Composables
const { t, locale } = useI18n();

// Reactive state
const selectedDoctorId = computed({
  get: () => props.modelValue,
  set: (value: any) => {
    // Convert string values from native select to number or null
    const numericValue = value === null || String(value) === 'null' || String(value) === '' ? null : Number(value);
    emit('update:modelValue', numericValue);
    emit('filter-change', numericValue);
  }
});

// Computed properties
const sortedDoctors = computed(() => {
  return [...props.doctors].sort((a, b) => {
    const nameA = getDoctorDisplayName(a);
    const nameB = getDoctorDisplayName(b);
    return nameA.localeCompare(nameB);
  });
});

// Helper function to get doctor display name
const getDoctorDisplayName = (doctor: DoctorAdmin) => {
  const name = locale.value === 'ar' 
    ? (doctor.fullNameAr || doctor.fullNameEn || doctor.fullName || 'Unknown Doctor')
    : (doctor.fullNameEn || doctor.fullNameAr || doctor.fullName || 'Unknown Doctor');
  
  const specialty = locale.value === 'ar'
    ? (doctor.specialtyAr || doctor.specialtyEn || doctor.specialty)
    : (doctor.specialtyEn || doctor.specialtyAr || doctor.specialty);
  
  return specialty ? `${name} - ${specialty}` : name;
};

// Methods
const clearFilter = () => {
  selectedDoctorId.value = null;
};

// Watch for external changes
watch(() => props.modelValue, (newValue) => {
  if (newValue !== selectedDoctorId.value) {
    selectedDoctorId.value = newValue;
  }
});
</script>

<style scoped>
/* Custom styles for the filter component */
.min-w-[200px] {
  min-width: 200px;
}

/* Native select styling */
select {
  background-image: none; /* Remove default arrow */
}

/* Focus styles for better accessibility */
select:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(139, 92, 246, 0.2);
}

/* Dark mode adjustments */
@media (prefers-color-scheme: dark) {
  select {
    color-scheme: dark;
  }
}

/* RTL support */
[dir="rtl"] .flex {
  direction: rtl;
}

[dir="rtl"] .gap-2 {
  gap: 0.5rem;
}

[dir="rtl"] .gap-3 {
  gap: 0.75rem;
}

[dir="rtl"] select {
  padding-left: 2.5rem;
  padding-right: 2.5rem;
}

[dir="rtl"] .absolute.left-3 {
  left: auto;
  right: 0.75rem;
}

[dir="rtl"] .absolute.right-3 {
  right: auto;
  left: 0.75rem;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .min-w-[200px] {
    min-width: auto;
    width: 100%;
  }
}
</style>