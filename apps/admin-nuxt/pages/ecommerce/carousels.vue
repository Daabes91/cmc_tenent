<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50/50 via-white to-indigo-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Child routes (detail/new) render here -->
    <NuxtPage v-if="isChildRoute" />

    <!-- Header Section -->
    <div v-else class="sticky top-0 z-10 bg-white/95 backdrop-blur-xl border-b border-blue-200/60 shadow-sm dark:bg-slate-900/95 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-600 to-blue-800 shadow-lg">
              <UIcon name="i-lucide-shield-check" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">Content Carousels</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">Manage promotional content and insurance product showcases</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="loading"
              @click="loadCarousels"
            >
              Refresh
            </UButton>
            <UButton 
              color="blue" 
              icon="i-lucide-plus" 
              to="/ecommerce/carousels/new"
            >
              Create Carousel
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div v-if="!isChildRoute" class="max-w-7xl mx-auto px-6 py-8">

      <!-- E-commerce Disabled Alert -->
      <div v-if="!tenant?.ecommerceEnabled" class="mb-8">
        <UAlert
          icon="i-lucide-store-x"
          color="amber"
          variant="soft"
          title="E-commerce Module Disabled"
          description="Enable the e-commerce module for this tenant to manage carousels and showcase products."
        />
      </div>

      <template v-else>
        <!-- Quick Stats -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
          <div class="bg-white dark:bg-slate-800 rounded-xl p-6 shadow-sm border border-blue-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
                <UIcon name="i-lucide-shield" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Total Campaigns</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ carousels?.length || 0 }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">All promotional content</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-xl p-6 shadow-sm border border-green-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
                <UIcon name="i-lucide-trending-up" class="h-5 w-5 text-green-600 dark:text-green-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Live Campaigns</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ activeCarousels }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">Currently promoting</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-xl p-6 shadow-sm border border-amber-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-900/20">
                <UIcon name="i-lucide-pause-circle" class="h-5 w-5 text-amber-600 dark:text-amber-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Paused</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ inactiveCarousels }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">Temporarily disabled</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-xl p-6 shadow-sm border border-indigo-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-50 dark:bg-indigo-900/20">
                <UIcon name="i-lucide-file-text" class="h-5 w-5 text-indigo-600 dark:text-indigo-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">With Content</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ carouselsWithItems }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">Ready to publish</p>
          </div>
        </div>

        <!-- Search and Filters -->
        <div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-blue-200/60 dark:border-slate-700/60 overflow-hidden mb-6">
          <div class="bg-gradient-to-r from-blue-600 to-blue-800 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-filter" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">Campaign Management</h2>
                <p class="text-sm text-blue-100">Filter and manage your insurance marketing campaigns</p>
              </div>
            </div>
          </div>
          <div class="p-6">
            <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <UFormGroup label="Search">
                <UInput
                  v-model="searchQuery"
                  size="lg"
                  placeholder="Search carousels..."
                  icon="i-lucide-search"
                />
              </UFormGroup>
              <UFormGroup label="Type">
                <USelect
                  v-model="filters.type"
                  size="lg"
                  :options="typeFilterOptions"
                  value-attribute="value"
                  label-attribute="label"
                />
              </UFormGroup>
              <UFormGroup label="Platform">
                <USelect
                  v-model="filters.platform"
                  size="lg"
                  :options="platformFilterOptions"
                  value-attribute="value"
                  label-attribute="label"
                />
              </UFormGroup>
              <UFormGroup label="Status">
                <USelect
                  v-model="filters.status"
                  size="lg"
                  :options="statusFilterOptions"
                  value-attribute="value"
                  label-attribute="label"
                />
              </UFormGroup>
            </div>
          </div>
        </div>

        <!-- Carousels Grid -->
        <div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-blue-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-blue-600 to-blue-800 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-megaphone" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">Marketing Campaigns</h2>
                <p class="text-sm text-blue-100">{{ filteredCarousels.length }} campaign{{ filteredCarousels.length !== 1 ? 's' : '' }} found</p>
              </div>
            </div>
          </div>

          <div v-if="loading" class="p-6">
            <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              <div v-for="i in 6" :key="i" class="animate-pulse">
                <div class="bg-slate-200 dark:bg-slate-700 rounded-xl p-6 space-y-4">
                  <div class="flex items-center gap-4">
                    <div class="w-12 h-12 bg-slate-300 dark:bg-slate-600 rounded-xl"></div>
                    <div class="flex-1 space-y-2">
                      <div class="h-4 bg-slate-300 dark:bg-slate-600 rounded w-3/4"></div>
                      <div class="h-3 bg-slate-300 dark:bg-slate-600 rounded w-1/2"></div>
                    </div>
                  </div>
                  <div class="h-3 bg-slate-300 dark:bg-slate-600 rounded w-full"></div>
                  <div class="h-3 bg-slate-300 dark:bg-slate-600 rounded w-2/3"></div>
                </div>
              </div>
            </div>
          </div>

          <div v-else-if="errorMessage" class="p-12 text-center">
            <div class="flex flex-col items-center gap-4">
              <div class="h-16 w-16 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
                <UIcon name="i-lucide-alert-triangle" class="h-8 w-8 text-red-600 dark:text-red-400" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-slate-900 dark:text-white">Failed to load campaigns</h3>
                <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">{{ errorMessage }}</p>
              </div>
              <UButton
                color="red"
                icon="i-lucide-refresh-cw"
                @click="loadCarousels"
              >
                Try Again
              </UButton>
            </div>
          </div>

          <div v-else-if="filteredCarousels.length > 0" class="p-6">
            <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              <div
                v-for="carousel in filteredCarousels"
                :key="carousel.id"
                class="bg-gradient-to-br from-white to-blue-50/30 dark:bg-slate-700/50 rounded-xl p-6 border border-blue-200/60 dark:border-slate-600 hover:shadow-lg hover:border-blue-400 dark:hover:border-blue-500 transition-all duration-200 group"
              >
                <!-- Carousel Header -->
                <div class="flex items-start justify-between mb-4">
                  <div class="flex items-center gap-3">
                    <div class="relative">
                      <div class="h-12 w-12 rounded-xl bg-gradient-to-br from-blue-100 to-blue-200 dark:from-blue-900/30 dark:to-blue-800/30 flex items-center justify-center shadow-sm">
                        <UIcon :name="getTypeIcon(carousel.type)" class="h-6 w-6 text-blue-700 dark:text-blue-400" />
                      </div>
                      <div
                        :class="[
                          'absolute -top-1 -right-1 w-4 h-4 rounded-full border-2 border-white dark:border-slate-700',
                          carousel.isActive ? 'bg-green-500' : 'bg-red-500'
                        ]"
                      ></div>
                    </div>
                    <div class="flex-1 min-w-0">
                      <h3 class="font-semibold text-slate-900 dark:text-white truncate">
                        {{ carousel.name }}
                      </h3>
                      <p class="text-sm text-slate-500 dark:text-slate-400 truncate">
                        {{ formatTypeLabel(carousel.type) }}
                      </p>
                    </div>
                  </div>
                  <UBadge
                    :color="carousel.isActive ? 'green' : 'amber'"
                    variant="soft"
                    size="sm"
                  >
                    {{ carousel.isActive ? 'Live' : 'Paused' }}
                  </UBadge>
                </div>

                <!-- Carousel Details -->
                <div class="space-y-3 mb-4">
                  <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                    <UIcon name="i-lucide-map-pin" class="h-4 w-4 text-slate-400" />
                    <code class="text-xs bg-slate-200 dark:bg-slate-600 px-2 py-1 rounded">{{ carousel.placement }}</code>
                  </div>
                  <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                    <UIcon :name="getPlatformIcon(carousel.platform)" class="h-4 w-4 text-slate-400" />
                    <span>{{ carousel.platform || 'WEB' }}</span>
                  </div>
                  <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                    <UIcon name="i-lucide-layers" class="h-4 w-4 text-slate-400" />
                    <span>{{ carousel.itemCount || 0 }} items</span>
                    <span v-if="carousel.maxItems" class="text-slate-400">/ {{ carousel.maxItems }} max</span>
                  </div>
                  <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                    <UIcon name="i-lucide-link" class="h-4 w-4 text-slate-400" />
                    <code class="text-xs">{{ carousel.slug }}</code>
                  </div>
                </div>

                <!-- Actions -->
                <div class="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-slate-600">
                  <div class="text-xs text-slate-500 dark:text-slate-400">
                    ID: {{ carousel.id }}
                  </div>
                  <div class="flex items-center gap-2">
                    <UButton
                      size="sm"
                      variant="ghost"
                      color="blue"
                      icon="i-lucide-edit"
                      :to="`/ecommerce/carousels/${carousel.id}`"
                    >
                      Edit
                    </UButton>
                    <UDropdown :items="getCarouselActions(carousel)" :popper="{ placement: 'bottom-end' }">
                      <UButton
                        size="sm"
                        variant="ghost"
                        color="gray"
                        icon="i-lucide-more-horizontal"
                      />
                    </UDropdown>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="p-12 text-center">
            <div class="flex flex-col items-center gap-4">
              <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                <UIcon name="i-lucide-panels-top-left" class="h-8 w-8 text-slate-400" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-slate-900 dark:text-white">No campaigns found</h3>
                <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                  {{ hasActiveFilters ? 'Try adjusting your search or filters' : 'Get started by creating your first marketing campaign to promote insurance products.' }}
                </p>
              </div>
              <UButton
                v-if="!hasActiveFilters"
                color="blue"
                icon="i-lucide-plus"
                to="/ecommerce/carousels/new"
              >
                Create Your First Campaign
              </UButton>
              <UButton
                v-else
                variant="outline"
                icon="i-lucide-x"
                @click="clearFilters"
              >
                Clear Filters
              </UButton>
            </div>
          </div>
        </div>
      </template>

      <!-- Delete Confirmation Modal -->
      <UModal v-model="showDeleteModal">
        <UCard>
          <template #header>
            <div class="flex items-center gap-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-full bg-red-100 dark:bg-red-900/30">
                <UIcon name="i-lucide-alert-triangle" class="h-5 w-5 text-red-600 dark:text-red-400" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-slate-900 dark:text-white">Delete Campaign</h3>
                <p class="text-sm text-slate-600 dark:text-slate-300">This action cannot be undone</p>
              </div>
            </div>
          </template>

          <div class="py-4">
            <p class="text-slate-700 dark:text-slate-300">
              Are you sure you want to delete "{{ carouselToDelete?.name }}"? This will permanently remove the campaign and all its content items.
            </p>
          </div>

          <template #footer>
            <div class="flex justify-end gap-3">
              <UButton
                variant="ghost"
                color="gray"
                @click="showDeleteModal = false"
                :disabled="deleting"
              >
                Cancel
              </UButton>
              <UButton
                color="red"
                icon="i-lucide-trash-2"
                :loading="deleting"
                @click="confirmDeleteCarousel"
              >
                Delete Campaign
              </UButton>
            </div>
          </template>
        </UCard>
      </UModal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useEcommerceService } from "@/services/ecommerce.service";
import type { Carousel } from "~/types/ecommerce";

definePageMeta({
  title: 'Marketing Campaigns',
  requiresAuth: true
});

const { tenant, fetchTenantContext } = useTenantContext();
const route = useRoute();
const ecommerce = useEcommerceService();
const toast = useToast();

// State
const carousels = ref<Carousel[]>([]);
const loading = ref(true);
const searchQuery = ref('');
const showDeleteModal = ref(false);
const carouselToDelete = ref<Carousel | null>(null);
const deleting = ref(false);
const errorMessage = ref<string | null>(null);

// Filters
const filters = reactive({
  type: "",
  platform: "",
  status: "",
});

// Filter Options
const typeFilterOptions = computed(() => [
  { label: "All Campaign Types", value: "" },
  { label: "Insurance Products", value: "PRODUCT" },
  { label: "Coverage Overview", value: "VIEW_ALL_PRODUCTS" },
  { label: "Visual Campaigns", value: "IMAGE" },
  { label: "Policy Categories", value: "CATEGORY" },
  { label: "Company Branding", value: "BRAND" },
  { label: "Special Promotions", value: "OFFER" },
  { label: "Customer Stories", value: "TESTIMONIAL" },
  { label: "Educational Content", value: "BLOG" },
  { label: "Mixed Campaigns", value: "MIXED" },
]);

const platformFilterOptions = computed(() => [
  { label: "All Platforms", value: "" },
  { label: "Web", value: "WEB" },
  { label: "Mobile", value: "MOBILE" },
  { label: "Both", value: "BOTH" },
]);

const statusFilterOptions = computed(() => [
  { label: "All Statuses", value: "" },
  { label: "Active", value: "active" },
  { label: "Inactive", value: "inactive" },
]);

const isChildRoute = computed(() => Boolean(route.params.id) || route.path.endsWith("/new"));

// Computed
const activeCarousels = computed(() => carousels.value?.filter(c => c.isActive).length || 0);
const inactiveCarousels = computed(() => carousels.value?.filter(c => !c.isActive).length || 0);
const carouselsWithItems = computed(() => carousels.value?.filter(c => (c.itemCount || 0) > 0).length || 0);

const hasActiveFilters = computed(() => {
  return searchQuery.value !== "" || 
         filters.type !== "" || 
         filters.platform !== "" || 
         filters.status !== "";
});

const filteredCarousels = computed(() => {
  let filtered = carousels.value || [];

  // Filter by search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(carousel =>
      carousel.name.toLowerCase().includes(query) ||
      carousel.slug.toLowerCase().includes(query) ||
      carousel.placement.toLowerCase().includes(query) ||
      carousel.type.toLowerCase().includes(query)
    );
  }

  // Filter by type
  if (filters.type) {
    filtered = filtered.filter(carousel => carousel.type === filters.type);
  }

  // Filter by platform
  if (filters.platform) {
    filtered = filtered.filter(carousel => carousel.platform === filters.platform);
  }

  // Filter by status
  if (filters.status) {
    filtered = filtered.filter(carousel => {
      if (filters.status === 'active') return carousel.isActive;
      if (filters.status === 'inactive') return !carousel.isActive;
      return true;
    });
  }

  return filtered;
});

// Helper Functions
const getTypeIcon = (type: string) => {
  const typeIcons: Record<string, string> = {
    PRODUCT: "i-lucide-shield-check",
    VIEW_ALL_PRODUCTS: "i-lucide-layout-grid",
    IMAGE: "i-lucide-camera",
    CATEGORY: "i-lucide-folder-open",
    BRAND: "i-lucide-building-2",
    OFFER: "i-lucide-tag",
    TESTIMONIAL: "i-lucide-message-circle",
    BLOG: "i-lucide-newspaper",
    MIXED: "i-lucide-layers-3",
  };
  return typeIcons[type] || "i-lucide-circle";
};

const getPlatformIcon = (platform?: string) => {
  const platformIcons: Record<string, string> = {
    WEB: "i-lucide-monitor",
    MOBILE: "i-lucide-smartphone",
    BOTH: "i-lucide-tablet-smartphone",
  };
  return platformIcons[platform || 'WEB'] || "i-lucide-monitor";
};

const formatTypeLabel = (type: string) => {
  const typeLabels: Record<string, string> = {
    PRODUCT: "Insurance Products",
    VIEW_ALL_PRODUCTS: "Coverage Overview",
    IMAGE: "Visual Campaign",
    CATEGORY: "Policy Categories",
    BRAND: "Company Branding",
    OFFER: "Special Promotions",
    TESTIMONIAL: "Customer Stories",
    BLOG: "Educational Content",
    MIXED: "Mixed Campaign",
  };
  return typeLabels[type] || type;
};

const clearFilters = () => {
  searchQuery.value = '';
  filters.type = "";
  filters.platform = "";
  filters.status = "";
};

// Methods
const loadCarousels = async () => {
  try {
    loading.value = true;
    errorMessage.value = null;
    
    if (!tenant.value?.id) {
      await fetchTenantContext();
    }
    
    if (!tenant.value?.id) {
      throw new Error('No tenant context available');
    }

    const page = await ecommerce.listCarousels(tenant.value.id, { size: 50 });
    carousels.value = page?.content ?? [];
  } catch (error) {
    console.error('Failed to load carousels:', error);
    carousels.value = [];
    errorMessage.value = (error as any)?.data?.message || (error as any)?.message || "Failed to load carousels";
    toast.add({
      title: "Failed to load campaigns",
      description: errorMessage.value,
      color: "red",
      icon: "i-lucide-alert-circle"
    });
  } finally {
    loading.value = false;
  }
};

const getCarouselActions = (carousel: Carousel) => {
  return [
    [
      {
        label: "Duplicate",
        icon: "i-lucide-copy",
        click: () => duplicateCarousel(carousel),
      },
      {
        label: carousel.isActive ? "Pause Campaign" : "Activate Campaign",
        icon: carousel.isActive ? "i-lucide-pause" : "i-lucide-play",
        click: () => toggleCarouselStatus(carousel),
      },
    ],
    [
      {
        label: "Delete",
        icon: "i-lucide-trash-2",
        click: () => confirmDelete(carousel),
      },
    ],
  ];
};

const confirmDelete = (carousel: Carousel) => {
  carouselToDelete.value = carousel;
  showDeleteModal.value = true;
};

const confirmDeleteCarousel = async () => {
  if (!carouselToDelete.value || !tenant.value?.id) return;

  try {
    deleting.value = true;
    await ecommerce.deleteCarousel(tenant.value.id, carouselToDelete.value.id);
    carousels.value = carousels.value.filter(c => c.id !== carouselToDelete.value!.id);
    showDeleteModal.value = false;
    carouselToDelete.value = null;
    
    toast.add({
      title: "Campaign deleted",
      description: "The marketing campaign has been deleted successfully.",
      color: "green",
      icon: "i-lucide-check-circle"
    });
  } catch (error) {
    console.error('Failed to delete carousel:', error);
    toast.add({
      title: "Failed to delete campaign",
      description: (error as any)?.data?.message || (error as any)?.message || "An error occurred",
      color: "red",
      icon: "i-lucide-alert-circle"
    });
  } finally {
    deleting.value = false;
  }
};

const duplicateCarousel = async (carousel: Carousel) => {
  if (!tenant.value?.id) return;
  
  try {
    const duplicatedData = {
      ...carousel,
      name: `${carousel.name} (Copy)`,
      slug: `${carousel.slug}-copy-${Date.now()}`,
      isActive: false,
    };
    delete (duplicatedData as any).id;
    
    await ecommerce.createCarousel(tenant.value.id, duplicatedData);
    toast.add({
      title: "Campaign duplicated",
      description: `"${carousel.name}" has been duplicated successfully.`,
      color: "green",
    });
    await loadCarousels();
  } catch (err: any) {
    toast.add({
      title: "Failed to duplicate campaign",
      description: err?.data?.message || err?.message || "An error occurred",
      color: "red",
    });
  }
};

const toggleCarouselStatus = async (carousel: Carousel) => {
  if (!tenant.value?.id) return;
  
  try {
    await ecommerce.updateCarousel(tenant.value.id, carousel.id, {
      isActive: !carousel.isActive,
    });
    
    // Update local state
    const index = carousels.value.findIndex(c => c.id === carousel.id);
    if (index !== -1) {
      carousels.value[index].isActive = !carousel.isActive;
    }
    
    toast.add({
      title: `Campaign ${carousel.isActive ? 'paused' : 'activated'}`,
      description: `"${carousel.name}" is now ${carousel.isActive ? 'paused' : 'live'}.`,
      color: "green",
    });
  } catch (err: any) {
    toast.add({
      title: "Failed to update campaign status",
      description: err?.data?.message || err?.message || "An error occurred",
      color: "red",
    });
  }
};

// Load data on mount
onMounted(() => {
  loadCarousels();
});
</script>
