<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 shadow-lg">
              <UIcon name="i-lucide-clipboard-list" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('treatmentPlans.list.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('treatmentPlans.list.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="loading"
              @click="fetchPlans"
            >
              {{ t('treatmentPlans.list.actions.refresh') }}
            </UButton>
            <UButton 
              color="indigo" 
              icon="i-lucide-plus" 
              @click="openCreateModal"
            >
              {{ t('treatmentPlans.list.actions.create') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">

      <!-- Quick Stats -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-50 dark:bg-indigo-900/20">
              <UIcon name="i-lucide-clipboard-list" class="h-5 w-5 text-indigo-600 dark:text-indigo-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('treatmentPlans.list.metrics.total.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatNumber(stats.total) }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('treatmentPlans.list.metrics.total.caption') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
              <UIcon name="i-lucide-activity" class="h-5 w-5 text-green-600 dark:text-green-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('treatmentPlans.list.metrics.active.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatNumber(stats.active) }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('treatmentPlans.list.metrics.active.caption') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-50 dark:bg-purple-900/20">
              <UIcon name="i-lucide-check-circle" class="h-5 w-5 text-purple-600 dark:text-purple-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('treatmentPlans.list.metrics.completion.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatNumber(stats.completionRate) }}%</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('treatmentPlans.list.metrics.completion.caption', { completed: formatNumber(stats.completed) }) }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-900/20">
              <UIcon name="i-lucide-banknote" class="h-5 w-5 text-amber-600 dark:text-amber-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('treatmentPlans.list.metrics.revenue.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatCurrency(stats.totalRevenue, defaultCurrency) }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('treatmentPlans.list.metrics.revenue.caption') }}</p>
        </div>
      </div>

      <!-- Search and Filters -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden mb-6">
        <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-search" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('treatmentPlans.list.filters.title') }}</h2>
              <p class="text-sm text-slate-300">{{ t('treatmentPlans.list.filters.subtitle') }}</p>
            </div>
          </div>
        </div>
        <div class="p-6">
          <div class="flex flex-col sm:flex-row gap-4 mb-4">
            <div class="flex-1">
              <UFormGroup :label="t('treatmentPlans.list.filters.searchLabel')">
                <UInput
                  v-model="searchQuery"
                  size="lg"
                  :placeholder="t('treatmentPlans.list.filters.searchPlaceholder')"
                  icon="i-lucide-search"
                />
              </UFormGroup>
            </div>
            <div class="sm:w-48">
              <UFormGroup :label="t('treatmentPlans.list.filters.sortLabel')">
                <USelect
                  v-model="sortOption"
                  size="lg"
                  :options="sortOptions"
                  value-attribute="value"
                  option-attribute="label"
                />
              </UFormGroup>
            </div>
          </div>
          
          <div class="flex flex-wrap items-center gap-2">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('treatmentPlans.list.filters.statusLabel') }}</span>
            <UButton
              v-for="status in statusFilters"
              :key="status.value"
              size="xs"
              :variant="statusFilter === status.value ? 'solid' : 'soft'"
              :color="statusFilter === status.value ? 'indigo' : 'gray'"
              class="rounded-full"
              @click="toggleStatus(status.value)"
            >
              {{ status.label }}
            </UButton>
          </div>

          <div
            v-if="activeFiltersCount"
            class="flex items-center justify-between rounded-xl bg-indigo-50 dark:bg-indigo-900/20 px-4 py-2 text-xs text-indigo-700 dark:text-indigo-300 mt-4"
          >
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-filter" class="h-4 w-4" />
              <span>{{ t('treatmentPlans.list.filters.results', { count: formatNumber(filteredPlans.length) }) }}</span>
            </div>
            <UButton
              size="xs"
              variant="ghost"
              color="indigo"
              @click="clearFilters"
            >
              {{ t('treatmentPlans.list.filters.clear') }}
            </UButton>
          </div>
        </div>
      </div>

      <!-- Treatment Plans Grid -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-indigo-500 to-purple-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-clipboard-list" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('treatmentPlans.list.directory.title') }}</h2>
              <p class="text-sm text-indigo-100">{{ t('treatmentPlans.list.directory.count', { count: formatNumber(filteredPlans.length) }) }}</p>
            </div>
          </div>
        </div>

        <div v-if="loading" class="p-6">
          <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            <div v-for="i in 6" :key="i" class="animate-pulse">
              <div class="bg-slate-200 dark:bg-slate-700 rounded-xl p-6 space-y-4">
                <div class="flex items-center gap-4">
                  <div class="w-12 h-12 bg-slate-300 dark:bg-slate-600 rounded-xl"></div>
                  <div class="flex-1 space-y-2">
                    <div class="h-4 bg-slate-300 dark:bg-slate-600 rounded w-3/4"></div>
                    <div class="h-3 bg-slate-300 dark:bg-slate-600 rounded w-1/2"></div>
                  </div>
                </div>
                <div class="h-3 bg-slate-300 dark:bg-slate-600 rounded w-full"></div>
                <div class="h-3 bg-slate-300 dark:bg-slate-600 rounded w-2/3"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="filteredPlans.length > 0" class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-slate-50 dark:bg-slate-700/50">
              <tr>
                <th class="px-6 py-4 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  {{ t('treatmentPlans.list.table.columns.patient') }}
                </th>
                <th class="px-6 py-4 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  {{ t('treatmentPlans.list.table.columns.treatment') }}
                </th>
                <th class="px-6 py-4 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  {{ t('treatmentPlans.list.table.columns.doctor') }}
                </th>
                <th class="px-6 py-4 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  {{ t('treatmentPlans.list.table.columns.status') }}
                </th>
                <th class="px-6 py-4 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  {{ t('treatmentPlans.list.table.columns.progress') }}
                </th>
                <th class="px-6 py-4 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  {{ t('treatmentPlans.list.table.columns.total') }}
                </th>
                <th class="px-6 py-4 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  {{ t('treatmentPlans.list.table.columns.remaining') }}
                </th>
                <th class="px-6 py-4 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  {{ t('treatmentPlans.list.table.columns.visits') }}
                </th>
                <th class="px-6 py-4 text-left text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                  {{ t('treatmentPlans.list.table.columns.actions') }}
                </th>
              </tr>
            </thead>
            <tbody class="bg-white dark:bg-slate-800 divide-y divide-slate-200 dark:divide-slate-700">
              <tr
                v-for="plan in paginatedPlans"
                :key="plan.id"
                class="hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors duration-200 cursor-pointer"
                @click="navigateTo(`/treatment-plans/${plan.id}`)"
              >
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="relative">
                      <UAvatar :alt="plan.patientName" size="sm" class="ring-2 ring-slate-100 dark:ring-slate-600" />
                      <div
                        :class="[
                          'absolute -top-1 -right-1 w-3 h-3 rounded-full border-2 border-white dark:border-slate-700',
                          getStatusColor(plan.status) === 'green' ? 'bg-green-500' : 
                          getStatusColor(plan.status) === 'amber' ? 'bg-amber-500' :
                          getStatusColor(plan.status) === 'blue' ? 'bg-blue-500' : 'bg-red-500'
                        ]"
                      ></div>
                    </div>
                    <div class="ml-3">
                      <div class="text-sm font-medium text-slate-900 dark:text-white">
                        {{ plan.patientName }}
                      </div>
                      <div class="text-xs text-slate-500 dark:text-slate-400">
                        {{ formatDate(plan.createdAt) }}
                      </div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-slate-900 dark:text-white">{{ plan.treatmentTypeName }}</div>
                  <div class="text-xs text-slate-500 dark:text-slate-400">
                    {{ t('treatmentPlans.list.table.followUpFrequency', { cadence: formatCadence(plan.followUpCadence) }) }}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-slate-900 dark:text-white">{{ t('treatmentPlans.list.table.doctorName', { name: plan.doctorName }) }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <UBadge
                    :color="getStatusColor(plan.status)"
                    variant="soft"
                    size="sm"
                  >
                    {{ formatStatus(plan.status) }}
                  </UBadge>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="w-full max-w-xs">
                    <div class="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400 mb-1">
                      <span>{{ Math.round(getPaymentProgress(plan)) }}%</span>
                      <span>{{ formatCurrency(plan.convertedTotalPaid, plan.convertedCurrency) }}</span>
                    </div>
                    <UProgress
                      :value="getPaymentProgress(plan)"
                      :color="getProgressColor(plan)"
                      size="sm"
                      class="rounded-full"
                    />
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm font-semibold text-slate-900 dark:text-white">
                    {{ formatCurrency(plan.convertedTotalPrice, plan.convertedCurrency) }}
                  </div>
                  <div v-if="plan.discountAmount" class="text-xs text-amber-600 dark:text-amber-400">
                    <UIcon name="i-lucide-tag" class="h-3 w-3 inline mr-1" />
                    {{ t('treatmentPlans.common.discountApplied') }}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm font-semibold text-red-600 dark:text-red-400">
                    {{ formatCurrency(plan.convertedRemainingBalance, plan.convertedCurrency) }}
                  </div>
                  <div v-if="plan.remainingBalance === 0" class="text-xs text-green-600 dark:text-green-400">
                    <UIcon name="i-lucide-check-circle" class="h-3 w-3 inline mr-1" />
                    {{ t('treatmentPlans.common.fullyPaid') }}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-slate-900 dark:text-white">
                    {{ formatNumber(plan.completedVisits) }}/{{ formatNumber(plan.plannedFollowups) }}
                  </div>
                  <div class="text-xs text-slate-500 dark:text-slate-400">
                    {{ t('treatmentPlans.common.remainingVisits', { count: formatNumber(Math.max(plan.plannedFollowups - plan.completedVisits, 0)) }) }}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex items-center gap-2">
                    <UButton
                      size="sm"
                      variant="ghost"
                      color="indigo"
                      icon="i-lucide-eye"
                      @click.stop="navigateTo(`/treatment-plans/${plan.id}`)"
                    >
                      {{ t('treatmentPlans.list.actions.view') }}
                    </UButton>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="p-12 text-center">
          <div class="flex flex-col items-center gap-4">
            <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
              <UIcon name="i-lucide-clipboard-list" class="h-8 w-8 text-slate-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('treatmentPlans.list.empty.title') }}</h3>
              <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                {{
                  searchQuery || statusFilter
                    ? t('treatmentPlans.list.empty.searchHint')
                    : t('treatmentPlans.list.empty.subtitle')
                }}
              </p>
            </div>
            <UButton
              v-if="!searchQuery && !statusFilter"
              color="indigo"
              icon="i-lucide-plus"
              @click="openCreateModal"
            >
              {{ t('treatmentPlans.list.actions.create') }}
            </UButton>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="filteredPlans.length > pageSize" class="px-6 py-4 border-t border-slate-200 dark:border-slate-700">
          <div class="flex justify-center">
            <UPagination
              v-model="currentPage"
              :page-count="pageSize"
              :total="filteredPlans.length"
            />
          </div>
        </div>
      </div>
    </div>
    <!-- Create Treatment Plan Modal -->
    <UModal v-model="showCreateModal" :ui="{ width: 'sm:max-w-3xl' }">
      <div class="relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800">
        <!-- Header with Gradient -->
        <div class="relative overflow-hidden bg-gradient-to-br from-indigo-600 via-indigo-700 to-purple-800 p-6 text-white">
          <div class="absolute -right-12 -top-12 h-40 w-40 rounded-full bg-white/10 blur-3xl"></div>
          <div class="absolute -bottom-8 -left-8 h-32 w-32 rounded-full bg-indigo-400/20 blur-2xl"></div>

          <div class="relative">
            <div class="flex items-center gap-3 mb-2">
              <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-white/20 backdrop-blur">
                <UIcon name="i-lucide-clipboard-plus" class="h-6 w-6" />
              </div>
              <div>
                <h3 class="text-xl font-bold">{{ t('treatmentPlans.create.modal.title') }}</h3>
                <p class="text-sm text-indigo-100 mt-0.5">
                  {{ t('treatmentPlans.create.modal.subtitle') }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Form Content -->
        <form class="p-6" @submit.prevent="handleCreatePlan">
          <div v-if="referenceLoading" class="space-y-4">
            <USkeleton class="h-12 rounded-xl" />
            <USkeleton class="h-12 rounded-xl" />
            <USkeleton class="h-32 rounded-xl" />
          </div>

          <template v-else>
            <UAlert
              v-if="referenceError"
              color="red"
              variant="soft"
              icon="i-lucide-alert-triangle"
              class="mb-6 border border-red-200"
            >
              <template #title>{{ t('treatmentPlans.create.toasts.referenceError.title') }}</template>
              <template #description>
                {{ referenceError }}
              </template>
              <template #actions>
                <UButton size="xs" color="red" variant="soft" @click="retryReferenceLoad">
                  <UIcon name="i-lucide-refresh-cw" class="h-3 w-3 mr-1" />
                  {{ t('treatmentPlans.create.actions.retry') }}
                </UButton>
              </template>
            </UAlert>

            <div v-else class="space-y-6">
              <!-- Patient & Doctor Section -->
              <div class="rounded-xl bg-slate-50 dark:bg-slate-700/50 p-5 border border-slate-200 dark:border-slate-600">
                <div class="flex items-center gap-2 mb-4">
                  <UIcon name="i-lucide-users" class="h-5 w-5 text-indigo-600" />
                  <h4 class="font-semibold text-slate-900 dark:text-white">{{ t('treatmentPlans.create.sections.participants.title') }}</h4>
                </div>
                <div class="grid gap-4 md:grid-cols-2">
                  <UFormGroup :label="t('treatmentPlans.create.form.fields.patient')" required>
                    <USelect
                      v-model="createForm.patientId"
                      :options="patientOptions"
                      option-attribute="label"
                      value-attribute="value"
                      :placeholder="t('treatmentPlans.create.form.placeholders.patient')"
                      :disabled="!patientOptions.length"
                      size="lg"
                      :ui="selectPaddingUi"
                    >
                      <template #leading>
                        <UIcon name="i-lucide-user" class="h-4 w-4 text-slate-400" />
                      </template>
                    </USelect>
                    <template #help v-if="!patientOptions.length">
                      <div class="flex items-center gap-2 text-xs text-amber-600">
                        <UIcon name="i-lucide-info" class="h-3.5 w-3.5" />
                        <span>{{ t('treatmentPlans.create.form.help.addPatient') }}</span>
                      </div>
                    </template>
                  </UFormGroup>

                  <UFormGroup :label="t('treatmentPlans.create.form.fields.doctor')" required>
                    <USelect
                      v-model="createForm.doctorId"
                      :options="doctorOptions"
                      option-attribute="label"
                      value-attribute="value"
                      :placeholder="t('treatmentPlans.create.form.placeholders.doctor')"
                      :disabled="!doctorOptions.length"
                      size="lg"
                      :ui="selectPaddingUi"
                    >
                      <template #leading>
                        <UIcon name="i-lucide-stethoscope" class="h-4 w-4 text-slate-400" />
                      </template>
                    </USelect>
                    <template #help v-if="createForm.serviceId && !doctorOptions.length">
                      <div class="flex items-center gap-2 text-xs text-amber-600">
                        <UIcon name="i-lucide-alert-triangle" class="h-3.5 w-3.5" />
                        <span>{{ t('treatmentPlans.create.form.help.noDoctorForTreatment') }}</span>
                      </div>
                    </template>
                  </UFormGroup>
                </div>
              </div>

              <!-- Treatment & Pricing Section -->
              <div class="rounded-xl bg-slate-50 dark:bg-slate-700/50 p-5 border border-slate-200 dark:border-slate-600">
                <div class="flex items-center gap-2 mb-4">
                  <UIcon name="i-lucide-heart-pulse" class="h-5 w-5 text-indigo-600" />
                  <h4 class="font-semibold text-slate-900 dark:text-white">{{ t('treatmentPlans.create.sections.treatment.title') }}</h4>
                </div>
                <div class="grid gap-4 md:grid-cols-2">
                  <UFormGroup :label="t('treatmentPlans.create.form.fields.treatmentType')" required>
                    <USelect
                      v-model="createForm.serviceId"
                      :options="serviceOptions"
                      option-attribute="label"
                      value-attribute="value"
                      :placeholder="t('treatmentPlans.create.form.placeholders.treatmentType')"
                      :disabled="!serviceOptions.length"
                      size="lg"
                      :ui="selectPaddingUi"
                    >
                      <template #leading>
                        <UIcon name="i-lucide-activity" class="h-4 w-4 text-slate-400" />
                      </template>
                    </USelect>
                  </UFormGroup>

                  <UFormGroup :label="t('treatmentPlans.create.form.fields.totalPrice')" required>
                    <UInput
                      v-model="createForm.totalPrice"
                      type="number"
                      min="0"
                      step="0.01"
                      :placeholder="t('treatmentPlans.create.form.placeholders.totalPrice')"
                      size="lg"
                    >
                      <template #leading>
                        <span class="text-slate-400 text-sm font-medium">{{ createForm.currency }}</span>
                      </template>
                    </UInput>
                  </UFormGroup>
                </div>

                <div class="mt-4">
                  <UFormGroup :label="t('treatmentPlans.create.form.fields.currency')" required>
                    <div class="grid grid-cols-3 gap-2">
                      <button
                        v-for="curr in currencyOptions"
                        :key="curr.value"
                        type="button"
                        @click="createForm.currency = curr.value"
                        :class="[
                          'px-4 py-3 rounded-lg border-2 text-sm font-medium transition-all',
                          createForm.currency === curr.value
                            ? 'border-indigo-600 bg-indigo-50 text-indigo-700 dark:bg-indigo-900/20 dark:text-indigo-300'
                            : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-300'
                        ]"
                      >
                        {{ curr.label }}
                      </button>
                    </div>
                  </UFormGroup>
                </div>
              </div>

              <!-- Schedule Section -->
              <div class="rounded-xl bg-slate-50 dark:bg-slate-700/50 p-5 border border-slate-200 dark:border-slate-600">
                <div class="flex items-center gap-2 mb-4">
                  <UIcon name="i-lucide-calendar-clock" class="h-5 w-5 text-indigo-600" />
                  <h4 class="font-semibold text-slate-900 dark:text-white">{{ t('treatmentPlans.create.sections.schedule.title') }}</h4>
                </div>
                <div class="grid gap-4 md:grid-cols-2">
                  <UFormGroup :label="t('treatmentPlans.create.form.fields.followupCount')" required>
                    <UInput
                      v-model="createForm.plannedFollowups"
                      type="number"
                      min="0"
                      step="1"
                      :placeholder="t('treatmentPlans.create.form.placeholders.followupCount')"
                      size="lg"
                    >
                      <template #leading>
                        <UIcon name="i-lucide-hash" class="h-4 w-4 text-slate-400" />
                      </template>
                    </UInput>
                    <template #hint>
                      <span class="text-xs text-slate-500">{{ t('treatmentPlans.create.form.hints.followupCount') }}</span>
                    </template>
                  </UFormGroup>

                  <UFormGroup :label="t('treatmentPlans.create.form.fields.followupCadence')" required>
                    <div class="grid grid-cols-2 gap-2">
                      <button
                        v-for="cadence in followUpCadenceOptions"
                        :key="cadence.value"
                        type="button"
                        @click="createForm.followUpCadence = cadence.value"
                        :class="[
                          'px-4 py-3 rounded-lg border-2 text-sm font-medium transition-all',
                          createForm.followUpCadence === cadence.value
                            ? 'border-indigo-600 bg-indigo-50 text-indigo-700 dark:bg-indigo-900/20 dark:text-indigo-300'
                            : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-300'
                        ]"
                      >
                        {{ cadence.label }}
                      </button>
                    </div>
                  </UFormGroup>
                </div>
              </div>

              <!-- Notes Section -->
              <div class="rounded-xl bg-slate-50 dark:bg-slate-700/50 p-5 border border-slate-200 dark:border-slate-600">
                <div class="flex items-center gap-2 mb-4">
                  <UIcon name="i-lucide-file-text" class="h-5 w-5 text-indigo-600" />
                  <h4 class="font-semibold text-slate-900 dark:text-white">{{ t('treatmentPlans.create.sections.notes.title') }}</h4>
                </div>
                <UFormGroup
                  :label="t('treatmentPlans.create.form.fields.notes')"
                  :hint="t('treatmentPlans.create.form.hints.notes')"
                >
                  <UTextarea
                    v-model="createForm.notes"
                    :rows="4"
                    :placeholder="t('treatmentPlans.create.form.placeholders.notes')"
                    size="lg"
                  />
                </UFormGroup>
              </div>

              <!-- Summary Preview -->
              <div v-if="createForm.patientId && createForm.totalPrice" class="rounded-xl bg-gradient-to-br from-indigo-50 to-purple-50 dark:from-indigo-900/20 dark:to-purple-900/20 p-5 border-2 border-indigo-200 dark:border-indigo-700">
                <div class="flex items-center gap-2 mb-3">
                  <UIcon name="i-lucide-sparkles" class="h-5 w-5 text-indigo-600" />
                  <h4 class="font-semibold text-indigo-900 dark:text-indigo-300">{{ t('treatmentPlans.create.sections.summary.title') }}</h4>
                </div>
                <div class="grid gap-3 text-sm">
                  <div class="flex items-center justify-between">
                    <span class="text-slate-600 dark:text-slate-300">{{ t('treatmentPlans.common.patientLabel') }}:</span>
                    <span class="font-semibold text-slate-900 dark:text-white">{{ patientOptions.find(p => p.value === createForm.patientId)?.label || t('treatmentPlans.common.notAvailable') }}</span>
                  </div>
                  <div class="flex items-center justify-between">
                    <span class="text-slate-600 dark:text-slate-300">{{ t('treatmentPlans.common.totalCost') }}:</span>
                    <span class="font-bold text-indigo-600 dark:text-indigo-400 text-lg">{{ formatCurrency(Number(createForm.totalPrice) || 0, createForm.currency) }}</span>
                  </div>
                  <div v-if="createForm.plannedFollowups && Number(createForm.plannedFollowups) > 0" class="flex items-center justify-between">
                    <span class="text-slate-600 dark:text-slate-300">{{ t('treatmentPlans.common.expectedPerVisit') }}:</span>
                    <span class="font-semibold text-slate-700 dark:text-slate-300">{{ formatCurrency((Number(createForm.totalPrice) || 0) / Number(createForm.plannedFollowups), createForm.currency) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <!-- Footer Actions -->
          <div class="flex justify-end gap-3 pt-6 mt-6 border-t border-slate-200 dark:border-slate-700">
            <UButton
              type="button"
              color="gray"
              variant="soft"
              size="lg"
              :disabled="creatingPlan"
              @click="cancelCreate"
            >
              {{ t('common.actions.cancel') }}
            </UButton>
            <UButton
              type="submit"
              color="indigo"
              size="lg"
              :loading="creatingPlan"
              :disabled="!canSubmitPlan || !!referenceError || referenceLoading"
            >
              <UIcon v-if="!creatingPlan" name="i-lucide-check" class="h-5 w-5 mr-2" />
              {{ t('treatmentPlans.create.actions.submit') }}
            </UButton>
          </div>
        </form>
      </div>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import type { TreatmentPlan, TreatmentPlanStatus, CreateTreatmentPlanRequest } from '~/composables/useTreatmentPlans'
import type { PatientAdmin } from '~/types/patients'
import type { DoctorAdmin } from '~/types/doctors'
import type { AdminServiceSummary } from '~/types/services'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useClinicSettings } from '~/composables/useClinicSettings'

definePageMeta({
  requiresAuth: true
})

const { t, locale } = useI18n()

useHead(() => ({
  title: t('treatmentPlans.list.meta.headTitle')
}))

const treatmentPlansApi = useTreatmentPlans()
const toast = useToast()
const { request } = useAdminApi()

const referenceLoading = ref(false)
const referenceLoaded = ref(false)
const referenceError = ref<string | null>(null)

const patients = ref<PatientAdmin[]>([])
const doctors = ref<DoctorAdmin[]>([])
const services = ref<AdminServiceSummary[]>([])

const createForm = reactive({
  patientId: '',
  doctorId: '',
  serviceId: '',
  totalPrice: '',
  currency: 'JOD',
  plannedFollowups: '0',
  followUpCadence: 'WEEKLY' as 'WEEKLY' | 'MONTHLY',
  notes: ''
})

const creatingPlan = ref(false)
const selectPaddingUi = {
  trailing: {
    padding: {
      sm: 'pe-12',
      md: 'pe-12',
      lg: 'pe-12'
    }
  }
}

const loading = ref(true)
const plans = ref<TreatmentPlan[]>([])
const searchQuery = ref('')
const statusFilter = ref<TreatmentPlanStatus | null>(null)
const sortOption = ref<'newest' | 'value' | 'outstanding'>('newest')
const showCreateModal = ref(false)
const currentPage = ref(1)
const pageSize = 10

const statusLabels = computed<Record<TreatmentPlanStatus, string>>(() => ({
  PLANNED: t('treatmentPlans.common.status.planned'),
  IN_PROGRESS: t('treatmentPlans.common.status.inProgress'),
  COMPLETED: t('treatmentPlans.common.status.completed'),
  CANCELLED: t('treatmentPlans.common.status.cancelled')
}))

const statusFilters = computed(() => [
  { label: statusLabels.value.PLANNED, value: 'PLANNED' as TreatmentPlanStatus },
  { label: statusLabels.value.IN_PROGRESS, value: 'IN_PROGRESS' as TreatmentPlanStatus },
  { label: statusLabels.value.COMPLETED, value: 'COMPLETED' as TreatmentPlanStatus },
  { label: statusLabels.value.CANCELLED, value: 'CANCELLED' as TreatmentPlanStatus }
])

const sortOptions = computed(() => [
  { value: 'newest', label: t('treatmentPlans.list.sort.newest'), icon: 'i-lucide-sparkles' },
  { value: 'value', label: t('treatmentPlans.list.sort.value'), icon: 'i-lucide-badge-dollar-sign' },
  { value: 'outstanding', label: t('treatmentPlans.list.sort.outstanding'), icon: 'i-lucide-piggy-bank' }
] as const)

const defaultCurrency = computed(() => plans.value[0]?.currency ?? 'JOD')

const numberFormatter = computed(() => new Intl.NumberFormat(locale.value || undefined))
const formatNumber = (value: number) => numberFormatter.value.format(Math.max(0, Math.round(value ?? 0)))

const currencyFormatter = computed(() => {
  return (amount: number, currency: string) =>
    new Intl.NumberFormat(locale.value || undefined, {
      style: 'currency',
      currency: currency || defaultCurrency.value || 'JOD'
    }).format(amount ?? 0)
})
const formatCurrency = (amount: number, currency: string) => currencyFormatter.value(amount, currency)

const dateFormatter = computed(() => new Intl.DateTimeFormat(locale.value || undefined, { dateStyle: 'medium' }))
const formatDate = (value: string | null | undefined) => {
  if (!value) {
    return t('treatmentPlans.common.notAvailable')
  }
  const date = new Date(value)
  return Number.isNaN(date.getTime())
    ? t('treatmentPlans.common.notAvailable')
    : dateFormatter.value.format(date)
}

const cadenceLabels = computed(() => ({
  WEEKLY: t('treatmentPlans.common.cadence.weekly'),
  MONTHLY: t('treatmentPlans.common.cadence.monthly')
}))

const formatCadence = (cadence: 'WEEKLY' | 'MONTHLY') => cadenceLabels.value[cadence] ?? cadence

const currencyOptions = computed(() => [
  { label: t('treatmentPlans.create.form.currency.options.jod'), value: 'JOD' },
  { label: t('treatmentPlans.create.form.currency.options.usd'), value: 'USD' },
  { label: t('treatmentPlans.create.form.currency.options.aed'), value: 'AED' }
])

const followUpCadenceOptions = computed(() => [
  { label: cadenceLabels.value.WEEKLY, value: 'WEEKLY' as const },
  { label: cadenceLabels.value.MONTHLY, value: 'MONTHLY' as const }
])

const stats = computed(() => {
  let planned = 0
  let inProgress = 0
  let completed = 0
  let cancelled = 0
  let totalRevenue = 0
  let outstandingBalance = 0
  let totalPlanValue = 0
  let fullyPaid = 0
  let upcomingVisits = 0

  for (const plan of plans.value) {
    switch (plan.status) {
      case 'PLANNED':
        planned += 1
        break
      case 'IN_PROGRESS':
        inProgress += 1
        break
      case 'COMPLETED':
        completed += 1
        break
      case 'CANCELLED':
        cancelled += 1
        break
    }

    totalRevenue += plan.totalPaid ?? 0

    const remainingBalance = plan.remainingBalance ?? 0
    if (remainingBalance <= 0) {
      fullyPaid += 1
    } else {
      outstandingBalance += remainingBalance
    }

    totalPlanValue += plan.totalPrice ?? 0

    const remainingVisits = plan.remainingVisits ?? Math.max(plan.plannedFollowups - plan.completedVisits, 0)
    upcomingVisits += remainingVisits > 0 ? remainingVisits : 0
  }

  const total = plans.value.length
  const active = planned + inProgress
  const averagePlanValue = total ? totalPlanValue / total : 0
  const completionRate = total ? Math.round((completed / total) * 100) : 0

  return {
    total,
    planned,
    inProgress,
    completed,
    cancelled,
    active,
    totalRevenue,
    outstandingBalance,
    averagePlanValue,
    completionRate,
    fullyPaid,
    upcomingVisits
  }
})

const activeFiltersCount = computed(() => {
  let count = 0
  if (searchQuery.value) count += 1
  if (statusFilter.value) count += 1
  return count
})

function extractItems<T>(value: any): T[] {
  if (Array.isArray(value)) {
    return value as T[]
  }
  if (value && Array.isArray((value as any).content)) {
    return (value as any).content as T[]
  }
  if (value && Array.isArray((value as any).data)) {
    return (value as any).data as T[]
  }
  return []
}

const patientOptions = computed(() =>
  patients.value.map(patient => ({
    label: `${patient.firstName} ${patient.lastName}`,
    value: patient.id.toString()
  }))
)

const serviceOptions = computed(() =>
  services.value.map(service => ({
    label: service.nameEn ?? service.nameAr ?? service.slug,
    value: service.id.toString()
  }))
)

const doctorOptions = computed(() => {
  const selectedService = createForm.serviceId ? Number(createForm.serviceId) : null
  return doctors.value
    .filter(doctor =>
      selectedService ? doctor.services.some(service => service.id === selectedService) : true
    )
    .map(doctor => ({
      label: doctor.fullNameEn || doctor.fullNameAr || (doctor as any).fullName,
      value: doctor.id.toString()
    }))
})

const canSubmitPlan = computed(() => {
  const totalPrice = Number(createForm.totalPrice)
  const followups = Number(createForm.plannedFollowups)
  return Boolean(
    createForm.patientId &&
    createForm.doctorId &&
    createForm.serviceId &&
    createForm.currency &&
    createForm.followUpCadence &&
    !Number.isNaN(totalPrice) &&
    totalPrice > 0 &&
    !Number.isNaN(followups) &&
    followups >= 0
  )
})

const toTime = (value?: string) => {
  if (!value) {
    return 0
  }
  const time = new Date(value).getTime()
  return Number.isNaN(time) ? 0 : time
}

const filteredPlans = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()

  let filtered = plans.value

  if (statusFilter.value) {
    filtered = filtered.filter(plan => plan.status === statusFilter.value)
  }

  if (query) {
    filtered = filtered.filter(plan =>
      [plan.patientName, plan.treatmentTypeName, plan.doctorName]
        .filter(Boolean)
        .some(field => field.toLowerCase().includes(query))
    )
  }

  const sorted = [...filtered].sort((a, b) => {
    switch (sortOption.value) {
      case 'value':
        return (b.totalPrice ?? 0) - (a.totalPrice ?? 0)
      case 'outstanding':
        return (b.remainingBalance ?? 0) - (a.remainingBalance ?? 0)
      default:
        return toTime(b.createdAt) - toTime(a.createdAt)
    }
  })

  return sorted
})

const paginatedPlans = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredPlans.value.slice(start, end)
})

const fetchPlans = async () => {
  try {
    loading.value = true
    plans.value = await treatmentPlansApi.getAll()
  } catch (error: any) {
    toast.add({
      title: t('treatmentPlans.list.toasts.loadError.title'),
      description: error?.message ?? t('treatmentPlans.list.toasts.loadError.description'),
      color: "red",
      icon: "i-lucide-alert-circle"
    })
  } finally {
    loading.value = false
  }
}

const toggleStatus = (value: TreatmentPlanStatus) => {
  statusFilter.value = statusFilter.value === value ? null : value
}

const clearFilters = () => {
  searchQuery.value = ''
  statusFilter.value = null
  sortOption.value = 'newest'
}

const formatStatus = (status: TreatmentPlanStatus | null) => {
  if (!status) {
    return t('treatmentPlans.common.statusAll')
  }
  return statusLabels.value[status] ?? status
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

const getPaymentProgress = (plan: TreatmentPlan) => {
  const total = plan.totalPrice - (plan.discountAmount || 0)
  if (total === 0) return 0
  return Math.min(100, (plan.totalPaid / total) * 100)
}

const getProgressColor = (plan: TreatmentPlan) => {
  const percentage = getPaymentProgress(plan)
  if (percentage >= 80) return 'green'
  if (percentage >= 50) return 'amber'
  return 'red'
}

const loadReferenceData = async () => {
  if (referenceLoading.value || referenceLoaded.value) {
    return
  }

  referenceLoading.value = true
  referenceError.value = null

  try {
    const [patientsRes, doctorsRes, servicesRes] = await Promise.all([
      request<any>('/patients?size=200'),
      request<any>('/doctors?size=200'),
      request<any>('/services?size=200')
    ])

    patients.value = extractItems<PatientAdmin>(patientsRes)
    doctors.value = extractItems<DoctorAdmin>(doctorsRes)
    services.value = extractItems<AdminServiceSummary>(servicesRes)

    referenceLoaded.value = true
  } catch (error: any) {
    referenceError.value = error?.message ?? t('treatmentPlans.create.toasts.referenceError.description')
    toast.add({
      title: t('treatmentPlans.create.toasts.referenceError.title'),
      description: referenceError.value,
      color: "red",
      icon: "i-lucide-alert-circle"
    })
  } finally {
    referenceLoading.value = false
  }
}

const resetCreateForm = () => {
  createForm.patientId = ''
  createForm.doctorId = ''
  createForm.serviceId = ''
  createForm.totalPrice = ''
  createForm.currency = 'JOD'
  createForm.plannedFollowups = '0'
  createForm.followUpCadence = 'WEEKLY'
  createForm.notes = ''
}

const handleCreatePlan = async () => {
  if (!canSubmitPlan.value || creatingPlan.value) {
    if (!canSubmitPlan.value) {
      toast.add({
        title: t('treatmentPlans.create.toasts.validationError.title'),
        description: t('treatmentPlans.create.toasts.validationError.description'),
        color: "amber",
        icon: "i-lucide-alert-triangle"
      })
    }
    return
  }

  creatingPlan.value = true

  const payload: CreateTreatmentPlanRequest = {
    patientId: Number(createForm.patientId),
    doctorId: Number(createForm.doctorId),
    treatmentTypeId: Number(createForm.serviceId),
    totalPrice: Number(createForm.totalPrice),
    currency: createForm.currency,
    plannedFollowups: Number(createForm.plannedFollowups),
    followUpCadence: createForm.followUpCadence,
    notes: createForm.notes.trim() || undefined
  }

  try {
    const plan = await treatmentPlansApi.create(payload)
    toast.add({
      title: t('treatmentPlans.create.toasts.success.title'),
      description: t('treatmentPlans.create.toasts.success.description'),
      color: "green",
      icon: "i-lucide-check-circle"
    })
    showCreateModal.value = false
    plans.value = [plan, ...plans.value.filter(existing => existing.id !== plan.id)]

    await navigateTo(`/treatment-plans/${plan.id}`)
  } catch (error: any) {
    toast.add({
      title: t('treatmentPlans.create.toasts.error.title'),
      description: error?.data?.message ?? error?.message ?? t('treatmentPlans.create.toasts.error.description'),
      color: "red",
      icon: "i-lucide-alert-circle"
    })
  } finally {
    creatingPlan.value = false
  }
}

const openCreateModal = () => {
  showCreateModal.value = true
}

const cancelCreate = () => {
  showCreateModal.value = false
}

const retryReferenceLoad = () => {
  referenceLoaded.value = false
  loadReferenceData()
}

watch(() => createForm.serviceId, () => {
  if (createForm.doctorId && !doctorOptions.value.some(option => option.value === createForm.doctorId)) {
    createForm.doctorId = ''
  }
})

watch(() => showCreateModal.value, (open) => {
  if (open) {
    loadReferenceData()
  } else {
    resetCreateForm()
  }
})

// Reset pagination when filters change
watch([searchQuery, statusFilter, sortOption], () => {
  currentPage.value = 1
})

onMounted(() => {
  fetchPlans()
})
</script>
