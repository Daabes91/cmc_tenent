export type PatientAdmin = {
  id: number;
  externalId: string;
  firstName: string;
  lastName: string;
  email: string | null;
  phone: string | null;
  createdAt: string;
  profileImageUrl?: string | null;
  dateOfBirth?: string | number | null | Record<string, unknown> | Array<number | string>;
};
