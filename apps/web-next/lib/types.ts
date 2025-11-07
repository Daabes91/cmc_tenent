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
