<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <UButton
        color="gray"
        variant="ghost"
        icon="i-heroicons-arrow-left"
        size="sm"
        @click="router.push(`/tenants/${tenantId}`)"
        class="mb-3"
      >
        Back to Tenant
      </UButton>
      <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
        <div>
          <h1 class="text-3xl font-bold text-slate-900 dark:text-white">Branding</h1>
          <p v-if="tenant" class="text-slate-600 dark:text-slate-400 mt-1">
            Configure branding for {{ tenant.name }}
          </p>
        </div>

        <div v-if="!loading" class="flex gap-3">
          <UButton
            color="gray"
            variant="outline"
            icon="i-heroicons-arrow-path"
            :disabled="!hasChanges"
            @click="resetChanges"
          >
            Reset
          </UButton>
          <UButton
            color="primary"
            icon="i-heroicons-check"
            :loading="saving"
            :disabled="!hasChanges || !isValid"
            @click="saveBranding"
          >
            Save Changes
          </UButton>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <USkeleton class="h-96" />
      <USkeleton class="h-96" />
    </div>

    <!-- Error -->
    <UAlert
      v-else-if="error"
      color="red"
      variant="subtle"
      icon="i-heroicons-exclamation-triangle"
      title="Error"
      :description="error"
    >
      <template #actions>
        <UButton color="red" variant="ghost" @click="loadBranding">Retry</UButton>
      </template>
    </UAlert>

    <!-- Editor -->
    <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Editor Panel -->
      <div>
        <BrandingEditor
          v-model="brandingConfig"
          :tenant-id="tenantId"
          :validation-errors="validationErrors"
        />
      </div>

      <!-- Preview Panel -->
      <div class="lg:sticky lg:top-6">
        <UCard>
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-heroicons-eye" class="w-5 h-5" />
              <h2 class="text-lg font-semibold">Live Preview</h2>
            </div>
          </template>

          <div class="space-y-6">
            <!-- Logo -->
            <div v-if="brandingConfig.logoUrl" class="text-center">
              <img
                :src="brandingConfig.logoUrl"
                alt="Logo"
                class="max-h-20 mx-auto"
              />
            </div>
            <div v-else class="text-center py-8 bg-slate-50 dark:bg-slate-800 rounded-lg">
              <UIcon name="i-heroicons-photo" class="w-12 h-12 text-slate-400 mx-auto mb-2" />
              <p class="text-sm text-slate-500">No logo uploaded</p>
            </div>

            <!-- Colors -->
            <div class="space-y-4">
              <div>
                <label class="text-sm font-medium mb-2 block">Primary Color</label>
                <div class="flex items-center gap-3">
                  <div
                    class="w-16 h-16 rounded-lg border-2 border-slate-200 dark:border-slate-700"
                    :style="{ backgroundColor: brandingConfig.primaryColor }"
                  />
                  <div>
                    <p class="font-mono text-sm">{{ brandingConfig.primaryColor }}</p>
                    <p class="text-xs text-slate-500">Buttons, links, accents</p>
                  </div>
                </div>
              </div>

              <div>
                <label class="text-sm font-medium mb-2 block">Secondary Color</label>
                <div class="flex items-center gap-3">
                  <div
                    class="w-16 h-16 rounded-lg border-2 border-slate-200 dark:border-slate-700"
                    :style="{ backgroundColor: brandingConfig.secondaryColor }"
                  />
                  <div>
                    <p class="font-mono text-sm">{{ brandingConfig.secondaryColor }}</p>
                    <p class="text-xs text-slate-500">Secondary elements</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Sample Elements -->
            <div class="space-y-3 pt-4 border-t border-slate-200 dark:border-slate-700">
              <p class="text-sm font-medium">Sample Elements</p>
              
              <button
                class="px-4 py-2 rounded-lg text-white font-medium w-full"
                :style="{ backgroundColor: brandingConfig.primaryColor }"
              >
                Primary Button
              </button>

              <button
                class="px-4 py-2 rounded-lg text-white font-medium w-full"
                :style="{ backgroundColor: brandingConfig.secondaryColor }"
              >
                Secondary Button
              </button>

              <p class="text-center">
                <a
                  href="#"
                  class="font-medium hover:underline"
                  :style="{ color: brandingConfig.primaryColor }"
                  @click.prevent
                >
                  Sample Link
                </a>
              </p>
            </div>
          </div>
        </UCard>

        <!-- Info -->
        <UCard class="mt-6">
          <div class="flex items-start gap-3">
            <UIcon name="i-heroicons-information-circle" class="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" />
            <div class="text-sm text-slate-600 dark:text-slate-400">
              <p class="font-medium text-slate-900 dark:text-white mb-1">Branding Tips</p>
              <ul class="list-disc list-inside space-y-1">
                <li>Use colors with good contrast</li>
                <li>PNG logo with transparent background</li>
                <li>Recommended size: 200x60 pixels</li>
                <li>Changes apply immediately</li>
              </ul>
            </div>
          </div>
        </UCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Tenant, BrandingConfig } from '~/types'

const route = useRoute()
const router = useRouter()
const api = useSaasApi()
const toast = useToast()

const tenant = ref<Tenant | null>(null)
const brandingConfig = ref<BrandingConfig>({
  primaryColor: '#3B82F6',
  secondaryColor: '#10B981',
  logoUrl: null
})
const originalConfig = ref<BrandingConfig | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const saving = ref(false)
const validationErrors = ref<Record<string, string>>({})

const tenantId = computed(() => Number(route.params.id))

const hasChanges = computed(() => {
  if (!originalConfig.value) return false
  return (
    brandingConfig.value.primaryColor !== originalConfig.value.primaryColor ||
    brandingConfig.value.secondaryColor !== originalConfig.value.secondaryColor ||
    brandingConfig.value.logoUrl !== originalConfig.value.logoUrl
  )
})

const validateHexColor = (color: string): boolean => {
  return /^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$/.test(color)
}

const validateBranding = (): boolean => {
  const errors: Record<string, string> = {}

  if (!brandingConfig.value.primaryColor) {
    errors.primaryColor = 'Primary color is required'
  } else if (!validateHexColor(brandingConfig.value.primaryColor)) {
    errors.primaryColor = 'Invalid hex color format'
  }

  if (!brandingConfig.value.secondaryColor) {
    errors.secondaryColor = 'Secondary color is required'
  } else if (!validateHexColor(brandingConfig.value.secondaryColor)) {
    errors.secondaryColor = 'Invalid hex color format'
  }

  validationErrors.value = errors
  return Object.keys(errors).length === 0
}

const isValid = computed(() => {
  return Object.keys(validationErrors.value).length === 0
})

watch(brandingConfig, () => {
  validateBranding()
}, { deep: true })

const loadBranding = async () => {
  loading.value = true
  error.value = null

  try {
    tenant.value = await api.getTenant(tenantId.value)
    const config = await api.getTenantBranding(tenantId.value)
    brandingConfig.value = { ...config }
    originalConfig.value = { ...config }
  } catch (err: any) {
    error.value = err.message || 'Failed to load branding'
  } finally {
    loading.value = false
  }
}

const resetChanges = () => {
  if (originalConfig.value) {
    brandingConfig.value = { ...originalConfig.value }
    validationErrors.value = {}
  }
}

const saveBranding = async () => {
  if (!isValid.value) {
    toast.add({
      title: 'Validation error',
      description: 'Please fix errors before saving',
      color: 'red'
    })
    return
  }

  saving.value = true

  try {
    await api.updateTenantBranding(tenantId.value, brandingConfig.value)
    originalConfig.value = { ...brandingConfig.value }
    
    toast.add({
      title: 'Branding updated',
      color: 'green'
    })
  } catch (err: any) {
    toast.add({
      title: 'Save failed',
      description: err.message,
      color: 'red'
    })
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadBranding()
})
</script>
