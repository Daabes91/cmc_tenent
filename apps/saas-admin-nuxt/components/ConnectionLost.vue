<template>
  <Transition
    enter-active-class="transition-all duration-300 ease-out"
    enter-from-class="translate-y-full opacity-0"
    enter-to-class="translate-y-0 opacity-100"
    leave-active-class="transition-all duration-300 ease-in"
    leave-from-class="translate-y-0 opacity-100"
    leave-to-class="translate-y-full opacity-0"
  >
    <div
      v-if="isVisible"
      class="fixed bottom-4 left-1/2 -translate-x-1/2 z-50 max-w-md w-full mx-4"
    >
      <div class="bg-red-600 text-white rounded-lg shadow-2xl p-4 flex items-center gap-4">
        <!-- Icon -->
        <div class="flex-shrink-0">
          <UIcon 
            :name="retrying ? 'i-heroicons-arrow-path' : 'i-heroicons-wifi-slash'" 
            :class="['w-6 h-6', retrying && 'animate-spin']" 
          />
        </div>

        <!-- Content -->
        <div class="flex-1 min-w-0">
          <h4 class="text-sm font-semibold mb-1">
            {{ retrying ? $t('errors.reconnecting') : $t('errors.connectionLost') }}
          </h4>
          <p class="text-xs opacity-90">
            {{ retrying ? $t('errors.reconnectingMessage') : $t('errors.connectionLostMessage') }}
          </p>
        </div>

        <!-- Retry Button -->
        <button
          v-if="!retrying"
          @click="handleRetry"
          class="flex-shrink-0 px-3 py-1.5 bg-white/20 hover:bg-white/30 rounded text-sm font-medium transition-colors"
        >
          {{ $t('errors.retry') }}
        </button>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
const props = defineProps<{
  visible: boolean
  onRetry?: () => void | Promise<void>
}>()

const emit = defineEmits<{
  retry: []
}>()

const isVisible = computed(() => props.visible)
const retrying = ref(false)

const handleRetry = async () => {
  retrying.value = true
  
  try {
    if (props.onRetry) {
      await props.onRetry()
    }
    emit('retry')
  } finally {
    // Keep retrying state for at least 1 second for visual feedback
    setTimeout(() => {
      retrying.value = false
    }, 1000)
  }
}
</script>
