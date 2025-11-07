<template>
  <button
    class="flex items-center gap-3 rounded-3xl border border-white/20 bg-white/10 px-4 py-2 text-left text-sm font-medium text-white transition hover:border-white/40 hover:bg-white/20"
    type="button"
  >
    <UIcon :name="icon" class="h-4 w-4 text-white/80" />
    <span>{{ label }}</span>
  </button>
</template>

<script setup lang="ts">
defineProps<{
  icon: string;
  label: string;
}>();
</script>
