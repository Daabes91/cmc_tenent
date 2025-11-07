/**
 * RTL-aware toast wrapper
 * Positioning is handled automatically via CSS based on document dir attribute
 */
import { computed } from "vue"

export const useRTLToast = () => {
  const toast = useToast()

  const getDocumentDirection = () => {
    if (typeof document !== 'undefined') {
      return document.documentElement.dir === 'rtl' || document.body.dir === 'rtl'
    }
    return false
  }

  return {
    add: (options: any) => toast.add(options),
    isRTL: computed(() => getDocumentDirection()),
    position: computed(() => getDocumentDirection() ? 'top-left' : 'top-right')
  }
}