<template>
  <div class="relative min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 text-slate-900 transition-colors duration-300 antialiased dark:from-slate-950 dark:via-slate-950 dark:to-slate-900 dark:text-slate-100">
    <!-- Gradient Background Orbs -->
    <div aria-hidden="true" class="pointer-events-none absolute inset-0 -z-[1] overflow-hidden">
      <div class="absolute left-1/2 top-[-20%] h-[480px] w-[480px] -translate-x-1/2 rounded-full bg-blue-300/40 blur-[180px] dark:bg-blue-500/20"></div>
      <div class="absolute right-[-10%] bottom-[-25%] h-[360px] w-[360px] rounded-full bg-emerald-200/30 blur-[140px] dark:bg-emerald-400/10"></div>
      <div class="absolute left-[-18%] top-1/3 h-72 w-72 rounded-full bg-sky-200/30 blur-[160px] dark:bg-sky-400/10"></div>
    </div>

    <!-- Mobile Header -->
    <div class="lg:hidden fixed top-0 left-0 right-0 z-50 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-950/80 dark:border-white/10">
      <div class="flex items-center justify-between px-4 py-3">
        <button
          @click="toggleMobileSidebar"
          class="p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-white/10 transition-colors"
          :aria-label="$t('nav.toggleMenu')"
        >
          <UIcon name="i-heroicons-bars-3" class="w-6 h-6 text-slate-700 dark:text-slate-200" />
        </button>

        <div class="flex items-center gap-2">
          <div class="w-8 h-8 bg-primary-gradient rounded-lg flex items-center justify-center text-white shadow-lg">
            <UIcon name="i-heroicons-building-office-2" class="w-5 h-5" />
          </div>
          <span class="text-sm font-semibold text-slate-900 dark:text-white">
            {{ $t('app.name') }}
          </span>
        </div>

        <div class="flex items-center gap-2">
          <NotificationBell />
          <button
            @click="toggleDarkMode"
            class="p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-white/10 transition-colors"
            :aria-label="$t('accessibility.toggleTheme')"
          >
            <UIcon v-if="isDark" name="i-heroicons-sun" class="w-5 h-5" />
            <UIcon v-else name="i-heroicons-moon" class="w-5 h-5" />
          </button>
        </div>
      </div>
    </div>

    <!-- Mobile Sidebar Overlay -->
    <Transition
      enter-active-class="transition-opacity duration-300"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-300"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="isMobileSidebarOpen"
        class="fixed inset-0 bg-slate-900/50 z-40 lg:hidden"
        @click="closeMobileSidebar"
      />
    </Transition>

    <!-- Main Container -->
    <div class="flex min-h-screen">
      <!-- Desktop Sidebar -->
      <Transition
        enter-active-class="transition-transform duration-300"
        enter-from-class="-translate-x-full"
        enter-to-class="translate-x-0"
        leave-active-class="transition-transform duration-300"
        leave-from-class="translate-x-0"
        leave-to-class="-translate-x-full"
      >
        <aside
          v-if="isMobileSidebarOpen || !isMobile"
          class="fixed lg:static top-0 left-0 bottom-0 w-72 flex-col border-r border-slate-200/60 bg-white/80 backdrop-blur-xl transition-colors duration-300 dark:border-white/10 dark:bg-slate-950/80 z-50 lg:z-auto flex overflow-hidden"
        >
          <!-- Sidebar Header -->
          <div class="flex h-20 items-center gap-3.5 border-b border-slate-200/60 px-6 dark:border-white/10">
            <div class="relative flex h-11 w-11 items-center justify-center overflow-hidden rounded-xl bg-primary-gradient text-2xl shadow-lg shadow-blue-500/30">
              <UIcon name="i-heroicons-building-office-2" class="w-6 h-6 text-white" />
              <div class="pointer-events-none absolute -inset-0.5 rounded-xl bg-white/60 opacity-40 blur-md dark:bg-white/20"></div>
            </div>
            <div class="flex-1">
              <p class="text-[11px] font-semibold uppercase tracking-[0.35em] text-primary-600 dark:text-primary-200">
                SAAS MANAGER
              </p>
              <p class="text-sm font-semibold text-slate-900 dark:text-white/90">
                {{ $t('app.name') }}
              </p>
            </div>
            <button
              @click="closeMobileSidebar"
              class="lg:hidden p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-white/10 transition-colors"
              :aria-label="$t('nav.closeMenu')"
            >
              <UIcon name="i-heroicons-x-mark" class="w-5 h-5 text-slate-500 dark:text-slate-400" />
            </button>
          </div>

          <!-- Quick Stats -->
          <div class="border-b border-slate-200/60 px-6 py-5 dark:border-white/10">
            <p class="text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400">
              {{ $t('layout.sidebar.today') }}
            </p>
            <div class="mt-3 grid grid-cols-2 gap-3 text-sm">
              <div class="rounded-lg border border-slate-200/60 bg-white/80 p-3 shadow-sm dark:border-white/10 dark:bg-white/5">
                <p class="text-xs text-slate-500 dark:text-slate-400">
                  {{ $t('layout.sidebar.metrics.totalTenants') }}
                </p>
                <p class="mt-1 text-lg font-semibold text-slate-900 dark:text-white">{{ totalTenants }}</p>
              </div>
              <div class="rounded-lg border border-slate-200/60 bg-white/80 p-3 shadow-sm dark:border-white/10 dark:bg-white/5">
                <p class="text-xs text-slate-500 dark:text-slate-400">
                  {{ $t('layout.sidebar.metrics.activeTenants') }}
                </p>
                <p class="mt-1 text-lg font-semibold text-slate-900 dark:text-white">{{ activeTenants }}</p>
              </div>
            </div>
          </div>

          <!-- Navigation Links -->
          <nav class="flex-1 overflow-y-auto px-4 py-6 scrollbar-thin scrollbar-track-transparent scrollbar-thumb-slate-300/60 dark:scrollbar-thumb-white/10">
            <UVerticalNavigation :links="navigation" :ui="navUi" />
          </nav>

          <!-- Sidebar Footer -->
          <div class="border-t border-slate-200/60 p-5 dark:border-white/10">
            <div class="relative overflow-hidden rounded-2xl border border-slate-200/60 bg-gradient-to-br from-blue-50 via-white to-slate-50 p-5 shadow-lg shadow-slate-200/50 transition-colors duration-300 dark:border-white/10 dark:bg-white/5 dark:from-white/5 dark:via-white/5 dark:to-white/5 dark:shadow-black/20">
              <div class="absolute -right-6 -top-6 h-24 w-24 rounded-full bg-blue-400/30 blur-2xl dark:bg-blue-500/30"></div>
              <div class="relative flex items-start gap-3">
                <div class="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-blue-700 text-white shadow-lg shadow-blue-500/40">
                  <UIcon name="i-heroicons-lifebuoy" class="h-5 w-5" />
                </div>
                <div class="flex-1">
                  <p class="text-sm font-semibold text-slate-900 dark:text-white">
                    {{ $t('layout.sidebar.support.title') }}
                  </p>
                  <p class="mt-1 text-xs leading-relaxed text-slate-600 dark:text-slate-300">
                    {{ $t('layout.sidebar.support.description') }}
                  </p>
                  <UButton
                    color="blue"
                    variant="soft"
                    size="sm"
                    class="mt-3 w-full justify-center"
                    icon="i-heroicons-chat-bubble-left-right"
                  >
                    {{ $t('layout.sidebar.support.action') }}
                  </UButton>
                </div>
              </div>
            </div>
          </div>
        </aside>
      </Transition>

      <!-- Main Content Area -->
      <div class="flex min-h-screen flex-1 flex-col">
        <!-- Desktop Header -->
        <header class="sticky top-0 z-30 border-b border-slate-200/60 bg-white/80 backdrop-blur-2xl transition-colors duration-300 dark:border-white/10 dark:bg-slate-950/80 hidden lg:block">
          <div class="flex h-20 items-center justify-between gap-4 px-6">
            <div class="flex flex-1 items-center gap-4">
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">
                {{ currentPageTitle }}
              </h1>
            </div>

            <div class="flex items-center gap-3">
              <!-- Notification Bell -->
              <NotificationBell />

              <!-- Language Switcher -->
              <LanguageSwitcher />

              <!-- Theme Toggle -->
              <button
                @click="toggleDarkMode"
                class="flex h-10 w-10 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 transition-colors dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:hover:bg-slate-800"
                :aria-label="$t('accessibility.toggleTheme')"
              >
                <UIcon v-if="isDark" name="i-heroicons-sun" class="h-5 w-5" />
                <UIcon v-else name="i-heroicons-moon" class="h-5 w-5" />
              </button>

              <!-- User Menu -->
              <UserMenu />
            </div>
          </div>
        </header>

        <!-- Page Content -->
        <main class="flex-1 p-4 lg:p-6 mt-14 lg:mt-0">
          <slot />
        </main>
      </div>
    </div>

    <!-- Session Timeout Warning -->
    <ClientOnly>
      <SessionTimeoutWarning />
    </ClientOnly>

    <!-- Mobile Bottom Navigation -->
    <nav
      class="lg:hidden fixed bottom-0 left-0 right-0 z-40 bg-white/90 backdrop-blur-xl border-t border-slate-200/60 dark:bg-slate-950/90 dark:border-white/10"
      role="navigation"
      :aria-label="$t('accessibility.mobileNavigation')"
    >
      <div class="flex items-center justify-around px-2 py-2 safe-area-inset-bottom">
        <NuxtLink
          v-for="item in mobileNavigation"
          :key="item.to"
          :to="item.to"
          class="flex flex-col items-center justify-center px-3 py-2 rounded-xl transition-colors min-w-[64px]"
          :class="[
            isActiveRoute(item.to)
              ? 'text-primary-700 dark:text-primary-400 bg-primary-50/50 dark:bg-primary-500/10'
              : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-white/5'
          ]"
        >
          <UIcon :name="item.icon" class="w-6 h-6 mb-1" />
          <span class="text-xs font-medium">{{ item.label }}</span>
        </NuxtLink>
      </div>
    </nav>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n()
const route = useRoute()
const colorMode = useColorMode()
const { initializeSessionMonitoring } = useSecurity()

// Theme management
const isDark = computed(() => colorMode.value === 'dark')
const toggleDarkMode = () => {
  colorMode.preference = isDark.value ? 'light' : 'dark'
}

// Initialize security monitoring
onMounted(() => {
  initializeSessionMonitoring()
})

// Mobile sidebar state
const isMobileSidebarOpen = ref(false)
const isMobile = ref(true)

const toggleMobileSidebar = () => {
  isMobileSidebarOpen.value = !isMobileSidebarOpen.value
}

const closeMobileSidebar = () => {
  isMobileSidebarOpen.value = false
}

// Handle responsive behavior
const updateMobileState = () => {
  isMobile.value = window.innerWidth < 1024
  if (!isMobile.value) {
    isMobileSidebarOpen.value = false
  }
}

onMounted(() => {
  updateMobileState()
  window.addEventListener('resize', updateMobileState)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateMobileState)
})

// Navigation items
const navigation = computed(() => [
  {
    label: t('nav.dashboard'),
    icon: 'i-heroicons-home',
    to: '/',
    badge: undefined
  },
  {
    label: t('nav.tenants'),
    icon: 'i-heroicons-building-office-2',
    to: '/tenants'
  },
  {
    label: t('nav.analytics'),
    icon: 'i-heroicons-chart-bar',
    to: '/analytics'
  },
  {
    label: t('nav.auditLogs'),
    icon: 'i-heroicons-document-text',
    to: '/audit-logs'
  }
])

// Mobile navigation (key items only)
const mobileNavigation = computed(() => [
  {
    label: t('nav.dashboard'),
    icon: 'i-heroicons-home',
    to: '/'
  },
  {
    label: t('nav.tenants'),
    icon: 'i-heroicons-building-office-2',
    to: '/tenants'
  },
  {
    label: t('nav.analytics'),
    icon: 'i-heroicons-chart-bar',
    to: '/analytics'
  },
  {
    label: t('nav.more'),
    icon: 'i-heroicons-ellipsis-horizontal-circle',
    to: '/audit-logs'
  }
])

// Navigation UI configuration
const navUi = {
  wrapper: 'space-y-1',
  base: 'group relative flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-150',
  active: 'bg-primary-50 text-primary-700 shadow-sm shadow-primary-200/50 dark:bg-primary-500/15 dark:text-primary-100 dark:shadow-primary-500/20',
  inactive: 'text-slate-700 hover:bg-slate-100/80 hover:text-slate-900 dark:text-slate-300 dark:hover:bg-white/5 dark:hover:text-white',
  icon: {
    base: 'flex-shrink-0 w-5 h-5',
    active: 'text-primary-600 dark:text-primary-400',
    inactive: 'text-slate-500 group-hover:text-slate-700 dark:text-slate-400 dark:group-hover:text-slate-200'
  },
  badge: {
    base: 'ml-auto',
    active: 'bg-primary-100 text-primary-700 dark:bg-primary-400/20 dark:text-primary-200',
    inactive: 'bg-slate-100 text-slate-600 dark:bg-white/10 dark:text-slate-300'
  }
}

// Page title
const currentPageTitle = computed(() => {
  const path = route.path
  if (path === '/') return t('nav.dashboard')
  if (path.startsWith('/tenants')) return t('nav.tenants')
  if (path.startsWith('/analytics')) return t('nav.analytics')
  if (path.startsWith('/audit-logs')) return t('nav.auditLogs')
  return t('app.name')
})

// Check if route is active
const isActiveRoute = (path: string) => {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}

// Mock metrics (replace with real data from composable)
const totalTenants = ref(0)
const activeTenants = ref(0)

// Load metrics
onMounted(() => {
  // You can fetch this from your composable
  // For now using mock data
  totalTenants.value = 12
  activeTenants.value = 10
})
</script>

<style scoped>
.scrollbar-thin::-webkit-scrollbar {
  width: 6px;
}

.scrollbar-thin::-webkit-scrollbar-track {
  background: transparent;
}

.scrollbar-thin::-webkit-scrollbar-thumb {
  @apply rounded-full bg-slate-300/60 dark:bg-white/10;
}

.scrollbar-thin::-webkit-scrollbar-thumb:hover {
  @apply bg-slate-400/80 dark:bg-white/20;
}

.safe-area-inset-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}

.skip-link {
  @apply sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4 focus:z-[9999] focus:px-4 focus:py-2 focus:bg-primary-600 focus:text-white focus:rounded-lg;
}
</style>
