<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 shadow-lg">
              <UIcon name="i-lucide-user-plus" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('patients.create.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('patients.create.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-arrow-left" 
              to="/patients"
            >
              {{ t('patients.create.actions.back') }}
            </UButton>
            <UButton 
              color="emerald" 
              icon="i-lucide-save" 
              :loading="saving"
              @click="handleSave"
            >
              {{ t('patients.create.actions.save') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-4xl mx-auto px-6 py-8">

      <!-- Form -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-emerald-500 to-teal-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-user-round" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('patients.create.form.title') }}</h2>
              <p class="text-sm text-emerald-100">{{ t('patients.create.form.subtitle') }}</p>
            </div>
          </div>
        </div>
        <form @submit.prevent="handleSave" class="p-6">
          <div class="space-y-6">
            <div class="grid gap-6 md:grid-cols-2">
              <UFormGroup :label="t('patients.details.form.fields.firstName')" required :error="errors.firstName">
                <UInput
                  v-model="form.firstName"
                  size="lg"
                  :placeholder="t('patients.details.form.placeholders.firstName')"
                  icon="i-lucide-user"
                  @blur="validateField('firstName')"
                  @input="clearFieldError('firstName')"
                />
              </UFormGroup>

              <UFormGroup :label="t('patients.details.form.fields.lastName')" required :error="errors.lastName">
                <UInput
                  v-model="form.lastName"
                  size="lg"
                  :placeholder="t('patients.details.form.placeholders.lastName')"
                  icon="i-lucide-user"
                  @blur="validateField('lastName')"
                  @input="clearFieldError('lastName')"
                />
              </UFormGroup>
            </div>

            <div class="grid gap-6 md:grid-cols-2">
              <UFormGroup :label="t('patients.details.form.fields.email')" :error="errors.email">
                <UInput
                  v-model="form.email"
                  type="email"
                  size="lg"
                  :placeholder="t('patients.details.form.placeholders.email')"
                  icon="i-lucide-mail"
                  @blur="validateField('email')"
                  @input="clearFieldError('email')"
                />
                <template #description>{{ t('patients.details.form.descriptions.email') }}</template>
              </UFormGroup>

              <UFormGroup :label="t('patients.details.form.fields.phone')" :error="errors.phone">
                <UInput
                  v-model="form.phone"
                  type="tel"
                  size="lg"
                  :placeholder="t('patients.details.form.placeholders.phone')"
                  icon="i-lucide-phone"
                />
              </UFormGroup>
            </div>

            <UFormGroup :label="t('patients.details.form.fields.dateOfBirth')" :hint="t('patients.details.form.hints.dateOfBirth')" :error="errors.dateOfBirth">
              <UInput
                v-model="form.dateOfBirth"
                type="date"
                size="lg"
                icon="i-lucide-calendar"
              />
            </UFormGroup>

            <UFormGroup :label="t('patients.details.form.fields.profileImage')" :hint="t('patients.details.form.hints.profileImage')" :error="errors.profileImageUrl">
              <ImageUploadButton
                v-model:imageUrl="form.profileImageUrl"
                :placeholder="t('patients.details.form.placeholders.profileImage')"
                :alt-text="`${form.firstName} ${form.lastName}`.trim() || t('patients.details.form.image.alt')"
                :preview-title="t('patients.details.form.image.title')"
                :preview-description="t('patients.details.form.image.description')"
                preview-size="w-16 h-16 rounded-full"
                @validate="validateField('profileImageUrl')"
                @clear-error="clearFieldError('profileImageUrl')"
                @image-error="handleImageError"
                @image-load="handleImageLoad"
                @upload-error="(error) => errors.profileImageUrl = error"
                @upload-success="handleImageUploadSuccess"
              />
            </UFormGroup>

            <UFormGroup :label="t('patients.details.form.fields.notes')" :hint="t('patients.details.form.hints.notes')" :error="errors.notes">
              <UTextarea
                v-model="form.notes"
                :placeholder="t('patients.details.form.placeholders.notes')"
                :rows="4"
                @input="clearFieldError('notes')"
              />
            </UFormGroup>

            <UFormGroup label="Tags" hint="Add tags to categorize this patient">
               <USelectMenu
                  v-model="form.tags"
                  :options="availableTags"
                  option-attribute="name"
                  value-attribute="id"
                  multiple
                  creatable
                  searchable
                  placeholder="Select or create tags"
                  @create="handleCreateTag"
               >
                 <template #label>
                    <span v-if="form.tags.length" class="truncate">{{ form.tags.length }} tags selected</span>
                    <span v-else class="text-gray-500">Select or create tags</span>
                 </template>
               </USelectMenu>
            </UFormGroup>
          </div>

          <!-- Form Actions -->
          <div class="flex justify-end gap-3 pt-6 border-t border-slate-200 dark:border-slate-700">
            <UButton
              variant="ghost"
              color="gray"
              to="/patients"
              :disabled="saving"
            >
              {{ t('common.actions.cancel') }}
            </UButton>
            <UButton
              type="submit"
              color="emerald"
              icon="i-lucide-save"
              :loading="saving"
            >
              {{ t('patients.create.actions.save') }}
            </UButton>
          </div>
        </form>
      </div>
    </div>


  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTagService, type Tag } from '@/services/tag.service'

const { t } = useI18n()

definePageMeta({
  requiresAuth: true
})

useHead(() => ({ title: t('patients.create.meta.headTitle') }))

const toast = useToast()
const router = useRouter()
const { request } = useAdminApi()
const { listTags, createTag } = useTagService()

// State
const form = ref({
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  dateOfBirth: '',
  profileImageUrl: '',
  notes: '',
  tags: [] as number[]
})

const availableTags = ref<Tag[]>([])

onMounted(async () => {
  try {
    availableTags.value = await listTags()
  } catch (e) {
    console.error("Failed to load tags", e)
  }
})

const handleCreateTag = async (name: string) => {
  try {
    const newTag = await createTag(name)
    if (!availableTags.value.some(tag => tag.id === newTag.id)) {
      availableTags.value.push(newTag)
    }
    if (!form.value.tags.includes(newTag.id)) {
      form.value.tags.push(newTag.id)
    }
  } catch (e) {
    toast.add({
      title: "Failed to create tag",
      color: "red"
    })
  }
}

const errors = ref<Record<string, string>>({})
const saving = ref(false)

// Validation functions
const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

const isValidUrl = (url: string): boolean => {
  if (!url.trim()) return true // Optional field
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}

// Individual field validation
const validateField = (fieldName: string) => {
  switch (fieldName) {
    case 'firstName':
      if (!form.value.firstName.trim()) {
        errors.value.firstName = t('patients.details.validation.firstNameRequired')
      } else {
        delete errors.value.firstName
      }
      break
    case 'lastName':
      if (!form.value.lastName.trim()) {
        errors.value.lastName = t('patients.details.validation.lastNameRequired')
      } else {
        delete errors.value.lastName
      }
      break
    case 'email':
      if (form.value.email && !isValidEmail(form.value.email)) {
        errors.value.email = t('patients.details.validation.invalidEmail')
      } else {
        delete errors.value.email
      }
      break
    case 'profileImageUrl':
      if (form.value.profileImageUrl && !isValidUrl(form.value.profileImageUrl)) {
        errors.value.profileImageUrl = t('patients.details.validation.invalidUrl')
      } else {
        delete errors.value.profileImageUrl
      }
      break
    case 'notes':
      if (form.value.notes && form.value.notes.length > 2000) {
        errors.value.notes = t('patients.details.validation.notesTooLong')
      } else {
        delete errors.value.notes
      }
      break
  }
}

// Clear field error when user starts typing
const clearFieldError = (fieldName: string) => {
  if (errors.value[fieldName]) {
    delete errors.value[fieldName]
  }
}

// Image handling
const handleImageError = () => {
  errors.value.profileImageUrl = t('patients.details.validation.invalidUrl')
}

const handleImageLoad = () => {
  delete errors.value.profileImageUrl
}

const handleImageUploadSuccess = () => {
  delete errors.value.profileImageUrl
}

const validateForm = (): boolean => {
  // Validate all fields
  validateField('firstName')
  validateField('lastName')
  validateField('email')
  validateField('profileImageUrl')
  validateField('notes')

  return Object.keys(errors.value).length === 0
}

const handleSave = async () => {
  if (!validateForm()) return

  try {
    saving.value = true

    const payload = {
      firstName: form.value.firstName.trim(),
      lastName: form.value.lastName.trim(),
      email: form.value.email.trim() || null,
      phone: form.value.phone.trim() || null,
      dateOfBirth: form.value.dateOfBirth || null,
      profileImageUrl: form.value.profileImageUrl.trim() || null,
      notes: form.value.notes.trim() || null,
      tagIds: form.value.tags
    }

    await request('/patients', {
      method: 'POST',
      body: payload
    })

    toast.add({
      title: t('patients.create.toasts.success.title'),
      description: t('patients.create.toasts.success.description'),
      color: "green",
      icon: "i-lucide-check-circle"
    })
    
    // Redirect to patients list
    await router.push('/patients')
  } catch (error: any) {
    console.error('Failed to create patient:', error)
    toast.add({
      title: t('patients.create.toasts.error.title'),
      description: error?.data?.message ?? error?.message ?? t('patients.create.toasts.error.description'),
      color: "red",
      icon: "i-lucide-alert-circle"
    })
  } finally {
    saving.value = false
  }
}


</script>
