<template>
  <div class="relative min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 text-slate-900 transition-colors duration-300 antialiased dark:from-slate-950 dark:via-slate-950 dark:to-slate-900 dark:text-slate-100">
    <div aria-hidden="true" class="pointer-events-none absolute inset-0 -z-[1] overflow-hidden">
      <div class="absolute left-1/2 top-[-20%] h-[480px] w-[480px] -translate-x-1/2 rounded-full bg-violet-300/40 blur-[180px] dark:bg-violet-500/20"></div>
      <div class="absolute right-[-10%] bottom-[-25%] h-[360px] w-[360px] rounded-full bg-emerald-200/30 blur-[140px] dark:bg-emerald-400/10"></div>
      <div class="absolute left-[-18%] top-1/3 h-72 w-72 rounded-full bg-sky-200/30 blur-[160px] dark:bg-sky-400/10"></div>
    </div>

    <div class="flex min-h-screen">
      <aside class="hidden w-72 flex-col border-r border-slate-200/60 bg-white/80 backdrop-blur-xl transition-colors duration-300 dark:border-white/10 dark:bg-slate-950/80 lg:flex">
        <div class="flex h-20 items-center gap-3.5 border-b border-slate-200/60 px-6 dark:border-white/10">
          <div class="relative flex h-11 w-11 items-center justify-center overflow-hidden rounded-xl bg-primary-gradient text-2xl shadow-lg shadow-teal-500/30">
            <img
              v-if="clinicLogo"
              :src="clinicLogo"
              :alt="clinicName"
              class="h-full w-full object-cover"
            />
            <img
              v-else
              src="https://imagedelivery.net/K88oXEK4nwOFUDLZaSq1vg/6c79054b-5ecc-4a97-be03-441518f70200/public"
              :alt="clinicName || 'Clinic logo'"
              class="h-full w-full object-cover"
              loading="lazy"
            />
            <div class="pointer-events-none absolute -inset-0.5 rounded-xl bg-white/60 opacity-40 blur-md dark:bg-white/20"></div>
          </div>
          <div class="flex-1">
            <p class="text-[11px] font-semibold uppercase tracking-[0.35em] text-primary-600 dark:text-primary-200">
              {{ clinicName }}
            </p>
            <p class="text-sm font-semibold text-slate-900 dark:text-white/90">
              {{ clinicTagline }}
            </p>
          </div>
        </div>

        <div class="border-b border-slate-200/60 px-6 py-5 dark:border-white/10">
          <p class="text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400">
            {{ t("layout.sidebar.today") }}
          </p>
          <div class="mt-3 grid grid-cols-2 gap-3 text-sm">
            <div class="rounded-lg border border-slate-200/60 bg-white/80 p-3 shadow-sm dark:border-white/10 dark:bg-white/5">
              <p class="text-xs text-slate-500 dark:text-slate-400">
                {{ t("layout.sidebar.metrics.appointments") }}
              </p>
              <p class="mt-1 text-lg font-semibold text-slate-900 dark:text-white">{{ todayAppointments }}</p>
            </div>
            <div class="rounded-lg border border-slate-200/60 bg-white/80 p-3 shadow-sm dark:border-white/10 dark:bg-white/5">
              <p class="text-xs text-slate-500 dark:text-slate-400">
                {{ t("layout.sidebar.metrics.utilization") }}
              </p>
              <p class="mt-1 text-lg font-semibold text-slate-900 dark:text-white">{{ utilizationRate }}%</p>
            </div>
            <div class="col-span-2 rounded-lg border border-slate-200/60 bg-white/80 p-3 shadow-sm dark:border-white/10 dark:bg-white/5">
              <p class="text-xs text-slate-500 dark:text-slate-400">
                {{ t("layout.sidebar.metrics.nextBreak") }}
              </p>
              <p class="mt-1 text-sm font-medium text-slate-900 dark:text-white/90">
                {{ nextBreakDisplay }}
              </p>
            </div>
          </div>
        </div>

        <nav class="flex-1 overflow-y-auto px-4 py-6 scrollbar-thin scrollbar-track-transparent scrollbar-thumb-slate-300/60 dark:scrollbar-thumb-white/10">
          <UVerticalNavigation :links="navigation" :ui="navUi" />
        </nav>

        <div class="border-t border-slate-200/60 p-5 dark:border-white/10">
          <div class="relative overflow-hidden rounded-2xl border border-slate-200/60 bg-gradient-to-br from-mint-50 via-white to-slate-50 p-5 shadow-lg shadow-slate-200/50 transition-colors duration-300 dark:border-white/10 dark:bg-white/5 dark:from-white/5 dark:via-white/5 dark:to-white/5 dark:shadow-black/20">
            <div class="absolute -right-6 -top-6 h-24 w-24 rounded-full bg-violet-400/30 blur-2xl dark:bg-violet-500/30"></div>
            <div class="relative flex items-start gap-3">
              <div class="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-mint-500 to-mint-400 text-white shadow-lg shadow-mint-500/40">
                <UIcon name="i-lucide-life-buoy" class="h-5 w-5" />
              </div>
              <div class="flex-1">
                <p class="text-sm font-semibold text-slate-900 dark:text-white">
                  {{ t("layout.sidebar.support.title") }}
                </p>
                <p class="mt-1 text-xs leading-relaxed text-slate-600 dark:text-slate-300">
                  {{ t("layout.sidebar.support.description") }}
                </p>
                <UButton
                  color="violet"
                  variant="soft"
                  size="sm"
                  class="mt-3 w-full justify-center bg-white/80 text-violet-700 hover:bg-white dark:bg-white/10 dark:text-white dark:hover:bg-white/20"
                  icon="i-lucide-message-circle"
                >
                  {{ t("layout.sidebar.support.action") }}
                </UButton>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <div class="flex min-h-screen flex-1 flex-col">
        <header class="sticky top-0 z-30 border-b border-slate-200/60 bg-white/80 backdrop-blur-2xl transition-colors duration-300 dark:border-white/10 dark:bg-slate-950/80">
          <UContainer class="flex h-20 items-center justify-between gap-4 px-6">
            <div class="flex flex-1 items-center gap-3">
              <UButton
                icon="i-lucide-menu"
                variant="ghost"
                size="md"
                class="text-slate-500 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-200 dark:hover:bg-white/10 dark:hover:text-white lg:hidden"
                @click="sidebarOpen = true"
              />
              <div class="relative flex-1 max-w-xl">
                <UInput
                  ref="searchInputRef"
                  v-model="search"
                  icon="i-lucide-search"
                  size="md"
                  :placeholder="t('layout.header.searchPlaceholder')"
                  data-search-input="global"
                  class="w-full"
                  :ui="searchInputUi"
                  @focus="handleSearchFocus"
                  @blur="handleSearchBlur"
                  @keydown="handleSearchKeydown"
                />
                <kbd class="absolute right-3 top-1/2 hidden -translate-y-1/2 items-center gap-1 rounded border border-slate-300 bg-white/80 px-2 py-1 text-[11px] font-medium text-slate-600 shadow-sm dark:border-white/10 dark:bg-white/10 dark:text-slate-200 sm:flex">
                  <span>⌘</span><span>K</span>
                </kbd>
                <transition name="fade">
                  <div
                    v-if="searchDropdownOpen"
                    ref="searchResultsRef"
                    class="absolute left-0 right-0 top-full z-50 mt-2 overflow-hidden rounded-2xl border border-slate-200/70 bg-white/95 shadow-2xl shadow-slate-200/60 backdrop-blur-xl dark:border-white/10 dark:bg-slate-900/95 dark:shadow-black/40"
                  >
                    <div
                      v-if="searchLoading"
                      class="flex items-center gap-2 px-3 py-2 text-xs text-slate-500 dark:text-slate-400"
                    >
                      <UIcon name="i-lucide-loader-2" class="h-4 w-4 animate-spin" />
                      <span>{{ t("layout.search.loading") }}</span>
                    </div>
                    <div
                      v-if="remoteError"
                      class="px-3 py-2 text-xs text-rose-500 dark:text-rose-400"
                    >
                      {{ remoteError }}
                    </div>
                    <template v-if="groupedSearchResults.length">
                      <div class="max-h-80 overflow-y-auto py-2">
                        <div
                          v-for="group in groupedSearchResults"
                          :key="group.key"
                          class="px-2 pb-2"
                        >
                          <p class="px-2 pb-1 text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-400 dark:text-slate-500">
                            {{ group.title }}
                          </p>
                          <button
                            v-for="result in group.items"
                            :key="result.id"
                            type="button"
                            class="group relative flex w-full items-center gap-3 rounded-xl px-3 py-3 text-left text-sm transition-colors duration-150"
                            :class="searchActiveIndex === result.index
                              ? 'bg-violet-50 text-violet-900 shadow-inner shadow-violet-200/40 dark:bg-white/10 dark:text-white'
                              : 'text-slate-700 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-200 dark:hover:bg-white/5 dark:hover:text-white'"
                            @mousedown.prevent
                            @click="applySearchResult(result)"
                            @mouseover="searchActiveIndex = result.index"
                          >
                            <div
                              class="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-xl text-base transition-colors duration-150"
                              :class="result.iconBg"
                            >
                              <UIcon :name="result.icon" class="h-5 w-5" />
                            </div>
                            <div class="flex-1">
                              <p class="font-semibold">
                                {{ result.label }}
                              </p>
                              <p class="text-xs text-slate-500 transition-colors duration-150 group-hover:text-slate-600 dark:text-slate-400 dark:group-hover:text-slate-300">
                                {{ result.meta }}
                              </p>
                            </div>
                          </button>
                        </div>
                      </div>
                    </template>
                    <template v-else>
                      <div
                        v-if="trimmedSearch.length >= MIN_REMOTE_SEARCH && !searchLoading"
                        class="px-4 py-6 text-center text-sm text-slate-500 dark:text-slate-400"
                      >
                        {{ t("layout.search.noResults", { query: trimmedSearch }) }}
                      </div>
                    </template>
                  </div>
                </transition>
              </div>
            </div>

            <div class="flex items-center gap-2">
              <UButton
                :icon="themeIcon"
                variant="ghost"
                size="md"
                :aria-label="themeLabel"
                class="text-slate-500 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-200 dark:hover:bg-white/10 dark:hover:text-white"
                @click="toggleTheme"
              />

              <div class="hidden items-center gap-1 rounded-xl border border-slate-200/80 bg-white/80 px-1 py-1 shadow-sm backdrop-blur md:flex dark:border-white/10 dark:bg-white/5">
                <button
                  v-for="option in languageToggleOptions"
                  :key="option.code"
                  type="button"
                  :class="[
                    'flex items-center gap-1 rounded-lg px-3 py-1.5 text-xs font-semibold transition-all duration-150 focus:outline-none focus-visible:ring-2 focus-visible:ring-violet-400/70',
                    locale.value === option.code
                      ? 'bg-violet-600 text-white shadow-sm shadow-violet-500/30'
                      : 'text-slate-500 hover:text-slate-900 hover:bg-slate-100 dark:text-slate-300 dark:hover:text-white dark:hover:bg-white/10'
                  ]"
                  @click="setLocale(option.code)"
                >
                  <span>{{ option.label }}</span>
                </button>
              </div>
              <UButton
                variant="ghost"
                size="md"
                class="flex items-center gap-2 rounded-xl px-2 text-slate-500 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-200 dark:hover:bg-white/10 dark:hover:text-white md:hidden"
                @click="toggleLocale"
                :aria-label="languageToggleAria"
              >
                <UIcon name="i-lucide-languages" class="h-4 w-4" />
                <span class="text-xs font-semibold">{{ currentLanguageLabel }}</span>
                <UIcon name="i-lucide-shuffle" class="h-4 w-4 text-slate-400 dark:text-slate-400" />
              </UButton>

              <UDropdown :items="notificationMenu" :ui="notificationDropdownUi">
                <UButton
                  icon="i-lucide-bell"
                  variant="ghost"
                  size="md"
                  class="relative text-slate-500 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-200 dark:hover:bg-white/10 dark:hover:text-white"
                >
                  <span v-if="hasUnreadNotifications" class="absolute right-2 top-2 flex h-2 w-2">
                    <span class="absolute inline-flex h-full w-full animate-ping rounded-full bg-rose-400/80 opacity-75"></span>
                    <span class="relative inline-flex h-2 w-2 rounded-full bg-rose-400 ring-2 ring-white dark:ring-slate-950/80"></span>
                  </span>
                </UButton>
              </UDropdown>

              <UDropdown :items="quickActions" :ui="dropdownUi">
                <UButton
                  icon="i-lucide-zap"
                  variant="ghost"
                  size="md"
                  class="text-slate-500 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-200 dark:hover:bg-white/10 dark:hover:text-white"
                />
              </UDropdown>

              <UButton
                icon="i-lucide-calendar-plus"
                color="violet"
                size="md"
                class="hidden items-center gap-2 rounded-xl bg-gradient-to-r from-mint-500 to-mint-400 text-white shadow-lg shadow-mint-500/40 transition-all duration-200 hover:shadow-mint-500/60 sm:inline-flex"
                @click="navigateTo('/appointments/new')"
              >
                {{ t("layout.header.newBooking") }}
              </UButton>

              <UDropdown :items="userMenu" :ui="dropdownUi">
                <UButton variant="ghost" size="md" class="gap-2 rounded-xl px-2 text-slate-600 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-200 dark:hover:bg-white/10 dark:hover:text-white">
                  <UAvatar
                    size="sm"
                    icon="i-lucide-user"
                    :ui="{ rounded: 'rounded-lg', background: 'bg-gradient-to-br from-mint-500 to-mint-400' }"
                    class="ring-2 ring-violet-500/30 dark:ring-white/20"
                  />
                  <div class="hidden flex-col items-start lg:flex">
                    <span class="text-xs font-semibold text-slate-900 dark:text-white">{{ userInfo.name }}</span>
                    <span class="text-[11px] text-slate-500 dark:text-slate-400">{{ userInfo.role }}</span>
                  </div>
                  <UIcon name="i-lucide-chevron-down" class="hidden h-4 w-4 text-slate-400 dark:text-slate-400 lg:block" />
                </UButton>
              </UDropdown>
            </div>
          </UContainer>
        </header>

        <USlideover v-model="sidebarOpen" side="left" :ui="{ width: 'max-w-sm' }">
          <UCard
            class="flex h-full flex-col bg-white text-slate-900 dark:bg-slate-950 dark:text-slate-100"
            :ui="{ body: { padding: '' }, header: { padding: 'px-6 py-5 border-b border-slate-200/60 dark:border-white/10' } }"
          >
            <template #header>
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <div class="relative flex h-10 w-10 items-center justify-center overflow-hidden rounded-xl bg-primary-gradient text-xl text-white shadow-lg shadow-teal-500/40">
                    <img
                      v-if="clinicLogo"
                      :src="clinicLogo"
                      :alt="clinicName"
                      class="h-full w-full object-cover"
                    />
                    <img
                      v-else
                      src="https://imagedelivery.net/K88oXEK4nwOFUDLZaSq1vg/6c79054b-5ecc-4a97-be03-441518f70200/public"
                      :alt="clinicName || 'Clinic logo'"
                      class="h-full w-full object-cover"
                      loading="lazy"
                    />
                  </div>
                  <div>
                    <span class="text-sm font-semibold text-slate-900 dark:text-white">{{ clinicName }}</span>
                    <p class="text-xs text-slate-500 dark:text-slate-400">{{ clinicTagline }}</p>
                  </div>
                </div>
                <UButton icon="i-lucide-x" variant="ghost" size="sm" class="text-slate-500 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-200 dark:hover:bg-white/10 dark:hover:text-white" @click="sidebarOpen = false" />
              </div>
            </template>
            <nav class="flex-1 overflow-y-auto px-4 py-6">
              <UVerticalNavigation :links="navigation" :ui="navUi" />
            </nav>
          </UCard>
        </USlideover>

        <main class="relative flex-1 transition-colors duration-300">
          <div aria-hidden="true" class="pointer-events-none absolute inset-0 -z-10">
            <div class="absolute left-8 top-12 h-64 w-64 rounded-full bg-violet-200/40 blur-3xl dark:bg-violet-500/10"></div>
            <div class="absolute bottom-10 right-8 h-48 w-48 rounded-full bg-blue-200/40 blur-3xl dark:bg-blue-500/10"></div>
          </div>

          <UContainer class="px-6 py-12">
            <div class="page-shell rounded-3xl border border-slate-200/60 bg-white/95 p-0 text-slate-900 shadow-xl shadow-slate-200/60 backdrop-blur-xl transition-colors duration-300 dark:border-white/10 dark:bg-slate-900/90 dark:text-slate-100 dark:shadow-black/40">
              <div class="page-content">
                <slot />
              </div>
            </div>
          </UContainer>
        </main>

        <footer class="border-t border-slate-200/60 bg-white/80 backdrop-blur-xl transition-colors duration-300 dark:border-white/10 dark:bg-slate-950/80">
          <UContainer class="flex h-14 items-center justify-between px-6 text-xs text-slate-500 dark:text-slate-400">
            <p>{{ t("layout.footer.copy", { year: currentYear }) }}</p>
            <div class="flex items-center gap-4">
              <a href="#" class="transition-colors hover:text-slate-900 dark:hover:text-white">{{ t("layout.footer.privacy") }}</a>
              <a href="#" class="transition-colors hover:text-slate-900 dark:hover:text-white">{{ t("layout.footer.terms") }}</a>
              <a href="#" class="transition-colors hover:text-slate-900 dark:hover:text-white">{{ t("layout.footer.docs") }}</a>
            </div>
          </UContainer>
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick } from "vue";
import type { ModuleName } from '@/types/staff';
import { dashboardSummaryMock, upcomingAppointmentsMock } from "@/data/mock";
import { useAdminApi } from "@/composables/useAdminApi";

type SearchResultGroup =
  | "navigation"
  | "appointments"
  | "patients"
  | "doctors"
  | "services"
  | "materials"
  | "insurance"
  | "treatmentPlans"
  | "staff"
  | "blogs"
  | "action";

interface GlobalSearchResultDto {
  type: SearchResultGroup;
  id: string;
  title: string;
  subtitle?: string | null;
  route?: string | null;
  icon?: string | null;
}

interface GlobalSearchResponse {
  results: GlobalSearchResultDto[];
}

interface SearchResultEntry {
  id: string;
  label: string;
  meta: string;
  icon: string;
  iconBg: string;
  group: SearchResultGroup;
  action: () => void;
  index: number;
}

const auth = useAuth();
const { settings: clinicSettings } = useClinicSettings();
const { hasModuleAccess } = usePermissions();
const colorMode = useColorMode();
const { t, locale, locales, setLocale } = useI18n();
const search = ref("");
const searchInputRef = ref<HTMLInputElement | null>(null);
const searchResultsRef = ref<HTMLElement | null>(null);
const searchDropdownOpen = ref(false);
const searchActiveIndex = ref(-1);
const trimmedSearch = computed(() => search.value.trim());
const remoteResults = ref<GlobalSearchResultDto[]>([]);
const searchLoading = ref(false);
const remoteError = ref<string | null>(null);
const MIN_REMOTE_SEARCH = 2;
let searchAbort: AbortController | null = null;
let searchDebounce: ReturnType<typeof setTimeout> | null = null;
const sidebarOpen = ref(false);
const pendingAppointmentsCount = ref<number>(0);
const appointmentNotifications = useAppointmentNotifications();
const { recentNotifications } = appointmentNotifications;
const hasUnreadNotifications = computed(() => recentNotifications.value.length > 0);
const { fetcher, request } = useAdminApi();

const isDark = computed(() => colorMode.value === "dark");
const themeIcon = computed(() => (isDark.value ? "i-lucide-sun" : "i-lucide-moon-star"));
const themeLabel = computed(() =>
  isDark.value ? t("layout.header.theme.light") : t("layout.header.theme.dark")
);
const toggleTheme = () => {
  colorMode.preference = isDark.value ? "light" : "dark";
};

const currentYear = computed(() => new Date().getFullYear());

const { data: sidebarSummaryData } = await useAsyncData("layout-dashboard-summary", () =>
  fetcher("/dashboard/summary", dashboardSummaryMock)
);

const summaryFallback = dashboardSummaryMock;
const sidebarSummary = computed(() => ({
  ...summaryFallback,
  ...((sidebarSummaryData.value ?? dashboardSummaryMock) || {})
}));

const todayAppointments = computed(() => Number(sidebarSummary.value.appointmentsToday ?? 0));

const utilizationRate = computed(() => {
  const scheduled = Number(sidebarSummary.value.appointmentsToday ?? 0);
  const pending = Number(sidebarSummary.value.pendingConfirmations ?? 0);
  const baselineCapacity = 32;
  if (baselineCapacity <= 0) return 0;
  const ratio = Math.min(1, (scheduled + pending) / baselineCapacity);
  return Math.round(ratio * 100);
});

const { data: sidebarUpcomingData } = await useAsyncData("layout-upcoming-appointments", () =>
  fetcher("/appointments?filter=upcoming&size=6", upcomingAppointmentsMock)
);

const upcomingAppointments = computed(() => {
  const raw = sidebarUpcomingData.value ?? upcomingAppointmentsMock;
  if (Array.isArray(raw)) return raw;
  if (raw && typeof raw === "object") {
    if ("items" in raw && Array.isArray((raw as any).items)) {
      return (raw as any).items as typeof upcomingAppointmentsMock;
    }
    if ("content" in raw && Array.isArray((raw as any).content)) {
      return (raw as any).content as typeof upcomingAppointmentsMock;
    }
    if ("data" in raw && Array.isArray((raw as any).data)) {
      return (raw as any).data as typeof upcomingAppointmentsMock;
    }
  }
  return upcomingAppointmentsMock;
});

const nextBreakDisplay = computed(() => {
  const now = Date.now();
  const slot = upcomingAppointments.value
    .map(item => {
      const scheduledRaw = (item as any).scheduledAt ?? (item as any).scheduled_at;
      const scheduledAt = new Date(scheduledRaw);
      return {
        doctorName:
          (item as any).doctorName ??
          (item as any).doctor?.name ??
          "",
        scheduledAt
      };
    })
    .filter(entry => entry.scheduledAt instanceof Date && !Number.isNaN(entry.scheduledAt.getTime()))
    .sort((a, b) => a.scheduledAt.getTime() - b.scheduledAt.getTime())
    .find(entry => entry.scheduledAt.getTime() > now);

  if (!slot) {
    return t("layout.sidebar.metrics.nextBreakFallback");
  }

  const doctor = slot.doctorName || t("layout.sidebar.metrics.onCallDoctor");
  const diffMs = slot.scheduledAt.getTime() - now;
  const diffMinutes = Math.round(diffMs / 60000);

  if (diffMinutes <= 0) {
    return t("layout.sidebar.metrics.nextBreakNow", { doctor });
  }

  let relativeLabel: string;
  try {
    const rtf = new Intl.RelativeTimeFormat(locale.value, { numeric: "auto" });
    if (diffMinutes >= 60) {
      const hours = Math.max(1, Math.round(diffMinutes / 60));
      relativeLabel = rtf.format(hours, "hour");
    } else {
      relativeLabel = rtf.format(diffMinutes, "minute");
    }
  } catch {
    if (diffMinutes >= 60) {
      const hours = Math.max(1, Math.round(diffMinutes / 60));
      relativeLabel = `in ${hours}h`;
    } else {
      relativeLabel = `in ${diffMinutes}m`;
    }
  }

  return t("layout.sidebar.metrics.nextBreakValue", {
    doctor,
    time: relativeLabel
  });
});

const languageToggleOptions = computed(() =>
  locales.value.map(option => ({
    code: option.code,
    label: option.code.toUpperCase()
  }))
);

const currentLanguageLabel = computed(
  () => locales.value.find(item => item.code === locale.value)?.name ?? locale.value.toUpperCase()
);

const nextLanguageLabel = computed(() => {
  const availableLocales = locales.value.map(item => item.code);
  if (availableLocales.length <= 1) {
    return currentLanguageLabel.value;
  }
  const nextIndex = (availableLocales.indexOf(locale.value) + 1) % availableLocales.length;
  const nextCode = availableLocales[nextIndex];
  return locales.value.find(item => item.code === nextCode)?.name ?? nextCode.toUpperCase();
});

const languageToggleAria = computed(() =>
  locales.value.length > 1
    ? t('layout.header.language.toggle', { name: nextLanguageLabel.value })
    : currentLanguageLabel.value
);

const toggleLocale = () => {
  const availableLocales = locales.value.map(item => item.code);
  if (availableLocales.length <= 1) return;
  const nextIndex = (availableLocales.indexOf(locale.value) + 1) % availableLocales.length;
  setLocale(availableLocales[nextIndex]);
};

const searchInputUi = computed(() => ({
  icon: {
    leading: {
      wrapper: "absolute inset-y-0 start-0 flex items-center ps-3 pointer-events-none",
      icon: isDark.value ? "text-slate-400" : "text-slate-400"
    }
  },
  base: isDark.value
    ? "bg-white/5 border-white/10 text-slate-100 placeholder:text-slate-400 focus:ring-2 focus:ring-violet-500/60"
    : "bg-white/90 border-slate-300 text-slate-900 placeholder:text-slate-500 focus:ring-2 focus:ring-violet-500/20",
  wrapper: "relative",
  padding: "ps-10 pe-12"
}));

const userInfo = computed(() => ({
  name: auth.userName.value || "Admin User",
  role: auth.userRole.value || "Administrator"
}));

const clinicName = computed(
  () => clinicSettings.value?.clinicName?.trim() || t("layout.brand.title")
);
const clinicTagline = computed(
  () => clinicSettings.value?.tagline?.trim() || t("layout.brand.subtitle")
);
const clinicLogo = computed(() => clinicSettings.value?.logoUrl?.trim() || "");

const navUi = computed(() => ({
  wrapper: "space-y-1.5",
  base: "group relative flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition-all duration-200",
  active: "bg-gradient-to-r from-mint-500 to-mint-400 text-white shadow-lg shadow-mint-600/40 ring-1 ring-white/20",
  inactive: isDark.value
    ? "text-slate-300 hover:bg-white/5 hover:text-white"
    : "text-slate-600 hover:bg-slate-100 hover:text-slate-900",
  icon: {
    base: "h-5 w-5 flex-shrink-0 transition-transform duration-200 group-hover:scale-110",
    active: "text-white",
    inactive: isDark.value ? "text-slate-400 group-hover:text-white" : "text-slate-400 group-hover:text-slate-700"
  },
  badge: {
    base: isDark.value
      ? "ml-auto rounded-full border border-white/15 bg-white/10 px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide text-white"
      : "ml-auto rounded-full border border-violet-100 bg-violet-50 px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide text-violet-600",
    size: "xs",
    color: "white"
  }
}));

const dropdownUi = computed(() => ({
  container: "z-50",
  width: "w-56",
  background: isDark.value ? "bg-slate-900/90 backdrop-blur-lg" : "bg-white/95 backdrop-blur-lg",
  shadow: isDark.value ? "shadow-xl shadow-black/40" : "shadow-xl shadow-slate-200/50",
  rounded: "rounded-xl",
  ring: isDark.value ? "ring-1 ring-white/10" : "ring-1 ring-slate-200/80",
  padding: "p-1.5",
  item: {
    base: "group flex w-full items-center gap-2.5 transition-all duration-150",
    padding: "px-3 py-2.5",
    size: "text-sm",
    rounded: "rounded-lg",
    active: isDark.value ? "bg-white/10 text-white" : "bg-violet-50 text-violet-900",
    inactive: isDark.value ? "text-slate-200 hover:bg-white/5" : "text-slate-700 hover:bg-slate-100",
    disabled: "opacity-50 cursor-not-allowed",
    label: "font-medium truncate",
    icon: {
      base: "flex-shrink-0 w-5 h-5",
      active: isDark.value ? "text-white" : "text-violet-600",
      inactive: isDark.value ? "text-slate-400 group-hover:text-white" : "text-slate-400 group-hover:text-slate-700"
    }
  },
  separator: isDark.value ? "h-px bg-white/10 my-1.5" : "h-px bg-slate-200 my-1.5"
}));

// Map routes to module names for permission checking
function getModuleForRoute(route: string): ModuleName | null {
  const routeToModuleMap: Record<string, ModuleName> = {
    '/appointments': 'appointments',
    '/doctors': 'doctors',
    '/patients': 'patients',
    '/treatment-plans': 'treatmentPlans',
    '/services': 'services',
    '/blogs': 'blogs',
    '/reports': 'reports',
    '/staff': 'staff',
    '/clinic-settings': 'settings'
  };
  return routeToModuleMap[route] || null;
}

const allNavigationItems = computed(() => [
  {
    label: t("navigation.dashboard"),
    icon: "i-lucide-layout-dashboard",
    to: "/",
    badge: undefined
  },
  {
    label: t("navigation.appointments"),
    icon: "i-lucide-calendar-check",
    to: "/appointments",
    badge: pendingAppointmentsCount.value > 0 ? pendingAppointmentsCount.value.toString() : undefined
  },
  {
    label: t("navigation.calendar"),
    icon: "i-lucide-calendar-days",
    to: "/calendar"
  },
  {
    label: t("navigation.doctors"),
    icon: "i-lucide-stethoscope",
    to: "/doctors"
  },
  {
    label: t("navigation.patients"),
    icon: "i-lucide-users",
    to: "/patients"
  },
  {
    label: t("navigation.treatmentPlans"),
    icon: "i-lucide-clipboard-list",
    to: "/treatment-plans"
  },
  {
    label: t("navigation.materials"),
    icon: "i-lucide-package",
    to: "/materials"
  },
  {
    label: t("navigation.services"),
    icon: "i-lucide-layers",
    to: "/services"
  },
  {
    label: t("navigation.insuranceCompanies"),
    icon: "i-lucide-shield-check",
    to: "/insurance-companies"
  },
  {
    label: t("navigation.blogs"),
    icon: "i-lucide-newspaper",
    to: "/blogs"
  },
  {
    label: t("navigation.reports"),
    icon: "i-lucide-bar-chart-3",
    to: "/reports"
  },
  {
    label: t("navigation.staff"),
    icon: "i-lucide-user-cog",
    to: "/staff"
  },
  {
    label: t("navigation.clinicSettings"),
    icon: "i-lucide-building-2",
    to: "/clinic-settings"
  },
  {
    label: t("navigation.settings"),
    icon: "i-lucide-settings",
    to: "/settings"
  }
]);

// Filter navigation based on permissions
const navigation = computed(() => {
  return allNavigationItems.value.filter(item => {
    const module = getModuleForRoute(item.to);
    // Always show dashboard and settings
    if (!module) return true;
    // Check if user has access to this module
    return hasModuleAccess(module);
  });
});

const quickActions = computed(() => [
  [
    {
      label: t("layout.dropdown.quickActions.newPatient"),
      icon: "i-lucide-user-plus",
      click: () => navigateTo("/patients/new"),
      shortcuts: ["N", "P"]
    },
    {
      label: t("layout.dropdown.quickActions.newDoctor"),
      icon: "i-lucide-user-cog",
      click: () => navigateTo("/doctors/new")
    },
    {
      label: t("layout.dropdown.quickActions.newService"),
      icon: "i-lucide-plus-circle",
      click: () => navigateTo("/services/new")
    }
  ],
  [
    {
      label: t("layout.dropdown.quickActions.newInsurance"),
      icon: "i-lucide-building-2",
      click: () => navigateTo("/insurance-companies/new")
    },
    {
      label: t("layout.dropdown.quickActions.newBlog"),
      icon: "i-lucide-newspaper",
      click: () => navigateTo("/blogs/new")
    }
  ]
]);

const navigationEntries = computed(() =>
  navigation.value.map(item => ({
    id: `nav-${item.to}`,
    label: item.label,
    meta: t("layout.search.openPage"),
    icon: item.icon || "i-lucide-compass",
    iconBg: "bg-violet-100 text-violet-600 dark:bg-violet-500/15 dark:text-violet-200",
    group: "navigation" as const,
    action: () => navigateTo(item.to)
  }))
);

const quickActionEntries = computed(() =>
  quickActions.value.flatMap(section =>
    section.map(item => ({
      id: `action-${item.label}`,
      label: item.label,
      meta: t("layout.search.runAction"),
      icon: item.icon || "i-lucide-zap",
      iconBg: "bg-emerald-100 text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-200",
      group: "action" as const,
      action: () => {
        if (typeof item.click === "function") {
          item.click();
        }
      }
    }))
  )
);

const typeVisuals: Record<SearchResultGroup, { icon: string; iconBg: string }> = {
  navigation: {
    icon: "i-lucide-compass",
    iconBg: "bg-violet-100 text-violet-600 dark:bg-violet-500/15 dark:text-violet-200"
  },
  appointments: {
    icon: "i-lucide-calendar-clock",
    iconBg: "bg-sky-100 text-sky-600 dark:bg-sky-500/15 dark:text-sky-200"
  },
  patients: {
    icon: "i-lucide-user",
    iconBg: "bg-emerald-100 text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-200"
  },
  doctors: {
    icon: "i-lucide-stethoscope",
    iconBg: "bg-indigo-100 text-indigo-600 dark:bg-indigo-500/15 dark:text-indigo-200"
  },
  services: {
    icon: "i-lucide-layers",
    iconBg: "bg-amber-100 text-amber-600 dark:bg-amber-500/15 dark:text-amber-200"
  },
  materials: {
    icon: "i-lucide-package",
    iconBg: "bg-slate-200 text-slate-700 dark:bg-white/10 dark:text-white"
  },
  insurance: {
    icon: "i-lucide-building-2",
    iconBg: "bg-orange-100 text-orange-600 dark:bg-orange-500/15 dark:text-orange-200"
  },
  treatmentPlans: {
    icon: "i-lucide-clipboard-list",
    iconBg: "bg-fuchsia-100 text-fuchsia-600 dark:bg-fuchsia-500/15 dark:text-fuchsia-200"
  },
  staff: {
    icon: "i-lucide-shield-check",
    iconBg: "bg-rose-100 text-rose-600 dark:bg-rose-500/15 dark:text-rose-200"
  },
  blogs: {
    icon: "i-lucide-newspaper",
    iconBg: "bg-slate-200 text-slate-700 dark:bg-white/10 dark:text-white"
  },
  action: {
    icon: "i-lucide-zap",
    iconBg: "bg-violet-100 text-violet-600 dark:bg-violet-500/15 dark:text-violet-200"
  }
};

const defaultVisual = {
  icon: "i-lucide-search",
  iconBg: "bg-slate-200 text-slate-600 dark:bg-white/10 dark:text-white"
};

const resolveGroup = (value?: string | null): SearchResultGroup => {
  const candidate = (value ?? "").trim();
  const allowed: SearchResultGroup[] = [
    "navigation",
    "appointments",
    "patients",
    "doctors",
    "services",
    "materials",
    "insurance",
    "treatmentPlans",
    "staff",
    "blogs",
    "action"
  ];
  return allowed.includes(candidate as SearchResultGroup)
    ? (candidate as SearchResultGroup)
    : "navigation";
};

const resolveRouteForResult = (item: GlobalSearchResultDto): string | null => {
  if (item.route && item.route.trim().length > 0) {
    return item.route;
  }
  switch (item.type) {
    case "patients":
      return `/patients/${item.id}`;
    case "doctors":
      return `/doctors/${item.id}`;
    case "appointments":
      return `/appointments/${item.id}`;
    case "services":
      return `/services/${item.id}`;
    case "materials":
      return `/materials`;
    case "insurance":
      return `/insurance-companies/${item.id}`;
    case "treatmentPlans":
      return `/treatment-plans/${item.id}`;
    case "staff":
      return `/staff/${item.id}`;
    case "blogs":
      return `/blogs/${item.id}`;
    default:
      return null;
  }
};

const remoteSearchEntries = computed(() => {
  if (!remoteResults.value.length) {
    return [] as Omit<SearchResultEntry, "index">[];
  }

  return remoteResults.value.map((item, idx) => {
    const group = resolveGroup(item.type);
    const visuals = typeVisuals[group] ?? defaultVisual;
    const route = resolveRouteForResult(item);

    return {
      id: `remote-${group}-${item.id ?? idx}`,
      label: item.title ?? "",
      meta: item.subtitle ?? "",
      icon: item.icon ?? visuals.icon,
      iconBg: visuals.iconBg,
      group,
      action: () => {
        if (route) {
          navigateTo(route);
        }
      }
    };
  });
});

const searchResults = computed<SearchResultEntry[]>(() => {
  const term = trimmedSearch.value.toLowerCase();
  if (!term) {
    return [];
  }

  const matches: Omit<SearchResultEntry, "index">[] = [];

  const sources = [
    ...remoteSearchEntries.value,
    ...navigationEntries.value,
    ...quickActionEntries.value
  ];

  sources.forEach(entry => {
    const haystack = entry.label.toLowerCase();
    const metaHaystack = entry.meta?.toLowerCase?.() ?? "";
    if (haystack.includes(term) || metaHaystack.includes(term)) {
      matches.push(entry);
    }
  });

  return matches.map((entry, index) => ({ ...entry, index }));
});

const groupedSearchResults = computed(() => {
  if (!searchResults.value.length) {
    return [];
  }

  const groupOrder: Record<SearchResultGroup, number> = {
    appointments: 0,
    patients: 1,
    doctors: 2,
    services: 3,
    materials: 4,
    insurance: 5,
    treatmentPlans: 6,
    staff: 7,
    blogs: 8,
    navigation: 9,
    action: 10
  };
  const labels = {
    appointments: t("layout.search.appointments"),
    patients: t("layout.search.patients"),
    doctors: t("layout.search.doctors"),
    services: t("layout.search.services"),
    materials: t("layout.search.materials"),
    insurance: t("layout.search.insurance"),
    treatmentPlans: t("layout.search.treatmentPlans"),
    staff: t("layout.search.staff"),
    blogs: t("layout.search.blogs"),
    navigation: t("layout.search.navigation"),
    action: t("layout.search.actions")
  } as const;

  const groups = new Map<SearchResultGroup, SearchResultEntry[]>();
  searchResults.value.forEach(result => {
    if (!groups.has(result.group)) {
      groups.set(result.group, []);
    }
    groups.get(result.group)!.push(result);
  });

  return Array.from(groups.entries())
    .sort((a, b) => groupOrder[a[0]] - groupOrder[b[0]])
    .map(([key, items]) => ({
      key,
      title: labels[key],
      items
    }));
});

const resetRemoteSearch = () => {
  remoteResults.value = [];
  remoteError.value = null;
  searchLoading.value = false;
};

const cancelRemoteSearch = () => {
  if (searchDebounce) {
    clearTimeout(searchDebounce);
    searchDebounce = null;
  }
  if (searchAbort) {
    searchAbort.abort();
    searchAbort = null;
  }
};

const executeRemoteSearch = async (term: string) => {
  cancelRemoteSearch();
  const controller = new AbortController();
  searchAbort = controller;
  searchLoading.value = true;
  remoteError.value = null;

  try {
    const response = await request<GlobalSearchResponse>(
      `/search?q=${encodeURIComponent(term)}&limit=5`,
      {
        method: "GET",
        signal: controller.signal
      }
    );

    if (searchAbort !== controller) {
      return;
    }

    remoteResults.value = response?.results ?? [];
  } catch (error: any) {
    if (controller.signal.aborted) {
      return;
    }
    remoteResults.value = [];
    const fallbackMessage = (error?.data?.message ?? error?.message ?? String(error)) as string;
    remoteError.value = fallbackMessage || t("layout.search.error");
  } finally {
    if (searchAbort === controller) {
      searchLoading.value = false;
      searchAbort = null;
    }
  }
};

function closeSearchPanel() {
  searchDropdownOpen.value = false;
  searchActiveIndex.value = -1;
  cancelRemoteSearch();
  resetRemoteSearch();
}

function selectSearchResult(result?: SearchResultEntry) {
  if (!result) {
    return;
  }

  try {
    result.action();
  } finally {
    search.value = "";
    closeSearchPanel();
    nextTick(() => {
      searchInputRef.value?.blur();
    });
  }
}

const handleSearchFocus = () => {
  if (trimmedSearch.value) {
    searchDropdownOpen.value = true;
  }
};

const handleSearchBlur = () => {
  requestAnimationFrame(() => {
    const activeElement = document.activeElement;
    const insideInput =
      searchInputRef.value && activeElement && searchInputRef.value.contains(activeElement);
    const insideResults =
      searchResultsRef.value && activeElement && searchResultsRef.value.contains(activeElement as Node);
    if (!insideInput && !insideResults) {
      closeSearchPanel();
    }
  });
};

const handleSearchKeydown = (event: KeyboardEvent) => {
  if (event.key === "Escape") {
    closeSearchPanel();
    search.value = "";
    return;
  }

  if (!searchResults.value.length) {
    return;
  }

  if (event.key === "ArrowDown") {
    event.preventDefault();
    const total = searchResults.value.length;
    searchActiveIndex.value = total ? (searchActiveIndex.value + 1 + total) % total : -1;
    searchDropdownOpen.value = true;
    return;
  }

  if (event.key === "ArrowUp") {
    event.preventDefault();
    const total = searchResults.value.length;
    searchActiveIndex.value = total ? (searchActiveIndex.value - 1 + total) % total : -1;
    searchDropdownOpen.value = true;
    return;
  }

  if (event.key === "Enter") {
    const total = searchResults.value.length;
    if (!total) return;
    const index = searchActiveIndex.value < 0 ? 0 : searchActiveIndex.value % total;
    const result = searchResults.value[index];
    if (result) {
      event.preventDefault();
      selectSearchResult(result);
    }
  }
};

watch(searchResults, results => {
  if (!results.length) {
    searchActiveIndex.value = -1;
    return;
  }
  if (searchActiveIndex.value < 0 || searchActiveIndex.value >= results.length) {
    searchActiveIndex.value = 0;
  }
});

watch(trimmedSearch, value => {
  if (value) {
    searchDropdownOpen.value = true;
  } else {
    closeSearchPanel();
    return;
  }

  cancelRemoteSearch();

  if (value.length < MIN_REMOTE_SEARCH) {
    resetRemoteSearch();
    return;
  }

  searchDebounce = setTimeout(() => executeRemoteSearch(value), 250);
});

const applySearchResult = (result: SearchResultEntry) => {
  selectSearchResult(result);
};

const clearNotifications = () => {
  recentNotifications.value = [];
};

const notificationMenu = computed(() => {
  const menuItems: any[] = [];

  // Recent notifications section
  if (recentNotifications.value.length > 0) {
    menuItems.push(
      recentNotifications.value.map(notification => ({
        label: notification.title,
        icon: notification.icon || "i-lucide-calendar-plus",
        click: () => {
          if (notification.to) {
            navigateTo(notification.to);
          }
        }
      }))
    );
  } else {
    menuItems.push([
      {
        label: "No new notifications",
        icon: "i-lucide-inbox",
        disabled: true
      }
    ]);
  }

  // Actions section
  menuItems.push([
    {
      label: "Clear all",
      icon: "i-lucide-trash-2",
      click: clearNotifications,
      disabled: recentNotifications.value.length === 0
    },
    {
      label: "Notification settings",
      icon: "i-lucide-settings",
      click: () => navigateTo("/settings")
    }
  ]);

  return menuItems;
});

const notificationDropdownUi = computed(() => ({
  container: "z-50",
  width: "w-80",
  background: isDark.value ? "bg-slate-900/95 backdrop-blur-lg" : "bg-white/95 backdrop-blur-lg",
  shadow: isDark.value ? "shadow-xl shadow-black/40" : "shadow-xl shadow-slate-200/50",
  rounded: "rounded-xl",
  ring: isDark.value ? "ring-1 ring-white/10" : "ring-1 ring-slate-200/80",
  padding: "p-2",
  item: {
    base: "group flex w-full items-start gap-3 transition-all duration-150",
    padding: "px-3 py-3",
    size: "text-sm",
    rounded: "rounded-lg",
    active: isDark.value ? "bg-white/10 text-white" : "bg-violet-50 text-violet-900",
    inactive: isDark.value ? "text-slate-200 hover:bg-white/5" : "text-slate-700 hover:bg-slate-50",
    disabled: "opacity-50 cursor-not-allowed",
    label: "font-medium",
    icon: {
      base: "flex-shrink-0 w-5 h-5 mt-0.5",
      active: isDark.value ? "text-white" : "text-violet-600",
      inactive: isDark.value ? "text-slate-400 group-hover:text-white" : "text-slate-400 group-hover:text-slate-700"
    }
  },
  separator: isDark.value ? "h-px bg-white/10 my-2" : "h-px bg-slate-200 my-2"
}));

const userMenu = computed(() => [
  [
    {
      label: t("layout.dropdown.userMenu.profile"),
      icon: "i-lucide-user",
      to: "/settings",
      shortcuts: ["⌘", "P"]
    },
    {
      label: t("layout.dropdown.userMenu.preferences"),
      icon: "i-lucide-sliders",
      to: "/settings"
    }
  ],
  [
    {
      label: t("layout.dropdown.userMenu.documentation"),
      icon: "i-lucide-book-open",
      click: () => window.open("https://docs.example.com", "_blank")
    },
    {
      label: t("layout.dropdown.userMenu.support"),
      icon: "i-lucide-life-buoy",
      click: () => {}
    }
  ],
  [
    {
      label: t("layout.dropdown.userMenu.signOut"),
      icon: "i-lucide-log-out",
      click: async () => {
        try {
          await auth.logout();
        } finally {
          await navigateTo("/login");
        }
      }
    }
  ]
]);

const route = useRoute();
watch(
  () => route.path,
  () => {
    sidebarOpen.value = false;
    search.value = "";
    closeSearchPanel();
  }
);

// Fetch pending appointments count
const fetchPendingCount = async () => {
  try {
    const apiBase = useApiBase();
    const response = await $fetch<{ data: { count: number } }>(
      `${apiBase}/appointments/count/pending`,
      {
        headers: {
          Authorization: `Bearer ${auth.accessToken.value}`
        }
      }
    );
    pendingAppointmentsCount.value = response.data.count;
  } catch (error) {
    console.error('Failed to fetch pending appointments count:', error);
  }
};

onMounted(() => {
  const handleKeydown = (e: KeyboardEvent) => {
    if ((e.metaKey || e.ctrlKey) && e.key === "k") {
      e.preventDefault();
      const searchInput = document.querySelector('input[data-search-input="global"]') as HTMLInputElement;
      searchInput?.focus();
    }
  };

  window.addEventListener("keydown", handleKeydown);

  // Fetch initial count
  fetchPendingCount();

  // Refresh count every 60 seconds
  const intervalId = setInterval(fetchPendingCount, 60000);

  // Connect to real-time notifications via SSE
  appointmentNotifications.connect();

  onUnmounted(() => {
    window.removeEventListener("keydown", handleKeydown);
    clearInterval(intervalId);
    appointmentNotifications.disconnect();
  });
});
</script>
