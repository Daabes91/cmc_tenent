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
            color="emerald"
            variant="outline"
            icon="i-heroicons-credit-card"
            @click="router.push(`/tenants/${tenant.id}/billing`)"
          >
            Billing
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
      <!-- Billing Status Card -->
      <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
        <div class="border-b border-slate-200 dark:border-slate-700 px-6 py-4">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold">Billing Status</h2>
            <UButton
              v-if="tenant.billingStatus !== 'ACTIVE'"
              color="blue"
              variant="ghost"
              icon="i-heroicons-pencil"
              size="sm"
              @click="showBillingOverrideModal = true"
            >
              Override
            </UButton>
          </div>
        </div>

        <div class="p-6">
          <div class="flex items-start gap-4">
            <div :class="[
              'w-16 h-16 rounded-xl flex items-center justify-center',
              getBillingStatusBgClass(tenant.billingStatus)
            ]">
              <UIcon :name="getBillingStatusIcon(tenant.billingStatus)" :class="[
                'w-8 h-8',
                getBillingStatusIconClass(tenant.billingStatus)
              ]" />
            </div>
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-2">
                <h3 class="text-2xl font-bold">{{ formatBillingStatus(tenant.billingStatus) }}</h3>
                <UBadge :color="getBillingStatusColor(tenant.billingStatus)" size="lg">
                  {{ tenant.billingStatus }}
                </UBadge>
              </div>
              <p class="text-slate-600 dark:text-slate-400">
                {{ getBillingStatusDescription(tenant.billingStatus) }}
              </p>

              <!-- Subscription Details -->
              <div
                v-if="subscription && tenant.billingStatus !== 'PENDING_PAYMENT'"
                class="mt-4 pt-4 border-t border-slate-200 dark:border-slate-700"
              >
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <p class="text-xs text-slate-500 uppercase mb-1">Next Billing Date</p>
                    <p class="font-semibold">{{ formatDate(subscription.currentPeriodEnd) }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-slate-500 uppercase mb-1">Subscription ID</p>
                    <p class="font-mono text-sm">{{ subscription.paypalSubscriptionId }}</p>
                  </div>
                </div>
              </div>

              <!-- Loading subscription -->
              <div v-else-if="subscriptionLoading" class="mt-4 pt-4 border-t border-slate-200 dark:border-slate-700">
                <USkeleton class="h-16" />
              </div>
            </div>
          </div>
        </div>
      </div>

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

    <!-- Billing Override Modal -->
    <UModal v-model="showBillingOverrideModal">
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
              <UIcon name="i-heroicons-credit-card" class="w-6 h-6 text-blue-600 dark:text-blue-400" />
            </div>
            <h3 class="text-lg font-semibold">Override Billing Status</h3>
          </div>
        </template>

        <div class="space-y-4">
          <p>Manually override the billing status for <strong>{{ tenant?.name }}</strong></p>

          <div>
            <label class="block text-sm font-medium mb-2">New Billing Status</label>
            <USelect
              v-model="billingOverride.status"
              :options="billingStatusOptions"
              size="lg"
            />
          </div>

          <div>
            <label class="block text-sm font-medium mb-2">Reason for Override</label>
            <UTextarea
              v-model="billingOverride.reason"
              placeholder="Explain why you're overriding the billing status..."
              :rows="3"
            />
          </div>

          <UAlert
            color="amber"
            variant="subtle"
            icon="i-heroicons-exclamation-triangle"
            title="Warning"
            description="This will manually change the billing status. This action is logged for audit purposes."
          />
        </div>

        <template #footer>
          <div class="flex justify-end gap-3">
            <UButton
              color="gray"
              variant="ghost"
              :disabled="billingOverrideLoading"
              @click="showBillingOverrideModal = false"
            >
              Cancel
            </UButton>
            <UButton
              color="blue"
              :loading="billingOverrideLoading"
              :disabled="!billingOverride.status || !billingOverride.reason"
              @click="handleBillingOverride"
            >
              Update Billing Status
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import type { Tenant, TenantMetrics, Subscription } from '~/types'

const route = useRoute()
const router = useRouter()
const api = useSaasApi()
const toast = useToast()

const tenant = ref<Tenant | null>(null)
const metrics = ref<TenantMetrics | null>(null)
const subscription = ref<Subscription | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const metricsLoading = ref(false)
const metricsError = ref<string | null>(null)
const subscriptionLoading = ref(false)
const showDeleteModal = ref(false)
const deleteLoading = ref(false)
const showBillingOverrideModal = ref(false)
const billingOverrideLoading = ref(false)

const billingOverride = reactive({
  status: '',
  reason: ''
})

const billingStatusOptions = [
  { label: 'Active', value: 'ACTIVE' },
  { label: 'Pending Payment', value: 'PENDING_PAYMENT' },
  { label: 'Past Due', value: 'PAST_DUE' },
  { label: 'Suspended', value: 'SUSPENDED' },
  { label: 'Canceled', value: 'CANCELED' }
]

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

const loadSubscription = async () => {
  if (!tenant.value || tenant.value.billingStatus === 'PENDING_PAYMENT') {
    return
  }

  subscriptionLoading.value = true

  try {
    subscription.value = await api.getTenantSubscription(tenantId.value)
  } catch (err: any) {
    console.error('Failed to load subscription:', err)
    // Don't show error to user, subscription might not exist yet
  } finally {
    subscriptionLoading.value = false
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

const handleBillingOverride = async () => {
  billingOverrideLoading.value = true

  try {
    await api.updateBillingStatus(
      tenantId.value,
      billingOverride.status,
      billingOverride.reason
    )
    
    toast.add({
      title: 'Billing status updated',
      description: `Status changed to ${formatBillingStatus(billingOverride.status)}`,
      color: 'green'
    })
    
    // Reload tenant data
    await loadTenant()
    await loadSubscription()
    
    showBillingOverrideModal.value = false
    billingOverride.status = ''
    billingOverride.reason = ''
  } catch (err: any) {
    toast.add({
      title: 'Update failed',
      description: err.message,
      color: 'red'
    })
  } finally {
    billingOverrideLoading.value = false
  }
}

const formatDate = (timestamp: number | string | null | undefined) => {
  if (timestamp === null || timestamp === undefined) {
    return 'Not available'
  }

  const date = typeof timestamp === 'number'
    ? new Date(timestamp * 1000)
    : new Date(timestamp)

  if (Number.isNaN(date.getTime())) {
    return 'Not available'
  }

  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  }).format(date)
}

const getBillingStatusColor = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'green'
    case 'PENDING_PAYMENT':
      return 'yellow'
    case 'PAST_DUE':
      return 'orange'
    case 'SUSPENDED':
      return 'blue'
    case 'CANCELED':
      return 'red'
    default:
      return 'gray'
  }
}

const getBillingStatusIcon = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'i-heroicons-check-circle'
    case 'PENDING_PAYMENT':
      return 'i-heroicons-clock'
    case 'PAST_DUE':
      return 'i-heroicons-exclamation-triangle'
    case 'SUSPENDED':
      return 'i-heroicons-pause-circle'
    case 'CANCELED':
      return 'i-heroicons-x-circle'
    default:
      return 'i-heroicons-question-mark-circle'
  }
}

const getBillingStatusBgClass = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'bg-green-100 dark:bg-green-900'
    case 'PENDING_PAYMENT':
      return 'bg-yellow-100 dark:bg-yellow-900'
    case 'PAST_DUE':
      return 'bg-orange-100 dark:bg-orange-900'
    case 'SUSPENDED':
      return 'bg-blue-100 dark:bg-blue-900'
    case 'CANCELED':
      return 'bg-red-100 dark:bg-red-900'
    default:
      return 'bg-gray-100 dark:bg-gray-900'
  }
}

const getBillingStatusIconClass = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'text-green-600 dark:text-green-400'
    case 'PENDING_PAYMENT':
      return 'text-yellow-600 dark:text-yellow-400'
    case 'PAST_DUE':
      return 'text-orange-600 dark:text-orange-400'
    case 'SUSPENDED':
      return 'text-blue-600 dark:text-blue-400'
    case 'CANCELED':
      return 'text-red-600 dark:text-red-400'
    default:
      return 'text-gray-600 dark:text-gray-400'
  }
}

const formatBillingStatus = (status: string) => {
  return status.split('_').map(word => 
    word.charAt(0) + word.slice(1).toLowerCase()
  ).join(' ')
}

const getBillingStatusDescription = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'This tenant has an active subscription and full access to the platform.'
    case 'PENDING_PAYMENT':
      return 'This tenant has been created but payment has not been completed yet.'
    case 'PAST_DUE':
      return 'This tenant\'s payment is overdue. Access may be restricted.'
    case 'SUSPENDED':
      return 'This tenant\'s subscription is suspended and can be reactivated.'
    case 'CANCELED':
      return 'This tenant\'s subscription has been canceled and access is restricted.'
    default:
      return 'Unknown billing status.'
  }
}

onMounted(async () => {
  await loadTenant()
  if (tenant.value) {
    loadMetrics()
    loadSubscription()
  }
})
</script>
