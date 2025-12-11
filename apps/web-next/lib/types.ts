// Patient types
export interface Patient {
  id: number;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  externalId: string;
  createdAt: string;
  profileImageUrl?: string | null;
  dateOfBirth?: string | null;
}

export interface PatientAuthResponse {
  tokenType: string;
  accessToken: string;
  accessTokenExpiresAt: string;
  patient: {
    id: number;
    externalId: string;
    firstName: string;
    lastName: string;
    email: string;
    profileImageUrl?: string | null;
    dateOfBirth?: string | null;
  };
}

export type AuthUser = PatientAuthResponse['patient'] & {
  name?: string;
};

export interface PatientProfileResponse {
  id: number;
  externalId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  profileImageUrl?: string | null;
  dateOfBirth?: string | null;
  googleId?: string | null;
  googleEmail?: string | null;
  authProvider?: string;
  hasPassword?: boolean;
}

export interface PatientProfileUpdateRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  profileImageUrl?: string | null;
  dateOfBirth?: string | null;
}

export interface PatientLoginRequest {
  email: string;
  password: string;
}

export interface PatientSignupRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  password: string;
  dateOfBirth?: string | null;
}

// Service types
export interface Service {
  slug: string;
  name: string;
  summary: string;
}

// Doctor types
export interface Doctor {
  id: number;
  name: string;
  specialty: string | null;
  bio: string | null;
  imageUrl: string | null;
  languages: string[];
  services: Service[];
}

// Appointment/Booking types
export interface AvailabilitySlot {
  doctorId: number;
  doctorName: string;
  start: string; // ISO datetime
  end: string;   // ISO datetime
}

export interface BookingRequest {
  patientId: number;
  doctorId: number;
  serviceSlug: string;
  scheduledAt: string; // ISO datetime
  bookingMode: 'CLINIC_VISIT' | 'VIRTUAL_CONSULTATION';
}

export interface Appointment {
  id: number;
  patient: {
    id: number;
    name: string;
  };
  doctor: {
    id: number;
    name: string;
  };
  service: {
    id: number;
    name: string;
  };
  scheduledAt: string;
  status: 'SCHEDULED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
  bookingMode: 'CLINIC_VISIT' | 'VIRTUAL_CONSULTATION';
  createdAt: string;
}

export interface PatientAppointment {
  id: number;
  service: string;
  doctor: {
    id: number;
    name: string;
    specialization: string;
  };
  scheduledAt: string;
  status: 'SCHEDULED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
  bookingMode: 'CLINIC_VISIT' | 'VIRTUAL_CONSULTATION';
  notes: string | null;
  createdAt: string;
}

// Treatment Plan types
export interface TreatmentPlan {
  id: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  treatmentTypeId: number;
  treatmentTypeName: string;
  totalPrice: number;
  currency: string;
  plannedFollowups: number;
  followUpCadence: 'WEEKLY' | 'BIWEEKLY' | 'MONTHLY' | 'AS_NEEDED';
  completedVisits: number;
  status: 'PLANNING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  notes: string | null;
  discountAmount: number | null;
  discountReason: string | null;
  createdAt: string;
  updatedAt: string;
  startedAt: string | null;
  completedAt: string | null;

  // Dynamic calculations
  totalPaid: number;
  remainingBalance: number;
  remainingVisits: number;
  expectedPaymentPerVisit: number;
  totalMaterialsCost: number;
  netRevenue: number;

  // Related data
  followUpVisits: FollowUpVisit[];
  scheduledFollowUps: ScheduledFollowUp[];
}

export interface FollowUpVisit {
  id: number;
  visitNumber: number;
  visitDate: string;
  notes: string | null;
  payments: Payment[];
  materialsUsed: MaterialUsage[];
}

export interface Payment {
  id: number;
  amount: number;
  paymentMethod: 'CASH' | 'CREDIT_CARD' | 'INSURANCE';
  notes: string | null;
  paidAt: string;
}

export interface MaterialUsage {
  id: number;
  materialName: string;
  quantity: number;
  unitCost: number;
  totalCost: number;
}

export interface ScheduledFollowUp {
  appointmentId: number;
  visitNumber: number;
  scheduledAt: string;
  status: 'SCHEDULED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
  bookingMode: 'CLINIC_VISIT' | 'VIRTUAL_CONSULTATION';
  paymentCollected: boolean;
  patientAttended: boolean | null;
}

// Clinic Settings types
export interface LocalizedText {
  en?: string | null;
  ar?: string | null;
}

export interface WhyChooseFeatureContent {
  key?: string | null;
  icon?: string | null;
  title?: LocalizedText | null;
  description?: LocalizedText | null;
}

export interface WhyChooseContent {
  title?: LocalizedText | null;
  subtitle?: LocalizedText | null;
  features?: WhyChooseFeatureContent[];
}

export interface ClinicSettings {
  id: number;
  clinicName: string;
  tagline?: string;
  phone: string;
  email: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  logoUrl?: string | null;
  faviconUrl?: string | null;
  faviconImageId?: string | null;
  slotDurationMinutes: number;
  workingHours: {
    monday: string;
    tuesday: string;
    wednesday: string;
    thursday: string;
    friday: string;
    saturday: string;
    sunday: string;
  };
  socialMedia: {
    facebook: string;
    instagram: string;
    twitter: string;
    linkedin: string;
  };
  virtualConsultationMeetingLink?: string | null;
  heroMediaType?: 'image' | 'video';
  heroImageUrl?: string | null;
  heroVideoId?: string | null;
  whyChoose?: WhyChooseContent;
  ecommerceEnabled?: boolean;
}

// Hero Media types
export interface HeroMedia {
  type: 'image' | 'video';
  imageUrl?: string;
  videoId?: string;
}

// Blog types
export interface Blog {
  id: number;
  title: string;
  slug: string;
  excerpt: string | null;
  content: string;
  featuredImage: string | null;
  authorName: string | null;

  // SEO fields
  metaTitle: string | null;
  metaDescription: string | null;
  ogImage: string | null;

  publishedAt: string | null;
  viewCount: number;
  locale: string;
  createdAt: string;
}

// Insurance Company types
export interface InsuranceCompany {
  id: number;
  name: string;
  logoUrl: string | null;
  websiteUrl: string | null;
  description: string | null;
}

// E-commerce types
export interface ProductImage {
  id: number;
  imageUrl: string;
  altText?: string | null;
  isMain?: boolean | null;
  sortOrder?: number | null;
}

export interface Product {
  id: number;
  name: string;
  slug: string;
  description?: string | null;
  shortDescription?: string | null;
  price?: number | null;
  compareAtPrice?: number | null;
  currency?: string | null;
  images?: ProductImage[];
  mainImageUrl?: string | null;
  status?: string;
  isVisible?: boolean;
}

export type CarouselPlacement =
  | 'HEADER'
  | 'HERO'
  | 'SIDEBAR'
  | 'FOOTER'
  | 'CATEGORY_PAGE'
  | 'PRODUCT_PAGE'
  | 'HOME_PAGE'
  | string;

export interface PublicCarouselItem {
  id: number;
  contentType: 'IMAGE' | 'PRODUCT' | 'CATEGORY' | 'BRAND' | 'OFFER' | 'VIEW_ALL_PRODUCTS';
  title?: string | null;
  subtitle?: string | null;
  imageUrl?: string | null;
  linkUrl?: string | null;
  ctaText?: string | null;
  sortOrder?: number | null;
  product?: {
    id: number;
    name: string;
    slug: string;
    price?: number | null;
    compareAtPrice?: number | null;
    currency?: string | null;
    shortDescription?: string | null;
    mainImageUrl?: string | null;
    images?: string[];
    inStock?: boolean | null;
  };
}

export interface PublicCarousel {
  id: number;
  name: string;
  slug: string;
  type: 'PRODUCT' | 'VIEW_ALL_PRODUCTS' | 'IMAGE' | 'CATEGORY' | 'BRAND' | 'OFFER' | 'TESTIMONIAL' | 'BLOG' | 'MIXED';
  placement: CarouselPlacement;
  platform?: 'WEB' | 'MOBILE' | string;
  maxItems?: number | null;
  items: PublicCarouselItem[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CartResponse {
  id?: number | null;
  session_id?: string | null;
  items?: Array<{
    id: number;
    productId: number;
    quantity: number;
    product?: Product;
    price?: number;
    currency?: string;
  }>;
  subtotal?: any;
  tax_amount?: any;
  total_amount?: any;
  item_count?: number;
  total_quantity?: number;
}
