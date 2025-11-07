import type { Metadata } from 'next';
import { locales } from '@/i18n/request';
import { withBasePath } from '@/utils/basePath';
import { getClinicSettingsServer, getSafeClinicNameServer } from './server-api';

const FALLBACK_DESCRIPTION =
  'Trusted dental clinic for preventive care, cosmetic dentistry, orthodontics, and virtual consultations with easy online booking.';

const FALLBACK_SOCIAL_IMAGE =
  'https://images.unsplash.com/photo-1606811971618-4486d14f3f99?q=80&w=1200&auto=format&fit=crop';

export const DEFAULT_KEYWORDS = [
  'dental clinic',
  'dentist',
  'cosmetic dentistry',
  'orthodontics',
  'teeth whitening',
  'dental implants',
  'emergency dental care',
  'virtual dental consultation',
];

export function getSiteUrl(): string {
  const envUrl =
    process.env.NEXT_PUBLIC_SITE_URL ||
    process.env.SITE_URL ||
    (process.env.VERCEL_URL ? `https://${process.env.VERCEL_URL}` : '');

  const formatted = envUrl?.trim().replace(/\/$/, '');
  return formatted || 'http://localhost:3000';
}

function ensureAbsoluteUrl(url?: string | null): string | undefined {
  if (!url) return undefined;
  if (url.startsWith('http')) return url;
  const normalized = url.startsWith('/') ? url : `/${url}`;
  return `${getSiteUrl()}${withBasePath(normalized)}`;
}

function normalizePath(path?: string): string {
  if (!path || path === '/' || path === '') {
    return '';
  }
  return path.startsWith('/') ? path : `/${path}`;
}

export function buildLocalizedPath(locale: string, path?: string): string {
  const normalized = normalizePath(path);
  return withBasePath(`/${locale}${normalized}`);
}

export function buildLocalizedUrl(locale: string, path?: string): string {
  return `${getSiteUrl()}${buildLocalizedPath(locale, path)}`;
}

export function buildAbsoluteUrl(path: string): string {
  const normalized = path.startsWith('/') ? path : `/${path}`;
  return `${getSiteUrl()}${withBasePath(normalized)}`;
}

export function getLanguageAlternates(path?: string): Record<string, string> {
  const alternates: Record<string, string> = {};
  for (const locale of locales) {
    alternates[locale] = buildLocalizedUrl(locale, path);
  }
  return alternates;
}

interface SeoDefaults {
  clinicName: string;
  description: string;
  keywords: string[];
  socialImage: string;
}

async function getSeoDefaults(): Promise<SeoDefaults> {
  const clinicSettings = await getClinicSettingsServer();
  const clinicName = getSafeClinicNameServer(clinicSettings?.clinicName);
  const description = clinicSettings?.tagline?.trim() || FALLBACK_DESCRIPTION;
  const keywords = Array.from(new Set([...DEFAULT_KEYWORDS, clinicName]));
  const socialImage =
    ensureAbsoluteUrl(clinicSettings?.logoUrl) || FALLBACK_SOCIAL_IMAGE;

  return {
    clinicName,
    description,
    keywords,
    socialImage,
  };
}

interface BuildMetadataOptions {
  locale: string;
  path?: string;
  title?: string;
  description?: string;
  keywords?: string[];
  image?: string;
}

export async function buildLocalizedMetadata({
  locale,
  path = '/',
  title,
  description,
  keywords = [],
  image,
}: BuildMetadataOptions): Promise<Metadata> {
  const seoDefaults = await getSeoDefaults();
  const canonicalUrl = buildLocalizedUrl(locale, path);
  const ogLocale = locale === 'ar' ? 'ar' : 'en_US';
  const mergedKeywords = Array.from(
    new Set([...seoDefaults.keywords, ...keywords]),
  );
  const socialImage = ensureAbsoluteUrl(image) || seoDefaults.socialImage;
  const pageTitle = title || 'Dental Care';
  const metaDescription = description || seoDefaults.description;

  return {
    title: pageTitle,
    description: metaDescription,
    keywords: mergedKeywords,
    alternates: {
      canonical: canonicalUrl,
      languages: getLanguageAlternates(path),
    },
    openGraph: {
      title: `${seoDefaults.clinicName} | ${pageTitle}`,
      description: metaDescription,
      url: canonicalUrl,
      type: 'website',
      siteName: seoDefaults.clinicName,
      locale: ogLocale,
      images: [
        {
          url: socialImage,
          width: 1200,
          height: 630,
          alt: `${seoDefaults.clinicName} dental clinic`,
        },
      ],
    },
    twitter: {
      card: 'summary_large_image',
      title: `${seoDefaults.clinicName} | ${pageTitle}`,
      description: metaDescription,
      images: [socialImage],
    },
  };
}

export async function buildRootMetadata(): Promise<Metadata> {
  const seoDefaults = await getSeoDefaults();
  const siteUrl = getSiteUrl();

  return {
    metadataBase: new URL(siteUrl),
    title: {
      default: `${seoDefaults.clinicName} | Advanced Dental Care & Cosmetic Dentistry`,
      template: `${seoDefaults.clinicName} | %s`,
    },
    description: seoDefaults.description,
    keywords: seoDefaults.keywords,
    applicationName: seoDefaults.clinicName,
    category: 'Health & Medical > Dental',
    alternates: {
      canonical: buildLocalizedUrl(locales[0], '/'),
      languages: getLanguageAlternates('/'),
    },
    openGraph: {
      title: `${seoDefaults.clinicName} | Advanced Dental Care & Cosmetic Dentistry`,
      description: seoDefaults.description,
      url: buildLocalizedUrl(locales[0], '/'),
      siteName: seoDefaults.clinicName,
      type: 'website',
      locale: 'en_US',
      alternateLocale: locales
        .filter((locale) => locale !== 'en')
        .map((locale) => (locale === 'ar' ? 'ar' : locale)),
      images: [
        {
          url: seoDefaults.socialImage,
          width: 1200,
          height: 630,
          alt: `${seoDefaults.clinicName} dental clinic`,
        },
      ],
    },
    twitter: {
      card: 'summary_large_image',
      title: `${seoDefaults.clinicName} | Advanced Dental Care & Cosmetic Dentistry`,
      description: seoDefaults.description,
      images: [seoDefaults.socialImage],
    },
    icons: {
      icon: '/favicon.ico',
      apple: '/favicon.ico',
    },
    robots: {
      index: true,
      follow: true,
      googleBot: {
        index: true,
        follow: true,
      },
    },
  };
}
