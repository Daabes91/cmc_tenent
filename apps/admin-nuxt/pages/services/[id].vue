<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-layers" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('services.detail.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('services.detail.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UBreadcrumb
              :links="[
                { label: t('services.detail.breadcrumb.services'), to: '/services' },
                { label: service?.nameEn ?? service?.nameAr ?? t('services.detail.breadcrumb.details') }
              ]"
              class="hidden sm:flex"
            />
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-arrow-left"
              @click="router.push('/services')"
            >
              {{ t('services.detail.actions.back') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-4xl mx-auto px-6 py-8">

      <div v-if="error" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-red-500 to-red-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-alert-triangle" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('services.detail.error.title') }}</h2>
              <p class="text-sm text-red-100">{{ error.message || t('services.detail.error.description') }}</p>
            </div>
          </div>
        </div>
        <div class="p-6">
          <UButton color="blue" icon="i-lucide-arrow-left" @click="router.push('/services')">
            {{ t('services.detail.actions.back') }}
          </UButton>
        </div>
      </div>

      <div v-else class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-layers" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('services.detail.breadcrumb.details') }}</h2>
              <p class="text-sm text-blue-100">{{ service?.nameEn || service?.nameAr || t('services.detail.loading') }}</p>
            </div>
          </div>
        </div>
        <div v-if="pending" class="p-6">
          <div class="space-y-6">
            <div class="animate-pulse space-y-4">
              <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-1/4"></div>
              <div class="grid gap-4 md:grid-cols-2">
                <div class="h-12 bg-slate-200 dark:bg-slate-700 rounded-xl"></div>
                <div class="h-12 bg-slate-200 dark:bg-slate-700 rounded-xl"></div>
              </div>
              <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-1/3"></div>
              <div class="grid gap-4 md:grid-cols-2">
                <div class="h-24 bg-slate-200 dark:bg-slate-700 rounded-xl"></div>
                <div class="h-24 bg-slate-200 dark:bg-slate-700 rounded-xl"></div>
              </div>
            </div>
          </div>
        </div>
        <form v-else class="p-6 space-y-8" @submit.prevent="handleSave">
          <div class="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-6 border border-slate-200 dark:border-slate-600">
            <div class="flex items-center gap-3 mb-4">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
                <UIcon name="i-lucide-type" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div>
                <h2 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('services.detail.sections.identity.title') }}</h2>
                <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('services.detail.sections.identity.subtitle') }}</p>
              </div>
            </div>
            <div class="grid gap-5 md:grid-cols-2">
              <UFormGroup :label="t('services.detail.form.nameEn.label')" :required="true">
                <UInput v-model="form.nameEn" :placeholder="t('services.detail.form.nameEn.placeholder')" size="lg" />
              </UFormGroup>
              <UFormGroup :label="t('services.detail.form.nameAr.label')">
                <UInput v-model="form.nameAr" :placeholder="t('services.detail.form.nameAr.placeholder')" dir="auto" size="lg" />
              </UFormGroup>
            </div>
          </div>

          <div class="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-6 border border-slate-200 dark:border-slate-600">
            <div class="flex items-center gap-3 mb-4">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
                <UIcon name="i-lucide-file-text" class="h-5 w-5 text-green-600 dark:text-green-400" />
              </div>
              <div>
                <h2 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('services.detail.sections.summary.title') }}</h2>
                <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('services.detail.sections.summary.subtitle') }}</p>
              </div>
            </div>
            <div class="grid gap-5 md:grid-cols-2">
              <UFormGroup :label="t('services.detail.form.summaryEn.label')">
                <UTextarea
                  v-model="form.summaryEn"
                  :rows="4"
                  size="lg"
                  :placeholder="t('services.detail.form.summaryEn.placeholder')"
                />
              </UFormGroup>
              <UFormGroup :label="t('services.detail.form.summaryAr.label')">
                <UTextarea v-model="form.summaryAr" :rows="4" size="lg" :placeholder="t('services.detail.form.summaryAr.placeholder')" dir="auto" />
              </UFormGroup>
            </div>
          </div>

          <div class="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-6 border border-slate-200 dark:border-slate-600">
            <div class="flex items-center gap-3 mb-4">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-50 dark:bg-purple-900/20">
                <UIcon name="i-lucide-link" class="h-5 w-5 text-purple-600 dark:text-purple-400" />
              </div>
              <div>
                <h2 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('services.detail.sections.metadata.title') }}</h2>
                <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('services.detail.sections.metadata.subtitle') }}</p>
              </div>
            </div>
            <UFormGroup :label="t('services.detail.form.slug.label')" :hint="t('services.detail.form.slug.hint')" :required="true">
              <UInput v-model="form.slug" :placeholder="t('services.detail.form.slug.placeholder')" size="lg" />
            </UFormGroup>
          </div>

          <div class="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-slate-600">
            <UButton type="button" variant="ghost" color="gray" :disabled="saving" @click="router.push('/services')">
              {{ t('services.detail.actions.cancel') }}
            </UButton>
            <UButton type="submit" color="blue" :loading="saving" icon="i-lucide-save">
              {{ t('services.detail.actions.save') }}
            </UButton>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watchEffect, watch } from "vue";
import type { AdminServiceSummary } from "@/types/services";

definePageMeta({
  title: 'Edit Service',
  requiresAuth: true
})

const { t } = useI18n();

useHead({ title: t('services.detail.meta.headTitle') });

const toast = useToast();
const router = useRouter();
const route = useRoute();
const { request } = useAdminApi();

const serviceId = computed(() => Number(route.params.id));

if (Number.isNaN(serviceId.value)) {
  throw createError({ statusCode: 404, statusMessage: "Service not found" });
}

const { data, pending, error } = await useAsyncData(
  `admin-service-${serviceId.value}`,
  async () => {
    return await request<AdminServiceSummary>(`/services/${serviceId.value}`);
  }
);

const service = computed(() => data.value ?? null);

const form = reactive({
  nameEn: "",
  nameAr: "",
  summaryEn: "",
  summaryAr: "",
  slug: ""
});

const slugManuallyEdited = ref(false);

watchEffect(() => {
  if (!service.value) return;
  form.nameEn = service.value.nameEn ?? "";
  form.nameAr = service.value.nameAr ?? "";
  form.summaryEn = service.value.summaryEn ?? "";
  form.summaryAr = service.value.summaryAr ?? "";
  form.slug = service.value.slug ?? "";
  slugManuallyEdited.value = false;
});

const saving = ref(false);

function slugify(input: string): string {
  return (input ?? "")
    .toString()
    .normalize("NFKD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, "")
    .trim()
    .replace(/\s+/g, "-")
    .replace(/-+/g, "-");
}

watch(
  () => form.nameEn,
  value => {
    if (!service.value || slugManuallyEdited.value) return;
    form.slug = slugify(value ?? "");
  }
);

watch(
  () => form.slug,
  value => {
    if (!service.value) return;
    slugManuallyEdited.value = value.trim() !== slugify(form.nameEn);
  }
);

async function handleSave() {
  if (!service.value) return;
  saving.value = true;

  const payload = {
    slug: form.slug.trim() || slugify(form.nameEn),
    nameEn: form.nameEn.trim(),
    nameAr: form.nameAr.trim() || null,
    summaryEn: form.summaryEn.trim() || null,
    summaryAr: form.summaryAr.trim() || null
  };

  try {
    if (!payload.nameEn) {
      throw new Error("English name is required");
    }

    await request(`/services/${service.value.id}`, {
      method: "PUT",
      body: payload
    });

    toast.add({ title: t('services.detail.toasts.saveSuccess') });
    router.push("/services");
  } catch (error: any) {
    toast.add({
      title: t('services.detail.toasts.saveError.title'),
      description: error?.data?.message ?? error?.message ?? t('services.detail.toasts.saveError.description'),
      color: "red"
    });
  } finally {
    saving.value = false;
  }
}
</script>
