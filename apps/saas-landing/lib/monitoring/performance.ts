/**
 * Performance Monitoring
 * 
 * Comprehensive performance monitoring for Core Web Vitals and custom metrics
 */

import { reportWebVitals, type WebVitalsMetric } from '../web-vitals';

export interface PerformanceMetrics {
  // Core Web Vitals
  lcp?: number; // Largest Contentful Paint
  fid?: number; // First Input Delay
  cls?: number; // Cumulative Layout Shift
  fcp?: number; // First Contentful Paint
  ttfb?: number; // Time to First Byte
  inp?: number; // Interaction to Next Paint

  // Custom metrics
  pageLoadTime?: number;
  domContentLoaded?: number;
  resourceLoadTime?: number;
  
  // Page info
  url: string;
  timestamp: number;
}

export interface PerformanceThresholds {
  lcp: { good: number; poor: number };
  fid: { good: number; poor: number };
  cls: { good: number; poor: number };
  fcp: { good: number; poor: number };
  ttfb: { good: number; poor: number };
  inp: { good: number; poor: number };
}

/**
 * Performance monitoring service
 */
export class PerformanceMonitoringService {
  private static metrics: Map<string, PerformanceMetrics> = new Map();
  private static isInitialized = false;

  // Performance thresholds based on Google's recommendations
  private static thresholds: PerformanceThresholds = {
    lcp: { good: 2500, poor: 4000 },
    fid: { good: 100, poor: 300 },
    cls: { good: 0.1, poor: 0.25 },
    fcp: { good: 1800, poor: 3000 },
    ttfb: { good: 800, poor: 1800 },
    inp: { good: 200, poor: 500 },
  };

  /**
   * Initialize performance monitoring
   */
  static init(): void {
    if (this.isInitialized) {
      return;
    }

    if (typeof window === 'undefined') {
      return;
    }

    this.isInitialized = true;

    // Monitor Core Web Vitals
    this.initWebVitals();

    // Monitor page load metrics
    this.initPageLoadMetrics();

    // Monitor resource loading
    this.initResourceMonitoring();

    console.log('Performance monitoring initialized');
  }

  /**
   * Initialize Web Vitals monitoring
   */
  private static async initWebVitals(): Promise<void> {
    try {
      // Dynamically import web-vitals
      // Note: web-vitals package must be installed: npm install web-vitals
      const webVitals = await import('web-vitals' as any).catch(() => null);

      if (!webVitals) {
        console.warn('web-vitals package not installed. Install with: npm install web-vitals');
        return;
      }

      const { onCLS, onFCP, onFID, onLCP, onTTFB, onINP } = webVitals;

      // Monitor all Core Web Vitals
      onLCP((metric: any) => this.handleWebVital(metric));
      onFID((metric: any) => this.handleWebVital(metric));
      onCLS((metric: any) => this.handleWebVital(metric));
      onFCP((metric: any) => this.handleWebVital(metric));
      onTTFB((metric: any) => this.handleWebVital(metric));
      onINP((metric: any) => this.handleWebVital(metric));
    } catch (error) {
      console.error('Failed to initialize Web Vitals:', error);
    }
  }

  /**
   * Handle Web Vital metric
   */
  private static handleWebVital(metric: any): void {
    const url = window.location.pathname;
    const currentMetrics = this.metrics.get(url) || {
      url,
      timestamp: Date.now(),
    };

    // Update metric
    const metricName = metric.name.toLowerCase() as keyof PerformanceMetrics;
    (currentMetrics as any)[metricName] = metric.value;

    this.metrics.set(url, currentMetrics);

    // Report to analytics
    reportWebVitals(metric);

    // Check if metric exceeds threshold
    this.checkThreshold(metric.name, metric.value);
  }

  /**
   * Initialize page load metrics
   */
  private static initPageLoadMetrics(): void {
    if (typeof window === 'undefined' || !('performance' in window)) {
      return;
    }

    window.addEventListener('load', () => {
      setTimeout(() => {
        const perfData = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;

        if (!perfData) {
          return;
        }

        const url = window.location.pathname;
        const currentMetrics = this.metrics.get(url) || {
          url,
          timestamp: Date.now(),
        };

        // Calculate page load metrics
        currentMetrics.pageLoadTime = perfData.loadEventEnd - perfData.fetchStart;
        currentMetrics.domContentLoaded = perfData.domContentLoadedEventEnd - perfData.fetchStart;
        currentMetrics.resourceLoadTime = perfData.loadEventEnd - perfData.domContentLoadedEventEnd;

        this.metrics.set(url, currentMetrics);

        // Send to analytics
        this.sendMetricsToAnalytics(currentMetrics);
      }, 0);
    });
  }

  /**
   * Initialize resource monitoring
   */
  private static initResourceMonitoring(): void {
    if (typeof window === 'undefined' || !('PerformanceObserver' in window)) {
      return;
    }

    try {
      // Monitor resource loading
      const resourceObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          const resource = entry as PerformanceResourceTiming;

          // Log slow resources (> 1 second)
          if (resource.duration > 1000) {
            console.warn('[Performance] Slow resource:', {
              name: resource.name,
              duration: Math.round(resource.duration),
              type: resource.initiatorType,
            });

            // Track in analytics
            if ((window as any).gtag) {
              (window as any).gtag('event', 'slow_resource', {
                event_category: 'Performance',
                event_label: resource.name,
                value: Math.round(resource.duration),
                resource_type: resource.initiatorType,
              });
            }
          }
        }
      });

      resourceObserver.observe({ entryTypes: ['resource'] });
    } catch (error) {
      console.error('Failed to initialize resource monitoring:', error);
    }
  }

  /**
   * Check if metric exceeds threshold
   */
  private static checkThreshold(metricName: string, value: number): void {
    const name = metricName.toLowerCase() as keyof PerformanceThresholds;
    const threshold = this.thresholds[name];

    if (!threshold) {
      return;
    }

    let rating: 'good' | 'needs-improvement' | 'poor';

    if (value <= threshold.good) {
      rating = 'good';
    } else if (value <= threshold.poor) {
      rating = 'needs-improvement';
    } else {
      rating = 'poor';
    }

    // Log poor performance
    if (rating === 'poor') {
      console.warn(`[Performance] Poor ${metricName}:`, {
        value: Math.round(value),
        threshold: threshold.poor,
        url: window.location.pathname,
      });
    }
  }

  /**
   * Send metrics to analytics
   */
  private static sendMetricsToAnalytics(metrics: PerformanceMetrics): void {
    if (typeof window === 'undefined' || !(window as any).gtag) {
      return;
    }

    // Send page load time
    if (metrics.pageLoadTime) {
      (window as any).gtag('event', 'page_load_time', {
        event_category: 'Performance',
        event_label: metrics.url,
        value: Math.round(metrics.pageLoadTime),
      });
    }

    // Send DOM content loaded time
    if (metrics.domContentLoaded) {
      (window as any).gtag('event', 'dom_content_loaded', {
        event_category: 'Performance',
        event_label: metrics.url,
        value: Math.round(metrics.domContentLoaded),
      });
    }
  }

  /**
   * Get metrics for current page
   */
  static getCurrentMetrics(): PerformanceMetrics | undefined {
    if (typeof window === 'undefined') {
      return undefined;
    }

    return this.metrics.get(window.location.pathname);
  }

  /**
   * Get all metrics
   */
  static getAllMetrics(): Map<string, PerformanceMetrics> {
    return new Map(this.metrics);
  }

  /**
   * Clear metrics
   */
  static clearMetrics(): void {
    this.metrics.clear();
  }

  /**
   * Export metrics as JSON
   */
  static exportMetrics(): string {
    const metricsArray = Array.from(this.metrics.entries()).map(([, metrics]) => ({
      ...metrics,
    }));

    return JSON.stringify(metricsArray, null, 2);
  }

  /**
   * Get performance summary
   */
  static getPerformanceSummary(): {
    totalPages: number;
    averageLCP?: number;
    averageFID?: number;
    averageCLS?: number;
    averagePageLoadTime?: number;
  } {
    const metricsArray = Array.from(this.metrics.values());

    if (metricsArray.length === 0) {
      return { totalPages: 0 };
    }

    const sum = (key: keyof PerformanceMetrics) =>
      metricsArray.reduce((acc, m) => acc + (m[key] as number || 0), 0);

    const avg = (key: keyof PerformanceMetrics) => {
      const values = metricsArray.filter((m) => m[key] !== undefined);
      return values.length > 0 ? sum(key) / values.length : undefined;
    };

    return {
      totalPages: metricsArray.length,
      averageLCP: avg('lcp'),
      averageFID: avg('fid'),
      averageCLS: avg('cls'),
      averagePageLoadTime: avg('pageLoadTime'),
    };
  }
}

/**
 * Initialize performance monitoring
 */
export function initPerformanceMonitoring(): void {
  PerformanceMonitoringService.init();
}

/**
 * Get current page metrics
 */
export function getCurrentPerformanceMetrics(): PerformanceMetrics | undefined {
  return PerformanceMonitoringService.getCurrentMetrics();
}

/**
 * Get performance summary
 */
export function getPerformanceSummary() {
  return PerformanceMonitoringService.getPerformanceSummary();
}

/**
 * Export all metrics
 */
export function exportPerformanceMetrics(): string {
  return PerformanceMonitoringService.exportMetrics();
}
