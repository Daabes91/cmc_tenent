<template>
  <span :class="currencyClasses">
    {{ formattedValue }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  amount: number | null | undefined
  currency: string | null | undefined
  variant?: 'primary' | 'secondary' | 'muted'
  size?: 'sm' | 'md' | 'lg'
  locale?: string
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md'
})

const { settings } = useClinicSettings()

const currencyClasses = computed(() => {
  const baseClasses = ['inline-block']
  
  // Size classes
  switch (props.size) {
    case 'sm':
      baseClasses.push('text-sm')
      break
    case 'lg':
      baseClasses.push('text-lg')
      break
    default:
      baseClasses.push('text-base')
  }
  
  // Variant classes
  switch (props.variant) {
    case 'primary':
      baseClasses.push('text-slate-900 dark:text-white font-semibold')
      break
    case 'secondary':
      baseClasses.push('text-slate-600 dark:text-slate-300')
      break
    case 'muted':
      baseClasses.push('text-slate-500 dark:text-slate-400')
      break
  }
  
  return baseClasses.join(' ')
})

const formattedValue = computed(() => {
  if (props.amount === null || props.amount === undefined) {
    return '—'
  }
  
  const numericAmount = Number(props.amount)
  if (!Number.isFinite(numericAmount)) {
    return '—'
  }
  
  const currencyCode = props.currency || settings.value?.currency || 'USD'
  const localeCode = props.locale || settings.value?.locale || 'en-US'
  
  try {
    return new Intl.NumberFormat(localeCode, {
      style: 'currency',
      currency: currencyCode
    }).format(numericAmount)
  } catch (error) {
    // Fallback formatting if Intl.NumberFormat fails
    return `${numericAmount.toFixed(2)} ${currencyCode}`
  }
})
</script>