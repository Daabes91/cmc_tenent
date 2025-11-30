'use client';

/**
 * Client component for dynamic head management in web-next
 * Handles dynamic title and favicon based on clinic settings and current route
 */

import { useEffect } from 'react';
import { usePathname } from 'next/navigation';
import { useClinicBranding } from '@/hooks/useClinicSettings';
import { getPageTitleFromRoute, formatWebTitle } from '@/utils/pageTitleUtils';
import { updateFavicon } from '@/utils/faviconUtils';

interface DynamicHeadProps {
  /** Optional initial page title override */
  initialTitle?: string;
  /** Optional locale override */
  locale?: string;
}

export function DynamicHead({ initialTitle, locale = 'en' }: DynamicHeadProps) {
  const pathname = usePathname();
  const { clinicName, faviconUrl, isLoading } = useClinicBranding();

  useEffect(() => {
    // Don't update if still loading clinic settings
    if (isLoading) return;

    // Get page title from route or use provided initial title
    const pageTitle = initialTitle || getPageTitleFromRoute(pathname, locale);
    
    // Format complete title with clinic name
    const fullTitle = formatWebTitle(pageTitle, clinicName);
    
    // Update document title
    document.title = fullTitle;

    // Update favicon with tenant-specific value or fallback
    updateFavicon(faviconUrl);
    
  }, [pathname, locale, clinicName, faviconUrl, isLoading, initialTitle]);

  return null; // This component doesn't render anything
}
