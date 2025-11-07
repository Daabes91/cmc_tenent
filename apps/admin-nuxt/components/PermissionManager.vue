<template>
  <div class="space-y-6">
    <div class="rounded-xl border border-slate-200 bg-white p-6">
      <h3 class="text-lg font-semibold text-slate-900 mb-4">{{ t('staff.permissions.title') }}</h3>
      <p class="text-sm text-slate-500 mb-6">
        {{ t('staff.permissions.subtitle') }}
      </p>

      <div class="space-y-4">
        <div
          v-for="module in modules"
          :key="module.key"
          class="rounded-lg border border-slate-200 p-4 hover:border-violet-300 transition-colors"
        >
          <div class="flex items-center justify-between mb-3">
            <div class="flex items-center gap-3">
              <UIcon :name="module.icon" class="w-5 h-5 text-violet-600" />
              <div>
                <h4 class="font-semibold text-slate-900">{{ module.label }}</h4>
                <p class="text-xs text-slate-500">{{ module.description }}</p>
              </div>
            </div>
            <UButton
              v-if="hasAnyPermission(module.key)"
              size="xs"
              variant="ghost"
              color="red"
              @click="clearModule(module.key)"
            >
              {{ t('staff.permissions.clearAll') }}
            </UButton>
          </div>

          <div class="flex flex-wrap gap-2">
            <UCheckbox
              v-for="action in actions"
              :key="action.value"
              v-model="permissions[module.key]"
              :value="action.value"
              :label="action.label"
              :color="action.color"
              class="inline-flex"
            >
              <template #label>
                <span class="text-sm font-medium">{{ action.label }}</span>
              </template>
            </UCheckbox>
          </div>
        </div>
      </div>
    </div>

    <div class="rounded-xl border border-amber-200 bg-amber-50 p-4">
      <div class="flex items-start gap-3">
        <UIcon name="i-lucide-alert-circle" class="w-5 h-5 text-amber-600 mt-0.5" />
        <div class="flex-1">
          <h4 class="font-semibold text-amber-900 text-sm">{{ t('staff.permissions.guidelines.title') }}</h4>
          <ul class="mt-2 text-sm text-amber-800 space-y-1">
            <li>• <strong>{{ t('staff.permissions.actions.view') }}</strong> - {{ t('staff.permissions.guidelines.view') }}</li>
            <li>• <strong>{{ t('staff.permissions.actions.create') }}</strong> - {{ t('staff.permissions.guidelines.create') }}</li>
            <li>• <strong>{{ t('staff.permissions.actions.edit') }}</strong> - {{ t('staff.permissions.guidelines.edit') }}</li>
            <li>• <strong>{{ t('staff.permissions.actions.delete') }}</strong> - {{ t('staff.permissions.guidelines.delete') }}</li>
          </ul>
          <p class="mt-3 text-xs text-amber-700">
            {{ t('staff.permissions.guidelines.note') }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ModulePermissions, ModuleName, PermissionAction } from '@/types/staff';
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const permissions = defineModel<ModulePermissions>({ required: true });

const modules = computed(() => [
  {
    key: 'appointments' as ModuleName,
    label: t('staff.permissions.modules.appointments.label'),
    description: t('staff.permissions.modules.appointments.description'),
    icon: 'i-lucide-calendar'
  },
  {
    key: 'patients' as ModuleName,
    label: t('staff.permissions.modules.patients.label'),
    description: t('staff.permissions.modules.patients.description'),
    icon: 'i-lucide-users'
  },
  {
    key: 'doctors' as ModuleName,
    label: t('staff.permissions.modules.doctors.label'),
    description: t('staff.permissions.modules.doctors.description'),
    icon: 'i-lucide-stethoscope'
  },
  {
    key: 'services' as ModuleName,
    label: t('staff.permissions.modules.services.label'),
    description: t('staff.permissions.modules.services.description'),
    icon: 'i-lucide-briefcase-medical'
  },
  {
    key: 'treatmentPlans' as ModuleName,
    label: t('staff.permissions.modules.treatmentPlans.label'),
    description: t('staff.permissions.modules.treatmentPlans.description'),
    icon: 'i-lucide-clipboard-list'
  },
  {
    key: 'reports' as ModuleName,
    label: t('staff.permissions.modules.reports.label'),
    description: t('staff.permissions.modules.reports.description'),
    icon: 'i-lucide-bar-chart-3'
  },
  {
    key: 'settings' as ModuleName,
    label: t('staff.permissions.modules.settings.label'),
    description: t('staff.permissions.modules.settings.description'),
    icon: 'i-lucide-settings'
  },
  {
    key: 'staff' as ModuleName,
    label: t('staff.permissions.modules.staff.label'),
    description: t('staff.permissions.modules.staff.description'),
    icon: 'i-lucide-user-cog'
  },
  {
    key: 'blogs' as ModuleName,
    label: t('staff.permissions.modules.blogs.label'),
    description: t('staff.permissions.modules.blogs.description'),
    icon: 'i-lucide-newspaper'
  }
]);

const actions = computed(() => [
  { value: 'VIEW' as PermissionAction, label: t('staff.permissions.actions.view'), color: 'blue' },
  { value: 'CREATE' as PermissionAction, label: t('staff.permissions.actions.create'), color: 'green' },
  { value: 'EDIT' as PermissionAction, label: t('staff.permissions.actions.edit'), color: 'amber' },
  { value: 'DELETE' as PermissionAction, label: t('staff.permissions.actions.delete'), color: 'red' }
]);

function hasAnyPermission(module: ModuleName) {
  return permissions.value[module]?.length > 0;
}

function clearModule(module: ModuleName) {
  permissions.value[module] = [];
}
</script>
