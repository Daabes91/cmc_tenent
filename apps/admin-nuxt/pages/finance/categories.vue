<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-purple-500 to-pink-600 shadow-lg">
              <UIcon name="i-lucide-folder" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('finance.categories.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('finance.categories.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="categoriesApi.loading.value"
              @click="loadCategories"
            >
              {{ t('finance.categories.actions.refresh') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">
      <CategoryManager
        :categories="categoriesApi.categories.value"
        :loading="categoriesApi.loading.value"
        :error="categoriesApi.error.value"
        @create="handleCreate"
        @update="handleUpdate"
        @toggle="handleToggle"
        @refresh="loadCategories"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n()

const categoriesApi = useExpenseCategories()

const loadCategories = async () => {
  await categoriesApi.fetchCategories()
}

const handleCreate = async (name: string) => {
  await categoriesApi.createCategory(name)
}

const handleUpdate = async (id: number, name: string) => {
  await categoriesApi.updateCategory(id, name)
}

const handleToggle = async (id: number) => {
  await categoriesApi.toggleActive(id)
}

useHead({
  title: () => t('finance.categories.meta.title')
})

onMounted(async () => {
  await loadCategories()
})
</script>
