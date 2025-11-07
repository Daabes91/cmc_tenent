# Swipe Gestures System

This document describes the comprehensive swipe gesture system implemented for the admin-nuxt application, providing touch-optimized interactions across all components.

## Overview

The swipe gesture system consists of three main components:

1. **`useSwipeGestures`** - Core swipe detection composable
2. **`useSwipeToClose`** - Utility for swipe-to-close patterns
3. **`useSwipeNavigation`** - Utility for swipe navigation patterns
4. **Swipe Actions** - Pre-configured swipe actions for common UI patterns

## Core Composable: `useSwipeGestures`

### Basic Usage

```typescript
const elementRef = ref<HTMLElement>();

useSwipeGestures(elementRef, {
  onSwipeLeft: (event, distance, velocity) => {
    console.log('Swiped left:', distance, 'px');
  },
  onSwipeRight: (event, distance, velocity) => {
    console.log('Swiped right:', distance, 'px');
  },
  onSwipeUp: (event, distance, velocity) => {
    console.log('Swiped up:', distance, 'px');
  },
  onSwipeDown: (event, distance, velocity) => {
    console.log('Swiped down:', distance, 'px');
  }
}, {
  threshold: 50,        // Minimum distance for swipe detection
  velocity: 0.3,        // Minimum velocity for swipe detection
  direction: 'both',    // 'horizontal', 'vertical', or 'both'
  preventScroll: false, // Prevent default scroll behavior
  maxTime: 1000        // Maximum time for swipe gesture
});
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `threshold` | `number` | `50` | Minimum distance in pixels for swipe detection |
| `velocity` | `number` | `0.3` | Minimum velocity for swipe detection |
| `preventScroll` | `boolean` | `false` | Prevent default scroll behavior during swipe |
| `direction` | `'horizontal' \| 'vertical' \| 'both'` | `'both'` | Allowed swipe directions |
| `maxTime` | `number` | `1000` | Maximum time in milliseconds for swipe gesture |

### Callback Events

| Callback | Parameters | Description |
|----------|------------|-------------|
| `onSwipeLeft` | `(event, distance, velocity)` | Triggered on left swipe |
| `onSwipeRight` | `(event, distance, velocity)` | Triggered on right swipe |
| `onSwipeUp` | `(event, distance, velocity)` | Triggered on up swipe |
| `onSwipeDown` | `(event, distance, velocity)` | Triggered on down swipe |
| `onSwipeStart` | `(event)` | Triggered when swipe starts |
| `onSwipeMove` | `(event, deltaX, deltaY)` | Triggered during swipe movement |
| `onSwipeEnd` | `(event)` | Triggered when swipe ends |

## Utility Composables

### `useSwipeToClose`

Simplified composable for swipe-to-close patterns:

```typescript
const cardRef = ref<HTMLElement>();

useSwipeToClose(cardRef, () => {
  // Close/dismiss the element
  showCard.value = false;
}, {
  threshold: 100,
  direction: 'left' // 'left', 'right', 'up', or 'down'
});
```

### `useSwipeNavigation`

Simplified composable for swipe navigation:

```typescript
const containerRef = ref<HTMLElement>();

useSwipeNavigation(
  containerRef,
  () => nextItem(),     // On swipe left (next)
  () => previousItem(), // On swipe right (previous)
  {
    threshold: 50,
    velocity: 0.2
  }
);
```

## Mobile Navigation Integration

The mobile navigation component uses enhanced swipe gestures:

```vue
<template>
  <nav 
    ref="sidebarRef"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove"
    @touchend="handleTouchEnd"
  >
    <!-- Navigation content -->
  </nav>
</template>

<script setup>
const sidebarRef = ref<HTMLElement>();

// Enhanced swipe-to-close with visual feedback
useSwipeToClose(sidebarRef, closeSidebar, {
  threshold: 80,
  direction: 'left'
});

// Custom drag handling for visual feedback
const handleTouchMove = (e: TouchEvent) => {
  // Provide real-time visual feedback during swipe
  const deltaX = e.touches[0].clientX - startX;
  if (deltaX < 0 && sidebarRef.value) {
    sidebarRef.value.style.transform = `translateX(${deltaX}px)`;
  }
};
</script>
```

## Swipe Actions System

Pre-configured swipe actions for common UI patterns:

### Table Row Actions

```typescript
const rowRef = ref<HTMLElement>();

useTableRowSwipeActions(rowRef, item, {
  onEdit: (item) => editItem(item),
  onDelete: (item) => deleteItem(item),
  onView: (item) => viewItem(item),
  onArchive: (item) => archiveItem(item)
});
```

### Card Actions

```typescript
const cardRef = ref<HTMLElement>();

useCardSwipeActions(cardRef, item, {
  onFavorite: (item) => toggleFavorite(item),
  onShare: (item) => shareItem(item),
  onBookmark: (item) => bookmarkItem(item)
});
```

### Custom Swipe Actions

```typescript
const customActions: SwipeAction[] = [
  {
    id: 'approve',
    label: 'Approve',
    icon: 'i-lucide-check',
    color: 'green',
    action: () => approveItem()
  },
  {
    id: 'reject',
    label: 'Reject',
    icon: 'i-lucide-x',
    color: 'red',
    action: () => rejectItem()
  }
];

useSwipeActions(elementRef, customActions, [], {
  threshold: 80,
  showFeedback: true,
  hapticFeedback: true,
  confirmDestructive: true
});
```

## Implementation Examples

### 1. Swipe-to-Delete List Item

```vue
<template>
  <div
    v-for="item in items"
    :key="item.id"
    ref="itemRefs"
    class="bg-white border rounded-lg p-4 touch-manipulation"
  >
    <h3>{{ item.title }}</h3>
    <p>{{ item.description }}</p>
  </div>
</template>

<script setup>
const items = ref([...]);
const itemRefs = ref<HTMLElement[]>([]);

// Set up swipe-to-delete for each item
items.value.forEach((item, index) => {
  useSwipeToClose(
    computed(() => itemRefs.value[index]),
    () => {
      items.value.splice(index, 1);
    },
    { threshold: 120, direction: 'left' }
  );
});
</script>
```

### 2. Image Gallery Navigation

```vue
<template>
  <div
    ref="galleryRef"
    class="relative overflow-hidden rounded-lg"
  >
    <img
      :src="images[currentIndex]"
      :alt="`Image ${currentIndex + 1}`"
      class="w-full h-64 object-cover"
    />
    <div class="absolute bottom-4 left-1/2 transform -translate-x-1/2">
      <div class="flex gap-2">
        <div
          v-for="(_, index) in images"
          :key="index"
          :class="[
            'w-2 h-2 rounded-full',
            index === currentIndex ? 'bg-white' : 'bg-white/50'
          ]"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
const images = ref(['image1.jpg', 'image2.jpg', 'image3.jpg']);
const currentIndex = ref(0);
const galleryRef = ref<HTMLElement>();

useSwipeNavigation(
  galleryRef,
  () => {
    // Next image (swipe left)
    currentIndex.value = (currentIndex.value + 1) % images.value.length;
  },
  () => {
    // Previous image (swipe right)
    currentIndex.value = currentIndex.value === 0 
      ? images.value.length - 1 
      : currentIndex.value - 1;
  }
);
</script>
```

### 3. Appointment Card with Actions

```vue
<template>
  <div
    ref="appointmentRef"
    class="bg-white rounded-lg shadow-sm border p-4 touch-manipulation"
  >
    <div class="flex justify-between items-start">
      <div>
        <h3 class="font-semibold">{{ appointment.patientName }}</h3>
        <p class="text-sm text-gray-600">{{ appointment.service }}</p>
        <p class="text-xs text-gray-500">{{ formatTime(appointment.time) }}</p>
      </div>
      <div class="flex gap-2">
        <button class="p-2 hover:bg-gray-100 rounded">
          <UIcon name="i-lucide-more-horizontal" class="w-4 h-4" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
const appointmentRef = ref<HTMLElement>();

useTableRowSwipeActions(appointmentRef, appointment, {
  onEdit: (appt) => editAppointment(appt),
  onDelete: (appt) => cancelAppointment(appt),
  onView: (appt) => viewAppointment(appt)
});
</script>
```

## Performance Considerations

### Optimization Tips

1. **Use `touch-action: manipulation`** CSS property for better touch responsiveness
2. **Debounce gesture detection** to avoid excessive event handling
3. **Use hardware acceleration** with CSS transforms for smooth animations
4. **Limit concurrent gesture listeners** to avoid performance issues

### CSS Classes

```css
.touch-manipulation {
  touch-action: manipulation;
}

.swipe-container {
  touch-action: pan-x; /* Allow horizontal panning only */
  -webkit-overflow-scrolling: touch;
}

.swipe-item {
  transform: translateZ(0); /* Enable hardware acceleration */
  transition: transform 0.3s ease-out;
}
```

## Accessibility Considerations

### Screen Reader Support

```vue
<template>
  <div
    ref="swipeElement"
    role="button"
    :aria-label="swipeInstructions"
    tabindex="0"
    @keydown="handleKeydown"
  >
    <!-- Content -->
  </div>
</template>

<script setup>
const swipeInstructions = computed(() => 
  `Swipe left to delete, swipe right to edit. Use keyboard shortcuts: Delete key to delete, Enter to edit.`
);

const handleKeydown = (event: KeyboardEvent) => {
  switch (event.key) {
    case 'Delete':
      deleteItem();
      break;
    case 'Enter':
      editItem();
      break;
  }
};
</script>
```

### Reduced Motion Support

```css
@media (prefers-reduced-motion: reduce) {
  .swipe-item {
    transition: none;
  }
  
  .swipe-feedback {
    animation: none;
  }
}
```

## Testing

### Manual Testing Checklist

- [ ] **Swipe Detection**: Gestures detected at correct thresholds
- [ ] **Direction Handling**: Only allowed directions trigger actions
- [ ] **Visual Feedback**: Smooth animations and visual cues
- [ ] **Touch Targets**: Minimum 44px touch targets maintained
- [ ] **Performance**: No lag or stuttering during gestures
- [ ] **Accessibility**: Keyboard alternatives work correctly
- [ ] **Cross-Device**: Works on various touch devices

### Automated Testing

```javascript
// Cypress test example
describe('Swipe Gestures', () => {
  it('should close sidebar on left swipe', () => {
    cy.viewport('iphone-6');
    cy.visit('/');
    
    // Open mobile navigation
    cy.get('[aria-label="Open navigation menu"]').click();
    cy.get('nav').should('be.visible');
    
    // Simulate swipe left
    cy.get('nav')
      .trigger('touchstart', { touches: [{ clientX: 200, clientY: 300 }] })
      .trigger('touchmove', { touches: [{ clientX: 50, clientY: 300 }] })
      .trigger('touchend', { changedTouches: [{ clientX: 50, clientY: 300 }] });
    
    cy.get('nav').should('not.be.visible');
  });
});
```

## Browser Support

| Browser | Version | Support Level |
|---------|---------|---------------|
| Chrome | 60+ | Full support |
| Safari | 13+ | Full support |
| Firefox | 65+ | Full support |
| Edge | 79+ | Full support |
| Samsung Internet | 8+ | Full support |

## Troubleshooting

### Common Issues

**Gestures not detected**
- Check if element has proper touch event listeners
- Verify threshold and velocity settings
- Ensure element is not covered by other elements

**Conflicting with scroll**
- Use `preventScroll: true` option
- Set appropriate `touch-action` CSS property
- Check for conflicting event handlers

**Poor performance**
- Reduce number of concurrent gesture listeners
- Use hardware acceleration with CSS transforms
- Debounce gesture callbacks if needed

**Accessibility issues**
- Provide keyboard alternatives
- Add proper ARIA labels
- Test with screen readers

This comprehensive swipe gesture system provides a solid foundation for touch-optimized interactions throughout the admin-nuxt application while maintaining accessibility and performance standards.