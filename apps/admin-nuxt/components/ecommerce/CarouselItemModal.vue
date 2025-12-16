<template>
  <UModal v-model="isOpen" :ui="{ width: 'sm:max-w-2xl' }">
    <UCard>
      <template #header>
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold">
            {{ isEditing ? 'Edit Carousel Item' : 'Add Carousel Item' }}
          </h3>
          <UButton
            color="gray"
            variant="ghost"
            icon="i-lucide-x"
            @click="close"
          />
        </div>
      </template>

      <div class="space-y-6">
        <!-- Content Type Selection -->
        <UFormGroup label="Content Type" required :error="errors.contentType">
          <USelectMenu
            v-model="form.contentType"
            :options="contentTypeOptions"
            placeholder="Select content type"
            @update:model-value="onContentTypeChange"
          >
            <template #label>
              <span v-if="form.contentType" class="flex items-center gap-2">
                <UIcon :name="getContentTypeIcon(form.contentType)" class="w-4 h-4" />
                {{ getContentTypeLabel(form.contentType) }}
              </span>
              <span v-else>Select content type</span>
            </template>
          </USelectMenu>
        </UFormGroup>

        <!-- Basic Information -->
        <div class="grid gap-4 sm:grid-cols-2">
          <UFormGroup label="Title" required :error="errors.title">
            <UInput
              v-model="form.title"
              placeholder="Enter item title"
              icon="i-lucide-type"
            />
          </UFormGroup>
          <UFormGroup label="Title (Arabic)">
            <UInput
              v-model="form.titleAr"
              dir="rtl"
              placeholder="العنوان بالعربية"
              icon="i-lucide-type"
            />
          </UFormGroup>
          <UFormGroup label="Subtitle">
            <UInput
              v-model="form.subtitle"
              placeholder="Optional subtitle"
              icon="i-lucide-align-left"
            />
          </UFormGroup>
          <UFormGroup label="Subtitle (Arabic)">
            <UInput
              v-model="form.subtitleAr"
              dir="rtl"
              placeholder="وصف مختصر"
              icon="i-lucide-align-left"
            />
          </UFormGroup>
        </div>

        <!-- Product Selection (for PRODUCT type) -->
        <UFormGroup 
          v-if="form.contentType === 'PRODUCT'" 
          label="Product" 
          required 
          :error="errors.productId"
        >
          <USelectMenu
            v-model="form.productId"
            :options="productOptions"
            :loading="loadingProducts"
            placeholder="Search and select a product"
            searchable
            @query-change="searchProducts"
          >
            <template #label>
              <span v-if="selectedProduct">
                {{ selectedProduct.name }}
              </span>
              <span v-else>Select a product</span>
            </template>
          </USelectMenu>
        </UFormGroup>

        <!-- Category Selection (for CATEGORY type) -->
        <UFormGroup 
          v-if="form.contentType === 'CATEGORY'" 
          label="Category" 
          required 
          :error="errors.categoryId"
        >
          <USelectMenu
            v-model="form.categoryId"
            :options="categoryOptions"
            :loading="loadingCategories"
            placeholder="Select a category"
            searchable
          >
            <template #label>
              <span v-if="selectedCategory">
                {{ selectedCategory.name }}
              </span>
              <span v-else>Select a category</span>
            </template>
          </USelectMenu>
        </UFormGroup>

        <!-- Image Upload (for IMAGE type or custom image) -->
        <UFormGroup 
          v-if="form.contentType === 'IMAGE' || showCustomImage" 
          label="Image" 
          :required="form.contentType === 'IMAGE'"
          :error="errors.imageUrl"
        >
          <div class="space-y-3">
            <UInput
              v-model="form.imageUrl"
              placeholder="Enter image URL or upload"
              icon="i-lucide-image"
            />
            <!-- TODO: Add file upload component -->
            <div v-if="form.imageUrl" class="flex items-center gap-3">
              <img 
                :src="form.imageUrl" 
                alt="Preview"
                class="w-16 h-16 object-cover rounded-lg border border-slate-200 dark:border-slate-700"
              />
              <UButton
                size="xs"
                variant="ghost"
                color="red"
                icon="i-lucide-x"
                @click="form.imageUrl = ''"
              >
                Remove
              </UButton>
            </div>
          </div>
        </UFormGroup>

        <!-- Link and CTA -->
        <div class="grid gap-4 sm:grid-cols-2">
          <UFormGroup label="Link URL">
            <UInput
              v-model="form.linkUrl"
              placeholder="https://example.com"
              icon="i-lucide-external-link"
            />
          </UFormGroup>
          <UFormGroup label="CTA Text">
            <UInput
              v-model="form.ctaText"
              placeholder="e.g. Shop Now, Learn More"
              icon="i-lucide-mouse-pointer-click"
            />
          </UFormGroup>
          <UFormGroup label="CTA Text (Arabic)">
            <UInput
              v-model="form.ctaTextAr"
              dir="rtl"
              placeholder="نص الدعوة للإجراء"
              icon="i-lucide-mouse-pointer-click"
            />
          </UFormGroup>
        </div>

        <!-- Sort Order and Status -->
        <div class="grid gap-4 sm:grid-cols-2">
          <UFormGroup label="Sort Order" description="Lower numbers appear first">
            <UInput
              v-model.number="form.sortOrder"
              type="number"
              min="0"
              placeholder="0"
              icon="i-lucide-arrow-up-down"
            />
          </UFormGroup>
          <UFormGroup label="Status">
            <div class="flex items-center justify-between p-3 bg-slate-50 dark:bg-slate-800/50 rounded-lg">
              <div>
                <span class="font-medium text-slate-900 dark:text-white">Active</span>
                <p class="text-sm text-slate-600 dark:text-slate-400">
                  {{ form.isActive ? 'Item is visible in carousel' : 'Item is hidden from carousel' }}
                </p>
              </div>
              <UToggle v-model="form.isActive" />
            </div>
          </UFormGroup>
        </div>

        <!-- Custom Image Toggle -->
        <div v-if="form.contentType === 'PRODUCT' || form.contentType === 'CATEGORY'">
          <UCheckbox 
            v-model="showCustomImage" 
            label="Use custom image instead of default"
          />
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-3">
          <UButton variant="ghost" @click="close">Cancel</UButton>
          <UButton 
            color="primary" 
            :loading="saving" 
            @click="save"
          >
            {{ isEditing ? 'Update Item' : 'Add Item' }}
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import { useEcommerceService } from "@/services/ecommerce.service";
import type { CarouselItem, Product } from "~/types/ecommerce";

interface Props {
  modelValue: boolean;
  carouselId: number;
  carouselType?: string;
  item?: CarouselItem | null;
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void;
  (e: 'saved'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const { tenant } = useTenantContext();
const ecommerce = useEcommerceService();
const toast = useToast();

// State
const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
});

const isEditing = computed(() => !!props.item);

const form = reactive<Partial<CarouselItem>>({
  contentType: 'IMAGE',
  title: '',
  titleAr: '',
  subtitle: '',
  subtitleAr: '',
  imageUrl: '',
  linkUrl: '',
  ctaText: '',
  ctaTextAr: '',
  sortOrder: 0,
  isActive: true,
});

const errors = reactive<Record<string, string>>({});
const saving = ref(false);
const loadingProducts = ref(false);
const loadingCategories = ref(false);
const showCustomImage = ref(false);

// Options
const productOptions = ref<Array<{ label: string; value: number }>>([]);
const categoryOptions = ref<Array<{ label: string; value: number }>>([]);
const selectedProduct = ref<Product | null>(null);
const selectedCategory = ref<any>(null);

const contentTypeOptions = [
  { label: "Image", value: "IMAGE", icon: "i-lucide-image" },
  { label: "Product", value: "PRODUCT", icon: "i-lucide-package" },
  { label: "Category", value: "CATEGORY", icon: "i-lucide-folder" },
  { label: "Brand", value: "BRAND", icon: "i-lucide-award" },
  { label: "Offer", value: "OFFER", icon: "i-lucide-percent" },
];

// Helper functions
const getContentTypeIcon = (type: string) => {
  const option = contentTypeOptions.find(opt => opt.value === type);
  return option?.icon || "i-lucide-circle";
};

const getContentTypeLabel = (type: string) => {
  const option = contentTypeOptions.find(opt => opt.value === type);
  return option?.label || type;
};

// API functions
const searchProducts = async (query: string) => {
  if (!tenant.value?.id || !query) return;
  
  loadingProducts.value = true;
  try {
    const page = await ecommerce.listProducts(tenant.value.id, {
      search: query,
      size: 20,
    });
    
    productOptions.value = (page?.content || []).map(product => ({
      label: product.name,
      value: product.id,
    }));
  } catch (err) {
    console.error("Failed to search products", err);
  } finally {
    loadingProducts.value = false;
  }
};

const loadCategories = async () => {
  if (!tenant.value?.id) return;
  
  loadingCategories.value = true;
  try {
    // TODO: Implement category listing
    categoryOptions.value = [];
  } catch (err) {
    console.error("Failed to load categories", err);
  } finally {
    loadingCategories.value = false;
  }
};

// Event handlers
const onContentTypeChange = (type: string) => {
  // Reset type-specific fields
  form.productId = undefined;
  form.categoryId = undefined;
  
  if (type === 'PRODUCT') {
    searchProducts('');
  } else if (type === 'CATEGORY') {
    loadCategories();
  }
};

// Validation
const validate = () => {
  errors.contentType = form.contentType ? "" : "Content type is required";
  errors.title = form.title ? "" : "Title is required";
  
  if (form.contentType === 'PRODUCT') {
    errors.productId = form.productId ? "" : "Product is required";
  }
  
  if (form.contentType === 'CATEGORY') {
    errors.categoryId = form.categoryId ? "" : "Category is required";
  }
  
  if (form.contentType === 'IMAGE') {
    errors.imageUrl = form.imageUrl ? "" : "Image is required";
  }
  
  return Object.values(errors).every(error => !error);
};

// Actions
const save = async () => {
  if (!tenant.value?.id || !validate()) return;
  
  saving.value = true;
  try {
    if (isEditing.value && props.item) {
      await ecommerce.updateCarouselItem(tenant.value.id, props.carouselId, props.item.id, form);
      toast.add({
        title: "Item updated",
        description: "Carousel item has been updated successfully.",
        color: "green",
      });
    } else {
      await ecommerce.addCarouselItem(tenant.value.id, props.carouselId, form);
      toast.add({
        title: "Item added",
        description: "New item has been added to the carousel.",
        color: "green",
      });
    }
    
    emit('saved');
  } catch (err: any) {
    toast.add({
      title: `Failed to ${isEditing.value ? 'update' : 'add'} item`,
      description: err?.data?.message || err?.message,
      color: "red",
    });
  } finally {
    saving.value = false;
  }
};

const close = () => {
  isOpen.value = false;
  resetForm();
};

const resetForm = () => {
  Object.assign(form, {
    contentType: 'IMAGE',
    title: '',
    titleAr: '',
    subtitle: '',
    subtitleAr: '',
    imageUrl: '',
    linkUrl: '',
    ctaText: '',
    ctaTextAr: '',
    sortOrder: 0,
    isActive: true,
  });
  
  Object.keys(errors).forEach(key => {
    errors[key] = '';
  });
  
  showCustomImage.value = false;
};

// Watchers
watch(() => props.item, (item) => {
  if (item) {
    Object.assign(form, item);
    showCustomImage.value = !!item.imageUrl && (item.contentType === 'PRODUCT' || item.contentType === 'CATEGORY');
  } else {
    resetForm();
  }
}, { immediate: true });

watch(() => props.modelValue, (isOpen) => {
  if (!isOpen) {
    resetForm();
  }
});
</script>
