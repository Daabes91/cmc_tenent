<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-3xl font-bold text-slate-900 dark:text-white">Tenants</h1>
        <p class="text-slate-600 dark:text-slate-400 mt-1">Manage all tenant organizations</p>
      </div>
      <UButton
        icon="i-heroicons-plus"
        color="primary"
        size="lg"
        @click="navigateTo('/tenants/new')"
      >
        Create Tenant
      </UButton>
    </div>

    <!-- Filters -->
    <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-4">
      <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
        <div class="md:col-span-2">
          <UInput
            v-model="filters.search"
            icon="i-heroicons-magnifying-glass"
            placeholder="Search tenants..."
            size="lg"
            @input="debouncedSearch"
          />
        </div>
        <USelect
          v-model="filters.status"
          :options="statusOptions"
          size="lg"
          @change="loadTenants"
        />
        <USelect
          v-model="filters.billingStatus"
          :options="billingStatusOptions"
          size="lg"
          @change="loadTenants"
        />
        <USelect
          v-model="filters.planTier"
          :options="planTierOptions"
          size="lg"
          @change="loadTenants"
        />
      </div>
    </div>

    <!-- Loading -->
    <LoadingSkeleton v-if="loading" type="table" :rows="5" />

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
        <UButton color="red" variant="ghost" @click="loadTenants">Retry</UButton>
      </template>
    </UAlert>

    <!-- Table -->
    <div v-if="!loading && !error" class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
      <TenantTable
        :tenants="tenants"
        @view="viewTenant"
        @edit="editTenant"
      />
    </div>

    <!-- Pagination -->
    <div v-if="totalElements > pageSize" class="flex justify-center">
      <UPagination
        v-model="currentPage"
        :page-count="pageSize"
        :total="totalElements"
        @update:model-value="loadTenants"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Tenant } from '~/types'
import TenantTable from '~/components/tenants/TenantTable.vue'

definePageMeta({
  layout: 'default'
})

const router = useRouter()
const route = useRoute()
const api = useSaasApi()
const { debounce } = useDebounce()

const tenants = ref<Tenant[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const totalElements = ref(0)
const currentPage = ref(Number(route.query.page) || 1)
const pageSize = ref(20)

const filters = reactive({
  search: (route.query.search as string) || '',
  status: (route.query.status as string) || 'all',
  billingStatus: (route.query.billingStatus as string) || 'all',
  planTier: (route.query.planTier as string) || 'all'
})

const statusOptions = [
  { label: 'All Statuses', value: 'all' },
  { label: 'Active', value: 'ACTIVE' },
  { label: 'Inactive', value: 'INACTIVE' }
]

const billingStatusOptions = [
  { label: 'All Billing Statuses', value: 'all' },
  { label: 'Active', value: 'ACTIVE' },
  { label: 'Pending Payment', value: 'PENDING_PAYMENT' },
  { label: 'Past Due', value: 'PAST_DUE' },
  { label: 'Suspended', value: 'SUSPENDED' },
  { label: 'Canceled', value: 'CANCELED' }
]

const planTierOptions = [
  { label: 'All Plan Tiers', value: 'all' },
  { label: 'Basic', value: 'BASIC' },
  { label: 'Professional', value: 'PROFESSIONAL' },
  { label: 'Enterprise', value: 'ENTERPRISE' },
  { label: 'Custom', value: 'CUSTOM' }
]

const loadTenants = async () => {
  loading.value = true
  error.value = null

  try {
    const params: any = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: 'createdAt',
      sortDirection: 'desc'
    }

    if (filters.search) params.search = filters.search
    if (filters.status !== 'all') params.status = filters.status
    if (filters.billingStatus !== 'all') params.billingStatus = filters.billingStatus
    if (filters.planTier !== 'all') params.planTier = filters.planTier

    const response = await api.getTenants(params)
    tenants.value = response.content || []
    totalElements.value = response.totalElements || 0

    updateUrl()
  } catch (err: any) {
    console.error('Error loading tenants:', err)
    error.value = err.message || 'Failed to load tenants'
  } finally {
    loading.value = false
  }
}

const updateUrl = () => {
  const query: Record<string, string> = {}
  if (currentPage.value > 1) query.page = String(currentPage.value)
  if (filters.search) query.search = filters.search
  if (filters.status !== 'all') query.status = filters.status
  if (filters.billingStatus !== 'all') query.billingStatus = filters.billingStatus
  if (filters.planTier !== 'all') query.planTier = filters.planTier
  router.push({ query })
}

const debouncedSearch = debounce(() => {
  currentPage.value = 1
  loadTenants()
}, 300)

const viewTenant = (id: number) => {
  router.push(`/tenants/${id}`)
}

const editTenant = (id: number) => {
  router.push(`/tenants/${id}/edit`)
}

onMounted(() => {
  loadTenants()
})
</script>
