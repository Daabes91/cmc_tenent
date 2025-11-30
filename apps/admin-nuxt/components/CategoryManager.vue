<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('finance.categories.title') }}</h2>
        <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('finance.categories.subtitle') }}</p>
      </div>
      <UButton
        color="blue"
        icon="i-lucide-plus"
        @click="openCreateModal"
      >
        {{ t('finance.categories.actions.add') }}
      </UButton>
    </div>

    <!-- Categories List -->
    <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
      <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
        <div class="flex items-center gap-3">
          <UIcon name="i-lucide-folder" class="h-5 w-5 text-white" />
          <div>
            <h3 class="text-lg font-semibold text-white">{{ t('finance.categories.list.title') }}</h3>
            <p class="text-sm text-blue-100">{{ t('finance.categories.list.count', { count: categories.length }) }}</p>
          </div>
        </div>
      </div>

      <div v-if="loading" class="p-6">
        <div class="space-y-4">
          <div v-for="i in 5" :key="i" class="animate-pulse">
            <div class="bg-slate-200 dark:bg-slate-700 rounded-xl p-4 flex items-center justify-between">
              <div class="flex items-center gap-4 flex-1">
                <div class="h-10 w-10 bg-slate-300 dark:bg-slate-600 rounded-full"></div>
                <div class="flex-1 space-y-2">
                  <div class="h-4 bg-slate-300 dark:bg-slate-600 rounded w-1/3"></div>
                  <div class="h-3 bg-slate-300 dark:bg-slate-600 rounded w-1/4"></div>
                </div>
              </div>
              <div class="h-8 w-24 bg-slate-300 dark:bg-slate-600 rounded"></div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="error" class="p-12 text-center">
        <div class="flex flex-col items-center gap-4">
          <div class="h-16 w-16 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
            <UIcon name="i-lucide-alert-circle" class="h-8 w-8 text-red-600 dark:text-red-400" />
          </div>
          <div>
            <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('finance.categories.list.error.title') }}</h3>
            <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">{{ error.message }}</p>
          </div>
        </div>
      </div>

      <div v-else-if="categories.length > 0" class="p-6">
        <div class="space-y-3">
          <div
            v-for="category in categories"
            :key="category.id"
            class="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-4 border border-slate-200 dark:border-slate-600 hover:shadow-md hover:border-blue-300 dark:hover:border-blue-600 transition-all duration-200"
          >
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-4">
                <div class="h-10 w-10 rounded-full bg-gradient-to-br from-blue-100 to-indigo-100 dark:from-blue-900/30 dark:to-indigo-900/30 flex items-center justify-center">
                  <UIcon name="i-lucide-folder" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
                </div>
                <div>
                  <div class="flex items-center gap-2">
                    <h4 class="font-semibold text-slate-900 dark:text-white">{{ category.name }}</h4>
                    <UBadge
                      v-if="category.isSystem"
                      color="gray"
                      variant="soft"
                      size="xs"
                    >
                      {{ t('finance.categories.badges.system') }}
                    </UBadge>
                  </div>
                  <p class="text-sm text-slate-500 dark:text-slate-400">
                    {{ category.isActive ? t('finance.categories.status.active') : t('finance.categories.status.inactive') }}
                  </p>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <UButton
                  size="sm"
                  variant="ghost"
                  color="blue"
                  icon="i-lucide-edit"
                  @click="openEditModal(category)"
                >
                  {{ t('finance.categories.actions.edit') }}
                </UButton>
                <UButton
                  size="sm"
                  variant="ghost"
                  :color="category.isActive ? 'amber' : 'green'"
                  :icon="category.isActive ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                  @click="handleToggleActive(category)"
                >
                  {{ category.isActive ? t('finance.categories.actions.disable') : t('finance.categories.actions.enable') }}
                </UButton>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="p-12 text-center">
        <div class="flex flex-col items-center gap-4">
          <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
            <UIcon name="i-lucide-folder" class="h-8 w-8 text-slate-400" />
          </div>
          <div>
            <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('finance.categories.list.empty.title') }}</h3>
            <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">{{ t('finance.categories.list.empty.subtitle') }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit/Create Modal -->
    <UModal v-model="showModal" :ui="{ width: 'sm:max-w-md' }">
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900/30">
              <UIcon name="i-lucide-folder" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">
                {{ isEditing ? t('finance.categories.modal.edit.title') : t('finance.categories.modal.create.title') }}
              </h3>
              <p class="text-sm text-slate-600 dark:text-slate-300">
                {{ isEditing ? t('finance.categories.modal.edit.subtitle') : t('finance.categories.modal.create.subtitle') }}
              </p>
            </div>
          </div>
        </template>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <UFormGroup 
            :label="t('finance.categories.modal.form.name.label')" 
            required
            :error="validationError"
          >
            <UInput
              v-model="formData.name"
              :placeholder="t('finance.categories.modal.form.name.placeholder')"
              :disabled="formLoading"
            />
          </UFormGroup>
        </form>

        <template #footer>
          <div class="flex justify-end gap-2">
            <UButton 
              color="gray" 
              variant="ghost" 
              @click="showModal = false" 
              :disabled="formLoading"
            >
              {{ t('finance.categories.modal.actions.cancel') }}
            </UButton>
            <UButton 
              color="blue" 
              @click="handleSubmit" 
              :loading="formLoading"
            >
              {{ isEditing ? t('finance.categories.modal.actions.update') : t('finance.categories.modal.actions.create') }}
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>

    <!-- Confirmation Modal for Disable -->
    <UModal v-model="showConfirmModal" :ui="{ width: 'sm:max-w-md' }">
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-full bg-amber-100 dark:bg-amber-900/30">
              <UIcon name="i-lucide-alert-triangle" class="h-5 w-5 text-amber-600 dark:text-amber-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">
                {{ t('finance.categories.confirm.title') }}
              </h3>
            </div>
          </div>
        </template>

        <p class="text-sm text-slate-600 dark:text-slate-300">
          {{ t('finance.categories.confirm.message', { name: categoryToToggle?.name }) }}
        </p>

        <template #footer>
          <div class="flex justify-end gap-2">
            <UButton 
              color="gray" 
              variant="ghost" 
              @click="showConfirmModal = false"
            >
              {{ t('finance.categories.confirm.cancel') }}
            </UButton>
            <UButton 
              color="amber" 
              @click="confirmToggle"
              :loading="formLoading"
            >
              {{ t('finance.categories.confirm.confirm') }}
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import type { ExpenseCategory } from '~/composables/useExpenseCategories'

const props = defineProps<{
  categories: ExpenseCategory[]
  loading: boolean
  error: Error | null
}>()

const emit = defineEmits<{
  create: [name: string]
  update: [id: number, name: string]
  toggle: [id: number]
  refresh: []
}>()

const { t } = useI18n()

const showModal = ref(false)
const showConfirmModal = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const categoryToToggle = ref<ExpenseCategory | null>(null)
const formLoading = ref(false)
const validationError = ref('')

const formData = ref({
  name: ''
})

const openCreateModal = () => {
  isEditing.value = false
  editingId.value = null
  formData.value.name = ''
  validationError.value = ''
  showModal.value = true
}

const openEditModal = (category: ExpenseCategory) => {
  isEditing.value = true
  editingId.value = category.id
  formData.value.name = category.name
  validationError.value = ''
  showModal.value = true
}

const handleToggleActive = (category: ExpenseCategory) => {
  if (category.isActive) {
    // Show confirmation for disabling
    categoryToToggle.value = category
    showConfirmModal.value = true
  } else {
    // Enable directly without confirmation
    emit('toggle', category.id)
  }
}

const confirmToggle = () => {
  if (categoryToToggle.value) {
    emit('toggle', categoryToToggle.value.id)
    showConfirmModal.value = false
    categoryToToggle.value = null
  }
}

const validateForm = (): boolean => {
  validationError.value = ''
  
  if (!formData.value.name.trim()) {
    validationError.value = t('finance.categories.modal.validation.nameRequired')
    return false
  }
  
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  formLoading.value = true
  
  try {
    if (isEditing.value && editingId.value !== null) {
      emit('update', editingId.value, formData.value.name.trim())
    } else {
      emit('create', formData.value.name.trim())
    }
    
    showModal.value = false
  } finally {
    formLoading.value = false
  }
}
</script>
