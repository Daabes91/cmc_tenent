<template>
  <div class="flex min-h-screen bg-white dark:bg-gray-900 transition-colors">
    <!-- Left Panel - Branding -->
    <div class="hidden w-1/2 bg-primary-gradient lg:flex lg:flex-col lg:justify-between p-12">
      <div>
          <div class="flex items-center gap-3">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-white/10 backdrop-blur-sm text-2xl">
              🦷
            </div>
            <div class="text-white">
              <p class="text-sm font-medium opacity-90">{{ t('auth.login.branding.welcome') }}</p>
              <p class="text-xl font-bold">{{ t('auth.login.branding.title') }}</p>
            </div>
          </div>
      </div>
      
      <div class="space-y-8">
        <div class="space-y-4">
          <h1 class="text-4xl font-bold leading-tight text-white">
            {{ t('auth.login.branding.headlineLine1') }}<br />{{ t('auth.login.branding.headlineLine2') }}
          </h1>
          <p class="text-lg text-violet-100">
            {{ t('auth.login.branding.description') }}
          </p>
        </div>
        
        <div class="grid gap-4">
          <div class="flex items-start gap-3 rounded-lg bg-white/10 p-4 backdrop-blur-sm">
            <UIcon name="i-lucide-calendar-check" class="h-6 w-6 text-violet-200 mt-0.5" />
            <div class="text-white">
              <p class="font-semibold">{{ t('auth.login.branding.features.scheduling.title') }}</p>
              <p class="text-sm text-violet-100">{{ t('auth.login.branding.features.scheduling.description') }}</p>
            </div>
          </div>
          <div class="flex items-start gap-3 rounded-lg bg-white/10 p-4 backdrop-blur-sm">
            <UIcon name="i-lucide-users" class="h-6 w-6 text-violet-200 mt-0.5" />
            <div class="text-white">
              <p class="font-semibold">{{ t('auth.login.branding.features.records.title') }}</p>
              <p class="text-sm text-violet-100">{{ t('auth.login.branding.features.records.description') }}</p>
            </div>
          </div>
          <div class="flex items-start gap-3 rounded-lg bg-white/10 p-4 backdrop-blur-sm">
            <UIcon name="i-lucide-bar-chart-3" class="h-6 w-6 text-violet-200 mt-0.5" />
            <div class="text-white">
              <p class="font-semibold">{{ t('auth.login.branding.features.analytics.title') }}</p>
              <p class="text-sm text-violet-100">{{ t('auth.login.branding.features.analytics.description') }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <div class="flex items-center gap-4 text-sm text-violet-200">
        <a href="#" class="hover:text-white transition-colors">{{ t('auth.login.branding.links.privacy') }}</a>
        <span>•</span>
        <a href="#" class="hover:text-white transition-colors">{{ t('auth.login.branding.links.terms') }}</a>
        <span>•</span>
        <a href="#" class="hover:text-white transition-colors">{{ t('auth.login.branding.links.support') }}</a>
      </div>
    </div>
    
    <!-- Right Panel - Forgot Password Form -->
    <div class="flex w-full items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100 dark:from-gray-800 dark:to-gray-900 p-8 lg:w-1/2 relative">
      <!-- Theme Toggle Button -->
      <div class="absolute top-4 right-4">
        <UButton
          :icon="colorMode.value === 'dark' ? 'i-lucide-sun' : 'i-lucide-moon'"
          color="gray"
          variant="ghost"
          size="lg"
          @click="toggleColorMode"
          class="rounded-full"
        />
      </div>

      <div class="w-full max-w-md space-y-8">
        <!-- Mobile Header -->
        <div class="text-center lg:hidden">
          <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-xl bg-primary-gradient text-3xl shadow-lg shadow-violet-500/30">
            🦷
          </div>
          <h1 class="mt-4 text-2xl font-bold text-gray-900 dark:text-white">{{ t('auth.forgotPassword.mobileHeader.title') }}</h1>
          <p class="mt-2 text-sm text-slate-600 dark:text-gray-400">{{ t('auth.forgotPassword.mobileHeader.subtitle') }}</p>
        </div>

        <div class="rounded-xl border border-slate-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-8 shadow-lg">
          <div class="space-y-6">
            <div class="hidden lg:block">
              <h2 class="text-2xl font-bold text-gray-900 dark:text-white">{{ t('auth.forgotPassword.form.title') }}</h2>
              <p class="mt-2 text-sm text-slate-600 dark:text-gray-400">
                {{ t('auth.forgotPassword.form.subtitle') }}
              </p>
            </div>

            <UAlert
              v-if="errorMessage !== null"
              color="red"
              variant="soft"
              icon="i-lucide-alert-circle"
              :title="errorMessage"
              class="animate-shake"
            />

            <UAlert
              v-if="submitted"
              color="green"
              variant="soft"
              icon="i-lucide-check-circle"
              :title="t('auth.forgotPassword.success.title')"
              :description="t('auth.forgotPassword.success.description')"
            />

            <form v-if="!submitted" @submit.prevent="onSubmit" class="space-y-5">
              <UFormGroup
                :label="t('auth.forgotPassword.form.tenantLabel')"
                :hint="t('auth.forgotPassword.form.tenantHint')"
                name="tenant"
                required
              >
                <div class="relative">
                  <input
                    id="tenant-input"
                    name="tenant"
                    type="text"
                    autocomplete="off"
                    :placeholder="t('auth.forgotPassword.form.tenantPlaceholder')"
                    class="block w-full transition-all duration-200 border border-slate-300 dark:border-gray-600 focus:border-primary-500 dark:focus:border-primary-400 placeholder:text-slate-400 dark:placeholder:text-gray-500 disabled:bg-slate-50 dark:disabled:bg-gray-800 disabled:cursor-not-allowed form-input rounded-lg h-11 text-base py-2.5 shadow-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white ring-1 ring-inset ring-gray-300 dark:ring-gray-600 focus:ring-2 focus:ring-primary-500 dark:focus:ring-primary-400 pl-16 pr-3.5"
                    v-model="form.tenantSlug"
                  />
                  <span class="absolute inset-y-0 start-0 flex items-center pointer-events-none px-3.5 rounded-s-lg bg-primary-gradient-soft">
                    <span class="flex h-8 w-8 items-center justify-center rounded-full bg-white/90 dark:bg-gray-800/90 shadow-inner">
                      <UIcon name="i-lucide-building" class="h-4 w-4 text-primary-600 dark:text-primary-300" aria-hidden="true" />
                    </span>
                  </span>
                </div>
              </UFormGroup>

              <UFormGroup :label="t('auth.forgotPassword.form.emailLabel')" name="email" required>
                <div class="relative">
                  <input
                    id="email-input"
                    name="email"
                    type="email"
                    autocomplete="username"
                    :placeholder="t('auth.forgotPassword.form.emailPlaceholder', { at: '@' })"
                    class="block w-full transition-all duration-200 border border-slate-300 dark:border-gray-600 focus:border-primary-500 dark:focus:border-primary-400 placeholder:text-slate-400 dark:placeholder:text-gray-500 disabled:bg-slate-50 dark:disabled:bg-gray-800 disabled:cursor-not-allowed form-input rounded-lg h-11 text-base py-2.5 shadow-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white ring-1 ring-inset ring-gray-300 dark:ring-gray-600 focus:ring-2 focus:ring-primary-500 dark:focus:ring-primary-400 pl-16 pr-3.5"
                    v-model="form.email"
                  />
                  <span class="absolute inset-y-0 start-0 flex items-center pointer-events-none px-3.5 rounded-s-lg bg-primary-gradient-soft">
                    <span class="flex h-8 w-8 items-center justify-center rounded-full bg-white/90 dark:bg-gray-800/90 shadow-inner">
                      <UIcon name="i-lucide-mail" class="h-4 w-4 text-primary-600 dark:text-primary-300" aria-hidden="true" />
                    </span>
                  </span>
                </div>
              </UFormGroup>

              <UButton
                :loading="pending"
                type="submit"
                color="violet"
                size="lg"
                block
                class="justify-center shadow-md hover:shadow-lg"
              >
                <template #leading>
                  <UIcon name="i-lucide-send" class="h-5 w-5" />
                </template>
                {{ t('auth.forgotPassword.submit') }}
              </UButton>
            </form>

            <div class="text-center">
              <NuxtLink to="/login" class="text-sm font-medium text-primary-600 dark:text-primary-300 hover:text-primary-700 dark:hover:text-primary-200 transition-colors inline-flex items-center gap-2">
                <UIcon name="i-lucide-arrow-left" class="h-4 w-4" />
                {{ t('auth.forgotPassword.backToLogin') }}
              </NuxtLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: false,
  auth: false
});

const { t } = useI18n();
const colorMode = useColorMode();
const { tenantSlug } = useTenantSlug();

const form = reactive({
  email: "",
  tenantSlug: tenantSlug.value || ""
});

const pending = ref(false);
const submitted = ref(false);
const errorMessage = ref<string | null>(null);

const { requestReset } = usePasswordReset();

const onSubmit = async (event?: Event) => {
  if (event) {
    event.preventDefault();
  }

  // Validate form
  if (!form.email.trim() || !form.tenantSlug.trim()) {
    errorMessage.value = t('auth.forgotPassword.errors.emptyFields');
    return;
  }

  pending.value = true;
  errorMessage.value = null;

  try {
    await requestReset(form.email, form.tenantSlug);
    submitted.value = true;
  } catch (error) {
    console.error('[ForgotPassword] Request failed:', error);
    errorMessage.value = (error as Error).message;
  } finally {
    pending.value = false;
  }
};

const toggleColorMode = () => {
  colorMode.preference = colorMode.value === 'dark' ? 'light' : 'dark';
};
</script>

<style scoped>
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.animate-shake {
  animation: shake 0.4s ease-in-out;
}
</style>
