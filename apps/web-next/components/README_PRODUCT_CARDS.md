# Modern Product Cards

A redesigned product card system for the web-next application featuring modern aesthetics, smooth animations, and improved user experience.

## Components

### ProductCard
The main product card component with three variants:

- **Default**: Standard card with balanced proportions
- **Compact**: Smaller card for dense layouts
- **Featured**: Larger card for highlighting products

### ProductGrid
A responsive grid layout component for displaying multiple product cards.

## Features

### 🎨 Modern Design
- Clean, minimalist aesthetic
- Rounded corners and subtle shadows
- Improved typography hierarchy
- Better color contrast

### ✨ Smooth Animations
- Hover effects with scale and translate transforms
- Smooth transitions for all interactive elements
- Loading animations for better perceived performance
- Active states for button interactions

### 📱 Responsive Design
- Works perfectly on all screen sizes
- Flexible grid layouts (2, 3, or 4 columns)
- Touch-friendly button sizes
- Optimized image loading

### 🌙 Dark Mode Support
- Beautiful appearance in both light and dark themes
- Proper contrast ratios maintained
- Consistent visual hierarchy

### 🔄 Loading States
- Skeleton loading animations
- Empty state handling
- Error state management

### 🛒 Enhanced UX
- Clear call-to-action buttons
- Visual feedback for cart actions
- Sale badges for discounted items
- Improved price display

## Usage

### Basic ProductCard
```tsx
import { ProductCard } from '@/components/ProductCard';

<ProductCard 
  item={productItem} 
  locale="en" 
  variant="default" 
/>
```

### ProductGrid
```tsx
import { ProductGrid } from '@/components/ProductGrid';

<ProductGrid 
  products={products} 
  locale="en" 
  variant="default" 
  columns={3}
  loading={false}
/>
```

## Variants

### Default
- Height: 64 (16rem)
- Best for: General product listings
- Use case: Main product pages, search results

### Compact
- Height: 48 (12rem)
- Best for: Dense layouts, sidebar recommendations
- Use case: Related products, quick browse sections

### Featured
- Height: 72 (18rem)
- Best for: Hero sections, promotional content
- Use case: Homepage highlights, special offers

## Props

### ProductCard Props
```tsx
type ProductCardProps = {
  item: PublicCarouselItem;
  locale: string;
  variant?: 'default' | 'compact' | 'featured';
};
```

### ProductGrid Props
```tsx
type ProductGridProps = {
  products: PublicCarouselItem[];
  locale: string;
  variant?: 'default' | 'compact' | 'featured';
  columns?: 2 | 3 | 4;
  loading?: boolean;
};
```

## Styling

The components use Tailwind CSS with custom design tokens:

- **Colors**: Slate color palette for neutrals
- **Spacing**: Consistent padding and margins
- **Typography**: Clear hierarchy with proper font weights
- **Shadows**: Subtle elevation effects
- **Borders**: Rounded corners with consistent radii

## Accessibility

- Proper ARIA labels for screen readers
- Keyboard navigation support
- High contrast ratios
- Focus indicators
- Semantic HTML structure

## Performance

- Optimized image loading with Next.js Image component
- Efficient re-renders with proper memoization
- Smooth animations using CSS transforms
- Lazy loading support

## Demo

Visit `/products/demo` to see all variants in action with interactive examples.

## Migration from Old Cards

The new ProductCard component is a drop-in replacement for the old implementation in CarouselRail.tsx. Key improvements:

1. **Better Visual Hierarchy**: Clearer separation between elements
2. **Enhanced Interactions**: Smoother hover effects and button states
3. **Improved Accessibility**: Better screen reader support
4. **Modern Aesthetics**: Updated design language
5. **Flexible Variants**: Multiple layouts for different use cases

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

All modern browsers with CSS Grid and Flexbox support.