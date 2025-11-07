<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-green-500 to-emerald-600 shadow-lg">
              <UIcon name="i-lucide-plus" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('insuranceCompanies.create.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('insuranceCompanies.create.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-arrow-left" 
              to="/insurance-companies"
            >
              {{ t('insuranceCompanies.create.actions.back') }}
            </UButton>
            <UButton 
              color="green" 
              icon="i-lucide-save" 
              :loading="submitting"
              @click="handleSubmit"
            >
              {{ t('insuranceCompanies.create.actions.save') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-4xl mx-auto px-6 py-8">

      <!-- Form -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-green-500 to-emerald-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-shield-check" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('insuranceCompanies.create.form.title') }}</h2>
              <p class="text-sm text-green-100">{{ t('insuranceCompanies.create.form.subtitle') }}</p>
            </div>
          </div>
        </div>
        <form @submit.prevent="handleSubmit" class="p-6">
          <div class="space-y-6">
            <div class="grid gap-6 md:grid-cols-2">
              <UFormGroup :label="t('insuranceCompanies.edit.form.fields.nameEn.label')" required :error="errors.nameEn">
                <UInput
                  v-model="form.nameEn"
                  size="lg"
                  :placeholder="t('insuranceCompanies.edit.form.fields.nameEn.placeholder')"
                  icon="i-lucide-building-2"
                />
              </UFormGroup>

              <UFormGroup :label="t('insuranceCompanies.edit.form.fields.nameAr.label')" :error="errors.nameAr">
                <UInput
                  v-model="form.nameAr"
                  size="lg"
                  :placeholder="t('insuranceCompanies.edit.form.fields.nameAr.placeholder')"
                  icon="i-lucide-building-2"
                  dir="rtl"
                />
              </UFormGroup>
            </div>

            <div class="grid gap-6 md:grid-cols-2">
              <UFormGroup :label="t('insuranceCompanies.edit.form.fields.email.label')" :error="errors.email">
                <UInput
                  v-model="form.email"
                  type="email"
                  size="lg"
                  :placeholder="t('insuranceCompanies.edit.form.fields.email.placeholder')"
                  icon="i-lucide-mail"
                />
              </UFormGroup>

              <UFormGroup :label="t('insuranceCompanies.edit.form.fields.phone.label')" :error="errors.phone">
                <UInput
                  v-model="form.phone"
                  type="tel"
                  size="lg"
                  :placeholder="t('insuranceCompanies.edit.form.fields.phone.placeholder')"
                  icon="i-lucide-phone"
                />
              </UFormGroup>
            </div>

            <UFormGroup :label="t('insuranceCompanies.edit.form.fields.websiteUrl.label')" :error="errors.websiteUrl">
              <UInput
                v-model="form.websiteUrl"
                type="url"
                size="lg"
                :placeholder="t('insuranceCompanies.edit.form.fields.websiteUrl.placeholder')"
                icon="i-lucide-globe"
              />
            </UFormGroup>

            <UFormGroup :label="t('insuranceCompanies.edit.form.fields.logoUrl.label')" :hint="t('insuranceCompanies.edit.form.fields.logoUrl.hint')" :error="errors.logoUrl">
              <div class="space-y-4">
                <div class="flex gap-3">
                  <UInput
                    v-model="form.logoUrl"
                    type="url"
                    size="lg"
                    :placeholder="t('insuranceCompanies.edit.form.fields.logoUrl.placeholder')"
                    icon="i-lucide-image"
                    class="flex-1"
                  />
                  <UButton
                    variant="outline"
                    color="gray"
                    icon="i-lucide-upload"
                    @click="openImageUpload"
                  >
                    {{ t('insuranceCompanies.edit.form.fields.logoUrl.upload') }}
                  </UButton>
                </div>
                <!-- Logo Preview -->
                <div v-if="form.logoUrl" class="flex items-center gap-3 p-3 bg-slate-50 dark:bg-slate-700 rounded-xl">
                  <div class="w-16 h-12 border border-slate-200 dark:border-slate-600 rounded-lg overflow-hidden bg-white dark:bg-slate-800 flex items-center justify-center">
                    <img
                      :src="form.logoUrl"
                      :alt="form.nameEn"
                      class="max-w-full max-h-full object-contain"
                      @error="logoError = true"
                    >
                  </div>
                  <div>
                    <p class="text-sm font-medium text-slate-900 dark:text-white">{{ t('insuranceCompanies.edit.form.fields.logoUrl.preview.title') }}</p>
                    <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('insuranceCompanies.edit.form.fields.logoUrl.preview.description') }}</p>
                  </div>
                </div>
              </div>
            </UFormGroup>

            <div class="grid gap-6 md:grid-cols-2">
              <UFormGroup :label="t('insuranceCompanies.edit.form.fields.descriptionEn.label')" :error="errors.descriptionEn">
                <UTextarea
                  v-model="form.descriptionEn"
                  size="lg"
                  :rows="3"
                  :placeholder="t('insuranceCompanies.edit.form.fields.descriptionEn.placeholder')"
                />
              </UFormGroup>

              <UFormGroup :label="t('insuranceCompanies.edit.form.fields.descriptionAr.label')" :error="errors.descriptionAr">
                <UTextarea
                  v-model="form.descriptionAr"
                  size="lg"
                  :rows="3"
                  :placeholder="t('insuranceCompanies.edit.form.fields.descriptionAr.placeholder')"
                  dir="rtl"
                />
              </UFormGroup>
            </div>

            <div class="grid gap-6 md:grid-cols-2">
              <UFormGroup :label="t('insuranceCompanies.edit.form.fields.displayOrder.label')" :hint="t('insuranceCompanies.edit.form.fields.displayOrder.hint')" :error="errors.displayOrder">
                <UInput
                  v-model="form.displayOrder"
                  type="number"
                  size="lg"
                  :placeholder="t('insuranceCompanies.edit.form.fields.displayOrder.placeholder')"
                  icon="i-lucide-hash"
                  min="0"
                />
              </UFormGroup>

              <UFormGroup :label="t('insuranceCompanies.edit.form.fields.status.label')">
                <div class="flex items-center gap-3 p-4 bg-slate-50 dark:bg-slate-700 rounded-xl">
                  <UToggle
                    v-model="form.isActive"
                    size="lg"
                  />
                  <div>
                    <p class="text-sm font-medium text-slate-900 dark:text-white">
                      {{ form.isActive ? t('insuranceCompanies.edit.form.fields.status.active') : t('insuranceCompanies.edit.form.fields.status.inactive') }}
                    </p>
                    <p class="text-xs text-slate-500 dark:text-slate-400">
                      {{ form.isActive ? t('insuranceCompanies.edit.form.fields.status.activeDescription') : t('insuranceCompanies.edit.form.fields.status.inactiveDescription') }}
                    </p>
                  </div>
                </div>
              </UFormGroup>
            </div>
          </div>

          <!-- Form Actions -->
          <div class="flex justify-end gap-3 pt-6 border-t border-slate-200 dark:border-slate-700">
            <UButton
              variant="ghost"
              color="gray"
              to="/insurance-companies"
              :disabled="submitting"
            >
              {{ t('insuranceCompanies.create.form.actions.cancel') }}
            </UButton>
            <UButton
              type="submit"
              color="green"
              icon="i-lucide-save"
              :loading="submitting"
            >
              {{ t('insuranceCompanies.create.form.actions.save') }}
            </UButton>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { InsuranceCompanyFormData } from '../../types/insurance'

definePageMeta({
  title: 'Add Insurance Company',
  requiresAuth: true
})

const { t } = useI18n()
useHead({ title: t('insuranceCompanies.create.meta.headTitle') })

const { createInsuranceCompany } = useInsuranceCompanies()
const toast = useToast()
const router = useRouter()

// State
const form = ref<InsuranceCompanyFormData>({
  nameEn: '',
  nameAr: '',
  logoUrl: '',
  websiteUrl: '',
  phone: '',
  email: '',
  descriptionEn: '',
  descriptionAr: '',
  isActive: true,
  displayOrder: 0
})

const errors = ref<Record<string, string>>({})
const submitting = ref(false)
const showImageUpload = ref(false)
const logoError = ref(false)

// Methods
const validateForm = (): boolean => {
  errors.value = {}

  if (!form.value.nameEn.trim()) {
    errors.value.nameEn = 'Company name (English) is required'
  }

  if (form.value.email && !isValidEmail(form.value.email)) {
    errors.value.email = 'Please enter a valid email address'
  }

  if (form.value.websiteUrl && !isValidUrl(form.value.websiteUrl)) {
    errors.value.websiteUrl = 'Please enter a valid URL'
  }

  if (form.value.logoUrl && !isValidUrl(form.value.logoUrl)) {
    errors.value.logoUrl = 'Please enter a valid URL'
  }

  return Object.keys(errors.value).length === 0
}

const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

const isValidUrl = (url: string): boolean => {
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}

const handleSubmit = async () => {
  if (!validateForm()) return

  try {
    submitting.value = true

    const data = {
      nameEn: form.value.nameEn.trim(),
      nameAr: form.value.nameAr.trim() || null,
      logoUrl: form.value.logoUrl.trim() || null,
      websiteUrl: form.value.websiteUrl.trim() || null,
      phone: form.value.phone.trim() || null,
      email: form.value.email.trim() || null,
      descriptionEn: form.value.descriptionEn.trim() || null,
      descriptionAr: form.value.descriptionAr.trim() || null,
      isActive: form.value.isActive,
      displayOrder: form.value.displayOrder
    }

    await createInsuranceCompany(data)
    
    toast.add({
      title: t('insuranceCompanies.create.success.title'),
      description: t('insuranceCompanies.create.success.description'),
      color: "green",
      icon: "i-lucide-check-circle"
    })
    
    // Redirect to companies list
    await router.push('/insurance-companies')
  } catch (error) {
    console.error('Failed to create insurance company:', error)
    toast.add({
      title: t('insuranceCompanies.create.error.title'),
      description: t('insuranceCompanies.create.error.description'),
      color: "red",
      icon: "i-lucide-alert-circle"
    })
  } finally {
    submitting.value = false
  }
}

const openImageUpload = () => {
  showImageUpload.value = true
}

const handleImageUploaded = (imageUrl: string) => {
  form.value.logoUrl = imageUrl
  showImageUpload.value = false
}
</script>