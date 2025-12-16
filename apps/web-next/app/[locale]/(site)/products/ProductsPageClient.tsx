'use client';

import { useEffect, useMemo, useState } from 'react';
import Image from 'next/image';
import { useLocale, useTranslations } from 'next-intl';
import { Link } from '@/navigation';
import { useSWRConfig } from 'swr';
import { api } from '@/lib/api';
import type { Product, ProductImage } from '@/lib/types';
import { useEcommerceFeature } from '@/hooks/useEcommerce';
import { ProductCard } from '@/components/ProductCard';

export default function ProductsPageClient() {
  const t = useTranslations('productsPage');
  const locale = useLocale();
  const { enabled: ecommerceEnabled, isLoading: ecommerceLoading } = useEcommerceFeature();

  const [products, setProducts] = useState<Product[]>([]);
  const [page, setPage] = useState(0);
  const [size] = useState(9);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    const loadProducts = async () => {
      if (!ecommerceEnabled) {
        setProducts([]);
        setLoading(false);
        return;
      }

      setLoading(true);
      setError('');
      try {
        const res = await api.getProducts({ page, size, status: 'ACTIVE', locale });
        if (cancelled) return;
        setProducts(res.items ?? []);
        setTotal(res.total ?? res.items?.length ?? 0);
      } catch (err) {
        console.error('[ProductsPage] Failed to load products', err);
        if (!cancelled) {
          setError(t('errors.load'));
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void loadProducts();
    return () => {
      cancelled = true;
    };
  }, [page, size, ecommerceEnabled, locale, t]);

  const totalPages = useMemo(() => {
    const denominator = size || 1;
    const derived = Math.ceil((total || 0) / denominator);
    return derived > 0 ? derived : 1;
  }, [total, size]);

  const paginationLabel = t('pagination.page', { page: page + 1, total: totalPages });

  if (!ecommerceEnabled && !ecommerceLoading) {
    return (
      <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
        <section className="py-16">
          <div className="mx-auto max-w-4xl px-4 md:px-6 lg:px-8">
            <div className="rounded-2xl border border-yellow-200 bg-yellow-50 p-6 text-yellow-800 shadow-md dark:border-yellow-900/50 dark:bg-yellow-900/20 dark:text-yellow-100">
              <div className="flex items-center gap-3">
                <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div>
                  <p className="text-lg font-semibold">{t('header.title')}</p>
                  <p className="text-sm text-yellow-700 dark:text-yellow-200/80">{t('disabled')}</p>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
      <section className="py-16 bg-gradient-to-r from-blue-600 to-cyan-600 dark:from-blue-800/80 dark:via-blue-900/80 dark:to-slate-900">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          <div className="max-w-3xl">
            <p className="text-blue-100 uppercase tracking-wide text-sm font-semibold">
              {t('header.eyebrow')}
            </p>
            <h1 className="mt-3 text-4xl md:text-5xl font-bold text-white">{t('header.title')}</h1>
            <p className="mt-4 text-blue-50 text-lg">{t('header.subtitle')}</p>
          </div>
        </div>
      </section>

      <section className="py-14">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          {loading ? (
            <div className="flex items-center justify-center py-16">
              <div className="text-center">
                <div className="inline-block h-12 w-12 animate-spin rounded-full border-4 border-blue-600 border-t-transparent dark:border-blue-400"></div>
                <p className="mt-4 font-medium text-slate-600 dark:text-slate-300">{t('loading')}</p>
              </div>
            </div>
          ) : error ? (
            <div className="mx-auto max-w-3xl rounded-2xl border-2 border-red-200 bg-red-50 p-6 text-red-700 shadow-md dark:border-red-900/60 dark:bg-red-950/40 dark:text-red-200">
              <div className="flex items-center gap-3">
                <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span className="font-semibold">{error}</span>
              </div>
            </div>
          ) : products.length === 0 ? (
            <div className="max-w-3xl mx-auto rounded-2xl border-2 border-dashed border-slate-200 bg-white p-10 text-center shadow-sm dark:border-slate-800 dark:bg-slate-900/60">
              <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-slate-100 text-slate-400 dark:bg-slate-800 dark:text-slate-500">
                <svg className="h-7 w-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-slate-900 dark:text-slate-100">{t('empty.title')}</h3>
              <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{t('empty.description')}</p>
            </div>
          ) : (
            <div className="space-y-8">
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                {products.map((product) => {
                  // Convert Product to PublicCarouselItem format
                  const carouselItem = {
                    id: product.id,
                    contentType: 'PRODUCT' as const,
                    product: product
                  };
                  
                  return (
                    <ProductCard
                      key={product.id}
                      item={carouselItem}
                      locale={locale}
                      variant="default"
                    />
                  );
                })}
              </div>

              {totalPages > 1 && (
                <div className="flex flex-col items-center gap-3 rounded-xl border border-slate-200 bg-white px-4 py-3 shadow-sm dark:border-slate-800 dark:bg-slate-900/70 sm:flex-row sm:justify-between">
                  <div className="flex items-center gap-2 text-sm font-semibold text-slate-700 dark:text-slate-200">
                    <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                    </svg>
                    <span>{paginationLabel}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                      disabled={page === 0}
                      className="inline-flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:border-blue-200 hover:text-blue-700 disabled:cursor-not-allowed disabled:opacity-50 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-200 dark:hover:border-blue-700 dark:hover:text-blue-300"
                    >
                      <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                      </svg>
                      {t('pagination.previous')}
                    </button>
                    <button
                      onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))}
                      disabled={page >= totalPages - 1}
                      className="inline-flex items-center gap-2 rounded-lg border border-blue-200 bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60 dark:border-blue-800 dark:bg-blue-700"
                    >
                      {t('pagination.next')}
                      <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                      </svg>
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </section>
    </main>
  );
}
