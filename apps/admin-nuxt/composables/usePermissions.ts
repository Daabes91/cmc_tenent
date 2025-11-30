import type { ModuleName, PermissionAction, ModulePermissions } from '@/types/staff';

export function usePermissions() {
  const { userRole } = useAuth();

  // Store user permissions fetched from API
  const permissions = useState<ModulePermissions | null>('user.permissions', () => null);
  const permissionsLoaded = useState<boolean>('user.permissionsLoaded', () => false);
  const lastLoadAttempt = useState<number | null>('user.permissionsLastLoad', () => null);
  const lastSuccess = useState<number | null>('user.permissionsLastSuccess', () => null);
  const lastError = useState<{ code?: number; at: number } | null>('user.permissionsLastError', () => null);
  const isLoading = useState<boolean>('user.permissionsLoading', () => false);

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

    const now = Date.now();
    // If we recently hit a rate limit, back off for 60s
    if (lastError.value && lastError.value.code === 429 && now - lastError.value.at < 60000) {
      return;
    }

    // If we recently succeeded, skip until 30s window expires
    if (lastSuccess.value && now - lastSuccess.value < 30000) {
      return;
    }

    // Throttle to avoid hammering API (429 responses)
    if (lastLoadAttempt.value && now - lastLoadAttempt.value < 5000) {
      return;
    }
    lastLoadAttempt.value = now;
    if (isLoading.value) return;
    isLoading.value = true;

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
      lastSuccess.value = Date.now();
      lastError.value = null;
    } catch (error) {
      console.error('Failed to load permissions:', error);
      permissions.value = null;
      permissionsLoaded.value = false;
      const status = (error as any)?.statusCode || (error as any)?.response?.status || (error as any)?.data?.statusCode;
      if (status) {
        lastError.value = { code: Number(status), at: Date.now() };
      }
      // swallow to avoid navigation loops; caller can decide what to do next
    }
    finally {
      isLoading.value = false;
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
