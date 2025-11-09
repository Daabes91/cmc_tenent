/**
 * Composable for responsive design utilities
 * Provides reactive breakpoint detection and responsive helpers
 */

export const useResponsive = () => {
  // Breakpoint values matching Tailwind defaults
  const breakpoints = {
    sm: 640,
    md: 768,
    lg: 1024,
    xl: 1280,
    '2xl': 1536
  }

  // Reactive state for current screen size
  const windowWidth = ref(0)
  const windowHeight = ref(0)

  // Computed breakpoint checks
  const isMobile = computed(() => windowWidth.value < breakpoints.md)
  const isTablet = computed(() => windowWidth.value >= breakpoints.md && windowWidth.value < breakpoints.lg)
  const isDesktop = computed(() => windowWidth.value >= breakpoints.lg)
  const isSmallScreen = computed(() => windowWidth.value < breakpoints.sm)
  const isLargeScreen = computed(() => windowWidth.value >= breakpoints.xl)

  // Specific breakpoint checks
  const isAboveSm = computed(() => windowWidth.value >= breakpoints.sm)
  const isAboveMd = computed(() => windowWidth.value >= breakpoints.md)
  const isAboveLg = computed(() => windowWidth.value >= breakpoints.lg)
  const isAboveXl = computed(() => windowWidth.value >= breakpoints.xl)

  // Touch device detection
  const isTouchDevice = computed(() => {
    if (process.client) {
      return 'ontouchstart' in window || navigator.maxTouchPoints > 0
    }
    return false
  })

  // Orientation detection
  const isPortrait = computed(() => windowHeight.value > windowWidth.value)
  const isLandscape = computed(() => windowWidth.value > windowHeight.value)

  // Update window dimensions
  const updateDimensions = () => {
    if (process.client) {
      windowWidth.value = window.innerWidth
      windowHeight.value = window.innerHeight
    }
  }

  // Setup resize listener
  if (process.client) {
    onMounted(() => {
      updateDimensions()
      window.addEventListener('resize', updateDimensions)
    })

    onUnmounted(() => {
      window.removeEventListener('resize', updateDimensions)
    })
  }

  // Helper to get responsive value based on breakpoint
  const getResponsiveValue = <T>(values: {
    base: T
    sm?: T
    md?: T
    lg?: T
    xl?: T
    '2xl'?: T
  }): T => {
    if (windowWidth.value >= breakpoints['2xl'] && values['2xl'] !== undefined) {
      return values['2xl']
    }
    if (windowWidth.value >= breakpoints.xl && values.xl !== undefined) {
      return values.xl
    }
    if (windowWidth.value >= breakpoints.lg && values.lg !== undefined) {
      return values.lg
    }
    if (windowWidth.value >= breakpoints.md && values.md !== undefined) {
      return values.md
    }
    if (windowWidth.value >= breakpoints.sm && values.sm !== undefined) {
      return values.sm
    }
    return values.base
  }

  // Helper to get grid columns based on screen size
  const getGridColumns = (config?: {
    mobile?: number
    tablet?: number
    desktop?: number
  }): number => {
    const defaults = {
      mobile: 1,
      tablet: 2,
      desktop: 4
    }
    const settings = { ...defaults, ...config }

    if (isMobile.value) return settings.mobile
    if (isTablet.value) return settings.tablet
    return settings.desktop
  }

  // Helper to determine if sidebar should be collapsed
  const shouldCollapseSidebar = computed(() => isMobile.value)

  // Helper for responsive padding
  const getResponsivePadding = (): string => {
    return getResponsiveValue({
      base: 'p-4',
      sm: 'p-4',
      md: 'p-6',
      lg: 'p-8'
    })
  }

  // Helper for responsive gap
  const getResponsiveGap = (): string => {
    return getResponsiveValue({
      base: 'gap-4',
      sm: 'gap-4',
      md: 'gap-6',
      lg: 'gap-6'
    })
  }

  return {
    // Dimensions
    windowWidth,
    windowHeight,

    // Breakpoint checks
    isMobile,
    isTablet,
    isDesktop,
    isSmallScreen,
    isLargeScreen,
    isAboveSm,
    isAboveMd,
    isAboveLg,
    isAboveXl,

    // Device detection
    isTouchDevice,
    isPortrait,
    isLandscape,

    // Helpers
    getResponsiveValue,
    getGridColumns,
    shouldCollapseSidebar,
    getResponsivePadding,
    getResponsiveGap,
    updateDimensions
  }
}
