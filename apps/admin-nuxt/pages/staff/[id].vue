<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-edit-3" class="h-6 w-6 text-white" />
            </div>
            <div>
              <div class="flex items-center gap-2 mb-1">
                <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ staff?.fullName || t('staff.detail.loading') }}</h1>
                <UBadge v-if="staff" :color="getStatusColor(staff.status)" variant="soft" size="sm">
                  {{ formatStatus(staff.status) }}
                </UBadge>
              </div>
              <div class="flex items-center gap-2">
                <UBreadcrumb
                  :links="[
                    { label: t('staff.detail.header.breadcrumb.staff'), to: '/staff' },
                    { label: staff?.fullName || t('staff.common.loading') }
                  ]"
                  class="text-sm text-slate-600 dark:text-slate-300"
                />
              </div>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton
              v-if="staff?.hasPendingInvitation"
              variant="ghost"
              color="amber"
              icon="i-lucide-send"
              @click="handleResendInvitation"
              :loading="resending"
            >
              {{ t('staff.detail.actions.resendInvitation') }}
            </UButton>
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-arrow-left"
              @click="navigateTo('/staff')"
            >
              {{ t('staff.detail.actions.back') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">

      <div v-if="loading" class="max-w-4xl">
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 p-6">
          <div class="space-y-4">
            <USkeleton class="h-12 w-full" />
            <USkeleton class="h-12 w-full" />
            <USkeleton class="h-32 w-full" />
          </div>
        </div>
      </div>

      <form v-else-if="staff" @submit.prevent="handleSubmit" class="max-w-4xl space-y-6">
        <!-- Basic Information -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-user" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">{{ t('staff.detail.sections.basicInfo.title') }}</h2>
                <p class="text-sm text-blue-100">{{ t('staff.detail.sections.basicInfo.subtitle') }}</p>
              </div>
            </div>
          </div>
          <div class="p-6">

        <div class="space-y-5">
          <UFormGroup :label="t('staff.detail.form.fields.fullName')" required>
            <UInput
              v-model="form.fullName"
              :placeholder="t('staff.detail.form.placeholders.fullName')"
              size="lg"
              icon="i-lucide-user"
            />
          </UFormGroup>

          <UFormGroup :label="t('staff.detail.form.fields.email')" required>
            <UInput
              v-model="form.email"
              type="email"
              :placeholder="t('staff.detail.form.placeholders.email')"
              size="lg"
              icon="i-lucide-mail"
            />
          </UFormGroup>

          <UFormGroup :label="t('staff.detail.form.fields.role')" required>
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
          </UFormGroup>

          <UFormGroup :label="t('staff.detail.form.fields.status')" required>
            <USelect
              v-model="form.status"
              :options="statuses"
              option-attribute="label"
              value-attribute="value"
              size="lg"
            >
              <template #leading>
                <UIcon name="i-lucide-shield" />
              </template>
            </USelect>
          </UFormGroup>

          <!-- Doctor Selection (only for DOCTOR role) -->
          <UFormGroup
            v-if="form.role === 'DOCTOR'"
            :label="t('staff.detail.form.fields.doctorLink')"
            :help="t('staff.detail.form.fields.doctorLinkHelp')"
          >
            <USelect
              v-model="form.doctorId"
              :options="doctorOptions"
              option-attribute="label"
              value-attribute="value"
              :placeholder="t('staff.detail.form.placeholders.doctorSelect')"
              size="lg"
              :loading="loadingDoctors"
            >
              <template #leading>
                <UIcon name="i-lucide-stethoscope" />
              </template>
            </USelect>
            <template #hint>
              <span class="text-xs text-slate-500">
                {{ staff?.doctor ? t('staff.detail.form.fields.doctorLinkHint', { name: staff.doctor.name }) : t('staff.detail.form.fields.doctorLinkNotLinked') }}
              </span>
            </template>
          </UFormGroup>
        </div>
          </div>
        </div>

        <!-- Permissions -->
        <PermissionManager v-model="form.permissions" />

        <!-- Stats -->
        <div v-if="staff" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-green-500 to-emerald-600 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-info" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">{{ t('staff.detail.sections.accountInfo.title') }}</h2>
                <p class="text-sm text-green-100">{{ t('staff.detail.sections.accountInfo.subtitle') }}</p>
              </div>
            </div>
          </div>
          <div class="p-6">

            <div class="grid gap-4 md:grid-cols-3">
              <div class="rounded-xl border border-slate-200 dark:border-slate-600 bg-slate-50 dark:bg-slate-700/50 p-4">
                <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('staff.detail.stats.accountStatus') }}</p>
                <p class="mt-1 text-sm font-medium text-slate-900 dark:text-white">{{ formatStatus(staff.status) }}</p>
              </div>
              <div class="rounded-xl border border-slate-200 dark:border-slate-600 bg-slate-50 dark:bg-slate-700/50 p-4">
                <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('staff.detail.stats.memberSince') }}</p>
                <p class="mt-1 text-sm font-medium text-slate-900 dark:text-white">{{ formatDate(staff.createdAt) }}</p>
              </div>
              <div class="rounded-xl border border-slate-200 dark:border-slate-600 bg-slate-50 dark:bg-slate-700/50 p-4">
                <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('staff.detail.stats.totalPermissions') }}</p>
                <p class="mt-1 text-sm font-medium text-slate-900 dark:text-white">{{ getPermissionCount(staff.permissions) }}</p>
              </div>
            </div>
          </div>
        </div>

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
              {{ t('staff.detail.actions.cancel') }}
            </UButton>
            <UButton
              type="submit"
              color="blue"
              :loading="saving"
              icon="i-lucide-save"
            >
              {{ t('staff.detail.actions.save') }}
            </UButton>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Staff, StaffRole, StaffStatus, ModulePermissions } from '@/types/staff';
import { useI18n } from 'vue-i18n';

const route = useRoute();
const router = useRouter();
const toast = useEnhancedToast();
const staffApi = useStaff();
const { t } = useI18n();

useHead(() => ({
  title: t('staff.detail.meta.headTitle')
}));

const id = computed(() => Number(route.params.id));

const roles = computed(() => [
  { value: 'ADMIN' as StaffRole, label: t('staff.common.roles.admin') },
  { value: 'RECEPTIONIST' as StaffRole, label: t('staff.common.roles.receptionist') },
  { value: 'DOCTOR' as StaffRole, label: t('staff.common.roles.doctor') }
]);

const statuses = computed(() => [
  { value: 'ACTIVE' as StaffStatus, label: t('staff.common.status.active') },
  { value: 'INVITED' as StaffStatus, label: t('staff.common.status.invited') },
  { value: 'SUSPENDED' as StaffStatus, label: t('staff.common.status.suspended') }
]);

const { data: staff, pending: loading, refresh } = await useAsyncData(
  `staff-${id.value}`,
  () => staffApi.getStaffById(id.value)
);

const form = reactive({
  fullName: '',
  email: '',
  role: 'RECEPTIONIST' as StaffRole,
  status: 'ACTIVE' as StaffStatus,
  doctorId: null as number | null,
  permissions: {
    appointments: [],
    patients: [],
    doctors: [],
    services: [],
    treatmentPlans: [],
    reports: [],
    settings: [],
    staff: [],
    blogs: []
  } as ModulePermissions
});

const saving = ref(false);
const resending = ref(false);

// Fetch doctors list using authenticated API
const { fetcher, request: apiRequest } = useAdminApi();
const { data: doctorsData, pending: loadingDoctors } = await useAsyncData(
  'doctors-for-staff-edit',
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

// Populate form when staff data loads
watch(
  staff,
  (newStaff) => {
    if (newStaff) {
      form.fullName = newStaff.fullName;
      form.email = newStaff.email;
      form.role = newStaff.role;
      form.status = newStaff.status;
      form.doctorId = newStaff.doctor?.id || null;
      form.permissions = { ...newStaff.permissions };
    }
  },
  { immediate: true }
);

function getStatusColor(status: StaffStatus) {
  switch (status) {
    case 'ACTIVE': return 'green';
    case 'INVITED': return 'amber';
    case 'SUSPENDED': return 'red';
    default: return 'gray';
  }
}

function formatStatus(status: StaffStatus) {
  const statusLabels = {
    'ACTIVE': t('staff.common.status.active'),
    'INVITED': t('staff.common.status.invited'),
    'SUSPENDED': t('staff.common.status.suspended')
  };
  return statusLabels[status] || status;
}

const { formatDate } = useDateFormatting()

function getPermissionCount(permissions: ModulePermissions) {
  return Object.values(permissions).reduce((count, actions) => count + actions.length, 0);
}

async function handleResendInvitation() {
  resending.value = true;
  try {
    await staffApi.resendInvitation(id.value);
    toast.success({ title: t('staff.detail.toasts.invitationSuccess') });
    await refresh();
  } catch (error: any) {
    toast.error({ title: t('staff.detail.toasts.invitationError'), error });
  } finally {
    resending.value = false;
  }
}

async function handleSubmit() {
  // Validate
  if (!form.fullName.trim()) {
    toast.error({ title: t('staff.detail.toasts.validationError.fullName') });
    return;
  }

  if (!form.email.trim() || !form.email.includes('@')) {
    toast.error({ title: t('staff.detail.toasts.validationError.email') });
    return;
  }

  // Check if at least one permission is granted
  const hasPermissions = Object.values(form.permissions).some(perms => perms.length > 0);
  if (!hasPermissions) {
    toast.error({
      title: t('staff.detail.toasts.validationError.noPermissions.title'),
      description: t('staff.detail.toasts.validationError.noPermissions.description')
    });
    return;
  }

  saving.value = true;

  try {
    await staffApi.updateStaff(id.value, {
      email: form.email.trim(),
      fullName: form.fullName.trim(),
      role: form.role,
      status: form.status,
      permissions: form.permissions,
      doctorId: form.doctorId || undefined
    });

    toast.success({ title: t('staff.detail.toasts.updateSuccess') });
    await refresh();
  } catch (error: any) {
    toast.error({
      title: t('staff.detail.toasts.updateError'),
      error
    });
  } finally {
    saving.value = false;
  }
}
</script>
