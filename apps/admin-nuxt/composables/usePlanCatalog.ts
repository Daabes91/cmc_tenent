import type { Ref } from 'vue';

type CatalogPlan = {
  tier: string;
  name: string;
  description?: string;
  monthlyPrice?: number | null;
  annualPrice?: number | null;
  currency?: string;
  features?: string[];
  popular?: boolean;
};

const FALLBACK_FEATURES = [
  'Unlimited doctors and staff accounts',
  'Priority chat & email support',
  'Custom domains and branding',
  'Audit-ready activity logs'
];

const FALLBACK_PLANS: CatalogPlan[] = [
  {
    tier: 'PROFESSIONAL',
    name: 'Professional',
    description: 'Perfect for growing clinics',
    monthlyPrice: 99,
    currency: 'USD',
    features: [
      'Up to 5 doctors',
      'Advanced reporting',
      'Priority support',
      'Custom branding'
    ],
    popular: true
  },
  {
    tier: 'ENTERPRISE',
    name: 'Enterprise',
    description: 'For large multi-location clinics',
    monthlyPrice: 199,
    currency: 'USD',
    features: [
      'Unlimited doctors',
      'Multi-location support',
      'Dedicated account manager',
      'Custom integrations'
    ]
  }
];

export function usePlanCatalog() {
  const apiBase = useApiBase();
  const auth = useAuth();
  const { tenantSlug } = useTenantSlug();
  const plans = useState<CatalogPlan[]>('planCatalog:plans', () => []);
  const loading = useState<boolean>('planCatalog:loading', () => false);
  const error = useState<Error | null>('planCatalog:error', () => null);

  const normalizedPlans = computed(() => {
    if (!plans.value.length) return [];
    return plans.value.map((plan) => ({
      tier: plan.tier,
      label: plan.name || plan.tier,
      price: plan.monthlyPrice ?? null,
      currency: plan.currency || 'USD',
      billingCycle: 'MONTHLY' as const,
      description: plan.description || '',
      features: plan.features?.length ? plan.features : FALLBACK_FEATURES,
      popular: plan.popular === true
    }));
  });

  const fallbackNormalized = FALLBACK_PLANS.map((plan) => ({
    tier: plan.tier,
    label: plan.name,
    price: plan.monthlyPrice,
    currency: plan.currency || 'USD',
    billingCycle: 'MONTHLY' as const,
    description: plan.description || '',
    features: plan.features || FALLBACK_FEATURES,
    popular: plan.popular === true
  }));

  async function fetchPlans(force = false) {
    // Only fetch client-side where cookies/tokens are available
    if (import.meta.server) return;
    if (!auth.isAuthenticated.value) return;
    if (!auth.accessToken.value) return;
    if (plans.value.length && !force) return;

    loading.value = true;
    error.value = null;

    try {
      const response = await $fetch<CatalogPlan[] | { data: CatalogPlan[] }>(
        '/plans?currency=USD',
        {
          // Use admin API base; endpoint is now admin-protected
          baseURL: apiBase,
          headers: {
            'Accept': 'application/json',
            ...auth.authorizationHeader(),
            ...(tenantSlug.value ? { 'X-Tenant-Slug': tenantSlug.value } : {})
          }
        }
      );

      const items = Array.isArray(response)
        ? response
        : Array.isArray((response as any)?.data)
          ? (response as any).data
          : [];

      if (items.length) {
        plans.value = items;
      } else {
        plans.value = [];
      }
    } catch (err: any) {
      console.error('[PlanCatalog] Failed to fetch public plans', err);
      error.value = err instanceof Error ? err : new Error('Failed to fetch plans');
      plans.value = [];
    } finally {
      loading.value = false;
    }
  }

  return {
    plans: readonly(plans) as Ref<CatalogPlan[]>,
    loading: readonly(loading),
    error: readonly(error),
    normalizedPlans,
    fallbackNormalized,
    fetchPlans
  };
}
