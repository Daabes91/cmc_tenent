import { useState } from "#app";

type TenantContext = {
  id: number;
  slug: string;
  ecommerceEnabled: boolean;
};

export function useTenantContext() {
  const tenant = useState<TenantContext | null>("tenant:context", () => null);
  const loading = useState<boolean>("tenant:context:loading", () => false);
  const error = useState<Error | null>("tenant:context:error", () => null);

  const fetchTenantContext = async () => {
    const api = useAdminApi();
    loading.value = true;
    error.value = null;
    try {
      const data = await api.request<TenantContext>("/admin/tenant/me");
      tenant.value = data;
    } catch (err: any) {
      error.value = err instanceof Error ? err : new Error(String(err));
      tenant.value = null;
    } finally {
      loading.value = false;
    }
  };

  return {
    tenant,
    loading,
    error,
    fetchTenantContext,
  };
}
