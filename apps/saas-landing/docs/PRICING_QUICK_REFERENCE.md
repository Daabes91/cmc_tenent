# Pricing Section - Quick Reference

## 📍 Location
- **Component:** `apps/saas-landing/components/pricing.tsx`
- **Content Source:** `apps/saas-landing/lib/content/healthcare-copy.ts`
- **Section ID:** `#pricing`

## 🎯 Key Features

### Pricing Tiers
1. **Solo Practice** - $49/month ($470/year)
   - 1 provider, 100 patients, 500 appointments/month
   
2. **Small Clinic** ⭐ - $149/month ($1,430/year)
   - 5 providers, 500 patients, 2,000 appointments/month
   
3. **Multi-Location** - $399/month ($3,830/year)
   - Unlimited providers, patients, appointments

### Display Elements
- ✅ Monthly and annual pricing
- ✅ Healthcare-specific limits (providers, patients, appointments)
- ✅ Feature lists with checkmarks
- ✅ Comparison table
- ✅ Bilingual support (EN/AR)
- ✅ Responsive design

## 🔧 How to Update

### Update Pricing
Edit `apps/saas-landing/lib/content/healthcare-copy.ts`:

```typescript
pricing: {
  tiers: [
    {
      name: "Solo Practice",
      price: { monthly: 49, annual: 470 },
      limits: { providers: 1, patients: 100, appointments: 500 },
      features: [...],
      cta: "Start Free Trial"
    }
  ]
}
```

### Update Labels
Edit the `copy` object in `components/pricing.tsx`:

```typescript
const copy = {
  en: {
    monthLabel: "per month",
    annualLabel: "per year",
    // ... other labels
  }
}
```

## 📊 Healthcare Terminology

### ✅ Use These Terms:
- Providers (not users/staff)
- Patients (not customers/clients)
- Appointments (not bookings/meetings)
- Clinic/Practice (not workspace/organization)

### ❌ Avoid These Terms:
- Users, Workspace, Projects, Customers, Bookings

## 🧪 Testing

### Quick Test Checklist:
- [ ] Three tiers displayed correctly
- [ ] Monthly and annual prices shown
- [ ] Healthcare limits visible
- [ ] Features use healthcare terminology
- [ ] Comparison table works
- [ ] Arabic translation works
- [ ] Mobile responsive
- [ ] CTA buttons work

### Test File:
`apps/saas-landing/test/pricing-healthcare.test.md`

## 📚 Documentation

- **Implementation Summary:** `docs/TASK_5_PRICING_SECTION_UPDATE_SUMMARY.md`
- **Content Preview:** `docs/PRICING_CONTENT_PREVIEW.md`
- **Test Guide:** `test/pricing-healthcare.test.md`

## 🎨 Design Specs

### Pricing Cards:
- 3-column grid (desktop) / stacked (mobile)
- White background with backdrop blur
- Popular badge on Small Clinic tier
- Healthcare limits in 3-column grid
- Features with checkmark icons
- Full-width CTA buttons

### Comparison Table:
- Side-by-side tier comparison
- Limits and features rows
- Checkmarks for included features
- Responsive horizontal scroll

## 🌐 Bilingual Support

### English:
- Title: "Flexible pricing for every clinic phase"
- Subtitle: "Licensing is per clinic location..."

### Arabic:
- Title: "أسعار مرنة لكل مرحلة من مراحل العيادة"
- Subtitle: "الترخيص لكل موقع عيادة..."

## 🔗 Related Components

- Hero Section: `components/hero.tsx`
- Features Section: `components/features.tsx`
- Testimonials: `components/testimonials.tsx`
- Content Types: `lib/content/types.ts`

## 💡 Tips

1. **Updating Prices:** Change values in `healthcare-copy.ts`, not in the component
2. **Adding Features:** Add to the `features` array in the tier configuration
3. **Changing Limits:** Update the `limits` object for each tier
4. **Translations:** Update both `en` and `ar` sections in healthcare-copy.ts
5. **Testing:** Always test both languages and mobile responsiveness

## 🚀 Quick Commands

```bash
# Navigate to saas-landing
cd apps/saas-landing

# Run development server
npm run dev

# Build for production
npm run build

# Run linter
npm run lint
```

## 📞 Support

For questions or issues:
1. Check the implementation summary
2. Review the test guide
3. Consult the content preview
4. Verify healthcare-copy.ts configuration
