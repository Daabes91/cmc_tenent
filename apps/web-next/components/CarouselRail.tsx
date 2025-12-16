 'use client';

import { useEffect, useState } from 'react';
import { useLocale, useTranslations } from 'next-intl';
import { Link } from '@/navigation';
import type { PublicCarousel, PublicCarouselItem } from '@/lib/types';
import { api } from '@/lib/api';
import { ProductCard } from './ProductCard';

type CarouselRailProps = {
  title: string;
  carousels: PublicCarousel[];
};



function CarouselItems({ carousel, locale }: { carousel: PublicCarousel; locale: string }) {
  const [fallbackItems, setFallbackItems] = useState<PublicCarouselItem[]>([]);

  useEffect(() => {
    const loadFallback = async () => {
      if (carousel.type !== 'VIEW_ALL_PRODUCTS' || (carousel.items?.length ?? 0) > 0) return;
      try {
        const res = await api.getProducts({ size: 12, status: 'ACTIVE', locale });
        const items: PublicCarouselItem[] =
          res.items?.map((product) => ({
            id: product.id,
            contentType: 'VIEW_ALL_PRODUCTS',
            product,
          })) ?? [];
        setFallbackItems(items);
      } catch (err) {
        console.error('Failed to load products for view-all carousel', err);
      }
    };
    loadFallback();
  }, [carousel, locale]);

  const items = carousel.items?.length ? carousel.items : fallbackItems;
  if (!items?.length) return null;

  const visibleItems = items.slice(0, 4);

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      {visibleItems.map((item) => (
        <ProductCard key={item.id} item={item} locale={locale} variant="default" />
      ))}
    </div>
  );
}

export function CarouselRail({ title, carousels }: CarouselRailProps) {
  const locale = useLocale();
  const ecommerce = useTranslations('ecommerce');
  if (!carousels.length) return null;
  return (
    <section className="relative overflow-hidden bg-slate-50 dark:bg-slate-950 py-16 transition-colors">
      <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
        <div className="mb-8 flex items-center justify-between">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">{title}</h2>
          <Link
            href="/products"
            className="inline-flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-6 py-3 text-sm font-semibold text-slate-700 shadow-sm transition-all hover:border-slate-300 hover:bg-slate-50 hover:shadow-md dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300 dark:hover:border-slate-600 dark:hover:bg-slate-700"
          >
            {ecommerce('viewAllProducts')}
            <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </Link>
        </div>
        <div className="space-y-12">
          {carousels.map((carousel) => (
            <div key={carousel.id} className="space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-xl font-semibold text-slate-900 dark:text-slate-100">
                    {locale === 'ar' && carousel.nameAr ? carousel.nameAr : carousel.name}
                  </h3>
                  {carousel.slug && (
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
                      {carousel.slug.replace(/[-_]/g, ' ')}
                    </p>
                  )}
                </div>
              </div>
              <CarouselItems carousel={carousel} locale={locale} />
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
