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
              <UIcon name="i-lucide-package" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">Product Catalog</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">Manage your insurance products and services</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="loading"
              @click="fetchProducts"
            >
              Refresh
            </UButton>
            <UButton 
              color="blue" 
              icon="i-lucide-plus" 
              to="/ecommerce/products/new"
            >
              Create Product
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
          description="Enable the e-commerce module for this tenant to manage products and services."
        />
      </div>

      <template v-else>
        <!-- Quick Stats -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
          <div class="bg-white dark:bg-slate-800 rounded-xl p-6 shadow-sm border border-blue-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
                <UIcon name="i-lucide-package" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Total Products</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ totalElements || 0 }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">All insurance products</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-xl p-6 shadow-sm border border-green-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
                <UIcon name="i-lucide-check-circle" class="h-5 w-5 text-green-600 dark:text-green-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Active Products</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ activeProducts }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">Available for purchase</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-xl p-6 shadow-sm border border-amber-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-900/20">
                <UIcon name="i-lucide-eye" class="h-5 w-5 text-amber-600 dark:text-amber-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Visible</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ visibleProducts }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">Shown in catalog</p>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-xl p-6 shadow-sm border border-indigo-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
            <div class="flex items-center gap-3 mb-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-50 dark:bg-indigo-900/20">
                <UIcon name="i-lucide-edit" class="h-5 w-5 text-indigo-600 dark:text-indigo-400" />
              </div>
              <div class="flex-1">
                <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Draft Products</p>
                <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ draftProducts }}</p>
              </div>
            </div>
            <p class="text-xs text-slate-600 dark:text-slate-300">In development</p>
          </div>
        </div>

        <!-- Search and Filters -->
        <div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-blue-200/60 dark:border-slate-700/60 overflow-hidden mb-6">
          <div class="bg-gradient-to-r from-blue-600 to-blue-800 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-filter" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">Product Management</h2>
                <p class="text-sm text-blue-100">Filter and manage your insurance product catalog</p>
              </div>
            </div>
          </div>
          <div class="p-6">
            <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <UFormGroup label="Search">
                <UInput
                  v-model="searchQuery"
                  size="lg"
                  placeholder="Search products..."
                  icon="i-lucide-search"
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
              <UFormGroup label="Visibility">
                <USelect
                  v-model="filters.visibility"
                  size="lg"
                  :options="visibilityFilterOptions"
                  value-attribute="value"
                  label-attribute="label"
                />
              </UFormGroup>
              <UFormGroup label="Category">
                <USelect
                  v-model="filters.category"
                  size="lg"
                  :options="categoryFilterOptions"
                  value-attribute="value"
                  label-attribute="label"
                />
              </UFormGroup>
            </div>
          </div>
        </div>

        <!-- Products Grid -->
        <div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-blue-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-blue-600 to-blue-800 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-package" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">Insurance Products</h2>
                <p class="text-sm text-blue-100">{{ totalElements }} product{{ totalElements !== 1 ? 's' : '' }} found</p>
              </div>
            </div>
          </div>

          <div v-if="loading" class="p-6">
            <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              <ProductCardSkeleton v-for="i in 6" :key="i" />
            </div>
          </div>

          <div v-else-if="errorMessage" class="p-12 text-center">
            <div class="flex flex-col items-center gap-4">
              <div class="h-16 w-16 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
                <UIcon name="i-lucide-alert-triangle" class="h-8 w-8 text-red-600 dark:text-red-400" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-slate-900 dark:text-white">Failed to load products</h3>
                <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">{{ errorMessage }}</p>
              </div>
              <UButton
                color="red"
                icon="i-lucide-refresh-cw"
                @click="fetchProducts"
              >
                Try Again
              </UButton>
            </div>
          </div>

          <div v-else-if="filteredProducts.length > 0">
            <div class="p-6">
              <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                <ProductCard
                  v-for="product in filteredProducts"
                  :key="product.id"
                  :product="product"
                  :categories="categories"
                  @edit="editProduct"
                  @duplicate="duplicateProduct"
                  @toggle-status="toggleProductStatus"
                  @toggle-visibility="toggleProductVisibility"
                  @delete="confirmDeleteProduct"
                />
              </div>
            </div>
            
            <!-- Pagination -->
            <ProductPagination
              v-if="totalPages > 1"
              :current-page="currentPage"
              :total-pages="totalPages"
              :total-elements="totalElements"
              :page-size="pageSize"
              @update:page="handlePageChange"
              @update:page-size="handlePageSizeChange"
            />
          </div>

          <div v-else class="p-12 text-center">
            <div class="flex flex-col items-center gap-4">
              <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                <UIcon name="i-lucide-package" class="h-8 w-8 text-slate-400" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-slate-900 dark:text-white">No products found</h3>
                <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                  {{ hasActiveFilters ? 'Try adjusting your search or filters' : 'Get started by creating your first insurance product.' }}
                </p>
              </div>
              <UButton
                v-if="!hasActiveFilters"
                color="blue"
                icon="i-lucide-plus"
                to="/ecommerce/products/new"
              >
                Create Your First Product
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
                <h3 class="text-lg font-semibold text-slate-900 dark:text-white">Delete Product</h3>
                <p class="text-sm text-slate-600 dark:text-slate-300">This action cannot be undone</p>
              </div>
            </div>
          </template>

          <div class="py-4">
            <p class="text-slate-700 dark:text-slate-300">
              Are you sure you want to delete "{{ productToDelete?.name }}"? This will permanently remove the product and all its associated data.
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
                @click="confirmDeleteProductAction"
              >
                Delete Product
              </UButton>
            </div>
          </template>
        </UCard>
      </UModal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useEcommerceService } from "~/services/ecommerce.service";
import type { Product, Category } from "~/types/ecommerce";
import ProductCard from "~/components/ecommerce/ProductCard.vue";
import ProductCardSkeleton from "~/components/ecommerce/ProductCardSkeleton.vue";
import ProductPagination from "~/components/ecommerce/ProductPagination.vue";

definePageMeta({
  title: 'Product Catalog',
  requiresAuth: true
});

const { tenant, fetchTenantContext } = useTenantContext();
const route = useRoute();
const router = useRouter();
const ecommerce = useEcommerceService();
const toast = useToast();

// State
const products = ref<Product[]>([]);
const categories = ref<Category[]>([]);
const loading = ref(true);
const searchQuery = ref('');
const showDeleteModal = ref(false);
const productToDelete = ref<Product | null>(null);
const deleting = ref(false);
const errorMessage = ref<string | null>(null);

// Pagination state
const currentPage = ref(0);
const pageSize = ref(12);
const totalElements = ref(0);
const totalPages = ref(0);

// Filters
const filters = reactive({
  status: "",
  visibility: "",
  category: "",
});

// Filter Options
const statusFilterOptions = computed(() => [
  { label: "All Statuses", value: "" },
  { label: "Active", value: "ACTIVE" },
  { label: "Draft", value: "DRAFT" },
]);

const visibilityFilterOptions = computed(() => [
  { label: "All Products", value: "" },
  { label: "Visible", value: "visible" },
  { label: "Hidden", value: "hidden" },
]);

const categoryFilterOptions = computed(() => [
  { label: "All Categories", value: "" },
  ...categories.value.map(category => ({
    label: category.name,
    value: category.id.toString()
  }))
]);

const isChildRoute = computed(() => {
  // Treat anything beyond the products list (detail/edit/new) as child route,
  // regardless of baseURL prefix (e.g., /admin-panel).
  const path = route.path || "";
  return path.includes("/ecommerce/products/") && path.split("/ecommerce/products/")[1]?.length > 0;
});

// Computed
const activeProducts = computed(() => products.value?.filter(p => p.status === 'ACTIVE').length || 0);
const visibleProducts = computed(() => products.value?.filter(p => p.isVisible).length || 0);
const draftProducts = computed(() => products.value?.filter(p => p.status === 'DRAFT').length || 0);

const hasActiveFilters = computed(() => {
  return searchQuery.value !== "" || 
         filters.status !== "" || 
         filters.visibility !== "" || 
         filters.category !== "";
});

// For display purposes, we'll show the current page products
// Filtering is now handled server-side via API parameters
const filteredProducts = computed(() => products.value || []);

const clearFilters = () => {
  searchQuery.value = '';
  filters.status = "";
  filters.visibility = "";
  filters.category = "";
  currentPage.value = 0; // Reset to first page
  fetchProducts();
};

// Methods
const fetchProducts = async () => {
  try {
    loading.value = true;
    errorMessage.value = null;
    
    if (!tenant.value?.id) {
      await fetchTenantContext();
    }
    
    if (!tenant.value?.id) {
      throw new Error('No tenant context available');
    }

    // Build API parameters
    const params: Record<string, any> = {
      page: currentPage.value,
      size: pageSize.value,
    };

    // Add search query
    if (searchQuery.value) {
      params.search = searchQuery.value;
    }

    // Add filters
    if (filters.status) {
      params.status = filters.status;
    }

    if (filters.visibility) {
      params.visible = filters.visibility === 'visible';
    }

    if (filters.category) {
      params.categoryId = parseInt(filters.category);
    }

    const response = await ecommerce.listProducts(tenant.value.id, params);

    // Handle both page-shaped and array-shaped responses
    if (Array.isArray(response)) {
      products.value = response;
      totalElements.value = response.length;
      totalPages.value = 1;
    } else {
      products.value = response?.content ?? [];
      totalElements.value = response?.totalElements ?? response?.numberOfElements ?? products.value.length;
      const size = response?.size ?? pageSize.value;
      const total = response?.totalElements ?? products.value.length;
      totalPages.value = response?.totalPages ?? (size ? Math.ceil(total / size) : 1);
    }

    // Load categories if not already loaded
    if (!categories.value.length) {
      const categoriesPage = await ecommerce.listCategories(tenant.value.id);
      categories.value = categoriesPage?.content ?? [];
    }
  } catch (error) {
    console.error('Failed to load products:', error);
    products.value = [];
    totalElements.value = 0;
    totalPages.value = 0;
    errorMessage.value = (error as any)?.data?.message || (error as any)?.message || "Failed to load products";
    toast.add({
      title: "Failed to load products",
      description: errorMessage.value,
      color: "red",
      icon: "i-lucide-alert-circle"
    });
  } finally {
    loading.value = false;
  }
};

const editProduct = (product: Product) => {
  router.push(`/ecommerce/products/${product.id}`);
};

const duplicateProduct = async (product: Product) => {
  if (!tenant.value?.id) return;
  
  try {
    const duplicatedData = {
      ...product,
      name: `${product.name} (Copy)`,
      slug: `${product.slug}-copy-${Date.now()}`,
      sku: product.sku ? `${product.sku}-copy` : undefined,
      status: 'DRAFT' as const,
      isVisible: false,
    };
    delete (duplicatedData as any).id;
    delete (duplicatedData as any).createdAt;
    delete (duplicatedData as any).updatedAt;
    delete (duplicatedData as any).images;
    
    await ecommerce.createProduct(tenant.value.id, duplicatedData);
    toast.add({
      title: "Product duplicated",
      description: `"${product.name}" has been duplicated successfully.`,
      color: "green",
    });
    await fetchProducts();
  } catch (err: any) {
    toast.add({
      title: "Failed to duplicate product",
      description: err?.data?.message || err?.message || "An error occurred",
      color: "red",
    });
  }
};

const toggleProductStatus = async (product: Product) => {
  if (!tenant.value?.id) return;
  
  try {
    const newStatus = product.status === 'ACTIVE' ? 'DRAFT' : 'ACTIVE';
    await ecommerce.updateProduct(tenant.value.id, product.id, {
      status: newStatus,
    });
    
    // Update local state
    const index = products.value.findIndex(p => p.id === product.id);
    if (index !== -1) {
      products.value[index].status = newStatus;
    }
    
    toast.add({
      title: `Product ${newStatus.toLowerCase()}`,
      description: `"${product.name}" is now ${newStatus.toLowerCase()}.`,
      color: "green",
    });
  } catch (err: any) {
    toast.add({
      title: "Failed to update product status",
      description: err?.data?.message || err?.message || "An error occurred",
      color: "red",
    });
  }
};

const toggleProductVisibility = async (product: Product) => {
  if (!tenant.value?.id) return;
  
  try {
    await ecommerce.updateProduct(tenant.value.id, product.id, {
      isVisible: !product.isVisible,
    });
    
    // Update local state
    const index = products.value.findIndex(p => p.id === product.id);
    if (index !== -1) {
      products.value[index].isVisible = !product.isVisible;
    }
    
    toast.add({
      title: `Product ${product.isVisible ? 'hidden' : 'shown'}`,
      description: `"${product.name}" is now ${product.isVisible ? 'hidden' : 'visible'}.`,
      color: "green",
    });
  } catch (err: any) {
    toast.add({
      title: "Failed to update product visibility",
      description: err?.data?.message || err?.message || "An error occurred",
      color: "red",
    });
  }
};

const confirmDeleteProduct = (product: Product) => {
  productToDelete.value = product;
  showDeleteModal.value = true;
};

const confirmDeleteProductAction = async () => {
  if (!productToDelete.value || !tenant.value?.id) return;

  try {
    deleting.value = true;
    await ecommerce.deleteProduct(tenant.value.id, productToDelete.value.id);
    products.value = products.value.filter(p => p.id !== productToDelete.value!.id);
    showDeleteModal.value = false;
    productToDelete.value = null;
    
    toast.add({
      title: "Product deleted",
      description: "The product has been deleted successfully.",
      color: "green",
      icon: "i-lucide-check-circle"
    });
  } catch (error) {
    console.error('Failed to delete product:', error);
    toast.add({
      title: "Failed to delete product",
      description: (error as any)?.data?.message || (error as any)?.message || "An error occurred",
      color: "red",
      icon: "i-lucide-alert-circle"
    });
  } finally {
    deleting.value = false;
  }
};

// Pagination handlers
const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchProducts();
};

const handlePageSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 0; // Reset to first page when changing page size
  fetchProducts();
};

// Watchers for filters
watch([searchQuery, () => filters.status, () => filters.visibility, () => filters.category], () => {
  currentPage.value = 0; // Reset to first page when filters change
  fetchProducts();
}, { deep: true });

// Load data on mount
onMounted(() => {
  fetchProducts();
});
</script>
