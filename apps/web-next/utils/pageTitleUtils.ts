/**
 * Utility functions for mapping routes to page titles in web-next application
 * Used for dynamic browser tab titles in "Clinic Name | Page Title" format
 */

export interface PageTitleMapping {
  [key: string]: string;
}

/**
 * Static route to page title mappings for public-facing pages
 */
const routeTitleMap: PageTitleMapping = {
  // Root and home pages
  '/': 'Home',
  '/en': 'Home',
  '/ar': 'الرئيسية',
  
  // Main public pages
  '/en/services': 'Our Services',
  '/ar/services': 'خدماتنا',
  '/en/doctors': 'Our Doctors',
  '/ar/doctors': 'أطباؤنا',
  '/en/blog': 'Blog',
  '/ar/blog': 'المدونة',
  '/en/appointments': 'Book Appointment',
  '/ar/appointments': 'حجز موعد',
  
  // Auth pages
  '/en/login': 'Login',
  '/ar/login': 'تسجيل الدخول',
  '/en/signup': 'Sign Up',
  '/ar/signup': 'إنشاء حساب',
  
  // Dashboard pages (patient portal)
  '/en/dashboard': 'Dashboard',
  '/ar/dashboard': 'لوحة التحكم',
  '/en/dashboard/appointments': 'My Appointments',
  '/ar/dashboard/appointments': 'مواعيدي',
  
  // PayPal test pages (development)
  '/paypal-test': 'PayPal Test',
  '/paypal-debug': 'PayPal Debug'
};

/**
 * Get page title from route path
 * Handles both static routes and dynamic routes with parameters
 * 
 * @param path - The current route path
 * @param locale - Current locale (en/ar) for localized titles
 * @returns The appropriate page title for the route
 */
export function getPageTitleFromRoute(path: string, locale: string = 'en'): string {
  // Remove trailing slash for consistency
  const normalizedPath = path.endsWith('/') && path.length > 1 
    ? path.slice(0, -1) 
    : path;
  
  // Check for exact match first
  if (routeTitleMap[normalizedPath]) {
    return routeTitleMap[normalizedPath];
  }
  
  // Handle dynamic routes with parameters
  const dynamicRouteTitle = getDynamicRouteTitle(normalizedPath, locale);
  if (dynamicRouteTitle) {
    return dynamicRouteTitle;
  }
  
  // Fallback to default based on locale
  return locale === 'ar' ? 'الرئيسية' : 'Home';
}

/**
 * Handle dynamic routes that contain parameters like [id] or [slug]
 * 
 * @param path - The route path with parameters
 * @param locale - Current locale for localized titles
 * @returns The appropriate title for dynamic routes
 */
function getDynamicRouteTitle(path: string, locale: string): string | null {
  // Doctor detail pages: /en/doctors/[id] or /ar/doctors/[id]
  if (path.match(/\/(en|ar)\/doctors\/\d+$/)) {
    return locale === 'ar' ? 'تفاصيل الطبيب' : 'Doctor Details';
  }
  
  // Blog post pages: /en/blog/[slug] or /ar/blog/[slug]
  if (path.match(/\/(en|ar)\/blog\/[^\/]+$/)) {
    return locale === 'ar' ? 'مقال' : 'Blog Post';
  }
  
  // Service detail pages: /en/services/[slug] or /ar/services/[slug]
  if (path.match(/\/(en|ar)\/services\/[^\/]+$/)) {
    return locale === 'ar' ? 'تفاصيل الخدمة' : 'Service Details';
  }
  
  // Check for base route patterns without exact matches
  if (path.includes('/doctors')) {
    return locale === 'ar' ? 'أطباؤنا' : 'Our Doctors';
  }
  
  if (path.includes('/services')) {
    return locale === 'ar' ? 'خدماتنا' : 'Our Services';
  }
  
  if (path.includes('/blog')) {
    return locale === 'ar' ? 'المدونة' : 'Blog';
  }
  
  if (path.includes('/appointments')) {
    return locale === 'ar' ? 'حجز موعد' : 'Book Appointment';
  }
  
  if (path.includes('/dashboard')) {
    return locale === 'ar' ? 'لوحة التحكم' : 'Dashboard';
  }
  
  if (path.includes('/login')) {
    return locale === 'ar' ? 'تسجيل الدخول' : 'Login';
  }
  
  if (path.includes('/signup')) {
    return locale === 'ar' ? 'إنشاء حساب' : 'Sign Up';
  }
  
  return null;
}

/**
 * Format the complete browser tab title with clinic name
 * 
 * @param pageTitle - The page-specific title
 * @param clinicName - The clinic name from settings
 * @returns Formatted title in "Clinic Name | Page Title" format
 * @deprecated Use createSafeWebTitle from titleUtils for enhanced fallback handling
 */
export function formatWebTitle(pageTitle: string, clinicName?: string): string {
  const defaultClinicName = 'Clinic';
  const name = clinicName?.trim() || defaultClinicName;
  
  return `${name} | ${pageTitle}`;
}

