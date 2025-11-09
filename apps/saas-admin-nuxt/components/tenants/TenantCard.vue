<template>
  <div
    class="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4 hover:shadow-md transition-shadow cursor-pointer"
    @click="$emit('click', tenant.id)"
  >
    <div class="flex items-start justify-between mb-3">
      <div class="flex-1 min-w-0">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 truncate">
          {{ tenant.name }}
        </h3>
        <p class="text-sm text-gray-500 dark:text-gray-400 truncate">
          {{ tenant.slug }}
        </p>
      </div>
      <UBadge
        :color="getStatusColor(tenant.status)"
        variant="subtle"
        class="ml-2 flex-shrink-0"
      >
        {{ tenant.status }}
      </UBadge>
    </div>

    <div class="space-y-2">
      <div v-if="tenant.customDomain" class="flex items-center gap-2 text-sm">
        <UIcon name="i-heroicons-globe-alt" class="w-4 h-4 text-gray-400" />
        <span class="text-gray-600 dark:text-gray-300 truncate">
          {{ tenant.customDomain }}
        </span>
      </div>

      <div class="flex items-center gap-2 text-sm">
        <UIcon name="i-heroicons-calendar" class="w-4 h-4 text-gray-400" />
        <span class="text-gray-600 dark:text-gray-300">
          Created {{ formatDate(tenant.createdAt) }}
        </span>
      </div>
    </div>

    <div class="mt-4 flex justify-end">
      <UButton
        icon="i-heroicons-arrow-right"
        color="gray"
        variant="ghost"
        size="sm"
        @click.stop="$emit('click', tenant.id)"
      >
        View Details
      </UButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Tenant } from '~/types'
import { formatDate as formatDateUtil } from '~/utils/dateUtils'

interface Props {
  tenant: Tenant
}

defineProps<Props>()

defineEmits<{
  'click': [id: number]
}>()

const getStatusColor = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'green'
    case 'INACTIVE':
      return 'gray'
    default:
      return 'red'
  }
}

const formatDate = (dateString: string | number) => {
  return formatDateUtil(dateString)
}
</script>
