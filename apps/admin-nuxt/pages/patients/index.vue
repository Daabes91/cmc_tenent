<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-sky-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-sky-500 to-blue-600 shadow-lg">
              <UIcon name="i-lucide-users" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t("patients.list.overview.title") }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t("patients.list.overview.subtitle") }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="pending"
              @click="refresh"
            >
              {{ t("patients.list.actions.refresh") }}
            </UButton>
            <UButton 
              color="sky" 
              icon="i-lucide-user-round-plus" 
              @click="openCreate"
            >
              {{ t("patients.list.actions.add") }}
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
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-sky-50 dark:bg-sky-900/20">
              <UIcon name="i-lucide-address-book" class="h-5 w-5 text-sky-600 dark:text-sky-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t("patients.list.metrics.total.label") }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatNumber(stats.total) }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t("patients.list.metrics.total.caption") }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
              <UIcon name="i-lucide-calendar-plus" class="h-5 w-5 text-green-600 dark:text-green-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t("patients.list.metrics.newThisMonth.label") }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatNumber(stats.newThisMonth) }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t("patients.list.metrics.newThisMonth.caption") }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-50 dark:bg-emerald-900/20">
              <UIcon name="i-lucide-mail" class="h-5 w-5 text-emerald-600 dark:text-emerald-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t("patients.list.metrics.reachableEmail.label") }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatNumber(stats.reachableEmail) }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t("patients.list.metrics.reachableEmail.caption") }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-50 dark:bg-purple-900/20">
              <UIcon name="i-lucide-clipboard-check" class="h-5 w-5 text-purple-600 dark:text-purple-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t("patients.list.metrics.completion.label") }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ stats.completionRate }}%</p>
            </div>
          </div>
          <div class="space-y-2">
            <UProgress :value="stats.completionRate" size="xs" />
            <p class="text-xs text-slate-600 dark:text-slate-300">{{ t("patients.list.metrics.completion.caption", { count: formatNumber(stats.completeProfiles) }) }}</p>
          </div>
        </div>
      </div>

      <!-- Search and Filters -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden mb-6">
        <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-search" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t("patients.list.filters.title") }}</h2>
              <p class="text-sm text-slate-300">{{ t("patients.list.filters.subtitle") }}</p>
            </div>
          </div>
        </div>
        <div class="p-6">
          <div class="flex flex-col gap-4">
            <div class="flex flex-col sm:flex-row gap-4">
              <div class="flex-1">
                <UFormGroup :label="t('patients.list.filters.searchLabel')">
                  <UInput
                    v-model="search"
                    size="lg"
                    :placeholder="t('patients.list.filters.searchPlaceholder')"
                    icon="i-lucide-search"
                  />
                </UFormGroup>
              </div>
              <div class="sm:w-48">
                <UFormGroup :label="t('patients.list.filters.viewMode')">
                  <UButtonGroup size="lg" class="w-full">
                    <UButton
                      :variant="viewMode === 'table' ? 'solid' : 'ghost'"
                      :color="viewMode === 'table' ? 'sky' : 'gray'"
                      icon="i-lucide-table-2"
                      @click="viewMode = 'table'"
                      class="flex-1"
                    >
                      {{ t('patients.list.filters.tableView') }}
                    </UButton>
                    <UButton
                      :variant="viewMode === 'cards' ? 'solid' : 'ghost'"
                      :color="viewMode === 'cards' ? 'sky' : 'gray'"
                      icon="i-lucide-layout-grid"
                      @click="viewMode = 'cards'"
                      class="flex-1"
                    >
                      {{ t('patients.list.filters.cardsView') }}
                    </UButton>
                  </UButtonGroup>
                </UFormGroup>
              </div>
            </div>

            <div class="flex flex-wrap items-center gap-2">
              <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('patients.list.filters.sortLabel') }}</span>
              <UButton
                v-for="option in sortOptions"
                :key="option.value"
                size="xs"
                :variant="sortOption === option.value ? 'solid' : 'soft'"
                :color="sortOption === option.value ? 'sky' : 'gray'"
                class="rounded-full"
                @click="sortOption = option.value"
              >
                <UIcon :name="option.icon" class="h-3.5 w-3.5" />
                <span>{{ option.label }}</span>
              </UButton>
            </div>

            <div class="flex flex-wrap items-center gap-2">
              <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('patients.list.filters.quickLabel') }}</span>
              <UButton
                v-for="option in quickFilters"
                :key="option.value"
                size="xs"
                :variant="quickFilter === option.value ? 'solid' : 'soft'"
                :color="quickFilter === option.value ? 'sky' : 'gray'"
                class="rounded-full"
                @click="quickFilter = option.value"
              >
                <UIcon :name="option.icon" class="h-3.5 w-3.5" />
                <span>{{ option.label }}</span>
              </UButton>
            </div>

            <div v-if="activeFiltersCount" class="flex items-center justify-between rounded-xl bg-sky-50 dark:bg-sky-900/20 px-4 py-2 text-xs text-sky-700 dark:text-sky-300">
              <div class="flex items-center gap-2">
                <UIcon name="i-lucide-filter" class="h-4 w-4" />
                <span>{{ matchCountText }}</span>
              </div>
              <UButton size="xs" variant="ghost" color="sky" @click="resetFilters">
                {{ t('patients.list.filters.clear') }}
              </UButton>
            </div>

            <div class="w-full">
               <USelectMenu
                  v-model="selectedTags"
                  :options="availableTags"
                  option-attribute="name"
                  value-attribute="id"
                  multiple
                  placeholder="Filter by tags"
                  searchable
                  class="w-full"
               >
                 <template #label>
                    <span v-if="selectedTags.length" class="truncate">{{ selectedTags.length }} tags selected</span>
                    <span v-else class="text-gray-500">Filter by tags</span>
                 </template>
               </USelectMenu>
            </div>
          </div>
        </div>
      </div>

      <!-- Patients Grid -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-sky-500 to-blue-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-users" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t("patients.list.directory.title") }}</h2>
              <p class="text-sm text-sky-100">{{ t("patients.list.directory.count", { count: formatNumber(filteredRows.length) }) }}</p>
            </div>
          </div>
        </div>

        <div v-if="pending" class="p-6">
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

        <div v-else-if="filteredRows.length > 0" class="p-6">
          <div
            v-if="viewMode === 'cards'"
            key="patient-cards"
            class="grid gap-6 md:grid-cols-2 lg:grid-cols-3"
          >
            <article
              v-for="row in filteredRows"
              :key="row.id"
              class="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-6 border border-slate-200 dark:border-slate-600 hover:shadow-md hover:border-sky-300 dark:hover:border-sky-600 transition-all duration-200 group"
            >
              <!-- Patient Header -->
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center gap-3">
                  <div class="relative">
                    <UAvatar
                      :alt="row.fullName"
                      :src="row.profileImageUrl || undefined"
                      size="lg"
                      class="ring-2 ring-sky-100 dark:ring-sky-800"
                    />
                    <div class="absolute -top-1 -right-1 w-4 h-4 rounded-full border-2 border-white dark:border-slate-700 bg-green-500"></div>
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="font-semibold text-slate-900 dark:text-white truncate">
                      {{ row.fullName || t('patients.list.labels.unnamed') }}
                    </h3>
                    <p class="text-sm text-slate-500 dark:text-slate-400 truncate">
                      {{ row.externalId ? t('patients.list.labels.patientId', { id: row.externalId }) : t('patients.list.labels.patientIdUnassigned') }}
                    </p>
                  </div>
                </div>
                <UDropdown :items="getRowActions(row)" :popper="{ placement: 'bottom-end' }">
                  <UButton icon="i-lucide-more-vertical" variant="ghost" color="gray" size="xs" @click.stop />
                </UDropdown>
              </div>

              <!-- Patient Details -->
              <div class="space-y-3 mb-4">
                <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                  <UIcon name="i-lucide-mail" class="h-4 w-4 text-slate-400" />
                  <div class="flex-1 min-w-0">
                    <a 
                      v-if="row.email" 
                      :href="`mailto:${row.email}`"
                      class="text-sky-600 dark:text-sky-400 hover:text-sky-700 dark:hover:text-sky-300 hover:underline transition-colors duration-200 truncate block"
                      :title="t('patients.list.labels.emailTooltip')"
                    >
                      {{ row.email }}
                    </a>
                    <span v-else class="text-slate-400 dark:text-slate-500 truncate block">
                      {{ t('patients.list.labels.emailMissing') }}
                    </span>
                  </div>
                </div>
                <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                  <UIcon name="i-lucide-phone" class="h-4 w-4 text-slate-400" />
                  <div class="flex-1 min-w-0">
                    <a 
                      v-if="row.phone" 
                      :href="`tel:${row.phone}`"
                      class="text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 dark:hover:text-emerald-300 hover:underline transition-colors duration-200"
                      :title="t('patients.list.labels.phoneTooltip')"
                    >
                      {{ row.phone }}
                    </a>
                    <span v-else class="text-slate-400 dark:text-slate-500">
                      {{ t('patients.list.labels.phoneMissing') }}
                    </span>
                  </div>
                </div>
                <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                  <UIcon name="i-lucide-cake" class="h-4 w-4 text-slate-400" />
                  <span>{{ row.birthdayLabel !== '—' ? row.birthdayLabel : t('patients.list.labels.birthdayMissing') }}</span>
                  <span v-if="row.ageYears != null" class="text-xs text-slate-400">
                    {{ t('patients.list.labels.age', { count: formatNumber(row.ageYears) }) }}
                  </span>
                </div>
              </div>

              <!-- Status Badge -->
              <div class="mb-4">
                <UBadge
                  :color="hasCompleteProfile(row) ? 'green' : 'amber'"
                  variant="soft"
                  size="sm"
                  class="rounded-full"
                >
                  {{ statusBadgeText(row) }}
                </UBadge>
              </div>

              <!-- Actions -->
              <div class="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-slate-600">
                <div class="text-xs text-slate-500 dark:text-slate-400" :title="t('patients.list.labels.addedTooltip', { date: row.createdAt })">
                  {{ t('patients.list.labels.addedOn', { date: row.createdAt }) }}
                </div>
                <div class="flex items-center gap-2">
                  <UButton
                    size="sm"
                    variant="ghost"
                    color="sky"
                    icon="i-lucide-eye"
                    @click="openEdit(row)"
                  >
                    {{ t('patients.list.actions.view') }}
                  </UButton>
                  <UButton
                    size="sm"
                    variant="ghost"
                    color="red"
                    icon="i-lucide-trash-2"
                    @click="confirmRemove(row)"
                  >
                    {{ t('patients.list.actions.delete') }}
                  </UButton>
                </div>
              </div>
            </article>
          </div>

          <div v-else key="patient-table" class="w-full">
            <UTable :rows="filteredRows" :columns="columns" class="w-full">
              <template #fullName-data="{ row }">
                <button class="flex items-center gap-3 text-left" @click="openEdit(row)">
                  <UAvatar
                    size="sm"
                    :alt="row.fullName"
                    :src="row.profileImageUrl || undefined"
                    class="ring-2 ring-sky-100 dark:ring-sky-800"
                  />
                  <div>
                    <p class="text-sm font-semibold text-slate-900 dark:text-white break-words">{{ row.fullName || t('patients.list.labels.unnamed') }}</p>
                  </div>
                </button>
              </template>
              <template #contact-data="{ row }">
                <div class="space-y-1 text-xs text-slate-600 dark:text-slate-300">
                  <div class="flex items-center gap-1.5">
                    <UIcon name="i-lucide-mail" class="h-3 w-3 text-sky-500" />
                    <div class="flex-1 min-w-0">
                      <a 
                        v-if="row.email" 
                        :href="`mailto:${row.email}`"
                        class="text-sky-600 dark:text-sky-400 hover:text-sky-700 dark:hover:text-sky-300 hover:underline transition-colors duration-200 break-all"
                        :title="t('patients.list.labels.emailTooltip')"
                      >
                        {{ row.email }}
                      </a>
                      <span v-else class="text-slate-400 dark:text-slate-500 break-all">
                        {{ t('patients.list.labels.emailMissing') }}
                      </span>
                    </div>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <UIcon name="i-lucide-phone" class="h-3 w-3 text-emerald-500" />
                    <div class="flex-1 min-w-0">
                      <a 
                        v-if="row.phone" 
                        :href="`tel:${row.phone}`"
                        class="text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 dark:hover:text-emerald-300 hover:underline transition-colors duration-200 break-all"
                        :title="t('patients.list.labels.phoneTooltip')"
                      >
                        {{ row.phone }}
                      </a>
                      <span v-else class="text-slate-400 dark:text-slate-500 break-all">
                        {{ t('patients.list.labels.phoneMissing') }}
                      </span>
                    </div>
                  </div>
                </div>
              </template>
              <template #status-data="{ row }">
                <UBadge
                  :color="hasCompleteProfile(row) ? 'green' : 'amber'"
                  variant="soft"
                  size="xs"
                  class="rounded-full"
                >
                  {{ statusTableText(row) }}
                </UBadge>
              </template>
              <template #tags-data="{ row }">
                <div class="flex flex-wrap gap-1">
                  <UBadge
                    v-for="tag in row.tags?.slice(0, 2) || []"
                    :key="tag.id"
                    :color="tag.color ? undefined : 'gray'"
                    :variant="tag.color ? 'solid' : 'soft'"
                    :style="tag.color ? { backgroundColor: tag.color, borderColor: tag.color, color: '#fff' } : undefined"
                    size="xs"
                    class="rounded-full"
                  >
                    {{ tag.name }}
                  </UBadge>
                  <UBadge
                    v-if="(row.tags?.length || 0) > 2"
                    color="gray"
                    variant="soft"
                    size="xs"
                    class="rounded-full"
                  >
                    +{{ row.tags.length - 2 }}
                  </UBadge>
                </div>
              </template>
              <template #age-data="{ row }">
                <span class="text-xs text-slate-600 dark:text-slate-300">
                  {{ row.ageYears != null ? t('patients.list.table.ageValue', { count: formatNumber(row.ageYears) }) : t('patients.list.labels.notAvailable') }}
                </span>
              </template>
              <template #actions-data="{ row }">
                <div class="flex justify-end gap-1">
                  <UButton size="xs" color="sky" variant="soft" icon="i-lucide-eye" @click="openEdit(row)">
                    {{ t('patients.list.actions.view') }}
                  </UButton>
                  <UButton size="xs" color="red" variant="ghost" icon="i-lucide-trash-2" @click="confirmRemove(row)">
                    {{ t('patients.list.actions.delete') }}
                  </UButton>
                </div>
              </template>
            </UTable>
          </div>
        </div>

        <div v-else class="p-12 text-center">
          <div class="flex flex-col items-center gap-4">
            <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
              <UIcon name="i-lucide-user-x" class="h-8 w-8 text-slate-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('patients.list.empty.title') }}</h3>
              <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                {{ search || quickFilter !== 'all' || sortOption !== 'newest' ? t('patients.list.empty.searchHint') : t('patients.list.empty.subtitle') }}
              </p>
            </div>
            <div class="flex flex-wrap items-center gap-2">
              <UButton
                v-if="search || quickFilter !== 'all' || sortOption !== 'newest'"
                variant="outline"
                color="gray"
                @click="resetFilters"
              >
                {{ t('patients.list.empty.reset') }}
              </UButton>
              <UButton
                color="sky"
                icon="i-lucide-user-round-plus"
                @click="openCreate"
              >
                {{ t('patients.list.actions.add') }}
              </UButton>
            </div>
          </div>
        </div>

        <template v-if="totalPages > 1">
          <div class="flex flex-wrap items-center justify-between gap-3 border-t border-slate-200 dark:border-slate-600 bg-slate-50 dark:bg-slate-700/50 px-6 py-4 text-xs text-slate-600 dark:text-slate-300">
            <span>{{ paginationSummary }}</span>
            <UPagination
              v-model="page"
              :page-count="pageSize"
              :total="totalItems"
              size="sm"
              show-first
              show-last
            />
          </div>
        </template>
      </div>

      <!-- Delete Confirmation Modal -->
      <UModal v-model="deleteOpen">
        <UCard>
          <template #header>
            <div class="flex items-center gap-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-full bg-red-100 dark:bg-red-900/30">
                <UIcon name="i-lucide-alert-triangle" class="h-5 w-5 text-red-600 dark:text-red-400" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('patients.list.delete.title') }}</h3>
                <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('patients.list.delete.subtitle') }}</p>
              </div>
            </div>
          </template>

          <div class="py-4">
            <p class="text-slate-700 dark:text-slate-300">
              {{ t('patients.list.delete.prompt', { name: deleteTarget?.fullName || t('patients.list.labels.unnamed') }) }}
            </p>
          </div>

          <template #footer>
            <div class="flex justify-end gap-3">
              <UButton
                variant="ghost"
                color="gray"
                @click="deleteOpen = false"
                :disabled="deleting"
              >
                {{ t('common.actions.cancel') }}
              </UButton>
              <UButton
                color="red"
                icon="i-lucide-trash-2"
                :loading="deleting"
                @click="handleDelete"
              >
                {{ t('patients.list.delete.confirmAction') }}
              </UButton>
            </div>
          </template>
        </UCard>
      </UModal>
    </div>
  </div>
</template>
<script setup lang="ts">
import type { PatientAdmin } from "@/types/patients";
import { useI18n } from "vue-i18n";
import { useTagService, type Tag } from "@/services/tag.service";

const { t, locale } = useI18n();

useHead(() => ({ title: t("patients.list.meta.title") }));

const toast = useToast();
const { fetcher, request } = useAdminApi();
const { listTags } = useTagService();
const router = useRouter();

const page = ref(1);
const pageSize = ref(12);
const totalItems = ref(0);
const totalPages = computed(() => Math.max(1, Math.ceil(totalItems.value / pageSize.value)));

const { data, pending, refresh: baseRefresh } = await useAsyncData(
  () => `admin-patients-${page.value}`,
  () => fetchPatients(),
  {
    watch: [page],
    // Fetch on client so the request is visible and up-to-date after navigation
    server: false
  }
);

async function fetchPatients() {
  const params: any = {
    page: page.value - 1,
    size: pageSize.value,
  };
  
  if (selectedTags.value.length > 0) {
    params.tags = selectedTags.value.join(',');
  }

  const response = await fetcher<any>(
    '/patients',
    { 
      params,
      content: [], totalElements: 0, totalPages: 0, number: 0, size: pageSize.value 
    }
  );

  if (Array.isArray(response)) {
    totalItems.value = response.length;
    return response;
  }

  pageSize.value = response.size ?? pageSize.value;
  totalItems.value = response.totalElements ?? 0;
  return response.content ?? [];
}

async function refresh() {
  try {
    await baseRefresh();
  } catch (error: any) {
    toast.add({
      title: t("patients.list.toasts.refreshError.title"),
      description: error?.data?.message ?? error?.message ?? t("patients.common.unexpectedError"),
      color: "red",
      icon: "i-lucide-alert-triangle"
    });
  }
}

const patients = computed(() => data.value ?? []);

watch(page, async (next, previous) => {
  if (next === previous) return;
  try {
    await baseRefresh();
  } catch (error: any) {
    toast.add({
      title: t("patients.list.toasts.loadError.title"),
      description: error?.data?.message ?? error?.message ?? t("patients.common.unexpectedError"),
      color: "red",
      icon: "i-lucide-alert-triangle"
    });
  }
});

onMounted(async () => {
  if (!patients.value.length) {
    try {
      await baseRefresh();
    } catch (error: any) {
      toast.add({
        title: t("patients.list.toasts.loadError.title"),
        description: error?.data?.message ?? error?.message ?? t("patients.common.unexpectedError"),
        color: "red",
        icon: "i-lucide-alert-triangle"
      });
    }
  }
});

const dateFormatter = computed(() => new Intl.DateTimeFormat(locale.value || undefined, { dateStyle: "medium" }));
const numberFormatter = computed(() => new Intl.NumberFormat(locale.value || undefined));
const conjunctionFormatter = computed(() =>
  new Intl.ListFormat(locale.value || undefined, { style: "long", type: "conjunction" })
);

const formatNumber = (value: number) => numberFormatter.value.format(Math.max(0, value ?? 0));

const parseLocalDateArray = (value: unknown[]): Date | null => {
  if (value.length < 3) return null;
  const [yearRaw, monthRaw, dayRaw] = value;
  const year = Number(yearRaw);
  const month = Number(monthRaw);
  const day = Number(dayRaw);
  if (![year, month, day].every(v => Number.isFinite(v))) {
    return null;
  }
  const date = new Date(year, month - 1, day);
  return Number.isNaN(date.getTime()) ? null : date;
};

const parseDateValue = (value: unknown): Date | null => {
  if (value == null) return null;

  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? null : value;
  }

  if (Array.isArray(value)) {
    return parseLocalDateArray(value);
  }

  if (typeof value === "string") {
    const trimmed = value.trim();
    if (!trimmed) return null;

    const parsed = Date.parse(trimmed);
    if (!Number.isNaN(parsed)) {
      const date = new Date(parsed);
      return Number.isNaN(date.getTime()) ? null : date;
    }

    const numeric = Number(trimmed);
    if (!Number.isNaN(numeric)) {
      const millis = numeric < 1e12 ? numeric * 1000 : numeric;
      const date = new Date(millis);
      return Number.isNaN(date.getTime()) ? null : date;
    }
    return null;
  }

  if (typeof value === "number" && Number.isFinite(value)) {
    const millis = value < 1e12 ? value * 1000 : value;
    const date = new Date(millis);
    return Number.isNaN(date.getTime()) ? null : date;
  }

  if (typeof value === "object") {
    const record = value as Record<string, unknown>;

    if (record.value && record.value !== value) {
      if (Array.isArray(record.value)) {
        const date = parseLocalDateArray(record.value);
        if (date) {
          return date;
        }
      } else {
        const nested = parseDateValue(record.value);
        if (nested) {
          return nested;
        }
      }
    }

    const epochSecond = record.epochSecond ?? record.epochSeconds;
    if (typeof epochSecond === "number" || typeof epochSecond === "string") {
      const numeric = Number(epochSecond);
      if (!Number.isNaN(numeric)) {
        const millis = numeric * 1000;
        const date = new Date(millis);
        return Number.isNaN(date.getTime()) ? null : date;
      }
    }

    const epochMilli = record.epochMilli ?? record.epochMillis ?? record.millis;
    if (typeof epochMilli === "number" || typeof epochMilli === "string") {
      const numeric = Number(epochMilli);
      if (!Number.isNaN(numeric)) {
        const date = new Date(numeric);
        return Number.isNaN(date.getTime()) ? null : date;
      }
    }

    const year = typeof record.year === "number" ? record.year : null;
    const day = typeof record.dayOfMonth === "number" ? record.dayOfMonth : null;
    if (year != null && day != null) {
      let monthIndex: number | null = null;
      if (typeof record.monthValue === "number") {
        monthIndex = record.monthValue - 1;
      } else if (typeof record.month === "number") {
        monthIndex = record.month - 1;
      } else if (typeof record.month === "string") {
        const monthName = record.month.trim().toLowerCase();
        const MONTHS = [
          "january",
          "february",
          "march",
          "april",
          "may",
          "june",
          "july",
          "august",
          "september",
          "october",
          "november",
          "december"
        ] as const;
        const idx = MONTHS.indexOf(monthName as typeof MONTHS[number]);
        if (idx !== -1) {
          monthIndex = idx;
        }
      }

      if (monthIndex == null) {
        monthIndex = 0;
      }

      const date = new Date(year, monthIndex, day);
      return Number.isNaN(date.getTime()) ? null : date;
    }
  }

  return null;
};

const formatDateLabel = (date: Date | null) => (date ? dateFormatter.value.format(date) : "—");

const formatTimestamp = (timestamp?: string | number | null) => {
  if (!timestamp) return "—";
  try {
    // Handle Unix timestamps (in seconds) and ISO strings
    let dateObj: Date;
    if (typeof timestamp === 'number') {
      // Unix timestamp: if less than 10 billion, it's in seconds (dates before 2286)
      const time = timestamp < 10000000000 ? timestamp * 1000 : timestamp;
      dateObj = new Date(time);
    } else {
      dateObj = new Date(timestamp);
    }

    if (isNaN(dateObj.getTime()) || dateObj.getTime() === 0) return "—";
    return dateFormatter.value.format(dateObj);
  } catch {
    return "—";
  }
};

const computeAge = (date: Date | null) => {
  if (!date) return null;
  const now = Date.now();
  const birth = date.getTime();
  if (Number.isNaN(birth) || birth <= 0 || birth > now) {
    return null;
  }

  const age = Math.floor((now - birth) / 31557600000); // average year length
  if (age < 0 || age > 130) {
    return null;
  }
  return age;
};

const rows = computed(() =>
  patients.value.map(patient => {
    const createdAtRaw = patient.createdAt ?? null;
    const createdAt = formatTimestamp(createdAtRaw);
    const profileImageUrl = patient.profileImageUrl?.trim() || null;
    const birthDate = parseDateValue(patient.dateOfBirth ?? null);
    const birthdayLabel = formatDateLabel(birthDate);
    const ageYears = computeAge(birthDate);

    return {
      ...patient,
      createdAtRaw,
      createdAt,
      profileImageUrl,
      birthDate,
      birthdayLabel,
      ageYears,
      fullName: `${patient.firstName ?? ""} ${patient.lastName ?? ""}`.trim() || patient.firstName || patient.lastName || ""
    };
  })
);

const stats = computed(() => {
  const now = new Date();
  const monthStart = new Date(now.getFullYear(), now.getMonth(), 1).getTime();

  let newThisMonth = 0;
  let reachableEmail = 0;
  let missingEmail = 0;
  let missingPhone = 0;
  let completeProfiles = 0;

  for (const row of rows.value) {
    if (row.createdAtRaw && new Date(row.createdAtRaw).getTime() >= monthStart) {
      newThisMonth += 1;
    }

    const hasEmail = Boolean(row.email?.trim());
    const hasPhone = Boolean(row.phone?.trim());

    if (hasEmail) {
      reachableEmail += 1;
    } else {
      missingEmail += 1;
    }

    if (!hasPhone) {
      missingPhone += 1;
    }

    if (hasEmail && hasPhone) {
      completeProfiles += 1;
    }
  }

  const total = totalItems.value;
  const pageTotal = rows.value.length;
  const completionRate = pageTotal ? Math.round((completeProfiles / pageTotal) * 100) : 0;

  return {
    total,
    newThisMonth,
    reachableEmail,
    missingEmail,
    missingPhone,
    completeProfiles,
    completionRate
  };
});

const search = ref("");
const quickFilter = ref<"all" | "withEmail" | "missingEmail" | "missingPhone">("all");
type ViewMode = "table" | "cards";

const viewMode = ref<ViewMode>("table");
const sortOption = ref<"newest" | "oldest" | "name">("newest");
const selectedTags = ref<number[]>([]);
const availableTags = ref<Tag[]>([]);

onMounted(async () => {
  try {
    availableTags.value = await listTags();
  } catch (e) {
    console.error("Failed to load tags", e);
  }
});

const QUICK_FILTER_DEFS = [
  { value: "all", icon: "i-lucide-users" },
  { value: "withEmail", icon: "i-lucide-mail" },
  { value: "missingEmail", icon: "i-lucide-mail-x" },
  { value: "missingPhone", icon: "i-lucide-phone-x" }
] as const;

const quickFilters = computed(() =>
  QUICK_FILTER_DEFS.map(filter => ({
    ...filter,
    label: t(`patients.list.filters.quick.${filter.value}`)
  }))
);

const SORT_OPTION_DEFS = [
  { value: "newest", icon: "i-lucide-sparkles" },
  { value: "oldest", icon: "i-lucide-history" },
  { value: "name", icon: "i-lucide-text-select" }
] as const;

const sortOptions = computed(() =>
  SORT_OPTION_DEFS.map(option => ({
    ...option,
    label: t(`patients.list.filters.sort.${option.value}`)
  }))
);

const activeFiltersCount = computed(() => {
  let count = 0;
  if (search.value.trim()) count += 1;
  if (quickFilter.value !== "all") count += 1;
  if (sortOption.value !== "newest") count += 1;
  if (selectedTags.value.length > 0) count += 1;
  return count;
});


const toTime = (value?: string | number | null) => {
  if (!value) {
    return 0;
  }
  // Handle Unix timestamps (in seconds) and ISO strings
  let time: number;
  if (typeof value === 'number') {
    // Unix timestamp: if less than 10 billion, it's in seconds (dates before 2286)
    time = value < 10000000000 ? value * 1000 : value;
  } else {
    time = new Date(value).getTime();
  }
  return Number.isNaN(time) ? 0 : time;
};

const filteredRows = computed(() => {
  const term = search.value.toLowerCase();

  const filtered = rows.value.filter(row => {
    const matchesSearch =
      !term ||
      [row.fullName, row.email, row.phone, row.externalId]
        .filter(Boolean)
        .some(value => value?.toLowerCase().includes(term));

    const matchesQuick =
      (quickFilter.value === "all") ||
      (quickFilter.value === "withEmail" && Boolean(row.email?.trim())) ||
      (quickFilter.value === "missingEmail" && !row.email?.trim()) ||
      (quickFilter.value === "missingPhone" && !row.phone?.trim());

    return matchesSearch && matchesQuick;
  });

  const sorted = [...filtered].sort((a, b) => {
    switch (sortOption.value) {
      case "oldest":
        return toTime(a.createdAtRaw) - toTime(b.createdAtRaw);
      case "name":
        return (a.fullName || "").localeCompare(b.fullName || "", undefined, { sensitivity: "base" });
      default:
        return toTime(b.createdAtRaw) - toTime(a.createdAtRaw);
    }
  });

  return sorted;
});
const matchCountText = computed(() => {
  const count = filteredRows.value.length;
  if (count === 0) {
    return t("patients.list.filters.matchCount.zero");
  }
  if (count === 1) {
    return t("patients.list.filters.matchCount.one");
  }
  return t("patients.list.filters.matchCount.other", { count: formatNumber(count) });
});

const startItem = computed(() =>
  totalItems.value === 0 ? 0 : (page.value - 1) * pageSize.value + 1
);
const endItem = computed(() =>
  totalItems.value === 0 ? 0 : Math.min(page.value * pageSize.value, totalItems.value)
);

const paginationSummary = computed(() =>
  t("patients.list.pagination.summary", {
    start: formatNumber(startItem.value),
    end: formatNumber(endItem.value),
    total: formatNumber(totalItems.value)
  })
);

const columns = computed(() => [
  { key: "fullName", label: t("patients.list.table.columns.patient") },
  { key: "contact", label: t("patients.list.table.columns.contact") },
  { key: "tags", label: "Tags" },
  { key: "status", label: t("patients.list.table.columns.profile"), class: "w-24 sm:w-28" },
  { key: "age", label: t("patients.list.table.columns.age"), class: "w-20 text-right sm:text-left" },
  { key: "actions", label: t("patients.list.table.columns.actions"), class: "w-24 text-right" }
]);

const hasCompleteProfile = (row: { email?: string | null; phone?: string | null }) =>
  Boolean(row.email?.trim() && row.phone?.trim());

const describeMissingFields = (row: { email?: string | null; phone?: string | null }) => {
  if (hasCompleteProfile(row)) {
    return t("patients.list.status.completeBadge");
  }

  const missingLabels: string[] = [];
  if (!row.email?.trim()) missingLabels.push(t("patients.list.status.fields.email"));
  if (!row.phone?.trim()) missingLabels.push(t("patients.list.status.fields.phone"));

  if (!missingLabels.length) {
    return t("patients.list.status.completeBadge");
  }

  const formatted = conjunctionFormatter.value.format(missingLabels);
  return t("patients.list.status.missing", { fields: formatted });
};

const statusBadgeText = (row: { email?: string | null; phone?: string | null }) =>
  hasCompleteProfile(row) ? t("patients.list.status.completeBadge") : describeMissingFields(row);

const statusTableText = (row: { email?: string | null; phone?: string | null }) =>
  hasCompleteProfile(row) ? t("patients.list.status.completeShort") : describeMissingFields(row);

const contactAlerts = computed(() =>
  filteredRows.value
    .filter(row => !hasCompleteProfile(row))
    .slice(0, 5)
);

const recentPatients = computed(() =>
  [...filteredRows.value]
    .sort((a, b) => toTime(b.createdAtRaw) - toTime(a.createdAtRaw))
    .slice(0, 5)
);

const deleteOpen = ref(false);
const deleteTarget = ref<{ id: number; fullName: string } | null>(null);
const deleting = ref(false);

watch([search, quickFilter, sortOption, selectedTags], () => {
  page.value = 1;
  refresh();
});

function resetFilters() {
  search.value = "";
  quickFilter.value = "all";
  sortOption.value = "newest";
  selectedTags.value = [];
}

function openCreate() {
  router.push("/patients/new");
}

function openEdit(row: (typeof rows.value)[number]) {
  router.push(`/patients/${row.id}`);
}

function getRowActions(row: (typeof rows.value)[number]) {
  return [
    [
      {
        label: t("patients.list.actions.viewDetails"),
        icon: "i-lucide-eye",
        click: () => openEdit(row)
      }
    ],
    [
      {
        label: t("patients.list.actions.delete"),
        icon: "i-lucide-trash-2",
        class: "text-red-600",
        click: () => confirmRemove(row)
      }
    ]
  ];
}

function confirmRemove(row: (typeof rows.value)[number]) {
  deleteTarget.value = { id: row.id, fullName: row.fullName };
  deleteOpen.value = true;
}

async function handleDelete() {
  if (!deleteTarget.value) return;
  deleting.value = true;
  try {
    await request(`/patients/${deleteTarget.value.id}`, { method: "DELETE" });
    toast.add({
      title: t("patients.list.toasts.deleteSuccess"),
      color: "green",
      icon: "i-lucide-check-circle"
    });
    deleteOpen.value = false;
    deleteTarget.value = null;

    if (filteredRows.value.length === 1 && page.value > 1) {
      page.value -= 1;
    } else {
      await baseRefresh();
    }
  } catch (error: any) {
    toast.add({
      title: t("patients.list.toasts.deleteError.title"),
      description: error?.data?.message ?? error?.message ?? t("patients.common.unexpectedError"),
      color: "red",
      icon: "i-lucide-alert-triangle"
    });
  } finally {
    deleting.value = false;
  }
}
</script>
