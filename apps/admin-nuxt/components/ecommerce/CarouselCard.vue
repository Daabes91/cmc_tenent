<template>
  <UCard 
    class="group hover:shadow-lg transition-all duration-200 border-slate-200 dark:border-slate-700"
    :ui="{ 
      body: { padding: 'p-0' },
      header: { padding: 'px-6 py-4' },
      footer: { padding: 'px-6 py-4' }
    }"
  >
    <template #header>
      <div class="flex items-start justify-between">
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-2">
            <UIcon :name="getTypeIcon(carousel.type)" class="w-5 h-5 text-slate-600 dark:text-slate-400" />
            <h3 class="font-semibold text-slate-900 dark:text-white truncate">
              {{ carousel.name }}
            </h3>
          </div>
          <div class="flex items-center gap-2">
            <UBadge 
              :color="carousel.isActive ? 'green' : 'gray'" 
              variant="soft" 
              size="xs"
            >
              {{ carousel.isActive ? 'Active' : 'Inactive' }}
            </UBadge>
            <UBadge color="blue" variant="soft" size="xs">
              {{ formatTypeLabel(carousel.type) }}
            </UBadge>
          </div>
        </div>
        
        <UDropdown :items="dropdownItems" :popper="{ placement: 'bottom-end' }">
          <UButton
            color="gray"
            variant="ghost"
            icon="i-lucide-more-horizontal"
            size="sm"
            class="opacity-0 group-hover:opacity-100 transition-opacity"
          />
        </UDropdown>
      </div>
    </template>

    <!-- Carousel Preview -->
    <div class="px-6 py-4 bg-slate-50 dark:bg-slate-800/50">
      <div class="space-y-3">
        <!-- Placement Info -->
        <div class="flex items-center justify-between text-sm">
          <div class="flex items-center gap-2">
            <UIcon name="i-lucide-map-pin" class="w-4 h-4 text-slate-500" />
            <span class="text-slate-600 dark:text-slate-400">Placement:</span>
            <code class="px-2 py-1 bg-slate-200 dark:bg-slate-700 rounded text-xs font-mono">
              {{ carousel.placement }}
            </code>
          </div>
        </div>

        <!-- Platform & Items Info -->
        <div class="flex items-center justify-between text-sm">
          <div class="flex items-center gap-4">
            <div class="flex items-center gap-2">
              <UIcon :name="getPlatformIcon(carousel.platform)" class="w-4 h-4 text-slate-500" />
              <span class="text-slate-600 dark:text-slate-400">{{ carousel.platform || 'WEB' }}</span>
            </div>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-layers" class="w-4 h-4 text-slate-500" />
              <span class="text-slate-600 dark:text-slate-400">
                {{ carousel.itemsCount || 0 }} items
              </span>
            </div>
          </div>
          <div v-if="carousel.maxItems" class="text-xs text-slate-500">
            Max: {{ carousel.maxItems }}
          </div>
        </div>

        <!-- Slug -->
        <div class="flex items-center gap-2 text-sm">
          <UIcon name="i-lucide-link" class="w-4 h-4 text-slate-500" />
          <code class="text-slate-600 dark:text-slate-400 font-mono text-xs">
            {{ carousel.slug }}
          </code>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <UButton
            size="xs"
            variant="outline"
            icon="i-lucide-pencil"
            @click="$emit('edit', carousel)"
          >
            Edit
          </UButton>
          <UButton
            size="xs"
            variant="ghost"
            icon="i-lucide-copy"
            @click="$emit('duplicate', carousel)"
          >
            Duplicate
          </UButton>
        </div>
        
        <UToggle
          :model-value="carousel.isActive"
          @update:model-value="$emit('toggle-status', carousel)"
          size="sm"
        />
      </div>
    </template>
  </UCard>
</template>

<script setup lang="ts">
import type { Carousel } from "~/types/ecommerce";

interface Props {
  carousel: Carousel;
}

interface Emits {
  (e: 'edit', carousel: Carousel): void;
  (e: 'duplicate', carousel: Carousel): void;
  (e: 'toggle-status', carousel: Carousel): void;
  (e: 'delete', carousel: Carousel): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const typeIcons = {
  PRODUCT: "i-lucide-package",
  VIEW_ALL_PRODUCTS: "i-lucide-grid-3x3",
  IMAGE: "i-lucide-image",
  CATEGORY: "i-lucide-folder",
  BRAND: "i-lucide-award",
  OFFER: "i-lucide-percent",
  TESTIMONIAL: "i-lucide-quote",
  BLOG: "i-lucide-file-text",
  MIXED: "i-lucide-layers",
};

const platformIcons = {
  WEB: "i-lucide-monitor",
  MOBILE: "i-lucide-smartphone",
  BOTH: "i-lucide-tablet-smartphone",
};

const typeLabels = {
  PRODUCT: "Product Showcase",
  VIEW_ALL_PRODUCTS: "All Products",
  IMAGE: "Image Gallery",
  CATEGORY: "Category Display",
  BRAND: "Brand Showcase",
  OFFER: "Special Offers",
  TESTIMONIAL: "Testimonials",
  BLOG: "Blog Posts",
  MIXED: "Mixed Content",
};

const getTypeIcon = (type: string) => {
  return typeIcons[type as keyof typeof typeIcons] || "i-lucide-circle";
};

const getPlatformIcon = (platform?: string) => {
  return platformIcons[platform as keyof typeof platformIcons] || "i-lucide-monitor";
};

const formatTypeLabel = (type: string) => {
  return typeLabels[type as keyof typeof typeLabels] || type;
};

const dropdownItems = computed(() => [
  [
    {
      label: "Edit",
      icon: "i-lucide-pencil",
      click: () => emit('edit', props.carousel),
    },
    {
      label: "Duplicate",
      icon: "i-lucide-copy",
      click: () => emit('duplicate', props.carousel),
    },
  ],
  [
    {
      label: props.carousel.isActive ? "Deactivate" : "Activate",
      icon: props.carousel.isActive ? "i-lucide-pause" : "i-lucide-play",
      click: () => emit('toggle-status', props.carousel),
    },
  ],
  [
    {
      label: "Delete",
      icon: "i-lucide-trash-2",
      click: () => emit('delete', props.carousel),
      class: "text-red-600 dark:text-red-400",
    },
  ],
]);
</script>