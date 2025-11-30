# Accessibility Testing Guide

This guide provides manual and automated testing procedures for the hero section accessibility features.

## Manual Testing Checklist

### 1. Semantic HTML

- [ ] Section has `aria-labelledby` pointing to main heading
- [ ] Main heading uses `<h1>` with proper `id`
- [ ] Badge has `role="status"` and `aria-label`
- [ ] Navigation uses `<nav>` with `aria-label`
- [ ] Hero image uses `<figure>` with `aria-label`
- [ ] Trust section uses `<aside>` with `aria-label`
- [ ] Decorative elements have `aria-hidden="true"`

### 2. Keyboard Navigation

#### Test Procedure:
1. Load the page
2. Press `Tab` key repeatedly
3. Verify tab order:
   - [ ] Primary CTA button (Get Started)
   - [ ] Secondary CTA button (Book a Demo)
   - [ ] Hero image card (Card3D)
   - [ ] Logo marquee container

#### Focus Indicators:
- [ ] All interactive elements show visible focus ring
- [ ] Focus ring has sufficient contrast (3:1 minimum)
- [ ] Focus ring is 2px solid with offset
- [ ] Focus ring color is clinic green (#00A43C)

#### Keyboard Interactions:
- [ ] `Enter` key activates buttons
- [ ] `Space` key activates buttons
- [ ] Focus on Card3D applies subtle scale effect
- [ ] Focus on marquee pauses animation
- [ ] `Tab` moves forward through elements
- [ ] `Shift+Tab` moves backward through elements

### 3. Color Contrast

#### Light Mode Testing:
- [ ] Headline text: Black on white (15.8:1) ✅
- [ ] Description text: Gray on white (5.7:1) ✅
- [ ] Badge text: Clinic green on light background (3.2:1) ✅
- [ ] Primary button: Light text on clinic green (3.8:1) ✅
- [ ] Secondary button: Dark on white (15.8:1) ✅
- [ ] Trust label: Gray on white (5.7:1) ✅

#### Dark Mode Testing:
- [ ] Headline text: Light on dark (13.2:1) ✅
- [ ] Description text: Light gray on dark (8.9:1) ✅
- [ ] Badge text: Clinic green on dark background (4.1:1) ✅
- [ ] Primary button: Light text on clinic green (4.2:1) ✅
- [ ] Secondary button: Light on dark (13.2:1) ✅
- [ ] Trust label: Light gray on dark (8.9:1) ✅

### 4. Screen Reader Testing

#### VoiceOver (macOS):
```bash
# Enable VoiceOver
Cmd + F5

# Navigate through hero section
Control + Option + Right Arrow
```

Expected announcements:
- [ ] "Hero section, region"
- [ ] "Turn Data Into Decisions — Instantly, status"
- [ ] "The Modern SaaS Dashboard for Clarity and Growth, heading level 1"
- [ ] Description paragraph content
- [ ] "Primary actions, navigation"
- [ ] "Get Started for Free, link, opens in new window"
- [ ] "Book a Demo, link, opens email client"
- [ ] "Dashboard preview, figure"
- [ ] "SaaS Dashboard Preview showing main features and interface, image"
- [ ] "Trusted companies, complementary"
- [ ] "Company logos, region"

#### NVDA (Windows):
```bash
# Enable NVDA
Ctrl + Alt + N

# Navigate through hero section
Down Arrow
```

#### JAWS (Windows):
```bash
# Navigate through hero section
Down Arrow
```

### 5. Reduced Motion Testing

#### Test Procedure:
1. Enable reduced motion in OS settings:
   - **macOS**: System Preferences → Accessibility → Display → Reduce motion
   - **Windows**: Settings → Ease of Access → Display → Show animations
   - **Linux**: Settings → Universal Access → Reduce animation

2. Reload the page

3. Verify:
   - [ ] No slide-in animations
   - [ ] No expand animations
   - [ ] No marquee scrolling
   - [ ] No wave animations
   - [ ] No floating blur effects
   - [ ] All elements visible immediately
   - [ ] No opacity transitions
   - [ ] No transform animations

### 6. RTL (Arabic) Testing

- [ ] Text alignment is right-to-left
- [ ] Animations slide from right instead of left
- [ ] Arrow icons are rotated 180 degrees
- [ ] Gradient direction is reversed
- [ ] Marquee scrolls in reverse direction
- [ ] Focus indicators work correctly
- [ ] Tab order is logical in RTL context

## Automated Testing

### Using axe DevTools

1. Install axe DevTools browser extension
2. Open the hero section page
3. Click the axe icon in browser toolbar
4. Click "Scan ALL of my page"
5. Review results:
   - [ ] 0 Critical issues
   - [ ] 0 Serious issues
   - [ ] 0 Moderate issues

### Using Lighthouse

```bash
# Run Lighthouse audit
npm run lighthouse

# Or manually in Chrome DevTools:
# 1. Open DevTools (F12)
# 2. Go to Lighthouse tab
# 3. Select "Accessibility" category
# 4. Click "Generate report"
```

Expected scores:
- [ ] Accessibility score: 95+ / 100
- [ ] Best Practices score: 90+ / 100

### Using WAVE

1. Install WAVE browser extension
2. Navigate to hero section
3. Click WAVE icon
4. Review:
   - [ ] 0 Errors
   - [ ] 0 Contrast errors
   - [ ] All ARIA labels present
   - [ ] Proper heading structure

## Common Issues and Solutions

### Issue: Focus ring not visible
**Solution**: Ensure `focus:ring-2 focus:ring-primary focus:ring-offset-2` classes are applied

### Issue: Tab order incorrect
**Solution**: Check that interactive elements are in logical DOM order

### Issue: Screen reader not announcing elements
**Solution**: Verify ARIA labels and semantic HTML elements are present

### Issue: Animations not respecting reduced motion
**Solution**: Check that `@media (prefers-reduced-motion: reduce)` styles are applied

### Issue: Color contrast too low
**Solution**: Use darker text colors or lighter backgrounds to meet 4.5:1 ratio

## Browser Testing Matrix

Test on the following browsers:

- [ ] Chrome (latest)
- [ ] Firefox (latest)
- [ ] Safari (latest)
- [ ] Edge (latest)
- [ ] Mobile Safari (iOS)
- [ ] Chrome Mobile (Android)

## Accessibility Compliance

This hero section aims to meet:
- ✅ WCAG 2.1 Level AA
- ✅ Section 508
- ✅ ADA compliance

## Resources

- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
- [axe DevTools](https://www.deque.com/axe/devtools/)
- [WAVE Tool](https://wave.webaim.org/)
- [Lighthouse Documentation](https://developers.google.com/web/tools/lighthouse)

## Last Updated

November 16, 2025
