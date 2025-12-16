'use client';

import { useTranslations } from 'next-intl';
import type { Product } from '@/lib/types';

interface ProductInfoProps {
  product: Product;
  locale: string;
}

export function ProductInfo({ product, locale }: ProductInfoProps) {
  const t = useTranslations('ecommerce.productDetails');
  const displayName = locale === 'ar' && product?.nameAr ? product.nameAr : product?.name;
  const displayShortDescription = locale === 'ar' && product?.shortDescriptionAr ? product.shortDescriptionAr : product?.shortDescription;
  const displayDescription = locale === 'ar' && product?.descriptionAr ? product.descriptionAr : product?.description;

  const hasDiscount = product?.compareAtPrice && product?.price && product.compareAtPrice > product.price;
  const discountPercentage = hasDiscount ? Math.round(((product.compareAtPrice! - product.price!) / product.compareAtPrice!) * 100) : 0;

  return (
    <div className="space-y-8">
      {/* Product Header */}
      <div className="space-y-4">
        <div className="flex items-start justify-between gap-4">
          <h1 className="text-2xl lg:text-3xl font-bold text-slate-900 dark:text-white leading-tight">
            {displayName}
          </h1>
          {hasDiscount && (
            <div className="bg-gradient-to-r from-red-500 to-pink-500 text-white text-sm font-bold px-3 py-1.5 rounded-full shadow-lg flex-shrink-0">
              -{discountPercentage}% OFF
            </div>
          )}
        </div>
        
        {displayShortDescription && (
          <p className="text-lg text-slate-600 dark:text-slate-300 leading-relaxed">
            {displayShortDescription}
          </p>
        )}
      </div>

      {/* Enhanced Pricing */}
      <div className="space-y-4">
        <div className="flex items-baseline gap-4 flex-wrap">
          {product.price != null && product.currency ? (
            <>
              <span className="text-2xl font-bold text-slate-900 dark:text-white">
                {product.price} {product.currency}
              </span>
              {hasDiscount && (
                <span className="text-xl font-medium text-slate-500 line-through">
                  {product.compareAtPrice} {product.currency}
                </span>
              )}
            </>
          ) : (
            <span className="text-xl text-slate-600 dark:text-slate-400 font-medium">
              Contact for price
            </span>
          )}
        </div>
        
        {hasDiscount && (
          <div className="inline-flex items-center gap-2 bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300 px-4 py-2 rounded-full text-sm font-medium">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
            </svg>
            {t('youSave', { 
              amount: (product.compareAtPrice! - product.price!).toFixed(2), 
              currency: product.currency 
            })}
          </div>
        )}
      </div>



      {/* Enhanced Product Description */}
      {displayDescription && (
        <div className="bg-white dark:bg-slate-800/50 rounded-2xl p-6 border border-slate-200/60 dark:border-slate-700/60">
          <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
            <svg className="w-6 h-6 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            {t('productDetails')}
          </h3>
          <div className="prose prose-slate dark:prose-invert max-w-none">
            <p className="text-slate-600 dark:text-slate-300 leading-relaxed">
              {displayDescription}
            </p>
          </div>
        </div>
      )}



      {/* Trust Badges */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="text-center p-4 bg-white dark:bg-slate-800/50 rounded-xl border border-slate-200/60 dark:border-slate-700/60">
          <svg className="w-8 h-8 text-green-600 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
          </svg>
          <p className="text-xs font-semibold text-slate-700 dark:text-slate-300">{t('trustBadges.securePayment')}</p>
        </div>
        
        <div className="text-center p-4 bg-white dark:bg-slate-800/50 rounded-xl border border-slate-200/60 dark:border-slate-700/60">
          <svg className="w-8 h-8 text-blue-600 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
          </svg>
          <p className="text-xs font-semibold text-slate-700 dark:text-slate-300">{t('trustBadges.fastShipping')}</p>
        </div>
        
        <div className="text-center p-4 bg-white dark:bg-slate-800/50 rounded-xl border border-slate-200/60 dark:border-slate-700/60">
          <svg className="w-8 h-8 text-purple-600 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
          </svg>
          <p className="text-xs font-semibold text-slate-700 dark:text-slate-300">{t('trustBadges.qualityGuarantee')}</p>
        </div>
        
        <div className="text-center p-4 bg-white dark:bg-slate-800/50 rounded-xl border border-slate-200/60 dark:border-slate-700/60">
          <svg className="w-8 h-8 text-orange-600 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192L5.636 18.364M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z" />
          </svg>
          <p className="text-xs font-semibold text-slate-700 dark:text-slate-300">{t('trustBadges.support247')}</p>
        </div>
      </div>
    </div>
  );
}