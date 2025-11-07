# Mobile Navigation Integration Guide

This guide explains how to integrate the new mobile navigation component with the existing default layout.

## Overview

The mobile navigation system consists of:
- `MobileNavigation.vue` - The main mobile navigation component
- `useMobileNavigation.ts` - State management composable
- `useViewport.ts` - Viewport detection composable

## Integration Steps

### 1. Update Default Layout

Replace the existing mobile navigation in `layouts/default.vue` with the new component:

```vue
<template>
  <div class="relative min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 text-slate-900 transition-colors duration-300 antialiased dark:from-slate-950 dark:via-slate-950 dark:to-slate-900 dark:text-slate-100">
    <!-- Background decorations -->
    <div aria-hidden="true" class="pointer-events-none absolute inset-0 -z-[1] overflow-hidden">
      <!-- ... existing background elements ... -->
    </div>

    <!-- Mobile Navigation Component -->
    <MobileNavigation
      :is-open="isMobileSidebarOpen"
      :page-title="currentPageTitle"
      :navigation-links="navigation"
      :clinic-name="clinicName"
      :clinic-tagline="clinicTagline"
      :clinic-logo="clinicLogo"
      :today-appointments="todayAppointments"
      :utilization-rate="utilizationRate"
      :user-menu-items="userMenu"
      :is-dark="isDark"
      @toggle="toggleMobileSidebar"
      @close="closeMobileSidebar"
      @nav-click="handleMobileNavClick"
      @toggle-theme="toggleTheme"
    />

    <div class="flex min-h-screen">
      <!-- Desktop Sidebar (hidden on mobile) -->
      <aside class="hidden w-72 flex-col border-r border-slate-200/60 bg-white/80 backdrop-blur-xl transition-colors duration-300 dark:border-white/10 dark:bg-slate-950/80 lg:flex">
        <!-- ... existing desktop sidebar content ... -->
      </aside>

      <div class="flex min-h-screen flex-1 flex-col">
        <!-- Desktop Header (hidden on mobile) -->
        <header class="sticky top-0 z-30 border-b border-slate-200/60 bg-white/80 backdrop-blur-2xl transition-colors duration-300 dark:border-white/10 dark:bg-slate-950/80 hidden lg:block">
          <!-- ... existing desktop header content ... -->
        </header>

        <!-- Main Content -->
        <main class="relative flex-1 transition-colors duration-300">
          <!-- ... existing main content ... -->
        </main>

        <!-- Footer -->
        <footer class="border-t border-slate-200/60 bg-white/80 backdrop-blur-xl transition-colors duration-300 dark:border-white/10 dark:bg-slate-950/80">
          <!-- ... existing footer content ... -->
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Import composables
const { viewport } = useViewport();
const { isMobileSidebarOpen, toggleMobileSidebar, closeMobileSidebar } = useMobileNavigation();

// ... existing script content ...

// Mobile navigation handlers
const handleMobileNavClick = (item: any) => {
  // Handle navigation click
  if (item.to) {
    navigateTo(item.to);
  }
};

// Get current page title dynamically
const currentPageTitle = computed(() => {
  const route = useRoute();
  // Map routes to titles or use meta title
  const titleMap: Record<string, string> = {
    '/': 'Dashboard',
    '/appointments': 'Appointments',
    '/patients': 'Patients',
    '/doctors': 'Doctors',
    '/calendar': 'Calendar'
  };
  return titleMap[route.path] || 'Admin Panel';
});

// ... rest of existing script ...
</script>
```

### 2. Remove Old Mobile Navigation

Remove the existing `USlideover` mobile navigation:

```vue
<!-- Remove this block -->
<USlideover v-model="sidebarOpen" side="left" :ui="{ width: 'max-w-sm' }">
  <!-- ... old mobile navigation content ... -->
</USlideover>
```

### 3. Update Header for Mobile

Modify the desktop header to be hidden on mobile since the mobile navigation handles the header:

```vue
<!-- Add 'hidden lg:block' classes to desktop header -->
<header class="sticky top-0 z-30 border-b border-slate-200/60 bg-white/80 backdrop-blur-2xl transition-colors duration-300 dark:border-white/10 dark:bg-slate-950/80 hidden lg:block">
  <!-- ... existing header content ... -->
</header>
```

## Component Props

### MobileNavigation Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `isOpen` | `boolean` | `false` | Controls sidebar visibility |
| `pageTitle` | `string` | `'Dashboard'` | Current page title |
| `navigationLinks` | `array` | `[]` | Navigation menu items |
| `clinicName` | `string` | `'Clinic Admin'` | Clinic name |
| `clinicTagline` | `string` | `'Management System'` | Clinic tagline |
| `clinicLogo` | `string` | `''` | Clinic logo URL |
| `todayAppointments` | `number` | `0` | Today's appointment count |
| `utilizationRate` | `number` | `0` | Utilization percentage |
| `userMenuItems` | `array` | `[]` | User dropdown menu items |
| `isDark` | `boolean` | `false` | Dark mode state |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `@toggle` | `void` | Emitted when hamburger menu is clicked |
| `@close` | `void` | Emitted when sidebar should close |
| `@nav-click` | `item: any` | Emitted when navigation item is clicked |
| `@toggle-theme` | `void` | Emitted when theme toggle is clicked |

## Features

### Touch Gestures
- **Swipe to close**: Swipe left on the sidebar to close it
- **Touch targets**: All interactive elements meet 44px minimum size
- **Smooth animations**: Hardware-accelerated transitions

### Responsive Behavior
- **Auto-hide on desktop**: Component only shows on mobile devices
- **Route-based closing**: Sidebar closes automatically on navigation
- **Escape key support**: Press Escape to close sidebar

### Accessibility
- **ARIA labels**: Proper labeling for screen readers
- **Focus management**: Keyboard navigation support
- **High contrast**: Works with system dark/light mode preferences

## Testing

### Manual Testing
1. Open the test page: `/mobile-nav-test`
2. Resize browser window to mobile size (≤768px)
3. Test hamburger menu functionality
4. Test swipe gestures on touch devices
5. Test navigation and quick actions

### Automated Testing
```javascript
// Cypress test example
describe('Mobile Navigation', () => {
  it('should open and close mobile sidebar', () => {
    cy.viewport(375, 667); // iPhone SE
    cy.visit('/');
    
    // Should show hamburger menu on mobile
    cy.get('[aria-label="Open navigation menu"]').should('be.visible');
    
    // Open sidebar
    cy.get('[aria-label="Open navigation menu"]').click();
    cy.get('nav').should('be.visible');
    
    // Close sidebar
    cy.get('[aria-label="Close navigation menu"]').click();
    cy.get('nav').should('not.be.visible');
  });
});
```

## Performance Considerations

### Optimizations
- **Conditional rendering**: Only renders on mobile devices
- **Lazy loading**: Quick actions loaded on demand
- **Touch optimization**: Uses `touch-action: manipulation`
- **Hardware acceleration**: CSS transforms for smooth animations

### Bundle Size
- **Tree shaking**: Unused features are removed
- **Component splitting**: Mobile-specific code is separated
- **Minimal dependencies**: Uses existing UI components

## Migration Checklist

- [ ] Install and test viewport composable
- [ ] Create mobile navigation component
- [ ] Update default layout template
- [ ] Remove old mobile navigation code
- [ ] Update header visibility classes
- [ ] Test on various mobile devices
- [ ] Verify touch gestures work
- [ ] Test accessibility features
- [ ] Update any custom navigation logic
- [ ] Deploy and monitor performance

## Troubleshooting

### Common Issues

**Sidebar not opening on mobile**
- Check viewport detection: `viewport.isMobile` should be `true`
- Verify component is receiving `isOpen` prop correctly

**Touch gestures not working**
- Ensure device supports touch events
- Check for conflicting CSS `touch-action` properties

**Navigation items not showing**
- Verify `navigationLinks` prop is passed correctly
- Check navigation data structure matches expected format

**Performance issues**
- Enable hardware acceleration: `transform: translateZ(0)`
- Reduce animation duration for slower devices
- Consider reducing backdrop blur effects

This integration provides a comprehensive mobile navigation solution that enhances the user experience on mobile devices while maintaining the existing desktop functionality.