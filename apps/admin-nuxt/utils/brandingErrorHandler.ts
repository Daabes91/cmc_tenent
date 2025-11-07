/**
 * Error handling utilities for dynamic branding in admin-nuxt
 * Provides graceful fallbacks for API failures and other branding errors
 */

import { computed, type Ref } from 'vue';
import { createSafeAdminTitle } from './titleUtils';


/**
 * Branding error types
 */
export enum BrandingErrorType {
  API_FAILURE = 'api_failure',

  TITLE_UPDATE_FAILED = 'title_update_failed',
  SETTINGS_UNAVAILABLE = 'settings_unavailable'
}

/**
 * Branding error details
 */
export interface BrandingError {
  type: BrandingErrorType;
  message: string;
  originalError?: {
    name: string;
    message: string;
    stack?: string;
  } | null;
  timestamp: string;
}

function serializeError(error: unknown): { name: string; message: string; stack?: string } | null {
  if (!(error instanceof Error)) {
    return null;
  }
  return {
    name: error.name,
    message: error.message,
    stack: error.stack
  };
}

/**
 * Safe branding configuration with fallbacks
 */
export interface SafeBrandingConfig {
  title: string;
  hasErrors: boolean;
  errors: BrandingError[];
}

/**
 * Create safe branding configuration with comprehensive error handling
 * 
 * @param pageName - Current page name
 * @param clinicSettings - Clinic settings (may be null/undefined)
 * @param onError - Optional error callback
 * @returns Safe branding configuration
 */
export function createSafeBrandingConfig(
  pageName?: string | null,
  clinicSettings?: any,
  onError?: (error: BrandingError) => void
): SafeBrandingConfig {
  const errors: BrandingError[] = [];
  
  // Handle title with performance monitoring
  let title: string;
  try {
    title = monitorTitleUpdatePerformance(
      pageName || 'Dashboard',
      () => createSafeAdminTitle(pageName)
    );
  } catch (error) {
    const brandingError: BrandingError = {
      type: BrandingErrorType.TITLE_UPDATE_FAILED,
      message: 'Failed to create admin title, using fallback',
      originalError: serializeError(error instanceof Error ? error : new Error('Unknown title error')),
      timestamp: new Date().toISOString()
    };
    errors.push(brandingError);
    if (onError) onError(brandingError);
    
    title = 'Admin | Dashboard'; // Absolute fallback
  }
  

  
  // Check if clinic settings are unavailable and log detailed information
  if (!clinicSettings) {
    const brandingError: BrandingError = {
      type: BrandingErrorType.SETTINGS_UNAVAILABLE,
      message: 'Clinic settings unavailable, using defaults',
      timestamp: new Date().toISOString()
    };
    errors.push(brandingError);
    if (onError) onError(brandingError);
    
    // Log clinic settings unavailability
    logClinicSettingsFailure(
      new Error('Clinic settings object is null or undefined'),
      0
    );
  }
  
  return {
    title,
    hasErrors: errors.length > 0,
    errors
  };
}

/**
 * Handle API failures gracefully
 * 
 * @param error - The API error
 * @param onError - Optional error callback
 * @returns Default branding configuration
 */
export function handleApiFailure(
  error: Error,
  onError?: (error: BrandingError) => void
): SafeBrandingConfig {
  const brandingError: BrandingError = {
    type: BrandingErrorType.API_FAILURE,
    message: 'Failed to load clinic settings from API',
    originalError: serializeError(error),
    timestamp: new Date().toISOString()
  };
  
  if (onError) onError(brandingError);
  
  return {
    title: 'Admin | Dashboard',
    hasErrors: true,
    errors: [brandingError]
  };
}

/**
 * Performance metrics for title updates
 */
interface TitleUpdateMetrics {
  startTime: number;
  endTime: number;
  duration: number;
  pageName: string;
  success: boolean;
  errorType?: BrandingErrorType;
}



/**
 * Clinic settings loading metrics
 */
interface ClinicSettingsMetrics {
  startTime: number;
  endTime?: number;
  duration?: number;
  success: boolean;
  errorMessage?: string;
  retryCount: number;
}

/**
 * Enhanced error logging with structured data
 * 
 * @param error - The branding error to log
 */
export function logBrandingError(error: BrandingError): void {
  const logLevel = error.type === BrandingErrorType.API_FAILURE ? 'error' : 'warn';
  
  // Structured logging with additional context
  const logData = {
    type: 'branding_error',
    errorType: error.type,
    message: error.message,
    timestamp: error.timestamp,
    userAgent: typeof navigator !== 'undefined' ? navigator.userAgent : 'unknown',
    url: typeof window !== 'undefined' ? window.location.href : 'unknown',
    originalError: error.originalError ?? null
  };
  
  console[logLevel](`[Branding ${error.type}] ${error.message}`, logData);
  
  // Send to external logging service if available
  if (typeof window !== 'undefined' && (window as any).gtag) {
    (window as any).gtag('event', 'branding_error', {
      event_category: 'error',
      event_label: error.type,
      value: 1
    });
  }
}



/**
 * Log clinic settings loading issues with retry information
 * 
 * @param error - The settings loading error
 * @param retryCount - Number of retry attempts
 * @param duration - Time taken for the failed request
 */
export function logClinicSettingsFailure(
  error: Error,
  retryCount: number = 0,
  duration?: number
): void {
  const logData = {
    type: 'clinic_settings_failure',
    errorMessage: error.message,
    retryCount,
    duration,
    timestamp: new Date().toISOString(),
    stack: error.stack
  };
  
  console.error('[Clinic Settings] Failed to load clinic settings', logData);
  
  // Track settings loading failures
  if (typeof window !== 'undefined' && (window as any).gtag) {
    (window as any).gtag('event', 'settings_load_error', {
      event_category: 'api',
      event_label: 'clinic_settings_failure',
      value: retryCount
    });
  }
}

/**
 * Monitor and log title update performance
 * 
 * @param pageName - The page name being updated
 * @param updateFunction - The title update function to monitor
 * @returns The result of the update function
 */
export function monitorTitleUpdatePerformance<T>(
  pageName: string,
  updateFunction: () => T
): T {
  const startTime = performance.now();
  let success = false;
  let errorType: BrandingErrorType | undefined;
  
  try {
    const result = updateFunction();
    success = true;
    return result;
  } catch (error) {
    errorType = BrandingErrorType.TITLE_UPDATE_FAILED;
    throw error;
  } finally {
    const endTime = performance.now();
    const duration = endTime - startTime;
    
    const metrics: TitleUpdateMetrics = {
      startTime,
      endTime,
      duration,
      pageName,
      success,
      errorType
    };
    
    // Log performance metrics
    if (duration > 100) { // Log slow title updates (>100ms)
      console.warn('[Title Performance] Slow title update detected', metrics);
    }
    
    // Track title update performance
    if (typeof window !== 'undefined' && (window as any).gtag) {
      (window as any).gtag('event', 'title_update_performance', {
        event_category: 'performance',
        event_label: success ? 'success' : 'failure',
        value: Math.round(duration)
      });
    }
  }
}



/**
 * Monitor clinic settings loading performance
 * 
 * @param loadFunction - The settings loading function to monitor
 * @param retryCount - Current retry attempt number
 * @returns Promise with the result and metrics
 */
export async function monitorClinicSettingsLoading<T>(
  loadFunction: () => Promise<T>,
  retryCount: number = 0
): Promise<{ result: T; metrics: ClinicSettingsMetrics }> {
  const startTime = performance.now();
  let success = false;
  let errorMessage: string | undefined;
  
  try {
    const result = await loadFunction();
    success = true;
    const endTime = performance.now();
    
    const metrics: ClinicSettingsMetrics = {
      startTime,
      endTime,
      duration: endTime - startTime,
      success,
      retryCount
    };
    
    // Log successful load times if they're slow
    if (metrics.duration && metrics.duration > 3000) {
      console.warn('[Settings Performance] Slow clinic settings loading', metrics);
    }
    
    return { result, metrics };
  } catch (error) {
    success = false;
    errorMessage = error instanceof Error ? error.message : 'Unknown error';
    
    const endTime = performance.now();
    const metrics: ClinicSettingsMetrics = {
      startTime,
      endTime,
      duration: endTime - startTime,
      success,
      errorMessage,
      retryCount
    };
    
    // Log the failure
    logClinicSettingsFailure(
      error instanceof Error ? error : new Error(errorMessage),
      retryCount,
      metrics.duration
    );
    
    throw error;
  }
}

/**
 * Create error-resilient branding composable
 * 
 * @param pageName - Current page name
 * @param clinicSettings - Clinic settings
 * @returns Safe branding configuration with error handling
 */
export function useSafeBranding(
  pageName: Ref<string>,
  clinicSettings: Ref<any>
) {
  const brandingConfig = computed(() => {
    return createSafeBrandingConfig(
      pageName.value,
      clinicSettings.value,
      logBrandingError
    );
  });
  
  return {
    title: computed(() => brandingConfig.value.title),
    hasErrors: computed(() => brandingConfig.value.hasErrors),
    errors: computed(() => brandingConfig.value.errors)
  };
}
