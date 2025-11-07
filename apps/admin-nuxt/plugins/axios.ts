export default defineNuxtPlugin(() => {
  if (!import.meta.server) {
    return;
  }

  const auth = useAuth();
  const nativeFetch = globalThis.fetch.bind(globalThis);

  const fetchWithAuth = async (request: RequestInfo | URL, init?: RequestInit) => {
    const headers = new Headers(init?.headers || {});
    const token = auth.accessToken.value;
    if (token && !headers.has("Authorization")) {
      headers.set("Authorization", `Bearer ${token}`);
    }
    return nativeFetch(request, { ...init, headers });
  };

  if (globalThis.fetch !== fetchWithAuth) {
    globalThis.fetch = fetchWithAuth;
  }
});
