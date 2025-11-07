import { reactive, computed, readonly, onMounted, onUnmounted } from 'vue';

/**
 * Viewport detection composable for responsive design
 * Provides reactive viewport state and device detection utilities
 */

interface ViewportState {
  width: number;
  height: number;
  isMobile: boolean;
  isTablet: boolean;
  isDesktop: boolean;
  orientation: 'portrait' | 'landscape';
}

interface ResponsiveConfig {
  breakpoints: {
    mobile: number;
    tablet: number;
    desktop: number;
  };
  touchTargetSize: {
    minimum: number;
    recommended: number;
  };
  spacing: {
    mobile: number;
    tablet: number;
    desktop: number;
  };
}

export const useViewport = () => {
  // Responsive configuration based on design specifications
  const config: ResponsiveConfig = {
    breakpoints: {
      mobile: 768,    // Mobile devices ≤ 768px
      tablet: 1024,   // Tablet devices 769px - 1024px
      desktop: 1025   // Desktop devices > 1024px
    },
    touchTargetSize: {
      minimum: 44,      // Minimum 44px touch target
      recommended: 48   // Recommended 48px touch target
    },
    spacing: {
      mobile: 16,       // 16px spacing on mobile
      tablet: 24,       // 24px spacing on tablet
      desktop: 32       // 32px spacing on desktop
    }
  };

  // Reactive viewport state
  const viewport = reactive<ViewportState>({
    width: 0,
    height: 0,
    isMobile: false,
    isTablet: false,
    isDesktop: false,
    orientation: 'portrait'
  });

  // Update viewport state based on current dimensions
  const updateViewport = () => {
    if (typeof window !== 'undefined') {
      const width = window.innerWidth;
      const height = window.innerHeight;
      
      viewport.width = width;
      viewport.height = height;
      viewport.isMobile = width <= config.breakpoints.mobile;
      viewport.isTablet = width > config.breakpoints.mobile && width <= config.breakpoints.tablet;
      viewport.isDesktop = width > config.breakpoints.tablet;
      viewport.orientation = width > height ? 'landscape' : 'portrait';
    }
  };

  // Initialize viewport state on client side
  onMounted(() => {
    updateViewport();
    
    // Add resize listener with debouncing for performance
    let resizeTimeout: ReturnType<typeof setTimeout>;
    const handleResize = () => {
      clearTimeout(resizeTimeout);
      resizeTimeout = setTimeout(updateViewport, 100);
    };
    
    window.addEventListener('resize', handleResize);
    window.addEventListener('orientationchange', updateViewport);
    
    // Cleanup listeners
    onUnmounted(() => {
      window.removeEventListener('resize', handleResize);
      window.removeEventListener('orientationchange', updateViewport);
      clearTimeout(resizeTimeout);
    });
  });

  // Computed properties for common breakpoint checks
  const isMobileOrTablet = computed(() => viewport.isMobile || viewport.isTablet);
  const isTabletOrDesktop = computed(() => viewport.isTablet || viewport.isDesktop);
  const isLandscape = computed(() => viewport.orientation === 'landscape');
  const isPortrait = computed(() => viewport.orientation === 'portrait');

  // Utility functions for responsive spacing
  const getResponsiveSpacing = () => {
    if (viewport.isMobile) return config.spacing.mobile;
    if (viewport.isTablet) return config.spacing.tablet;
    return config.spacing.desktop;
  };

  // Utility function to check if touch target meets minimum size
  const isTouchTargetValid = (size: number) => {
    return size >= config.touchTargetSize.minimum;
  };

  // Utility function to get recommended touch target size
  const getTouchTargetSize = (variant: 'minimum' | 'recommended' = 'minimum') => {
    return config.touchTargetSize[variant];
  };

  // Utility function for responsive grid columns
  const getResponsiveColumns = (mobile: number = 1, tablet: number = 2, desktop: number = 4) => {
    if (viewport.isMobile) return mobile;
    if (viewport.isTablet) return tablet;
    return desktop;
  };

  // Utility function to check if device supports touch
  const isTouchDevice = computed(() => {
    if (typeof window !== 'undefined') {
      return 'ontouchstart' in window || navigator.maxTouchPoints > 0;
    }
    return false;
  });

  // Utility function for responsive font sizes
  const getResponsiveFontSize = (mobile: string, tablet?: string, desktop?: string) => {
    if (viewport.isMobile) return mobile;
    if (viewport.isTablet && tablet) return tablet;
    if (viewport.isDesktop && desktop) return desktop;
    return mobile;
  };

  // Media query utilities
  const matchesMediaQuery = (query: string) => {
    if (typeof window !== 'undefined') {
      return window.matchMedia(query).matches;
    }
    return false;
  };

  // Predefined media queries
  const mediaQueries = {
    mobile: `(max-width: ${config.breakpoints.mobile}px)`,
    tablet: `(min-width: ${config.breakpoints.mobile + 1}px) and (max-width: ${config.breakpoints.tablet}px)`,
    desktop: `(min-width: ${config.breakpoints.desktop}px)`,
    landscape: '(orientation: landscape)',
    portrait: '(orientation: portrait)',
    prefersReducedMotion: '(prefers-reduced-motion: reduce)',
    prefersDarkMode: '(prefers-color-scheme: dark)'
  };

  // Check specific media queries
  const matchesMobile = computed(() => matchesMediaQuery(mediaQueries.mobile));
  const matchesTablet = computed(() => matchesMediaQuery(mediaQueries.tablet));
  const matchesDesktop = computed(() => matchesMediaQuery(mediaQueries.desktop));
  const prefersReducedMotion = computed(() => matchesMediaQuery(mediaQueries.prefersReducedMotion));
  const prefersDarkMode = computed(() => matchesMediaQuery(mediaQueries.prefersDarkMode));

  return {
    // State
    viewport: readonly(viewport),
    config,
    
    // Computed properties
    isMobileOrTablet,
    isTabletOrDesktop,
    isLandscape,
    isPortrait,
    isTouchDevice,
    
    // Media query checks
    matchesMobile,
    matchesTablet,
    matchesDesktop,
    prefersReducedMotion,
    prefersDarkMode,
    
    // Utility functions
    updateViewport,
    getResponsiveSpacing,
    isTouchTargetValid,
    getTouchTargetSize,
    getResponsiveColumns,
    getResponsiveFontSize,
    matchesMediaQuery,
    
    // Media queries
    mediaQueries
  };
};

// Type exports for use in other components
export type { ViewportState, ResponsiveConfig };