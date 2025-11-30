# Integrations Section - Quick Reference

## Overview
Healthcare-focused integrations section showcasing tools that connect with clinic operations.

## Current Integrations

| Integration | Category | Description Focus |
|------------|----------|-------------------|
| **Stripe** | Payment | Patient payments, clinic billing |
| **PayPal** | Payment | Clinic payments, treatment plan subscriptions |
| **Google Calendar** | Calendar | Appointment schedules, provider coordination |
| **Mailchimp** | Email | Appointment reminders, patient health tips |
| **QuickBooks** | Accounting | Clinic revenue/expenses, practice accounting |
| **Twilio** | Communication | SMS appointment reminders, patient notifications |

## Key Features
- ✅ All descriptions mention clinic operations
- ✅ Bilingual support (English/Arabic)
- ✅ Responsive grid layout (2/3/5 columns)
- ✅ Dark mode support
- ✅ Analytics tracking on CTA
- ✅ Smooth animations
- ✅ Custom SVG logos

## File Locations
- **Content**: `lib/content/healthcare-copy.ts` → `integrations` section
- **Component**: `components/integrations.tsx`
- **Logos**: `public/images/integrations/*.svg`
- **Page**: Used in `app/page.tsx` (lazy loaded)

## Quick Edits

### Add New Integration
```typescript
// In lib/content/healthcare-copy.ts
{
  name: "New Tool",
  description: "How it helps clinic operations",
  logo: "/images/integrations/new-tool.svg",
  category: "payment" | "calendar" | "email" | "accounting" | "other"
}
```

### Update CTA Text
```typescript
// In lib/content/healthcare-copy.ts
integrations: {
  ctaText: "Your New CTA Text"
}
```

### Change CTA Destination
```bash
# In .env.local
NEXT_PUBLIC_API_DOCS_URL=https://your-docs-url.com
```

## Testing Commands
```bash
# Development
npm run dev

# Production build
npm run build

# Type check
npm run type-check
```

## Requirements Coverage
- ✅ 9.1: Display integrations section
- ✅ 9.2: List relevant integrations (payment, calendar, email, accounting)
- ✅ 9.3: Replace generic logos with healthcare tools
- ✅ 9.4: Descriptions mention clinic operations
- ✅ 9.5: Include "View All Integrations" CTA

## Common Tasks

### Update Integration Description
1. Open `lib/content/healthcare-copy.ts`
2. Find integration in `integrations.items` array
3. Update `description` field
4. Ensure it mentions clinic operations
5. Update Arabic translation if needed

### Add Integration Logo
1. Create SVG file in `public/images/integrations/`
2. Use `currentColor` for fill (dark mode support)
3. Keep dimensions 24x24 viewBox
4. Update logo path in healthcare-copy.ts

### Change Grid Layout
Edit `components/integrations.tsx`:
```tsx
// Current: 2/3/5 columns
className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-6"

// Example: 3/4/6 columns
className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 gap-6"
```

## Analytics Events
CTA clicks are tracked with:
- **Event Name**: CTA text (e.g., "View All Integrations")
- **Section**: "integrations_section"
- **Destination**: API_DOCS_URL value

## Accessibility
- Semantic HTML structure
- ARIA labels on interactive elements
- Keyboard navigation support
- Screen reader friendly
- Focus indicators on CTA button

## Performance
- Lazy loaded on landing page
- Optimized SVG logos
- Framer Motion animations with viewport detection
- No external image dependencies
