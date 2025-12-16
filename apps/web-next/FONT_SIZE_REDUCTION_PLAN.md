# Font Size Reduction Plan for web-next

## Current Issues Identified
- Hero titles are using text-4xl/5xl (36px/48px) which are too large
- Product titles using text-4xl/5xl (36px/48px) 
- Section headings using text-3xl/4xl/5xl (30px/36px/48px)
- Price displays using text-3xl/4xl (30px/36px)
- Many components using text-2xl (24px) for regular content

## Recommended Font Size Reductions

### Hero Section
- **Current**: text-3xl md:text-4xl lg:text-5xl (24px/36px/48px)
- **New**: text-2xl md:text-3xl lg:text-4xl (20px/30px/36px)

### Section Headings (H2)
- **Current**: text-3xl md:text-4xl/5xl (30px/36px/48px)
- **New**: text-2xl md:text-3xl (20px/30px)

### Product Titles
- **Current**: text-4xl lg:text-5xl (36px/48px)
- **New**: text-2xl lg:text-3xl (20px/30px)

### Price Displays
- **Current**: text-3xl/4xl (30px/36px)
- **New**: text-xl/2xl (20px/24px)

### Card Titles
- **Current**: text-xl/2xl (20px/24px)
- **New**: text-lg/xl (18px/20px)

### Navigation and UI Elements
- Keep current sizes as they are appropriate (text-sm, text-base)

## Files to Update
1. HomePageClient.tsx - Hero and section headings
2. ProductInfo.tsx - Product titles and prices
3. ProductCard.tsx - Product card titles
4. CarouselRail.tsx - Section titles
5. BookingSlider.tsx - Modal titles
6. Header components - Logo text
7. Footer.tsx - Brand text
8. Various checkout and form components

## Implementation Strategy
1. Start with the most prominent elements (hero, main headings)
2. Update product-related components
3. Review and adjust other components
4. Test responsiveness across devices
5. Ensure accessibility is maintained