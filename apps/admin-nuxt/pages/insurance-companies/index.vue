<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-shield-check" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('insuranceCompanies.list.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('insuranceCompanies.list.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="loading"
              @click="loadCompanies"
            >
              {{ t('insuranceCompanies.list.actions.refresh') }}
            </UButton>
            <UButton 
              color="blue" 
              icon="i-lucide-plus" 
              to="/insurance-companies/new"
            >
              {{ t('insuranceCompanies.list.actions.addCompany') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">

      <!-- Quick Stats -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
              <UIcon name="i-lucide-building-2" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('insuranceCompanies.list.metrics.total.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ companies?.length || 0 }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('insuranceCompanies.list.metrics.total.caption') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
              <UIcon name="i-lucide-check-circle" class="h-5 w-5 text-green-600 dark:text-green-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('insuranceCompanies.list.metrics.active.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ activeCompanies }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('insuranceCompanies.list.metrics.active.caption') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-red-50 dark:bg-red-900/20">
              <UIcon name="i-lucide-x-circle" class="h-5 w-5 text-red-600 dark:text-red-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('insuranceCompanies.list.metrics.inactive.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ inactiveCompanies }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('insuranceCompanies.list.metrics.inactive.caption') }}</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-50 dark:bg-purple-900/20">
              <UIcon name="i-lucide-image" class="h-5 w-5 text-purple-600 dark:text-purple-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('insuranceCompanies.list.metrics.withLogos.label') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ companiesWithLogos }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ t('insuranceCompanies.list.metrics.withLogos.caption') }}</p>
        </div>
      </div>

      <!-- Search and Filters -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden mb-6">
        <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-search" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('insuranceCompanies.list.filters.title') }}</h2>
              <p class="text-sm text-slate-300">{{ t('insuranceCompanies.list.filters.subtitle') }}</p>
            </div>
          </div>
        </div>
        <div class="p-6">
          <div class="flex flex-col sm:flex-row gap-4">
            <div class="flex-1">
              <UFormGroup :label="t('insuranceCompanies.list.filters.searchLabel')">
                <UInput
                  v-model="searchQuery"
                  size="lg"
                  :placeholder="t('insuranceCompanies.list.filters.searchPlaceholder')"
                  icon="i-lucide-search"
                />
              </UFormGroup>
            </div>
            <div class="sm:w-48">
              <UFormGroup :label="t('insuranceCompanies.list.filters.statusLabel')">
                <USelect
                  v-model="statusFilter"
                  size="lg"
                  :options="statusOptions"
                  value-attribute="value"
                  label-attribute="label"
                />
              </UFormGroup>
            </div>
          </div>
        </div>
      </div>

      <!-- Companies Grid -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-shield-check" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('insuranceCompanies.list.directory.title') }}</h2>
              <p class="text-sm text-blue-100">{{ t('insuranceCompanies.list.directory.count', { count: filteredCompanies.length }) }}</p>
            </div>
          </div>
        </div>

        <div v-if="loading" class="p-6">
          <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            <div v-for="i in 6" :key="i" class="animate-pulse">
              <div class="bg-slate-200 dark:bg-slate-700 rounded-xl p-6 space-y-4">
                <div class="flex items-center gap-4">
                  <div class="w-12 h-12 bg-slate-300 dark:bg-slate-600 rounded-xl"></div>
                  <div class="flex-1 space-y-2">
                    <div class="h-4 bg-slate-300 dark:bg-slate-600 rounded w-3/4"></div>
                    <div class="h-3 bg-slate-300 dark:bg-slate-600 rounded w-1/2"></div>
                  </div>
                </div>
                <div class="h-3 bg-slate-300 dark:bg-slate-600 rounded w-full"></div>
                <div class="h-3 bg-slate-300 dark:bg-slate-600 rounded w-2/3"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="filteredCompanies.length > 0" class="p-6">
          <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            <div
              v-for="company in filteredCompanies"
              :key="company.id"
              class="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-6 border border-slate-200 dark:border-slate-600 hover:shadow-md hover:border-blue-300 dark:hover:border-blue-600 transition-all duration-200 group"
            >
              <!-- Company Header -->
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center gap-3">
                  <div class="relative">
                    <div v-if="company.logoUrl" class="h-12 w-12 rounded-xl overflow-hidden bg-white shadow-sm">
                      <img
                        :src="company.logoUrl"
                        :alt="company.nameEn"
                        class="h-full w-full object-contain p-1"
                      >
                    </div>
                    <div v-else class="h-12 w-12 rounded-xl bg-gradient-to-br from-blue-100 to-indigo-100 dark:from-blue-900/30 dark:to-indigo-900/30 flex items-center justify-center">
                      <UIcon name="i-lucide-shield-check" class="h-6 w-6 text-blue-600 dark:text-blue-400" />
                    </div>
                    <div
                      :class="[
                        'absolute -top-1 -right-1 w-4 h-4 rounded-full border-2 border-white dark:border-slate-700',
                        company.isActive ? 'bg-green-500' : 'bg-red-500'
                      ]"
                    ></div>
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="font-semibold text-slate-900 dark:text-white truncate">
                      {{ company.nameEn }}
                    </h3>
                    <p v-if="company.nameAr" class="text-sm text-slate-500 dark:text-slate-400 truncate">
                      {{ company.nameAr }}
                    </p>
                  </div>
                </div>
                <UBadge
                  :color="company.isActive ? 'green' : 'red'"
                  variant="soft"
                  size="sm"
                >
                  {{ company.isActive ? t('insuranceCompanies.list.status.active') : t('insuranceCompanies.list.status.inactive') }}
                </UBadge>
              </div>

              <!-- Company Details -->
              <div class="space-y-3 mb-4">
                <div v-if="company.email" class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                  <UIcon name="i-lucide-mail" class="h-4 w-4 text-slate-400" />
                  <span class="truncate">{{ company.email }}</span>
                </div>
                <div v-if="company.phone" class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                  <UIcon name="i-lucide-phone" class="h-4 w-4 text-slate-400" />
                  <span>{{ company.phone }}</span>
                </div>
                <div v-if="company.websiteUrl" class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                  <UIcon name="i-lucide-globe" class="h-4 w-4 text-slate-400" />
                  <a :href="company.websiteUrl" target="_blank" class="truncate hover:text-blue-600 dark:hover:text-blue-400">
                    {{ company.websiteUrl }}
                  </a>
                </div>
                <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                  <UIcon name="i-lucide-hash" class="h-4 w-4 text-slate-400" />
                  <span>{{ t('insuranceCompanies.common.order', { order: company.displayOrder }) }}</span>
                </div>
              </div>

              <!-- Description -->
              <div v-if="company.descriptionEn" class="mb-4">
                <p class="text-sm text-slate-600 dark:text-slate-300 line-clamp-2">
                  {{ company.descriptionEn }}
                </p>
              </div>

              <!-- Actions -->
              <div class="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-slate-600">
                <div class="text-xs text-slate-500 dark:text-slate-400" :title="getUpdatedText(company)">
                  {{ getUpdatedText(company) }}
                </div>
                <div class="flex items-center gap-2">
                  <UButton
                    size="sm"
                    variant="ghost"
                    color="blue"
                    icon="i-lucide-edit"
                    :to="`/insurance-companies/${company.id}`"
                  >
                    {{ t('insuranceCompanies.list.actions.edit') }}
                  </UButton>
                  <UButton
                    size="sm"
                    variant="ghost"
                    color="red"
                    icon="i-lucide-trash-2"
                    @click="confirmDelete(company)"
                  >
                    {{ t('insuranceCompanies.list.actions.delete') }}
                  </UButton>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="p-12 text-center">
          <div class="flex flex-col items-center gap-4">
            <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
              <UIcon name="i-lucide-shield-check" class="h-8 w-8 text-slate-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('insuranceCompanies.list.empty.title') }}</h3>
              <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                {{ searchQuery || statusFilter ? t('insuranceCompanies.list.empty.searchHint') : t('insuranceCompanies.list.empty.subtitle') }}
              </p>
            </div>
            <UButton
              v-if="!searchQuery && !statusFilter"
              color="blue"
              icon="i-lucide-plus"
              to="/insurance-companies/new"
            >
              {{ t('insuranceCompanies.list.actions.addCompany') }}
            </UButton>
          </div>
        </div>
      </div>

      <!-- Delete Confirmation Modal -->
      <UModal v-model="showDeleteModal">
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-full bg-red-100 dark:bg-red-900/30">
              <UIcon name="i-lucide-alert-triangle" class="h-5 w-5 text-red-600 dark:text-red-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('insuranceCompanies.list.delete.modal.title') }}</h3>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('insuranceCompanies.list.delete.modal.subtitle') }}</p>
            </div>
          </div>
        </template>

        <div class="py-4">
          <p class="text-slate-700 dark:text-slate-300">
            {{ t('insuranceCompanies.list.delete.modal.description', { name: companyToDelete?.nameEn }) }}
          </p>
        </div>

        <template #footer>
          <div class="flex justify-end gap-3">
            <UButton
              variant="ghost"
              color="gray"
              @click="showDeleteModal = false"
              :disabled="deleting"
            >
              {{ t('insuranceCompanies.list.delete.modal.cancel') }}
            </UButton>
            <UButton
              color="red"
              icon="i-lucide-trash-2"
              :loading="deleting"
              @click="deleteCompany"
            >
              {{ t('insuranceCompanies.list.delete.modal.confirm') }}
            </UButton>
          </div>
        </template>
      </UCard>
      </UModal>
    </div>
  </div>
</template>

<script setup lang="ts">

import type { InsuranceCompany } from '../../types/insurance'
import { parseDateValue } from '~/utils/dateUtils'

definePageMeta({
  title: 'Insurance Companies',
  requiresAuth: true
})

const { t, locale } = useI18n()
useHead({ title: t('insuranceCompanies.list.meta.headTitle') })

const { getInsuranceCompanies, deleteInsuranceCompany } = useInsuranceCompanies()
const toast = useToast()

// State
const companies = ref<InsuranceCompany[]>([])
const loading = ref(true)
const searchQuery = ref('')
const statusFilter = ref('')
const showDeleteModal = ref(false)
const companyToDelete = ref<InsuranceCompany | null>(null)
const deleting = ref(false)

// Options
const statusOptions = computed(() => [
  { label: t('common.filters.allStatus'), value: '' },
  { label: t('insuranceCompanies.list.status.active'), value: 'active' },
  { label: t('insuranceCompanies.list.status.inactive'), value: 'inactive' }
])

// Computed
const activeCompanies = computed(() => companies.value?.filter(c => c.isActive).length || 0)
const inactiveCompanies = computed(() => companies.value?.filter(c => !c.isActive).length || 0)
const companiesWithLogos = computed(() => companies.value?.filter(c => c.logoUrl).length || 0)

const filteredCompanies = computed(() => {
  let filtered = companies.value || []

  // Filter by search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(company =>
      company.nameEn.toLowerCase().includes(query) ||
      company.nameAr?.toLowerCase().includes(query) ||
      company.email?.toLowerCase().includes(query) ||
      company.descriptionEn?.toLowerCase().includes(query) ||
      company.descriptionAr?.toLowerCase().includes(query)
    )
  }

  // Filter by status
  if (statusFilter.value) {
    filtered = filtered.filter(company => {
      if (statusFilter.value === 'active') return company.isActive
      if (statusFilter.value === 'inactive') return !company.isActive
      return true
    })
  }

  return filtered
})

// Methods
const loadCompanies = async () => {
  try {
    loading.value = true
    companies.value = await getInsuranceCompanies()
  } catch (error) {
    console.error('Failed to load insurance companies:', error)
    companies.value = [] // Ensure companies is always an array
    toast.add({
      title: t('insuranceCompanies.list.toasts.loadError.title'),
      description: t('insuranceCompanies.list.toasts.loadError.description'),
      color: "red",
      icon: "i-lucide-alert-circle"
    })
  } finally {
    loading.value = false
  }
}

const confirmDelete = (company: InsuranceCompany) => {
  companyToDelete.value = company
  showDeleteModal.value = true
}

const deleteCompany = async () => {
  if (!companyToDelete.value) return

  try {
    deleting.value = true
    await deleteInsuranceCompany(companyToDelete.value.id)
    companies.value = (companies.value || []).filter(c => c.id !== companyToDelete.value!.id)
    showDeleteModal.value = false
    companyToDelete.value = null
    
    toast.add({
      title: t('insuranceCompanies.list.delete.success.title'),
      description: t('insuranceCompanies.list.delete.success.description'),
      color: "green",
      icon: "i-lucide-check-circle"
    })
  } catch (error) {
    console.error('Failed to delete insurance company:', error)
    toast.add({
      title: t('insuranceCompanies.list.delete.error.title'),
      description: t('insuranceCompanies.list.delete.error.description'),
      color: "red",
      icon: "i-lucide-alert-circle"
    })
  } finally {
    deleting.value = false
  }
}

const dateFormatter = computed(() => new Intl.DateTimeFormat(locale.value || undefined, {
  year: 'numeric',
  month: 'short',
  day: 'numeric'
}))

const isMeaningfulDate = (date: Date | null) => {
  if (!date) return false
  // Filter out epoch/placeholder dates
  return date.getFullYear() > 1970
}

const getUpdatedText = (company: InsuranceCompany) => {
  const parsedUpdatedAt = parseDateValue(company.updatedAt)
  const parsedCreatedAt = parseDateValue(company.createdAt)

  const date =
    (isMeaningfulDate(parsedUpdatedAt) ? parsedUpdatedAt : null) ??
    (isMeaningfulDate(parsedCreatedAt) ? parsedCreatedAt : null)

  if (!date) {
    return t('insuranceCompanies.common.updatedUnknown')
  }

  const formatted = dateFormatter.value.format(date)
  return t('insuranceCompanies.common.updated', { date: formatted })
}

// Load data on mount
onMounted(() => {
  loadCompanies()
})
</script>
