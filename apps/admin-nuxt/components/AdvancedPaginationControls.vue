<template>
  <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
    <!-- Page Info and Direct Navigation -->
    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:gap-4">
      <div class="text-sm text-slate-500 dark:text-slate-400">
        Showing {{ startItem }}-{{ endItem }} of {{ totalItems }} {{ itemLabel }}
      </div>
      
      <!-- Direct Page Input -->
      <div class="flex items-center gap-2 text-sm">
        <span class="text-slate-500 dark:text-slate-400">Go to page:</span>
        <input
          v-model="directPageInput"
          type="number"
          :min="1"
          :max="totalPages"
          :disabled="loading"
          class="w-16 px-2 py-1 text-sm border border-slate-300 dark:border-slate-600 rounded-md bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          @keyup.enter="handleDirectPageNavigation"
          @blur="handleDirectPageNavigation"
        />
        <span class="text-slate-500 dark:text-slate-400">of {{ totalPages }}</span>
      </div>
    </div>
    
    <!-- Navigation Controls -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
      <!-- Page Navigation -->
      <div class="flex items-center gap-1">
        <!-- First -->
        <UButton
          :disabled="!hasPrevious || loading"
          variant="soft"
          color="gray"
          size="sm"
          icon="i-lucide-chevrons-left"
          @click="$emit('page-change', 1)"
        >
          First
        </UButton>
        
        <!-- Previous -->
        <UButton
          :disabled="!hasPrevious || loading"
          variant="soft"
          color="gray"
          size="sm"
          icon="i-lucide-chevron-left"
          @click="$emit('page-change', currentPage - 1)"
        >
          Previous
        </UButton>
        
        <!-- Page Numbers with Ellipsis -->
        <div class="flex items-center gap-1">
          <template v-for="(page, index) in visiblePagesWithEllipsis" :key="index">
            <UButton
              v-if="typeof page === 'number'"
              :variant="page === currentPage ? 'solid' : 'ghost'"
              :color="page === currentPage ? 'blue' : 'gray'"
              size="sm"
              :disabled="loading"
              @click="$emit('page-change', page)"
            >
              {{ page }}
            </UButton>
            <span
              v-else
              class="px-2 py-1 text-sm text-slate-400 dark:text-slate-500"
            >
              ...
            </span>
          </template>
        </div>
        
        <!-- Next -->
        <UButton
          :disabled="!hasNext || loading"
          variant="soft"
          color="gray"
          size="sm"
          @click="$emit('page-change', currentPage + 1)"
        >
          Next
          <UIcon name="i-lucide-chevron-right" class="ml-1 h-4 w-4" />
        </UButton>
        
        <!-- Last -->
        <UButton
          :disabled="!hasNext || loading"
          variant="soft"
          color="gray"
          size="sm"
          @click="$emit('page-change', totalPages)"
        >
          Last
          <UIcon name="i-lucide-chevrons-right" class="ml-1 h-4 w-4" />
        </UButton>
      </div>
      
      <!-- Page Size Selector -->
      <div class="flex items-center gap-2">
        <span class="text-xs text-slate-500 dark:text-slate-400 whitespace-nowrap">Show:</span>
        <USelect
          :model-value="pageSize"
          :options="pageSizeOptions"
          option-attribute="label"
          value-attribute="value"
          size="xs"
          :disabled="loading"
          @update:model-value="$emit('page-size-change', $event)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  currentPage: number;
  totalPages: number;
  totalItems: number;
  pageSize: number;
  hasPrevious?: boolean;
  hasNext?: boolean;
  loading?: boolean;
  itemLabel?: string;
  maxVisiblePages?: number;
  pageSizes?: number[];
}

interface Emits {
  (e: 'page-change', page: number): void;
  (e: 'page-size-change', size: number): void;
  (e: 'direct-page-input', page: number): void;
}

const props = withDefaults(defineProps<Props>(), {
  hasPrevious: false,
  hasNext: false,
  loading: false,
  itemLabel: 'items',
  maxVisiblePages: 7,
  pageSizes: () => [5, 10, 20, 50]
});

const emit = defineEmits<Emits>();

// Direct page input
const directPageInput = ref<number>(props.currentPage);

// Watch for current page changes to update direct input
watch(() => props.currentPage, (newPage) => {
  directPageInput.value = newPage;
});

// Computed properties
const startItem = computed(() => {
  if (props.totalItems === 0) return 0;
  return (props.currentPage - 1) * props.pageSize + 1;
});

const endItem = computed(() => {
  const end = props.currentPage * props.pageSize;
  return Math.min(end, props.totalItems);
});

const pageSizeOptions = computed(() => {
  return props.pageSizes.map(size => ({
    label: `${size} per page`,
    value: size
  }));
});

const visiblePagesWithEllipsis = computed(() => {
  const pages: (number | string)[] = [];
  const maxVisible = props.maxVisiblePages;
  const total = props.totalPages;
  const current = props.currentPage;
  
  if (total <= maxVisible) {
    // Show all pages if total is less than max visible
    for (let i = 1; i <= total; i++) {
      pages.push(i);
    }
  } else {
    // Always show first page
    pages.push(1);
    
    // Calculate the range around current page
    const sidePages = Math.floor((maxVisible - 3) / 2); // -3 for first, last, and current
    let start = Math.max(2, current - sidePages);
    let end = Math.min(total - 1, current + sidePages);
    
    // Adjust range if we're near the beginning or end
    if (current <= sidePages + 2) {
      end = Math.min(total - 1, maxVisible - 2);
    }
    if (current >= total - sidePages - 1) {
      start = Math.max(2, total - maxVisible + 2);
    }
    
    // Add ellipsis before start if needed
    if (start > 2) {
      pages.push('...');
    }
    
    // Add pages in range
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    
    // Add ellipsis after end if needed
    if (end < total - 1) {
      pages.push('...');
    }
    
    // Always show last page (if not already included)
    if (total > 1) {
      pages.push(total);
    }
  }
  
  return pages;
});

// Methods
const handleDirectPageNavigation = () => {
  const page = Number(directPageInput.value);
  if (page >= 1 && page <= props.totalPages && page !== props.currentPage) {
    emit('direct-page-input', page);
    emit('page-change', page);
  } else {
    // Reset to current page if invalid
    directPageInput.value = props.currentPage;
  }
};
</script>