<template>
  <div class="space-y-4">
    <div>
      <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
        {{ t('settings.timezone.label') }}
      </label>
      <USelect
        v-model="selectedTimezone"
        :options="timezoneOptions"
        value-attribute="value"
        option-attribute="label"
        :placeholder="t('settings.timezone.placeholder')"
        size="lg"
        icon="i-lucide-globe"
        :loading="loading"
        @change="handleTimezoneChange"
      />
    </div>
    
    <div v-if="selectedTimezone" class="bg-slate-50 dark:bg-slate-800 rounded-lg p-4">
      <div class="flex items-center gap-3 mb-3">
        <UIcon name="i-lucide-clock" class="h-5 w-5 text-slate-500" />
        <h4 class="text-sm font-medium text-slate-900 dark:text-white">
          {{ t('settings.timezone.preview.title') }}
        </h4>
      </div>
      
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
        <div>
          <p class="text-slate-500 dark:text-slate-400">{{ t('settings.timezone.preview.current') }}</p>
          <p class="font-medium text-slate-900 dark:text-white">{{ currentTime }}</p>
        </div>
        <div>
          <p class="text-slate-500 dark:text-slate-400">{{ t('settings.timezone.preview.offset') }}</p>
          <p class="font-medium text-slate-900 dark:text-white">{{ currentOffset }}</p>
        </div>
      </div>
    </div>
    
    <div class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
      <div class="flex items-start gap-3">
        <UIcon name="i-lucide-info" class="h-5 w-5 text-blue-600 dark:text-blue-400 mt-0.5" />
        <div class="text-sm">
          <p class="font-medium text-blue-900 dark:text-blue-100 mb-1">
            {{ t('settings.timezone.info.title') }}
          </p>
          <p class="text-blue-700 dark:text-blue-200">
            {{ t('settings.timezone.info.description') }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue';

interface TimezoneOption {
  value: string;
  label: string;
  offset: string;
}

const props = defineProps<{
  modelValue?: string;
  loading?: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string];
  'change': [value: string];
}>();

const { t } = useI18n();
const selectedTimezone = ref(props.modelValue || 'Asia/Amman');
const currentTime = ref('');
const currentOffset = ref('');

// Common timezone options for Middle East clinics
const timezoneOptions: TimezoneOption[] = [
  { value: 'Asia/Amman', label: 'Jordan (Amman)', offset: 'UTC+3' },
  { value: 'Asia/Dubai', label: 'UAE (Dubai)', offset: 'UTC+4' },
  { value: 'Asia/Riyadh', label: 'Saudi Arabia (Riyadh)', offset: 'UTC+3' },
  { value: 'Asia/Kuwait', label: 'Kuwait', offset: 'UTC+3' },
  { value: 'Asia/Bahrain', label: 'Bahrain', offset: 'UTC+3' },
  { value: 'Asia/Qatar', label: 'Qatar', offset: 'UTC+3' },
  { value: 'Asia/Baghdad', label: 'Iraq (Baghdad)', offset: 'UTC+3' },
  { value: 'Asia/Beirut', label: 'Lebanon (Beirut)', offset: 'UTC+2/+3' },
  { value: 'Asia/Damascus', label: 'Syria (Damascus)', offset: 'UTC+2/+3' },
  { value: 'Africa/Cairo', label: 'Egypt (Cairo)', offset: 'UTC+2' },
  { value: 'Europe/Istanbul', label: 'Turkey (Istanbul)', offset: 'UTC+3' },
  { value: 'Europe/London', label: 'United Kingdom (London)', offset: 'UTC+0/+1' },
  { value: 'Europe/Paris', label: 'France (Paris)', offset: 'UTC+1/+2' },
  { value: 'America/New_York', label: 'USA (New York)', offset: 'UTC-5/-4' },
  { value: 'America/Los_Angeles', label: 'USA (Los Angeles)', offset: 'UTC-8/-7' }
];

const updatePreview = () => {
  if (!selectedTimezone.value) return;
  
  try {
    const now = new Date();
    
    // Format current time in selected timezone
    currentTime.value = now.toLocaleString('en-US', {
      timeZone: selectedTimezone.value,
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: true
    });
    
    // Get timezone offset
    const formatter = new Intl.DateTimeFormat('en', {
      timeZone: selectedTimezone.value,
      timeZoneName: 'longOffset'
    });
    
    const parts = formatter.formatToParts(now);
    const offsetPart = parts.find(part => part.type === 'timeZoneName');
    currentOffset.value = offsetPart?.value || 'Unknown';
    
  } catch (error) {
    console.error('Error updating timezone preview:', error);
    currentTime.value = 'Invalid timezone';
    currentOffset.value = 'Unknown';
  }
};

const handleTimezoneChange = (value: string) => {
  selectedTimezone.value = value;
  emit('update:modelValue', value);
  emit('change', value);
  updatePreview();
};

// Update preview when timezone changes
watch(selectedTimezone, updatePreview, { immediate: true });

// Update preview every second
let intervalId: ReturnType<typeof setInterval>;

onMounted(() => {
  updatePreview();
  intervalId = setInterval(updatePreview, 1000);
});

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId);
  }
});

// Watch for prop changes
watch(() => props.modelValue, (newValue) => {
  if (newValue && newValue !== selectedTimezone.value) {
    selectedTimezone.value = newValue;
    updatePreview();
  }
});
</script>