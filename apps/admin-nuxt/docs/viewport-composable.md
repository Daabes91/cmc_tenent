# Viewport Detection Composable

The `useViewport` composable provides reactive viewport state and device detection utilities for building responsive interfaces in the admin-nuxt application.

## Features

- **Reactive viewport state** - Automatically updates when window size changes
- **Device detection** - Mobile, tablet, and desktop breakpoints
- **Touch device detection** - Identifies touch-capable devices
- **Orientation detection** - Portrait vs landscape orientation
- **Media query utilities** - Predefined and custom media queries
- **Touch target validation** - Ensures accessibility compliance
- **Responsive spacing** - Device-appropriate spacing values

## Basic Usage

```vue
<template>
  <div>
    <!-- Conditional rendering based on device type -->
    <div v-if="viewport.isMobile" class="mobile-layout">
      Mobile content
    </div>
    <div v-else-if="viewport.isTablet" class="tablet-layout">
      Tablet content
    </div>
    <div v-else class="desktop-layout">
      Desktop content
    </div>
    
    <!-- Touch-optimized button -->
    <button 
      :class="[
        'px-4 py-2 bg-blue-500 text-white rounded',
        isTouchDevice ? 'min-h-[44px] min-w-[44px]' : ''
      ]"
    >
      Click me
    </button>
  </div>
</template>

<script setup>
const { viewport, isTouchDevice } = useViewport();
</script>
```

## API Reference

### Viewport State

```typescript
interface ViewportState {
  width: number;           // Current viewport width in pixels
  height: number;          // Current viewport height in pixels
  isMobile: boolean;       // True if width ≤ 768px
  isTablet: boolean;       // True if width 769px - 1024px
  isDesktop: boolean;      // True if width > 1024px
  orientation: 'portrait' | 'landscape';
}
```

### Computed Properties

- `isMobileOrTablet` - True for mobile or tablet devices
- `isTabletOrDesktop` - True for tablet or desktop devices
- `isLandscape` - True when in landscape orientation
- `isPortrait` - True when in portrait orientation
- `isTouchDevice` - True for touch-capable devices

### Utility Functions

#### `getResponsiveSpacing()`
Returns appropriate spacing value based on current device:
- Mobile: 16px
- Tablet: 24px
- Desktop: 32px

#### `getTouchTargetSize(variant?)`
Returns touch target size:
- `'minimum'` (default): 44px
- `'recommended'`: 48px

#### `getResponsiveColumns(mobile?, tablet?, desktop?)`
Returns appropriate number of columns for current device.

#### `isTouchTargetValid(size)`
Validates if a size meets minimum touch target requirements (44px).

### Media Query Utilities

#### `matchesMediaQuery(query)`
Tests if a media query matches the current viewport.

#### Predefined Media Queries
```typescript
const mediaQueries = {
  mobile: '(max-width: 768px)',
  tablet: '(min-width: 769px) and (max-width: 1024px)',
  desktop: '(min-width: 1025px)',
  landscape: '(orientation: landscape)',
  portrait: '(orientation: portrait)',
  prefersReducedMotion: '(prefers-reduced-motion: reduce)',
  prefersDarkMode: '(prefers-color-scheme: dark)'
}
```

## Responsive Design Patterns

### 1. Conditional Component Rendering

```vue
<template>
  <!-- Mobile: Card layout -->
  <div v-if="viewport.isMobile" class="space-y-4">
    <div v-for="item in items" :key="item.id" class="card">
      {{ item.title }}
    </div>
  </div>
  
  <!-- Desktop: Table layout -->
  <table v-else class="w-full">
    <tr v-for="item in items" :key="item.id">
      <td>{{ item.title }}</td>
    </tr>
  </table>
</template>
```

### 2. Responsive Grid Columns

```vue
<template>
  <div :class="gridClasses">
    <div v-for="item in items" :key="item.id">
      {{ item.title }}
    </div>
  </div>
</template>

<script setup>
const { getResponsiveColumns } = useViewport();

const gridClasses = computed(() => {
  const cols = getResponsiveColumns(1, 2, 4); // mobile: 1, tablet: 2, desktop: 4
  return `grid grid-cols-${cols} gap-4`;
});
</script>
```

### 3. Touch-Optimized Interactions

```vue
<template>
  <button 
    :class="buttonClasses"
    @click="handleClick"
  >
    {{ label }}
  </button>
</template>

<script setup>
const { isTouchDevice, getTouchTargetSize } = useViewport();

const buttonClasses = computed(() => [
  'px-4 py-2 bg-blue-500 text-white rounded transition-colors',
  isTouchDevice.value ? 'hover:bg-blue-600 active:bg-blue-700' : 'hover:bg-blue-600',
  `min-h-[${getTouchTargetSize()}px]`,
  `min-w-[${getTouchTargetSize()}px]`
]);
</script>
```

### 4. Responsive Spacing

```vue
<template>
  <div :style="{ padding: `${spacing}px` }">
    Content with responsive padding
  </div>
</template>

<script setup>
const { getResponsiveSpacing } = useViewport();

const spacing = computed(() => getResponsiveSpacing());
</script>
```

## Configuration

The composable uses the following breakpoints (matching design specifications):

```typescript
const config = {
  breakpoints: {
    mobile: 768,    // Mobile devices ≤ 768px
    tablet: 1024,   // Tablet devices 769px - 1024px
    desktop: 1025   // Desktop devices > 1024px
  },
  touchTargetSize: {
    minimum: 44,      // Minimum 44px touch target
    recommended: 48   // Recommended 48px touch target
  },
  spacing: {
    mobile: 16,       // 16px spacing on mobile
    tablet: 24,       // 24px spacing on tablet
    desktop: 32       // 32px spacing on desktop
  }
};
```

## Testing

To test the viewport composable, you can temporarily add the `ResponsiveTest` component to any page:

```vue
<template>
  <div>
    <!-- Your page content -->
    
    <!-- Temporary debug component -->
    <ResponsiveTest v-if="isDev" />
  </div>
</template>

<script setup>
const isDev = process.env.NODE_ENV === 'development';
</script>
```

This will show a debug panel in the bottom-right corner displaying current viewport information and testing touch targets.

## Best Practices

1. **Mobile-First Design**: Start with mobile layout and enhance for larger screens
2. **Touch Targets**: Ensure all interactive elements meet minimum 44px size
3. **Performance**: Use computed properties for responsive calculations
4. **Accessibility**: Consider reduced motion preferences and screen readers
5. **Testing**: Test on actual devices, not just browser dev tools

## Integration with Existing Components

The viewport composable is designed to work seamlessly with existing components. You can gradually adopt it by:

1. Adding device-specific rendering to existing components
2. Updating button and form components with touch-friendly sizes
3. Converting table layouts to card layouts on mobile
4. Adding responsive spacing and typography

This composable provides the foundation for implementing comprehensive mobile responsiveness across the admin-nuxt application.