<template>
  <form @submit.prevent="handleSubmit" class="space-y-6">
    <!-- Slug -->
    <UFormGroup
      label="Slug"
      name="slug"
      :error="errors.slug"
      required
      help="Unique identifier for the tenant (lowercase, alphanumeric, hyphens)"
    >
      <UInput
        v-model="form.slug"
        placeholder="my-clinic"
        size="lg"
        :disabled="mode === 'edit' || loading"
        @input="handleSlugInput"
      />
      <div v-if="slugChecking" class="mt-2 flex items-center gap-2 text-sm text-slate-500">
        <UIcon name="i-heroicons-arrow-path" class="w-4 h-4 animate-spin" />
        <span>Checking availability...</span>
      </div>
      <div v-else-if="slugAvailable && form.slug && mode === 'create'" class="mt-2 flex items-center gap-2 text-sm text-green-600">
        <UIcon name="i-heroicons-check-circle" class="w-4 h-4" />
        <span>Slug is available</span>
      </div>
    </UFormGroup>

    <!-- Admin Email Preview -->
    <UAlert
      v-if="mode === 'create' && form.slug"
      color="blue"
      variant="subtle"
      icon="i-heroicons-information-circle"
      title="Admin Email"
      :description="`Admin account will be created with email: admin@${form.slug}.clinic.com`"
    />

    <!-- Name -->
    <UFormGroup
      label="Name"
      name="name"
      :error="errors.name"
      required
      help="Display name for the tenant"
    >
      <UInput
        v-model="form.name"
        placeholder="My Clinic"
        size="lg"
        :disabled="loading"
      />
    </UFormGroup>

    <!-- Custom Domain -->
    <UFormGroup
      label="Custom Domain"
      name="customDomain"
      :error="errors.customDomain"
      help="Optional custom domain (e.g., clinic.example.com)"
    >
      <UInput
        v-model="form.customDomain"
        placeholder="clinic.example.com"
        size="lg"
        :disabled="loading"
      />
    </UFormGroup>

    <!-- Actions -->
    <div class="flex items-center justify-end gap-3 pt-4 border-t border-slate-200 dark:border-slate-700">
      <UButton
        color="gray"
        variant="ghost"
        size="lg"
        :disabled="loading"
        @click="$emit('cancel')"
      >
        Cancel
      </UButton>
      <UButton
        type="submit"
        color="primary"
        size="lg"
        :loading="loading"
        :disabled="!isValid || slugChecking"
      >
        {{ mode === 'create' ? 'Create Tenant' : 'Save Changes' }}
      </UButton>
    </div>
  </form>
</template>

<script setup lang="ts">
import type { TenantFormData } from '~/types'

interface Props {
  mode: 'create' | 'edit'
  initialData?: Partial<TenantFormData>
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'create',
  loading: false
})

const emit = defineEmits<{
  submit: [data: TenantFormData]
  cancel: []
}>()

const api = useSaasApi()

const form = reactive<TenantFormData>({
  slug: props.initialData?.slug || '',
  name: props.initialData?.name || '',
  customDomain: props.initialData?.customDomain || ''
})

const errors = reactive({
  slug: '',
  name: '',
  customDomain: ''
})

const slugChecking = ref(false)
const slugAvailable = ref(false)
let slugCheckTimer: NodeJS.Timeout | null = null

const isValid = computed(() => {
  return (
    form.slug.trim() !== '' &&
    form.name.trim() !== '' &&
    !errors.slug &&
    !errors.name &&
    !errors.customDomain &&
    (props.mode === 'edit' || slugAvailable.value)
  )
})

const validateSlugFormat = (slug: string): string | null => {
  if (!slug) return 'Slug is required'
  if (slug.length < 3) return 'Slug must be at least 3 characters'
  if (slug.length > 50) return 'Slug must be less than 50 characters'
  if (!/^[a-z0-9-]+$/.test(slug)) return 'Slug can only contain lowercase letters, numbers, and hyphens'
  if (slug.startsWith('-') || slug.endsWith('-')) return 'Slug cannot start or end with a hyphen'
  if (slug.includes('--')) return 'Slug cannot contain consecutive hyphens'
  return null
}

const handleSlugInput = (event: Event) => {
  const input = event.target as HTMLInputElement
  let value = input.value.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '')
  form.slug = value

  slugAvailable.value = false
  errors.slug = ''

  const formatError = validateSlugFormat(value)
  if (formatError) {
    errors.slug = formatError
    return
  }

  if (props.mode === 'create') {
    if (slugCheckTimer) clearTimeout(slugCheckTimer)
    slugCheckTimer = setTimeout(() => checkSlugAvailability(value), 500)
  }
}

const checkSlugAvailability = async (slug: string) => {
  if (!slug || props.mode === 'edit') return

  slugChecking.value = true
  try {
    const response = await api.getTenants({ search: slug, size: 1 })
    const exactMatch = response.content.find(t => t.slug === slug)
    
    if (exactMatch) {
      slugAvailable.value = false
      errors.slug = 'This slug is already taken'
    } else {
      slugAvailable.value = true
      errors.slug = ''
    }
  } catch (error) {
    console.error('Error checking slug:', error)
  } finally {
    slugChecking.value = false
  }
}

const validateForm = (): boolean => {
  let valid = true

  if (!form.slug.trim()) {
    errors.slug = 'Slug is required'
    valid = false
  }

  if (!form.name.trim()) {
    errors.name = 'Name is required'
    valid = false
  } else if (form.name.length < 2) {
    errors.name = 'Name must be at least 2 characters'
    valid = false
  }

  if (form.customDomain && !/^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?(\.[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?)*$/i.test(form.customDomain)) {
    errors.customDomain = 'Invalid domain format'
    valid = false
  }

  return valid
}

const handleSubmit = () => {
  if (!validateForm()) return

  emit('submit', {
    slug: form.slug,
    name: form.name,
    customDomain: form.customDomain || undefined
  })
}

watch(() => form.name, () => {
  if (errors.name) errors.name = ''
})

watch(() => form.customDomain, () => {
  if (errors.customDomain) errors.customDomain = ''
})

onUnmounted(() => {
  if (slugCheckTimer) clearTimeout(slugCheckTimer)
})
</script>
