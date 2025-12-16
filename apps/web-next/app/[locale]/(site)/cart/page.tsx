'use client';

import {useEffect, useState} from 'react';
import {api} from '@/lib/api';
import type {CartResponse} from '@/lib/types';
import {Link} from '@/navigation';
import {useParams, useRouter} from 'next/navigation';

export default function CartPage() {
  const {locale} = useParams<{locale: string}>();
  const router = useRouter();
  const [cart, setCart] = useState<CartResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [updatingItemId, setUpdatingItemId] = useState<number | null>(null);
  const [removingItemId, setRemovingItemId] = useState<number | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        const data = await api.getCart();
        setCart(data);
        if ((data as any)?.session_id) {
          setSessionId((data as any).session_id);
        }
      } catch (err: any) {
        console.error('Failed to load cart', err);
        setError(err?.message || 'Failed to load cart');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const updateQuantity = async (itemId: number, quantity: number) => {
    if (quantity < 1) return;
    setUpdatingItemId(itemId);
    setError(null);
    try {
      const updated = await api.updateCartItem(itemId, quantity);
      setCart(updated);
      if ((updated as any)?.session_id) {
        setSessionId((updated as any).session_id);
      }
    } catch (err: any) {
      console.error('Failed to update quantity', err);
      setError(err?.message || 'Failed to update quantity');
    } finally {
      setUpdatingItemId(null);
    }
  };

  const removeItem = async (itemId: number) => {
    setRemovingItemId(itemId);
    setError(null);
    try {
      const updated = await api.removeCartItem(itemId);
      setCart(updated);
      if ((updated as any)?.session_id) {
        setSessionId((updated as any).session_id);
      }
    } catch (err: any) {
      console.error('Failed to remove item', err);
      setError(err?.message || 'Failed to remove item');
    } finally {
      setRemovingItemId(null);
    }
  };

  const formatMoney = (amount?: CartResponse['total_amount']) => {
    if (!amount) return '—';
    return amount.formatted || (amount.amount != null && amount.currency ? `${amount.amount} ${amount.currency}` : '—');
  };

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 md:py-16">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-100">Your Cart</h1>
        {sessionId && (
          <span className="text-xs text-slate-500 dark:text-slate-400">Session: {sessionId}</span>
        )}
        <Link
          href="/"
          className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:border-blue-400 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-400 dark:hover:text-blue-200"
        >
          Continue shopping
        </Link>
      </div>

      {loading && (
        <div className="rounded-xl border border-slate-200 bg-white p-6 text-slate-600 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300">
          Loading cart...
        </div>
      )}

      {error && !loading && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-6 text-rose-700 dark:border-rose-900 dark:bg-rose-950 dark:text-rose-100">
          {error}
        </div>
      )}

      {!loading && !error && (!cart || !cart.items || cart.items.length === 0) && (
        <div className="rounded-xl border border-slate-200 bg-white p-6 text-slate-600 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300">
          Your cart is empty.
        </div>
      )}

      {!loading && !error && cart && cart.items && cart.items.length > 0 && (
        <div className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
          <div className="divide-y divide-slate-200 dark:divide-slate-800">
            {cart.items.map((item) => (
              <div key={item.id} className="flex flex-col gap-4 py-4 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex items-center gap-4">
                  <div className="h-16 w-16 overflow-hidden rounded-lg border border-slate-200 bg-slate-100 dark:border-slate-800 dark:bg-slate-800">
                    {item.image_url ? (
                      <img src={item.image_url} alt={item.product_name || 'Product image'} className="h-full w-full object-cover" />
                    ) : (
                      <div className="flex h-full w-full items-center justify-center text-xs text-slate-400">No image</div>
                    )}
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                      {item.product_name || item.product_slug || `Product #${item.product_id}`}
                    </p>
                    <p className="text-xs text-slate-500 dark:text-slate-400">
                      SKU: {item.sku || '—'}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <div className="flex items-center rounded-lg border border-slate-200 dark:border-slate-700">
                    <button
                      className="px-3 py-2 text-sm font-semibold text-slate-700 dark:text-slate-200"
                      onClick={() => updateQuantity(item.id, item.quantity - 1)}
                      disabled={item.quantity <= 1 || updatingItemId === item.id || removingItemId === item.id}
                    >
                      −
                    </button>
                    <input
                      type="number"
                      min={1}
                      value={item.quantity}
                      onChange={(e) => {
                        const next = parseInt(e.target.value, 10);
                        if (!Number.isNaN(next) && next > 0) {
                          updateQuantity(item.id, next);
                        }
                      }}
                      className="w-16 border-x border-slate-200 bg-white text-center text-sm text-slate-900 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
                      disabled={updatingItemId === item.id || removingItemId === item.id}
                    />
                    <button
                      className="px-3 py-2 text-sm font-semibold text-slate-700 dark:text-slate-200"
                      onClick={() => updateQuantity(item.id, item.quantity + 1)}
                      disabled={updatingItemId === item.id || removingItemId === item.id}
                    >
                      +
                    </button>
                  </div>
                  <button
                    onClick={() => removeItem(item.id)}
                    disabled={removingItemId === item.id || updatingItemId === item.id}
                    className="text-sm font-semibold text-rose-600 transition hover:text-rose-700 disabled:opacity-60"
                  >
                    {removingItemId === item.id ? 'Removing…' : 'Remove'}
                  </button>
                </div>
              </div>
            ))}
          </div>
          <div className="flex items-center justify-between border-t border-slate-200 pt-4 text-sm font-semibold text-slate-900 dark:border-slate-800 dark:text-slate-100">
            <span>Total</span>
            <span>{formatMoney(cart.total_amount)}</span>
          </div>
          <div className="flex justify-end">
            <button
              onClick={() => router.push(`/${locale || 'en'}/cart/checkout`)}
              className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-blue-600 px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:opacity-60"
              disabled={!cart.items?.length}
            >
              Proceed to checkout
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
