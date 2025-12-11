 'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useEffect, useState } from 'react';
import type { PublicCarousel, PublicCarouselItem } from '@/lib/types';
import { api } from '@/lib/api';

type CarouselRailProps = {
  title: string;
  carousels: PublicCarousel[];
};

function ProductCard({ item }: { item: PublicCarouselItem }) {
  const product = item.product;
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);

  if (!product) return null;
  const image =
    product.mainImageUrl ||
    (product.images && product.images.length > 0 ? product.images[0] : undefined);

  const addToCart = async () => {
    try {
      setAdding(true);
      await api.addToCart({ product_id: product.id, quantity: 1 });
      setAdded(true);
      setTimeout(() => setAdded(false), 2000);
    } catch (err) {
      console.error('Add to cart failed', err);
      setAdded(false);
      // no toast system here; rely on console for now
    } finally {
      setAdding(false);
    }
  };

  return (
    <div className="group relative overflow-hidden rounded-2xl border border-slate-100/60 bg-white/90 p-4 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-lg dark:border-slate-800/60 dark:bg-slate-900/70">
      {image ? (
        <div className="relative mb-3 h-40 w-full overflow-hidden rounded-xl bg-slate-50 dark:bg-slate-800">
          <Image
            src={image}
            alt={product.name}
            fill
            className="object-cover transition-transform duration-300 group-hover:scale-105"
            sizes="(max-width: 768px) 80vw, 240px"
          />
        </div>
      ) : (
        <div className="mb-3 flex h-40 w-full items-center justify-center rounded-xl bg-slate-50 text-slate-400 dark:bg-slate-800">
          <span className="text-sm">No image</span>
        </div>
      )}
      <div className="space-y-1">
        <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100 line-clamp-2">
          {product.name}
        </h3>
        {product.shortDescription && (
          <p className="text-sm text-slate-600 dark:text-slate-300 line-clamp-2">
            {product.shortDescription}
          </p>
        )}
        <div className="flex items-center gap-2 text-sm font-semibold text-blue-600 dark:text-blue-300">
          {product.price != null && product.currency ? (
            <>
              <span>
                {product.price} {product.currency}
              </span>
              {product.compareAtPrice && product.compareAtPrice > product.price && (
                <span className="text-xs font-medium text-slate-500 line-through">
                  {product.compareAtPrice} {product.currency}
                </span>
              )}
            </>
          ) : (
            <span className="text-slate-500 dark:text-slate-400">Contact for price</span>
          )}
        </div>
      </div>
      <div className="mt-3 flex items-center gap-2">
        <Link
          href={`/products/${product.slug}`}
          className="inline-flex items-center gap-2 text-sm font-semibold text-blue-600 transition-colors hover:text-blue-700 dark:text-blue-300 dark:hover:text-blue-200"
        >
          View product
        </Link>
        <button
          onClick={addToCart}
          disabled={adding}
          className="inline-flex items-center gap-2 rounded-md bg-emerald-600 px-3 py-1.5 text-sm font-semibold text-white transition hover:bg-emerald-700 disabled:opacity-60"
        >
          {adding ? 'Adding…' : added ? 'Added' : 'Add to cart'}
        </button>
      </div>
    </div>
  );
}

function CarouselItems({ carousel }: { carousel: PublicCarousel }) {
  const [fallbackItems, setFallbackItems] = useState<PublicCarouselItem[]>([]);

  useEffect(() => {
    const loadFallback = async () => {
      if (carousel.type !== 'VIEW_ALL_PRODUCTS' || (carousel.items?.length ?? 0) > 0) return;
      try {
        const res = await api.getProducts({ size: 12, status: 'ACTIVE' });
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
  }, [carousel]);

  const items = carousel.items?.length ? carousel.items : fallbackItems;
  if (!items?.length) return null;

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {items.map((item) => (
        <ProductCard key={item.id} item={item} />
      ))}
    </div>
  );
}

export function CarouselRail({ title, carousels }: CarouselRailProps) {
  if (!carousels.length) return null;
  return (
    <section className="relative overflow-hidden bg-white dark:bg-slate-950 py-12 transition-colors">
      <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
        <div className="mb-6 flex items-center justify-between">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">{title}</h2>
        </div>
        <div className="space-y-10">
          {carousels.map((carousel) => (
            <div key={carousel.id} className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-100">
                    {carousel.name}
                  </h3>
                  {carousel.slug && (
                    <p className="text-sm text-slate-500 dark:text-slate-400">
                      {carousel.slug.replace(/[-_]/g, ' ')}
                    </p>
                  )}
                </div>
              </div>
              <CarouselItems carousel={carousel} />
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
