import { readonly, watch, onMounted, onUnmounted } from 'vue';

/**
 * Mobile navigation state management composable
 * Handles mobile sidebar state and navigation interactions
 */

export const useMobileNavigation = () => {
  // Mobile sidebar state
  const isMobileSidebarOpen = useState<boolean>('mobile-sidebar-open', () => false);

  // Toggle mobile sidebar
  const toggleMobileSidebar = () => {
    isMobileSidebarOpen.value = !isMobileSidebarOpen.value;
  };

  // Open mobile sidebar
  const openMobileSidebar = () => {
    isMobileSidebarOpen.value = true;
  };

  // Close mobile sidebar
  const closeMobileSidebar = () => {
    isMobileSidebarOpen.value = false;
  };

  // Auto-close sidebar on route change
  const route = useRoute();
  watch(() => route.path, () => {
    if (isMobileSidebarOpen.value) {
      closeMobileSidebar();
    }
  });

  // Close sidebar on escape key
  const handleKeydown = (event: KeyboardEvent) => {
    if (event.key === 'Escape' && isMobileSidebarOpen.value) {
      closeMobileSidebar();
    }
  };

  // Add/remove keyboard listeners
  onMounted(() => {
    document.addEventListener('keydown', handleKeydown);
  });

  onUnmounted(() => {
    document.removeEventListener('keydown', handleKeydown);
  });

  // Prevent body scroll when sidebar is open
  watch(isMobileSidebarOpen, (isOpen) => {
    if (typeof document !== 'undefined') {
      if (isOpen) {
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.overflow = '';
      }
    }
  });

  // Cleanup on unmount
  onUnmounted(() => {
    if (typeof document !== 'undefined') {
      document.body.style.overflow = '';
    }
  });

  return {
    isMobileSidebarOpen: readonly(isMobileSidebarOpen),
    toggleMobileSidebar,
    openMobileSidebar,
    closeMobileSidebar
  };
};