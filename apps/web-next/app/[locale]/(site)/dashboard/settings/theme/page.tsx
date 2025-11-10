'use client';

import { useState, useEffect } from 'react';
import { useTranslations } from 'next-intl';

interface Theme {
  id: string;
  key: string;
  name: string;
}

interface TenantInfo {
  slug: string;
  themeId: string | null;
}

export default function ThemeSettingsPage() {
  const t = useTranslations('themeSettings');
  const [themes, setThemes] = useState<Theme[]>([]);
  const [tenant, setTenant] = useState<TenantInfo | null>(null);
  const [selectedThemeId, setSelectedThemeId] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const loadData = async () => {
      try {
        setInitialLoading(true);
        setError(null);

        // Load available themes
        const themesResponse = await fetch('/api/themes');
        if (!themesResponse.ok) {
          throw new Error('Failed to load themes');
        }
        const themesData = await themesResponse.json();
        setThemes(themesData);

        // Load current tenant info
        const tenantResponse = await fetch('/api/internal/tenant');
        if (!tenantResponse.ok) {
          throw new Error('Failed to load tenant information');
        }
        const tenantData = await tenantResponse.json();
        setTenant(tenantData);
        setSelectedThemeId(tenantData.themeId || '');
      } catch (err) {
        console.error('Failed to load theme settings:', err);
        setError(err instanceof Error ? err.message : 'Failed to load theme settings');
      } finally {
        setInitialLoading(false);
      }
    };

    loadData();
  }, []);

  const handleSave = async () => {
    if (!tenant || !selectedThemeId) return;

    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const response = await fetch(`/api/tenants/${tenant.slug}/theme`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ themeId: selectedThemeId }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to update theme');
      }

      setSuccess(true);

      // Reload to apply new theme
      setTimeout(() => {
        window.location.reload();
      }, 1000);
    } catch (err) {
      console.error('Failed to update theme:', err);
      setError(err instanceof Error ? err.message : 'Failed to update theme');
      setLoading(false);
    }
  };

  if (initialLoading) {
    return (
      <main className="min-h-screen bg-slate-50 dark:bg-slate-950">
        <div className="mx-auto max-w-4xl px-4 md:px-6 lg:px-8 py-20">
          <div className="flex items-center justify-center py-20">
            <div className="text-center">
              <div className="inline-block animate-spin rounded-full h-16 w-16 border-4 border-blue-600 border-t-transparent"></div>
              <p className="mt-4 text-slate-600 dark:text-slate-300 font-medium">Loading theme settings...</p>
            </div>
          </div>
        </div>
      </main>
    );
  }

  const hasChanges = tenant?.themeId !== selectedThemeId;

  return (
    <main className="min-h-screen bg-slate-50 dark:bg-slate-950">
      {/* Header */}
      <section className="bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800">
        <div className="mx-auto max-w-4xl px-4 md:px-6 lg:px-8 py-8">
          <div>
            <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-100">Theme Settings</h1>
            <p className="text-slate-600 dark:text-slate-300 mt-1">
              Choose a theme for your website. Changes take effect immediately.
            </p>
          </div>
        </div>
      </section>

      {/* Main Content */}
      <section className="py-8">
        <div className="mx-auto max-w-4xl px-4 md:px-6 lg:px-8">
          <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
            <div className="px-6 py-4 border-b border-slate-200 dark:border-slate-700">
              <h2 className="text-lg font-bold text-slate-900 dark:text-slate-100">Select Theme</h2>
              <p className="text-sm text-slate-600 dark:text-slate-300 mt-1">
                Choose from available themes to customize your website appearance
              </p>
            </div>

            <div className="p-6">
              {/* Error Message */}
              {error && (
                <div className="mb-6 p-4 rounded-lg bg-red-50 dark:bg-red-950/50 border border-red-200 dark:border-red-900/50">
                  <div className="flex items-start gap-3">
                    <svg
                      className="w-5 h-5 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                      />
                    </svg>
                    <div>
                      <p className="text-sm font-medium text-red-800 dark:text-red-200">Error</p>
                      <p className="text-sm text-red-700 dark:text-red-300 mt-1">{error}</p>
                    </div>
                  </div>
                </div>
              )}

              {/* Success Message */}
              {success && (
                <div className="mb-6 p-4 rounded-lg bg-green-50 dark:bg-green-950/50 border border-green-200 dark:border-green-900/50">
                  <div className="flex items-start gap-3">
                    <svg
                      className="w-5 h-5 text-green-600 dark:text-green-400 flex-shrink-0 mt-0.5"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                      />
                    </svg>
                    <div>
                      <p className="text-sm font-medium text-green-800 dark:text-green-200">Success</p>
                      <p className="text-sm text-green-700 dark:text-green-300 mt-1">
                        Theme updated successfully. Reloading page...
                      </p>
                    </div>
                  </div>
                </div>
              )}

              {/* Theme Selection */}
              <div className="space-y-4">
                <div>
                  <label
                    htmlFor="theme-select"
                    className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2"
                  >
                    Available Themes
                  </label>
                  <select
                    id="theme-select"
                    value={selectedThemeId}
                    onChange={(e) => setSelectedThemeId(e.target.value)}
                    disabled={loading || themes.length === 0}
                    className="w-full px-4 py-3 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    <option value="">-- Select a theme --</option>
                    {themes.map((theme) => (
                      <option key={theme.id} value={theme.id}>
                        {theme.name}
                      </option>
                    ))}
                  </select>
                  {themes.length === 0 && (
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-2">
                      No themes available at the moment
                    </p>
                  )}
                </div>

                {/* Current Theme Info */}
                {tenant?.themeId && (
                  <div className="p-4 rounded-lg bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700">
                    <p className="text-sm text-slate-600 dark:text-slate-300">
                      <span className="font-medium">Current theme:</span>{' '}
                      {themes.find((t) => t.id === tenant.themeId)?.name || 'Unknown'}
                    </p>
                  </div>
                )}

                {/* Action Buttons */}
                <div className="flex items-center gap-3 pt-4">
                  <button
                    onClick={handleSave}
                    disabled={loading || !selectedThemeId || !hasChanges}
                    className="px-6 py-3 bg-gradient-to-r from-blue-600 to-cyan-600 text-white font-semibold rounded-lg hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:shadow-none"
                  >
                    {loading ? (
                      <span className="flex items-center gap-2">
                        <svg
                          className="animate-spin h-5 w-5"
                          fill="none"
                          viewBox="0 0 24 24"
                        >
                          <circle
                            className="opacity-25"
                            cx="12"
                            cy="12"
                            r="10"
                            stroke="currentColor"
                            strokeWidth="4"
                          />
                          <path
                            className="opacity-75"
                            fill="currentColor"
                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                          />
                        </svg>
                        Saving...
                      </span>
                    ) : (
                      'Save Theme'
                    )}
                  </button>

                  {hasChanges && (
                    <button
                      onClick={() => setSelectedThemeId(tenant?.themeId || '')}
                      disabled={loading}
                      className="px-6 py-3 bg-slate-200 dark:bg-slate-700 text-slate-700 dark:text-slate-300 font-semibold rounded-lg hover:bg-slate-300 dark:hover:bg-slate-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      Cancel
                    </button>
                  )}
                </div>

                {/* Help Text */}
                <div className="pt-4 border-t border-slate-200 dark:border-slate-700">
                  <div className="flex items-start gap-3">
                    <svg
                      className="w-5 h-5 text-blue-600 dark:text-blue-400 flex-shrink-0 mt-0.5"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                      />
                    </svg>
                    <div className="text-sm text-slate-600 dark:text-slate-300">
                      <p className="font-medium mb-1">About Themes</p>
                      <p>
                        Themes control the overall look and feel of your website, including layout, colors, and
                        components. After saving, the page will reload to apply your new theme.
                      </p>
                    </div>
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
