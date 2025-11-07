<!--
  Test component to verify viewport detection functionality
  This component can be temporarily added to any page to test responsive behavior
-->
<template>
  <div class="fixed bottom-4 right-4 z-50 bg-black/80 text-white p-4 rounded-lg text-sm font-mono max-w-xs">
    <h3 class="font-bold mb-2">Viewport Debug</h3>
    <div class="space-y-1">
      <div>Width: {{ viewport.width }}px</div>
      <div>Height: {{ viewport.height }}px</div>
      <div>Device: 
        <span v-if="viewport.isMobile" class="text-red-400">Mobile</span>
        <span v-else-if="viewport.isTablet" class="text-yellow-400">Tablet</span>
        <span v-else-if="viewport.isDesktop" class="text-green-400">Desktop</span>
      </div>
      <div>Orientation: {{ viewport.orientation }}</div>
      <div>Touch: {{ isTouchDevice ? 'Yes' : 'No' }}</div>
      <div>Spacing: {{ getResponsiveSpacing() }}px</div>
      <div>Touch Target: {{ getTouchTargetSize() }}px</div>
    </div>
    
    <!-- Visual indicators -->
    <div class="mt-3 flex gap-2">
      <div 
        :class="[
          'w-3 h-3 rounded-full',
          viewport.isMobile ? 'bg-red-400' : 'bg-gray-600'
        ]"
        title="Mobile"
      />
      <div 
        :class="[
          'w-3 h-3 rounded-full',
          viewport.isTablet ? 'bg-yellow-400' : 'bg-gray-600'
        ]"
        title="Tablet"
      />
      <div 
        :class="[
          'w-3 h-3 rounded-full',
          viewport.isDesktop ? 'bg-green-400' : 'bg-gray-600'
        ]"
        title="Desktop"
      />
    </div>
    
    <!-- Test touch targets -->
    <div class="mt-3">
      <div class="text-xs mb-1">Touch Target Test:</div>
      <div class="flex gap-1">
        <button 
          :class="[
            'bg-blue-500 rounded text-xs',
            getTouchTargetSize() >= 44 ? 'px-2 py-2' : 'px-1 py-1'
          ]"
          :style="{ 
            minWidth: getTouchTargetSize() + 'px',
            minHeight: getTouchTargetSize() + 'px'
          }"
        >
          OK
        </button>
        <button 
          class="bg-red-500 rounded text-xs px-1 py-1"
          style="min-width: 30px; min-height: 30px;"
        >
          Bad
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const { 
  viewport, 
  isTouchDevice, 
  getResponsiveSpacing, 
  getTouchTargetSize 
} = useViewport();
</script>