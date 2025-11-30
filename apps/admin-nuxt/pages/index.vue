<template>
  <div>
    <!-- Dashboard Actions Header (Desktop Only) -->
    <div class="hidden lg:block bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60 mb-6">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-mint-500 to-mint-400 shadow-lg">
              <UIcon name="i-lucide-layout-dashboard" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t("dashboard.hero.title") }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t("dashboard.hero.description") }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw"
              @click="refreshData"
            >
              {{ t('dashboard.actions.refresh') }}
            </UButton>
            <UButton 
              color="primary" 
              icon="i-lucide-calendar-plus"
              @click="navigateTo('/appointments/new')"
            >
              {{ t("dashboard.hero.actions.schedule") }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Mobile Actions Bar -->
    <div class="lg:hidden bg-white/95 backdrop-blur-sm border-b border-slate-200/60 dark:bg-slate-900/95 dark:border-slate-700/60 mb-4">
      <div class="px-4 py-3">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-lg font-bold text-slate-900 dark:text-white">{{ t("dashboard.hero.title") }}</h1>
          </div>
          <div class="flex items-center gap-2">
            <UButton 
              variant="ghost" 
              size="sm"
              icon="i-lucide-refresh-cw"
              :class="getTouchTargetClass()"
              @click="refreshData"
            />
            <UButton 
              color="primary" 
              size="sm"
              icon="i-lucide-plus"
              :class="getTouchTargetClass()"
              @click="navigateTo('/appointments/new')"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 pb-8">
      <!-- Quick Stats - Mobile Optimized -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 mb-6 sm:mb-8">
        <div class="bg-white dark:bg-slate-800 rounded-2xl p-4 sm:p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200 touch-manipulation">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-mint-50 dark:bg-mint-900/20">
              <UIcon name="i-lucide-calendar-check" class="h-5 w-5 text-mint-600 dark:text-mint-400" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide truncate">{{ t('dashboard.metrics.todayAppointments.label') }}</p>
              <p class="text-lg sm:text-xl font-semibold text-slate-900 dark:text-white">{{ summary.appointmentsToday }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300 line-clamp-2">{{ t('dashboard.metrics.todayAppointments.description') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-4 sm:p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200 touch-manipulation">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-50 dark:bg-emerald-900/20">
              <UIcon name="i-lucide-clock-3" class="h-5 w-5 text-emerald-600 dark:text-emerald-400" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide truncate">{{ t('dashboard.metrics.pendingConfirmations.label') }}</p>
              <p class="text-lg sm:text-xl font-semibold text-slate-900 dark:text-white">{{ summary.pendingConfirmations }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300 line-clamp-2">{{ t('dashboard.metrics.pendingConfirmations.description') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-4 sm:p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200 touch-manipulation">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
              <UIcon name="i-lucide-trending-up" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide truncate">{{ t('dashboard.metrics.monthlyRevenue.label') }}</p>
              <p class="text-lg sm:text-xl font-semibold text-slate-900 dark:text-white">{{ formatCurrency(summary.revenueMonthToDate, { notation: "compact" }) }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300 line-clamp-2">{{ t('dashboard.metrics.monthlyRevenue.description') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-4 sm:p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200 touch-manipulation">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-900/20">
              <UIcon name="i-lucide-users" class="h-5 w-5 text-amber-600 dark:text-amber-400" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide truncate">{{ t('dashboard.metrics.newPatients.label') }}</p>
              <p class="text-lg sm:text-xl font-semibold text-slate-900 dark:text-white">{{ summary.newPatients }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300 line-clamp-2">{{ t('dashboard.metrics.newPatients.description') }}</p>
        </div>
      </div>

      <!-- Load Factor Card - Mobile Optimized -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden mb-6 sm:mb-8">
        <div class="bg-gradient-to-r from-mint-500 to-mint-400 px-4 sm:px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-activity" class="h-5 w-5 text-white" />
            <div class="min-w-0 flex-1">
              <h2 class="text-lg font-semibold text-white truncate">{{ t("dashboard.hero.loadFactor.label") }}</h2>
              <p class="text-sm text-mint-100 hidden sm:block">{{ t('dashboard.loadFactor.subtitle') }}</p>
            </div>
          </div>
        </div>
        <div class="p-4 sm:p-6">
          <div class="flex items-center justify-between mb-4">
            <span class="text-sm font-medium text-slate-700 dark:text-slate-200">{{ t('dashboard.loadFactor.currentLabel') }}</span>
            <span class="text-2xl sm:text-3xl font-bold text-slate-900 dark:text-white">{{ loadFactor }}%</span>
          </div>
          <div class="h-3 w-full overflow-hidden rounded-full bg-slate-200/80 dark:bg-slate-700/60 mb-4">
            <div
              class="h-full rounded-full bg-gradient-to-r from-mint-500 via-mint-400 to-mint-300 transition-all duration-500"
              :style="{ width: `${loadFactor}%` }"
            ></div>
          </div>
          <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 text-sm">
            <div class="text-center p-2 rounded-lg bg-slate-50 dark:bg-slate-700/50">
              <p class="text-slate-500 dark:text-slate-400 text-xs truncate">{{ t("dashboard.hero.metrics.appointments.label") }}</p>
              <p class="font-semibold text-slate-900 dark:text-white text-lg">{{ summary.appointmentsToday }}</p>
            </div>
            <div class="text-center p-2 rounded-lg bg-slate-50 dark:bg-slate-700/50">
              <p class="text-slate-500 dark:text-slate-400 text-xs truncate">{{ t("dashboard.hero.metrics.pending.label") }}</p>
              <p class="font-semibold text-slate-900 dark:text-white text-lg">{{ summary.pendingConfirmations }}</p>
            </div>
            <div class="text-center p-2 rounded-lg bg-slate-50 dark:bg-slate-700/50">
              <p class="text-slate-500 dark:text-slate-400 text-xs truncate">{{ t("dashboard.hero.metrics.newPatients.label") }}</p>
              <p class="font-semibold text-slate-900 dark:text-white text-lg">{{ summary.newPatients }}</p>
            </div>
            <div class="text-center p-2 rounded-lg bg-slate-50 dark:bg-slate-700/50">
              <p class="text-slate-500 dark:text-slate-400 text-xs truncate">{{ t("dashboard.hero.metrics.revenue.label") }}</p>
              <p class="font-semibold text-slate-900 dark:text-white text-lg">{{ formatCurrency(summary.revenueMonthToDate, { notation: "compact" }) }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Schedule and Quick Actions Section - Mobile First -->
      <section class="grid gap-4 sm:gap-6 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
        <!-- Schedule Section - Mobile Optimized -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-mint-500 to-mint-400 px-4 sm:px-6 py-4">
            <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-calendar-days" class="h-5 w-5 text-white" />
                <div class="min-w-0 flex-1">
                  <h2 class="text-lg font-semibold text-white truncate">{{ t("dashboard.schedule.title") }}</h2>
                  <p class="text-sm text-mint-100 hidden sm:block">{{ t('dashboard.schedule.appointmentsScheduled', { count: upcoming.length }) }}</p>
                </div>
              </div>
              <UButton
                class="w-full sm:w-auto"
                variant="ghost"
                color="white"
                size="sm"
                :class="getTouchTargetClass()"
                @click="navigateTo('/appointments')"
              >
                {{ t("dashboard.schedule.viewAll") }}
              </UButton>
            </div>
          </div>
          <div class="p-4 sm:p-6">
            <div class="space-y-3">
              <div
                v-for="appointment in upcoming"
                :key="appointment.id"
              class="group relative overflow-hidden rounded-2xl border border-slate-200/70 bg-white/80 p-3 sm:p-4 transition-all duration-300 hover:border-mint-300 hover:bg-mint-50/60 dark:border-white/10 dark:bg-white/5 dark:hover:border-mint-400/60 dark:hover:bg-mint-500/10 touch-manipulation"
              >
                <div class="flex items-start justify-between gap-3">
                  <div class="flex-1 min-w-0 space-y-1">
                    <p class="text-sm font-semibold text-slate-900 truncate dark:text-white group-hover:text-mint-700 dark:group-hover:text-mint-200">
                      {{ appointment?.patientName || appointment?.patient?.name || '—' }}
                    </p>
                    <p class="text-xs text-slate-500 truncate dark:text-slate-400">
                      {{ appointment?.serviceName || appointment?.service?.name || '—' }}
                    </p>
                    <div class="flex flex-col sm:flex-row sm:flex-wrap sm:items-center gap-1 sm:gap-2 text-xs text-slate-500 dark:text-slate-400">
                      <span class="inline-flex items-center gap-1">
                        <UIcon name="i-lucide-stethoscope" class="h-3.5 w-3.5 text-mint-500 dark:text-mint-300" />
                        <span class="truncate">{{ appointment?.doctorName || appointment?.doctor?.name || '—' }}</span>
                      </span>
                      <span class="hidden sm:inline-flex h-1.5 w-1.5 rounded-full bg-slate-400/60"></span>
                      <span class="inline-flex items-center gap-1">
                        <UIcon name="i-lucide-map-pin" class="h-3.5 w-3.5 text-slate-400" />
                        {{ t("dashboard.schedule.operatory", { room: getOperatoryCode(appointment?.id) }) }}
                      </span>
                    </div>
                  </div>
                  <div class="flex flex-col items-end gap-1 flex-shrink-0">
                    <UBadge :color="statusColor(appointment?.status)" variant="soft" size="xs">
                      {{ formatStatus(appointment?.status) }}
                    </UBadge>
                    <div class="text-right text-xs text-slate-500 dark:text-slate-400">
                      <p class="font-semibold text-slate-900 dark:text-white">{{ formatTime(appointment?.scheduledAt || appointment?.scheduled_at) }}</p>
                      <p class="hidden sm:block">{{ formatDate(appointment?.scheduledAt || appointment?.scheduled_at) }}</p>
                    </div>
                  </div>
                </div>
                <div class="absolute inset-x-0 bottom-0 h-0.5 bg-gradient-to-r from-mint-500 via-mint-400 to-mint-300 opacity-0 transition group-hover:opacity-100"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Quick Actions and Team Section - Mobile Optimized -->
        <div class="flex flex-col gap-4 sm:gap-6">
          <!-- Quick Actions -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-4 sm:px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-zap" class="h-5 w-5 text-white" />
                <div class="min-w-0 flex-1">
                  <h2 class="text-lg font-semibold text-white truncate">{{ t("dashboard.quickActions.title") }}</h2>
                  <p class="text-sm text-slate-300 hidden sm:block">{{ t("dashboard.quickActions.subtitle") }}</p>
                </div>
              </div>
            </div>
            <div class="p-4 sm:p-6">
              <div class="grid gap-3 grid-cols-1 sm:grid-cols-2">
                <button
                  v-for="action in quickActionCards"
                  :key="action.title"
                  type="button"
                  class="group flex w-full flex-col gap-3 rounded-2xl border border-slate-200/70 bg-white/80 p-4 text-left transition-all duration-300 hover:-translate-y-0.5 hover:border-mint-300 hover:bg-mint-50/70 dark:border-white/10 dark:bg-white/5 dark:hover:border-mint-400/60 dark:hover:bg-mint-500/10 touch-manipulation"
                  :class="getTouchTargetClass()"
                  @click="action.click()"
                >
                  <span :class="['inline-flex h-10 w-10 items-center justify-center rounded-xl text-white shadow-sm shadow-mint-500/30', action.accent]">
                    <UIcon :name="action.icon" class="h-5 w-5" />
                  </span>
                  <div class="space-y-1 min-w-0">
                    <p class="text-sm font-semibold text-slate-900 dark:text-white truncate">{{ action.title }}</p>
                    <p class="text-xs text-slate-500 dark:text-slate-400 line-clamp-2">{{ action.description }}</p>
                  </div>
                  <span class="text-xs font-semibold text-mint-600 transition group-hover:text-mint-700 dark:text-mint-300 dark:group-hover:text-mint-200">
                    {{ action.cta }}
                  </span>
                </button>
              </div>
            </div>
          </div>

          <!-- Team Section -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-emerald-500 to-teal-600 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-users" class="h-5 w-5 text-white" />
                <div>
                  <h2 class="text-lg font-semibold text-white">{{ t("dashboard.team.title") }}</h2>
                  <p class="text-sm text-emerald-100">{{ t("dashboard.team.subtitle") }}</p>
                </div>
              </div>
            </div>
            <div class="p-6">
              <div class="space-y-3">
                <div
                  v-for="doctor in doctors"
                  :key="doctor.name"
                  class="group flex items-center gap-3 rounded-2xl border border-slate-200/70 bg-white/80 p-3 transition-all duration-300 hover:border-emerald-300 hover:bg-emerald-50/60 dark:border-white/10 dark:bg-white/5 dark:hover:border-emerald-400/70 dark:hover:bg-emerald-500/10"
                >
                  <UAvatar :alt="doctor.name" size="md" class="ring-2 ring-emerald-200/80 group-hover:ring-emerald-300 transition" />
                  <div class="flex-1 min-w-0">
                    <p class="truncate text-sm font-semibold text-slate-900 dark:text-white group-hover:text-emerald-700 dark:group-hover:text-emerald-200">
                      {{ doctor.name }}
                    </p>
                    <p class="truncate text-xs text-slate-500 dark:text-slate-400">{{ doctor.specialty }}</p>
                    <div class="mt-2 inline-flex items-center gap-1 rounded-full border border-emerald-200/80 bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-600 dark:border-emerald-400/40 dark:bg-emerald-500/10 dark:text-emerald-300">
                      <span class="flex h-1.5 w-1.5 rounded-full bg-emerald-500"></span>
                      {{ doctor.status }}
                    </div>
                  </div>
                  <div class="text-xs text-slate-500 dark:text-slate-400">
                    {{ t("dashboard.team.nextSlot") }}
                    <p class="font-semibold text-slate-900 dark:text-white">{{ doctor.nextSlot }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Communication, Revenue, and Operations Section - Mobile First -->
      <section class="grid gap-4 sm:gap-6 lg:grid-cols-3 mt-6 sm:mt-8">
        <!-- Communication Section -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-waveform" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">{{ t("dashboard.communication.title") }}</h2>
                <p class="text-sm text-blue-100">{{ t("dashboard.communication.subtitle") }}</p>
              </div>
            </div>
          </div>
          <div class="p-6">
            <div class="grid gap-3">
              <div
                v-for="channel in channelPerformance"
                :key="channel.id"
                class="group relative overflow-hidden rounded-2xl border border-slate-200/70 bg-white/80 p-4 transition-all duration-300 hover:border-blue-300 hover:bg-blue-50/60 dark:border-white/10 dark:bg-white/5 dark:hover:border-blue-400/60 dark:hover:bg-blue-500/10"
              >
                <div class="flex items-start justify-between gap-3">
                  <div class="space-y-1">
                    <div class="inline-flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                      <span :class="['flex h-7 w-7 items-center justify-center rounded-xl', channel.badgeBg]">
                        <UIcon :name="channel.icon" class="h-4 w-4" />
                      </span>
                      {{ channel.label }}
                    </div>
                    <p class="text-2xl font-semibold text-slate-900 dark:text-white">{{ channel.rate }}</p>
                    <p class="text-xs text-slate-500 dark:text-slate-400">{{ channel.description }}</p>
                  </div>
                  <div class="text-right text-xs">
                    <p :class="['inline-flex items-center gap-1 rounded-full px-2 py-1 font-semibold', channel.deltaBadge]">
                      <UIcon :name="channel.trendIcon" class="h-3.5 w-3.5" />
                      {{ channel.delta }}
                    </p>
                  </div>
                </div>
                <div class="mt-4 h-1.5 w-full overflow-hidden rounded-full bg-slate-200/80 dark:bg-slate-700/60">
                  <div
                    class="h-full rounded-full bg-gradient-to-r"
                    :class="channel.progressGradient"
                    :style="{ width: channel.rate }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Revenue Section -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-amber-500 to-orange-600 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-piggy-bank" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">{{ t("dashboard.revenue.title") }}</h2>
                <p class="text-sm text-amber-100">{{ t("dashboard.revenue.subtitle") }}</p>
              </div>
            </div>
          </div>
          <div class="p-6">
            <div class="space-y-5">
              <div>
                <div class="flex items-baseline justify-between">
                  <p class="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">{{ t("dashboard.revenue.monthToDate") }}</p>
                  <span class="rounded-full bg-emerald-100/80 px-2 py-0.5 text-[11px] font-semibold text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-300">
                    {{ t("dashboard.revenue.monthDelta") }}
                  </span>
                </div>
                <p class="mt-2 text-3xl font-semibold text-slate-900 dark:text-white">{{ formatCurrency(summary.revenueMonthToDate) }}</p>
                <p class="text-xs text-slate-500 dark:text-slate-400">{{ t("dashboard.revenue.target") }}: {{ formatCurrency(targetRevenue) }}</p>
                <div class="mt-4 h-2 w-full overflow-hidden rounded-full bg-slate-200/80 dark:bg-slate-700/60">
                  <div
                    class="h-full rounded-full bg-gradient-to-r from-amber-400 via-amber-500 to-amber-600"
                    :style="{ width: revenueProgress + '%' }"
                  ></div>
                </div>
              </div>

              <div class="grid gap-4 sm:grid-cols-2">
                <div class="rounded-2xl border border-slate-200/70 bg-white/80 p-4 dark:border-white/10 dark:bg-white/5">
                  <p class="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">{{ t("dashboard.revenue.collectionsToday") }}</p>
                  <p class="mt-2 text-lg font-semibold text-slate-900 dark:text-white">{{ formatCurrency(Math.round(summary.revenueMonthToDate / 12)) }}</p>
                  <p class="text-xs text-emerald-600 dark:text-emerald-300">{{ t("dashboard.revenue.collectionsDelta") }}</p>
                </div>
                <div class="rounded-2xl border border-slate-200/70 bg-white/80 p-4 dark:border-white/10 dark:bg-white/5">
                  <p class="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">{{ t("dashboard.revenue.averageTicket") }}</p>
                  <p class="mt-2 text-lg font-semibold text-slate-900 dark:text-white">{{ formatCurrency(820) }}</p>
                  <p class="text-xs text-slate-500 dark:text-slate-400">{{ t("dashboard.revenue.averageTicketSubtitle") }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Operations Section -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-mint-500 to-mint-400 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-check-square" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">{{ t("dashboard.operations.title") }}</h2>
                <p class="text-sm text-mint-100">{{ t("dashboard.operations.subtitle") }}</p>
              </div>
            </div>
          </div>
          <div class="p-6">
            <div class="space-y-4">
              <div
                v-for="item in operationsProgress"
                :key="item.id"
                class="rounded-2xl border border-slate-200/70 bg-white/80 p-4 dark:border-white/10 dark:bg-white/5"
              >
                <div class="flex items-center justify-between text-xs">
                  <span class="font-semibold text-slate-700 dark:text-slate-200">{{ item.label }}</span>
                  <div class="flex items-center gap-1.5">
                    <span :class="['rounded-full px-2 py-0.5 font-semibold', item.badge]">{{ item.percent }}%</span>
                    <span class="font-semibold text-slate-900 dark:text-white">{{ item.completed }}/{{ item.total }}</span>
                  </div>
                </div>
                <div class="mt-2 h-2 w-full overflow-hidden rounded-full bg-slate-200/80 dark:bg-slate-700/60">
                  <div
                    class="h-full rounded-full"
                    :class="item.progressGradient"
                    :style="{ width: item.percent + '%' }"
                  ></div>
                </div>
                <p class="mt-2 text-xs text-slate-500 dark:text-slate-400">{{ item.caption }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { dashboardSummaryMock, upcomingAppointmentsMock } from "@/data/mock";
import { computed } from "vue";

// Mobile responsiveness utilities
const { viewport, getTouchTargetSize } = useViewport();

// Touch target utility
const getTouchTargetClass = () => {
  const size = getTouchTargetSize();
  return `min-w-[${size}px] min-h-[${size}px]`;
};

const { fetcher } = useAdminApi();
const { formatCurrency } = useClinicSettings();
const { t, locale } = useI18n();
const router = useRouter();
const navigateTo = (path: string) => router.push(path);

const refreshData = async () => {
  await refreshCookie("dashboard-summary");
  await refreshCookie("upcoming-appointments");
};

useHead({
  title: "Dashboard – Clinic Admin"
});

const { data: summaryData } = await useAsyncData("dashboard-summary", () =>
  fetcher("/dashboard/summary", dashboardSummaryMock)
);

const summaryFallback = {
  appointmentsToday: 0,
  pendingConfirmations: 0,
  revenueMonthToDate: 0,
  newPatients: 0
};

const summary = computed(() => ({
  ...summaryFallback,
  ...((summaryData.value ?? dashboardSummaryMock) || {})
}));

const targetRevenue = 250000;

const statHighlights = computed(() => {
  const data = summary.value;
  return [
    {
      id: "appointments",
      label: t("dashboard.hero.metrics.appointments.label"),
      description: t("dashboard.hero.metrics.appointments.description"),
      value: data.appointmentsToday ?? 0,
      display: `${data.appointmentsToday ?? 0}`,
      icon: "i-lucide-calendar-check",
      trendLabel: "+8%",
      trendTone: "text-emerald-200",
      trendIcon: "i-lucide-trending-up"
    },
    {
      id: "pending",
      label: t("dashboard.hero.metrics.pending.label"),
      description: t("dashboard.hero.metrics.pending.description"),
      value: data.pendingConfirmations ?? 0,
      display: `${data.pendingConfirmations ?? 0}`,
      icon: "i-lucide-clock-3",
      trendLabel: "+2",
      trendTone: "text-amber-200",
      trendIcon: "i-lucide-alert-circle"
    },
    {
      id: "revenue",
      label: t("dashboard.hero.metrics.revenue.label"),
      description: t("dashboard.hero.metrics.revenue.description"),
      value: data.revenueMonthToDate ?? 0,
      display: formatCurrency(data.revenueMonthToDate ?? 0, { notation: "compact" }),
      icon: "i-lucide-trending-up",
      trendLabel: "+12%",
      trendTone: "text-emerald-200",
      trendIcon: "i-lucide-arrow-up-right"
    },
    {
      id: "newPatients",
      label: t("dashboard.hero.metrics.newPatients.label"),
      description: t("dashboard.hero.metrics.newPatients.description"),
      value: data.newPatients ?? 0,
      display: `${data.newPatients ?? 0}`,
      icon: "i-lucide-users",
      trendLabel: "+3",
      trendTone: "text-sky-200",
      trendIcon: "i-lucide-user-plus"
    }
  ];
});

const loadFactor = computed(() => {
  const scheduled = summary.value.appointmentsToday ?? 0;
  const pending = summary.value.pendingConfirmations ?? 0;
  const baselineCapacity = 32;
  const ratio = baselineCapacity > 0 ? Math.min(1, (scheduled + pending) / baselineCapacity) : 0;
  return Math.round(ratio * 100);
});

const revenueProgress = computed(() => {
  const revenue = summary.value.revenueMonthToDate ?? 0;
  if (targetRevenue <= 0) {
    return 0;
  }
  return Math.min(100, Math.round((revenue / targetRevenue) * 100));
});

const quickActionCards = computed(() => [
  {
    title: t("dashboard.quickActions.actions.bookAppointment.title"),
    description: t("dashboard.quickActions.actions.bookAppointment.description"),
    icon: "i-lucide-calendar-plus",
    accent: "bg-gradient-to-br from-mint-600 to-mint-400",
    cta: t("dashboard.quickActions.actions.bookAppointment.cta"),
    click: () => navigateTo("/appointments/new")
  },
  {
    title: t("dashboard.quickActions.actions.registerPatient.title"),
    description: t("dashboard.quickActions.actions.registerPatient.description"),
    icon: "i-lucide-user-plus",
    accent: "bg-gradient-to-br from-emerald-500 to-teal-500",
    cta: t("dashboard.quickActions.actions.registerPatient.cta"),
    click: () => navigateTo("/patients/new")
  },
  {
    title: t("dashboard.quickActions.actions.sendReminders.title"),
    description: t("dashboard.quickActions.actions.sendReminders.description"),
    icon: "i-lucide-message-circle",
    accent: "bg-gradient-to-br from-sky-500 to-blue-500",
    cta: t("dashboard.quickActions.actions.sendReminders.cta"),
    click: () => navigateTo("/appointments")
  },
  {
    title: t("dashboard.quickActions.actions.reviewCollections.title"),
    description: t("dashboard.quickActions.actions.reviewCollections.description"),
    icon: "i-lucide-credit-card",
    accent: "bg-gradient-to-br from-amber-500 to-amber-600",
    cta: t("dashboard.quickActions.actions.reviewCollections.cta"),
    click: () => navigateTo("/reports")
  }
]);

const channelPerformance = computed(() => [
  {
    id: "whatsapp",
    label: t("dashboard.communication.channels.whatsapp.label"),
    icon: "i-lucide-message-circle",
    badgeBg: "bg-emerald-500/15 text-emerald-300",
    rate: "92%",
    delta: `+6% ${t("dashboard.communication.channels.whatsapp.delta")}`,
    deltaBadge: "bg-emerald-100/80 text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-300",
    trendIcon: "i-lucide-trending-up",
    description: t("dashboard.communication.channels.whatsapp.description"),
    progressGradient: "from-emerald-400 via-emerald-500 to-emerald-400"
  },
  {
    id: "email",
    label: t("dashboard.communication.channels.email.label"),
    icon: "i-lucide-mail",
    badgeBg: "bg-mint-500/15 text-mint-300",
    rate: "81%",
    delta: `+3% ${t("dashboard.communication.channels.email.delta")}`,
    deltaBadge: "bg-mint-100/80 text-mint-600 dark:bg-mint-500/15 dark:text-mint-200",
    trendIcon: "i-lucide-trending-up",
    description: t("dashboard.communication.channels.email.description"),
    progressGradient: "from-mint-500 via-mint-400 to-mint-300"
  },
  {
    id: "sms",
    label: t("dashboard.communication.channels.sms.label"),
    icon: "i-lucide-smartphone",
    badgeBg: "bg-amber-500/15 text-amber-300",
    rate: "74%",
    delta: `-4% ${t("dashboard.communication.channels.sms.delta")}`,
    deltaBadge: "bg-rose-100/80 text-rose-600 dark:bg-rose-500/15 dark:text-rose-300",
    trendIcon: "i-lucide-trending-down",
    description: t("dashboard.communication.channels.sms.description"),
    progressGradient: "from-amber-400 via-orange-500 to-amber-400"
  }
]);

const operationsProgress = computed(() => [
  {
    id: "billing",
    label: t("dashboard.operations.items.invoices.label"),
    percent: 40,
    completed: 2,
    total: 5,
    badge: "bg-emerald-100/80 text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-300",
    progressGradient: "bg-gradient-to-r from-emerald-400 via-emerald-500 to-emerald-600",
    caption: t("dashboard.operations.items.invoices.caption")
  },
  {
    id: "treatments",
    label: t("dashboard.operations.items.treatments.label"),
    percent: 70,
    completed: 7,
    total: 10,
    badge: "bg-blue-100/80 text-blue-600 dark:bg-blue-500/15 dark:text-blue-200",
    progressGradient: "bg-gradient-to-r from-blue-400 via-blue-500 to-indigo-500",
    caption: t("dashboard.operations.items.treatments.caption")
  },
  {
    id: "labs",
    label: t("dashboard.operations.items.labs.label"),
    percent: 54,
    completed: 13,
    total: 24,
    badge: "bg-mint-100/80 text-mint-600 dark:bg-mint-500/15 dark:text-mint-200",
    progressGradient: "bg-gradient-to-r from-mint-500 via-mint-400 to-mint-300",
    caption: t("dashboard.operations.items.labs.caption")
  }
]);

const { data: upcomingData } = await useAsyncData("upcoming-appointments", () =>
  fetcher("/appointments?filter=upcoming", upcomingAppointmentsMock)
);
const upcoming = computed(() => {
  const data = upcomingData.value ?? upcomingAppointmentsMock;

  let appointments = [];

  if (Array.isArray(data)) {
    appointments = data;
  } else if (data && typeof data === "object" && "items" in data) {
    appointments = data.items as typeof upcomingAppointmentsMock;
  } else if (data && typeof data === "object" && "content" in data) {
    appointments = data.content as typeof upcomingAppointmentsMock;
  } else {
    appointments = upcomingAppointmentsMock;
  }

  return appointments.slice(0, 5);
});

// Fetch team on call from API
const { data: teamOnCallData } = await useAsyncData("team-on-call", () =>
  fetcher("/dashboard/team-on-call", [])
);

const doctors = computed(() => {
  const team = teamOnCallData.value ?? [];

  // Transform API response to match expected format
  return team.map((member: any) => ({
    id: member.id,
    name: member.name,
    specialty: member.specialty || "General Practice",
    status: member.status,
    nextSlot: member.nextSlot ? formatTime(member.nextSlot) : "—"
  }));
});

// Use clinic timezone for all date/time displays
// CRITICAL: All admins see times in clinic timezone, not their browser timezone
const { timezone, abbreviation } = useClinicTimezone();

const formatDate = (date: string | number | null | undefined) => {
  if (!date) return "—";
  // Use clinic timezone formatter from dateUtils
  return formatDateInClinicTimezone(date, timezone.value, {
    month: "short",
    day: "numeric"
  });
};

const formatTime = (date: string | number | null | undefined) => {
  if (!date) return "—";
  // Use clinic timezone formatter with abbreviation (e.g., "2:00 PM EET")
  return formatTimeInClinicTimezone(date, timezone.value, abbreviation.value);
};

function formatStatus(status: string | undefined) {
  if (!status) return "—";
  const key = status.toLowerCase();
  const translationKey = `dashboard.schedule.status.${key}`;
  const translated = t(translationKey);
  return translated === translationKey ? key.replace(/_/g, " ") : translated;
}

function getOperatoryCode(id: unknown) {
  if (typeof id === "string" || typeof id === "number") {
    const value = String(id);
    return value.slice(-2).toUpperCase() || "—";
  }
  return "—";
}

function statusColor(status: string | undefined) {
  if (!status) return "gray";

  switch (status) {
    case "CONFIRMED":
      return "emerald";
    case "COMPLETED":
      return "blue";
    case "CANCELLED":
      return "red";
    case "SCHEDULED":
    default:
      return "mint";
  }
}

const todayDate = computed(() =>
  new Intl.DateTimeFormat(locale.value, {
    weekday: "long",
    month: "long",
    day: "numeric"
  }).format(new Date())
);
</script>
