'use client';

import { useState } from 'react';
import { useTranslations } from 'next-intl';
import { api } from '@/lib/api';
import type { Product } from '@/lib/types';

interface ProductActionsProps {
  product: Product;
  onAddToCart?: () => void;
}

export function ProductActions({ product, onAddToCart }: ProductActionsProps) {
  const t = useTranslations('ecommerce.productDetails');
  const tEcommerce = useTranslations('ecommerce');
  const [quantity, setQuantity] = useState(1);
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);
  const addToCart = async () => {
    if (!product?.id) return;
    setAdding(true);
    try {
      await api.addToCart({ product_id: product.id, quantity: quantity || 1 });
      setAdded(true);
      onAddToCart?.();
      setTimeout(() => setAdded(false), 3000);
    } catch (err) {
      console.error('Add to cart failed', err);
      setAdded(false);
    } finally {
      setAdding(false);
    }
  };

  const shareProduct = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: product.name,
          text: product.shortDescription || product.name,
          url: window.location.href,
        });
      } catch (err) {
        console.log('Error sharing:', err);
      }
    } else {
      // Fallback: copy to clipboard
      navigator.clipboard.writeText(window.location.href);
      // TODO: Show toast notification
    }
  };

  return (
    <div className="space-y-6">
      {/* Quantity Selector */}
      <div className="space-y-3">
        <label className="block text-sm font-semibold text-slate-700 dark:text-slate-300">
          {t('quantity')}
        </label>
        <div className="flex items-center gap-1 w-fit">
          <button
            type="button"
            className="w-12 h-12 rounded-xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center font-semibold text-lg"
            onClick={() => setQuantity((q) => Math.max(1, q - 1))}
            disabled={quantity <= 1}
          >
            −
          </button>
          <input
            type="number"
            min={1}
            max={99}
            value={quantity}
            onChange={(e) => setQuantity(Math.max(1, Math.min(99, Number(e.target.value) || 1)))}
            className="w-20 h-12 border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-center font-semibold rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
          />
          <button
            type="button"
            className="w-12 h-12 rounded-xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors flex items-center justify-center font-semibold text-lg"
            onClick={() => setQuantity((q) => Math.min(99, q + 1))}
          >
            +
          </button>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="space-y-4">
        {/* Primary Actions */}
        <div className="flex flex-col sm:flex-row gap-3">
          <button
            onClick={addToCart}
            disabled={adding}
            className={`flex-1 inline-flex items-center justify-center gap-3 px-8 py-4 rounded-2xl font-bold text-lg transition-all duration-200 shadow-lg ${
              added
                ? 'bg-emerald-600 hover:bg-emerald-700 text-white shadow-emerald-500/25'
                : 'bg-blue-600 hover:bg-blue-700 text-white shadow-blue-600/25 hover:shadow-blue-600/40'
            } disabled:opacity-60 disabled:cursor-not-allowed hover:scale-105 active:scale-95`}
          >
            {adding ? (
              <>
                <svg className="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                {tEcommerce('adding')}
              </>
            ) : added ? (
              <>
                <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                {tEcommerce('addedToCart')}
              </>
            ) : (
              <>
                <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-2.5 5M7 13l2.5 5m0 0h8" />
                </svg>
                {tEcommerce('addToCart')}
              </>
            )}
          </button>

          {/* Buy Now Button */}
          <button
            onClick={async () => {
              if (!product?.id) return;
              
              try {
                // Get current cart to remove existing items
                const currentCart = await api.getCart();
                
                // Remove all existing items from cart
                if (currentCart?.items?.length > 0) {
                  for (const item of currentCart.items) {
                    await api.removeCartItem(item.id);
                  }
                }
                
                // Add this product to cart
                await api.addToCart({ product_id: product.id, quantity: quantity || 1 });
                
                // Redirect to cart page (which has checkout button)
                window.location.href = '/cart';
              } catch (err) {
                console.error('Buy now failed', err);
                // Fallback: just add to cart and go to cart page
                try {
                  await api.addToCart({ product_id: product.id, quantity: quantity || 1 });
                  window.location.href = '/cart';
                } catch (fallbackErr) {
                  console.error('Fallback also failed', fallbackErr);
                }
              }
            }}
            className="sm:w-auto px-6 py-4 rounded-2xl bg-slate-900 dark:bg-white text-white dark:text-slate-900 hover:bg-slate-800 dark:hover:bg-slate-100 transition-all duration-200 font-bold flex items-center justify-center gap-2 shadow-lg hover:scale-105 active:scale-95"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
            {t('buyNow')}
          </button>
        </div>

        {/* Secondary Actions */}
        <div className="flex gap-3">
          <button
            onClick={shareProduct}
            className="flex-1 px-4 py-3 rounded-xl border-2 border-slate-300 dark:border-slate-600 text-slate-700 dark:text-slate-300 hover:border-slate-400 dark:hover:border-slate-500 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all duration-200 font-semibold flex items-center justify-center gap-2"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.367 2.684 3 3 0 00-5.367-2.684z" />
            </svg>
            {t('share')}
          </button>
        </div>
      </div>


    </div>
  );
}