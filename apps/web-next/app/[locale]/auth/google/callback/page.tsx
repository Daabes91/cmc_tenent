'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useTranslations } from 'next-intl';
import { saveAuth } from '@/lib/auth';
import { getTenantSlugClient, TENANT_HEADER } from '@/lib/tenant';
import type { PatientAuthResponse } from '@/lib/types';
import {
  classifyOAuthError,
  logOAuthError,
  getErrorRedirectPath,
  type OAuthError,
} from '@/lib/oauth-errors';
import OAuthErrorDisplay from '@/components/OAuthErrorDisplay';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/public';
const RETURN_URL_KEY = 'oauthReturnUrl';

function consumeReturnUrl(): string | null {
  if (typeof window === 'undefined') return null;

  // Prefer sessionStorage (same-origin), fall back to cookie (shared across subdomains)
  let url = sessionStorage.getItem(RETURN_URL_KEY);
  if (url) {
    sessionStorage.removeItem(RETURN_URL_KEY);
  } else {
    const match = document.cookie
      .split(';')
      .map((c) => c.trim())
      .find((c) => c.startsWith(`${RETURN_URL_KEY}=`));
    if (match) {
      url = decodeURIComponent(match.split('=')[1]);
    }
  }

  // Clear cookie
  const host = window.location.hostname;
  const hostParts = host.split('.');
  const baseDomain =
    host === 'localhost' || host.endsWith('.localhost')
      ? 'localhost'
      : hostParts.length > 1
        ? hostParts.slice(-2).join('.')
        : host;
  const cookieDomain = baseDomain === host ? '' : `; domain=.${baseDomain}`;
  document.cookie = `${RETURN_URL_KEY}=; Max-Age=0; path=/${cookieDomain}; sameSite=Lax`;

  return url;
}

export default function GoogleCallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const t = useTranslations();
  const [oauthError, setOauthError] = useState<OAuthError | null>(null);
  const [processing, setProcessing] = useState(true);
  const [retryCount, setRetryCount] = useState(0);
  const MAX_RETRIES = 2;

  const handleCallback = async (isRetry = false) => {
    try {
      if (isRetry) {
        setProcessing(true);
        setOauthError(null);
      }

      const code = searchParams.get('code');
      const state = searchParams.get('state');
      const errorParam = searchParams.get('error');

      // Classify and handle OAuth error parameter
      if (errorParam) {
        const error = classifyOAuthError(null, undefined, errorParam);
        logOAuthError(error, {
          component: 'GoogleCallbackPage',
          errorParam,
        });
        setOauthError(error);
        setProcessing(false);

        const redirectPath = getErrorRedirectPath(error);
        setTimeout(() => router.push(redirectPath), 3000);
        return;
      }

      // Validate required parameters
      if (!code || !state) {
        const error = classifyOAuthError(new Error('Missing code or state parameter'));
        logOAuthError(error, {
          component: 'GoogleCallbackPage',
          hasCode: !!code,
          hasState: !!state,
        });
        setOauthError(error);
        setProcessing(false);
        setTimeout(() => router.push('/login'), 3000);
        return;
      }

      const tenantSlug = getTenantSlugClient();
      if (!tenantSlug) {
        const error = classifyOAuthError(new Error('Tenant context not found'));
        logOAuthError(error, {
          component: 'GoogleCallbackPage',
        });
        setOauthError(error);
        setProcessing(false);
        setTimeout(() => router.push('/'), 3000);
        return;
      }

      const response = await fetch(
        `${API_URL}/auth/google/callback?code=${encodeURIComponent(code)}&state=${encodeURIComponent(state)}`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            [TENANT_HEADER]: tenantSlug,
          },
        }
      );

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        const error = classifyOAuthError(
          new Error(errorData.message || 'Authentication failed'),
          response.status
        );

        logOAuthError(error, {
          component: 'GoogleCallbackPage',
          statusCode: response.status,
          tenantSlug,
          retryCount,
        });

        setOauthError(error);
        setProcessing(false);

        const redirectPath = getErrorRedirectPath(error);
        setTimeout(() => router.push(redirectPath), 3000);
        return;
      }

      const authData: PatientAuthResponse = await response.json();

      saveAuth(authData);

      const returnUrl = consumeReturnUrl();
      if (returnUrl) {
        try {
          const target = new URL(returnUrl);
          const dashboardUrl = new URL('/dashboard', target.origin);
          window.location.href = dashboardUrl.toString();
          return;
        } catch {
          // ignore and fall through
        }
      }

      router.push('/dashboard');
    } catch (err) {
      const error = classifyOAuthError(err);
      logOAuthError(error, {
        component: 'GoogleCallbackPage',
        retryCount,
        tenantSlug: getTenantSlugClient(),
      });

      setOauthError(error);
      setProcessing(false);

      const redirectPath = getErrorRedirectPath(error);
      setTimeout(() => router.push(redirectPath), 3000);
    }
  };

  useEffect(() => {
    handleCallback();
  }, [searchParams]);

  const handleRetry = () => {
    if (retryCount < MAX_RETRIES) {
      setRetryCount((prev) => prev + 1);
      handleCallback(true);
    } else {
      router.push('/login');
    }
  };

  const handleDismiss = () => {
    const redirectPath = oauthError ? getErrorRedirectPath(oauthError) : '/login';
    router.push(redirectPath);
  };

  return (
    <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 flex items-center justify-center px-4">
      <div className="max-w-md w-full">
        <div className="bg-white dark:bg-slate-800 rounded-3xl shadow-2xl p-8 border border-slate-100 dark:border-slate-700">
          {/* Logo/Brand */}
          <div className="flex justify-center mb-6">
            <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-blue-600 to-cyan-600 flex items-center justify-center">
              <svg className="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
          </div>

          {processing ? (
            <>
              <div className="text-center">
                <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-blue-100 dark:bg-blue-900/30 mb-4">
                  <div className="animate-spin rounded-full h-10 w-10 border-4 border-blue-600 border-t-transparent"></div>
                </div>
                <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100 mb-2">
                  {t('auth.google.callback.processing.title')}
                </h1>
                <p className="text-slate-600 dark:text-slate-300">
                  {t('auth.google.callback.processing.message')}
                </p>
              </div>
            </>
          ) : oauthError ? (
            <>
              <OAuthErrorDisplay
                error={oauthError}
                onRetry={retryCount < MAX_RETRIES ? handleRetry : undefined}
                onDismiss={handleDismiss}
              />
            </>
          ) : null}
        </div>
      </div>
    </main>
  );
}
