/**
 * Responsive design utilities and helpers
 * Provides consistent breakpoint values and utility functions
 */

// Tailwind CSS breakpoints (matching design specifications)
export const BREAKPOINTS = {
  xs: 475,    // Extra small phones
  sm: 640,    // Small phones and up
  md: 768,    // Tablets and up
  lg: 1024,   // Small laptops and up
  xl: 1280,   // Large laptops and up
  '2xl': 1536, // Large screens and up
  '3xl': 1920  // Extra large screens
} as const;

// Device categories based on requirements
export const DEVICE_BREAKPOINTS = {
  mobile: 768,    // Mobile devices ≤ 768px
  tablet: 1024,   // Tablet devices 769px - 1024px
  desktop: 1025   // Desktop devices > 1024px
} as const;

// Touch target sizes (WCAG AA compliance)
export const TOUCH_TARGETS = {
  minimum: 44,      // Minimum touch target size (44px)
  recommended: 48,  // Recommended touch target size (48px)
  comfortable: 56   // Comfortable touch target size (56px)
} as const;

// Responsive spacing scale
export const RESPONSIVE_SPACING = {
  mobile: {
    xs: 8,    // 0.5rem
    sm: 12,   // 0.75rem
    md: 16,   // 1rem
    lg: 20,   // 1.25rem
    xl: 24,   // 1.5rem
    '2xl': 32 // 2rem
  },
  tablet: {
    xs: 12,   // 0.75rem
    sm: 16,   // 1rem
    md: 24,   // 1.5rem
    lg: 32,   // 2rem
    xl: 40,   // 2.5rem
    '2xl': 48 // 3rem
  },
  desktop: {
    xs: 16,   // 1rem
    sm: 24,   // 1.5rem
    md: 32,   // 2rem
    lg: 48,   // 3rem
    xl: 64,   // 4rem
    '2xl': 80 // 5rem
  }
} as const;

// Device detection utilities
export const isClient = () => typeof window !== 'undefined';

export const getViewportWidth = (): number => {
  return isClient() ? window.innerWidth : 0;
};

export const getViewportHeight = (): number => {
  return isClient() ? window.innerHeight : 0;
};

export const isMobileDevice = (): boolean => {
  return getViewportWidth() <= DEVICE_BREAKPOINTS.mobile;
};

export const isTabletDevice = (): boolean => {
  const width = getViewportWidth();
  return width > DEVICE_BREAKPOINTS.mobile && width <= DEVICE_BREAKPOINTS.tablet;
};

export const isDesktopDevice = (): boolean => {
  return getViewportWidth() > DEVICE_BREAKPOINTS.tablet;
};

export const isTouchDevice = (): boolean => {
  if (!isClient()) return false;
  return 'ontouchstart' in window || navigator.maxTouchPoints > 0;
};

// Orientation detection
export const isLandscape = (): boolean => {
  return getViewportWidth() > getViewportHeight();
};

export const isPortrait = (): boolean => {
  return getViewportWidth() <= getViewportHeight();
};

// Media query utilities
export const createMediaQuery = (minWidth?: number, maxWidth?: number): string => {
  const conditions: string[] = [];
  
  if (minWidth) {
    conditions.push(`(min-width: ${minWidth}px)`);
  }
  
  if (maxWidth) {
    conditions.push(`(max-width: ${maxWidth}px)`);
  }
  
  return conditions.join(' and ');
};

export const matchesMediaQuery = (query: string): boolean => {
  if (!isClient()) return false;
  return window.matchMedia(query).matches;
};

// Predefined media queries
export const MEDIA_QUERIES = {
  mobile: createMediaQuery(undefined, DEVICE_BREAKPOINTS.mobile),
  tablet: createMediaQuery(DEVICE_BREAKPOINTS.mobile + 1, DEVICE_BREAKPOINTS.tablet),
  desktop: createMediaQuery(DEVICE_BREAKPOINTS.desktop),
  mobileAndTablet: createMediaQuery(undefined, DEVICE_BREAKPOINTS.tablet),
  tabletAndDesktop: createMediaQuery(DEVICE_BREAKPOINTS.mobile + 1),
  landscape: '(orientation: landscape)',
  portrait: '(orientation: portrait)',
  prefersReducedMotion: '(prefers-reduced-motion: reduce)',
  prefersDarkMode: '(prefers-color-scheme: dark)',
  prefersLightMode: '(prefers-color-scheme: light)',
  highDPI: '(-webkit-min-device-pixel-ratio: 2), (min-resolution: 192dpi)'
} as const;

// Responsive class generators
export const generateResponsiveClasses = (
  mobile: string,
  tablet?: string,
  desktop?: string
): string => {
  const classes = [mobile];
  
  if (tablet) {
    classes.push(`md:${tablet}`);
  }
  
  if (desktop) {
    classes.push(`lg:${desktop}`);
  }
  
  return classes.join(' ');
};

// Grid column utilities
export const getResponsiveGridCols = (
  mobile: number = 1,
  tablet: number = 2,
  desktop: number = 4
): string => {
  return generateResponsiveClasses(
    `grid-cols-${mobile}`,
    `grid-cols-${tablet}`,
    `grid-cols-${desktop}`
  );
};

// Spacing utilities
export const getResponsiveSpacing = (
  size: keyof typeof RESPONSIVE_SPACING.mobile,
  device: 'mobile' | 'tablet' | 'desktop' = 'mobile'
): number => {
  return RESPONSIVE_SPACING[device][size];
};

// Touch target validation
export const validateTouchTarget = (size: number): boolean => {
  return size >= TOUCH_TARGETS.minimum;
};

export const getTouchTargetClass = (variant: 'minimum' | 'recommended' | 'comfortable' = 'minimum'): string => {
  const size = TOUCH_TARGETS[variant];
  return `min-w-[${size}px] min-h-[${size}px]`;
};

// Responsive font size utilities
export const getResponsiveFontSize = (
  mobile: string,
  tablet?: string,
  desktop?: string
): string => {
  return generateResponsiveClasses(
    `text-${mobile}`,
    tablet ? `text-${tablet}` : undefined,
    desktop ? `text-${desktop}` : undefined
  );
};

// Container utilities
export const getResponsiveContainer = (): string => {
  return 'w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8';
};

export const getResponsivePadding = (): string => {
  return 'px-4 sm:px-6 lg:px-8';
};

// Animation utilities for mobile
export const getReducedMotionClass = (): string => {
  return 'motion-reduce:transition-none motion-reduce:transform-none';
};

// Accessibility utilities
export const getFocusVisibleClass = (): string => {
  return 'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2';
};

export const getTouchManipulationClass = (): string => {
  return 'touch-manipulation'; // Improves touch responsiveness
};

// Export types
export type DeviceType = 'mobile' | 'tablet' | 'desktop';
export type SpacingSize = keyof typeof RESPONSIVE_SPACING.mobile;
export type TouchTargetVariant = keyof typeof TOUCH_TARGETS;