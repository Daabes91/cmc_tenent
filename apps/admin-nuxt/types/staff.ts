export type StaffRole = 'ADMIN' | 'RECEPTIONIST' | 'DOCTOR';

export type StaffStatus = 'ACTIVE' | 'SUSPENDED' | 'INVITED';

export type PermissionAction = 'VIEW' | 'CREATE' | 'EDIT' | 'DELETE';

export type ModuleName =
  | 'appointments'
  | 'calendar'
  | 'patients'
  | 'doctors'
  | 'materials'
  | 'services'
  | 'insuranceCompanies'
  | 'treatmentPlans'
  | 'reports'
  | 'billing'
  | 'translations'
  | 'settings'
  | 'clinicSettings'
  | 'staff'
  | 'blogs';

export interface ModulePermissions {
  appointments: PermissionAction[];
  calendar: PermissionAction[];
  patients: PermissionAction[];
  doctors: PermissionAction[];
  materials: PermissionAction[];
  services: PermissionAction[];
  insuranceCompanies: PermissionAction[];
  treatmentPlans: PermissionAction[];
  reports: PermissionAction[];
  billing: PermissionAction[];
  translations: PermissionAction[];
  settings: PermissionAction[];
  clinicSettings: PermissionAction[];
  staff: PermissionAction[];
  blogs: PermissionAction[];
}

export interface Staff {
  id: number;
  email: string;
  fullName: string;
  role: StaffRole;
  status: StaffStatus;
  permissions: ModulePermissions;
  hasPendingInvitation: boolean;
  createdAt: string;
}

export interface StaffProfile {
  id: number;
  email: string;
  fullName: string;
  role: StaffRole;
  permissions: ModulePermissions | null;
  lastLoginAt?: string;
  passwordUpdatedAt?: string;
}

export interface CreateStaffRequest {
  email: string;
  fullName: string;
  role: StaffRole;
  permissions: ModulePermissions;
}

export interface UpdateStaffRequest {
  email?: string;
  fullName?: string;
  role?: StaffRole;
  status?: StaffStatus;
  permissions?: ModulePermissions;
}

export interface SetPasswordRequest {
  token: string;
  password: string;
}
