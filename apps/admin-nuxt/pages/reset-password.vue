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
    
    <!-- Right Panel - Reset Password Form -->
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
          <h1 class="mt-4 text-2xl font-bold text-gray-900 dark:text-white">{{ t('auth.resetPassword.mobileHeader.title') }}</h1>
          <p class="mt-2 text-sm text-slate-600 dark:text-gray-400">{{ t('auth.resetPassword.mobileHeader.subtitle') }}</p>
        </div>

        <div class="rounded-xl border border-slate-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-8 shadow-lg">
          <div class="space-y-6">
            <div class="hidden lg:block">
              <h2 class="text-2xl font-bold text-gray-900 dark:text-white">{{ t('auth.resetPassword.form.title') }}</h2>
              <p class="mt-2 text-sm text-slate-600 dark:text-gray-400">
                {{ t('auth.resetPassword.form.subtitle') }}
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
              v-if="successMessage !== null"
              color="green"
              variant="soft"
              icon="i-lucide-check-circle"
              :title="successMessage"
              :description="t('auth.resetPassword.success.redirecting')"
            />

            <form v-if="!successMessage && tokenValid" @submit.prevent="onSubmit" class="space-y-5">
              <UFormGroup :label="t('auth.resetPassword.form.passwordLabel')" name="newPassword" required>
                <div class="relative">
                  <input
                    id="password-input"
                    name="newPassword"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="new-password"
                    :placeholder="t('auth.resetPassword.form.passwordPlaceholder')"
                    class="block w-full transition-all duration-200 border border-slate-300 dark:border-gray-600 focus:border-primary-500 dark:focus:border-primary-400 placeholder:text-slate-400 dark:placeholder:text-gray-500 disabled:bg-slate-50 dark:disabled:bg-gray-800 disabled:cursor-not-allowed form-input rounded-lg h-11 text-base py-2.5 shadow-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white ring-1 ring-inset ring-gray-300 dark:ring-gray-600 focus:ring-2 focus:ring-primary-500 dark:focus:ring-primary-400 pl-16 pr-12"
                    v-model="form.newPassword"
                  />
                  <span class="absolute inset-y-0 start-0 flex items-center pointer-events-none px-3.5 rounded-s-lg bg-primary-gradient-soft">
                    <span class="flex h-8 w-8 items-center justify-center rounded-full bg-white/90 dark:bg-gray-800/90 shadow-inner">
                      <UIcon name="i-lucide-lock" class="h-4 w-4 text-primary-600 dark:text-primary-300" aria-hidden="true" />
                    </span>
                  </span>
                  <UButton
                    type="button"
                    variant="ghost"
                    size="xs"
                    class="absolute inset-y-1 end-2 flex items-center"
                    @click.prevent="showPassword = !showPassword"
                  >
                    <span
                      class="iconify flex-shrink-0 h-4 w-4"
                      :class="showPassword ? 'i-lucide:eye-off' : 'i-lucide:eye'"
                      aria-hidden="true"
                    ></span>
                  </UButton>
                </div>
                <p class="mt-1 text-xs text-slate-500 dark:text-gray-400">{{ t('auth.resetPassword.form.passwordHint') }}</p>
              </UFormGroup>

              <UFormGroup :label="t('auth.resetPassword.form.confirmPasswordLabel')" name="confirmPassword" required>
                <div class="relative">
                  <input
                    id="confirm-password-input"
                    name="confirmPassword"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    autocomplete="new-password"
                    :placeholder="t('auth.resetPassword.form.confirmPasswordPlaceholder')"
                    class="block w-full transition-all duration-200 border border-slate-300 dark:border-gray-600 focus:border-primary-500 dark:focus:border-primary-400 placeholder:text-slate-400 dark:placeholder:text-gray-500 disabled:bg-slate-50 dark:disabled:bg-gray-800 disabled:cursor-not-allowed form-input rounded-lg h-11 text-base py-2.5 shadow-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white ring-1 ring-inset ring-gray-300 dark:ring-gray-600 focus:ring-2 focus:ring-primary-500 dark:focus:ring-primary-400 pl-16 pr-12"
                    v-model="form.confirmPassword"
                  />
                  <span class="absolute inset-y-0 start-0 flex items-center pointer-events-none px-3.5 rounded-s-lg bg-primary-gradient-soft">
                    <span class="flex h-8 w-8 items-center justify-center rounded-full bg-white/90 dark:bg-gray-800/90 shadow-inner">
                      <UIcon name="i-lucide-lock" class="h-4 w-4 text-primary-600 dark:text-primary-300" aria-hidden="true" />
                    </span>
                  </span>
                  <UButton
                    type="button"
                    variant="ghost"
                    size="xs"
                    class="absolute inset-y-1 end-2 flex items-center"
                    @click.prevent="showConfirmPassword = !showConfirmPassword"
                  >
                    <span
                      class="iconify flex-shrink-0 h-4 w-4"
                      :class="showConfirmPassword ? 'i-lucide:eye-off' : 'i-lucide:eye'"
                      aria-hidden="true"
                    ></span>
                  </UButton>
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
                  <UIcon name="i-lucide-check" class="h-5 w-5" />
                </template>
                {{ t('auth.resetPassword.submit') }}
              </UButton>
            </form>

            <div class="text-center">
              <NuxtLink to="/forgot-password" class="text-sm font-medium text-primary-600 dark:text-primary-300 hover:text-primary-700 dark:hover:text-primary-200 transition-colors inline-flex items-center gap-2">
                <UIcon name="i-lucide-refresh-cw" class="h-4 w-4" />
                {{ t('auth.resetPassword.requestNew') }}
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
const route = useRoute();
const router = useRouter();
const colorMode = useColorMode();

const form = reactive({
  token: "",
  newPassword: "",
  confirmPassword: ""
});

const pending = ref(false);
const tokenValid = ref<boolean | null>(null);
const successMessage = ref<string | null>(null);
const errorMessage = ref<string | null>(null);
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const tenantSlugFromToken = ref<string | null>(null);

const { validateToken: validateTokenApi, resetPassword: resetPasswordApi } = usePasswordReset();

// Extract token from URL query parameter on mount
onMounted(async () => {
  const token = Array.isArray(route.query.token)
    ? route.query.token[0]
    : route.query.token;
  
  if (typeof token === "string" && token.trim()) {
    form.token = token.trim();
    await validateToken();
  } else {
    errorMessage.value = t('auth.resetPassword.errors.missingToken');
    tokenValid.value = false;
  }
});

const validateToken = async () => {
  if (!form.token) {
    errorMessage.value = t('auth.resetPassword.errors.missingToken');
    tokenValid.value = false;
    return;
  }

  try {
    const response = await validateTokenApi(form.token);
    tokenValid.value = response.valid;
    if (response.valid && response.tenantSlug) {
      tenantSlugFromToken.value = response.tenantSlug;
      console.log('[ResetPassword] Tenant slug extracted from token:', response.tenantSlug);
    }
    if (!response.valid) {
      errorMessage.value = t('auth.resetPassword.errors.invalidToken');
    }
  } catch (error) {
    console.error('[ResetPassword] Token validation failed:', error);
    errorMessage.value = t('auth.resetPassword.errors.invalidToken');
    tokenValid.value = false;
  }
};

const onSubmit = async (event?: Event) => {
  if (event) {
    event.preventDefault();
  }

  // Validate password length
  if (form.newPassword.length < 8) {
    errorMessage.value = t('auth.resetPassword.errors.passwordTooShort');
    return;
  }

  // Validate password match
  if (form.newPassword !== form.confirmPassword) {
    errorMessage.value = t('auth.resetPassword.errors.passwordMismatch');
    return;
  }

  pending.value = true;
  errorMessage.value = null;

  try {
    await resetPasswordApi(form.token, form.newPassword);
    
    successMessage.value = t('auth.resetPassword.success.title');
    
    // Redirect to login after 3 seconds, preserving tenant slug
    setTimeout(() => {
      if (tenantSlugFromToken.value) {
        router.push(`/login?tenant=${tenantSlugFromToken.value}`);
      } else {
        router.push('/login');
      }
    }, 3000);
  } catch (error) {
    console.error('[ResetPassword] Reset failed:', error);
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
