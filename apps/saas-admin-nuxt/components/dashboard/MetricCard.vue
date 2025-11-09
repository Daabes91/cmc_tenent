<template>
  <UCard 
    :ui="{ body: { padding: 'p-6' } }"
    role="article"
    :aria-label="`${title}: ${formattedValue}`"
  >
    <LoadingSkeleton v-if="loading" type="metric" />

    <div v-else>
      <div class="flex items-start justify-between mb-4">
        <h3 
          class="text-sm font-medium text-gray-600 dark:text-gray-400"
          :id="`metric-title-${metricId}`"
        >
          {{ title }}
        </h3>
        <UIcon 
          :name="icon" 
          class="w-8 h-8 text-primary-500"
          aria-hidden="true"
        />
      </div>

      <div class="flex items-baseline gap-2">
        <p 
          class="text-3xl font-bold text-gray-900 dark:text-white"
          :aria-labelledby="`metric-title-${metricId}`"
        >
          {{ formattedValue }}
        </p>
        
        <div 
          v-if="trend" 
          class="flex items-center gap-1 text-sm font-medium"
          :class="trendColorClass"
          :aria-label="trendAriaLabel"
        >
          <UIcon 
            :name="trendIcon" 
            class="w-4 h-4"
            aria-hidden="true"
          />
          <span aria-hidden="true">{{ Math.abs(trend.value) }}%</span>
        </div>
      </div>

      <p 
        v-if="subtitle" 
        class="mt-2 text-xs text-gray-500 dark:text-gray-400"
        role="note"
      >
        {{ subtitle }}
      </p>
    </div>
  </UCard>
</template>

<script setup lang="ts">
interface MetricCardProps {
  title: string
  value: string | number
  icon: string
  trend?: {
    value: number
    direction: 'up' | 'down'
  }
  subtitle?: string
  loading?: boolean
}

const props = withDefaults(defineProps<MetricCardProps>(), {
  loading: false
})

// Generate unique ID for accessibility (use title as stable ID)
const metricId = computed(() => {
  return `metric-${props.title.toLowerCase().replace(/\s+/g, '-')}`
})

const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toLocaleString()
  }
  return props.value
})

const trendIcon = computed(() => {
  if (!props.trend) return ''
  return props.trend.direction === 'up' 
    ? 'i-heroicons-arrow-trending-up' 
    : 'i-heroicons-arrow-trending-down'
})

const trendColorClass = computed(() => {
  if (!props.trend) return ''
  return props.trend.direction === 'up'
    ? 'text-green-600 dark:text-green-400'
    : 'text-red-600 dark:text-red-400'
})

const trendAriaLabel = computed(() => {
  if (!props.trend) return ''
  const direction = props.trend.direction === 'up' ? 'increased' : 'decreased'
  return `Trend ${direction} by ${Math.abs(props.trend.value)} percent`
})
</script>
