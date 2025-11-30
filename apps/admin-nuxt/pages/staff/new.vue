<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-user-plus" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('staff.create.header.title') }}</h1>
              <div class="flex items-center gap-2">
                <UBreadcrumb
                  :links="[
                    { label: t('staff.create.header.breadcrumb.staff'), to: '/staff' },
                    { label: t('staff.create.header.breadcrumb.addNew') }
                  ]"
                  class="text-sm text-slate-600 dark:text-slate-300"
                />
              </div>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-arrow-left"
              @click="navigateTo('/staff')"
            >
              {{ t('staff.create.actions.backToStaff') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">

      <form @submit.prevent="handleSubmit" class="max-w-4xl space-y-6">
        <!-- Basic Information -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-user" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">{{ t('staff.create.form.basicInfo.title') }}</h2>
                <p class="text-sm text-blue-100">{{ t('staff.create.form.basicInfo.subtitle') }}</p>
              </div>
            </div>
          </div>
          <div class="p-6">

            <div class="space-y-5">
              <UFormGroup :label="t('staff.create.form.fields.firstName')" required>
                <UInput
                  v-model="form.fullName"
                  :placeholder="t('staff.create.form.placeholders.firstName')"
                  size="lg"
                  icon="i-lucide-user"
                />
              </UFormGroup>

              <UFormGroup :label="t('staff.create.form.fields.email')" required>
                <UInput
                  v-model="form.email"
                  type="email"
                  :placeholder="t('staff.create.form.placeholders.email')"
                  size="lg"
                  icon="i-lucide-mail"
                />
                <template #hint>
                  <span class="text-xs text-slate-500 dark:text-slate-400">{{ t('staff.create.form.hints.emailInvitation') }}</span>
                </template>
              </UFormGroup>

              <UFormGroup :label="t('staff.create.form.fields.role')" required>
                <USelect
                  v-model="form.role"
                  :options="roles"
                  option-attribute="label"
                  value-attribute="value"
                  size="lg"
                >
                  <template #leading>
                    <UIcon name="i-lucide-briefcase" />
                  </template>
                </USelect>
                <template #hint>
                  <span class="text-xs text-slate-500 dark:text-slate-400">{{ getRoleDescription(form.role) }}</span>
                </template>
              </UFormGroup>

              <!-- Doctor Selection (only for DOCTOR role) -->
              <UFormGroup
                v-if="form.role === 'DOCTOR'"
                :label="t('staff.create.form.fields.doctorLink')"
                :help="t('staff.create.form.fields.doctorLinkHelp')"
              >
                <USelect
                  v-model="form.doctorId"
                  :options="doctorOptions"
                  option-attribute="label"
                  value-attribute="value"
                  :placeholder="t('staff.create.form.placeholders.doctorSelect')"
                  size="lg"
                  :loading="loadingDoctors"
                >
                  <template #leading>
                    <UIcon name="i-lucide-stethoscope" />
                  </template>
                </USelect>
                <template #hint>
                  <span class="text-xs text-slate-500 dark:text-slate-400">
                    {{ t('staff.create.form.hints.doctorLink') }}
                  </span>
                </template>
              </UFormGroup>
            </div>
          </div>
        </div>

        <!-- Permissions -->
        <PermissionManager v-model="form.permissions" />

        <!-- Actions -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 p-6">
          <div class="flex justify-end gap-3">
            <UButton
              type="button"
              variant="ghost"
              color="gray"
              :disabled="saving"
              @click="navigateTo('/staff')"
            >
              {{ t('staff.create.actions.cancel') }}
            </UButton>
            <UButton
              type="submit"
              color="blue"
              :loading="saving"
              icon="i-lucide-user-plus"
            >
              {{ t('staff.create.actions.save') }}
            </UButton>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { StaffRole, ModulePermissions } from '@/types/staff';
import { watch, computed, ref, reactive } from 'vue';
import { useI18n } from 'vue-i18n';

const toast = useEnhancedToast();
const router = useRouter();
const staffApi = useStaff();
const { t } = useI18n();

useHead(() => ({
  title: t('staff.meta.createTitle')
}));

const roles = computed(() => [
  {
    value: 'ADMIN' as StaffRole,
    label: t('staff.common.roles.admin')
  },
  {
    value: 'RECEPTIONIST' as StaffRole,
    label: t('staff.common.roles.receptionist')
  },
  {
    value: 'DOCTOR' as StaffRole,
    label: t('staff.common.roles.doctor')
  }
]);

const form = reactive({
  fullName: '',
  email: '',
  role: 'RECEPTIONIST' as StaffRole,
  doctorId: null as number | null,
  permissions: {
    appointments: [],
    calendar: [],
    patients: [],
    doctors: [],
    materials: [],
    services: [],
    insuranceCompanies: [],
    treatmentPlans: [],
    reports: [],
    billing: [],
    translations: [],
    settings: [],
    clinicSettings: [],
    staff: [],
    blogs: []
  } as ModulePermissions
});

const saving = ref(false);

// Fetch doctors list using authenticated API
const { fetcher } = useAdminApi();
const { data: doctorsData, pending: loadingDoctors } = await useAsyncData(
  'doctors-for-staff',
  () => fetcher<any[]>('/doctors', [])
);

const doctors = computed(() => doctorsData.value ?? []);

// Doctor options for dropdown
const doctorOptions = computed(() => {
  return doctors.value.map(doctor => ({
    value: doctor.id,
    label: `${doctor.fullNameEn || doctor.fullNameAr || (doctor as any).fullName}${(doctor.specialtyEn || doctor.specialtyAr || (doctor as any).specialty) ? ` - ${doctor.specialtyEn || doctor.specialtyAr || (doctor as any).specialty}` : ''}`
  }));
});

// Watch role changes to clear doctorId when role is not DOCTOR
watch(() => form.role, (newRole) => {
  if (newRole !== 'DOCTOR') {
    form.doctorId = null;
  }
});

function getRoleDescription(role: StaffRole) {
  switch (role) {
    case 'ADMIN':
      return t('staff.create.form.roleDescriptions.admin');
    case 'RECEPTIONIST':
      return t('staff.create.form.roleDescriptions.receptionist');
    case 'DOCTOR':
      return t('staff.create.form.roleDescriptions.doctor');
    default:
      return '';
  }
}

async function handleSubmit() {
  // Validate
  if (!form.fullName.trim()) {
    toast.error({ title: t('staff.create.validation.fullNameRequired') });
    return;
  }

  if (!form.email.trim() || !form.email.includes('@')) {
    toast.error({ title: t('staff.create.validation.emailRequired') });
    return;
  }

  // Check if at least one permission is granted
  const hasPermissions = Object.values(form.permissions).some(perms => perms.length > 0);
  if (!hasPermissions) {
    toast.error({
      title: t('staff.create.validation.noPermissions.title'),
      description: t('staff.create.validation.noPermissions.description')
    });
    return;
  }

  saving.value = true;

  try {
    await staffApi.createStaff({
      email: form.email.trim(),
      fullName: form.fullName.trim(),
      role: form.role,
      permissions: form.permissions,
      doctorId: form.doctorId || undefined
    });

    toast.success({
      title: t('staff.create.toasts.createSuccess.title'),
      description: t('staff.create.toasts.createSuccess.description')
    });

    router.push('/staff');
  } catch (error: any) {
    toast.error({
      title: t('staff.create.toasts.createError'),
      error
    });
  } finally {
    saving.value = false;
  }
}
</script>
