export default defineNuxtRouteMiddleware((to) => {
  const queryValue = Array.isArray(to.query.tenant)
    ? to.query.tenant[0]
    : to.query.tenant;

  if (typeof queryValue === "string" && queryValue.trim().length) {
    const { setTenantSlug } = useTenantSlug();
    setTenantSlug(queryValue);

    const cleanedQuery = { ...to.query };
    delete cleanedQuery.tenant;

    return navigateTo({
      path: to.path,
      hash: to.hash,
      query: cleanedQuery
    });
  }
});
