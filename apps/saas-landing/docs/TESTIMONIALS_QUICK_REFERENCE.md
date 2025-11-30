# Healthcare Testimonials - Quick Reference

## Overview

Healthcare-specific testimonials showcasing real results from diverse clinic types and sizes.

## Location

- **Component**: `apps/saas-landing/components/testimonials.tsx`
- **Data Source**: `apps/saas-landing/lib/content/healthcare-copy.ts`
- **Images**: `apps/saas-landing/public/images/testimonials/`

## Data Structure

```typescript
{
  name: string;           // Full name with title (e.g., "Dr. Sarah Johnson")
  role: string;           // Position (e.g., "Clinic Director")
  clinicType: string;     // Practice type (e.g., "Family Medicine Practice")
  quote: string;          // Testimonial text
  metric?: string;        // Optional quantifiable result
  avatar?: string;        // Optional image path
}
```

## Current Testimonials (5)

| Name | Role | Clinic Type | Metric |
|------|------|-------------|--------|
| Dr. Sarah Johnson | Clinic Director | Family Medicine Practice | 80% reduction in scheduling conflicts |
| Michael Chen | Practice Manager | Dental Clinic | 15 hours saved per week |
| Dr. Aisha Al-Rashid | Owner | Physical Therapy Center | 35% increase in patient satisfaction |
| Dr. Layla Qadri | Medical Director | Qadri Dental Group | 28% reduction in no-shows |
| Sameer Haddad | COO | Shifa Health Network | 11 locations managed seamlessly |

## Key Features

✅ All required fields displayed (name, role, clinic type, quote)  
✅ Metric badges with visual indicators  
✅ Responsive 3-column grid layout  
✅ Dark mode support  
✅ Hover effects and transitions  
✅ Fallback avatar images  
✅ Bilingual content support (EN/AR)  

## Adding New Testimonials

1. Edit `lib/content/healthcare-copy.ts`
2. Add testimonial object to `testimonials.items` array
3. Add avatar image to `public/images/testimonials/`
4. Update image path in testimonial data

## Customization

### Change Title/Subtitle
Edit in `healthcare-copy.ts`:
```typescript
testimonials: {
  title: "Your custom title",
  subtitle: "Your custom subtitle",
  items: [...]
}
```

### Modify Layout
Edit `components/testimonials.tsx`:
- Grid columns: `md:grid-cols-3` (change number)
- Card styling: Tailwind classes in card div
- Spacing: `gap-8` and padding classes

### Update Colors
- Metric badge: `bg-primary/10 text-primary`
- Card border: `border-slate-200 dark:border-gray-800`
- Text colors: `text-slate-700 dark:text-gray-300`

## Image Requirements

- **Format**: JPG or WebP
- **Size**: 400x400px (square)
- **File Size**: < 100KB
- **Quality**: Professional headshots
- **Background**: Neutral/clean

## Fallback Behavior

If avatar image is missing, component uses Unsplash placeholder:
```
https://images.unsplash.com/photo-1472099645785-5658abf4ff4e
```

## Responsive Breakpoints

- **Mobile** (< 768px): 1 column
- **Tablet** (768px - 1024px): 2 columns
- **Desktop** (> 1024px): 3 columns

## Accessibility

- Semantic HTML structure
- Alt text for images
- WCAG AA color contrast
- Keyboard navigation
- Screen reader support

## Testing Commands

```bash
# Type check
npm run type-check --prefix apps/saas-landing

# Build
npm run build --prefix apps/saas-landing

# Dev server
npm run dev --prefix apps/saas-landing
```

## Common Issues

### Images Not Loading
- Check file path matches configuration
- Verify image exists in `/public/images/testimonials/`
- Check file permissions

### Layout Issues
- Clear Next.js cache: `rm -rf .next`
- Rebuild: `npm run build`
- Check Tailwind classes are valid

### Content Not Updating
- Restart dev server
- Clear browser cache
- Check correct language is being used

## Related Files

- `lib/content/types.ts` - TypeScript interfaces
- `lib/content/examples.ts` - Usage examples
- `app/page.tsx` - Main landing page
- `docs/TASK_4_TESTIMONIALS_IMPLEMENTATION_SUMMARY.md` - Full documentation

## Support

For questions or issues:
1. Check implementation summary document
2. Review content preview document
3. Verify data structure in types.ts
4. Test with different viewport sizes
