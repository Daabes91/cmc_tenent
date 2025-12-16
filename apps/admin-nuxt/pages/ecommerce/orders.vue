<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50/50 via-white to-indigo-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/95 backdrop-blur-xl border-b border-blue-200/60 shadow-sm dark:bg-slate-900/95 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-600 to-blue-800 shadow-lg">
              <UIcon name="i-lucide-shopping-bag" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">Order Management</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">Track and manage customer orders and payments</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="loading"
              @click="refresh"
            >
              Refresh
            </UButton>
            <UButton 
              color="blue" 
              icon="i-lucide-download" 
              @click="exportOrders"
            >
              Export Orders
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">

      <!-- E-commerce Disabled Alert -->
      <div v-if="!tenant?.ecommerceEnabled" class="mb-8">
        <UAlert
          icon="i-lucide-store-x"
          color="amber"
          variant="soft"
          title="E-commerce Module Disabled"
          description="Enable the e-commerce module for this tenant to manage orders."
        />
      </div>

      <template v-else>
        <!-- Quick Stats -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
          <div class="bg-white dark:bg-slate-800 rounded-xl p-6 shadow-sm border border-blue-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
                <UIcon name="i-lucide-shopping-bag" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Total Orders</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ totalElements || 0 }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">All customer orders</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-xl p-6 shadow-sm border border-green-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
                <UIcon name="i-lucide-check-circle" class="h-5 w-5 text-green-600 dark:text-green-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Completed</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ completedOrders }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">Successfully processed</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-xl p-6 shadow-sm border border-amber-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-900/20">
                <UIcon name="i-lucide-clock" class="h-5 w-5 text-amber-600 dark:text-amber-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Pending</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ pendingOrders }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">Awaiting payment</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-xl p-6 shadow-sm border border-indigo-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-50 dark:bg-indigo-900/20">
                <UIcon name="i-lucide-dollar-sign" class="h-5 w-5 text-indigo-600 dark:text-indigo-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Total Revenue</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ totalRevenue }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">From completed orders</p>
          </div>
        </div>

        <!-- Search and Filters -->
        <div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-blue-200/60 dark:border-slate-700/60 overflow-hidden mb-6">
          <div class="bg-gradient-to-r from-blue-600 to-blue-800 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-filter" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">Order Search & Filters</h2>
                <p class="text-sm text-blue-100">Find orders by customer details or order status</p>
              </div>
            </div>
          </div>
          <div class="p-6">
            <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <UFormGroup label="Search Orders">
                <UInput
                  v-model="searchQuery"
                  size="lg"
                  placeholder="Customer name or email..."
                  icon="i-lucide-search"
                  @keyup.enter="fetchOrders"
                />
              </UFormGroup>
              <UFormGroup label="Status">
                <USelect
                  v-model="filters.status"
                  size="lg"
                  :options="statusFilterOptions"
                  value-attribute="value"
                  label-attribute="label"
                />
              </UFormGroup>
              <UFormGroup label="Payment Method">
                <USelect
                  v-model="filters.paymentMethod"
                  size="lg"
                  :options="paymentMethodOptions"
                  value-attribute="value"
                  label-attribute="label"
                />
              </UFormGroup>
              <UFormGroup label="Date Range">
                <USelect
                  v-model="filters.dateRange"
                  size="lg"
                  :options="dateRangeOptions"
                  value-attribute="value"
                  label-attribute="label"
                />
              </UFormGroup>
            </div>
            <div class="flex justify-end gap-2 mt-4">
              <UButton
                variant="outline"
                icon="i-lucide-x"
                @click="clearFilters"
              >
                Clear Filters
              </UButton>
              <UButton
                color="blue"
                icon="i-lucide-search"
                :loading="loading"
                @click="fetchOrders"
              >
                Search Orders
              </UButton>
            </div>
          </div>
        </div>

        <!-- Orders Table -->
        <div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-blue-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-blue-600 to-blue-800 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-shopping-bag" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">Customer Orders</h2>
                <p class="text-sm text-blue-100">{{ filteredOrders.length }} order{{ filteredOrders.length !== 1 ? 's' : '' }} found</p>
              </div>
            </div>
          </div>

          <div v-if="errorMessage" class="p-12 text-center">
            <div class="flex flex-col items-center gap-4">
              <div class="h-16 w-16 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
                <UIcon name="i-lucide-alert-triangle" class="h-8 w-8 text-red-600 dark:text-red-400" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-slate-900 dark:text-white">Failed to load orders</h3>
                <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">{{ errorMessage }}</p>
              </div>
              <UButton
                color="red"
                icon="i-lucide-refresh-cw"
                @click="fetchOrders"
              >
                Try Again
              </UButton>
            </div>
          </div>

          <div v-else-if="filteredOrders.length > 0">
            <UTable
              :rows="tableRows"
              :columns="columns"
              :loading="loading"
              class="min-w-full"
              :ui="{
                td: { padding: 'px-4 py-3' },
                th: { padding: 'px-4 py-3', color: 'text-slate-500 dark:text-slate-300' }
              }"
            >
              <template #status-data="{ row }">
                <UBadge :color="statusColor(row.rawStatus)" variant="soft">
                  {{ row.status }}
                </UBadge>
              </template>
              <template #totalAmount-data="{ row }">
                {{ formatCurrency(row.totalAmount, row.currency) }}
              </template>
              <template #createdAt-data="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
              <template #actions-data="{ row }">
                <div class="flex gap-2">
                  <UButton size="xs" variant="ghost" icon="i-lucide-eye" @click="viewOrder(row._original)">
                    View
                  </UButton>
                  <UButton size="xs" variant="ghost" color="gray" icon="i-lucide-download" @click="exportOrder(row._original)">
                    Export
                  </UButton>
                </div>
              </template>
            </UTable>

            <div class="border-t border-slate-200/60 dark:border-slate-700/60">
              <ProductPagination
                v-if="totalPages > 1"
                :current-page="currentPage"
                :total-pages="totalPages"
                :total-elements="totalElements"
                :page-size="pageSize"
                @update:page="handlePageChange"
                @update:page-size="handlePageSizeChange"
              />
            </div>
          </div>

          <div v-else class="p-12 text-center">
            <div class="flex flex-col items-center gap-4">
              <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                <UIcon name="i-lucide-shopping-bag" class="h-8 w-8 text-slate-400" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-slate-900 dark:text-white">No orders found</h3>
                <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                  {{ hasActiveFilters ? 'Try adjusting your search or filters' : 'Orders will appear here when customers make purchases.' }}
                </p>
              </div>
              <UButton
                v-if="hasActiveFilters"
                variant="outline"
                icon="i-lucide-x"
                @click="clearFilters"
              >
                Clear Filters
              </UButton>
            </div>
          </div>
        </div>

        <!-- Order detail modal -->
        <UModal v-model="showOrderModal" :ui="{ width: 'sm:max-w-3xl' }">
          <UCard>
            <template #header>
              <div class="flex items-center justify-between gap-3">
                <div>
                  <p class="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">Order</p>
                  <h3 class="text-lg font-semibold text-slate-900 dark:text-white">
                    {{ selectedOrder?.orderNumber || `Order #${selectedOrder?.id}` }}
                  </h3>
                  <p class="text-sm text-slate-500 dark:text-slate-300">
                    {{ selectedOrder?.customerName || selectedOrder?.customerEmail || 'Customer' }}
                  </p>
                </div>
                <UBadge :color="statusColor(selectedOrder?.status)" variant="soft">
                  {{ selectedOrder?.statusDisplayName || selectedOrder?.status || 'Unknown' }}
                </UBadge>
              </div>
            </template>

            <div class="grid gap-4 md:grid-cols-2">
              <div class="space-y-2">
                <p class="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">Status & Notes</p>
                <div class="space-y-3 rounded-lg border border-slate-200 dark:border-slate-700 p-3 bg-slate-50/50 dark:bg-slate-800/50">
                  <UFormGroup label="Status">
                    <USelect v-model="editForm.status" :options="statusOptions" />
                  </UFormGroup>
                  <UFormGroup label="Notes">
                    <UTextarea v-model="editForm.notes" :rows="3" placeholder="Internal notes" />
                  </UFormGroup>
                </div>
              </div>

              <div class="space-y-2">
                <p class="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">Customer</p>
                <div class="space-y-3 rounded-lg border border-slate-200 dark:border-slate-700 p-3 bg-slate-50/50 dark:bg-slate-800/50">
                  <UFormGroup label="Name">
                    <UInput v-model="editForm.customerName" />
                  </UFormGroup>
                  <UFormGroup label="Email">
                    <UInput v-model="editForm.customerEmail" />
                  </UFormGroup>
                  <UFormGroup label="Phone">
                    <UInput v-model="editForm.customerPhone" />
                  </UFormGroup>
                </div>
              </div>

              <div class="space-y-2 md:col-span-2">
                <p class="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">Totals</p>
                <div class="grid gap-4 md:grid-cols-3">
                  <UFormGroup label="Subtotal">
                    <UInput :model-value="formatCurrency(selectedOrder?.subtotal, selectedOrder?.currency)" disabled />
                  </UFormGroup>
                  <UFormGroup label="Tax">
                    <UInput :model-value="formatCurrency(selectedOrder?.taxAmount, selectedOrder?.currency)" disabled />
                  </UFormGroup>
                  <UFormGroup label="Total">
                    <UInput :model-value="formatCurrency(selectedOrder?.totalAmount, selectedOrder?.currency)" disabled />
                  </UFormGroup>
                </div>
              </div>

              <div class="space-y-2 md:col-span-2">
                <p class="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">Billing Address</p>
                <div class="rounded-lg border border-slate-200 dark:border-slate-700 p-3 bg-slate-50/50 dark:bg-slate-800/50 text-sm">
                  {{ selectedOrder?.fullBillingAddress || 'Not provided' }}
                </div>
              </div>

              <div class="space-y-2 md:col-span-2" v-if="selectedOrder?.items?.length">
                <div class="flex items-center justify-between">
                  <p class="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">Items</p>
                  <p class="text-xs text-slate-500 dark:text-slate-400">
                    {{ selectedOrder?.items?.length }} item{{ selectedOrder?.items?.length === 1 ? '' : 's' }}
                  </p>
                </div>
                <div class="rounded-lg border border-slate-200 dark:border-slate-700 overflow-hidden">
                  <table class="min-w-full divide-y divide-slate-200 dark:divide-slate-700">
                    <thead class="bg-slate-50 dark:bg-slate-800/60">
                      <tr class="text-left text-xs font-semibold text-slate-500 dark:text-slate-300 uppercase tracking-wide">
                        <th class="px-4 py-3">Product</th>
                        <th class="px-4 py-3 text-center">Qty</th>
                        <th class="px-4 py-3 text-right">Unit</th>
                        <th class="px-4 py-3 text-right">Total</th>
                      </tr>
                    </thead>
                    <tbody class="divide-y divide-slate-200 dark:divide-slate-800">
                      <tr v-for="item in selectedOrder?.items" :key="item.id" class="text-sm">
                        <td class="px-4 py-3">
                          <div class="flex flex-col">
                            <span class="font-semibold text-slate-900 dark:text-white">{{ item.productName || 'Product' }}</span>
                            <span v-if="item.variantName" class="text-xs text-slate-500 dark:text-slate-400">{{ item.variantName }}</span>
                            <span v-if="item.sku" class="text-xs text-slate-400 dark:text-slate-500">SKU: {{ item.sku }}</span>
                          </div>
                        </td>
                        <td class="px-4 py-3 text-center text-slate-700 dark:text-slate-200">{{ item.quantity ?? '—' }}</td>
                        <td class="px-4 py-3 text-right text-slate-700 dark:text-slate-200">
                          {{ formatCurrency(item.unitPrice, item.currency || selectedOrder?.currency) }}
                        </td>
                        <td class="px-4 py-3 text-right font-semibold text-slate-900 dark:text-white">
                          {{ formatCurrency(item.totalPrice, item.currency || selectedOrder?.currency) }}
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            <template #footer>
              <div class="flex justify-end gap-2">
                <UButton variant="ghost" color="gray" @click="showOrderModal = false">Close</UButton>
                <UButton color="blue" icon="i-lucide-save" :loading="savingOrder" @click="saveOrderEdits">
                  Save
                </UButton>
              </div>
            </template>
          </UCard>
        </UModal>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useEcommerceService } from "~/services/ecommerce.service";
import type { Order } from "~/types/ecommerce";
import ProductPagination from "~/components/ecommerce/ProductPagination.vue";

definePageMeta({
  title: 'Order Management',
  requiresAuth: true
});

const { tenant, fetchTenantContext } = useTenantContext();
const ecommerce = useEcommerceService();
const toast = useToast();

// State
const orders = ref<Order[]>([]);
const loading = ref(true);
const searchQuery = ref('');
const errorMessage = ref<string | null>(null);
const selectedOrder = ref<Order | null>(null);
const showOrderModal = ref(false);
const savingOrder = ref(false);
const editForm = reactive<Partial<Order>>({
  status: "",
  customerName: "",
  customerEmail: "",
  customerPhone: "",
  notes: "",
});

// Pagination state
const currentPage = ref(0);
const pageSize = ref(12);
const totalElements = ref(0);
const totalPages = ref(0);

// Filters
const filters = reactive({
  status: "",
  paymentMethod: "",
  dateRange: "",
});

// Filter Options
const statusFilterOptions = computed(() => [
  { label: "All Statuses", value: "" },
  { label: "Completed", value: "COMPLETED" },
  { label: "Paid", value: "PAID" },
  { label: "Pending Payment", value: "PENDING_PAYMENT" },
  { label: "Cancelled", value: "CANCELLED" },
  { label: "Failed", value: "FAILED" },
]);

const paymentMethodOptions = computed(() => [
  { label: "All Payment Methods", value: "" },
  { label: "PayPal", value: "paypal" },
  { label: "Cash on Delivery", value: "cod" },
  { label: "Bank Transfer", value: "bank" },
  { label: "Online Payment", value: "online" },
]);

const dateRangeOptions = computed(() => [
  { label: "All Time", value: "" },
  { label: "Today", value: "today" },
  { label: "This Week", value: "week" },
  { label: "This Month", value: "month" },
  { label: "Last 3 Months", value: "quarter" },
]);

// Computed
const completedOrders = computed(() => {
  return orders.value?.filter(o => 
    ['COMPLETED', 'PAID', 'CAPTURED'].includes((o.status || '').toUpperCase())
  ).length || 0;
});

const pendingOrders = computed(() => {
  return orders.value?.filter(o => 
    ['PENDING_PAYMENT', 'CREATED', 'APPROVED'].includes((o.status || '').toUpperCase())
  ).length || 0;
});

const totalRevenue = computed(() => {
  const revenue = orders.value
    ?.filter(o => ['COMPLETED', 'PAID', 'CAPTURED'].includes((o.status || '').toUpperCase()))
    ?.reduce((sum, order) => sum + (order.totalAmount || 0), 0) || 0;
  
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(revenue);
});

const hasActiveFilters = computed(() => {
  return searchQuery.value !== "" || 
         filters.status !== "" || 
         filters.paymentMethod !== "" || 
         filters.dateRange !== "";
});

// For display purposes, we'll show the current page orders
const filteredOrders = computed(() => orders.value || []);

const columns = [
  { key: "orderNumber", label: "Order #" },
  { key: "customer", label: "Customer" },
  { key: "status", label: "Status" },
  { key: "totalAmount", label: "Total" },
  { key: "createdAt", label: "Created" },
  { key: "actions", label: "" },
];

const tableRows = computed(() =>
  filteredOrders.value.map(order => ({
    orderNumber: order.orderNumber || `#${order.id}`,
    customer: order.customerName || order.customerEmail || "Customer",
    status: order.statusDisplayName || order.status || "Unknown",
    rawStatus: order.status || "",
    totalAmount: order.totalAmount ?? 0,
    currency: order.currency || "USD",
    createdAt: order.createdAt,
    _original: order,
  }))
);

const formatCurrency = (amount?: number | null, currency?: string) => {
  if (amount === null || amount === undefined) return "—";
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: currency || "USD",
    minimumFractionDigits: 2,
  }).format(amount);
};

const formatDate = (date?: string | number | Date) => {
  if (!date) return "—";
  return new Date(date).toLocaleString();
};

const statusColor = (status?: string) => {
  const normalized = (status || "").toUpperCase();
  if (["COMPLETED", "PAID", "CAPTURED"].includes(normalized)) return "green";
  if (["PENDING_PAYMENT", "CREATED", "APPROVED"].includes(normalized)) return "amber";
  if (["FAILED", "CANCELLED"].includes(normalized)) return "red";
  return "gray";
};

const statusOptions = computed(() => [
  { label: "Pending Payment", value: "PENDING_PAYMENT" },
  { label: "Paid", value: "PAID" },
  { label: "Completed", value: "COMPLETED" },
  { label: "Captured", value: "CAPTURED" },
  { label: "Processing", value: "PROCESSING" },
  { label: "Shipped", value: "SHIPPED" },
  { label: "Delivered", value: "DELIVERED" },
  { label: "Cancelled", value: "CANCELLED" },
  { label: "Failed", value: "FAILED" },
]);

const clearFilters = () => {
  searchQuery.value = '';
  filters.status = "";
  filters.paymentMethod = "";
  filters.dateRange = "";
  currentPage.value = 0;
  fetchOrders();
};

// Methods
const fetchOrders = async () => {
  try {
    loading.value = true;
    errorMessage.value = null;
    
    if (!tenant.value?.id) {
      await fetchTenantContext();
    }
    
    if (!tenant.value?.id) {
      throw new Error('No tenant context available');
    }

    // Build API parameters
    const params: Record<string, any> = {
      page: currentPage.value,
      size: pageSize.value,
    };

    // Add search query
    if (searchQuery.value && searchQuery.value.trim().length >= 2) {
      params.search = searchQuery.value.trim();
    }

    // Add filters
    if (filters.status) {
      params.status = filters.status;
    }

    if (filters.paymentMethod) {
      params.paymentMethod = filters.paymentMethod;
    }

    if (filters.dateRange) {
      params.dateRange = filters.dateRange;
    }

    const response = await ecommerce.listOrders(tenant.value.id, params);
    
    // Update orders and pagination info
    if (Array.isArray(response)) {
      orders.value = response;
      totalElements.value = response.length;
      totalPages.value = 1;
    } else {
      orders.value = response?.content ?? [];
      totalElements.value = response?.totalElements ?? orders.value.length ?? 0;
      const size = response?.size ?? pageSize.value;
      const total = response?.totalElements ?? orders.value.length ?? 0;
      totalPages.value = response?.totalPages ?? (size ? Math.ceil(total / size) : 1);
    }
  } catch (error) {
    console.error('Failed to load orders:', error);
    orders.value = [];
    totalElements.value = 0;
    totalPages.value = 0;
    errorMessage.value = (error as any)?.data?.message || (error as any)?.message || "Failed to load orders";
    toast.add({
      title: "Failed to load orders",
      description: errorMessage.value,
      color: "red",
      icon: "i-lucide-alert-circle"
    });
  } finally {
    loading.value = false;
  }
};

// Pagination handlers
const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchOrders();
};

const handlePageSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 0;
  fetchOrders();
};

const refresh = () => {
  fetchOrders();
};

// Order actions
const viewOrder = async (order: Order) => {
  if (!tenant.value?.id) return;
  try {
    loading.value = true;
    const detailed = await ecommerce.getOrder(tenant.value.id, order.id);
    const hydrated = detailed || order;
    selectedOrder.value = hydrated;
    editForm.status = hydrated.status || "";
    editForm.customerName = hydrated.customerName || "";
    editForm.customerEmail = hydrated.customerEmail || "";
    editForm.customerPhone = hydrated.customerPhone || "";
    editForm.notes = hydrated.notes || "";
    showOrderModal.value = true;
  } catch (err: any) {
    toast.add({
      title: "Order details unavailable",
      description: err?.data?.message || err?.message || "Unable to load full order details",
      color: "red",
    });
  } finally {
    loading.value = false;
  }
};

const saveOrderEdits = async () => {
  if (!tenant.value?.id || !selectedOrder.value?.id) return;
  try {
    savingOrder.value = true;
    const payload: Partial<Order> = {
      status: editForm.status || undefined,
      customerName: editForm.customerName || undefined,
      customerEmail: editForm.customerEmail || undefined,
      customerPhone: editForm.customerPhone || undefined,
      notes: editForm.notes || undefined,
    };
    const updated = await ecommerce.updateOrder(tenant.value.id, selectedOrder.value.id, payload);
    selectedOrder.value = updated;
    const idx = orders.value.findIndex(o => o.id === updated.id);
    if (idx !== -1) {
      orders.value[idx] = { ...orders.value[idx], ...updated };
    }
    toast.add({
      title: "Order updated",
      description: `Order ${updated.orderNumber} saved.`,
      color: "green",
    });
  } catch (err: any) {
    toast.add({
      title: "Update failed",
      description: err?.data?.message || err?.message || "Unable to update order",
      color: "red",
    });
  } finally {
    savingOrder.value = false;
  }
};

const printOrder = (order: Order) => {
  toast.add({
    title: "Print Invoice",
    description: `Printing invoice for order ${order.orderNumber}`,
    color: "green",
  });
};

const exportOrder = (order: Order) => {
  toast.add({
    title: "Export Order",
    description: `Exporting data for order ${order.orderNumber}`,
    color: "blue",
  });
};

const refundOrder = (order: Order) => {
  toast.add({
    title: "Process Refund",
    description: `Processing refund for order ${order.orderNumber}`,
    color: "amber",
  });
};

const exportOrders = () => {
  toast.add({
    title: "Export Orders",
    description: "Exporting all orders to CSV",
    color: "green",
  });
};

// Watchers for filters
watch([searchQuery, () => filters.status, () => filters.paymentMethod, () => filters.dateRange], () => {
  currentPage.value = 0;
  fetchOrders();
}, { deep: true });

// Load data on mount
onMounted(() => {
  fetchOrders();
});
</script>
