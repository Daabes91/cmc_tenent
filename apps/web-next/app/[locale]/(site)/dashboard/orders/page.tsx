'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';
import type { OrderResponse } from '@/lib/types';

const PAGE_SIZE = 10;

export default function OrdersPage() {
  const router = useRouter();
  const { isAuthenticated, loading, user } = useAuth();
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, loading, router]);

  const fetchOrders = async (pageToLoad: number) => {
    if (!user?.email) return;
    try {
      setIsLoading(true);
      setError(null);
      const res = await api.getPatientOrders(user.email, { page: pageToLoad, size: PAGE_SIZE });
      const list = (res as any)?.orders ?? [];
      const pagination = (res as any)?.pagination ?? {};
      setOrders(list);
      setPage(pagination.page ?? pageToLoad);
      setTotalPages(pagination.totalPages ?? 1);
      setTotalElements(pagination.totalElements ?? list.length);
    } catch (err: any) {
      console.error('Failed to load orders', err);
      setError(err?.message || 'Unable to load your orders right now.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (!isAuthenticated || !user?.email) return;
    fetchOrders(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated, user?.email]);

  const formatCurrency = (amount?: number | null, currency?: string | null) => {
    if (amount === undefined || amount === null) return '—';
    try {
      return new Intl.NumberFormat(undefined, {
        style: 'currency',
        currency: currency || 'USD',
        minimumFractionDigits: 2,
      }).format(amount);
    } catch {
      return `${currency ?? 'USD'} ${amount.toFixed(2)}`;
    }
  };

  const formatDateValue = (value?: string | null) => {
    if (!value) return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '—';
    return date.toLocaleDateString(undefined, {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  const statusClass = (status?: string) => {
    const normalized = (status || '').toUpperCase();
    if (['COMPLETED', 'PAID', 'CAPTURED', 'DELIVERED'].includes(normalized)) {
      return 'bg-green-100 text-green-700';
    }
    if (['PENDING_PAYMENT', 'CREATED', 'APPROVED', 'PROCESSING', 'SHIPPED'].includes(normalized)) {
      return 'bg-amber-100 text-amber-700';
    }
    if (['FAILED', 'CANCELLED'].includes(normalized)) {
      return 'bg-red-100 text-red-700';
    }
    return 'bg-slate-100 text-slate-700';
  };

  const hasNext = useMemo(() => page < (totalPages ?? 1) - 1, [page, totalPages]);
  const hasPrev = useMemo(() => page > 0, [page]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50 dark:from-slate-900 dark:via-slate-900 dark:to-slate-900">
      <div className="max-w-5xl mx-auto px-4 py-8">
        <div className="mb-6">
          <Link href="/dashboard" className="text-sm font-semibold text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300">
            ← Back to dashboard
          </Link>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white mt-2">Your Orders</h1>
          <p className="text-slate-600 dark:text-slate-300">Track your recent purchases and payments.</p>
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
          <div className="px-6 py-4 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between">
            <div>
              <p className="text-sm font-semibold text-slate-900 dark:text-slate-100">Orders</p>
              <p className="text-xs text-slate-500 dark:text-slate-400">{totalElements} total</p>
            </div>
          </div>

          <div className="p-6">
            {isLoading ? (
              <div className="text-center py-10">
                <div className="inline-block animate-spin rounded-full h-10 w-10 border-4 border-blue-600 border-t-transparent"></div>
              </div>
            ) : error ? (
              <div className="text-center py-8">
                <p className="text-red-600 dark:text-red-400 font-medium">{error}</p>
              </div>
            ) : orders.length === 0 ? (
              <div className="text-center py-10">
                <div className="w-14 h-14 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center mx-auto mb-3">
                  <svg className="w-7 h-7 text-slate-400 dark:text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h18v4H3zm0 4h18v14H3z" />
                  </svg>
                </div>
                <p className="text-slate-600 dark:text-slate-300 font-medium">No orders yet.</p>
              </div>
            ) : (
              <div className="space-y-3">
                {orders.map((order) => (
                  <div
                    key={order.id}
                    className="p-4 rounded-lg border border-slate-200 dark:border-slate-700 hover:border-blue-300 dark:hover:border-blue-600 hover:shadow-sm transition-all flex items-start justify-between gap-4"
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <h3 className="font-semibold text-slate-900 dark:text-slate-100">Order {order.orderNumber}</h3>
                        <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${statusClass(order.status)}`}>
                          {order.statusDisplayName || order.status || 'Unknown'}
                        </span>
                      </div>
                      <p className="text-sm text-slate-600 dark:text-slate-300">Placed on {formatDateValue(order.createdAt)}</p>
                      {order.totalItemCount ? (
                        <p className="text-xs text-slate-500 dark:text-slate-400">{order.totalItemCount} item(s)</p>
                      ) : null}
                    </div>
                    <div className="text-right">
                      <p className="text-sm text-slate-500 dark:text-slate-400">Total</p>
                      <p className="text-lg font-bold text-slate-900 dark:text-white">
                        {formatCurrency(order.totalAmount ?? undefined, order.currency ?? undefined)}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {orders.length > 0 && (
            <div className="flex items-center justify-between px-6 py-4 border-t border-slate-200 dark:border-slate-700">
              <button
                onClick={() => fetchOrders(page - 1)}
                disabled={!hasPrev || isLoading}
                className="px-3 py-1.5 text-sm font-medium text-slate-700 dark:text-slate-300 rounded-lg border border-slate-300 dark:border-slate-600 hover:bg-slate-50 dark:hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <span className="text-sm text-slate-600 dark:text-slate-300">
                Page {page + 1} of {totalPages || 1}
              </span>
              <button
                onClick={() => fetchOrders(page + 1)}
                disabled={!hasNext || isLoading}
                className="px-3 py-1.5 text-sm font-medium text-slate-700 dark:text-slate-300 rounded-lg border border-slate-300 dark:border-slate-600 hover:bg-slate-50 dark:hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
