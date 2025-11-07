<template>
  <div class="rounded-2xl border border-slate-200 bg-white px-4 py-4 shadow-sm">
    <div class="flex items-center justify-between">
      <div>
        <p class="text-sm font-semibold text-slate-900">{{ name }}</p>
        <p class="text-xs text-slate-500">{{ specialty }}</p>
      </div>
      <UBadge variant="soft" color="emerald">{{ status }}</UBadge>
    </div>
    <div class="mt-3 flex items-center gap-2 text-xs text-slate-500">
      <UIcon name="i-lucide-clock" class="h-4 w-4" />
      <span>Next slot {{ nextSlot }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  name: string;
  specialty: string;
  status: string;
  nextSlot: string;
}>();
</script>
