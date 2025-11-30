<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-green-500 to-emerald-600 shadow-lg">
              <UIcon name="i-lucide-bar-chart" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('finance.summary.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('finance.summary.header.subtitle') }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">
      <MonthlySummary
        :summary="summaryApi.summary.value"
        :loading="summaryApi.loading.value"
        :error="summaryApi.error.value"
        @load="handleLoad"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n()

const summaryApi = useFinanceSummary()

const handleLoad = async (year: number, month: number) => {
  await summaryApi.fetchMonthlySummary(year, month)
}

useHead({
  title: () => t('finance.summary.meta.title')
})
</script>
