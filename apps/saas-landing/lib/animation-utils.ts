import { cn } from "./utils"
import { 
  prefersReducedMotion as checkReducedMotion,
  supportsIntersectionObserver as checkIntersectionObserver,
  supportsCSSAnimations,
} from "./browser-compat"

/**
 * Animation delay values in milliseconds
 */
export const ANIMATION_DELAYS = {
  none: 0,
  short: 400,
  medium: 800,
  long: 1200,
  extraLong: 1500,
  trust: 1000,
} as const

/**
 * Animation duration values in milliseconds
 */
export const ANIMATION_DURATIONS = {
  fast: 300,
  normal: 600,
  slow: 900,
  marquee: 30000,
} as const

/**
 * Get animation class names for intersection-triggered animations
 * @param animationType - Type of animation (slide-right, expand, etc.)
 * @param delay - Delay in milliseconds before animation starts
 * @param isVisible - Whether the element is visible (from intersection observer)
 * @returns Combined class names for the animation
 */
export function getAnimationClasses(
  animationType: "slide-right" | "expand",
  delay: number = 0,
  isVisible: boolean = false
): string {
  // Check if animations are supported and user doesn't prefer reduced motion
  const shouldAnimate = supportsCSSAnimations() && !checkReducedMotion()
  
  const baseClasses = "transition-all duration-900"
  const animationClass = shouldAnimate ? `animate-${animationType}` : ""
  const delayClass = delay > 0 && shouldAnimate ? `delay-${delay}` : ""
  const visibilityClass = isVisible || !shouldAnimate ? "opacity-100" : "opacity-0"

  return cn(baseClasses, isVisible && animationClass, delayClass, visibilityClass)
}

/**
 * Check if user prefers reduced motion
 * @returns true if user prefers reduced motion
 */
export function prefersReducedMotion(): boolean {
  return checkReducedMotion()
}

/**
 * Check if Intersection Observer is supported
 * @returns true if Intersection Observer is supported
 */
export function supportsIntersectionObserver(): boolean {
  return checkIntersectionObserver()
}

/**
 * Get staggered delay for multiple elements
 * @param index - Index of the element in the list
 * @param baseDelay - Base delay in milliseconds
 * @param staggerAmount - Amount to stagger each element in milliseconds
 * @returns Calculated delay in milliseconds
 */
export function getStaggeredDelay(
  index: number,
  baseDelay: number = 0,
  staggerAmount: number = 400
): number {
  return baseDelay + index * staggerAmount
}

/**
 * Animation class presets for common use cases
 */
export const ANIMATION_PRESETS = {
  hero: {
    badge: "opacity-0",
    headline: "opacity-0",
    description: "opacity-0",
    buttons: "opacity-0",
    image: "opacity-0",
  },
  trust: {
    label: "opacity-0",
    marquee: "opacity-0",
  },
} as const

/**
 * Get CSS custom properties for animation delays
 * @param delay - Delay in milliseconds
 * @returns CSS custom properties object
 */
export function getAnimationDelayStyle(delay: number): React.CSSProperties {
  return {
    animationDelay: `${delay}ms`,
  }
}

/**
 * Create a staggered animation configuration for multiple elements
 * @param count - Number of elements to animate
 * @param baseDelay - Base delay before first element animates
 * @param staggerAmount - Delay between each element
 * @returns Array of delay values
 */
export function createStaggeredAnimation(
  count: number,
  baseDelay: number = 0,
  staggerAmount: number = 400
): number[] {
  return Array.from({ length: count }, (_, i) =>
    getStaggeredDelay(i, baseDelay, staggerAmount)
  )
}
