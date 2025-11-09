<template>
  <div class="max-w-4xl mx-auto space-y-6">
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <h1 class="text-2xl font-bold mb-4">Security Features Test</h1>
      <p class="text-gray-600 dark:text-gray-400 mb-6">
        This page tests the security features implementation.
      </p>
    </div>

    <!-- XSS Prevention Test -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <h2 class="text-xl font-semibold mb-4">XSS Prevention</h2>
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium mb-2">Test Input (try entering script tags)</label>
          <UInput
            v-model="xssTestInput"
            placeholder="Enter malicious code to test"
            class="mb-2"
          />
        </div>
        <UButton @click="testXssPrevention">Test Sanitization</UButton>
        <div v-if="xssResult" class="mt-4 p-4 bg-gray-50 dark:bg-gray-900 rounded">
          <p class="text-sm"><strong>Original:</strong> {{ xssTestInput }}</p>
          <p class="text-sm"><strong>Sanitized:</strong> {{ xssResult }}</p>
        </div>
      </div>
    </div>

    <!-- Rate Limiting Test -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <h2 class="text-xl font-semibold mb-4">Rate Limiting</h2>
      <div class="space-y-4">
        <p class="text-sm text-gray-600 dark:text-gray-400">
          Click the button rapidly to test rate limiting (100 requests per minute)
        </p>
        <div class="flex gap-4 items-center">
          <UButton @click="testRateLimit">Make Request</UButton>
          <span class="text-sm">Requests made: {{ requestCount }}</span>
        </div>
        <div v-if="rateLimitHit" class="p-4 bg-amber-50 dark:bg-amber-900/20 rounded">
          <p class="text-sm text-amber-800 dark:text-amber-200">
            ✓ Rate limit working! Request blocked after {{ requestCount }} requests.
          </p>
        </div>
      </div>
    </div>

    <!-- Session Timeout Test -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <h2 class="text-xl font-semibold mb-4">Session Timeout</h2>
      <div class="space-y-4">
        <p class="text-sm text-gray-600 dark:text-gray-400">
          Session timeout is set to 30 minutes with a 2-minute warning.
        </p>
        <div class="grid grid-cols-2 gap-4">
          <div class="p-4 bg-gray-50 dark:bg-gray-900 rounded">
            <p class="text-sm font-medium">Timeout Duration</p>
            <p class="text-2xl font-bold">30 minutes</p>
          </div>
          <div class="p-4 bg-gray-50 dark:bg-gray-900 rounded">
            <p class="text-sm font-medium">Warning Before</p>
            <p class="text-2xl font-bold">2 minutes</p>
          </div>
        </div>
        <UButton @click="triggerSessionWarning">Trigger Warning (Test)</UButton>
      </div>
    </div>

    <!-- Secure Logging Test -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <h2 class="text-xl font-semibold mb-4">Secure Logging</h2>
      <div class="space-y-4">
        <p class="text-sm text-gray-600 dark:text-gray-400">
          Sensitive data is automatically redacted in logs. Check the browser console.
        </p>
        <UButton @click="testSecureLogging">Test Secure Logging</UButton>
        <div v-if="logTestDone" class="p-4 bg-green-50 dark:bg-green-900/20 rounded">
          <p class="text-sm text-green-800 dark:text-green-200">
            ✓ Check browser console - sensitive fields should show [REDACTED]
          </p>
        </div>
      </div>
    </div>

    <!-- Token Refresh Test -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <h2 class="text-xl font-semibold mb-4">Token Refresh</h2>
      <div class="space-y-4">
        <p class="text-sm text-gray-600 dark:text-gray-400">
          Tokens are automatically refreshed when less than 5 minutes remain until expiry.
        </p>
        <div class="p-4 bg-gray-50 dark:bg-gray-900 rounded">
          <p class="text-sm"><strong>Refresh Threshold:</strong> 5 minutes before expiry</p>
          <p class="text-sm"><strong>Check Interval:</strong> Every 60 seconds</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const xssTestInput = ref('<scr' + 'ipt>alert("xss")</scr' + 'ipt>')
const xssResult = ref('')
const requestCount = ref(0)
const rateLimitHit = ref(false)
const logTestDone = ref(false)

const { sanitizeInput, checkRateLimit, secureLog } = useSecurity()

const testXssPrevention = () => {
  xssResult.value = sanitizeInput(xssTestInput.value)
}

const testRateLimit = () => {
  const allowed = checkRateLimit('/test-endpoint')
  if (allowed) {
    requestCount.value++
  } else {
    rateLimitHit.value = true
  }
}

const triggerSessionWarning = () => {
  const { showTimeoutWarning } = useSecurity()
  showTimeoutWarning.value = true
}

const testSecureLogging = () => {
  secureLog('Test log with sensitive data', {
    username: 'testuser',
    password: 'secret123',
    token: 'jwt-token-here',
    email: 'user@example.com',
    apiKey: 'api-key-secret'
  })
  logTestDone.value = true
}

definePageMeta({
  layout: 'default'
})
</script>
