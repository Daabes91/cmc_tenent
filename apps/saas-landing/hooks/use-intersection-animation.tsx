import { useEffect, useRef, useState, RefObject } from "react"

interface UseIntersectionAnimationOptions {
  threshold?: number
  rootMargin?: string
  triggerOnce?: boolean
}

/**
 * Hook for triggering animations when elements enter the viewport
 * @param options - Configuration options for the intersection observer
 * @returns A tuple containing [ref, isVisible] where ref should be attached to the element
 */
export function useIntersectionAnimation<T extends HTMLElement = HTMLDivElement>(
  options: UseIntersectionAnimationOptions = {}
): [RefObject<T | null>, boolean] {
  const {
    threshold = 0.1,
    rootMargin = "50px",
    triggerOnce = true,
  } = options

  const ref = useRef<T>(null)
  const [isVisible, setIsVisible] = useState(false)

  useEffect(() => {
    const element = ref.current
    if (!element) return

    // Check for Intersection Observer support
    if (!("IntersectionObserver" in window)) {
      // Fallback: show element immediately if not supported
      setIsVisible(true)
      return
    }

    // Check for reduced motion preference
    const prefersReducedMotion = window.matchMedia(
      "(prefers-reduced-motion: reduce)"
    ).matches

    if (prefersReducedMotion) {
      // Show element immediately if user prefers reduced motion
      setIsVisible(true)
      return
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true)
          if (triggerOnce) {
            observer.disconnect()
          }
        } else if (!triggerOnce) {
          setIsVisible(false)
        }
      },
      { threshold, rootMargin }
    )

    observer.observe(element)

    return () => {
      observer.disconnect()
    }
  }, [threshold, rootMargin, triggerOnce])

  return [ref, isVisible]
}
