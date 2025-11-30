# Mobile Responsiveness Quick Reference

## Quick Compliance Check

### ✅ Requirement 11.1: Mobile Viewports (320px+)
All sections display correctly on screens from 320px width and above.

**Key Breakpoints:**
- 320px: iPhone SE
- 375px: iPhone 12 Mini
- 390px: iPhone 12/13/14
- 768px: iPad
- 1024px: iPad Pro

**Tailwind Classes Used:**
```css
/* Responsive padding */
py-12 sm:py-16 md:py-24 lg:py-32

/* Responsive grids */
grid-cols-1 md:grid-cols-2 lg:grid-cols-3

/* Responsive text */
text-lg sm:text-2xl md:text-3xl lg:text-4xl

/* Responsive spacing */
gap-3 sm:gap-4 md:gap-6 lg:gap-8
```

### ✅ Requirement 11.2: Font Sizes (16px minimum)
Body text is minimum 16px for readability.

**Font Size Scale:**
- Body text: `text-base` (16px)
- Small text: `text-sm` (14px) - metadata only
- Large text: `text-lg` (18px)
- Headings: Scaled appropriately

**Implementation:**
```tsx
// Body text
<p className="text-base">Content here</p>

// Small metadata
<span className="text-sm text-muted-foreground">5 min read</span>

// Headings
<h1 className="text-lg sm:text-2xl md:text-3xl lg:text-4xl">Title</h1>
```

### ✅ Requirement 11.3: Touch Targets (44px minimum)
All interactive elements meet 44px minimum height.

**Button Heights:**
- Mobile: `h-11` (44px)
- Desktop: `h-12` (48px)
- Large: `h-14` (56px)

**Implementation:**
```tsx
// Standard CTA button
<Button className="h-11 sm:h-12 px-6 sm:px-8">
  Get Started
</Button>

// Full width on mobile
<Button className="h-11 sm:h-12 w-full sm:w-auto">
  Sign Up
</Button>
```

### ✅ Requirement 11.4: Image Optimization
Images use lazy loading and responsive sizes.

**Next.js Image Component:**
```tsx
<Image
  src="/images/hero.png"
  alt="Dashboard preview"
  width={1366}
  height={709}
  priority // For above-fold images
  loading="lazy" // For below-fold images
  sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
  placeholder="blur"
  blurDataURL="data:image/..."
/>
```

## Component Checklist

### Hero Section
- ✅ Responsive padding: `py-12 sm:py-16 md:py-24 lg:py-32`
- ✅ Font sizes: `text-lg sm:text-2xl md:text-3xl`
- ✅ CTA buttons: `h-11 sm:h-12` (44px/48px)
- ✅ Full width buttons on mobile: `w-full sm:w-auto`
- ✅ Image with priority loading
- ✅ Proper spacing between elements

### Features Section
- ✅ Grid: `grid-cols-1 sm:grid-cols-2`
- ✅ Card padding: `p-6`
- ✅ Touch-friendly cards
- ✅ Proper text hierarchy
- ✅ Icon sizes: `h-6 w-6`

### Testimonials Section
- ✅ Grid: `grid-cols-1 md:grid-cols-3`
- ✅ Card spacing: `gap-8`
- ✅ Avatar size: `h-12 w-12` (48px)
- ✅ Readable quotes
- ✅ Metric badges

### Pricing Section
- ✅ Grid: `grid-cols-1 md:grid-cols-3`
- ✅ Full width cards on mobile
- ✅ CTA buttons: Full width on mobile
- ✅ Comparison table: Horizontal scroll
- ✅ Proper spacing

### Blog Components
- ✅ BlogCard: Responsive image
- ✅ Grid: `grid-cols-1 md:grid-cols-2 lg:grid-cols-3`
- ✅ Search bar: Full width on mobile
- ✅ Category filters: Wrap on mobile
- ✅ Pagination: Touch-friendly

### Security Section
- ✅ Badge grid: `grid-cols-2 md:grid-cols-4`
- ✅ Trust indicators: `grid-cols-1 md:grid-cols-2 lg:grid-cols-3`
- ✅ CTA buttons: Stacked on mobile
- ✅ Proper spacing

### CTA Sections
- ✅ Flex direction: `flex-col sm:flex-row`
- ✅ Full width buttons on mobile
- ✅ Proper spacing: `gap-3 sm:gap-4`
- ✅ Touch-friendly

## Common Patterns

### Responsive Container
```tsx
<div className="container px-4 sm:px-6 lg:px-8">
  {/* Content */}
</div>
```

### Responsive Grid
```tsx
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
  {/* Items */}
</div>
```

### Responsive Flex
```tsx
<div className="flex flex-col sm:flex-row gap-4">
  {/* Items */}
</div>
```

### Responsive Text
```tsx
<h2 className="text-2xl sm:text-3xl md:text-4xl">
  Heading
</h2>
<p className="text-base sm:text-lg">
  Body text
</p>
```

### Responsive Button
```tsx
<Button className="h-11 sm:h-12 w-full sm:w-auto px-6 sm:px-8">
  Click Me
</Button>
```

### Responsive Image
```tsx
<Image
  src="/image.jpg"
  alt="Description"
  width={800}
  height={600}
  className="w-full h-auto"
  sizes="(max-width: 768px) 100vw, 50vw"
  loading="lazy"
/>
```

## Testing Commands

### Start Development Server
```bash
cd apps/saas-landing
npm run dev
```

### Run Lighthouse Audit
```bash
# Install Lighthouse CLI
npm install -g lighthouse

# Run mobile audit
lighthouse http://localhost:3000 --preset=mobile --view
```

### Check Bundle Size
```bash
npm run build
npm run analyze
```

## Browser DevTools Testing

### Chrome DevTools
1. Open DevTools (F12)
2. Click device toolbar icon (Ctrl+Shift+M)
3. Select device or enter custom dimensions
4. Test at: 320px, 375px, 390px, 768px, 1024px

### Firefox Responsive Design Mode
1. Open DevTools (F12)
2. Click responsive design mode (Ctrl+Shift+M)
3. Select device or enter custom dimensions

### Safari Web Inspector
1. Open Web Inspector (Cmd+Option+I)
2. Enter Responsive Design Mode
3. Select device or enter custom dimensions

## Performance Targets

### Mobile Performance
- First Contentful Paint: < 1.8s
- Largest Contentful Paint: < 2.5s
- Time to Interactive: < 3.8s
- Total Load Time: < 3s (on 3G)

### Lighthouse Scores
- Performance: ≥ 90
- Accessibility: ≥ 95
- Best Practices: ≥ 95
- SEO: ≥ 95

## Common Issues & Solutions

### Issue: Text too small on mobile
**Solution:** Use `text-base` (16px) minimum for body text
```tsx
<p className="text-base">Content</p>
```

### Issue: Buttons too small to tap
**Solution:** Use `h-11` (44px) minimum height
```tsx
<Button className="h-11 sm:h-12">Click</Button>
```

### Issue: Horizontal scrolling
**Solution:** Use proper container and responsive classes
```tsx
<div className="container px-4 max-w-full overflow-hidden">
  {/* Content */}
</div>
```

### Issue: Images not loading efficiently
**Solution:** Use Next.js Image with proper sizes
```tsx
<Image
  src="/image.jpg"
  alt="Description"
  width={800}
  height={600}
  sizes="(max-width: 768px) 100vw, 50vw"
  loading="lazy"
/>
```

### Issue: Layout breaks on small screens
**Solution:** Use mobile-first responsive classes
```tsx
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
  {/* Items */}
</div>
```

## Accessibility Considerations

### Focus Indicators
All interactive elements have visible focus indicators:
```css
*:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}
```

### Touch Target Spacing
Minimum 8px spacing between touch targets:
```tsx
<div className="flex flex-col gap-3">
  <Button>Button 1</Button>
  <Button>Button 2</Button>
</div>
```

### Keyboard Navigation
All interactive elements are keyboard accessible with proper tab order.

## Resources

### Documentation
- [Tailwind CSS Responsive Design](https://tailwindcss.com/docs/responsive-design)
- [Next.js Image Optimization](https://nextjs.org/docs/basic-features/image-optimization)
- [Web Content Accessibility Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)

### Tools
- [Chrome DevTools](https://developer.chrome.com/docs/devtools/)
- [Lighthouse](https://developers.google.com/web/tools/lighthouse)
- [WebPageTest](https://www.webpagetest.org/)
- [BrowserStack](https://www.browserstack.com/)

### Testing
- [Mobile-Friendly Test](https://search.google.com/test/mobile-friendly)
- [PageSpeed Insights](https://pagespeed.web.dev/)
- [GTmetrix](https://gtmetrix.com/)

## Quick Verification

Run this checklist before deployment:

- [ ] All sections visible on 320px width
- [ ] Body text minimum 16px
- [ ] All buttons minimum 44px height
- [ ] Images use Next.js Image component
- [ ] Images have lazy loading
- [ ] Images have responsive sizes
- [ ] No horizontal scrolling on mobile
- [ ] Touch targets properly spaced
- [ ] Lighthouse mobile score ≥ 90
- [ ] Tested on real devices
- [ ] Keyboard navigation works
- [ ] Focus indicators visible
- [ ] Page loads < 3s on 3G

## Contact

For questions or issues, refer to:
- Design document: `.kiro/specs/saas-landing-content-customization/design.md`
- Requirements: `.kiro/specs/saas-landing-content-customization/requirements.md`
- Tasks: `.kiro/specs/saas-landing-content-customization/tasks.md`
