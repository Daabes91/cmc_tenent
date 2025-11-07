export const useNotificationPreferences = () => {
  const STORAGE_KEY = 'clinic_notification_preferences';

  interface NotificationPreferences {
    soundEnabled: boolean;
    browserNotificationsEnabled: boolean;
    appointmentNotifications: boolean;
    cancellationNotifications: boolean;
    pollingInterval: number; // in seconds
  }

  const defaultPreferences: NotificationPreferences = {
    soundEnabled: true,
    browserNotificationsEnabled: true,
    appointmentNotifications: true,
    cancellationNotifications: true,
    pollingInterval: 30
  };

  const preferences = ref<NotificationPreferences>(defaultPreferences);

  // Load preferences from localStorage
  const loadPreferences = () => {
    if (process.client) {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        try {
          preferences.value = { ...defaultPreferences, ...JSON.parse(stored) };
        } catch (e) {
          console.error('Failed to parse notification preferences:', e);
        }
      }
    }
  };

  // Save preferences to localStorage
  const savePreferences = (newPreferences: Partial<NotificationPreferences>) => {
    preferences.value = { ...preferences.value, ...newPreferences };
    if (process.client) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences.value));
    }
  };

  // Request browser notification permission
  const requestBrowserPermission = async () => {
    if (!process.client || !('Notification' in window)) {
      return false;
    }

    if (Notification.permission === 'granted') {
      return true;
    }

    if (Notification.permission !== 'denied') {
      const permission = await Notification.requestPermission();
      return permission === 'granted';
    }

    return false;
  };

  // Check if browser notifications are available
  const browserNotificationsAvailable = computed(() => {
    if (!process.client) return false;
    return 'Notification' in window && Notification.permission === 'granted';
  });

  // Initialize
  loadPreferences();

  return {
    preferences: readonly(preferences),
    savePreferences,
    requestBrowserPermission,
    browserNotificationsAvailable
  };
};
