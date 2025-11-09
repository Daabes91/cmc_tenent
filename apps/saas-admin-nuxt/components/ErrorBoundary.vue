<template>
  <div v-if="hasError" class="min-h-screen flex items-center justify-center p-4 bg-gray-50 dark:bg-gray-900">
    <div class="max-w-md w-full bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6">
      <!-- Error Icon -->
      <div class="flex justify-center mb-4">
        <div class="w-16 h-16 bg-red-100 dark:bg-red-900/20 rounded-full flex items-center justify-center">
          <UIcon name="i-heroicons-exclamation-triangle" class="w-8 h-8 text-red-600 dark:text-red-400" />
        </div>
      </div>

      <!-- Error Title -->
      <h2 class="text-xl font-semibold text-center text-gray-900 dark:text-white mb-2">
        {{ $t('errors.unexpectedError') }}
      </h2>

      <!-- Error Message -->
      <p class="text-sm text-gray-600 dark:text-gray-400 text-center mb-6">
        {{ $t('errors.unexpectedErrorMessage') }}
      </p>

      <!-- Error Details (Development Only) -->
      <div v-if="showDetails && errorDetails" class="mb-6 p-4 bg-gray-100 dark:bg-gray-900 rounded-lg">
        <p class="text-xs font-mono text-gray-700 dark:text-gray-300 break-all">
          {{ errorDetails }}
        </p>
      </div>

      <!-- Actions -->
      <div class="flex flex-col gap-3">
        <UButton
          color="primary"
          block
          @click="handleReload"
          :loading="reloading"
        >
          {{ $t('errors.reloadPage') }}
        </UButton>

        <UButton
          color="gray"
          variant="ghost"
          block
          @click="handleGoHome"
        >
          {{ $t('errors.goToDashboard') }}
        </UButton>

        <UButton
          v-if="!showDetails && isDevelopment"
          color="gray"
          variant="ghost"
          block
          size="sm"
          @click="showDetails = true"
        >
          {{ $t('errors.showDetails') }}
        </UButton>
      </div>
    </div>
  </div>

  <slot v-else />
</template>

<script setup lang="ts">
const hasError = ref(false)
const errorDetails = ref<string>('')
const showDetails = ref(false)
const reloading = ref(false)
const router = useRouter()
const config = useRuntimeConfig()

const isDevelopment = computed(() => config.public.environment === 'development' || import.meta.dev)

// Error handler
const handleError = (error: Error) => {
  console.error('ErrorBoundary caught error:', error)
  hasError.value = true
  errorDetails.value = `${error.name}: ${error.message}\n${error.stack || ''}`
}

// Reload page
const handleReload = () => {
  reloading.value = true
  window.location.reload()
}

// Go to dashboard
const handleGoHome = () => {
  hasError.value = false
  errorDetails.value = ''
  showDetails.value = false
  router.push('/')
}

// Reset error state when route changes
watch(() => router.currentRoute.value.path, () => {
  if (hasError.value) {
    hasError.value = false
    errorDetails.value = ''
    showDetails.value = false
  }
})

// Expose error handler for parent components
defineExpose({
  handleError
})

// Global error handler
if (import.meta.client) {
  window.addEventListener('error', (event) => {
    handleError(event.error || new Error(event.message))
  })

  window.addEventListener('unhandledrejection', (event) => {
    handleError(new Error(event.reason?.message || 'Unhandled promise rejection'))
  })
}
</script>
