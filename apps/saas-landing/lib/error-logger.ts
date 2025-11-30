/**
 * Error Logging Utilities
 * 
 * Centralized error logging for monitoring and debugging
 */

export interface ErrorLogContext {
  component?: string;
  action?: string;
  userId?: string;
  metadata?: Record<string, any>;
}

export enum ErrorSeverity {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high',
  CRITICAL = 'critical',
}

/**
 * Log error to console and analytics
 */
export function logError(
  error: Error | string,
  context?: ErrorLogContext,
  severity: ErrorSeverity = ErrorSeverity.MEDIUM
) {
  const errorMessage = typeof error === 'string' ? error : error.message;
  const errorStack = typeof error === 'string' ? undefined : error.stack;

  // Console logging with context
  console.error('[Error]', {
    message: errorMessage,
    severity,
    context,
    stack: errorStack,
    timestamp: new Date().toISOString(),
  });

  // Log to Google Analytics if available
  if (typeof window !== 'undefined' && (window as any).gtag) {
    (window as any).gtag('event', 'exception', {
      description: errorMessage,
      fatal: severity === ErrorSeverity.CRITICAL,
      ...context,
    });
  }

  // In production, you might want to send to error tracking service
  // Example: Sentry, LogRocket, etc.
  if (process.env.NODE_ENV === 'production') {
    // Sentry.captureException(error, { contexts: { custom: context } });
  }
}

/**
 * Log warning (non-critical issues)
 */
export function logWarning(
  message: string,
  context?: ErrorLogContext
) {
  console.warn('[Warning]', {
    message,
    context,
    timestamp: new Date().toISOString(),
  });

  // Log to analytics
  if (typeof window !== 'undefined' && (window as any).gtag) {
    (window as any).gtag('event', 'warning', {
      description: message,
      ...context,
    });
  }
}

/**
 * Log info (for debugging and monitoring)
 */
export function logInfo(
  message: string,
  context?: ErrorLogContext
) {
  if (process.env.NODE_ENV === 'development') {
    console.info('[Info]', {
      message,
      context,
      timestamp: new Date().toISOString(),
    });
  }
}

/**
 * Create error logger for specific component
 */
export function createComponentLogger(componentName: string) {
  return {
    error: (error: Error | string, metadata?: Record<string, any>, severity?: ErrorSeverity) => {
      logError(error, { component: componentName, metadata }, severity);
    },
    warning: (message: string, metadata?: Record<string, any>) => {
      logWarning(message, { component: componentName, metadata });
    },
    info: (message: string, metadata?: Record<string, any>) => {
      logInfo(message, { component: componentName, metadata });
    },
  };
}

/**
 * Track error metrics
 */
export function trackErrorMetric(
  errorType: string,
  count: number = 1
) {
  if (typeof window !== 'undefined' && (window as any).gtag) {
    (window as any).gtag('event', 'error_metric', {
      error_type: errorType,
      count,
    });
  }
}
