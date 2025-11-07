export default defineNuxtRouteMiddleware(async (to) => {
  const auth = useAuth();

  // Check if page explicitly disables auth via meta
  if (to.meta.auth === false) {
    return;
  }

  // allow login page without authentication
  if (to.path === "/login") {
    if (auth.isAuthenticated.value) {
      return navigateTo("/");
    }
    return;
  }

  if (!auth.isAuthenticated.value) {
    try {
      await auth.refresh();
    } catch {
      return navigateTo({ path: "/login", query: { redirect: to.fullPath } });
    }
  }

  if (!auth.userEmail.value && auth.isAuthenticated.value) {
    await auth.fetchProfile().catch(() => undefined);
  }
});
