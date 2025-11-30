<template>
  <UModal v-model="isOpen" :ui="{ width: 'sm:max-w-xl' }">
    <UCard>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900/30">
            <UIcon name="i-lucide-receipt" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
          </div>
          <div>
            <h3 class="text-lg font-semibold text-slate-900 dark:text-white">
              {{ isEditMode ? t('finance.expenses.form.edit.title') : t('finance.expenses.form.create.title') }}
            </h3>
            <p class="text-sm text-slate-600 dark:text-slate-300">
              {{ isEditMode ? t('finance.expenses.form.edit.subtitle') : t('finance.expenses.form.create.subtitle') }}
            </p>
          </div>
        </div>
      </template>

      <form @submit.prevent="handleSubmit" class="space-y-4">
        <UFormGroup 
          :label="t('finance.expenses.form.date.label')" 
          required
          :error="validationErrors.expenseDate"
        >
          <UInput
            v-model="formData.expenseDate"
            type="date"
            :placeholder="t('finance.expenses.form.date.placeholder')"
            :disabled="loading"
          />
        </UFormGroup>

        <UFormGroup 
          :label="t('finance.expenses.form.category.label')" 
          required
          :error="validationErrors.categoryId"
        >
          <USelect
            v-model="formData.categoryId"
            :options="categoryOptions"
            value-attribute="value"
            label-attribute="label"
            :placeholder="t('finance.expenses.form.category.placeholder')"
            :disabled="loading"
          />
        </UFormGroup>

        <UFormGroup 
          :label="t('finance.expenses.form.amount.label')" 
          required
          :error="validationErrors.amount"
        >
          <UInput
            v-model.number="formData.amount"
            type="number"
            step="0.01"
            min="0.01"
            :placeholder="t('finance.expenses.form.amount.placeholder')"
            :disabled="loading"
          />
        </UFormGroup>

        <UFormGroup 
          :label="t('finance.expenses.form.notes.label')"
        >
          <UTextarea
            v-model="formData.notes"
            :placeholder="t('finance.expenses.form.notes.placeholder')"
            :rows="3"
            :disabled="loading"
          />
        </UFormGroup>
      </form>

      <template #footer>
        <div class="flex justify-end gap-2">
          <UButton 
            color="gray" 
            variant="ghost" 
            @click="handleCancel" 
            :disabled="loading"
          >
            {{ t('finance.expenses.form.actions.cancel') }}
          </UButton>
          <UButton 
            color="blue" 
            @click="handleSubmit" 
            :loading="loading"
          >
            {{ isEditMode ? t('finance.expenses.form.actions.update') : t('finance.expenses.form.actions.create') }}
          </UButton>
        </div>
      </template>
    </UCard>
  </UModal>
</template>

<script setup lang="ts">
import type { Expense, ExpenseCreateRequest } from '~/composables/useExpenses'
import type { ExpenseCategory } from '~/composables/useExpenseCategories'

const props = defineProps<{
  modelValue: boolean
  expense?: Expense | null
  categories: ExpenseCategory[]
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [data: ExpenseCreateRequest]
}>()

const { t } = useI18n()

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isEditMode = computed(() => !!props.expense)

const formData = ref<ExpenseCreateRequest>({
  categoryId: 0,
  amount: 0,
  expenseDate: new Date().toISOString().split('T')[0],
  notes: ''
})

const validationErrors = ref<Record<string, string>>({})

// Filter to show only active categories
const categoryOptions = computed(() => 
  props.categories
    .filter(cat => cat.isActive)
    .map(cat => ({
      label: cat.name,
      value: cat.id
    }))
)

// Watch for expense changes to populate form in edit mode
watch(() => props.expense, (expense) => {
  if (expense) {
    formData.value = {
      categoryId: expense.categoryId,
      amount: expense.amount,
      expenseDate: expense.expenseDate,
      notes: expense.notes || ''
    }
  } else {
    resetForm()
  }
}, { immediate: true })

const validateForm = (): boolean => {
  validationErrors.value = {}
  let isValid = true

  if (!formData.value.expenseDate) {
    validationErrors.value.expenseDate = t('finance.expenses.form.validation.dateRequired')
    isValid = false
  }

  if (!formData.value.categoryId || formData.value.categoryId === 0) {
    validationErrors.value.categoryId = t('finance.expenses.form.validation.categoryRequired')
    isValid = false
  }

  if (!formData.value.amount || formData.value.amount <= 0) {
    validationErrors.value.amount = t('finance.expenses.form.validation.amountPositive')
    isValid = false
  }

  return isValid
}

const handleSubmit = () => {
  if (!validateForm()) {
    return
  }

  emit('submit', { ...formData.value })
}

const handleCancel = () => {
  resetForm()
  isOpen.value = false
}

function resetForm() {
  formData.value = {
    categoryId: 0,
    amount: 0,
    expenseDate: new Date().toISOString().split('T')[0],
    notes: ''
  }
  validationErrors.value = {}
}

// Reset form when modal closes
watch(isOpen, (newValue) => {
  if (!newValue && !props.expense) {
    resetForm()
  }
})
</script>
