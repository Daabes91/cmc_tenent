<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-users" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('staff.list.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('staff.list.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="loading"
              @click="refresh"
            >
              {{ t('staff.list.actions.refresh') }}
            </UButton>
            <UButton 
              color="blue" 
              icon="i-lucide-user-plus" 
              @click="navigateTo('/staff/new')"
            >
              {{ t('staff.list.actions.addStaff') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">

      <!-- Quick Stats -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
              <UIcon name="i-lucide-users" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('staff.list.stats.totalStaff') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ staffList.length }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">Team members</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
              <UIcon name="i-lucide-user-check" class="h-5 w-5 text-green-600 dark:text-green-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('staff.list.stats.activeStaff') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ activeCount }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">Currently working</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-900/20">
              <UIcon name="i-lucide-mail" class="h-5 w-5 text-amber-600 dark:text-amber-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('staff.list.stats.pendingInvitations') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ invitedCount }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">Awaiting response</p>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200">
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-50 dark:bg-purple-900/20">
              <UIcon name="i-lucide-shield" class="h-5 w-5 text-purple-600 dark:text-purple-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ t('staff.list.stats.adminUsers') }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ adminCount }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">Full access</p>
        </div>
      </div>

      <!-- Staff List -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-users" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">{{ t('staff.list.table.title') }}</h2>
                <p class="text-sm text-blue-100">{{ staffList.length }} {{ t('staff.list.table.totalMembers') }}</p>
              </div>
            </div>
            <div class="relative w-64">
              <UInput
                v-model="search"
                :placeholder="t('staff.list.filters.search')"
                size="lg"
                class="bg-white/10 border-white/20 text-white placeholder-white/70 pl-4"
              />
            </div>
          </div>
        </div>

        <div class="p-6">

          <div v-if="loading" class="space-y-4">
            <USkeleton class="h-20 w-full" v-for="i in 3" :key="i" />
          </div>

          <div v-else-if="filteredStaff.length === 0" class="py-12 text-center">
            <div class="flex flex-col items-center gap-4">
              <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                <UIcon name="i-lucide-users" class="h-8 w-8 text-slate-400" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('staff.list.empty.title') }}</h3>
                <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                  {{ search ? t('staff.list.empty.searchHint') : t('staff.list.empty.description') }}
                </p>
              </div>
              <UButton
                v-if="!search"
                color="blue"
                icon="i-lucide-user-plus"
                @click="navigateTo('/staff/new')"
              >
                {{ t('staff.list.actions.addStaff') }}
              </UButton>
            </div>
          </div>

          <div v-else class="space-y-3">
        <div
          v-for="staff in filteredStaff"
          :key="staff.id"
          class="rounded-lg border border-slate-200 p-4 hover:border-violet-300 hover:shadow-md transition-all"
        >
          <div class="flex items-start justify-between">
            <div class="flex items-start gap-4 flex-1">
              <!-- Avatar -->
              <div class="flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-violet-600 to-purple-600 text-white font-bold text-lg">
                {{ staff.fullName.charAt(0).toUpperCase() }}
              </div>

              <!-- Info -->
              <div class="flex-1">
                <div class="flex items-center gap-2">
                  <h3 class="font-semibold text-slate-900">{{ staff.fullName }}</h3>
                  <UBadge :color="getStatusColor(staff.status)" size="xs">
                    {{ staff.status }}
                  </UBadge>
                  <UBadge v-if="staff.hasPendingInvitation" color="amber" size="xs">
                    <UIcon name="i-lucide-mail" class="h-3 w-3" />
                    {{ t('staff.common.invitationSent') }}
                  </UBadge>
                </div>
                <p class="text-sm text-slate-500 mt-0.5">{{ staff.email }}</p>
                <div class="mt-2 flex items-center gap-3">
                  <div class="flex items-center gap-1.5">
                    <UIcon name="i-lucide-briefcase" class="h-4 w-4 text-slate-400" />
                    <span class="text-sm font-medium text-slate-600">{{ staff.role }}</span>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <UIcon name="i-lucide-shield-check" class="h-4 w-4 text-slate-400" />
                    <span class="text-sm text-slate-600">{{ getPermissionCount(staff.permissions) }} {{ t('staff.common.permissions') }}</span>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <UIcon name="i-lucide-calendar" class="h-4 w-4 text-slate-400" />
                    <span class="text-sm text-slate-600">{{ t('staff.common.joined') }} {{ formatDate(staff.createdAt) }}</span>
                  </div>
                </div>

                <!-- Permission Summary -->
                <div class="mt-3 flex flex-wrap gap-1">
                  <UBadge
                    v-for="module in getActiveModules(staff.permissions)"
                    :key="module"
                    color="violet"
                    variant="soft"
                    size="xs"
                  >
                    {{ module }}
                  </UBadge>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex items-center gap-2">
              <UButton
                v-if="staff.hasPendingInvitation"
                icon="i-lucide-send"
                size="sm"
                color="amber"
                variant="soft"
                @click="handleResendInvitation(staff.id)"
                :loading="resending === staff.id"
              >
                {{ t('staff.list.actions.resendInvitation') }}
              </UButton>
              <UButton
                icon="i-lucide-edit"
                size="sm"
                color="gray"
                variant="ghost"
                @click="navigateTo(`/staff/${staff.id}`)"
              >
                {{ t('staff.list.actions.edit') }}
              </UButton>
              <UButton
                icon="i-lucide-trash"
                size="sm"
                color="red"
                variant="ghost"
                @click="confirmDelete(staff)"
              >
                {{ t('staff.list.actions.delete') }}
              </UButton>
            </div>
          </div>
        </div>
          </div>
        </div>
      </div>

    <!-- Delete Confirmation Modal -->
    <UModal v-model="deleteModal">
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <div class="rounded-lg bg-red-100 p-2">
              <UIcon name="i-lucide-alert-triangle" class="h-5 w-5 text-red-600" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-slate-900">Delete Staff Member</h3>
              <p class="text-sm text-slate-500">This action cannot be undone</p>
            </div>
          </div>
        </template>

        <div class="space-y-4">
          <p class="text-sm text-slate-700">
            Are you sure you want to delete <strong>{{ staffToDelete?.fullName }}</strong>?
            This will permanently remove their account and all associated permissions.
          </p>
        </div>

        <template #footer>
          <div class="flex justify-end gap-3">
            <UButton color="gray" variant="ghost" @click="deleteModal = false">
              Cancel
            </UButton>
            <UButton color="red" @click="handleDelete" :loading="deleting">
              Delete Staff Member
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Staff, ModulePermissions } from '@/types/staff';
import { computed, watchEffect, ref } from 'vue';
import { useI18n } from 'vue-i18n';

const toast = useEnhancedToast();
const staffApi = useStaff();
const { t } = useI18n();

useHead(() => ({
  title: t('staff.meta.listTitle')
}));

const search = ref('');
const loading = ref(true);
const staffList = ref<Staff[]>([]);
const resending = ref<number | null>(null);
const deleting = ref(false);
const deleteModal = ref(false);
const staffToDelete = ref<Staff | null>(null);

// Fetch staff
const { data, pending, refresh } = await useAsyncData('staff-list', () => staffApi.getAllStaff());

watchEffect(() => {
  if (data.value) {
    staffList.value = data.value;
  }
  loading.value = pending.value;
});

// Computed stats
const activeCount = computed(() => staffList.value.filter(s => s.status === 'ACTIVE').length);
const invitedCount = computed(() => staffList.value.filter(s => s.status === 'INVITED').length);
const adminCount = computed(() => staffList.value.filter(s => s.role === 'ADMIN').length);

// Filtered staff
const filteredStaff = computed(() => {
  if (!search.value) return staffList.value;
  const query = search.value.toLowerCase();
  return staffList.value.filter(
    s => s.fullName.toLowerCase().includes(query) || s.email.toLowerCase().includes(query)
  );
});

function getStatusColor(status: string) {
  switch (status) {
    case 'ACTIVE': return 'green';
    case 'INVITED': return 'amber';
    case 'SUSPENDED': return 'red';
    default: return 'gray';
  }
}

function getPermissionCount(permissions: ModulePermissions) {
  return Object.values(permissions).reduce((count, actions) => count + actions.length, 0);
}

function getActiveModules(permissions: ModulePermissions) {
  const moduleNames: Record<string, string> = {
    appointments: 'Appointments',
    patients: 'Patients',
    doctors: 'Doctors',
    services: 'Services',
    treatmentPlans: 'Treatment Plans',
    reports: 'Reports',
    settings: 'Settings',
    staff: 'Staff',
    blogs: 'Blogs'
  };

  return Object.entries(permissions)
    .filter(([_, actions]) => actions.includes('VIEW'))
    .map(([key]) => moduleNames[key] || key);
}

const { formatDate } = useDateFormatting()

async function handleResendInvitation(staffId: number) {
  resending.value = staffId;
  try {
    await staffApi.resendInvitation(staffId);
    toast.success({ title: t('staff.toasts.invitationSent') });
  } catch (error: any) {
    toast.error({ title: t('staff.toasts.invitationError'), error });
  } finally {
    resending.value = null;
  }
}

function confirmDelete(staff: Staff) {
  staffToDelete.value = staff;
  deleteModal.value = true;
}

async function handleDelete() {
  if (!staffToDelete.value) return;

  deleting.value = true;
  try {
    await staffApi.deleteStaff(staffToDelete.value.id);
    toast.success({ title: t('staff.toasts.deleteSuccess') });
    deleteModal.value = false;
    staffToDelete.value = null;
    await refresh();
  } catch (error: any) {
    toast.error({ title: t('staff.toasts.deleteError'), error });
  } finally {
    deleting.value = false;
  }
}
</script>
