/**
 * Animation configuration and utilities for the landing page
 * This file centralizes all animation-related constants and helper functions
 */

export * from "./animation-utils"

/**
 * Animation class names for different element types
 */
export const ANIMATION_CLASSES = {
  // Slide animations
  slideRight: "animate-slide-right",
  
  // Scale animations
  expand: "animate-expand",
  
  // Marquee animations
  marquee: "animate-marquee",
  
  // Base transition classes
  transition: "transition-all duration-900",
  
  // Opacity classes
  hidden: "opacity-0",
  visible: "opacity-100",
} as const

/**
 * Animation timing functions
 */
export const ANIMATION_TIMING = {
  easeOut: "ease-out",
  easeIn: "ease-in",
  easeInOut: "ease-in-out",
  linear: "linear",
} as const

/**
 * Reduced motion media query
 */
export const REDUCED_MOTION_QUERY = "(prefers-reduced-motion: reduce)"

/**
 * Intersection observer configuration presets
 */
export const INTERSECTION_CONFIG = {
  default: {
    threshold: 0.1,
    rootMargin: "50px",
  },
  immediate: {
    threshold: 0,
    rootMargin: "100px",
  },
  delayed: {
    threshold: 0.2,
    rootMargin: "0px",
  },
} as const
