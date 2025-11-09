<template>
  <div class="relative">
    <!-- Bell Button -->
    <button
      @click="toggleHistory"
      class="relative p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
      :aria-label="$t('notifications.viewHistory')"
    >
      <UIcon name="i-heroicons-bell" class="w-6 h-6 text-gray-700 dark:text-gray-200" />
      
      <!-- Badge -->
      <span
        v-if="unreadCount > 0"
        class="absolute top-1 right-1 flex items-center justify-center min-w-[18px] h-[18px] px-1 text-xs font-bold text-white bg-red-600 rounded-full"
      >
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
    </button>

    <!-- History Dropdown -->
    <Transition
      enter-active-class="transition-all duration-200 ease-out"
      enter-from-class="opacity-0 scale-95 translate-y-2"
      enter-to-class="opacity-100 scale-100 translate-y-0"
      leave-active-class="transition-all duration-150 ease-in"
      leave-from-class="opacity-100 scale-100 translate-y-0"
      leave-to-class="opacity-0 scale-95 translate-y-2"
    >
      <div
        v-if="showHistory"
        v-click-outside="closeHistory"
        class="absolute top-full mt-2 z-50"
        :class="isRTL ? 'left-0' : 'right-0'"
      >
        <NotificationHistory />
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { useNotifications } from '~/composables/useNotifications'

const { unreadCount } = useNotifications()
const { locale } = useI18n()

const showHistory = ref(false)
const isRTL = computed(() => locale.value === 'ar')

const toggleHistory = () => {
  showHistory.value = !showHistory.value
}

const closeHistory = () => {
  showHistory.value = false
}

// Click outside directive
const vClickOutside = {
  mounted(el: HTMLElement, binding: any) {
    el.clickOutsideEvent = (event: Event) => {
      if (!(el === event.target || el.contains(event.target as Node))) {
        binding.value()
      }
    }
    document.addEventListener('click', el.clickOutsideEvent)
  },
  unmounted(el: HTMLElement) {
    document.removeEventListener('click', el.clickOutsideEvent)
  }
}
</script>
