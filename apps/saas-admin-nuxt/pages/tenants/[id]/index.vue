<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <div class="flex items-center gap-2 text-sm text-slate-500 dark:text-slate-400 mb-4">
        <NuxtLink to="/tenants" class="hover:text-primary-600 transition-colors">
          Tenants
        </NuxtLink>
        <UIcon name="i-heroicons-chevron-right" class="w-4 h-4" />
        <span>{{ tenant?.name || 'Loading...' }}</span>
      </div>

      <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
        <div>
          <div class="flex items-center gap-3 mb-2">
            <h1 class="text-3xl font-bold text-slate-900 dark:text-white">
              {{ tenant?.name || 'Loading...' }}
            </h1>
            <UBadge
              v-if="tenant"
              :color="tenant.status === 'ACTIVE' ? 'green' : 'gray'"
              size="md"
            >
              {{ tenant.status }}
            </UBadge>
          </div>
          <p v-if="tenant" class="text-sm text-slate-500 font-mono">{{ tenant.slug }}</p>
        </div>

        <div v-if="tenant && !loading" class="flex gap-2">
          <UButton
            color="blue"
            icon="i-heroicons-pencil"
            @click="router.push(`/tenants/${tenant.id}/edit`)"
          >
            Edit
          </UButton>
          <UButton
            color="gray"
            variant="outline"
            icon="i-heroicons-paint-brush"
            @click="router.push(`/tenants/${tenant.id}/branding`)"
          >
            Branding
          </UButton>
          <UButton
            color="red"
            variant="ghost"
            icon="i-heroicons-trash"
            @click="showDeleteModal = true"
          />
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <USkeleton class="h-32" />
        <USkeleton class="h-32" />
        <USkeleton class="h-32" />
        <USkeleton class="h-32" />
      </div>
      <USkeleton class="h-64" />
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

    <!-- Content -->
    <div v-else-if="tenant" class="space-y-6">
      <!-- Stats -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-xs font-medium text-slate-500 uppercase">Created</p>
              <p class="text-lg font-semibold mt-2">{{ formatDate(tenant.createdAt) }}</p>
            </div>
            <div class="w-10 h-10 rounded-lg bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
              <UIcon name="i-heroicons-calendar-days" class="w-5 h-5 text-blue-600 dark:text-blue-400" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-xs font-medium text-slate-500 uppercase">Updated</p>
              <p class="text-lg font-semibold mt-2">{{ formatDate(tenant.updatedAt) }}</p>
            </div>
            <div class="w-10 h-10 rounded-lg bg-emerald-100 dark:bg-emerald-900 flex items-center justify-center">
              <UIcon name="i-heroicons-arrow-path" class="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-xs font-medium text-slate-500 uppercase">Domain</p>
              <p class="text-sm font-semibold mt-2 truncate">{{ tenant.customDomain || 'Not set' }}</p>
            </div>
            <div class="w-10 h-10 rounded-lg bg-violet-100 dark:bg-violet-900 flex items-center justify-center">
              <UIcon name="i-heroicons-globe-alt" class="w-5 h-5 text-violet-600 dark:text-violet-400" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-xs font-medium text-slate-500 uppercase">Status</p>
              <div class="mt-2">
                <UBadge :color="tenant.status === 'ACTIVE' ? 'green' : 'gray'">
                  {{ tenant.status }}
                </UBadge>
              </div>
            </div>
            <div class="w-10 h-10 rounded-lg bg-amber-100 dark:bg-amber-900 flex items-center justify-center">
              <UIcon name="i-heroicons-signal" class="w-5 h-5 text-amber-600 dark:text-amber-400" />
            </div>
          </div>
        </div>
      </div>

      <!-- Metrics -->
      <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700">
        <div class="border-b border-slate-200 dark:border-slate-700 px-6 py-4">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold">Metrics</h2>
            <UButton
              color="gray"
              variant="ghost"
              icon="i-heroicons-arrow-path"
              size="sm"
              :loading="metricsLoading"
              @click="loadMetrics"
            />
          </div>
        </div>

        <div class="p-6">
          <div v-if="metricsLoading" class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <USkeleton class="h-24" />
            <USkeleton class="h-24" />
            <USkeleton class="h-24" />
          </div>

          <div v-else-if="metricsError" class="text-center py-12">
            <UIcon name="i-heroicons-exclamation-triangle" class="w-12 h-12 text-slate-400 mx-auto mb-3" />
            <p class="text-slate-600 dark:text-slate-400">{{ metricsError }}</p>
          </div>

          <div v-else-if="metrics" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div class="rounded-lg border border-blue-200 dark:border-blue-800 bg-blue-50 dark:bg-blue-900/20 p-4">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 rounded-lg bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                  <UIcon name="i-heroicons-users" class="w-6 h-6 text-blue-600 dark:text-blue-400" />
                </div>
                <div>
                  <p class="text-xs text-slate-600 dark:text-slate-400">Total Users</p>
                  <p class="text-2xl font-bold">{{ metrics.userCount }}</p>
                </div>
              </div>
            </div>

            <div class="rounded-lg border border-emerald-200 dark:border-emerald-800 bg-emerald-50 dark:bg-emerald-900/20 p-4">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 rounded-lg bg-emerald-100 dark:bg-emerald-900 flex items-center justify-center">
                  <UIcon name="i-heroicons-user-group" class="w-6 h-6 text-emerald-600 dark:text-emerald-400" />
                </div>
                <div>
                  <p class="text-xs text-slate-600 dark:text-slate-400">Staff</p>
                  <p class="text-2xl font-bold">{{ metrics.staffCount }}</p>
                </div>
              </div>
            </div>

            <div class="rounded-lg border border-violet-200 dark:border-violet-800 bg-violet-50 dark:bg-violet-900/20 p-4">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 rounded-lg bg-violet-100 dark:bg-violet-900 flex items-center justify-center">
                  <UIcon name="i-heroicons-heart" class="w-6 h-6 text-violet-600 dark:text-violet-400" />
                </div>
                <div>
                  <p class="text-xs text-slate-600 dark:text-slate-400">Patients</p>
                  <p class="text-2xl font-bold">{{ metrics.patientCount }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <UModal v-model="showDeleteModal">
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-red-100 dark:bg-red-900 flex items-center justify-center">
              <UIcon name="i-heroicons-exclamation-triangle" class="w-6 h-6 text-red-600 dark:text-red-400" />
            </div>
            <h3 class="text-lg font-semibold">Delete Tenant</h3>
          </div>
        </template>

        <div class="space-y-4">
          <p>Are you sure you want to delete <strong>{{ tenant?.name }}</strong>?</p>

          <UAlert
            color="red"
            variant="subtle"
            icon="i-heroicons-exclamation-triangle"
            title="Warning"
            description="This will archive the tenant's data and prevent all access. This cannot be undone."
          />
        </div>

        <template #footer>
          <div class="flex justify-end gap-3">
            <UButton
              color="gray"
              variant="ghost"
              :disabled="deleteLoading"
              @click="showDeleteModal = false"
            >
              Cancel
            </UButton>
            <UButton
              color="red"
              :loading="deleteLoading"
              @click="handleDelete"
            >
              Delete Tenant
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import type { Tenant, TenantMetrics } from '~/types'

const route = useRoute()
const router = useRouter()
const api = useSaasApi()
const toast = useToast()

const tenant = ref<Tenant | null>(null)
const metrics = ref<TenantMetrics | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const metricsLoading = ref(false)
const metricsError = ref<string | null>(null)
const showDeleteModal = ref(false)
const deleteLoading = ref(false)

const tenantId = computed(() => Number(route.params.id))

const loadTenant = async () => {
  loading.value = true
  error.value = null

  try {
    tenant.value = await api.getTenant(tenantId.value, true)
  } catch (err: any) {
    error.value = err.message || 'Failed to load tenant'
  } finally {
    loading.value = false
  }
}

const loadMetrics = async () => {
  metricsLoading.value = true
  metricsError.value = null

  try {
    metrics.value = await api.getTenantMetrics(tenantId.value)
  } catch (err: any) {
    metricsError.value = err.message || 'Failed to load metrics'
  } finally {
    metricsLoading.value = false
  }
}

const handleDelete = async () => {
  deleteLoading.value = true

  try {
    await api.deleteTenant(tenantId.value)
    toast.add({
      title: 'Tenant deleted',
      color: 'green'
    })
    router.push('/tenants')
  } catch (err: any) {
    toast.add({
      title: 'Delete failed',
      description: err.message,
      color: 'red'
    })
  } finally {
    deleteLoading.value = false
    showDeleteModal.value = false
  }
}

const formatDate = (timestamp: number | string) => {
  const date = typeof timestamp === 'number' 
    ? new Date(timestamp * 1000)
    : new Date(timestamp)
  
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  }).format(date)
}

onMounted(async () => {
  await loadTenant()
  if (tenant.value) {
    loadMetrics()
  }
})
</script>
