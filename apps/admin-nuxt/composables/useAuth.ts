import type { AuthTokensResponse, StaffLoginRequest } from "@/types/auth";

const ACCESS_COOKIE = "admin_access_token";
const REFRESH_COOKIE = "admin_refresh_token";
const REFRESH_LEAD_TIME = 15_000;
const REFRESH_MIN_DELAY = 5_000;
const REFRESH_FAILURE_COOLDOWN = 5_000;
const MAX_TIMEOUT_DELAY = 2_147_483_647; // Max safe setTimeout delay in milliseconds (~24.8 days)

export function useAuth() {
  const apiBase = useApiBase();
  const { tenantSlug } = useTenantSlug();

  const isDev = import.meta.dev;

  const accessTokenCookie = useCookie<string | null>(ACCESS_COOKIE, {
    sameSite: "lax",
    secure: !isDev,
    httpOnly: false,
    maxAge: 60 * 60
  });
  const refreshTokenCookie = useCookie<string | null>(REFRESH_COOKIE, {
    sameSite: "lax",
    secure: !isDev,
    httpOnly: false,
    maxAge: 60 * 60 * 24 * 30
  });

  const accessToken = useState<string | null>("auth.accessToken", () => accessTokenCookie.value ?? null);
  const accessTokenExpiresAt = useState<number | null>("auth.accessTokenExpiresAt", () => null);
  const userEmail = useState<string | null>("auth.userEmail", () => null);
  const userName = useState<string | null>("auth.userName", () => null);
  const userRole = useState<string | null>("auth.userRole", () => null);

  // Use ref to persist timeout across composable calls
  const refreshTimeout = useState<ReturnType<typeof setTimeout> | null>("auth.refreshTimeout", () => null);
  const refreshPromise = useState<Promise<void> | null>("auth.refreshPromise", () => null);
  const lastRefreshFailureAt = useState<number | null>("auth.lastRefreshFailureAt", () => null);

  // Only hydrate on client side, and only once
  if (import.meta.client && accessToken.value && !userEmail.value) {
    hydrateUserFromToken(accessToken.value);
  }

  const isAuthenticated = computed(() => {
    if (!accessToken.value) {
      return false;
    }
    if (!accessTokenExpiresAt.value) {
      return true;
    }
    return Date.now() < accessTokenExpiresAt.value - 5_000;
  });

  async function login(payload: StaffLoginRequest) {
    console.log('[useAuth] login called with payload:', payload);
    console.log('[useAuth] apiBase:', apiBase);

    try {
      console.log('[useAuth] Making fetch request to /auth/login');
      const response = await $fetch<AuthTokensResponse>("/auth/login", {
        baseURL: apiBase,
        method: "POST",
        headers: tenantHeaders(),
        body: payload
      });

      console.log('[useAuth] Login response received:', response);
      persistTokens(response);
      // Fetch profile in background - don't block login on it
      fetchProfile().catch((err) => {
        console.warn('[Auth] Failed to fetch profile after login:', err);
      });
    } catch (error: any) {
      console.error('[useAuth] Login error:', error);
      clearAuth();
      throw normalizeError(error);
    }
  }

  async function refresh() {
    if (refreshPromise.value) {
      return refreshPromise.value;
    }

    if (
      lastRefreshFailureAt.value &&
      Date.now() - lastRefreshFailureAt.value < REFRESH_FAILURE_COOLDOWN
    ) {
      console.warn('[Auth] Refresh blocked - recently failed');
      throw new Error("Refresh recently failed");
    }

    if (!refreshTokenCookie.value) {
      console.warn('[Auth] Refresh blocked - no refresh token');
      clearAuth();
      lastRefreshFailureAt.value = Date.now();
      throw new Error("Missing refresh token");
    }

    console.log('[Auth] Starting token refresh...');
    refreshPromise.value = (async () => {
      try {
        const response = await $fetch<AuthTokensResponse>("/auth/refresh", {
          baseURL: apiBase,
          method: "POST",
          headers: tenantHeaders(),
          body: { refreshToken: refreshTokenCookie.value }
        });
        console.log('[Auth] Token refresh successful');
        persistTokens(response);
        lastRefreshFailureAt.value = null;
      } catch (error: any) {
        console.error('[Auth] Token refresh failed:', error?.status, error?.message);
        if (error?.status === 401 || error?.status === 403) {
          console.log('[Auth] Invalid refresh token - clearing auth');
          clearAuth();
        }
        lastRefreshFailureAt.value = Date.now();
        throw error;
      } finally {
        refreshPromise.value = null;
      }
    })();

    return refreshPromise.value;
  }

  async function logout() {
    if (refreshTokenCookie.value) {
      await $fetch("/auth/logout", {
        baseURL: apiBase,
        method: "POST",
        headers: tenantHeaders(),
        body: { refreshToken: refreshTokenCookie.value }
      }).catch(() => undefined);
    }
    clearAuth();
  }

  function persistTokens(tokens: AuthTokensResponse) {
    // Set refresh token first
    const refreshExpiry = tokens.refreshTokenExpiresAt ? new Date(tokens.refreshTokenExpiresAt).getTime() : null;
    refreshTokenCookie.value = tokens.refreshToken;
    if (refreshExpiry) {
      const seconds = Math.max(60, Math.floor((refreshExpiry - Date.now()) / 1000));
      refreshTokenCookie.maxAge = seconds;
    }

    // Set access token - hydrateUserFromToken will extract expiry from JWT and schedule refresh
    accessToken.value = tokens.accessToken;
    accessTokenCookie.value = tokens.accessToken;

    // Calculate cookie maxAge from the response expiry (if provided)
    const accessExpiry = tokens.accessTokenExpiresAt ? new Date(tokens.accessTokenExpiresAt).getTime() : null;
    if (accessExpiry) {
      const seconds = Math.max(60, Math.floor((accessExpiry - Date.now()) / 1000));
      accessTokenCookie.maxAge = seconds;
    }

    // This will extract user info AND set accessTokenExpiresAt from JWT, which triggers scheduleTokenRefresh
    hydrateUserFromToken(tokens.accessToken);
  }

  async function fetchProfile() {
    try {
      const profile = await $fetch<{ email: string; fullName: string; role: string }>("/auth/profile", {
        baseURL: apiBase,
        headers: {
          ...authorizationHeader(),
          ...tenantHeaders()
        }
      });
      userEmail.value = profile.email;
      userName.value = profile.fullName;
      userRole.value = profile.role;
    } catch {
      // fallback to email claim from token in future enhancement
    }
  }

  function cancelScheduledRefresh() {
    if (refreshTimeout.value) {
      clearTimeout(refreshTimeout.value);
      refreshTimeout.value = null;
      console.log('[Auth] Cancelled scheduled refresh');
    }
  }

  function scheduleTokenRefresh() {
    if (import.meta.server) {
      return;
    }
    cancelScheduledRefresh();
    if (!accessTokenExpiresAt.value) {
      console.log('[Auth] No access token expiry - skipping refresh schedule');
      return;
    }

    const delay = accessTokenExpiresAt.value - Date.now() - REFRESH_LEAD_TIME;

    // Cap delay to prevent setTimeout overflow (max ~24.8 days)
    let normalizedDelay = delay <= REFRESH_MIN_DELAY ? REFRESH_MIN_DELAY : delay;
    if (normalizedDelay > MAX_TIMEOUT_DELAY) {
      console.warn('[Auth] Token expiry exceeds max setTimeout delay - capping at 24 days');
      normalizedDelay = MAX_TIMEOUT_DELAY;
    }

    if (delay <= REFRESH_MIN_DELAY) {
      console.warn('[Auth] Token expiring soon - scheduling refresh in', Math.round(normalizedDelay / 1000), 'seconds');
    } else {
      console.log('[Auth] Scheduling token refresh - delay:', Math.round(normalizedDelay / 1000), 'seconds');
    }

    refreshTimeout.value = setTimeout(() => {
      console.log('[Auth] Scheduled refresh triggered');
      refresh().catch((error) => {
        console.error('[Auth] Scheduled refresh failed:', error);
      });
    }, normalizedDelay);
  }

  function clearAuth() {
    cancelScheduledRefresh();
    accessToken.value = null;
    accessTokenCookie.value = null;
    accessTokenExpiresAt.value = null;
    refreshTokenCookie.value = null;
    userEmail.value = null;
    userName.value = null;
    userRole.value = null;

    // Clear permissions when clearing auth
    const { clearPermissions } = usePermissions();
    clearPermissions();
  }

  function hydrateUserFromToken(token: string | null) {
    if (!token) return;
    try {
      const base64 = token.split(".")[1] || "";
      const decoded = import.meta.server
        ? (globalThis.Buffer?.from(base64, "base64").toString("utf-8") ?? "")
        : atob(base64);
      const payload = JSON.parse(decoded);
      userEmail.value = payload.email ?? null;
      userName.value = payload.name ?? null;
      const roles = Array.isArray(payload.roles) ? payload.roles : [];
      userRole.value = roles[0]?.replace("ROLE_", "") ?? null;
      if (typeof payload.exp === "number") {
        accessTokenExpiresAt.value = payload.exp * 1000;
        scheduleTokenRefresh();
      }
    } catch {
      // ignore parsing errors
    }
  }

  function authorizationHeader(): Record<string, string> {
    return accessToken.value ? { Authorization: `Bearer ${accessToken.value}` } : {};
  }

  function normalizeError(error: any): Error {
    if (error?.status === 401) {
      return new Error("Invalid credentials or verification code.");
    }
    if (error?.data?.message) {
      return new Error(error.data.message);
    }
    return error instanceof Error ? error : new Error("Unexpected authentication error.");
  }

  return {
    login,
    logout,
    refresh,
    fetchProfile,
    isAuthenticated,
    authorizationHeader,
    accessToken,
    accessTokenExpiresAt,
    userEmail,
    userName,
    userRole
  };

  function tenantHeaders() {
    const slug = tenantSlug.value;
    return slug ? { "X-Tenant-Slug": slug } : {};
  }
}
