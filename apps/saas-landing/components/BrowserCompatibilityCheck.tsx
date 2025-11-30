'use client';

import { useEffect } from 'react';
import { logBrowserCompatibility } from '@/lib/browser-compat';

/**
 * Component that logs browser compatibility information in development mode
 * This component renders nothing but runs compatibility checks on mount
 */
export function BrowserCompatibilityCheck() {
  useEffect(() => {
    // Only log in development mode
    if (process.env.NODE_ENV === 'development') {
      logBrowserCompatibility();
    }
  }, []);

  // This component doesn't render anything
  return null;
}
