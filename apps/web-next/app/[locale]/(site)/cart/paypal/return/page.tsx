'use client';

import {useEffect, useState} from 'react';
import {useRouter, useSearchParams} from 'next/navigation';
import {api} from '@/lib/api';
import {Link} from '@/navigation';

export default function PayPalReturnPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const token = searchParams?.get('token');
  const cancelled = searchParams?.get('cancelled');
  const orderId = searchParams?.get('orderId');

  const [status, setStatus] = useState<'processing' | 'success' | 'error'>(cancelled ? 'error' : 'processing');
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    const capture = async () => {
      if (!token || cancelled) {
        setStatus('error');
        setMessage(cancelled ? 'Payment was cancelled.' : 'Missing PayPal token.');
        return;
      }
      try {
        const res = await api.captureCartPayPalPayment(token);
        if (res?.success) {
          setStatus('success');
          setMessage('Payment completed successfully.');
        } else {
          setStatus('error');
          setMessage(res?.message || 'Failed to capture payment.');
        }
      } catch (err: any) {
        console.error('Failed to capture PayPal payment', err);
        setStatus('error');
        setMessage(err?.message || 'Failed to capture payment.');
      }
    };
    capture();
  }, [token, cancelled]);

  return (
    <div className="mx-auto max-w-2xl px-4 py-12">
      <div className="rounded-xl border border-slate-200 bg-white p-6 text-slate-800 shadow-sm dark:border-slate-800 dark:bg-slate-900 dark:text-slate-100">
        {status === 'processing' && (
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 animate-spin rounded-full border-4 border-blue-500 border-t-transparent"></div>
            <div>
              <p className="text-lg font-semibold">Finishing your payment…</p>
              <p className="text-sm text-slate-500">Please wait while we confirm with PayPal.</p>
            </div>
          </div>
        )}

        {status === 'success' && (
          <div className="space-y-3">
            <p className="text-lg font-semibold text-emerald-600">Payment successful</p>
            {message && <p className="text-sm text-slate-600 dark:text-slate-300">{message}</p>}
            {orderId && (
              <p className="text-sm text-slate-600 dark:text-slate-300">Order ID: {orderId}</p>
            )}
            <div className="flex gap-3">
              <Link
                href="/cart"
                className="inline-flex items-center justify-center rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700"
              >
                Back to cart
              </Link>
              <Link
                href="/"
                className="inline-flex items-center justify-center rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:border-blue-400 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-400 dark:hover:text-blue-200"
              >
                Continue shopping
              </Link>
            </div>
          </div>
        )}

        {status === 'error' && (
          <div className="space-y-3">
            <p className="text-lg font-semibold text-rose-600">Payment failed</p>
            {message && <p className="text-sm text-slate-600 dark:text-slate-300">{message}</p>}
            <div className="flex gap-3">
              <button
                onClick={() => router.replace('/cart')}
                className="inline-flex items-center justify-center rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700"
              >
                Return to cart
              </button>
              <Link
                href="/"
                className="inline-flex items-center justify-center rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:border-blue-400 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-400 dark:hover:text-blue-200"
              >
                Continue shopping
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
