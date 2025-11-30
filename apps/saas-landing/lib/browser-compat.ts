/**
 * Browser Compatibility Utilities
 * Provides feature detection and fallbacks for older browsers
 */

/**
 * Check if Intersection Observer API is supported
 * @returns true if Intersection Observer is supported
 */
export function supportsIntersectionObserver(): boolean {
  if (typeof window === 'undefined') return false;
  return 'IntersectionObserver' in window;
}

/**
 * Check if CSS 3D transforms are supported
 * @returns true if 3D transforms are supported
 */
export function supports3DTransforms(): boolean {
  if (typeof window === 'undefined') return false;
  
  const el = document.createElement('div');
  const has3d =
    'WebkitPerspective' in el.style ||
    'MozPerspective' in el.style ||
    'perspective' in el.style;
  
  return has3d;
}

/**
 * Check if CSS animations are supported
 * @returns true if CSS animations are supported
 */
export function supportsCSSAnimations(): boolean {
  if (typeof window === 'undefined') return false;
  
  const el = document.createElement('div');
  const hasAnimation =
    'animation' in el.style ||
    'WebkitAnimation' in el.style ||
    'MozAnimation' in el.style;
  
  return hasAnimation;
}

/**
 * Check if user prefers reduced motion
 * @returns true if user prefers reduced motion
 */
export function prefersReducedMotion(): boolean {
  if (typeof window === 'undefined') return false;
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

/**
 * Check if device is mobile based on screen width
 * @param breakpoint - Width in pixels to consider as mobile (default: 768)
 * @returns true if device is mobile
 */
export function isMobileDevice(breakpoint: number = 768): boolean {
  if (typeof window === 'undefined') return false;
  return window.innerWidth < breakpoint;
}

/**
 * Check if device has touch support
 * @returns true if device supports touch
 */
export function hasTouchSupport(): boolean {
  if (typeof window === 'undefined') return false;
  return (
    'ontouchstart' in window ||
    navigator.maxTouchPoints > 0 ||
    // @ts-ignore - for older browsers
    navigator.msMaxTouchPoints > 0
  );
}

/**
 * Check if MutationObserver is supported
 * @returns true if MutationObserver is supported
 */
export function supportsMutationObserver(): boolean {
  if (typeof window === 'undefined') return false;
  return 'MutationObserver' in window;
}

/**
 * Check if WebP image format is supported
 * @returns Promise that resolves to true if WebP is supported
 */
export async function supportsWebP(): Promise<boolean> {
  if (typeof window === 'undefined') return false;
  
  return new Promise((resolve) => {
    const webP = new Image();
    webP.onload = webP.onerror = () => {
      resolve(webP.height === 2);
    };
    webP.src =
      'data:image/webp;base64,UklGRjoAAABXRUJQVlA4IC4AAACyAgCdASoCAAIALmk0mk0iIiIiIgBoSygABc6WWgAA/veff/0PP8bA//LwYAAA';
  });
}

/**
 * Get browser name and version
 * @returns Object with browser name and version
 */
export function getBrowserInfo(): { name: string; version: string } {
  if (typeof window === 'undefined') {
    return { name: 'unknown', version: 'unknown' };
  }

  const ua = navigator.userAgent;
  let name = 'unknown';
  let version = 'unknown';

  if (ua.indexOf('Firefox') > -1) {
    name = 'Firefox';
    version = ua.match(/Firefox\/([0-9.]+)/)?.[1] || 'unknown';
  } else if (ua.indexOf('Chrome') > -1) {
    name = 'Chrome';
    version = ua.match(/Chrome\/([0-9.]+)/)?.[1] || 'unknown';
  } else if (ua.indexOf('Safari') > -1) {
    name = 'Safari';
    version = ua.match(/Version\/([0-9.]+)/)?.[1] || 'unknown';
  } else if (ua.indexOf('Edge') > -1 || ua.indexOf('Edg') > -1) {
    name = 'Edge';
    version = ua.match(/Edg\/([0-9.]+)/)?.[1] || 'unknown';
  } else if (ua.indexOf('MSIE') > -1 || ua.indexOf('Trident') > -1) {
    name = 'IE';
    version = ua.match(/(?:MSIE |rv:)([0-9.]+)/)?.[1] || 'unknown';
  }

  return { name, version };
}

/**
 * Check if browser is considered "modern" (supports all required features)
 * @returns true if browser is modern
 */
export function isModernBrowser(): boolean {
  return (
    supportsIntersectionObserver() &&
    supports3DTransforms() &&
    supportsCSSAnimations() &&
    supportsMutationObserver()
  );
}

/**
 * Get a list of unsupported features
 * @returns Array of unsupported feature names
 */
export function getUnsupportedFeatures(): string[] {
  const unsupported: string[] = [];

  if (!supportsIntersectionObserver()) {
    unsupported.push('IntersectionObserver');
  }
  if (!supports3DTransforms()) {
    unsupported.push('3D Transforms');
  }
  if (!supportsCSSAnimations()) {
    unsupported.push('CSS Animations');
  }
  if (!supportsMutationObserver()) {
    unsupported.push('MutationObserver');
  }

  return unsupported;
}

/**
 * Log browser compatibility information to console (development only)
 */
export function logBrowserCompatibility(): void {
  if (typeof window === 'undefined' || process.env.NODE_ENV !== 'development') {
    return;
  }

  const browserInfo = getBrowserInfo();
  const isModern = isModernBrowser();
  const unsupported = getUnsupportedFeatures();

  console.group('🌐 Browser Compatibility Check');
  console.log('Browser:', `${browserInfo.name} ${browserInfo.version}`);
  console.log('Modern Browser:', isModern ? '✅' : '❌');
  console.log('Intersection Observer:', supportsIntersectionObserver() ? '✅' : '❌');
  console.log('3D Transforms:', supports3DTransforms() ? '✅' : '❌');
  console.log('CSS Animations:', supportsCSSAnimations() ? '✅' : '❌');
  console.log('Mutation Observer:', supportsMutationObserver() ? '✅' : '❌');
  console.log('Touch Support:', hasTouchSupport() ? '✅' : '❌');
  console.log('Prefers Reduced Motion:', prefersReducedMotion() ? '✅' : '❌');
  
  if (unsupported.length > 0) {
    console.warn('Unsupported Features:', unsupported.join(', '));
  }
  
  console.groupEnd();
}
