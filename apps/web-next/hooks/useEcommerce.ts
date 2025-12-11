'use client';

import useSWR from 'swr';
import { api } from '@/lib/api';
import type { ClinicSettings, PublicCarousel } from '@/lib/types';
import { useClinicSettings } from './useClinicSettings';

type UseEcommerceFeature = {
  enabled: boolean;
  settings: ClinicSettings | null;
  isLoading: boolean;
  error: Error | null;
};

export function useEcommerceFeature(): UseEcommerceFeature {
  const { clinicSettings, isLoading, error } = useClinicSettings();
  // Default to enabled unless explicitly disabled in settings; treat loading as enabled for UX.
  const enabled = clinicSettings?.ecommerceEnabled !== false;
  return { enabled, settings: clinicSettings, isLoading, error };
}

export function usePublicCarousels(enabled: boolean) {
  const { data, error, isLoading, mutate } = useSWR<PublicCarousel[]>(
    enabled ? 'public-carousels' : null,
    api.getCarousels,
    {
      revalidateOnFocus: false,
      dedupingInterval: 2 * 60 * 1000, // 2 minutes
    }
  );

  const filtered = (data ?? []).filter((carousel) => {
    const items = carousel.items ?? [];
    if (!items.length) return false;
    // ensure at least one product-backed item or content entry
    return items.some((item) => Boolean(item.product) || Boolean(item.imageUrl) || Boolean(item.linkUrl));
  });

  return {
    carousels: filtered,
    isLoading: !!enabled && isLoading,
    error,
    refetch: mutate,
  };
}
