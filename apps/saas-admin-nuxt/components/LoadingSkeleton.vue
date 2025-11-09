<template>
  <div :class="['animate-pulse', containerClass]">
    <!-- Card Skeleton -->
    <div v-if="type === 'card'" class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div class="flex items-center gap-4 mb-4">
        <div class="w-12 h-12 bg-gray-200 dark:bg-gray-700 rounded-full" />
        <div class="flex-1">
          <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mb-2" />
          <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded w-1/2" />
        </div>
      </div>
      <div class="space-y-2">
        <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded" />
        <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded w-5/6" />
      </div>
    </div>

    <!-- Table Skeleton -->
    <div v-else-if="type === 'table'" class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
      <div class="p-4 border-b border-gray-200 dark:border-gray-700">
        <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/4" />
      </div>
      <div v-for="i in rows" :key="i" class="p-4 border-b border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-4">
          <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded flex-1" />
          <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded w-24" />
          <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded w-20" />
        </div>
      </div>
    </div>

    <!-- Metric Card Skeleton -->
    <div v-else-if="type === 'metric'" class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div class="flex items-center justify-between mb-4">
        <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/2" />
        <div class="w-8 h-8 bg-gray-200 dark:bg-gray-700 rounded" />
      </div>
      <div class="h-8 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mb-2" />
      <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded w-1/3" />
    </div>

    <!-- Form Skeleton -->
    <div v-else-if="type === 'form'" class="space-y-4">
      <div v-for="i in rows" :key="i">
        <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/4 mb-2" />
        <div class="h-10 bg-gray-200 dark:bg-gray-700 rounded" />
      </div>
    </div>

    <!-- Chart Skeleton -->
    <div v-else-if="type === 'chart'" class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/3 mb-6" />
      <div class="h-64 bg-gray-200 dark:bg-gray-700 rounded" />
    </div>

    <!-- Text Skeleton -->
    <div v-else-if="type === 'text'" class="space-y-2">
      <div v-for="i in rows" :key="i" class="h-4 bg-gray-200 dark:bg-gray-700 rounded" :style="{ width: `${Math.random() * 30 + 70}%` }" />
    </div>

    <!-- Custom Skeleton -->
    <div v-else>
      <div class="h-20 bg-gray-200 dark:bg-gray-700 rounded" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  type?: 'card' | 'table' | 'metric' | 'form' | 'chart' | 'text' | 'custom'
  rows?: number
  containerClass?: string
}

withDefaults(defineProps<Props>(), {
  type: 'card',
  rows: 3,
  containerClass: ''
})
</script>
