<template>
  <div class="p-8 space-y-8">
    <div class="max-w-4xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">
          Internationalization Test Page
        </h1>
        <p class="text-gray-600 dark:text-gray-400">
          Test the i18n implementation with live language switching
        </p>
      </div>

      <!-- Language Switcher -->
      <UCard class="mb-8">
        <template #header>
          <h2 class="text-xl font-semibold">Language Switcher</h2>
        </template>
        
        <div class="space-y-4">
          <div class="flex items-center gap-4">
            <span class="text-sm font-medium">Current Language:</span>
            <UBadge size="lg" color="primary">
              {{ currentLocaleName }}
            </UBadge>
          </div>
          
          <div class="flex gap-2">
            <UButton
              v-for="loc in locales"
              :key="loc.code"
              :color="locale === loc.code ? 'primary' : 'gray'"
              :variant="locale === loc.code ? 'solid' : 'outline'"
              @click="setLocale(loc.code)"
            >
              {{ loc.name }}
            </UButton>
          </div>
        </div>
      </UCard>

      <!-- Translation Examples -->
      <UCard class="mb-8">
        <template #header>
          <h2 class="text-xl font-semibold">Translation Examples</h2>
        </template>
        
        <div class="space-y-6">
          <!-- Common Translations -->
          <div>
            <h3 class="text-lg font-medium mb-3">Common UI Elements</h3>
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <UButton color="primary">{{ $t('common.save') }}</UButton>
              <UButton color="gray">{{ $t('common.cancel') }}</UButton>
              <UButton color="red">{{ $t('common.delete') }}</UButton>
              <UButton color="blue">{{ $t('common.edit') }}</UButton>
            </div>
          </div>

          <!-- Navigation -->
          <div>
            <h3 class="text-lg font-medium mb-3">Navigation</h3>
            <div class="flex flex-wrap gap-2">
              <UBadge>{{ $t('nav.dashboard') }}</UBadge>
              <UBadge>{{ $t('nav.tenants') }}</UBadge>
              <UBadge>{{ $t('nav.analytics') }}</UBadge>
              <UBadge>{{ $t('nav.auditLogs') }}</UBadge>
            </div>
          </div>

          <!-- Dashboard Metrics -->
          <div>
            <h3 class="text-lg font-medium mb-3">Dashboard Metrics</h3>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
                <p class="text-sm text-gray-600 dark:text-gray-400">{{ $t('dashboard.totalTenants') }}</p>
                <p class="text-2xl font-bold text-gray-900 dark:text-white">42</p>
              </div>
              <div class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
                <p class="text-sm text-gray-600 dark:text-gray-400">{{ $t('dashboard.activeTenants') }}</p>
                <p class="text-2xl font-bold text-gray-900 dark:text-white">38</p>
              </div>
              <div class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
                <p class="text-sm text-gray-600 dark:text-gray-400">{{ $t('dashboard.totalUsers') }}</p>
                <p class="text-2xl font-bold text-gray-900 dark:text-white">1,234</p>
              </div>
              <div class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
                <p class="text-sm text-gray-600 dark:text-gray-400">{{ $t('dashboard.activeUsers') }}</p>
                <p class="text-2xl font-bold text-gray-900 dark:text-white">987</p>
              </div>
            </div>
          </div>

          <!-- Tenant Form Labels -->
          <div>
            <h3 class="text-lg font-medium mb-3">Tenant Form</h3>
            <div class="space-y-3">
              <UFormGroup :label="$t('tenants.form.slug')" :help="$t('tenants.form.slugHelp')">
                <UInput :placeholder="$t('tenants.form.slugPlaceholder')" disabled />
              </UFormGroup>
              <UFormGroup :label="$t('tenants.form.name')" :help="$t('tenants.form.nameHelp')">
                <UInput :placeholder="$t('tenants.form.namePlaceholder')" disabled />
              </UFormGroup>
              <UFormGroup :label="$t('tenants.form.customDomain')" :help="$t('tenants.form.customDomainHelp')">
                <UInput :placeholder="$t('tenants.form.customDomainPlaceholder')" disabled />
              </UFormGroup>
            </div>
          </div>

          <!-- Status Labels -->
          <div>
            <h3 class="text-lg font-medium mb-3">Status Labels</h3>
            <div class="flex flex-wrap gap-2">
              <UBadge color="green">{{ $t('tenants.active') }}</UBadge>
              <UBadge color="gray">{{ $t('tenants.inactive') }}</UBadge>
              <UBadge color="red">{{ $t('tenants.deleted') }}</UBadge>
              <UBadge color="blue">{{ $t('tenants.all') }}</UBadge>
            </div>
          </div>

          <!-- Notifications -->
          <div>
            <h3 class="text-lg font-medium mb-3">Notification Messages</h3>
            <div class="space-y-2">
              <UAlert
                color="green"
                variant="subtle"
                :title="$t('notifications.success')"
                :description="$t('notifications.tenantCreatedMessage', { name: 'Test Clinic' })"
              />
              <UAlert
                color="red"
                variant="subtle"
                :title="$t('notifications.error')"
                :description="$t('errors.networkError')"
              />
              <UAlert
                color="amber"
                variant="subtle"
                :title="$t('notifications.warning')"
                :description="$t('notifications.systemHealthDegraded', { metric: 'API Response Time' })"
              />
            </div>
          </div>
        </div>
      </UCard>

      <!-- RTL Test -->
      <UCard>
        <template #header>
          <h2 class="text-xl font-semibold">RTL Layout Test</h2>
        </template>
        
        <div class="space-y-4">
          <p class="text-sm text-gray-600 dark:text-gray-400">
            Switch to Arabic to see the RTL layout in action. The entire layout should mirror, including:
          </p>
          <ul class="list-disc list-inside space-y-1 text-sm text-gray-600 dark:text-gray-400">
            <li>Text alignment (right-aligned in RTL)</li>
            <li>Spacing and margins (reversed)</li>
            <li>Form layouts (labels on the right)</li>
            <li>Button groups (reversed order)</li>
            <li>Navigation menus (reversed)</li>
          </ul>
          
          <div class="mt-4 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
            <p class="text-sm font-medium text-blue-900 dark:text-blue-100">
              Current Direction: <code class="px-2 py-1 bg-blue-100 dark:bg-blue-800 rounded">{{ currentDirection }}</code>
            </p>
          </div>
        </div>
      </UCard>

      <!-- Back Button -->
      <div class="mt-8">
        <UButton
          to="/"
          color="gray"
          variant="outline"
          icon="i-heroicons-arrow-left"
        >
          {{ $t('common.back') }} {{ $t('nav.dashboard') }}
        </UButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const { locale, locales, setLocale } = useI18n()

const currentLocaleName = computed(() => {
  const current = locales.value.find((l: any) => l.code === locale.value)
  return current?.name || 'Unknown'
})

const currentDirection = computed(() => {
  if (process.client) {
    return document.documentElement.getAttribute('dir') || 'ltr'
  }
  return locale.value === 'ar' ? 'rtl' : 'ltr'
})

definePageMeta({
  layout: 'default'
})
</script>
