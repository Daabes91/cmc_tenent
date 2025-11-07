<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <UButton 
              variant="ghost" 
              color="gray"
              icon="i-lucide-arrow-left"
              @click="navigateBack"
            >
              Back to Reports
            </UButton>
            <div class="flex items-center gap-4">
              <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
                <UIcon name="i-lucide-credit-card" class="h-6 w-6 text-white" />
              </div>
              <div>
                <h1 class="text-2xl font-bold text-slate-900 dark:text-white">PayPal Transactions</h1>
                <p class="text-sm text-slate-600 dark:text-slate-300">Complete transaction history and analytics</p>
              </div>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="paypalLoading"
              @click="refreshData"
            >
              Refresh
            </UButton>
            <UButton 
              color="blue" 
              icon="i-lucide-download" 
              @click="exportData"
            >
              Export Data
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">
      <!-- Loading State -->
      <div v-if="paypalLoading" class="space-y-6">
        <div class="grid gap-4 md:grid-cols-4">
          <USkeleton v-for="i in 4" :key="i" class="h-24 rounded-2xl" />
        </div>
        <USkeleton class="h-96 rounded-2xl" />
      </div>

      <!-- Error State -->
      <UCard v-else-if="error" class="bg-red-50 dark:bg-red-900/20">
        <div class="flex items-center gap-3 text-red-600 dark:text-red-400">
          <UIcon name="i-lucide-alert-circle" class="h-6 w-6" />
          <div>
            <p class="font-semibold">Failed to load PayPal transaction data</p>
            <p class="text-sm">{{ error }}</p>
          </div>
        </div>
      </UCard>

      <!-- Main Content -->
      <div v-else-if="correctedPaypalData" class="space-y-8">
        <!-- Summary Metrics -->
        <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <UCard class="transition-shadow shadow-sm hover:shadow-lg">
            <div class="flex items-start justify-between">
              <div>
                <p class="text-sm text-slate-500 dark:text-slate-400">Total Revenue</p>
                <p class="mt-2 text-3xl font-bold text-slate-900 dark:text-white">{{ formatCurrency(correctedPaypalData.totalRevenue) }}</p>
                <p class="mt-2 text-xs text-blue-600 dark:text-blue-400">
                  <span class="font-semibold">{{ correctedPaypalData.completedTransactions }}</span> completed transactions
                </p>
              </div>
              <div class="rounded-lg bg-blue-100 dark:bg-blue-900/30 p-3 text-blue-600 dark:text-blue-400">
                <UIcon name="i-lucide-wallet" class="h-6 w-6" />
              </div>
            </div>
          </UCard>

          <UCard class="transition-shadow shadow-sm hover:shadow-lg">
            <div class="flex items-start justify-between">
              <div>
                <p class="text-sm text-slate-500 dark:text-slate-400">Success Rate</p>
                <p class="mt-2 text-3xl font-bold text-slate-900 dark:text-white">{{ correctedPaypalData.successRate.toFixed(1) }}%</p>
                <p class="mt-2 text-xs text-green-600 dark:text-green-400">
                  <span class="font-semibold">{{ correctedPaypalData.totalTransactions }}</span> total transactions
                </p>
              </div>
              <div class="rounded-lg bg-green-100 dark:bg-green-900/30 p-3 text-green-600 dark:text-green-400">
                <UIcon name="i-lucide-trending-up" class="h-6 w-6" />
              </div>
            </div>
          </UCard>

          <UCard class="transition-shadow shadow-sm hover:shadow-lg">
            <div class="flex items-start justify-between">
              <div>
                <p class="text-sm text-slate-500 dark:text-slate-400">Average Value</p>
                <p class="mt-2 text-3xl font-bold text-slate-900 dark:text-white">{{ formatCurrency(correctedPaypalData.averageTransactionValue) }}</p>
                <p class="mt-2 text-xs text-purple-600 dark:text-purple-400">
                  <span class="font-semibold">per transaction</span>
                </p>
              </div>
              <div class="rounded-lg bg-purple-100 dark:bg-purple-900/30 p-3 text-purple-600 dark:text-purple-400">
                <UIcon name="i-lucide-bar-chart-3" class="h-6 w-6" />
              </div>
            </div>
          </UCard>

          <UCard class="transition-shadow shadow-sm hover:shadow-lg">
            <div class="flex items-start justify-between">
              <div>
                <p class="text-sm text-slate-500 dark:text-slate-400">Net Revenue</p>
                <p class="mt-2 text-3xl font-bold text-slate-900 dark:text-white">{{ formatCurrency(correctedPaypalData.netRevenue) }}</p>
                <p class="mt-2 text-xs text-amber-600 dark:text-amber-400">
                  <span class="font-semibold">after PayPal fees</span>
                </p>
              </div>
              <div class="rounded-lg bg-amber-100 dark:bg-amber-900/30 p-3 text-amber-600 dark:text-amber-400">
                <UIcon name="i-lucide-piggy-bank" class="h-6 w-6" />
              </div>
            </div>
          </UCard>
        </div>

        <!-- Transaction History -->
        <UCard class="shadow-sm">
          <template #header>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <UIcon name="i-lucide-list" class="h-5 w-5 text-slate-600 dark:text-slate-300" />
                <h3 class="font-semibold text-slate-900 dark:text-white">Transaction History</h3>
              </div>
              <div class="flex items-center gap-2 text-sm text-slate-500 dark:text-slate-400">
                <span>{{ correctedPaypalData.pagination?.totalElements || 0 }} total transactions</span>
              </div>
            </div>
          </template>

          <div class="p-6">
            <!-- Transaction List -->
            <div v-if="correctedPaypalData.recentTransactions?.length > 0" class="space-y-3 mb-6">
              <div
                v-for="transaction in correctedPaypalData.recentTransactions"
                :key="transaction.id"
                class="group relative overflow-hidden rounded-xl border border-slate-200/70 bg-white/80 p-4 transition-all duration-300 hover:border-blue-300 hover:bg-blue-50/60 dark:border-white/10 dark:bg-white/5 dark:hover:border-blue-400/60 dark:hover:bg-blue-500/10 cursor-pointer"
                @click="openTransactionModal(transaction)"
              >
                <div class="flex items-start justify-between gap-4">
                  <div class="flex-1 min-w-0 space-y-1">
                    <div class="flex items-center gap-2 flex-wrap">
                      <p class="text-sm font-semibold text-slate-900 truncate dark:text-white group-hover:text-blue-700 dark:group-hover:text-blue-200">
                        {{ transaction.patientName || 'Unknown Patient' }}
                      </p>
                      <UBadge :color="getPaymentStatusColor(transaction.status)" variant="soft" size="xs">
                        {{ transaction.status }}
                      </UBadge>
                    </div>
                    <p class="text-xs text-slate-500 truncate dark:text-slate-400">
                      {{ transaction.doctorName || 'Unknown Doctor' }}
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
                      {{ formatCurrency(transaction.amount) }}
                    </p>
                    <p class="text-xs text-slate-500 dark:text-slate-400">
                      {{ transaction.currency }}
                    </p>
                  </div>
                </div>
                <div class="absolute inset-x-0 bottom-0 h-0.5 bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 opacity-0 transition group-hover:opacity-100"></div>
              </div>
            </div>

            <!-- Empty State -->
            <div v-else class="rounded-xl border border-dashed border-slate-200 dark:border-slate-600 p-12 text-center">
              <div class="flex flex-col items-center gap-4">
                <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                  <UIcon name="i-lucide-credit-card" class="h-8 w-8 text-slate-400" />
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-slate-900 dark:text-white">No PayPal transactions</h3>
                  <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                    PayPal transactions will appear here once virtual consultations are booked
                  </p>
                </div>
              </div>
            </div>

            <!-- Advanced Pagination Controls -->
            <div v-if="(correctedPaypalData.pagination?.totalPages || 1) > 1">
              <AdvancedPaginationControls
                :current-page="(correctedPaypalData.pagination?.currentPage || 0) + 1"
                :total-pages="correctedPaypalData.pagination?.totalPages || 1"
                :total-items="correctedPaypalData.pagination?.totalElements || 0"
                :page-size="pageSize"
                :has-previous="correctedPaypalData.pagination?.hasPrevious || false"
                :has-next="correctedPaypalData.pagination?.hasNext || false"
                :loading="paypalLoading"
                item-label="transactions"
                :page-sizes="[5, 10, 20, 50]"
                @page-change="handlePageChange"
                @page-size-change="handlePageSizeChange"
                @direct-page-input="handleDirectPageInput"
              />
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
            <h3 class="text-lg font-semibold text-slate-900 dark:text-white">Transaction Details</h3>
            <p class="text-sm text-slate-500 dark:text-slate-400">PayPal Order ID: {{ selectedTransaction.orderId || 'Unknown' }}</p>
          </div>
          <UBadge :color="getPaymentStatusColor(selectedTransaction.status)" variant="soft">
            {{ selectedTransaction.status || 'Unknown' }}
          </UBadge>
        </div>
      </template>

      <div class="space-y-6">
        <!-- Payment Information -->
        <div>
          <h4 class="text-sm font-semibold text-slate-700 dark:text-slate-200 mb-3">Payment Information</h4>
          <div class="grid gap-4 sm:grid-cols-2">
            <div>
              <p class="text-xs text-slate-500 dark:text-slate-400">Amount</p>
              <p class="text-lg font-bold text-slate-900 dark:text-white">
                {{ formatCurrency(selectedTransaction.amount || 0) }} {{ selectedTransaction.currency || 'USD' }}
              </p>
            </div>
            <div>
              <p class="text-xs text-slate-500 dark:text-slate-400">Transaction Type</p>
              <p class="text-sm font-medium text-slate-900 dark:text-white">
                {{ selectedTransaction.type?.replace('_', ' ') || 'Unknown' }}
              </p>
            </div>
            <div v-if="selectedTransaction.captureId">
              <p class="text-xs text-slate-500 dark:text-slate-400">Capture ID</p>
              <p class="text-sm font-mono text-slate-900 dark:text-white">
                {{ selectedTransaction.captureId }}
              </p>
            </div>
            <div>
              <p class="text-xs text-slate-500 dark:text-slate-400">Created</p>
              <p class="text-sm text-slate-900 dark:text-white">
                {{ selectedTransaction.createdAt ? formatDateTime(selectedTransaction.createdAt) : 'Unknown' }}
              </p>
            </div>
          </div>
        </div>

        <!-- Patient & Doctor Information -->
        <div>
          <h4 class="text-sm font-semibold text-slate-700 dark:text-slate-200 mb-3">Appointment Details</h4>
          <div class="grid gap-4 sm:grid-cols-2">
            <div>
              <p class="text-xs text-slate-500 dark:text-slate-400">Patient</p>
              <p class="text-sm font-medium text-slate-900 dark:text-white">
                {{ selectedTransaction.patientName || 'Unknown Patient' }}
              </p>
            </div>
            <div>
              <p class="text-xs text-slate-500 dark:text-slate-400">Doctor</p>
              <p class="text-sm font-medium text-slate-900 dark:text-white">
                {{ selectedTransaction.doctorName || 'Unknown Doctor' }}
              </p>
            </div>
            <div v-if="selectedTransaction.payerEmail">
              <p class="text-xs text-slate-500 dark:text-slate-400">Payer Email</p>
              <p class="text-sm text-slate-900 dark:text-white">
                {{ selectedTransaction.payerEmail }}
              </p>
            </div>
            <div v-if="selectedTransaction.payerName">
              <p class="text-xs text-slate-500 dark:text-slate-400">Payer Name</p>
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
            View Appointment
          </UButton>
          <UButton
            color="gray"
            variant="soft"
            icon="i-lucide-copy"
            @click="copyToClipboard(selectedTransaction.orderId || '')"
          >
            Copy Order ID
          </UButton>
        </div>
      </div>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const { fetcher } = useAdminApi();
const toast = useToast();
const { formatCurrency } = useClinicSettings();
const router = useRouter();

useHead({
  title: "PayPal Transactions – Clinic Admin"
});

// PayPal data types (same as reports page)
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
  recentTransactions: [],
  pagination: {
    currentPage: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 1,
    hasNext: false,
    hasPrevious: false
  }
};

const { data: paypalData, pending: paypalLoading, refresh: refreshPaypalData } = await useAsyncData(
  "paypal-transactions-all",
  async () => {
    const params = new URLSearchParams({
      page: currentPage.value.toString(),
      size: pageSize.value.toString()
    });
    return await fetcher<PayPalSummaryDTO>(`/reports/paypal-summary?${params}`, paypalFallback);
  }
);

const error = ref<string | null>(null);

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

// Transaction modal
const showTransactionModal = ref(false);
const selectedTransaction = ref<PayPalTransactionDTO | null>(null);

// Methods
const refreshData = async () => {
  await refreshPaypalData();
};

const exportData = () => {
  toast.add({
    title: "Export feature coming soon",
    description: "Data export functionality will be available in the next update.",
    color: "blue",
    icon: "i-lucide-info"
  });
};

const navigateBack = () => {
  router.push('/reports');
};

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
      title: "Copied to clipboard",
      description: `Order ID ${text} copied successfully`,
      color: "green",
      icon: "i-lucide-check"
    });
  } catch {
    toast.add({
      title: "Copy failed",
      description: "Failed to copy to clipboard",
      color: "red",
      icon: "i-lucide-x"
    });
  }
};

const navigateTo = (path: string) => router.push(path);

// Pagination functions
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

const handleDirectPageInput = async (page: number) => {
  // This is already handled by handlePageChange, but we can add specific logic if needed
  await handlePageChange(page);
};
</script>