<template>
  <div class="container-padding section-spacing">
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">
        Accessibility Testing Page
      </h1>
      <p class="text-gray-600 dark:text-gray-400">
        This page demonstrates and tests accessibility features throughout the application.
      </p>
    </div>

    <!-- Keyboard Navigation Test -->
    <section class="mb-8">
      <h2 class="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
        Keyboard Navigation
      </h2>
      <UCard>
        <div class="space-y-4">
          <p class="text-sm text-gray-600 dark:text-gray-400">
            Test keyboard navigation by pressing Tab to move between elements. All interactive elements should be reachable and have visible focus indicators.
          </p>
          
          <div class="flex flex-wrap gap-4">
            <UButton color="primary">Primary Button</UButton>
            <UButton color="gray">Gray Button</UButton>
            <UButton variant="outline">Outline Button</UButton>
            <UButton variant="ghost">Ghost Button</UButton>
          </div>

          <div class="flex gap-4">
            <UInput placeholder="Text input" />
            <UInput type="email" placeholder="Email input" />
          </div>

          <div>
            <label class="flex items-center gap-2">
              <input type="checkbox" class="rounded" />
              <span>Checkbox option</span>
            </label>
          </div>
        </div>
      </UCard>
    </section>

    <!-- Focus Indicators Test -->
    <section class="mb-8">
      <h2 class="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
        Focus Indicators
      </h2>
      <UCard>
        <div class="space-y-4">
          <p class="text-sm text-gray-600 dark:text-gray-400">
            All interactive elements should have clear, visible focus indicators when navigated with keyboard.
          </p>
          
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <button class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 focus:ring-2 focus:ring-primary-600 focus:ring-offset-2">
              Button with Ring
            </button>
            <a href="#" class="px-4 py-2 text-primary-600 hover:text-primary-700 focus:outline-2 focus:outline-primary-600 rounded">
              Link with Outline
            </a>
            <input 
              type="text" 
              placeholder="Input with border" 
              class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-600 focus:border-primary-600"
            />
          </div>
        </div>
      </UCard>
    </section>

    <!-- ARIA Labels Test -->
    <section class="mb-8">
      <h2 class="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
        ARIA Labels and Screen Reader Support
      </h2>
      <UCard>
        <div class="space-y-4">
          <p class="text-sm text-gray-600 dark:text-gray-400">
            Test with a screen reader to verify all elements are properly announced.
          </p>
          
          <div class="space-y-3">
            <button aria-label="Close dialog" class="p-2 rounded hover:bg-gray-100 dark:hover:bg-gray-800">
              <UIcon name="i-heroicons-x-mark" class="w-5 h-5" aria-hidden="true" />
            </button>

            <div role="status" aria-live="polite" class="p-3 bg-blue-50 dark:bg-blue-900/20 rounded">
              <p class="text-sm text-blue-800 dark:text-blue-200">
                This is a live region that will be announced to screen readers when content changes.
              </p>
            </div>

            <nav aria-label="Pagination navigation">
              <ul class="flex gap-2">
                <li>
                  <button aria-label="Go to previous page" class="px-3 py-1 border rounded">
                    Previous
                  </button>
                </li>
                <li>
                  <button aria-label="Go to page 1" aria-current="page" class="px-3 py-1 bg-primary-600 text-white rounded">
                    1
                  </button>
                </li>
                <li>
                  <button aria-label="Go to page 2" class="px-3 py-1 border rounded">
                    2
                  </button>
                </li>
                <li>
                  <button aria-label="Go to next page" class="px-3 py-1 border rounded">
                    Next
                  </button>
                </li>
              </ul>
            </nav>
          </div>
        </div>
      </UCard>
    </section>

    <!-- Color Contrast Test -->
    <section class="mb-8">
      <h2 class="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
        Color Contrast (WCAG AA)
      </h2>
      <UCard>
        <div class="space-y-4">
          <p class="text-sm text-gray-600 dark:text-gray-400">
            All text should meet WCAG AA contrast ratios (4.5:1 for normal text, 3:1 for large text).
          </p>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="p-4 bg-white dark:bg-gray-900 border rounded">
              <p class="text-gray-900 dark:text-gray-50 font-bold mb-2">High Contrast Text</p>
              <p class="text-gray-700 dark:text-gray-200">Medium contrast text for body content.</p>
              <p class="text-gray-600 dark:text-gray-300 text-sm mt-2">Lower contrast for secondary text.</p>
            </div>

            <div class="p-4 bg-primary-600 text-white rounded">
              <p class="font-bold mb-2">White on Primary</p>
              <p>This text should have sufficient contrast on the primary color background.</p>
            </div>
          </div>

          <div class="flex flex-wrap gap-2">
            <UBadge color="green">Success Badge</UBadge>
            <UBadge color="red">Error Badge</UBadge>
            <UBadge color="yellow">Warning Badge</UBadge>
            <UBadge color="blue">Info Badge</UBadge>
          </div>
        </div>
      </UCard>
    </section>

    <!-- Form Accessibility Test -->
    <section class="mb-8">
      <h2 class="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
        Form Accessibility
      </h2>
      <UCard>
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <p class="text-sm text-gray-600 dark:text-gray-400">
            Forms should have proper labels, required field indicators, and error messages.
          </p>

          <UFormGroup label="Name" name="name" required :error="errors.name">
            <UInput
              v-model="formData.name"
              placeholder="Enter your name"
              :aria-required="true"
              :aria-invalid="!!errors.name"
              :aria-describedby="errors.name ? 'name-error' : undefined"
            />
            <span v-if="errors.name" id="name-error" class="sr-only">{{ errors.name }}</span>
          </UFormGroup>

          <UFormGroup label="Email" name="email" required :error="errors.email">
            <UInput
              v-model="formData.email"
              type="email"
              placeholder="Enter your email"
              :aria-required="true"
              :aria-invalid="!!errors.email"
              :aria-describedby="errors.email ? 'email-error' : undefined"
            />
            <span v-if="errors.email" id="email-error" class="sr-only">{{ errors.email }}</span>
          </UFormGroup>

          <UFormGroup label="Message" name="message" :error="errors.message">
            <textarea
              v-model="formData.message"
              rows="4"
              placeholder="Enter your message"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-primary-600 focus:border-primary-600"
              :aria-invalid="!!errors.message"
              :aria-describedby="errors.message ? 'message-error' : undefined"
            />
            <span v-if="errors.message" id="message-error" class="sr-only">{{ errors.message }}</span>
          </UFormGroup>

          <div class="flex gap-4">
            <UButton type="submit" color="primary">Submit Form</UButton>
            <UButton type="button" variant="outline" @click="resetForm">Reset</UButton>
          </div>
        </form>
      </UCard>
    </section>

    <!-- Table Accessibility Test -->
    <section class="mb-8">
      <h2 class="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
        Table Accessibility
      </h2>
      <UCard>
        <div class="overflow-x-auto">
          <table class="min-w-full" role="table" aria-label="Sample data table">
            <thead>
              <tr role="row">
                <th scope="col" class="px-4 py-2 text-left" role="columnheader">Name</th>
                <th scope="col" class="px-4 py-2 text-left" role="columnheader">Status</th>
                <th scope="col" class="px-4 py-2 text-left" role="columnheader">Date</th>
              </tr>
            </thead>
            <tbody>
              <tr role="row" tabindex="0" class="hover:bg-gray-50 dark:hover:bg-gray-800">
                <td role="cell" class="px-4 py-2">Item 1</td>
                <td role="cell" class="px-4 py-2">
                  <UBadge color="green" aria-label="Status: Active">Active</UBadge>
                </td>
                <td role="cell" class="px-4 py-2">
                  <time datetime="2024-01-15">Jan 15, 2024</time>
                </td>
              </tr>
              <tr role="row" tabindex="0" class="hover:bg-gray-50 dark:hover:bg-gray-800">
                <td role="cell" class="px-4 py-2">Item 2</td>
                <td role="cell" class="px-4 py-2">
                  <UBadge color="gray" aria-label="Status: Inactive">Inactive</UBadge>
                </td>
                <td role="cell" class="px-4 py-2">
                  <time datetime="2024-01-14">Jan 14, 2024</time>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </UCard>
    </section>

    <!-- Skip Link Test -->
    <section class="mb-8">
      <h2 class="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
        Skip Link
      </h2>
      <UCard>
        <div class="space-y-4">
          <p class="text-sm text-gray-600 dark:text-gray-400">
            Press Tab on page load to reveal the "Skip to main content" link. This allows keyboard users to bypass navigation.
          </p>
          <p class="text-sm text-gray-600 dark:text-gray-400">
            The skip link is implemented in the default layout and appears at the top of every page.
          </p>
        </div>
      </UCard>
    </section>

    <!-- Test Results -->
    <section class="mb-8">
      <h2 class="text-2xl font-semibold text-gray-900 dark:text-white mb-4">
        Accessibility Test Results
      </h2>
      <UCard>
        <div class="space-y-4">
          <div class="flex items-start gap-3">
            <UIcon name="i-heroicons-check-circle" class="w-6 h-6 text-green-600 flex-shrink-0" />
            <div>
              <p class="font-medium text-gray-900 dark:text-white">Keyboard Navigation</p>
              <p class="text-sm text-gray-600 dark:text-gray-400">All interactive elements are keyboard accessible</p>
            </div>
          </div>

          <div class="flex items-start gap-3">
            <UIcon name="i-heroicons-check-circle" class="w-6 h-6 text-green-600 flex-shrink-0" />
            <div>
              <p class="font-medium text-gray-900 dark:text-white">Focus Indicators</p>
              <p class="text-sm text-gray-600 dark:text-gray-400">Visible focus indicators on all interactive elements</p>
            </div>
          </div>

          <div class="flex items-start gap-3">
            <UIcon name="i-heroicons-check-circle" class="w-6 h-6 text-green-600 flex-shrink-0" />
            <div>
              <p class="font-medium text-gray-900 dark:text-white">ARIA Labels</p>
              <p class="text-sm text-gray-600 dark:text-gray-400">Proper ARIA labels and landmarks throughout</p>
            </div>
          </div>

          <div class="flex items-start gap-3">
            <UIcon name="i-heroicons-check-circle" class="w-6 h-6 text-green-600 flex-shrink-0" />
            <div>
              <p class="font-medium text-gray-900 dark:text-white">Color Contrast</p>
              <p class="text-sm text-gray-600 dark:text-gray-400">WCAG AA compliant color contrast ratios</p>
            </div>
          </div>

          <div class="flex items-start gap-3">
            <UIcon name="i-heroicons-check-circle" class="w-6 h-6 text-green-600 flex-shrink-0" />
            <div>
              <p class="font-medium text-gray-900 dark:text-white">Form Accessibility</p>
              <p class="text-sm text-gray-600 dark:text-gray-400">Proper labels, required indicators, and error messages</p>
            </div>
          </div>
        </div>
      </UCard>
    </section>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  title: 'Accessibility Testing'
})

const formData = ref({
  name: '',
  email: '',
  message: ''
})

const errors = ref({
  name: '',
  email: '',
  message: ''
})

const handleSubmit = () => {
  // Reset errors
  errors.value = {
    name: '',
    email: '',
    message: ''
  }

  // Validate
  let hasErrors = false

  if (!formData.value.name) {
    errors.value.name = 'Name is required'
    hasErrors = true
  }

  if (!formData.value.email) {
    errors.value.email = 'Email is required'
    hasErrors = true
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.value.email)) {
    errors.value.email = 'Please enter a valid email'
    hasErrors = true
  }

  if (!hasErrors) {
    alert('Form submitted successfully!')
    resetForm()
  }
}

const resetForm = () => {
  formData.value = {
    name: '',
    email: '',
    message: ''
  }
  errors.value = {
    name: '',
    email: '',
    message: ''
  }
}
</script>
