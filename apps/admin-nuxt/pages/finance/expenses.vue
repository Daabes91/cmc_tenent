<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-receipt" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('finance.expenses.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('finance.expenses.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="expensesApi.loading.value"
              @click="loadExpenses"
            >
              {{ t('finance.expenses.actions.refresh') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">
      <!-- Snapshot Cards -->
      <div class="space-y-4 mb-8">
        <div class="flex flex-col sm:flex-row sm:items-end gap-4">
          <div class="flex-1 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
            <UFormGroup label="Month">
              <USelect
                v-model="selectedMonth"
                :options="monthOptions"
                size="lg"
                value-attribute="value"
                label-attribute="label"
              />
            </UFormGroup>
            <UFormGroup label="Year">
              <USelect
                v-model="selectedYear"
                :options="yearOptions"
                size="lg"
                value-attribute="value"
                label-attribute="label"
              />
            </UFormGroup>
          </div>
          <div class="flex gap-2">
            <UButton color="blue" :loading="summaryApi.loading.value" @click="loadSummary">
              Refresh Summary
            </UButton>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-50 dark:bg-indigo-900/20">
                <span class="iconify i-lucide:wallet h-5 w-5 text-indigo-600 dark:text-indigo-400" aria-hidden="true"></span>
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Total expenses</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatCurrency(summaryApi.summary.value?.totalExpenses || 0) }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">Month-to-date</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
                <span class="iconify i-lucide:folder h-5 w-5 text-green-600 dark:text-green-400" aria-hidden="true"></span>
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Categories</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ summaryApi.summary.value?.expensesByCategory.length || 0 }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">With spend this month</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-mint-50 dark:bg-mint-900/20">
                <span class="iconify i-lucide:pie-chart h-5 w-5 text-mint-600 dark:text-mint-400" aria-hidden="true"></span>
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Top category</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ topCategoryLabel }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">{{ topCategorySubtitle }}</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-900/20">
                <span class="iconify i-lucide:banknote h-5 w-5 text-amber-600 dark:text-amber-400" aria-hidden="true"></span>
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Avg per day</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatCurrency(avgPerDay) }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">Based on selected month</p>
          </div>
        </div>
      </div>

      <ExpenseList
        :expenses="expensesApi.expenses.value"
        :categories="categoriesApi.categories.value"
        :loading="expensesApi.loading.value"
        :error="expensesApi.error.value"
        @create="openCreateModal"
        @edit="openEditModal"
        @delete="handleDelete"
        @filter="handleFilter"
      />
    </div>

    <!-- Expense Form Modal -->
    <ExpenseForm
      v-model="showExpenseForm"
      :expense="selectedExpense"
      :categories="categoriesApi.categories.value"
      :loading="formLoading"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import type { Expense, ExpenseCreateRequest } from '~/composables/useExpenses'

const { t } = useI18n()
const toast = useEnhancedToast()

const expensesApi = useExpenses()
const categoriesApi = useExpenseCategories()
const summaryApi = useFinanceSummary()

const showExpenseForm = ref(false)
const selectedExpense = ref<Expense | null>(null)
const formLoading = ref(false)

const today = new Date()
const selectedMonth = ref(today.getMonth() + 1)
const selectedYear = ref(today.getFullYear())

const monthOptions = computed(() => [
  { label: 'January', value: 1 },
  { label: 'February', value: 2 },
  { label: 'March', value: 3 },
  { label: 'April', value: 4 },
  { label: 'May', value: 5 },
  { label: 'June', value: 6 },
  { label: 'July', value: 7 },
  { label: 'August', value: 8 },
  { label: 'September', value: 9 },
  { label: 'October', value: 10 },
  { label: 'November', value: 11 },
  { label: 'December', value: 12 }
])

const yearOptions = computed(() => {
  const currentYear = today.getFullYear()
  return Array.from({ length: 5 }, (_, i) => ({
    label: String(currentYear - i),
    value: currentYear - i
  }))
})

const loadExpenses = async () => {
  await expensesApi.fetchExpenses()
}

const loadCategories = async () => {
  await categoriesApi.fetchCategories()
}

const loadSummary = async () => {
  await summaryApi.fetchMonthlySummary(selectedYear.value, selectedMonth.value)
}

const topCategoryLabel = computed(() => {
  const summary = summaryApi.summary.value
  if (!summary || !summary.expensesByCategory.length) return '—'
  const top = [...summary.expensesByCategory].sort((a, b) => b.amount - a.amount)[0]
  return top.categoryName
})

const topCategorySubtitle = computed(() => {
  const summary = summaryApi.summary.value
  if (!summary || !summary.expensesByCategory.length || !summary.totalExpenses) return 'No category data'
  const top = [...summary.expensesByCategory].sort((a, b) => b.amount - a.amount)[0]
  const percent = summary.totalExpenses > 0 ? ((top.amount / summary.totalExpenses) * 100).toFixed(1) : '0.0'
  return `${percent}% of spend`
})

const avgPerDay = computed(() => {
  const summary = summaryApi.summary.value
  if (!summary) return 0
  const daysInMonth = new Date(selectedYear.value, selectedMonth.value, 0).getDate()
  return (summary.totalExpenses || 0) / daysInMonth
})

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value || 0)
}

const openCreateModal = () => {
  selectedExpense.value = null
  showExpenseForm.value = true
}

const openEditModal = (expense: Expense) => {
  selectedExpense.value = expense
  showExpenseForm.value = true
}

const handleSubmit = async (data: ExpenseCreateRequest) => {
  formLoading.value = true
  try {
    if (selectedExpense.value) {
      await expensesApi.updateExpense(selectedExpense.value.id, data)
    } else {
      await expensesApi.createExpense(data)
    }
    showExpenseForm.value = false
    selectedExpense.value = null
  } catch (error) {
    // Error already handled by composable
  } finally {
    formLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  if (confirm(t('finance.expenses.confirm.delete'))) {
    await expensesApi.deleteExpense(id)
  }
}

const handleFilter = async (filters: { startDate?: string; endDate?: string; categoryId?: number }) => {
  await expensesApi.fetchExpenses(filters)
}

useHead({
  title: () => t('finance.expenses.meta.title')
})

onMounted(async () => {
  await Promise.all([
    loadExpenses(),
    loadCategories(),
    loadSummary()
  ])
})
</script>
