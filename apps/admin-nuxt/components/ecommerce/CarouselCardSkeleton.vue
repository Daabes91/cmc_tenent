<template>
  <UCard 
    class="border-slate-200 dark:border-slate-700"
    :ui="{ 
      body: { padding: 'p-0' },
      header: { padding: 'px-6 py-4' },
      footer: { padding: 'px-6 py-4' }
    }"
  >
    <template #header>
      <div class="flex items-start justify-between">
        <div class="flex-1 min-w-0 space-y-2">
          <div class="flex items-center gap-2">
            <USkeleton class="h-5 w-5 rounded" />
            <USkeleton class="h-5 w-32" />
          </div>
          <div class="flex items-center gap-2">
            <USkeleton class="h-4 w-12 rounded-full" />
            <USkeleton class="h-4 w-20 rounded-full" />
          </div>
        </div>
        <USkeleton class="h-8 w-8 rounded" />
      </div>
    </template>

    <div class="px-6 py-4 bg-slate-50 dark:bg-slate-800/50 space-y-3">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <USkeleton class="h-4 w-4 rounded" />
          <USkeleton class="h-4 w-16" />
          <USkeleton class="h-4 w-24 rounded" />
        </div>
      </div>
      
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2">
            <USkeleton class="h-4 w-4 rounded" />
            <USkeleton class="h-4 w-12" />
          </div>
          <div class="flex items-center gap-2">
            <USkeleton class="h-4 w-4 rounded" />
            <USkeleton class="h-4 w-16" />
          </div>
        </div>
        <USkeleton class="h-3 w-8" />
      </div>
      
      <div class="flex items-center gap-2">
        <USkeleton class="h-4 w-4 rounded" />
        <USkeleton class="h-4 w-28" />
      </div>
    </div>

    <template #footer>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <USkeleton class="h-6 w-12 rounded" />
          <USkeleton class="h-6 w-16 rounded" />
        </div>
        <USkeleton class="h-5 w-10 rounded-full" />
      </div>
    </template>
  </UCard>
</template>

<script setup lang="ts">
// No props needed for skeleton
</script>