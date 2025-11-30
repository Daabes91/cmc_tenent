import { withBasePath } from './basePath';

/**
 * Favicon utilities for web-next
 * Supports tenant-aware favicon resolution and DOM updates
 */

/**
 * Default favicon for web application
 */
export const DEFAULT_FAVICON = '/favicon.ico';

/**
 * Get the appropriate MIME type for favicon link element
 * 
 * @param url - The favicon URL
 * @returns MIME type string
 */
export function getFaviconMimeType(url: string): string {
  const extension = url.toLowerCase().split('.').pop();
  
  const mimeTypeMap: Record<string, string> = {
    'ico': 'image/x-icon',
    'png': 'image/png',
    'jpg': 'image/jpeg',
    'jpeg': 'image/jpeg',
    'gif': 'image/gif',
    'svg': 'image/svg+xml',
    'webp': 'image/webp'
  };
  
  return mimeTypeMap[extension || ''] || 'image/x-icon';
}

/**
 * Normalize favicon URL with base path support and fallback
 * 
 * @param url - Favicon URL from settings
 * @returns Resolved URL string
 */
export function resolveFaviconUrl(url?: string | null): string {
  if (!url || !url.trim()) {
    return withBasePath(DEFAULT_FAVICON);
  }

  const trimmed = url.trim();

  if (/^https?:\/\//i.test(trimmed)) {
    return trimmed;
  }

  if (trimmed.startsWith('/')) {
    return withBasePath(trimmed);
  }

  return withBasePath(`/${trimmed}`);
}

/**
 * Update or create favicon link elements in the document head
 * 
 * @param url - Target favicon URL
 */
export function updateFavicon(url?: string | null): void {
  if (typeof document === 'undefined') return;

  const href = resolveFaviconUrl(url);
  const mimeType = getFaviconMimeType(href);
  const relValues = ['icon', 'shortcut icon', 'apple-touch-icon'];

  relValues.forEach(rel => {
    let link = document.querySelector<HTMLLinkElement>(`link[rel="${rel}"]`);

    if (!link) {
      link = document.createElement('link');
      link.rel = rel;
      document.head.appendChild(link);
    }

    link.href = href;
    if (rel !== 'apple-touch-icon') {
      link.type = mimeType;
    }
  });
}
