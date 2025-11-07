import {redirect} from 'next/navigation';
import {locales, type Locale} from '@/i18n/request';

export default function IndexRedirect() {
  const defaultLocale =
    locales.includes('en' as Locale) ? 'en' : (locales[0] as Locale);

  return redirect(`/${defaultLocale}`);
}
