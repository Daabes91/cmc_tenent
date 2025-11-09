<template>
  <div class="max-w-3xl mx-auto space-y-6">
    <!-- Header -->
    <div>
      <div class="flex items-center gap-2 text-sm text-slate-500 dark:text-slate-400 mb-4">
        <NuxtLink to="/tenants" class="hover:text-primary-600 transition-colors">
          Tenants
        </NuxtLink>
        <UIcon name="i-heroicons-chevron-right" class="w-4 h-4" />
        <NuxtLink :to="`/tenants/${tenantId}`" class="hover:text-primary-600 transition-colors">
          {{ tenant?.name || 'Tenant' }}
        </NuxtLink>
        <UIcon name="i-heroicons-chevron-right" class="w-4 h-4" />
        <span>Edit</span>
      </div>
      <h1 class="text-3xl font-bold text-slate-900 dark:text-white">Edit Tenant</h1>
      <p class="text-slate-600 dark:text-slate-400 mt-1">Update tenant information</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-4">
      <USkeleton class="h-64" />
      <USkeleton class="h-32" />
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
        <UButton color="red" variant="ghost" @click="loadTenant">Retry</UButton>
      </template>
    </UAlert>

    <!-- Form -->
    <div v-else-if="tenant" class="space-y-6">
      <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
        <div class="border-b border-slate-200 dark:border-slate-700 px-6 py-4">
          <h2 class="text-lg font-semibold">Tenant Information</h2>
        </div>
        <div class="p-6">
          <TenantForm
            mode="edit"
            :initial-data="formData"
            :loading="saving"
            @submit="handleSubmit"
            @cancel="router.push(`/tenants/${tenantId}`)"
          />
        </div>
      </div>

      <!-- Status Management -->
      <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
        <div class="border-b border-slate-200 dark:border-slate-700 px-6 py-4">
          <h2 class="text-lg font-semibold">Status Management</h2>
        </div>
        <div class="p-6">
          <div class="space-y-4">
            <div class="flex items-center justify-between p-4 bg-slate-50 dark:bg-slate-900 rounded-lg">
              <div>
                <h3 class="font-medium">Current Status</h3>
                <p class="text-sm text-slate-500 mt-1">
                  {{ tenant.status === 'ACTIVE'
                    ? 'Tenant is active and accessible'
                    : 'Tenant is inactive and users cannot access'
                  }}
                </p>
              </div>
              <UBadge
                :color="tenant.status === 'ACTIVE' ? 'green' : 'gray'"
                size="lg"
              >
                {{ tenant.status }}
              </UBadge>
            </div>

            <div class="flex items-center justify-between">
              <div>
                <h4 class="font-medium">Change Status</h4>
                <p class="text-sm text-slate-500 mt-1">
                  {{ tenant.status === 'ACTIVE'
                    ? 'Deactivate to prevent user access'
                    : 'Activate to restore user access'
                  }}
                </p>
              </div>
              <UButton
                :color="tenant.status === 'ACTIVE' ? 'red' : 'green'"
                variant="outline"
                :loading="statusChanging"
                @click="showStatusModal = true"
              >
                {{ tenant.status === 'ACTIVE' ? 'Deactivate' : 'Activate' }}
              </UButton>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Status Change Modal -->
    <UModal v-model="showStatusModal">
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <div 
              class="w-10 h-10 rounded-lg flex items-center justify-center"
              :class="tenant?.status === 'ACTIVE' ? 'bg-red-100 dark:bg-red-900' : 'bg-green-100 dark:bg-green-900'"
            >
              <UIcon 
                name="i-heroicons-exclamation-triangle" 
                class="w-6 h-6"
                :class="tenant?.status === 'ACTIVE' ? 'text-red-600 dark:text-red-400' : 'text-green-600 dark:text-green-400'"
              />
            </div>
            <h3 class="text-lg font-semibold">
              {{ tenant?.status === 'ACTIVE' ? 'Deactivate' : 'Activate' }} Tenant
            </h3>
          </div>
        </template>

        <div class="space-y-4">
          <p>
            Are you sure you want to {{ tenant?.status === 'ACTIVE' ? 'deactivate' : 'activate' }} 
            <strong>{{ tenant?.name }}</strong>?
          </p>

          <UAlert
            :color="tenant?.status === 'ACTIVE' ? 'red' : 'green'"
            variant="subtle"
            :icon="tenant?.status === 'ACTIVE' ? 'i-heroicons-exclamation-triangle' : 'i-heroicons-check-circle'"
            :title="tenant?.status === 'ACTIVE' ? 'Warning' : 'Confirmation'"
            :description="tenant?.status === 'ACTIVE' 
              ? 'Deactivating will immediately prevent all users from accessing their system.' 
              : 'Activating will restore access for all users.'"
          />
        </div>

        <template #footer>
          <div class="flex justify-end gap-3">
            <UButton
              color="gray"
              variant="ghost"
              :disabled="statusChanging"
              @click="showStatusModal = false"
            >
              Cancel
            </UButton>
            <UButton
              :color="tenant?.status === 'ACTIVE' ? 'red' : 'green'"
              :loading="statusChanging"
              @click="handleStatusChange"
            >
              {{ tenant?.status === 'ACTIVE' ? 'Deactivate' : 'Activate' }}
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import type { Tenant, TenantFormData } from '~/types'
import TenantForm from '~/components/tenants/TenantForm.vue'

const route = useRoute()
const router = useRouter()
const api = useSaasApi()
const toast = useToast()
const notifications = useNotifications()

const tenant = ref<Tenant | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const saving = ref(false)
const statusChanging = ref(false)
const showStatusModal = ref(false)

const tenantId = computed(() => Number(route.params.id))

const formData = computed(() => {
  if (!tenant.value) return {}
  return {
    slug: tenant.value.slug,
    name: tenant.value.name,
    customDomain: tenant.value.customDomain || ''
  }
})

const loadTenant = async () => {
  loading.value = true
  error.value = null

  try {
    tenant.value = await api.getTenant(tenantId.value)
  } catch (err: any) {
    error.value = err.message || 'Failed to load tenant'
  } finally {
    loading.value = false
  }
}

const handleSubmit = async (data: TenantFormData) => {
  saving.value = true

  try {
    const updateData: Partial<TenantFormData> = {
      name: data.name,
      customDomain: data.customDomain || undefined
    }

    const updated = await api.updateTenant(tenantId.value, updateData)
    
    notifications.success('Tenant Updated', `${updated.name} has been updated`)
    toast.add({
      title: 'Tenant updated',
      color: 'green'
    })

    tenant.value = updated
    router.push(`/tenants/${tenantId.value}`)
  } catch (err: any) {
    notifications.error('Error', err.message || 'Failed to update tenant')
    toast.add({
      title: 'Update failed',
      description: err.message,
      color: 'red'
    })
  } finally {
    saving.value = false
  }
}

const handleStatusChange = async () => {
  if (!tenant.value) return

  statusChanging.value = true

  try {
    const newStatus = tenant.value.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    
    const updated = await api.updateTenant(tenantId.value, {
      name: tenant.value.name,
      customDomain: tenant.value.customDomain || undefined,
      status: newStatus
    } as any)

    toast.add({
      title: 'Status updated',
      description: `Tenant is now ${newStatus}`,
      color: 'green'
    })

    tenant.value = updated
    showStatusModal.value = false
  } catch (err: any) {
    toast.add({
      title: 'Status change failed',
      description: err.message,
      color: 'red'
    })
  } finally {
    statusChanging.value = false
  }
}

onMounted(() => {
  loadTenant()
})
</script>
