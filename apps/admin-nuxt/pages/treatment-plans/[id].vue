<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="flex flex-col items-center gap-4">
        <UIcon name="i-lucide-loader-2" class="h-8 w-8 animate-spin text-indigo-600" />
        <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('treatmentPlans.detail.loading') }}</p>
      </div>
    </div>

    <template v-else-if="plan">
      <!-- Header Section -->
      <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
        <div class="max-w-7xl mx-auto px-6 py-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-4">
              <UButton
                icon="i-lucide-arrow-left"
                color="gray"
                variant="ghost"
                size="sm"
                @click="navigateTo('/treatment-plans')"
              >
                {{ t('treatmentPlans.detail.back.list') }}
              </UButton>
              <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-mint-600 to-mint-400 shadow-lg">
                <UIcon name="i-lucide-clipboard-heart" class="h-6 w-6 text-white" />
              </div>
              <div>
                <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ plan.patientName }}</h1>
                <p class="text-sm text-slate-600 dark:text-slate-300">
                  {{ t('treatmentPlans.detail.header.subtitle', { treatment: plan.treatmentTypeName, doctor: t('treatmentPlans.common.doctorPrefix', { name: plan.doctorName }) }) }}
                </p>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <UBadge :color="getStatusColor(plan.status)" variant="soft" size="lg">
                {{ formatStatus(plan.status) }}
              </UBadge>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div class="max-w-7xl mx-auto px-6 py-8 space-y-8">

        <!-- Financial Overview -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
                <UIcon name="i-lucide-banknote" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('treatmentPlans.detail.cards.total.title') }}</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatCurrency(plan.convertedTotalPrice, plan.convertedCurrency) }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('treatmentPlans.detail.cards.total.caption') }}</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
                <UIcon name="i-lucide-check-circle" class="h-5 w-5 text-green-600 dark:text-green-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('treatmentPlans.detail.cards.paid.title') }}</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatCurrency(plan.convertedTotalPaid, plan.convertedCurrency) }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('treatmentPlans.detail.cards.paid.caption') }}</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-900/20">
                <UIcon name="i-lucide-banknote" class="h-5 w-5 text-amber-600 dark:text-amber-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('treatmentPlans.detail.cards.remaining.title') }}</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatCurrency(plan.convertedRemainingBalance, plan.convertedCurrency) }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('treatmentPlans.detail.cards.remaining.caption') }}</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-50 dark:bg-purple-900/20">
                <UIcon name="i-lucide-calendar-clock" class="h-5 w-5 text-purple-600 dark:text-purple-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('treatmentPlans.detail.cards.perVisit.title') }}</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatCurrency(plan.convertedExpectedPaymentPerVisit, plan.convertedCurrency) }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('treatmentPlans.detail.cards.perVisit.caption') }}</p>
          </div>
        </div>

        <!-- Progress Section -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-mint-600 to-mint-400 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-trending-up" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">{{ t('treatmentPlans.detail.progress.title') }}</h2>
                <p class="text-sm text-indigo-100">
                  {{ t('treatmentPlans.detail.progress.subtitle', { completed: formatNumber(plan.completedVisits), total: formatNumber(plan.plannedFollowups) }) }}
                </p>
              </div>
            </div>
          </div>
          <div class="p-6 space-y-6">
            <div>
              <div class="flex items-center justify-between text-sm font-medium text-slate-500 dark:text-slate-400 mb-2">
                <span>{{ t('treatmentPlans.detail.progress.visits.label') }}</span>
                <span>{{ Math.round(getVisitProgressPercent(plan)) }}%</span>
              </div>
              <UProgress
                :value="getVisitProgressPercent(plan)"
                color="indigo"
                size="lg"
                class="rounded-full"
              />
            </div>
            <div>
              <div class="flex items-center justify-between text-sm font-medium text-slate-500 dark:text-slate-400 mb-2">
                <span>{{ t('treatmentPlans.detail.progress.payments.label') }}</span>
                <span>{{ Math.round(getPaymentProgressPercent(plan)) }}%</span>
              </div>
              <UProgress
                :value="getPaymentProgressPercent(plan)"
                :color="getProgressColor(plan)"
                size="lg"
                class="rounded-full"
              />
              <div class="flex items-center justify-between text-sm mt-2">
                <span class="text-slate-600 dark:text-slate-300">
                  {{ t('treatmentPlans.detail.progress.payments.paid', { amount: formatCurrency(plan.convertedTotalPaid, plan.convertedCurrency) }) }}
                </span>
                <span class="font-medium text-slate-900 dark:text-white">
                  {{ t('treatmentPlans.detail.progress.payments.total', { amount: formatCurrency(plan.convertedTotalPrice - (plan.discountAmount || 0), plan.convertedCurrency) }) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Actions Section -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-zap" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">{{ t('treatmentPlans.detail.actions.title') }}</h2>
                <p class="text-sm text-slate-300">{{ t('treatmentPlans.detail.actions.subtitle') }}</p>
              </div>
            </div>
          </div>
          <div class="p-6">
            <div class="flex flex-wrap gap-3">
              <UButton
                v-if="plan.status !== 'COMPLETED' && plan.status !== 'CANCELLED'"
                icon="i-lucide-calendar-plus"
                color="indigo"
                size="lg"
                @click="openRecordVisitModal"
              >
                {{ t('treatmentPlans.detail.actions.recordVisit') }}
              </UButton>
              <UButton
                v-if="plan.status !== 'COMPLETED' && plan.status !== 'CANCELLED'"
                icon="i-lucide-tag"
                color="amber"
                size="lg"
                @click="showDiscountModal = true"
              >
                {{ t('treatmentPlans.detail.actions.applyDiscount') }}
              </UButton>
              <UButton
                v-if="plan.status === 'IN_PROGRESS' && plan.completedVisits >= plan.plannedFollowups"
                icon="i-lucide-check-circle"
                color="green"
                size="lg"
                @click="completePlan"
              >
                {{ t('treatmentPlans.detail.actions.markComplete') }}
              </UButton>
              <UButton
                v-if="plan.status !== 'COMPLETED' && plan.status !== 'CANCELLED'"
                icon="i-lucide-x-circle"
                color="red"
                variant="soft"
                size="lg"
                @click="showCancelModal = true"
              >
                {{ t('treatmentPlans.detail.actions.cancelPlan') }}
              </UButton>
            </div>
          </div>
        </div>

        <!-- Scheduled Follow-Up Visits -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-mint-500 to-mint-400 px-6 py-4">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-calendar-clock" class="h-5 w-5 text-white" />
                <div>
                  <h2 class="text-lg font-semibold text-white">{{ t('treatmentPlans.detail.scheduled.title') }}</h2>
                  <p class="text-sm text-violet-100">{{ t('treatmentPlans.detail.scheduled.subtitle') }}</p>
                </div>
              </div>
              <UBadge color="white" variant="solid" size="sm">
                {{ t('treatmentPlans.detail.scheduled.count', { count: formatNumber(plan.scheduledFollowUps.length) }) }}
              </UBadge>
            </div>
          </div>
          <div class="p-6">

            <div v-if="plan.scheduledFollowUps.length" class="space-y-4">
              <div
                v-for="item in plan.scheduledFollowUps"
                :key="item.appointmentId"
                class="rounded-xl border p-6 flex flex-col gap-4 md:flex-row md:items-center md:justify-between transition-all duration-200 hover:shadow-md"
                :class="isVisitRecorded(item.appointmentId) ? 'border-green-200 bg-green-50/50 dark:bg-green-900/10 dark:border-green-700' : 'border-slate-200 dark:border-slate-600 hover:border-indigo-300 dark:hover:border-indigo-600'"
              >
            <div class="space-y-2 flex-1">
              <div class="flex items-center gap-2 flex-wrap">
                <UBadge color="violet" variant="soft" size="sm">{{ t('treatmentPlans.detail.scheduled.badges.visit', { number: formatNumber(item.visitNumber) }) }}</UBadge>
                <UBadge :color="appointmentStatusColor(item.status)" variant="soft" size="sm">
                  {{ formatStatus(item.status) }}
                </UBadge>
                <UBadge color="gray" variant="soft" size="sm">
                  {{ formatMode(item.bookingMode) }}
                </UBadge>
                <UBadge v-if="isVisitRecorded(item.appointmentId)" color="green" variant="soft" size="sm">
                  <UIcon name="i-lucide-check-circle" class="mr-1 h-3 w-3" />
                  {{ t('treatmentPlans.detail.scheduled.badges.recorded') }}
                </UBadge>
              </div>
              <p class="text-sm text-slate-600 dark:text-slate-400">
                {{ formatDateTime(item.scheduledAt) }}
              </p>
              <div class="flex flex-wrap gap-2">
                <UBadge :color="item.paymentCollected ? 'green' : 'gray'" variant="soft" size="xs">
                  <UIcon name="i-lucide-wallet" class="mr-1 h-3 w-3" />
                  {{ item.paymentCollected ? t('treatmentPlans.detail.badges.payment.paid') : t('treatmentPlans.detail.badges.payment.unpaid') }}
                </UBadge>
                <UBadge :color="attendanceStatusColor(item.patientAttended)" variant="soft" size="xs">
                  <UIcon :name="attendanceStatusIcon(item.patientAttended)" class="mr-1 h-3 w-3" />
                  {{ attendanceStatusLabel(item.patientAttended) }}
                </UBadge>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <!-- Allow recording for any scheduled visit until it's been logged -->
              <UButton
                v-if="!isVisitRecorded(item.appointmentId)"
                color="green"
                size="sm"
                icon="i-lucide-clipboard-check"
                @click="openRecordVisitForAppointment(item)"
              >
                {{ t('treatmentPlans.detail.scheduled.buttons.record') }}
              </UButton>
              <UButton
                v-else
                color="gray"
                variant="soft"
                size="sm"
                icon="i-lucide-check"
                disabled
              >
                {{ t('treatmentPlans.detail.scheduled.buttons.recorded') }}
              </UButton>
              <UButton
                color="primary"
                variant="soft"
                size="sm"
                icon="i-lucide-pencil"
                @click="navigateTo(`/appointments/${item.appointmentId}`)"
              >
                {{ t('treatmentPlans.detail.scheduled.buttons.viewEdit') }}
              </UButton>
            </div>
          </div>
        </div>
            <div v-else class="rounded-xl border border-dashed border-slate-200 dark:border-slate-600 p-8 text-center">
              <div class="flex flex-col items-center gap-4">
                <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                  <UIcon name="i-lucide-calendar-clock" class="h-8 w-8 text-slate-400" />
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('treatmentPlans.detail.scheduled.empty.title') }}</h3>
                  <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                    {{ t('treatmentPlans.detail.scheduled.empty.subtitle') }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Recorded Follow-up Visits -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-green-500 to-emerald-600 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-clipboard-check" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">{{ t('treatmentPlans.detail.recorded.title') }}</h2>
                <p class="text-sm text-green-100">{{ t('treatmentPlans.detail.recorded.subtitle') }}</p>
              </div>
            </div>
          </div>
          <div class="p-6">

            <div v-if="plan.followUpVisits.length > 0" class="space-y-4">
              <div
                v-for="visit in plan.followUpVisits"
                :key="visit.id"
                class="rounded-xl border border-slate-200 dark:border-slate-600 p-6 space-y-4 hover:shadow-md hover:border-indigo-300 dark:hover:border-indigo-600 transition-all duration-200"
              >
            <div class="flex items-start justify-between gap-4">
              <div>
                <h4 class="font-semibold text-gray-900 dark:text-white">
                  {{ t('treatmentPlans.detail.scheduled.badges.visit', { number: formatNumber(visit.visitNumber) }) }}
                </h4>
                <p class="text-sm text-gray-500 dark:text-gray-400">
                  {{ formatDate(visit.visitDate) }}
                </p>
              </div>
              <div class="flex items-center gap-2">
                <UBadge color="green" variant="subtle">
                  {{ formatCurrency(visit.totalPaymentsThisVisit, plan.convertedCurrency) }}
                </UBadge>
                <UButton
                  size="xs"
                  variant="soft"
                  color="primary"
                  icon="i-lucide-pencil"
                  :disabled="recordVisitLoading"
                  @click.stop="openEditVisit(visit)"
                >
                  {{ t('treatmentPlans.detail.recorded.buttons.edit') }}
                </UButton>
              </div>
            </div>

            <div v-if="visit.notes" class="text-sm text-gray-600 dark:text-gray-300">
              {{ visit.notes }}
            </div>

            <!-- Payments -->
            <div v-if="visit.payments.length > 0" class="space-y-2">
              <p class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">
                {{ t('treatmentPlans.detail.recorded.paymentsTitle') }}
              </p>
              <div class="space-y-1">
                <div
                  v-for="payment in visit.payments"
                  :key="payment.id"
                  class="flex items-center justify-between text-sm"
                >
                  <div class="flex items-center gap-2">
                    <UBadge :color="getPaymentMethodColor(payment.paymentMethod)" size="xs">
                      {{ payment.paymentMethod }}
                    </UBadge>
                    <span class="text-gray-600 dark:text-gray-300">
                      {{ formatDate(payment.paymentDate) }}
                    </span>
                  </div>
                  <span class="font-semibold text-gray-900 dark:text-white">
                    {{ formatCurrency(payment.convertedAmount, payment.convertedCurrency) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Materials -->
            <div v-if="visit.materials.length > 0" class="space-y-2">
              <p class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">
                {{ t('treatmentPlans.detail.recorded.materialsTitle') }}
              </p>
              <div class="space-y-1">
                <div
                  v-for="material in visit.materials"
                  :key="material.id"
                  class="flex items-center justify-between text-sm"
                >
                  <span class="text-gray-600 dark:text-gray-300">
                    {{ material.materialName }} ({{ material.quantity }} units)
                  </span>
                  <span class="text-gray-900 dark:text-white">
                    {{ formatCurrency(material.convertedTotalCost, material.convertedCurrency) }}
                  </span>
                </div>
              </div>
              <div class="flex items-center justify-between text-xs font-semibold pt-2 border-t border-gray-200 dark:border-gray-700">
                <span class="text-gray-500 dark:text-gray-400">
                  {{ t('treatmentPlans.detail.recorded.materialsTotal') }}
                </span>
                <span class="text-red-600 dark:text-red-400">
                  {{ formatCurrency(visit.totalMaterialsCostThisVisit, plan.convertedCurrency) }}
                </span>
              </div>
            </div>
          </div>
        </div>

            <div v-else class="py-8 text-center">
              <div class="flex flex-col items-center gap-4">
                <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                  <UIcon name="i-lucide-clipboard-x" class="h-8 w-8 text-slate-400" />
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('treatmentPlans.detail.recorded.empty.title') }}</h3>
                  <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                    {{ t('treatmentPlans.detail.recorded.empty.subtitle') }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Additional Info -->
        <div class="grid gap-6 sm:grid-cols-2">
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-amber-500 to-orange-600 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-calculator" class="h-5 w-5 text-white" />
              <div>
                <h3 class="text-lg font-semibold text-white">{{ t('treatmentPlans.detail.financial.title') }}</h3>
                <p class="text-sm text-amber-100">{{ t('treatmentPlans.detail.financial.subtitle') }}</p>
              </div>
            </div>
          </div>
          <div class="p-6">
            <dl class="space-y-3 text-sm">
              <div class="flex justify-between items-center">
                <dt class="text-slate-500 dark:text-slate-400">{{ t('treatmentPlans.detail.financial.materials') }}</dt>
                <dd class="font-semibold text-red-600 dark:text-red-400">
                  {{ formatCurrency(plan.convertedTotalMaterialsCost, plan.convertedCurrency) }}
                </dd>
              </div>
              <div class="flex justify-between items-center">
                <dt class="text-slate-500 dark:text-slate-400">{{ t('treatmentPlans.detail.financial.revenue') }}</dt>
                <dd class="font-semibold text-green-600 dark:text-green-400">
                  {{ formatCurrency(plan.convertedNetRevenue, plan.convertedCurrency) }}
                </dd>
              </div>
              <div v-if="plan.discountAmount" class="flex justify-between items-center pt-3 border-t border-slate-200 dark:border-slate-700">
                <dt class="text-slate-500 dark:text-slate-400">{{ t('treatmentPlans.common.discountApplied') }}</dt>
                <dd class="font-semibold text-amber-600 dark:text-amber-400">
                  -{{ formatCurrency(plan.discountAmount, plan.convertedCurrency) }}
                </dd>
              </div>
              <div v-if="plan.discountReason" class="text-xs text-slate-500 dark:text-slate-400 bg-slate-50 dark:bg-slate-700/50 rounded-lg p-3">
                  <strong>{{ t('treatmentPlans.detail.financial.reason') }}:</strong> {{ plan.discountReason }}
              </div>
            </dl>
          </div>
        </div>

          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-info" class="h-5 w-5 text-white" />
              <div>
                  <h3 class="text-lg font-semibold text-white">{{ t('treatmentPlans.detail.planInfo.title') }}</h3>
                  <p class="text-sm text-slate-300">{{ t('treatmentPlans.detail.planInfo.subtitle') }}</p>
              </div>
            </div>
          </div>
          <div class="p-6">
            <dl class="space-y-3 text-sm">
              <div class="flex justify-between items-center">
                  <dt class="text-slate-500 dark:text-slate-400">{{ t('treatmentPlans.detail.planInfo.created') }}</dt>
                  <dd class="text-slate-900 dark:text-white font-medium">
                    {{ formatDate(plan.createdAt) }}
                  </dd>
                </div>
                <div v-if="plan.startedAt" class="flex justify-between items-center">
                  <dt class="text-slate-500 dark:text-slate-400">{{ t('treatmentPlans.detail.planInfo.started') }}</dt>
                  <dd class="text-slate-900 dark:text-white font-medium">
                    {{ formatDate(plan.startedAt) }}
                  </dd>
                </div>
                <div v-if="plan.completedAt" class="flex justify-between items-center">
                  <dt class="text-slate-500 dark:text-slate-400">{{ t('treatmentPlans.detail.planInfo.completed') }}</dt>
                  <dd class="text-slate-900 dark:text-white font-medium">
                    {{ formatDate(plan.completedAt) }}
                  </dd>
                </div>
                <div class="flex justify-between items-center">
                  <dt class="text-slate-500 dark:text-slate-400">{{ t('treatmentPlans.detail.planInfo.cadence') }}</dt>
                  <dd class="text-slate-900 dark:text-white font-medium">
                    {{ cadenceLabels[plan.followUpCadence] ?? plan.followUpCadence }}
                  </dd>
                </div>
                <div v-if="plan.notes" class="pt-3 border-t border-slate-200 dark:border-slate-700">
                  <dt class="text-slate-500 dark:text-slate-400 mb-2">{{ t('treatmentPlans.detail.planInfo.notes') }}</dt>
                  <dd class="text-slate-900 dark:text-white text-sm bg-slate-50 dark:bg-slate-700/50 rounded-lg p-3">
                    {{ plan.notes }}
                  </dd>
                </div>
              </dl>
            </div>
          </div>
        </div>
      </div>
    </template>
    <!-- Error State -->
    <div v-else class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900 flex items-center justify-center">
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 p-8 text-center max-w-md mx-auto">
        <div class="flex flex-col items-center gap-4">
          <div class="h-16 w-16 rounded-full bg-red-100 dark:bg-red-900/20 flex items-center justify-center">
            <UIcon name="i-lucide-alert-triangle" class="h-8 w-8 text-red-600 dark:text-red-400" />
          </div>
          <div>
            <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('treatmentPlans.detail.emptyState.title') }}</h3>
            <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
              {{ t('treatmentPlans.detail.emptyState.description') }}
            </p>
          </div>
          <UButton
            color="indigo"
            icon="i-lucide-arrow-left"
            @click="navigateTo('/treatment-plans')"
          >
            {{ t('treatmentPlans.detail.emptyState.action') }}
          </UButton>
        </div>
      </div>
    </div>

    <!-- Record Visit Modal -->
    <UModal v-model="showRecordVisitModal" :ui="{ width: 'sm:max-w-3xl' }">
      <UCard>
        <template #header>
          <div class="space-y-1">
            <h3 class="text-lg font-semibold text-slate-900">{{ recordVisitModalTitle }}</h3>
            <p class="text-sm text-slate-500">
              {{ recordVisitModalSubtitle }}
            </p>
          </div>
        </template>

        <form class="space-y-6" @submit.prevent="handleRecordVisit">
          <div class="grid gap-4 md:grid-cols-2">
            <UFormGroup :label="t('treatmentPlans.detail.recordVisitModal.form.visitDate')" required>
              <UInput
                v-model="recordVisitForm.visitDate"
                type="datetime-local"
                size="lg"
                :disabled="recordVisitLoading"
              />
            </UFormGroup>
            <UFormGroup :label="t('treatmentPlans.detail.recordVisitModal.form.performedProcedures')">
              <UTextarea
                v-model="recordVisitForm.performedProcedures"
                :rows="2"
                :disabled="recordVisitLoading"
                :placeholder="t('treatmentPlans.detail.recordVisitModal.form.performedProceduresPlaceholder')"
              />
            </UFormGroup>
          </div>

          <UFormGroup :label="t('treatmentPlans.detail.recordVisitModal.form.notes')">
            <UTextarea
              v-model="recordVisitForm.notes"
              :rows="3"
              :disabled="recordVisitLoading"
              :placeholder="t('treatmentPlans.detail.recordVisitModal.form.notesPlaceholder')"
            />
          </UFormGroup>

          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <h4 class="text-sm font-semibold text-slate-900">{{ t('treatmentPlans.detail.recordVisitModal.form.payments.title') }}</h4>
              <UButton
                size="xs"
                color="gray"
                variant="soft"
                icon="i-lucide-plus"
                :disabled="recordVisitLoading"
                @click.prevent="addPaymentRow"
              >
                {{ t('treatmentPlans.detail.recordVisitModal.form.payments.add') }}
              </UButton>
            </div>

            <div v-if="recordVisitForm.payments.length === 0" class="rounded-lg border border-dashed border-slate-200 p-4 text-sm text-slate-500">
              {{ t('treatmentPlans.detail.recordVisitModal.form.payments.empty') }}
            </div>

            <div
              v-for="(payment, index) in recordVisitForm.payments"
              :key="index"
              class="rounded-lg border border-slate-200 p-4 space-y-4"
            >
              <div class="flex items-start justify-between">
                <h5 class="text-sm font-medium text-slate-900">{{ t('treatmentPlans.detail.recordVisitModal.form.payments.itemTitle', { number: formatNumber(index + 1) }) }}</h5>
                <UButton
                  v-if="recordVisitForm.payments.length > 1"
                  icon="i-lucide-trash"
                  color="red"
                  variant="ghost"
                  size="xs"
                  :disabled="recordVisitLoading"
                  @click.prevent="removePaymentRow(index)"
                />
              </div>

              <div class="grid gap-4 md:grid-cols-2">
                <UFormGroup :label="t('treatmentPlans.detail.recordVisitModal.form.payments.amount')" required>
                  <UInput
                    v-model="payment.amount"
                    type="number"
                    step="0.01"
                    min="0"
                    :placeholder="t('treatmentPlans.detail.recordVisitModal.form.payments.amountPlaceholder')"
                    :disabled="recordVisitLoading"
                  />
                </UFormGroup>
                <UFormGroup :label="t('treatmentPlans.detail.recordVisitModal.form.payments.method')" required>
                  <USelect
                    v-model="payment.paymentMethod"
                    :options="paymentMethodOptions"
                    option-attribute="label"
                    value-attribute="value"
                    :disabled="recordVisitLoading"
                  />
                </UFormGroup>
              </div>

              <div class="grid gap-4 md:grid-cols-2">
                <UFormGroup :label="t('treatmentPlans.detail.recordVisitModal.form.payments.date')" required>
                  <UInput
                    v-model="payment.paymentDate"
                    type="datetime-local"
                    :disabled="recordVisitLoading"
                  />
                </UFormGroup>
                <UFormGroup :label="t('treatmentPlans.detail.recordVisitModal.form.payments.reference')">
                  <UInput
                    v-model="payment.transactionReference"
                    :placeholder="t('treatmentPlans.detail.recordVisitModal.form.payments.referencePlaceholder')"
                    :disabled="recordVisitLoading"
                  />
                </UFormGroup>
              </div>

              <UFormGroup :label="t('treatmentPlans.detail.recordVisitModal.form.payments.notes')">
                <UTextarea
                  v-model="payment.notes"
                  :rows="2"
                  :disabled="recordVisitLoading"
                  :placeholder="t('treatmentPlans.detail.recordVisitModal.form.payments.notesPlaceholder')"
                />
              </UFormGroup>
            </div>
          </div>

          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <h4 class="text-sm font-semibold text-slate-900">{{ t('treatmentPlans.detail.recordVisitModal.form.materials.title') }}</h4>
              <UButton
                size="xs"
                color="gray"
                variant="soft"
                icon="i-lucide-plus"
                :disabled="recordVisitLoading || (!!materialsError && !materialsLoaded)"
                @click.prevent="addMaterialRow"
              >
                {{ t('treatmentPlans.detail.recordVisitModal.form.materials.add') }}
              </UButton>
            </div>

            <UAlert
              v-if="materialsError"
              color="red"
              variant="soft"
              icon="i-lucide-alert-triangle"
              class="border border-red-100"
            >
              <template #description>
                {{ materialsError }}
              </template>
              <template #actions>
                <UButton size="xs" color="red" variant="soft" @click="loadMaterials">
                  {{ t('treatmentPlans.detail.recordVisitModal.form.materials.retry') }}
                </UButton>
              </template>
            </UAlert>

            <div v-if="recordVisitForm.materials.length === 0" class="rounded-lg border border-dashed border-slate-200 p-4 text-sm text-slate-500">
              {{ t('treatmentPlans.detail.recordVisitModal.form.materials.empty') }}
            </div>

            <div
              v-for="(material, index) in recordVisitForm.materials"
              :key="`material-${index}`"
              class="rounded-lg border border-slate-200 p-4 space-y-4"
            >
              <div class="flex items-start justify-between">
                <h5 class="text-sm font-medium text-slate-900">{{ t('treatmentPlans.detail.recordVisitModal.form.materials.itemTitle', { number: formatNumber(index + 1) }) }}</h5>
                <UButton
                  icon="i-lucide-trash"
                  color="red"
                  variant="ghost"
                  size="xs"
                  :disabled="recordVisitLoading"
                  @click.prevent="removeMaterialRow(index)"
                />
              </div>

              <div class="grid gap-4 md:grid-cols-2">
                <UFormGroup :label="t('treatmentPlans.detail.recordVisitModal.form.materials.material')" required>
                  <USelect
                    v-model="material.materialId"
                    :options="materialOptions"
                    option-attribute="label"
                    value-attribute="value"
                    :placeholder="t('treatmentPlans.detail.recordVisitModal.form.materials.materialPlaceholder')"
                    :disabled="recordVisitLoading || materialsLoading"
                  />
                </UFormGroup>
                <UFormGroup :label="t('treatmentPlans.detail.recordVisitModal.form.materials.quantity')" required>
                  <UInput
                    v-model="material.quantity"
                    type="number"
                    min="0"
                    step="0.01"
                    :placeholder="t('treatmentPlans.detail.recordVisitModal.form.materials.quantityPlaceholder')"
                    :disabled="recordVisitLoading"
                  />
                </UFormGroup>
              </div>

              <UFormGroup :label="t('treatmentPlans.detail.recordVisitModal.form.materials.notes')">
                <UTextarea
                  v-model="material.notes"
                  :rows="2"
                  :disabled="recordVisitLoading"
                  :placeholder="t('treatmentPlans.detail.recordVisitModal.form.materials.notesPlaceholder')"
                />
              </UFormGroup>
            </div>
          </div>

          <div class="flex justify-end gap-2 pt-4 border-t border-slate-200">
            <UButton type="button" color="gray" variant="soft" :disabled="recordVisitLoading" @click="showRecordVisitModal = false">
              {{ t('treatmentPlans.detail.recordVisitModal.actions.cancel') }}
            </UButton>
            <UButton
              type="submit"
              color="primary"
              :loading="recordVisitLoading"
              :disabled="!canSubmitVisit || recordVisitLoading"
            >
              {{ recordVisitSubmitLabel }}
            </UButton>
          </div>
        </form>
      </UCard>
    </UModal>

    <!-- Discount Modal (simplified placeholder) -->
    <UModal v-model="showDiscountModal">
      <UCard>
        <template #header>
          <h3 class="text-lg font-semibold">{{ t('treatmentPlans.detail.discountModal.title') }}</h3>
        </template>
        <p class="text-sm text-gray-500">
          {{ t('treatmentPlans.detail.discountModal.description') }}
        </p>
        <template #footer>
          <div class="flex justify-end gap-2">
            <UButton color="gray" variant="soft" @click="showDiscountModal = false">
              {{ t('treatmentPlans.detail.discountModal.cancel') }}
            </UButton>
            <UButton @click="showDiscountModal = false">
              {{ t('treatmentPlans.detail.discountModal.confirm') }}
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>

    <!-- Cancel Modal (simplified placeholder) -->
    <UModal v-model="showCancelModal">
      <UCard>
        <template #header>
          <h3 class="text-lg font-semibold">{{ t('treatmentPlans.detail.cancelModal.title') }}</h3>
        </template>
        <p class="text-sm text-gray-500">
          {{ t('treatmentPlans.detail.cancelModal.description') }}
        </p>
        <template #footer>
          <div class="flex justify-end gap-2">
            <UButton color="gray" variant="soft" @click="showCancelModal = false">
              {{ t('treatmentPlans.detail.cancelModal.cancel') }}
            </UButton>
            <UButton color="red" @click="showCancelModal = false">
              {{ t('treatmentPlans.detail.cancelModal.confirm') }}
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import type { TreatmentPlan, TreatmentPlanStatus, PaymentMethod, RecordVisitRequest, FollowUpVisit } from '~/composables/useTreatmentPlans'
import type { Material } from '~/composables/useMaterials'
import { useMaterials } from '~/composables/useMaterials'
import { watch } from 'vue'
import { onMounted } from 'vue'
import { computed } from 'vue'
import { reactive } from 'vue'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

definePageMeta({
  requiresAuth: true
})

const route = useRoute()
const treatmentPlansApi = useTreatmentPlans()
const materialsApi = useMaterials()
const toast = useEnhancedToast()
const { t, locale } = useI18n()

useHead(() => ({
  title: t('treatmentPlans.detail.meta.headTitle')
}))

const loading = ref(true)
const plan = ref<TreatmentPlan | null>(null)
const showRecordVisitModal = ref(false)
const showDiscountModal = ref(false)
const showCancelModal = ref(false)
const recordVisitLoading = ref(false)
const materialsLoading = ref(false)
const materialsLoaded = ref(false)
const materialsError = ref<string | null>(null)
const materials = ref<Material[]>([])
const editingVisitId = ref<number | null>(null)

const isEditingVisit = computed(() => editingVisitId.value !== null)
const recordVisitModalTitle = computed(() =>
  isEditingVisit.value
    ? t('treatmentPlans.detail.recordVisitModal.editTitle')
    : t('treatmentPlans.detail.recordVisitModal.title')
)
const recordVisitModalSubtitle = computed(() =>
  isEditingVisit.value
    ? t('treatmentPlans.detail.recordVisitModal.editSubtitle')
    : t('treatmentPlans.detail.recordVisitModal.subtitle')
)
const recordVisitSubmitLabel = computed(() =>
  isEditingVisit.value
    ? t('treatmentPlans.detail.recordVisitModal.actions.save')
    : t('treatmentPlans.detail.recordVisitModal.actions.submit')
)

const statusLabels = computed<Record<TreatmentPlanStatus, string>>(() => ({
  PLANNED: t('treatmentPlans.common.status.planned'),
  IN_PROGRESS: t('treatmentPlans.common.status.inProgress'),
  COMPLETED: t('treatmentPlans.common.status.completed'),
  CANCELLED: t('treatmentPlans.common.status.cancelled')
}))

const appointmentStatusLabels = computed<Record<string, string>>(() => ({
  SCHEDULED: t('treatmentPlans.detail.appointmentStatus.scheduled'),
  CONFIRMED: t('treatmentPlans.detail.appointmentStatus.confirmed'),
  COMPLETED: t('treatmentPlans.detail.appointmentStatus.completed'),
  CANCELLED: t('treatmentPlans.detail.appointmentStatus.cancelled')
}))

const bookingModeLabels = computed<Record<string, string>>(() => ({
  CLINIC_VISIT: t('treatmentPlans.common.bookingMode.clinic'),
  VIRTUAL_CONSULTATION: t('treatmentPlans.common.bookingMode.virtual')
}))

const cadenceLabels = computed<Record<string, string>>(() => ({
  WEEKLY: t('treatmentPlans.common.cadence.weekly'),
  MONTHLY: t('treatmentPlans.common.cadence.monthly')
}))

const paymentMethodOptions = computed(() => [
  { label: t('treatmentPlans.common.paymentMethods.cash'), value: 'CASH' as PaymentMethod },
  { label: t('treatmentPlans.common.paymentMethods.pos'), value: 'POS' as PaymentMethod },
  { label: t('treatmentPlans.common.paymentMethods.bankTransfer'), value: 'BANK_TRANSFER' as PaymentMethod },
  { label: t('treatmentPlans.common.paymentMethods.paypal'), value: 'PAYPAL' as PaymentMethod },
  { label: t('treatmentPlans.common.paymentMethods.other'), value: 'OTHER' as PaymentMethod }
])

const numberFormatter = computed(() => new Intl.NumberFormat(locale.value || undefined))
const formatNumber = (value: number | string | null | undefined) => {
  const numeric = Number(value ?? 0)
  if (Number.isNaN(numeric)) {
    return numberFormatter.value.format(0)
  }
  return numberFormatter.value.format(numeric)
}

const createPaymentRow = () => ({
  amount: '',
  paymentMethod: 'CASH' as PaymentMethod,
  paymentDate: formatDateTimeLocal(new Date()),
  transactionReference: '',
  notes: ''
})

const createMaterialRow = () => ({
  materialId: '',
  quantity: '1',
  notes: ''
})

const recordVisitForm = reactive<{
  visitDate: string
  notes: string
  performedProcedures: string
  payments: Array<{
    amount: string
    paymentMethod: PaymentMethod
    paymentDate: string
    transactionReference: string
    notes: string
  }>
  materials: Array<{ materialId: string; quantity: string; notes: string }>
  appointmentId?: number
}>({
  visitDate: formatDateTimeLocal(new Date()),
  notes: '',
  performedProcedures: '',
  payments: [createPaymentRow()],
  materials: []
})

const materialOptions = computed(() =>
  materials.value.map(material => ({
    label: material.name,
    value: material.id.toString(),
    unit: material.unitOfMeasure || 'unit',
    unitCost: material.unitCost
  }))
)

// Helper function to check if a visit has been recorded for an appointment
const isVisitRecorded = (appointmentId: number): boolean => {
  if (!plan.value) return false
  return plan.value.followUpVisits.some(visit => visit.appointmentId != null && visit.appointmentId === appointmentId)
}

// Open record visit modal pre-filled with appointment info
const openRecordVisitForAppointment = (appointment: any) => {
  editingVisitId.value = null
  resetRecordVisitForm()

  // Pre-fill the form with appointment data
  // Handle Unix timestamps (in seconds) and ISO strings
  let scheduledDate: Date;
  if (typeof appointment.scheduledAt === 'number') {
    // Unix timestamp: if less than 10 billion, it's in seconds (dates before 2286)
    const timestamp = appointment.scheduledAt < 10000000000 ? appointment.scheduledAt * 1000 : appointment.scheduledAt;
    scheduledDate = new Date(timestamp);
  } else {
    scheduledDate = new Date(appointment.scheduledAt);
  }
  recordVisitForm.visitDate = formatDateTimeLocal(scheduledDate)
  recordVisitForm.appointmentId = appointment.appointmentId

  // Open the modal
  showRecordVisitModal.value = true

  // Load materials if not already loaded
  if (!materialsLoaded.value) {
    loadMaterials()
  }
}

// Open record visit modal (generic, not linked to appointment)
const openRecordVisitModal = () => {
  // Reset the form
  editingVisitId.value = null
  resetRecordVisitForm()

  showRecordVisitModal.value = true

  if (!materialsLoaded.value) {
    loadMaterials()
  }
}

const openEditVisit = (visit: FollowUpVisit) => {
  editingVisitId.value = visit.id

  const visitDate = typeof visit.visitDate === 'number' ? new Date(visit.visitDate) : new Date(visit.visitDate)
  recordVisitForm.visitDate = formatDateTimeLocal(visitDate)
  recordVisitForm.notes = visit.notes ?? ''
  recordVisitForm.performedProcedures = visit.performedProcedures ?? ''

  if (visit.appointmentId) {
    recordVisitForm.appointmentId = visit.appointmentId
  } else {
    delete recordVisitForm.appointmentId
  }

  recordVisitForm.payments = visit.payments.length
    ? visit.payments.map(payment => ({
        amount: (payment.amount ?? 0).toString(),
        paymentMethod: payment.paymentMethod,
        paymentDate: formatDateTimeLocal(payment.paymentDate ? new Date(payment.paymentDate) : new Date()),
        transactionReference: payment.transactionReference ?? '',
        notes: payment.notes ?? ''
      }))
    : []

  recordVisitForm.materials = visit.materials.length
    ? visit.materials.map(material => ({
        materialId: material.materialId.toString(),
        quantity: material.quantity.toString(),
        notes: material.notes ?? ''
      }))
    : []

  showRecordVisitModal.value = true

  if (!materialsLoaded.value) {
    loadMaterials()
  }
}

const fetchPlan = async () => {
  try {
    loading.value = true
    const id = parseInt(route.params.id as string)
    plan.value = await treatmentPlansApi.getById(id)
  } catch (error: any) {
    toast.error(t('treatmentPlans.detail.toasts.loadPlanError'), error)
    navigateTo('/treatment-plans')
  } finally {
    loading.value = false
  }
}

const completePlan = async () => {
  if (!plan.value) return
  try {
    plan.value = await treatmentPlansApi.complete(plan.value.id)
    toast.success(t('treatmentPlans.detail.toasts.completeSuccess'))
  } catch (error: any) {
    toast.error(t('treatmentPlans.detail.toasts.completeError'), error)
  }
}

const loadMaterials = async () => {
  if (materialsLoading.value || materialsLoaded.value) {
    return
  }
  materialsLoading.value = true
  materialsError.value = null
  try {
    const response = await materialsApi.getAll(true)
    materials.value = response
    materialsLoaded.value = true
  } catch (error: any) {
    materialsError.value = error?.message ?? t('treatmentPlans.common.materials.loadError')
    toast.error(t('treatmentPlans.detail.toasts.materialsError'), error)
  } finally {
    materialsLoading.value = false
  }
}

const resetRecordVisitForm = () => {
  recordVisitForm.visitDate = formatDateTimeLocal(new Date())
  recordVisitForm.notes = ''
  recordVisitForm.performedProcedures = ''
  recordVisitForm.payments.splice(0, recordVisitForm.payments.length, createPaymentRow())
  recordVisitForm.materials.splice(0, recordVisitForm.materials.length)
  delete recordVisitForm.appointmentId
}

const addPaymentRow = () => {
  recordVisitForm.payments.push(createPaymentRow())
}

const removePaymentRow = (index: number) => {
  if (recordVisitForm.payments.length === 1) {
    recordVisitForm.payments[0] = createPaymentRow()
    return
  }
  recordVisitForm.payments.splice(index, 1)
}

const addMaterialRow = () => {
  recordVisitForm.materials.push(createMaterialRow())
}

const removeMaterialRow = (index: number) => {
  recordVisitForm.materials.splice(index, 1)
}

const canSubmitVisit = computed(() => {
  if (!plan.value) return false
  if (!recordVisitForm.visitDate) return false
  const hasValidPayment = recordVisitForm.payments.some(payment => {
    const amount = Number(payment.amount)
    return !Number.isNaN(amount) && amount > 0
  })
  const hasMaterials = recordVisitForm.materials.some(material => material.materialId && Number(material.quantity) > 0)
  return hasValidPayment || hasMaterials || recordVisitForm.notes.trim().length > 0 || recordVisitForm.performedProcedures.trim().length > 0
})

const handleRecordVisit = async () => {
  if (!plan.value || recordVisitLoading.value || !canSubmitVisit.value) {
    if (!canSubmitVisit.value) {
      toast.warning({
        title: t('treatmentPlans.detail.toasts.recordVisitIncompleteTitle'),
        description: t('treatmentPlans.detail.toasts.recordVisitIncompleteDescription')
      })
    }
    return
  }

  recordVisitLoading.value = true

  const payload: RecordVisitRequest = {
    appointmentId: recordVisitForm.appointmentId,
    visitDate: new Date(recordVisitForm.visitDate).toISOString(),
    notes: recordVisitForm.notes.trim() || undefined,
    performedProcedures: recordVisitForm.performedProcedures.trim() || undefined,
    payments: recordVisitForm.payments
      .map(payment => ({
        amount: Number(payment.amount),
        paymentMethod: payment.paymentMethod,
        transactionReference: payment.transactionReference.trim() || undefined,
        paymentDate: payment.paymentDate ? new Date(payment.paymentDate).toISOString() : new Date().toISOString(),
        notes: payment.notes.trim() || undefined
      }))
      .filter(payment => !Number.isNaN(payment.amount) && payment.amount > 0),
    materials: recordVisitForm.materials
      .map(material => ({
        materialId: Number(material.materialId),
        quantity: Number(material.quantity),
        notes: material.notes.trim() || undefined
      }))
      .filter(material => material.materialId > 0 && !Number.isNaN(material.quantity) && material.quantity > 0)
  }

  try {
    let updatedPlan: TreatmentPlan
    if (editingVisitId.value !== null) {
      updatedPlan = await treatmentPlansApi.updateVisit(plan.value.id, editingVisitId.value, payload)
      toast.success(t('treatmentPlans.detail.toasts.updateVisitSuccess'))
    } else {
      updatedPlan = await treatmentPlansApi.recordVisit(plan.value.id, payload)
      toast.success(t('treatmentPlans.detail.toasts.recordVisitSuccess'))
    }
    plan.value = updatedPlan
    showRecordVisitModal.value = false
    editingVisitId.value = null
    resetRecordVisitForm()
  } catch (error: any) {
    const message = editingVisitId.value !== null
      ? t('treatmentPlans.detail.toasts.updateVisitError')
      : t('treatmentPlans.detail.toasts.recordVisitError')
    toast.error(message, error)
  } finally {
    recordVisitLoading.value = false
  }
}

const getStatusColor = (status: TreatmentPlanStatus) => {
  const colors = {
    PLANNED: 'blue',
    IN_PROGRESS: 'amber',
    COMPLETED: 'green',
    CANCELLED: 'red'
  }
  return colors[status] || 'gray'
}

const getProgressColor = (plan: TreatmentPlan) => {
  const percentage = getPaymentProgressPercent(plan)
  if (percentage >= 80) return 'green'
  if (percentage >= 50) return 'amber'
  return 'red'
}

const getPaymentMethodColor = (method: PaymentMethod) => {
  const colors = {
    CASH: 'green',
    PAYPAL: 'blue',
    POS: 'purple',
    BANK_TRANSFER: 'indigo',
    OTHER: 'gray'
  }
  return colors[method] || 'gray'
}

const formatStatus = (status?: string) => {
  if (!status) return ''
  const treatmentLabel = statusLabels.value[status as TreatmentPlanStatus]
  if (treatmentLabel) return treatmentLabel
  const appointmentLabel = appointmentStatusLabels.value[status]
  if (appointmentLabel) return appointmentLabel
  return status
}

const formatMode = (mode?: string) => {
  if (!mode) return ''
  return bookingModeLabels.value[mode] ?? mode
}

const appointmentStatusColor = (status?: string) => {
  switch (status) {
    case 'CONFIRMED':
      return 'green'
    case 'COMPLETED':
      return 'blue'
    case 'CANCELLED':
      return 'red'
    case 'SCHEDULED':
    default:
      return 'violet'
  }
}

const attendanceStatusLabel = (value?: boolean | null) => {
  if (value === true) return t('treatmentPlans.common.attendance.attended')
  if (value === false) return t('treatmentPlans.common.attendance.noShow')
  return t('treatmentPlans.common.attendance.pending')
}

const attendanceStatusColor = (value?: boolean | null) => {
  if (value === true) return 'green'
  if (value === false) return 'red'
  return 'gray'
}

const attendanceStatusIcon = (value?: boolean | null) => {
  if (value === true) return 'i-lucide-user-check'
  if (value === false) return 'i-lucide-user-x'
  return 'i-lucide-user'
}

const formatCurrency = (amount: number, currency: string) => {
  return new Intl.NumberFormat(locale.value || undefined, {
    style: 'currency',
    currency: currency || 'JOD'
  }).format(amount ?? 0)
}

// Use clinic timezone for all date/time displays
// CRITICAL: All admins see times in clinic timezone, not their browser timezone
const { timezone, abbreviation } = useClinicTimezone();

const formatDateTime = (date?: string | number) => {
  if (!date) return t('treatmentPlans.common.notScheduled')
  // Use clinic timezone formatter with abbreviation (e.g., "Jan 15, 2024, 2:00 PM EET")
  return formatDateTimeInClinicTimezone(date, timezone.value, abbreviation.value);
}

function formatDateTimeLocal(date: Date) {
  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
  return offsetDate.toISOString().slice(0, 16)
}

const formatDate = (date: string | number) => {
  if (!date) return "—";
  // Use clinic timezone formatter from dateUtils
  return formatDateInClinicTimezone(date, timezone.value);
}

const getVisitProgressPercent = (currentPlan: TreatmentPlan) => {
  const totalVisits = currentPlan.plannedFollowups || 0
  if (!totalVisits) return 0
  const percentage = (currentPlan.completedVisits / totalVisits) * 100
  return Math.min(100, Math.max(0, percentage))
}

const getPaymentProgressPercent = (currentPlan: TreatmentPlan) => {
  const effectiveTotal = Math.max((currentPlan.totalPrice || 0) - (currentPlan.discountAmount || 0), 0)
  if (!effectiveTotal) {
    return currentPlan.totalPaid > 0 ? 100 : 0
  }
  const percentage = (currentPlan.totalPaid / effectiveTotal) * 100
  return Math.min(100, Math.max(0, percentage))
}

onMounted(() => {
  fetchPlan()
})

watch(showRecordVisitModal, (open) => {
  if (open) {
    if (!materialsLoaded.value) {
      loadMaterials()
    }
  } else if (!recordVisitLoading.value) {
    editingVisitId.value = null
    resetRecordVisitForm()
  }
})
</script>
