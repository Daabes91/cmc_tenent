/**
 * Favicon utilities for web-next
 * Simplified utilities for static favicon handling
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