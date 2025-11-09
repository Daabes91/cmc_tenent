<template>
  <div class="bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 max-w-md w-full max-h-[600px] flex flex-col">
    <!-- Header -->
    <div class="flex items-center justify-between px-4 py-3 border-b border-gray-200 dark:border-gray-700">
      <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
        {{ $t('notifications.history') }}
      </h3>
      <button
        v-if="notificationHistory.length > 0"
        @click="clearAll"
        class="text-sm text-primary-600 hover:text-primary-700 dark:text-primary-400 dark:hover:text-primary-300 font-medium"
      >
        {{ $t('notifications.clearAll') }}
      </button>
    </div>

    <!-- Notification List -->
    <div class="flex-1 overflow-y-auto">
      <div v-if="notificationHistory.length === 0" class="p-8 text-center">
        <UIcon name="i-heroicons-bell-slash" class="w-12 h-12 mx-auto mb-3 text-gray-400" />
        <p class="text-sm text-gray-500 dark:text-gray-400">
          {{ $t('notifications.noHistory') }}
        </p>
      </div>

      <div v-else class="divide-y divide-gray-200 dark:divide-gray-700">
        <div
          v-for="notification in notificationHistory"
          :key="notification.id"
          class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
          :class="{ 'opacity-60': notification.dismissed }"
        >
          <div class="flex items-start gap-3">
            <!-- Icon -->
            <div class="flex-shrink-0 mt-0.5">
              <UIcon
                :name="getIconName(notification.type)"
                :class="['w-5 h-5', getIconColor(notification.type)]"
              />
            </div>

            <!-- Content -->
            <div class="flex-1 min-w-0">
              <div class="flex items-start justify-between gap-2 mb-1">
                <h4 class="text-sm font-semibold text-gray-900 dark:text-white">
                  {{ notification.title }}
                </h4>
                <span class="text-xs text-gray-500 dark:text-gray-400 whitespace-nowrap">
                  {{ formatTime(notification.timestamp) }}
                </span>
              </div>
              <p class="text-sm text-gray-600 dark:text-gray-300">
                {{ notification.message }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useNotifications } from '~/composables/useNotifications'
import type { NotificationType } from '~/composables/useNotifications'

const { notificationHistory, clearAll } = useNotifications()

// Icon configuration
const getIconName = (type: NotificationType): string => {
  const icons = {
    success: 'i-heroicons-check-circle',
    error: 'i-heroicons-x-circle',
    warning: 'i-heroicons-exclamation-triangle',
    info: 'i-heroicons-information-circle'
  }
  return icons[type]
}

const getIconColor = (type: NotificationType): string => {
  const colors = {
    success: 'text-green-600 dark:text-green-400',
    error: 'text-red-600 dark:text-red-400',
    warning: 'text-amber-600 dark:text-amber-400',
    info: 'text-blue-600 dark:text-blue-400'
  }
  return colors[type]
}

// Format timestamp
const formatTime = (timestamp: Date): string => {
  // Use client-side only to avoid hydration mismatch
  if (!import.meta.client) {
    return ''
  }
  
  const now = new Date()
  const diff = now.getTime() - new Date(timestamp).getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return 'Just now'
  if (minutes < 60) return `${minutes}m ago`
  if (hours < 24) return `${hours}h ago`
  if (days < 7) return `${days}d ago`
  
  return new Date(timestamp).toLocaleDateString()
}
</script>
