/**
 * Sentry Error Monitoring Configuration
 * 
 * Provides error tracking and monitoring for production environments
 */

export interface SentryConfig {
  dsn: string;
  environment: string;
  tracesSampleRate: number;
  enabled: boolean;
}

export interface ErrorEvent {
  message: string;
  level: 'error' | 'warning' | 'info' | 'debug';
  tags?: Record<string, string>;
  extra?: Record<string, any>;
  user?: {
    id?: string;
    email?: string;
    username?: string;
  };
}

/**
 * Sentry-like error monitoring service
 * Can be replaced with actual Sentry SDK when DSN is available
 */
export class ErrorMonitoring {
  private static config: SentryConfig = {
    dsn: process.env.NEXT_PUBLIC_SENTRY_DSN || '',
    environment: process.env.NODE_ENV || 'development',
    tracesSampleRate: 0.1,
    enabled: process.env.NODE_ENV === 'production' && !!process.env.NEXT_PUBLIC_SENTRY_DSN,
  };

  private static isInitialized = false;

  /**
   * Initialize error monitoring
   */
  static init(config?: Partial<SentryConfig>): void {
    if (this.isInitialized) {
      console.warn('Error monitoring already initialized');
      return;
    }

    this.config = { ...this.config, ...config };

    if (!this.config.enabled) {
      console.log('Error monitoring disabled');
      return;
    }

    if (!this.config.dsn) {
      console.warn('Error monitoring DSN not configured');
      return;
    }

    // In production, this would initialize Sentry SDK:
    // Sentry.init({
    //   dsn: this.config.dsn,
    //   environment: this.config.environment,
    //   tracesSampleRate: this.config.tracesSampleRate,
    //   beforeSend(event) {
    //     // Filter out non-critical errors
    //     if (event.level === 'warning') {
    //       return null;
    //     }
    //     return event;
    //   },
    // });

    this.isInitialized = true;
    console.log('Error monitoring initialized:', this.config.environment);
  }

  /**
   * Capture exception
   */
  static captureException(error: Error, context?: Partial<ErrorEvent>): void {
    if (!this.config.enabled) {
      console.error('[Error Monitoring]', error, context);
      return;
    }

    // Log to console in development
    if (this.config.environment === 'development') {
      console.error('[Error Monitoring]', {
        error: error.message,
        stack: error.stack,
        context,
        timestamp: new Date().toISOString(),
      });
    }

    // In production, send to Sentry:
    // Sentry.captureException(error, {
    //   tags: context?.tags,
    //   extra: context?.extra,
    //   user: context?.user,
    //   level: context?.level,
    // });

    // Also track in Google Analytics
    if (typeof window !== 'undefined' && (window as any).gtag) {
      (window as any).gtag('event', 'exception', {
        description: error.message,
        fatal: context?.level === 'error',
        ...context?.tags,
      });
    }
  }

  /**
   * Capture message
   */
  static captureMessage(message: string, context?: Partial<ErrorEvent>): void {
    if (!this.config.enabled) {
      console.log('[Error Monitoring]', message, context);
      return;
    }

    // Log to console in development
    if (this.config.environment === 'development') {
      console.log('[Error Monitoring]', {
        message,
        level: context?.level || 'info',
        context,
        timestamp: new Date().toISOString(),
      });
    }

    // In production, send to Sentry:
    // Sentry.captureMessage(message, {
    //   level: context?.level || 'info',
    //   tags: context?.tags,
    //   extra: context?.extra,
    //   user: context?.user,
    // });
  }

  /**
   * Set user context
   */
  static setUser(user: ErrorEvent['user']): void {
    if (!this.config.enabled) {
      return;
    }

    // In production:
    // Sentry.setUser(user);
  }

  /**
   * Set tags
   */
  static setTags(tags: Record<string, string>): void {
    if (!this.config.enabled) {
      return;
    }

    // In production:
    // Sentry.setTags(tags);
  }

  /**
   * Add breadcrumb
   */
  static addBreadcrumb(breadcrumb: {
    message: string;
    category?: string;
    level?: ErrorEvent['level'];
    data?: Record<string, any>;
  }): void {
    if (!this.config.enabled) {
      return;
    }

    // In production:
    // Sentry.addBreadcrumb(breadcrumb);
  }

  /**
   * Check if monitoring is enabled
   */
  static isEnabled(): boolean {
    return this.config.enabled;
  }

  /**
   * Get configuration
   */
  static getConfig(): SentryConfig {
    return { ...this.config };
  }
}

/**
 * Error boundary helper
 */
export function withErrorMonitoring<T extends (...args: any[]) => any>(
  fn: T,
  context?: Partial<ErrorEvent>
): T {
  return ((...args: any[]) => {
    try {
      const result = fn(...args);
      
      // Handle async functions
      if (result instanceof Promise) {
        return result.catch((error) => {
          ErrorMonitoring.captureException(error, context);
          throw error;
        });
      }
      
      return result;
    } catch (error) {
      ErrorMonitoring.captureException(error as Error, context);
      throw error;
    }
  }) as T;
}

/**
 * Initialize error monitoring (call this in your app entry point)
 */
export function initErrorMonitoring(): void {
  ErrorMonitoring.init();

  // Set up global error handlers
  if (typeof window !== 'undefined') {
    // Catch unhandled promise rejections
    window.addEventListener('unhandledrejection', (event) => {
      ErrorMonitoring.captureException(
        new Error(event.reason?.message || 'Unhandled promise rejection'),
        {
          level: 'error',
          tags: { type: 'unhandled_rejection' },
          extra: { reason: event.reason },
        }
      );
    });

    // Catch global errors
    window.addEventListener('error', (event) => {
      ErrorMonitoring.captureException(
        event.error || new Error(event.message),
        {
          level: 'error',
          tags: { type: 'global_error' },
          extra: {
            filename: event.filename,
            lineno: event.lineno,
            colno: event.colno,
          },
        }
      );
    });
  }
}

// Export convenience functions
export const {
  init,
  captureException,
  captureMessage,
  setUser,
  setTags,
  addBreadcrumb,
  isEnabled,
} = ErrorMonitoring;
