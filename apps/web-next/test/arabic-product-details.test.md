# Arabic Product Details Page Testing Guide

## Manual Testing Checklist

### 1. Navigation and Breadcrumbs
- [ ] Visit `/ar/products/[any-product-slug]`
- [ ] Verify breadcrumb shows "المنتجات" instead of "Products"
- [ ] Verify "العودة إلى المنتجات" button appears on error page

### 2. Product Information Display
- [ ] Product name displays in Arabic if `nameAr` field exists
- [ ] Product description shows Arabic content if `descriptionAr` exists
- [ ] Short description uses Arabic if `shortDescriptionAr` available
- [ ] Falls back to English content if Arabic versions not available

### 3. UI Elements Translation
- [ ] Quantity label shows "الكمية"
- [ ] "Add to Cart" button shows "أضف إلى السلة"
- [ ] "Buy Now" button shows "اشتر الآن"
- [ ] "Share" button shows "مشاركة"
- [ ] Loading state shows "جاري الإضافة…"
- [ ] Success state shows "تمت الإضافة إلى السلة"

### 4. Trust Badges
- [ ] "Secure Payment" shows "دفع آمن"
- [ ] "Fast Shipping" shows "شحن سريع"
- [ ] "Quality Guarantee" shows "ضمان الجودة"
- [ ] "24/7 Support" shows "دعم على مدار الساعة"

### 5. Product Details Section
- [ ] Section title shows "تفاصيل المنتج"
- [ ] Discount message shows "توفر [amount] [currency]"

### 6. Error Handling
- [ ] Product not found error shows "المنتج غير موجود"
- [ ] Error page title shows Arabic text
- [ ] Back button shows "العودة إلى المنتجات"

### 7. RTL Layout
- [ ] Text flows right-to-left correctly
- [ ] Icons and buttons align properly in RTL
- [ ] Spacing and margins work correctly
- [ ] No layout breaking or overlapping elements

### 8. Currency and Numbers
- [ ] Currency symbols display correctly
- [ ] Price formatting works with Arabic locale
- [ ] Discount percentages show properly

## Test Data Requirements

### Sample Product with Arabic Content
```json
{
  "id": 1,
  "name": "Premium Dental Kit",
  "nameAr": "طقم أسنان متميز",
  "description": "Complete dental care kit with advanced tools",
  "descriptionAr": "طقم رعاية أسنان كامل مع أدوات متقدمة",
  "shortDescription": "Professional dental care",
  "shortDescriptionAr": "رعاية أسنان احترافية",
  "price": 150.00,
  "compareAtPrice": 200.00,
  "currency": "USD"
}
```

## Browser Testing

### Desktop Browsers
- [ ] Chrome - Arabic text rendering and RTL layout
- [ ] Firefox - Font support and text direction
- [ ] Safari - Unicode display and layout
- [ ] Edge - RTL support and translations

### Mobile Browsers
- [ ] Mobile Chrome - Responsive Arabic layout
- [ ] Mobile Safari - Touch interactions with RTL
- [ ] Mobile Firefox - Text input and display

## Accessibility Testing

### Screen Reader Support
- [ ] Arabic content read correctly by screen readers
- [ ] Button labels announced in Arabic
- [ ] Navigation structure clear in Arabic
- [ ] Form labels properly associated

### Keyboard Navigation
- [ ] Tab order works correctly in RTL layout
- [ ] Focus indicators visible and positioned correctly
- [ ] Keyboard shortcuts work with Arabic interface

## Performance Testing

### Loading Times
- [ ] Arabic fonts load efficiently
- [ ] No layout shift when switching languages
- [ ] Translation loading doesn't block rendering

### Memory Usage
- [ ] No memory leaks with language switching
- [ ] Efficient translation caching
- [ ] Proper cleanup of unused translations

## Cross-Language Testing

### Language Switching
- [ ] Switch from English to Arabic maintains product context
- [ ] URL structure correct for both languages
- [ ] No broken links when switching languages
- [ ] State preservation across language changes

### Content Consistency
- [ ] Same product information in both languages
- [ ] Pricing consistent across languages
- [ ] Images and media display correctly in both versions

## Error Scenarios

### Missing Translations
- [ ] Graceful fallback to English when Arabic missing
- [ ] No broken UI elements with missing translations
- [ ] Console warnings for missing translation keys

### Network Issues
- [ ] Proper error messages in Arabic for network failures
- [ ] Retry mechanisms work with Arabic interface
- [ ] Offline state displays Arabic messages

## Reporting Issues

### Bug Report Template
```
**Language**: Arabic (ar)
**Page**: Product Details (/ar/products/[slug])
**Browser**: [Browser name and version]
**Issue**: [Description of the problem]
**Expected**: [What should happen]
**Actual**: [What actually happens]
**Steps to Reproduce**:
1. [Step 1]
2. [Step 2]
3. [Step 3]
```

### Common Issues to Watch For
- Text overflow in Arabic due to longer translations
- RTL layout breaking with certain CSS properties
- Font rendering issues with Arabic characters
- Incorrect text direction in form inputs
- Missing or incorrect translations
- Currency formatting problems

## Success Criteria

### Functional Requirements
- ✅ All UI elements display in Arabic
- ✅ Product content shows Arabic versions when available
- ✅ RTL layout works correctly
- ✅ Navigation and interactions function properly

### User Experience
- ✅ Natural reading flow for Arabic users
- ✅ Consistent visual hierarchy
- ✅ Intuitive interface in Arabic context
- ✅ Fast and responsive interactions

### Technical Requirements
- ✅ No console errors or warnings
- ✅ Proper SEO meta tags for Arabic content
- ✅ Accessibility standards met
- ✅ Performance benchmarks achieved