<template>
  <div class="relative">
    <!-- Compact Date Filter Button -->
    <UButton
      variant="outline"
      color="gray"
      size="sm"
      :disabled="disabled"
      @click="showDropdown = !showDropdown"
      class="min-w-[200px] justify-between"
    >
      <div class="flex items-center gap-2">
        <UIcon name="i-lucide-calendar-range" class="h-4 w-4" />
        <span class="text-sm">{{ displayText }}</span>
      </div>
      <UIcon 
        name="i-lucide-chevron-down" 
        class="h-4 w-4 transition-transform duration-200"
        :class="{ 'rotate-180': showDropdown }"
      />
    </UButton>

    <!-- Dropdown Panel -->
    <div
      v-if="showDropdown"
      class="absolute top-full right-0 mt-2 w-80 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg shadow-lg z-50"
    >
      <div class="p-4 space-y-4">
        <!-- Quick Presets -->
        <div>
          <h4 class="text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
            {{ t('reports.dateFilter.quickSelect') }}
          </h4>
          <div class="grid grid-cols-2 gap-2">
            <UButton
              v-for="preset in presets.slice(0, 6)"
              :key="preset.key"
              variant="ghost"
              color="gray"
              size="xs"
              @click="applyPreset(preset)"
              :class="{ 'bg-blue-50 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300': isActivePreset(preset) }"
              class="justify-start"
            >
              {{ t(`reports.dateFilter.presets.${preset.key}`) }}
            </UButton>
          </div>
        </div>

        <!-- Custom Date Range -->
        <div>
          <h4 class="text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
            {{ t('reports.dateFilter.customRange') }}
          </h4>
          <div class="space-y-3">
            <!-- Start Date -->
            <div>
              <label class="block text-xs text-slate-500 dark:text-slate-400 mb-1">
                {{ t('reports.dateFilter.startDate') }}
              </label>
              <input
                v-model="startDateInput"
                type="date"
                :max="maxStartDate"
                :disabled="disabled"
                class="w-full px-3 py-2 text-sm bg-white dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:focus:ring-blue-400 dark:focus:border-blue-400 text-slate-900 dark:text-white disabled:opacity-50"
              />
            </div>

            <!-- End Date -->
            <div>
              <label class="block text-xs text-slate-500 dark:text-slate-400 mb-1">
                {{ t('reports.dateFilter.endDate') }}
              </label>
              <input
                v-model="endDateInput"
                type="date"
                :min="minEndDate"
                :max="maxEndDate"
                :disabled="disabled"
                class="w-full px-3 py-2 text-sm bg-white dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:focus:ring-blue-400 dark:focus:border-blue-400 text-slate-900 dark:text-white disabled:opacity-50"
              />
            </div>
          </div>
        </div>

        <!-- Error Message -->
        <div v-if="rangeError" class="flex items-center gap-2 text-red-600 dark:text-red-400 text-sm">
          <UIcon name="i-lucide-alert-circle" class="h-4 w-4" />
          <span>{{ rangeError }}</span>
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-between pt-2 border-t border-slate-200 dark:border-slate-700">
          <UButton
            v-if="!isDefaultRange"
            variant="ghost"
            color="gray"
            size="xs"
            @click="resetToDefault"
          >
            {{ t('reports.dateFilter.reset') }}
          </UButton>
          <div v-else></div>
          
          <UButton
            color="blue"
            size="xs"
            @click="showDropdown = false"
          >
            {{ t('reports.dateFilter.apply') }}
          </UButton>
        </div>
      </div>
    </div>

    <!-- Backdrop to close dropdown -->
    <div
      v-if="showDropdown"
      class="fixed inset-0 z-40"
      @click="showDropdown = false"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch, ref } from 'vue'
import { useDateRangeFilter, type DateRange, type DateRangePreset } from '../composables/useDateRangeFilter'
import { formatDateForApi } from '../utils/dateRangeUtils'

// Props
interface DateRangeFilterProps {
  modelValue: DateRange
  disabled?: boolean
  mobile?: boolean
  maxRange?: number // days
}

const props = withDefaults(defineProps<DateRangeFilterProps>(), {
  disabled: false,
  mobile: false,
  maxRange: 730 // 2 years
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: DateRange]
  'change': [value: DateRange]
}>()

// Composables
const { t } = useI18n()
const { presets } = useDateRangeFilter()

// Local state
const showDropdown = ref(false)

// Local validation for the current range
const isValidRange = computed(() => {
  const { startDate, endDate } = props.modelValue
  if (!startDate || !endDate) return false
  
  const start = new Date(startDate)
  const end = new Date(endDate)
  const now = new Date()
  
  return end >= start && end <= now
})

const rangeError = computed(() => {
  const { startDate, endDate } = props.modelValue
  
  if (!startDate || !endDate) {
    return t('reports.dateFilter.errors.requiredFields')
  }
  
  const start = new Date(startDate)
  const end = new Date(endDate)
  const now = new Date()
  
  if (end < start) {
    return t('reports.dateFilter.errors.invalidRange')
  }
  
  if (end > now) {
    return t('reports.dateFilter.errors.futureDate')
  }
  
  const daysDiff = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
  if (daysDiff > props.maxRange) {
    return t('reports.dateFilter.errors.maxRangeExceeded')
  }
  
  return null
})

// Reactive state for date inputs
const startDateInput = computed({
  get: () => props.modelValue.startDate || '',
  set: (value: string) => {
    const newRange = {
      startDate: value || null,
      endDate: props.modelValue.endDate
    }
    emit('update:modelValue', newRange)
    emit('change', newRange)
  }
})

const endDateInput = computed({
  get: () => props.modelValue.endDate || '',
  set: (value: string) => {
    const newRange = {
      startDate: props.modelValue.startDate,
      endDate: value || null
    }
    emit('update:modelValue', newRange)
    emit('change', newRange)
  }
})

// Computed properties for date constraints
const today = computed(() => formatDateForApi(new Date()))

const maxStartDate = computed(() => {
  if (props.modelValue.endDate) {
    return props.modelValue.endDate
  }
  return today.value
})

const minEndDate = computed(() => {
  return props.modelValue.startDate || undefined
})

const maxEndDate = computed(() => today.value)

// Check if current range matches default (current month)
const isDefaultRange = computed(() => {
  const now = new Date()
  const startOfMonth = formatDateForApi(new Date(now.getFullYear(), now.getMonth(), 1))
  const endOfMonth = formatDateForApi(new Date(now.getFullYear(), now.getMonth() + 1, 0))
  
  return props.modelValue.startDate === startOfMonth && 
         props.modelValue.endDate === endOfMonth
})

// Display text for the button
const displayText = computed(() => {
  // Check if it matches a preset
  const activePreset = presets.value.find(preset => isActivePreset(preset))
  if (activePreset) {
    return t(`reports.dateFilter.presets.${activePreset.key}`)
  }
  
  // Show custom range
  if (props.modelValue.startDate && props.modelValue.endDate) {
    const start = new Date(props.modelValue.startDate).toLocaleDateString()
    const end = new Date(props.modelValue.endDate).toLocaleDateString()
    return `${start} - ${end}`
  }
  
  return t('reports.dateFilter.selectRange')
})

// Methods
const applyPreset = (preset: DateRangePreset) => {
  const newRange = { ...preset.value }
  emit('update:modelValue', newRange)
  emit('change', newRange)
  showDropdown.value = false
}

const isActivePreset = (preset: DateRangePreset): boolean => {
  return props.modelValue.startDate === preset.value.startDate &&
         props.modelValue.endDate === preset.value.endDate
}

const resetToDefault = () => {
  const now = new Date()
  const defaultRange: DateRange = {
    startDate: formatDateForApi(new Date(now.getFullYear(), now.getMonth(), 1)),
    endDate: formatDateForApi(new Date(now.getFullYear(), now.getMonth() + 1, 0))
  }
  emit('update:modelValue', defaultRange)
  emit('change', defaultRange)
  showDropdown.value = false
}

// Watch for validation changes and emit validation status
watch(isValidRange, (valid) => {
  // Could emit validation status if needed by parent components
}, { immediate: true })
</script>

<style scoped>
/* Date input styling */
input[type="date"] {
  color-scheme: light;
}

input[type="date"]::-webkit-calendar-picker-indicator {
  opacity: 0.7;
  cursor: pointer;
}

/* Dark mode date input */
@media (prefers-color-scheme: dark) {
  input[type="date"] {
    color-scheme: dark;
  }
}

/* Dropdown animation */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Preset button active state */
.bg-blue-50 {
  background-color: rgb(239 246 255);
}

.text-blue-700 {
  color: rgb(29 78 216);
}

.dark .dark\:bg-blue-900\/30 {
  background-color: rgb(30 58 138 / 0.3);
}

.dark .dark\:text-blue-300 {
  color: rgb(147 197 253);
}

/* RTL support */
[dir="rtl"] .absolute.right-0 {
  right: auto;
  left: 0;
}
</style>