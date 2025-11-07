import type { MetadataRoute } from 'next';
import { buildAbsoluteUrl, getSiteUrl } from '@/lib/seo';

export default function robots(): MetadataRoute.Robots {
  return {
    rules: [
      {
        userAgent: '*',
        allow: '/',
        disallow: ['/api', '/dashboard', '/paypal-test', '/paypal-debug', '/staff', '/patient'],
      },
    ],
    sitemap: buildAbsoluteUrl('/sitemap.xml'),
    host: getSiteUrl(),
  };
}
