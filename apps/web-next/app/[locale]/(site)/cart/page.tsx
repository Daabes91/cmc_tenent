'use client';

import {useEffect, useState} from 'react';
import {api} from '@/lib/api';
import type {CartResponse} from '@/lib/types';
import {Link} from '@/navigation';

export default function CartPage() {
  const [cart, setCart] = useState<CartResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);

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
              <div key={item.id} className="flex items-center justify-between py-4">
                <div>
                  <p className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                    Product #{item.productId}
                  </p>
                  <p className="text-xs text-slate-500 dark:text-slate-400">Quantity: {item.quantity}</p>
                </div>
              </div>
            ))}
          </div>
          <div className="flex items-center justify-between border-t border-slate-200 pt-4 text-sm font-semibold text-slate-900 dark:border-slate-800 dark:text-slate-100">
            <span>Total</span>
            <span>{cart.total_amount != null ? cart.total_amount : '—'}</span>
          </div>
          <div className="flex justify-end">
            <button
              disabled
              className="inline-flex items-center gap-2 rounded-lg bg-blue-600 px-5 py-3 text-sm font-semibold text-white shadow-sm opacity-70"
            >
              Checkout (coming soon)
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
