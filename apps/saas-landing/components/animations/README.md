# Animation Components

This directory contains reusable animation components for the Vireo landing page redesign.

## Components

### Card3D

A component that applies a 3D perspective transform effect based on mouse position.

**Features:**
- Mouse-tracking 3D rotation
- Smooth transitions with cubic-bezier easing
- Subtle scale effect on hover
- Resets to default state on mouse leave

**Usage:**
```tsx
import { Card3D } from '@/components/animations';

<Card3D className="rounded-lg border shadow-lg">
  <img src="/hero-image.png" alt="Hero" />
</Card3D>
```

**Props:**
- `children`: React.ReactNode - Content to be wrapped
- `className`: string (optional) - Additional CSS classes

---

### LogoMarquee

An infinite scrolling marquee component for displaying company logos.

**Features:**
- Seamless infinite loop animation
- Hover to pause functionality
- Grayscale filter with hover effect
- Configurable speed

**Usage:**
```tsx
import { LogoMarquee } from '@/components/animations';

const logos = ['dailydev', 'ycombinator', 'bestofjs', 'product-hunt'];

<LogoMarquee logos={logos} speed={30} />
```

**Props:**
- `logos`: string[] - Array of logo filenames (without extension)
- `speed`: number (optional, default: 30) - Animation duration in seconds
- `className`: string (optional) - Additional CSS classes

**Note:** Logo images should be placed in `/public/logos/` directory as PNG files.

---

### AnimatedBackground

A decorative background component with animated SVG waves and floating blur effects.

**Features:**
- Three-layer SVG wave animation
- Gradient overlays
- Floating blur orbs
- Optimized for performance

**Usage:**
```tsx
import { AnimatedBackground } from '@/components/animations';

<section className="relative">
  <AnimatedBackground />
  <div className="relative z-10">
    {/* Your content here */}
  </div>
</section>
```

**Props:**
- `className`: string (optional) - Additional CSS classes

**Note:** This component uses absolute positioning and should be placed inside a relative container.

---

## Tailwind Configuration

The following animations are configured in `tailwind.config.ts`:

### Keyframes:
- `wave-slow`, `wave-medium`, `wave-fast` - Wave animations
- `float-slow`, `float-medium`, `float-fast` - Floating blur animations
- `marquee` - Infinite scroll animation

### Animation Classes:
- `animate-wave-slow` - 8s ease-in-out infinite
- `animate-wave-medium` - 6s ease-in-out infinite
- `animate-wave-fast` - 4s ease-in-out infinite
- `animate-float-slow` - 10s ease-in-out infinite
- `animate-float-medium` - 8s ease-in-out infinite
- `animate-float-fast` - 6s ease-in-out infinite
- `animate-marquee` - 30s linear infinite

## Requirements Satisfied

- **Requirement 2.2**: 3D card effect with mouse-tracking
- **Requirement 2.3**: Animated background with SVG waves
- **Requirement 2.5**: Logo marquee with infinite scroll
- **Requirement 3.3**: Trust section animations
