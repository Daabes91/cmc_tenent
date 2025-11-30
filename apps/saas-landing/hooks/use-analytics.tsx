'use client';

/**
 * useAnalytics Hook
 * React hook for easy analytics tracking in components
 */

import { useCallback, useEffect, useRef } from 'react';
import { usePathname } from 'next/navigation';
import { 
  trackPageView, 
  trackEvent, 
  trackConversion,
  trackCTAClick,
  trackFormSubmission,
  trackFeatureInteraction,
  type ConversionEvent,
  type AnalyticsEvent,
} from '@/lib/analytics';

export function useAnalytics() {
  const pathname = usePathname();
  const previousPathname = useRef<string | null>(null);

  // Track page views on route change
  useEffect(() => {
    if (pathname && pathname !== previousPathname.current) {
      trackPageView({
        url: window.location.href,
        title: document.title,
      });
      previousPathname.current = pathname;
    }
  }, [pathname]);

  // Memoized tracking functions
  const trackCTA = useCallback((
    ctaText: string,
    ctaLocation: string
  ) => {
    trackCTAClick(ctaText, ctaLocation);
  }, []);

  const trackForm = useCallback((
    formName: string,
    formType: string
  ) => {
    trackFormSubmission(formName, formType);
  }, []);

  const trackFeature = useCallback((
    featureName: string,
    interactionType: string
  ) => {
    trackFeatureInteraction(featureName, interactionType);
  }, []);

  const trackCustomEvent = useCallback((event: AnalyticsEvent) => {
    trackEvent(event);
  }, []);

  const trackCustomConversion = useCallback((event: ConversionEvent) => {
    trackConversion(event);
  }, []);

  return {
    trackCTA,
    trackForm,
    trackFeature,
    trackEvent: trackCustomEvent,
    trackConversion: trackCustomConversion,
  };
}

/**
 * usePageViewTracking Hook
 * Automatically tracks page views with custom title
 */
export function usePageViewTracking(pageTitle?: string) {
  const pathname = usePathname();

  useEffect(() => {
    trackPageView({
      url: window.location.href,
      title: pageTitle || document.title,
    });
  }, [pathname, pageTitle]);
}

/**
 * useTimeOnPage Hook
 * Tracks time spent on page and returns elapsed time
 */
export function useTimeOnPage() {
  const startTime = useRef(Date.now());
  const elapsedTime = useRef(0);

  useEffect(() => {
    const interval = setInterval(() => {
      elapsedTime.current = Math.floor((Date.now() - startTime.current) / 1000);
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  const getElapsedTime = useCallback(() => {
    return elapsedTime.current;
  }, []);

  return { getElapsedTime };
}

/**
 * useScrollTracking Hook
 * Tracks scroll depth on the current page
 */
export function useScrollTracking(thresholds: number[] = [25, 50, 75, 100]) {
  const tracked = useRef(new Set<number>());

  useEffect(() => {
    const handleScroll = () => {
      const scrollHeight = document.documentElement.scrollHeight - window.innerHeight;
      const scrolled = window.scrollY;
      const scrollPercentage = Math.round((scrolled / scrollHeight) * 100);

      thresholds.forEach((threshold) => {
        if (scrollPercentage >= threshold && !tracked.current.has(threshold)) {
          tracked.current.add(threshold);
          trackEvent({
            action: 'scroll_depth',
            category: 'engagement',
            label: `${threshold}%`,
            value: threshold,
          });
        }
      });
    };

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
    return () => window.removeEventListener('scroll', throttledScroll);
  }, [thresholds]);
}
