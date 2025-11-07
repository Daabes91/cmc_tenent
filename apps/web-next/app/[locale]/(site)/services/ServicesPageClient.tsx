'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useLocale, useTranslations } from 'next-intl';
import { api } from '@/lib/api';
import type { Service } from '@/lib/types';
import { getBookingSectionPath } from '@/utils/basePath';

const serviceIcons: Record<string, JSX.Element> = {
  cleaning: (
    <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  ),
  whitening: (
    <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
    </svg>
  ),
  orthodontics: (
    <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
    </svg>
  ),
  implants: (
    <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
    </svg>
  ),
};

const defaultIcon = (
  <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
);

export default function ServicesPageClient() {
  const locale = useLocale();
  const t = useTranslations('servicesPage');
  const [services, setServices] = useState<Service[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const bookingSectionPath = getBookingSectionPath(locale);

  useEffect(() => {
    const fetchServices = async () => {
      try {
        const data = await api.getServices(locale);
        setServices(data);
      } catch (err) {
        console.error('[ServicesPage] Failed to load services:', err);
        setError(t('errors.load'));
      } finally {
        setLoading(false);
      }
    };

    fetchServices();
  }, [locale, t]);

  if (loading) {
    return (
      <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8 py-20">
          <div className="flex items-center justify-center py-20">
            <div className="text-center">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-blue-600 border-t-transparent"></div>
              <p className="mt-4 text-slate-600 dark:text-slate-300 font-medium">{t('loading')}</p>
            </div>
          </div>
        </div>
      </main>
    );
  }

  if (error) {
    return (
      <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8 py-20">
          <div className="p-6 rounded-2xl bg-red-50 dark:bg-red-950/50 border-2 border-red-200 dark:border-red-800 text-red-700 dark:text-red-300">
            <div className="flex items-center gap-3">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span className="font-semibold">{error}</span>
            </div>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950">
      {/* Header Section */}
      <section className="py-16 bg-gradient-to-r from-blue-600 to-cyan-600 dark:from-blue-800/80 dark:via-blue-900/80 dark:to-slate-900">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto">
            <h1 className="text-4xl md:text-5xl font-bold text-white">{t('header.title')}</h1>
            <p className="text-blue-50 mt-4 text-lg">{t('header.subtitle')}</p>
          </div>
        </div>
      </section>

      {/* Services Grid */}
      <section className="py-16">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          {services.length === 0 ? (
            <div className="max-w-2xl mx-auto p-12 rounded-2xl bg-white dark:bg-slate-800 border-2 border-dashed border-slate-200 dark:border-slate-700 text-center">
              <svg className="w-16 h-16 mx-auto text-slate-300 dark:text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
              </svg>
              <p className="text-slate-600 dark:text-slate-300 mt-4 text-lg font-medium">{t('empty.title')}</p>
              <p className="text-slate-500 dark:text-slate-400 mt-2">{t('empty.description')}</p>
            </div>
          ) : (
            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-8">
              {services.map((service) => {
                const icon = serviceIcons[service.slug] || defaultIcon;

                return (
                  <div
                    key={service.slug}
                    className="group p-8 rounded-2xl bg-white dark:bg-slate-800 hover:shadow-2xl transition-all duration-300 border border-slate-100 dark:border-slate-700 hover:border-blue-200 dark:hover:border-blue-700"
                  >
                    <div className="w-16 h-16 rounded-xl bg-gradient-to-br from-blue-600 to-cyan-600 dark:from-blue-500 dark:to-cyan-500 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform">
                      {icon}
                    </div>
                    <h3 className="text-xl font-bold text-slate-900 dark:text-slate-100 mb-3">{service.name}</h3>
                    {service.summary && (
                      <p className="text-slate-600 dark:text-slate-300 leading-relaxed">{service.summary}</p>
                    )}
                    <Link
                      href={bookingSectionPath}
                      locale={false}
                      className="mt-6 inline-flex items-center text-blue-700 dark:text-blue-400 font-semibold hover:text-blue-800 dark:hover:text-blue-300 transition-colors group/link"
                    >
                      {t('actions.bookService')}
                      <svg className="w-4 h-4 ml-2 group-hover/link:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                      </svg>
                    </Link>
                  </div>
                );
              })}
            </div>
          )}

          {/* CTA Section */}
          <div className="mt-16 text-center p-12 rounded-2xl bg-gradient-to-br from-blue-50 to-cyan-50 dark:from-blue-950/30 dark:to-cyan-950/30 border border-blue-100 dark:border-blue-900/50">
            <h2 className="text-3xl font-bold text-slate-900 dark:text-slate-100 mb-4">{t('cta.title')}</h2>
            <p className="text-slate-600 dark:text-slate-300 text-lg mb-8 max-w-2xl mx-auto">
              {t('cta.subtitle')}
            </p>
            <Link
              href={bookingSectionPath}
              locale={false}
              className="inline-flex items-center justify-center px-8 py-4 rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 text-white font-semibold text-lg hover:from-blue-700 hover:to-cyan-700 transition-all shadow-lg hover:shadow-xl transform hover:-translate-y-0.5"
            >
              {t('cta.button')}
              <svg className="w-5 h-5 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </Link>
          </div>
        </div>
      </section>
    </main>
  );
}
