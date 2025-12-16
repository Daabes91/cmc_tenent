<template>
  <div class="bg-gradient-to-br from-white to-blue-50/30 dark:bg-slate-700/50 rounded-xl p-6 border border-blue-200/60 dark:border-slate-600 hover:shadow-lg hover:border-blue-400 dark:hover:border-blue-500 transition-all duration-200 group">
    <!-- Product Header -->
    <div class="flex items-start justify-between mb-4">
      <div class="flex items-center gap-3">
        <div class="relative">
          <div class="h-12 w-12 rounded-xl bg-gradient-to-br from-blue-100 to-blue-200 dark:from-blue-900/30 dark:to-blue-800/30 flex items-center justify-center shadow-sm overflow-hidden">
            <img
              v-if="product.images?.length"
              :src="product.images[0]?.imageUrl"
              class="h-full w-full object-cover"
              :alt="product.images[0]?.altText || product.name"
            />
            <UIcon v-else name="i-lucide-package" class="h-6 w-6 text-blue-700 dark:text-blue-400" />
          </div>
          <div
            :class="[
              'absolute -top-1 -right-1 w-4 h-4 rounded-full border-2 border-white dark:border-slate-700',
              getStatusColor(product.status)
            ]"
          ></div>
        </div>
        <div class="flex-1 min-w-0">
          <h3 class="font-semibold text-slate-900 dark:text-white truncate">
            {{ product.name }}
          </h3>
          <p class="text-sm text-slate-500 dark:text-slate-400 truncate">
            {{ product.sku || `ID: ${product.id}` }}
          </p>
        </div>
      </div>
      <UBadge
        :color="product.status === 'ACTIVE' ? 'green' : 'amber'"
        variant="soft"
        size="sm"
      >
        {{ product.status || 'DRAFT' }}
      </UBadge>
    </div>

    <!-- Product Details -->
    <div class="space-y-3 mb-4">
      <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
        <UIcon name="i-lucide-dollar-sign" class="h-4 w-4 text-slate-400" />
        <span class="font-medium">{{ formatPrice(product.price, product.currency) }}</span>
        <span v-if="product.compareAtPrice" class="text-slate-400 line-through text-xs">
          {{ formatPrice(product.compareAtPrice, product.currency) }}
        </span>
      </div>
      <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
        <UIcon name="i-lucide-eye" class="h-4 w-4 text-slate-400" />
        <span>{{ product.isVisible ? 'Visible' : 'Hidden' }}</span>
      </div>
      <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
        <UIcon name="i-lucide-images" class="h-4 w-4 text-slate-400" />
        <span>{{ product.images?.length || 0 }} images</span>
      </div>
      <div v-if="product.shortDescription" class="text-sm text-slate-600 dark:text-slate-300 line-clamp-2">
        {{ product.shortDescription }}
      </div>
    </div>



    <!-- Actions -->
    <div class="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-slate-600">
      <div class="text-xs text-slate-500 dark:text-slate-400">
        Updated {{ formatDate(product.updatedAt) }}
      </div>
      <div class="flex items-center gap-2">
        <UButton
          size="sm"
          variant="ghost"
          color="blue"
          icon="i-lucide-edit"
          :to="`/ecommerce/products/${product.id}/edit`"
        >
          Edit
        </UButton>
        <UDropdown :items="getProductActions(product)" :popper="{ placement: 'bottom-end' }">
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
import type { Product } from "~/types/ecommerce";

interface Props {
  product: Product;
}

interface Emits {
  (e: 'edit', product: Product): void;
  (e: 'duplicate', product: Product): void;
  (e: 'toggle-status', product: Product): void;
  (e: 'toggle-visibility', product: Product): void;
  (e: 'delete', product: Product): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const getStatusColor = (status?: string) => {
  switch (status) {
    case 'ACTIVE': return 'bg-green-500';
    case 'DRAFT': return 'bg-amber-500';
    default: return 'bg-gray-500';
  }
};

const formatPrice = (price?: number | null, currency?: string) => {
  if (price === null || price === undefined) return '—';
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency || 'USD',
    minimumFractionDigits: 2,
  }).format(price);
};

const formatDate = (date?: string) => {
  if (!date) return '—';
  return new Date(date).toLocaleDateString();
};



const getProductActions = (product: Product) => {
  return [
    [
      {
        label: "View Details",
        icon: "i-lucide-eye",
        click: () => emit('edit', product),
      },
      {
        label: "Duplicate",
        icon: "i-lucide-copy",
        click: () => emit('duplicate', product),
      },
    ],
    [
      {
        label: product.status === 'ACTIVE' ? "Set to Draft" : "Activate",
        icon: product.status === 'ACTIVE' ? "i-lucide-pause" : "i-lucide-play",
        click: () => emit('toggle-status', product),
      },
      {
        label: product.isVisible ? "Hide Product" : "Show Product",
        icon: product.isVisible ? "i-lucide-eye-off" : "i-lucide-eye",
        click: () => emit('toggle-visibility', product),
      },
    ],
    [
      {
        label: "Delete",
        icon: "i-lucide-trash-2",
        click: () => emit('delete', product),
        class: "text-red-600 dark:text-red-400",
      },
    ],
  ];
};
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-clamp: 2;
}
</style>
