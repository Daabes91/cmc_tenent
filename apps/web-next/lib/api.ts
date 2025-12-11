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
  PublicCarousel,
  Product,
  CartResponse,
} from './types';
import { locales } from '@/i18n/request';
import { withBasePath, stripBasePath } from '@/utils/basePath';
import {
  getDefaultTenantSlug,
  getTenantSlugClient,
  TENANT_COOKIE,
  TENANT_HEADER,
} from './tenant';

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

export class TenantNotFoundError extends APIError {
  constructor(public tenant: string) {
    super(`Tenant not found: ${tenant}`, 404);
    this.name = 'TenantNotFoundError';
  }
}

const CART_SESSION_KEY = 'cartSessionId';

function getCartSessionId(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(CART_SESSION_KEY);
}

function setCartSessionId(id?: string | null) {
  if (typeof window === 'undefined') return;
  if (id) {
    localStorage.setItem(CART_SESSION_KEY, id);
  } else {
    localStorage.removeItem(CART_SESSION_KEY);
  }
}

function clearTenantCookie() {
  if (typeof document === 'undefined') return;
  document.cookie = `${TENANT_COOKIE}=; path=/; max-age=0; sameSite=lax`;
}

function applyTenantFallback(tenantSlug: string) {
  if (typeof window === 'undefined') return;

  // Clear the bad slug and fall back to the default tenant so we stop looping bad requests
  clearTenantCookie();
  const fallback = getDefaultTenantSlug();
  document.cookie = `${TENANT_COOKIE}=${encodeURIComponent(fallback)}; path=/; sameSite=lax`;

  // Send the user to the no-tenant page for a clear message
  window.location.href = withBasePath('/_errors/no-tenant');
  throw new TenantNotFoundError(tenantSlug);
}

async function fetchAPI<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('authToken') : null;

  const headers = new Headers({
    'Content-Type': 'application/json',
  });

  if (options.headers) {
    new Headers(options.headers as HeadersInit).forEach((value, key) => {
      headers.set(key, value);
    });
  }

  const tenantSlug = getTenantSlugClient();
  if (tenantSlug) {
    headers.set(TENANT_HEADER, tenantSlug);
  }

  if (token && token !== 'null' && token !== 'undefined') {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_URL}${endpoint}`, {
    credentials: options.credentials,
    ...options,
    headers,
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));

    // If settings request comes back 404, assume the tenant slug is invalid and fall back cleanly
    if (response.status === 404 && endpoint === '/settings' && tenantSlug) {
      applyTenantFallback(tenantSlug);
    }

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
  // Carousels (public)
  getCarousels: async () => fetchAPI<PublicCarousel[]>('/carousels/all'),

  // Public products (basic listing)
  getProducts: async (params?: { page?: number; size?: number; search?: string; status?: string }) => {
    const searchParams = new URLSearchParams();
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size !== undefined) searchParams.set('size', String(params.size));
    if (params?.search) searchParams.set('search', params.search);
    if (params?.status) searchParams.set('status', params.status);
    const qs = searchParams.toString();
    return fetchAPI<{ items: Product[]; page: number; size: number; total: number }>(
      `/products${qs ? `?${qs}` : ''}`
    );
  },
  getProductBySlug: async (slug: string) => fetchAPI<Product>(`/products/by-slug/${slug}`),

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

  // Cart
  addToCart: async (data: { product_id: number; quantity: number; variant_id?: number | null }) => {
    const slug = getTenantSlugClient();
    const sessionId = getCartSessionId();
    const search = new URLSearchParams();
    if (slug) search.set('slug', slug);
    if (sessionId) search.set('session_id', sessionId);
    const suffix = search.toString() ? `?${search.toString()}` : '';
    const res = await fetchAPI<CartResponse>(`/cart/items${suffix}`, {
      method: 'POST',
      credentials: 'include',
      body: JSON.stringify(data),
    });
    if ((res as any)?.session_id) {
      setCartSessionId((res as any).session_id);
    }
    return res;
  },
  getCart: async () => {
    const slug = getTenantSlugClient();
    const sessionId = getCartSessionId();
    const search = new URLSearchParams();
    if (slug) search.set('slug', slug);
    if (sessionId) search.set('session_id', sessionId);
    const suffix = search.toString() ? `?${search.toString()}` : '';
    const res = await fetchAPI<CartResponse>(`/cart${suffix}`, { credentials: 'include' });
    if ((res as any)?.session_id) {
      setCartSessionId((res as any).session_id);
    }
    return res;
  },
};

export { APIError };
