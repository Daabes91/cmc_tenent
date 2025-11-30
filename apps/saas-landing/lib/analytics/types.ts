/**
 * Analytics Types
 * Type definitions for Google Analytics 4 integration
 */

export interface AnalyticsEvent {
  action: string;
  category: string;
  label?: string;
  value?: number;
}

export type ConversionEventType = 
  | 'signup_started' 
  | 'demo_requested' 
  | 'pricing_viewed' 
  | 'blog_read'
  | 'form_submitted'
  | 'feature_interaction';

export interface ConversionEvent {
  event: ConversionEventType;
  properties?: Record<string, any>;
}

export interface PageViewEvent {
  url: string;
  title: string;
  referrer?: string;
}

export interface ScrollDepthEvent {
  depth: number;
  page: string;
}

// Extend Window interface for gtag
declare global {
  interface Window {
    gtag?: (
      command: 'config' | 'event' | 'js' | 'set',
      targetId: string | Date,
      config?: Record<string, any>
    ) => void;
    dataLayer?: any[];
  }
}
