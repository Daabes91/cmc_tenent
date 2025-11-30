<template>
  <div class="overflow-x-auto">
    <table class="w-full">
      <thead>
        <tr class="border-b border-slate-200 dark:border-slate-700">
          <th class="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
            Tenant
          </th>
          <th class="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
            Domain
          </th>
          <th class="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
            Status
          </th>
          <th class="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
            Billing
          </th>
          <th class="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
            Plan
          </th>
          <th class="px-4 py-3 text-left text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
            Created
          </th>
          <th class="px-4 py-3 text-right text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
            Actions
          </th>
        </tr>
      </thead>
      <tbody class="divide-y divide-slate-200 dark:divide-slate-700">
        <tr
          v-for="tenant in props.tenants"
          :key="tenant.id"
          class="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors"
        >
          <td class="px-4 py-4">
            <div>
              <div class="font-medium text-slate-900 dark:text-white">
                {{ tenant.name }}
              </div>
              <div class="text-sm text-slate-500 dark:text-slate-400 font-mono">
                {{ tenant.slug }}
              </div>
            </div>
          </td>
          <td class="px-4 py-4">
            <div class="text-sm text-slate-600 dark:text-slate-300">
              {{ tenant.customDomain || '-' }}
            </div>
          </td>
          <td class="px-4 py-4">
            <UBadge
              :color="tenant.status === 'ACTIVE' ? 'green' : 'gray'"
              variant="subtle"
              size="sm"
            >
              {{ tenant.status }}
            </UBadge>
          </td>
          <td class="px-4 py-4">
            <UBadge
              :color="getBillingStatusColor(tenant.billingStatus)"
              variant="subtle"
              size="sm"
            >
              <div class="flex items-center gap-1.5">
                <UIcon :name="getBillingStatusIcon(tenant.billingStatus)" class="w-3 h-3" />
                <span>{{ formatBillingStatus(tenant.billingStatus) }}</span>
              </div>
            </UBadge>
          </td>
          <td class="px-4 py-4">
            <UBadge
              v-if="tenant.planTier"
              :color="getPlanTierColor(tenant.planTier)"
              variant="subtle"
              size="sm"
            >
              <div class="flex items-center gap-1.5">
                <UIcon :name="getPlanTierIcon(tenant.planTier)" class="w-3 h-3" />
                <span>{{ formatPlanTier(tenant.planTier) }}</span>
              </div>
            </UBadge>
            <span v-else class="text-sm text-slate-400 dark:text-slate-500">-</span>
          </td>
          <td class="px-4 py-4">
            <div class="text-sm text-slate-600 dark:text-slate-300">
              {{ formatDate(tenant.createdAt) }}
            </div>
          </td>
          <td class="px-4 py-4">
            <div class="flex items-center justify-end gap-2">
              <UButton
                color="gray"
                variant="ghost"
                size="sm"
                icon="i-heroicons-eye"
                @click="$emit('view', tenant.id)"
              />
              <UButton
                color="gray"
                variant="ghost"
                size="sm"
                icon="i-heroicons-pencil"
                @click="$emit('edit', tenant.id)"
              />
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <div
      v-if="props.tenants?.length === 0"
      class="text-center py-12"
    >
      <UIcon name="i-heroicons-building-office-2" class="w-16 h-16 text-slate-300 dark:text-slate-600 mx-auto mb-3" />
      <h3 class="text-lg font-medium text-slate-900 dark:text-white mb-2">
        No tenants found
      </h3>
      <p class="text-sm text-slate-500 dark:text-slate-400">
        Get started by creating your first tenant
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Tenant } from '~/types'

interface Props {
  tenants: Tenant[]
}

const props = defineProps<Props>()
defineEmits<{
  view: [id: number]
  edit: [id: number]
}>()

const formatDate = (timestamp: number | string) => {
  const date = typeof timestamp === 'number' 
    ? new Date(timestamp * 1000)
    : new Date(timestamp)
  
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
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

const formatBillingStatus = (status: string) => {
  return status.split('_').map(word => 
    word.charAt(0) + word.slice(1).toLowerCase()
  ).join(' ')
}

const getPlanTierColor = (planTier: string) => {
  switch (planTier) {
    case 'BASIC':
      return 'blue'
    case 'PROFESSIONAL':
      return 'purple'
    case 'ENTERPRISE':
      return 'amber'
    case 'CUSTOM':
      return 'pink'
    default:
      return 'gray'
  }
}

const getPlanTierIcon = (planTier: string) => {
  switch (planTier) {
    case 'BASIC':
      return 'i-heroicons-star'
    case 'PROFESSIONAL':
      return 'i-heroicons-sparkles'
    case 'ENTERPRISE':
      return 'i-heroicons-building-office'
    case 'CUSTOM':
      return 'i-heroicons-cog-6-tooth'
    default:
      return 'i-heroicons-question-mark-circle'
  }
}

const formatPlanTier = (planTier: string) => {
  return planTier.charAt(0) + planTier.slice(1).toLowerCase()
}
</script>
