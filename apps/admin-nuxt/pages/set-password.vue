<template>
  <div class="min-h-screen bg-gradient-to-br from-violet-50 via-purple-50 to-fuchsia-50 flex items-center justify-center p-4">
    <div class="w-full max-w-md">
      <!-- Logo/Header -->
      <div class="text-center mb-8">
        <div class="inline-flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-violet-600 to-purple-600 text-white shadow-lg mb-4">
          <UIcon name="i-lucide-key" class="h-8 w-8" />
        </div>
        <h1 class="text-3xl font-bold text-slate-900">Set Your Password</h1>
        <p class="mt-2 text-slate-600">Complete your account setup to get started</p>
      </div>

      <!-- Success State -->
      <UCard v-if="success" class="shadow-xl">
        <div class="text-center py-8">
          <div class="inline-flex h-16 w-16 items-center justify-center rounded-full bg-green-100 text-green-600 mb-4">
            <UIcon name="i-lucide-check-circle" class="h-8 w-8" />
          </div>
          <h2 class="text-xl font-semibold text-slate-900">Password Set Successfully!</h2>
          <p class="mt-2 text-sm text-slate-600">Your account is now active. You can log in with your credentials.</p>
          <UButton
            class="mt-6"
            size="lg"
            color="violet"
            @click="navigateTo('/login')"
          >
            Go to Login
          </UButton>
        </div>
      </UCard>

      <!-- Form -->
      <UCard v-else class="shadow-xl">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Password -->
          <UFormGroup label="Password" required>
            <UInput
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="Enter a secure password"
              size="lg"
              icon="i-lucide-lock"
            >
              <template #trailing>
                <UButton
                  :icon="showPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                  color="gray"
                  variant="ghost"
                  size="xs"
                  @click="showPassword = !showPassword"
                />
              </template>
            </UInput>
            <template #hint>
              <span class="text-xs text-slate-500">Minimum 8 characters</span>
            </template>
          </UFormGroup>

          <!-- Confirm Password -->
          <UFormGroup label="Confirm Password" required>
            <UInput
              v-model="confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="Re-enter your password"
              size="lg"
              icon="i-lucide-lock"
            >
              <template #trailing>
                <UButton
                  :icon="showConfirmPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                  color="gray"
                  variant="ghost"
                  size="xs"
                  @click="showConfirmPassword = !showConfirmPassword"
                />
              </template>
            </UInput>
          </UFormGroup>

          <!-- Password Requirements -->
          <div class="rounded-lg border border-slate-200 bg-slate-50 p-4">
            <p class="text-sm font-medium text-slate-700 mb-2">Password Requirements:</p>
            <ul class="text-xs text-slate-600 space-y-1">
              <li class="flex items-center gap-2">
                <UIcon
                  :name="form.password.length >= 8 ? 'i-lucide-check-circle' : 'i-lucide-circle'"
                  :class="form.password.length >= 8 ? 'text-green-600' : 'text-slate-400'"
                  class="h-4 w-4"
                />
                At least 8 characters
              </li>
              <li class="flex items-center gap-2">
                <UIcon
                  :name="passwordsMatch ? 'i-lucide-check-circle' : 'i-lucide-circle'"
                  :class="passwordsMatch ? 'text-green-600' : 'text-slate-400'"
                  class="h-4 w-4"
                />
                Passwords match
              </li>
            </ul>
          </div>

          <!-- Error Message -->
          <div v-if="error" class="rounded-lg border border-red-200 bg-red-50 p-4">
            <div class="flex items-start gap-3">
              <UIcon name="i-lucide-alert-circle" class="h-5 w-5 text-red-600 mt-0.5" />
              <div class="flex-1">
                <p class="text-sm font-medium text-red-900">{{ error }}</p>
              </div>
            </div>
          </div>

          <!-- Submit Button -->
          <UButton
            type="submit"
            size="lg"
            color="violet"
            block
            :loading="loading"
            :disabled="!isFormValid"
          >
            Set Password & Activate Account
          </UButton>
        </form>

        <div class="mt-6 text-center">
          <p class="text-xs text-slate-500">
            Having trouble? Contact your administrator for assistance.
          </p>
        </div>
      </UCard>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: false, // No auth layout for this page
  auth: false
});

const route = useRoute();
const staffApi = useStaff();

const form = reactive({
  token: (route.query.token as string) || '',
  password: ''
});

const confirmPassword = ref('');
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const loading = ref(false);
const success = ref(false);
const error = ref('');

const passwordsMatch = computed(() => {
  return confirmPassword.value && form.password === confirmPassword.value;
});

const isFormValid = computed(() => {
  return form.password.length >= 8 && passwordsMatch.value && form.token;
});

// Check if token is missing
onMounted(() => {
  if (!form.token) {
    error.value = 'Invalid or missing invitation token. Please check your email link.';
  }
});

async function handleSubmit() {
  if (!isFormValid.value) {
    error.value = 'Please ensure all requirements are met';
    return;
  }

  loading.value = true;
  error.value = '';

  try {
    await staffApi.setPassword({
      token: form.token,
      password: form.password
    });

    success.value = true;
  } catch (err: any) {
    const errorMessage = err.data?.message || err.message || 'Failed to set password';

    if (errorMessage.includes('expired') || errorMessage.includes('token')) {
      error.value = 'This invitation link has expired or is invalid. Please contact your administrator.';
    } else if (errorMessage.includes('already used')) {
      error.value = 'This invitation has already been used. You can log in with your existing password.';
    } else {
      error.value = errorMessage;
    }
  } finally {
    loading.value = false;
  }
}
</script>
