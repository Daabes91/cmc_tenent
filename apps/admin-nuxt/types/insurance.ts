export interface InsuranceCompany {
  id: number
  nameEn: string
  nameAr?: string | null
  logoUrl?: string | null
  websiteUrl?: string | null
  phone?: string | null
  email?: string | null
  descriptionEn?: string | null
  descriptionAr?: string | null
  isActive: boolean
  displayOrder: number
  createdAt: string | number
  updatedAt: string | number
}

export interface InsuranceCompanyRequest {
  nameEn: string
  nameAr?: string | null
  logoUrl?: string | null
  websiteUrl?: string | null
  phone?: string | null
  email?: string | null
  descriptionEn?: string | null
  descriptionAr?: string | null
  isActive?: boolean
  displayOrder?: number
}

export interface InsuranceCompanyFormData {
  nameEn: string
  nameAr: string
  logoUrl: string
  websiteUrl: string
  phone: string
  email: string
  descriptionEn: string
  descriptionAr: string
  isActive: boolean
  displayOrder: number
}