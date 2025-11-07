import type { MetadataRoute } from 'next';
import { locales } from '@/i18n/request';
import { buildLocalizedUrl } from '@/lib/seo';
import { getBlogsServer } from '@/lib/server-api';

const STATIC_ROUTES = ['/', '/services', '/doctors', '/appointments', '/blog', '/login', '/signup'];

const safeDate = (value?: string | null): Date | undefined => {
  if (!value) return undefined;
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? undefined : parsed;
};

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const routes: MetadataRoute.Sitemap = [];

  for (const locale of locales) {
    for (const route of STATIC_ROUTES) {
      routes.push({
        url: buildLocalizedUrl(locale, route),
        changeFrequency: route === '/' ? 'daily' : 'weekly',
        priority: route === '/' ? 1 : 0.8,
      });
    }
  }

  const blogEntries = await Promise.all(
    locales.map(async (locale) => {
      const posts = await getBlogsServer(locale);
      return posts.map((post) => {
        const lastModified = safeDate(post.publishedAt) ?? safeDate(post.createdAt);

        return {
          url: buildLocalizedUrl(locale, `/blog/${post.slug}`),
          ...(lastModified ? { lastModified } : {}),
          changeFrequency: 'weekly',
          priority: 0.7,
        };
      });
    }),
  );

  blogEntries.forEach((entryGroup) => {
    routes.push(...entryGroup);
  });

  return routes;
}
