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
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('services.create.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('services.create.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UBreadcrumb
              :links="[
                { label: t('services.detail.breadcrumb.services'), to: '/services' },
                { label: t('services.create.breadcrumb.newService') }
              ]"
              class="hidden sm:flex"
            />
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-arrow-left"
              @click="router.push('/services')"
            >
              {{ t('services.create.actions.cancel') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-4xl mx-auto px-6 py-8">

      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-layers" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('services.create.sections.title') }}</h2>
              <p class="text-sm text-blue-100">{{ t('services.create.sections.subtitle') }}</p>
            </div>
          </div>
        </div>
        <form class="p-6 space-y-8" @submit.prevent="handleSave">
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
              <UFormGroup :label="t('services.create.form.nameEn.label')" :required="true">
                <UInput v-model="form.nameEn" :placeholder="t('services.create.form.nameEn.placeholder')" size="lg" />
              </UFormGroup>
              <UFormGroup :label="t('services.create.form.nameAr.label')">
                <UInput v-model="form.nameAr" :placeholder="t('services.create.form.nameAr.placeholder')" dir="auto" size="lg" />
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
              <UFormGroup :label="t('services.create.form.summaryEn.label')">
                <UTextarea
                  v-model="form.summaryEn"
                  :rows="4"
                  size="lg"
                  :placeholder="t('services.create.form.summaryEn.placeholder')"
                />
              </UFormGroup>
              <UFormGroup :label="t('services.create.form.summaryAr.label')">
                <UTextarea v-model="form.summaryAr" :rows="4" size="lg" :placeholder="t('services.create.form.summaryAr.placeholder')" dir="auto" />
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
            <UFormGroup :label="t('services.create.form.slug.label')" :hint="t('services.create.form.slug.hint')" :required="true">
              <UInput v-model="form.slug" :placeholder="t('services.create.form.slug.placeholder')" size="lg" />
            </UFormGroup>
          </div>

          <div class="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-slate-600">
            <UButton type="button" variant="ghost" color="gray" :disabled="saving" @click="router.push('/services')">
              {{ t('services.create.actions.cancel') }}
            </UButton>
            <UButton type="submit" color="blue" :loading="saving" icon="i-lucide-plus">
              {{ t('services.create.actions.create') }}
            </UButton>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from "vue";

definePageMeta({
  title: 'Create Service',
  requiresAuth: true
})

const { t } = useI18n();

useHead({ title: t('services.create.meta.headTitle') });

const toast = useToast();
const router = useRouter();
const { request } = useAdminApi();

const form = reactive({
  nameEn: "",
  nameAr: "",
  summaryEn: "",
  summaryAr: "",
  slug: ""
});

const saving = ref(false);
const slugManuallyEdited = ref(false);

watch(
  () => form.nameEn,
  value => {
    if (!slugManuallyEdited.value) {
      form.slug = slugify(value ?? "");
    }
  },
  { immediate: true }
);

watch(
  () => form.slug,
  value => {
    slugManuallyEdited.value = value.trim() !== slugify(form.nameEn);
  }
);

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

async function handleSave() {
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

    await request("/services", {
      method: "POST",
      body: payload
    });

    toast.add({ title: t('services.create.toasts.createSuccess') });
    router.push("/services");
  } catch (error: any) {
    toast.add({
      title: t('services.create.toasts.createError.title'),
      description: error?.data?.message ?? error?.message ?? t('services.create.toasts.createError.description'),
      color: "red"
    });
  } finally {
    saving.value = false;
  }
}
</script>
