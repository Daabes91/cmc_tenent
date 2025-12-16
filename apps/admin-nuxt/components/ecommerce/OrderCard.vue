<template>
  <div class="bg-gradient-to-br from-white to-blue-50/30 dark:bg-slate-700/50 rounded-xl p-6 border border-blue-200/60 dark:border-slate-600 hover:shadow-lg hover:border-blue-400 dark:hover:border-blue-500 transition-all duration-200 group">
    <!-- Order Header -->
    <div class="flex items-start justify-between mb-4">
      <div class="flex items-center gap-3">
        <div class="relative">
          <div class="h-12 w-12 rounded-xl bg-gradient-to-br from-blue-100 to-blue-200 dark:from-blue-900/30 dark:to-blue-800/30 flex items-center justify-center shadow-sm">
            <UIcon name="i-lucide-shopping-bag" class="h-6 w-6 text-blue-700 dark:text-blue-400" />
          </div>
          <div
            :class="[
              'absolute -top-1 -right-1 w-4 h-4 rounded-full border-2 border-white dark:border-slate-700',
              getStatusColor(order.status)
            ]"
          ></div>
        </div>
        <div class="flex-1 min-w-0">
          <h3 class="font-semibold text-slate-900 dark:text-white truncate">
            {{ order.orderNumber }}
          </h3>
          <p class="text-sm text-slate-500 dark:text-slate-400 truncate">
            Order #{{ order.id }}
          </p>
        </div>
      </div>
      <UBadge
        :color="getBadgeColor(order.status)"
        variant="soft"
        size="sm"
      >
        {{ order.statusDisplayName || order.status || 'Unknown' }}
      </UBadge>
    </div>

    <!-- Customer Info -->
    <div class="space-y-3 mb-4">
      <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
        <UIcon name="i-lucide-user" class="h-4 w-4 text-slate-400" />
        <span class="font-medium">{{ order.customerName || 'Unknown Customer' }}</span>
      </div>
      <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
        <UIcon name="i-lucide-mail" class="h-4 w-4 text-slate-400" />
        <span class="truncate">{{ order.customerEmail || 'No email' }}</span>
      </div>
      <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
        <UIcon name="i-lucide-phone" class="h-4 w-4 text-slate-400" />
        <span>{{ order.customerPhone || 'No phone' }}</span>
      </div>
    </div>

    <!-- Order Details -->
    <div class="space-y-3 mb-4">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
          <UIcon name="i-lucide-dollar-sign" class="h-4 w-4 text-slate-400" />
          <span class="font-semibold text-lg text-slate-900 dark:text-white">
            {{ formatMoney(order.totalAmount, order.currency) }}
          </span>
        </div>
        <div class="text-xs text-slate-500 dark:text-slate-400">
          {{ order.totalItemCount || 0 }} items
        </div>
      </div>
      
      <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
        <UIcon name="i-lucide-credit-card" class="h-4 w-4 text-slate-400" />
        <span>{{ getPaymentMethod(order.notes) }}</span>
      </div>
      
      <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
        <UIcon name="i-lucide-calendar" class="h-4 w-4 text-slate-400" />
        <span>{{ formatDate(order.createdAt) }}</span>
      </div>
    </div>

    <!-- Billing Address -->
    <div v-if="order.fullBillingAddress" class="mb-4">
      <div class="flex items-start gap-2 text-sm text-slate-600 dark:text-slate-300">
        <UIcon name="i-lucide-map-pin" class="h-4 w-4 text-slate-400 mt-0.5" />
        <div class="flex-1">
          <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide mb-1">Billing Address</p>
          <p class="text-sm leading-relaxed whitespace-pre-line">{{ order.fullBillingAddress }}</p>
        </div>
      </div>
    </div>

    <!-- Order Breakdown -->
    <div class="space-y-2 mb-4 p-3 bg-slate-50 dark:bg-slate-800/50 rounded-lg">
      <div class="flex justify-between text-sm">
        <span class="text-slate-600 dark:text-slate-400">Subtotal:</span>
        <span class="font-medium">{{ formatMoney(order.subtotal, order.currency) }}</span>
      </div>
      <div v-if="order.taxAmount" class="flex justify-between text-sm">
        <span class="text-slate-600 dark:text-slate-400">Tax:</span>
        <span class="font-medium">{{ formatMoney(order.taxAmount, order.currency) }}</span>
      </div>
      <div v-if="order.shippingAmount" class="flex justify-between text-sm">
        <span class="text-slate-600 dark:text-slate-400">Shipping:</span>
        <span class="font-medium">{{ formatMoney(order.shippingAmount, order.currency) }}</span>
      </div>
      <div class="flex justify-between text-sm font-semibold pt-2 border-t border-slate-200 dark:border-slate-600">
        <span>Total:</span>
        <span>{{ formatMoney(order.totalAmount, order.currency) }}</span>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-slate-600">
      <div class="text-xs text-slate-500 dark:text-slate-400">
        Updated {{ formatDate(order.updatedAt) }}
      </div>
      <div class="flex items-center gap-2">
        <UButton
          size="sm"
          variant="ghost"
          color="blue"
          icon="i-lucide-eye"
          @click="$emit('view', order)"
        >
          View
        </UButton>
        <UDropdown :items="getOrderActions(order)" :popper="{ placement: 'bottom-end' }">
          <UButton
            size="sm"
            variant="ghost"
            color="gray"
            icon="i-lucide-more-horizontal"
          />
        </UDropdown>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Order } from "~/types/ecommerce";

interface Props {
  order: Order;
}

interface Emits {
  (e: 'view', order: Order): void;
  (e: 'print', order: Order): void;
  (e: 'export', order: Order): void;
  (e: 'refund', order: Order): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const getStatusColor = (status?: string) => {
  switch ((status || "").toUpperCase()) {
    case "PAID":
    case "CAPTURED":
    case "COMPLETED":
      return "bg-green-500";
    case "PENDING_PAYMENT":
    case "CREATED":
    case "APPROVED":
      return "bg-amber-500";
    case "CANCELLED":
    case "FAILED":
      return "bg-red-500";
    default:
      return "bg-gray-500";
  }
};

const getBadgeColor = (status?: string) => {
  switch ((status || "").toUpperCase()) {
    case "PAID":
    case "CAPTURED":
    case "COMPLETED":
      return "green";
    case "PENDING_PAYMENT":
    case "CREATED":
    case "APPROVED":
      return "amber";
    case "CANCELLED":
    case "FAILED":
      return "red";
    default:
      return "gray";
  }
};

const formatMoney = (amount?: number, currency?: string) => {
  if (amount == null) return "—";
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency || 'USD',
    minimumFractionDigits: 2,
  }).format(amount);
};

const formatDate = (date?: string) => {
  if (!date) return "—";
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const getPaymentMethod = (notes?: string) => {
  if (!notes) return "Unknown";
  if (notes.toUpperCase().includes("COD")) return "Cash on Delivery";
  if (notes.toUpperCase().includes("BANK")) return "Bank Transfer";
  if (notes.toUpperCase().includes("ONLINE")) return "Online Payment";
  if (notes.toUpperCase().includes("PAYPAL")) return "PayPal";
  return "Other";
};

const getOrderActions = (order: Order) => {
  return [
    [
      {
        label: "View Details",
        icon: "i-lucide-eye",
        click: () => emit('view', order),
      },
      {
        label: "Print Invoice",
        icon: "i-lucide-printer",
        click: () => emit('print', order),
      },
    ],
    [
      {
        label: "Export Data",
        icon: "i-lucide-download",
        click: () => emit('export', order),
      },
    ],
    [
      {
        label: "Process Refund",
        icon: "i-lucide-undo-2",
        click: () => emit('refund', order),
        class: "text-red-600 dark:text-red-400",
      },
    ],
  ];
};
</script>