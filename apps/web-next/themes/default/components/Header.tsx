'use client';

import {Link, usePathname} from '@/navigation';
import {useState, useMemo, useRef, useEffect} from 'react';
import {Menu, X, ChevronDown, ShoppingCart} from 'lucide-react';
import {useAuth} from '@/hooks/useAuth';
import {useTranslations, useLocale} from 'next-intl';
import {api} from '@/lib/api';
import type {ClinicSettings} from '@/lib/types';
import {ThemeToggle} from '@/components/ui/ThemeToggle';
import {getBookingSectionUrl} from '@/utils/basePath';

export function Header() {
  const pathname = usePathname();
  const { user, isAuthenticated, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [mounted, setMounted] = useState(false);
  const [settings, setSettings] = useState<ClinicSettings | null>(null);
  const userMenuRef = useRef<HTMLDivElement>(null);
  const t = useTranslations('nav');
  const common = useTranslations('common');
  const locale = useLocale();

  // Prevent hydration mismatch by only showing auth-dependent UI after mount
  useEffect(() => {
    setMounted(true);
  }, []);

  // Fetch clinic settings for logo
  useEffect(() => {
    const loadSettings = async () => {
      try {
        const data = await api.getClinicSettings();
        setSettings(data);
      } catch (error) {
        console.error('Failed to load clinic settings:', error);
      }
    };
    loadSettings();
  }, []);

  const languageSwitchLocale = locale === 'ar' ? 'en' : 'ar';
  const normalizePath = (value: string | null) => {
    if (!value) return '/';
    const withoutLocale = value.replace(/^\/(ar|en)(?=\/|$)/, '');
    return withoutLocale === '' ? '/' : withoutLocale;
  };
  const currentPath = useMemo(() => normalizePath(pathname), [pathname]);
  const switchHref = useMemo(() => normalizePath(pathname), [pathname]);
  const isActive = (path: string) => currentPath === path;

  const navItems = useMemo(
    () => [
      { href: '/', label: t('home') },
      { href: '/services', label: t('services') },
      { href: '/doctors', label: t('doctors') },
      { href: '/blog', label: t('blog') }
    ],
    [t]
  );

  // Close user menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (userMenuRef.current && !userMenuRef.current.contains(event.target as Node)) {
        setUserMenuOpen(false);
      }
    };

    if (userMenuOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [userMenuOpen]);

  const scrollToBooking = () => {
    const bookingSection = document.getElementById('booking-section');
    if (bookingSection) {
      bookingSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } else {
      // If not on homepage, navigate to homepage
      const targetUrl = getBookingSectionUrl(locale);
      window.location.href = targetUrl;
    }
  };

  return (
    <header className="sticky top-0 z-[2147483648] bg-white/95 dark:bg-slate-900/95 backdrop-blur-md border-b border-slate-200 dark:border-slate-800 shadow-sm">
      <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8 h-20 flex items-center justify-between">
        {/* Logo */}
        <Link href="/" className="inline-flex items-center gap-3 group">
          {settings?.logoUrl ? (
            <div className="h-11 w-11 rounded-xl overflow-hidden shadow-lg group-hover:scale-105 transition-transform">
              <img
                src={settings.logoUrl}
                alt={settings.clinicName || common('clinicName')}
                className="w-full h-full object-cover"
              />
            </div>
          ) : (
            <div className="h-11 w-11 rounded-xl bg-gradient-to-br from-blue-600 to-cyan-600 flex items-center justify-center shadow-lg group-hover:scale-105 transition-transform">
              <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
          )}
          <div className="flex flex-col">
            <span className="font-bold text-lg text-slate-900 dark:text-slate-100 leading-tight">
              {settings?.clinicName || common('clinicName')}
            </span>
            <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
              {settings?.tagline || common('tagline')}
            </span>
          </div>
        </Link>

        {/* Desktop Navigation */}
        <nav className="hidden md:flex items-center gap-2">
          {/* Main Nav Items */}
          {navItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                isActive(item.href)
                  ? 'text-blue-700 dark:text-blue-400 bg-blue-50 dark:bg-blue-950'
                  : 'text-slate-700 dark:text-slate-300 hover:text-blue-700 dark:hover:text-blue-400 hover:bg-slate-50 dark:hover:bg-slate-800'
              }`}
            >
              {item.label}
            </Link>
          ))}

          {/* Language Switcher - Pill Toggle */}
          <div className="flex items-center rounded-lg border border-slate-200 bg-slate-50 dark:border-slate-700 dark:bg-slate-800 p-1">
            <Link
              href={switchHref}
              locale="en"
              className={`px-3 py-1.5 rounded-md text-xs font-semibold transition-all ${
                locale === 'en'
                  ? 'bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-100 shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
              }`}
            >
              EN
            </Link>
            <Link
              href={switchHref}
              locale="ar"
              className={`px-3 py-1.5 rounded-md text-xs font-semibold transition-all ${
                locale === 'ar'
                  ? 'bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-100 shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
              }`}
            >
              ع
            </Link>
          </div>

          {/* Cart (always visible; backend enforces e-commerce gating) */}
          <Link
            href="/cart"
            className="inline-flex items-center gap-2 rounded-xl border border-blue-200 bg-blue-50 px-3 py-2 text-sm font-semibold text-blue-700 shadow-sm transition hover:bg-blue-100 dark:border-blue-900 dark:bg-blue-950 dark:text-blue-100"
            aria-label="View cart"
          >
            <ShoppingCart className="h-5 w-5" />
            <span>Cart</span>
          </Link>

          {/* Theme Toggle */}
          <ThemeToggle />

          {/* Book Now Button */}
          <button
            onClick={scrollToBooking}
            className="ml-2 inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 text-white font-semibold text-sm hover:from-blue-700 hover:to-cyan-700 transition-all shadow-lg hover:shadow-xl transform hover:-translate-y-0.5"
          >
            {t('bookNow')}
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </button>

          {/* User Menu / Auth Buttons */}
          {!mounted ? (
            // Show loading state during hydration to prevent mismatch
            <div className="ml-2 flex items-center gap-2">
              <div className="px-4 py-2 rounded-lg text-sm font-semibold text-slate-700 dark:text-slate-300">
                {t('signIn')}
              </div>
              <div className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-slate-900 dark:bg-slate-700 text-white font-semibold text-sm">
                {t('getStarted')}
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </div>
            </div>
          ) : isAuthenticated ? (
            <div className="relative ml-2" ref={userMenuRef}>
              <button
                onClick={() => setUserMenuOpen(!userMenuOpen)}
                className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 transition-all"
              >
                {user?.profileImageUrl ? (
                  <div className="w-8 h-8 rounded-full overflow-hidden border-2 border-slate-200">
                    <img
                      src={user.profileImageUrl}
                      alt={user?.name || 'User'}
                      className="w-full h-full object-cover"
                    />
                  </div>
                ) : (
                  <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-600 to-cyan-600 flex items-center justify-center">
                    <span className="text-white text-xs font-bold">
                      {user?.name?.charAt(0).toUpperCase() || 'U'}
                    </span>
                  </div>
                )}
                <span className="text-sm font-medium text-slate-700 dark:text-slate-300 hidden lg:block">
                  {user?.name?.split(' ')[0] || 'User'}
                </span>
                <ChevronDown className={`w-4 h-4 text-slate-500 dark:text-slate-400 transition-transform ${userMenuOpen ? 'rotate-180' : ''}`} />
              </button>

              {/* User Dropdown Menu */}
              {userMenuOpen && (
                <div className="absolute right-0 mt-2 w-56 rounded-xl bg-white dark:bg-slate-800 shadow-xl border border-slate-200 dark:border-slate-700 py-2 animate-in fade-in slide-in-from-top-2 duration-200">
                  <div className="px-4 py-3 border-b border-slate-100 dark:border-slate-700">
                    <p className="text-sm font-semibold text-slate-900 dark:text-slate-100">{user?.name || 'User'}</p>
                    <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{user?.email || ''}</p>
                  </div>
                  <Link
                    href="/dashboard"
                    className="flex items-center gap-3 px-4 py-2.5 text-sm font-medium text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors"
                    onClick={() => setUserMenuOpen(false)}
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                    </svg>
                    {t('dashboard')}
                  </Link>
                  <button
                    onClick={() => {
                      logout();
                      setUserMenuOpen(false);
                    }}
                    className="w-full flex items-center gap-3 px-4 py-2.5 text-sm font-medium text-red-700 hover:bg-red-50 transition-colors"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                    {t('signOut')}
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="ml-2 flex items-center gap-2">
              <Link
                href="/login"
                className="px-4 py-2 rounded-lg text-sm font-semibold text-slate-700 dark:text-slate-300 hover:text-blue-700 dark:hover:text-blue-400 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all"
              >
                {t('signIn')}
              </Link>
              <Link
                href="/signup"
                className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-slate-900 dark:bg-slate-700 text-white font-semibold text-sm hover:bg-slate-800 dark:hover:bg-slate-600 transition-all shadow-lg hover:shadow-xl"
              >
                {t('getStarted')}
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </Link>
            </div>
          )}
        </nav>

        {/* Mobile Menu Button */}
        <button
          className="md:hidden inline-flex items-center justify-center h-11 w-11 rounded-xl border-2 border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle menu"
        >
          {mobileMenuOpen ? <X className="h-5 w-5 text-slate-700 dark:text-slate-300" /> : <Menu className="h-5 w-5 text-slate-700 dark:text-slate-300" />}
        </button>
      </div>

      {/* Mobile Navigation */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-lg">
          <div className="mx-auto max-w-7xl px-4 py-6 space-y-1">
            {navItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={`block px-4 py-3 rounded-xl text-sm font-semibold transition-all ${
                  isActive(item.href)
                    ? 'text-blue-700 dark:text-blue-400 bg-blue-50 dark:bg-blue-950'
                    : 'text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800'
                }`}
                onClick={() => setMobileMenuOpen(false)}
              >
                {item.label}
              </Link>
            ))}

            {/* Language Switcher - Mobile */}
            <div className="flex items-center justify-center rounded-lg border border-slate-200 bg-slate-50 dark:border-slate-700 dark:bg-slate-800 p-1">
              <Link
                href={switchHref}
                locale="en"
                className={`flex-1 px-4 py-2.5 rounded-md text-sm font-semibold transition-all text-center ${
                  locale === 'en'
                    ? 'bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-100 shadow-sm'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
                }`}
                onClick={() => setMobileMenuOpen(false)}
              >
                English
              </Link>
              <Link
                href={switchHref}
                locale="ar"
                className={`flex-1 px-4 py-2.5 rounded-md text-sm font-semibold transition-all text-center ${
                  locale === 'ar'
                    ? 'bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-100 shadow-sm'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
                }`}
                onClick={() => setMobileMenuOpen(false)}
              >
                العربية
              </Link>
            </div>

            {/* Theme Toggle - Mobile */}
            <div className="flex justify-center">
              <ThemeToggle />
            </div>

            <button
              onClick={() => {
                scrollToBooking();
                setMobileMenuOpen(false);
              }}
              className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 text-white font-semibold text-sm hover:from-blue-700 hover:to-cyan-700 transition-all shadow-lg"
            >
              {t('bookNow')}
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </button>

            {!mounted ? (
              // Show loading state during hydration to prevent mismatch
              <div className="pt-3 border-t border-slate-200 dark:border-slate-800 mt-3 space-y-2">
                <div className="block px-4 py-3 rounded-xl text-sm font-semibold text-slate-700 dark:text-slate-300 text-center">
                  {t('signIn')}
                </div>
                <div className="block px-4 py-3 rounded-xl bg-slate-900 dark:bg-slate-700 text-white font-semibold text-sm text-center">
                  {t('getStarted')}
                </div>
              </div>
            ) : isAuthenticated ? (
              <>
                <div className="pt-3 border-t border-slate-200 dark:border-slate-800 mt-3">
                  <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-slate-50 dark:bg-slate-800 mb-2">
                    {user?.profileImageUrl ? (
                      <div className="w-10 h-10 rounded-full overflow-hidden border-2 border-slate-200">
                        <img
                          src={user.profileImageUrl}
                          alt={user?.name || 'User'}
                          className="w-full h-full object-cover"
                        />
                      </div>
                    ) : (
                      <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-600 to-cyan-600 flex items-center justify-center">
                        <span className="text-white text-sm font-bold">
                          {user?.name?.charAt(0).toUpperCase() || 'U'}
                        </span>
                      </div>
                    )}
                    <div>
                      <p className="text-sm font-semibold text-slate-900 dark:text-slate-100">{user?.name || 'User'}</p>
                      <p className="text-xs text-slate-500 dark:text-slate-400">{user?.email || ''}</p>
                    </div>
                  </div>

                  <Link
                    href="/dashboard"
                    className="block px-4 py-3 rounded-xl text-sm font-semibold text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-all mb-2"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    {t('myDashboard')}
                  </Link>

                  <button
                    onClick={() => {
                      logout();
                      setMobileMenuOpen(false);
                    }}
                    className="w-full px-4 py-3 rounded-xl text-sm font-semibold text-red-700 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950 transition-all text-center"
                  >
                    {t('signOut')}
                  </button>
                </div>
              </>
            ) : (
              <div className="pt-3 border-t border-slate-200 dark:border-slate-800 mt-3 space-y-2">
                <Link
                  href="/login"
                  className="block px-4 py-3 rounded-xl text-sm font-semibold text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all text-center"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  {t('signIn')}
                </Link>
                <Link
                  href="/signup"
                  className="block px-4 py-3 rounded-xl bg-slate-900 dark:bg-slate-700 text-white font-semibold text-sm hover:bg-slate-800 dark:hover:bg-slate-600 transition-all shadow-lg text-center"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  {t('getStarted')}
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
