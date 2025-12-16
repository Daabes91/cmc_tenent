<template>
  <div class="flex items-center justify-between px-6 py-4 border-t border-slate-200 dark:border-slate-700">
    <!-- Results Info -->
    <div class="flex items-center gap-4">
      <p class="text-sm text-slate-600 dark:text-slate-400">
        Showing {{ startItem }} to {{ endItem }} of {{ totalElements }} products
      </p>
      
      <!-- Page Size Selector -->
      <div class="flex items-center gap-2">
        <span class="text-sm text-slate-600 dark:text-slate-400">Show:</span>
        <USelect
          :model-value="pageSize"
          @update:model-value="$emit('update:pageSize', $event)"
          :options="pageSizeOptions"
          size="sm"
          class="w-20"
        />
      </div>
    </div>

    <!-- Pagination Controls -->
    <div class="flex items-center gap-2">
      <!-- Previous Button -->
      <UButton
        variant="ghost"
        color="gray"
        icon="i-lucide-chevron-left"
        size="sm"
        :disabled="isFirstPage"
        @click="$emit('update:page', currentPage - 1)"
      >
        Previous
      </UButton>

      <!-- Page Numbers -->
      <div class="flex items-center gap-1">
        <!-- First page -->
        <UButton
          v-if="showFirstPage"
          variant="ghost"
          :color="currentPage === 0 ? 'blue' : 'gray'"
          size="sm"
          @click="$emit('update:page', 0)"
        >
          1
        </UButton>

        <!-- First ellipsis -->
        <span v-if="showFirstEllipsis" class="px-2 text-slate-400">...</span>

        <!-- Visible page numbers -->
        <UButton
          v-for="page in visiblePages"
          :key="page"
          variant="ghost"
          :color="currentPage === page ? 'blue' : 'gray'"
          size="sm"
          @click="$emit('update:page', page)"
        >
          {{ page + 1 }}
        </UButton>

        <!-- Last ellipsis -->
        <span v-if="showLastEllipsis" class="px-2 text-slate-400">...</span>

        <!-- Last page -->
        <UButton
          v-if="showLastPage"
          variant="ghost"
          :color="currentPage === totalPages - 1 ? 'blue' : 'gray'"
          size="sm"
          @click="$emit('update:page', totalPages - 1)"
        >
          {{ totalPages }}
        </UButton>
      </div>

      <!-- Next Button -->
      <UButton
        variant="ghost"
        color="gray"
        icon="i-lucide-chevron-right"
        size="sm"
        :disabled="isLastPage"
        @click="$emit('update:page', currentPage + 1)"
      >
        Next
      </UButton>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
}

interface Emits {
  (e: 'update:page', page: number): void;
  (e: 'update:pageSize', size: number): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const pageSizeOptions = [6, 12, 24, 48];

const isFirstPage = computed(() => props.currentPage === 0);
const isLastPage = computed(() => props.currentPage === props.totalPages - 1);

const startItem = computed(() => {
  if (props.totalElements === 0) return 0;
  return props.currentPage * props.pageSize + 1;
});

const endItem = computed(() => {
  const end = (props.currentPage + 1) * props.pageSize;
  return Math.min(end, props.totalElements);
});

// Pagination logic for showing page numbers
const visiblePages = computed(() => {
  const delta = 2; // Number of pages to show on each side of current page
  const range = [];
  const rangeWithDots = [];

  for (let i = Math.max(0, props.currentPage - delta); 
       i <= Math.min(props.totalPages - 1, props.currentPage + delta); 
       i++) {
    range.push(i);
  }

  return range;
});

const showFirstPage = computed(() => {
  return props.totalPages > 1 && !visiblePages.value.includes(0);
});

const showLastPage = computed(() => {
  return props.totalPages > 1 && !visiblePages.value.includes(props.totalPages - 1);
});

const showFirstEllipsis = computed(() => {
  return showFirstPage.value && visiblePages.value[0] > 1;
});

const showLastEllipsis = computed(() => {
  return showLastPage.value && visiblePages.value[visiblePages.value.length - 1] < props.totalPages - 2;
});
</script>