import {createNavigation} from 'next-intl/navigation';
import {locales} from './i18n/request';

export const localePrefix = 'always';
export const defaultLocale = 'en';

export const pathnames = {
  '/': '/',
  '/services': '/services',
  '/doctors': '/doctors',
  '/appointments': '/appointments',
  '/dashboard': '/dashboard',
  '/login': '/login',
  '/signup': '/signup',
  '/products': '/products'
} as const;

export const {Link, redirect, usePathname, useRouter} = createNavigation({
  locales,
  localePrefix,
  defaultLocale
});
