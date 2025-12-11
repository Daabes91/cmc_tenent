import type { NextRequest } from 'next/server';
import { NextResponse } from 'next/server';
import { locales } from './i18n/request';
import {
  TENANT_COOKIE,
  TENANT_HEADER,
  sanitizeTenantSlug,
  tenantFromHost,
  getDefaultTenantSlug,
  normalizeDomain,
} from './lib/tenant';

const defaultLocale = locales[0] ?? 'en';
const TENANT_LOOKUP_PATH = '/_internal/tenant/resolve';
const baseDomain =
  normalizeDomain(process.env.NEXT_PUBLIC_BASE_DOMAIN) ||
  normalizeDomain(process.env.APP_BASE_DOMAIN) ||
  null;

function getLookupPath() {
  const rawBasePath = process.env.NEXT_PUBLIC_BASE_PATH?.trim() || '';
  if (!rawBasePath || rawBasePath === '/') {
    return TENANT_LOOKUP_PATH;
  }

  const normalizedBasePath = rawBasePath.startsWith('/')
    ? rawBasePath.replace(/\/$/, '')
    : `/${rawBasePath.replace(/\/$/, '')}`;

  return `${normalizedBasePath}${TENANT_LOOKUP_PATH}`;
}

function getRequestHost(request: NextRequest) {
  // Prefer forwarded host (load balancers/proxies) but fallback to direct host header
  const forwardedHost = request.headers.get('x-forwarded-host');
  if (forwardedHost) {
    const firstHost = forwardedHost.split(',').map((h) => h.trim()).find(Boolean);
    if (firstHost) return firstHost;
  }
  return request.headers.get('host');
}

async function lookupTenantByDomain(host: string, request: NextRequest): Promise<{ slug: string | null; notFound?: boolean }> {
  const domain = normalizeDomain(host);
  if (!domain) return { slug: null };

  return lookupTenantViaApi(domain);
}

async function lookupTenantViaApi(domain: string): Promise<{ slug: string | null; notFound?: boolean }> {
  const apiBase = process.env.NEXT_PUBLIC_API_URL || '';
  if (!apiBase) {
    return { slug: null };
  }

  try {
    const apiUrl = buildTenantResolveUrl(apiBase, domain);
    if (!apiUrl) {
      return { slug: null };
    }

    const response = await fetch(apiUrl.toString(), { cache: 'no-store' });

    if (response.status === 404) {
      return { slug: null, notFound: true };
    }

    if (!response.ok) {
      console.error('API tenant lookup failed with status:', response.status);
      return { slug: null };
    }

    const data = await response.json();
    return { slug: sanitizeTenantSlug(data.slug) };
  } catch (error) {
    console.error('API tenant lookup failed:', error);
    return { slug: null };
  }
}

function buildTenantResolveUrl(apiBase: string, domain: string): URL | null {
  try {
    const base = new URL(apiBase);
    const basePath = base.pathname.endsWith('/')
      ? base.pathname.slice(0, -1)
      : base.pathname;
    const resolvePath = `${basePath}/tenant/resolve`;
    const url = new URL(resolvePath, base.origin);
    url.searchParams.set('domain', domain);
    return url;
  } catch {
    console.error('Invalid NEXT_PUBLIC_API_URL, cannot build tenant resolve URL');
    return null;
  }
}

function deriveSubdomainFromBase(host?: string | null): string | null {
  if (!host || !baseDomain) return null;
  const normalizedHost = normalizeDomain(host);
  if (!normalizedHost) return null;

  if (normalizedHost === baseDomain) {
    return null;
  }

  if (normalizedHost.endsWith(`.${baseDomain}`)) {
    const candidate = normalizedHost.slice(0, normalizedHost.length - baseDomain.length - 1);
    return sanitizeTenantSlug(candidate);
  }

  return null;
}

function extractSubdomain(host?: string | null): string | null {
  const normalizedHost = normalizeDomain(host);
  if (!normalizedHost) return null;

  const parts = normalizedHost.split('.');
  if (parts.length < 3) {
    return null;
  }

  const candidate = parts[0];
  return sanitizeTenantSlug(candidate);
}

async function resolveTenantSlug(request: NextRequest): Promise<{ slug: string | null; redirect?: URL; notFound?: boolean }> {
  const defaultSlug = getDefaultTenantSlug();
  const url = request.nextUrl;

  const queryParam = sanitizeTenantSlug(url.searchParams.get('tenant'));

  let slug: string | null = queryParam;
  let notFound = false;

  if (!slug) {
    const host =
      getRequestHost(request) ||
      request.nextUrl.host ||
      request.headers.get('host');
    const subdomainSlug =
      sanitizeTenantSlug(tenantFromHost(host)) ||
      deriveSubdomainFromBase(host) ||
      extractSubdomain(host);

    if (subdomainSlug) {
      slug = subdomainSlug;
    } else if (host) {
      const lookup = await lookupTenantByDomain(host, request);
      slug = lookup.slug;
      notFound = lookup.notFound ?? false;
    }
  }

  if (!slug) {
    const cookieSlug = request.cookies.get(TENANT_COOKIE)?.value;
    slug = sanitizeTenantSlug(cookieSlug);
  }

  if (!slug) {
    slug = defaultSlug;
  }

  if (queryParam) {
    const cleanUrl = new URL(url.href);
    cleanUrl.searchParams.delete('tenant');
    return { slug, redirect: cleanUrl, notFound };
  }

  return { slug, notFound };
}

function ensureLocale(url: URL) {
  const pathname = url.pathname;

  const segments = pathname.split('/').filter(Boolean);
  const locale = segments[0];

  if (locale && locales.includes(locale as any)) {
    return null;
  }

  const normalizedPath = pathname.startsWith('/') ? pathname : `/${pathname}`;
  url.pathname =
    normalizedPath === '/' ? `/${defaultLocale}` : `/${defaultLocale}${normalizedPath}`;
  return url;
}

export default async function middleware(request: NextRequest) {
  if (request.headers.get('x-tenant-lookup') === '1') {
    return NextResponse.next();
  }

  const tenantResolution = await resolveTenantSlug(request);
  
  // If tenant not found, rewrite to error page
  if (tenantResolution.notFound) {
    return NextResponse.rewrite(new URL('/_errors/no-tenant', request.url));
  }
  
  const localeRedirect = ensureLocale(new URL(request.url));

  if (tenantResolution.redirect) {
    const response = NextResponse.redirect(tenantResolution.redirect);
    response.cookies.set(TENANT_COOKIE, tenantResolution.slug!, {
      path: '/',
      sameSite: 'lax',
    });
    return response;
  }

  if (localeRedirect) {
    const response = NextResponse.redirect(localeRedirect);
    response.cookies.set(TENANT_COOKIE, tenantResolution.slug!, {
      path: '/',
      sameSite: 'lax',
    });
    return response;
  }

  const requestHeaders = new Headers(request.headers);
  requestHeaders.set(TENANT_HEADER, tenantResolution.slug!);

  const response = NextResponse.next({
    request: {
      headers: requestHeaders,
    },
  });

  response.headers.set('x-tenant-slug', tenantResolution.slug!);

  response.cookies.set(TENANT_COOKIE, tenantResolution.slug!, {
    path: '/',
    sameSite: 'lax',
  });

  return response;
}

export const config = {
  matcher: [
    '/',
    '/(ar|en)/:path*',
    '/((?!_next|_vercel|_errors|api/internal|_internal|favicon.ico|.*\\..*).*)',
  ],
};
