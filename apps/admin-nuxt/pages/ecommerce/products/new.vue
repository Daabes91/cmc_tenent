<template>
  <div class="space-y-6 max-w-5xl">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <div class="flex items-center gap-2 mb-2">
          <UButton to="/ecommerce/products" variant="ghost" icon="i-lucide-arrow-left" size="sm">
            Back to Products
          </UButton>
          <UBadge color="blue" variant="soft">New</UBadge>
        </div>
        <h1 class="text-3xl font-bold text-slate-900 dark:text-white">Create product</h1>
        <p class="text-sm text-slate-600 dark:text-slate-400">
          Add pricing, visibility, and descriptions.
        </p>
      </div>
      <div class="flex gap-2">
        <UButton variant="outline" color="gray" icon="i-lucide-x" @click="router.back()">Cancel</UButton>
        <UButton color="primary" icon="i-lucide-save" :loading="saving" @click="save">
          Save product
        </UButton>
      </div>
    </div>

    <UAlert
      v-if="errorMessage"
      icon="i-lucide-alert-triangle"
      color="red"
      :title="errorMessage"
      description="Please fix the errors and try again."
    />

    <UCard>
      <div class="grid gap-6 md:grid-cols-2">
        <UFormGroup label="Name" required :error="errors.name">
          <UInput v-model="form.name" placeholder="Product name" @input="generateSlug" />
        </UFormGroup>
        <UFormGroup label="Slug" required :error="errors.slug">
          <UInput v-model="form.slug" placeholder="product-slug" />
        </UFormGroup>
        <UFormGroup label="SKU" :error="errors.sku">
          <UInput v-model="form.sku" placeholder="SKU" />
        </UFormGroup>
        <UFormGroup label="Currency">
          <USelect v-model="form.currency" :options="currencyOptions" />
        </UFormGroup>
        <UFormGroup label="Price" :error="errors.price">
          <UInput v-model.number="form.price" type="number" min="0" step="0.01" />
        </UFormGroup>
        <UFormGroup label="Compare at price">
          <UInput v-model.number="form.compareAtPrice" type="number" min="0" step="0.01" />
        </UFormGroup>
        <UFormGroup label="Short description">
          <UInput v-model="form.shortDescription" placeholder="Shown in cards" />
        </UFormGroup>
        <UFormGroup label="Description">
          <UTextarea v-model="form.description" placeholder="Full description" :rows="4" />
        </UFormGroup>
      </div>

      <UDivider class="my-6" />

      <div class="grid gap-4 md:grid-cols-2">
        <UFormGroup label="Categories">
          <USelectMenu
            v-model="form.categoryIds"
            :options="categories.map(c => ({ label: c.name, value: c.id }))"
            value-attribute="value"
            option-attribute="label"
            multiple
            searchable
            placeholder="Select categories"
          />
        </UFormGroup>
      </div>

      <UDivider class="my-6" />

      <div class="grid gap-4 sm:grid-cols-3">
        <div class="flex items-center justify-between p-4 rounded-lg bg-slate-50 dark:bg-slate-800/60">
          <div>
            <p class="text-sm text-slate-500 dark:text-slate-400">Visible</p>
            <p class="font-medium text-slate-900 dark:text-white">
              {{ form.isVisible ? "Shown in store" : "Hidden" }}
            </p>
          </div>
          <UToggle v-model="form.isVisible" />
        </div>
        <div class="flex items-center justify-between p-4 rounded-lg bg-slate-50 dark:bg-slate-800/60">
          <div>
            <p class="text-sm text-slate-500 dark:text-slate-400">Taxable</p>
            <p class="font-medium text-slate-900 dark:text-white">
              {{ form.isTaxable ? "Yes" : "No" }}
            </p>
          </div>
          <UToggle v-model="form.isTaxable" />
        </div>
        <div class="p-4 rounded-lg bg-slate-50 dark:bg-slate-800/60 space-y-2">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-slate-500 dark:text-slate-400">Status</p>
              <p class="font-medium text-slate-900 dark:text-white">{{ form.status }}</p>
            </div>
            <UBadge :color="form.status === 'ACTIVE' ? 'green' : 'gray'" variant="soft">
              {{ form.status }}
            </UBadge>
          </div>
          <USelect
            v-model="form.status"
            :options="[
              { label: 'Active', value: 'ACTIVE' },
              { label: 'Draft', value: 'DRAFT' },
            ]"
          />
        </div>
      </div>

      <UDivider class="my-6" />

      <div class="space-y-4">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold">Images</h3>
            <p class="text-sm text-slate-500 dark:text-slate-400">Add product images (first main by default).</p>
          </div>
          <UButton size="xs" icon="i-lucide-plus" @click="imageInputs.push({ url: '', alt: '', isMain: imageInputs.length === 0 })">
            Add image
          </UButton>
        </div>
        <div class="space-y-3">
          <div
            v-for="(img, idx) in imageInputs"
            :key="idx"
            class="grid gap-3 md:grid-cols-3 p-4 rounded-lg border border-slate-200 dark:border-slate-700"
          >
            <div class="space-y-2">
              <ImageUpload
                v-model="img.url"
                :alt-text="img.alt"
                @upload-success="onUploadSuccess(idx, $event)"
                @upload-error="onUploadError"
              />
            </div>
            <UInput v-model="img.alt" placeholder="Alt text (optional)" />
            <div class="flex items-center gap-2">
              <UToggle v-model="img.isMain" @change="setMainImage(idx)" />
              <span class="text-sm">Main image</span>
              <UButton
                size="xs"
                variant="ghost"
                color="red"
                icon="i-lucide-trash"
                @click="imageInputs.splice(idx, 1)"
              />
            </div>
          </div>
        </div>
      </div>
    </UCard>
  </div>
</template>

<script setup lang="ts">
import { useEcommerceService } from "@/services/ecommerce.service";
import type { Category, Product } from "~/types/ecommerce";

definePageMeta({
  requiresAuth: true,
  ssr: false,
});

const router = useRouter();
const toast = useToast();
const { tenant, fetchTenantContext } = useTenantContext();
const ecommerce = useEcommerceService();

const form = reactive<Partial<Product>>({
  name: "",
  slug: "",
  sku: "",
  description: "",
  shortDescription: "",
  price: null,
  compareAtPrice: null,
  currency: "USD",
  status: "DRAFT",
  isTaxable: true,
  isVisible: true,
  categoryIds: [],
  images: [],
});

const errors = reactive<Record<string, string>>({});
const saving = ref(false);
const errorMessage = ref<string | null>(null);

const currencyOptions = ["USD", "EUR", "JOD", "SAR", "AED"];
const categories = ref<Category[]>([]);
const imageInputs = ref<Array<{ url: string; alt: string; isMain: boolean }>>([
  { url: "", alt: "", isMain: true },
]);

const setMainImage = (idx: number) => {
  imageInputs.value = imageInputs.value.map((img, i) => ({
    ...img,
    isMain: i === idx,
  }));
};

const onUploadSuccess = (idx: number, data: { publicUrl: string }) => {
  imageInputs.value[idx].url = data.publicUrl;
  // If no other main selected, make this main
  if (!imageInputs.value.some((img) => img.isMain)) {
    setMainImage(idx);
  }
  toast.add({ title: "Image uploaded", color: "green" });
};

const onUploadError = (err: string) => {
  toast.add({ title: "Upload failed", description: err, color: "red" });
};

const generateSlug = () => {
  if (!form.name) return;
  form.slug = form.name
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
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

const validate = () => {
  errors.name = form.name ? "" : "Name is required";
  errors.slug = form.slug ? "" : "Slug is required";
  errors.price = form.price !== null && form.price !== undefined ? "" : "Price is required";
  errors.sku = "";
  return Object.values(errors).every((v) => !v);
};

const save = async () => {
  const hasTenant = await ensureTenant();
  if (!hasTenant || !validate()) return;

  saving.value = true;
  errorMessage.value = null;
  try {
    const filledImages = imageInputs.value.filter((img) => img.url?.trim()).map((img) => img.url.trim());

    // Backend createProduct accepts only fields from ProductCreateRequest
    const created = await ecommerce.createProduct(tenant.value!.id, {
      name: form.name,
      slug: form.slug,
      sku: form.sku,
      description: form.description,
      shortDescription: form.shortDescription,
      price: form.price ?? undefined,
      compareAtPrice: form.compareAtPrice ?? undefined,
      currency: form.currency,
      isTaxable: form.isTaxable,
      isVisible: form.isVisible,
      images: filledImages,
    });

    toast.add({
      title: "Product created",
      description: `"${form.name}" has been saved.`,
      color: "green",
    });
    router.push(`/ecommerce/products/${created.id}`);
  } catch (err: any) {
    console.error("[product create] error", err);
    errorMessage.value = err?.data?.message || err?.message || "Failed to create product";
    toast.add({
      title: "Creation failed",
      description: errorMessage.value,
      color: "red",
    });
  } finally {
    saving.value = false;
  }
};

onMounted(async () => {
  await ensureTenant();
  if (tenant.value?.id) {
    categories.value = (await ecommerce.listCategories(tenant.value.id))?.content ?? [];
  }
});
</script>
