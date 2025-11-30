import type { Ref } from 'vue'

export interface CategoryExpense {
  categoryName: string
  amount: number
}

export interface MonthlySummary {
  totalExpenses: number
  expensesByCategory: CategoryExpense[]
}

export function useFinanceSummary() {
  const { request } = useAdminApi()
  const toast = useEnhancedToast()

  const summary: Ref<MonthlySummary | null> = ref(null)
  const loading = ref(false)
  const error: Ref<Error | null> = ref(null)

  const fetchMonthlySummary = async (year: number, month: number) => {
    loading.value = true
    error.value = null
    
    try {
      summary.value = await request<MonthlySummary>(
        `/admin/finance-summary/monthly?year=${year}&month=${month}`
      )
    } catch (e) {
      error.value = e as Error
      toast.error('Failed to fetch monthly summary')
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    summary,
    loading,
    error,
    fetchMonthlySummary
  }
}
