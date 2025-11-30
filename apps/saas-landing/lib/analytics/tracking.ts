/**
 * Analytics Tracking Service
 * Handles all Google Analytics 4 tracking operations
 */

import type { 
  AnalyticsEvent, 
  ConversionEvent, 
  PageViewEvent, 
  ScrollDepthEvent 
} from './types';

export class AnalyticsService {
  private static measurementId: string | null = null;
  private static isInitialized = false;

  /**
   * Initialize Google Analytics 4
   * @param measurementId - GA4 Measurement ID (e.g., G-XXXXXXXXXX)
   */
  static initialize(measurementId: string): void {
    if (typeof window === 'undefined') {
      return; // Skip on server-side
    }

    if (this.isInitialized) {
      console.warn('Analytics already initialized');
      return;
    }

    this.measurementId = measurementId;

    try {
      // Initialize dataLayer
      window.dataLayer = window.dataLayer || [];
      
      // Define gtag function
      window.gtag = function gtag() {
        window.dataLayer?.push(arguments);
      };

      // Initialize with current timestamp
      window.gtag('js', new Date());

      // Configure GA4 with enhanced measurement
      window.gtag('config', measurementId, {
        page_path: window.location.pathname,
        send_page_view: true,
        // Enhanced measurement settings
        enhanced_measurement: {
          scrolls: true,
          outbound_clicks: true,
          site_search: true,
          video_engagement: true,
          file_downloads: true,
        },
      });

      this.isInitialized = true;
      console.log('Google Analytics 4 initialized:', measurementId);
    } catch (error) {
      console.error('Failed to initialize Google Analytics:', error);
    }
  }

  /**
   * Track page view
   * @param url - Page URL
   * @param title - Page title
   */
  static trackPageView({ url, title, referrer }: PageViewEvent): void {
    if (!this.isAvailable()) return;

    try {
      window.gtag!('event', 'page_view', {
        page_title: title,
        page_location: url,
        page_referrer: referrer || document.referrer,
      });
    } catch (error) {
      console.error('Failed to track page view:', error);
    }
  }

  /**
   * Track custom event
   * @param event - Analytics event details
   */
  static trackEvent({ action, category, label, value }: AnalyticsEvent): void {
    if (!this.isAvailable()) return;

    try {
      window.gtag!('event', action, {
        event_category: category,
        event_label: label,
        value: value,
      });
    } catch (error) {
      console.error('Failed to track event:', error);
    }
  }

  /**
   * Track conversion event
   * @param event - Conversion event details
   */
  static trackConversion({ event, properties = {} }: ConversionEvent): void {
    if (!this.isAvailable()) return;

    try {
      window.gtag!('event', event, {
        ...properties,
        timestamp: new Date().toISOString(),
      });

      // Also track as a custom event for reporting
      this.trackEvent({
        action: event,
        category: 'conversion',
        label: properties.label || event,
        value: properties.value,
      });
    } catch (error) {
      console.error('Failed to track conversion:', error);
    }
  }

  /**
   * Track scroll depth
   * @param depth - Scroll depth percentage (0-100)
   */
  static trackScrollDepth({ depth, page }: ScrollDepthEvent): void {
    if (!this.isAvailable()) return;

    try {
      window.gtag!('event', 'scroll', {
        event_category: 'engagement',
        event_label: `${depth}% - ${page}`,
        value: depth,
        page_path: page,
      });
    } catch (error) {
      console.error('Failed to track scroll depth:', error);
    }
  }

  /**
   * Track form submission
   * @param formName - Name of the form
   * @param formType - Type of form (signup, contact, etc.)
   */
  static trackFormSubmission(formName: string, formType: string): void {
    if (!this.isAvailable()) return;

    try {
      this.trackConversion({
        event: 'form_submitted',
        properties: {
          form_name: formName,
          form_type: formType,
        },
      });
    } catch (error) {
      console.error('Failed to track form submission:', error);
    }
  }

  /**
   * Track blog post read
   * @param postTitle - Blog post title
   * @param postSlug - Blog post slug
   * @param timeOnPage - Time spent on page in seconds
   */
  static trackBlogRead(postTitle: string, postSlug: string, timeOnPage?: number): void {
    if (!this.isAvailable()) return;

    try {
      this.trackConversion({
        event: 'blog_read',
        properties: {
          post_title: postTitle,
          post_slug: postSlug,
          time_on_page: timeOnPage,
        },
      });
    } catch (error) {
      console.error('Failed to track blog read:', error);
    }
  }

  /**
   * Track feature interaction
   * @param featureName - Name of the feature
   * @param interactionType - Type of interaction (click, hover, etc.)
   */
  static trackFeatureInteraction(featureName: string, interactionType: string): void {
    if (!this.isAvailable()) return;

    try {
      this.trackConversion({
        event: 'feature_interaction',
        properties: {
          feature_name: featureName,
          interaction_type: interactionType,
        },
      });
    } catch (error) {
      console.error('Failed to track feature interaction:', error);
    }
  }

  /**
   * Track CTA button click
   * @param ctaText - CTA button text
   * @param ctaLocation - Location of CTA on page
   * @param ctaDestination - Where the CTA leads
   */
  static trackCTAClick(ctaText: string, ctaLocation: string, ctaDestination = ""): void {
    if (!this.isAvailable()) return;

    try {
      // Determine conversion event type based on CTA text/destination
      let eventType: ConversionEvent['event'] = 'signup_started';
      
      if (ctaText.toLowerCase().includes('demo') || ctaDestination.includes('demo')) {
        eventType = 'demo_requested';
      } else if (ctaText.toLowerCase().includes('pricing') || ctaDestination.includes('pricing')) {
        eventType = 'pricing_viewed';
      }

      this.trackConversion({
        event: eventType,
        properties: {
          cta_text: ctaText,
          cta_location: ctaLocation,
          cta_destination: ctaDestination,
        },
      });
    } catch (error) {
      console.error('Failed to track CTA click:', error);
    }
  }

  /**
   * Track search query
   * @param query - Search query string
   * @param resultsCount - Number of results returned
   */
  static trackSearch(query: string, resultsCount: number): void {
    if (!this.isAvailable()) return;

    try {
      window.gtag!('event', 'search', {
        search_term: query,
        results_count: resultsCount,
      });
    } catch (error) {
      console.error('Failed to track search:', error);
    }
  }

  /**
   * Track outbound link click
   * @param url - Destination URL
   * @param linkText - Link text
   */
  static trackOutboundLink(url: string, linkText: string): void {
    if (!this.isAvailable()) return;

    try {
      window.gtag!('event', 'click', {
        event_category: 'outbound',
        event_label: linkText,
        value: url,
      });
    } catch (error) {
      console.error('Failed to track outbound link:', error);
    }
  }

  /**
   * Set user properties
   * @param properties - User properties to set
   */
  static setUserProperties(properties: Record<string, any>): void {
    if (!this.isAvailable()) return;

    try {
      window.gtag!('set', 'user_properties', properties);
    } catch (error) {
      console.error('Failed to set user properties:', error);
    }
  }

  /**
   * Check if analytics is available
   * @returns true if gtag is available
   */
  private static isAvailable(): boolean {
    if (typeof window === 'undefined') {
      return false;
    }

    if (!window.gtag) {
      console.warn('Google Analytics not loaded');
      return false;
    }

    return true;
  }

  /**
   * Get measurement ID
   * @returns Current measurement ID or null
   */
  static getMeasurementId(): string | null {
    return this.measurementId;
  }

  /**
   * Check if analytics is initialized
   * @returns true if initialized
   */
  static isReady(): boolean {
    return this.isInitialized && this.isAvailable();
  }
}

// Export convenience functions without losing class context
export const initialize = (measurementId: string) =>
  AnalyticsService.initialize(measurementId);
export const trackPageView = (event: PageViewEvent) =>
  AnalyticsService.trackPageView(event);
export const trackEvent = (event: AnalyticsEvent) =>
  AnalyticsService.trackEvent(event);
export const trackConversion = (event: ConversionEvent) =>
  AnalyticsService.trackConversion(event);
export const trackScrollDepth = (event: ScrollDepthEvent) =>
  AnalyticsService.trackScrollDepth(event);
export const trackFormSubmission = (formName: string, formType: string) =>
  AnalyticsService.trackFormSubmission(formName, formType);
export const trackBlogRead = (postTitle: string, postSlug: string, timeOnPage?: number) =>
  AnalyticsService.trackBlogRead(postTitle, postSlug, timeOnPage);
export const trackFeatureInteraction = (featureName: string, interactionType: string) =>
  AnalyticsService.trackFeatureInteraction(featureName, interactionType);
export const trackCTAClick = (ctaName: string, ctaLocation = "", ctaDestination = "") =>
  AnalyticsService.trackCTAClick(ctaName, ctaLocation, ctaDestination);
export const trackSearch = (query: string, resultsCount: number) =>
  AnalyticsService.trackSearch(query, resultsCount);
export const trackOutboundLink = (url: string, linkText: string) =>
  AnalyticsService.trackOutboundLink(url, linkText);
export const setUserProperties = (properties: Record<string, any>) =>
  AnalyticsService.setUserProperties(properties);
