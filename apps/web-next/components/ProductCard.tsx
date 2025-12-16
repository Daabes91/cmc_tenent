'use client';

import Image from 'next/image';
import { useMemo, useState } from 'react';
import { useSWRConfig } from 'swr';
import { useTranslations } from 'next-intl';
import { Link } from '@/navigation';
import type { PublicCarouselItem, ProductImage } from '@/lib/types';
import { api } from '@/lib/api';

type ProductCardProps = {
  item: PublicCarouselItem;
  locale: string;
  variant?: 'default' | 'compact' | 'featured';
};

export function ProductCard({ item, locale, variant = 'default' }: ProductCardProps) {
  const product = item.product;
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);
  const { mutate } = useSWRConfig();
  const t = useTranslations('ecommerce');

  if (!product) return null;

  const image = useMemo(() => {
    // First try to find the main image
    if (product.images && product.images.length > 0) {
      // Handle both array of strings and array of objects
      if (typeof product.images[0] === 'string') {
        // Array of strings (from API response)
        return product.images[0] as string;
      } else {
        // Array of objects (ProductImage type)
        const images = product.images as ProductImage[];
        const mainImage = images.find(img => img.isMain);
        if (mainImage) {
          return mainImage.imageUrl;
        }
        // If no main image, use the first image
        return images[0].imageUrl;
      }
    }
    // Fallback to mainImageUrl if it exists (for backward compatibility)
    return product.mainImageUrl || undefined;
  }, [product.images, product.mainImageUrl]);

  const displayName = locale === 'ar' && product.nameAr ? product.nameAr : product.name;
  const displayShortDescription =
    locale === 'ar' && product.shortDescriptionAr ? product.shortDescriptionAr : product.shortDescription;

  const addToCart = async () => {
    try {
      setAdding(true);
      await api.addToCart({ product_id: product.id, quantity: 1 });
      setAdded(true);
      void mutate('cart');
      setTimeout(() => setAdded(false), 2000);
    } catch (err) {
      console.error('Add to cart failed', err);
      setAdded(false);
    } finally {
      setAdding(false);
    }
  };

  const priceDisplay = (
    <div className="flex items-center gap-2">
      {product.price != null && product.currency ? (
        <div className="flex items-center gap-2">
          <span className="text-sm font-bold text-slate-900 dark:text-white">
            {product.price} {product.currency}
          </span>
          {product.compareAtPrice && product.compareAtPrice > product.price && (
            <span className="text-xs font-medium text-slate-500 line-through">
              {product.compareAtPrice} {product.currency}
            </span>
          )}
        </div>
      ) : (
        <span className="text-slate-500 dark:text-slate-400 font-medium">{t('contactForPrice')}</span>
      )}
    </div>
  );

  const cardVariants = {
    default: 'group relative bg-white dark:bg-slate-900 rounded-3xl border border-slate-200/60 dark:border-slate-700/60 overflow-hidden transition-all duration-300 hover:shadow-xl hover:shadow-slate-900/10 dark:hover:shadow-black/20 hover:-translate-y-2',
    compact: 'group relative bg-white dark:bg-slate-900 rounded-2xl border border-slate-200/60 dark:border-slate-700/60 overflow-hidden transition-all duration-300 hover:shadow-lg hover:-translate-y-1',
    featured: 'group relative bg-gradient-to-br from-white via-slate-50 to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900 rounded-3xl border border-slate-200/60 dark:border-slate-700/60 overflow-hidden transition-all duration-300 hover:shadow-2xl hover:shadow-blue-500/10 dark:hover:shadow-blue-500/5 hover:-translate-y-3'
  };

  const imageHeights = {
    default: 'h-48',
    compact: 'h-40',
    featured: 'h-64'
  };

  return (
    <div className={cardVariants[variant]}>
      <Link 
        href={`/products/${product.slug}`} 
        className="absolute inset-0 z-10" 
        aria-label={displayName}
      />

      {/* Image Container */}
      <div className={`relative ${imageHeights[variant]} w-full overflow-hidden bg-slate-100 dark:bg-slate-800`}>
        {image ? (
          <Image
            src={image}
            alt={displayName}
            fill
            className="object-cover transition-all duration-500 group-hover:scale-110"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center">
            <div className="text-center">
              <div className="mx-auto h-16 w-16 rounded-full bg-slate-200 dark:bg-slate-700 flex items-center justify-center mb-2">
                <svg className="h-8 w-8 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <span className="text-sm text-slate-500 dark:text-slate-400">{t('noImage')}</span>
            </div>
          </div>
        )}
        
        {/* Gradient Overlay */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/20 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
        
        {/* Sale Badge */}
        {product.compareAtPrice && product.price && product.compareAtPrice > product.price && (
          <div className="absolute top-4 left-4 z-20">
            <div className="bg-red-500 text-white text-xs font-bold px-2 py-1 rounded-full">
              {t('sale')}
            </div>
          </div>
        )}
      </div>

      {/* Content */}
      <div className="p-4 space-y-3">
        {/* Title and Description */}
        <div className="space-y-2">
          <h3 className="text-base font-bold text-slate-900 dark:text-white line-clamp-2 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors duration-200">
            {displayName}
          </h3>
          {displayShortDescription && (
            <p className="text-xs text-slate-600 dark:text-slate-300 line-clamp-2 leading-relaxed">
              {displayShortDescription}
            </p>
          )}
        </div>

        {/* Price */}
        <div className="flex items-center justify-between">
          {priceDisplay}
        </div>

        {/* Add to Cart Button */}
        <div className="pt-2">
          <button
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              void addToCart();
            }}
            disabled={adding}
            className={`
              relative z-20 w-full inline-flex items-center justify-center gap-2 
              rounded-2xl px-4 py-2 text-xs font-semibold transition-all duration-200
              ${added
                ? 'bg-emerald-600 hover:bg-emerald-700 text-white'
                : 'bg-blue-600 hover:bg-blue-700 text-white'
              }
              disabled:opacity-60 disabled:cursor-not-allowed
              hover:shadow-lg hover:shadow-blue-600/25
              active:scale-95
            `}
          >
            {adding ? (
              <>
                <svg className="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                {t('adding')}
              </>
            ) : added ? (
              <>
                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                {t('addedToCart')}
              </>
            ) : (
              <>
                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-2.5 5M7 13l2.5 5m0 0h8" />
                </svg>
                {t('addToCart')}
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
}