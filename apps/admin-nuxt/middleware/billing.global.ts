/**
 * Global middleware to check billing status and restrict access based on subscription state.
 * 
 * This middleware:
 * - Checks billing status on every route navigation
 * - Redirects to appropriate billing pages if subscription is not active
 * - Allows access to login, billing pages, and set-password without billing checks
 * - Fetches billing status from API with caching
 */

export default defineNuxtRouteMiddleware(async (to) => {
  // Skip billing check for pages that don't require it
  const publicPaths = ['/login', '/set-password'];
  const billingPaths = ['/billing/pending', '/billing/past-due', '/billing/canceled'];
  
  // Allow access to public pages and billing pages without checking
  if (publicPaths.includes(to.path) || billingPaths.includes(to.path)) {
    return;
  }

  // Skip if page explicitly disables billing check via meta
  if (to.meta.billing === false) {
    return;
  }

  const auth = useAuth();
  const billing = useBillingStatus();

  // Only check billing if user is authenticated
  if (!auth.isAuthenticated.value) {
    return;
  }

  try {
    // Fetch billing status (uses cache if available)
    await billing.fetchBillingStatus();

    // Allow access if billing is active
    if (billing.isActive.value) {
      return;
    }

    // Redirect based on billing status
    const status = billing.billingStatus.value;
    
    if (status === 'pending_payment') {
      return navigateTo('/billing/pending');
    }
    
    if (status === 'past_due') {
      return navigateTo('/billing/past-due');
    }
    
    if (status === 'canceled') {
      return navigateTo('/billing/canceled');
    }

    // Default: redirect to pending payment page
    return navigateTo('/billing/pending');
    
  } catch (error) {
    console.error('[Billing Middleware] Error checking billing status:', error);
    
    // On error, allow access but log the issue
    // This prevents blocking users if the billing API is temporarily unavailable
    return;
  }
});
