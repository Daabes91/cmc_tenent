import type { Blog, ClinicSettings, Doctor } from './types';

const API_URL =
  process.env.NEXT_SERVER_API_URL ||
  process.env.NEXT_PUBLIC_API_URL ||
  'http://localhost:8080/public';



/**
 * Server-side API utilities for Next.js App Router
 * These functions can be used in server components, generateMetadata, etc.
 */

/**
 * Fetch clinic settings on the server side
 * Used for server-side rendering and metadata generation
 * 
 * @returns Promise resolving to clinic settings or null if failed
 */
export async function getClinicSettingsServer(): Promise<ClinicSettings | null> {
  try {
    const response = await fetch(`${API_URL}/settings`, {
      // Add cache control for better performance
      next: { revalidate: 300 }, // Revalidate every 5 minutes
    });

    if (!response.ok) {
      console.error(`Failed to fetch clinic settings: ${response.status} ${response.statusText}`);
      return null;
    }

    const settings = await response.json();
    return settings;
  } catch (error) {
    console.error('Error fetching clinic settings on server:', error);
    return null;
  }
}

/**
 * Get safe clinic name with fallback
 * 
 * @param clinicName - Raw clinic name from settings
 * @returns Safe clinic name or default fallback
 */
export function getSafeClinicNameServer(clinicName?: string): string {
  if (!clinicName || typeof clinicName !== 'string') {
    return 'Clinic';
  }
  
  const trimmed = clinicName.trim();
  return trimmed || 'Clinic';
}

async function safeFetch<T>(url: string): Promise<T | null> {
  try {
    const response = await fetch(url, {
      next: { revalidate: 300 },
    });

    if (!response.ok) {
      console.error(`Failed to fetch ${url}: ${response.status} ${response.statusText}`);
      return null;
    }

    return (await response.json()) as T;
  } catch (error) {
    console.error(`Error fetching ${url}:`, error);
    return null;
  }
}

export async function getBlogsServer(locale?: string): Promise<Blog[]> {
  const localeParam = locale ? `?locale=${locale}` : '';
  const data = await safeFetch<Blog[]>(`${API_URL}/blogs${localeParam}`);
  return data ?? [];
}

export async function getBlogBySlugServer(slug: string): Promise<Blog | null> {
  if (!slug) return null;
  return safeFetch<Blog>(`${API_URL}/blogs/${slug}`);
}

export async function getDoctorServer(id: number, locale?: string): Promise<Doctor | null> {
  if (!id) return null;
  const localeParam = locale ? `?locale=${locale}` : '';
  return safeFetch<Doctor>(`${API_URL}/doctors/${id}${localeParam}`);
}
