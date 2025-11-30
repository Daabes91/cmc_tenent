<template>
  <div class="group relative overflow-hidden rounded-xl border border-slate-200/80 bg-white p-6 shadow-sm transition-all duration-300 hover:shadow-md hover:border-violet-200">
    <!-- Decorative gradient background -->
    <div class="absolute right-0 top-0 h-24 w-24 rounded-full bg-gradient-to-br from-mint-100 to-transparent opacity-30 blur-2xl transition-opacity group-hover:opacity-50"></div>
    
    <div class="relative">
      <div class="flex items-start justify-between">
        <p class="text-xs font-semibold uppercase tracking-wider text-slate-500">{{ title }}</p>
        <UIcon v-if="icon" :name="icon" class="h-5 w-5 text-violet-500" />
      </div>
      
      <p class="mt-4 text-3xl font-bold text-slate-900 transition-colors group-hover:text-violet-700">
        {{ metric }}
      </p>
      
      <div class="mt-3 flex items-center gap-2">
        <UBadge 
          v-if="change" 
          :color="changeColor" 
          variant="soft" 
          size="sm"
          class="font-medium"
        >
          {{ change }}
        </UBadge>
        <p class="text-xs text-slate-600">{{ description }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  title: string;
  metric: string;
  change?: string;
  description: string;
  icon?: string;
}>();

const changeColor = computed(() => {
  if (!props.change) return 'gray';
  if (props.change.startsWith('+')) return 'emerald';
  if (props.change.startsWith('-')) return 'red';
  return 'violet';
});
</script>
