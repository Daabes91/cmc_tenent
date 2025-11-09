// Lazy Loading Utilities
import type { Component } from 'vue'

/**
 * Lazy load a component with loading and error states
 */
export const lazyLoadComponent = (
  loader: () => Promise<Component>,
  options: {
    loadingComponent?: Component
    errorComponent?: Component
    delay?: number
    timeout?: number
  } = {}
) => {
  return defineAsyncComponent({
    loader,
    loadingComponent: options.loadingComponent,
    errorComponent: options.errorComponent,
    delay: options.delay || 200,
    timeout: options.timeout || 10000
  })
}

/**
 * Preload a component for faster subsequent loads
 */
export const preloadComponent = async (
  loader: () => Promise<Component>
): Promise<void> => {
  try {
    await loader()
  } catch (error) {
    console.error('Failed to preload component:', error)
  }
}

/**
 * Lazy load multiple components
 */
export const lazyLoadComponents = (
  loaders: Record<string, () => Promise<Component>>,
  options?: {
    loadingComponent?: Component
    errorComponent?: Component
    delay?: number
    timeout?: number
  }
): Record<string, ReturnType<typeof defineAsyncComponent>> => {
  const components: Record<string, ReturnType<typeof defineAsyncComponent>> = {}
  
  for (const [name, loader] of Object.entries(loaders)) {
    components[name] = lazyLoadComponent(loader, options)
  }
  
  return components
}

/**
 * Check if element is in viewport (for lazy loading)
 */
export const isInViewport = (element: HTMLElement): boolean => {
  const rect = element.getBoundingClientRect()
  return (
    rect.top >= 0 &&
    rect.left >= 0 &&
    rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
    rect.right <= (window.innerWidth || document.documentElement.clientWidth)
  )
}

/**
 * Intersection Observer for lazy loading
 */
export const createLazyObserver = (
  callback: (entry: IntersectionObserverEntry) => void,
  options?: IntersectionObserverInit
): IntersectionObserver => {
  return new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        callback(entry)
      }
    })
  }, {
    rootMargin: '50px',
    threshold: 0.01,
    ...options
  })
}
