# Accessibility Guide

## Overview

The SAAS Manager Admin Panel is built with accessibility as a core principle, ensuring that all users, including those using assistive technologies, can effectively manage tenants and monitor system health.

## WCAG 2.1 AA Compliance

This application strives to meet WCAG 2.1 Level AA standards for accessibility.

### Key Features

#### 1. Keyboard Navigation

**Full Keyboard Support**
- All interactive elements are keyboard accessible
- Logical tab order throughout the application
- Skip links to bypass repetitive navigation
- Keyboard shortcuts for common actions

**Navigation Keys**
- `Tab` / `Shift+Tab`: Navigate between interactive elements
- `Enter` / `Space`: Activate buttons and links
- `Escape`: Close modals and dropdowns
- `Arrow Keys`: Navigate within lists and tables
- `Home` / `End`: Jump to first/last item in lists

**Skip Links**
- Press `Tab` on page load to reveal "Skip to main content" link
- Allows keyboard users to bypass navigation and jump directly to content

#### 2. Screen Reader Support

**ARIA Labels and Landmarks**
- Semantic HTML5 elements (`<main>`, `<nav>`, `<header>`, etc.)
- ARIA landmarks for major page sections
- Descriptive labels for all interactive elements
- Live regions for dynamic content updates

**Screen Reader Announcements**
- Form validation errors announced immediately
- Success/error messages announced via live regions
- Loading states communicated clearly
- Table sorting changes announced

**Accessible Names**
- All images have alt text
- Icons have aria-labels or are marked as decorative
- Form inputs have associated labels
- Buttons have descriptive text or aria-labels

#### 3. Visual Design

**Color Contrast**
- All text meets WCAG AA contrast ratios (4.5:1 for normal text, 3:1 for large text)
- Interactive elements have sufficient contrast
- Focus indicators are clearly visible
- High contrast mode support

**Focus Indicators**
- Visible focus outlines on all interactive elements
- Enhanced focus styles with 2px solid borders
- Focus indicators work in both light and dark modes
- Custom focus styles for different element types

**Typography**
- Minimum font size of 14px on mobile, 16px on desktop
- Readable line heights (1.5 for body text)
- Clear font hierarchy
- Responsive text sizing

#### 4. Forms and Inputs

**Form Accessibility**
- All inputs have associated labels
- Required fields clearly marked
- Inline validation with error messages
- Error messages linked to inputs via aria-describedby
- Autocomplete attributes for common fields

**Input States**
- Clear visual distinction between states (default, hover, focus, disabled, error)
- aria-invalid for fields with errors
- aria-required for required fields
- aria-busy for loading states

#### 5. Tables

**Accessible Tables**
- Proper table markup with `<thead>`, `<tbody>`, `<th>`, `<td>`
- Column headers with scope="col"
- Row headers where appropriate
- Sortable columns with aria-sort attributes
- Keyboard navigation within tables

**Table Features**
- Click or press Enter/Space to sort columns
- Current sort direction announced to screen readers
- Empty state messages for tables with no data
- Responsive table alternatives on mobile (cards)

#### 6. Modals and Dialogs

**Modal Accessibility**
- Focus trapped within modal when open
- Focus returns to trigger element on close
- Escape key closes modal
- Proper ARIA roles (role="dialog")
- aria-modal="true" attribute
- Descriptive aria-labelledby and aria-describedby

#### 7. Notifications and Alerts

**Accessible Notifications**
- Toast notifications use aria-live regions
- Different priorities (polite/assertive) based on urgency
- Dismissible with keyboard
- Sufficient display time for reading
- Color not the only indicator of status

#### 8. Responsive Design

**Mobile Accessibility**
- Touch targets minimum 44x44px
- Sufficient spacing between interactive elements
- Pinch-to-zoom enabled
- Responsive text sizing
- Mobile-friendly navigation

#### 9. Motion and Animation

**Reduced Motion Support**
- Respects prefers-reduced-motion media query
- Animations can be disabled
- No auto-playing videos or carousels
- Smooth scrolling can be disabled

## Testing Checklist

### Keyboard Navigation
- [ ] All interactive elements reachable via keyboard
- [ ] Tab order is logical and intuitive
- [ ] Skip link appears on first Tab press
- [ ] No keyboard traps
- [ ] Focus indicators always visible
- [ ] Escape closes modals and dropdowns

### Screen Readers
- [ ] Test with NVDA (Windows)
- [ ] Test with JAWS (Windows)
- [ ] Test with VoiceOver (macOS/iOS)
- [ ] Test with TalkBack (Android)
- [ ] All content announced correctly
- [ ] Form errors announced
- [ ] Dynamic content updates announced

### Visual
- [ ] Color contrast meets WCAG AA (use browser DevTools)
- [ ] Focus indicators visible in all states
- [ ] Text readable at 200% zoom
- [ ] No information conveyed by color alone
- [ ] High contrast mode works correctly

### Forms
- [ ] All inputs have labels
- [ ] Required fields marked
- [ ] Error messages clear and helpful
- [ ] Validation errors announced
- [ ] Autocomplete works correctly

### Mobile
- [ ] Touch targets at least 44x44px
- [ ] Pinch-to-zoom works
- [ ] Content reflows correctly
- [ ] No horizontal scrolling required
- [ ] Mobile navigation accessible

## Browser and Assistive Technology Support

### Browsers
- Chrome/Edge (latest 2 versions)
- Firefox (latest 2 versions)
- Safari (latest 2 versions)

### Screen Readers
- NVDA (Windows) - Latest version
- JAWS (Windows) - Latest version
- VoiceOver (macOS/iOS) - Latest version
- TalkBack (Android) - Latest version

## Common Accessibility Patterns

### Skip Link
```vue
<a href="#main-content" class="skip-link">
  Skip to main content
</a>
```

### Accessible Button
```vue
<button
  type="button"
  :aria-label="descriptiveLabel"
  :aria-pressed="isPressed"
  @click="handleClick"
>
  <UIcon name="icon-name" aria-hidden="true" />
  <span>Button Text</span>
</button>
```

### Accessible Form Input
```vue
<UFormGroup
  :label="label"
  :error="errorMessage"
>
  <UInput
    v-model="value"
    :aria-required="true"
    :aria-invalid="!!errorMessage"
    :aria-describedby="errorMessage ? 'input-error' : undefined"
  />
  <span v-if="errorMessage" id="input-error" class="sr-only">
    {{ errorMessage }}
  </span>
</UFormGroup>
```

### Accessible Table
```vue
<table role="table" :aria-label="tableDescription">
  <thead>
    <tr role="row">
      <th
        scope="col"
        role="columnheader"
        :aria-sort="sortDirection"
        @click="sort"
        @keydown.enter="sort"
        tabindex="0"
      >
        Column Name
      </th>
    </tr>
  </thead>
  <tbody>
    <tr
      role="row"
      tabindex="0"
      @click="selectRow"
      @keydown.enter="selectRow"
    >
      <td role="cell">Cell Content</td>
    </tr>
  </tbody>
</table>
```

### Live Region for Announcements
```vue
<div
  role="status"
  aria-live="polite"
  aria-atomic="true"
  class="sr-only"
>
  {{ announcement }}
</div>
```

## Resources

### Tools
- [axe DevTools](https://www.deque.com/axe/devtools/) - Browser extension for accessibility testing
- [WAVE](https://wave.webaim.org/) - Web accessibility evaluation tool
- [Lighthouse](https://developers.google.com/web/tools/lighthouse) - Built into Chrome DevTools
- [Color Contrast Analyzer](https://www.tpgi.com/color-contrast-checker/) - Check color contrast ratios

### Guidelines
- [WCAG 2.1](https://www.w3.org/WAI/WCAG21/quickref/) - Web Content Accessibility Guidelines
- [ARIA Authoring Practices](https://www.w3.org/WAI/ARIA/apg/) - ARIA design patterns
- [MDN Accessibility](https://developer.mozilla.org/en-US/docs/Web/Accessibility) - Accessibility documentation

### Testing
- [WebAIM Screen Reader Testing](https://webaim.org/articles/screenreader_testing/)
- [Keyboard Accessibility](https://webaim.org/techniques/keyboard/)
- [Color Contrast Testing](https://webaim.org/resources/contrastchecker/)

## Reporting Accessibility Issues

If you encounter any accessibility issues, please report them with:
1. Description of the issue
2. Steps to reproduce
3. Browser and assistive technology used
4. Expected vs actual behavior
5. Screenshots or screen recordings if applicable

## Continuous Improvement

Accessibility is an ongoing commitment. We regularly:
- Conduct accessibility audits
- Test with real users using assistive technologies
- Update components based on feedback
- Stay current with WCAG guidelines
- Train developers on accessibility best practices
