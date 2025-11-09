/**
 * Accessibility utilities for SAAS Admin Panel
 * Provides helpers for ARIA labels, keyboard navigation, and screen reader support
 */

/**
 * Generate unique IDs for ARIA relationships
 */
export const generateAriaId = (prefix: string): string => {
  return `${prefix}-${Math.random().toString(36).substr(2, 9)}`
}

/**
 * Announce message to screen readers
 */
export const announceToScreenReader = (message: string, priority: 'polite' | 'assertive' = 'polite') => {
  if (typeof document === 'undefined') return

  const announcement = document.createElement('div')
  announcement.setAttribute('role', 'status')
  announcement.setAttribute('aria-live', priority)
  announcement.setAttribute('aria-atomic', 'true')
  announcement.className = 'sr-only'
  announcement.textContent = message

  document.body.appendChild(announcement)

  // Remove after announcement
  setTimeout(() => {
    document.body.removeChild(announcement)
  }, 1000)
}

/**
 * Trap focus within a modal or dialog
 */
export const trapFocus = (element: HTMLElement) => {
  const focusableElements = element.querySelectorAll<HTMLElement>(
    'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
  )
  
  const firstFocusable = focusableElements[0]
  const lastFocusable = focusableElements[focusableElements.length - 1]

  const handleTabKey = (e: KeyboardEvent) => {
    if (e.key !== 'Tab') return

    if (e.shiftKey) {
      if (document.activeElement === firstFocusable) {
        lastFocusable?.focus()
        e.preventDefault()
      }
    } else {
      if (document.activeElement === lastFocusable) {
        firstFocusable?.focus()
        e.preventDefault()
      }
    }
  }

  element.addEventListener('keydown', handleTabKey)

  // Focus first element
  firstFocusable?.focus()

  return () => {
    element.removeEventListener('keydown', handleTabKey)
  }
}

/**
 * Handle keyboard navigation for lists
 */
export const handleListKeyboardNavigation = (
  event: KeyboardEvent,
  currentIndex: number,
  totalItems: number,
  onSelect: (index: number) => void
) => {
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      onSelect(Math.min(currentIndex + 1, totalItems - 1))
      break
    case 'ArrowUp':
      event.preventDefault()
      onSelect(Math.max(currentIndex - 1, 0))
      break
    case 'Home':
      event.preventDefault()
      onSelect(0)
      break
    case 'End':
      event.preventDefault()
      onSelect(totalItems - 1)
      break
    case 'Enter':
    case ' ':
      event.preventDefault()
      // Trigger selection
      break
  }
}

/**
 * Get readable status text for screen readers
 */
export const getStatusAriaLabel = (status: string): string => {
  const statusMap: Record<string, string> = {
    'ACTIVE': 'Active status',
    'INACTIVE': 'Inactive status',
    'DELETED': 'Deleted status',
    'healthy': 'System healthy',
    'degraded': 'System degraded',
    'down': 'System down'
  }
  return statusMap[status] || status
}

/**
 * Format date for screen readers
 */
export const formatDateForScreenReader = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

/**
 * Check if element is visible to screen readers
 */
export const isVisibleToScreenReader = (element: HTMLElement): boolean => {
  return !element.hasAttribute('aria-hidden') || element.getAttribute('aria-hidden') !== 'true'
}

/**
 * Skip to main content (for skip links)
 */
export const skipToMainContent = () => {
  const mainContent = document.querySelector('main')
  if (mainContent) {
    mainContent.setAttribute('tabindex', '-1')
    mainContent.focus()
    mainContent.removeAttribute('tabindex')
  }
}
