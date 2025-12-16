<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/90 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/85 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 py-4">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div class="flex items-center gap-3 sm:gap-4">
            <div class="flex h-11 w-11 sm:h-12 sm:w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-layers" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white">{{ t('services.list.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300 sm:max-w-md">{{ t('services.list.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-end">
            <UButton 
              variant="ghost"
              color="gray"
              icon="i-lucide-refresh-cw" 
              :loading="pending"
              class="w-full sm:w-auto justify-center"
              @click="refresh"
            >
              {{ t('services.list.actions.refresh') }}
            </UButton>
            <UButton 
              color="blue"
              icon="i-lucide-plus" 
              class="w-full sm:w-auto justify-center"
              @click="openCreate"
            >
              {{ t('services.list.actions.addService') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 py-8">
      <!-- Quick Stats -->
      <div class="mb-8">
        <div class="flex gap-4 overflow-x-auto pb-3 snap-x snap-mandatory md:grid md:grid-cols-2 xl:grid-cols-4 md:gap-4 md:overflow-visible md:snap-none">
          <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 sm:p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200 flex-shrink-0 w-64 md:w-auto snap-start">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
                <UIcon name="i-lucide-layers" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('services.list.metrics.total.label') }}</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ services?.length || 0 }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300 sm:text-sm">{{ t('services.list.metrics.total.caption') }}</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 sm:p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200 flex-shrink-0 w-64 md:w-auto snap-start">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
                <UIcon name="i-lucide-users" class="h-5 w-5 text-green-600 dark:text-green-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('services.list.metrics.linkedDoctors.label') }}</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ totalDoctors }}</p>
              </div>
            </div>
            <p class="text-xs sm:text-sm text-slate-600 dark:text-slate-300">{{ t('services.list.metrics.linkedDoctors.caption') }}</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 sm:p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200 flex-shrink-0 w-64 md:w-auto snap-start">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-50 dark:bg-purple-900/20">
                <UIcon name="i-lucide-calendar" class="h-5 w-5 text-purple-600 dark:text-purple-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('services.list.metrics.recentlyUpdated.label') }}</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ recentlyUpdated }}</p>
              </div>
            </div>
            <p class="text-xs sm:text-sm text-slate-600 dark:text-slate-300">{{ t('services.list.metrics.recentlyUpdated.caption') }}</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 sm:p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200 flex-shrink-0 w-64 md:w-auto snap-start">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-50 dark:bg-emerald-900/20">
                <UIcon name="i-lucide-check-circle" class="h-5 w-5 text-emerald-600 dark:text-emerald-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('services.list.metrics.active.label') }}</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ activeServices }}</p>
              </div>
            </div>
            <p class="text-xs sm:text-sm text-slate-600 dark:text-slate-300">{{ t('services.list.metrics.active.caption') }}</p>
          </div>
        </div>
      </div>

      <!-- Search and View Mode -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden mb-6">
        <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-4 sm:px-6 py-4">
          <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:gap-3">
            <UIcon name="i-lucide-search" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('services.list.search.title') }}</h2>
              <p class="text-sm text-slate-300">{{ t('services.list.search.subtitle') }}</p>
            </div>
          </div>
        </div>
        <div class="p-4 sm:p-6">
          <div class="flex flex-col gap-4 sm:flex-row">
            <div class="flex-1">
              <UFormGroup :label="t('services.list.search.searchLabel')">
                <UInput
                  v-model="search"
                  size="lg"
                  :placeholder="t('services.list.search.searchPlaceholder')"
                  icon="i-lucide-search"
                />
              </UFormGroup>
            </div>
            <div class="w-full sm:w-48">
              <UFormGroup :label="t('services.list.search.viewModeLabel')">
                <UButtonGroup size="lg" class="w-full flex flex-col gap-2 sm:flex-row sm:gap-0">
                  <UButton
                    :variant="viewMode === 'table' ? 'solid' : 'ghost'"
                    :color="viewMode === 'table' ? 'blue' : 'gray'"
                    icon="i-lucide-table-2"
                    class="flex-1 justify-center"
                    @click="viewMode = 'table'"
                  >
                    {{ t('services.list.search.table') }}
                  </UButton>
                  <UButton
                    :variant="viewMode === 'cards' ? 'solid' : 'ghost'"
                    :color="viewMode === 'cards' ? 'blue' : 'gray'"
                    icon="i-lucide-layout-grid"
                    class="flex-1 justify-center"
                    @click="viewMode = 'cards'"
                  >
                    {{ t('services.list.search.cards') }}
                  </UButton>
                </UButtonGroup>
              </UFormGroup>
            </div>
          </div>
        </div>
      </div>

      <!-- Services Display -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-4 sm:px-6 py-4">
          <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:gap-3">
            <UIcon name="i-lucide-layers" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('services.list.directory.title') }}</h2>
              <p class="text-sm text-blue-100">{{ t('services.list.directory.count', { count: filteredRows.length }) }}</p>
            </div>
          </div>
        </div>

        <div v-if="pending" class="p-4 sm:p-6">
            <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
            <div v-for="i in 6" :key="i" class="animate-pulse">
              <div class="bg-slate-200 dark:bg-slate-700 rounded-xl p-5 space-y-4">
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

        <template v-else>
          <div v-if="!filteredRows.length" class="p-12 text-center">
            <div class="flex flex-col items-center gap-4">
              <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                <UIcon name="i-lucide-layers" class="h-8 w-8 text-slate-400" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('services.list.empty.title') }}</h3>
                <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                  {{ search ? t('services.list.empty.searchHint') : t('services.list.empty.subtitle') }}
                </p>
              </div>
              <UButton
                v-if="!search"
                color="blue"
                icon="i-lucide-plus"
                @click="openCreate"
              >
                {{ t('services.list.actions.addService') }}
              </UButton>
            </div>
          </div>

          <div v-else-if="viewMode === 'cards'" key="service-cards" class="p-4 sm:p-6">
            <div class="grid gap-5 sm:grid-cols-2 sm:gap-6 xl:grid-cols-3">
              <div
                v-for="row in filteredRows"
                :key="row.id"
                class="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-5 sm:p-6 border border-slate-200 dark:border-slate-600 hover:shadow-md hover:border-blue-300 dark:hover:border-blue-600 transition-all duration-200 group"
              >
                <!-- Service Header -->
                <div class="flex items-start justify-between mb-4">
                  <div class="flex items-center gap-3">
                    <div class="relative">
                      <div class="h-12 w-12 rounded-xl bg-gradient-to-br from-blue-100 to-indigo-100 dark:from-blue-900/30 dark:to-indigo-900/30 flex items-center justify-center">
                        <UIcon name="i-lucide-briefcase-medical" class="h-6 w-6 text-blue-600 dark:text-blue-400" />
                      </div>
                    </div>
                    <div class="flex-1 min-w-0">
                      <h3 class="font-semibold text-slate-900 dark:text-white truncate">
                        {{ row.name }}
                      </h3>
                      <p v-if="row.secondaryName" class="text-sm text-slate-500 dark:text-slate-400 truncate">
                        {{ row.secondaryName }}
                      </p>
                    </div>
                  </div>
                </div>

                <!-- Service Details -->
                <div class="space-y-3 mb-4">
                  <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                    <UIcon name="i-lucide-hash" class="h-4 w-4 text-slate-400" />
                    <code class="rounded bg-slate-100 dark:bg-slate-600 px-2 py-0.5 text-xs font-mono text-slate-700 dark:text-slate-300">{{ row.slug }}</code>
                  </div>
                  <div v-if="row.summary" class="flex items-start gap-2 text-sm text-slate-600 dark:text-slate-300">
                    <UIcon name="i-lucide-file-text" class="h-4 w-4 text-slate-400 mt-0.5 flex-shrink-0" />
                    <p class="line-clamp-2 leading-relaxed">{{ row.summary }}</p>
                  </div>
                  <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                    <UIcon name="i-lucide-users" class="h-4 w-4 text-slate-400" />
                    <span>{{ t('services.list.cards.doctorCount', { count: row.doctorCount, plural: row.doctorCount === 1 ? '' : 's' }) }}</span>
                  </div>
                  <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                    <UIcon name="i-lucide-calendar-plus" class="h-4 w-4 text-slate-400" />
                    <span>{{ t('services.list.cards.addedDate', { date: row.createdAt }) }}</span>
                  </div>
                </div>

                <!-- Actions -->
                <div class="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-slate-600">
                  <div class="text-xs text-slate-500 dark:text-slate-400">
                    {{ t('services.list.cards.serviceId', { id: row.id }) }}
                  </div>
                  <div class="flex items-center gap-2">
                    <UButton
                      size="sm"
                      variant="ghost"
                      color="blue"
                      icon="i-lucide-edit"
                      @click="openEdit(row)"
                    >
                      {{ t('services.list.cards.edit') }}
                    </UButton>
                    <UButton
                      size="sm"
                      variant="ghost"
                      color="red"
                      icon="i-lucide-trash-2"
                      @click="confirmRemove(row)"
                    >
                      {{ t('services.list.cards.delete') }}
                    </UButton>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else key="service-table" class="p-4 sm:p-6 overflow-x-auto">
            <UTable :rows="filteredRows" :columns="columns" class="w-full min-w-[720px]">
              <template #name-data="{ row }">
                <div class="flex items-center gap-2.5">
                  <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-gradient-to-br from-blue-50 to-indigo-50 ring-2 ring-blue-100 dark:from-blue-900/30 dark:to-indigo-900/30 dark:ring-blue-800">
                    <UIcon name="i-lucide-briefcase-medical" class="h-4 w-4 text-blue-600 dark:text-blue-400" />
                  </div>
                  <div>
                    <p class="text-sm font-semibold text-slate-900 dark:text-white">{{ row.name }}</p>
                    <p v-if="row.secondaryName" class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{{ row.secondaryName }}</p>
                  </div>
                </div>
              </template>

              <template #summary-data="{ row }">
                <p class="text-xs text-slate-600 dark:text-slate-300 line-clamp-2 leading-relaxed">{{ row.summary }}</p>
              </template>

              <template #slug-data="{ row }">
                <div class="flex items-center gap-1.5">
                  <div class="flex h-5 w-5 items-center justify-center rounded bg-slate-100 dark:bg-slate-600">
                    <UIcon name="i-lucide-hash" class="h-3 w-3 text-slate-500 dark:text-slate-400" />
                  </div>
                  <code class="rounded bg-slate-100 dark:bg-slate-600 px-2 py-0.5 text-xs font-mono text-slate-700 dark:text-slate-300">{{ row.slug }}</code>
                </div>
              </template>

              <template #doctorCount-data="{ row }">
                <div class="flex items-center gap-1.5">
                  <div class="flex h-7 w-7 items-center justify-center rounded-lg bg-green-50 dark:bg-green-900/30">
                    <UIcon name="i-lucide-users" class="h-3.5 w-3.5 text-green-600 dark:text-green-400" />
                  </div>
                  <span class="text-xs font-semibold text-slate-900 dark:text-white">{{ row.doctorCount }}</span>
                </div>
              </template>

              <template #createdAt-data="{ row }">
                <div class="flex items-center gap-1.5">
                  <div class="flex h-7 w-7 items-center justify-center rounded-lg bg-purple-50 dark:bg-purple-900/30">
                    <UIcon name="i-lucide-calendar-plus" class="h-3.5 w-3.5 text-purple-600 dark:text-purple-400" />
                  </div>
                  <span class="text-xs font-medium text-slate-700 dark:text-slate-300">{{ row.createdAt }}</span>
                </div>
              </template>

              <template #actions-data="{ row }">
                <div class="flex justify-end gap-1">
                  <UButton size="xs" color="blue" variant="soft" icon="i-lucide-pencil" @click="openEdit(row)">
                    {{ t('services.list.cards.edit') }}
                  </UButton>
                  <UButton size="xs" color="red" variant="ghost" icon="i-lucide-trash-2" @click="confirmRemove(row)">
                    {{ t('services.list.cards.delete') }}
                  </UButton>
                </div>
              </template>
            </UTable>
          </div>
        </template>
      </div>

    <!-- Delete confirmation -->
    <UModal v-model="deleteOpen">
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-full bg-red-100 dark:bg-red-900/30">
              <UIcon name="i-lucide-alert-triangle" class="h-5 w-5 text-red-600 dark:text-red-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('services.list.deleteModal.title') }}</h3>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('services.list.deleteModal.subtitle') }}</p>
            </div>
          </div>
        </template>

        <div class="py-4">
          <p class="text-slate-700 dark:text-slate-300">
            {{ t('services.list.deleteModal.description', { name: deleteTarget?.name }) }}
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
              {{ t('services.list.deleteModal.cancel') }}
            </UButton>
            <UButton
              color="red"
              icon="i-lucide-trash-2"
              :loading="deleting"
              @click="handleDelete"
            >
              {{ t('services.list.deleteModal.confirm') }}
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import type { AdminServiceSummary } from "@/types/services";

definePageMeta({
  title: 'Services',
  requiresAuth: true
})

const { t, locale } = useI18n();

useHead({ title: t('services.list.meta.headTitle') });

const toast = useToast();
const { fetcher, request } = useAdminApi();
const router = useRouter();

const viewMode = ref<"table" | "cards">("table");
const { data, pending, refresh } = await useAsyncData("admin-services", () =>
  fetcher<AdminServiceSummary[] | { content?: AdminServiceSummary[]; items?: AdminServiceSummary[] }>("/services", [])
);

const services = computed(() => {
  const raw = data.value;
  if (!raw) return [];
  if (Array.isArray(raw)) return raw;
  return raw.content ?? raw.items ?? [];
});
const totalDoctors = computed(() => {
  const set = new Set(services.value.flatMap(service => (service.doctorCount ?? 0) > 0 ? [service.slug] : []));
  return set.size;
});
const recentlyUpdated = computed(() =>
  services.value.filter(service => service.createdAt && daysSince(service.createdAt) <= 30).length.toString()
);

const search = ref("");

const columns = [
  { key: "name", label: t('services.list.table.columns.name'), sortable: true, class: "min-w-[240px]" },
  { key: "summary", label: t('services.list.table.columns.summary'), class: "min-w-[260px] max-w-lg" },
  { key: "slug", label: t('services.list.table.columns.slug'), class: "w-40" },
  { key: "doctorCount", label: t('services.list.table.columns.doctorCount'), class: "w-32" },
  { key: "createdAt", label: t('services.list.table.columns.createdAt'), class: "w-40" },
  { key: "actions", label: t('services.list.table.columns.actions'), class: "w-32 text-right" }
];

const isArabic = computed(() => (locale.value ?? '').startsWith('ar'));

const dateFormatter = computed(() => new Intl.DateTimeFormat(locale.value || undefined, { dateStyle: "medium" }));

const rows = computed(() =>
  services.value.map(service => {
    const primaryName = isArabic.value ? service.nameAr : service.nameEn;
    const fallbackName = isArabic.value ? service.nameEn : service.nameAr;
    const primarySummary = isArabic.value ? service.summaryAr : service.summaryEn;
    const fallbackSummary = isArabic.value ? service.summaryEn : service.summaryAr;

    const hasSecondaryName =
      primaryName &&
      fallbackName &&
      primaryName.trim() &&
      fallbackName.trim() &&
      primaryName.trim() !== fallbackName.trim();

    const hasSecondarySummary =
      primarySummary &&
      fallbackSummary &&
      primarySummary.trim() &&
      fallbackSummary.trim() &&
      primarySummary.trim() !== fallbackSummary.trim();

    return {
      id: service.id,
      slug: service.slug,
      nameEn: service.nameEn,
      nameAr: service.nameAr,
      name: primaryName?.trim() || fallbackName?.trim() || "—",
      secondaryName: hasSecondaryName ? fallbackName : null,
      summaryEn: service.summaryEn,
      summaryAr: service.summaryAr,
      summary: primarySummary?.trim() || fallbackSummary?.trim() || "—",
      secondarySummary: hasSecondarySummary ? fallbackSummary : null,
      doctorCount: service.doctorCount ?? 0,
      createdAt: formatTimestamp(service.createdAt)
    };
  })
);

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

const filteredRows = computed(() => {
  if (!search.value.trim()) {
    return rows.value;
  }
  const term = search.value.toLowerCase();
  return rows.value.filter(row =>
    [row.name, row.secondaryName, row.summary, row.secondarySummary, row.slug].some(value =>
      value?.toLowerCase().includes(term)
    )
  );
});

const deleteOpen = ref(false);
const deleting = ref(false);
const deleteTarget = ref<{ id: number; name: string } | null>(null);

function openCreate() {
  router.push("/services/new");
}

function openEdit(row: (typeof rows.value)[number]) {
  router.push(`/services/${row.id}`);
}

function confirmRemove(row: (typeof rows.value)[number]) {
  deleteTarget.value = { id: row.id, name: row.name };
  deleteOpen.value = true;
}

function daysSince(value: string | number) {
  // Handle Unix timestamps (in seconds) and ISO strings
  let timestamp: number;
  if (typeof value === 'number') {
    // Unix timestamp: if less than 10 billion, it's in seconds (dates before 2286)
    timestamp = value < 10000000000 ? value * 1000 : value;
  } else {
    timestamp = new Date(value).getTime();
  }
  const diff = Date.now() - timestamp;
  return Math.floor(diff / (1000 * 60 * 60 * 24));
}

async function handleDelete() {
  if (!deleteTarget.value) return;
  deleting.value = true;
  try {
    await request(`/services/${deleteTarget.value.id}`, {
      method: "DELETE"
    });
    toast.add({ title: t('services.list.toasts.deleteSuccess') });
    deleteOpen.value = false;
    deleteTarget.value = null;
    await refresh();
  } catch (error: any) {
    toast.add({
      title: t('services.list.toasts.deleteError.title'),
      description: error?.data?.message ?? error?.message ?? t('services.list.toasts.deleteError.description'),
      color: "red"
    });
  } finally {
    deleting.value = false;
  }
}
</script>
