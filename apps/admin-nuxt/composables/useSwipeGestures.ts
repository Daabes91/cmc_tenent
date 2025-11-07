import { ref, watch, onUnmounted, type Ref } from 'vue';

/**
 * Swipe gesture composable for touch interactions
 * Provides reusable swipe gesture detection and handling
 */

export interface SwipeGestureOptions {
  threshold?: number;           // Minimum distance for swipe detection (default: 50)
  velocity?: number;           // Minimum velocity for swipe detection (default: 0.3)
  preventScroll?: boolean;     // Prevent default scroll behavior (default: false)
  direction?: 'horizontal' | 'vertical' | 'both'; // Allowed swipe directions (default: 'both')
  maxTime?: number;           // Maximum time for swipe gesture (default: 1000ms)
}

export interface SwipeGestureCallbacks {
  onSwipeLeft?: (event: TouchEvent, distance: number, velocity: number) => void;
  onSwipeRight?: (event: TouchEvent, distance: number, velocity: number) => void;
  onSwipeUp?: (event: TouchEvent, distance: number, velocity: number) => void;
  onSwipeDown?: (event: TouchEvent, distance: number, velocity: number) => void;
  onSwipeStart?: (event: TouchEvent) => void;
  onSwipeMove?: (event: TouchEvent, deltaX: number, deltaY: number) => void;
  onSwipeEnd?: (event: TouchEvent) => void;
}

export const useSwipeGestures = (
  target: Ref<HTMLElement | null>,
  callbacks: SwipeGestureCallbacks,
  options: SwipeGestureOptions = {}
) => {
  const {
    threshold = 50,
    velocity = 0.3,
    preventScroll = false,
    direction = 'both',
    maxTime = 1000
  } = options;

  // Touch state
  let startX = 0;
  let startY = 0;
  let startTime = 0;
  let isTracking = false;

  // Handle touch start
  const handleTouchStart = (event: TouchEvent) => {
    if (!target.value) return;

    const touch = event.touches[0];
    startX = touch.clientX;
    startY = touch.clientY;
    startTime = Date.now();
    isTracking = true;

    callbacks.onSwipeStart?.(event);
  };

  // Handle touch move
  const handleTouchMove = (event: TouchEvent) => {
    if (!isTracking || !target.value) return;

    const touch = event.touches[0];
    const deltaX = touch.clientX - startX;
    const deltaY = touch.clientY - startY;

    // Prevent scroll if requested
    if (preventScroll) {
      const absDeltaX = Math.abs(deltaX);
      const absDeltaY = Math.abs(deltaY);
      
      if (
        (direction === 'horizontal' && absDeltaX > absDeltaY) ||
        (direction === 'vertical' && absDeltaY > absDeltaX) ||
        direction === 'both'
      ) {
        event.preventDefault();
      }
    }

    callbacks.onSwipeMove?.(event, deltaX, deltaY);
  };

  // Handle touch end
  const handleTouchEnd = (event: TouchEvent) => {
    if (!isTracking || !target.value) return;

    const touch = event.changedTouches[0];
    const endX = touch.clientX;
    const endY = touch.clientY;
    const endTime = Date.now();

    const deltaX = endX - startX;
    const deltaY = endY - startY;
    const deltaTime = endTime - startTime;

    // Calculate distances and velocities
    const distanceX = Math.abs(deltaX);
    const distanceY = Math.abs(deltaY);
    const velocityX = distanceX / deltaTime;
    const velocityY = distanceY / deltaTime;

    // Check if gesture meets criteria
    const isValidTime = deltaTime <= maxTime;
    const isValidVelocity = Math.max(velocityX, velocityY) >= velocity;
    const isValidDistance = Math.max(distanceX, distanceY) >= threshold;

    if (isValidTime && (isValidVelocity || isValidDistance)) {
      // Determine swipe direction
      if (distanceX > distanceY) {
        // Horizontal swipe
        if (direction === 'horizontal' || direction === 'both') {
          if (deltaX > 0) {
            callbacks.onSwipeRight?.(event, distanceX, velocityX);
          } else {
            callbacks.onSwipeLeft?.(event, distanceX, velocityX);
          }
        }
      } else {
        // Vertical swipe
        if (direction === 'vertical' || direction === 'both') {
          if (deltaY > 0) {
            callbacks.onSwipeDown?.(event, distanceY, velocityY);
          } else {
            callbacks.onSwipeUp?.(event, distanceY, velocityY);
          }
        }
      }
    }

    callbacks.onSwipeEnd?.(event);
    isTracking = false;
  };

  // Add event listeners
  const addListeners = () => {
    if (!target.value) return;

    target.value.addEventListener('touchstart', handleTouchStart, { passive: !preventScroll });
    target.value.addEventListener('touchmove', handleTouchMove, { passive: !preventScroll });
    target.value.addEventListener('touchend', handleTouchEnd, { passive: true });
  };

  // Remove event listeners
  const removeListeners = () => {
    if (!target.value) return;

    target.value.removeEventListener('touchstart', handleTouchStart);
    target.value.removeEventListener('touchmove', handleTouchMove);
    target.value.removeEventListener('touchend', handleTouchEnd);
  };

  // Watch for target changes
  watch(target, (newTarget, oldTarget) => {
    if (oldTarget) {
      removeListeners();
    }
    if (newTarget) {
      addListeners();
    }
  }, { immediate: true });

  // Cleanup on unmount
  onUnmounted(() => {
    removeListeners();
  });

  return {
    addListeners,
    removeListeners
  };
};

// Utility composable for common swipe patterns
export const useSwipeToClose = (
  target: Ref<HTMLElement | null>,
  onClose: () => void,
  options: {
    threshold?: number;
    direction?: 'left' | 'right' | 'up' | 'down';
  } = {}
) => {
  const { threshold = 100, direction = 'left' } = options;

  const callbacks: SwipeGestureCallbacks = {};

  switch (direction) {
    case 'left':
      callbacks.onSwipeLeft = () => onClose();
      break;
    case 'right':
      callbacks.onSwipeRight = () => onClose();
      break;
    case 'up':
      callbacks.onSwipeUp = () => onClose();
      break;
    case 'down':
      callbacks.onSwipeDown = () => onClose();
      break;
  }

  return useSwipeGestures(target, callbacks, {
    threshold,
    direction: direction === 'left' || direction === 'right' ? 'horizontal' : 'vertical',
    preventScroll: true
  });
};

// Utility composable for swipe navigation
export const useSwipeNavigation = (
  target: Ref<HTMLElement | null>,
  onNext: () => void,
  onPrevious: () => void,
  options: SwipeGestureOptions = {}
) => {
  const callbacks: SwipeGestureCallbacks = {
    onSwipeLeft: () => onNext(),
    onSwipeRight: () => onPrevious()
  };

  return useSwipeGestures(target, callbacks, {
    ...options,
    direction: 'horizontal',
    preventScroll: true
  });
};