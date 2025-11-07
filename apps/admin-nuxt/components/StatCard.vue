<template>
  <div class="rounded-3xl border border-white/20 bg-white/15 px-4 py-5 text-white shadow-lg shadow-emerald-900/20 backdrop-blur">
    <p class="text-xs uppercase tracking-widest text-white/60">{{ label }}</p>
    <p class="mt-3 text-2xl font-semibold">{{ value }}</p>
    <p v-if="trend" class="text-xs text-emerald-100">{{ trend }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string;
  value: string | number;
  trend?: string;
}>();
</script>
