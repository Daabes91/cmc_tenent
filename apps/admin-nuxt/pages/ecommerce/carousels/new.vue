<template>
  <div class="max-w-4xl space-y-6">
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
          <UBadge color="blue" variant="soft">New Carousel</UBadge>
        </div>
        <h1 class="text-3xl font-bold text-slate-900 dark:text-white">Create Carousel</h1>
        <p class="text-slate-600 dark:text-slate-400 mt-1">
          Set up a new carousel to showcase content across your store
        </p>
      </div>
      <div class="flex items-center gap-3">
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
          icon="i-lucide-plus"
          :loading="saving"
          size="sm"
          @click="save"
        >
          Create Carousel
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
    />

    <!-- Form -->
    <UCard>
      <template #header>
        <div class="flex items-center gap-2">
          <UIcon name="i-lucide-settings" class="w-5 h-5" />
          <h2 class="text-lg font-semibold">Carousel Configuration</h2>
        </div>
      </template>

      <div class="space-y-6">
        <!-- Basic Information -->
        <div class="space-y-4">
          <h3 class="text-sm font-medium text-slate-900 dark:text-white uppercase tracking-wide">
            Basic Information
          </h3>
          
        <div class="grid gap-4 sm:grid-cols-2">
          <UFormGroup label="Name" required :error="errors.name">
            <UInput
              v-model="form.name"
              placeholder="Enter carousel name"
              icon="i-lucide-type"
              @input="generateSlug"
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
        </div>

        <UDivider />

        <!-- Content Configuration -->
        <div class="space-y-4">
          <h3 class="text-sm font-medium text-slate-900 dark:text-white uppercase tracking-wide">
            Content Configuration
          </h3>
          
          <div class="grid gap-4 sm:grid-cols-2">
              <UFormGroup label="Content Type" required :error="errors.type">
                <USelectMenu
                  v-model="form.type"
                  :options="typeOptions"
                  value-attribute="value"
                  option-attribute="label"
                  placeholder="Select content type"
                  searchable
                >
                <template #label>
                  <span v-if="form.type" class="flex items-center gap-2">
                    <UIcon :name="getTypeIcon(form.type)" class="w-4 h-4" />
                    {{ getTypeLabel(form.type) }}
                  </span>
                  <span v-else>Select content type</span>
                </template>
              </USelectMenu>
            </UFormGroup>
              <UFormGroup label="Placement" required :error="errors.placement">
                <USelectMenu
                  v-model="form.placement"
                  :options="placementOptions"
                  value-attribute="value"
                  option-attribute="label"
                  placeholder="Select placement"
                  searchable
                  creatable
                >
                <template #label>
                  <span v-if="form.placement" class="flex items-center gap-2">
                    <UIcon name="i-lucide-map-pin" class="w-4 h-4" />
                    {{ form.placement }}
                  </span>
                  <span v-else>Select placement</span>
                </template>
              </USelectMenu>
            </UFormGroup>
          </div>

          <!-- Type Description -->
          <div v-if="form.type" class="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
            <div class="flex items-start gap-3">
              <UIcon :name="getTypeIcon(form.type)" class="w-5 h-5 text-blue-600 dark:text-blue-400 mt-0.5" />
              <div>
                <h4 class="font-medium text-blue-900 dark:text-blue-100 mb-1">
                  {{ getTypeLabel(form.type) }}
                </h4>
                <p class="text-sm text-blue-700 dark:text-blue-300">
                  {{ getTypeDescription(form.type) }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <UDivider />

        <!-- Display Settings -->
        <div class="space-y-4">
          <h3 class="text-sm font-medium text-slate-900 dark:text-white uppercase tracking-wide">
            Display Settings
          </h3>
          
          <div class="grid gap-4 sm:grid-cols-2">
            <UFormGroup label="Platform">
              <USelectMenu
                v-model="form.platform"
                :options="platformOptions"
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
            <UFormGroup label="Maximum Items" description="Leave empty for unlimited">
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
        </div>

        <UDivider />

        <!-- Status -->
        <div class="space-y-4">
          <h3 class="text-sm font-medium text-slate-900 dark:text-white uppercase tracking-wide">
            Status
          </h3>
          
          <div class="flex items-center justify-between p-4 bg-slate-50 dark:bg-slate-800/50 rounded-lg">
            <div>
              <h4 class="font-medium text-slate-900 dark:text-white">Activate Carousel</h4>
              <p class="text-sm text-slate-600 dark:text-slate-400">
                {{ form.isActive ? 'Carousel will be visible to users immediately after creation.' : 'Carousel will be created but remain hidden until activated.' }}
              </p>
            </div>
            <UToggle v-model="form.isActive" size="lg" />
          </div>
        </div>

        <!-- Auto-population Notice -->
        <UAlert
          v-if="form.type === 'VIEW_ALL_PRODUCTS'"
          icon="i-lucide-info"
          color="blue"
          variant="soft"
          title="Auto-populated Carousel"
          description="This carousel will automatically display all active products from your store. You won't need to manually add items."
        />
      </div>
    </UCard>

    <!-- Next Steps Preview -->
    <UCard v-if="form.name && form.type && form.type !== 'VIEW_ALL_PRODUCTS'">
      <template #header>
        <div class="flex items-center gap-2">
          <UIcon name="i-lucide-route" class="w-5 h-5" />
          <h3 class="font-semibold">Next Steps</h3>
        </div>
      </template>
      <div class="space-y-3">
        <div class="flex items-center gap-3">
          <div class="w-6 h-6 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center">
            <span class="text-xs font-medium text-blue-600 dark:text-blue-400">1</span>
          </div>
          <span class="text-sm text-slate-600 dark:text-slate-400">
            Create the carousel configuration
          </span>
        </div>
        <div class="flex items-center gap-3">
          <div class="w-6 h-6 bg-slate-100 dark:bg-slate-800 rounded-full flex items-center justify-center">
            <span class="text-xs font-medium text-slate-500">2</span>
          </div>
          <span class="text-sm text-slate-600 dark:text-slate-400">
            Add content items to populate the carousel
          </span>
        </div>
        <div class="flex items-center gap-3">
          <div class="w-6 h-6 bg-slate-100 dark:bg-slate-800 rounded-full flex items-center justify-center">
            <span class="text-xs font-medium text-slate-500">3</span>
          </div>
          <span class="text-sm text-slate-600 dark:text-slate-400">
            Preview and activate your carousel
          </span>
        </div>
      </div>
    </UCard>
  </div>
</template>

<script setup lang="ts">
import { useEcommerceService } from "@/services/ecommerce.service";
import type { Carousel } from "~/types/ecommerce";

definePageMeta({
  requiresAuth: true,
  ssr: false,
});

const router = useRouter();
const toast = useToast();
const { tenant, fetchTenantContext } = useTenantContext();
const ecommerce = useEcommerceService();

// Form state
const form = reactive<Partial<Carousel>>({
  name: "",
  nameAr: "",
  slug: "",
  type: "",
  placement: "",
  platform: "WEB",
  maxItems: 10,
  isActive: true,
});

const errors = reactive<Record<string, string>>({});
const saving = ref(false);
const errorMessage = ref<string | null>(null);

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

const placementOptions = [
  { label: "Home Page", value: "HOME_PAGE" },
  { label: "Category Page", value: "CATEGORY_PAGE" },
  { label: "Product Page", value: "PRODUCT_PAGE" },
  { label: "Search Results", value: "SEARCH_RESULTS" },
  { label: "Checkout Page", value: "CHECKOUT_PAGE" },
  { label: "Header", value: "HEADER" },
  { label: "Footer", value: "FOOTER" },
  { label: "Sidebar", value: "SIDEBAR" },
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

const getTypeDescription = (type: string) => {
  const descriptions = {
    PRODUCT: "Display selected products with images, titles, and prices. Perfect for featuring bestsellers or new arrivals.",
    VIEW_ALL_PRODUCTS: "Automatically shows all active products from your store. No manual item management required.",
    IMAGE: "Showcase custom images with optional links and call-to-action buttons. Great for promotions and banners.",
    CATEGORY: "Highlight product categories to help customers navigate your store more easily.",
    BRAND: "Feature brand logos and information to build trust and showcase partnerships.",
    OFFER: "Promote special deals, discounts, and limited-time offers to drive sales.",
    TESTIMONIAL: "Display customer reviews and testimonials to build social proof.",
    BLOG: "Showcase blog posts and articles to engage customers with valuable content.",
    MIXED: "Combine different content types in a single carousel for maximum flexibility.",
  };
  return descriptions[type as keyof typeof descriptions] || "Custom carousel type.";
};

const generateSlug = () => {
  if (form.name && !form.slug) {
    form.slug = form.name
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  }
};

// Validation
const validate = () => {
  errors.name = form.name ? "" : "Name is required";
  errors.slug = form.slug ? "" : "Slug is required";
  errors.type = form.type ? "" : "Content type is required";
  errors.placement = form.placement ? "" : "Placement is required";
  
  // Validate slug format
  if (form.slug && !/^[a-z0-9-]+$/.test(form.slug)) {
    errors.slug = "Slug can only contain lowercase letters, numbers, and hyphens";
  }
  
  return Object.values(errors).every(error => !error);
};

// Actions
const save = async () => {
  if (!tenant.value?.id) return;
  if (!validate()) return;
  
  saving.value = true;
  try {
    const newCarousel = await ecommerce.createCarousel(tenant.value.id, form);
    toast.add({ 
      title: "Carousel created successfully", 
      description: `"${form.name}" is ready for content.`,
      color: "green" 
    });
    
    // Navigate to edit page if it's not auto-populated
    if (form.type === 'VIEW_ALL_PRODUCTS') {
      router.push("/ecommerce/carousels");
    } else {
      router.push(`/ecommerce/carousels/${newCarousel.id}`);
    }
  } catch (err: any) {
    toast.add({
      title: "Failed to create carousel",
      description: err?.data?.message || err?.message || "An error occurred while creating the carousel.",
      color: "red",
    });
  } finally {
    saving.value = false;
  }
};

// Lifecycle
onMounted(async () => {
  if (!tenant.value) {
    await fetchTenantContext().catch((err) => {
      console.error("[carousel new] tenant context error", err);
      errorMessage.value = "Failed to load tenant context";
    });
  }
});
</script>
