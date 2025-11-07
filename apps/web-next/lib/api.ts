import type {
  PatientAuthResponse,
  PatientProfileResponse,
  PatientProfileUpdateRequest,
  ClinicSettings,
  Service,
  Doctor,
  AvailabilitySlot,
  TreatmentPlan,
  Blog,
  InsuranceCompany,
} from './types';
import {locales} from '@/i18n/request';
import {withBasePath, stripBasePath} from '@/utils/basePath';

const SUPPORTED_LOCALES = new Set(locales as readonly string[]);
const DEFAULT_LOCALE = locales[0] as (typeof locales)[number];

const API_URL =
  process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/public';

class APIError extends Error {
  constructor(message: string, public status: number, public data?: any) {
    super(message);
    this.name = 'APIError';
  }
}

async function fetchAPI<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('authToken') : null;

  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token && token !== 'null' && token !== 'undefined') {
    (headers as Record<string, string>).Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_URL}${endpoint}`, { ...options, headers });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));

    // Auto-logout on 401 Unauthorized (token expired or invalid)
    if (response.status === 401 && typeof window !== 'undefined') {
      localStorage.removeItem('authToken');
      localStorage.removeItem('authUser');
      localStorage.removeItem('tokenExpiry');

      const normalizedPath = stripBasePath(window.location.pathname);
      const pathSegments = normalizedPath.split('/').filter(Boolean);
      const localeCandidate = pathSegments[0];
      const activeLocale = SUPPORTED_LOCALES.has(localeCandidate ?? '')
        ? (localeCandidate as (typeof locales)[number])
        : DEFAULT_LOCALE;

      const isAuthRoute =
        normalizedPath.endsWith('/login') || normalizedPath.endsWith('/signup');

      if (!isAuthRoute) {
        window.location.href = `${withBasePath(`/${activeLocale}/login`)}?expired=true`;
      }
    }

    throw new APIError(
      errorData.message || `HTTP error! status: ${response.status}`,
      response.status,
      errorData
    );
  }

  return response.json();
}

export const api = {
  // Auth - Note: token storage is handled by saveAuth() in useAuth hook
  login: async (email: string, password: string) => {
    return fetchAPI<PatientAuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
  },

  signup: async (data: {
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    password: string;
  }) => {
    return fetchAPI<PatientAuthResponse>('/auth/signup', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // Services
  getServices: async (locale?: string) => {
    const localeParam = locale ? `?locale=${locale}` : '';
    return fetchAPI<Service[]>(`/services${localeParam}`);
  },

  // Clinic Settings (public endpoint)
  getClinicSettings: async () => fetchAPI<ClinicSettings>('/settings'),

  // Doctors
  getDoctors: async (locale?: string) => {
    const localeParam = locale ? `?locale=${locale}` : '';
    return fetchAPI<Doctor[]>(`/doctors${localeParam}`);
  },
  getDoctor: async (id: number, locale?: string) => {
    const localeParam = locale ? `?locale=${locale}` : '';
    return fetchAPI<Doctor>(`/doctors/${id}${localeParam}`);
  },

  // Availability
  getAvailabilitySlots: async (doctorId: number, serviceSlug: string, date: string) => {
    return fetchAPI<AvailabilitySlot[]>('/availability', {
      method: 'POST',
      body: JSON.stringify({ doctorId, serviceSlug, date }),
    });
  },

  // Booking (requires authentication - patientId comes from JWT token)
  createBooking: async (data: {
    doctorId: number;
    serviceSlug: string;
    slot: string;
    bookingMode: 'CLINIC_VISIT' | 'VIRTUAL_CONSULTATION';
    notes?: string;
  }) => {
    const token =
      typeof window !== 'undefined' ? localStorage.getItem('authToken') : null;

    if (!token) {
      throw new APIError('Authentication required to book appointments', 401);
    }

    return fetchAPI('/bookings', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    });
  },

  // Guest Booking (no authentication required)
  createGuestBooking: async (data: {
    serviceSlug: string;
    doctorId?: number;
    slot: string;
    consultationType: string;
    phoneNumber: string;
    guestEmail?: string;
    guestName?: string;
    notes?: string;
  }) => {
    return fetchAPI('/bookings/guest', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // Patient Appointments (requires authentication)
  getMyAppointments: async () => {
    const token =
      typeof window !== 'undefined' ? localStorage.getItem('authToken') : null;

    if (!token) {
      throw new APIError('Authentication required', 401);
    }

    return fetchAPI('/appointments/my', {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  },

  // Patient Profile (requires authentication)
  getMyProfile: async () => {
    const token =
      typeof window !== 'undefined' ? localStorage.getItem('authToken') : null;

    if (!token) {
      throw new APIError('Authentication required', 401);
    }

    return fetchAPI<PatientProfileResponse>('/patient/me', {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  },

  updateMyProfile: async (data: PatientProfileUpdateRequest) => {
    const token =
      typeof window !== 'undefined' ? localStorage.getItem('authToken') : null;

    if (!token) {
      throw new APIError('Authentication required', 401);
    }

    return fetchAPI<PatientProfileResponse>('/patient/me', {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    });
  },

  // Treatment Plans (requires authentication)
  getMyTreatmentPlans: async () => {
    const token =
      typeof window !== 'undefined' ? localStorage.getItem('authToken') : null;

    if (!token) {
      throw new APIError('Authentication required', 401);
    }

    return fetchAPI<TreatmentPlan[]>('/treatment-plans/my', {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  },

  // Blog Posts (public endpoints)
  getBlogs: async (locale?: string) => {
    const localeParam = locale ? `?locale=${locale}` : '';
    return fetchAPI<Blog[]>(`/blogs${localeParam}`);
  },

  getBlogBySlug: async (slug: string) => {
    return fetchAPI<Blog>(`/blogs/${slug}`);
  },

  // Insurance Companies (public endpoints)
  getInsuranceCompanies: async (locale?: string) => {
    const localeParam = locale ? `?locale=${locale}` : '';
    return fetchAPI<InsuranceCompany[]>(`/insurance-companies${localeParam}`);
  },

  // Image Upload (requires authentication)
  uploadProfileImage: async (file: File) => {
    const token =
      typeof window !== 'undefined' ? localStorage.getItem('authToken') : null;

    if (!token) {
      throw new APIError('Authentication required', 401);
    }

    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${API_URL}/patient/upload-profile-image`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
      },
      body: formData,
    });

    if (!response.ok) {
      let errorMessage = `Upload failed with status: ${response.status}`;

      try {
        const errorData = await response.json();
        if (errorData.message) {
          errorMessage = errorData.message;
        } else if (errorData.error) {
          errorMessage = `${errorData.error}: ${errorData.message || 'Unknown error'}`;
        }
      } catch (e) {
        // If JSON parsing fails, use the default error message
        console.error('Failed to parse error response:', e);
      }

      throw new APIError(errorMessage, response.status);
    }

    return response.json();
  },

  // PayPal Payment Methods
  getPaymentSettings: async () => {
    const response = await fetchAPI<{
      success: boolean;
      data: {
        virtualConsultationFee: number;
        currency: string;
        paypalClientId: string;
        paypalEnvironment: string;
      };
    }>('/payments/settings');
    return response.data;
  },

  createPayPalOrder: async (data: {
    patientId: number;
    doctorId: number;
    serviceId: number;
    slotId: string;
  }) => {
    const response = await fetchAPI<{
      success: boolean;
      data: { orderID: string };
    }>('/payments/paypal/create-order', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    return response.data;
  },

  capturePayPalOrder: async (orderID: string) => {
    const response = await fetchAPI<{
      success: boolean;
      data: { success: boolean; message: string };
    }>('/payments/paypal/capture', {
      method: 'POST',
      body: JSON.stringify({ orderID }),
    });
    return response.data;
  },
};

export { APIError };
