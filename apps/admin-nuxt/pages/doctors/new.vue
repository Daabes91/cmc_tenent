<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-user-plus" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('doctors.create.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('doctors.create.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-arrow-left" 
              to="/doctors"
            >
              {{ t('doctors.create.actions.back') }}
            </UButton>
            <UButton 
              color="blue" 
              icon="i-lucide-save" 
              :loading="saving"
              @click="handleSave"
            >
              {{ t('doctors.create.actions.save') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-4xl mx-auto px-6 py-8">

      <!-- Form -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-stethoscope" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('doctors.create.form.title') }}</h2>
              <p class="text-sm text-blue-100">{{ t('doctors.create.form.subtitle') }}</p>
            </div>
          </div>
        </div>
        <form @submit.prevent="handleSave" class="p-6">
          <div class="space-y-6">
            <div class="grid gap-6 md:grid-cols-2">
              <UFormGroup :label="t('doctors.create.form.fields.fullNameEn')" required :error="errors.fullNameEn">
                <UInput
                  v-model="form.fullNameEn"
                  size="lg"
                  :placeholder="t('doctors.create.form.placeholders.fullNameEn')"
                  icon="i-lucide-user"
                />
              </UFormGroup>

              <UFormGroup :label="t('doctors.create.form.fields.fullNameAr')" :error="errors.fullNameAr">
                <UInput
                  v-model="form.fullNameAr"
                  size="lg"
                  :placeholder="t('doctors.create.form.placeholders.fullNameAr')"
                  icon="i-lucide-user"
                  dir="rtl"
                />
              </UFormGroup>
            </div>

            <div class="grid gap-6 md:grid-cols-2">
              <UFormGroup :label="t('doctors.create.form.fields.email')" :error="errors.email">
                <UInput
                  v-model="form.email"
                  type="email"
                  size="lg"
                  :placeholder="t('doctors.create.form.placeholders.email')"
                  icon="i-lucide-mail"
                />
              </UFormGroup>

              <UFormGroup :label="t('doctors.create.form.fields.phone')" :error="errors.phone">
                <UInput
                  v-model="form.phone"
                  type="tel"
                  size="lg"
                  :placeholder="t('doctors.create.form.placeholders.phone')"
                  icon="i-lucide-phone"
                />
              </UFormGroup>
            </div>

            <div class="grid gap-6 md:grid-cols-2">
              <UFormGroup :label="t('doctors.create.form.fields.specialtyEn')" :error="errors.specialtyEn">
                <UInput
                  v-model="form.specialtyEn"
                  size="lg"
                  :placeholder="t('doctors.create.form.placeholders.specialtyEn')"
                  icon="i-lucide-briefcase"
                />
              </UFormGroup>

              <UFormGroup :label="t('doctors.create.form.fields.specialtyAr')" :error="errors.specialtyAr">
                <UInput
                  v-model="form.specialtyAr"
                  size="lg"
                  :placeholder="t('doctors.create.form.placeholders.specialtyAr')"
                  icon="i-lucide-briefcase"
                  dir="rtl"
                />
              </UFormGroup>
            </div>

            <UFormGroup
              :label="t('doctors.create.form.fields.profileImage')"
              :hint="t('doctors.create.form.hints.profileImage')"
              :error="errors.imageUrl"
            >
              <ImageUploadButton
                v-model:imageUrl="form.imageUrl"
                :placeholder="t('doctors.create.form.placeholders.imageUrl')"
                :alt-text="form.fullNameEn.trim() || t('doctors.create.form.imageUpload.altText')"
                :preview-title="t('doctors.create.form.imageUpload.title')"
                :preview-description="t('doctors.create.form.imageUpload.description')"
                preview-size="w-16 h-16 rounded-full"
                @upload-error="(error) => errors.imageUrl = error"
                @upload-success="handleImageUploadSuccess"
                @update:imageUrl="(url) => { form.imageUrl = url }"
              />
            </UFormGroup>

            <div class="grid gap-6 md:grid-cols-1">
              <UFormGroup
                :label="t('doctors.create.form.fields.bioEn')"
                :hint="t('doctors.create.form.hints.bioEn')"
                :error="errors.bioEn"
              >
                <UTextarea
                  v-model="form.bioEn"
                  :rows="4"
                  size="lg"
                  :placeholder="t('doctors.create.form.placeholders.bioEn')"
                />
              </UFormGroup>
              
              <UFormGroup
                :label="t('doctors.create.form.fields.bioAr')"
                :hint="t('doctors.create.form.hints.bioAr')"
                :error="errors.bioAr"
              >
                <UTextarea
                  v-model="form.bioAr"
                  :rows="4"
                  size="lg"
                  :placeholder="t('doctors.create.form.placeholders.bioAr')"
                  dir="rtl"
                />
              </UFormGroup>
            </div>

            <div class="grid gap-6 md:grid-cols-2">
              <UFormGroup
                :label="t('doctors.create.form.fields.languages')"
                :hint="t('doctors.create.form.hints.languages')"
              >
                <div class="rounded-lg border border-slate-200 dark:border-slate-600 bg-white dark:bg-slate-800 p-4">
                  <div class="flex flex-wrap gap-3">
                    <UCheckbox
                      v-for="option in languageOptions"
                      :key="option.value"
                      v-model="form.locales"
                      :value="option.value"
                      :label="option.label"
                      color="blue"
                      class="inline-flex"
                    />
                  </div>
                </div>
              </UFormGroup>

              <UFormGroup
                :label="t('doctors.create.form.fields.services')"
                :hint="t('doctors.create.form.hints.services')"
              >
                <div class="rounded-lg border border-slate-200 dark:border-slate-600 bg-white dark:bg-slate-800 p-4">
                  <div v-if="serviceOptions.length > 0" class="max-h-64 overflow-y-auto">
                    <div class="flex flex-wrap gap-3">
                      <UCheckbox
                        v-for="option in serviceOptions"
                        :key="option.value"
                        v-model="form.serviceIds"
                        :value="option.value"
                        :label="option.label"
                        color="blue"
                        class="inline-flex"
                      />
                    </div>
                  </div>

                  <!-- Empty State -->
                  <div v-else class="flex flex-col items-center justify-center py-12 text-center">
                    <div class="flex h-16 w-16 items-center justify-center rounded-full bg-slate-100 dark:bg-slate-700 mb-4">
                      <UIcon name="i-lucide-package-x" class="h-8 w-8 text-slate-400" />
                    </div>
                    <h3 class="text-sm font-semibold text-slate-900 dark:text-white mb-1">
                      {{ t('doctors.create.form.emptyServices.title') }}
                    </h3>
                    <p class="text-xs text-slate-500 dark:text-slate-400">
                      {{ t('doctors.create.form.emptyServices.description') }}
                    </p>
                  </div>
                </div>
              </UFormGroup>
            </div>

            <UFormGroup
              :label="t('doctors.create.form.fields.displayOrder')"
              :hint="t('doctors.create.form.hints.displayOrder')"
              :error="errors.displayOrder"
            >
              <UInput
                v-model.number="form.displayOrder"
                type="number"
                min="0"
                step="1"
                size="lg"
                :placeholder="t('doctors.create.form.placeholders.displayOrder')"
                icon="i-lucide-hash"
              />
            </UFormGroup>
          </div>

          <!-- Form Actions -->
          <div class="flex justify-end gap-3 pt-6 border-t border-slate-200 dark:border-slate-700">
            <UButton
              variant="ghost"
              color="gray"
              to="/doctors"
              :disabled="saving"
            >
              {{ t('common.actions.cancel') }}
            </UButton>
            <UButton
              type="submit"
              color="blue"
              icon="i-lucide-save"
              :loading="saving"
            >
              {{ t('doctors.create.actions.save') }}
            </UButton>
          </div>
        </form>
      </div>
    </div>


  </div>
</template>

<script setup lang="ts">
import type { AdminServiceSummary } from "@/types/services";
import { computed, reactive, ref } from "vue";
import { useI18n } from "vue-i18n";

const toast = useToast()
const router = useRouter();
const { fetcher, request } = useAdminApi();
const { t, locale } = useI18n();

useHead(() => ({
  title: t('doctors.create.meta.title')
}));

const { data: servicesData } = await useAsyncData("admin-doctor-services", () =>
  fetcher<AdminServiceSummary[]>("/services", [])
);

const services = computed(() => servicesData.value ?? []);
const serviceOptions = computed(() => {
  const useArabic = locale.value?.startsWith("ar");

  return services.value.map(service => {
    const primary = useArabic ? service.nameAr : service.nameEn;
    const secondary = useArabic ? service.nameEn : service.nameAr;
    const fallback = service.slug;
    const label = (primary || secondary || fallback || "").trim() || fallback;
    return {
      label,
      value: service.id.toString()
    };
  }).sort((a, b) => a.label.localeCompare(b.label, locale.value || undefined));
});

// Language options
const languageOptions = computed(() => [
  { label: t('doctors.common.languages.english'), value: "en" },
  { label: t('doctors.common.languages.arabic'), value: "ar" },
  { label: t('doctors.common.languages.russian'), value: "ru" }
]);

const form = reactive({
  fullNameEn: "",
  fullNameAr: "",
  email: "",
  phone: "",
  specialtyEn: "",
  specialtyAr: "",
  bioEn: "",
  bioAr: "",
  imageUrl: "",
  locales: [] as string[],
  serviceIds: [] as string[],
  displayOrder: 0
});

const errors = ref<Record<string, string>>({})
const saving = ref(false)

// Language and service options are now handled directly by UCheckbox components

// Methods
const validateForm = (): boolean => {
  errors.value = {}

  if (!form.fullNameEn.trim()) {
    errors.value.fullNameEn = t('doctors.create.validation.fullNameRequired')
  }

  if (form.email && !isValidEmail(form.email)) {
    errors.value.email = t('doctors.create.validation.invalidEmail')
  }

  if (form.displayOrder < 0) {
    errors.value.displayOrder = t('doctors.create.validation.invalidDisplayOrder')
  }

  return Object.keys(errors.value).length === 0
}

const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

const handleImageUploadSuccess = () => {
  delete errors.value.imageUrl
}


const handleSave = async () => {
  if (!validateForm()) return

  try {
    saving.value = true

    const payload = {
      fullNameEn: form.fullNameEn.trim(),
      fullNameAr: form.fullNameAr.trim() || null,
      email: form.email.trim() || null,
      phone: form.phone.trim() || null,
      specialtyEn: form.specialtyEn.trim() || null,
      specialtyAr: form.specialtyAr.trim() || null,
      bioEn: form.bioEn.trim() || null,
      bioAr: form.bioAr.trim() || null,
      imageUrl: form.imageUrl.trim() || null,
      locales: form.locales,
      serviceIds: form.serviceIds.map(id => Number(id)),
      displayOrder: form.displayOrder,
      isActive: true
    }

    await request('/doctors', {
      method: 'POST',
      body: payload
    })

    toast.add({
      title: t('doctors.create.toasts.success'),
      description: t('doctors.create.toasts.successDescription'),
      color: "green",
      icon: "i-lucide-check-circle"
    })
    
    // Redirect to doctors list
    await router.push('/doctors')
  } catch (error: any) {
    console.error('Failed to create doctor:', error)
    toast.add({
      title: t('doctors.create.toasts.error'),
      description: error?.data?.message ?? error?.message ?? t('doctors.create.toasts.errorDescription'),
      color: "red",
      icon: "i-lucide-alert-circle"
    })
  } finally {
    saving.value = false
  }
}
</script>
