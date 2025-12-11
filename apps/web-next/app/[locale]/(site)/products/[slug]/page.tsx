'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import type { Product, ProductImage } from '@/lib/types';

export default function ProductDetailPage() {
  const { slug } = useParams<{ slug: string }>();
  const router = useRouter();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);

  useEffect(() => {
    const load = async () => {
      if (!slug) return;
      setLoading(true);
      setError(null);
      try {
        const data = await api.getProductBySlug(Array.isArray(slug) ? slug[0] : slug);
        setProduct(data);
      } catch (err: any) {
        console.error('Failed to load product', err);
        setError(err?.data?.message || 'Product not found');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [slug]);

  const addToCart = async () => {
    if (!product?.id) return;
    setAdding(true);
    try {
      await api.addToCart({ product_id: product.id, quantity: 1 });
      setAdded(true);
      setTimeout(() => setAdded(false), 2000);
    } catch (err) {
      console.error('Add to cart failed', err);
      setAdded(false);
    } finally {
      setAdding(false);
    }
  };

  const mainImage: string | null =
    product?.mainImageUrl ||
    ((product?.images as ProductImage[] | undefined)?.find((img) => img.isMain)?.imageUrl ??
      (product?.images && product.images[0]?.imageUrl) ??
      null);

  return (
    <div className="max-w-6xl mx-auto px-4 py-10">
      <div className="mb-4 flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
        <Link href="/products" className="hover:text-blue-600 dark:hover:text-blue-300">
          Products
        </Link>
        <span>/</span>
        <span className="text-slate-800 dark:text-slate-100">
          {product?.name || (Array.isArray(slug) ? slug[0] : slug)}
        </span>
      </div>

      {loading ? (
        <div className="space-y-4">
          <div className="h-8 w-48 rounded bg-slate-200 dark:bg-slate-800 animate-pulse" />
          <div className="grid gap-6 md:grid-cols-2">
            <div className="h-80 rounded-xl bg-slate-200 dark:bg-slate-800 animate-pulse" />
            <div className="space-y-3">
              <div className="h-6 w-32 rounded bg-slate-200 dark:bg-slate-800 animate-pulse" />
              <div className="h-4 w-full rounded bg-slate-200 dark:bg-slate-800 animate-pulse" />
              <div className="h-4 w-3/4 rounded bg-slate-200 dark:bg-slate-800 animate-pulse" />
            </div>
          </div>
        </div>
      ) : error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-red-700 dark:border-red-800 dark:bg-red-900/30 dark:text-red-200">
          {error}
        </div>
      ) : product ? (
        <div className="grid gap-8 md:grid-cols-2">
          <div className="relative overflow-hidden rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900">
            {mainImage ? (
              <Image
                src={mainImage}
                alt={product.name}
                width={800}
                height={800}
                className="h-full w-full object-cover"
              />
            ) : (
              <div className="flex h-80 items-center justify-center text-slate-400">No image</div>
            )}
          </div>
          <div className="space-y-4">
            <h1 className="text-3xl font-bold text-slate-900 dark:text-white">{product.name}</h1>
            {product.shortDescription && (
              <p className="text-base text-slate-600 dark:text-slate-300">{product.shortDescription}</p>
            )}
            <div className="flex items-center gap-3 text-2xl font-semibold text-blue-700 dark:text-blue-300">
              {product.price != null && product.currency ? (
                <>
                  <span>
                    {product.price} {product.currency}
                  </span>
                  {product.compareAtPrice && product.compareAtPrice > product.price && (
                    <span className="text-sm font-medium text-slate-500 line-through">
                      {product.compareAtPrice} {product.currency}
                    </span>
                  )}
                </>
              ) : (
                <span className="text-slate-500 dark:text-slate-400 text-base">Contact for price</span>
              )}
            </div>
            <div className="flex gap-3">
              <button
                onClick={addToCart}
                disabled={adding}
                className="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-5 py-3 text-white font-semibold shadow-md transition hover:bg-emerald-700 disabled:opacity-60"
              >
                {adding ? 'Adding…' : added ? 'Added to cart' : 'Add to cart'}
              </button>
              <button
                onClick={() => router.push('/products')}
                className="inline-flex items-center gap-2 rounded-lg border border-slate-300 px-5 py-3 text-slate-700 transition hover:border-slate-400 dark:border-slate-700 dark:text-slate-200 dark:hover:border-slate-500"
              >
                Back to products
              </button>
            </div>
            {product.description && (
              <div className="prose prose-slate dark:prose-invert max-w-none">
                <p>{product.description}</p>
              </div>
            )}
          </div>
        </div>
      ) : null}
    </div>
  );
}
