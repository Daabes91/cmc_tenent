<template>
  <div class="max-w-6xl space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <div class="flex items-center gap-3 mb-2">
          <UButton
            variant="ghost"
            color="gray"
            icon="i-lucide-arrow-left"
            size="sm"
            @click="router.back()"
          >
            Back
          </UButton>
          <div class="h-6 w-px bg-slate-300 dark:bg-slate-600"></div>
          <UBadge 
            :color="form.isActive ? 'green' : 'gray'" 
            variant="soft"
          >
            {{ form.isActive ? 'Active' : 'Inactive' }}
          </UBadge>
        </div>
        <h1 class="text-3xl font-bold text-slate-900 dark:text-white">
          {{ form.name || 'Edit Carousel' }}
        </h1>
        <p class="text-slate-600 dark:text-slate-400 mt-1">
          Configure carousel settings and manage content items
        </p>
      </div>
      <div class="flex items-center gap-3">
        <UButton
          variant="outline"
          color="gray"
          icon="i-lucide-eye"
          size="sm"
          @click="previewCarousel"
        >
          Preview
        </UButton>
        <UButton
          variant="outline"
          color="gray"
          icon="i-lucide-x"
          size="sm"
          @click="router.back()"
        >
          Cancel
        </UButton>
        <UButton
          color="primary"
          icon="i-lucide-save"
          :loading="saving"
          size="sm"
          @click="save"
        >
          Save Changes
        </UButton>
      </div>
    </div>

    <!-- Error Alert -->
    <UAlert
      v-if="errorMessage"
      icon="i-lucide-alert-triangle"
      color="red"
      variant="soft"
      :title="errorMessage"
      description="Please fix the issues and try again."
      :actions="[{ label: 'Retry', click: loadCarousel }]"
    />

    <div v-if="loaded" class="grid gap-6 lg:grid-cols-3">
      <!-- Carousel Configuration -->
      <div class="lg:col-span-2 space-y-6">
        <UCard>
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-settings" class="w-5 h-5" />
              <h2 class="text-lg font-semibold">Carousel Configuration</h2>
            </div>
          </template>

          <div class="space-y-6">
            <!-- Basic Info -->
            <div class="grid gap-4 sm:grid-cols-2">
              <UFormGroup label="Name" required :error="errors.name">
                <UInput
                  v-model="form.name"
                  placeholder="Enter carousel name"
                  icon="i-lucide-type"
                />
              </UFormGroup>
              <UFormGroup label="Name (Arabic)">
                <UInput
                  v-model="form.nameAr"
                  dir="rtl"
                  placeholder="اسم المعرض بالعربية"
                  icon="i-lucide-type"
                />
              </UFormGroup>
              <UFormGroup label="Slug" required :error="errors.slug">
                <UInput
                  v-model="form.slug"
                  placeholder="carousel-slug"
                  icon="i-lucide-link"
                />
              </UFormGroup>
            </div>

            <!-- Type & Placement -->
            <div class="grid gap-4 sm:grid-cols-2">
              <UFormGroup label="Type" required :error="errors.type">
                <USelectMenu
                  v-model="form.type"
                  :options="typeOptions"
                  value-attribute="value"
                  option-attribute="label"
                  placeholder="Select carousel type"
                  searchable
                >
                  <template #label>
                    <span v-if="form.type" class="flex items-center gap-2">
                      <UIcon :name="getTypeIcon(form.type)" class="w-4 h-4" />
                      {{ getTypeLabel(form.type) }}
                    </span>
                    <span v-else>Select type</span>
                  </template>
                </USelectMenu>
              </UFormGroup>
              <UFormGroup label="Placement" required :error="errors.placement">
                <UInput
                  v-model="form.placement"
                  placeholder="e.g. HOME_PAGE, CATEGORY_PAGE"
                  icon="i-lucide-map-pin"
                />
              </UFormGroup>
            </div>

            <!-- Platform & Settings -->
            <div class="grid gap-4 sm:grid-cols-2">
              <UFormGroup label="Platform">
                <USelectMenu
                  v-model="form.platform"
                  :options="platformOptions"
                  value-attribute="value"
                  option-attribute="label"
                  placeholder="Select platform"
                >
                  <template #label>
                    <span v-if="form.platform" class="flex items-center gap-2">
                      <UIcon :name="getPlatformIcon(form.platform)" class="w-4 h-4" />
                      {{ form.platform }}
                    </span>
                    <span v-else>Select platform</span>
                  </template>
                </USelectMenu>
              </UFormGroup>
              <UFormGroup label="Max Items" description="Maximum number of items to display">
                <UInput
                  v-model.number="form.maxItems"
                  type="number"
                  min="1"
                  max="50"
                  placeholder="10"
                  icon="i-lucide-hash"
                />
              </UFormGroup>
            </div>

            <!-- Status -->
            <div class="flex items-center justify-between p-4 bg-slate-50 dark:bg-slate-800/50 rounded-lg">
              <div>
                <h3 class="font-medium text-slate-900 dark:text-white">Carousel Status</h3>
                <p class="text-sm text-slate-600 dark:text-slate-400">
                  {{ form.isActive ? 'This carousel is currently active and visible to users.' : 'This carousel is inactive and hidden from users.' }}
                </p>
              </div>
              <UToggle v-model="form.isActive" size="lg" />
            </div>

            <!-- Type-specific Info -->
            <UAlert
              v-if="form.type === 'VIEW_ALL_PRODUCTS'"
              icon="i-lucide-info"
              color="blue"
              variant="soft"
              title="Auto-populated Carousel"
              description="This carousel will automatically display all active products. Manual items will be ignored."
            />
          </div>
        </UCard>

        <!-- Carousel Items Management -->
        <UCard v-if="form.type && form.type !== 'VIEW_ALL_PRODUCTS'">
          <template #header>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <UIcon name="i-lucide-layers" class="w-5 h-5" />
                <h2 class="text-lg font-semibold">Carousel Items</h2>
                <UBadge variant="soft" size="xs">
                  {{ carouselItems.length }} items
                </UBadge>
              </div>
              <UButton
                icon="i-lucide-plus"
                size="sm"
                @click="showAddItemModal = true"
              >
                Add Item
              </UButton>
            </div>
          </template>

          <div v-if="loadingItems" class="space-y-3">
            <div v-for="i in 3" :key="i" class="flex items-center gap-4 p-4 border border-slate-200 dark:border-slate-700 rounded-lg">
              <USkeleton class="h-16 w-16 rounded" />
              <div class="flex-1 space-y-2">
                <USkeleton class="h-4 w-1/3" />
                <USkeleton class="h-3 w-1/2" />
              </div>
              <USkeleton class="h-8 w-20" />
            </div>
          </div>

          <div v-else-if="carouselItems.length === 0" class="text-center py-8">
            <UIcon name="i-lucide-image-off" class="w-12 h-12 mx-auto text-slate-400 mb-3" />
            <h3 class="font-medium text-slate-900 dark:text-white mb-2">No items yet</h3>
            <p class="text-slate-600 dark:text-slate-400 mb-4">
              Add your first item to start building this carousel.
            </p>
            <UButton
              icon="i-lucide-plus"
              @click="showAddItemModal = true"
            >
              Add First Item
            </UButton>
          </div>

          <div v-else class="space-y-3">
            <CarouselItemCard
              v-for="item in carouselItems"
              :key="item.id"
              :item="item"
              @edit="editItem"
              @delete="deleteItem"
              @move-up="moveItemUp"
              @move-down="moveItemDown"
            />
          </div>
        </UCard>
      </div>

      <!-- Sidebar -->
      <div class="space-y-6">
        <!-- Quick Stats -->
        <UCard>
          <template #header>
            <h3 class="font-semibold">Quick Stats</h3>
          </template>
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <span class="text-sm text-slate-600 dark:text-slate-400">Carousel ID</span>
              <code class="text-sm font-mono bg-slate-100 dark:bg-slate-800 px-2 py-1 rounded">
                {{ carouselId }}
              </code>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-slate-600 dark:text-slate-400">Items Count</span>
              <span class="font-medium">{{ carouselItems.length }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-slate-600 dark:text-slate-400">Max Items</span>
              <span class="font-medium">{{ form.maxItems || 'Unlimited' }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-slate-600 dark:text-slate-400">Status</span>
              <UBadge :color="form.isActive ? 'green' : 'gray'" variant="soft" size="xs">
                {{ form.isActive ? 'Active' : 'Inactive' }}
              </UBadge>
            </div>
          </div>
        </UCard>

        <!-- Actions -->
        <UCard>
          <template #header>
            <h3 class="font-semibold">Actions</h3>
          </template>
          <div class="space-y-3">
            <UButton
              block
              variant="outline"
              icon="i-lucide-eye"
              @click="previewCarousel"
            >
              Preview Carousel
            </UButton>
            <UButton
              block
              variant="outline"
              icon="i-lucide-copy"
              @click="duplicateCarousel"
            >
              Duplicate Carousel
            </UButton>
            <UButton
              block
              variant="outline"
              color="red"
              icon="i-lucide-trash-2"
              @click="showDeleteModal = true"
            >
              Delete Carousel
            </UButton>
          </div>
        </UCard>
      </div>
    </div>

    <!-- Loading State -->
    <div v-else class="space-y-6">
      <USkeleton class="h-20 w-full" />
      <div class="grid gap-6 lg:grid-cols-3">
        <div class="lg:col-span-2">
          <USkeleton class="h-96 w-full" />
        </div>
        <div>
          <USkeleton class="h-64 w-full" />
        </div>
      </div>
    </div>

    <!-- Add Item Modal -->
    <CarouselItemModal
      v-model="showAddItemModal"
      :carousel-id="carouselId"
      :carousel-type="form.type"
      @saved="onItemSaved"
    />

    <!-- Edit Item Modal -->
    <CarouselItemModal
      v-model="showEditItemModal"
      :carousel-id="carouselId"
      :carousel-type="form.type"
      :item="editingItem"
      @saved="onItemSaved"
    />

    <!-- Delete Confirmation Modal -->
    <UModal v-model="showDeleteModal">
      <UCard>
        <template #header>
          <h3 class="text-lg font-semibold">Delete Carousel</h3>
        </template>
        <div class="space-y-4">
          <p class="text-slate-600 dark:text-slate-400">
            Are you sure you want to delete "{{ form.name }}"? This action cannot be undone.
          </p>
          <div class="flex justify-end gap-3">
            <UButton variant="ghost" @click="showDeleteModal = false">Cancel</UButton>
            <UButton color="red" @click="deleteCarousel" :loading="deleting">Delete</UButton>
          </div>
        </div>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import { useEcommerceService } from "@/services/ecommerce.service";
import type { Carousel, CarouselItem } from "~/types/ecommerce";

definePageMeta({
  requiresAuth: true,
  ssr: false,
});

const route = useRoute();
const router = useRouter();
const toast = useToast();
const { tenant, fetchTenantContext } = useTenantContext();
const ecommerce = useEcommerceService();

// State
const carouselId = Number(route.params.id);
const form = reactive<Partial<Carousel>>({
  nameAr: "",
  platform: "WEB",
  maxItems: 10,
  isActive: true,
});
const errors = reactive<Record<string, string>>({});
const carouselItems = ref<CarouselItem[]>([]);

// Loading states
const saving = ref(false);
const loaded = ref(false);
const loadingItems = ref(false);
const deleting = ref(false);
const errorMessage = ref<string | null>(null);

// Modal states
const showAddItemModal = ref(false);
const showEditItemModal = ref(false);
const showDeleteModal = ref(false);
const editingItem = ref<CarouselItem | null>(null);

// Options
const typeOptions = [
  { label: "Product Showcase", value: "PRODUCT", icon: "i-lucide-package" },
  { label: "All Products View", value: "VIEW_ALL_PRODUCTS", icon: "i-lucide-grid-3x3" },
  { label: "Image Gallery", value: "IMAGE", icon: "i-lucide-image" },
  { label: "Category Display", value: "CATEGORY", icon: "i-lucide-folder" },
  { label: "Brand Showcase", value: "BRAND", icon: "i-lucide-award" },
  { label: "Special Offers", value: "OFFER", icon: "i-lucide-percent" },
  { label: "Testimonials", value: "TESTIMONIAL", icon: "i-lucide-quote" },
  { label: "Blog Posts", value: "BLOG", icon: "i-lucide-file-text" },
  { label: "Mixed Content", value: "MIXED", icon: "i-lucide-layers" },
];

const platformOptions = [
  { label: "Web", value: "WEB", icon: "i-lucide-monitor" },
  { label: "Mobile", value: "MOBILE", icon: "i-lucide-smartphone" },
  { label: "Both", value: "BOTH", icon: "i-lucide-tablet-smartphone" },
];

// Helper functions
const getTypeIcon = (type: string) => {
  const option = typeOptions.find(opt => opt.value === type);
  return option?.icon || "i-lucide-circle";
};

const getTypeLabel = (type: string) => {
  const option = typeOptions.find(opt => opt.value === type);
  return option?.label || type;
};

const getPlatformIcon = (platform: string) => {
  const option = platformOptions.find(opt => opt.value === platform);
  return option?.icon || "i-lucide-monitor";
};

// API functions
const ensureTenant = async () => {
  if (tenant.value?.id) return true;
  try {
    await fetchTenantContext();
    return Boolean(tenant.value?.id);
  } catch (err) {
    console.error("[carousel edit] tenant context error", err);
    errorMessage.value = "Failed to load tenant context";
    return false;
  }
};

const loadCarousel = async () => {
  loaded.value = false;

  const hasTenant = await ensureTenant();
  if (!hasTenant || !carouselId) {
    loaded.value = true;
    return;
  }
  
  try {
    const carousel = await ecommerce.getCarousel(tenant.value!.id, carouselId);
    Object.assign(form, carousel);
    
    // Load items if not auto-populated type
    if (carousel.type !== 'VIEW_ALL_PRODUCTS') {
      await loadCarouselItems();
    }
  } catch (err: any) {
    console.error("[carousel edit] load error", err);
    errorMessage.value = err?.data?.message || err?.message || "Failed to load carousel";
  } finally {
    loaded.value = true;
  }
};

const loadCarouselItems = async () => {
  if (!tenant.value?.id || !carouselId) return;
  
  loadingItems.value = true;
  try {
    const page = await ecommerce.listCarouselItems(tenant.value.id, carouselId, {
      size: 50,
      sort: 'sortOrder,asc'
    });
    carouselItems.value = page?.content ?? [];
  } catch (err: any) {
    console.error("[carousel items] load error", err);
    toast.add({
      title: "Failed to load carousel items",
      description: err?.data?.message || err?.message,
      color: "red",
    });
  } finally {
    loadingItems.value = false;
  }
};

// Validation
const validate = () => {
  errors.name = form.name ? "" : "Name is required";
  errors.slug = form.slug ? "" : "Slug is required";
  errors.type = form.type ? "" : "Type is required";
  errors.placement = form.placement ? "" : "Placement is required";
  return !errors.name && !errors.slug && !errors.type && !errors.placement;
};

// Actions
const save = async () => {
  if (!tenant.value?.id || !carouselId) return;
  if (!validate()) return;
  
  saving.value = true;
  try {
    await ecommerce.updateCarousel(tenant.value.id, carouselId, form);
    toast.add({ 
      title: "Carousel updated", 
      description: "Your changes have been saved successfully.",
      color: "green" 
    });
  } catch (err: any) {
    toast.add({
      title: "Failed to update carousel",
      description: err?.data?.message || err?.message,
      color: "red",
    });
  } finally {
    saving.value = false;
  }
};

const previewCarousel = () => {
  // TODO: Implement preview functionality
  toast.add({
    title: "Preview coming soon",
    description: "Carousel preview functionality will be available soon.",
    color: "blue",
  });
};

const duplicateCarousel = async () => {
  if (!tenant.value?.id) return;
  
  try {
    const duplicatedData = {
      ...form,
      name: `${form.name} (Copy)`,
      slug: `${form.slug}-copy-${Date.now()}`,
      isActive: false,
    };
    delete duplicatedData.id;
    
    const newCarousel = await ecommerce.createCarousel(tenant.value.id, duplicatedData);
    toast.add({
      title: "Carousel duplicated",
      description: "The carousel has been duplicated successfully.",
      color: "green",
    });
    router.push(`/ecommerce/carousels/${newCarousel.id}`);
  } catch (err: any) {
    toast.add({
      title: "Failed to duplicate carousel",
      description: err?.data?.message || err?.message,
      color: "red",
    });
  }
};

const deleteCarousel = async () => {
  if (!tenant.value?.id || !carouselId) return;
  
  deleting.value = true;
  try {
    await ecommerce.deleteCarousel(tenant.value.id, carouselId);
    toast.add({
      title: "Carousel deleted",
      description: "The carousel has been deleted successfully.",
      color: "green",
    });
    router.push("/ecommerce/carousels");
  } catch (err: any) {
    toast.add({
      title: "Failed to delete carousel",
      description: err?.data?.message || err?.message,
      color: "red",
    });
  } finally {
    deleting.value = false;
    showDeleteModal.value = false;
  }
};

// Item management
const editItem = (item: CarouselItem) => {
  editingItem.value = item;
  showEditItemModal.value = true;
};

const deleteItem = async (item: CarouselItem) => {
  if (!tenant.value?.id || !carouselId) return;
  
  try {
    await ecommerce.deleteCarouselItem(tenant.value.id, carouselId, item.id);
    
    // Remove from local state
    const index = carouselItems.value.findIndex(i => i.id === item.id);
    if (index !== -1) {
      carouselItems.value.splice(index, 1);
    }
    
    toast.add({
      title: "Item deleted",
      description: "The carousel item has been removed.",
      color: "green",
    });
  } catch (err: any) {
    toast.add({
      title: "Failed to delete item",
      description: err?.data?.message || err?.message,
      color: "red",
    });
  }
};

const moveItemUp = async (item: CarouselItem) => {
  const index = carouselItems.value.findIndex(i => i.id === item.id);
  if (index <= 0) return;
  
  // Swap with previous item
  const prevItem = carouselItems.value[index - 1];
  const tempOrder = item.sortOrder;
  item.sortOrder = prevItem.sortOrder;
  prevItem.sortOrder = tempOrder;
  
  // Update in array
  carouselItems.value.splice(index, 1);
  carouselItems.value.splice(index - 1, 0, item);
  
  // TODO: Update on server
};

const moveItemDown = async (item: CarouselItem) => {
  const index = carouselItems.value.findIndex(i => i.id === item.id);
  if (index >= carouselItems.value.length - 1) return;
  
  // Swap with next item
  const nextItem = carouselItems.value[index + 1];
  const tempOrder = item.sortOrder;
  item.sortOrder = nextItem.sortOrder;
  nextItem.sortOrder = tempOrder;
  
  // Update in array
  carouselItems.value.splice(index, 1);
  carouselItems.value.splice(index + 1, 0, item);
  
  // TODO: Update on server
};

const onItemSaved = () => {
  showAddItemModal.value = false;
  showEditItemModal.value = false;
  editingItem.value = null;
  loadCarouselItems();
};

// Lifecycle
onMounted(async () => {
  await loadCarousel();
});

watch(
  () => tenant.value?.id,
  async (id) => {
    if (id) {
      await loadCarousel();
    }
  }
);
</script>
