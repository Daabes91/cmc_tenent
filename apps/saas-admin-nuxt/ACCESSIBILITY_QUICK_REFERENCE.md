# Accessibility Quick Reference Guide

## For Developers

This quick reference guide provides common accessibility patterns and best practices for the SAAS Manager Admin Panel.

## Quick Checklist

When creating or modifying components, ensure:

- [ ] All interactive elements are keyboard accessible
- [ ] Focus indicators are visible
- [ ] ARIA labels are present where needed
- [ ] Color contrast meets WCAG AA (4.5:1 for text)
- [ ] Forms have proper labels and error messages
- [ ] Images have alt text or are marked decorative
- [ ] Tables have proper semantic structure
- [ ] Dynamic content updates are announced

## Common Patterns

### 1. Buttons

```vue
<!-- Good: Descriptive text -->
<UButton @click="handleClick">
  Save Changes
</UButton>

<!-- Good: Icon with aria-label -->
<UButton
  icon="i-heroicons-x-mark"
  :aria-label="$t('common.close')"
  @click="handleClose"
/>

<!-- Bad: Icon without label -->
<UButton icon="i-heroicons-x-mark" @click="handleClose" />
```

### 2. Links

```vue
<!-- Good: Descriptive text -->
<NuxtLink to="/tenants">
  View All Tenants
</NuxtLink>

<!-- Good: With aria-current for active state -->
<NuxtLink
  to="/dashboard"
  :aria-current="isActive ? 'page' : undefined"
>
  Dashboard
</NuxtLink>
```

### 3. Form Inputs

```vue
<!-- Good: With label and error handling -->
<UFormGroup
  :label="$t('form.email')"
  name="email"
  required
  :error="emailError"
>
  <UInput
    v-model="email"
    type="email"
    :aria-required="true"
    :aria-invalid="!!emailError"
    :aria-describedby="emailError ? 'email-error' : undefined"
    autocomplete="email"
  />
  <span v-if="emailError" id="email-error" class="sr-only">
    {{ emailError }}
  </span>
</UFormGroup>

<!-- Bad: Input without label -->
<input v-model="email" type="email" />
```

### 4. Icons

```vue
<!-- Good: Decorative icon -->
<UIcon name="i-heroicons-check" aria-hidden="true" />

<!-- Good: Meaningful icon with label -->
<UIcon
  name="i-heroicons-information-circle"
  :aria-label="$t('common.information')"
/>
```

### 5. Tables

```vue
<!-- Good: Accessible table -->
<table role="table" :aria-label="$t('tenants.tableDescription')">
  <thead>
    <tr role="row">
      <th
        scope="col"
        role="columnheader"
        :aria-sort="sortDirection"
        @click="sort"
        @keydown.enter="sort"
        @keydown.space.prevent="sort"
        tabindex="0"
      >
        Name
      </th>
    </tr>
  </thead>
  <tbody>
    <tr
      role="row"
      tabindex="0"
      @click="selectRow"
      @keydown.enter="selectRow"
      @keydown.space.prevent="selectRow"
    >
      <td role="cell">Cell Content</td>
    </tr>
  </tbody>
</table>
```

### 6. Modals/Dialogs

```vue
<!-- Good: Accessible modal -->
<UModal
  v-model="isOpen"
  role="dialog"
  :aria-modal="true"
  :aria-labelledby="modalTitleId"
  :aria-describedby="modalDescId"
>
  <h2 :id="modalTitleId">Modal Title</h2>
  <p :id="modalDescId">Modal description</p>
  <!-- Modal content -->
</UModal>
```

### 7. Loading States

```vue
<!-- Good: Loading with aria-busy -->
<div :aria-busy="loading" :aria-live="polite">
  <LoadingSkeleton v-if="loading" />
  <div v-else>Content</div>
</div>
```

### 8. Live Regions

```vue
<!-- Good: Announce updates to screen readers -->
<div
  role="status"
  aria-live="polite"
  aria-atomic="true"
  class="sr-only"
>
  {{ statusMessage }}
</div>
```

### 9. Navigation

```vue
<!-- Good: Navigation with landmarks -->
<nav
  role="navigation"
  :aria-label="$t('accessibility.primaryNavigation')"
>
  <NuxtLink
    v-for="item in items"
    :key="item.path"
    :to="item.path"
    :aria-current="isActive(item.path) ? 'page' : undefined"
  >
    {{ item.label }}
  </NuxtLink>
</nav>
```

### 10. Skip Links

```vue
<!-- Good: Skip to main content -->
<a href="#main-content" class="skip-link">
  {{ $t('accessibility.skipToMain') }}
</a>

<main id="main-content" role="main">
  <!-- Main content -->
</main>
```

## CSS Classes

### Screen Reader Only

```css
/* Hide visually but keep for screen readers */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}

/* Show when focused */
.sr-only-focusable:focus {
  position: static;
  width: auto;
  height: auto;
  /* ... */
}
```

### Focus Indicators

```css
/* Enhanced focus for all elements */
*:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

/* Button focus */
button:focus-visible {
  ring: 2px solid var(--primary-color);
  ring-offset: 2px;
}
```

## Utility Functions

### Announce to Screen Reader

```typescript
import { announceToScreenReader } from '~/utils/accessibility'

// Polite announcement (doesn't interrupt)
announceToScreenReader('Form submitted successfully', 'polite')

// Assertive announcement (interrupts current reading)
announceToScreenReader('Error: Please fix the form', 'assertive')
```

### Trap Focus in Modal

```typescript
import { trapFocus } from '~/utils/accessibility'

const modalElement = ref<HTMLElement>()

onMounted(() => {
  if (modalElement.value) {
    const cleanup = trapFocus(modalElement.value)
    
    onUnmounted(() => {
      cleanup()
    })
  }
})
```

### Generate Unique ARIA ID

```typescript
import { generateAriaId } from '~/utils/accessibility'

const labelId = generateAriaId('form-label')
const errorId = generateAriaId('form-error')
```

## Testing Checklist

### Keyboard Navigation
- [ ] Tab through all interactive elements
- [ ] Shift+Tab works in reverse
- [ ] Enter/Space activates buttons and links
- [ ] Escape closes modals
- [ ] Arrow keys work in lists/tables
- [ ] No keyboard traps

### Screen Reader
- [ ] All content is announced
- [ ] Form errors are announced
- [ ] Dynamic updates are announced
- [ ] Images have alt text
- [ ] Links are descriptive
- [ ] Buttons have labels

### Visual
- [ ] Focus indicators are visible
- [ ] Color contrast is sufficient
- [ ] Text is readable at 200% zoom
- [ ] No information by color alone

### Forms
- [ ] All inputs have labels
- [ ] Required fields are marked
- [ ] Errors are clear and helpful
- [ ] Validation is accessible

## Common Mistakes to Avoid

### ❌ Don't Do This

```vue
<!-- Missing label -->
<input v-model="search" placeholder="Search" />

<!-- Icon without label -->
<button @click="close">
  <UIcon name="i-heroicons-x-mark" />
</button>

<!-- Div as button -->
<div @click="handleClick">Click me</div>

<!-- Color only indicator -->
<span class="text-red-500">Error</span>

<!-- Missing alt text -->
<img src="logo.png" />
```

### ✅ Do This Instead

```vue
<!-- With label -->
<UFormGroup label="Search">
  <UInput v-model="search" placeholder="Search" />
</UFormGroup>

<!-- Icon with label -->
<button @click="close" aria-label="Close">
  <UIcon name="i-heroicons-x-mark" aria-hidden="true" />
</button>

<!-- Proper button -->
<button @click="handleClick">Click me</button>

<!-- Text + color indicator -->
<span class="text-red-500">
  <UIcon name="i-heroicons-exclamation-circle" aria-hidden="true" />
  Error
</span>

<!-- With alt text -->
<img src="logo.png" alt="Company Logo" />
```

## Resources

### Tools
- [Lighthouse](https://developers.google.com/web/tools/lighthouse) - Built into Chrome DevTools
- [axe DevTools](https://www.deque.com/axe/devtools/) - Browser extension
- [WAVE](https://wave.webaim.org/) - Web accessibility evaluation tool

### Guidelines
- [WCAG 2.1 Quick Reference](https://www.w3.org/WAI/WCAG21/quickref/)
- [ARIA Authoring Practices](https://www.w3.org/WAI/ARIA/apg/)
- [MDN Accessibility](https://developer.mozilla.org/en-US/docs/Web/Accessibility)

### Testing
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
- [Keyboard Testing Guide](https://webaim.org/techniques/keyboard/)

## Need Help?

- Check [docs/accessibility.md](./docs/accessibility.md) for detailed guide
- Review [ACCESSIBILITY_IMPLEMENTATION_SUMMARY.md](./ACCESSIBILITY_IMPLEMENTATION_SUMMARY.md)
- Test on [/accessibility-test](http://localhost:3000/accessibility-test) page
- Ask the team for accessibility review

## Remember

**Accessibility is not optional—it's a requirement.**

Every user deserves equal access to our application, regardless of their abilities or the assistive technologies they use.
