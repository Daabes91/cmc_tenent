import { billingPlanSeeds } from '@/data/billingPlanSeeds';
import type {
  TenantPlan,
  PlanChangeResponse,
  CancellationResponse,
  PaymentMethodUpdateResponse
} from '@/types/billing';

/**
 * Composable for managing tenant subscription plan operations.
 * Provides methods for viewing, upgrading, cancelling, and updating payment methods.
 */

const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds

export function useBillingPlan() {
  const apiBase = useApiBase();
  const auth = useAuth();
  const { tenantSlug } = useTenantSlug();
  const toast = useEnhancedToast();

  const normalizeDateValue = (value: unknown): string | null => {
    if (!value) return null;

    if (typeof value === 'string') {
      const parsed = new Date(value);
      return Number.isNaN(parsed.getTime()) ? null : parsed.toISOString();
    }

    if (Array.isArray(value) && value.length >= 3) {
      const [year, month, day, hour = 0, minute = 0, second = 0] = value.map(Number);
      const parsed = new Date(Date.UTC(year, (month || 1) - 1, day || 1, hour, minute, second));
      return Number.isNaN(parsed.getTime()) ? null : parsed.toISOString();
    }

    return null;
  };

  const normalizePlanResponse = (plan: TenantPlan): TenantPlan => ({
    ...plan,
    renewalDate: normalizeDateValue((plan as any).renewalDate),
    cancellationDate: normalizeDateValue((plan as any).cancellationDate),
    cancellationEffectiveDate: normalizeDateValue((plan as any).cancellationEffectiveDate),
    pendingPlanEffectiveDate: normalizeDateValue((plan as any).pendingPlanEffectiveDate)
  });

  // Shared state across all instances
  const currentPlan = useState<TenantPlan | null>('billingPlan.current', () => null);
  const lastFetchTime = useState<number | null>('billingPlan.lastFetchTime', () => null);
  const isLoading = useState<boolean>('billingPlan.isLoading', () => false);
  const error = useState<Error | null>('billingPlan.error', () => null);

  const clonePlan = (plan: TenantPlan): TenantPlan => ({
    ...plan,
    features: [...plan.features]
  });

  const getSeedPlan = (): TenantPlan | null => {
    const key = (tenantSlug.value || 'default').toLowerCase();
    const seeded = billingPlanSeeds[key] ?? billingPlanSeeds.default;
    return seeded ? clonePlan(seeded) : null;
  };

  /**
   * Check if cached data is still valid
   */
  const isCacheValid = computed(() => {
    if (!lastFetchTime.value) return false;
    return Date.now() - lastFetchTime.value < CACHE_DURATION;
  });

  /**
   * Fetch current plan details from API
   * Calls /billing/current-plan endpoint
   */
  async function fetchPlanDetails(force = false): Promise<void> {
    // Return cached data if valid and not forcing refresh
    if (!force && isCacheValid.value && currentPlan.value) {
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
      const response = await $fetch<TenantPlan>('/billing/current-plan', {
        baseURL: apiBase,
        headers: {
          ...auth.authorizationHeader(),
          ...(tenantSlug.value ? { 'X-Tenant-Slug': tenantSlug.value } : {})
        }
      });

      currentPlan.value = normalizePlanResponse(response);
      lastFetchTime.value = Date.now();
    } catch (err: any) {
      console.error('[BillingPlan] Failed to fetch plan details:', err);
      error.value = err instanceof Error ? err : new Error('Failed to fetch plan details');
      
      // Extract error details from response
      const errorCode = err?.data?.error || err?.statusCode;
      const errorMessage = err?.data?.message || err?.message;
      
      let userMessage = 'Unable to retrieve your subscription information. Please try again.';
      
      // Provide specific error messages based on error code
      if (errorCode === 404 || errorCode === 'NOT_FOUND') {
        userMessage = 'No subscription found for your account. Please contact support.';
      } else if (errorCode === 403 || errorCode === 'ACCESS_DENIED') {
        userMessage = 'You do not have permission to view subscription details.';
      } else if (errorCode === 503 || errorCode === 'SERVICE_UNAVAILABLE') {
        userMessage = 'Payment service is temporarily unavailable. Please try again in a few moments.';
      }
      
      toast.error({
        title: 'Failed to load plan details',
        description: userMessage,
        error: err
      });

      const seededPlan = getSeedPlan();
      if (seededPlan) {
        console.warn('[BillingPlan] Using seeded plan data due to API failure.');
        currentPlan.value = seededPlan;
        lastFetchTime.value = Date.now();
      }
    } finally {
      isLoading.value = false;
    }
  }

  /**
   * Upgrade subscription plan to a higher tier
   * Redirects to PayPal for payment approval
   * 
   * @param targetTier - The target plan tier (e.g., 'PROFESSIONAL', 'ENTERPRISE')
   * @param billingCycle - Optional billing cycle override
   */
  async function upgradePlan(targetTier: string, billingCycle?: string): Promise<void> {
    if (!currentPlan.value) {
      toast.error({
        title: 'Unable to upgrade',
        description: 'Please load your current plan details first.'
      });
      return;
    }

    isLoading.value = true;
    error.value = null;

    try {
      const response = await $fetch<PlanChangeResponse>(
        `/billing/plan/upgrade`,
        {
          baseURL: apiBase,
          method: 'POST',
          headers: {
            ...auth.authorizationHeader(),
            'Content-Type': 'application/json',
            ...(tenantSlug.value ? { 'X-Tenant-Slug': tenantSlug.value } : {})
          },
          body: {
            targetTier,
            billingCycle
          }
        }
      );

      // Show success message
      toast.success({
        title: 'Upgrade initiated',
        description: 'Redirecting to PayPal to complete your upgrade...'
      });

      // Redirect to PayPal approval URL
      if (response.approvalUrl) {
        window.location.href = response.approvalUrl;
      } else {
        throw new Error('No approval URL received from server');
      }
    } catch (err: any) {
      console.error('[BillingPlan] Failed to upgrade plan:', err);
      error.value = err instanceof Error ? err : new Error('Failed to upgrade plan');
      
      // Extract error details from response
      const errorCode = err?.data?.error || err?.statusCode;
      const errorMessage = err?.data?.message || err?.message;
      
      let userMessage = 'Unable to initiate plan upgrade. Please try again or contact support.';
      let title = 'Upgrade failed';
      
      // Provide specific error messages based on error code
      if (errorCode === 400 || errorCode === 'INVALID_PLAN_TIER') {
        title = 'Invalid plan selection';
        userMessage = 'The selected plan tier is not available. Please choose a different plan.';
      } else if (errorCode === 409 || errorCode === 'PLAN_CHANGE_CONFLICT') {
        title = 'Plan change pending';
        userMessage = errorMessage || 'You already have a pending plan change. Please wait for it to complete.';
      } else if (errorCode === 400 || errorCode === 'INVALID_SUBSCRIPTION_STATUS') {
        title = 'Subscription not active';
        userMessage = 'Your subscription must be active to upgrade. Please resolve any billing issues first.';
      } else if (errorCode === 502 || errorCode === 'PAYPAL_API_ERROR') {
        title = 'Payment service error';
        userMessage = 'Payment service is temporarily unavailable. Please try again in a few moments.';
      } else if (errorCode === 503 || errorCode === 'SERVICE_UNAVAILABLE') {
        title = 'Service unavailable';
        userMessage = 'Payment service is temporarily unavailable. Please try again later.';
      }
      
      toast.error({
        title,
        description: userMessage,
        error: err
      });
    } finally {
      isLoading.value = false;
    }
  }

  /**
   * Cancel subscription plan
   * Shows confirmation and processes cancellation
   * 
   * @param immediate - Whether to cancel immediately or at end of billing period
   * @param reason - Optional cancellation reason
   */
  async function cancelPlan(immediate = false, reason?: string): Promise<void> {
    if (!currentPlan.value) {
      toast.error({
        title: 'Unable to cancel',
        description: 'Please load your current plan details first.'
      });
      return;
    }

    isLoading.value = true;
    error.value = null;

    try {
      const response = await $fetch<CancellationResponse>(
        `/billing/plan/cancel`,
        {
          baseURL: apiBase,
          method: 'POST',
          headers: {
            ...auth.authorizationHeader(),
            'Content-Type': 'application/json',
            ...(tenantSlug.value ? { 'X-Tenant-Slug': tenantSlug.value } : {})
          },
          body: {
            immediate,
            reason
          }
        }
      );

      // Show success message with effective date
      const effectiveDate = new Date(response.effectiveDate).toLocaleDateString();
      
      toast.success({
        title: 'Subscription cancelled',
        description: response.immediate 
          ? 'Your subscription has been cancelled immediately.'
          : `Your subscription will be cancelled on ${effectiveDate}.`
      });

      // Refresh plan details to show updated status
      await fetchPlanDetails(true);
    } catch (err: any) {
      console.error('[BillingPlan] Failed to cancel plan:', err);
      error.value = err instanceof Error ? err : new Error('Failed to cancel plan');
      
      // Extract error details from response
      const errorCode = err?.data?.error || err?.statusCode;
      const errorMessage = err?.data?.message || err?.message;
      
      let userMessage = 'Unable to cancel your subscription. Please try again or contact support.';
      let title = 'Cancellation failed';
      
      // Provide specific error messages based on error code
      if (errorCode === 409 || errorCode === 'INVALID_STATE') {
        title = 'Already cancelled';
        userMessage = errorMessage || 'Your subscription is already cancelled or has a pending cancellation.';
      } else if (errorCode === 400 || errorCode === 'INVALID_SUBSCRIPTION_STATUS') {
        title = 'Cannot cancel subscription';
        userMessage = 'Your subscription cannot be cancelled in its current state. Please contact support.';
      } else if (errorCode === 502 || errorCode === 'PAYPAL_API_ERROR') {
        title = 'Payment service error';
        userMessage = 'Payment service is temporarily unavailable. Your cancellation will be processed when service is restored.';
      } else if (errorCode === 503 || errorCode === 'SERVICE_UNAVAILABLE') {
        title = 'Service unavailable';
        userMessage = 'Payment service is temporarily unavailable. Please try again later.';
      }
      
      toast.error({
        title,
        description: userMessage,
        error: err
      });
    } finally {
      isLoading.value = false;
    }
  }

  /**
   * Update payment method
   * Redirects to PayPal billing portal
   */
  async function updatePaymentMethod(): Promise<void> {
    if (!currentPlan.value) {
      toast.error({
        title: 'Unable to update payment method',
        description: 'Please load your current plan details first.'
      });
      return;
    }

    isLoading.value = true;
    error.value = null;

    try {
      const response = await $fetch<PaymentMethodUpdateResponse>(
        `/billing/plan/payment-method`,
        {
          baseURL: apiBase,
          method: 'POST',
          headers: {
            ...auth.authorizationHeader(),
            'Content-Type': 'application/json',
            ...(tenantSlug.value ? { 'X-Tenant-Slug': tenantSlug.value } : {})
          }
        }
      );

      // Show success message
      toast.success({
        title: 'Redirecting to PayPal',
        description: 'Opening PayPal billing portal to update your payment method...'
      });

      // Redirect to PayPal billing portal
      if (response.portalUrl) {
        window.location.href = response.portalUrl;
      } else {
        throw new Error('No portal URL received from server');
      }
    } catch (err: any) {
      console.error('[BillingPlan] Failed to update payment method:', err);
      error.value = err instanceof Error ? err : new Error('Failed to update payment method');
      
      // Extract error details from response
      const errorCode = err?.data?.error || err?.statusCode;
      const errorMessage = err?.data?.message || err?.message;
      
      let userMessage = 'Unable to open payment method update portal. Please try again or contact support.';
      let title = 'Payment update failed';
      
      // Provide specific error messages based on error code
      if (errorCode === 404 || errorCode === 'NOT_FOUND') {
        title = 'Subscription not found';
        userMessage = 'No active subscription found. Please contact support.';
      } else if (errorCode === 502 || errorCode === 'PAYPAL_API_ERROR') {
        title = 'Payment service error';
        userMessage = 'Payment service is temporarily unavailable. Please try again in a few moments.';
      } else if (errorCode === 503 || errorCode === 'SERVICE_UNAVAILABLE') {
        title = 'Service unavailable';
        userMessage = 'Payment service is temporarily unavailable. Please try again later.';
      }
      
      toast.error({
        title,
        description: userMessage,
        error: err
      });
    } finally {
      isLoading.value = false;
    }
  }

  /**
   * Resume a cancelled subscription
   */
  async function resumePlan(): Promise<void> {
    if (!currentPlan.value) {
      toast.error({
        title: 'Unable to resume',
        description: 'Please load your current plan details first.'
      });
      return;
    }

    isLoading.value = true;
    error.value = null;

    try {
      await $fetch<void>(
        `/billing/plan/resume`,
        {
          baseURL: apiBase,
          method: 'POST',
          headers: {
            ...auth.authorizationHeader(),
            'Content-Type': 'application/json',
            ...(tenantSlug.value ? { 'X-Tenant-Slug': tenantSlug.value } : {})
          }
        }
      );

      toast.success({
        title: 'Subscription resumed',
        description: 'Your subscription has been reactivated.'
      });

      await fetchPlanDetails(true);
    } catch (err: any) {
      console.error('[BillingPlan] Failed to resume subscription:', err);
      error.value = err instanceof Error ? err : new Error('Failed to resume subscription');

      const errorCode = err?.data?.error || err?.statusCode;

      let userMessage = 'Unable to resume your subscription. Please try again or contact support.';
      let title = 'Resumption failed';

      if (errorCode === 400 || errorCode === 'INVALID_SUBSCRIPTION_STATUS') {
        title = 'Cannot resume';
        userMessage = 'Your subscription is not in a resumable state.';
      } else if (errorCode === 404 || errorCode === 'NOT_FOUND') {
        title = 'Subscription not found';
        userMessage = 'No subscription found. Please contact support.';
      } else if (errorCode === 502 || errorCode === 'PAYPAL_API_ERROR') {
        title = 'Payment service unavailable';
        userMessage = 'Payment service is temporarily unavailable. Please try again in a few moments or contact support to manually reactivate your subscription.';
      }

      toast.error({
        title,
        description: userMessage,
        error: err
      });
    } finally {
      isLoading.value = false;
    }
  }

  /**
   * Refresh plan details (force fetch from API)
   */
  async function refreshPlanDetails(): Promise<void> {
    return fetchPlanDetails(true);
  }

  /**
   * Clear cached plan details
   */
  function clearPlanDetails(): void {
    currentPlan.value = null;
    lastFetchTime.value = null;
    error.value = null;
  }

  /**
   * Check if plan has a pending change (downgrade or cancellation)
   */
  const hasPendingChange = computed(() => {
    return !!(currentPlan.value?.pendingPlanTier || currentPlan.value?.cancellationEffectiveDate);
  });

  /**
   * Get pending change description
   */
  const pendingChangeDescription = computed(() => {
    if (!currentPlan.value) return null;

    if (currentPlan.value.cancellationEffectiveDate) {
      const effectiveDate = new Date(currentPlan.value.cancellationEffectiveDate).toLocaleDateString();
      return `Subscription will be cancelled on ${effectiveDate}`;
    }

    if (currentPlan.value.pendingPlanTier && currentPlan.value.pendingPlanEffectiveDate) {
      const effectiveDate = new Date(currentPlan.value.pendingPlanEffectiveDate).toLocaleDateString();
      return `Plan will change to ${currentPlan.value.pendingPlanTier} on ${effectiveDate}`;
    }

    return null;
  });

  /**
   * Check if plan is active
   */
  const isActive = computed(() => {
    return currentPlan.value?.status === 'ACTIVE';
  });

  /**
   * Check if plan is cancelled
   */
  const isCancelled = computed(() => {
    return currentPlan.value?.status === 'CANCELLED';
  });

  /**
   * Get status color for UI display
   */
  const statusColor = computed(() => {
    switch (currentPlan.value?.status) {
      case 'ACTIVE':
        return 'green';
      case 'PAST_DUE':
        return 'orange';
      case 'CANCELLED':
        return 'red';
      case 'PENDING':
        return 'yellow';
      default:
        return 'gray';
    }
  });

  return {
    // State
    currentPlan: readonly(currentPlan),
    isLoading: readonly(isLoading),
    error: readonly(error),
    isCacheValid,
    
    // Computed
    hasPendingChange,
    pendingChangeDescription,
    isActive,
    isCancelled,
    statusColor,
    
    // Methods
    fetchPlanDetails,
    refreshPlanDetails,
    clearPlanDetails,
    upgradePlan,
    cancelPlan,
    updatePaymentMethod,
    resumePlan
  };
}
