<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-calendar" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t("appointments.list.title") }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t("appointments.list.subtitle") }}</p>
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
              Refresh
            </UButton>
            <UButton 
              color="blue" 
              icon="i-lucide-plus" 
              @click="openCreate"
            >
              {{ t("appointments.list.actions.new") }}
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
              <UIcon name="i-lucide-calendar" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t("appointments.list.stats.total") }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ totalItems }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">All appointments</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-900/20">
              <UIcon name="i-lucide-clock" class="h-5 w-5 text-amber-600 dark:text-amber-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t("appointments.list.stats.scheduled") }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ scheduledCount }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">Awaiting confirmation</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
              <UIcon name="i-lucide-check-circle" class="h-5 w-5 text-green-600 dark:text-green-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t("appointments.list.stats.confirmed") }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ confirmedCount }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">Ready to proceed</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-50 dark:bg-purple-900/20">
              <UIcon name="i-lucide-circle-check-big" class="h-5 w-5 text-purple-600 dark:text-purple-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t("appointments.list.stats.completed") }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ completedCount }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">Successfully finished</p>
        </div>
      </div>

      <!-- Filters Section -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden mb-6">
        <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-filter" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">Filters & Search</h2>
              <p class="text-sm text-slate-300">Find specific appointments</p>
            </div>
          </div>
        </div>
        <div class="p-6">
      <!-- Main Filters Row -->
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-4">
        <UInput
          v-model="search"
          icon="i-lucide-search"
          placeholder="Search appointments..."
          size="lg"
        />

        <USelect
          v-model="filter"
          :options="filterOptions"
          placeholder="Time Range"
          size="lg"
        />

        <USelect
          v-model="statusFilter"
          :options="statusOptions"
          placeholder="Status"
          size="lg"
        />

        <div class="flex gap-2">
          <UButton
            icon="i-lucide-refresh-cw"
            size="lg"
            color="gray"
            variant="soft"
            :loading="pending"
            @click="refresh"
          >
            Refresh
          </UButton>
          <UButton
            icon="i-lucide-x"
            size="lg"
            color="gray"
            variant="ghost"
            @click="clearAllFilters"
          >
            Clear
          </UButton>
        </div>
      </div>

      <!-- Advanced Filters (Collapsible) -->
      <div v-if="showAdvancedFilters" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 pt-4 border-t border-slate-200">
        <USelect
          v-model="bookingModeFilter"
          :options="bookingModeOptions"
          placeholder="Booking Mode"
          size="lg"
        />

        <USelect
          v-model="paymentFilter"
          :options="paymentOptions"
          placeholder="Payment Status"
          size="lg"
        />

        <UInput
          v-model="fromDate"
          type="date"
          placeholder="From Date"
          size="lg"
        />

        <UInput
          v-model="toDate"
          type="date"
          placeholder="To Date"
          size="lg"
        />
      </div>

      <!-- Quick Filters -->
      <div class="flex flex-wrap gap-2 mt-4 pt-4 border-t border-slate-200">
        <UButton
          size="xs"
          variant="soft"
          color="blue"
          @click="applyQuickFilter('today-scheduled')"
        >
          Today's Scheduled
        </UButton>
        <UButton
          size="xs"
          variant="soft"
          color="green"
          @click="applyQuickFilter('unpaid')"
        >
          Unpaid Appointments
        </UButton>
        <UButton
          size="xs"
          variant="soft"
          color="purple"
          @click="applyQuickFilter('virtual')"
        >
          Virtual Consultations
        </UButton>
        <UButton
          size="xs"
          variant="soft"
          color="orange"
          @click="applyQuickFilter('pending-confirmation')"
        >
          Pending Confirmation
        </UButton>
      </div>

      <!-- Toggle Advanced Filters -->
      <div class="flex items-center justify-between mt-4">
        <UButton
          variant="ghost"
          color="gray"
          size="sm"
          @click="showAdvancedFilters = !showAdvancedFilters"
        >
          <UIcon :name="showAdvancedFilters ? 'i-lucide-chevron-up' : 'i-lucide-chevron-down'" class="mr-2 h-4 w-4" />
          {{ showAdvancedFilters ? 'Hide Advanced Filters' : 'Show Advanced Filters' }}
        </UButton>

        <div class="flex items-center gap-4">
          <div class="text-sm text-slate-500">
            {{ totalItems }} appointments found
          </div>
          <UButton
            size="xs"
            variant="ghost"
            color="gray"
            icon="i-lucide-download"
            @click="exportAppointments"
          >
            Export
          </UButton>
        </div>
      </div>

      <!-- Active Filters -->
      <div v-if="doctorIdFilter || patientIdFilter" class="mt-4 flex flex-wrap gap-2">
        <UBadge
          v-if="doctorIdFilter"
          color="blue"
          variant="soft"
          size="md"
        >
          <UIcon name="i-lucide-stethoscope" class="mr-1 h-3 w-3" />
          {{ t("appointments.list.badges.doctorFilter") }}
          <button @click="clearDoctorFilter" class="ml-2 hover:text-blue-700">
            <UIcon name="i-lucide-x" class="h-3 w-3" />
          </button>
        </UBadge>
        <UBadge
          v-if="patientIdFilter"
          color="green"
          variant="soft"
          size="md"
        >
          <UIcon name="i-lucide-user" class="mr-1 h-3 w-3" />
          {{ t("appointments.list.badges.patientFilter") }}
          <button @click="clearPatientFilter" class="ml-2 hover:text-green-700">
            <UIcon name="i-lucide-x" class="h-3 w-3" />
          </button>
        </UBadge>
      </div>

      <div class="mt-4 flex flex-wrap items-center justify-between gap-3">
        <div class="flex items-center gap-2 text-sm text-slate-500">
          <UIcon name="i-lucide-info" class="h-4 w-4 text-slate-400" />
          <span>{{ t("appointments.list.pagination", { page, total: totalPages }) }}</span>
        </div>
        <div class="flex items-center gap-2">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t("appointments.list.view.label") }}</span>
          <UButtonGroup size="sm">
            <UButton
              v-for="mode in viewModes"
              :key="mode.value"
              :icon="mode.icon"
              :variant="viewMode === mode.value ? 'solid' : 'ghost'"
              :color="viewMode === mode.value ? 'primary' : 'gray'"
              class="font-medium"
              @click="viewMode = mode.value"
            >
              <span class="hidden sm:inline">{{ mode.label }}</span>
            </UButton>
          </UButtonGroup>
        </div>
      </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="pending && !appointments.length" class="space-y-4">
        <div v-for="i in 5" :key="i" class="rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60">
        <div class="flex items-start justify-between gap-4">
          <div class="flex items-start gap-4 flex-1">
            <USkeleton class="h-12 w-12 rounded-full" />
            <div class="flex-1 space-y-3">
              <USkeleton class="h-5 w-48" />
              <div class="flex gap-4">
                <USkeleton class="h-4 w-32" />
                <USkeleton class="h-4 w-24" />
                <USkeleton class="h-4 w-28" />
              </div>
              <div class="flex gap-2">
                <USkeleton class="h-6 w-16 rounded-full" />
                <USkeleton class="h-6 w-20 rounded-full" />
              </div>
            </div>
          </div>
          <div class="text-right space-y-2">
            <USkeleton class="h-5 w-20" />
            <USkeleton class="h-4 w-24" />
          </div>
        </div>
      </div>
    </div>

      <!-- Empty State -->
      <div v-else-if="!filteredRows.length" class="rounded-2xl bg-white dark:bg-slate-800 p-12 text-center shadow-sm border border-slate-200/60 dark:border-slate-700/60">
        <div class="mx-auto max-w-md">
          <div class="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-slate-100 dark:bg-slate-700">
            <UIcon name="i-lucide-calendar-x" class="h-10 w-10 text-slate-400" />
          </div>
          <h3 class="mt-6 text-xl font-semibold text-slate-900 dark:text-white">
          {{ hasActiveFilters ? 'No appointments match your filters' : 'No appointments found' }}
        </h3>
          <p class="mt-2 text-sm text-slate-600 dark:text-slate-300">
          {{ hasActiveFilters 
            ? 'Try adjusting your filters or clearing them to see more results.' 
            : 'Get started by creating your first appointment or check back later.' 
          }}
        </p>
        <div class="mt-6 flex justify-center gap-3">
          <UButton v-if="hasActiveFilters" color="gray" variant="soft" @click="clearAllFilters">
            <UIcon name="i-lucide-filter-x" class="mr-2 h-4 w-4" />
            Clear All Filters
          </UButton>
          <UButton color="primary" @click="openCreate">
            <UIcon name="i-lucide-plus" class="mr-2 h-4 w-4" />
            New Appointment
          </UButton>
        </div>
      </div>
    </div>

      <!-- Appointments List -->
      <div v-else class="relative">
      <!-- Refresh Overlay -->
      <div v-if="pending && appointments.length" class="absolute inset-0 bg-white/50 dark:bg-slate-900/50 backdrop-blur-sm z-10 flex items-center justify-center rounded-2xl">
        <div class="bg-white dark:bg-slate-800 rounded-lg shadow-lg p-4 flex items-center gap-3 border border-slate-200 dark:border-slate-700">
          <UIcon name="i-lucide-loader-2" class="h-5 w-5 animate-spin text-blue-600" />
          <span class="text-sm font-medium text-slate-700 dark:text-slate-300">Refreshing appointments...</span>
        </div>
      </div>

      <template v-if="viewMode === 'cards'">
        <div class="space-y-3">
          <div
            v-for="row in filteredRows"
            :key="row.id"
            class="group rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 transition-all hover:shadow-md hover:border-blue-300 dark:hover:border-blue-600 cursor-pointer"
            @click="openDetails(row)"
          >
            <div class="flex items-start justify-between gap-4">
            <!-- Left Section: Patient Info -->
            <div class="flex items-start gap-4 flex-1">
              <UAvatar
                :alt="row.patientName"
                size="lg"
                class="ring-2 ring-slate-100"
              />
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <h3 class="text-lg font-semibold text-slate-900 dark:text-white truncate">
                    {{ row.patientName }}
                  </h3>
                  <UBadge :color="statusColor(row.status)" variant="soft" size="sm">
                    {{ formatStatus(row.status) }}
                  </UBadge>
                  <UBadge :color="patientConfirmationColor(row.patientConfirmed)" variant="outline" size="xs">
                    {{ patientConfirmationLabel(row.patientConfirmed) }}
                  </UBadge>
                </div>

                <div class="flex flex-wrap items-center gap-4 text-sm text-slate-600 dark:text-slate-300">
                  <div class="flex items-center gap-1.5">
                    <UIcon name="i-lucide-briefcase-medical" class="h-4 w-4" />
                    <span>{{ row.serviceName }}</span>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <UIcon name="i-lucide-stethoscope" class="h-4 w-4" />
                    <span>{{ t("common.titles.doctorWithName", { name: row.doctorName }) }}</span>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <UIcon :name="row.bookingMode === 'VIRTUAL_CONSULTATION' ? 'i-lucide-video' : 'i-lucide-building-2'" class="h-4 w-4" />
                    <span>{{ row.bookingModeLabel }}</span>
                  </div>
                </div>
                <div class="mt-3 flex flex-wrap items-start gap-3">
                  <div class="flex flex-col items-start">
                    <UBadge :color="paymentStatusColor(row.paymentCollected)" variant="soft" size="xs">
                      <UIcon name="i-lucide-wallet" :class="['mr-1 h-3 w-3', row.paymentCollected ? 'text-green-600' : 'text-slate-400']" />
                      {{ paymentStatusLabel(row.paymentCollected) }}
                    </UBadge>
                    <span
                      v-if="row.paymentCollected && row.paymentAmount !== null"
                      class="text-xs text-slate-500 dark:text-slate-400 mt-1"
                    >
                      {{ formatCurrencyValue(row.paymentAmount, row.paymentCurrency) }}
                    </span>
                  </div>
                  <UBadge :color="attendanceStatusColor(row.patientAttended)" variant="soft" size="xs">
                    <UIcon :name="attendanceStatusIcon(row.patientAttended)" class="mr-1 h-3 w-3" />
                    {{ attendanceStatusLabel(row.patientAttended) }}
                  </UBadge>
                </div>
              </div>
            </div>

            <!-- Right Section: Time & Actions -->
            <div class="flex items-center gap-4">
              <div class="text-right">
                <div class="flex items-center gap-1.5 text-slate-900 font-semibold">
                  <UIcon name="i-lucide-clock" class="h-4 w-4" />
                  {{ formatTime(row.scheduledAtRaw) }}
                </div>
                <p class="mt-1 text-sm text-slate-600">
                  {{ formatDate(row.scheduledAtRaw) }}
                </p>
              </div>

              <div class="flex items-center gap-2" @click.stop>
                <UTooltip :text="t('appointments.list.actions.approve')" v-if="canApprove(row.status)">
                  <UButton
                    icon="i-lucide-check"
                    color="green"
                    size="sm"
                    :loading="approveLoadingId === row.id"
                    @click="approveAppointment(row)"
                  />
                </UTooltip>
                <UTooltip :text="t('appointments.list.actions.decline')" v-if="canDecline(row.status)">
                  <UButton
                    icon="i-lucide-x"
                    color="red"
                    size="sm"
                    variant="soft"
                    :loading="declineLoadingId === row.id"
                    @click="declineAppointment(row)"
                  />
                </UTooltip>
                <UButton
                  icon="i-lucide-chevron-right"
                  color="gray"
                  size="sm"
                  variant="ghost"
                />
              </div>
            </div>
            </div>
          </div>
        </div>
      </template>

      <template v-else>
        <UCard class="border border-slate-200 shadow-sm">
          <UTable :columns="tableColumns" :rows="tableRows">
            <template #patient-data="{ row }">
              <div class="flex items-center gap-3">
                <UAvatar size="sm" class="ring-2 ring-slate-100" />
                <div>
                  <p class="text-sm font-semibold text-slate-900">{{ row.patientName }}</p>
                  <p class="text-xs text-slate-500">{{ row.serviceName }}</p>
                </div>
              </div>
            </template>
            <template #doctor-data="{ row }">
              <div class="text-sm text-slate-700">{{ t("common.titles.doctorWithName", { name: row.doctorName }) }}</div>
            </template>
            <template #schedule-data="{ row }">
              <div class="flex flex-col text-sm text-slate-700">
                <span class="font-semibold">{{ row.scheduledTime }}</span>
                <span class="text-xs text-slate-500">{{ row.scheduledDate }}</span>
              </div>
            </template>
            <template #mode-data="{ row }">
              <div class="flex items-center gap-2 text-sm text-slate-700">
                <UIcon :name="row.bookingMode === 'VIRTUAL_CONSULTATION' ? 'i-lucide-video' : 'i-lucide-building-2'" class="h-4 w-4 text-slate-400" />
                <span>{{ row.bookingModeLabel }}</span>
              </div>
            </template>
            <template #payment-data="{ row }">
              <div class="flex flex-col items-start gap-1">
                <UBadge :color="paymentStatusColor(row.paymentCollected)" variant="soft" size="sm">
                  {{ paymentStatusLabel(row.paymentCollected) }}
                </UBadge>
                <span
                  v-if="row.paymentCollected && row.paymentAmount !== null"
                  class="text-xs text-slate-500"
                >
                  {{ formatCurrencyValue(row.paymentAmount, row.paymentCurrency) }}
                </span>
              </div>
            </template>
            <template #attendance-data="{ row }">
              <UBadge :color="attendanceStatusColor(row.patientAttended)" variant="soft" size="sm">
                {{ attendanceStatusLabel(row.patientAttended) }}
              </UBadge>
            </template>
            <template #status-data="{ row }">
              <div class="flex flex-col gap-1 items-start">
                <UBadge :color="statusColor(row.status)" variant="soft" size="sm">
                  {{ formatStatus(row.status) }}
                </UBadge>
                <UBadge :color="patientConfirmationColor(row.patientConfirmed)" variant="outline" size="xs">
                  {{ patientConfirmationLabel(row.patientConfirmed) }}
                </UBadge>
              </div>
            </template>
            <template #actions-data="{ row }">
              <div class="flex justify-end gap-1" @click.stop>
                <UTooltip :text="t('appointments.list.actions.approve')" v-if="canApprove(row.status)">
                  <UButton
                    icon="i-lucide-check"
                    color="green"
                    size="xs"
                    :loading="approveLoadingId === row.id"
                    @click="approveAppointment(row)"
                  />
                </UTooltip>
                <UTooltip :text="t('appointments.list.actions.decline')" v-if="canDecline(row.status)">
                  <UButton
                    icon="i-lucide-x"
                    color="red"
                    variant="soft"
                    size="xs"
                    :loading="declineLoadingId === row.id"
                    @click="declineAppointment(row)"
                  />
                </UTooltip>
                <UTooltip :text="t('appointments.list.actions.viewDetails')">
                  <UButton
                    icon="i-lucide-chevron-right"
                    color="gray"
                    variant="ghost"
                    size="xs"
                    @click="openDetails(row)"
                  />
                </UTooltip>
              </div>
            </template>
          </UTable>
          <div class="mt-4 flex flex-wrap items-center justify-between gap-3 text-xs text-slate-500">
            <span>{{ t("appointments.list.table.summary", { current: tableRows.length, total: totalItems }) }}</span>
            <UButton color="gray" variant="ghost" size="sm" @click="refresh">
              <UIcon name="i-lucide-refresh-cw" class="mr-1.5 h-3.5 w-3.5" />
              {{ t("appointments.list.actions.sync") }}
            </UButton>
          </div>
        </UCard>
      </template>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="mt-8 flex justify-center">
      <UPagination
        v-model="page"
        :page-count="pageSize"
        :total="totalItems"
        :ui="{
          wrapper: 'flex items-center gap-1',
          rounded: '!rounded-full',
          default: {
            activeButton: {
              variant: 'solid'
            }
          }
        }"
      />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AppointmentAdmin } from "@/types/appointments";
import { watch, computed, ref } from "vue";
import { useAppointmentTime } from "../../composables/useAppointmentTime";

const router = useRouter();
const route = useRoute();
const toast = useToast();
const { t, locale } = useI18n();
const { request: adminRequest } = useAdminApi();

// Pagination
const page = ref(parseInt(route.query.page as string) || 1);
const pageSize = 10;

type AppointmentPage = {
  content: AppointmentAdmin[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

// Filters
const search = ref("");
const filter = ref<string>("upcoming");
const statusFilter = ref<string>("all");
const bookingModeFilter = ref<string>("all");
const paymentFilter = ref<string>("all");
const fromDate = ref<string>("");
const toDate = ref<string>("");
const showAdvancedFilters = ref(false);

type ViewMode = "cards" | "table";
const viewModes = computed(() => [
  { label: t("appointments.list.view.modes.cards"), value: "cards" as ViewMode, icon: "i-lucide-layout-grid" },
  { label: t("appointments.list.view.modes.table"), value: "table" as ViewMode, icon: "i-lucide-table" }
]);

const viewMode = ref<ViewMode>("cards");

const runtimeConfig = useRuntimeConfig();
const defaultCurrency = (runtimeConfig.public?.defaultCurrency as string | undefined)?.toUpperCase?.() || "JOD";

const filterOptions = [
  { label: "Upcoming", value: "upcoming" },
  { label: "Today", value: "today" },
  { label: "This Week", value: "week" },
  { label: "All", value: "all" }
];

const statusOptions = [
  { label: "All Statuses", value: "all" },
  { label: "Scheduled", value: "SCHEDULED" },
  { label: "Confirmed", value: "CONFIRMED" },
  { label: "Completed", value: "COMPLETED" },
  { label: "Cancelled", value: "CANCELLED" }
];

const bookingModeOptions = [
  { label: "All Modes", value: "all" },
  { label: "Clinic Visit", value: "CLINIC_VISIT" },
  { label: "Virtual Consultation", value: "VIRTUAL_CONSULTATION" }
];

const paymentOptions = [
  { label: "All Payments", value: "all" },
  { label: "Paid", value: "true" },
  { label: "Unpaid", value: "false" }
];

const doctorIdFilter = computed(() => route.query.doctorId ? parseInt(route.query.doctorId as string) : null);
const patientIdFilter = computed(() => route.query.patientId ? parseInt(route.query.patientId as string) : null);

// Fetch appointments
const defaultPage: AppointmentPage = {
  content: [],
  totalElements: 0,
  totalPages: 0,
  number: 0,
  size: pageSize
};

const {
  data: appointmentPage,
  pending,
  refresh: baseRefresh
} = await useAsyncData("admin-appointments", async () => {
  try {
    const queryParams: any = {
      page: Math.max(page.value - 1, 0),
      size: pageSize
    };

    // Add filters only if they're not default values
    if (filter.value && filter.value !== "all") {
      queryParams.filter = filter.value;
    }
    
    if (statusFilter.value && statusFilter.value !== "all") {
      queryParams.status = statusFilter.value;
    }
    
    if (bookingModeFilter.value && bookingModeFilter.value !== "all") {
      queryParams.bookingMode = bookingModeFilter.value;
    }
    
    if (paymentFilter.value && paymentFilter.value !== "all") {
      queryParams.paymentCollected = paymentFilter.value === "true";
    }
    
    if (doctorIdFilter.value) {
      queryParams.doctorId = doctorIdFilter.value;
    }
    
    if (patientIdFilter.value) {
      queryParams.patientId = patientIdFilter.value;
    }
    
    if (fromDate.value) {
      queryParams.fromDate = fromDate.value;
    }
    
    if (toDate.value) {
      queryParams.toDate = toDate.value;
    }
    
    if (search.value.trim()) {
      queryParams.search = search.value.trim();
    }

    return await adminRequest<AppointmentPage>("/appointments", { query: queryParams });
  } catch (error) {
    console.error("[appointments] Failed to load appointments:", error);
    toast.add({
      title: "Failed to load appointments",
      description: "Please try again or contact support if the problem persists.",
      color: "red"
    });
    return defaultPage;
  }
}, {
  default: () => defaultPage,
  watch: [page, filter, statusFilter, bookingModeFilter, paymentFilter, 
          fromDate, toDate, search, doctorIdFilter, patientIdFilter],
  server: false
});

const appointments = computed(() => appointmentPage.value?.content ?? []);
const totalItems = computed(() => appointmentPage.value?.totalElements ?? appointments.value.length ?? 0);
const totalPages = computed(() => Math.max(appointmentPage.value?.totalPages ?? 1, 1));

const scheduledCount = computed(() =>
  appointments.value?.filter(a => a.status === "SCHEDULED").length || 0
);
const confirmedCount = computed(() =>
  appointments.value?.filter(a => a.status === "CONFIRMED").length || 0
);
const completedCount = computed(() =>
  appointments.value?.filter(a => a.status === "COMPLETED").length || 0
);

const tableColumns = computed(() => [
  { key: "patient", label: t("appointments.list.table.columns.patient") },
  { key: "doctor", label: t("appointments.list.table.columns.doctor") },
  { key: "schedule", label: t("appointments.list.table.columns.schedule") },
  { key: "mode", label: t("appointments.list.table.columns.mode") },
  { key: "payment", label: t("appointments.list.table.columns.payment") },
  { key: "attendance", label: t("appointments.list.table.columns.attendance") },
  { key: "status", label: t("appointments.list.table.columns.status") },
  { key: "actions", label: "", class: "w-20 text-right" }
] as const);

// Transform data
const rows = computed(() => {
  if (!appointments.value) return [];
  return appointments.value.map(appt => ({
    id: appt.id,
    patientName: appt.patientName || t("appointments.list.labels.unknownPatient"),
    serviceName: appt.serviceName || "—",
    doctorName: appt.doctorName || t("appointments.list.labels.unassignedDoctor"),
    bookingMode: appt.bookingMode || "CLINIC_VISIT",
    bookingModeLabel: formatMode(appt.bookingMode),
    status: appt.status,
    scheduledAtRaw: appt.scheduledAt,
    paymentCollected: Boolean(appt.paymentCollected),
    patientAttended: appt.patientAttended ?? null,
    patientConfirmed: appt.patientConfirmed ?? false,
    patientConfirmedAt: appt.patientConfirmedAt || null,
    paymentAmount: appt.paymentAmount ?? null,
    paymentCurrency: appt.paymentCurrency ?? null,
    paymentMethod: appt.paymentMethod ?? null
  }));
});

const tableRows = computed(() => rows.value.map(row => ({
  id: row.id,
  patient: row.patientName,
  patientName: row.patientName,
  doctor: row.doctorName,
  doctorName: row.doctorName,
  serviceName: row.serviceName,
  bookingMode: row.bookingMode,
  bookingModeLabel: row.bookingModeLabel,
  status: row.status,
  scheduledAtRaw: row.scheduledAtRaw,
  scheduledDate: formatDate(row.scheduledAtRaw),
  scheduledTime: formatTime(row.scheduledAtRaw),
  paymentCollected: row.paymentCollected,
  patientAttended: row.patientAttended,
  patientConfirmed: row.patientConfirmed,
  patientConfirmedAt: row.patientConfirmedAt,
  paymentAmount: row.paymentAmount,
  paymentCurrency: row.paymentCurrency,
  paymentMethod: row.paymentMethod
})));

// Use rows directly since filtering is now done on the backend
const filteredRows = computed(() => rows.value);

// Check if any filters are active
const hasActiveFilters = computed(() => {
  return search.value.trim() !== '' ||
         filter.value !== 'upcoming' ||
         statusFilter.value !== 'all' ||
         bookingModeFilter.value !== 'all' ||
         paymentFilter.value !== 'all' ||
         fromDate.value !== '' ||
         toDate.value !== '' ||
         doctorIdFilter.value !== null ||
         patientIdFilter.value !== null;
});



// Reset page when filters change
watch([filter, statusFilter, bookingModeFilter, paymentFilter, 
       fromDate, toDate, search], () => {
  page.value = 1;
});

watch([doctorIdFilter, patientIdFilter], () => {
  page.value = 1;
});

watch(totalPages, (next) => {
  if (page.value > next) {
    page.value = next;
  }
});

// Use the new appointment time composable for consistent time handling
const { 
  formatAppointmentTime, 
  formatForList,
  timezone,
  abbreviation 
} = useAppointmentTime();

function formatDate(date: string | number | null | undefined) {
  if (!date) return "—";
  // Use the appointment time composable for consistent date formatting
  const formatted = formatAppointmentTime(date);
  if (formatted === "Not scheduled" || formatted === "Invalid date") return "—";
  
  // Extract just the date part for list display
  try {
    const datePart = formatted.split(',').slice(0, 2).join(','); // "Jan 15, 2024"
    return datePart;
  } catch (error) {
    console.warn('Error extracting date part:', error);
    return formatted;
  }
}

function formatTime(date: string | number | null | undefined) {
  if (!date) return "—";
  // Use the appointment time composable for list display
  const formatted = formatForList(date);
  if (formatted === "Not scheduled" || formatted === "Invalid date") return "—";
  
  // Extract just the time part for list display
  try {
    const parts = formatted.split(',');
    if (parts.length >= 2) {
      // Extract time and timezone: "2:30 PM EET"
      const timePart = parts[parts.length - 1].trim();
      return timePart;
    }
    return formatted;
  } catch (error) {
    console.warn('Error extracting time part:', error);
    return formatted;
  }
}

function formatDateTime(date: string | number | null | undefined) {
  if (!date) return "—";
  // Use the appointment time composable for full datetime display
  const formatted = formatAppointmentTime(date);
  return formatted === "Not scheduled" || formatted === "Invalid date" ? "—" : formatted;
}

async function openCreate() {
  await navigateTo("/appointments/new");
}

function openDetails(row: any) {
  router.push(`/appointments/${row.id}`);
}

// Approval/Decline logic
const { request } = useAuthorizedFetch();
const approveLoadingId = ref<number | null>(null);
const declineLoadingId = ref<number | null>(null);

function canApprove(status: string | undefined) {
  return status === "SCHEDULED";
}

function canDecline(status: string | undefined) {
  return status === "SCHEDULED" || status === "CONFIRMED";
}

async function approveAppointment(row: any) {
  if (!canApprove(row.status) || approveLoadingId.value === row.id) return;
  approveLoadingId.value = row.id;
  try {
    await request(`/appointments/${row.id}/approve`, { method: "POST" });
    toast.add({ title: t("appointments.list.toasts.approveSuccess"), color: "green" });
    await baseRefresh();
  } catch (error: any) {
    toast.add({
      title: t("appointments.list.toasts.approveError.title"),
      description: error?.data?.message ?? error?.message ?? t("common.errors.unexpected"),
      color: "red"
    });
  } finally {
    approveLoadingId.value = null;
  }
}

async function declineAppointment(row: any) {
  if (!canDecline(row.status) || declineLoadingId.value === row.id) return;
  declineLoadingId.value = row.id;
  try {
    await request(`/appointments/${row.id}/decline`, { method: "POST" });
    toast.add({ title: t("appointments.list.toasts.declineSuccess"), color: "red" });
    await baseRefresh();
  } catch (error: any) {
    toast.add({
      title: t("appointments.list.toasts.declineError.title"),
      description: error?.data?.message ?? error?.message ?? t("common.errors.unexpected"),
      color: "red"
    });
  } finally {
    declineLoadingId.value = null;
  }
}

async function refresh() {
  await baseRefresh();
  toast.add({ title: t("appointments.list.toasts.refresh"), color: "blue" });
}

function statusColor(status: string | undefined) {
  switch (status) {
    case "CONFIRMED":
      return "green";
    case "COMPLETED":
      return "blue";
    case "CANCELLED":
      return "red";
    case "SCHEDULED":
    default:
      return "amber";
  }
}

const statusLabels = computed(() => ({
  SCHEDULED: t("appointments.status.scheduled"),
  CONFIRMED: t("appointments.status.confirmed"),
  COMPLETED: t("appointments.status.completed"),
  CANCELLED: t("appointments.status.cancelled")
}));

const modeLabels = computed(() => ({
  VIRTUAL_CONSULTATION: t("appointments.modes.virtual"),
  CLINIC_VISIT: t("appointments.modes.clinic")
}));

function patientConfirmationColor(confirmed: boolean | undefined) {
  return confirmed ? "green" : "gray";
}

function patientConfirmationLabel(confirmed: boolean | undefined) {
  return confirmed ? t("appointments.status.patientConfirmed") : t("appointments.status.patientPending");
}

function formatStatus(status: string | undefined) {
  if (!status) return "";
  return statusLabels.value[status as keyof typeof statusLabels.value] ?? status;
}

function formatMode(mode: string | undefined) {
  return modeLabels.value[mode as keyof typeof modeLabels.value] ?? modeLabels.value.CLINIC_VISIT;
}

function formatCurrencyValue(amount?: number | null, currency?: string | null) {
  if (amount === null || amount === undefined) {
    return "—";
  }
  const numericAmount = Number(amount);
  if (!Number.isFinite(numericAmount)) {
    return "—";
  }
  const safeCurrency = currency && currency.trim().length > 0
    ? currency.trim().toUpperCase().slice(0, 3)
    : defaultCurrency;
  try {
    return new Intl.NumberFormat(locale.value || undefined, {
      style: "currency",
      currency: safeCurrency
    }).format(numericAmount);
  } catch (error) {
    return `${numericAmount.toFixed(2)} ${safeCurrency}`;
  }
}

function paymentStatusLabel(collected?: boolean) {
  return collected ? t("appointments.payment.paid") : t("appointments.payment.unpaid");
}

function paymentStatusColor(collected?: boolean) {
  return collected ? "green" : "gray";
}

function attendanceStatusLabel(attended?: boolean | null) {
  if (attended === true) return t("appointments.attendance.attended");
  if (attended === false) return t("appointments.attendance.noShow");
  return t("appointments.attendance.pending");
}

function attendanceStatusColor(attended?: boolean | null) {
  if (attended === true) return "green";
  if (attended === false) return "red";
  return "gray";
}

function attendanceStatusIcon(attended?: boolean | null) {
  if (attended === true) return "i-lucide-user-check";
  if (attended === false) return "i-lucide-user-x";
  return "i-lucide-user";
}

function clearDoctorFilter() {
  const query = { ...route.query };
  delete query.doctorId;
  router.push({ path: route.path, query });
}

function clearPatientFilter() {
  const query = { ...route.query };
  delete query.patientId;
  router.push({ path: route.path, query });
}

function clearAllFilters() {
  search.value = "";
  filter.value = "upcoming";
  statusFilter.value = "all";
  bookingModeFilter.value = "all";
  paymentFilter.value = "all";
  fromDate.value = "";
  toDate.value = "";
}

function applyQuickFilter(type: string) {
  // Clear existing filters first
  clearAllFilters();
  
  switch (type) {
    case 'today-scheduled':
      filter.value = "today";
      statusFilter.value = "SCHEDULED";
      break;
    case 'unpaid':
      paymentFilter.value = "false";
      showAdvancedFilters.value = true;
      break;
    case 'virtual':
      bookingModeFilter.value = "VIRTUAL_CONSULTATION";
      showAdvancedFilters.value = true;
      break;
    case 'pending-confirmation':
      statusFilter.value = "SCHEDULED";
      break;
  }
}

async function exportAppointments() {
  try {
    // Create CSV content from current filtered appointments
    const csvContent = generateCSV(filteredRows.value);
    
    // Create and download file
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `appointments-${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    toast.add({
      title: "Export successful",
      description: `Exported ${filteredRows.value.length} appointments to CSV`,
      color: "green"
    });
  } catch (error) {
    toast.add({
      title: "Export failed",
      description: "Failed to export appointments. Please try again.",
      color: "red"
    });
  }
}

function generateCSV(appointments: any[]) {
  const headers = [
    'ID', 'Patient Name', 'Doctor Name', 'Service', 'Date', 'Time', 
    'Status', 'Booking Mode', 'Payment Collected', 'Patient Attended'
  ];
  
  const csvRows = [
    headers.join(','),
    ...appointments.map(apt => [
      apt.id,
      `"${apt.patientName}"`,
      `"${apt.doctorName}"`,
      `"${apt.serviceName}"`,
      formatDate(apt.scheduledAtRaw),
      formatTime(apt.scheduledAtRaw),
      apt.status,
      apt.bookingModeLabel,
      apt.paymentCollected ? 'Yes' : 'No',
      apt.patientAttended === true ? 'Yes' : apt.patientAttended === false ? 'No' : 'Pending'
    ].join(','))
  ];
  
  return csvRows.join('\n');
}

// Watch for page changes
watch(page, (newPage) => {
  const query = { ...route.query };
  if (newPage <= 1) {
    delete query.page;
  } else {
    query.page = String(newPage);
  }
  router.push({ path: route.path, query });
});

watch(() => route.query.page, (value) => {
  const parsed = value ? parseInt(value as string, 10) : 1;
  if (!Number.isNaN(parsed) && parsed !== page.value) {
    page.value = parsed;
  }
});
</script>
