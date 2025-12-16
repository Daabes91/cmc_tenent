'use client';

import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useLocale, useTranslations } from 'next-intl';
import { api } from '@/lib/api';
import type { Product, ProductImage } from '@/lib/types';
import { ProductImageGallery } from '@/components/ProductImageGallery';
import { ProductInfo } from '@/components/ProductInfo';
import { ProductActions } from '@/components/ProductActions';

export default function ProductDetailPage() {
  const { slug } = useParams<{ slug: string }>();
  const router = useRouter();
  const locale = useLocale();
  const t = useTranslations('ecommerce.productDetails');
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      if (!slug) return;
      setLoading(true);
      setError(null);
      try {
        const data = await api.getProductBySlug(Array.isArray(slug) ? slug[0] : slug, locale);
        setProduct(data);
      } catch (err: any) {
        console.error('Failed to load product', err);
        setError(err?.data?.message || 'Product not found');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [slug, locale]);

  const displayName = locale === 'ar' && product?.nameAr ? product.nameAr : product?.name;

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
        <div className="max-w-7xl mx-auto px-4 py-8">
          {/* Breadcrumb Skeleton */}
          <div className="mb-8">
            <div className="h-4 w-48 rounded bg-slate-200 dark:bg-slate-700 animate-pulse" />
          </div>
          
          {/* Main Content Skeleton */}
          <div className="grid gap-12 lg:grid-cols-2">
            {/* Image Gallery Skeleton */}
            <div className="space-y-4">
              <div className="aspect-square rounded-3xl bg-slate-200 dark:bg-slate-700 animate-pulse" />
              <div className="flex gap-3">
                {[...Array(4)].map((_, i) => (
                  <div key={i} className="w-20 h-20 rounded-2xl bg-slate-200 dark:bg-slate-700 animate-pulse" />
                ))}
              </div>
            </div>
            
            {/* Product Info Skeleton */}
            <div className="space-y-6">
              <div className="space-y-3">
                <div className="h-10 w-3/4 rounded bg-slate-200 dark:bg-slate-700 animate-pulse" />
                <div className="h-6 w-full rounded bg-slate-200 dark:bg-slate-700 animate-pulse" />
                <div className="h-6 w-2/3 rounded bg-slate-200 dark:bg-slate-700 animate-pulse" />
              </div>
              <div className="h-12 w-48 rounded bg-slate-200 dark:bg-slate-700 animate-pulse" />
              <div className="flex gap-4">
                <div className="h-14 w-32 rounded bg-slate-200 dark:bg-slate-700 animate-pulse" />
                <div className="h-14 w-40 rounded bg-slate-200 dark:bg-slate-700 animate-pulse" />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900 flex items-center justify-center">
        <div className="max-w-md mx-auto text-center p-8">
          <div className="w-24 h-24 mx-auto mb-6 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
            <svg className="w-12 h-12 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-4">{t('errors.productNotFound')}</h2>
          <p className="text-slate-600 dark:text-slate-300 mb-6">{error}</p>
          <button
            onClick={() => router.push(`/${locale}/products`)}
            className="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-xl font-semibold transition-colors"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            {t('backToProducts')}
          </button>
        </div>
      </div>
    );
  }

  if (!product) return null;

  const images = product?.images as ProductImage[] | undefined;

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
      <div className="max-w-7xl mx-auto px-4 py-8">
        {/* Enhanced Breadcrumb */}
        <nav className="mb-8" aria-label="Breadcrumb">
          <div className="flex items-center gap-2 text-sm">
            <Link 
              href={`/${locale}/products`} 
              className="flex items-center gap-1 text-slate-600 dark:text-slate-300 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
              </svg>
              {t('products')}
            </Link>
            <svg className="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
            <span className="text-slate-800 dark:text-slate-100 font-medium">
              {displayName || (Array.isArray(slug) ? slug[0] : slug)}
            </span>
          </div>
        </nav>

        {/* Main Product Content */}
        <div className="grid gap-12 lg:grid-cols-2">
          {/* Enhanced Image Gallery */}
          <ProductImageGallery 
            images={images || []}
            productName={displayName || ''}
            mainImageUrl={product.mainImageUrl}
          />

          {/* Product Information and Actions */}
          <div className="space-y-8">
            <ProductInfo product={product} locale={locale} />
            <ProductActions 
              product={product} 
              onAddToCart={() => {
                // Optional callback for when item is added to cart
                console.log('Item added to cart');
              }}
            />
            

          </div>
        </div>


      </div>
    </div>
  );
}
