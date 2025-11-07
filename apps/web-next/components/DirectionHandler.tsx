'use client';

import { useEffect } from 'react';
import { useLocale } from 'next-intl';

/**
 * Client component that updates the HTML dir and lang attributes
 * when the locale changes without requiring a page refresh
 */
export function DirectionHandler() {
  const locale = useLocale();

  useEffect(() => {
    const dir = locale === 'ar' ? 'rtl' : 'ltr';

    // Update the HTML element's dir attribute
    document.documentElement.dir = dir;

    // Also update the lang attribute for consistency
    document.documentElement.lang = locale;
  }, [locale]);

  return null;
}
