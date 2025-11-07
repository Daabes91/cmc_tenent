<!--
  Swipe Gestures Test Page
  Demonstrates various swipe gesture implementations
-->
<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-4xl mx-auto space-y-8">
      <!-- Header -->
      <div class="text-center">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">
          Swipe Gestures Test
        </h1>
        <p class="text-gray-600 dark:text-gray-300">
          Test various swipe gesture implementations on touch devices
        </p>
      </div>

      <!-- Mobile Navigation Test -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
          Mobile Navigation Swipe
        </h2>
        <p class="text-gray-600 dark:text-gray-300 mb-4">
          Test the mobile navigation swipe-to-close functionality. Only visible on mobile devices (≤768px).
        </p>
        <div class="space-y-2">
          <p class="text-sm text-gray-500 dark:text-gray-400">
            Current viewport: {{ viewport.width }}px × {{ viewport.height }}px
          </p>
          <p class="text-sm text-gray-500 dark:text-gray-400">
            Device type: 
            <span v-if="viewport.isMobile" class="text-red-600 font-medium">Mobile</span>
            <span v-else-if="viewport.isTablet" class="text-yellow-600 font-medium">Tablet</span>
            <span v-else class="text-green-600 font-medium">Desktop</span>
          </p>
        </div>
      </div>

      <!-- Swipe to Close Demo -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
          Swipe to Close Demo
        </h2>
        <p class="text-gray-600 dark:text-gray-300 mb-4">
          Swipe left on the card below to close it.
        </p>
        
        <div v-if="showSwipeCard" class="relative">
          <div
            ref="swipeCardRef"
            class="bg-gradient-to-r from-blue-500 to-purple-600 text-white p-6 rounded-lg shadow-lg touch-manipulation"
          >
            <h3 class="text-lg font-semibold mb-2">Swipe Me Left!</h3>
            <p class="text-blue-100">
              This card can be closed by swiping left. Try it on a touch device!
            </p>
            <div class="mt-4 flex items-center gap-2 text-sm text-blue-200">
              <UIcon name="i-lucide-arrow-left" class="w-4 h-4" />
              <span>Swipe left to close</span>
            </div>
          </div>
        </div>
        
        <div v-else class="text-center py-8">
          <p class="text-gray-500 dark:text-gray-400 mb-4">Card was closed by swipe gesture!</p>
          <button
            @click="showSwipeCard = true"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            Show Card Again
          </button>
        </div>
      </div>

      <!-- Swipe Navigation Demo -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
          Swipe Navigation Demo
        </h2>
        <p class="text-gray-600 dark:text-gray-300 mb-4">
          Swipe left/right to navigate between items.
        </p>
        
        <div class="relative">
          <div
            ref="navigationRef"
            class="bg-gradient-to-r from-green-500 to-teal-600 text-white p-8 rounded-lg shadow-lg touch-manipulation overflow-hidden"
          >
            <div class="text-center">
              <h3 class="text-2xl font-bold mb-2">Item {{ currentItem + 1 }}</h3>
              <p class="text-green-100 mb-4">
                {{ navigationItems[currentItem] }}
              </p>
              <div class="flex items-center justify-center gap-4 text-sm text-green-200">
                <div class="flex items-center gap-1">
                  <UIcon name="i-lucide-arrow-left" class="w-4 h-4" />
                  <span>Previous</span>
                </div>
                <div class="w-px h-4 bg-green-300"></div>
                <div class="flex items-center gap-1">
                  <span>Next</span>
                  <UIcon name="i-lucide-arrow-right" class="w-4 h-4" />
                </div>
              </div>
            </div>
          </div>
          
          <!-- Navigation dots -->
          <div class="flex justify-center mt-4 gap-2">
            <button
              v-for="(item, index) in navigationItems"
              :key="index"
              @click="currentItem = index"
              :class="[
                'w-3 h-3 rounded-full transition-colors',
                index === currentItem 
                  ? 'bg-green-600' 
                  : 'bg-gray-300 dark:bg-gray-600 hover:bg-gray-400 dark:hover:bg-gray-500'
              ]"
            />
          </div>
        </div>
      </div>

      <!-- Gesture Log -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
          Gesture Log
        </h2>
        <p class="text-gray-600 dark:text-gray-300 mb-4">
          Real-time log of detected gestures.
        </p>
        
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4 max-h-48 overflow-y-auto">
          <div v-if="gestureLog.length === 0" class="text-gray-500 dark:text-gray-400 text-sm">
            No gestures detected yet. Try swiping on the demos above.
          </div>
          <div v-else class="space-y-1">
            <div
              v-for="(log, index) in gestureLog.slice().reverse()"
              :key="index"
              class="text-sm font-mono"
            >
              <span class="text-gray-500 dark:text-gray-400">{{ log.time }}</span>
              <span class="ml-2 font-medium" :class="getLogColor(log.type)">{{ log.message }}</span>
            </div>
          </div>
        </div>
        
        <button
          @click="gestureLog = []"
          class="mt-4 px-3 py-1 text-sm bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-gray-300 rounded hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
        >
          Clear Log
        </button>
      </div>

      <!-- Instructions -->
      <div class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-6">
        <h2 class="text-lg font-semibold text-blue-900 dark:text-blue-100 mb-3">
          Testing Instructions
        </h2>
        <ul class="text-blue-800 dark:text-blue-200 text-sm space-y-2">
          <li>• <strong>Mobile Navigation:</strong> Open hamburger menu and swipe left to close</li>
          <li>• <strong>Swipe to Close:</strong> Swipe left on the blue card to dismiss it</li>
          <li>• <strong>Swipe Navigation:</strong> Swipe left/right on the green card to navigate</li>
          <li>• <strong>Best Experience:</strong> Test on actual touch devices (phones/tablets)</li>
          <li>• <strong>Browser Testing:</strong> Use Chrome DevTools device simulation</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface GestureLogEntry {
  time: string;
  type: 'swipe' | 'navigation' | 'close';
  message: string;
}

// Use viewport composable
const { viewport } = useViewport();

// Swipe to close demo
const showSwipeCard = ref(true);
const swipeCardRef = ref<HTMLElement>();

// Swipe navigation demo
const navigationRef = ref<HTMLElement>();
const currentItem = ref(0);
const navigationItems = [
  'First item - swipe to navigate',
  'Second item - keep swiping',
  'Third item - almost there',
  'Fourth item - one more',
  'Fifth item - back to start'
];

// Gesture logging
const gestureLog = ref<GestureLogEntry[]>([]);

const addToLog = (type: GestureLogEntry['type'], message: string) => {
  const time = new Date().toLocaleTimeString();
  gestureLog.value.push({ time, type, message });
  
  // Keep only last 20 entries
  if (gestureLog.value.length > 20) {
    gestureLog.value = gestureLog.value.slice(-20);
  }
};

const getLogColor = (type: GestureLogEntry['type']) => {
  switch (type) {
    case 'swipe': return 'text-blue-600 dark:text-blue-400';
    case 'navigation': return 'text-green-600 dark:text-green-400';
    case 'close': return 'text-red-600 dark:text-red-400';
    default: return 'text-gray-600 dark:text-gray-400';
  }
};

// Set up swipe to close for the demo card
useSwipeToClose(swipeCardRef, () => {
  showSwipeCard.value = false;
  addToLog('close', 'Card closed by left swipe');
}, {
  threshold: 60,
  direction: 'left'
});

// Set up swipe navigation for the navigation demo
useSwipeNavigation(
  navigationRef,
  () => {
    // Next item (swipe left)
    const nextIndex = (currentItem.value + 1) % navigationItems.length;
    currentItem.value = nextIndex;
    addToLog('navigation', `Navigated to item ${nextIndex + 1} (swipe left)`);
  },
  () => {
    // Previous item (swipe right)
    const prevIndex = currentItem.value === 0 ? navigationItems.length - 1 : currentItem.value - 1;
    currentItem.value = prevIndex;
    addToLog('navigation', `Navigated to item ${prevIndex + 1} (swipe right)`);
  },
  {
    threshold: 50,
    velocity: 0.2
  }
);

// Set page title
useHead({
  title: 'Swipe Gestures Test'
});
</script>

<style scoped>
.touch-manipulation {
  touch-action: manipulation;
}
</style>