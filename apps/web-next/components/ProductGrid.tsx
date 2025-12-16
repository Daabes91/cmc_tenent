'use client';

import { useTranslations } from 'next-intl';
import { ProductCard } from './ProductCard';
import type { PublicCarouselItem } from '@/lib/types';

type ProductGridProps = {
  products: PublicCarouselItem[];
  locale: string;
  variant?: 'default' | 'compact' | 'featured';
  columns?: 2 | 3 | 4 | 5;
  loading?: boolean;
};

export function ProductGrid({ 
  products, 
  locale, 
  variant = 'default', 
  columns = 4,
  loading = false 
}: ProductGridProps) {
  const t = useTranslations('ecommerce');
  const gridCols = {
    2: 'grid-cols-1 sm:grid-cols-2',
    3: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3',
    4: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-4',
    5: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-5'
  };

  const gaps = {
    default: 'gap-4',
    compact: 'gap-3',
    featured: 'gap-6'
  };

  if (loading) {
    return (
      <div className={`grid ${gridCols[columns]} ${gaps[variant]}`}>
        {Array.from({ length: 6 }).map((_, index) => (
          <ProductCardSkeleton key={index} variant={variant} />
        ))}
      </div>
    );
  }

  if (!products.length) {
    return (
      <div className="text-center py-16">
        <div className="mx-auto h-24 w-24 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center mb-4">
          <svg className="h-12 w-12 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
          </svg>
        </div>
        <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-2">{t('noProductsFound')}</h3>
        <p className="text-slate-600 dark:text-slate-400">{t('adjustSearchCriteria')}</p>
      </div>
    );
  }

  return (
    <div className={`grid ${gridCols[columns]} ${gaps[variant]}`}>
      {products.map((item) => (
        <ProductCard 
          key={item.id} 
          item={item} 
          locale={locale} 
          variant={variant}
        />
      ))}
    </div>
  );
}

function ProductCardSkeleton({ variant }: { variant: 'default' | 'compact' | 'featured' }) {
  const heights = {
    default: 'h-48',
    compact: 'h-40',
    featured: 'h-64'
  };

  return (
    <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200/60 dark:border-slate-700/60 overflow-hidden animate-pulse">
      <div className={`${heights[variant]} w-full bg-slate-200 dark:bg-slate-700`} />
      <div className="p-4 space-y-3">
        <div className="space-y-2">
          <div className="h-5 bg-slate-200 dark:bg-slate-700 rounded-lg w-3/4" />
          <div className="h-3 bg-slate-200 dark:bg-slate-700 rounded w-full" />
          <div className="h-3 bg-slate-200 dark:bg-slate-700 rounded w-2/3" />
        </div>
        <div className="h-5 bg-slate-200 dark:bg-slate-700 rounded w-1/3" />
        <div className="h-10 bg-slate-200 dark:bg-slate-700 rounded-2xl w-full" />
      </div>
    </div>
  );
}