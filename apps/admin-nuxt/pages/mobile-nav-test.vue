<!--
  Test page for mobile navigation component
  This page demonstrates the mobile navigation functionality using the default layout
-->
<template>
  <div>
    <!-- Main Content (now uses default layout) -->
      <div class="max-w-4xl mx-auto">
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6 mb-6">
          <h1 class="text-2xl font-bold text-gray-900 dark:text-white mb-4">
            Mobile Navigation Test
          </h1>
          <p class="text-gray-600 dark:text-gray-300 mb-6">
            This page demonstrates the mobile navigation component. Resize your browser window or use mobile device tools to test the mobile navigation.
          </p>
          
          <!-- Viewport Info -->
          <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4 mb-6">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-3">
              Viewport Information
            </h2>
            <div class="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span class="text-gray-500 dark:text-gray-400">Width:</span>
                <span class="ml-2 font-medium text-gray-900 dark:text-white">{{ viewport.width }}px</span>
              </div>
              <div>
                <span class="text-gray-500 dark:text-gray-400">Height:</span>
                <span class="ml-2 font-medium text-gray-900 dark:text-white">{{ viewport.height }}px</span>
              </div>
              <div>
                <span class="text-gray-500 dark:text-gray-400">Device:</span>
                <span class="ml-2 font-medium text-gray-900 dark:text-white">
                  <span v-if="viewport.isMobile" class="text-red-600">Mobile</span>
                  <span v-else-if="viewport.isTablet" class="text-yellow-600">Tablet</span>
                  <span v-else class="text-green-600">Desktop</span>
                </span>
              </div>
              <div>
                <span class="text-gray-500 dark:text-gray-400">Orientation:</span>
                <span class="ml-2 font-medium text-gray-900 dark:text-white">{{ viewport.orientation }}</span>
              </div>
            </div>
          </div>
          
          <!-- Test Instructions -->
          <div class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
            <h3 class="text-lg font-semibold text-blue-900 dark:text-blue-100 mb-2">
              Testing Instructions
            </h3>
            <ul class="text-blue-800 dark:text-blue-200 text-sm space-y-1">
              <li>• On mobile devices (≤768px), you should see a hamburger menu in the top-left</li>
              <li>• Tap the hamburger menu to open the mobile navigation sidebar</li>
              <li>• Swipe left on the sidebar to close it</li>
              <li>• The desktop sidebar should be hidden on mobile</li>
              <li>• Test theme toggle and user menu in the mobile header</li>
            </ul>
          </div>
        </div>
        
        <!-- Sample Content -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="i in 6" :key="i" class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">
              Sample Card {{ i }}
            </h3>
            <p class="text-gray-600 dark:text-gray-300">
              This is sample content to demonstrate the layout with mobile navigation.
            </p>
          </div>
        </div>
      </div>
  </div>
</template>

<script setup lang="ts">
// Use viewport composable for testing
const { viewport } = useViewport();

// Set page title
useHead({
  title: 'Mobile Navigation Test'
});
</script>