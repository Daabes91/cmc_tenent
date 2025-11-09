<template>
  <div class="relative">
    <button
      @click="toggleNotifications"
      class="relative p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
      :aria-label="$t('notifications.title')"
    >
      <UIcon name="i-heroicons-bell" class="w-5 h-5 text-gray-700 dark:text-gray-300" />
      
      <!-- Notification Badge -->
      <span
        v-if="unreadCount > 0"
        class="absolute top-1 right-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white"
      >
        {{ unreadCount > 9 ? '9+' : unreadCount }}
      </span>
    </button>

    <!-- Notifications Dropdown -->
    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0 scale-95"
      enter-to-class="opacity-100 scale-100"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100 scale-100"
      leave-to-class="opacity-0 scale-95"
    >
      <div
        v-if="isOpen"
        class="absolute right-0 mt-2 w-80 sm:w-96 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-50"
      >
        <div class="p-4 border-b border-gray-200 dark:border-gray-700">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
              {{ $t('notifications.title') }}
            </h3>
            <button
              v-if="unreadCount > 0"
              @click="markAllAsRead"
              class="text-sm text-primary-600 hover:text-primary-700 dark:text-primary-400"
            >
              {{ $t('notifications.markAllRead') }}
            </button>
          </div>
        </div>

        <div class="max-h-96 overflow-y-auto">
          <div v-if="notifications.length === 0" class="p-8 text-center">
            <UIcon name="i-heroicons-bell-slash" class="w-12 h-12 text-gray-400 mx-auto mb-2" />
            <p class="text-sm text-gray-500 dark:text-gray-400">
              {{ $t('notifications.noNotifications') }}
            </p>
          </div>

          <div v-else>
            <div
              v-for="notification in notifications"
              :key="notification.id"
              class="p-4 border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors cursor-pointer"
              :class="{ 'bg-blue-50 dark:bg-blue-900/20': !notification.read }"
              @click="handleNotificationClick(notification)"
            >
              <div class="flex items-start gap-3">
                <div
                  class="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center"
                  :class="getNotificationIconClass(notification.type)"
                >
                  <UIcon :name="getNotificationIcon(notification.type)" class="w-4 h-4" />
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-gray-900 dark:text-white">
                    {{ notification.title }}
                  </p>
                  <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                    {{ notification.message }}
                  </p>
                  <p class="text-xs text-gray-500 dark:text-gray-500 mt-1">
                    {{ formatTimestamp(notification.timestamp) }}
                  </p>
                </div>
                <div v-if="!notification.read" class="flex-shrink-0">
                  <div class="w-2 h-2 bg-blue-500 rounded-full"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="p-3 border-t border-gray-200 dark:border-gray-700">
          <button
            @click="viewAllNotifications"
            class="w-full text-center text-sm text-primary-600 hover:text-primary-700 dark:text-primary-400 font-medium"
          >
            {{ $t('notifications.viewAll') }}
          </button>
        </div>
      </div>
    </Transition>

    <!-- Backdrop for mobile -->
    <div
      v-if="isOpen"
      class="fixed inset-0 z-40 lg:hidden"
      @click="closeNotifications"
    />
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n()
const { notifications, unreadCount, dismissNotification, clearAll } = useNotifications()

const isOpen = ref(false)

const toggleNotifications = () => {
  isOpen.value = !isOpen.value
}

const closeNotifications = () => {
  isOpen.value = false
}

const handleNotificationClick = (notification: any) => {
  dismissNotification(notification.id)
  // Handle navigation based on notification type
  closeNotifications()
}

const markAllAsRead = () => {
  clearAll()
}

const viewAllNotifications = () => {
  // Navigate to notifications page if it exists
  closeNotifications()
}

const getNotificationIcon = (type: string): string => {
  const icons: Record<string, string> = {
    'tenant_created': 'i-heroicons-building-office-2',
    'security_alert': 'i-heroicons-shield-exclamation',
    'system_health': 'i-heroicons-exclamation-triangle',
    'info': 'i-heroicons-information-circle',
    'success': 'i-heroicons-check-circle',
    'warning': 'i-heroicons-exclamation-circle',
    'error': 'i-heroicons-x-circle'
  }
  return icons[type] || 'i-heroicons-bell'
}

const getNotificationIconClass = (type: string): string => {
  const classes: Record<string, string> = {
    'tenant_created': 'bg-green-100 dark:bg-green-900 text-green-600 dark:text-green-400',
    'security_alert': 'bg-red-100 dark:bg-red-900 text-red-600 dark:text-red-400',
    'system_health': 'bg-yellow-100 dark:bg-yellow-900 text-yellow-600 dark:text-yellow-400',
    'info': 'bg-blue-100 dark:bg-blue-900 text-blue-600 dark:text-blue-400',
    'success': 'bg-green-100 dark:bg-green-900 text-green-600 dark:text-green-400',
    'warning': 'bg-yellow-100 dark:bg-yellow-900 text-yellow-600 dark:text-yellow-400',
    'error': 'bg-red-100 dark:bg-red-900 text-red-600 dark:text-red-400'
  }
  return classes[type] || 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400'
}

const formatTimestamp = (timestamp: string): string => {
  // Use client-side only to avoid hydration mismatch
  if (!import.meta.client) {
    return ''
  }
  
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return t('notifications.justNow')
  if (minutes < 60) return t('notifications.minutesAgo', { count: minutes })
  if (hours < 24) return t('notifications.hoursAgo', { count: hours })
  if (days < 7) return t('notifications.daysAgo', { count: days })
  
  return date.toLocaleDateString()
}

// Close dropdown when clicking outside
onMounted(() => {
  if (process.client) {
    document.addEventListener('click', (e) => {
      const target = e.target as HTMLElement
      if (!target.closest('.relative')) {
        closeNotifications()
      }
    })
  }
})
</script>
