<template>
  <div class="space-y-6">
    <!-- Filters Section -->
    <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
      <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-6 py-4">
        <div class="flex items-center gap-3">
          <UIcon name="i-lucide-filter" class="h-5 w-5 text-white" />
          <div>
            <h2 class="text-lg font-semibold text-white">{{ t('finance.expenses.filters.title') }}</h2>
            <p class="text-sm text-slate-300">{{ t('finance.expenses.filters.subtitle') }}</p>
          </div>
        </div>
      </div>
      <div class="p-6">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <UFormGroup :label="t('finance.expenses.filters.startDate')">
            <UInput
              v-model="filters.startDate"
              type="date"
              size="lg"
            />
          </UFormGroup>
          <UFormGroup :label="t('finance.expenses.filters.endDate')">
            <UInput
              v-model="filters.endDate"
              type="date"
              size="lg"
            />
          </UFormGroup>
          <UFormGroup :label="t('finance.expenses.filters.category')">
            <USelect
              v-model="filters.categoryId"
              :options="categoryOptions"
              size="lg"
              value-attribute="value"
              label-attribute="label"
            />
          </UFormGroup>
        </div>
        <div class="flex justify-end gap-2 mt-4">
          <UButton
            variant="ghost"
            color="gray"
            @click="clearFilters"
          >
            {{ t('finance.expenses.filters.clear') }}
          </UButton>
          <UButton
            color="blue"
            @click="applyFilters"
            :loading="loading"
          >
            {{ t('finance.expenses.filters.apply') }}
          </UButton>
        </div>
      </div>
    </div>

    <!-- Expenses Table -->
    <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
      <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-receipt" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">{{ t('finance.expenses.list.title') }}</h2>
                <p class="text-sm text-blue-100">{{ t('finance.expenses.list.count', { count: expenses.length }) }}</p>
              </div>
            </div>
          <div class="flex items-center gap-2">
            <UButton
              variant="ghost"
              color="white"
              icon="i-lucide-folder"
              @click="goToCategories"
            >
              {{ t('finance.expenses.list.manageCategories') }}
            </UButton>
            <UButton
              color="white"
              icon="i-lucide-plus"
              @click="$emit('create')"
            >
              {{ t('finance.expenses.list.addNew') }}
            </UButton>
          </div>
          </div>
        </div>

      <div v-if="loading" class="p-6">
        <div class="space-y-4">
          <div v-for="i in 5" :key="i" class="animate-pulse">
            <div class="bg-slate-200 dark:bg-slate-700 rounded-xl p-4 space-y-3">
              <div class="flex items-center justify-between">
                <div class="h-4 bg-slate-300 dark:bg-slate-600 rounded w-1/4"></div>
                <div class="h-4 bg-slate-300 dark:bg-slate-600 rounded w-1/6"></div>
              </div>
              <div class="h-3 bg-slate-300 dark:bg-slate-600 rounded w-3/4"></div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="error" class="p-12 text-center">
        <div class="flex flex-col items-center gap-4">
          <div class="h-16 w-16 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
            <UIcon name="i-lucide-alert-circle" class="h-8 w-8 text-red-600 dark:text-red-400" />
          </div>
          <div>
            <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('finance.expenses.list.error.title') }}</h3>
            <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">{{ error.message }}</p>
          </div>
          <UButton
            color="blue"
            @click="applyFilters"
          >
            {{ t('finance.expenses.list.error.retry') }}
          </UButton>
        </div>
      </div>

      <div v-else-if="expenses.length > 0" class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-slate-50 dark:bg-slate-700/50 border-b border-slate-200 dark:border-slate-600">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                {{ t('finance.expenses.table.date') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                {{ t('finance.expenses.table.category') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                {{ t('finance.expenses.table.amount') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                {{ t('finance.expenses.table.notes') }}
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                {{ t('finance.expenses.table.actions') }}
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-200 dark:divide-slate-700">
            <tr
              v-for="expense in expenses"
              :key="expense.id"
              class="hover:bg-slate-50 dark:hover:bg-slate-700/30 transition-colors"
            >
              <td class="px-6 py-4 whitespace-nowrap text-sm text-slate-900 dark:text-white">
                {{ formatDate(expense.expenseDate) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <UBadge color="blue" variant="soft">
                  {{ expense.categoryName }}
                </UBadge>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-slate-900 dark:text-white">
                <CurrencyValue :amount="expense.amount" />
              </td>
              <td class="px-6 py-4 text-sm text-slate-600 dark:text-slate-300 max-w-xs truncate">
                {{ expense.notes || '—' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm">
                <div class="flex items-center justify-end gap-2">
                  <UButton
                    size="sm"
                    variant="ghost"
                    color="blue"
                    icon="i-lucide-edit"
                    @click="$emit('edit', expense)"
                  >
                    {{ t('finance.expenses.table.edit') }}
                  </UButton>
                  <UButton
                    size="sm"
                    variant="ghost"
                    color="red"
                    icon="i-lucide-trash"
                    @click="$emit('delete', expense.id)"
                  >
                    {{ t('finance.expenses.table.delete') }}
                  </UButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="p-12 text-center">
        <div class="flex flex-col items-center gap-4">
          <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
            <UIcon name="i-lucide-receipt" class="h-8 w-8 text-slate-400" />
          </div>
          <div>
            <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('finance.expenses.list.empty.title') }}</h3>
            <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">{{ t('finance.expenses.list.empty.subtitle') }}</p>
          </div>
          <UButton
            color="blue"
            icon="i-lucide-plus"
            @click="$emit('create')"
          >
            {{ t('finance.expenses.list.empty.action') }}
          </UButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Expense } from '~/composables/useExpenses'
import type { ExpenseCategory } from '~/composables/useExpenseCategories'

const props = defineProps<{
  expenses: Expense[]
  categories: ExpenseCategory[]
  loading: boolean
  error: Error | null
}>()

const emit = defineEmits<{
  create: []
  edit: [expense: Expense]
  delete: [id: number]
  filter: [filters: { startDate?: string; endDate?: string; categoryId?: number }]
}>()

const { t } = useI18n()

const filters = ref({
  startDate: '',
  endDate: '',
  categoryId: undefined as number | undefined
})

const categoryOptions = computed(() => [
  { label: t('finance.expenses.filters.allCategories'), value: undefined },
  ...props.categories.map(cat => ({
    label: cat.name,
    value: cat.id
  }))
])

const goToCategories = () => {
  navigateTo('/finance/categories')
}

const applyFilters = () => {
  const filterData: { startDate?: string; endDate?: string; categoryId?: number } = {}
  
  if (filters.value.startDate) filterData.startDate = filters.value.startDate
  if (filters.value.endDate) filterData.endDate = filters.value.endDate
  if (filters.value.categoryId) filterData.categoryId = filters.value.categoryId
  
  emit('filter', filterData)
}

const clearFilters = () => {
  filters.value = {
    startDate: '',
    endDate: '',
    categoryId: undefined
  }
  emit('filter', {})
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString()
}
</script>
