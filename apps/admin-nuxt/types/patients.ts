export type PatientAdmin = {
  id: number;
  externalId: string;
  firstName: string;
  lastName: string;
  email: string | null;
  phone: string | null;
  createdAt: string;
  profileImageUrl?: string | null;
  driveFolderUrl?: string | null;
  dateOfBirth?: string | number | null | Record<string, unknown> | Array<number | string>;
  notes?: string | null;
};
