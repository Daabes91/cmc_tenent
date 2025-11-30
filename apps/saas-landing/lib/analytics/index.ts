/**
 * Analytics Module
 * Central export for all analytics functionality
 */

export { AnalyticsService } from './tracking';
export { analyticsConfig, shouldLoadAnalytics, getGAScriptUrl } from './config';
export type { 
  AnalyticsEvent, 
  ConversionEvent, 
  ConversionEventType,
  PageViewEvent, 
  ScrollDepthEvent 
} from './types';

// Re-export convenience functions
export {
  initialize,
  trackPageView,
  trackEvent,
  trackConversion,
  trackScrollDepth,
  trackFormSubmission,
  trackBlogRead,
  trackFeatureInteraction,
  trackCTAClick,
  trackSearch,
  trackOutboundLink,
  setUserProperties,
} from './tracking';
