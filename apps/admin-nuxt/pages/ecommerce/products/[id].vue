<template>
  <ProductForm
    v-if="isEditRoute"
    :product-id="Number(route.params.id)"
    mode="edit"
  />
  <div v-else class="space-y-6 max-w-6xl">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <div class="flex items-center gap-2 mb-2">
          <UButton
            icon="i-lucide-arrow-left"
            variant="ghost"
            size="sm"
            to="/ecommerce/products"
          >
            Back to Products
          </UButton>
          <UBadge :color="product?.isVisible ? 'green' : 'gray'" variant="soft">
            {{ product?.isVisible ? "Visible" : "Hidden" }}
          </UBadge>
        </div>
        <h1 class="text-3xl font-bold text-slate-900 dark:text-white">
          {{ product?.name || "Product details" }}
        </h1>
        <p class="text-sm text-slate-600 dark:text-slate-400">
          Review pricing, status, and content.
        </p>
      </div>
      <div class="flex gap-2">
        <UButton
          variant="outline"
          color="gray"
          icon="i-lucide-refresh-ccw"
          :loading="loading"
          @click="loadProduct"
        >
          Refresh
        </UButton>
        <UButton
          color="primary"
          icon="i-lucide-pencil"
          :to="`/ecommerce/products/${route.params.id}/edit`"
        >
          Edit product
        </UButton>
        <UButton
          color="green"
          icon="i-lucide-check-circle"
          :loading="activating"
          @click="activateProduct"
          v-if="product?.status !== 'ACTIVE'"
        >
          Activate
        </UButton>
      </div>
    </div>

    <UAlert
      v-if="errorMessage"
      icon="i-lucide-alert-triangle"
      color="red"
      :title="errorMessage"
      description="Try again or check product availability."
    />

    <div v-if="loading" class="space-y-4">
      <USkeleton class="h-24 w-full" />
      <USkeleton class="h-64 w-full" />
    </div>

    <div v-else class="grid gap-6 lg:grid-cols-3">
      <div class="space-y-4 lg:col-span-2">
        <UCard>
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-info" class="h-4 w-4 text-slate-500" />
              <h3 class="text-lg font-semibold">Overview</h3>
            </div>
          </template>

          <div class="grid gap-4 sm:grid-cols-2">
            <div>
              <p class="text-sm text-slate-500 dark:text-slate-400">Name</p>
              <p class="font-medium text-slate-900 dark:text-white">{{ product?.name }}</p>
            </div>
            <div>
              <p class="text-sm text-slate-500 dark:text-slate-400">SKU</p>
              <p class="font-medium text-slate-900 dark:text-white">{{ product?.sku || "—" }}</p>
            </div>
            <div>
              <p class="text-sm text-slate-500 dark:text-slate-400">Status</p>
              <UBadge :color="statusColor" variant="soft">
                {{ product?.status || "Unknown" }}
              </UBadge>
            </div>
            <div>
              <p class="text-sm text-slate-500 dark:text-slate-400">Currency</p>
              <p class="font-medium text-slate-900 dark:text-white">{{ product?.currency || "—" }}</p>
            </div>
            <div>
              <p class="text-sm text-slate-500 dark:text-slate-400">Price</p>
              <p class="font-semibold text-slate-900 dark:text-white">
                {{ formatMoney(product?.price) }}
              </p>
            </div>
            <div>
              <p class="text-sm text-slate-500 dark:text-slate-400">Compare at</p>
              <p class="font-semibold text-slate-900 dark:text-white">
                {{ formatMoney(product?.compareAtPrice) }}
              </p>
            </div>
            <div>
              <p class="text-sm text-slate-500 dark:text-slate-400">Taxable</p>
              <p class="font-medium text-slate-900 dark:text-white">
                {{ product?.isTaxable ? "Yes" : "No" }}
              </p>
            </div>
            <div>
              <p class="text-sm text-slate-500 dark:text-slate-400">Updated</p>
              <p class="font-medium text-slate-900 dark:text-white">
                {{ formatDate(product?.updatedAt) }}
              </p>
            </div>
          </div>
        </UCard>

        <UCard>
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-file-text" class="h-4 w-4 text-slate-500" />
              <h3 class="text-lg font-semibold">Descriptions</h3>
            </div>
          </template>
          <div class="space-y-3">
            <div>
              <p class="text-sm text-slate-500 dark:text-slate-400">Short description</p>
              <p class="text-sm text-slate-900 dark:text-white">
                {{ product?.shortDescription || "Not provided" }}
              </p>
            </div>
            <UDivider />
            <div>
              <p class="text-sm text-slate-500 dark:text-slate-400">Description</p>
              <p class="text-sm leading-relaxed text-slate-900 dark:text-white whitespace-pre-line">
                {{ product?.description || "Not provided" }}
              </p>
            </div>
          </div>
        </UCard>

        <UCard>
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-images" class="h-4 w-4 text-slate-500" />
              <h3 class="text-lg font-semibold">Images</h3>
            </div>
          </template>
          <div class="space-y-4">
            <div class="grid gap-3 sm:grid-cols-2">
              <div
                v-for="img in images"
                :key="img.id"
                class="border rounded-lg p-3 flex gap-3 items-start dark:border-slate-700"
              >
                <img :src="img.imageUrl" class="h-16 w-16 rounded object-cover border" :alt="img.altText || 'image'" />
                <div class="flex-1">
                  <div class="flex items-center gap-2">
                    <p class="font-medium text-sm truncate">{{ img.altText || 'Image' }}</p>
                    <UBadge v-if="img.isMain" color="green" size="xs">Main</UBadge>
                  </div>
                  <p class="text-xs text-slate-500 break-all">{{ img.imageUrl }}</p>
                  <div class="flex gap-2 mt-2">
                    <UButton size="2xs" variant="ghost" icon="i-lucide-star" @click="markMainImage(img.id)">
                      Set main
                    </UButton>
                    <UButton size="2xs" variant="ghost" color="red" icon="i-lucide-trash" @click="removeImage(img.id)">
                      Remove
                    </UButton>
                  </div>
                </div>
              </div>
              <div v-if="!images.length" class="text-sm text-slate-500">No images yet.</div>
            </div>
            <UDivider />
            <div class="space-y-3">
              <ImageUpload
                v-model="newImage.url"
                :alt-text="newImage.alt"
                @upload-success="onDetailUploadSuccess"
                @upload-error="onDetailUploadError"
              />
              <div class="grid gap-3 sm:grid-cols-3 items-center">
                <UInput v-model="newImage.alt" placeholder="Alt text" icon="i-lucide-type" />
                <div class="flex items-center gap-2">
                  <UToggle v-model="newImage.isMain" />
                  <span class="text-sm">Main</span>
                </div>
                <div class="flex justify-end">
                  <UButton :loading="addingImage" icon="i-lucide-plus" @click="addImage">
                    Add image
                  </UButton>
                </div>
              </div>
            </div>
          </div>
        </UCard>
      </div>

      <div class="space-y-4">

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useEcommerceService } from "@/services/ecommerce.service";
import type { Product, ProductImage } from "~/types/ecommerce";
import ProductForm from "./new.vue";

definePageMeta({
  requiresAuth: true,
  ssr: false,
});

const route = useRoute();
const isEditRoute = computed(() => route.path.endsWith("/edit"));
const toast = useToast();
const { tenant, fetchTenantContext } = useTenantContext();
const ecommerce = useEcommerceService();

const product = ref<Product | null>(null);
const loading = ref(true);
const errorMessage = ref<string | null>(null);

const images = ref<ProductImage[]>([]);
const addingImage = ref(false);
const activating = ref(false);
const newImage = reactive({ url: "", alt: "", isMain: false });

const statusColor = computed(() => {
  switch (product.value?.status) {
    case "ACTIVE": return "green";
    case "DRAFT": return "yellow";
    default: return "gray";
  }
});

const formatMoney = (value?: number | null) => {
  if (value === null || value === undefined) return "—";
  return new Intl.NumberFormat("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
};

const formatDate = (value?: string | null) => {
  if (!value) return "—";
  return new Date(value).toLocaleString();
};

const ensureTenant = async () => {
  if (tenant.value?.id) return true;
  try {
    await fetchTenantContext();
    return Boolean(tenant.value?.id);
  } catch (err) {
    errorMessage.value = "Failed to load tenant context";
    return false;
  }
};

const loadProduct = async () => {
  loading.value = true;
  errorMessage.value = null;

  const hasTenant = await ensureTenant();
  if (!hasTenant) {
    loading.value = false;
    return;
  }

  try {
    console.log("[product detail] loading product", route.params.id);
    const id = Number(route.params.id);
    product.value = await ecommerce.getProduct(tenant.value!.id, id);
    images.value = await ecommerce.listProductImages(tenant.value!.id, id);
  } catch (err: any) {
    console.error("[product detail] load error", err);
    errorMessage.value = err?.data?.message || err?.message || "Failed to load product";
    toast.add({
      title: "Unable to load product",
      description: errorMessage.value,
      color: "red",
    });
  } finally {
    loading.value = false;
  }
};

onMounted(loadProduct);

watch(
  () => tenant.value?.id,
  (id) => {
    if (id) loadProduct();
  }
);

const addImage = async () => {
  if (!tenant.value?.id || !product.value?.id || !newImage.url.trim()) return;
  addingImage.value = true;
  try {
    const created = await ecommerce.addProductImage(tenant.value.id, product.value.id, {
      imageUrl: newImage.url,
      altText: newImage.alt,
      isMain: newImage.isMain,
      sortOrder: (images.value?.length || 0) + 1,
    });
    images.value.push(created);
    newImage.url = "";
    newImage.alt = "";
    newImage.isMain = false;
    toast.add({ title: "Image added", color: "green" });
  } catch (err: any) {
    toast.add({ title: "Failed to add image", description: err?.data?.message || err?.message, color: "red" });
  } finally {
    addingImage.value = false;
  }
};

const activateProduct = async () => {
  if (!tenant.value?.id || !product.value?.id) return;
  activating.value = true;
  try {
    const updated = await ecommerce.updateProduct(tenant.value.id, product.value.id, {
      status: "ACTIVE",
    });
    product.value = updated;
    toast.add({ title: "Product activated", color: "green" });
  } catch (err: any) {
    toast.add({
      title: "Failed to activate",
      description: err?.data?.message || err?.message,
      color: "red",
    });
  } finally {
    activating.value = false;
  }
};
const markMainImage = async (imageId: number) => {
  if (!tenant.value?.id || !product.value?.id) return;
  await ecommerce.setMainProductImage(tenant.value.id, product.value.id, imageId);
  images.value = images.value.map((img) => ({ ...img, isMain: img.id === imageId }));
};

const removeImage = async (imageId: number) => {
  if (!tenant.value?.id || !product.value?.id) return;
  try {
    await ecommerce.deleteProductImage(tenant.value.id, product.value.id, imageId);
    images.value = images.value.filter((img) => img.id !== imageId);
    toast.add({ title: "Image removed", color: "green" });
  } catch (err: any) {
    toast.add({ title: "Failed to remove image", description: err?.data?.message || err?.message, color: "red" });
  }
};

const onDetailUploadSuccess = (data: { publicUrl: string }) => {
  newImage.url = data.publicUrl;
  toast.add({ title: "Upload complete", color: "green" });
};

const onDetailUploadError = (err: string) => {
  toast.add({ title: "Upload failed", description: err, color: "red" });
};
</script>
