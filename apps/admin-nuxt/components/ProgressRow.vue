<template>
  <div class="space-y-2">
    <div class="flex items-center justify-between text-xs text-slate-500">
      <span>{{ label }}</span>
      <span>{{ value }}/{{ target }}</span>
    </div>
    <div class="h-2 w-full overflow-hidden rounded-full bg-slate-200">
      <div
        class="h-full rounded-full bg-gradient-to-r from-emerald-500 to-emerald-600 transition-all"
        :style="{ width: `${percentage}%` }"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  label: string;
  value: number;
  target: number;
}>();

const percentage = computed(() => {
  if (!props.target) return 0;
  return Math.min(100, Math.round((props.value / props.target) * 100));
});
</script>
