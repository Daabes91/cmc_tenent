// Connection Status Composable
import { ref, computed } from 'vue'

const isOnline = ref(true)
const lastOnlineTime = ref<Date | null>(null)
const connectionLostVisible = ref(false)

export const useConnectionStatus = () => {
  // Check connection by making a simple request
  const checkConnection = async (): Promise<boolean> => {
    try {
      const response = await fetch('/api/health', {
        method: 'HEAD',
        cache: 'no-cache'
      })
      return response.ok
    } catch {
      return false
    }
  }

  // Update online status
  const updateOnlineStatus = async () => {
    const online = await checkConnection()
    
    if (online && !isOnline.value) {
      // Connection restored
      isOnline.value = true
      connectionLostVisible.value = false
      lastOnlineTime.value = new Date()
    } else if (!online && isOnline.value) {
      // Connection lost
      isOnline.value = false
      connectionLostVisible.value = true
    }
  }

  // Handle online event
  const handleOnline = () => {
    isOnline.value = true
    connectionLostVisible.value = false
    lastOnlineTime.value = new Date()
  }

  // Handle offline event
  const handleOffline = () => {
    isOnline.value = false
    connectionLostVisible.value = true
  }

  // Retry connection
  const retryConnection = async () => {
    await updateOnlineStatus()
  }

  // Setup event listeners (client-side only)
  if (import.meta.client) {
    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)

    // Check connection periodically when offline
    const checkInterval = setInterval(() => {
      if (!isOnline.value) {
        updateOnlineStatus()
      }
    }, 5000)

    // Cleanup on unmount
    onUnmounted(() => {
      window.removeEventListener('online', handleOnline)
      window.removeEventListener('offline', handleOffline)
      clearInterval(checkInterval)
    })
  }

  return {
    isOnline: computed(() => isOnline.value),
    connectionLostVisible: computed(() => connectionLostVisible.value),
    lastOnlineTime: computed(() => lastOnlineTime.value),
    checkConnection,
    updateOnlineStatus,
    retryConnection,
    hideConnectionLost: () => {
      connectionLostVisible.value = false
    }
  }
}
