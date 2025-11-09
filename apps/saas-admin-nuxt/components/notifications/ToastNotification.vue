<template>
  <Transition
    enter-active-class="transition-all duration-300 ease-out"
    enter-from-class="translate-x-full opacity-0"
    enter-to-class="translate-x-0 opacity-100"
    leave-active-class="transition-all duration-300 ease-in"
    leave-from-class="translate-x-0 opacity-100"
    leave-to-class="translate-x-full opacity-0"
  >
    <div
      v-if="!notification.dismissed"
      :class="[
        'flex items-start gap-3 p-4 rounded-lg shadow-lg border max-w-md',
        variantClasses[notification.type]
      ]"
      role="alert"
      :aria-live="notification.type === 'error' ? 'assertive' : 'polite'"
    >
      <!-- Icon -->
      <div class="flex-shrink-0 mt-0.5">
        <UIcon :name="iconName" :class="['w-5 h-5', iconColorClass]" />
      </div>

      <!-- Content -->
      <div class="flex-1 min-w-0">
        <h4 class="text-sm font-semibold mb-1" :class="titleColorClass">
          {{ notification.title }}
        </h4>
        <p class="text-sm" :class="messageColorClass">
          {{ notification.message }}
        </p>
      </div>

      <!-- Dismiss Button -->
      <button
        @click="dismiss"
        class="flex-shrink-0 p-1 rounded hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
        :aria-label="$t('notifications.dismiss')"
      >
        <UIcon name="i-heroicons-x-mark" :class="['w-4 h-4', iconColorClass]" />
      </button>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import type { Notification } from '~/composables/useNotifications'

const props = defineProps<{
  notification: Notification
}>()

const emit = defineEmits<{
  dismiss: [id: string]
}>()

// Variant styles
const variantClasses = {
  success: 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800',
  error: 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800',
  warning: 'bg-amber-50 dark:bg-amber-900/20 border-amber-200 dark:border-amber-800',
  info: 'bg-blue-50 dark:bg-blue-900/20 border-blue-200 dark:border-blue-800'
}

// Icon configuration
const iconConfig = {
  success: { name: 'i-heroicons-check-circle', color: 'text-green-600 dark:text-green-400' },
  error: { name: 'i-heroicons-x-circle', color: 'text-red-600 dark:text-red-400' },
  warning: { name: 'i-heroicons-exclamation-triangle', color: 'text-amber-600 dark:text-amber-400' },
  info: { name: 'i-heroicons-information-circle', color: 'text-blue-600 dark:text-blue-400' }
}

const iconName = computed(() => iconConfig[props.notification.type].name)
const iconColorClass = computed(() => iconConfig[props.notification.type].color)

// Text colors
const titleColorClass = computed(() => {
  const colors = {
    success: 'text-green-900 dark:text-green-100',
    error: 'text-red-900 dark:text-red-100',
    warning: 'text-amber-900 dark:text-amber-100',
    info: 'text-blue-900 dark:text-blue-100'
  }
  return colors[props.notification.type]
})

const messageColorClass = computed(() => {
  const colors = {
    success: 'text-green-700 dark:text-green-200',
    error: 'text-red-700 dark:text-red-200',
    warning: 'text-amber-700 dark:text-amber-200',
    info: 'text-blue-700 dark:text-blue-200'
  }
  return colors[props.notification.type]
})

const dismiss = () => {
  emit('dismiss', props.notification.id)
}
</script>
