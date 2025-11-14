<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-violet-50/40 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950">
    <header class="sticky top-0 z-10 border-b border-slate-200/60 bg-white/80 backdrop-blur-xl dark:border-white/10 dark:bg-slate-900/80">
      <div class="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">
        <div class="flex items-center gap-4">
          <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-cyan-500 to-blue-600 text-2xl text-white shadow-lg shadow-cyan-500/40">
            <UIcon name="i-lucide-languages" class="h-6 w-6" />
          </div>
          <div>
            <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('translationsPage.header.title') }}</h1>
            <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('translationsPage.header.subtitle') }}</p>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <UButton color="gray" variant="ghost" icon="i-lucide-refresh-cw" :loading="loading" @click="loadTranslations">
            {{ t('translationsPage.actions.refresh') }}
          </UButton>
          <UButton color="primary" icon="i-lucide-save" :loading="saving" @click="saveTranslations">
            {{ t('translationsPage.actions.save') }}
          </UButton>
        </div>
      </div>
    </header>

    <div class="mx-auto max-w-7xl px-6 py-8 space-y-6">
      <UAlert
        v-if="errorMessage"
        icon="i-lucide-alert-triangle"
        color="red"
        variant="solid"
        :title="t('translationsPage.errors.loadTitle')"
        :description="errorMessage"
        class="rounded-2xl"
      />

      <div v-else class="space-y-6">
        <div class="grid gap-4 md:grid-cols-3">
          <div class="rounded-2xl border border-slate-200/60 bg-white p-5 shadow-sm dark:border-white/10 dark:bg-slate-900">
            <label class="text-sm font-semibold text-slate-700 dark:text-slate-200">{{ t('translationsPage.filters.search') }}</label>
            <UInput
              v-model="search"
              :placeholder="t('translationsPage.filters.searchPlaceholder')"
              icon="i-lucide-search"
              size="lg"
            />
          </div>
          <div class="rounded-2xl border border-slate-200/60 bg-white p-5 shadow-sm dark:border-white/10 dark:bg-slate-900">
            <label class="text-sm font-semibold text-slate-700 dark:text-slate-200">{{ t('translationsPage.filters.namespace') }}</label>
            <USelect
              v-model="selectedNamespace"
              size="lg"
              :options="namespaceOptions"
            />
          </div>
          <div class="flex items-end">
            <UButton
              variant="outline"
              color="blue"
              icon="i-lucide-plus"
              class="w-full rounded-2xl border-2 border-dashed border-blue-200 py-5 dark:border-blue-800"
              @click="showAddDialog = true"
            >
              {{ t('translationsPage.actions.add') }}
            </UButton>
          </div>
        </div>

        <USkeleton v-if="loading" class="h-64 rounded-2xl" />

        <div v-else class="space-y-6">
          <div
            v-for="namespace in groupedRows"
            :key="namespace.name"
            class="rounded-2xl border border-slate-200/70 bg-white shadow-sm dark:border-white/10 dark:bg-slate-900"
          >
            <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4 dark:border-white/5">
              <div>
                <p class="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">
                  {{ namespace.name }}
                </p>
                <p class="text-xs text-slate-400 dark:text-slate-500">{{ t('translationsPage.namespace.count', { count: namespace.items.length }) }}</p>
              </div>
              <div class="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400">
                <span class="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-3 py-1 font-semibold text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-300">
                  <UIcon name="i-lucide-check-circle-2" class="h-4 w-4" />
                  {{ namespace.completed }}/{{ namespace.items.length }}
                </span>
              </div>
            </div>

            <div class="overflow-x-auto">
              <table class="min-w-full divide-y divide-slate-100 dark:divide-white/5">
                <thead>
                  <tr class="text-xs uppercase tracking-wider text-slate-400 dark:text-slate-500">
                    <th class="px-6 py-3 text-left">{{ t('translationsPage.table.key') }}</th>
                    <th class="px-6 py-3 text-left">{{ t('translationsPage.table.english') }}</th>
                    <th class="px-6 py-3 text-left">{{ t('translationsPage.table.arabic') }}</th>
                    <th class="px-6 py-3 text-right">{{ t('translationsPage.table.status') }}</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 dark:divide-white/5">
                  <tr
                    v-for="row in namespace.items"
                    :key="`${row.namespace}-${row.key}`"
                    class="transition-colors hover:bg-slate-50 dark:hover:bg-white/5"
                  >
                    <td class="px-6 py-4 align-top text-sm font-semibold text-slate-700 dark:text-slate-100">
                      <div class="flex flex-col">
                        <span>{{ row.key }}</span>
                        <span class="text-xs text-slate-400">{{ row.namespace }}</span>
                      </div>
                    </td>
                    <td class="px-6 py-4 align-top">
                      <UTextarea
                        v-model="row.values.en"
                        size="lg"
                        :placeholder="t('translationsPage.table.placeholder')"
                        @input="markDirty(row)"
                      />
                    </td>
                    <td class="px-6 py-4 align-top">
                      <UTextarea
                        v-model="row.values.ar"
                        dir="rtl"
                        size="lg"
                        :placeholder="t('translationsPage.table.placeholder')"
                        @input="markDirty(row)"
                      />
                    </td>
                    <td class="px-6 py-4 text-right align-top">
                      <UBadge
                        :color="row.dirty ? 'amber' : row.isComplete ? 'emerald' : 'gray'"
                        variant="soft"
                        size="sm"
                      >
                        {{ row.dirty ? t('translationsPage.status.modified') : row.isComplete ? t('translationsPage.status.complete') : t('translationsPage.status.pending') }}
                      </UBadge>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div v-if="!groupedRows.length" class="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center text-slate-500 dark:border-white/20 dark:bg-slate-900 dark:text-slate-400">
            <p>{{ t('translationsPage.empty') }}</p>
          </div>
        </div>
      </div>
    </div>

    <UModal v-model="showAddDialog">
      <div class="space-y-4 p-6">
        <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('translationsPage.addDialog.title') }}</h3>
        <UFormGroup :label="t('translationsPage.addDialog.namespace')" required>
          <UInput v-model="newEntry.namespace" placeholder="hero" />
        </UFormGroup>
        <UFormGroup :label="t('translationsPage.addDialog.key')" required>
          <UInput v-model="newEntry.key" placeholder="title" />
        </UFormGroup>
        <div class="flex justify-end gap-3">
          <UButton variant="ghost" color="gray" @click="showAddDialog = false">{{ t('translationsPage.actions.cancel') }}</UButton>
          <UButton color="primary" icon="i-lucide-plus" @click="addTranslation">{{ t('translationsPage.actions.addKey') }}</UButton>
        </div>
      </div>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';

interface TranslationRow {
  namespace: string;
  key: string;
  values: {
    en: string;
    ar: string;
  };
  dirty?: boolean;
  isComplete?: boolean;
}

interface TranslationResponse {
  namespace: string;
  key: string;
  locale: string;
  value: string;
}

const { t } = useI18n();
const { request } = useAdminApi();
const toast = useToast();

const rows = ref<TranslationRow[]>([]);
const loading = ref(true);
const saving = ref(false);
const errorMessage = ref<string | null>(null);
const search = ref('');
const selectedNamespace = ref<string>('all');

const showAddDialog = ref(false);
const newEntry = reactive({
  namespace: '',
  key: ''
});

const namespaceOptions = computed(() => {
  const namespaces = Array.from(new Set(rows.value.map((row) => row.namespace))).sort();
  return [
    { label: t('translationsPage.filters.allNamespaces'), value: 'all' },
    ...namespaces.map((namespace) => ({
      label: namespace,
      value: namespace
    }))
  ];
});

const filteredRows = computed(() => {
  const term = search.value.trim().toLowerCase();
  return rows.value.filter((row) => {
    const matchesSearch =
      !term ||
      row.key.toLowerCase().includes(term) ||
      row.namespace.toLowerCase().includes(term) ||
      row.values.en?.toLowerCase().includes(term) ||
      row.values.ar?.toLowerCase().includes(term);

    const matchesNamespace =
      selectedNamespace.value === 'all' ||
      row.namespace === selectedNamespace.value;

    return matchesSearch && matchesNamespace;
  });
});

const groupedRows = computed(() => {
  const groups: Record<string, { name: string; items: TranslationRow[]; completed: number }> = {};
  filteredRows.value.forEach((row) => {
    if (!groups[row.namespace]) {
      groups[row.namespace] = {
        name: row.namespace,
        items: [],
        completed: 0
      };
    }
    row.isComplete = Boolean(row.values.en?.trim() && row.values.ar?.trim());
    if (row.isComplete) {
      groups[row.namespace].completed += 1;
    }
    groups[row.namespace].items.push(row);
  });

  return Object.values(groups).sort((a, b) => a.name.localeCompare(b.name));
});

const markDirty = (row: TranslationRow) => {
  row.dirty = true;
};

const mergeTranslations = (enData: TranslationResponse[], arData: TranslationResponse[]) => {
  const map = new Map<string, TranslationRow>();

  for (const item of enData) {
    const key = `${item.namespace}:${item.key}`;
    const existing = map.get(key) ?? {
      namespace: item.namespace,
      key: item.key,
      values: { en: '', ar: '' }
    };
    existing.values.en = item.value || '';
    map.set(key, existing);
  }

  for (const item of arData) {
    const key = `${item.namespace}:${item.key}`;
    const existing = map.get(key) ?? {
      namespace: item.namespace,
      key: item.key,
      values: { en: '', ar: '' }
    };
    existing.values.ar = item.value || '';
    map.set(key, existing);
  }

  const merged = Array.from(map.values()).sort((a, b) => {
    if (a.namespace === b.namespace) {
      return a.key.localeCompare(b.key);
    }
    return a.namespace.localeCompare(b.namespace);
  });

  merged.forEach((row) => {
    row.dirty = false;
  });

  rows.value = merged;
};

const loadTranslations = async () => {
  loading.value = true;
  errorMessage.value = null;
  try {
    const [enData, arData] = await Promise.all([
      request<TranslationResponse[]>('/translations?locale=en'),
      request<TranslationResponse[]>('/translations?locale=ar')
    ]);
    mergeTranslations(enData, arData);
  } catch (error: any) {
    errorMessage.value = error?.data?.message || error?.message || t('translationsPage.errors.generic');
  } finally {
    loading.value = false;
  }
};

const addTranslation = () => {
  const namespace = newEntry.namespace.trim();
  const key = newEntry.key.trim();

  if (!namespace || !key) {
    toast.add({
      title: t('translationsPage.toasts.addInvalid.title'),
      description: t('translationsPage.toasts.addInvalid.description'),
      color: 'amber',
      icon: 'i-lucide-alert-triangle'
    });
    return;
  }

  const exists = rows.value.some((row) => row.namespace === namespace && row.key === key);
  if (exists) {
    toast.add({
      title: t('translationsPage.toasts.duplicate.title'),
      description: t('translationsPage.toasts.duplicate.description'),
      color: 'red',
      icon: 'i-lucide-x-circle'
    });
    return;
  }

  rows.value.push({
    namespace,
    key,
    values: { en: '', ar: '' },
    dirty: true
  });

  newEntry.namespace = '';
  newEntry.key = '';
  showAddDialog.value = false;
};

const buildPayload = () => {
  const payload: TranslationResponse[] = [];
  rows.value
    .filter((row) => row.dirty)
    .forEach((row) => {
      payload.push({
        namespace: row.namespace,
        key: row.key,
        locale: 'en',
        value: row.values.en || ''
      });
      payload.push({
        namespace: row.namespace,
        key: row.key,
        locale: 'ar',
        value: row.values.ar || ''
      });
    });
  return payload;
};

const saveTranslations = async () => {
  saving.value = true;
  try {
    const payload = buildPayload();
    if (!payload.length) {
      toast.add({
        title: t('translationsPage.toasts.noChanges.title'),
        description: t('translationsPage.toasts.noChanges.description'),
        color: 'blue',
        icon: 'i-lucide-info'
      });
      saving.value = false;
      return;
    }

    await request('/translations', {
      method: 'PUT',
      body: payload
    });

    toast.add({
      title: t('translationsPage.toasts.saveSuccess.title'),
      description: t('translationsPage.toasts.saveSuccess.description'),
      color: 'green',
      icon: 'i-lucide-check-circle'
    });
    await loadTranslations();
  } catch (error: any) {
    toast.add({
      title: t('translationsPage.toasts.saveError.title'),
      description: error?.data?.message || error?.message || t('translationsPage.toasts.saveError.description'),
      color: 'red',
      icon: 'i-lucide-alert-triangle'
    });
  } finally {
    saving.value = false;
  }
};

onMounted(loadTranslations);
</script>
