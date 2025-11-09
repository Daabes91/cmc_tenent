<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Header -->
      <div class="text-center">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">
          {{ $t('login.title') }}
        </h1>
        <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">
          {{ $t('login.subtitle') }}
        </p>
      </div>

      <!-- Login Form -->
      <UCard role="form" aria-labelledby="login-title">
        <form 
          @submit.prevent="handleSubmit" 
          class="space-y-6"
          novalidate
          aria-describedby="login-description"
        >
          <span id="login-title" class="sr-only">{{ $t('login.title') }}</span>
          <span id="login-description" class="sr-only">{{ $t('login.formDescription') }}</span>
          <!-- Error Message -->
          <UAlert
            v-if="errorMessage"
            color="red"
            variant="soft"
            :title="$t('login.error')"
            :description="errorMessage"
            :close-button="{ icon: 'i-heroicons-x-mark-20-solid', color: 'red', variant: 'link' }"
            @close="errorMessage = ''"
          />

          <!-- Email Field -->
          <UFormGroup
            :label="$t('login.email')"
            name="email"
            required
            :error="emailError"
          >
            <UInput
              v-model="email"
              type="email"
              :placeholder="$t('login.emailPlaceholder')"
              size="lg"
              icon="i-heroicons-envelope"
              :disabled="loading"
              :aria-required="true"
              :aria-invalid="!!emailError"
              :aria-describedby="emailError ? 'email-error' : undefined"
              autocomplete="email"
              @blur="validateEmail"
            />
            <span v-if="emailError" id="email-error" class="sr-only">{{ emailError }}</span>
          </UFormGroup>

          <!-- Password Field -->
          <UFormGroup
            :label="$t('login.password')"
            name="password"
            required
            :error="passwordError"
          >
            <UInput
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              :placeholder="$t('login.passwordPlaceholder')"
              size="lg"
              icon="i-heroicons-lock-closed"
              :disabled="loading"
              :aria-required="true"
              :aria-invalid="!!passwordError"
              :aria-describedby="passwordError ? 'password-error' : undefined"
              autocomplete="current-password"
              @blur="validatePassword"
            >
              <template #trailing>
                <UButton
                  :icon="showPassword ? 'i-heroicons-eye-slash' : 'i-heroicons-eye'"
                  color="gray"
                  variant="link"
                  :padded="false"
                  :aria-label="showPassword ? $t('login.hidePassword') : $t('login.showPassword')"
                  @click="showPassword = !showPassword"
                />
              </template>
            </UInput>
            <span v-if="passwordError" id="password-error" class="sr-only">{{ passwordError }}</span>
          </UFormGroup>

          <!-- Submit Button -->
          <UButton
            type="submit"
            color="primary"
            size="lg"
            block
            :loading="loading"
            :disabled="!isFormValid || loading"
            :aria-busy="loading"
            :aria-label="loading ? $t('login.loggingIn') : $t('login.submit')"
          >
            {{ loading ? $t('login.loggingIn') : $t('login.submit') }}
          </UButton>
        </form>
      </UCard>

      <!-- Footer -->
      <div class="text-center text-sm text-gray-600 dark:text-gray-400">
        <p>{{ $t('login.footer') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useSaasAuth } from '#imports'
import { useI18n } from '#imports'
import { definePageMeta } from '#imports'
import { ref, computed } from 'vue'

definePageMeta({
  layout: false
})

const { t } = useI18n()
const { login, logout } = useSaasAuth()
const router = useRouter()

// Force clear any existing auth state when landing on login page
if (import.meta.client) {
  logout()
}

// Form state
const email = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const errorMessage = ref('')

// Validation errors
const emailError = ref('')
const passwordError = ref('')

// Validate email
const validateEmail = () => {
  if (!email.value) {
    emailError.value = t('login.validation.emailRequired')
    return false
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email.value)) {
    emailError.value = t('login.validation.emailInvalid')
    return false
  }
  
  emailError.value = ''
  return true
}

// Validate password
const validatePassword = () => {
  if (!password.value) {
    passwordError.value = t('login.validation.passwordRequired')
    return false
  }
  
  if (password.value.length < 8) {
    passwordError.value = t('login.validation.passwordMinLength')
    return false
  }
  
  passwordError.value = ''
  return true
}

// Check if form is valid
const isFormValid = computed(() => {
  return email.value && password.value && !emailError.value && !passwordError.value
})

// Handle form submission
const handleSubmit = async () => {
  // Validate all fields
  const isEmailValid = validateEmail()
  const isPasswordValid = validatePassword()
  
  if (!isEmailValid || !isPasswordValid) {
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    await login(email.value, password.value)
    
    // Redirect to dashboard on successful login
    await router.push('/')
  } catch (error: any) {
    console.error('Login error:', error)
    errorMessage.value = error.message || t('login.validation.loginFailed')
  } finally {
    loading.value = false
  }
}
</script>
