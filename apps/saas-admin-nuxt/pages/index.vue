<template>
  <div class="space-y-6">
    <!-- Quick Actions -->
    <div class="flex items-center justify-between">
      <div>
        <p class="text-sm text-slate-600 dark:text-slate-400">
          Welcome back!
        </p>
      </div>
      <div class="flex items-center gap-3">
        <UButton
          icon="i-heroicons-plus"
          color="blue"
          size="md"
          @click="navigateTo('/tenants/new')"
        >
          {{ $t('dashboard.createTenant') }}
        </UButton>
        <UButton
          icon="i-heroicons-arrow-path"
          color="gray"
          variant="outline"
          size="md"
          :loading="loading"
          @click="refresh"
          :aria-label="$t('common.refresh')"
        />
      </div>
    </div>

    <!-- System Metrics Grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <!-- Total Tenants -->
      <div class="relative overflow-hidden rounded-2xl border border-slate-200/60 bg-white/80 backdrop-blur-sm p-6 shadow-sm transition-all duration-200 hover:shadow-md dark:border-white/10 dark:bg-white/5">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <p class="text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400">
              {{ $t('dashboard.totalTenants') }}
            </p>
            <p class="mt-2 text-3xl font-bold text-slate-900 dark:text-white">
              {{ metrics?.totalTenants ?? 0 }}
            </p>
          </div>
          <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 text-white shadow-lg shadow-blue-500/30">
            <UIcon name="i-heroicons-building-office-2" class="h-6 w-6" />
          </div>
        </div>
      </div>

      <!-- Active Tenants -->
      <div class="relative overflow-hidden rounded-2xl border border-slate-200/60 bg-white/80 backdrop-blur-sm p-6 shadow-sm transition-all duration-200 hover:shadow-md dark:border-white/10 dark:bg-white/5">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <p class="text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400">
              {{ $t('dashboard.activeTenants') }}
            </p>
            <p class="mt-2 text-3xl font-bold text-slate-900 dark:text-white">
              {{ metrics?.activeTenants ?? 0 }}
            </p>
          </div>
          <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-emerald-600 text-white shadow-lg shadow-emerald-500/30">
            <UIcon name="i-heroicons-check-circle" class="h-6 w-6" />
          </div>
        </div>
      </div>

      <!-- Total Users -->
      <div class="relative overflow-hidden rounded-2xl border border-slate-200/60 bg-white/80 backdrop-blur-sm p-6 shadow-sm transition-all duration-200 hover:shadow-md dark:border-white/10 dark:bg-white/5">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <p class="text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400">
              {{ $t('dashboard.totalUsers') }}
            </p>
            <p class="mt-2 text-3xl font-bold text-slate-900 dark:text-white">
              {{ metrics?.totalUsers ?? 0 }}
            </p>
          </div>
          <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-violet-500 to-violet-600 text-white shadow-lg shadow-violet-500/30">
            <UIcon name="i-heroicons-users" class="h-6 w-6" />
          </div>
        </div>
      </div>

      <!-- Active Users -->
      <div class="relative overflow-hidden rounded-2xl border border-slate-200/60 bg-white/80 backdrop-blur-sm p-6 shadow-sm transition-all duration-200 hover:shadow-md dark:border-white/10 dark:bg-white/5">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <p class="text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400">
              {{ $t('dashboard.activeUsers') }}
            </p>
            <p class="mt-2 text-3xl font-bold text-slate-900 dark:text-white">
              {{ metrics?.activeUsers ?? 0 }}
            </p>
          </div>
          <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-amber-500 to-amber-600 text-white shadow-lg shadow-amber-500/30">
            <UIcon name="i-heroicons-user-circle" class="h-6 w-6" />
          </div>
        </div>
      </div>
    </div>

    <!-- System Health and Recent Activity -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- System Health -->
      <div class="rounded-2xl border border-slate-200/60 bg-white/80 backdrop-blur-sm shadow-sm dark:border-white/10 dark:bg-white/5">
        <div class="border-b border-slate-200/60 px-6 py-4 dark:border-white/10">
          <h2 class="text-lg font-semibold text-slate-900 dark:text-white">
            {{ $t('dashboard.systemHealth') }}
          </h2>
        </div>
        <div class="p-6">
          <SystemHealthWidget
            :api-response-time="metrics?.apiResponseTime ?? 0"
            :database-status="metrics?.databaseStatus ?? 'healthy'"
            :loading="loading"
          />
        </div>
      </div>

      <!-- Recent Activity -->
      <div class="rounded-2xl border border-slate-200/60 bg-white/80 backdrop-blur-sm shadow-sm dark:border-white/10 dark:bg-white/5">
        <div class="border-b border-slate-200/60 px-6 py-4 dark:border-white/10">
          <h2 class="text-lg font-semibold text-slate-900 dark:text-white">
            {{ $t('dashboard.recentActivity') }}
          </h2>
        </div>
        <div class="p-6">
          <RecentActivityFeed
            :activities="metrics?.recentActivity ?? []"
            :loading="loading"
          />
        </div>
      </div>
    </div>

    <!-- Quick Actions Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
      <NuxtLink
        to="/tenants"
        class="group relative overflow-hidden rounded-2xl border border-slate-200/60 bg-white/80 backdrop-blur-sm p-6 shadow-sm transition-all duration-200 hover:shadow-lg hover:scale-[1.02] dark:border-white/10 dark:bg-white/5"
      >
        <div class="flex items-center gap-4">
          <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-blue-50 text-blue-600 transition-colors group-hover:bg-blue-100 dark:bg-blue-500/10 dark:text-blue-400">
            <UIcon name="i-heroicons-building-office-2" class="h-6 w-6" />
          </div>
          <div class="flex-1">
            <h3 class="font-semibold text-slate-900 dark:text-white">
              {{ $t('dashboard.viewTenants') }}
            </h3>
            <p class="text-sm text-slate-600 dark:text-slate-400">
              Manage all tenants
            </p>
          </div>
          <UIcon name="i-heroicons-arrow-right" class="h-5 w-5 text-slate-400 transition-transform group-hover:translate-x-1" />
        </div>
      </NuxtLink>

      <NuxtLink
        to="/analytics"
        class="group relative overflow-hidden rounded-2xl border border-slate-200/60 bg-white/80 backdrop-blur-sm p-6 shadow-sm transition-all duration-200 hover:shadow-lg hover:scale-[1.02] dark:border-white/10 dark:bg-white/5"
      >
        <div class="flex items-center gap-4">
          <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-violet-50 text-violet-600 transition-colors group-hover:bg-violet-100 dark:bg-violet-500/10 dark:text-violet-400">
            <UIcon name="i-heroicons-chart-bar" class="h-6 w-6" />
          </div>
          <div class="flex-1">
            <h3 class="font-semibold text-slate-900 dark:text-white">
              Analytics
            </h3>
            <p class="text-sm text-slate-600 dark:text-slate-400">
              View insights
            </p>
          </div>
          <UIcon name="i-heroicons-arrow-right" class="h-5 w-5 text-slate-400 transition-transform group-hover:translate-x-1" />
        </div>
      </NuxtLink>

      <NuxtLink
        to="/audit-logs"
        class="group relative overflow-hidden rounded-2xl border border-slate-200/60 bg-white/80 backdrop-blur-sm p-6 shadow-sm transition-all duration-200 hover:shadow-lg hover:scale-[1.02] dark:border-white/10 dark:bg-white/5"
      >
        <div class="flex items-center gap-4">
          <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600 transition-colors group-hover:bg-emerald-100 dark:bg-emerald-500/10 dark:text-emerald-400">
            <UIcon name="i-heroicons-document-text" class="h-6 w-6" />
          </div>
          <div class="flex-1">
            <h3 class="font-semibold text-slate-900 dark:text-white">
              {{ $t('dashboard.viewAuditLogs') }}
            </h3>
            <p class="text-sm text-slate-600 dark:text-slate-400">
              Track activities
            </p>
          </div>
          <UIcon name="i-heroicons-arrow-right" class="h-5 w-5 text-slate-400 transition-transform group-hover:translate-x-1" />
        </div>
      </NuxtLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SystemMetrics } from '~/types'

definePageMeta({
  title: 'Dashboard'
})

const { metrics, loading, refresh } = useSystemMetrics()
const { fetchSystemHealth } = useSystemHealth()
const { monitorSecurityEvents } = useSecurityMonitoring()

// Initial load
onMounted(() => {
  refresh()

  // Only run monitoring in production
  if (import.meta.env.PROD) {
    // Start health monitoring
    fetchSystemHealth()

    // Start security monitoring
    monitorSecurityEvents()

    // Set up periodic monitoring (every 5 minutes)
    const healthInterval = setInterval(() => {
      fetchSystemHealth()
    }, 5 * 60 * 1000)

    const securityInterval = setInterval(() => {
      monitorSecurityEvents()
    }, 5 * 60 * 1000)

    // Cleanup on unmount
    onUnmounted(() => {
      clearInterval(healthInterval)
      clearInterval(securityInterval)
    })
  }
})
</script>
