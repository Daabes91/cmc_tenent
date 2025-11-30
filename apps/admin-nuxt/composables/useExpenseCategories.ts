import type { Ref } from 'vue'

export interface ExpenseCategory {
  id: number
  name: string
  isSystem: boolean
  isActive: boolean
}

export interface ExpenseCategoryCreateRequest {
  name: string
}

export function useExpenseCategories() {
  const { request } = useAdminApi()
  const toast = useEnhancedToast()

  const categories: Ref<ExpenseCategory[]> = ref([])
  const loading = ref(false)
  const error: Ref<Error | null> = ref(null)

  const fetchCategories = async (activeOnly = false) => {
    loading.value = true
    error.value = null
    
    try {
      const path = activeOnly 
        ? '/admin/expense-categories?activeOnly=true' 
        : '/admin/expense-categories'
      
      categories.value = await request<ExpenseCategory[]>(path)
    } catch (e) {
      error.value = e as Error
      toast.error('Failed to fetch expense categories')
      throw e
    } finally {
      loading.value = false
    }
  }

  const createCategory = async (name: string) => {
    loading.value = true
    error.value = null
    
    try {
      const newCategory = await request<ExpenseCategory>('/admin/expense-categories', {
        method: 'POST',
        body: { name }
      })
      
      categories.value.push(newCategory)
      toast.success('Category created successfully')
      return newCategory
    } catch (e) {
      error.value = e as Error
      toast.error('Failed to create category')
      throw e
    } finally {
      loading.value = false
    }
  }

  const updateCategory = async (id: number, name: string) => {
    loading.value = true
    error.value = null
    
    try {
      const updatedCategory = await request<ExpenseCategory>(`/admin/expense-categories/${id}`, {
        method: 'PUT',
        body: { name }
      })
      
      const index = categories.value.findIndex(c => c.id === id)
      if (index !== -1) {
        categories.value[index] = updatedCategory
      }
      
      toast.success('Category updated successfully')
      return updatedCategory
    } catch (e) {
      error.value = e as Error
      toast.error('Failed to update category')
      throw e
    } finally {
      loading.value = false
    }
  }

  const toggleActive = async (id: number) => {
    loading.value = true
    error.value = null
    
    try {
      const updatedCategory = await request<ExpenseCategory>(`/admin/expense-categories/${id}/toggle`, {
        method: 'PATCH'
      })
      
      const index = categories.value.findIndex(c => c.id === id)
      if (index !== -1) {
        categories.value[index] = updatedCategory
      }
      
      const status = updatedCategory.isActive ? 'enabled' : 'disabled'
      toast.success(`Category ${status} successfully`)
      return updatedCategory
    } catch (e) {
      error.value = e as Error
      toast.error('Failed to toggle category status')
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    categories,
    loading,
    error,
    fetchCategories,
    createCategory,
    updateCategory,
    toggleActive
  }
}
