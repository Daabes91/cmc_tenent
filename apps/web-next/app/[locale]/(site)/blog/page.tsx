import type { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';
import { buildLocalizedMetadata } from '@/lib/seo';
import BlogPageClient from './BlogPageClient';

type PageProps = {
  params: { locale: string };
};

export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const t = await getTranslations({ locale: params.locale, namespace: 'blogPage' });

  return buildLocalizedMetadata({
    locale: params.locale,
    path: '/blog',
    title: t('header.title'),
    description: t('header.subtitle'),
    keywords: ['dental blog', 'oral health tips', 'cosmetic dentistry advice'],
  });
}

export default function BlogPage() {
  return <BlogPageClient />;
}
