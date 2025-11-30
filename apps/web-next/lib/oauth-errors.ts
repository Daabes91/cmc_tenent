/**
 * OAuth Error Handling Utilities
 * 
 * Provides centralized error classification, logging, and user-friendly
 * error messages for Google OAuth authentication flow.
 */

export enum OAuthErrorType {
  // User-initiated errors
  USER_CANCELLED = 'USER_CANCELLED',
  
  // Network errors
  NETWORK_ERROR = 'NETWORK_ERROR',
  TIMEOUT = 'TIMEOUT',
  
  // OAuth flow errors
  INVALID_STATE = 'INVALID_STATE',
  INVALID_REQUEST = 'INVALID_REQUEST',
  MISSING_PARAMS = 'MISSING_PARAMS',
  
  // Tenant context errors
  TENANT_NOT_FOUND = 'TENANT_NOT_FOUND',
  TENANT_CONTEXT_MISSING = 'TENANT_CONTEXT_MISSING',
  
  // Google service errors
  GOOGLE_UNAVAILABLE = 'GOOGLE_UNAVAILABLE',
  GOOGLE_API_ERROR = 'GOOGLE_API_ERROR',
  
  // Account errors
  EMAIL_NOT_VERIFIED = 'EMAIL_NOT_VERIFIED',
  ACCOUNT_LINKING_FAILED = 'ACCOUNT_LINKING_FAILED',
  
  // Generic errors
  UNKNOWN_ERROR = 'UNKNOWN_ERROR',
}

export interface OAuthError {
  type: OAuthErrorType;
  message: string;
  originalError?: Error | unknown;
  statusCode?: number;
  retryable: boolean;
  userMessage: string;
}

/**
 * Classify an error based on its characteristics
 */
export function classifyOAuthError(
  error: unknown,
  statusCode?: number,
  errorParam?: string | null
): OAuthError {
  // Handle OAuth error parameter from Google
  if (errorParam === 'access_denied') {
    return {
      type: OAuthErrorType.USER_CANCELLED,
      message: 'User cancelled OAuth flow',
      originalError: error,
      retryable: true,
      userMessage: 'auth.google.callback.errors.cancelled',
    };
  }

  if (errorParam) {
    return {
      type: OAuthErrorType.GOOGLE_API_ERROR,
      message: `Google OAuth error: ${errorParam}`,
      originalError: error,
      retryable: true,
      userMessage: 'auth.google.callback.errors.oauthError',
    };
  }

  // Handle HTTP status codes
  if (statusCode) {
    switch (statusCode) {
      case 401:
        return {
          type: OAuthErrorType.INVALID_STATE,
          message: 'Invalid OAuth state parameter',
          originalError: error,
          statusCode,
          retryable: true,
          userMessage: 'auth.google.callback.errors.invalidState',
        };
      
      case 400:
        return {
          type: OAuthErrorType.INVALID_REQUEST,
          message: 'Invalid OAuth request',
          originalError: error,
          statusCode,
          retryable: true,
          userMessage: 'auth.google.callback.errors.invalidRequest',
        };
      
      case 404:
        return {
          type: OAuthErrorType.TENANT_NOT_FOUND,
          message: 'Tenant not found',
          originalError: error,
          statusCode,
          retryable: false,
          userMessage: 'auth.google.callback.errors.tenantNotFound',
        };
      
      case 502:
      case 503:
        return {
          type: OAuthErrorType.GOOGLE_UNAVAILABLE,
          message: 'Google authentication service unavailable',
          originalError: error,
          statusCode,
          retryable: true,
          userMessage: 'auth.google.callback.errors.googleUnavailable',
        };
      
      case 504:
        return {
          type: OAuthErrorType.TIMEOUT,
          message: 'Request timeout',
          originalError: error,
          statusCode,
          retryable: true,
          userMessage: 'auth.google.callback.errors.network',
        };
    }
  }

  // Handle network errors
  if (error instanceof TypeError && error.message.includes('fetch')) {
    return {
      type: OAuthErrorType.NETWORK_ERROR,
      message: 'Network error during OAuth flow',
      originalError: error,
      retryable: true,
      userMessage: 'auth.google.callback.errors.network',
    };
  }

  // Handle missing parameters
  if (error instanceof Error && error.message.includes('missing')) {
    return {
      type: OAuthErrorType.MISSING_PARAMS,
      message: 'Missing required OAuth parameters',
      originalError: error,
      retryable: true,
      userMessage: 'auth.google.callback.errors.missingParams',
    };
  }

  // Handle tenant context errors
  if (error instanceof Error && error.message.toLowerCase().includes('tenant')) {
    return {
      type: OAuthErrorType.TENANT_CONTEXT_MISSING,
      message: 'Tenant context not found',
      originalError: error,
      retryable: false,
      userMessage: 'auth.google.callback.errors.tenantContext',
    };
  }

  // Default to unknown error
  return {
    type: OAuthErrorType.UNKNOWN_ERROR,
    message: error instanceof Error ? error.message : 'Unknown error',
    originalError: error,
    retryable: true,
    userMessage: 'auth.google.callback.errors.generic',
  };
}

/**
 * Log OAuth error with appropriate level and context
 */
export function logOAuthError(error: OAuthError, context?: Record<string, unknown>): void {
  const logData = {
    type: error.type,
    message: error.message,
    statusCode: error.statusCode,
    retryable: error.retryable,
    timestamp: new Date().toISOString(),
    ...context,
  };

  // Log security-related errors at higher severity
  const securityErrors = [
    OAuthErrorType.INVALID_STATE,
    OAuthErrorType.INVALID_REQUEST,
  ];

  if (securityErrors.includes(error.type)) {
    console.error('[OAuth Security Error]', logData, error.originalError);
  } else if (error.type === OAuthErrorType.USER_CANCELLED) {
    // User cancellation is informational, not an error
    console.info('[OAuth User Action]', logData);
  } else if (error.retryable) {
    console.warn('[OAuth Retryable Error]', logData, error.originalError);
  } else {
    console.error('[OAuth Error]', logData, error.originalError);
  }
}

/**
 * Determine if an error should trigger an automatic retry
 */
export function shouldRetryOAuthError(error: OAuthError): boolean {
  return error.retryable && [
    OAuthErrorType.NETWORK_ERROR,
    OAuthErrorType.TIMEOUT,
    OAuthErrorType.GOOGLE_UNAVAILABLE,
  ].includes(error.type);
}

/**
 * Get redirect path based on error type
 */
export function getErrorRedirectPath(error: OAuthError): string {
  switch (error.type) {
    case OAuthErrorType.TENANT_NOT_FOUND:
    case OAuthErrorType.TENANT_CONTEXT_MISSING:
      return '/';
    
    case OAuthErrorType.USER_CANCELLED:
    case OAuthErrorType.INVALID_STATE:
    case OAuthErrorType.INVALID_REQUEST:
    case OAuthErrorType.GOOGLE_UNAVAILABLE:
    case OAuthErrorType.NETWORK_ERROR:
    default:
      return '/login';
  }
}
