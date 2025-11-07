export type DoctorServiceReference = {
  id: number;
  slug: string;
  nameEn: string | null;
  nameAr?: string | null;
};

export type DoctorAvailability = {
  id: number;
  type: "WEEKLY" | "ONE_TIME" | string;
  dayOfWeek: string | null;
  date: string | null;
  startTime: string;
  endTime: string;
  createdAt: string;
  updatedAt: string;
};

export type DoctorAdmin = {
  id: number;
  fullNameEn: string;
  fullNameAr: string | null;
  specialtyEn: string | null;
  specialtyAr: string | null;
  bioEn: string | null;
  bioAr: string | null;
  imageUrl: string | null;
  locales: string[];
  services: DoctorServiceReference[];
  createdAt: string;
  // Backward compatibility fields (will be removed after migration)
  fullName?: string;
  specialty?: string | null;
  bio?: string | null;
};
