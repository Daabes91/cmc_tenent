/**
 * Analytics Dashboard
 * 
 * Provides key metrics and insights for monitoring landing page performance
 */

export interface DashboardMetrics {
  // Traffic metrics
  pageViews: number;
  uniqueVisitors: number;
  bounceRate: number;
  avgSessionDuration: number;

  // Conversion metrics
  signupStarted: number;
  demoRequested: number;
  pricingViewed: number;
  conversionRate: number;

  // Engagement metrics
  blogPostReads: number;
  avgTimeOnBlog: number;
  ctaClicks: number;
  ctaClickRate: number;

  // Performance metrics
  avgPageLoadTime: number;
  avgLCP: number;
  avgFID: number;
  avgCLS: number;

  // Traffic sources
  trafficSources: {
    direct: number;
    organic: number;
    social: number;
    referral: number;
    paid: number;
  };

  // Top pages
  topPages: Array<{
    url: string;
    views: number;
    avgTime: number;
  }>;

  // Period
  period: {
    start: Date;
    end: Date;
  };
}

export interface AnalyticsEvent {
  type: string;
  timestamp: number;
  properties: Record<string, any>;
}

/**
 * Analytics Dashboard Service
 */
export class AnalyticsDashboard {
  private static events: AnalyticsEvent[] = [];
  private static sessionStart: number = Date.now();

  /**
   * Track event for dashboard
   */
  static trackEvent(type: string, properties: Record<string, any> = {}): void {
    this.events.push({
      type,
      timestamp: Date.now(),
      properties,
    });

    // Keep only last 1000 events in memory
    if (this.events.length > 1000) {
      this.events = this.events.slice(-1000);
    }
  }

  /**
   * Get dashboard metrics
   */
  static getMetrics(startDate?: Date, endDate?: Date): DashboardMetrics {
    const start = startDate || new Date(this.sessionStart);
    const end = endDate || new Date();

    // Filter events by date range
    const filteredEvents = this.events.filter(
      (event) => event.timestamp >= start.getTime() && event.timestamp <= end.getTime()
    );

    // Calculate metrics
    const pageViews = filteredEvents.filter((e) => e.type === 'page_view').length;
    const uniqueVisitors = new Set(
      filteredEvents.filter((e) => e.type === 'page_view').map((e) => e.properties.sessionId)
    ).size;

    const signupStarted = filteredEvents.filter((e) => e.type === 'signup_started').length;
    const demoRequested = filteredEvents.filter((e) => e.type === 'demo_requested').length;
    const pricingViewed = filteredEvents.filter((e) => e.type === 'pricing_viewed').length;
    const totalConversions = signupStarted + demoRequested + pricingViewed;

    const blogPostReads = filteredEvents.filter((e) => e.type === 'blog_read').length;
    const ctaClicks = filteredEvents.filter((e) => e.type.includes('cta_click')).length;

    // Calculate rates
    const conversionRate = pageViews > 0 ? (totalConversions / pageViews) * 100 : 0;
    const ctaClickRate = pageViews > 0 ? (ctaClicks / pageViews) * 100 : 0;

    // Calculate averages
    const blogTimes = filteredEvents
      .filter((e) => e.type === 'blog_read' && e.properties.timeOnPage)
      .map((e) => e.properties.timeOnPage);
    const avgTimeOnBlog = blogTimes.length > 0 ? blogTimes.reduce((a, b) => a + b, 0) / blogTimes.length : 0;

    // Get traffic sources
    const trafficSources = this.calculateTrafficSources(filteredEvents);

    // Get top pages
    const topPages = this.calculateTopPages(filteredEvents);

    // Get performance metrics (mock data - would come from actual monitoring)
    const performanceMetrics = this.getPerformanceMetrics();

    return {
      pageViews,
      uniqueVisitors,
      bounceRate: 0, // Would be calculated from actual session data
      avgSessionDuration: 0, // Would be calculated from actual session data
      signupStarted,
      demoRequested,
      pricingViewed,
      conversionRate,
      blogPostReads,
      avgTimeOnBlog,
      ctaClicks,
      ctaClickRate,
      avgPageLoadTime: performanceMetrics.avgPageLoadTime,
      avgLCP: performanceMetrics.avgLCP,
      avgFID: performanceMetrics.avgFID,
      avgCLS: performanceMetrics.avgCLS,
      trafficSources,
      topPages,
      period: {
        start,
        end,
      },
    };
  }

  /**
   * Calculate traffic sources
   */
  private static calculateTrafficSources(events: AnalyticsEvent[]): DashboardMetrics['trafficSources'] {
    const pageViews = events.filter((e) => e.type === 'page_view');

    const sources = {
      direct: 0,
      organic: 0,
      social: 0,
      referral: 0,
      paid: 0,
    };

    pageViews.forEach((event) => {
      const referrer = event.properties.referrer || '';

      if (!referrer) {
        sources.direct++;
      } else if (referrer.includes('google') || referrer.includes('bing')) {
        sources.organic++;
      } else if (referrer.includes('facebook') || referrer.includes('twitter') || referrer.includes('linkedin')) {
        sources.social++;
      } else if (referrer.includes('ads') || referrer.includes('campaign')) {
        sources.paid++;
      } else {
        sources.referral++;
      }
    });

    return sources;
  }

  /**
   * Calculate top pages
   */
  private static calculateTopPages(events: AnalyticsEvent[]): DashboardMetrics['topPages'] {
    const pageViews = events.filter((e) => e.type === 'page_view');

    const pageStats = new Map<string, { views: number; totalTime: number }>();

    pageViews.forEach((event) => {
      const url = event.properties.url || '/';
      const timeOnPage = event.properties.timeOnPage || 0;

      const stats = pageStats.get(url) || { views: 0, totalTime: 0 };
      stats.views++;
      stats.totalTime += timeOnPage;

      pageStats.set(url, stats);
    });

    return Array.from(pageStats.entries())
      .map(([url, stats]) => ({
        url,
        views: stats.views,
        avgTime: stats.views > 0 ? stats.totalTime / stats.views : 0,
      }))
      .sort((a, b) => b.views - a.views)
      .slice(0, 10);
  }

  /**
   * Get performance metrics
   */
  private static getPerformanceMetrics(): {
    avgPageLoadTime: number;
    avgLCP: number;
    avgFID: number;
    avgCLS: number;
  } {
    // In a real implementation, this would fetch from PerformanceMonitoringService
    // For now, return mock data
    return {
      avgPageLoadTime: 0,
      avgLCP: 0,
      avgFID: 0,
      avgCLS: 0,
    };
  }

  /**
   * Export metrics as CSV
   */
  static exportAsCSV(metrics: DashboardMetrics): string {
    const rows = [
      ['Metric', 'Value'],
      ['Page Views', metrics.pageViews.toString()],
      ['Unique Visitors', metrics.uniqueVisitors.toString()],
      ['Conversion Rate', `${metrics.conversionRate.toFixed(2)}%`],
      ['Signup Started', metrics.signupStarted.toString()],
      ['Demo Requested', metrics.demoRequested.toString()],
      ['Pricing Viewed', metrics.pricingViewed.toString()],
      ['Blog Post Reads', metrics.blogPostReads.toString()],
      ['CTA Clicks', metrics.ctaClicks.toString()],
      ['CTA Click Rate', `${metrics.ctaClickRate.toFixed(2)}%`],
      ['Avg Page Load Time', `${metrics.avgPageLoadTime.toFixed(0)}ms`],
      ['Avg LCP', `${metrics.avgLCP.toFixed(0)}ms`],
      ['Avg FID', `${metrics.avgFID.toFixed(0)}ms`],
      ['Avg CLS', metrics.avgCLS.toFixed(3)],
    ];

    return rows.map((row) => row.join(',')).join('\n');
  }

  /**
   * Get real-time metrics
   */
  static getRealTimeMetrics(): {
    activeUsers: number;
    recentEvents: AnalyticsEvent[];
    topPages: string[];
  } {
    const now = Date.now();
    const fiveMinutesAgo = now - 5 * 60 * 1000;

    const recentEvents = this.events.filter((e) => e.timestamp >= fiveMinutesAgo);

    const activeUsers = new Set(
      recentEvents.filter((e) => e.type === 'page_view').map((e) => e.properties.sessionId)
    ).size;

    const pageViews = recentEvents.filter((e) => e.type === 'page_view');
    const pageCounts = new Map<string, number>();

    pageViews.forEach((event) => {
      const url = event.properties.url || '/';
      pageCounts.set(url, (pageCounts.get(url) || 0) + 1);
    });

    const topPages = Array.from(pageCounts.entries())
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5)
      .map(([url]) => url);

    return {
      activeUsers,
      recentEvents: recentEvents.slice(-20),
      topPages,
    };
  }

  /**
   * Clear all events
   */
  static clearEvents(): void {
    this.events = [];
  }

  /**
   * Get events
   */
  static getEvents(): AnalyticsEvent[] {
    return [...this.events];
  }
}

/**
 * Track dashboard event
 */
export function trackDashboardEvent(type: string, properties: Record<string, any> = {}): void {
  AnalyticsDashboard.trackEvent(type, properties);
}

/**
 * Get dashboard metrics
 */
export function getDashboardMetrics(startDate?: Date, endDate?: Date): DashboardMetrics {
  return AnalyticsDashboard.getMetrics(startDate, endDate);
}

/**
 * Get real-time metrics
 */
export function getRealTimeMetrics() {
  return AnalyticsDashboard.getRealTimeMetrics();
}

/**
 * Export metrics as CSV
 */
export function exportMetricsAsCSV(metrics: DashboardMetrics): string {
  return AnalyticsDashboard.exportAsCSV(metrics);
}
