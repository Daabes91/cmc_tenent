<template>
  <UDropdown :items="menuItems" :popper="{ placement: 'bottom-end' }">
    <button
      class="flex items-center space-x-2 px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
      :aria-label="$t('nav.userMenu')"
    >
      <div class="w-8 h-8 bg-primary-600 rounded-full flex items-center justify-center">
        <UIcon name="i-heroicons-user" class="w-5 h-5 text-white" />
      </div>
      <div class="hidden lg:block text-left">
        <p class="text-sm font-medium text-gray-900 dark:text-white">
          {{ managerName || 'SAAS Manager' }}
        </p>
        <p class="text-xs text-gray-500 dark:text-gray-400">
          {{ $t('nav.manager') }}
        </p>
      </div>
      <UIcon name="i-heroicons-chevron-down" class="hidden lg:block w-4 h-4 text-gray-500 dark:text-gray-400" />
    </button>

    <template #item="{ item }">
      <div class="flex items-center space-x-2">
        <UIcon :name="item.icon" class="w-4 h-4" />
        <span>{{ item.label }}</span>
      </div>
    </template>
  </UDropdown>
</template>

<script setup lang="ts">
const { t } = useI18n()
const { logout } = useSaasAuth()
const router = useRouter()

// Get manager name from auth state
const managerName = computed(() => {
  const auth = useSaasAuth()
  return auth.managerName.value
})

// Handle logout
const handleLogout = async () => {
  try {
    logout()
    await router.push('/login')
  } catch (error) {
    console.error('Logout error:', error)
  }
}

// Get available locales
const { locale, locales, setLocale } = useI18n()

// Handle language change
const changeLanguage = (code: string) => {
  setLocale(code)
}

// Menu items
const menuItems = computed(() => [
  [
    {
      label: t('nav.changeLanguage'),
      icon: 'i-heroicons-language',
      children: locales.value.map((loc: any) => ({
        label: loc.name,
        icon: loc.code === locale.value ? 'i-heroicons-check' : undefined,
        click: () => changeLanguage(loc.code)
      }))
    }
  ],
  [
    {
      label: t('auth.logout'),
      icon: 'i-heroicons-arrow-right-on-rectangle',
      click: handleLogout
    }
  ]
])
</script>
