export type AdminServiceSummary = {
  id: number;
  slug: string;
  nameEn: string;
  nameAr: string | null;
  summaryEn: string | null;
  summaryAr: string | null;
  createdAt: string;
  doctorCount: number;
};
