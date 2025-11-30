import type { NextRequest } from 'next/server';
import { NextResponse } from 'next/server';
import { locales } from './i18n/request';
import {
  TENANT_COOKIE,
  TENANT_HEADER,
  sanitizeTenantSlug,
  tenantFromHost,
  getDefaultTenantSlug,
} from './lib/tenant';

const defaultLocale = locales[0] ?? 'en';

async function resolveTenantSlug(request: NextRequest): Promise<{ slug: string | null; redirect?: URL; notFound?: boolean }> {
  const defaultSlug = getDefaultTenantSlug();
  const url = request.nextUrl;

  const queryParam = sanitizeTenantSlug(url.searchParams.get('tenant'));

  let slug: string | null = queryParam;

  if (!slug) {
    const host = request.headers.get('host');
    slug = sanitizeTenantSlug(tenantFromHost(host));
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
    return { slug, redirect: cleanUrl };
  }

  return { slug };
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
    '/((?!_next|_vercel|_errors|api/internal|favicon.ico|.*\\..*).*)',
  ],
};
