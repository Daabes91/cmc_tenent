<template>
  <UCard>
    <template #header>
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
          {{ $t('dashboard.recentActivity') }}
        </h3>
        <UIcon 
          name="i-heroicons-clock" 
          class="w-5 h-5 text-gray-400"
        />
      </div>
    </template>

    <div v-if="loading" class="space-y-4">
      <div v-for="i in 3" :key="i" class="animate-pulse flex gap-3">
        <div class="w-8 h-8 bg-gray-200 rounded-full"></div>
        <div class="flex-1">
          <div class="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
          <div class="h-3 bg-gray-200 rounded w-1/2"></div>
        </div>
      </div>
    </div>

    <div v-else-if="activities.length === 0" class="text-center py-8">
      <UIcon 
        name="i-heroicons-inbox" 
        class="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3"
      />
      <p class="text-sm text-gray-500 dark:text-gray-400">
        No recent activity
      </p>
    </div>

    <div v-else class="space-y-4">
      <div 
        v-for="activity in displayedActivities" 
        :key="activity.id"
        class="flex gap-3 pb-4 last:pb-0 border-b border-gray-100 dark:border-gray-800 last:border-0"
      >
        <!-- Activity Icon -->
        <div 
          class="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center"
          :class="activityIconBgClass(activity.type)"
        >
          <UIcon 
            :name="activityIcon(activity.type)" 
            class="w-4 h-4"
            :class="activityIconColorClass(activity.type)"
          />
        </div>

        <!-- Activity Content -->
        <div class="flex-1 min-w-0">
          <p class="text-sm text-gray-900 dark:text-white font-medium">
            {{ activity.description }}
          </p>
          <div class="flex items-center gap-2 mt-1">
            <p class="text-xs text-gray-500 dark:text-gray-400">
              {{ activity.managerName }}
            </p>
            <span class="text-xs text-gray-400">•</span>
            <p class="text-xs text-gray-500 dark:text-gray-400">
              {{ formatTimestamp(activity.timestamp) }}
            </p>
          </div>
        </div>

        <!-- Activity Badge -->
        <div class="flex-shrink-0">
          <UBadge 
            :color="activityBadgeColor(activity.type)" 
            variant="subtle"
            size="xs"
          >
            {{ activityTypeLabel(activity.type) }}
          </UBadge>
        </div>
      </div>

      <!-- View All Link -->
      <div v-if="activities.length > maxDisplay" class="pt-2">
        <UButton
          to="/audit-logs"
          color="gray"
          variant="ghost"
          size="xs"
          block
        >
          View all activity
        </UButton>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
import type { Activity } from '~/types'

interface RecentActivityFeedProps {
  activities: Activity[]
  loading?: boolean
  maxDisplay?: number
}

const props = withDefaults(defineProps<RecentActivityFeedProps>(), {
  loading: false,
  maxDisplay: 5
})

const displayedActivities = computed(() => {
  return props.activities.slice(0, props.maxDisplay)
})

const activityIcon = (type: Activity['type']) => {
  switch (type) {
    case 'tenant_created':
      return 'i-heroicons-plus-circle'
    case 'tenant_updated':
      return 'i-heroicons-pencil-square'
    case 'tenant_deleted':
      return 'i-heroicons-trash'
    default:
      return 'i-heroicons-information-circle'
  }
}

const activityIconBgClass = (type: Activity['type']) => {
  switch (type) {
    case 'tenant_created':
      return 'bg-green-100 dark:bg-green-900/30'
    case 'tenant_updated':
      return 'bg-blue-100 dark:bg-blue-900/30'
    case 'tenant_deleted':
      return 'bg-red-100 dark:bg-red-900/30'
    default:
      return 'bg-gray-100 dark:bg-gray-800'
  }
}

const activityIconColorClass = (type: Activity['type']) => {
  switch (type) {
    case 'tenant_created':
      return 'text-green-600 dark:text-green-400'
    case 'tenant_updated':
      return 'text-blue-600 dark:text-blue-400'
    case 'tenant_deleted':
      return 'text-red-600 dark:text-red-400'
    default:
      return 'text-gray-600 dark:text-gray-400'
  }
}

const activityBadgeColor = (type: Activity['type']) => {
  switch (type) {
    case 'tenant_created':
      return 'green'
    case 'tenant_updated':
      return 'blue'
    case 'tenant_deleted':
      return 'red'
    default:
      return 'gray'
  }
}

const activityTypeLabel = (type: Activity['type']) => {
  switch (type) {
    case 'tenant_created':
      return 'Created'
    case 'tenant_updated':
      return 'Updated'
    case 'tenant_deleted':
      return 'Deleted'
    default:
      return 'Activity'
  }
}

const formatTimestamp = (timestamp: string) => {
  // Use client-side only to avoid hydration mismatch
  if (!import.meta.client) {
    return ''
  }
  
  const date = new Date(timestamp)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return 'Just now'
  if (diffMins < 60) return `${diffMins}m ago`
  if (diffHours < 24) return `${diffHours}h ago`
  if (diffDays < 7) return `${diffDays}d ago`
  
  return date.toLocaleDateString()
}
</script>
