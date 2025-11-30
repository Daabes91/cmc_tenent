# Animation Infrastructure

This document describes the animation infrastructure set up for the Vireo landing page redesign.

## Overview

The animation system provides:
- Custom Tailwind CSS animations (slide-right, expand, marquee)
- Intersection Observer hook for scroll-triggered animations
- Animation utility functions and classes
- Reduced motion support for accessibility

## Files

### 1. Tailwind Configuration (`tailwind.config.ts`)

Custom animations added:
- `slide-right`: Slides element from left with fade-in (0.9s ease-out)
- `expand`: Scales element from 0.8 to 1.0 with fade-in (0.9s ease-out)
- `marquee`: Infinite horizontal scroll for logo carousel (30s linear)

Custom utilities:
- `animation-delay-{value}`: Delays animation start (0, 400, 800, 1000, 1200, 1500ms)
- `duration-900`: 900ms transition duration

### 2. Intersection Observer Hook (`hooks/use-intersection-animation.tsx`)

**Usage:**
```tsx
import { useIntersectionAnimation } from "@/hooks/use-intersection-animation"

function MyComponent() {
  const [ref, isVisible] = useIntersectionAnimation({
    threshold: 0.1,
    rootMargin: "50px",
    triggerOnce: true,
  })

  return (
    <div 
      ref={ref}
      className={isVisible ? "animate-slide-right" : "opacity-0"}
    >
      Content
    </div>
  )
}
```

**Options:**
- `threshold`: Percentage of element visibility to trigger (default: 0.1)
- `rootMargin`: Margin around viewport for early triggering (default: "50px")
- `triggerOnce`: Only trigger animation once (default: true)

**Features:**
- Automatic fallback for browsers without Intersection Observer support
- Respects `prefers-reduced-motion` user preference
- TypeScript support with generic element types

### 3. Animation Utilities (`lib/animation-utils.ts`)

**Constants:**
```typescript
ANIMATION_DELAYS = {
  none: 0,
  short: 400,
  medium: 800,
  long: 1200,
  extraLong: 1500,
  trust: 1000,
}

ANIMATION_DURATIONS = {
  fast: 300,
  normal: 600,
  slow: 900,
  marquee: 30000,
}
```

**Functions:**

`getAnimationClasses(animationType, delay, isVisible)`
- Combines animation classes based on visibility state
- Returns: Combined class string

`prefersReducedMotion()`
- Checks if user prefers reduced motion
- Returns: boolean

`supportsIntersectionObserver()`
- Checks browser support for Intersection Observer
- Returns: boolean

`getStaggeredDelay(index, baseDelay, staggerAmount)`
- Calculates staggered delays for multiple elements
- Returns: delay in milliseconds

`getAnimationDelayStyle(delay)`
- Creates inline style object for animation delay
- Returns: React.CSSProperties

`createStaggeredAnimation(count, baseDelay, staggerAmount)`
- Creates array of delays for staggered animations
- Returns: number[]

### 4. Animation Configuration (`lib/animations.ts`)

Central export file for all animation-related utilities.

**Exports:**
- All utilities from `animation-utils.ts`
- `ANIMATION_CLASSES`: Predefined animation class names
- `ANIMATION_TIMING`: Timing function constants
- `REDUCED_MOTION_QUERY`: Media query string
- `INTERSECTION_CONFIG`: Preset configurations

## Usage Examples

### Basic Scroll Animation

```tsx
import { useIntersectionAnimation } from "@/hooks/use-intersection-animation"

function HeroSection() {
  const [ref, isVisible] = useIntersectionAnimation()

  return (
    <div 
      ref={ref}
      className={`transition-all duration-900 ${
        isVisible ? "animate-slide-right opacity-100" : "opacity-0"
      }`}
    >
      <h1>Welcome</h1>
    </div>
  )
}
```

### Staggered Animations

```tsx
import { useIntersectionAnimation } from "@/hooks/use-intersection-animation"
import { getStaggeredDelay } from "@/lib/animations"

function FeatureList() {
  const [ref, isVisible] = useIntersectionAnimation()
  const features = ["Feature 1", "Feature 2", "Feature 3"]

  return (
    <div ref={ref}>
      {features.map((feature, index) => (
        <div
          key={feature}
          className={isVisible ? "animate-slide-right" : "opacity-0"}
          style={{ animationDelay: `${getStaggeredDelay(index)}ms` }}
        >
          {feature}
        </div>
      ))}
    </div>
  )
}
```

### Marquee Animation

```tsx
function LogoMarquee() {
  return (
    <div className="overflow-hidden">
      <div className="flex gap-8 animate-marquee">
        {logos.map((logo, i) => (
          <img key={i} src={logo} alt="" />
        ))}
      </div>
    </div>
  )
}
```

### With Reduced Motion Support

```tsx
import { useIntersectionAnimation } from "@/hooks/use-intersection-animation"
import { prefersReducedMotion } from "@/lib/animations"

function AnimatedComponent() {
  const [ref, isVisible] = useIntersectionAnimation()
  const shouldAnimate = !prefersReducedMotion()

  return (
    <div 
      ref={ref}
      className={`transition-all ${
        shouldAnimate && isVisible ? "animate-slide-right" : ""
      } ${isVisible ? "opacity-100" : "opacity-0"}`}
    >
      Content
    </div>
  )
}
```

## Accessibility

The animation system includes built-in accessibility features:

1. **Reduced Motion Support**: Automatically detects and respects `prefers-reduced-motion` setting
2. **Fallback Support**: Gracefully handles browsers without Intersection Observer
3. **Semantic HTML**: Animations don't interfere with screen readers
4. **Performance**: Uses GPU-accelerated CSS transforms

## Performance Considerations

- All animations use CSS transforms (GPU-accelerated)
- Intersection Observer reduces unnecessary calculations
- `triggerOnce` option prevents repeated animations
- Animations automatically disabled for users who prefer reduced motion

## Browser Support

- Modern browsers: Full support
- Older browsers: Graceful degradation (elements appear immediately)
- Reduced motion: Respected across all browsers

## Requirements Satisfied

This implementation satisfies the following requirements from the spec:
- **2.1**: Scroll-triggered animations with staggered delays
- **2.4**: Expand effect and opacity fade-in for hero image
- **5.5**: Reduced motion support and smooth 60fps animations
