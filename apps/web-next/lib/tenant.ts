const DEFAULT_TENANT =
  process.env.NEXT_PUBLIC_DEFAULT_TENANT?.toLowerCase() || 'default';

export const normalizeDomain = (value?: string | null): string | null => {
  if (!value) return null;
  const cleaned = value.trim().toLowerCase().replace(/^https?:\/\//, '');
  const withoutPath = cleaned.split('/')[0];
  const withoutPort = withoutPath.split(':')[0];
  const withoutWww = withoutPort.startsWith('www.')
    ? withoutPort.slice(4)
    : withoutPort;
  return withoutWww || null;
};

const baseDomainEnv =
  normalizeDomain(process.env.NEXT_PUBLIC_BASE_DOMAIN) ||
  normalizeDomain(process.env.APP_BASE_DOMAIN);

const BASE_DOMAIN = baseDomainEnv || 'localhost';

export const TENANT_COOKIE = 'tenantSlug';
export const TENANT_HEADER = 'x-tenant-slug';

export function sanitizeTenantSlug(value?: string | null): string | null {
  if (!value) return null;
  const normalized = value.trim().toLowerCase();
  return normalized.length ? normalized : null;
}

export function tenantFromHost(host?: string | null): string | null {
  const normalizedHost = normalizeDomain(host);
  if (!normalizedHost) return null;
  const hostParts = normalizedHost.split('.');

  if (!normalizedHost) {
    return null;
  }

  // If base domain was misconfigured to include a subdomain (e.g., "tenant.domain.com"),
  // recover the tenant from the first label instead of falling back to default.
  if (normalizedHost === BASE_DOMAIN) {
    if (hostParts.length >= 3) {
      return hostParts[0];
    }
    return null;
  }

  if (normalizedHost.endsWith(`.${BASE_DOMAIN}`)) {
    return normalizedHost.slice(0, normalizedHost.length - BASE_DOMAIN.length - 1);
  }

  // Fallback: if host has multiple labels and doesn't match the expected base domain, use the first label as tenant.
  if (hostParts.length > 1) {
    return hostParts[0];
  }

  return null;
}

export function getTenantSlugClient(): string {
  if (typeof window === 'undefined') {
    return DEFAULT_TENANT;
  }

  const cookieMatch = document.cookie
    .split(';')
    .map((c) => c.trim())
    .find((c) => c.startsWith(`${TENANT_COOKIE}=`));

  if (cookieMatch) {
    const [, value] = cookieMatch.split('=');
    const slug = sanitizeTenantSlug(decodeURIComponent(value));
    if (slug) {
      return slug;
    }
  }

  // Fallback on the client: derive from hostname and persist to cookie for consistency.
  const slug = tenantFromHost(window.location.hostname);
  if (slug) {
    document.cookie = `${TENANT_COOKIE}=${encodeURIComponent(slug)}; path=/; sameSite=lax`;
    return slug;
  }

  return DEFAULT_TENANT;
}

export function getDefaultTenantSlug() {
  return DEFAULT_TENANT;
}
