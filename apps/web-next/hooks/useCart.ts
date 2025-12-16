'use client';

import useSWR from 'swr';
import { api } from '@/lib/api';
import type { CartResponse } from '@/lib/types';

type UseCartReturn = {
  cart: CartResponse | null;
  totalQuantity: number;
  isLoading: boolean;
  error: Error | null;
  refetch: () => void;
};

/**
 * Lightweight cart hook for showing cart badge / count.
 * Accepts a boolean flag to disable fetching when ecommerce is off.
 */
export function useCart(enabled = true): UseCartReturn {
  const { data, error, isLoading, mutate } = useSWR<CartResponse>(
    enabled ? 'cart' : null,
    api.getCart,
    {
      revalidateOnFocus: true,
      dedupingInterval: 30_000,
    }
  );

  const totalQuantity =
    data?.totalQuantity ??
    (data as any)?.total_quantity ??
    data?.item_count ??
    (data as any)?.itemCount ??
    (data as any)?.total_items ??
    (data as any)?.totalItems ??
    data?.total_quantity ??
    data?.items?.reduce((sum, item) => sum + (item.quantity ?? 0), 0) ??
    0;

  return {
    cart: data ?? null,
    totalQuantity,
    isLoading: !!enabled && isLoading,
    error: error ?? null,
    refetch: mutate,
  };
}
