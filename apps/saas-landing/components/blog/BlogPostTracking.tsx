'use client';

/**
 * Blog Post Tracking Component
 * Tracks blog post engagement metrics including time on page and scroll depth
 */

import { useEffect, useRef } from 'react';
import { trackBlogRead, trackScrollDepth } from '@/lib/analytics';

interface BlogPostTrackingProps {
  postTitle: string;
  postSlug: string;
}

export function BlogPostTracking({ postTitle, postSlug }: BlogPostTrackingProps) {
  const startTime = useRef(Date.now());
  const tracked = useRef(new Set<number>());
  const hasTrackedRead = useRef(false);

  useEffect(() => {
    // Track initial blog read event
    if (!hasTrackedRead.current) {
      trackBlogRead(postTitle, postSlug);
      hasTrackedRead.current = true;
    }

    // Set up scroll depth tracking
    const thresholds = [25, 50, 75, 100];
    
    const handleScroll = () => {
      const scrollHeight = document.documentElement.scrollHeight - window.innerHeight;
      const scrolled = window.scrollY;
      const scrollPercentage = Math.round((scrolled / scrollHeight) * 100);

      thresholds.forEach((threshold) => {
        if (scrollPercentage >= threshold && !tracked.current.has(threshold)) {
          tracked.current.add(threshold);
          trackScrollDepth({
            depth: threshold,
            page: `/blog/${postSlug}`,
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

    // Track time on page when user leaves
    const handleBeforeUnload = () => {
      const timeOnPage = Math.floor((Date.now() - startTime.current) / 1000);
      trackBlogRead(postTitle, postSlug, timeOnPage);
    };

    window.addEventListener('beforeunload', handleBeforeUnload);

    // Cleanup
    return () => {
      window.removeEventListener('scroll', throttledScroll);
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [postTitle, postSlug]);

  return null;
}
