/**
 * Analytics Configuration
 * Configuration settings for Google Analytics 4
 */

export const analyticsConfig = {
  // GA4 Measurement ID - should be set via environment variable
  measurementId: process.env.NEXT_PUBLIC_GA_MEASUREMENT_ID || '',
  
  // Enable/disable analytics based on environment
  enabled: process.env.NODE_ENV === 'production' || process.env.NEXT_PUBLIC_ENABLE_ANALYTICS === 'true',
  
  // Debug mode
  debug: process.env.NODE_ENV === 'development',
  
  // Enhanced measurement settings
  enhancedMeasurement: {
    scrolls: true,
    outboundClicks: true,
    siteSearch: true,
    videoEngagement: true,
    fileDownloads: true,
  },
  
  // Scroll depth tracking thresholds (percentages)
  scrollDepthThresholds: [25, 50, 75, 90, 100],
  
  // Time on page tracking interval (milliseconds)
  timeOnPageInterval: 30000, // 30 seconds
} as const;

/**
 * Check if analytics should be loaded
 */
export function shouldLoadAnalytics(): boolean {
  return analyticsConfig.enabled && !!analyticsConfig.measurementId;
}

/**
 * Get GA4 script URL
 */
export function getGAScriptUrl(): string {
  return `https://www.googletagmanager.com/gtag/js?id=${analyticsConfig.measurementId}`;
}
