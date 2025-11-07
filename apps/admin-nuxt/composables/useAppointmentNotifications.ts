export const useAppointmentNotifications = () => {
  const toast = useToast();
  const config = useRuntimeConfig();
  const auth = useAuth();
  const { t } = useI18n();
  const { request } = useAdminApi();
  const { preferences, browserNotificationsAvailable } = useNotificationPreferences();

  const isEnabled = ref(preferences.value.appointmentNotifications);
  const eventSource = ref<EventSource | null>(null);
  let reconnectTimeout: ReturnType<typeof setTimeout> | null = null;
  let pollingTimer: ReturnType<typeof setInterval> | null = null;
  const isPolling = ref(false);
  const lastPollTimestamp = ref<string>(new Date(Date.now() - 5 * 60 * 1000).toISOString());
  const isConnected = ref(false);
  const recentNotifications = ref<any[]>([]);
  const seenAppointmentIds = ref<number[]>([]);
  const MAX_SEEN_IDS = 200;

  // Show browser notification
  const showBrowserNotification = (title: string, body: string, appointmentId: number) => {
    if (!preferences.value.browserNotificationsEnabled || !browserNotificationsAvailable.value) {
      return;
    }

    if (process.client && 'Notification' in window && Notification.permission === 'granted') {
      const notification = new Notification(title, {
        body,
        icon: '/admin-favicon.ico',
        badge: '/admin-favicon.ico',
        tag: `appointment-${appointmentId}`,
        requireInteraction: false,
        silent: !preferences.value.soundEnabled
      });

      notification.onclick = () => {
        window.focus();
        navigateTo(`/appointments/${appointmentId}`);
        notification.close();
      };

      // Auto-close after 8 seconds
      setTimeout(() => notification.close(), 8000);
    }
  };

  // Handle new appointment notification
  const handleNewAppointment = (appointment: any) => {
    if (!appointment?.id) {
      return;
    }

    if (seenAppointmentIds.value.includes(appointment.id)) {
      return;
    }
    seenAppointmentIds.value.unshift(appointment.id);
    if (seenAppointmentIds.value.length > MAX_SEEN_IDS) {
      seenAppointmentIds.value.pop();
    }

    // Handle Unix timestamps (in seconds) and ISO strings
    let scheduledDate: Date;
    if (typeof appointment.scheduledAt === 'number') {
      // Unix timestamp: if less than 10 billion, it's in seconds (dates before 2286)
      const timestamp = appointment.scheduledAt < 10000000000 ? appointment.scheduledAt * 1000 : appointment.scheduledAt;
      scheduledDate = new Date(timestamp);
    } else {
      scheduledDate = new Date(appointment.scheduledAt);
    }

    const timeStr = scheduledDate.toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit'
    });

    const title = '🗓️ New Appointment';
    const description = `${appointment.patientName} - ${appointment.serviceName} at ${timeStr}`;

    // Store in recent notifications (max 10)
    recentNotifications.value.unshift({
      id: appointment.id,
      title: description,
      icon: 'i-lucide-calendar-plus',
      to: `/appointments/${appointment.id}`,
      timestamp: new Date()
    });
    if (recentNotifications.value.length > 10) {
      recentNotifications.value.pop();
    }

    // Toast notification
    if (preferences.value.appointmentNotifications) {
      toast.add({
        id: `appointment-${appointment.id}`,
        title,
        description,
        color: 'violet',
        timeout: 8000,
        icon: 'i-lucide-calendar-plus',
        actions: [{
          label: 'View',
          click: () => {
            navigateTo(`/appointments/${appointment.id}`);
          }
        }]
      });
    }

    // Browser notification
    showBrowserNotification(title, description, appointment.id);
  };

  const pollForAppointments = async () => {
    if (!import.meta.client) {
      return;
    }
    if (!isEnabled.value || !auth.isAuthenticated.value || isPolling.value) {
      return;
    }

    const pollStartedAt = new Date().toISOString();
    const query: Record<string, string | number> = {
      limit: 10
    };
    if (lastPollTimestamp.value) {
      query.since = lastPollTimestamp.value;
    }

    isPolling.value = true;
    try {
      const appointments = await request<any[]>("/appointments/recent", { query });
      if (Array.isArray(appointments) && appointments.length) {
        appointments.forEach(handleNewAppointment);
      }
    } catch (error) {
      console.error('Failed to poll appointment notifications:', error);
    } finally {
      lastPollTimestamp.value = pollStartedAt;
      isPolling.value = false;
    }
  };

  const stopPolling = () => {
    if (pollingTimer) {
      clearInterval(pollingTimer);
      pollingTimer = null;
    }
  };

  const startPolling = () => {
    if (!import.meta.client || !isEnabled.value) {
      return;
    }

    stopPolling();
    const intervalSeconds = Math.max(5, preferences.value.pollingInterval || 30);
    pollingTimer = setInterval(() => {
      pollForAppointments();
    }, intervalSeconds * 1000);

    pollForAppointments();
  };

  // Connect to SSE stream
  const closeEventSource = () => {
    if (eventSource.value) {
      eventSource.value.close();
      eventSource.value = null;
      isConnected.value = false;
    }
    if (reconnectTimeout) {
      clearTimeout(reconnectTimeout);
      reconnectTimeout = null;
    }
  };

  const connect = () => {
    if (!auth.isAuthenticated.value || !isEnabled.value || !process.client) {
      return;
    }

    // Close existing connection
    closeEventSource();

    try {
      const token = auth.accessToken.value;
      // EventSource doesn't support custom headers, so we pass token as query param
      const url = `${useApiBase()}/appointments/notifications/stream?token=${encodeURIComponent(token)}`;

      const es = new EventSource(url);

      es.onopen = () => {
        console.log('SSE connection established');
        isConnected.value = true;
      };

      es.addEventListener('connected', (event: MessageEvent) => {
        console.log('SSE connected event:', event.data);
      });

      es.addEventListener('new-appointment', (event: MessageEvent) => {
        try {
          const appointment = JSON.parse(event.data);
          handleNewAppointment(appointment);
        } catch (error) {
          console.error('Failed to parse appointment notification:', error);
        }
      });

      es.onerror = (error) => {
        console.error('SSE error:', error);
        isConnected.value = false;
        pollForAppointments();

        // Attempt to reconnect after 5 seconds
        if (reconnectTimeout) {
          clearTimeout(reconnectTimeout);
        }
        reconnectTimeout = setTimeout(() => {
          console.log('Attempting to reconnect SSE...');
          connect();
        }, 5000);
      };

      eventSource.value = es;
    } catch (error) {
      console.error('Failed to establish SSE connection:', error);
      isConnected.value = false;
    }
  };

  // Disconnect from SSE stream
  const disconnect = () => {
    closeEventSource();
    stopPolling();
  };

  // Enable/disable notifications
  const setEnabled = (enabled: boolean) => {
    isEnabled.value = enabled;
    if (!enabled) {
      disconnect();
    } else {
      startPolling();
      if (!isConnected.value) {
        connect();
      }
    }
  };

  if (import.meta.client) {
    watch(
      () => preferences.value.appointmentNotifications,
      (enabled) => {
        setEnabled(enabled);
      },
      { immediate: true }
    );

    watch(
      () => preferences.value.pollingInterval,
      () => {
        if (isEnabled.value) {
          startPolling();
        }
      }
    );

    watch(
      () => auth.isAuthenticated.value,
      (loggedIn) => {
        if (!loggedIn) {
          disconnect();
          seenAppointmentIds.value = [];
        } else if (isEnabled.value) {
          startPolling();
          connect();
        }
      },
      { immediate: true }
    );

    watch(
      () => auth.accessToken.value,
      (token, previous) => {
        if (!token) {
          disconnect();
          seenAppointmentIds.value = [];
          return;
        }

        if (token !== previous && isEnabled.value) {
          // Token rotated (login or refresh). Re-establish realtime channels.
          startPolling();
          connect();
        }
      },
      { immediate: true }
    );
  }

  onMounted(() => {
    startPolling();
    connect();
  });

  // Cleanup on unmount
  onUnmounted(() => {
    disconnect();
    stopPolling();
  });

  return {
    isEnabled,
    isConnected,
    recentNotifications,
    connect,
    disconnect,
    setEnabled
  };
};
