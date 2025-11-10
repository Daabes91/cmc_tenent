# Hero Media Responsive Design & Dark Mode Test Guide

## Overview
This guide provides step-by-step instructions to test the responsive design and dark mode improvements for the hero media customization feature.

## Prerequisites
- Both `web-next` and `admin-nuxt` applications running
- Access to browser DevTools
- Test images and YouTube video URLs ready

## Test Environment Setup

### 1. Start Applications
```bash
# Terminal 1 - Start web-next
cd apps/web-next
npm run dev

# Terminal 2 - Start admin-nuxt
cd apps/admin-nuxt
npm run dev
```

### 2. Browser DevTools Setup
1. Open Chrome/Firefox DevTools (F12)
2. Enable Device Toolbar (Ctrl+Shift+M / Cmd+Shift+M)
3. Prepare viewport presets:
   - Mobile: 375x667 (iPhone SE)
   - Tablet: 768x1024 (iPad)
   - Desktop: 1920x1080

## Test Scenarios

### Scenario 1: Mobile Responsiveness (< 640px)

#### Admin Panel Configuration
1. **Navigate to Clinic Settings**
   - URL: `http://localhost:3000/clinic-settings`
   - Set viewport: 375x667

2. **Test Hero Media Configuration Component**
   - [ ] Header displays in stacked layout (icon above text)
   - [ ] Radio buttons stack vertically
   - [ ] Image upload button is touch-friendly (min 44px height)
   - [ ] YouTube URL input is full width
   - [ ] Example URLs are readable with line breaks
   - [ ] Clear button shows short text or icon only

3. **Test Hero Media Preview Component**
   - [ ] Preview container has appropriate padding (16px)
   - [ ] Loading skeleton height is 250px
   - [ ] Image/video displays with rounded corners (16px)
   - [ ] Fallback indicator text is centered and readable
   - [ ] Info badge stacks vertically
   - [ ] All text is legible (minimum 12px)

#### Landing Page Display
1. **Navigate to Home Page**
   - URL: `http://localhost:3001`
   - Set viewport: 375x667

2. **Test Hero Section**
   - [ ] Hero media height is 300px
   - [ ] YouTube embed maintains 16:9 aspect ratio
   - [ ] Image scales correctly without distortion
   - [ ] Gradient overlay provides text contrast
   - [ ] Loading icon is 32px
   - [ ] Hero highlights display in single column
   - [ ] Text is readable (12px minimum)

### Scenario 2: Tablet Responsiveness (640px - 1024px)

#### Admin Panel Configuration
1. **Set viewport: 768x1024**

2. **Test Hero Media Configuration Component**
   - [ ] Header displays horizontally
   - [ ] Radio buttons display horizontally with spacing
   - [ ] Form elements use medium sizing
   - [ ] YouTube examples are readable
   - [ ] Clear button shows full text

3. **Test Hero Media Preview Component**
   - [ ] Preview container has 24px padding
   - [ ] Loading skeleton height is 350px
   - [ ] Media displays with 24px rounded corners
   - [ ] Fallback indicator displays horizontally
   - [ ] Info badge displays horizontally

#### Landing Page Display
1. **Set viewport: 768x1024**

2. **Test Hero Section**
   - [ ] Hero media height is 400px
   - [ ] YouTube embed scales properly
   - [ ] Image quality is appropriate
   - [ ] Hero highlights display in 2-column grid
   - [ ] Spacing between elements is balanced
   - [ ] Loading icon is 40px

### Scenario 3: Desktop Responsiveness (> 1024px)

#### Admin Panel Configuration
1. **Set viewport: 1920x1080**

2. **Test Hero Media Configuration Component**
   - [ ] All elements use desktop spacing
   - [ ] Form is easy to use with mouse
   - [ ] Preview is large and clear
   - [ ] Typography is optimal size

3. **Test Hero Media Preview Component**
   - [ ] Preview container has 24px padding
   - [ ] Loading skeleton height is 400px
   - [ ] Media displays at maximum quality
   - [ ] All elements are properly aligned

#### Landing Page Display
1. **Set viewport: 1920x1080**

2. **Test Hero Section**
   - [ ] Hero media height is 500px
   - [ ] YouTube embed is crisp and clear
   - [ ] Image is high quality
   - [ ] Hero highlights display in 2-column grid
   - [ ] Loading icon is 48px
   - [ ] All spacing is optimal

### Scenario 4: Dark Mode Testing

#### Enable Dark Mode
1. **Toggle dark mode** in both applications
2. **Test all viewport sizes** (mobile, tablet, desktop)

#### Admin Panel Dark Mode
1. **Hero Media Configuration**
   - [ ] Background colors transition smoothly (300ms)
   - [ ] Text is readable with good contrast
   - [ ] Borders are visible
   - [ ] Input fields have appropriate styling
   - [ ] Icons are visible
   - [ ] Error states are clear

2. **Hero Media Preview**
   - [ ] Gradient overlay adapts to dark theme
   - [ ] Preview container has dark background
   - [ ] Text maintains readability
   - [ ] Borders and shadows are visible
   - [ ] Info badge has appropriate colors
   - [ ] Loading state is visible

#### Landing Page Dark Mode
1. **Hero Section**
   - [ ] Gradient overlay is enhanced (from-blue-500/20)
   - [ ] Text maintains excellent contrast
   - [ ] Shadows are visible (dark:shadow-blue-900/50)
   - [ ] Hero highlights have dark backgrounds
   - [ ] All transitions are smooth (300ms)
   - [ ] No flash of unstyled content

### Scenario 5: Theme Switching

#### Test Smooth Transitions
1. **Start in light mode**
2. **Toggle to dark mode** - observe transitions
3. **Toggle back to light mode** - observe transitions

**Check:**
- [ ] All color transitions take 300ms
- [ ] No jarring color changes
- [ ] No layout shifts
- [ ] Text remains readable during transition
- [ ] Gradient overlays transition smoothly
- [ ] Shadows transition smoothly

### Scenario 6: YouTube Embed Responsiveness

#### Test Video Scaling
1. **Configure YouTube video** in admin panel
2. **Test on all viewport sizes**

**Mobile (375px):**
- [ ] Video maintains 16:9 aspect ratio
- [ ] No horizontal scrolling
- [ ] Gradient overlay is visible
- [ ] Loading state is appropriate

**Tablet (768px):**
- [ ] Video scales proportionally
- [ ] Quality is good
- [ ] Overlay adapts to size

**Desktop (1920px):**
- [ ] Video is full quality
- [ ] No pixelation
- [ ] Overlay is subtle but effective

### Scenario 7: Image Responsiveness

#### Test Image Scaling
1. **Upload custom hero image** in admin panel
2. **Test on all viewport sizes**

**Check:**
- [ ] Image loads appropriate size for viewport
- [ ] No unnecessary large images on mobile
- [ ] Image maintains aspect ratio
- [ ] No distortion or stretching
- [ ] Gradient overlay works on all sizes
- [ ] Border radius scales appropriately

### Scenario 8: Touch Interactions (Mobile)

#### Test Touch Targets
1. **Use mobile device or touch emulation**
2. **Test all interactive elements**

**Admin Panel:**
- [ ] Radio buttons are easy to tap (min 44x44px)
- [ ] Image upload button is touch-friendly
- [ ] YouTube URL input is easy to focus
- [ ] Clear button is easy to tap
- [ ] All buttons have appropriate spacing

**Landing Page:**
- [ ] Hero section doesn't interfere with scrolling
- [ ] YouTube video doesn't capture touch events
- [ ] All buttons are touch-friendly

### Scenario 9: Cross-Browser Testing

#### Chrome/Edge
- [ ] All responsive breakpoints work
- [ ] Dark mode transitions are smooth
- [ ] YouTube embed loads correctly
- [ ] Images display properly

#### Firefox
- [ ] Responsive design matches Chrome
- [ ] Dark mode works correctly
- [ ] No layout issues
- [ ] Performance is good

#### Safari (macOS/iOS)
- [ ] Responsive design works
- [ ] Dark mode respects system preference
- [ ] YouTube embed works
- [ ] Touch interactions work (iOS)
- [ ] No webkit-specific issues

### Scenario 10: Performance Testing

#### Measure Performance
1. **Open DevTools Performance tab**
2. **Record page load**
3. **Toggle dark mode**
4. **Resize viewport**

**Check:**
- [ ] Page load time is acceptable
- [ ] Dark mode toggle is instant (< 300ms)
- [ ] Viewport resize is smooth (60fps)
- [ ] No layout thrashing
- [ ] Images load progressively
- [ ] YouTube embed doesn't block rendering

## Common Issues to Watch For

### Layout Issues
- Text overflow on small screens
- Horizontal scrolling
- Overlapping elements
- Misaligned components

### Dark Mode Issues
- Poor contrast ratios
- Invisible borders
- Unreadable text
- Flash of unstyled content
- Slow transitions

### Responsive Issues
- Images not scaling
- Fixed widths breaking layout
- Touch targets too small
- Text too small to read
- Buttons cut off

### YouTube Embed Issues
- Aspect ratio not maintained
- Video not loading
- Controls showing when they shouldn't
- Autoplay not working
- Overlay not visible

## Success Criteria

### Must Pass
- ✅ All text is readable on all viewport sizes
- ✅ No horizontal scrolling on any device
- ✅ Touch targets are minimum 44x44px
- ✅ Dark mode has good contrast (WCAG AA)
- ✅ Transitions are smooth (300ms)
- ✅ YouTube embed maintains 16:9 ratio
- ✅ Images scale appropriately

### Should Pass
- ✅ Performance is good (< 3s load time)
- ✅ No layout shifts during load
- ✅ Animations are smooth (60fps)
- ✅ Cross-browser consistency
- ✅ Keyboard navigation works

## Reporting Issues

If you find issues, document:
1. **Viewport size** where issue occurs
2. **Browser and version**
3. **Dark/light mode**
4. **Steps to reproduce**
5. **Expected vs actual behavior**
6. **Screenshots**

## Automated Testing

Consider adding these automated tests:
```typescript
// Responsive breakpoint tests
describe('Hero Media Responsive Design', () => {
  test('displays correctly on mobile', () => {
    cy.viewport(375, 667)
    // assertions
  })
  
  test('displays correctly on tablet', () => {
    cy.viewport(768, 1024)
    // assertions
  })
  
  test('displays correctly on desktop', () => {
    cy.viewport(1920, 1080)
    // assertions
  })
})

// Dark mode tests
describe('Hero Media Dark Mode', () => {
  test('transitions smoothly to dark mode', () => {
    cy.get('[data-theme-toggle]').click()
    cy.get('[data-hero-media]').should('have.class', 'dark')
  })
})
```

## Conclusion

After completing all test scenarios, the hero media feature should:
- Work seamlessly across all viewport sizes
- Provide excellent dark mode support
- Maintain performance and accessibility
- Offer a consistent user experience

Document any issues found and create tickets for fixes if needed.
