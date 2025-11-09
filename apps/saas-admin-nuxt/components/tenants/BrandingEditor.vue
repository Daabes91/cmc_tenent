<template>
  <UCard>
    <template #header>
      <div class="flex items-center gap-2">
        <UIcon name="i-heroicons-paint-brush" class="w-5 h-5" />
        <h3 class="text-lg font-semibold">Branding Settings</h3>
      </div>
    </template>

    <div class="space-y-6">
      <!-- Logo -->
      <div>
        <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-3">
          Logo
        </label>
        
        <div v-if="localConfig.logoUrl" class="mb-3">
          <div class="relative inline-block">
            <img
              :src="localConfig.logoUrl"
              alt="Logo"
              class="max-h-20 rounded-lg border-2 border-slate-200 dark:border-slate-700"
            />
            <button
              type="button"
              class="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1 hover:bg-red-600"
              @click="removeLogo"
            >
              <UIcon name="i-heroicons-x-mark" class="w-4 h-4" />
            </button>
          </div>
        </div>

        <input
          ref="fileInput"
          type="file"
          accept="image/png,image/jpeg,image/jpg,image/svg+xml"
          class="hidden"
          @change="handleFileUpload"
        />
        <UButton
          color="gray"
          variant="outline"
          icon="i-heroicons-arrow-up-tray"
          :loading="uploading"
          @click="fileInput?.click()"
        >
          {{ localConfig.logoUrl ? 'Change Logo' : 'Upload Logo' }}
        </UButton>

        <p class="text-xs text-slate-500 dark:text-slate-400 mt-2">
          PNG with transparent background recommended, 200x60px
        </p>
      </div>

      <!-- Primary Color -->
      <div>
        <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-3">
          Primary Color
        </label>
        
        <div class="flex items-center gap-3">
          <input
            v-model="localConfig.primaryColor"
            type="color"
            class="w-14 h-14 rounded-lg cursor-pointer border-2 border-slate-200 dark:border-slate-700"
          />
          <UInput
            v-model="localConfig.primaryColor"
            placeholder="#3B82F6"
            size="lg"
            class="flex-1"
            :error="!!validationErrors.primaryColor"
          />
        </div>
        <p v-if="validationErrors.primaryColor" class="text-xs text-red-500 mt-1">
          {{ validationErrors.primaryColor }}
        </p>
        <p class="text-xs text-slate-500 dark:text-slate-400 mt-2">
          Used for buttons, links, and primary accents
        </p>
      </div>

      <!-- Secondary Color -->
      <div>
        <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-3">
          Secondary Color
        </label>
        
        <div class="flex items-center gap-3">
          <input
            v-model="localConfig.secondaryColor"
            type="color"
            class="w-14 h-14 rounded-lg cursor-pointer border-2 border-slate-200 dark:border-slate-700"
          />
          <UInput
            v-model="localConfig.secondaryColor"
            placeholder="#10B981"
            size="lg"
            class="flex-1"
            :error="!!validationErrors.secondaryColor"
          />
        </div>
        <p v-if="validationErrors.secondaryColor" class="text-xs text-red-500 mt-1">
          {{ validationErrors.secondaryColor }}
        </p>
        <p class="text-xs text-slate-500 dark:text-slate-400 mt-2">
          Used for secondary elements
        </p>
      </div>

      <!-- Color Presets -->
      <div>
        <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-3">
          Quick Presets
        </label>
        
        <div class="grid grid-cols-6 gap-2">
          <button
            v-for="preset in presets"
            :key="preset.name"
            type="button"
            class="aspect-square rounded-lg border-2 hover:scale-110 transition-transform"
            :class="localConfig.primaryColor === preset.primary ? 'border-slate-900 dark:border-white' : 'border-slate-200 dark:border-slate-700'"
            :style="{ backgroundColor: preset.primary }"
            :title="preset.name"
            @click="applyPreset(preset)"
          />
        </div>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
import type { BrandingConfig } from '~/types'

interface Props {
  modelValue: BrandingConfig
  tenantId: number
  validationErrors?: Record<string, string>
}

const props = withDefaults(defineProps<Props>(), {
  validationErrors: () => ({})
})

const emit = defineEmits<{
  'update:modelValue': [value: BrandingConfig]
}>()

const toast = useToast()
const { getToken } = useSaasAuth()
const config = useRuntimeConfig()

const localConfig = ref<BrandingConfig>({ ...props.modelValue })
const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)

const presets = [
  { name: 'Blue', primary: '#3B82F6', secondary: '#10B981' },
  { name: 'Purple', primary: '#8B5CF6', secondary: '#EC4899' },
  { name: 'Red', primary: '#EF4444', secondary: '#F97316' },
  { name: 'Teal', primary: '#14B8A6', secondary: '#06B6D4' },
  { name: 'Indigo', primary: '#6366F1', secondary: '#3B82F6' },
  { name: 'Emerald', primary: '#10B981', secondary: '#84CC16' }
]

watch(() => props.modelValue, (newValue) => {
  localConfig.value = { ...newValue }
}, { deep: true })

watch(localConfig, (newValue) => {
  emit('update:modelValue', { ...newValue })
}, { deep: true })

const applyPreset = (preset: { primary: string; secondary: string }) => {
  localConfig.value.primaryColor = preset.primary
  localConfig.value.secondaryColor = preset.secondary
}

const handleFileUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const validTypes = ['image/png', 'image/jpeg', 'image/jpg', 'image/svg+xml']
  if (!validTypes.includes(file.type)) {
    toast.add({
      title: 'Invalid file type',
      description: 'Please upload PNG, JPG, or SVG',
      color: 'red'
    })
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    toast.add({
      title: 'File too large',
      description: 'Maximum size is 2MB',
      color: 'red'
    })
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await $fetch<{ url: string }>(
      `${config.public.saasApiBase}/tenants/${props.tenantId}/branding/upload-logo`,
      {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${getToken()}` },
        body: formData
      }
    )

    localConfig.value.logoUrl = response.url
    toast.add({
      title: 'Logo uploaded',
      color: 'green'
    })
  } catch (err: any) {
    toast.add({
      title: 'Upload failed',
      description: err.message,
      color: 'red'
    })
  } finally {
    uploading.value = false
    if (target) target.value = ''
  }
}

const removeLogo = () => {
  localConfig.value.logoUrl = null
}
</script>
