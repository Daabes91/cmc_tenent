<template>
  <div class="space-y-5">
    <!-- Child routes (detail/new) render here -->
    <NuxtPage v-if="isChildRoute" />

    <!-- List view -->
    <div v-else class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p class="text-xs uppercase tracking-wide text-primary-600 dark:text-primary-300">E-commerce</p>
        <h1 class="text-2xl font-bold text-slate-900 dark:text-white">Products</h1>
        <p class="text-sm text-slate-500 dark:text-slate-400">Create, edit, and publish products.</p>
      </div>
      <div class="flex gap-2">
        <UButton icon="i-lucide-plus" color="emerald" to="/ecommerce/products/new">
          New product
        </UButton>
        <UButton icon="i-lucide-refresh-ccw" variant="ghost" @click="fetchProducts">Refresh</UButton>
      </div>
    </div>

    <UAlert
      v-if="!tenant?.ecommerceEnabled"
      icon="i-lucide-eye-off"
      color="amber"
      title="E-commerce disabled"
      description="Enable e-commerce for this tenant to manage products."
    />

    <UAlert
      v-else-if="errorMessage"
      icon="i-lucide-alert-triangle"
      color="red"
      :title="errorMessage"
      description="Please retry or adjust filters."
    />

    <UCard v-if="!isChildRoute">
      <div class="grid gap-4 md:grid-cols-3">
        <UFormGroup label="Search">
          <UInput v-model="filters.search" placeholder="Name or SKU" clearable />
        </UFormGroup>
        <UFormGroup label="Status">
          <USelect v-model="filters.status" :options="['ACTIVE', 'DRAFT']" />
        </UFormGroup>
        <UFormGroup label="Visibility">
          <USelect
            v-model="filters.visibility"
            :options="[
              { label: 'All', value: 'all' },
              { label: 'Visible', value: 'visible' },
              { label: 'Hidden', value: 'hidden' },
            ]"
          />
        </UFormGroup>
      </div>
    </UCard>

    <UCard v-if="!isChildRoute">
      <UTable
        :rows="products"
        :columns="columns"
        :loading="loading"
        :empty-state="{ icon: 'i-lucide-package', label: 'No products' }"
      >
        <template #thumbnail-data="{ row }">
          <div class="flex items-center gap-2">
            <img
              v-if="row.images?.length"
              :src="row.images[0]?.imageUrl"
              class="h-10 w-10 rounded border object-cover"
              alt="thumb"
            />
            <div class="flex flex-col">
              <span class="font-medium">{{ row.name }}</span>
              <span class="text-xs text-slate-500">#{{ row.id }}</span>
            </div>
          </div>
        </template>
        <template #currency-data="{ row }">
          <span>{{ row.currency || '—' }}</span>
        </template>
        <template #price-data="{ row }">
          <span>{{ row.price ?? '—' }}</span>
        </template>
        <template #status-data="{ row }">
          <UBadge :color="row.isVisible ? 'green' : 'gray'">
            {{ row.status || 'DRAFT' }}
          </UBadge>
        </template>
        <template #isVisible-data="{ row }">
          <UBadge :color="row.isVisible ? 'green' : 'gray'">
            {{ row.isVisible ? 'Visible' : 'Hidden' }}
          </UBadge>
        </template>
        <template #actions-data="{ row }">
          <UButton
            size="xs"
            variant="ghost"
            icon="i-lucide-pencil"
            :to="`/ecommerce/products/${row.id}`"
          >
            Edit
          </UButton>
        </template>
      </UTable>
    </UCard>
  </div>
</template>

<script setup lang="ts">
import { useEcommerceService } from "@/services/ecommerce.service";
import { onMounted, reactive, ref, watch } from "vue";
import type { Product } from "~/types/ecommerce";

const { tenant, fetchTenantContext } = useTenantContext();
const route = useRoute();
const ecommerce = useEcommerceService();

definePageMeta({
  requiresAuth: true,
  ssr: false,
});

const products = ref<Product[]>([]);
const loading = ref(false);
const errorMessage = ref<string | null>(null);
const filters = reactive({
  search: "",
  status: "ACTIVE",
  visibility: "all",
});

const columns = [
  { key: "thumbnail", label: "Product" },
  { key: "sku", label: "SKU" },
  { key: "price", label: "Price" },
  { key: "currency", label: "Currency" },
  { key: "status", label: "Status" },
  { key: "actions", label: "" },
];

const isChildRoute = computed(() => Boolean(route.params.id) || route.path.endsWith("/new"));

const fetchProducts = async () => {
  if (!tenant.value?.id) return;
  loading.value = true;
  errorMessage.value = null;
  try {
    const page = await ecommerce.listProducts(tenant.value.id, {
      size: 50,
      search: filters.search,
      status: filters.status,
      visible: filters.visibility === "all" ? undefined : filters.visibility === "visible",
    });
    products.value = page?.items ?? page?.content ?? [];
  } catch (err) {
    console.error("Failed to load products", err);
    errorMessage.value = (err as any)?.message || "Failed to load products";
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  if (!tenant.value) {
    await fetchTenantContext();
  }
  if (tenant.value?.ecommerceEnabled) {
    await fetchProducts();
  }
});

watch(
  () => ({ ...filters, tenant: tenant.value?.id }),
  () => fetchProducts(),
  { deep: true }
);
</script>
