<template>
  <div class="flex min-h-screen bg-white dark:bg-gray-900 transition-colors">
    <!-- Left Panel - Branding -->
    <div class="hidden w-1/2 bg-primary-gradient lg:flex lg:flex-col lg:justify-between p-12">
      <div>
          <div class="flex items-center gap-3">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-white shadow-md shadow-slate-900/10">
              <img
                src="https://imagedelivery.net/K88oXEK4nwOFUDLZaSq1vg/6c79054b-5ecc-4a97-be03-441518f70200/public"
                alt="Clinic logo"
                class="h-8 w-8"
                loading="lazy"
              />
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

        <!-- Hero Mockup -->
        <div class="relative rounded-4xl overflow-hidden border border-white/60 shadow-2xl shadow-slate-900/40 bg-gradient-to-br from-white/10 via-white/5 to-white/0 backdrop-blur-xl p-4">
          <div class="absolute inset-0 pointer-events-none bg-[radial-gradient(circle_at_20%_20%,rgba(255,255,255,0.18),transparent_35%),radial-gradient(circle_at_80%_0%,rgba(59,130,246,0.2),transparent_25%),radial-gradient(circle_at_20%_80%,rgba(94,234,212,0.18),transparent_30%)]"></div>
          <div class="relative rounded-3xl overflow-hidden ring-1 ring-white/20">
            <img
              src="https://imagedelivery.net/K88oXEK4nwOFUDLZaSq1vg/11137b81-cbbd-4b24-daac-4b1ea5499a00/public?auto=format&fit=crop&w=1800&q=80"
              alt="Dashboard preview"
              class="w-full h-full object-cover"
              loading="lazy"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-slate-900/65 via-slate-900/35 to-transparent"></div>
            <div class="absolute bottom-0 left-0 right-0 p-6 space-y-3 text-white">
              <div class="flex items-center gap-2 text-xs font-semibold tracking-wide uppercase text-blue-100">
                <span class="h-2 w-2 rounded-full bg-emerald-400 animate-pulse"></span>
                {{ t('auth.login.branding.tagline') }}
              </div>
              <p class="text-2xl font-bold leading-tight">Data-rich overview, crafted for clinic teams.</p>
              <div class="grid grid-cols-3 gap-3 text-sm">
                <div class="rounded-2xl bg-white/10 px-3 py-2">
                  <p class="text-blue-100 text-xs">{{ t('auth.login.branding.metrics.sessions') }}</p>
                  <p class="text-lg font-semibold">+18%</p>
                </div>
                <div class="rounded-2xl bg-white/10 px-3 py-2">
                  <p class="text-blue-100 text-xs">{{ t('auth.login.branding.metrics.waitTime') }}</p>
                  <p class="text-lg font-semibold">12m</p>
                </div>
                <div class="rounded-2xl bg-white/10 px-3 py-2">
                  <p class="text-blue-100 text-xs">{{ t('auth.login.branding.metrics.satisfaction') }}</p>
                  <p class="text-lg font-semibold">4.8★</p>
                </div>
              </div>
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
    
    <!-- Right Panel - Login Form -->
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
          <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-xl bg-white shadow-lg shadow-violet-500/30">
            <img
              src="https://imagedelivery.net/K88oXEK4nwOFUDLZaSq1vg/6c79054b-5ecc-4a97-be03-441518f70200/public"
              alt="Clinic logo"
              class="h-9 w-9"
              loading="lazy"
            />
          </div>
          <h1 class="mt-4 text-2xl font-bold text-gray-900 dark:text-white">{{ t('auth.login.mobileHeader.title') }}</h1>
          <p class="mt-2 text-sm text-slate-600 dark:text-gray-400">{{ t('auth.login.mobileHeader.subtitle') }}</p>
        </div>

        <div class="rounded-xl border border-slate-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-8 shadow-lg">
          <div class="space-y-6">
            <div class="hidden lg:block">
              <h2 class="text-2xl font-bold text-gray-900 dark:text-white">{{ t('auth.login.form.title') }}</h2>
              <p class="mt-2 text-sm text-slate-600 dark:text-gray-400">
                {{ t('auth.login.form.subtitle') }}
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

            <form @submit.prevent="onSubmit" class="space-y-5">
              <UFormGroup
                :label="t('auth.login.form.tenantLabel')"
                :hint="t('auth.login.form.tenantHint')"
                name="tenant"
                required
              >
                <div class="relative">
                  <input
                    id="tenant-input"
                    name="tenant"
                    type="text"
                    autocomplete="off"
                    :placeholder="t('auth.login.form.tenantPlaceholder')"
                    class="block w-full transition-all duration-200 border border-slate-300 dark:border-gray-600 focus:border-primary-500 dark:focus:border-primary-400 placeholder:text-slate-400 dark:placeholder:text-gray-500 disabled:bg-slate-50 dark:disabled:bg-gray-800 disabled:cursor-not-allowed form-input rounded-lg h-11 text-base py-2.5 shadow-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white ring-1 ring-inset ring-gray-300 dark:ring-gray-600 focus:ring-2 focus:ring-primary-500 dark:focus:ring-primary-400 pl-16 pr-3.5"
                    v-model="loginTenantSlug"
                  />
                  <span class="absolute inset-y-0 start-0 flex items-center pointer-events-none px-3.5 rounded-s-lg bg-primary-gradient-soft">
                    <span class="flex h-8 w-8 items-center justify-center rounded-full bg-white/90 dark:bg-gray-800/90 shadow-inner">
                      <UIcon name="i-lucide-building" class="h-4 w-4 text-primary-600 dark:text-primary-300" aria-hidden="true" />
                    </span>
                  </span>
                </div>
              </UFormGroup>

              <UFormGroup :label="t('auth.login.form.emailLabel')" name="email" required>
                <div class="relative">
                  <input
                    id="email-input"
                    name="email"
                    type="email"
                    autocomplete="username"
                    :placeholder="t('auth.login.form.emailPlaceholder', { at: '@' })"
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

              <UFormGroup :label="t('auth.login.form.passwordLabel')" name="password" required>
              <div class="relative">
                <input
                  id="password-input"
                  name="password"
                  :type="showPassword ? 'text' : 'password'"
                  autocomplete="current-password"
                  :placeholder="t('auth.login.form.passwordPlaceholder')"
                  class="block w-full transition-all duration-200 border border-slate-300 dark:border-gray-600 focus:border-primary-500 dark:focus:border-primary-400 placeholder:text-slate-400 dark:placeholder:text-gray-500 disabled:bg-slate-50 dark:disabled:bg-gray-800 disabled:cursor-not-allowed form-input rounded-lg h-11 text-base py-2.5 shadow-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white ring-1 ring-inset ring-gray-300 dark:ring-gray-600 focus:ring-2 focus:ring-primary-500 dark:focus:ring-primary-400 pl-16 pr-3.5"
                  v-model="form.password"
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
              </UFormGroup>

              <div class="flex items-center justify-between text-sm">
                <label class="flex items-center gap-2 cursor-pointer">
                  <input type="checkbox" class="rounded border-slate-300 dark:border-gray-600 text-primary-600 dark:text-primary-400 focus:ring-primary-500 dark:focus:ring-primary-400 dark:bg-gray-700" />
                  <span class="text-slate-700 dark:text-gray-300">{{ t('auth.login.rememberMe') }}</span>
                </label>
                <NuxtLink to="/forgot-password" class="font-medium text-primary-600 dark:text-primary-300 hover:text-primary-700 dark:hover:text-primary-200 transition-colors">
                  {{ t('auth.login.forgotPassword') }}
                </NuxtLink>
              </div>

              <UButton
                :loading="pending"
                type="submit"
                color="violet"
                size="lg"
                block
                class="justify-center shadow-md hover:shadow-lg"
              >
                <template #leading>
                  <UIcon name="i-lucide-log-in" class="h-5 w-5" />
                </template>
                {{ t('auth.login.submit') }}
              </UButton>
            </form>
          </div>
        </div>

        <p class="text-center text-sm text-slate-600 dark:text-gray-400">
          {{ t('auth.login.needAccess.text') }}
          <a href="#" class="font-medium text-primary-600 dark:text-primary-300 hover:text-primary-700 dark:hover:text-primary-200 transition-colors">
            {{ t('auth.login.needAccess.cta') }}
          </a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { StaffLoginRequest } from "@/types/auth";

definePageMeta({
  layout: false
});

const { t } = useI18n();
const auth = useAuth();
const router = useRouter();
const route = useRoute();
const colorMode = useColorMode();
const { tenantSlug, setTenantSlug } = useTenantSlug();
const loginTenantSlug = ref<string>(tenantSlug.value);

const tenantFromQuery = computed(() => {
  const value = Array.isArray(route.query.tenant)
    ? route.query.tenant[0]
    : route.query.tenant;
  return typeof value === "string" ? value.trim().toLowerCase() : "";
});

const applyTenantFromQuery = (slug: string) => {
  if (!slug) return;
  setTenantSlug(slug);
  if (import.meta.client) {
    const cleanedQuery = { ...route.query };
    delete cleanedQuery.tenant;
    router.replace({ path: route.path, query: cleanedQuery }).catch(() => {});
  }
};

if (tenantFromQuery.value) {
  applyTenantFromQuery(tenantFromQuery.value);
}

watch(tenantFromQuery, (slug) => {
  if (slug) {
    applyTenantFromQuery(slug);
  }
});

watch(
  () => tenantSlug.value,
  (slug) => {
    if (slug && slug !== loginTenantSlug.value) {
      loginTenantSlug.value = slug;
    }
  },
  { immediate: true }
);

watch(loginTenantSlug, (slug) => {
  if (slug) {
    setTenantSlug(slug);
  }
});

const form = reactive<StaffLoginRequest>({
  email: "",
  password: "",
  twoFactorCode: ""
});

const pending = ref(false);
const errorMessage = ref<string | null>(null);
const showPassword = ref(false);

const onSubmit = async (event?: Event) => {
  console.log('[Login] Form submitted', form);

  if (event) {
    event.preventDefault();
  }

  pending.value = true;
  errorMessage.value = null;

  try {
    console.log('[Login] Calling auth.login with:', form);
    const payload: StaffLoginRequest = {
      email: form.email,
      password: form.password,
      twoFactorCode: form.twoFactorCode?.trim() ? form.twoFactorCode.trim() : undefined
    };
    await auth.login(payload);
    console.log('[Login] Login successful, redirecting...');

    const redirect = Array.isArray(route.query.redirect)
      ? route.query.redirect[0]
      : route.query.redirect;
    await router.push(redirect && redirect !== "/login" ? redirect : "/");
  } catch (error) {
    console.error('[Login] Login failed:', error);
    errorMessage.value = (error as Error).message;
  } finally {
    pending.value = false;
  }
};

const onTwoFactorInput = (event: Event) => {
  const input = event.target as HTMLInputElement;
  const sanitized = input.value.replace(/\D/g, "").slice(0, 6);
  form.twoFactorCode = sanitized;
  input.value = sanitized;
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
