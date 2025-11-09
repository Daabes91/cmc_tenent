<template>
  <ClientOnly>
    <Teleport to="body">
      <div
        class="fixed top-4 right-4 z-[9999] flex flex-col gap-3 pointer-events-none"
        :class="{ 'left-4': isRTL }"
      >
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="pointer-events-auto"
        >
          <ToastNotification
            :notification="notification"
            @dismiss="handleDismiss"
          />
        </div>
      </div>
    </Teleport>
  </ClientOnly>
</template>

<script setup lang="ts">
import { useNotifications } from '~/composables/useNotifications'

const { notifications, dismissNotification } = useNotifications()
const { locale } = useI18n()

const isRTL = computed(() => locale.value === 'ar')

const handleDismiss = (id: string) => {
  dismissNotification(id)
}
</script>
