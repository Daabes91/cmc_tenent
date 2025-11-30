/**
 * Monitoring and Analytics Initialization
 * 
 * Central module for initializing all monitoring services
 */

import { initErrorMonitoring, ErrorMonitoring } from './sentry';
import { initPerformanceMonitoring, PerformanceMonitoringService } from './performance';
import { AnalyticsDashboard } from './analytics-dashboard';

export interface MonitoringConfig {
  // Error monitoring
  sentryDsn?: string;
  sentryEnvironment?: string;
  
  // Analytics
  gaEnabled?: boolean;
  gaMeasurementId?: string;
  
  // Performance
  performanceEnabled?: boolean;
  
  // General
  debug?: boolean;
}

/**
 * Initialize all monitoring services
 */
export function initMonitoring(config: MonitoringConfig = {}): void {
  const {
    sentryDsn = process.env.NEXT_PUBLIC_SENTRY_DSN,
    sentryEnvironment = process.env.NODE_ENV,
    gaEnabled = true,
    gaMeasurementId = process.env.NEXT_PUBLIC_GA_MEASUREMENT_ID,
    performanceEnabled = true,
    debug = process.env.NODE_ENV === 'development',
  } = config;

  if (typeof window === 'undefined') {
    return; // Skip on server-side
  }

  try {
    // Initialize error monitoring
    if (sentryDsn) {
      initErrorMonitoring();
      
      if (debug) {
        console.log('[Monitoring] Error monitoring initialized');
      }
    } else if (debug) {
      console.warn('[Monitoring] Error monitoring disabled - no DSN provided');
    }

    // Initialize performance monitoring
    if (performanceEnabled) {
      initPerformanceMonitoring();
      
      if (debug) {
        console.log('[Monitoring] Performance monitoring initialized');
      }
    }

    // Initialize analytics dashboard tracking
    if (gaEnabled && gaMeasurementId) {
      setupAnalyticsDashboard();
      
      if (debug) {
        console.log('[Monitoring] Analytics dashboard initialized');
      }
    }

    if (debug) {
      console.log('[Monitoring] All monitoring services initialized successfully');
    }
  } catch (error) {
    console.error('[Monitoring] Failed to initialize monitoring:', error);
  }
}

/**
 * Setup analytics dashboard tracking
 */
function setupAnalyticsDashboard(): void {
  if (typeof window === 'undefined') {
    return;
  }

  // Track page views
  const trackPageView = () => {
    AnalyticsDashboard.trackEvent('page_view', {
      url: window.location.pathname,
      title: document.title,
      referrer: document.referrer,
      sessionId: getSessionId(),
      timestamp: Date.now(),
    });
  };

  // Track initial page view
  trackPageView();

  // Track page views on navigation (for SPAs)
  let lastUrl = window.location.pathname;
  
  const observer = new MutationObserver(() => {
    if (window.location.pathname !== lastUrl) {
      lastUrl = window.location.pathname;
      trackPageView();
    }
  });

  observer.observe(document.body, {
    childList: true,
    subtree: true,
  });

  // Track time on page
  let pageStartTime = Date.now();
  
  window.addEventListener('beforeunload', () => {
    const timeOnPage = Date.now() - pageStartTime;
    
    AnalyticsDashboard.trackEvent('page_exit', {
      url: window.location.pathname,
      timeOnPage,
      sessionId: getSessionId(),
    });
  });

  // Track visibility changes
  document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
      const timeOnPage = Date.now() - pageStartTime;
      
      AnalyticsDashboard.trackEvent('page_hidden', {
        url: window.location.pathname,
        timeOnPage,
        sessionId: getSessionId(),
      });
    } else {
      pageStartTime = Date.now();
      
      AnalyticsDashboard.trackEvent('page_visible', {
        url: window.location.pathname,
        sessionId: getSessionId(),
      });
    }
  });
}

/**
 * Get or create session ID
 */
function getSessionId(): string {
  if (typeof window === 'undefined') {
    return 'unknown';
  }

  let sessionId = sessionStorage.getItem('sessionId');

  if (!sessionId) {
    sessionId = `${Date.now()}-${Math.random().toString(36).substring(2, 15)}`;
    sessionStorage.setItem('sessionId', sessionId);
  }

  return sessionId;
}

/**
 * Get monitoring status
 */
export function getMonitoringStatus(): {
  errorMonitoring: boolean;
  performanceMonitoring: boolean;
  analytics: boolean;
} {
  return {
    errorMonitoring: ErrorMonitoring.isEnabled(),
    performanceMonitoring: typeof window !== 'undefined' && PerformanceMonitoringService !== undefined,
    analytics: typeof window !== 'undefined' && (window as any).gtag !== undefined,
  };
}

/**
 * Export monitoring data
 */
export function exportMonitoringData(): {
  performance: string;
  analytics: string;
} {
  return {
    performance: PerformanceMonitoringService.exportMetrics(),
    analytics: JSON.stringify(AnalyticsDashboard.getEvents(), null, 2),
  };
}

// Re-export monitoring services
export { ErrorMonitoring, initErrorMonitoring } from './sentry';
export { PerformanceMonitoringService, initPerformanceMonitoring } from './performance';
export { AnalyticsDashboard, getDashboardMetrics, getRealTimeMetrics } from './analytics-dashboard';
export type { DashboardMetrics } from './analytics-dashboard';
export type { PerformanceMetrics } from './performance';
