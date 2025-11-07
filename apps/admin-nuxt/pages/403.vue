<template>
  <div class="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-slate-100 px-6">
    <div class="w-full max-w-md text-center">
      <div class="mb-8 inline-flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-rose-500 to-rose-600 text-4xl text-white shadow-2xl shadow-rose-500/40">
        <UIcon name="i-lucide-lock" class="h-12 w-12" />
      </div>

      <h1 class="mb-4 text-4xl font-bold text-slate-900 dark:text-white">
        {{ t('error.403.title') }}
      </h1>

      <p class="mb-8 text-lg text-slate-600 dark:text-slate-300">
        {{ t('error.403.description') }}
      </p>

      <div class="flex flex-col gap-3 sm:flex-row sm:justify-center">
        <UButton
          icon="i-lucide-arrow-left"
          size="lg"
          color="violet"
          @click="navigateTo('/')"
        >
          {{ t('error.403.backToHome') }}
        </UButton>

        <UButton
          icon="i-lucide-life-buoy"
          size="lg"
          variant="outline"
          color="gray"
          @click="contactSupport"
        >
          {{ t('error.403.contactSupport') }}
        </UButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n();

definePageMeta({
  layout: false
});

function contactSupport() {
  // TODO: Implement support contact logic
  console.log('Contact support');
}
</script>
