# Healthcare Content Configuration System

This directory contains the centralized content management system for the SaaS landing page, with healthcare-specific terminology and bilingual support (English and Arabic).

## Overview

The content configuration system separates content from presentation, making it easy to update healthcare-specific messaging, testimonials, features, and pricing without modifying component code.

## Structure

```
lib/content/
├── types.ts              # TypeScript interfaces for all content types
├── healthcare-copy.ts    # Healthcare-specific content in English and Arabic
├── index.ts             # Exports and utility functions
└── README.md            # This file
```

## Usage

### Basic Usage

```typescript
import { healthcareCopy } from '@/lib/content';

// Access English content
const heroContent = healthcareCopy.en.hero;
console.log(heroContent.headline.prefix); // "Your All-in-One Platform to Run Your Clinic"

// Access Arabic content
const heroContentAr = healthcareCopy.ar.hero;
console.log(heroContentAr.headline.prefix); // "منصتك الشاملة لإدارة عيادتك"
```

### With Language Context

```typescript
import { useHealthcareCopy } from '@/lib/content';
import { useLanguage } from '@/contexts/LanguageContext';

function MyComponent() {
  const { language } = useLanguage();
  const copy = useHealthcareCopy(language);
  
  return (
    <div>
      <h1>{copy.hero.headline.prefix}</h1>
      <p>{copy.hero.description}</p>
    </div>
  );
}
```

### Get Specific Section

```typescript
import { getHealthcareSection } from '@/lib/content';

const features = getHealthcareSection('en', 'features');
const testimonials = getHealthcareSection('ar', 'testimonials');
```

## Content Sections

### Hero Section
- Badge text
- Headline (prefix and highlight)
- Description
- CTA buttons (primary and secondary)
- Trust label and company logos

### Features Section
- Section title and subtitle
- Feature items with:
  - Icon name
  - Title
  - Description
  - Benefits list

### Testimonials Section
- Section title and subtitle
- Testimonial items with:
  - Name, role, and clinic type
  - Quote
  - Metric (optional)
  - Avatar image path (optional)

### Pricing Section
- Section title and subtitle
- Pricing tiers with:
  - Name and description
  - Monthly and annual pricing
  - Features list
  - Limits (providers, patients, appointments)
  - CTA text
  - Popular flag (optional)

### Integrations Section
- Section title and subtitle
- Integration items with:
  - Name and description
  - Logo path
  - Category (payment, calendar, email, accounting, other)
- CTA text

### Security Section
- Title and description
- Compliance badges
- Trust indicators

## Healthcare Terminology

The content system ensures consistent use of healthcare-specific terminology:

- **Patients** (not users or customers)
- **Appointments** (not bookings or meetings)
- **Providers** (not staff or employees)
- **Clinic** (not workspace or organization)
- **Treatment** (not service or project)

## Bilingual Support

All content is available in both English and Arabic:

- English (`en`): Left-to-right (LTR) layout
- Arabic (`ar`): Right-to-left (RTL) layout

The language context automatically handles text direction and content selection.

## Updating Content

To update content:

1. Open `healthcare-copy.ts`
2. Locate the section you want to update
3. Modify the content for both `en` and `ar` languages
4. Save the file - no component changes needed!

## Type Safety

All content is fully typed using TypeScript interfaces defined in `types.ts`. This ensures:

- Autocomplete in your IDE
- Compile-time error checking
- Consistent structure across languages
- Easy refactoring

## Best Practices

1. **Always update both languages** when changing content
2. **Use healthcare terminology** consistently
3. **Keep descriptions concise** and focused on benefits
4. **Include metrics** in testimonials when possible
5. **Test RTL layout** when updating Arabic content
6. **Validate image paths** before committing

## Examples

### Adding a New Feature

```typescript
// In healthcare-copy.ts, add to features.items array:
{
  icon: "Stethoscope",
  title: "Clinical Documentation",
  description: "Streamline clinical notes and documentation",
  benefits: [
    "Template-based notes",
    "Voice-to-text dictation",
    "ICD-10 code lookup",
    "E-signature support"
  ]
}
```

### Adding a New Testimonial

```typescript
// In healthcare-copy.ts, add to testimonials.items array:
{
  name: "Dr. John Smith",
  role: "Chief Medical Officer",
  clinicType: "Orthopedic Surgery Center",
  quote: "The platform has revolutionized our patient intake process...",
  metric: "50% faster patient onboarding",
  avatar: "/images/testimonials/dr-john-smith.jpg"
}
```

### Updating Pricing

```typescript
// In healthcare-copy.ts, modify pricing.tiers:
{
  name: "Solo Practice",
  price: {
    monthly: 59,  // Updated from 49
    annual: 590   // Updated from 470
  },
  // ... rest of the tier configuration
}
```

## Validation

The TypeScript compiler will catch:
- Missing required fields
- Incorrect data types
- Typos in property names
- Missing translations

Run `npm run type-check` to validate all content.

## Future Enhancements

Potential improvements to the content system:

- [ ] CMS integration for non-technical content updates
- [ ] Content versioning and rollback
- [ ] A/B testing support
- [ ] Additional language support (French, Spanish, etc.)
- [ ] Content validation rules
- [ ] Automated translation workflow
