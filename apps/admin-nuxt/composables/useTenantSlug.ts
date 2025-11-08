import { computed, watch } from 'vue';

const TENANT_COOKIE_NAME = 'tenantSlug';

export function useTenantSlug() {
  const runtimeConfig = useRuntimeConfig();
  const defaultTenant =
    runtimeConfig.public?.defaultTenantSlug?.toLowerCase() || 'default';

  const cookie = useCookie<string | null>(TENANT_COOKIE_NAME, {
    path: '/',
    sameSite: 'lax',
    default: () => defaultTenant,
  });

  const state = useState<string>('tenant:slug', () => cookie.value || defaultTenant);

  watch(
    () => cookie.value,
    (value) => {
      if (value && value !== state.value) {
        state.value = value.toLowerCase();
      }
    },
    { immediate: true }
  );

  watch(
    () => state.value,
    (value) => {
      const normalized = (value || defaultTenant).toLowerCase();
      if (cookie.value !== normalized) {
        cookie.value = normalized;
      }
    }
  );

  const setTenantSlug = (value: string) => {
    const normalized = (value || '').trim().toLowerCase();
    state.value = normalized || defaultTenant;
  };

  return {
    tenantSlug: computed(() => state.value || defaultTenant),
    setTenantSlug,
  };
}
