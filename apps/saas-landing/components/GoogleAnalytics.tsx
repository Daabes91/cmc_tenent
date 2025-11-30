'use client';

/**
 * Google Analytics Component
 * Loads GA4 script and initializes analytics tracking
 */

import { useEffect } from 'react';
import Script from 'next/script';
import { AnalyticsService } from '@/lib/analytics/tracking';
import { analyticsConfig, shouldLoadAnalytics, getGAScriptUrl } from '@/lib/analytics/config';

export function GoogleAnalytics() {
  // Don't render if analytics is disabled or no measurement ID
  if (!shouldLoadAnalytics()) {
    if (analyticsConfig.debug) {
      console.log('Google Analytics disabled or no measurement ID provided');
    }
    return null;
  }

  return (
    <>
      {/* Load GA4 script */}
      <Script
        src={getGAScriptUrl()}
        strategy="afterInteractive"
        onLoad={() => {
          // Initialize analytics after script loads
          AnalyticsService.initialize(analyticsConfig.measurementId);
          
          if (analyticsConfig.debug) {
            console.log('Google Analytics script loaded and initialized');
          }
        }}
        onError={(e) => {
          console.error('Failed to load Google Analytics script:', e);
        }}
      />
      
      {/* Initialize page view tracking */}
      <AnalyticsInitializer />
    </>
  );
}

/**
 * Analytics Initializer
 * Sets up page view tracking and scroll depth tracking
 */
function AnalyticsInitializer() {
  useEffect(() => {
    // Wait for analytics to be ready
    const checkReady = setInterval(() => {
      if (AnalyticsService.isReady()) {
        clearInterval(checkReady);
        initializeTracking();
      }
    }, 100);

    // Cleanup
    return () => clearInterval(checkReady);
  }, []);

  return null;
}

/**
 * Initialize tracking features
 */
function initializeTracking() {
  // Track initial page view
  AnalyticsService.trackPageView({
    url: window.location.href,
    title: document.title,
  });

  // Set up scroll depth tracking
  setupScrollDepthTracking();

  // Set up time on page tracking
  setupTimeOnPageTracking();

  if (analyticsConfig.debug) {
    console.log('Analytics tracking initialized');
  }
}

/**
 * Set up scroll depth tracking
 */
function setupScrollDepthTracking() {
  const thresholds = analyticsConfig.scrollDepthThresholds;
  const tracked = new Set<number>();

  const handleScroll = () => {
    const scrollHeight = document.documentElement.scrollHeight - window.innerHeight;
    const scrolled = window.scrollY;
    const scrollPercentage = Math.round((scrolled / scrollHeight) * 100);

    // Track each threshold once
    thresholds.forEach((threshold) => {
      if (scrollPercentage >= threshold && !tracked.has(threshold)) {
        tracked.add(threshold);
        AnalyticsService.trackScrollDepth({
          depth: threshold,
          page: window.location.pathname,
        });
      }
    });
  };

  // Throttle scroll events
  let ticking = false;
  const throttledScroll = () => {
    if (!ticking) {
      window.requestAnimationFrame(() => {
        handleScroll();
        ticking = false;
      });
      ticking = true;
    }
  };

  window.addEventListener('scroll', throttledScroll, { passive: true });

  // Cleanup on unmount
  return () => window.removeEventListener('scroll', throttledScroll);
}

/**
 * Set up time on page tracking
 */
function setupTimeOnPageTracking() {
  const startTime = Date.now();
  let lastTrackedTime = 0;

  const trackTimeOnPage = () => {
    const timeOnPage = Math.floor((Date.now() - startTime) / 1000); // in seconds
    
    // Track every 30 seconds
    if (timeOnPage - lastTrackedTime >= 30) {
      lastTrackedTime = timeOnPage;
      
      AnalyticsService.trackEvent({
        action: 'time_on_page',
        category: 'engagement',
        label: window.location.pathname,
        value: timeOnPage,
      });
    }
  };

  // Track time on page periodically
  const interval = setInterval(trackTimeOnPage, analyticsConfig.timeOnPageInterval);

  // Track final time on page before leaving
  const handleBeforeUnload = () => {
    const finalTime = Math.floor((Date.now() - startTime) / 1000);
    AnalyticsService.trackEvent({
      action: 'time_on_page_final',
      category: 'engagement',
      label: window.location.pathname,
      value: finalTime,
    });
  };

  window.addEventListener('beforeunload', handleBeforeUnload);

  // Cleanup
  return () => {
    clearInterval(interval);
    window.removeEventListener('beforeunload', handleBeforeUnload);
  };
}
