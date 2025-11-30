'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useLocale, useTranslations } from 'next-intl';
import { useAuth } from '@/hooks/useAuth';
import { api } from '@/lib/api';

export default function SettingsPage() {
  const router = useRouter();
  const locale = useLocale();
  const t = useTranslations('settings');
  const { user, isAuthenticated, loading } = useAuth();
  const [accountInfo, setAccountInfo] = useState<{
    hasLocalAuth: boolean;
    hasGoogleAuth: boolean;
    googleEmail?: string;
    authProvider: string;
  } | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isLinking, setIsLinking] = useState(false);

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.push('/login');
    }
  }, [loading, isAuthenticated, router]);

  useEffect(() => {
    const fetchAccountInfo = async () => {
      if (!isAuthenticated) return;

      try {
        setIsLoading(true);
        setError(null);
        const profile = await api.getMyProfile();
        
        // Determine auth methods based on profile data
        const hasGoogleAuth = !!profile.googleId;
        const hasLocalAuth = !!profile.hasPassword; // Assuming this field exists
        
        setAccountInfo({
          hasLocalAuth,
          hasGoogleAuth,
          googleEmail: profile.googleEmail,
          authProvider: profile.authProvider || 'LOCAL'
        });
      } catch (error) {
        console.error('Failed to fetch account info:', error);
        setError(t('errors.loadFailed'));
      } finally {
        setIsLoading(false);
      }
    };

    fetchAccountInfo();
  }, [isAuthenticated, t]);

  const handleLinkGoogle = async () => {
    try {
      setIsLinking(true);
      setError(null);
      
      // Get tenant slug from current URL or context
      const tenantSlug = window.location.hostname.split('.')[0];
      
      // Redirect to Google OAuth flow
      // The OAuth flow will handle account linking automatically if the email matches
      window.location.href = `/api/public/auth/google/authorize?tenant=${tenantSlug}`;
      
    } catch (error) {
      console.error('Failed to initiate Google linking:', error);
      setError(t('errors.linkFailed'));
      setIsLinking(false);
    }
  };

  if (loading || isLoading) {
    return (
      <main className="min-h-screen bg-slate-50 dark:bg-slate-950">
        <div className="mx-auto max-w-4xl px-4 md:px-6 lg:px-8 py-20">
          <div className="flex items-center justify-center py-20">
            <div className="text-center">
              <div className="inline-block animate-spin rounded-full h-16 w-16 border-4 border-blue-600 border-t-transparent"></div>
              <p className="mt-4 text-slate-600 dark:text-slate-300 font-medium">{t('loading')}</p>
            </div>
          </div>
        </div>
      </main>
    );
  }

  if (!isAuthenticated || !user) {
    return null;
  }

  return (
    <main className="min-h-screen bg-slate-50 dark:bg-slate-950">
      {/* Header */}
      <section className="bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800">
        <div className="mx-auto max-w-4xl px-4 md:px-6 lg:px-8 py-8">
          <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-100">
            {t('title')}
          </h1>
          <p className="text-slate-600 dark:text-slate-300 mt-1">{t('subtitle')}</p>
        </div>
      </section>

      {/* Main Content */}
      <section className="py-8">
        <div className="mx-auto max-w-4xl px-4 md:px-6 lg:px-8">
          {error && (
            <div className="mb-6 p-4 bg-red-50 dark:bg-red-950/50 border border-red-200 dark:border-red-900 rounded-lg">
              <div className="flex items-center gap-3">
                <svg className="w-5 h-5 text-red-600 dark:text-red-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <p className="text-red-600 dark:text-red-400 font-medium">{error}</p>
              </div>
            </div>
          )}

          {/* Linked Accounts Section */}
          <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
            <div className="px-6 py-4 border-b border-slate-200 dark:border-slate-700">
              <h2 className="text-lg font-bold text-slate-900 dark:text-slate-100">
                {t('linkedAccounts.title')}
              </h2>
              <p className="text-sm text-slate-600 dark:text-slate-300 mt-1">
                {t('linkedAccounts.subtitle')}
              </p>
            </div>

            <div className="p-6 space-y-4">
              {/* Email/Password Authentication */}
              <div className="flex items-center justify-between p-4 rounded-lg border border-slate-200 dark:border-slate-700">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                    <svg className="w-6 h-6 text-slate-600 dark:text-slate-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="font-semibold text-slate-900 dark:text-slate-100">
                      {t('linkedAccounts.email.title')}
                    </h3>
                    <p className="text-sm text-slate-600 dark:text-slate-300">
                      {user.email}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  {accountInfo?.hasLocalAuth ? (
                    <span className="px-3 py-1 rounded-full text-xs font-bold bg-green-100 dark:bg-green-950/50 text-green-700 dark:text-green-400">
                      {t('linkedAccounts.status.connected')}
                    </span>
                  ) : (
                    <span className="px-3 py-1 rounded-full text-xs font-bold bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400">
                      {t('linkedAccounts.status.notSet')}
                    </span>
                  )}
                </div>
              </div>

              {/* Google Authentication */}
              <div className="flex items-center justify-between p-4 rounded-lg border border-slate-200 dark:border-slate-700">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-full bg-white dark:bg-slate-700 flex items-center justify-center border border-slate-200 dark:border-slate-600">
                    <svg className="w-6 h-6" viewBox="0 0 24 24">
                      <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                      <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                      <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                      <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                    </svg>
                  </div>
                  <div>
                    <h3 className="font-semibold text-slate-900 dark:text-slate-100">
                      {t('linkedAccounts.google.title')}
                    </h3>
                    <p className="text-sm text-slate-600 dark:text-slate-300">
                      {accountInfo?.hasGoogleAuth 
                        ? accountInfo.googleEmail || t('linkedAccounts.google.connected')
                        : t('linkedAccounts.google.notConnected')
                      }
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  {accountInfo?.hasGoogleAuth ? (
                    <span className="px-3 py-1 rounded-full text-xs font-bold bg-green-100 dark:bg-green-950/50 text-green-700 dark:text-green-400">
                      {t('linkedAccounts.status.connected')}
                    </span>
                  ) : (
                    <button
                      onClick={handleLinkGoogle}
                      disabled={isLinking}
                      className="px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {isLinking ? t('linkedAccounts.google.linking') : t('linkedAccounts.google.link')}
                    </button>
                  )}
                </div>
              </div>

              {/* Info Box */}
              <div className="mt-6 p-4 bg-blue-50 dark:bg-blue-950/30 border border-blue-200 dark:border-blue-900/50 rounded-lg">
                <div className="flex gap-3">
                  <svg className="w-5 h-5 text-blue-600 dark:text-blue-400 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <div>
                    <h4 className="font-semibold text-blue-900 dark:text-blue-100 mb-1">
                      {t('linkedAccounts.info.title')}
                    </h4>
                    <p className="text-sm text-blue-700 dark:text-blue-300">
                      {t('linkedAccounts.info.description')}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}
