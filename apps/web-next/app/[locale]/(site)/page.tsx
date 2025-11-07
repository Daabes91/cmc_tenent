import type { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';
import { buildLocalizedMetadata } from '@/lib/seo';
import HomePageClient from './HomePageClient';

type PageProps = {
  params: { locale: string };
};

export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const t = await getTranslations({ locale: params.locale, namespace: 'hero' });

  return buildLocalizedMetadata({
    locale: params.locale,
    path: '/',
    title: t('title'),
    description: t('subtitle'),
    keywords: ['dental services', 'cosmetic dentistry', 'orthodontics'],
  });
}

export default function HomePage() {
  return <HomePageClient />;
}
