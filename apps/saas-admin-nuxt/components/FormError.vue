<template>
  <Transition
    enter-active-class="transition-all duration-200 ease-out"
    enter-from-class="opacity-0 -translate-y-1"
    enter-to-class="opacity-100 translate-y-0"
    leave-active-class="transition-all duration-150 ease-in"
    leave-from-class="opacity-100 translate-y-0"
    leave-to-class="opacity-0 -translate-y-1"
  >
    <div
      v-if="error"
      class="flex items-start gap-2 mt-1.5 text-sm text-red-600 dark:text-red-400"
      role="alert"
    >
      <UIcon name="i-heroicons-exclamation-circle" class="w-4 h-4 flex-shrink-0 mt-0.5" />
      <span>{{ error }}</span>
    </div>
  </Transition>
</template>

<script setup lang="ts">
defineProps<{
  error?: string | null
}>()
</script>
