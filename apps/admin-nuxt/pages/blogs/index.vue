<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-newspaper" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('blogs.list.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('blogs.list.header.subtitle') }}</p>
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
              {{ t('blogs.list.actions.refresh') }}
            </UButton>
            <UButton 
              color="blue" 
              icon="i-lucide-plus" 
              @click="openCreate"
            >
              {{ t('blogs.list.actions.create') }}
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
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
              <UIcon name="i-lucide-newspaper" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('blogs.list.metrics.totalPosts.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ blogs?.length || 0 }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('blogs.list.metrics.totalPosts.caption') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
              <UIcon name="i-lucide-check-circle" class="h-5 w-5 text-green-600 dark:text-green-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('blogs.list.metrics.published.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ publishedCount }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('blogs.list.metrics.published.caption') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-900/20">
              <UIcon name="i-lucide-edit" class="h-5 w-5 text-amber-600 dark:text-amber-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('blogs.list.metrics.drafts.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ draftCount }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('blogs.list.metrics.drafts.caption') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-50 dark:bg-purple-900/20">
              <UIcon name="i-lucide-eye" class="h-5 w-5 text-purple-600 dark:text-purple-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('blogs.list.metrics.totalViews.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ formatNumber(lifetimeViews) }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('blogs.list.metrics.totalViews.caption') }}</p>
        </div>
      </div>

      <!-- Search and Filters -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden mb-6">
        <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-search" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('blogs.list.filters.title') }}</h2>
              <p class="text-sm text-slate-300">{{ t('blogs.list.filters.subtitle') }}</p>
            </div>
          </div>
        </div>
        <div class="p-6">
          <div class="flex flex-col sm:flex-row gap-4 mb-6">
            <div class="flex-1">
              <UFormGroup :label="t('blogs.list.filters.searchLabel')">
                <UInput
                  v-model="search"
                  size="lg"
                  :placeholder="t('blogs.list.filters.searchPlaceholder')"
                  icon="i-lucide-search"
                />
              </UFormGroup>
            </div>
            <div class="sm:w-48">
              <UFormGroup :label="t('blogs.list.filters.viewModeLabel')">
                <UButtonGroup size="lg" class="w-full">
                  <UButton
                    :variant="viewMode === 'cards' ? 'solid' : 'ghost'"
                    :color="viewMode === 'cards' ? 'blue' : 'gray'"
                    icon="i-lucide-layout-grid"
                    class="flex-1"
                    @click="viewMode = 'cards'"
                  >
                    {{ t('blogs.list.filters.viewModeCards') }}
                  </UButton>
                  <UButton
                    :variant="viewMode === 'table' ? 'solid' : 'ghost'"
                    :color="viewMode === 'table' ? 'blue' : 'gray'"
                    icon="i-lucide-table-2"
                    class="flex-1"
                    @click="viewMode = 'table'"
                  >
                    {{ t('blogs.list.filters.viewModeTable') }}
                  </UButton>
                </UButtonGroup>
              </UFormGroup>
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-2 mb-4">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">{{ t('blogs.list.filters.statusLabel') }}</span>
            <UButton
              v-for="status in statuses"
              :key="status"
              size="xs"
              :variant="selectedStatuses.includes(status) ? 'solid' : 'soft'"
              :color="selectedStatuses.includes(status) ? 'blue' : 'gray'"
              class="rounded-full"
              @click="toggleStatus(status)"
            >
              {{ status }}
            </UButton>
          </div>

          <div class="flex flex-wrap items-center gap-2 mb-4">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">{{ t('blogs.list.filters.languageLabel') }}</span>
            <UButton
              v-for="lang in languages"
              :key="lang.code"
              size="xs"
              :variant="selectedLanguages.includes(lang.code) ? 'solid' : 'soft'"
              :color="selectedLanguages.includes(lang.code) ? 'blue' : 'gray'"
              class="rounded-full uppercase"
              @click="toggleLanguage(lang.code)"
            >
              {{ lang.name }}
            </UButton>
          </div>

          <div v-if="activeFiltersCount" class="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-blue-100 dark:border-blue-800 bg-blue-50/80 dark:bg-blue-900/20 px-4 py-2 text-xs text-blue-700 dark:text-blue-300">
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-filter" class="h-4 w-4" />
              <span>{{ t('blogs.list.filters.resultsFound', { count: filteredRows.length }) }}</span>
            </div>
            <UButton size="xs" variant="ghost" color="blue" @click="clearFilters">
              {{ t('blogs.list.filters.resetFilters') }}
            </UButton>
          </div>
        </div>
      </div>

      <!-- Blog Posts Display -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-newspaper" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('blogs.list.display.title') }}</h2>
              <p class="text-sm text-blue-100">{{ t('blogs.list.display.subtitle', { count: filteredRows.length }) }}</p>
            </div>
          </div>
        </div>

        <div v-if="pending" class="p-6">
          <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            <div v-for="i in 6" :key="i" class="animate-pulse">
              <div class="bg-slate-200 dark:bg-slate-700 rounded-xl p-6 space-y-4">
                <div class="h-32 bg-slate-300 dark:bg-slate-600 rounded-xl"></div>
                <div class="space-y-2">
                  <div class="h-4 bg-slate-300 dark:bg-slate-600 rounded w-3/4"></div>
                  <div class="h-3 bg-slate-300 dark:bg-slate-600 rounded w-1/2"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="!filteredRows.length" class="p-12 text-center">
          <div class="flex flex-col items-center gap-4">
            <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
              <UIcon name="i-lucide-newspaper" class="h-8 w-8 text-slate-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('blogs.list.empty.title') }}</h3>
              <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                {{ search || selectedStatuses.length || selectedLanguages.length ? t('blogs.list.empty.searchHint') : t('blogs.list.empty.createHint') }}
              </p>
            </div>
            <div class="flex flex-wrap items-center gap-2">
              <UButton
                v-if="search || selectedStatuses.length || selectedLanguages.length"
                variant="outline"
                color="gray"
                size="sm"
                @click="clearFilters"
              >
                {{ t('blogs.list.empty.resetFilters') }}
              </UButton>
              <UButton color="blue" size="sm" icon="i-lucide-plus" @click="openCreate">
                {{ t('blogs.list.empty.createPost') }}
              </UButton>
            </div>
          </div>
        </div>

        <div v-else-if="viewMode === 'cards'" key="blog-cards" class="p-6">
          <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            <div
              v-for="row in filteredRows"
              :key="row.id"
              class="bg-slate-50 dark:bg-slate-700/50 rounded-xl border border-slate-200 dark:border-slate-600 hover:shadow-md hover:border-blue-300 dark:hover:border-blue-600 transition-all duration-200 group overflow-hidden"
            >
              <!-- Blog Header -->
              <div class="relative h-44 w-full overflow-hidden bg-slate-100 dark:bg-slate-600">
                <img
                  v-if="row.featuredImage"
                  :src="row.featuredImage"
                  :alt="row.title"
                  class="h-full w-full object-cover transition group-hover:scale-[1.03]"
                />
                <div
                  v-else
                  class="flex h-full w-full items-center justify-center bg-gradient-to-br from-blue-100 to-indigo-100 dark:from-blue-900/30 dark:to-indigo-900/30 text-sm font-medium text-blue-600 dark:text-blue-400"
                >
                  {{ t('blogs.list.display.noCoverImage') }}
                </div>

                <div class="absolute left-3 top-3 flex flex-wrap items-center gap-2">
                  <UBadge :color="getStatusColor(row.status)" variant="soft" size="xs">
                    {{ row.status }}
                  </UBadge>
                  <UBadge color="gray" variant="soft" size="xs" class="uppercase">
                    {{ row.locale }}
                  </UBadge>
                </div>
              </div>

              <!-- Blog Content -->
              <div class="flex flex-1 flex-col gap-4 p-6">
                <div class="space-y-2">
                  <button class="text-left w-full" @click="openEdit(row)">
                    <h3 class="line-clamp-2 text-base font-semibold text-slate-900 dark:text-white transition group-hover:text-blue-600 dark:group-hover:text-blue-400">
                      {{ row.title }}
                    </h3>
                    <p v-if="row.excerpt" class="mt-1 line-clamp-3 text-sm text-slate-500 dark:text-slate-400">
                      {{ row.excerpt }}
                    </p>
                  </button>
                </div>

                <div class="flex flex-wrap items-center gap-3 text-xs text-slate-500 dark:text-slate-400">
                  <div class="flex items-center gap-1.5">
                    <UIcon name="i-lucide-eye" class="h-3.5 w-3.5 text-slate-400" />
                    <span>{{ formatNumber(row.viewCount ?? 0) }} {{ t('blogs.list.display.views') }}</span>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <UIcon name="i-lucide-calendar" class="h-3.5 w-3.5 text-slate-400" />
                    <span>{{ row.primaryDateLabel }}</span>
                  </div>
                </div>

                <!-- Actions -->
                <div class="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-slate-600">
                  <div class="text-xs text-slate-500 dark:text-slate-400">
                    {{ row.authorName || t('blogs.list.display.unknownAuthor') }}
                  </div>
                  <div class="flex items-center gap-2">
                    <UButton
                      size="sm"
                      variant="ghost"
                      color="blue"
                      icon="i-lucide-edit"
                      @click="openEdit(row)"
                    >
                      {{ t('blogs.list.display.edit') }}
                    </UButton>
                    <UButton
                      size="sm"
                      variant="ghost"
                      color="red"
                      icon="i-lucide-trash-2"
                      @click="confirmRemove(row)"
                    >
                      {{ t('blogs.list.display.delete') }}
                    </UButton>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else key="blog-table" class="p-6">
          <UTable :columns="tableColumns" :rows="tableRows" class="w-full">
            <template #title-data="{ row }">
              <button class="flex flex-col text-left" @click="openEdit(row.source)">
                <span class="text-sm font-semibold text-slate-900 dark:text-white">{{ row.title }}</span>
                <span class="text-xs text-slate-500 dark:text-slate-400">{{ row.slug }}</span>
              </button>
            </template>
            <template #status-data="{ row }">
              <UBadge :color="getStatusColor(row.status)" variant="soft" size="xs">
                {{ row.status }}
              </UBadge>
            </template>
            <template #locale-data="{ row }">
              <span class="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">{{ row.locale }}</span>
            </template>
            <template #views-data="{ row }">
              <div class="flex items-center gap-1.5 text-xs text-slate-600 dark:text-slate-300">
                <UIcon name="i-lucide-eye" class="h-3.5 w-3.5 text-slate-400" />
                <span>{{ formatNumber(row.views) }}</span>
              </div>
            </template>
            <template #created-data="{ row }">
              <div class="flex items-center gap-1.5 text-xs text-slate-600 dark:text-slate-300">
                <UIcon name="i-lucide-calendar" class="h-3.5 w-3.5 text-slate-400" />
                <span>{{ row.created }}</span>
              </div>
            </template>
            <template #actions-data="{ row }">
              <div class="flex justify-end gap-1">
                <UButton
                  v-if="row.source.status === 'DRAFT'"
                  size="xs"
                  color="green"
                  variant="soft"
                  icon="i-lucide-check-circle"
                  @click="handlePublish(row.source)"
                >
                  {{ t('blogs.list.table.actions.publish') }}
                </UButton>
                <UButton
                  v-else-if="row.source.status === 'PUBLISHED'"
                  size="xs"
                  color="amber"
                  variant="soft"
                  icon="i-lucide-archive"
                  @click="handleUnpublish(row.source)"
                >
                  {{ t('blogs.list.table.actions.unpublish') }}
                </UButton>
                <UButton size="xs" color="blue" variant="soft" icon="i-lucide-edit-3" @click="openEdit(row.source)">
                  {{ t('blogs.list.table.actions.edit') }}
                </UButton>
                <UButton size="xs" color="red" variant="ghost" icon="i-lucide-trash-2" @click="confirmRemove(row.source)">
                  {{ t('blogs.list.table.actions.delete') }}
                </UButton>
              </div>
            </template>
          </UTable>
        </div>
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
                <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('blogs.list.deleteModal.title') }}</h3>
                <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('blogs.list.deleteModal.subtitle') }}</p>
              </div>
            </div>
          </template>

          <div class="py-4">
            <p class="text-slate-700 dark:text-slate-300">
              {{ t('blogs.list.deleteModal.content', { title: deleteTarget?.title }) }}
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
                {{ t('blogs.list.deleteModal.cancel') }}
              </UButton>
              <UButton
                color="red"
                icon="i-lucide-trash-2"
                :loading="deleting"
                @click="handleDelete"
              >
                {{ t('blogs.list.deleteModal.confirm') }}
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
import type { Blog } from "@/types/blogs";
import { parseDateValue } from "~/utils/dateUtils";

const { t } = useI18n();

useHead(() => ({
  title: t('blogs.list.meta.title')
}));

const toast = useEnhancedToast();
const router = useRouter();
const blogApi = useBlogs();

const { data, pending, refresh } = await useAsyncData("admin-blogs", () =>
  blogApi.getAllBlogs()
);

const blogs = computed(() => data.value ?? []);

const publishedCount = computed(() =>
  blogs.value.filter(blog => blog.status === "PUBLISHED").length
);
const draftCount = computed(() =>
  blogs.value.filter(blog => blog.status === "DRAFT").length
);

const lifetimeViews = computed(() =>
  blogs.value.reduce((total, blog) => total + (blog.viewCount ?? 0), 0)
);

const publishedViews = computed(() =>
  blogs.value
    .filter(blog => blog.status === "PUBLISHED")
    .reduce((total, blog) => total + (blog.viewCount ?? 0), 0)
);

const averageViews = computed(() => {
  if (!publishedCount.value) {
    return 0;
  }

  return Math.round(publishedViews.value / publishedCount.value);
});

const toTime = (value: string | number | Date | null | undefined) => {
  const date = parseDateValue(value);
  return date ? date.getTime() : 0;
};

const getPrimaryDate = (entry: Blog) => entry.publishedAt ?? entry.createdAt;

// Use clinic timezone for all date/time displays
// CRITICAL: All admins see times in clinic timezone, not their browser timezone
const { timezone } = useClinicTimezone();

const formatDate = (date: string | number | null | undefined) => {
  if (!date) return "—";
  // Use clinic timezone formatter from dateUtils
  return formatDateInClinicTimezone(date, timezone.value);
};

const formatNumber = (value: number) => new Intl.NumberFormat().format(value);

const rows = computed(() =>
  blogs.value.map(blog => {
    const primaryDate = getPrimaryDate(blog);
    const createdDate = blog.createdAt ?? null;

    return {
      ...blog,
      primaryDate,
      primaryDateValue: toTime(primaryDate),
      primaryDateLabel: formatDate(primaryDate),
      createdLabel: formatDate(createdDate)
    };
  })
);

const search = ref("");
const selectedStatuses = ref<string[]>([]);
const selectedLanguages = ref<string[]>([]);
const viewMode = ref<"cards" | "table">("cards");
const sortOption = ref<"newest" | "oldest" | "views">("newest");

const statuses = ["DRAFT", "PUBLISHED", "ARCHIVED"];
const languages = [
  { code: "en", name: "English" },
  { code: "ar", name: "Arabic" }
];

const sortOptions = [
  { value: "newest", label: "Newest first", icon: "i-lucide-sparkles" },
  { value: "oldest", label: "Oldest first", icon: "i-lucide-history" },
  { value: "views", label: "Most viewed", icon: "i-lucide-flame" }
];

const selectedSortOption = computed(
  () => sortOptions.find(option => option.value === sortOption.value) ?? sortOptions[0]
);

const activeFiltersCount = computed(
  () =>
    selectedStatuses.value.length +
    selectedLanguages.value.length +
    (search.value.trim() ? 1 : 0)
);

const filteredRows = computed(() => {
  const term = search.value.toLowerCase();
  const filtered = rows.value.filter(row => {
    const matchesSearch =
      !term ||
      [row.title, row.slug, row.excerpt, row.content]
        .filter(Boolean)
        .some(value => value?.toLowerCase().includes(term));

    const matchesStatus =
      !selectedStatuses.value.length ||
      selectedStatuses.value.includes(row.status);

    const matchesLanguage =
      !selectedLanguages.value.length ||
      selectedLanguages.value.includes(row.locale);

    return matchesSearch && matchesStatus && matchesLanguage;
  });

  return filtered.sort((a, b) => {
    if (sortOption.value === "views") {
      return (b.viewCount ?? 0) - (a.viewCount ?? 0);
    }

    if (sortOption.value === "oldest") {
      return a.primaryDateValue - b.primaryDateValue;
    }

    return b.primaryDateValue - a.primaryDateValue;
  });
});

const tableColumns = computed(() => [
  { key: "title", label: t('blogs.list.table.columns.title') },
  { key: "status", label: t('blogs.list.table.columns.status') },
  { key: "locale", label: t('blogs.list.table.columns.language') },
  { key: "views", label: t('blogs.list.table.columns.views') },
  { key: "created", label: t('blogs.list.table.columns.published') },
  { key: "actions", label: t('blogs.list.table.columns.actions'), class: "w-64 text-right" }
]);

const tableRows = computed(() =>
  filteredRows.value.map(row => ({
    id: row.id,
    title: row.title,
    slug: row.slug,
    status: row.status,
    locale: row.locale,
    views: row.viewCount ?? 0,
    created: row.primaryDateLabel,
    source: row
  }))
);

const languageStats = computed(() => {
  if (!blogs.value.length) {
    return [];
  }

  const total = blogs.value.length;
  const counts = blogs.value.reduce<Record<string, number>>((acc, blog) => {
    if (blog.locale) {
      acc[blog.locale] = (acc[blog.locale] ?? 0) + 1;
    }
    return acc;
  }, {});

  return Object.entries(counts)
    .map(([code, count]) => {
      const ratio = (count / total) * 100;
      return {
        code,
        count,
        label: languages.find(language => language.code === code)?.name ?? code.toUpperCase(),
        percentage: Math.round(ratio),
        fill: Math.min(100, Math.max(12, Math.round(ratio)))
      };
    })
    .sort((a, b) => b.count - a.count);
});

const activeLanguageCount = computed(() => languageStats.value.length);

const statusBreakdown = computed(() =>
  statuses.map(status => ({
    status,
    count: blogs.value.filter(blog => blog.status === status).length
  }))
);

const recentPosts = computed(() =>
  [...rows.value]
    .sort((a, b) => b.primaryDateValue - a.primaryDateValue)
    .slice(0, 5)
);

const topViewedPost = computed<Blog | null>(() =>
  blogs.value.reduce<Blog | null>((top, blog) => {
    const views = blog.viewCount ?? 0;
    if (!top || views > (top.viewCount ?? 0)) {
      return blog;
    }
    return top;
  }, null)
);

const deleteOpen = ref(false);
const deleting = ref(false);
const deleteTarget = ref<Blog | null>(null);

function toggleStatus(value: string) {
  selectedStatuses.value = selectedStatuses.value.includes(value)
    ? selectedStatuses.value.filter(item => item !== value)
    : [...selectedStatuses.value, value];
}

function toggleLanguage(value: string) {
  selectedLanguages.value = selectedLanguages.value.includes(value)
    ? selectedLanguages.value.filter(item => item !== value)
    : [...selectedLanguages.value, value];
}

function clearFilters() {
  search.value = "";
  selectedStatuses.value = [];
  selectedLanguages.value = [];
}

function openCreate() {
  router.push("/blogs/new");
}

function openEdit(row: Blog) {
  router.push(`/blogs/${row.id}`);
}

function confirmRemove(row: Blog) {
  deleteTarget.value = row;
  deleteOpen.value = true;
}

function getStatusColor(status: string) {
  switch (status) {
    case "PUBLISHED":
      return "green";
    case "DRAFT":
      return "amber";
    case "ARCHIVED":
      return "gray";
    default:
      return "gray";
  }
}

function getStatusIndicatorClass(status: string) {
  switch (getStatusColor(status)) {
    case "green":
      return "bg-green-500";
    case "amber":
      return "bg-amber-500";
    case "gray":
      return "bg-slate-400";
    default:
      return "bg-slate-400";
  }
}

function getRowActions(row: Blog) {
  const actions = [
    [
      {
        label: "Edit Post",
        icon: "i-lucide-pencil",
        click: () => openEdit(row)
      }
    ]
  ];

  if (row.status === "DRAFT") {
    actions.push([
      {
        label: "Publish",
        icon: "i-lucide-check-circle",
        click: () => handlePublish(row)
      }
    ]);
  } else if (row.status === "PUBLISHED") {
    actions.push([
      {
        label: "Unpublish",
        icon: "i-lucide-archive",
        click: () => handleUnpublish(row)
      }
    ]);
  }

  actions.push([
    {
      label: "Delete",
      icon: "i-lucide-trash-2",
      click: () => confirmRemove(row),
      class: "text-red-600"
    }
  ]);

  return actions;
}

async function handlePublish(blog: Blog) {
  try {
    await blogApi.publishBlog(blog.id);
    toast.success({ title: t('blogs.list.toasts.publishSuccess') });
    await refresh();
  } catch (error: any) {
    toast.error({
      title: t('blogs.list.toasts.publishError'),
      error
    });
  }
}

async function handleUnpublish(blog: Blog) {
  try {
    await blogApi.unpublishBlog(blog.id);
    toast.success({ title: t('blogs.list.toasts.unpublishSuccess') });
    await refresh();
  } catch (error: any) {
    toast.error({
      title: t('blogs.list.toasts.unpublishError'),
      error
    });
  }
}

async function handleDelete() {
  if (!deleteTarget.value) return;
  deleting.value = true;
  try {
    await blogApi.deleteBlog(deleteTarget.value.id);
    toast.success({ title: t('blogs.list.toasts.deleteSuccess') });
    deleteOpen.value = false;
    deleteTarget.value = null;
    await refresh();
  } catch (error: any) {
    toast.error({
      title: t('blogs.list.toasts.deleteError'),
      error
    });
  } finally {
    deleting.value = false;
  }
}
</script>
