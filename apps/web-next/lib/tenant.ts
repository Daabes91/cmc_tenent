const DEFAULT_TENANT =
  process.env.NEXT_PUBLIC_DEFAULT_TENANT?.toLowerCase() || 'default';
const BASE_DOMAIN =
  process.env.NEXT_PUBLIC_BASE_DOMAIN?.toLowerCase() || 'localhost';

export const TENANT_COOKIE = 'tenantSlug';
export const TENANT_HEADER = 'x-tenant-slug';

export function sanitizeTenantSlug(value?: string | null): string | null {
  if (!value) return null;
  const normalized = value.trim().toLowerCase();
  return normalized.length ? normalized : null;
}

export function tenantFromHost(host?: string | null): string | null {
  if (!host) return null;
  const bareHost = host.toLowerCase().split(':')[0];

  if (!bareHost || bareHost === BASE_DOMAIN) {
    return null;
  }

  if (bareHost.endsWith(`.${BASE_DOMAIN}`)) {
    return bareHost.slice(0, bareHost.length - BASE_DOMAIN.length - 1);
  }

  // When using dedicated domains (per-tenant custom domains), rely on host itself.
  if (BASE_DOMAIN === 'localhost') {
    // Support dev domains like tenant.localhost or tenant.local
    const parts = bareHost.split('.');
    if (parts.length > 1 && parts[parts.length - 1] === 'localhost') {
      return parts[0];
    }
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

  // As a fallback on the client, attempt to derive from hostname
  const slug = tenantFromHost(window.location.hostname);
  if (slug) {
    return slug;
  }

  return DEFAULT_TENANT;
}

export function getDefaultTenantSlug() {
  return DEFAULT_TENANT;
}
