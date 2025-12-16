import type { Metadata } from 'next';
import { getTranslations } from 'next-intl/server';
import { buildLocalizedMetadata } from '@/lib/seo';
import ProductsPageClient from './ProductsPageClient';

type PageProps = {
  params: { locale: string };
};

export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const t = await getTranslations({ locale: params.locale, namespace: 'productsPage' });

  return buildLocalizedMetadata({
    locale: params.locale,
    path: '/products',
    title: t('header.title'),
    description: t('header.subtitle'),
    keywords: ['dental products', 'oral care', 'treatments', 'clinic shop'],
  });
}

export default function ProductsPage() {
  return <ProductsPageClient />;
}
