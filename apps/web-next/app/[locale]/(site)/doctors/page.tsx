import type { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';
import { buildLocalizedMetadata } from '@/lib/seo';
import DoctorsPageClient from './DoctorsPageClient';

type PageProps = {
  params: { locale: string };
};

export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const t = await getTranslations({ locale: params.locale, namespace: 'doctors' });

  return buildLocalizedMetadata({
    locale: params.locale,
    path: '/doctors',
    title: t('list.hero.title'),
    description: t('list.hero.subtitle'),
    keywords: ['dentist team', 'specialist dentists', 'dental experts'],
  });
}

export default function DoctorsPage() {
  return <DoctorsPageClient />;
}
