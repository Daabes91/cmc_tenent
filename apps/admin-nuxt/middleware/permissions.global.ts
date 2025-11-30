import type { ModuleName } from '@/types/staff';

// Map routes to module names for permission checking
function getModuleForRoute(path: string): ModuleName | null {
  // Match route patterns - handle both base paths and sub-routes
  const pathSegment = path.split('/')[1]; // Get first path segment

  const routeToModuleMap: Record<string, ModuleName> = {
    'appointments': 'appointments',
    'doctors': 'doctors',
    'patients': 'patients',
    'treatment-plans': 'treatmentPlans',
    'services': 'services',
    'blogs': 'blogs',
    'reports': 'reports',
    'staff': 'staff',
    'clinic-settings': 'settings',
    'billing': 'settings'
  };

  return routeToModuleMap[pathSegment] || null;
}

export default defineNuxtRouteMiddleware(async (to) => {
  const auth = useAuth();
  const { hasModuleAccess, permissionsLoaded, loadPermissions } = usePermissions();

  // Skip permission check for public routes
  if (to.path === '/login' || to.path === '/403' || to.path === '/password-setup') {
    return;
  }

  // Skip permission check if not authenticated (auth middleware will handle)
  if (!auth.isAuthenticated.value) {
    return;
  }

  // ADMIN users have access to everything
  if (auth.userRole.value === 'ADMIN') {
    return;
  }

  // Load permissions if not already loaded
  if (!permissionsLoaded.value) {
    try {
      await loadPermissions();
    } catch (error) {
      console.error('Failed to load permissions:', error);
      return navigateTo('/403');
    }
    // If still not loaded (e.g., throttled/backed off), allow navigation to avoid lockout
    if (!permissionsLoaded.value) {
      return;
    }
  }

  // Check if this route requires module access
  const module = getModuleForRoute(to.path);

  // Allow access to routes that don't map to a module (like dashboard, settings)
  if (!module) {
    return;
  }

  // Check if user has VIEW permission for this module
  if (!hasModuleAccess(module)) {
    return navigateTo('/403');
  }
});
