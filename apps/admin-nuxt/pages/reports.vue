<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-bar-chart-3" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('reports.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('reports.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <!-- Date Range Filter -->
            <DateRangeFilter 
              v-model="dateRange" 
              @change="handleDateRangeChange"
              :disabled="loading"
            />
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="loading"
              @click="refreshData"
            >
              {{ t('reports.header.actions.refresh') }}
            </UButton>
            <UButton 
              color="blue" 
              icon="i-lucide-download" 
              @click="exportData"
            >
              {{ t('reports.header.actions.export') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">

      <!-- Loading State -->
      <div v-if="loading" class="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        <USkeleton v-for="i in 8" :key="i" class="h-32 rounded-2xl" />
      </div>

      <!-- Error State -->
      <UCard v-else-if="error" class="bg-red-50">
        <div class="flex items-center gap-3 text-red-600">
          <UIcon name="i-lucide-alert-circle" class="h-6 w-6" />
          <div>
            <p class="font-semibold">{{ t('reports.messages.loadError') }}</p>
            <p class="text-sm">{{ error }}</p>
          </div>
        </div>
      </UCard>

      <!-- Main Content -->
      <div v-else-if="metrics" class="space-y-8">
      <!-- Key Metrics -->
      <div class="grid gap-6 md:grid-cols-2 xl:grid-cols-4">
        <UCard class="transition-shadow shadow-sm hover:shadow-lg">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-sm text-slate-500">{{ t('reports.metrics.totalAppointments.label') }}</p>
              <p class="mt-2 text-3xl font-bold text-slate-900">{{ metrics.totalAppointments }}</p>
              <p class="mt-2 text-xs text-emerald-600">
                <span class="font-semibold">+{{ metrics.thisMonthAppointments }}</span> {{ t('reports.metrics.totalAppointments.thisMonth') }}
              </p>
            </div>
            <div class="rounded-lg bg-violet-100 p-3 text-violet-600">
              <UIcon name="i-lucide-calendar" class="h-6 w-6" />
            </div>
          </div>
        </UCard>

        <UCard class="transition-shadow shadow-sm hover:shadow-lg">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-sm text-slate-500">{{ t('reports.metrics.totalPatients.label') }}</p>
              <p class="mt-2 text-3xl font-bold text-slate-900">{{ metrics.totalPatients }}</p>
              <p class="mt-2 text-xs text-emerald-600">
                <span class="font-semibold">+{{ metrics.newPatientsThisMonth }}</span> {{ t('reports.metrics.totalPatients.newThisMonth') }}
              </p>
            </div>
            <div class="rounded-lg bg-emerald-100 p-3 text-emerald-600">
              <UIcon name="i-lucide-users" class="h-6 w-6" />
            </div>
          </div>
        </UCard>

        <UCard class="transition-shadow shadow-sm hover:shadow-lg">
          <div class="flex items-start justify-between">
            <div class="w-full">
              <p class="text-sm text-slate-500 dark:text-slate-400">{{ t('reports.metrics.revenueThisMonth.label') }}</p>

              <!-- Multi-currency revenue display -->
              <div v-if="metrics.revenueThisMonthByCurrency && Object.keys(metrics.revenueThisMonthByCurrency).length > 0" class="mt-2 space-y-1">
                <div v-for="(amount, currency) in metrics.revenueThisMonthByCurrency" :key="currency" class="flex items-baseline gap-2">
                  <p class="text-2xl font-bold text-slate-900 dark:text-white">{{ formatCurrencyWithCode(amount, currency) }}</p>
                  <span class="text-xs text-slate-500 dark:text-slate-400 font-medium">{{ currency }}</span>
                </div>
              </div>
              <!-- Fallback to single currency (backwards compatibility) -->
              <p v-else class="mt-2 text-3xl font-bold text-slate-900 dark:text-white">{{ formatCurrency(metrics.revenueThisMonth) }}</p>

              <p class="mt-2 flex items-center gap-1 text-xs font-semibold" :class="revenueMomentum.positive ? 'text-emerald-600' : 'text-red-600'">
                <UIcon :name="revenueMomentum.positive ? 'i-lucide-trending-up' : 'i-lucide-trending-down'" class="h-3.5 w-3.5" />
                {{ revenueMomentum.percent >= 0 ? '+' : '-' }}{{ formatPercent(Math.abs(revenueMomentum.percent)) }} {{ t('reports.metrics.revenueThisMonth.vsLastMonth') }}
              </p>
            </div>
            <div class="rounded-lg bg-emerald-100 p-3 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400">
              <UIcon name="i-lucide-wallet" class="h-6 w-6" />
            </div>
          </div>
        </UCard>

        <UCard class="transition-shadow shadow-sm hover:shadow-lg">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-sm text-slate-500">{{ t('reports.metrics.collectionHealth.label') }}</p>
              <p class="mt-2 text-3xl font-bold text-slate-900">{{ formatPercent(metrics.collectionRate) }}</p>
              <p class="mt-2 text-xs text-amber-600">
                <span class="font-semibold">{{ metrics.paymentsOutstandingCount }}</span> {{ t('reports.metrics.collectionHealth.outstanding') }}
              </p>
            </div>
            <div class="rounded-lg bg-amber-100 p-3 text-amber-600">
              <UIcon name="i-lucide-piggy-bank" class="h-6 w-6" />
            </div>
          </div>
        </UCard>
      </div>

      <!-- Snapshot -->
      <div class="grid gap-6 xl:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
        <UCard class="shadow-sm">
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-pie-chart" class="h-5 w-5 text-violet-600" />
              <h3 class="font-semibold">{{ t('reports.snapshots.period.title') }}</h3>
            </div>
          </template>
          <div class="grid gap-4 sm:grid-cols-3">
            <div class="rounded-xl border border-violet-100 bg-violet-50/70 p-4 text-center">
              <p class="text-xs font-semibold uppercase tracking-wide text-violet-500">{{ t('reports.snapshots.period.today') }}</p>
              <p class="mt-2 text-3xl font-bold text-violet-700">{{ metrics.todayAppointments }}</p>
              <p class="mt-1 text-xs text-violet-500">{{ t('reports.snapshots.period.appointments') }}</p>
            </div>
            <div class="rounded-xl border border-emerald-100 bg-emerald-50/70 p-4 text-center">
              <p class="text-xs font-semibold uppercase tracking-wide text-emerald-500">{{ t('reports.snapshots.period.thisWeek') }}</p>
              <p class="mt-2 text-3xl font-bold text-emerald-700">{{ metrics.thisWeekAppointments }}</p>
              <p class="mt-1 text-xs text-emerald-500">{{ t('reports.snapshots.period.appointments') }}</p>
            </div>
            <div class="rounded-xl border border-sky-100 bg-sky-50/70 p-4 text-center">
              <p class="text-xs font-semibold uppercase tracking-wide text-sky-500">{{ t('reports.snapshots.period.thisMonth') }}</p>
              <p class="mt-2 text-3xl font-bold text-sky-700">{{ metrics.thisMonthAppointments }}</p>
              <p class="mt-1 text-xs text-sky-500">{{ t('reports.snapshots.period.appointments') }}</p>
            </div>
          </div>
        </UCard>

        <UCard class="shadow-sm">
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-gem" class="h-5 w-5 text-slate-600" />
              <h3 class="font-semibold">{{ t('reports.snapshots.operational.title') }}</h3>
            </div>
          </template>
          <div class="grid gap-3">
            <div
              v-for="kpi in operationalKpis"
              :key="kpi.label"
              class="flex items-center justify-between rounded-xl border border-slate-200/60 bg-white p-4 shadow-sm"
            >
              <div class="flex items-center gap-3">
                <div :class="['flex h-10 w-10 items-center justify-center rounded-xl', kpi.tone, kpi.text]">
                  <UIcon :name="kpi.icon" class="h-5 w-5" />
                </div>
                <div>
                  <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ kpi.label }}</p>
                  <p class="text-lg font-semibold text-slate-900">{{ kpi.value }}</p>
                </div>
              </div>
              <p class="text-xs text-slate-500">{{ kpi.sublabel }}</p>
            </div>
          </div>
        </UCard>
      </div>

      <!-- Trends -->
      <div class="grid gap-6 xl:grid-cols-2">
        <UCard class="shadow-sm">
          <template #header>
            <div class="flex items-center justify-between">
              <h3 class="font-semibold">{{ t('reports.charts.appointmentTrend.title') }}</h3>
              <UBadge color="violet" variant="soft" size="xs">{{ t('reports.charts.appointmentTrend.badge') }}</UBadge>
            </div>
          </template>
          <div class="h-72">
            <Line v-if="hasAppointmentTrend" :data="appointmentTrendChart" :options="lineChartOptions" />
            <div v-else class="flex h-full items-center justify-center text-sm text-slate-400">
              {{ t('reports.charts.appointmentTrend.noData') }}
            </div>
          </div>
        </UCard>

        <UCard class="shadow-sm">
          <template #header>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <h3 class="font-semibold">{{ t('reports.charts.revenueTrend.title') }}</h3>
                <UBadge
                  v-if="metrics?.revenueTrendByCurrency && Object.keys(metrics.revenueTrendByCurrency).length > 1"
                  color="blue"
                  variant="soft"
                  size="xs"
                >
                  Multi-Currency
                </UBadge>
              </div>
              <UBadge color="emerald" variant="soft" size="xs">{{ t('reports.charts.revenueTrend.badge') }}</UBadge>
            </div>
          </template>
          <div class="h-72">
            <Line v-if="hasRevenueTrend" :data="revenueChartData" :options="lineChartOptions" />
            <div v-else class="flex h-full items-center justify-center text-sm text-slate-400 dark:text-slate-500">
              {{ t('reports.charts.revenueTrend.noData') }}
            </div>
          </div>
        </UCard>
      </div>

      <!-- Breakdown -->
      <div class="grid gap-6 xl:grid-cols-3">
        <UCard class="shadow-sm">
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-stethoscope" class="h-5 w-5 text-violet-600" />
              <h3 class="font-semibold">{{ t('reports.charts.doctorPerformance.title') }}</h3>
            </div>
          </template>
          <div class="h-72">
            <Bar v-if="hasDoctorBreakdown" :data="doctorChartData" :options="doctorChartOptions" />
            <div v-else class="flex h-full items-center justify-center text-sm text-slate-400">
              {{ t('reports.charts.doctorPerformance.noData') }}
            </div>
          </div>
        </UCard>

        <UCard class="shadow-sm">
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-layers" class="h-5 w-5 text-emerald-600" />
              <h3 class="font-semibold">{{ t('reports.charts.serviceMix.title') }}</h3>
            </div>
          </template>
          <div class="h-72">
            <Doughnut v-if="hasServiceBreakdown" :data="serviceChartData" :options="doughnutOptions" />
            <div v-else class="flex h-full items-center justify-center text-sm text-slate-400">
              {{ t('reports.charts.serviceMix.noData') }}
            </div>
          </div>
        </UCard>

        <UCard class="shadow-sm">
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-credit-card" class="h-5 w-5 text-amber-600" />
              <h3 class="font-semibold">{{ t('reports.charts.paymentMethods.title') }}</h3>
            </div>
          </template>
          <div class="h-72">
            <Doughnut v-if="hasPaymentBreakdown" :data="paymentChartData" :options="doughnutOptions" />
            <div v-else class="flex h-full items-center justify-center text-sm text-slate-400">
              {{ t('reports.charts.paymentMethods.noData') }}
            </div>
          </div>
        </UCard>
      </div>

      <!-- Status Breakdown -->
      <UCard class="shadow-sm">
        <template #header>
          <div class="flex items-center gap-2">
            <UIcon name="i-lucide-gauge" class="h-5 w-5 text-violet-600" />
            <h3 class="font-semibold">{{ t('reports.charts.statusBreakdown.title') }}</h3>
          </div>
        </template>
        <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div
            v-for="status in statusCards"
            :key="status.key"
            class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm"
            :class="status.tone"
          >
            <div class="flex items-center gap-3">
              <div :class="['flex h-10 w-10 items-center justify-center rounded-xl bg-white/80', status.text]">
                <UIcon :name="status.icon" class="h-5 w-5" />
              </div>
              <div>
                <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ status.label }}</p>
                <p class="text-2xl font-bold text-slate-900">{{ status.count }}</p>
              </div>
            </div>
            <p class="mt-3 text-xs font-medium text-slate-600">
              {{ status.percentage }}{{ t('reports.charts.statusBreakdown.ofTotal') }}
            </p>
          </div>
        </div>
      </UCard>

      <!-- PayPal Transactions -->
      <UCard class="shadow-sm">
        <template #header>
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-credit-card" class="h-5 w-5 text-blue-600" />
              <h3 class="font-semibold">{{ t('reports.paypal.title') }}</h3>
            </div>
            <UButton 
              color="blue" 
              variant="soft" 
              icon="i-lucide-external-link"
              @click="navigateToAllTransactions"
            >
              {{ t('reports.paypal.viewAll') }}
            </UButton>
          </div>
        </template>
        
        <div v-if="paypalLoading" class="p-6">
          <div class="space-y-4">
            <div v-for="i in 3" :key="i" class="animate-pulse">
              <div class="bg-slate-200 dark:bg-slate-700 rounded-xl p-4 space-y-3">
                <div class="flex items-center gap-4">
                  <div class="w-10 h-10 bg-slate-300 dark:bg-slate-600 rounded-full"></div>
                  <div class="flex-1 space-y-2">
                    <div class="h-4 bg-slate-300 dark:bg-slate-600 rounded w-3/4"></div>
                    <div class="h-3 bg-slate-300 dark:bg-slate-600 rounded w-1/2"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="correctedPaypalData" class="p-6">
          <!-- Currency Notice -->
          <div class="mb-4 flex items-center justify-between rounded-lg border border-blue-200 bg-blue-50 px-4 py-2 dark:border-blue-800/50 dark:bg-blue-900/20">
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-info" class="h-4 w-4 text-blue-600 dark:text-blue-400" />
              <p class="text-sm font-medium text-blue-800 dark:text-blue-300">
                All PayPal transactions are in <span class="font-bold">USD</span>
              </p>
            </div>
            <UBadge color="blue" variant="solid" size="xs">USD Only</UBadge>
          </div>

          <!-- PayPal Metrics -->
          <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4 mb-6">
            <div class="rounded-xl border border-blue-100 bg-blue-50/70 p-4 text-center dark:border-blue-900/30 dark:bg-blue-900/20">
              <p class="text-xs font-semibold uppercase tracking-wide text-blue-500 dark:text-blue-400">{{ t('reports.paypal.metrics.totalRevenue') }}</p>
              <p class="mt-2 text-2xl font-bold text-blue-700 dark:text-blue-300">{{ formatCurrencyUSD(correctedPaypalData?.totalRevenue || 0) }}</p>
              <p class="mt-1 text-xs text-blue-500 dark:text-blue-400">{{ correctedPaypalData?.completedTransactions || 0 }} {{ t('reports.paypal.metrics.completed') }}</p>
            </div>
            <div class="rounded-xl border border-green-100 bg-green-50/70 p-4 text-center dark:border-green-900/30 dark:bg-green-900/20">
              <p class="text-xs font-semibold uppercase tracking-wide text-green-500 dark:text-green-400">{{ t('reports.paypal.metrics.successRate') }}</p>
              <p class="mt-2 text-2xl font-bold text-green-700 dark:text-green-300">{{ (correctedPaypalData?.successRate || 0).toFixed(1) }}%</p>
              <p class="mt-1 text-xs text-green-500 dark:text-green-400">{{ correctedPaypalData?.totalTransactions || 0 }} {{ t('reports.paypal.metrics.totalTransactions') }}</p>
            </div>
            <div class="rounded-xl border border-purple-100 bg-purple-50/70 p-4 text-center dark:border-purple-900/30 dark:bg-purple-900/20">
              <p class="text-xs font-semibold uppercase tracking-wide text-purple-500 dark:text-purple-400">{{ t('reports.paypal.metrics.averageValue') }}</p>
              <p class="mt-2 text-2xl font-bold text-purple-700 dark:text-purple-300">{{ formatCurrencyUSD(correctedPaypalData?.averageTransactionValue || 0) }}</p>
              <p class="mt-1 text-xs text-purple-500 dark:text-purple-400">{{ t('reports.paypal.metrics.perTransaction') }}</p>
            </div>
            <div class="rounded-xl border border-amber-100 bg-amber-50/70 p-4 text-center dark:border-amber-900/30 dark:bg-amber-900/20">
              <p class="text-xs font-semibold uppercase tracking-wide text-amber-500 dark:text-amber-400">{{ t('reports.paypal.metrics.netRevenue') }}</p>
              <p class="mt-2 text-2xl font-bold text-amber-700 dark:text-amber-300">{{ formatCurrencyUSD(correctedPaypalData?.netRevenue || 0) }}</p>
              <p class="mt-1 text-xs text-amber-500 dark:text-amber-400">{{ t('reports.paypal.metrics.afterFees') }}</p>
            </div>
          </div>

          <!-- Recent Transactions -->
          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <h4 class="text-sm font-semibold text-slate-700 dark:text-slate-200">{{ t('reports.paypal.transactions.title') }}</h4>
              <div class="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400">
                <span>{{ correctedPaypalData?.pagination?.totalElements || 0 }} {{ t('reports.paypal.transactions.total') }}</span>
                <span>•</span>
                <span>{{ t('reports.paypal.transactions.page') }} {{ (correctedPaypalData?.pagination?.currentPage || 0) + 1 }} {{ t('reports.paypal.transactions.of') }} {{ correctedPaypalData?.pagination?.totalPages || 1 }}</span>
              </div>
            </div>
            <div v-if="correctedPaypalData?.recentTransactions?.length > 0" class="space-y-3">
              <div
                v-for="transaction in (correctedPaypalData?.recentTransactions || [])"
                :key="transaction.id"
                class="group relative overflow-hidden rounded-xl border border-slate-200/70 bg-white/80 p-4 transition-all duration-300 hover:border-blue-300 hover:bg-blue-50/60 dark:border-white/10 dark:bg-white/5 dark:hover:border-blue-400/60 dark:hover:bg-blue-500/10 cursor-pointer"
                @click="openTransactionModal(transaction)"
              >
                <div class="flex items-start justify-between gap-4">
                  <div class="flex-1 min-w-0 space-y-1">
                    <div class="flex items-center gap-2 flex-wrap">
                      <p class="text-sm font-semibold text-slate-900 truncate dark:text-white group-hover:text-blue-700 dark:group-hover:text-blue-200">
                        {{ transaction.patientName || t('reports.paypal.transactions.unknownPatient') }}
                      </p>
                      <UBadge :color="getPaymentStatusColor(transaction.status)" variant="soft" size="xs">
                        {{ transaction.status }}
                      </UBadge>
                    </div>
                    <p class="text-xs text-slate-500 truncate dark:text-slate-400">
                      {{ transaction.doctorName || t('reports.paypal.transactions.unknownDoctor') }}
                    </p>
                    <div class="flex flex-wrap items-center gap-2 text-xs text-slate-500 dark:text-slate-400">
                      <span class="inline-flex items-center gap-1">
                        <UIcon name="i-lucide-calendar" class="h-3.5 w-3.5 text-blue-500 dark:text-blue-300" />
                        {{ formatTransactionDate(transaction.createdAt) }}
                      </span>
                      <span class="hidden h-1.5 w-1.5 rounded-full bg-slate-400/60 sm:inline-flex"></span>
                      <span class="inline-flex items-center gap-1">
                        <UIcon name="i-lucide-hash" class="h-3.5 w-3.5 text-slate-400" />
                        {{ transaction.orderId.substring(0, 8) }}...
                      </span>
                    </div>
                  </div>
                  <div class="flex flex-col items-end gap-1">
                    <p class="text-lg font-bold text-slate-900 dark:text-white">
                      {{ formatCurrencyUSD(transaction.amount) }}
                    </p>
                    <p class="text-xs text-slate-500 dark:text-slate-400">
                      {{ transaction.currency }}
                    </p>
                  </div>
                </div>
                <div class="absolute inset-x-0 bottom-0 h-0.5 bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 opacity-0 transition group-hover:opacity-100"></div>
              </div>
            </div>
            <div v-else class="rounded-xl border border-dashed border-slate-200 dark:border-slate-600 p-8 text-center">
              <div class="flex flex-col items-center gap-4">
                <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                  <UIcon name="i-lucide-credit-card" class="h-8 w-8 text-slate-400" />
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('reports.paypal.empty.title') }}</h3>
                  <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                    {{ t('reports.paypal.empty.description') }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Enhanced Pagination Controls -->
            <div v-if="(correctedPaypalData?.pagination?.totalPages || 1) > 1" class="pt-4 border-t border-slate-200 dark:border-slate-700">
              <PaginationControls
                :current-page="(correctedPaypalData?.pagination?.currentPage || 0) + 1"
                :total-pages="correctedPaypalData?.pagination?.totalPages || 1"
                :total-items="correctedPaypalData?.pagination?.totalElements || 0"
                :page-size="pageSize"
                :has-previous="correctedPaypalData?.pagination?.hasPrevious || false"
                :has-next="correctedPaypalData?.pagination?.hasNext || false"
                :loading="paypalLoading"
                item-label="transactions"
                :show-first-last="false"
                :page-sizes="[5, 10, 20, 50]"
                @page-change="handlePageChange"
                @page-size-change="handlePageSizeChange"
              />
            </div>
          </div>
        </div>

        <div v-else class="p-6">
          <div class="rounded-xl border border-red-100 bg-red-50/70 p-4 text-center">
            <UIcon name="i-lucide-alert-circle" class="h-8 w-8 text-red-600 mx-auto mb-2" />
            <p class="text-sm font-semibold text-red-700">{{ t('reports.paypal.error.title') }}</p>
            <p class="text-xs text-red-600 mt-1">{{ t('reports.paypal.error.description') }}</p>
          </div>
        </div>
      </UCard>
      </div>
    </div>
  </div>

  <!-- Transaction Detail Modal -->
  <UModal v-model="showTransactionModal" :ui="{ width: 'sm:max-w-2xl' }">
    <UCard v-if="selectedTransaction">
      <template #header>
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('reports.paypal.modal.title') }}</h3>
            <p class="text-sm text-slate-500 dark:text-slate-400">{{ t('reports.paypal.modal.orderIdLabel') }}: {{ selectedTransaction.orderId || t('reports.paypal.modal.unknown') }}</p>
          </div>
          <UBadge :color="getPaymentStatusColor(selectedTransaction.status)" variant="soft">
            {{ selectedTransaction.status || t('reports.paypal.modal.unknown') }}
          </UBadge>
        </div>
      </template>

      <div class="space-y-6">
        <!-- Payment Information -->
        <div>
          <h4 class="text-sm font-semibold text-slate-700 dark:text-slate-200 mb-3">{{ t('reports.paypal.modal.sections.payment') }}</h4>
          <div class="grid gap-4 sm:grid-cols-2">
            <div>
              <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('reports.paypal.modal.fields.amount') }}</p>
              <p class="text-lg font-bold text-slate-900 dark:text-white">
                {{ formatCurrencyUSD(selectedTransaction.amount || 0) }}
              </p>
              <p class="text-xs text-slate-500 dark:text-slate-400 mt-1">
                {{ selectedTransaction.currency || 'USD' }}
              </p>
            </div>
            <div>
              <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('reports.paypal.modal.fields.transactionType') }}</p>
              <p class="text-sm font-medium text-slate-900 dark:text-white">
                {{ selectedTransaction.type?.replace('_', ' ') || t('reports.paypal.modal.unknown') }}
              </p>
            </div>
            <div v-if="selectedTransaction.captureId">
              <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('reports.paypal.modal.fields.captureId') }}</p>
              <p class="text-sm font-mono text-slate-900 dark:text-white">
                {{ selectedTransaction.captureId }}
              </p>
            </div>
            <div>
              <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('reports.paypal.modal.fields.created') }}</p>
              <p class="text-sm text-slate-900 dark:text-white">
                {{ selectedTransaction.createdAt ? formatDateTime(selectedTransaction.createdAt) : t('reports.paypal.modal.unknown') }}
              </p>
            </div>
          </div>
        </div>

        <!-- Patient & Doctor Information -->
        <div>
          <h4 class="text-sm font-semibold text-slate-700 dark:text-slate-200 mb-3">{{ t('reports.paypal.modal.sections.appointment') }}</h4>
          <div class="grid gap-4 sm:grid-cols-2">
            <div>
              <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('reports.paypal.modal.fields.patient') }}</p>
              <p class="text-sm font-medium text-slate-900 dark:text-white">
                {{ selectedTransaction.patientName || t('reports.paypal.transactions.unknownPatient') }}
              </p>
            </div>
            <div>
              <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('reports.paypal.modal.fields.doctor') }}</p>
              <p class="text-sm font-medium text-slate-900 dark:text-white">
                {{ selectedTransaction.doctorName || t('reports.paypal.transactions.unknownDoctor') }}
              </p>
            </div>
            <div v-if="selectedTransaction.payerEmail">
              <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('reports.paypal.modal.fields.payerEmail') }}</p>
              <p class="text-sm text-slate-900 dark:text-white">
                {{ selectedTransaction.payerEmail }}
              </p>
            </div>
            <div v-if="selectedTransaction.payerName">
              <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('reports.paypal.modal.fields.payerName') }}</p>
              <p class="text-sm text-slate-900 dark:text-white">
                {{ selectedTransaction.payerName }}
              </p>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex items-center gap-3 pt-4 border-t border-slate-200 dark:border-slate-700">
          <UButton
            v-if="selectedTransaction.appointmentId"
            color="blue"
            variant="soft"
            icon="i-lucide-calendar"
            @click="navigateTo(`/appointments/${selectedTransaction.appointmentId}`)"
          >
            {{ t('reports.paypal.modal.actions.viewAppointment') }}
          </UButton>
          <UButton
            color="gray"
            variant="soft"
            icon="i-lucide-copy"
            @click="copyToClipboard(selectedTransaction.orderId || '')"
          >
            {{ t('reports.paypal.modal.actions.copyOrderId') }}
          </UButton>
        </div>
      </div>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Tooltip,
  Legend,
  Filler
} from "chart.js";
import { ref, computed, watch } from "vue";
import { Line, Bar, Doughnut } from "vue-chartjs";
import { useI18n } from 'vue-i18n';
import DateRangeFilter from '../components/DateRangeFilter.vue';
import { useDateRangeFilter, type DateRange } from '../composables/useDateRangeFilter';
import { useAdminApi } from '../composables/useAdminApi';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Tooltip, Legend, Filler);

const { fetcher, request } = useAdminApi();
const toast = useToast();
const { formatCurrency } = useClinicSettings();
const { t } = useI18n();

// Date range filtering
const { dateRange, getApiParams } = useDateRangeFilter();

useHead(() => ({
  title: t('reports.meta.headTitle')
}));

type AppointmentsStatusKey = "SCHEDULED" | "CONFIRMED" | "COMPLETED" | "CANCELLED" | string;

interface CategoryBreakdown {
  label: string;
  value: number;
  percentage: number;
}

interface TimeSeriesPoint {
  period: string;
  value: number;
}

interface RevenuePoint {
  period: string;
  amount: number;
}

interface ReportMetrics {
  totalAppointments: number;
  todayAppointments: number;
  thisWeekAppointments: number;
  thisMonthAppointments: number;
  scheduledAppointments: number;
  completedAppointments: number;
  cancelledAppointments: number;
  totalPatients: number;
  newPatientsThisMonth: number;
  totalDoctors: number;
  paymentsCollectedCount: number;
  paymentsOutstandingCount: number;
  appointmentsByStatus: Record<AppointmentsStatusKey, number>;
  doctorBreakdown: CategoryBreakdown[];
  serviceBreakdown: CategoryBreakdown[];
  paymentMethodBreakdown: CategoryBreakdown[];
  appointmentTrend: TimeSeriesPoint[];
  revenueTrend: RevenuePoint[];
  averageDailyAppointments: number;
  noShowRate: number;
  collectionRate: number;
  followUpVisitsThisMonth: number;
  activeTreatmentPlans: number;
  revenueThisMonth: number;
  revenueLastMonth: number;
  // Multi-currency support
  revenueThisMonthByCurrency?: Record<string, number>;
  revenueLastMonthByCurrency?: Record<string, number>;
  revenueTrendByCurrency?: Record<string, RevenuePoint[]>;
}

const reportsFallback: ReportMetrics = {
  totalAppointments: 1287,
  todayAppointments: 14,
  thisWeekAppointments: 72,
  thisMonthAppointments: 268,
  scheduledAppointments: 412,
  completedAppointments: 712,
  cancelledAppointments: 95,
  totalPatients: 842,
  newPatientsThisMonth: 31,
  totalDoctors: 14,
  paymentsCollectedCount: 756,
  paymentsOutstandingCount: 48,
  appointmentsByStatus: {
    SCHEDULED: 230,
    CONFIRMED: 182,
    COMPLETED: 712,
    CANCELLED: 95
  },
  doctorBreakdown: [
    { label: "Dr. Laila Haddad", value: 188, percentage: 26.4 },
    { label: "Dr. Samir Al-Otaibi", value: 162, percentage: 22.8 },
    { label: "Dr. Nour Al-Rawi", value: 141, percentage: 19.6 },
    { label: "Dr. Mariam Saadi", value: 118, percentage: 16.5 },
    { label: "Dr. Omar Khouri", value: 94, percentage: 13.0 },
    { label: "Dr. Youssef Rahman", value: 82, percentage: 11.4 }
  ],
  serviceBreakdown: [
    { label: "Orthodontics", value: 210, percentage: 29.6 },
    { label: "Cosmetic Dentistry", value: 164, percentage: 23.1 },
    { label: "Implant Surgery", value: 138, percentage: 19.4 },
    { label: "Routine Checkups", value: 112, percentage: 15.7 },
    { label: "Emergency Care", value: 76, percentage: 10.7 },
    { label: "Other", value: 52, percentage: 7.3 }
  ],
  paymentMethodBreakdown: [
    { label: "Card", value: 324, percentage: 42.8 },
    { label: "Cash", value: 268, percentage: 35.4 },
    { label: "Bank Transfer", value: 112, percentage: 14.8 },
    { label: "Insurance", value: 52, percentage: 6.9 },
    { label: "Other", value: 26, percentage: 3.4 }
  ],
  appointmentTrend: [
    { period: "Mar 20", value: 7 },
    { period: "Mar 22", value: 9 },
    { period: "Mar 24", value: 10 },
    { period: "Mar 26", value: 11 },
    { period: "Mar 28", value: 12 },
    { period: "Mar 30", value: 9 },
    { period: "Apr 1", value: 12 },
    { period: "Apr 3", value: 14 },
    { period: "Apr 5", value: 15 },
    { period: "Apr 7", value: 13 },
    { period: "Apr 9", value: 16 },
    { period: "Apr 11", value: 18 },
    { period: "Apr 13", value: 17 },
    { period: "Apr 15", value: 15 },
    { period: "Apr 17", value: 19 }
  ],
  revenueTrend: [
    { period: "Nov 2024", amount: 16200 },
    { period: "Dec 2024", amount: 17120 },
    { period: "Jan 2025", amount: 17680 },
    { period: "Feb 2025", amount: 16240 },
    { period: "Mar 2025", amount: 17210 },
    { period: "Apr 2025", amount: 18450 }
  ],
  averageDailyAppointments: 8.9,
  noShowRate: 5.8,
  collectionRate: 86.1,
  followUpVisitsThisMonth: 52,
  activeTreatmentPlans: 38,
  revenueThisMonth: 18450,
  revenueLastMonth: 17210
};

const { data: metrics, pending: loading, error: fetchError, refresh: refreshMetrics } = await useAsyncData(
  "report-metrics",
  async () => {
    const params = getApiParams();
    const url = `/reports/metrics${params.toString() ? `?${params.toString()}` : ''}`;
    
    // Try to get real data first, but log if we're using fallback
    try {
      const realData = await request<ReportMetrics>(url);
      console.log('✅ Got real API data:', { totalAppointments: realData.totalAppointments });
      return realData;
    } catch (error) {
      console.error('❌ API call failed, using fallback data:', error);
      console.log('🔄 Using fallback data with totalAppointments:', reportsFallback.totalAppointments);
      return reportsFallback;
    }
  },
  {
    watch: [dateRange], // Refresh when date range changes
  }
);

// PayPal data types
interface PayPalTransactionDTO {
  id: number;
  orderId: string;
  captureId?: string;
  status: string;
  amount: number;
  currency: string;
  type: string;
  patientName?: string;
  doctorName?: string;
  payerEmail?: string;
  payerName?: string;
  appointmentId?: number;
  createdAt: string;
  updatedAt: string;
}

interface PayPalSummaryDTO {
  totalRevenue: number;
  totalTransactions: number;
  completedTransactions: number;
  pendingTransactions: number;
  failedTransactions: number;
  averageTransactionValue: number;
  estimatedPayPalFees: number;
  netRevenue: number;
  successRate: number;
  recentTransactions: PayPalTransactionDTO[];
  pagination: {
    currentPage: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}

// Pagination state
const currentPage = ref(0);
const pageSize = ref(10);

// PayPal fallback data
const paypalFallback: PayPalSummaryDTO = {
  totalRevenue: 2450.00,
  totalTransactions: 12,
  completedTransactions: 10,
  pendingTransactions: 1,
  failedTransactions: 1,
  averageTransactionValue: 245.00,
  estimatedPayPalFees: 78.50,
  netRevenue: 2371.50,
  successRate: 83.3,
  recentTransactions: [
    {
      id: 1,
      orderId: "8XY12345678901234",
      captureId: "9AB12345678901234",
      status: "COMPLETED",
      amount: 150.00,
      currency: "USD",
      type: "VIRTUAL_CONSULTATION",
      patientName: "Sarah Johnson",
      doctorName: "Dr. Ahmad Al-Rashid",
      payerEmail: "sarah.j@email.com",
      appointmentId: 123,
      createdAt: "2025-01-15T10:30:00Z",
      updatedAt: "2025-01-15T10:32:00Z"
    },
    {
      id: 2,
      orderId: "7WX98765432109876",
      captureId: "6VW98765432109876",
      status: "COMPLETED",
      amount: 200.00,
      currency: "USD",
      type: "VIRTUAL_CONSULTATION",
      patientName: "Michael Chen",
      doctorName: "Dr. Layla Rahman",
      payerEmail: "m.chen@email.com",
      appointmentId: 124,
      createdAt: "2025-01-14T14:15:00Z",
      updatedAt: "2025-01-14T14:17:00Z"
    }
  ],
  pagination: {
    currentPage: 0,
    pageSize: 10,
    totalElements: 2,
    totalPages: 1,
    hasNext: false,
    hasPrevious: false
  }
};

const { data: paypalData, pending: paypalLoading, refresh: refreshPaypalData } = await useAsyncData(
  "paypal-summary",
  async () => {
    const params = new URLSearchParams({
      page: currentPage.value.toString(),
      size: pageSize.value.toString()
    });
    
    // Add date range parameters
    const dateParams = getApiParams();
    for (const [key, value] of dateParams.entries()) {
      params.append(key, value);
    }
    
    const result = await fetcher<PayPalSummaryDTO>(`/reports/paypal-summary?${params}`, paypalFallback);
    
    // Debug logging to understand the data
    console.log('PayPal API Response:', {
      totalElements: result?.pagination?.totalElements,
      transactionsLength: result?.recentTransactions?.length,
      totalTransactions: result?.totalTransactions,
      result
    });
    
    return result;
  },
  {
    watch: [dateRange, currentPage, pageSize], // Refresh when date range, page, or size changes
  }
);

const error = computed(() => fetchError.value?.message);

// Computed property to ensure consistent pagination data
const correctedPaypalData = computed(() => {
  if (!paypalData.value) return null;
  
  const data = paypalData.value;
  const actualTransactionCount = data.recentTransactions?.length || 0;
  const reportedTotalElements = data.pagination?.totalElements || 0;
  
  // If we have transactions but totalElements is 0, use the transaction count
  const correctedTotalElements = Math.max(reportedTotalElements, actualTransactionCount);
  const correctedTotalPages = Math.max(data.pagination?.totalPages || 1, 1);
  
  return {
    ...data,
    pagination: {
      ...data.pagination,
      totalElements: correctedTotalElements,
      totalPages: correctedTotalPages
    }
  };
});

const revenueMomentum = computed(() => {
  if (!metrics.value) {
    return { delta: 0, percent: 0, positive: true };
  }
  const delta = (metrics.value.revenueThisMonth ?? 0) - (metrics.value.revenueLastMonth ?? 0);
  const percent = metrics.value.revenueLastMonth
    ? (delta / metrics.value.revenueLastMonth) * 100
    : 0;
  return {
    delta,
    percent,
    positive: delta >= 0
  };
});

const formatPercent = (value?: number) => `${Number(value ?? 0).toFixed(1)}%`;

const formatCurrencyWithCode = (amount: number, currencyCode: string) => {
  return new Intl.NumberFormat('en-US', {
    style: 'decimal',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount);
};

// Format amount as USD (for PayPal)
const formatCurrencyUSD = (amount: number) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount);
};

const appointmentTrendChart = computed(() => {
  const labels = metrics.value?.appointmentTrend?.map(point => point.period) ?? [];
  const data = metrics.value?.appointmentTrend?.map(point => point.value) ?? [];
  return {
    labels,
    datasets: [
      {
        label: "Appointments",
        data,
        borderWidth: 3,
        borderColor: "#8b5cf6",
        backgroundColor: "rgba(139, 92, 246, 0.12)",
        tension: 0.4,
        pointRadius: 3,
        fill: true
      }
    ]
  };
});

const revenueChartData = computed(() => {
  // Check if we have multi-currency data
  const hasMultiCurrency = metrics.value?.revenueTrendByCurrency &&
    Object.keys(metrics.value.revenueTrendByCurrency).length > 0;

  if (hasMultiCurrency) {
    // Multi-currency mode: show separate line for each currency
    const currencies = Object.keys(metrics.value.revenueTrendByCurrency!);
    const labels = metrics.value.revenueTrendByCurrency![currencies[0]]?.map(point => point.period) ?? [];

    // Define colors for different currencies
    const currencyColors: Record<string, { border: string; background: string }> = {
      USD: { border: '#3b82f6', background: 'rgba(59, 130, 246, 0.12)' }, // blue
      JOD: { border: '#22c55e', background: 'rgba(34, 197, 94, 0.12)' }, // green
      AED: { border: '#a855f7', background: 'rgba(168, 85, 247, 0.12)' }, // purple
      SAR: { border: '#f59e0b', background: 'rgba(245, 158, 11, 0.12)' }, // amber
      EUR: { border: '#ef4444', background: 'rgba(239, 68, 68, 0.12)' }, // red
      GBP: { border: '#ec4899', background: 'rgba(236, 72, 153, 0.12)' } // pink
    };

    const datasets = currencies.map(currency => {
      const data = metrics.value!.revenueTrendByCurrency![currency].map(point => point.amount);
      const colors = currencyColors[currency] || {
        border: '#64748b',
        background: 'rgba(100, 116, 139, 0.12)'
      };

      return {
        label: `${currency}`,
        data,
        borderWidth: 3,
        borderColor: colors.border,
        backgroundColor: colors.background,
        tension: 0.35,
        pointRadius: 3,
        fill: true
      };
    });

    return { labels, datasets };
  }

  // Fallback to single currency mode (backwards compatibility)
  const labels = metrics.value?.revenueTrend?.map(point => point.period) ?? [];
  const data = metrics.value?.revenueTrend?.map(point => point.amount) ?? [];
  return {
    labels,
    datasets: [
      {
        label: "Revenue",
        data,
        borderWidth: 3,
        borderColor: "#22c55e",
        backgroundColor: "rgba(34, 197, 94, 0.12)",
        tension: 0.35,
        pointRadius: 3,
        fill: true
      }
    ]
  };
});

const hasAppointmentTrend = computed(() => {
  const dataset = appointmentTrendChart.value.datasets?.[0];
  return Array.isArray(dataset?.data) && dataset.data.length > 0;
});

const hasRevenueTrend = computed(() => {
  const datasets = revenueChartData.value.datasets;
  return datasets && datasets.length > 0 && datasets.some(ds => Array.isArray(ds.data) && ds.data.length > 0);
});

const doctorChartData = computed(() => {
  const labels = metrics.value?.doctorBreakdown?.map(entry => entry.label) ?? [];
  const data = metrics.value?.doctorBreakdown?.map(entry => entry.value) ?? [];
  return {
    labels,
    datasets: [
      {
        label: "Appointments",
        data,
        backgroundColor: "#6366f1",
        borderRadius: 14,
        barPercentage: 0.6,
        categoryPercentage: 0.6
      }
    ]
  };
});

const hasDoctorBreakdown = computed(() => {
  const dataset = doctorChartData.value.datasets?.[0];
  return Array.isArray(dataset?.data) && dataset.data.length > 0;
});

const palette = ["#8b5cf6", "#22c55e", "#0ea5e9", "#f97316", "#14b8a6", "#facc15", "#ec4899"];

const serviceChartData = computed(() => {
  const labels = metrics.value?.serviceBreakdown?.map(entry => entry.label) ?? [];
  const data = metrics.value?.serviceBreakdown?.map(entry => entry.value) ?? [];
  return {
    labels,
    datasets: [
      {
        data,
        backgroundColor: palette.slice(0, Math.max(data.length, 1))
      }
    ]
  };
});

const hasServiceBreakdown = computed(() => {
  const dataset = serviceChartData.value.datasets?.[0];
  return Array.isArray(dataset?.data) && dataset.data.length > 0;
});

const paymentChartData = computed(() => {
  const labels = metrics.value?.paymentMethodBreakdown?.map(entry => entry.label) ?? [];
  const data = metrics.value?.paymentMethodBreakdown?.map(entry => entry.value) ?? [];
  return {
    labels,
    datasets: [
      {
        data,
        backgroundColor: palette.slice(0, Math.max(data.length, 1)).reverse()
      }
    ]
  };
});

const hasPaymentBreakdown = computed(() => {
  const dataset = paymentChartData.value.datasets?.[0];
  return Array.isArray(dataset?.data) && dataset.data.length > 0;
});

const statusCards = computed(() => {
  const total = metrics.value?.totalAppointments ?? 0;
  const config = [
    { key: "SCHEDULED", label: t('reports.charts.statusBreakdown.scheduled'), icon: "i-lucide-calendar-clock", tone: "bg-emerald-50", text: "text-emerald-600" },
    { key: "CONFIRMED", label: t('reports.charts.statusBreakdown.confirmed'), icon: "i-lucide-badge-check", tone: "bg-violet-50", text: "text-violet-600" },
    { key: "COMPLETED", label: t('reports.charts.statusBreakdown.completed'), icon: "i-lucide-check-circle-2", tone: "bg-sky-50", text: "text-sky-600" },
    { key: "CANCELLED", label: t('reports.charts.statusBreakdown.cancelled'), icon: "i-lucide-x-circle", tone: "bg-red-50", text: "text-red-600" }
  ];
  return config.map(item => {
    const count = metrics.value?.appointmentsByStatus?.[item.key] ?? 0;
    const percentage = total > 0 ? ((count / total) * 100).toFixed(1) : "0.0";
    return {
      ...item,
      count,
      percentage
    };
  });
});

const operationalKpis = computed(() => {
  if (!metrics.value) {
    return [];
  }
  return [
    {
      label: t('reports.snapshots.operational.avgDaily'),
      value: Number(metrics.value.averageDailyAppointments).toFixed(1),
      sublabel: t('reports.snapshots.operational.avgDailySubtitle'),
      icon: "i-lucide-activity",
      tone: "bg-violet-100",
      text: "text-violet-600"
    },
    {
      label: t('reports.snapshots.operational.collectionRate'),
      value: formatPercent(metrics.value.collectionRate),
      sublabel: t('reports.snapshots.operational.collectionRateSubtitle'),
      icon: "i-lucide-badge-check",
      tone: "bg-emerald-100",
      text: "text-emerald-600"
    },
    {
      label: t('reports.snapshots.operational.noShowRate'),
      value: formatPercent(metrics.value.noShowRate),
      sublabel: t('reports.snapshots.operational.noShowRateSubtitle'),
      icon: "i-lucide-user-x",
      tone: "bg-amber-100",
      text: "text-amber-600"
    },
    {
      label: t('reports.snapshots.operational.activePlans'),
      value: metrics.value.activeTreatmentPlans.toString(),
      sublabel: `${metrics.value.followUpVisitsThisMonth} ${t('reports.snapshots.operational.followUps')}`,
      icon: "i-lucide-heart-pulse",
      tone: "bg-sky-100",
      text: "text-sky-600"
    }
  ];
});

const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      mode: "index" as const,
      intersect: false
    }
  },
  scales: {
    x: {
      grid: {
        display: false
      },
      ticks: {
        font: {
          family: "Inter"
        }
      }
    },
    y: {
      beginAtZero: true,
      ticks: {
        font: {
          family: "Inter"
        }
      }
    }
  }
};

const doctorChartOptions = {
  indexAxis: "y" as const,
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      mode: "index" as const,
      intersect: false
    }
  },
  scales: {
    x: {
      beginAtZero: true,
      grid: {
        color: "rgba(15, 23, 42, 0.08)"
      },
      ticks: {
        precision: 0,
        font: {
          family: "Inter"
        }
      }
    },
    y: {
      grid: {
        display: false
      },
      ticks: {
        font: {
          family: "Inter"
        }
      }
    }
  }
};

const doughnutOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: "bottom" as const,
      labels: {
        boxWidth: 12,
        usePointStyle: true,
        font: {
          family: "Inter",
          size: 12
        }
      }
    }
  },
  cutout: "65%"
};

const refreshData = async () => {
  await Promise.all([
    refreshMetrics(),
    refreshPaypalData()
  ]);
};

// Handle date range changes
const handleDateRangeChange = async (newRange: DateRange) => {
  // The watch on dateRange will automatically trigger data refresh
  // Reset PayPal pagination to first page when date range changes
  currentPage.value = 0;
};

const exportData = () => {
  toast.add({
    title: t('reports.toasts.exportSoon'),
    description: t('reports.toasts.exportDescription'),
    color: "blue",
    icon: "i-lucide-info"
  });
};

// PayPal transaction modal
const showTransactionModal = ref(false);
const selectedTransaction = ref<PayPalTransactionDTO | null>(null);

const openTransactionModal = (transaction: PayPalTransactionDTO) => {
  selectedTransaction.value = transaction;
  showTransactionModal.value = true;
};

const getPaymentStatusColor = (status: string) => {
  switch (status) {
    case 'COMPLETED':
      return 'green';
    case 'PENDING':
      return 'yellow';
    case 'FAILED':
      return 'red';
    default:
      return 'gray';
  }
};

// Use clinic timezone for all date/time displays
// CRITICAL: All admins see times in clinic timezone, not their browser timezone
const { timezone, abbreviation } = useClinicTimezone();

const formatTransactionDate = (dateInput: string | number) => {
  if (!dateInput) return "—";
  // Use clinic timezone formatter from dateUtils
  return formatDateInClinicTimezone(dateInput, timezone.value);
}

const formatDateTime = (dateInput: string | number | null | undefined) => {
  if (!dateInput) return "—";
  // Use clinic timezone formatter with abbreviation (e.g., "Jan 15, 2024, 2:00 PM EET")
  return formatDateTimeInClinicTimezone(dateInput, timezone.value, abbreviation.value);
}

const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text);
    toast.add({
      title: t('reports.paypal.toasts.copied'),
      description: t('reports.paypal.toasts.copiedDescription', { orderId: text }),
      color: "green",
      icon: "i-lucide-check"
    });
  } catch {
    toast.add({
      title: t('reports.paypal.toasts.copyFailed'),
      description: t('reports.paypal.toasts.copyFailedDescription'),
      color: "red",
      icon: "i-lucide-x"
    });
  }
};

const router = useRouter();
const navigateTo = (path: string) => router.push(path);

// Pagination functions
const goToPage = async (page: number) => {
  if (page < 0 || (paypalData.value && page >= (paypalData.value.pagination?.totalPages || 1))) {
    return;
  }
  currentPage.value = page;
  await refreshPaypalData();
};

const handlePageChange = async (page: number) => {
  // Convert from 1-based to 0-based page numbering for API
  const apiPage = page - 1;
  if (apiPage < 0 || (paypalData.value && apiPage >= (paypalData.value.pagination?.totalPages || 1))) {
    return;
  }
  currentPage.value = apiPage;
  await refreshPaypalData();
};

const handlePageSizeChange = async (newSize: number) => {
  pageSize.value = newSize;
  currentPage.value = 0; // Reset to first page when changing page size
  await refreshPaypalData();
};

const changePageSize = async () => {
  currentPage.value = 0; // Reset to first page when changing page size
  await refreshPaypalData();
};

// Navigation function
const navigateToAllTransactions = () => {
  navigateTo('/paypal-transactions');
};
</script>
