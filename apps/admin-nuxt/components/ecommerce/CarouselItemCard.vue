<template>
  <div class="flex items-center gap-4 p-4 border border-slate-200 dark:border-slate-700 rounded-lg hover:border-slate-300 dark:hover:border-slate-600 transition-colors">
    <!-- Drag Handle -->
    <div class="flex flex-col gap-1 cursor-grab active:cursor-grabbing">
      <div class="w-1 h-1 bg-slate-400 rounded-full"></div>
      <div class="w-1 h-1 bg-slate-400 rounded-full"></div>
      <div class="w-1 h-1 bg-slate-400 rounded-full"></div>
      <div class="w-1 h-1 bg-slate-400 rounded-full"></div>
      <div class="w-1 h-1 bg-slate-400 rounded-full"></div>
      <div class="w-1 h-1 bg-slate-400 rounded-full"></div>
    </div>

    <!-- Item Preview -->
    <div class="flex-shrink-0">
      <div 
        v-if="item.imageUrl" 
        class="w-16 h-16 bg-slate-100 dark:bg-slate-800 rounded-lg overflow-hidden"
      >
        <img 
          :src="item.imageUrl" 
          :alt="item.title || 'Carousel item'"
          class="w-full h-full object-cover"
        />
      </div>
      <div 
        v-else 
        class="w-16 h-16 bg-slate-100 dark:bg-slate-800 rounded-lg flex items-center justify-center"
      >
        <UIcon :name="getContentTypeIcon(item.contentType)" class="w-6 h-6 text-slate-400" />
      </div>
    </div>

    <!-- Item Info -->
    <div class="flex-1 min-w-0">
      <div class="flex items-center gap-2 mb-1">
        <h4 class="font-medium text-slate-900 dark:text-white truncate">
          {{ item.title || 'Untitled Item' }}
        </h4>
        <UBadge 
          :color="getContentTypeColor(item.contentType)" 
          variant="soft" 
          size="xs"
        >
          {{ formatContentType(item.contentType) }}
        </UBadge>
        <UBadge 
          v-if="!item.isActive" 
          color="gray" 
          variant="soft" 
          size="xs"
        >
          Inactive
        </UBadge>
      </div>
      
      <p v-if="item.subtitle" class="text-sm text-slate-600 dark:text-slate-400 truncate mb-1">
        {{ item.subtitle }}
      </p>
      
      <div class="flex items-center gap-4 text-xs text-slate-500">
        <span v-if="item.sortOrder !== null">Order: {{ item.sortOrder }}</span>
        <span v-if="item.linkUrl">Has Link</span>
        <span v-if="item.ctaText">CTA: {{ item.ctaText }}</span>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex items-center gap-2">
      <!-- Move buttons -->
      <div class="flex flex-col gap-1">
        <UButton
          size="2xs"
          variant="ghost"
          color="gray"
          icon="i-lucide-chevron-up"
          @click="$emit('move-up', item)"
          :disabled="isFirst"
        />
        <UButton
          size="2xs"
          variant="ghost"
          color="gray"
          icon="i-lucide-chevron-down"
          @click="$emit('move-down', item)"
          :disabled="isLast"
        />
      </div>

      <!-- Main actions -->
      <UDropdown :items="dropdownItems" :popper="{ placement: 'bottom-end' }">
        <UButton
          color="gray"
          variant="ghost"
          icon="i-lucide-more-horizontal"
          size="sm"
        />
      </UDropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { CarouselItem } from "~/types/ecommerce";

interface Props {
  item: CarouselItem;
  isFirst?: boolean;
  isLast?: boolean;
}

interface Emits {
  (e: 'edit', item: CarouselItem): void;
  (e: 'delete', item: CarouselItem): void;
  (e: 'move-up', item: CarouselItem): void;
  (e: 'move-down', item: CarouselItem): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const contentTypeIcons = {
  IMAGE: "i-lucide-image",
  PRODUCT: "i-lucide-package",
  CATEGORY: "i-lucide-folder",
  BRAND: "i-lucide-award",
  OFFER: "i-lucide-percent",
};

const contentTypeColors = {
  IMAGE: "blue",
  PRODUCT: "green",
  CATEGORY: "purple",
  BRAND: "orange",
  OFFER: "red",
};

const contentTypeLabels = {
  IMAGE: "Image",
  PRODUCT: "Product",
  CATEGORY: "Category",
  BRAND: "Brand",
  OFFER: "Offer",
};

const getContentTypeIcon = (type: string) => {
  return contentTypeIcons[type as keyof typeof contentTypeIcons] || "i-lucide-circle";
};

const getContentTypeColor = (type: string) => {
  return contentTypeColors[type as keyof typeof contentTypeColors] || "gray";
};

const formatContentType = (type: string) => {
  return contentTypeLabels[type as keyof typeof contentTypeLabels] || type;
};

const dropdownItems = computed(() => [
  [
    {
      label: "Edit",
      icon: "i-lucide-pencil",
      click: () => emit('edit', props.item),
    },
  ],
  [
    {
      label: "Delete",
      icon: "i-lucide-trash-2",
      click: () => emit('delete', props.item),
      class: "text-red-600 dark:text-red-400",
    },
  ],
]);
</script>