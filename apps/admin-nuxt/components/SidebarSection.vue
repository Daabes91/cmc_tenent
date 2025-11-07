<template>
  <section class="space-y-2">
    <p class="px-2 text-xs uppercase tracking-widest text-white/40">
      {{ title }}
    </p>
    <div class="space-y-1.5">
      <NuxtLink
        v-for="item in items"
        :key="item.to"
        :to="item.to"
        class="flex items-center gap-3 rounded-2xl px-3 py-3 text-sm font-medium transition"
        :class="linkClass(item.to)"
      >
        <UIcon :name="item.icon" class="h-4 w-4" />
        <span>{{ item.label }}</span>
      </NuxtLink>
    </div>
  </section>
</template>

<script setup lang="ts">
const props = defineProps<{
  title: string;
  items: { label: string; icon: string; to: string }[];
}>();

const route = useRoute();

const linkClass = (to: string) => {
  const isActive = route.path === to || route.path.startsWith(`${to}/`);
  return isActive
    ? "bg-white text-emerald-700 shadow-lg shadow-emerald-900/20"
    : "text-white/70 hover:bg-white/10 hover:text-white";
};
</script>
