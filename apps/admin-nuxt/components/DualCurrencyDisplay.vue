<template>
  <div class="space-y-1">
    <!-- Primary (Clinic Currency) -->
    <div class="flex items-center gap-2">
      <CurrencyValue 
        :amount="convertedAmount" 
        :currency="convertedCurrency" 
        variant="primary" 
        :size="size"
      />
      <UBadge 
        v-if="showClinicBadge" 
        color="blue" 
        variant="soft" 
        size="xs"
      >
        {{ t('currency.clinic') }}
      </UBadge>
    </div>
    
    <!-- Secondary (Original Currency) - only if different -->
    <div v-if="shouldShowOriginal" class="text-xs text-slate-500 dark:text-slate-400">
      {{ t('currency.original') }}: 
      <CurrencyValue 
        :amount="originalAmount" 
        :currency="originalCurrency" 
        variant="secondary" 
        size="sm"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  convertedAmount: number | null | undefined
  convertedCurrency: string | null | undefined
  originalAmount: number | null | undefined
  originalCurrency: string | null | undefined
  size?: 'sm' | 'md' | 'lg'
  showClinicBadge?: boolean
  alwaysShowOriginal?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  showClinicBadge: false,
  alwaysShowOriginal: false
})

const { t } = useI18n()
const { prepareDualCurrency } = useCurrencyFormatting()

// Prepare dual currency data using utilities
const currencyData = computed(() => 
  prepareDualCurrency(
    props.convertedAmount,
    props.convertedCurrency,
    props.originalAmount,
    props.originalCurrency,
    props.alwaysShowOriginal
  )
)

// Determine if we should show the original currency
const shouldShowOriginal = computed(() => currencyData.value.showSecondary)

// Show clinic badge when we have different currencies or when explicitly requested
const showClinicBadge = computed(() => {
  return props.showClinicBadge && shouldShowOriginal.value
})
</script>