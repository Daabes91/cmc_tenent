import type { ModuleName, PermissionAction, ModulePermissions } from '@/types/staff';

export function usePermissions() {
  const { userRole } = useAuth();

  // Store user permissions fetched from API
  const permissions = useState<ModulePermissions | null>('user.permissions', () => null);
  const permissionsLoaded = useState<boolean>('user.permissionsLoaded', () => false);

  /**
   * Check if the current user has a specific permission for a module
   */
  function hasPermission(module: ModuleName, action: PermissionAction): boolean {
    // ADMIN role has all permissions
    if (userRole.value === 'ADMIN') {
      return true;
    }

    // If permissions not loaded yet, deny access (will be loaded by middleware)
    if (!permissions.value) {
      return false;
    }

    const modulePermissions = permissions.value[module];
    return modulePermissions ? modulePermissions.includes(action) : false;
  }

  /**
   * Check if user has VIEW permission for a module
   */
  function canView(module: ModuleName): boolean {
    return hasPermission(module, 'VIEW');
  }

  /**
   * Check if user has CREATE permission for a module
   */
  function canCreate(module: ModuleName): boolean {
    return hasPermission(module, 'CREATE');
  }

  /**
   * Check if user has EDIT permission for a module
   */
  function canEdit(module: ModuleName): boolean {
    return hasPermission(module, 'EDIT');
  }

  /**
   * Check if user has DELETE permission for a module
   */
  function canDelete(module: ModuleName): boolean {
    return hasPermission(module, 'DELETE');
  }

  /**
   * Check if user has any permission for a module (at least VIEW)
   */
  function hasModuleAccess(module: ModuleName): boolean {
    return canView(module);
  }

  /**
   * Load current user's permissions from the API
   */
  async function loadPermissions() {
    // ADMIN users don't need to fetch permissions
    if (userRole.value === 'ADMIN') {
      permissionsLoaded.value = true;
      return;
    }

    try {
      const { authorizationHeader } = useAuth();
      const config = useRuntimeConfig();
    const apiBase = useApiBase();

      const response = await $fetch<{ permissions: ModulePermissions }>('/auth/profile', {
        baseURL: apiBase,
        headers: authorizationHeader()
      });

      permissions.value = response.permissions;
      permissionsLoaded.value = true;
    } catch (error) {
      console.error('Failed to load permissions:', error);
      permissions.value = null;
      permissionsLoaded.value = false;
      throw error;
    }
  }

  /**
   * Clear permissions (used on logout)
   */
  function clearPermissions() {
    permissions.value = null;
    permissionsLoaded.value = false;
  }

  return {
    permissions,
    permissionsLoaded,
    hasPermission,
    canView,
    canCreate,
    canEdit,
    canDelete,
    hasModuleAccess,
    loadPermissions,
    clearPermissions
  };
}
