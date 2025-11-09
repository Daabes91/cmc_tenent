<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-white">
        Responsive Design Test
      </h1>
      <p class="text-sm sm:text-base text-gray-600 dark:text-gray-400 mt-2">
        Test responsive breakpoints and mobile optimization
      </p>
    </div>

    <!-- Current Viewport Info -->
    <UCard>
      <template #header>
        <h2 class="text-lg font-semibold">Current Viewport</h2>
      </template>
      
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        <div class="text-center p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
          <p class="text-sm text-gray-500 dark:text-gray-400">Width</p>
          <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ windowWidth }}px</p>
        </div>
        <div class="text-center p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
          <p class="text-sm text-gray-500 dark:text-gray-400">Height</p>
          <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ windowHeight }}px</p>
        </div>
        <div class="text-center p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
          <p class="text-sm text-gray-500 dark:text-gray-400">Device</p>
          <p class="text-lg font-bold text-gray-900 dark:text-white">
            {{ isMobile ? 'Mobile' : isTablet ? 'Tablet' : 'Desktop' }}
          </p>
        </div>
        <div class="text-center p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
          <p class="text-sm text-gray-500 dark:text-gray-400">Touch</p>
          <p class="text-lg font-bold text-gray-900 dark:text-white">
            {{ isTouchDevice ? 'Yes' : 'No' }}
          </p>
        </div>
      </div>
    </UCard>

    <!-- Breakpoint Indicators -->
    <UCard>
      <template #header>
        <h2 class="text-lg font-semibold">Active Breakpoints</h2>
      </template>
      
      <div class="space-y-2">
        <div class="flex items-center justify-between p-3 rounded-lg" :class="isSmallScreen ? 'bg-green-100 dark:bg-green-900' : 'bg-gray-100 dark:bg-gray-800'">
          <span class="font-medium">Small Screen (&lt; 640px)</span>
          <UBadge :color="isSmallScreen ? 'green' : 'gray'">
            {{ isSmallScreen ? 'Active' : 'Inactive' }}
          </UBadge>
        </div>
        <div class="flex items-center justify-between p-3 rounded-lg" :class="isMobile ? 'bg-green-100 dark:bg-green-900' : 'bg-gray-100 dark:bg-gray-800'">
          <span class="font-medium">Mobile (&lt; 768px)</span>
          <UBadge :color="isMobile ? 'green' : 'gray'">
            {{ isMobile ? 'Active' : 'Inactive' }}
          </UBadge>
        </div>
        <div class="flex items-center justify-between p-3 rounded-lg" :class="isTablet ? 'bg-green-100 dark:bg-green-900' : 'bg-gray-100 dark:bg-gray-800'">
          <span class="font-medium">Tablet (768px - 1024px)</span>
          <UBadge :color="isTablet ? 'green' : 'gray'">
            {{ isTablet ? 'Active' : 'Inactive' }}
          </UBadge>
        </div>
        <div class="flex items-center justify-between p-3 rounded-lg" :class="isDesktop ? 'bg-green-100 dark:bg-green-900' : 'bg-gray-100 dark:bg-gray-800'">
          <span class="font-medium">Desktop (≥ 1024px)</span>
          <UBadge :color="isDesktop ? 'green' : 'gray'">
            {{ isDesktop ? 'Active' : 'Inactive' }}
          </UBadge>
        </div>
        <div class="flex items-center justify-between p-3 rounded-lg" :class="isLargeScreen ? 'bg-green-100 dark:bg-green-900' : 'bg-gray-100 dark:bg-gray-800'">
          <span class="font-medium">Large Screen (≥ 1280px)</span>
          <UBadge :color="isLargeScreen ? 'green' : 'gray'">
            {{ isLargeScreen ? 'Active' : 'Inactive' }}
          </UBadge>
        </div>
      </div>
    </UCard>

    <!-- Responsive Grid Test -->
    <UCard>
      <template #header>
        <h2 class="text-lg font-semibold">Responsive Grid (1/2/4 columns)</h2>
      </template>
      
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div v-for="i in 8" :key="i" class="p-6 bg-primary-100 dark:bg-primary-900 rounded-lg text-center">
          <p class="text-lg font-semibold text-primary-900 dark:text-primary-100">Card {{ i }}</p>
        </div>
      </div>
    </UCard>

    <!-- Touch-Friendly Buttons -->
    <UCard>
      <template #header>
        <h2 class="text-lg font-semibold">Touch-Friendly Buttons (44px min height)</h2>
      </template>
      
      <div class="space-y-3">
        <UButton size="lg" class="w-full sm:w-auto" color="primary">
          Primary Button
        </UButton>
        <UButton size="lg" class="w-full sm:w-auto" color="gray" variant="outline">
          Secondary Button
        </UButton>
        <UButton size="lg" class="w-full sm:w-auto" color="red" variant="ghost">
          Danger Button
        </UButton>
      </div>
    </UCard>

    <!-- Responsive Form -->
    <UCard>
      <template #header>
        <h2 class="text-lg font-semibold">Responsive Form Layout</h2>
      </template>
      
      <div class="space-y-4">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <UFormGroup label="First Name" required>
            <UInput placeholder="Enter first name" size="lg" />
          </UFormGroup>
          <UFormGroup label="Last Name" required>
            <UInput placeholder="Enter last name" size="lg" />
          </UFormGroup>
        </div>
        
        <UFormGroup label="Email" required>
          <UInput type="email" placeholder="Enter email" size="lg" />
        </UFormGroup>
        
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <UFormGroup label="City">
            <UInput placeholder="City" size="lg" />
          </UFormGroup>
          <UFormGroup label="State">
            <UInput placeholder="State" size="lg" />
          </UFormGroup>
          <UFormGroup label="Zip">
            <UInput placeholder="Zip" size="lg" />
          </UFormGroup>
        </div>
        
        <div class="flex flex-col sm:flex-row gap-3 pt-4">
          <UButton size="lg" color="primary" class="w-full sm:w-auto">
            Submit
          </UButton>
          <UButton size="lg" color="gray" variant="outline" class="w-full sm:w-auto">
            Cancel
          </UButton>
        </div>
      </div>
    </UCard>

    <!-- Responsive Table/Card Toggle -->
    <UCard>
      <template #header>
        <h2 class="text-lg font-semibold">Responsive Table (Desktop) / Cards (Mobile)</h2>
      </template>
      
      <!-- Desktop Table -->
      <div class="hidden md:block overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead class="bg-gray-50 dark:bg-gray-800">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-for="i in 3" :key="i">
              <td class="px-6 py-4 whitespace-nowrap">User {{ i }}</td>
              <td class="px-6 py-4 whitespace-nowrap">user{{ i }}@example.com</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <UBadge color="green">Active</UBadge>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <UButton size="sm" variant="ghost">View</UButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Mobile Cards -->
      <div class="md:hidden space-y-3">
        <div v-for="i in 3" :key="i" class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
          <div class="flex justify-between items-start mb-2">
            <div>
              <p class="font-semibold">User {{ i }}</p>
              <p class="text-sm text-gray-500">user{{ i }}@example.com</p>
            </div>
            <UBadge color="green">Active</UBadge>
          </div>
          <UButton size="sm" variant="ghost" class="w-full mt-2">View Details</UButton>
        </div>
      </div>
    </UCard>

    <!-- Spacing Test -->
    <UCard>
      <template #header>
        <h2 class="text-lg font-semibold">Responsive Spacing</h2>
      </template>
      
      <div class="space-y-4">
        <div class="p-4 sm:p-6 lg:p-8 bg-blue-100 dark:bg-blue-900 rounded-lg">
          <p class="text-sm">Padding: p-4 sm:p-6 lg:p-8</p>
        </div>
        <div class="grid grid-cols-2 gap-2 sm:gap-4 lg:gap-6">
          <div class="p-4 bg-green-100 dark:bg-green-900 rounded-lg text-center">
            <p class="text-xs sm:text-sm">Gap: 2/4/6</p>
          </div>
          <div class="p-4 bg-green-100 dark:bg-green-900 rounded-lg text-center">
            <p class="text-xs sm:text-sm">Gap: 2/4/6</p>
          </div>
        </div>
      </div>
    </UCard>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'default'
})

const {
  windowWidth,
  windowHeight,
  isMobile,
  isTablet,
  isDesktop,
  isSmallScreen,
  isLargeScreen,
  isTouchDevice
} = useResponsive()
</script>
