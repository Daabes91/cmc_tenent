// Optimistic UI Updates Composable
import type { Ref } from 'vue'

interface OptimisticUpdateOptions<T> {
  onSuccess?: (result: T) => void
  onError?: (error: Error, rollback: () => void) => void
  showSuccessToast?: boolean
  showErrorToast?: boolean
  successMessage?: string
  errorMessage?: string
}

export const useOptimisticUpdate = () => {
  const toast = useToast()

  /**
   * Perform optimistic update on a reactive state
   * Updates the UI immediately, then performs the actual API call
   * Rolls back on error
   */
  const optimisticUpdate = async <T, R>(
    state: Ref<T>,
    optimisticValue: T,
    apiCall: () => Promise<R>,
    options: OptimisticUpdateOptions<R> = {}
  ): Promise<R | null> => {
    // Store original value for rollback
    const originalValue = JSON.parse(JSON.stringify(state.value))

    // Apply optimistic update immediately
    state.value = optimisticValue

    try {
      // Perform actual API call
      const result = await apiCall()

      // Call success callback if provided
      if (options.onSuccess) {
        options.onSuccess(result)
      }

      // Show success toast if enabled
      if (options.showSuccessToast) {
        toast.add({
          title: 'Success',
          description: options.successMessage || 'Operation completed successfully',
          color: 'green',
          timeout: 3000
        })
      }

      return result
    } catch (error: any) {
      // Rollback to original value
      const rollback = () => {
        state.value = originalValue
      }
      
      rollback()

      // Call error callback if provided
      if (options.onError) {
        options.onError(error, rollback)
      }

      // Show error toast if enabled
      if (options.showErrorToast) {
        toast.add({
          title: 'Error',
          description: options.errorMessage || error.message || 'Operation failed',
          color: 'red',
          timeout: 5000
        })
      }

      console.error('Optimistic update failed:', error)
      return null
    }
  }

  /**
   * Optimistic list item addition
   */
  const optimisticAdd = async <T extends { id?: number | string }>(
    list: Ref<T[]>,
    newItem: T,
    apiCall: () => Promise<T>,
    options: OptimisticUpdateOptions<T> = {}
  ): Promise<T | null> => {
    // Add item to list immediately with temporary ID
    const tempItem = { ...newItem, id: `temp-${Date.now()}` }
    list.value = [...list.value, tempItem as T]

    try {
      // Perform actual API call
      const result = await apiCall()

      // Replace temp item with real item
      list.value = list.value.map(item => 
        item.id === tempItem.id ? result : item
      )

      // Call success callback
      if (options.onSuccess) {
        options.onSuccess(result)
      }

      // Show success toast
      if (options.showSuccessToast) {
        toast.add({
          title: 'Success',
          description: options.successMessage || 'Item added successfully',
          color: 'green',
          timeout: 3000
        })
      }

      return result
    } catch (error: any) {
      // Remove temp item on error
      list.value = list.value.filter(item => item.id !== tempItem.id)

      // Call error callback
      if (options.onError) {
        options.onError(error, () => {
          list.value = list.value.filter(item => item.id !== tempItem.id)
        })
      }

      // Show error toast
      if (options.showErrorToast) {
        toast.add({
          title: 'Error',
          description: options.errorMessage || error.message || 'Failed to add item',
          color: 'red',
          timeout: 5000
        })
      }

      console.error('Optimistic add failed:', error)
      return null
    }
  }

  /**
   * Optimistic list item update
   */
  const optimisticUpdateItem = async <T extends { id: number | string }>(
    list: Ref<T[]>,
    itemId: number | string,
    updates: Partial<T>,
    apiCall: () => Promise<T>,
    options: OptimisticUpdateOptions<T> = {}
  ): Promise<T | null> => {
    // Store original list for rollback
    const originalList = JSON.parse(JSON.stringify(list.value))

    // Update item immediately
    list.value = list.value.map(item =>
      item.id === itemId ? { ...item, ...updates } : item
    )

    try {
      // Perform actual API call
      const result = await apiCall()

      // Update with real result
      list.value = list.value.map(item =>
        item.id === itemId ? result : item
      )

      // Call success callback
      if (options.onSuccess) {
        options.onSuccess(result)
      }

      // Show success toast
      if (options.showSuccessToast) {
        toast.add({
          title: 'Success',
          description: options.successMessage || 'Item updated successfully',
          color: 'green',
          timeout: 3000
        })
      }

      return result
    } catch (error: any) {
      // Rollback to original list
      list.value = originalList

      // Call error callback
      if (options.onError) {
        options.onError(error, () => {
          list.value = originalList
        })
      }

      // Show error toast
      if (options.showErrorToast) {
        toast.add({
          title: 'Error',
          description: options.errorMessage || error.message || 'Failed to update item',
          color: 'red',
          timeout: 5000
        })
      }

      console.error('Optimistic update failed:', error)
      return null
    }
  }

  /**
   * Optimistic list item deletion
   */
  const optimisticDelete = async <T extends { id: number | string }>(
    list: Ref<T[]>,
    itemId: number | string,
    apiCall: () => Promise<void>,
    options: OptimisticUpdateOptions<void> = {}
  ): Promise<boolean> => {
    // Store original list for rollback
    const originalList = JSON.parse(JSON.stringify(list.value))

    // Remove item immediately
    list.value = list.value.filter(item => item.id !== itemId)

    try {
      // Perform actual API call
      await apiCall()

      // Call success callback
      if (options.onSuccess) {
        options.onSuccess()
      }

      // Show success toast
      if (options.showSuccessToast) {
        toast.add({
          title: 'Success',
          description: options.successMessage || 'Item deleted successfully',
          color: 'green',
          timeout: 3000
        })
      }

      return true
    } catch (error: any) {
      // Rollback to original list
      list.value = originalList

      // Call error callback
      if (options.onError) {
        options.onError(error, () => {
          list.value = originalList
        })
      }

      // Show error toast
      if (options.showErrorToast) {
        toast.add({
          title: 'Error',
          description: options.errorMessage || error.message || 'Failed to delete item',
          color: 'red',
          timeout: 5000
        })
      }

      console.error('Optimistic delete failed:', error)
      return false
    }
  }

  return {
    optimisticUpdate,
    optimisticAdd,
    optimisticUpdateItem,
    optimisticDelete
  }
}
