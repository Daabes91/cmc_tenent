import { defineNuxtRouteMiddleware, navigateTo } from "#app";

export default defineNuxtRouteMiddleware(async () => {
  // Run this guard only on the client to avoid SSR/plugin issues
  if (process.server) return;

  try {
    const { tenant, fetchTenantContext } = useTenantContext();
    const toast = useToast();

    if (!tenant.value) {
      await fetchTenantContext().catch((err) => {
        // log but don't block navigation
        console.error("[ecommerce-enabled] Failed to fetch tenant context", err);
      });
    }

    if (tenant.value && tenant.value.ecommerceEnabled === false) {
      toast.add({
        title: "E-commerce is disabled",
        description: "Enable the e-commerce feature for this tenant to access this section.",
        color: "amber",
        icon: "i-lucide-eye-off",
      });
      return navigateTo("/");
    }
  } catch (err) {
    // Never hard-block navigation; surface the error for debugging
    console.error("[ecommerce-enabled] Middleware error", err);
  }
});
