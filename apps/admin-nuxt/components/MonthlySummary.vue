<template>
  <div class="space-y-6">
    <!-- Month/Year Selector -->
    <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
      <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-6 py-4">
        <div class="flex items-center gap-3">
          <UIcon name="i-lucide-calendar" class="h-5 w-5 text-white" />
          <div>
            <h2 class="text-lg font-semibold text-white">{{ t('finance.summary.selector.title') }}</h2>
            <p class="text-sm text-slate-300">{{ t('finance.summary.selector.subtitle') }}</p>
          </div>
        </div>
      </div>
      <div class="p-6">
        <div class="flex flex-col sm:flex-row gap-4">
          <div class="flex-1">
            <UFormGroup :label="t('finance.summary.selector.month')">
              <USelect
                v-model="selectedMonth"
                :options="monthOptions"
                size="lg"
                value-attribute="value"
                label-attribute="label"
              />
            </UFormGroup>
          </div>
          <div class="flex-1">
            <UFormGroup :label="t('finance.summary.selector.year')">
              <USelect
                v-model="selectedYear"
                :options="yearOptions"
                size="lg"
                value-attribute="value"
                label-attribute="label"
              />
            </UFormGroup>
          </div>
          <div class="flex items-end">
            <UButton
              color="blue"
              size="lg"
              @click="loadSummary"
              :loading="loading"
            >
              {{ t('finance.summary.selector.load') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Total Expenses Card -->
    <div v-if="summary" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
      <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
        <div class="flex items-center gap-3">
          <UIcon name="i-lucide-wallet" class="h-5 w-5 text-white" />
          <div>
            <h3 class="text-lg font-semibold text-white">{{ t('finance.summary.total.title') }}</h3>
            <p class="text-sm text-blue-100">{{ formatMonthYear(selectedMonth, selectedYear) }}</p>
          </div>
        </div>
      </div>
      <div class="p-8">
        <div class="text-center">
          <p class="text-sm text-slate-600 dark:text-slate-300 mb-2">{{ t('finance.summary.total.label') }}</p>
          <div class="text-4xl font-bold text-slate-900 dark:text-white">
            <CurrencyValue :amount="summary.totalExpenses" />
          </div>
        </div>
      </div>
    </div>

    <!-- Category Breakdown -->
    <div v-if="summary && summary.expensesByCategory.length > 0" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
      <div class="bg-gradient-to-r from-purple-500 to-pink-600 px-6 py-4">
        <div class="flex items-center gap-3">
          <UIcon name="i-lucide-pie-chart" class="h-5 w-5 text-white" />
          <div>
            <h3 class="text-lg font-semibold text-white">{{ t('finance.summary.breakdown.title') }}</h3>
            <p class="text-sm text-purple-100">{{ t('finance.summary.breakdown.subtitle') }}</p>
          </div>
        </div>
      </div>
      <div class="p-6">
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-slate-50 dark:bg-slate-700/50 border-b border-slate-200 dark:border-slate-600">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  {{ t('finance.summary.breakdown.table.category') }}
                </th>
                <th class="px-6 py-3 text-right text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  {{ t('finance.summary.breakdown.table.amount') }}
                </th>
                <th class="px-6 py-3 text-right text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  {{ t('finance.summary.breakdown.table.percentage') }}
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-200 dark:divide-slate-700">
              <tr
                v-for="item in summary.expensesByCategory"
                :key="item.categoryName"
                class="hover:bg-slate-50 dark:hover:bg-slate-700/30 transition-colors"
              >
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center gap-2">
                    <div class="h-3 w-3 rounded-full bg-blue-500"></div>
                    <span class="text-sm font-medium text-slate-900 dark:text-white">
                      {{ item.categoryName }}
                    </span>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium text-slate-900 dark:text-white">
                  <CurrencyValue :amount="item.amount" />
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm text-slate-600 dark:text-slate-300">
                  {{ calculatePercentage(item.amount) }}%
                </td>
              </tr>
            </tbody>
            <tfoot class="bg-slate-50 dark:bg-slate-700/50 border-t-2 border-slate-300 dark:border-slate-600">
              <tr>
                <td class="px-6 py-4 text-sm font-semibold text-slate-900 dark:text-white">
                  {{ t('finance.summary.breakdown.table.total') }}
                </td>
                <td class="px-6 py-4 text-right text-sm font-bold text-slate-900 dark:text-white">
                  <CurrencyValue :amount="summary.totalExpenses" />
                </td>
                <td class="px-6 py-4 text-right text-sm font-semibold text-slate-600 dark:text-slate-300">
                  100%
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 p-12">
      <div class="flex flex-col items-center gap-4">
        <div class="animate-spin h-12 w-12 border-4 border-blue-500 border-t-transparent rounded-full"></div>
        <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('finance.summary.loading') }}</p>
      </div>
    </div>

    <!-- Error State -->
    <div v-if="error" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 p-12">
      <div class="flex flex-col items-center gap-4">
        <div class="h-16 w-16 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
          <UIcon name="i-lucide-alert-circle" class="h-8 w-8 text-red-600 dark:text-red-400" />
        </div>
        <div class="text-center">
          <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('finance.summary.error.title') }}</h3>
          <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">{{ error.message }}</p>
        </div>
        <UButton
          color="blue"
          @click="loadSummary"
        >
          {{ t('finance.summary.error.retry') }}
        </UButton>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && !error && summary && summary.totalExpenses === 0" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 p-12">
      <div class="flex flex-col items-center gap-4">
        <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
          <UIcon name="i-lucide-inbox" class="h-8 w-8 text-slate-400" />
        </div>
        <div class="text-center">
          <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('finance.summary.empty.title') }}</h3>
          <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">{{ t('finance.summary.empty.subtitle') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { MonthlySummary } from '~/composables/useFinanceSummary'

const props = defineProps<{
  summary: MonthlySummary | null
  loading: boolean
  error: Error | null
}>()

const emit = defineEmits<{
  load: [year: number, month: number]
}>()

const { t } = useI18n()

const currentDate = new Date()
const selectedMonth = ref(currentDate.getMonth() + 1)
const selectedYear = ref(currentDate.getFullYear())

const monthOptions = computed(() => [
  { label: t('finance.summary.months.january'), value: 1 },
  { label: t('finance.summary.months.february'), value: 2 },
  { label: t('finance.summary.months.march'), value: 3 },
  { label: t('finance.summary.months.april'), value: 4 },
  { label: t('finance.summary.months.may'), value: 5 },
  { label: t('finance.summary.months.june'), value: 6 },
  { label: t('finance.summary.months.july'), value: 7 },
  { label: t('finance.summary.months.august'), value: 8 },
  { label: t('finance.summary.months.september'), value: 9 },
  { label: t('finance.summary.months.october'), value: 10 },
  { label: t('finance.summary.months.november'), value: 11 },
  { label: t('finance.summary.months.december'), value: 12 }
])

const yearOptions = computed(() => {
  const years = []
  const currentYear = new Date().getFullYear()
  for (let i = currentYear; i >= currentYear - 5; i--) {
    years.push({ label: i.toString(), value: i })
  }
  return years
})

const loadSummary = () => {
  emit('load', selectedYear.value, selectedMonth.value)
}

const calculatePercentage = (amount: number): string => {
  if (!props.summary || props.summary.totalExpenses === 0) return '0.0'
  return ((amount / props.summary.totalExpenses) * 100).toFixed(1)
}

const formatMonthYear = (month: number, year: number): string => {
  const monthName = monthOptions.value.find(m => m.value === month)?.label || ''
  return `${monthName} ${year}`
}

// Load current month on mount
onMounted(() => {
  loadSummary()
})
</script>
