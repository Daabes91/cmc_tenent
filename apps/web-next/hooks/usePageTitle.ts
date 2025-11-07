'use client';

/**
 * Hook for setting custom page titles in web-next application
 * Allows individual pages to override the default route-based title
 */

import { useEffect } from 'react';
import { useLocale } from 'next-intl';
import { useClinicBranding } from './useClinicSettings';
import { formatWebTitle } from '@/utils/pageTitleUtils';

/**
 * Set a custom page title that will be formatted with clinic name
 * 
 * @param title - The page-specific title to set
 */
export function usePageTitle(title: string) {
  const locale = useLocale();
  const { clinicName, isLoading } = useClinicBranding();

  useEffect(() => {
    // Don't update if still loading clinic settings
    if (isLoading || !title) return;

    // Format complete title with clinic name
    const fullTitle = formatWebTitle(title, clinicName);
    
    // Update document title
    document.title = fullTitle;
    
  }, [title, clinicName, isLoading]);
}

/**
 * Hook for setting localized page titles
 * Useful for pages that need different titles based on locale
 * 
 * @param titles - Object with locale-specific titles
 */
export function useLocalizedPageTitle(titles: { en: string; ar: string }) {
  const locale = useLocale();
  const title = titles[locale as keyof typeof titles] || titles.en;
  
  usePageTitle(title);
}