import type { NextConfig } from 'next';
import createNextIntlPlugin from 'next-intl/plugin';

const withNextIntl = createNextIntlPlugin('./i18n/request.ts');

const disallowedBasePaths = new Set([
  'admin',
  '/admin',
  'admin-panel',
  '/admin-panel',
]);

const shouldIgnoreBasePath = (value?: string | null) => {
  if (!value) return true;
  const trimmed = value.trim();
  if (!trimmed || trimmed === '/') return true;
  const normalized = trimmed.endsWith('/') ? trimmed.slice(0, -1) : trimmed;
  return disallowedBasePaths.has(normalized);
};

const rawBasePath = process.env.NEXT_PUBLIC_BASE_PATH;
const basePath =
  !shouldIgnoreBasePath(rawBasePath)
    ? rawBasePath!.endsWith('/')
      ? rawBasePath!.slice(0, -1)
      : rawBasePath!
    : '';

const nextConfig: NextConfig = {
  ...(basePath ? { basePath } : {}),
  async redirects() {
    if (!basePath) {
      return [];
    }

    return [
      {
        source: '/',
        destination: basePath,
        permanent: false,
      },
    ];
  },
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'Strict-Transport-Security',
            value: 'max-age=63072000; includeSubDomains; preload',
          },
        ],
      },
    ];
  },
  // Disable linting and type checking during production builds
  eslint: {
    ignoreDuringBuilds: true,
  },
  typescript: {
    ignoreBuildErrors: true,
  },
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'images.unsplash.com',
      },
      {
        protocol: 'https',
        hostname: 'imagedelivery.net',
      },
    ],
  },
  env: {
    NEXT_PUBLIC_API_URL:
      process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/public',
    NEXT_PUBLIC_BASE_PATH: basePath,
    NEXT_PUBLIC_SITE_URL:
      process.env.NEXT_PUBLIC_SITE_URL ||
      process.env.SITE_URL ||
      (process.env.VERCEL_URL ? `https://${process.env.VERCEL_URL}` : 'http://localhost:3000'),
    NEXT_PUBLIC_GA_MEASUREMENT_ID: process.env.NEXT_PUBLIC_GA_MEASUREMENT_ID || '',
  },
};

export default withNextIntl(nextConfig);
