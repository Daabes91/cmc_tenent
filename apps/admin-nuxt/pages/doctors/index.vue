<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-emerald-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 shadow-lg">
              <UIcon name="i-lucide-stethoscope" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t("doctors.list.overview.title") }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t("doctors.list.overview.subtitle") }}</p>
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
              {{ t("doctors.list.actions.refresh") }}
            </UButton>
            <UButton 
              color="emerald" 
              icon="i-lucide-user-plus" 
              @click="openCreate"
            >
              {{ t("doctors.list.actions.add") }}
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
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-50 dark:bg-emerald-900/20">
              <UIcon name="i-lucide-users" class="h-5 w-5 text-emerald-600 dark:text-emerald-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t("doctors.list.metrics.totalDoctors.label") }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ doctors.length }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t("doctors.list.metrics.totalDoctors.caption") }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
              <UIcon name="i-lucide-graduation-cap" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t("doctors.list.metrics.specialties.label") }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ uniqueSpecialties }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t("doctors.list.metrics.specialties.caption") }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-50 dark:bg-purple-900/20">
              <UIcon name="i-lucide-layers" class="h-5 w-5 text-purple-600 dark:text-purple-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t("doctors.list.metrics.services.label") }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ uniqueServices }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t("doctors.list.metrics.services.caption") }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-orange-50 dark:bg-orange-900/20">
              <UIcon name="i-lucide-globe" class="h-5 w-5 text-orange-600 dark:text-orange-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t("doctors.list.metrics.languages.label") }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ uniqueLanguages }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t("doctors.list.metrics.languages.caption") }}</p>
        </div>
      </div>

      <!-- Search and Filters -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden mb-6">
        <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-search" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t("doctors.list.filters.title") }}</h2>
              <p class="text-sm text-slate-300">{{ t("doctors.list.filters.subtitle") }}</p>
            </div>
          </div>
        </div>
        <div class="p-6">
          <div class="flex flex-col gap-4">
            <div class="flex flex-col sm:flex-row gap-4">
              <div class="flex-1">
                <UFormGroup :label="t('doctors.list.filters.searchLabel')">
                  <UInput
                    v-model="search"
                    size="lg"
                    :placeholder="t('doctors.list.filters.searchPlaceholder')"
                    icon="i-lucide-search"
                  />
                </UFormGroup>
              </div>
              <div class="sm:w-48">
                <UFormGroup :label="t('doctors.list.filters.viewMode')">
                  <UButtonGroup size="lg" class="w-full">
                    <UButton
                      :variant="viewMode === 'cards' ? 'solid' : 'ghost'"
                      :color="viewMode === 'cards' ? 'emerald' : 'gray'"
                      icon="i-lucide-layout-grid"
                      @click="viewMode = 'cards'"
                      class="flex-1"
                    >
                      {{ t("doctors.list.filters.cardsView") }}
                    </UButton>
                    <UButton
                      :variant="viewMode === 'table' ? 'solid' : 'ghost'"
                      :color="viewMode === 'table' ? 'emerald' : 'gray'"
                      icon="i-lucide-table-2"
                      @click="viewMode = 'table'"
                      class="flex-1"
                    >
                      {{ t("doctors.list.filters.tableView") }}
                    </UButton>
                  </UButtonGroup>
                </UFormGroup>
              </div>
            </div>

            <div v-if="specialties.length" class="flex flex-wrap items-center gap-2">
              <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t("doctors.list.filters.specialty") }}</span>
              <UButton
                v-for="specialty in specialties"
                :key="specialty"
                size="xs"
                :variant="selectedSpecialties.includes(specialty) ? 'solid' : 'soft'"
                :color="selectedSpecialties.includes(specialty) ? 'emerald' : 'gray'"
                class="rounded-full"
                @click="toggleSpecialty(specialty)"
              >
                {{ specialty }}
              </UButton>
            </div>

            <div v-if="languageOptions.length" class="flex flex-wrap items-center gap-2">
              <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t("doctors.list.filters.language") }}</span>
              <UButton
                v-for="option in languageOptions"
                :key="option.code"
                size="xs"
                :variant="selectedLanguages.includes(option.code) ? 'solid' : 'soft'"
                :color="selectedLanguages.includes(option.code) ? 'emerald' : 'gray'"
                class="rounded-full"
                @click="toggleLanguage(option.code)"
              >
                {{ option.label }}
              </UButton>
            </div>

            <div v-if="activeFiltersCount" class="flex items-center justify-between rounded-xl bg-emerald-50 dark:bg-emerald-900/20 px-4 py-2 text-xs text-emerald-700 dark:text-emerald-300">
              <div class="flex items-center gap-2">
                <UIcon name="i-lucide-filter" class="h-4 w-4" />
                <span>{{ t("doctors.list.filters.matchCount", { count: filteredRows.length }) }}</span>
              </div>
              <UButton size="xs" variant="ghost" color="emerald" @click="clearFilters">
                {{ t("doctors.list.filters.clear") }}
              </UButton>
            </div>
          </div>
        </div>
      </div>

      <!-- Doctors Grid -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-emerald-500 to-teal-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-stethoscope" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t("doctors.list.directory.title") }}</h2>
              <p class="text-sm text-emerald-100">{{ t("doctors.list.directory.count", { count: filteredRows.length }) }}</p>
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
            key="doctor-cards"
            class="grid gap-6 md:grid-cols-2 lg:grid-cols-3"
          >
            <div
              v-for="row in filteredRows"
              :key="row.id"
              class="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-6 border border-slate-200 dark:border-slate-600 hover:shadow-md hover:border-emerald-300 dark:hover:border-emerald-600 transition-all duration-200 group"
            >
              <!-- Doctor Header -->
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center gap-3">
                  <div class="relative">
                    <UAvatar 
                      :src="row.imageUrl" 
                      :alt="row.fullName" 
                      size="lg" 
                      class="ring-2 ring-emerald-100 dark:ring-emerald-800" 
                    />
                    <div class="absolute -top-1 -right-1 w-4 h-4 rounded-full border-2 border-white dark:border-slate-700 bg-green-500"></div>
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="font-semibold text-slate-900 dark:text-white truncate">
                      {{ row.fullName }}
                    </h3>
                    <p class="text-sm text-slate-500 dark:text-slate-400 truncate">
                      {{ row.specialty ?? t("doctors.list.labels.specialtyNotSet") }}
                    </p>
                  </div>
                </div>
                <UDropdown :items="getRowActions(row)" :popper="{ placement: 'bottom-end' }">
                  <UButton icon="i-lucide-more-vertical" variant="ghost" color="gray" size="xs" @click.stop />
                </UDropdown>
              </div>

              <!-- Services -->
              <div class="space-y-3 mb-4">
                <div class="flex flex-wrap gap-2">
                  <UBadge
                    v-for="service in row.services.slice(0, 3)"
                    :key="service.id"
                    color="emerald"
                    variant="soft"
                    size="xs"
                  >
                    {{ service.label ?? service.slug }}
                  </UBadge>
                  <UBadge
                    v-if="row.services.length > 3"
                    color="gray"
                    variant="soft"
                    size="xs"
                  >
                    {{ t("doctors.list.labels.moreServices", { count: row.services.length - 3 }) }}
                  </UBadge>
                </div>
                
                <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                  <UIcon name="i-lucide-globe" class="h-4 w-4 text-slate-400" />
                  <span class="text-xs tracking-wide">
                    {{ row.languagesCard || t("doctors.list.labels.languagesPending") }}
                  </span>
                </div>
                
                <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                  <UIcon name="i-lucide-layers" class="h-4 w-4 text-slate-400" />
                  <span>{{ t("doctors.list.labels.serviceCount", { count: row.services.length }) }}</span>
                </div>
              </div>

              <!-- Actions -->
              <div class="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-slate-600">
                <div
                  class="text-xs text-slate-500 dark:text-slate-400"
                  :title="t('doctors.list.labels.addedTooltip', { date: row.createdAt })"
                >
                  {{ t("doctors.list.labels.addedOn", { date: row.createdAt }) }}
                </div>
                <div class="flex items-center gap-2">
                  <UButton
                    size="sm"
                    variant="ghost"
                    color="emerald"
                    icon="i-lucide-edit"
                    @click="openEdit(row)"
                  >
                    {{ t("doctors.list.actions.edit") }}
                  </UButton>
                  <UButton
                    size="sm"
                    variant="ghost"
                    color="red"
                    icon="i-lucide-trash-2"
                    @click="confirmRemove(row)"
                  >
                    {{ t("doctors.list.actions.remove") }}
                  </UButton>
                </div>
              </div>
            </div>
          </div>

          <div v-else key="doctor-table">
            <UTable :columns="tableColumns" :rows="tableRows">
              <template #doctor-data="{ row }">
                <button class="flex flex-col text-left" @click="openEdit(row.source)">
                  <span class="text-sm font-semibold text-slate-900 dark:text-white">{{ row.doctor }}</span>
                  <span class="text-xs text-slate-500 dark:text-slate-400">{{ row.email ?? t("doctors.list.labels.noEmail") }}</span>
                </button>
              </template>
              <template #services-data="{ row }">
                <span class="text-xs text-slate-600 dark:text-slate-300">{{ row.services }}</span>
              </template>
              <template #languages-data="{ row }">
                <span class="text-xs tracking-wide text-slate-500 dark:text-slate-400">{{ row.languages }}</span>
              </template>
              <template #actions-data="{ row }">
                <div class="flex justify-end gap-1">
                  <UButton size="xs" color="emerald" variant="soft" icon="i-lucide-edit-3" @click="openEdit(row.source)">
                    {{ t("doctors.list.actions.edit") }}
                  </UButton>
                  <UButton size="xs" color="red" variant="ghost" icon="i-lucide-trash-2" @click="confirmRemove(row.source)">
                    {{ t("doctors.list.actions.remove") }}
                  </UButton>
                </div>
              </template>
            </UTable>
          </div>
        </div>

        <div v-else class="p-12 text-center">
          <div class="flex flex-col items-center gap-4">
            <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
              <UIcon name="i-lucide-stethoscope" class="h-8 w-8 text-slate-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t("doctors.list.empty.title") }}</h3>
              <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                {{
                  search || selectedSpecialties.length || selectedLanguages.length
                    ? t("doctors.list.empty.searchHint")
                    : t("doctors.list.empty.subtitle")
                }}
              </p>
            </div>
            <div class="flex flex-wrap items-center gap-2">
              <UButton
                v-if="search || selectedSpecialties.length || selectedLanguages.length"
                variant="outline"
                color="gray"
                @click="clearFilters"
              >
                {{ t("doctors.list.empty.reset") }}
              </UButton>
              <UButton
                color="emerald"
                icon="i-lucide-user-plus"
                @click="openCreate"
              >
                {{ t("doctors.list.empty.create") }}
              </UButton>
            </div>
          </div>
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
                <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t("doctors.list.delete.title") }}</h3>
                <p class="text-sm text-slate-600 dark:text-slate-300">{{ t("doctors.list.delete.subtitle") }}</p>
              </div>
            </div>
          </template>

          <div class="py-4">
            <p class="text-slate-700 dark:text-slate-300">
              {{ t("doctors.list.delete.prompt", { name: deleteTarget?.fullName ?? "—" }) }}
            </p>
            <div class="mt-3 rounded-lg bg-red-50 dark:bg-red-900/20 p-3 text-xs text-red-700 dark:text-red-300">
              <UIcon name="i-lucide-alert-triangle" class="mr-1 inline h-3.5 w-3.5" />
              {{ t("doctors.list.delete.impact") }}
            </div>
          </div>

          <template #footer>
            <div class="flex justify-end gap-3">
              <UButton
                variant="ghost"
                color="gray"
                @click="deleteOpen = false"
                :disabled="deleting"
              >
                {{ t("common.actions.cancel") }}
              </UButton>
              <UButton
                color="red"
                icon="i-lucide-trash-2"
                :loading="deleting"
                @click="handleDelete"
              >
                {{ t("doctors.list.delete.confirmAction") }}
              </UButton>
            </div>
          </template>
        </UCard>
      </UModal>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { DoctorAdmin, DoctorServiceReference } from "@/types/doctors";

const { t, locale } = useI18n();

useHead(() => ({
  title: t("doctors.list.meta.title")
}));

const toast = useEnhancedToast();
const { fetcher, request } = useAdminApi();
const router = useRouter();

const { data, pending, refresh } = await useAsyncData("admin-doctors", () =>
  fetcher<DoctorAdmin[]>("/doctors", [])
);

const doctors = computed(() => data.value ?? []);

const specialtyList = computed(() => {
  const useArabic = locale.value?.startsWith("ar");

  const set = new Set(
    doctors.value
      .map(doctor => {
        const primary = useArabic ? doctor.specialtyAr : doctor.specialtyEn;
        const secondary = useArabic ? doctor.specialtyEn : doctor.specialtyAr;
        const legacy = (doctor as any).specialty;
        return (primary || secondary || legacy || "").trim();
      })
      .filter((value): value is string => Boolean(value))
  );

  const localeOptions = useArabic ? "ar" : undefined;
  return Array.from(set).sort((a, b) => a.localeCompare(b, localeOptions));
});

const resolveServiceLabel = (service: DoctorServiceReference, useArabic: boolean): string => {
  const primary = useArabic ? service.nameAr : service.nameEn;
  const secondary = useArabic ? service.nameEn : service.nameAr;
  const fallback = service.slug;
  return (primary || secondary || fallback || "").trim() || fallback;
};

const languageCatalog = computed<Record<string, string>>(() => ({
  en: t("doctors.common.languages.english"),
  ar: t("doctors.common.languages.arabic"),
  ru: t("doctors.common.languages.russian")
}));

const resolveLanguageLabel = (code: string): string => {
  const normalized = String(code ?? "").toLowerCase();
  if (!normalized) return "—";
  return languageCatalog.value[normalized] ?? normalized.toUpperCase();
};

const languageOptions = computed(() => {
  const set = new Set(
    doctors.value
      .flatMap(doctor => doctor.locales ?? [])
      .filter((code): code is string => Boolean(code))
  );
  return Array.from(set)
    .map(code => ({
      code,
      label: resolveLanguageLabel(code)
    }))
    .sort((a, b) => a.label.localeCompare(b.label, locale.value || undefined));
});

const uniqueSpecialties = computed(() => specialtyList.value.length);

const uniqueLanguages = computed(() => languageOptions.value.length);

const uniqueServices = computed(() => {
  const set = new Set<number>();
  doctors.value.forEach(doctor => {
    (doctor.services ?? []).forEach(service => {
      if (service.id != null) {
        set.add(service.id);
      }
    });
  });
  return set.size;
});

const dateFormatter = new Intl.DateTimeFormat(undefined, { dateStyle: "medium" });

const formatDate = (date: string | number | null | undefined) => {
  if (!date) return "—";
  try {
    // Handle Unix timestamps (in seconds) and ISO strings
    let dateObj: Date;
    if (typeof date === 'number') {
      // Unix timestamp: if less than 10 billion, it's in seconds (dates before 2286)
      const timestamp = date < 10000000000 ? date * 1000 : date;
      dateObj = new Date(timestamp);
    } else {
      dateObj = new Date(date);
    }

    if (isNaN(dateObj.getTime()) || dateObj.getTime() === 0) return "—";
    return dateFormatter.format(dateObj);
  } catch {
    return "—";
  }
};

const rows = computed(() => {
  const useArabic = locale.value?.startsWith("ar");
  const cardSeparator = useArabic ? "، " : " · ";
  const tableSeparator = useArabic ? "، " : ", ";

  return doctors.value.map(doctor => {
    const fullName =
      (useArabic ? doctor.fullNameAr : doctor.fullNameEn)?.trim() ||
      doctor.fullNameEn?.trim() ||
      doctor.fullNameAr?.trim() ||
      (doctor as any).fullName?.trim() ||
      "";

    const specialty =
      (useArabic ? doctor.specialtyAr : doctor.specialtyEn)?.trim() ||
      doctor.specialtyEn?.trim() ||
      doctor.specialtyAr?.trim() ||
      (doctor as any).specialty?.trim() ||
      null;

    const services = (doctor.services ?? []).map(service => ({
      ...service,
      label: resolveServiceLabel(service, useArabic)
    }));

    const locales = doctor.locales ?? [];
    const languageLabels = locales.map(resolveLanguageLabel);

    return {
      ...doctor,
      fullName,
      specialty,
      services,
      locales,
      languageLabels,
      languagesCard: languageLabels.join(cardSeparator),
      languagesTable: languageLabels.join(tableSeparator),
      createdAt: formatDate(doctor.createdAt)
    };
  });
});

const search = ref("");
const selectedSpecialties = ref<string[]>([]);
const selectedLanguages = ref<string[]>([]);
const viewMode = ref<"cards" | "table">("cards");

const activeFiltersCount = computed(
  () =>
    selectedSpecialties.value.length +
    selectedLanguages.value.length +
    (search.value.trim() ? 1 : 0)
);

const filteredRows = computed(() => {
  const term = search.value.toLowerCase();
  return rows.value.filter(row => {
    const matchesSearch =
      !term ||
      [
        row.fullName,
        row.specialty,
        row.locales?.join(" "),
        row.languageLabels.join(" "),
        row.services.map(service => service.label ?? service.slug).join(" ")
      ]
        .filter(Boolean)
        .some(value => value?.toLowerCase().includes(term));

    const matchesSpecialty =
      !selectedSpecialties.value.length ||
      (row.specialty ? selectedSpecialties.value.includes(row.specialty) : false);

    const matchesLanguage =
      !selectedLanguages.value.length ||
      selectedLanguages.value.every(code => row.locales?.includes(code));

    return matchesSearch && matchesSpecialty && matchesLanguage;
  });
});

const tableColumns = computed(() => [
  { key: "doctor", label: t("doctors.list.table.columns.doctor") },
  { key: "specialty", label: t("doctors.list.table.columns.specialty") },
  { key: "services", label: t("doctors.list.table.columns.services") },
  { key: "languages", label: t("doctors.list.table.columns.languages") },
  { key: "created", label: t("doctors.list.table.columns.created") },
  { key: "actions", label: t("doctors.list.table.columns.actions"), class: "w-32 text-right" }
]);

const tableRows = computed(() => {
  const useArabic = locale.value?.startsWith("ar");
  const joiner = useArabic ? "، " : ", ";

  return filteredRows.value.map(row => ({
    id: row.id,
    doctor: row.fullName,
    email: row.email,
    specialty: row.specialty ?? t("doctors.list.labels.specialtyNotSet"),
    services:
      row.services.length > 0
        ? row.services.map(service => service.label ?? service.slug).join(joiner)
        : "—",
    languages: row.languagesTable || "—",
    created: row.createdAt,
    source: row
  }));
});

const specialties = computed(() => specialtyList.value);

const deleteOpen = ref(false);
const deleting = ref(false);
const deleteTarget = ref<DoctorAdmin | null>(null);

function toggleSpecialty(value: string) {
  selectedSpecialties.value = selectedSpecialties.value.includes(value)
    ? selectedSpecialties.value.filter(item => item !== value)
    : [...selectedSpecialties.value, value];
}

function toggleLanguage(value: string) {
  selectedLanguages.value = selectedLanguages.value.includes(value)
    ? selectedLanguages.value.filter(item => item !== value)
    : [...selectedLanguages.value, value];
}

function clearFilters() {
  search.value = "";
  selectedSpecialties.value = [];
  selectedLanguages.value = [];
}

function openCreate() {
  router.push("/doctors/new");
}

function openEdit(row: DoctorAdmin) {
  router.push(`/doctors/${row.id}`);
}

function confirmRemove(row: DoctorAdmin) {
  deleteTarget.value = row;
  deleteOpen.value = true;
}

function getRowActions(row: DoctorAdmin) {
  return [
    [
      {
        label: t("doctors.list.actions.editProfile"),
        icon: "i-lucide-pencil",
        click: () => openEdit(row)
      },
      {
        label: t("doctors.list.actions.viewSchedule"),
        icon: "i-lucide-calendar",
        click: () => openEdit(row)
      }
    ],
    [
      {
        label: t("doctors.list.actions.delete"),
        icon: "i-lucide-trash-2",
        click: () => confirmRemove(row),
        class: "text-red-600"
      }
    ]
  ];
}

async function handleDelete() {
  if (!deleteTarget.value) return;
  deleting.value = true;
  try {
    await request(`/doctors/${deleteTarget.value.id}`, {
      method: "DELETE"
    });
    toast.success(t("doctors.list.toasts.deleteSuccess"));
    deleteOpen.value = false;
    deleteTarget.value = null;
    await refresh();
  } catch (error: any) {
    toast.error({
      title: t("doctors.list.toasts.deleteError.title"),
      error
    });
  } finally {
    deleting.value = false;
  }
}
</script>
