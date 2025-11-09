<template>
  <UModal v-model="isOpen" :prevent-close="true">
    <UCard>
      <template #header>
        <div class="flex items-center gap-3">
          <UIcon name="i-heroicons-exclamation-triangle" class="w-6 h-6 text-amber-500" />
          <h3 class="text-lg font-semibold">{{ $t('security.sessionTimeout.title') }}</h3>
        </div>
      </template>

      <div class="space-y-4">
        <p class="text-gray-600 dark:text-gray-400">
          {{ $t('security.sessionTimeout.message') }}
        </p>
        
        <div class="bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-lg p-4">
          <p class="text-sm text-amber-800 dark:text-amber-200">
            {{ $t('security.sessionTimeout.warning') }}
          </p>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-3">
          <UButton
            color="gray"
            variant="ghost"
            @click="handleLogout"
          >
            {{ $t('security.sessionTimeout.logout') }}
          </UButton>
          <UButton
            color="primary"
            @click="handleExtend"
          >
            {{ $t('security.sessionTimeout.extend') }}
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
const { showTimeoutWarning, extendSession } = useSecurity()
const { logout } = useSaasAuth()
const router = useRouter()

const isOpen = computed({
  get: () => showTimeoutWarning.value,
  set: (value) => {
    if (!value) {
      showTimeoutWarning.value = false
    }
  }
})

const handleExtend = () => {
  extendSession()
  isOpen.value = false
}

const handleLogout = () => {
  logout()
  router.push('/login')
}
</script>
