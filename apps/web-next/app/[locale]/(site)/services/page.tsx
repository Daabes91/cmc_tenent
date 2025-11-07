import type { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';
import { buildLocalizedMetadata } from '@/lib/seo';
import ServicesPageClient from './ServicesPageClient';

type PageProps = {
  params: { locale: string };
};

export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const t = await getTranslations({ locale: params.locale, namespace: 'servicesPage' });

  return buildLocalizedMetadata({
    locale: params.locale,
    path: '/services',
    title: t('header.title'),
    description: t('header.subtitle'),
    keywords: ['dental treatments', 'orthodontics', 'implants'],
  });
}

export default function ServicesPage() {
  return <ServicesPageClient />;
}
