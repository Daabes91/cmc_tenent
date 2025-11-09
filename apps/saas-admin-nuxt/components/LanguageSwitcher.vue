<template>
  <UDropdown :items="languageItems" :popper="{ placement: 'bottom-end' }">
    <button
      class="flex items-center space-x-2 px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
      :aria-label="$t('nav.changeLanguage')"
    >
      <UIcon name="i-heroicons-language" class="w-5 h-5 text-gray-700 dark:text-gray-300" />
      <span class="hidden lg:inline text-sm font-medium text-gray-700 dark:text-gray-300">
        {{ currentLocale.name }}
      </span>
    </button>

    <template #item="{ item }">
      <div class="flex items-center justify-between w-full">
        <span>{{ item.label }}</span>
        <UIcon
          v-if="item.active"
          name="i-heroicons-check"
          class="w-4 h-4 text-primary-600"
        />
      </div>
    </template>
  </UDropdown>
</template>

<script setup lang="ts">
const { locale, locales, setLocale } = useI18n()

// Get current locale info
const currentLocale = computed(() => {
  const current = locales.value.find((l: any) => l.code === locale.value)
  return current || { code: 'en', name: 'English' }
})

// Handle language change
const changeLanguage = (code: string) => {
  setLocale(code)
}

// Language menu items
const languageItems = computed(() => [
  locales.value.map((loc: any) => ({
    label: loc.name,
    code: loc.code,
    active: loc.code === locale.value,
    click: () => changeLanguage(loc.code)
  }))
])
</script>
