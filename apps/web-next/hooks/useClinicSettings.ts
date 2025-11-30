'use client';

/**
 * Hook for accessing clinic settings in web-next application
 * Provides clinic data for dynamic branding (name, logo, etc.)
 */


import useSWR from 'swr';
import { api } from '@/lib/api';
import type { ClinicSettings } from '@/lib/types';

interface UseClinicSettingsReturn {
  clinicSettings: ClinicSettings | null;
  isLoading: boolean;
  error: Error | null;
  refetch: () => void;
}

/**
 * Custom hook to fetch and manage clinic settings
 * Uses SWR for caching and automatic revalidation
 * 
 * @returns Object containing clinic settings, loading state, error, and refetch function
 */
export function useClinicSettings(): UseClinicSettingsReturn {
  const {
    data: clinicSettings,
    error,
    isLoading,
    mutate
  } = useSWR<ClinicSettings>(
    'clinic-settings',
    api.getClinicSettings,
    {
      // Cache for 5 minutes
      revalidateOnFocus: false,
      revalidateOnReconnect: true,
      dedupingInterval: 300000, // 5 minutes
      errorRetryCount: 3,
      errorRetryInterval: 5000, // 5 seconds
    }
  );

  return {
    clinicSettings: clinicSettings || null,
    isLoading,
    error: error || null,
    refetch: mutate
  };
}

/**
 * Hook specifically for clinic branding data (name and logo)
 * Provides fallback values and comprehensive error handling
 * 
 * @returns Object with clinic name, logo URL, and loading state
 */
export function useClinicBranding() {
  const { clinicSettings, isLoading, error } = useClinicSettings();
  
  // Use enhanced safe clinic name handling
  const clinicName = getSafeClinicName(clinicSettings?.clinicName);
  const logoUrl = clinicSettings?.logoUrl?.trim() || null;
  const faviconUrl = clinicSettings?.faviconUrl?.trim() || null;
  
  // Validate logo URL with enhanced validation
  const validLogoUrl = logoUrl ? validateImageUrl(logoUrl) : null;
  const validFaviconUrl = faviconUrl ? validateImageUrl(faviconUrl) : null;
  
  return {
    clinicName,
    logoUrl: validLogoUrl,
    faviconUrl: validFaviconUrl,
    isLoading,
    error,
    hasCustomBranding: !!(clinicSettings?.clinicName || clinicSettings?.logoUrl || clinicSettings?.faviconUrl)
  };
}

/**
 * Hook for safe branding configuration with comprehensive error handling
 * 
 * @param pageTitle - Current page title
 * @param locale - Current locale (en/ar)
 * @returns Safe branding configuration
 */
export function useSafeBrandingConfig(pageTitle?: string, locale: string = 'en') {
  const { clinicSettings, isLoading, error } = useClinicSettings();
  
  // Handle API errors
  if (error) {
    return {
      ...handleApiFailure(error, locale, logBrandingError),
      isLoading,
      error
    };
  }
  
  // Create safe branding configuration
  const brandingConfig = createSafeBrandingFromSettings(
    pageTitle,
    clinicSettings,
    locale,
    logBrandingError
  );
  
  return {
    ...brandingConfig,
    isLoading,
    error: null
  };
}


import { getSafeClinicName } from '@/utils/titleUtils';
import { createSafeBrandingFromSettings, logBrandingError, handleApiFailure } from '@/utils/brandingErrorHandler';

/**
 * Validate image URL format
 * 
 * @param url - The URL to validate
 * @returns Valid URL or null if invalid
 */
function validateImageUrl(url: string): string | null {
  if (!url?.trim()) {
    return null;
  }
  
  try {
    new URL(url);
    return url;
  } catch {
    return null;
  }
}
