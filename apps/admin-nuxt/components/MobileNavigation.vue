<!--
  Mobile Navigation Component
  Provides touch-optimized navigation for mobile devices
  Integrates with viewport detection for responsive behavior
-->
<template>
  <!-- Mobile Header Bar -->
  <header 
    v-if="shouldShowMobileNav" 
    class="sticky top-0 z-50 bg-white/95 backdrop-blur-sm border-b border-slate-200/60 dark:bg-slate-900/95 dark:border-slate-700/60 lg:hidden"
  >
    <div class="mx-auto flex w-full max-w-7xl items-center justify-between px-4 py-3 sm:px-6">
      <!-- Hamburger Menu Button -->
      <button 
        @click="toggleSidebar"
        :class="[
          'flex items-center justify-center rounded-lg transition-colors duration-200',
          'hover:bg-slate-100 dark:hover:bg-white/10',
          'focus:outline-none focus:ring-2 focus:ring-violet-500 focus:ring-offset-2',
          'touch-manipulation',
          getTouchTargetClass()
        ]"
        :aria-label="isOpen ? 'Close navigation menu' : 'Open navigation menu'"
        :aria-expanded="isOpen"
      >
        <Transition name="hamburger" mode="out-in">
          <UIcon 
            v-if="!isOpen"
            name="i-lucide-menu" 
            class="w-6 h-6 text-slate-600 dark:text-slate-300" 
          />
          <UIcon 
            v-else
            name="i-lucide-x" 
            class="w-6 h-6 text-slate-600 dark:text-slate-300" 
          />
        </Transition>
      </button>
      
      <!-- Page Title -->
      <div class="flex-1 text-center px-4">
        <h1 class="text-lg font-semibold text-slate-900 dark:text-white truncate">
          {{ pageTitle }}
        </h1>
      </div>
      
      <!-- Mobile Actions -->
      <div class="flex items-center gap-2">
        <!-- View Website Button -->
        <button
          @click="openWebsite"
          :class="[
            'flex items-center justify-center rounded-lg transition-colors duration-200',
            'hover:bg-slate-100 dark:hover:bg-white/10',
            'focus:outline-none focus:ring-2 focus:ring-violet-500 focus:ring-offset-2',
            'touch-manipulation',
            getTouchTargetClass()
          ]"
          :aria-label="$t('layout.header.viewWebsite')"
        >
          <UIcon
            name="i-lucide-globe"
            class="w-5 h-5 text-slate-600 dark:text-slate-300"
          />
        </button>

        <!-- Theme Toggle -->
        <button
          @click="$emit('toggle-theme')"
          :class="[
            'flex items-center justify-center rounded-lg transition-colors duration-200',
            'hover:bg-slate-100 dark:hover:bg-white/10',
            'focus:outline-none focus:ring-2 focus:ring-violet-500 focus:ring-offset-2',
            'touch-manipulation',
            getTouchTargetClass()
          ]"
          :aria-label="'Toggle theme'"
        >
          <UIcon
            :name="isDark ? 'i-lucide-sun' : 'i-lucide-moon'"
            class="w-5 h-5 text-slate-600 dark:text-slate-300"
          />
        </button>

        <!-- User Menu -->
        <UDropdown :items="userMenuItems" :ui="mobileDropdownUi">
          <button
            :class="[
              'flex items-center justify-center rounded-lg transition-colors duration-200',
              'hover:bg-slate-100 dark:hover:bg-white/10',
              'focus:outline-none focus:ring-2 focus:ring-mint-500 focus:ring-offset-2',
              'touch-manipulation',
              getTouchTargetClass()
            ]"
          >
            <UAvatar
              size="sm"
              icon="i-lucide-user"
              :ui="{ 
                rounded: 'rounded-lg', 
                background: 'bg-gradient-to-br from-mint-500 to-mint-400' 
              }"
              class="ring-2 ring-mint-500/30 dark:ring-white/20"
            />
          </button>
        </UDropdown>
      </div>
    </div>
  </header>

  <!-- Mobile Sidebar Overlay -->
  <Teleport to="body">
    <Transition name="mobile-sidebar">
      <div 
        v-if="isOpen && shouldShowMobileNav" 
        class="fixed inset-0 z-50 lg:hidden"
        @touchstart="handleTouchStart"
        @touchmove="handleTouchMove"
        @touchend="handleTouchEnd"
      >
        <!-- Backdrop -->
        <div 
          class="fixed inset-0 bg-black/50 transition-opacity duration-300"
          @click="closeSidebar"
        />
        
        <!-- Sidebar Panel -->
        <nav 
          ref="sidebarRef"
          :class="[
            'fixed left-0 top-0 h-full bg-white dark:bg-slate-950 shadow-xl',
            'transform transition-transform duration-300 ease-out',
            'w-80 max-w-[85vw]',
            isOpen ? 'translate-x-0' : '-translate-x-full'
          ]"
        >
          <!-- Sidebar Header -->
      <div class="flex items-center justify-between p-6 border-b border-slate-200/60 dark:border-white/10">
            <div class="flex items-center gap-3">
              <div class="relative flex h-10 w-10 items-center justify-center overflow-hidden rounded-xl bg-primary-gradient text-xl text-white shadow-lg shadow-teal-500/40">
                <img
                  v-if="clinicLogo"
                  :src="clinicLogo"
                  :alt="clinicName"
                  class="h-full w-full object-cover"
                />
                <img
                  v-else
                  src="https://imagedelivery.net/K88oXEK4nwOFUDLZaSq1vg/6c79054b-5ecc-4a97-be03-441518f70200/public"
                  :alt="clinicName || 'Clinic logo'"
                  class="h-full w-full object-cover"
                  loading="lazy"
                />
              </div>
              <div>
                <span class="text-sm font-semibold text-slate-900 dark:text-white">{{ clinicName }}</span>
                <p class="text-xs text-slate-500 dark:text-slate-400">{{ clinicTagline }}</p>
              </div>
            </div>
            
            <button 
              @click="closeSidebar"
              :class="[
                'flex items-center justify-center rounded-lg transition-colors duration-200',
                'hover:bg-slate-100 dark:hover:bg-white/10',
                'focus:outline-none focus:ring-2 focus:ring-violet-500 focus:ring-offset-2',
                'touch-manipulation',
                getTouchTargetClass()
              ]"
              aria-label="Close navigation menu"
            >
              <UIcon name="i-lucide-x" class="w-5 h-5 text-slate-500 dark:text-slate-400" />
            </button>
          </div>
          
          <!-- Today's Metrics (Mobile) -->
          <div class="p-6 border-b border-slate-200/60 dark:border-white/10">
            <p class="text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400 mb-3">
              Today's Overview
            </p>
            <div class="grid grid-cols-2 gap-3 text-sm">
              <div class="rounded-lg border border-slate-200/60 bg-slate-50/50 dark:border-white/10 dark:bg-white/5 p-3">
                <p class="text-xs text-slate-500 dark:text-slate-400">Appointments</p>
                <p class="mt-1 text-lg font-semibold text-slate-900 dark:text-white">{{ todayAppointments }}</p>
              </div>
              <div class="rounded-lg border border-slate-200/60 bg-slate-50/50 dark:border-white/10 dark:bg-white/5 p-3">
                <p class="text-xs text-slate-500 dark:text-slate-400">Utilization</p>
                <p class="mt-1 text-lg font-semibold text-slate-900 dark:text-white">{{ utilizationRate }}%</p>
              </div>
            </div>
          </div>
          
          <!-- Navigation Links -->
          <div class="flex-1 overflow-y-auto px-4 py-6">
            <div class="lg:hidden mb-6">
              <p class="text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400 mb-3">
                Quick Navigation
              </p>
              <div class="overflow-x-auto -mx-4 px-4">
                <div class="flex gap-3 snap-x snap-mandatory pr-4">
                  <button
                    v-for="link in flattenedPrimaryLinks"
                    :key="link.id"
                    @click="handleQuickNavClick(link)"
                    :class="[
                      'flex flex-col items-center min-w-[72px] px-3 py-2 rounded-xl border text-xs font-medium transition-all duration-200 snap-start',
                      link.active
                        ? 'bg-violet-500/10 border-violet-500/30 text-violet-600 dark:text-violet-300'
                        : 'border-slate-200/60 bg-white dark:bg-white/5 dark:border-white/10 text-slate-600 dark:text-slate-300 hover:border-violet-400 hover:text-violet-500'
                    ]"
                  >
                    <UIcon :name="link.icon || 'i-lucide-circle'" class="mb-1 h-4 w-4" />
                    <span class="text-center leading-tight line-clamp-2">{{ link.label }}</span>
                  </button>
                </div>
              </div>
            </div>

            <UVerticalNavigation
              :links="navigationLinks"
              :ui="mobileNavUi"
              @click="handleNavClick"
            />
          </div>
          
          <!-- Quick Actions (Mobile) -->
          <div class="p-6 border-t border-slate-200/60 dark:border-white/10">
            <p class="text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400 mb-3">
              Quick Actions
            </p>
            <div class="space-y-2">
              <button
                v-for="action in quickActions"
                :key="action.id"
                @click="handleQuickAction(action)"
                :class="[
                  'w-full flex items-center gap-3 p-3 rounded-xl transition-colors duration-200',
                  'hover:bg-slate-100 dark:hover:bg-white/5',
                  'focus:outline-none focus:ring-2 focus:ring-violet-500 focus:ring-offset-2',
                  'touch-manipulation',
                  'min-h-[48px]'
                ]"
              >
                <div :class="['flex items-center justify-center w-8 h-8 rounded-lg', action.iconBg]">
                  <UIcon :name="action.icon" class="w-4 h-4" />
                </div>
                <span class="text-sm font-medium text-slate-900 dark:text-white">{{ action.label }}</span>
              </button>
            </div>
          </div>
        </nav>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';

interface QuickAction {
  id: string;
  label: string;
  icon: string;
  iconBg: string;
  action: () => void;
}

interface Props {
  isOpen?: boolean;
  pageTitle?: string;
  navigationLinks?: any[];
  clinicName?: string;
  clinicTagline?: string;
  clinicLogo?: string;
  todayAppointments?: number;
  utilizationRate?: number;
  userMenuItems?: any[];
  isDark?: boolean;
}

interface Emits {
  (e: 'toggle'): void;
  (e: 'close'): void;
  (e: 'nav-click', item: any): void;
  (e: 'toggle-theme'): void;
}

const props = withDefaults(defineProps<Props>(), {
  isOpen: false,
  pageTitle: 'Dashboard',
  navigationLinks: () => [],
  clinicName: 'Clinic Admin',
  clinicTagline: 'Management System',
  clinicLogo: '',
  todayAppointments: 0,
  utilizationRate: 0,
  userMenuItems: () => [],
  isDark: false
});

const emit = defineEmits<Emits>();

// Use viewport composable
const { viewport, getTouchTargetSize } = useViewport();
const shouldShowMobileNav = computed(() => viewport.isMobile || viewport.isTablet);

// Navigation methods
const toggleSidebar = () => {
  emit('toggle');
};

const closeSidebar = () => {
  emit('close');
};

const handleNavClick = (item: any) => {
  emit('nav-click', item);
  closeSidebar(); // Auto-close on navigation
};

const handleQuickAction = (action: QuickAction) => {
  action.action();
  closeSidebar();
};

// Open patient-facing website with tenant slug
const config = useRuntimeConfig();
const { tenantSlug } = useTenantSlug();
const openWebsite = () => {
  const baseUrl = config.public.webUrl;

  // Build URL with tenant slug subdomain
  // For local: http://daabes.localhost:3001
  // For production: https://daabes.yourdomain.com
  try {
    const url = new URL(baseUrl);
    const slug = tenantSlug.value;

    // Check if hostname already includes the tenant slug
    if (!url.hostname.startsWith(`${slug}.`)) {
      // Insert tenant slug as subdomain
      url.hostname = `${slug}.${url.hostname}`;
    }

    window.open(url.toString(), '_blank');
  } catch (error) {
    // Fallback to base URL if URL construction fails
    console.error('Failed to construct tenant URL:', error);
    window.open(baseUrl, '_blank');
  }
};

// Touch target utility
const getTouchTargetClass = () => {
  const size = getTouchTargetSize();
  return `min-w-[${size}px] min-h-[${size}px]`;
};

// Enhanced swipe gesture handling
const sidebarRef = ref<HTMLElement>();
let isDragging = false;
let dragStartX = 0;

// Use enhanced swipe gesture composable for swipe-to-close
useSwipeToClose(sidebarRef, closeSidebar, {
  threshold: 80,
  direction: 'left'
});

// Custom drag handling for visual feedback during swipe
const handleTouchStart = (e: TouchEvent) => {
  dragStartX = e.touches[0].clientX;
  isDragging = false;
};

const handleTouchMove = (e: TouchEvent) => {
  if (!sidebarRef.value) return;
  
  const currentX = e.touches[0].clientX;
  const deltaX = currentX - dragStartX;
  const absDeltaX = Math.abs(deltaX);
  
  // Start dragging if horizontal movement is significant
  if (!isDragging && absDeltaX > 10) {
    isDragging = true;
  }
  
  // Only allow left swipe (closing) with visual feedback
  if (isDragging && deltaX < 0) {
    const translateX = Math.max(deltaX, -320); // Max swipe distance
    sidebarRef.value.style.transform = `translateX(${translateX}px)`;
    sidebarRef.value.style.transition = 'none'; // Disable transition during drag
  }
};

const handleTouchEnd = (e: TouchEvent) => {
  if (!sidebarRef.value || !isDragging) return;
  
  const endX = e.changedTouches[0].clientX;
  const deltaX = endX - dragStartX;
  
  // Re-enable transition
  sidebarRef.value.style.transition = '';
  
  // Close if swiped more than threshold
  if (deltaX < -120) {
    closeSidebar();
  } else {
    // Reset position with animation
    sidebarRef.value.style.transform = 'translateX(0)';
  }
  
  isDragging = false;
};

// Quick actions
const quickActions = computed<QuickAction[]>(() => [
  {
    id: 'new-appointment',
    label: 'New Appointment',
    icon: 'i-lucide-calendar-plus',
    iconBg: 'bg-violet-100 text-violet-600 dark:bg-violet-500/15 dark:text-violet-300',
    action: () => navigateTo('/appointments/new')
  },
  {
    id: 'new-patient',
    label: 'New Patient',
    icon: 'i-lucide-user-plus',
    iconBg: 'bg-emerald-100 text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-300',
    action: () => navigateTo('/patients/new')
  },
  {
    id: 'calendar',
    label: 'View Calendar',
    icon: 'i-lucide-calendar-days',
    iconBg: 'bg-blue-100 text-blue-600 dark:bg-blue-500/15 dark:text-blue-300',
    action: () => navigateTo('/calendar')
  }
]);

// Mobile-optimized UI configurations
const mobileNavUi = computed(() => ({
  wrapper: 'space-y-2',
  base: 'group relative flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition-all duration-200 touch-manipulation min-h-[48px]',
  active: 'bg-gradient-to-r from-mint-500 to-mint-400 text-white shadow-lg shadow-mint-600/40',
  inactive: 'text-slate-600 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-300 dark:hover:bg-white/5 dark:hover:text-white',
  icon: {
    base: 'h-5 w-5 flex-shrink-0 transition-transform duration-200',
    active: 'text-white',
    inactive: 'text-slate-400 group-hover:text-slate-700 dark:group-hover:text-white'
  },
  badge: {
    base: 'ml-auto rounded-full border border-white/15 bg-white/10 px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide text-white',
    size: 'xs'
  }
}));

const mobileDropdownUi = computed(() => ({
  container: 'z-50',
  width: 'w-56',
  background: 'bg-white/95 backdrop-blur-lg dark:bg-slate-900/95',
  shadow: 'shadow-xl shadow-slate-200/50 dark:shadow-black/40',
  rounded: 'rounded-xl',
  ring: 'ring-1 ring-slate-200/80 dark:ring-white/10',
  padding: 'p-2',
  item: {
    base: 'group flex w-full items-center gap-3 transition-all duration-150 touch-manipulation',
    padding: 'px-3 py-3',
    size: 'text-sm',
    rounded: 'rounded-lg',
    active: 'bg-violet-50 text-violet-900 dark:bg-white/10 dark:text-white',
    inactive: 'text-slate-700 hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-white/5',
    disabled: 'opacity-50 cursor-not-allowed',
    label: 'font-medium',
    icon: {
      base: 'flex-shrink-0 w-5 h-5',
      active: 'text-violet-600 dark:text-white',
      inactive: 'text-slate-400 group-hover:text-slate-700 dark:group-hover:text-white'
    }
  },
  separator: 'h-px bg-slate-200 dark:bg-white/10 my-2'
}));

const flattenedPrimaryLinks = computed(() => {
  const results: any[] = [];
  (props.navigationLinks || []).forEach((section: any) => {
    const items = section?.children || [];
    items
      .filter((item: any) => !item.divider)
      .slice(0, 8)
      .forEach((item: any) => {
        results.push({
          id: item.id || item.label,
          label: item.label,
          icon: item.icon,
          to: item.to,
          active: item.active
        });
      });
  });
  return results;
});

const handleQuickNavClick = (item: any) => {
  emit('nav-click', item);
  closeSidebar();
};

// Watch for route changes to close sidebar
const route = useRoute();
watch(() => route.path, () => {
  if (props.isOpen) {
    closeSidebar();
  }
});
</script>

<style scoped>
/* Hamburger icon transition */
.hamburger-enter-active,
.hamburger-leave-active {
  transition: all 0.2s ease;
}

.hamburger-enter-from,
.hamburger-leave-to {
  opacity: 0;
  transform: rotate(90deg);
}

/* Mobile sidebar transitions */
.mobile-sidebar-enter-active,
.mobile-sidebar-leave-active {
  transition: all 0.3s ease-out;
}

.mobile-sidebar-enter-from,
.mobile-sidebar-leave-to {
  opacity: 0;
}

.mobile-sidebar-enter-from .fixed.left-0,
.mobile-sidebar-leave-to .fixed.left-0 {
  transform: translateX(-100%);
}

/* Touch manipulation for better mobile performance */
.touch-manipulation {
  touch-action: manipulation;
}

/* Smooth scrolling for navigation */
.overflow-y-auto {
  -webkit-overflow-scrolling: touch;
}
</style>
