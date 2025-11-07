<template>
  <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
    <!-- Page Info -->
    <div class="text-sm text-slate-500 dark:text-slate-400">
      Showing {{ startItem }}-{{ endItem }} of {{ totalItems }} {{ itemLabel }}
    </div>
    
    <!-- Navigation Controls -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
      <!-- Page Navigation -->
      <div class="flex items-center gap-1">
        <!-- First/Previous -->
        <UButton
          v-if="showFirstLast"
          :disabled="!hasPrevious || loading"
          variant="soft"
          color="gray"
          size="sm"
          icon="i-lucide-chevrons-left"
          @click="$emit('page-change', 1)"
        >
          <span class="hidden sm:inline">First</span>
        </UButton>
        
        <UButton
          :disabled="!hasPrevious || loading"
          variant="soft"
          color="gray"
          size="sm"
          icon="i-lucide-chevron-left"
          @click="$emit('page-change', currentPage - 1)"
        >
          <span class="hidden sm:inline">Previous</span>
        </UButton>
        
        <!-- Page Numbers -->
        <div class="flex items-center gap-1">
          <UButton
            v-for="page in visiblePages"
            :key="page"
            :variant="page === currentPage ? 'solid' : 'ghost'"
            :color="page === currentPage ? 'blue' : 'gray'"
            size="sm"
            :disabled="loading"
            @click="$emit('page-change', page)"
          >
            {{ page }}
          </UButton>
        </div>
        
        <!-- Next/Last -->
        <UButton
          :disabled="!hasNext || loading"
          variant="soft"
          color="gray"
          size="sm"
          @click="$emit('page-change', currentPage + 1)"
        >
          <span class="hidden sm:inline">Next</span>
          <UIcon name="i-lucide-chevron-right" class="h-4 w-4" />
        </UButton>
        
        <UButton
          v-if="showFirstLast"
          :disabled="!hasNext || loading"
          variant="soft"
          color="gray"
          size="sm"
          @click="$emit('page-change', totalPages)"
        >
          <span class="hidden sm:inline">Last</span>
          <UIcon name="i-lucide-chevrons-right" class="h-4 w-4" />
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
  showFirstLast?: boolean;
  maxVisiblePages?: number;
  pageSizes?: number[];
}

interface Emits {
  (e: 'page-change', page: number): void;
  (e: 'page-size-change', size: number): void;
}

const props = withDefaults(defineProps<Props>(), {
  hasPrevious: false,
  hasNext: false,
  loading: false,
  itemLabel: 'items',
  showFirstLast: true,
  maxVisiblePages: 5,
  pageSizes: () => [5, 10, 20, 50]
});

defineEmits<Emits>();

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

const visiblePages = computed(() => {
  const pages: number[] = [];
  const maxVisible = props.maxVisiblePages;
  const total = props.totalPages;
  const current = props.currentPage;
  
  if (total <= maxVisible) {
    // Show all pages if total is less than max visible
    for (let i = 1; i <= total; i++) {
      pages.push(i);
    }
  } else {
    // Calculate start and end pages
    let start = Math.max(1, current - Math.floor(maxVisible / 2));
    let end = Math.min(total, start + maxVisible - 1);
    
    // Adjust start if we're near the end
    if (end - start + 1 < maxVisible) {
      start = Math.max(1, end - maxVisible + 1);
    }
    
    // Add pages
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    
    // Add ellipsis logic (simplified for now)
    if (start > 1) {
      pages.unshift(-1); // -1 represents ellipsis
      pages.unshift(1);
    }
    
    if (end < total) {
      pages.push(-2); // -2 represents ellipsis
      pages.push(total);
    }
  }
  
  return pages.filter(page => page > 0); // Remove ellipsis for now, can be enhanced later
});
</script>