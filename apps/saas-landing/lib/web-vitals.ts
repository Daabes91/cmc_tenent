/**
 * Web Vitals Monitoring
 * 
 * This module provides utilities for monitoring Core Web Vitals metrics
 * and reporting them for performance analysis.
 */

export interface WebVitalsMetric {
  id: string;
  name: 'CLS' | 'FCP' | 'FID' | 'LCP' | 'TTFB' | 'INP';
  value: number;
  rating: 'good' | 'needs-improvement' | 'poor';
  delta: number;
  navigationType: string;
}

/**
 * Thresholds for Core Web Vitals
 * Based on Google's recommendations
 */
export const WEB_VITALS_THRESHOLDS = {
  LCP: {
    good: 2500,
    poor: 4000,
  },
  FID: {
    good: 100,
    poor: 300,
  },
  CLS: {
    good: 0.1,
    poor: 0.25,
  },
  FCP: {
    good: 1800,
    poor: 3000,
  },
  TTFB: {
    good: 800,
    poor: 1800,
  },
  INP: {
    good: 200,
    poor: 500,
  },
} as const;

/**
 * Get rating for a metric value
 */
function getRating(
  name: WebVitalsMetric['name'],
  value: number
): WebVitalsMetric['rating'] {
  const thresholds = WEB_VITALS_THRESHOLDS[name];
  
  if (!thresholds) {
    return 'good';
  }
  
  if (value <= thresholds.good) {
    return 'good';
  }
  
  if (value <= thresholds.poor) {
    return 'needs-improvement';
  }
  
  return 'poor';
}

/**
 * Report Web Vitals metric
 * 
 * This function can be customized to send metrics to your analytics service
 */
export function reportWebVitals(metric: WebVitalsMetric) {
  // Log to console in development
  if (process.env.NODE_ENV === 'development') {
    console.log('[Web Vitals]', {
      name: metric.name,
      value: Math.round(metric.name === 'CLS' ? metric.value * 1000 : metric.value),
      rating: metric.rating,
      id: metric.id,
    });
  }
  
  // Send to analytics service
  // Example: Google Analytics
  if (typeof window !== 'undefined' && (window as any).gtag) {
    (window as any).gtag('event', metric.name, {
      event_category: 'Web Vitals',
      event_label: metric.id,
      value: Math.round(metric.name === 'CLS' ? metric.value * 1000 : metric.value),
      non_interaction: true,
    });
  }
  
  // Example: Custom analytics endpoint
  if (typeof window !== 'undefined' && process.env.NEXT_PUBLIC_ANALYTICS_ENDPOINT) {
    fetch(process.env.NEXT_PUBLIC_ANALYTICS_ENDPOINT, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        metric: metric.name,
        value: metric.value,
        rating: metric.rating,
        id: metric.id,
        timestamp: Date.now(),
        url: window.location.href,
        userAgent: navigator.userAgent,
      }),
    }).catch((error) => {
      console.error('Failed to send Web Vitals metric:', error);
    });
  }
}

/**
 * Initialize Web Vitals monitoring
 * 
 * Call this function in your app to start monitoring
 * Note: Requires 'web-vitals' package to be installed
 * Install with: npm install web-vitals
 */
export async function initWebVitals() {
  if (typeof window === 'undefined') {
    return;
  }
  
  try {
    // Dynamically import web-vitals library (optional dependency)
    // Using dynamic import to avoid build errors if package is not installed
    const webVitalsModule = 'web-vitals';
    const webVitals = await import(/* webpackIgnore: true */ webVitalsModule).catch(() => null);
    
    if (!webVitals) {
      console.warn('web-vitals package not installed. Install with: npm install web-vitals');
      return;
    }
    
    const { onCLS, onFCP, onFID, onLCP, onTTFB, onINP } = webVitals;
    
    // Monitor all Core Web Vitals
    onCLS((metric: any) => {
      reportWebVitals({
        ...metric,
        rating: getRating('CLS', metric.value),
      });
    });
    
    onFCP((metric: any) => {
      reportWebVitals({
        ...metric,
        rating: getRating('FCP', metric.value),
      });
    });
    
    onFID((metric: any) => {
      reportWebVitals({
        ...metric,
        rating: getRating('FID', metric.value),
      });
    });
    
    onLCP((metric: any) => {
      reportWebVitals({
        ...metric,
        rating: getRating('LCP', metric.value),
      });
    });
    
    onTTFB((metric: any) => {
      reportWebVitals({
        ...metric,
        rating: getRating('TTFB', metric.value),
      });
    });
    
    onINP((metric: any) => {
      reportWebVitals({
        ...metric,
        rating: getRating('INP', metric.value),
      });
    });
  } catch (error) {
    console.error('Failed to initialize Web Vitals monitoring:', error);
  }
}

/**
 * Performance observer for custom metrics
 */
export class PerformanceMonitor {
  private static instance: PerformanceMonitor;
  private observers: PerformanceObserver[] = [];
  
  private constructor() {
    this.initObservers();
  }
  
  static getInstance(): PerformanceMonitor {
    if (!PerformanceMonitor.instance) {
      PerformanceMonitor.instance = new PerformanceMonitor();
    }
    return PerformanceMonitor.instance;
  }
  
  private initObservers() {
    if (typeof window === 'undefined' || !('PerformanceObserver' in window)) {
      return;
    }
    
    // Observe long tasks (> 50ms)
    try {
      const longTaskObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if (entry.duration > 50) {
            console.warn('[Performance] Long task detected:', {
              duration: Math.round(entry.duration),
              startTime: Math.round(entry.startTime),
            });
          }
        }
      });
      
      longTaskObserver.observe({ entryTypes: ['longtask'] });
      this.observers.push(longTaskObserver);
    } catch (error) {
      // Long task API not supported
    }
    
    // Observe layout shifts
    try {
      const layoutShiftObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if ((entry as any).hadRecentInput) {
            continue; // Ignore shifts caused by user input
          }
          
          const value = (entry as any).value;
          if (value > 0.1) {
            console.warn('[Performance] Layout shift detected:', {
              value: value.toFixed(4),
              sources: (entry as any).sources,
            });
          }
        }
      });
      
      layoutShiftObserver.observe({ entryTypes: ['layout-shift'] });
      this.observers.push(layoutShiftObserver);
    } catch (error) {
      // Layout shift API not supported
    }
  }
  
  /**
   * Measure custom timing
   */
  measure(name: string, startMark?: string, endMark?: string) {
    if (typeof window === 'undefined' || !('performance' in window)) {
      return;
    }
    
    try {
      performance.measure(name, startMark, endMark);
      const measure = performance.getEntriesByName(name, 'measure')[0];
      
      if (measure) {
        console.log(`[Performance] ${name}:`, Math.round(measure.duration), 'ms');
      }
    } catch (error) {
      console.error('Failed to measure performance:', error);
    }
  }
  
  /**
   * Mark a point in time
   */
  mark(name: string) {
    if (typeof window === 'undefined' || !('performance' in window)) {
      return;
    }
    
    try {
      performance.mark(name);
    } catch (error) {
      console.error('Failed to mark performance:', error);
    }
  }
  
  /**
   * Disconnect all observers
   */
  disconnect() {
    this.observers.forEach((observer) => observer.disconnect());
    this.observers = [];
  }
}

/**
 * Hook for measuring component render time
 */
export function usePerformanceMonitor(componentName: string) {
  if (typeof window === 'undefined') {
    return;
  }
  
  const monitor = PerformanceMonitor.getInstance();
  
  // Mark component mount
  monitor.mark(`${componentName}-mount-start`);
  
  return {
    markMountEnd: () => {
      monitor.mark(`${componentName}-mount-end`);
      monitor.measure(
        `${componentName}-mount`,
        `${componentName}-mount-start`,
        `${componentName}-mount-end`
      );
    },
    markRenderStart: () => {
      monitor.mark(`${componentName}-render-start`);
    },
    markRenderEnd: () => {
      monitor.mark(`${componentName}-render-end`);
      monitor.measure(
        `${componentName}-render`,
        `${componentName}-render-start`,
        `${componentName}-render-end`
      );
    },
  };
}
