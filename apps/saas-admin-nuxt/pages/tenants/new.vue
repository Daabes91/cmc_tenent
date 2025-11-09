<template>
  <div class="max-w-3xl mx-auto space-y-6">
    <!-- Header -->
    <div>
      <div class="flex items-center gap-2 text-sm text-slate-500 dark:text-slate-400 mb-4">
        <NuxtLink to="/tenants" class="hover:text-primary-600 transition-colors">
          Tenants
        </NuxtLink>
        <UIcon name="i-heroicons-chevron-right" class="w-4 h-4" />
        <span>Create New</span>
      </div>
      <h1 class="text-3xl font-bold text-slate-900 dark:text-white">Create Tenant</h1>
      <p class="text-slate-600 dark:text-slate-400 mt-1">Set up a new tenant organization</p>
    </div>

    <!-- Form -->
    <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
      <TenantForm
        mode="create"
        :loading="loading"
        @submit="handleSubmit"
        @cancel="router.push('/tenants')"
      />
    </div>

    <!-- Success Modal -->
    <UModal v-model="showSuccess" :prevent-close="true">
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full bg-green-100 dark:bg-green-900 flex items-center justify-center">
              <UIcon name="i-heroicons-check-circle" class="w-6 h-6 text-green-600 dark:text-green-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold">Tenant Created</h3>
              <p class="text-sm text-slate-500">{{ createdTenant?.name }}</p>
            </div>
          </div>
        </template>

        <div class="space-y-4">
          <UAlert
            color="amber"
            variant="subtle"
            icon="i-heroicons-exclamation-triangle"
            title="Save These Credentials"
            description="These credentials will only be shown once. Make sure to save them securely."
          />

          <!-- Email -->
          <div>
            <label class="block text-sm font-medium mb-1">Admin Email</label>
            <div class="flex gap-2">
              <UInput
                :model-value="createdTenant?.adminCredentials.email"
                readonly
                size="lg"
                class="flex-1"
              />
              <UButton
                icon="i-heroicons-clipboard-document"
                color="gray"
                variant="outline"
                size="lg"
                @click="copyToClipboard(createdTenant?.adminCredentials.email || '', 'email')"
              >
                {{ copiedField === 'email' ? 'Copied!' : 'Copy' }}
              </UButton>
            </div>
          </div>

          <!-- Password -->
          <div>
            <label class="block text-sm font-medium mb-1">Admin Password</label>
            <div class="flex gap-2">
              <UInput
                :model-value="createdTenant?.adminCredentials.password"
                :type="showPassword ? 'text' : 'password'"
                readonly
                size="lg"
                class="flex-1"
              />
              <UButton
                :icon="showPassword ? 'i-heroicons-eye-slash' : 'i-heroicons-eye'"
                color="gray"
                variant="outline"
                size="lg"
                @click="showPassword = !showPassword"
              />
              <UButton
                icon="i-heroicons-clipboard-document"
                color="gray"
                variant="outline"
                size="lg"
                @click="copyToClipboard(createdTenant?.adminCredentials.password || '', 'password')"
              >
                {{ copiedField === 'password' ? 'Copied!' : 'Copy' }}
              </UButton>
            </div>
          </div>

          <!-- Details -->
          <div class="pt-4 border-t">
            <h4 class="font-medium mb-3">Tenant Details</h4>
            <dl class="grid grid-cols-2 gap-3 text-sm">
              <div>
                <dt class="text-slate-500">Slug</dt>
                <dd class="font-medium">{{ createdTenant?.slug }}</dd>
              </div>
              <div>
                <dt class="text-slate-500">Status</dt>
                <dd>
                  <UBadge color="green" variant="subtle">{{ createdTenant?.status }}</UBadge>
                </dd>
              </div>
            </dl>
          </div>
        </div>

        <template #footer>
          <div class="flex justify-end gap-3">
            <UButton color="gray" variant="ghost" @click="router.push('/tenants')">
              Close
            </UButton>
            <UButton color="primary" @click="router.push(`/tenants/${createdTenant?.id}`)">
              View Tenant
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import type { TenantFormData, TenantCreateResponse } from '~/types'
import TenantForm from '~/components/tenants/TenantForm.vue'

definePageMeta({
  layout: 'default'
})

const router = useRouter()
const api = useSaasApi()
const toast = useToast()
const notifications = useNotifications()

const loading = ref(false)
const showSuccess = ref(false)
const showPassword = ref(false)
const copiedField = ref<'email' | 'password' | null>(null)
const createdTenant = ref<TenantCreateResponse | null>(null)

const handleSubmit = async (data: TenantFormData) => {
  loading.value = true

  try {
    const response = await api.createTenant(data)
    createdTenant.value = response
    showSuccess.value = true

    notifications.success('Tenant Created', `${response.name} has been created successfully`)
  } catch (err: any) {
    const message = err.data?.message || err.message || 'Failed to create tenant'
    notifications.error('Error', message)
    toast.add({
      title: 'Error',
      description: message,
      color: 'red'
    })
  } finally {
    loading.value = false
  }
}

const copyToClipboard = async (text: string, field: 'email' | 'password') => {
  try {
    await navigator.clipboard.writeText(text)
    copiedField.value = field
    toast.add({
      title: 'Copied',
      description: `${field === 'email' ? 'Email' : 'Password'} copied to clipboard`,
      color: 'green'
    })
    setTimeout(() => {
      copiedField.value = null
    }, 2000)
  } catch (error) {
    toast.add({
      title: 'Failed to copy',
      color: 'red'
    })
  }
}
</script>
