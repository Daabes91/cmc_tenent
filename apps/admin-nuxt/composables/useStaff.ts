import type { Staff, CreateStaffRequest, UpdateStaffRequest, ModulePermissions, SetPasswordRequest } from '@/types/staff';

export function useStaff() {
  const { fetcher, request } = useAdminApi();

  /**
   * Get all staff members
   */
  async function getAllStaff() {
    return await fetcher<Staff[]>('/staff', []);
  }

  /**
   * Get a staff member by ID
   */
  async function getStaffById(id: number) {
    return await fetcher<Staff>(`/staff/${id}`, null);
  }

  /**
   * Create a new staff member
   */
  async function createStaff(data: CreateStaffRequest) {
    return await request<Staff>('/staff', {
      method: 'POST',
      body: data
    });
  }

  /**
   * Update a staff member
   */
  async function updateStaff(id: number, data: UpdateStaffRequest) {
    return await request<Staff>(`/staff/${id}`, {
      method: 'PUT',
      body: data
    });
  }

  /**
   * Delete a staff member
   */
  async function deleteStaff(id: number) {
    return await request<void>(`/staff/${id}`, {
      method: 'DELETE'
    });
  }

  /**
   * Resend invitation email to a staff member
   */
  async function resendInvitation(id: number) {
    return await request<void>(`/staff/${id}/resend-invitation`, {
      method: 'POST'
    });
  }

  /**
   * Get permissions for a staff member
   */
  async function getStaffPermissions(id: number) {
    return await fetcher<ModulePermissions>(`/staff/${id}/permissions`, null);
  }

  /**
   * Update permissions for a staff member
   */
  async function updateStaffPermissions(id: number, permissions: ModulePermissions) {
    return await request<void>(`/staff/${id}/permissions`, {
      method: 'PUT',
      body: permissions
    });
  }

  /**
   * Set password using invitation token (public endpoint)
   */
  async function setPassword(data: SetPasswordRequest) {
    const apiBase = useApiBase().replace('/admin', ''); // Use base API URL

    const response = await $fetch<void>(`${apiBase}/admin/setup/set-password`, {
      method: 'POST',
      body: data
    });

    return response;
  }

  return {
    getAllStaff,
    getStaffById,
    createStaff,
    updateStaff,
    deleteStaff,
    resendInvitation,
    getStaffPermissions,
    updateStaffPermissions,
    setPassword
  };
}
