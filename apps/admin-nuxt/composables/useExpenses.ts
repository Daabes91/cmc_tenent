import type { Ref } from 'vue'

export interface Expense {
  id: number
  categoryId: number
  categoryName: string
  amount: number
  expenseDate: string
  notes?: string
  createdAt: string
}

export interface ExpenseCreateRequest {
  categoryId: number
  amount: number
  expenseDate: string
  notes?: string
}

export interface ExpenseFilters {
  startDate?: string
  endDate?: string
  categoryId?: number
}

export function useExpenses() {
  const { request } = useAdminApi()
  const toast = useEnhancedToast()

  const expenses: Ref<Expense[]> = ref([])
  const loading = ref(false)
  const error: Ref<Error | null> = ref(null)

  const fetchExpenses = async (filters?: ExpenseFilters) => {
    loading.value = true
    error.value = null
    
    try {
      const params = new URLSearchParams()
      if (filters?.startDate) params.append('startDate', filters.startDate)
      if (filters?.endDate) params.append('endDate', filters.endDate)
      if (filters?.categoryId) params.append('categoryId', filters.categoryId.toString())
      
      const queryString = params.toString()
      const path = `/admin/expenses${queryString ? `?${queryString}` : ''}`
      
      expenses.value = await request<Expense[]>(path)
    } catch (e) {
      error.value = e as Error
      toast.error('Failed to fetch expenses')
      throw e
    } finally {
      loading.value = false
    }
  }

  const createExpense = async (data: ExpenseCreateRequest) => {
    loading.value = true
    error.value = null
    
    try {
      const newExpense = await request<Expense>('/admin/expenses', {
        method: 'POST',
        body: data
      })
      
      expenses.value.unshift(newExpense)
      toast.success('Expense created successfully')
      return newExpense
    } catch (e) {
      error.value = e as Error
      toast.error('Failed to create expense')
      throw e
    } finally {
      loading.value = false
    }
  }

  const updateExpense = async (id: number, data: ExpenseCreateRequest) => {
    loading.value = true
    error.value = null
    
    try {
      const updatedExpense = await request<Expense>(`/admin/expenses/${id}`, {
        method: 'PUT',
        body: data
      })
      
      const index = expenses.value.findIndex(e => e.id === id)
      if (index !== -1) {
        expenses.value[index] = updatedExpense
      }
      
      toast.success('Expense updated successfully')
      return updatedExpense
    } catch (e) {
      error.value = e as Error
      toast.error('Failed to update expense')
      throw e
    } finally {
      loading.value = false
    }
  }

  const deleteExpense = async (id: number) => {
    loading.value = true
    error.value = null
    
    try {
      await request<void>(`/admin/expenses/${id}`, {
        method: 'DELETE'
      })
      
      expenses.value = expenses.value.filter(e => e.id !== id)
      toast.success('Expense deleted successfully')
    } catch (e) {
      error.value = e as Error
      toast.error('Failed to delete expense')
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    expenses,
    loading,
    error,
    fetchExpenses,
    createExpense,
    updateExpense,
    deleteExpense
  }
}
