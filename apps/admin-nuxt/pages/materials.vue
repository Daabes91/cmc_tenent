<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-package" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('materials.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('materials.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="loading"
              @click="fetchMaterials"
            >
              {{ t('materials.actions.refresh') }}
            </UButton>
            <UButton 
              color="blue" 
              icon="i-lucide-plus" 
              @click="openCreateModal"
            >
              {{ t('materials.actions.add') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">
      <!-- Quick Stats -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
              <UIcon name="i-lucide-package" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('materials.metrics.total.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ materials?.length || 0 }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('materials.metrics.total.description') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
              <UIcon name="i-lucide-check-circle" class="h-5 w-5 text-green-600 dark:text-green-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('materials.metrics.active.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ activeCount }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('materials.metrics.active.description') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-red-50 dark:bg-red-900/20">
              <UIcon name="i-lucide-x-circle" class="h-5 w-5 text-red-600 dark:text-red-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('materials.metrics.inactive.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ materials?.length - activeCount || 0 }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('materials.metrics.inactive.description') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-50 dark:bg-purple-900/20">
              <UIcon name="i-lucide-wallet" class="h-5 w-5 text-purple-600 dark:text-purple-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('materials.metrics.averageCost.label') }}</p>
              <DualCurrencyDisplay
                :converted-amount="averageCosts.converted"
                :converted-currency="averageCosts.convertedCurrency"
                :original-amount="averageCosts.original"
                :original-currency="averageCosts.originalCurrency"
                size="lg"
                class="text-lg font-semibold"
              />
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('materials.metrics.averageCost.description') }}</p>
        </div>
      </div>

      <!-- Search and Filters -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden mb-6">
        <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-search" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('materials.search.title') }}</h2>
              <p class="text-sm text-slate-300">{{ t('materials.search.subtitle') }}</p>
            </div>
          </div>
        </div>
        <div class="p-6">
          <div class="flex flex-col sm:flex-row gap-4">
            <div class="flex-1">
              <UFormGroup :label="t('materials.search.label')">
                <UInput
                  v-model="searchQuery"
                  size="lg"
                  :placeholder="t('materials.search.placeholder')"
                  icon="i-lucide-search"
                />
              </UFormGroup>
            </div>
            <div class="sm:w-48">
              <UFormGroup :label="t('materials.search.statusFilter')">
                <USelect
                  v-model="activeFilter"
                  size="lg"
                  :options="statusOptions"
                  value-attribute="value"
                  label-attribute="label"
                />
              </UFormGroup>
            </div>
          </div>
        </div>
      </div>

      <!-- Materials Grid -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-package" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('materials.list.title') }}</h2>
              <p class="text-sm text-blue-100">{{ t('materials.list.count', { count: filteredMaterials.length }) }}</p>
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

        <div v-else-if="filteredMaterials.length > 0" class="p-6">
          <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            <div
              v-for="material in filteredMaterials"
              :key="material.id"
              class="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-6 border border-slate-200 dark:border-slate-600 hover:shadow-md hover:border-blue-300 dark:hover:border-blue-600 transition-all duration-200 group"
            >
              <!-- Material Header -->
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center gap-3">
                  <div class="relative">
                    <div class="h-12 w-12 rounded-xl bg-gradient-to-br from-blue-100 to-indigo-100 dark:from-blue-900/30 dark:to-indigo-900/30 flex items-center justify-center">
                      <UIcon name="i-lucide-package" class="h-6 w-6 text-blue-600 dark:text-blue-400" />
                    </div>
                    <div
                      :class="[
                        'absolute -top-1 -right-1 w-4 h-4 rounded-full border-2 border-white dark:border-slate-700',
                        material.active ? 'bg-green-500' : 'bg-red-500'
                      ]"
                    ></div>
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="font-semibold text-slate-900 dark:text-white truncate">
                      {{ material.name }}
                    </h3>
                    <p v-if="material.unitOfMeasure" class="text-sm text-slate-500 dark:text-slate-400 truncate">
                      {{ t('materials.card.per', { unit: material.unitOfMeasure }) }}
                    </p>
                  </div>
                </div>
                <UBadge
                  :color="material.active ? 'green' : 'red'"
                  variant="soft"
                  size="sm"
                >
                  {{ material.active ? t('materials.status.active') : t('materials.status.inactive') }}
                </UBadge>
              </div>

              <!-- Material Details -->
              <div class="space-y-3 mb-4">
                <div class="flex items-start gap-3 text-sm text-slate-600 dark:text-slate-300">
                  <UIcon name="i-lucide-wallet" class="h-4 w-4 text-slate-400 mt-0.5" />
                  <DualCurrencyDisplay
                    :converted-amount="material.convertedPrice"
                    :converted-currency="material.convertedCurrency"
                    :original-amount="material.unitCost"
                    :original-currency="material.currency"
                    size="md"
                    :show-clinic-badge="true"
                  />
                </div>
                <div v-if="material.description" class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                  <UIcon name="i-lucide-file-text" class="h-4 w-4 text-slate-400" />
                  <span class="truncate">{{ material.description }}</span>
                </div>
                <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                  <UIcon name="i-lucide-calendar" class="h-4 w-4 text-slate-400" />
                  <span>{{ t('materials.card.updated', { date: formatDate(material.updatedAt) }) }}</span>
                </div>
              </div>

              <!-- Actions -->
              <div class="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-slate-600">
                <div class="text-xs text-slate-500 dark:text-slate-400" :title="t('materials.card.unit', { unit: material.unitOfMeasure || t('materials.card.defaultUnit') })">
                  {{ t('materials.card.unit', { unit: material.unitOfMeasure || t('materials.card.defaultUnit') }) }}
                </div>
                <div class="flex items-center gap-2">
                  <UButton
                    size="sm"
                    variant="ghost"
                    color="blue"
                    icon="i-lucide-edit"
                    @click="openEditModal(material)"
                  >
                    {{ t('materials.actions.edit') }}
                  </UButton>
                  <UButton
                    v-if="material.active"
                    size="sm"
                    variant="ghost"
                    color="amber"
                    icon="i-lucide-archive"
                    @click="deactivateMaterial(material.id)"
                  >
                    {{ t('materials.actions.deactivate') }}
                  </UButton>
                  <UButton
                    v-else
                    size="sm"
                    variant="ghost"
                    color="emerald"
                    icon="i-lucide-archive-restore"
                    @click="activateMaterial(material.id)"
                  >
                    {{ t('materials.actions.activate') }}
                  </UButton>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="p-12 text-center">
          <div class="flex flex-col items-center gap-4">
            <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
              <UIcon name="i-lucide-package" class="h-8 w-8 text-slate-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('materials.empty.title') }}</h3>
              <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                {{ searchQuery || activeFilter !== 'all' ? t('materials.empty.searchHint') : t('materials.empty.createHint') }}
              </p>
            </div>
            <UButton
              v-if="!searchQuery && activeFilter === 'all'"
              color="blue"
              icon="i-lucide-plus"
              @click="openCreateModal"
            >
              {{ t('materials.empty.action') }}
            </UButton>
          </div>
        </div>
      </div>

    <!-- Modal -->
    <UModal v-model="showModal" :ui="{ width: 'sm:max-w-xl' }">
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900/30">
              <UIcon name="i-lucide-package" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ isEditing ? t('materials.modal.edit.title') : t('materials.modal.create.title') }}</h3>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ isEditing ? t('materials.modal.edit.subtitle') : t('materials.modal.create.subtitle') }}</p>
            </div>
          </div>
        </template>

        <form @submit.prevent="submitForm" class="space-y-4">
          <UFormGroup :label="t('materials.modal.form.name.label')" required>
            <UInput v-model="formData.name" :placeholder="t('materials.modal.form.name.placeholder')" :disabled="formLoading" />
          </UFormGroup>

          <UFormGroup :label="t('materials.modal.form.description.label')">
            <UTextarea
              v-model="formData.description"
              :placeholder="t('materials.modal.form.description.placeholder')"
              :rows="3"
              :disabled="formLoading"
            />
          </UFormGroup>

          <div class="grid gap-4 md:grid-cols-3">
            <UFormGroup :label="t('materials.modal.form.unitCost.label')" required>
              <UInput
                v-model.number="formData.unitCost"
                type="number"
                step="0.01"
                min="0"
                :placeholder="t('materials.modal.form.unitCost.placeholder')"
                :disabled="formLoading"
              />
            </UFormGroup>

            <UFormGroup :label="t('materials.modal.form.currency.label')" required>
              <USelect
                v-model="formData.currency"
                :options="materialCurrencyOptions"
                :placeholder="t('materials.modal.form.currency.placeholder')"
                :disabled="formLoading || materialCurrencyOptions.length === 0"
              />
            </UFormGroup>

            <UFormGroup :label="t('materials.modal.form.unitOfMeasure.label')">
              <UInput
                v-model="formData.unitOfMeasure"
                :placeholder="t('materials.modal.form.unitOfMeasure.placeholder')"
                :disabled="formLoading"
              />
            </UFormGroup>
          </div>

          <div class="flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-600">
            <UToggle v-model="formData.active" :disabled="formLoading" />
            <span>{{ t('materials.modal.form.active.label') }}</span>
          </div>
        </form>

        <template #footer>
          <div class="flex justify-end gap-2">
            <UButton color="gray" variant="ghost" @click="showModal = false" :disabled="formLoading">
              {{ t('materials.modal.actions.cancel') }}
            </UButton>
            <UButton color="blue" @click="submitForm" :loading="formLoading">
              {{ isEditing ? t('materials.modal.actions.update') : t('materials.modal.actions.create') }}
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed, ref } from "vue";
import type { Material } from "~/composables/useMaterials";
import { useClinicSettings } from "~/composables/useClinicSettings";

const { settings: clinicSettings } = useClinicSettings();
const clinicCurrencyCode = computed(() => clinicSettings.value?.currency || "USD");
const clinicLocale = computed(() => clinicSettings.value?.locale || "en-US");

const materialsApi = useMaterials();
const toast = useEnhancedToast();
const { t } = useI18n();

const loading = ref(true);
const materials = ref<Material[]>([]);
const searchQuery = ref("");
const activeFilter = ref<"all" | "active" | "inactive">("all");
const showModal = ref(false);

// Options
const statusOptions = computed(() => [
  { label: t('materials.status.all'), value: 'all' },
  { label: t('materials.status.active'), value: 'active' },
  { label: t('materials.status.inactive'), value: 'inactive' }
]);
const formLoading = ref(false);
const isEditing = ref(false);
const editingId = ref<number | null>(null);
type MaterialFormState = {
  name: string;
  description: string;
  unitCost: number;
  currency: string;
  unitOfMeasure: string;
  active: boolean;
};

const formData = ref<MaterialFormState>({
  name: "",
  description: "",
  unitCost: 0,
  currency: clinicCurrencyCode.value,
  unitOfMeasure: "",
  active: true
});

const materialCurrencyOptions = computed(() => {
  const codes = new Set<string>();
  const exchangeRates = clinicSettings.value?.exchangeRates ?? {};

  if (clinicCurrencyCode.value) {
    codes.add(clinicCurrencyCode.value);
  }

  Object.keys(exchangeRates).forEach(code => {
    if (code) {
      codes.add(code.trim().toUpperCase());
    }
  });

  return Array.from(codes)
    .sort((a, b) => a.localeCompare(b))
    .map(code => {
      const symbol = resolveCurrencySymbol(code);
      return {
        label: symbol && symbol !== code ? `${code} (${symbol})` : code,
        value: code
      };
    });
});

const activeCount = computed(() => materials.value.filter(material => material.active).length);
const averageCosts = computed(() => {
  const activeItems = materials.value.filter(item => item.active);
  if (!activeItems.length) {
    return {
      converted: 0,
      original: 0,
      convertedCurrency: clinicCurrencyCode.value,
      originalCurrency: clinicCurrencyCode.value
    };
  }
  
  const convertedTotal = activeItems.reduce((sum, item) => sum + (item.convertedPrice ?? 0), 0);
  const originalTotal = activeItems.reduce((sum, item) => sum + (item.unitCost ?? 0), 0);
  
  return {
    converted: convertedTotal / activeItems.length,
    original: originalTotal / activeItems.length,
    convertedCurrency: clinicCurrencyCode.value,
    originalCurrency: activeItems[0]?.currency || clinicCurrencyCode.value
  };
});

// Keep backward compatibility
const averageConvertedCost = computed(() => formatClinicCurrency(averageCosts.value.converted));

const filteredMaterials = computed(() => {
  const term = searchQuery.value.toLowerCase();
  return materials.value
    .filter(material => {
      const matchesSearch =
        !term ||
        material.name.toLowerCase().includes(term) ||
        material.description?.toLowerCase().includes(term) ||
        material.unitOfMeasure?.toLowerCase().includes(term);

      const matchesFilter =
        activeFilter.value === "all" ||
        (activeFilter.value === "active" && material.active) ||
        (activeFilter.value === "inactive" && !material.active);

      return matchesSearch && matchesFilter;
    })
    .sort((a, b) => a.name.localeCompare(b.name));
});

async function fetchMaterials() {
  try {
    loading.value = true;
    materials.value = await materialsApi.getAll();
  } catch (error: any) {
    toast.error(t('materials.toasts.loadError'), error.message);
  } finally {
    loading.value = false;
  }
}

function openCreateModal() {
  isEditing.value = false;
  editingId.value = null;
  formData.value = {
    name: "",
    description: "",
    unitCost: 0,
    currency: clinicCurrencyCode.value,
    unitOfMeasure: "",
    active: true
  };
  showModal.value = true;
}

function openEditModal(material: Material) {
  isEditing.value = true;
  editingId.value = material.id;
  formData.value = {
    name: material.name,
    description: material.description ?? "",
    unitCost: material.unitCost,
    currency: normalizeCurrencyCode(material.currency) || clinicCurrencyCode.value,
    unitOfMeasure: material.unitOfMeasure ?? "",
    active: material.active
  };
  showModal.value = true;
}

async function submitForm() {
  try {
    formLoading.value = true;
    if (!formData.value.name.trim()) {
      throw new Error(t('materials.toasts.validation.nameRequired'));
    }
    const unitCost = Number(formData.value.unitCost);
    if (!Number.isFinite(unitCost) || unitCost < 0) {
      throw new Error(t('materials.toasts.validation.costPositive'));
    }
    const normalizedCurrency = normalizeCurrencyCode(formData.value.currency) || clinicCurrencyCode.value;
    formData.value.currency = normalizedCurrency;
    const payload = {
      name: formData.value.name.trim(),
      description: formData.value.description?.trim() || undefined,
      unitCost,
      currency: normalizedCurrency,
      unitOfMeasure: formData.value.unitOfMeasure?.trim() || undefined,
      active: formData.value.active
    };
    if (isEditing.value && editingId.value != null) {
      await materialsApi.update(editingId.value, payload);
      toast.updated(t('materials.toasts.updateSuccess'));
    } else {
      await materialsApi.create(payload);
      toast.add({ title: t('materials.toasts.createSuccess'), color: "sky" });
    }
    showModal.value = false;
    await fetchMaterials();
  } catch (error: any) {
    toast.error(isEditing.value ? t('materials.toasts.updateError') : t('materials.toasts.createError'), error.message);
  } finally {
    formLoading.value = false;
  }
}

async function activateMaterial(id: number) {
  try {
    await materialsApi.activate(id);
    toast.success(t('materials.toasts.activateSuccess'));
    await fetchMaterials();
  } catch (error: any) {
    toast.error(t('materials.toasts.activateError'), error.message);
  }
}

async function deactivateMaterial(id: number) {
  try {
    await materialsApi.deactivate(id);
    toast.success(t('materials.toasts.deactivateSuccess'));
    await fetchMaterials();
  } catch (error: any) {
    toast.error(t('materials.toasts.deactivateError'), error.message);
  }
}

function resolveCurrencySymbol(code: string) {
  try {
    const formatter = new Intl.NumberFormat(undefined, {
      style: "currency",
      currency: code,
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    });
    const parts = formatter.formatToParts(1);
    const symbol = parts.find(part => part.type === "currency")?.value;
    return symbol || code;
  } catch {
    return code;
  }
}

function normalizeCurrencyCode(input?: string | null) {
  if (!input) return undefined;
  const trimmed = input.trim().toUpperCase();
  if (trimmed.length < 3) return undefined;
  return trimmed.slice(0, 3);
}

function formatCurrencyValue(amount?: number | null, currency?: string | null) {
  if (amount === null || amount === undefined) {
    return "—";
  }
  const numericAmount = Number(amount);
  if (!Number.isFinite(numericAmount)) {
    return "—";
  }

  const safeCurrency = normalizeCurrencyCode(currency) || clinicCurrencyCode.value || "USD";
  try {
    return new Intl.NumberFormat(clinicLocale.value || undefined, {
      style: "currency",
      currency: safeCurrency
    }).format(numericAmount);
  } catch {
    return `${numericAmount.toFixed(2)} ${safeCurrency}`;
  }
}

function formatClinicCurrency(amount?: number | null) {
  return formatCurrencyValue(amount, clinicCurrencyCode.value);
}

// Use clinic timezone for all date/time displays
// CRITICAL: All admins see times in clinic timezone, not their browser timezone
const { timezone } = useClinicTimezone();

const formatDate = (date: string | number | null | undefined) => {
  if (!date) return "—";
  // Use clinic timezone formatter from dateUtils
  return formatDateInClinicTimezone(date, timezone.value);
};

useHead({
  title: () => t('materials.meta.title')
});

onMounted(() => {
  fetchMaterials();
});
</script>
