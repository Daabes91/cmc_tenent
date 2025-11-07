import type { Ref } from 'vue';
import { useSwipeGestures } from '../composables/useSwipeGestures';

/**
 * Swipe action utilities for common UI patterns
 * Provides pre-configured swipe behaviors for tables, cards, and lists
 */

export interface SwipeAction {
  id: string;
  label: string;
  icon: string;
  color: 'red' | 'blue' | 'green' | 'yellow' | 'purple' | 'gray';
  action: () => void | Promise<void>;
}

export interface SwipeActionConfig {
  threshold?: number;
  showFeedback?: boolean;
  hapticFeedback?: boolean;
  confirmDestructive?: boolean;
}

// Color mappings for swipe actions
const actionColors = {
  red: {
    bg: 'bg-red-500',
    text: 'text-white',
    icon: 'text-white'
  },
  blue: {
    bg: 'bg-blue-500',
    text: 'text-white',
    icon: 'text-white'
  },
  green: {
    bg: 'bg-green-500',
    text: 'text-white',
    icon: 'text-white'
  },
  yellow: {
    bg: 'bg-yellow-500',
    text: 'text-white',
    icon: 'text-white'
  },
  purple: {
    bg: 'bg-purple-500',
    text: 'text-white',
    icon: 'text-white'
  },
  gray: {
    bg: 'bg-gray-500',
    text: 'text-white',
    icon: 'text-white'
  }
};

// Common swipe actions for different contexts
export const commonSwipeActions = {
  // Table row actions
  table: {
    edit: (onEdit: () => void): SwipeAction => ({
      id: 'edit',
      label: 'Edit',
      icon: 'i-lucide-edit',
      color: 'blue',
      action: onEdit
    }),
    delete: (onDelete: () => void): SwipeAction => ({
      id: 'delete',
      label: 'Delete',
      icon: 'i-lucide-trash-2',
      color: 'red',
      action: onDelete
    }),
    archive: (onArchive: () => void): SwipeAction => ({
      id: 'archive',
      label: 'Archive',
      icon: 'i-lucide-archive',
      color: 'gray',
      action: onArchive
    }),
    view: (onView: () => void): SwipeAction => ({
      id: 'view',
      label: 'View',
      icon: 'i-lucide-eye',
      color: 'green',
      action: onView
    })
  },

  // Card actions
  card: {
    favorite: (onFavorite: () => void): SwipeAction => ({
      id: 'favorite',
      label: 'Favorite',
      icon: 'i-lucide-heart',
      color: 'red',
      action: onFavorite
    }),
    share: (onShare: () => void): SwipeAction => ({
      id: 'share',
      label: 'Share',
      icon: 'i-lucide-share',
      color: 'blue',
      action: onShare
    }),
    bookmark: (onBookmark: () => void): SwipeAction => ({
      id: 'bookmark',
      label: 'Bookmark',
      icon: 'i-lucide-bookmark',
      color: 'yellow',
      action: onBookmark
    })
  },

  // List item actions
  list: {
    complete: (onComplete: () => void): SwipeAction => ({
      id: 'complete',
      label: 'Complete',
      icon: 'i-lucide-check',
      color: 'green',
      action: onComplete
    }),
    postpone: (onPostpone: () => void): SwipeAction => ({
      id: 'postpone',
      label: 'Postpone',
      icon: 'i-lucide-clock',
      color: 'yellow',
      action: onPostpone
    }),
    remove: (onRemove: () => void): SwipeAction => ({
      id: 'remove',
      label: 'Remove',
      icon: 'i-lucide-x',
      color: 'red',
      action: onRemove
    })
  }
};

// Utility to create swipe action element
export const createSwipeActionElement = (action: SwipeAction): HTMLElement => {
  const element = document.createElement('div');
  const colors = actionColors[action.color];
  
  element.className = `
    flex items-center justify-center px-4 py-2 ${colors.bg} ${colors.text}
    transition-all duration-200 cursor-pointer select-none
  `.trim();
  
  element.innerHTML = `
    <div class="flex items-center gap-2">
      <i class="${action.icon} w-5 h-5 ${colors.icon}"></i>
      <span class="text-sm font-medium">${action.label}</span>
    </div>
  `;
  
  return element;
};

// Haptic feedback utility (if supported)
export const triggerHapticFeedback = (type: 'light' | 'medium' | 'heavy' = 'light') => {
  if ('vibrate' in navigator) {
    const patterns = {
      light: [10],
      medium: [20],
      heavy: [30]
    };
    navigator.vibrate(patterns[type]);
  }
};

// Confirmation utility for destructive actions
export const confirmDestructiveAction = async (
  actionLabel: string,
  itemName?: string
): Promise<boolean> => {
  const message = itemName 
    ? `Are you sure you want to ${actionLabel.toLowerCase()} "${itemName}"?`
    : `Are you sure you want to ${actionLabel.toLowerCase()} this item?`;
    
  return confirm(message);
};

// Swipe action composable for Vue components
export const useSwipeActions = (
  element: Ref<HTMLElement | null>,
  leftActions: SwipeAction[] = [],
  rightActions: SwipeAction[] = [],
  config: SwipeActionConfig = {}
) => {
  const {
    threshold = 80,
    showFeedback = true,
    hapticFeedback = true,
    confirmDestructive = true
  } = config;

  let isRevealing = false;
  let revealedSide: 'left' | 'right' | null = null;

  const executeAction = async (action: SwipeAction) => {
    // Haptic feedback
    if (hapticFeedback) {
      triggerHapticFeedback(action.color === 'red' ? 'medium' : 'light');
    }

    // Confirmation for destructive actions
    if (confirmDestructive && action.color === 'red') {
      const confirmed = await confirmDestructiveAction(action.label);
      if (!confirmed) return;
    }

    // Execute action
    try {
      await action.action();
    } catch (error) {
      console.error(`Error executing swipe action "${action.id}":`, error);
    }
  };

  const resetPosition = () => {
    if (!element.value) return;
    element.value.style.transform = 'translateX(0)';
    element.value.style.transition = 'transform 0.3s ease-out';
    isRevealing = false;
    revealedSide = null;
  };

  // Set up swipe gestures
  useSwipeGestures(element, {
    onSwipeLeft: (event, distance) => {
      if (rightActions.length > 0 && distance >= threshold) {
        const action = rightActions[0]; // Use first action for now
        executeAction(action);
      }
    },
    onSwipeRight: (event, distance) => {
      if (leftActions.length > 0 && distance >= threshold) {
        const action = leftActions[0]; // Use first action for now
        executeAction(action);
      }
    },
    onSwipeMove: (event, deltaX, deltaY) => {
      if (!element.value || Math.abs(deltaY) > Math.abs(deltaX)) return;

      const maxReveal = 120;
      const clampedDelta = Math.max(-maxReveal, Math.min(maxReveal, deltaX));
      
      element.value.style.transform = `translateX(${clampedDelta}px)`;
      element.value.style.transition = 'none';
      
      // Visual feedback
      if (showFeedback) {
        const opacity = Math.abs(clampedDelta) / maxReveal;
        element.value.style.boxShadow = `0 0 20px rgba(0,0,0,${opacity * 0.2})`;
      }
    },
    onSwipeEnd: () => {
      resetPosition();
    }
  }, {
    threshold: 20,
    direction: 'horizontal',
    preventScroll: true
  });

  return {
    resetPosition,
    executeAction
  };
};

// Pre-configured swipe actions for common use cases
export const useTableRowSwipeActions = (
  element: Ref<HTMLElement | null>,
  item: any,
  actions: {
    onEdit?: (item: any) => void;
    onDelete?: (item: any) => void;
    onView?: (item: any) => void;
    onArchive?: (item: any) => void;
  }
) => {
  const leftActions: SwipeAction[] = [];
  const rightActions: SwipeAction[] = [];

  if (actions.onEdit) {
    leftActions.push(commonSwipeActions.table.edit(() => actions.onEdit!(item)));
  }
  if (actions.onView) {
    leftActions.push(commonSwipeActions.table.view(() => actions.onView!(item)));
  }

  if (actions.onDelete) {
    rightActions.push(commonSwipeActions.table.delete(() => actions.onDelete!(item)));
  }
  if (actions.onArchive) {
    rightActions.push(commonSwipeActions.table.archive(() => actions.onArchive!(item)));
  }

  return useSwipeActions(element, leftActions, rightActions);
};

export const useCardSwipeActions = (
  element: Ref<HTMLElement | null>,
  item: any,
  actions: {
    onFavorite?: (item: any) => void;
    onShare?: (item: any) => void;
    onBookmark?: (item: any) => void;
  }
) => {
  const leftActions: SwipeAction[] = [];
  const rightActions: SwipeAction[] = [];

  if (actions.onFavorite) {
    rightActions.push(commonSwipeActions.card.favorite(() => actions.onFavorite!(item)));
  }
  if (actions.onShare) {
    leftActions.push(commonSwipeActions.card.share(() => actions.onShare!(item)));
  }
  if (actions.onBookmark) {
    leftActions.push(commonSwipeActions.card.bookmark(() => actions.onBookmark!(item)));
  }

  return useSwipeActions(element, leftActions, rightActions);
};