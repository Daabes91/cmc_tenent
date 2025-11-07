import {getRequestConfig} from 'next-intl/server';
import {notFound} from 'next/navigation';

// Can be imported from a shared config
export const locales = ['en', 'ar'] as const;
export type Locale = (typeof locales)[number];

const defaultLocale: Locale = 'en';

export default getRequestConfig(async ({locale}) => {
  const resolvedLocale = (locale ?? defaultLocale) as Locale;
  // Validate that the incoming `locale` parameter is valid
  if (!locales.includes(resolvedLocale)) notFound();

  return {
    locale: resolvedLocale,
    messages: (await import(`../messages/${resolvedLocale}.json`)).default
  };
});
