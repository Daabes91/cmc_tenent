import Link from 'next/link';
import { getLocale, getTranslations } from 'next-intl/server';
import { getClinicSettingsServer } from '@/lib/server-api';
import { getBookingSectionPath } from '@/utils/basePath';

export default async function NotFound() {
  const locale = await getLocale();
  const t = await getTranslations('notFound');
  const clinicSettings = await getClinicSettingsServer();
  const supportPhone = clinicSettings?.phone || '+000000000';
  const supportEmail = clinicSettings?.email || 'support@example.com';

  const homeHref = `/${locale}`;
  const servicesHref = `/${locale}/services`;
  const appointmentsHref = `/${locale}/appointments`;
  const bookingHref = getBookingSectionPath(locale);

  return (
    <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
      <div className="mx-auto flex max-w-5xl flex-col items-center px-4 py-20 text-center md:px-6 lg:px-8">
        <div className="inline-flex items-center justify-center rounded-full bg-blue-100 p-4 text-blue-600 dark:bg-blue-900/40 dark:text-blue-300">
          <svg className="h-10 w-10" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
          </svg>
        </div>
        <p className="mt-8 text-sm font-semibold uppercase tracking-widest text-blue-600 dark:text-blue-300">
          404
        </p>
        <h1 className="mt-4 text-4xl font-extrabold leading-tight text-slate-900 dark:text-slate-50 md:text-5xl">
          {t('title')}
        </h1>
        <p className="mt-4 max-w-2xl text-lg text-slate-600 dark:text-slate-300">
          {t('description')}
        </p>

        <div className="mt-10 grid w-full gap-4 md:grid-cols-3">
          <Link
            href={homeHref}
            className="rounded-2xl border border-slate-200 bg-white px-6 py-5 text-left shadow-sm transition hover:-translate-y-1 hover:border-blue-200 hover:shadow-lg dark:border-slate-800 dark:bg-slate-900/70"
          >
            <p className="text-sm font-semibold text-blue-600 dark:text-blue-300">{t('links.home.label')}</p>
            <p className="mt-1 text-slate-600 dark:text-slate-300">{t('links.home.description')}</p>
          </Link>
          <Link
            href={servicesHref}
            className="rounded-2xl border border-slate-200 bg-white px-6 py-5 text-left shadow-sm transition hover:-translate-y-1 hover:border-blue-200 hover:shadow-lg dark:border-slate-800 dark:bg-slate-900/70"
          >
            <p className="text-sm font-semibold text-blue-600 dark:text-blue-300">{t('links.services.label')}</p>
            <p className="mt-1 text-slate-600 dark:text-slate-300">{t('links.services.description')}</p>
          </Link>
          <Link
            href={appointmentsHref}
            className="rounded-2xl border border-slate-200 bg-white px-6 py-5 text-left shadow-sm transition hover:-translate-y-1 hover:border-blue-200 hover:shadow-lg dark:border-slate-800 dark:bg-slate-900/70"
          >
            <p className="text-sm font-semibold text-blue-600 dark:text-blue-300">{t('links.appointments.label')}</p>
            <p className="mt-1 text-slate-600 dark:text-slate-300">{t('links.appointments.description')}</p>
          </Link>
        </div>

        <div className="mt-12 w-full rounded-3xl border border-blue-100 bg-gradient-to-br from-blue-50 to-cyan-50 p-8 text-left shadow-lg dark:border-blue-900/40 dark:from-blue-950/40 dark:to-cyan-950/30">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">{t('cta.title')}</h2>
          <p className="mt-3 text-slate-600 dark:text-slate-300">{t('cta.description')}</p>
          <div className="mt-6 flex flex-col gap-3 text-slate-700 dark:text-slate-200">
            <p>
              {t('support.phone', { phone: supportPhone })}
            </p>
            <p>
              {t('support.email', { email: supportEmail })}
            </p>
          </div>
          <div className="mt-8 flex flex-wrap gap-4">
            <Link
              href={homeHref}
              className="inline-flex items-center justify-center rounded-xl border border-blue-200 bg-white px-6 py-3 font-semibold text-blue-700 transition hover:border-blue-300 hover:bg-blue-50 dark:border-blue-900/60 dark:bg-slate-900/60 dark:text-blue-200"
            >
              {t('cta.home')}
            </Link>
            <Link
              href={bookingHref}
              className="inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 px-6 py-3 font-semibold text-white shadow-lg transition hover:from-blue-700 hover:to-cyan-700"
            >
              {t('cta.book')}
            </Link>
          </div>
        </div>
      </div>
    </main>
  );
}
