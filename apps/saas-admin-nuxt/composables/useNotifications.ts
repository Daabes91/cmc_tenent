// Notification System Composable
import { ref, computed } from 'vue'

export type NotificationType = 'success' | 'error' | 'warning' | 'info'

export interface Notification {
  id: string
  type: NotificationType
  title: string
  message: string
  timestamp: Date
  dismissed: boolean
  autoDismiss: boolean
}

interface NotificationOptions {
  title: string
  message: string
  type?: NotificationType
  autoDismiss?: boolean
  duration?: number
}

// Global notification state
const notifications = ref<Notification[]>([])
const notificationHistory = ref<Notification[]>([])

export const useNotifications = () => {
  // Generate unique ID
  const generateId = (): string => {
    return `notification-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
  }

  // Add notification
  const addNotification = (options: NotificationOptions): string => {
    const notification: Notification = {
      id: generateId(),
      type: options.type || 'info',
      title: options.title,
      message: options.message,
      timestamp: new Date(),
      dismissed: false,
      autoDismiss: options.autoDismiss !== undefined ? options.autoDismiss : options.type === 'success'
    }

    // Add to active notifications
    notifications.value.push(notification)

    // Add to history
    notificationHistory.value.unshift(notification)

    // Auto-dismiss if enabled
    if (notification.autoDismiss) {
      const duration = options.duration || 5000
      setTimeout(() => {
        dismissNotification(notification.id)
      }, duration)
    }

    return notification.id
  }

  // Dismiss notification
  const dismissNotification = (id: string) => {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index !== -1) {
      notifications.value[index].dismissed = true
      // Remove from active notifications after animation
      setTimeout(() => {
        notifications.value = notifications.value.filter(n => n.id !== id)
      }, 300)
    }

    // Update history
    const historyIndex = notificationHistory.value.findIndex(n => n.id === id)
    if (historyIndex !== -1) {
      notificationHistory.value[historyIndex].dismissed = true
    }
  }

  // Clear all notifications
  const clearAll = () => {
    notifications.value.forEach(n => {
      n.dismissed = true
    })
    setTimeout(() => {
      notifications.value = []
    }, 300)
  }

  // Convenience methods for different notification types
  const success = (title: string, message: string, duration?: number) => {
    return addNotification({ title, message, type: 'success', autoDismiss: true, duration })
  }

  const error = (title: string, message: string) => {
    return addNotification({ title, message, type: 'error', autoDismiss: false })
  }

  const warning = (title: string, message: string, duration?: number) => {
    return addNotification({ title, message, type: 'warning', autoDismiss: true, duration })
  }

  const info = (title: string, message: string, duration?: number) => {
    return addNotification({ title, message, type: 'info', autoDismiss: true, duration })
  }

  // Clear old history (keep last 50)
  const pruneHistory = () => {
    if (notificationHistory.value.length > 50) {
      notificationHistory.value = notificationHistory.value.slice(0, 50)
    }
  }

  // Computed properties
  const activeNotifications = computed(() => 
    notifications.value.filter(n => !n.dismissed)
  )

  const unreadCount = computed(() => 
    notificationHistory.value.filter(n => !n.dismissed).length
  )

  return {
    // State
    notifications: activeNotifications,
    notificationHistory: computed(() => notificationHistory.value),
    unreadCount,

    // Methods
    addNotification,
    dismissNotification,
    clearAll,
    success,
    error,
    warning,
    info,
    pruneHistory
  }
}
