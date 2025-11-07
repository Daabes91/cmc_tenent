<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-settings" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('settings.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('settings.header.subtitle') }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">
      <!-- Quick Stats -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <div
          v-for="metric in overviewMetrics"
          :key="metric.label"
          class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200"
        >
          <div class="flex items-center gap-3 mb-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
              <UIcon :name="getMetricIcon(metric.label)" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ metric.label }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ metric.value }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ metric.description }}</p>
        </div>
      </div>

      <div v-if="error" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-red-200/60 dark:border-red-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-red-500 to-red-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-alert-circle" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('settings.errors.loadTitle') }}</h2>
              <p class="text-sm text-red-100">{{ t('settings.errors.loadSubtitle') }}</p>
            </div>
          </div>
        </div>
        <div class="p-6">
          <p class="text-sm text-slate-600 dark:text-slate-300">{{ error }}</p>
        </div>
      </div>

      <div v-else class="grid gap-6 lg:grid-cols-[minmax(0,1.8fr)_minmax(0,1fr)]">
        <div class="space-y-6">
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-user" class="h-5 w-5 text-white" />
                <div>
                  <h2 class="text-lg font-semibold text-white">{{ t('settings.sections.profile.title') }}</h2>
                  <p class="text-sm text-blue-100">{{ t('settings.sections.profile.subtitle') }}</p>
                </div>
              </div>
            </div>
            <div class="p-6">

          <div v-if="loading" class="space-y-4">
            <USkeleton class="h-24 rounded-2xl" />
            <USkeleton class="h-24 rounded-2xl" />
          </div>
          <div v-else-if="profile" class="space-y-6">
            <div class="flex flex-col gap-4 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-5 sm:flex-row sm:items-center sm:gap-6">
              <UAvatar :alt="profile.fullName" size="lg" class="ring-4 ring-white shadow-inner" />
              <div class="space-y-1">
                <h3 class="text-lg font-semibold text-slate-900">{{ profile.fullName }}</h3>
                <p class="text-sm text-slate-600">{{ profile.email }}</p>
                <UBadge :color="roleBadge.color" variant="soft" size="sm">
                  {{ roleBadge.label }}
                </UBadge>
              </div>
            </div>

            <form class="space-y-5" @submit.prevent="saveProfile">
              <div class="grid gap-4 md:grid-cols-2">
                <UFormGroup :label="t('settings.form.profile.fullName')" name="fullName" required>
                  <UInput
                    v-model="profileForm.fullName"
                    :placeholder="t('settings.form.profile.fullNamePlaceholder')"
                    :disabled="savingProfile"
                  />
                </UFormGroup>
                <UFormGroup :label="t('settings.form.profile.email')" name="email" required>
                  <UInput
                    v-model="profileForm.email"
                    type="email"
                    :placeholder="t('settings.form.profile.emailPlaceholder')"
                    :disabled="savingProfile"
                  />
                </UFormGroup>
              </div>

              <div
                v-if="profileSubmitError"
                class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600"
              >
                {{ profileSubmitError }}
              </div>

              <div class="flex flex-wrap items-center justify-end gap-3">
                <UButton
                  type="button"
                  variant="ghost"
                  color="gray"
                  size="sm"
                  :disabled="!isProfileDirty || savingProfile"
                  @click="resetProfileForm"
                >
                  {{ t('settings.actions.reset') }}
                </UButton>
                <UButton
                  type="submit"
                  color="violet"
                  size="sm"
                  :loading="savingProfile"
                  :disabled="!canSubmitProfile"
                  class="shadow-sm hover:shadow-md"
                >
                  {{ t('settings.actions.saveChanges') }}
                </UButton>
              </div>
            </form>

            <div class="grid gap-4 md:grid-cols-2">
              <div class="rounded-2xl border border-slate-200 bg-white p-4">
                <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('settings.profile.fullName') }}</p>
                <p class="mt-1 text-sm font-medium text-slate-900">{{ profile.fullName }}</p>
              </div>
              <div class="rounded-2xl border border-slate-200 bg-white p-4">
                <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('settings.profile.email') }}</p>
                <p class="mt-1 text-sm font-medium text-slate-900">{{ profile.email }}</p>
              </div>
              <div class="rounded-2xl border border-slate-200 bg-white p-4">
                <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('settings.profile.role') }}</p>
                <p class="mt-1 text-sm font-medium text-slate-900">{{ roleBadge.label }}</p>
              </div>
              <div class="rounded-2xl border border-slate-200 bg-white p-4">
                <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('settings.profile.accountId') }}</p>
                <p class="mt-1 text-sm font-medium text-slate-900">#{{ profile.id }}</p>
              </div>
            </div>
          </div>
            </div>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-green-500 to-emerald-600 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-lock" class="h-5 w-5 text-white" />
                <div>
                  <h2 class="text-lg font-semibold text-white">{{ t('settings.sections.security.title') }}</h2>
                  <p class="text-sm text-green-100">{{ t('settings.sections.security.subtitle') }}</p>
                </div>
              </div>
            </div>
            <div class="p-6">

          <div class="space-y-4">
            <div class="rounded-2xl border border-slate-200 bg-white p-5">
              <form class="space-y-4" @submit.prevent="savePassword">
                <div class="flex items-start justify-between">
                  <div>
                    <h3 class="text-sm font-semibold text-slate-900">{{ t('settings.security.password.title') }}</h3>
                    <p class="text-xs text-slate-500">{{ t('settings.security.password.description') }}</p>
                  </div>
                </div>

                <div class="grid gap-4 md:grid-cols-2">
                  <UFormGroup :label="t('settings.security.password.currentPassword')" name="currentPassword" required>
                    <UInput
                      v-model="passwordForm.currentPassword"
                      type="password"
                      autocomplete="current-password"
                      :placeholder="t('settings.security.password.currentPasswordPlaceholder')"
                      :disabled="savingPassword"
                    />
                  </UFormGroup>
                  <UFormGroup :label="t('settings.security.password.newPassword')" name="newPassword" required>
                    <UInput
                      v-model="passwordForm.newPassword"
                      type="password"
                      autocomplete="new-password"
                      :placeholder="t('settings.security.password.newPasswordPlaceholder')"
                      :disabled="savingPassword"
                    />
                    <p class="mt-1 text-xs text-slate-500">
                      {{ t('settings.security.password.requirements') }}
                    </p>
                  </UFormGroup>
                  <UFormGroup :label="t('settings.security.password.confirmPassword')" name="confirmPassword" required>
                    <UInput
                      v-model="passwordForm.confirmPassword"
                      type="password"
                      autocomplete="new-password"
                      :placeholder="t('settings.security.password.confirmPasswordPlaceholder')"
                      :disabled="savingPassword"
                    />
                    <p v-if="passwordForm.confirmPassword && !passwordsMatch" class="mt-1 text-xs text-red-600">
                      {{ t('settings.security.password.passwordsDoNotMatch') }}
                    </p>
                  </UFormGroup>
                </div>

                <div
                  v-if="passwordError"
                  class="rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-600"
                >
                  {{ passwordError }}
                </div>

                <div class="flex items-center justify-end gap-3">
                  <UButton
                    type="button"
                    variant="ghost"
                    color="gray"
                    size="sm"
                    :disabled="!passwordFormIsDirty || savingPassword"
                    @click="resetPasswordForm"
                  >
                    {{ t('settings.actions.reset') }}
                  </UButton>
                  <UButton
                    type="submit"
                    color="violet"
                    size="sm"
                    :loading="savingPassword"
                    :disabled="!canSubmitPassword"
                    class="shadow-sm hover:shadow-md"
                  >
                    {{ t('settings.actions.updatePassword') }}
                  </UButton>
                </div>
              </form>
            </div>
            <div class="rounded-2xl border border-slate-200 bg-white p-4">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <h3 class="text-sm font-semibold text-slate-900">{{ t('settings.security.twoFactor.title') }}</h3>
                  <p class="text-xs text-slate-500">{{ t('settings.security.twoFactor.description') }}</p>
                </div>
                <UButton size="xs" color="gray" variant="ghost" disabled>{{ t('settings.security.twoFactor.enable') }}</UButton>
              </div>
            </div>
          </div>
            </div>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-amber-500 to-orange-600 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-bell" class="h-5 w-5 text-white" />
                <div>
                  <h2 class="text-lg font-semibold text-white">{{ t('settings.sections.notifications.title') }}</h2>
                  <p class="text-sm text-amber-100">{{ t('settings.sections.notifications.subtitle') }}</p>
                </div>
              </div>
            </div>
            <div class="p-6">

          <div class="space-y-4">
            <!-- Sound Notifications -->
            <div class="rounded-2xl border border-slate-200 bg-white p-4">
              <div class="flex items-center justify-between gap-4">
                <div>
                  <h3 class="text-sm font-semibold text-slate-900">{{ t('settings.notifications.sound.title') }}</h3>
                  <p class="text-xs text-slate-500">{{ t('settings.notifications.sound.description') }}</p>
                </div>
                <UToggle
                  :model-value="notificationPreferences.soundEnabled"
                  @update:model-value="updatePreference('soundEnabled', $event)"
                />
              </div>
            </div>

            <!-- Browser Notifications -->
            <div class="rounded-2xl border border-slate-200 bg-white p-4">
              <div class="flex flex-col gap-3">
                <div class="flex items-center justify-between gap-4">
                  <div>
                    <h3 class="text-sm font-semibold text-slate-900">{{ t('settings.notifications.browser.title') }}</h3>
                    <p class="text-xs text-slate-500">{{ t('settings.notifications.browser.description') }}</p>
                  </div>
                  <UToggle
                    :model-value="notificationPreferences.browserNotificationsEnabled"
                    @update:model-value="updatePreference('browserNotificationsEnabled', $event)"
                    :disabled="!browserNotificationsAvailable && !notificationPreferences.browserNotificationsEnabled"
                  />
                </div>
                <div v-if="!browserNotificationsAvailable && notificationPreferences.browserNotificationsEnabled" class="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2">
                  <p class="text-xs text-amber-800 mb-2">{{ t('settings.notifications.browser.notEnabled') }}</p>
                  <UButton
                    size="xs"
                    color="amber"
                    variant="soft"
                    @click="requestBrowserPermission"
                    icon="i-lucide-bell-ring"
                  >
                    {{ t('settings.notifications.browser.enableButton') }}
                  </UButton>
                </div>
              </div>
            </div>

            <!-- Appointment Notifications -->
            <div class="rounded-2xl border border-slate-200 bg-white p-4">
              <div class="flex items-center justify-between gap-4">
                <div>
                  <h3 class="text-sm font-semibold text-slate-900">{{ t('settings.notifications.appointments.title') }}</h3>
                  <p class="text-xs text-slate-500">{{ t('settings.notifications.appointments.description') }}</p>
                </div>
                <UToggle
                  :model-value="notificationPreferences.appointmentNotifications"
                  @update:model-value="updatePreference('appointmentNotifications', $event)"
                />
              </div>
            </div>

            <!-- Polling Interval -->
            <div class="rounded-2xl border border-slate-200 bg-white p-4">
              <div class="space-y-3">
                <div>
                  <h3 class="text-sm font-semibold text-slate-900">{{ t('settings.notifications.interval.title') }}</h3>
                  <p class="text-xs text-slate-500">{{ t('settings.notifications.interval.description') }}</p>
                </div>
                <UFormGroup>
                  <USelect
                    :model-value="notificationPreferences.pollingInterval"
                    @update:model-value="updatePreference('pollingInterval', $event)"
                    :options="pollingIntervalOptions"
                  />
                </UFormGroup>
              </div>
            </div>
          </div>
            </div>
          </div>
        </div>

        <aside class="space-y-6">
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-purple-500 to-violet-600 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-info" class="h-5 w-5 text-white" />
                <div>
                  <h3 class="text-lg font-semibold text-white">{{ t('settings.sidebar.snapshot.title') }}</h3>
                  <p class="text-sm text-purple-100">{{ t('settings.sidebar.snapshot.subtitle') }}</p>
                </div>
              </div>
            </div>
            <div class="p-6">
          <div v-if="loading" class="space-y-2">
            <USkeleton class="h-10 rounded-2xl" />
            <USkeleton class="h-10 rounded-2xl" />
            <USkeleton class="h-10 rounded-2xl" />
          </div>
          <ul v-else class="space-y-2 text-sm text-slate-600">
            <li class="flex items-center gap-2 rounded-xl bg-slate-50 px-3 py-2">
              <UIcon name="i-lucide-mail" class="h-4 w-4 text-sky-500" />
              <span>{{ profile?.email ?? t('settings.sidebar.snapshot.emailNotAvailable') }}</span>
            </li>
            <li class="flex items-center gap-2 rounded-xl bg-slate-50 px-3 py-2">
              <UIcon name="i-lucide-shield-check" class="h-4 w-4 text-emerald-500" />
              <span>{{ t('settings.sidebar.snapshot.role') }}: {{ roleBadge.label }}</span>
            </li>
            <li class="flex items-center gap-2 rounded-xl bg-slate-50 px-3 py-2">
              <UIcon name="i-lucide-hash" class="h-4 w-4 text-purple-500" />
              <span>{{ t('settings.sidebar.snapshot.staffId') }}: {{ profile?.id ?? "—" }}</span>
            </li>
          </ul>
            </div>
          </div>

          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-clock" class="h-5 w-5 text-white" />
                <div>
                  <h3 class="text-lg font-semibold text-white">{{ t('settings.sidebar.activity.title') }}</h3>
                  <p class="text-sm text-slate-300">{{ t('settings.sidebar.activity.subtitle') }}</p>
                </div>
              </div>
            </div>
            <div class="p-6">
              <div class="space-y-3 text-sm text-slate-600 dark:text-slate-300">
                <div class="flex items-center gap-2">
                  <UIcon name="i-lucide-log-in" class="h-4 w-4 text-slate-400" />
                  <span>{{ t('settings.sidebar.activity.lastLogin') }}: {{ lastLogin }}</span>
                </div>
                <div class="flex items-center gap-2">
                  <UIcon name="i-lucide-key" class="h-4 w-4 text-slate-400" />
                  <span>{{ t('settings.sidebar.activity.passwordUpdated') }}: {{ passwordUpdated }}</span>
                </div>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { StaffProfile } from "@/types/staff";
import { useI18n } from 'vue-i18n';
import { computed, reactive, ref, watch } from 'vue';

const { t } = useI18n();
const { fetcher, request } = useAdminApi();
const toast = useToast();
const auth = useAuth();
const {
  preferences: notificationPreferences,
  savePreferences,
  requestBrowserPermission: requestBrowserNotificationPermission,
  browserNotificationsAvailable
} = useNotificationPreferences();

useHead(() => ({ title: t('settings.meta.title') }));

const { data, pending: loading, error: fetchError, refresh } = await useAsyncData(
  "staff-profile",
  async () => {
    return await fetcher<StaffProfile>("/auth/profile");
  }
);

const profile = computed(() => data.value ?? null);
const error = computed(() => fetchError.value?.message);

const profileForm = reactive({
  fullName: "",
  email: ""
});
const savingProfile = ref(false);
const profileSubmitError = ref<string | null>(null);

const passwordForm = reactive({
  currentPassword: "",
  newPassword: "",
  confirmPassword: ""
});
const savingPassword = ref(false);
const passwordError = ref<string | null>(null);

watch(
  profile,
  value => {
    if (!value) {
      profileForm.fullName = "";
      profileForm.email = "";
      return;
    }
    profileForm.fullName = value.fullName ?? "";
    profileForm.email = value.email ?? "";
  },
  { immediate: true }
);

watch(
  () => [profileForm.fullName, profileForm.email],
  () => {
    profileSubmitError.value = null;
  }
);

watch(
  () => [passwordForm.currentPassword, passwordForm.newPassword, passwordForm.confirmPassword],
  () => {
    passwordError.value = null;
  }
);

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const normalizedProfileForm = computed(() => ({
  fullName: profileForm.fullName.trim(),
  email: profileForm.email.trim()
}));

const normalizedPasswordForm = computed(() => ({
  current: passwordForm.currentPassword.trim(),
  next: passwordForm.newPassword.trim(),
  confirm: passwordForm.confirmPassword.trim()
}));

const isEmailValid = computed(() => {
  const email = normalizedProfileForm.value.email;
  return email.length > 0 && emailPattern.test(email);
});

const isProfileDirty = computed(() => {
  if (!profile.value) return false;
  const currentName = profile.value.fullName ?? "";
  const currentEmail = profile.value.email ?? "";
  return (
    normalizedProfileForm.value.fullName !== currentName ||
    normalizedProfileForm.value.email.toLowerCase() !== currentEmail.toLowerCase()
  );
});

const canSubmitProfile = computed(
  () =>
    !!profile.value &&
    !savingProfile.value &&
    normalizedProfileForm.value.fullName.length > 0 &&
    isEmailValid.value &&
    isProfileDirty.value
);

const passwordMeetsRequirements = computed(() => normalizedPasswordForm.value.next.length >= 8);

const passwordsMatch = computed(
  () =>
    normalizedPasswordForm.value.next.length === 0 && normalizedPasswordForm.value.confirm.length === 0
      ? true
      : normalizedPasswordForm.value.next === normalizedPasswordForm.value.confirm
);

const passwordFormIsDirty = computed(() =>
  normalizedPasswordForm.value.current.length > 0 ||
  normalizedPasswordForm.value.next.length > 0 ||
  normalizedPasswordForm.value.confirm.length > 0
);

const canSubmitPassword = computed(() =>
  !savingPassword.value &&
  normalizedPasswordForm.value.current.length > 0 &&
  passwordMeetsRequirements.value &&
  passwordsMatch.value &&
  normalizedPasswordForm.value.current !== normalizedPasswordForm.value.next
);

const roleBadge = computed(() => {
  switch (profile.value?.role) {
    case "ADMIN":
      return { label: t('settings.roles.admin'), color: "violet" };
    case "RECEPTIONIST":
      return { label: t('settings.roles.receptionist'), color: "sky" };
    case "DOCTOR":
      return { label: t('settings.roles.doctor'), color: "emerald" };
    default:
      return { label: t('settings.roles.staff'), color: "gray" };
  }
});

const overviewMetrics = computed(() => [
  {
    label: t('settings.metrics.email.label'),
    value: profile.value?.email ?? t('settings.metrics.email.notSet'),
    description: t('settings.metrics.email.description')
  },
  {
    label: t('settings.metrics.role.label'),
    value: roleBadge.value.label,
    description: t('settings.metrics.role.description')
  },
  {
    label: t('settings.metrics.staffId.label'),
    value: profile.value ? `#${profile.value.id}` : "—",
    description: t('settings.metrics.staffId.description')
  }
]);

const lastLogin = computed(() => {
  if (!profile.value?.lastLoginAt) return t('settings.sidebar.activity.notAvailable');
  // Use clinic timezone for last login timestamp
  const { timezone, abbreviation } = useClinicTimezone();
  return formatDateTimeInClinicTimezone(profile.value.lastLoginAt, timezone.value, abbreviation.value);
});

const passwordUpdated = computed(() => {
  if (!profile.value?.passwordUpdatedAt) return t('settings.sidebar.activity.notAvailable');
  // Use clinic timezone for password update timestamp
  const { timezone, abbreviation } = useClinicTimezone();
  return formatDateTimeInClinicTimezone(profile.value.passwordUpdatedAt, timezone.value, abbreviation.value);
});

const pollingIntervalOptions = computed(() => [
  { label: t('settings.notifications.interval.options.10s'), value: 10 },
  { label: t('settings.notifications.interval.options.20s'), value: 20 },
  { label: t('settings.notifications.interval.options.30s'), value: 30 },
  { label: t('settings.notifications.interval.options.60s'), value: 60 },
  { label: t('settings.notifications.interval.options.2m'), value: 120 }
]);

const getMetricIcon = (label: string) => {
  if (label === t('settings.metrics.email.label')) {
    return "i-lucide-mail";
  } else if (label === t('settings.metrics.role.label')) {
    return "i-lucide-shield";
  } else if (label === t('settings.metrics.staffId.label')) {
    return "i-lucide-hash";
  } else {
    return "i-lucide-info";
  }
};

const updatePreference = (key: string, value: any) => {
  savePreferences({ [key]: value });
  toast.add({
    title: t('settings.toasts.preferenceUpdated.title'),
    description: t('settings.toasts.preferenceUpdated.description'),
    color: "green",
    timeout: 3000
  });
};

const requestBrowserPermission = async () => {
  const granted = await requestBrowserNotificationPermission();
  if (granted) {
    toast.add({
      title: t('settings.toasts.notificationsEnabled.title'),
      description: t('settings.toasts.notificationsEnabled.description'),
      color: "green"
    });
  } else {
    toast.add({
      title: t('settings.toasts.permissionDenied.title'),
      description: t('settings.toasts.permissionDenied.description'),
      color: "red"
    });
  }
};

const resetPasswordForm = () => {
  passwordForm.currentPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
  passwordError.value = null;
};

const savePassword = async () => {
  const { current, next, confirm } = normalizedPasswordForm.value;

  if (!current || !next || !confirm) {
    passwordError.value = t('settings.errors.password.completeAllFields');
    return;
  }

  if (!passwordMeetsRequirements.value) {
    passwordError.value = t('settings.errors.password.minimumLength');
    return;
  }

  if (!passwordsMatch.value) {
    passwordError.value = t('settings.errors.password.passwordsDoNotMatch');
    return;
  }

  if (current === next) {
    passwordError.value = t('settings.errors.password.mustBeDifferent');
    return;
  }

  savingPassword.value = true;

  try {
    await request<void>("/auth/profile/password", {
      method: "PUT",
      body: {
        currentPassword: current,
        newPassword: next
      }
    });
    toast.add({
      title: t('settings.toasts.passwordUpdated.title'),
      description: t('settings.toasts.passwordUpdated.description'),
      color: "green"
    });
    resetPasswordForm();
  } catch (error: any) {
    passwordError.value = error?.data?.message || error?.message || t('settings.errors.password.updateFailed');
  } finally {
    savingPassword.value = false;
  }
};

const resetProfileForm = () => {
  if (!profile.value) return;
  profileForm.fullName = profile.value.fullName ?? "";
  profileForm.email = profile.value.email ?? "";
  profileSubmitError.value = null;
};

const saveProfile = async () => {
  if (!canSubmitProfile.value) {
    profileSubmitError.value = t('settings.errors.profile.updateRequired');
    return;
  }

  savingProfile.value = true;
  profileSubmitError.value = null;

  try {
    await request<StaffProfile>("/auth/profile", {
      method: "PUT",
      body: {
        fullName: normalizedProfileForm.value.fullName,
        email: normalizedProfileForm.value.email
      }
    });
    await Promise.all([refresh(), auth.fetchProfile()]);
    toast.add({ title: t('settings.toasts.profileUpdated.title'), color: "green" });
  } catch (error: any) {
    profileSubmitError.value =
      error?.data?.message || error?.message || t('settings.errors.profile.updateFailed');
  } finally {
    savingProfile.value = false;
  }
};
</script>
