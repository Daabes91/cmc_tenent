/**
 * Composable for managing tenant billing status.
 * Provides reactive billing status state with caching and refresh capabilities.
 */

export type BillingStatus = 'pending_payment' | 'active' | 'past_due' | 'canceled';

export interface BillingStatusResponse {
  status: BillingStatus;
  isActive: boolean;
}

const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds

export function useBillingStatus() {
  const apiBase = useApiBase();
  const auth = useAuth();
  const { tenantSlug } = useTenantSlug();

  // Shared state across all instances
  const billingStatus = useState<BillingStatus | null>('billing.status', () => null);
  const isActive = useState<boolean>('billing.isActive', () => false);
  const lastFetchTime = useState<number | null>('billing.lastFetchTime', () => null);
  const isLoading = useState<boolean>('billing.isLoading', () => false);
  const error = useState<Error | null>('billing.error', () => null);

  /**
   * Check if cached data is still valid
   */
  const isCacheValid = computed(() => {
    if (!lastFetchTime.value) return false;
    return Date.now() - lastFetchTime.value < CACHE_DURATION;
  });

  /**
   * Fetch billing status from API
   */
  async function fetchBillingStatus(force = false): Promise<void> {
    // Return cached data if valid and not forcing refresh
    if (!force && isCacheValid.value && billingStatus.value) {
      return;
    }

    // Don't fetch if not authenticated
    if (!auth.isAuthenticated.value) {
      return;
    }

    // Prevent concurrent fetches
    if (isLoading.value) {
      return;
    }

    isLoading.value = true;
    error.value = null;

    try {
      const response = await $fetch<BillingStatusResponse>('/billing/status', {
        baseURL: apiBase,
        headers: {
          ...auth.authorizationHeader(),
          ...(tenantSlug.value ? { 'X-Tenant-Slug': tenantSlug.value } : {})
        }
      });

      billingStatus.value = response.status;
      isActive.value = response.isActive;
      lastFetchTime.value = Date.now();
    } catch (err: any) {
      console.error('[BillingStatus] Failed to fetch billing status:', err);
      error.value = err instanceof Error ? err : new Error('Failed to fetch billing status');
      
      // Set default values on error
      billingStatus.value = 'pending_payment';
      isActive.value = false;
    } finally {
      isLoading.value = false;
    }
  }

  /**
   * Refresh billing status (force fetch from API)
   */
  async function refreshBillingStatus(): Promise<void> {
    return fetchBillingStatus(true);
  }

  /**
   * Clear cached billing status
   */
  function clearBillingStatus(): void {
    billingStatus.value = null;
    isActive.value = false;
    lastFetchTime.value = null;
    error.value = null;
  }

  /**
   * Get display message for current billing status
   */
  const statusMessage = computed(() => {
    switch (billingStatus.value) {
      case 'pending_payment':
        return 'Payment required to activate your account';
      case 'active':
        return 'Your subscription is active';
      case 'past_due':
        return 'Your payment is past due. Please update your payment method';
      case 'canceled':
        return 'Your subscription has been canceled';
      default:
        return '';
    }
  });

  /**
   * Get status color for UI display
   */
  const statusColor = computed(() => {
    switch (billingStatus.value) {
      case 'active':
        return 'green';
      case 'pending_payment':
        return 'yellow';
      case 'past_due':
        return 'orange';
      case 'canceled':
        return 'red';
      default:
        return 'gray';
    }
  });

  /**
   * Check if user should be blocked from accessing the admin panel
   */
  const shouldBlockAccess = computed(() => {
    return billingStatus.value !== 'active';
  });

  return {
    // State
    billingStatus: readonly(billingStatus),
    isActive: readonly(isActive),
    isLoading: readonly(isLoading),
    error: readonly(error),
    isCacheValid,
    
    // Computed
    statusMessage,
    statusColor,
    shouldBlockAccess,
    
    // Methods
    fetchBillingStatus,
    refreshBillingStatus,
    clearBillingStatus
  };
}
